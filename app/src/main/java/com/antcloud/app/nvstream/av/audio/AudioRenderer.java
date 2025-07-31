package com.antcloud.app.nvstream.av.audio;

import com.antcloud.app.nvstream.jni.AntBridge;

public interface AudioRenderer {
    int setup(AntBridge.AudioConfiguration audioConfiguration, int sampleRate, int samplesPerFrame);

    void start();

    void stop();
    
    void playDecodedAudio(short[] audioData);
    
    void cleanup();
}
