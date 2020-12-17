package com.example.waveform.soundfile;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.IOException;

public class AudioDecoderBuilder {
    @Nullable
    public static AudioDecoder build(String filePath, ProgressListener progressListener) {
        File file = new File(filePath);
        if (file.exists()) {
            try {
                AudioDecoder audioDecoder = CheapSoundFile.create(filePath, progressListener);
                if(audioDecoder != null){
                    return audioDecoder;
                }else{
                    return HeavySoundFile.create(filePath, progressListener);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (WavFileException e) {
                e.printStackTrace();
            }
        }
        return null;

    }
}
