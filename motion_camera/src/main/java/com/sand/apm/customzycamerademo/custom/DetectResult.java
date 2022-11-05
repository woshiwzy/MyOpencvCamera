package com.sand.apm.customzycamerademo.custom;

import android.graphics.Bitmap;

/**
 * @ProjectName: MyOpenCV
 * @Date: 2022/9/1
 * @Desc:
 */
public class DetectResult {

    private Bitmap bitmap,bitmapTotal,sourceBitmapTotal;
    private MyPoseInfo poseInfo;//单人模式用这个
    private MyPoseInfo leftPoseInfo,rightPoseInfo;//双人模式这2个
    private boolean isDouble=false;
    private  boolean showSource=false;



    public DetectResult(boolean isDouble) {
        this.isDouble=isDouble;
        poseInfo=new MyPoseInfo();
        leftPoseInfo=new MyPoseInfo();
        rightPoseInfo=new MyPoseInfo();
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public boolean isSingleOk(){

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

    public Bitmap getBitmapTotal() {
        return bitmapTotal;
    }

    public void setBitmapTotal(Bitmap bitmapTotal) {
        this.bitmapTotal = bitmapTotal;
    }

    public Bitmap getSourceBitmapTotal() {
        return sourceBitmapTotal;
    }

    public void setSourceBitmapTotal(Bitmap sourceBitmapTotal) {
        this.sourceBitmapTotal = sourceBitmapTotal;
    }

    public boolean isShowSource() {
        return showSource;
    }

    public void setShowSource(boolean showSource) {
        this.showSource = showSource;
    }

}