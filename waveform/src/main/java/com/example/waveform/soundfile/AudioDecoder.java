package com.example.waveform.soundfile;

import java.io.File;
import java.io.IOException;

public interface AudioDecoder {
    void readFile(File inputFile) throws IOException;

}
