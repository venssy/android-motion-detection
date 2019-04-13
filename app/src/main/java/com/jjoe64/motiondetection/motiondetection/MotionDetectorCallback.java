package com.jjoe64.motiondetection.motiondetection;

public interface MotionDetectorCallback {
    void onMotionDetected(byte[] data, int width, int height);
    void onTooDark();
}
