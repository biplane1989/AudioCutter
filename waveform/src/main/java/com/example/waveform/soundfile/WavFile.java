package com.example.waveform.soundfile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

class WavFile {
    private enum IOState {
        READING, WRITING, CLOSED
    }

    private final static int BUFFER_SIZE = 4096;

    private final static int FMT_CHUNK_ID = 0x20746D66;
    private final static int DATA_CHUNK_ID = 0x61746164;
    private final static int RIFF_CHUNK_ID = 0x46464952;
    private final static int RIFF_TYPE_ID = 0x45564157;

    private File file;
    private IOState ioState;
    private int bytesPerSample;
    private long numFrames;
    private FileOutputStream oStream;
    private FileInputStream iStream;
    private float floatScale;
    private float floatOffset;
    private boolean wordAlignAdjust;

    // Wav Header
    private int numChannels;
    private long sampleRate;

    private int blockAlign;
    private int validBits;

    private byte[] buffer;
    private int bufferPointer;
    private int bytesRead;
    private long frameCounter;
    private long fileSize;

    private WavFile() {
        buffer = new byte[BUFFER_SIZE];
    }

    public int getNumChannels() {
        return numChannels;
    }

    public long getNumFrames() {
        return numFrames;
    }

    public long getFramesRemaining() {
        return numFrames - frameCounter;
    }

    public long getSampleRate() {
        return sampleRate;
    }

    public int getValidBits() {
        return validBits;
    }

    public long getDuration() {
        return getNumFrames() / getSampleRate();
    }

    public long getFileSize() {
        return fileSize;
    }

    public static WavFile openWavFile(File file) throws IOException, WavFileException {
        WavFile wavFile = new WavFile();
        wavFile.file = file;

        wavFile.iStream = new FileInputStream(file);

        int bytesRead = wavFile.iStream.read(wavFile.buffer, 0, 12);
        if (bytesRead != 12) throw new WavFileException("Not enough wav file bytes for header");
        long riffChunkID = getLE(wavFile.buffer, 0, 4);
        long chunkSize = getLE(wavFile.buffer, 4, 4);
        long riffTypeID = getLE(wavFile.buffer, 8, 4);

        if (riffChunkID != RIFF_CHUNK_ID) {
            throw new WavFileException("Invalid Wav header data, incorrect riff chunk ID");
        }
        if (riffTypeID != RIFF_TYPE_ID) {
            throw new WavFileException("Invalid Wav Header data, incorrect riff type ID");
        }
        if (file.length() != chunkSize + 8) {
            throw new WavFileException("Header chunk size (" + chunkSize + ") does not match file size (" + file.length() + ")");
        }
        wavFile.fileSize = chunkSize;
        boolean foundFormat = false;
        boolean foundData = false;

        while (true) {
            bytesRead = wavFile.iStream.read(wavFile.buffer, 0, 8);
            if (bytesRead == -1)
                throw new WavFileException("Reached end of file without finding format chunk");
            if (bytesRead != 8) throw new WavFileException("Could not read chunk header");

            long chunkID = getLE(wavFile.buffer, 0, 4);
            chunkSize = getLE(wavFile.buffer, 4, 4);

            long numChunkBytes = (chunkSize % 2 == 1) ? chunkSize + 1 : chunkSize;
            if (chunkID == FMT_CHUNK_ID) {
                foundFormat = true;

                bytesRead = wavFile.iStream.read(wavFile.buffer, 0, 16);
                int compressionCode = (int) getLE(wavFile.buffer, 0, 2);
                if (compressionCode != 1) {
                    throw new WavFileException("Compression Code " + compressionCode + " not supported");
                }
                wavFile.numChannels = (int) getLE(wavFile.buffer, 2, 2);
                wavFile.sampleRate = getLE(wavFile.buffer, 4, 4);
                wavFile.blockAlign = (int) getLE(wavFile.buffer, 12, 2);
                wavFile.validBits = (int) getLE(wavFile.buffer, 14, 2);
                if (wavFile.numChannels == 0) {
                    throw new WavFileException("Number of channels specified in header is equal to zero");
                }
                if (wavFile.blockAlign == 0) {
                    throw new WavFileException("Block Align specified in header is equal to zero");
                }
                if (wavFile.validBits < 2)
                    throw new WavFileException("Valid Bits specified in header is less than 2");
                if (wavFile.validBits > 64)
                    throw new WavFileException("Valid Bits specified in header is greater than 64, this is greater than a long can hold");
                wavFile.bytesPerSample = (wavFile.validBits + 7) / 8;
                if (wavFile.bytesPerSample * wavFile.numChannels != wavFile.blockAlign) {
                    throw new WavFileException("Block Align does not agree with bytes required for validBits and number of channels");
                }
                numChunkBytes -= 16;
                if (numChunkBytes > 0) wavFile.iStream.skip(numChunkBytes);
            } else if (chunkID == DATA_CHUNK_ID) {
                if (!foundFormat)
                    throw new WavFileException("Data chunk found before Format chunk");
                if (chunkSize % wavFile.blockAlign != 0)
                    throw new WavFileException("Data Chunk size is not multiple of Block Align");

                wavFile.numFrames = chunkSize / wavFile.blockAlign;
                foundData = true;
                break;
            } else {
                wavFile.iStream.skip(numChunkBytes);
            }
        }

        if (!foundData) throw new WavFileException("Did not find a data chunk");
        if (wavFile.validBits > 8) {
            wavFile.floatOffset = 0;
            wavFile.floatScale = 1 << (wavFile.validBits - 1);
        } else {
            wavFile.floatOffset = -1;
            wavFile.floatScale = 0.5f * ((1 << wavFile.validBits) - 1);
        }
        wavFile.bufferPointer = 0;
        wavFile.bytesRead = 0;
        wavFile.frameCounter = 0;
        wavFile.ioState = IOState.READING;
        return wavFile;
    }

    private static long getLE(byte[] buffer, int pos, int numBytes) {
        numBytes--;
        pos += numBytes;
        long val = buffer[pos] & 0xFF;
        for (int b = 0; b < numBytes; b++) {
            val = (val << 8) + (buffer[--pos] & 0xFF);
        }
        return val;
    }

    private long readSample() throws IOException, WavFileException {
        long val = 0;
        for (int b = 0; b < bytesPerSample; b++) {
            if (bufferPointer == bytesRead) {
                int read = iStream.read(buffer, 0, BUFFER_SIZE);
                if (read == -1) throw new WavFileException("Not enough data available");
                bytesRead = read;
                bufferPointer = 0;
            }
            int v = buffer[bufferPointer];
            if (b < bytesPerSample - 1 || bytesPerSample == 1) v &= 0xFF;
            val += v << (b * 8);
            bufferPointer++;
        }
        return val;
    }

    public int readFrames(float[] sampleBuffer, int numFramesToRead) throws IOException, WavFileException {
        return readFramesInternal(sampleBuffer, 0, numFramesToRead);
    }

    public int readFrames(int[] sampleBuffer, int numFramesToRead) throws IOException, WavFileException {
        return readFramesInternal(sampleBuffer, 0, numFramesToRead);
    }

    private int readFramesInternal(float[] sampleBuffer, int offset, int numFramesToRead) throws IOException, WavFileException {
        if (ioState != IOState.READING) throw new IOException("Cannot read from WavFile instance");
        for (int f = 0; f < numFramesToRead; f++) {
            if (frameCounter == numFrames) return f;

            for (int c = 0; c < numChannels; c++) {
                sampleBuffer[offset] = floatOffset + (float) readSample() / floatScale;
                offset++;
            }
            frameCounter++;
        }
        return numFramesToRead;
    }

    private int readFramesInternal(int[] sampleBuffer, int offset, int numFramesToRead) throws IOException, WavFileException {
        if (ioState != IOState.READING) throw new IOException("Cannot read from WavFile instance");

        for (int f = 0; f < numFramesToRead; f++) {
            if (frameCounter == numFrames) return f;

            for (int c = 0; c < numChannels; c++) {
                sampleBuffer[offset] = (int) readSample();
                offset++;
            }

            frameCounter++;
        }

        return numFramesToRead;
    }

    public void close() throws IOException {
        if (iStream != null) {
            iStream.close();
            iStream = null;
        }
        if (oStream != null) {
            if (bufferPointer > 0) oStream.write(buffer, 0, bufferPointer);
        }
        if (wordAlignAdjust) oStream.write(0);

        oStream.close();
        oStream = null;
    }


}
