package com.sand.apm.customzycamerademo.custom;

import android.graphics.Bitmap;

/**
 * @ProjectName: MyOpenCV
 * @Date: 2022/9/1
 * @Desc:
 */
public class DetectResult {

    public Bitmap bitmap,bitmapTotal;
    public MyPoseInfo poseInfo;
    public MyPoseInfo leftPoseInfo,rightPoseInfo;
    private boolean isDouble=false;

    public DetectResult(boolean isDouble) {
        this.isDouble=isDouble;
        poseInfo=new MyPoseInfo();
        leftPoseInfo=new MyPoseInfo();
        rightPoseInfo=new MyPoseInfo();
    }

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

    public boolean isDouble() {
        return isDouble;
    }

    public void setDouble(boolean aDouble) {
        isDouble = aDouble;
    }

    public MyPoseInfo getLeftPoseInfo() {
        return leftPoseInfo;
    }

    public void setLeftPoseInfo(MyPoseInfo leftPoseInfo) {
        this.leftPoseInfo = leftPoseInfo;
    }

    public MyPoseInfo getRightPoseInfo() {
        return rightPoseInfo;
    }

    public void setRightPoseInfo(MyPoseInfo rightPoseInfo) {
        this.rightPoseInfo = rightPoseInfo;
    }
}