package com.sand.apm.customzycamerademo.custom;

import android.graphics.Bitmap;

/**
 * @ProjectName: MyOpenCV
 * @Date: 2022/9/1
 * @Desc:
 */
public class DetectResult {

    public Bitmap bitmap;
    public MyPoseInfo poseInfo;

    public Bitmap getBitmap() {
        return bitmap;
    }

    public boolean isOk(){

        if(null!=bitmap && null!=poseInfo && null!=poseInfo.getPose()){
            return true;
        }
        return false;
    }


    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public MyPoseInfo getPoseInfo() {
        return poseInfo;
    }

    public void setPoseInfo(MyPoseInfo poseInfo) {
        this.poseInfo = poseInfo;
    }
}