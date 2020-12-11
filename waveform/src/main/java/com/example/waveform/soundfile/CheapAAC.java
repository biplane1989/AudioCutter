package com.example.waveform.soundfile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

class CheapAAC extends CheapSoundFile {
    public static Factory getFactory() {
        return new Factory() {
            @Override
            public CheapSoundFile create() {
                return new CheapAAC();
            }

            @Override
            public String[] getSupportedExtensions() {
                return new String[]{"aac", "m4a"};
            }
        };
    }

    class Atom {
        public int start;
        public int len;
        public byte[] data;
    }

    public static final int kDINF = 0x64696e66;
    public static final int kFTYP = 0x66747970;
    public static final int kHDLR = 0x68646c72;
    public static final int kMDAT = 0x6d646174;
    public static final int kMDHD = 0x6d646864;
    public static final int kMDIA = 0x6d646961;
    public static final int kMINF = 0x6d696e66;
    public static final int kMOOV = 0x6d6f6f76;
    public static final int kMP4A = 0x6d703461;
    public static final int kMVHD = 0x6d766864;
    public static final int kSMHD = 0x736d6864;
    public static final int kSTBL = 0x7374626c;
    public static final int kSTCO = 0x7374636f;
    public static final int kSTSC = 0x73747363;
    public static final int kSTSD = 0x73747364;
    public static final int kSTSZ = 0x7374737a;
    public static final int kSTTS = 0x73747473;
    public static final int kTKHD = 0x746b6864;
    public static final int kTRAK = 0x7472616b;

    public static final int[] kRequiredAtoms = {
            kDINF,
            kHDLR,
            kMDHD,
            kMDIA,
            kMINF,
            kMOOV,
            kMVHD,
            kSMHD,
            kSTBL,
            kSTSD,
            kSTSZ,
            kSTTS,
            kTKHD,
            kTRAK,
    };

    public static final int[] kSaveDataAtoms = {
            kDINF,
            kHDLR,
            kMDHD,
            kMVHD,
            kSMHD,
            kTKHD,
            kSTSD,
    };

    private int mNumFrames;
    private int[] mFrameLens;
    private int[] mFrameGains;
    private int mFileSize;
    private HashMap<Integer, Atom> mAtomMap;

    private int mBitrate;
    private int mSampleRate;
    private int mChannels;
    private int mSamplesPerFrame;

    private int mOffset;
    private int mMinGain;
    private int mMaxGain;
    private int mDataOffset;
    private int mDataLength;

    public CheapAAC() {
    }

    public int getNumFrames() {
        return mNumFrames;
    }

    public int getSamplesPerFrame() {
        return mSamplesPerFrame;
    }

    public int[] getFrameGains() {
        return mFrameGains;
    }

    public int getFileSizeBytes() {
        return mFileSize;
    }

    public int getAvgBitrateKbps() {
        return mFileSize / (mNumFrames * mSamplesPerFrame);
    }

    public int getSampleRate() {
        return mSampleRate;
    }

    public int getChannels() {
        return mChannels;
    }

    public String getFiletype() {
        return "AAC";
    }

    public String atomToString(int atomType) {
        String str = "";
        str += (char) ((atomType >> 24) & 0xff);
        str += (char) ((atomType >> 16) & 0xff);
        str += (char) ((atomType >> 8) & 0xff);
        str += (char) (atomType & 0xff);
        return str;
    }

    @Override
    public void readFile(File inputFile) throws FileNotFoundException, IOException {
        super.readFile(inputFile);
        mChannels = 0;
        mSampleRate = 0;
        mBitrate = 0;
        mSamplesPerFrame = 0;
        mNumFrames = 0;
        mMinGain = 255;
        mMaxGain = 0;
        mOffset = 0;
        mDataOffset = -1;
        mDataLength = -1;
        mAtomMap = new HashMap<Integer, Atom>();
        mFileSize = (int) mInputFile.length();
        if (mFileSize < 128) {
            throw new IOException("File too small to parse");
        }
        FileInputStream stream = new FileInputStream(mInputFile);
        byte[] header = new byte[8];
        stream.read(header, 0, 8);
        if (header[0] == 0 &&
                header[4] == 'f' &&
                header[5] == 't' &&
                header[6] == 'y' &&
                header[7] == 'p') {
            stream = new FileInputStream(mInputFile);
            parseMp4(stream, mFileSize);
        } else {
            throw new IOException("Unknown file format");
        }
        if (mDataOffset > 0 && mDataLength > 0) {
            stream = new FileInputStream(mInputFile);
            stream.skip(mDataOffset);
            mOffset = mDataOffset;
            parseData(stream, mDataLength);
        } else {
            throw new IOException("Didn't find mdat");
        }
        boolean bad = false;
        for (int requiredAtomType : kRequiredAtoms) {
            if (!mAtomMap.containsKey(requiredAtomType)) {
                System.out.println("Missing atom: " +
                        atomToString(requiredAtomType));
                bad = true;
            }
        }
        if (bad) {
            throw new java.io.IOException("Could not parse MP4 file");
        }
    }

    private void parseMp4(InputStream stream, int maxLen) throws java.io.IOException {
        while (maxLen > 8) {
            int initialOffset = mOffset;

            byte[] atomHeader = new byte[8];
            stream.read(atomHeader, 0, 8);
            int atomLen =
                    ((0xff & atomHeader[0]) << 24) |
                            ((0xff & atomHeader[1]) << 16) |
                            ((0xff & atomHeader[2]) << 8) |
                            ((0xff & atomHeader[3]));
            if (atomLen > maxLen)
                atomLen = maxLen;
            int atomType =
                    ((0xff & atomHeader[4]) << 24) |
                            ((0xff & atomHeader[5]) << 16) |
                            ((0xff & atomHeader[6]) << 8) |
                            ((0xff & atomHeader[7]));

            Atom atom = new Atom();
            atom.start = mOffset;
            atom.len = atomLen;
            mAtomMap.put(atomType, atom);

            mOffset += 8;

            if (atomType == kMOOV ||
                    atomType == kTRAK ||
                    atomType == kMDIA ||
                    atomType == kMINF ||
                    atomType == kSTBL) {
                parseMp4(stream, atomLen);
            } else if (atomType == kSTSZ) {
                parseStsz(stream, atomLen - 8);
            } else if (atomType == kSTTS) {
                parseStts(stream, atomLen - 8);
            } else if (atomType == kMDAT) {
                mDataOffset = mOffset;
                mDataLength = atomLen - 8;
            } else {
                for (int savedAtomType : kSaveDataAtoms) {
                    if (savedAtomType == atomType) {
                        byte[] data = new byte[atomLen - 8];
                        stream.read(data, 0, atomLen - 8);
                        mOffset += atomLen - 8;
                        mAtomMap.get(atomType).data = data;
                    }
                }
            }

            if (atomType == kSTSD) {
                parseMp4aFromStsd();
            }

            maxLen -= atomLen;
            int skipLen = atomLen - (mOffset - initialOffset);
            if (skipLen < 0) {
                throw new java.io.IOException(
                        "Went over by " + (-skipLen) + " bytes");
            }

            stream.skip(skipLen);
            mOffset += skipLen;
        }
    }

    void parseMp4aFromStsd() {
        byte[] stsdData = mAtomMap.get(kSTSD).data;
        mChannels =
                ((0xff & stsdData[32]) << 8) |
                        ((0xff & stsdData[33]));
        mSampleRate =
                ((0xff & stsdData[40]) << 8) |
                        ((0xff & stsdData[41]));
    }

    void parseStts(InputStream stream, int maxLen)
            throws java.io.IOException {
        byte[] sttsData = new byte[16];
        stream.read(sttsData, 0, 16);
        mOffset += 16;
        mSamplesPerFrame =
                ((0xff & sttsData[12]) << 24) |
                        ((0xff & sttsData[13]) << 16) |
                        ((0xff & sttsData[14]) << 8) |
                        ((0xff & sttsData[15]));
    }

    void parseStsz(InputStream stream, int maxLen)
            throws java.io.IOException {
        byte[] stszHeader = new byte[12];
        stream.read(stszHeader, 0, 12);
        mOffset += 12;
        mNumFrames =
                ((0xff & stszHeader[8]) << 24) |
                        ((0xff & stszHeader[9]) << 16) |
                        ((0xff & stszHeader[10]) << 8) |
                        ((0xff & stszHeader[11]));

        mFrameLens = new int[mNumFrames];
        mFrameGains = new int[mNumFrames];
        byte[] frameLenBytes = new byte[4 * mNumFrames];
        stream.read(frameLenBytes, 0, 4 * mNumFrames);
        mOffset += 4 * mNumFrames;
        for (int i = 0; i < mNumFrames; i++) {
            mFrameLens[i] =
                    ((0xff & frameLenBytes[4 * i + 0]) << 24) |
                            ((0xff & frameLenBytes[4 * i + 1]) << 16) |
                            ((0xff & frameLenBytes[4 * i + 2]) << 8) |
                            ((0xff & frameLenBytes[4 * i + 3]));
        }
    }

    void parseData(InputStream stream, int maxLen) throws IOException {
        int initialOffset = mOffset;
        for (int i = 0; i < mNumFrames; i++) {
            if (mOffset - initialOffset + mFrameLens[i] > maxLen - 8) {
                mFrameGains[i] = 0;
            } else {
                readFrameAndComputeGain(stream, i);
            }
            if (mFrameGains[i] < mMinGain) {
                mMinGain = mFrameGains[i];
            }
            if (mFrameGains[i] > mMaxGain) {
                mMaxGain = mFrameGains[i];
            }
            if (mProgressListener != null) {
                boolean keepGoing = mProgressListener.reportProgress(mOffset * 1.0 / mFileSize);
                if (!keepGoing) {
                    break;
                }
            }
        }
    }

    void readFrameAndComputeGain(InputStream stream, int frameIndex) throws java.io.IOException {
        if (mFrameLens[frameIndex] < 4) {
            mFrameGains[frameIndex] = 0;
            stream.skip(mFrameLens[frameIndex]);
            return;
        }

        int initialOffset = mOffset;

        byte[] data = new byte[4];
        stream.read(data, 0, 4);
        mOffset += 4;

        int idSynEle = (0xe0 & data[0]) >> 5;

        switch (idSynEle) {
            case 0:  // ID_SCE: mono
                int monoGain = ((0x01 & data[0]) << 7) | ((0xfe & data[1]) >> 1);
                mFrameGains[frameIndex] = monoGain;
                break;
            case 1:  // ID_CPE: stereo
                int windowSequence = (0x60 & data[1]) >> 5;
                int windowShape = (0x10 & data[1]) >> 4;

                int maxSfb;
                int scaleFactorGrouping;
                int maskPresent;
                int startBit;

                if (windowSequence == 2) {
                    maxSfb = 0x0f & data[1];

                    scaleFactorGrouping = (0xfe & data[2]) >> 1;

                    maskPresent =
                            ((0x01 & data[2]) << 1) |
                                    ((0x80 & data[3]) >> 7);

                    startBit = 25;
                } else {
                    maxSfb =
                            ((0x0f & data[1]) << 2) |
                                    ((0xc0 & data[2]) >> 6);

                    scaleFactorGrouping = -1;

                    maskPresent = (0x18 & data[2]) >> 3;

                    startBit = 21;
                }

                if (maskPresent == 1) {
                    int sfgZeroBitCount = 0;
                    for (int b = 0; b < 7; b++) {
                        if ((scaleFactorGrouping & (1 << b)) == 0) {
                            sfgZeroBitCount++;
                        }
                    }

                    int numWindowGroups = 1 + sfgZeroBitCount;

                    int skip = maxSfb * numWindowGroups;

                    startBit += skip;
                }

                // We may need to fill our buffer with more than the 4
                // bytes we've already read, here.
                int bytesNeeded = 1 + ((startBit + 7) / 8);
                byte[] oldData = data;
                data = new byte[bytesNeeded];
                data[0] = oldData[0];
                data[1] = oldData[1];
                data[2] = oldData[2];
                data[3] = oldData[3];
                stream.read(data, 4, bytesNeeded - 4);
                mOffset += (bytesNeeded - 4);

                int firstChannelGain = 0;
                for (int b = 0; b < 8; b++) {
                    int b0 = (b + startBit) / 8;
                    int b1 = 7 - ((b + startBit) % 8);
                    int add = (((1 << b1) & data[b0]) >> b1) << (7 - b);
                    firstChannelGain += add;
                }

                mFrameGains[frameIndex] = firstChannelGain;
                break;

            default:
                if (frameIndex > 0) {
                    mFrameGains[frameIndex] = mFrameGains[frameIndex - 1];
                } else {
                    mFrameGains[frameIndex] = 0;
                }
                break;
        }

        int skip = mFrameLens[frameIndex] - (mOffset - initialOffset);

        stream.skip(skip);
        mOffset += skip;
    }

    @Override
    public void writeFile(File outputFile, int startFrame, int numFrames) throws IOException {
        super.writeFile(outputFile, startFrame, numFrames);
    }
}
