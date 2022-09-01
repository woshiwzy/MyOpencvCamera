package com.sand.apm.customzycamerademo.custom;

import org.opencv.core.Mat;

/**
 * Created by wangzy on 4/21/21
 * description:
 */
public class CvCameraViewListener2Adapter implements CameraDataGeterBase.CvCameraViewListener2{
    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(Mat rgbaMat) {
        return null;
    }
}
