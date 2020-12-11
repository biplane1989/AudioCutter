package com.example.waveform.soundfile;

import java.io.File;

class CheapWAV extends CheapSoundFile {
    public static Factory getFactory() {
        return new Factory() {
            @Override
            public CheapSoundFile create() {
                return new CheapWAV();
            }

            @Override
            public String[] getSupportedExtensions() {
                return new String[]{"wav"};
            }
        };
    }

    // Member variables containing frame info
    private int mNumFrames;
    private int[] mFrameGains;
    private int mFileSize;
    private int mSampleRate;
    private int mChannels;

    public CheapWAV() {
    }

    public int getNumFrames() {
        return mNumFrames;
    }

    public int getSamplesPerFrame() {
        return 1024;
    }

    public int[] getFrameGains() {
        return mFrameGains;
    }

    public int getFileSizeBytes() {
        return mFileSize;
    }

    public int getAvgBitrateKbps() {
        return mSampleRate * mChannels * 2 / 1024;
    }

    public int getSampleRate() {
        return mSampleRate;
    }

    public int getChannels() {
        return mChannels;
    }

    public String getFiletype() {
        return "WAV";
    }

    @Override
    public void readFile(File inputFile) throws java.io.IOException {
        super.readFile(inputFile);
        mFileSize = (int) mInputFile.length();

        if (mFileSize < 128) {
            throw new java.io.IOException("File too small to parse");
        }
        try {
            WavFile wavFile = WavFile.openWavFile(inputFile);
            mNumFrames = (int) (wavFile.getNumFrames() / getSamplesPerFrame());
            mFrameGains = new int[mNumFrames];
            mSampleRate = (int) wavFile.getSampleRate();
            mChannels = wavFile.getNumChannels();

            int gain, value;
            int[] buffer = new int[getSamplesPerFrame()];
            for (int i = 0; i < mNumFrames; i++) {
                gain = -1;
                wavFile.readFrames(buffer, getSamplesPerFrame());
                for (int j = 0; j < getSamplesPerFrame(); j++) {
                    value = buffer[j];
                    if (gain < value) {
                        gain = value;
                    }
                }
                mFrameGains[i] = (int) Math.sqrt(gain);
                if (mProgressListener != null) {
                    boolean keepGoing = mProgressListener.reportProgress(i * 1.0 / mFrameGains.length);
                    if (!keepGoing) {
                        break;
                    }
                }
            }
            if (wavFile != null) {
                wavFile.close();
            }
        } catch (WavFileException e) {

        }
    }
}
