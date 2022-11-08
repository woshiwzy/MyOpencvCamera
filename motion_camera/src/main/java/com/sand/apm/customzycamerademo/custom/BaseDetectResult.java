package com.sand.apm.customzycamerademo.custom;

import android.graphics.Bitmap;

/**
 * @ProjectName: MyOpenCV
 * @Date: 2022/11/8
 * @Desc:
 */
public class BaseDetectResult {


    public static int SOURCE_TYPE_MP_SINGLE = 1;
    public static int SOURCE_TYPE_MP_DOUBLE = 2;
    public static int SOURCE_TYPE_MOVE_NET_SINGLE = 3;
    public static int SOURCE_TYPE_MOVE_NET_DOUBLE = 4;


    protected Bitmap bitmap, bitmapTotal, sourceBitmapTotal;
    protected MyPoseInfo poseInfo;//单人模式用这个
    protected MyPoseInfo leftPoseInfo, rightPoseInfo;//双人模式这2个
    protected boolean isDouble = false;
    protected boolean showSource = false;
    protected int sourceType;


    public void clear() {
        setPoseInfo(null);
        setLeftPoseInfo(null);
        setRightPoseInfo(null);
    }

    public BaseDetectResult(boolean isDouble) {
        this.isDouble = isDouble;
        poseInfo = new MyPoseInfo();
        leftPoseInfo = new MyPoseInfo();
        rightPoseInfo = new MyPoseInfo();
    }


    public Bitmap getBitmap() {
        return bitmap;
    }

    public boolean isSingleOk() {

        if (null != bitmap && null != poseInfo && null != poseInfo.getPose()) {
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


    public int getSourceType() {
        return sourceType;
    }

    public void setSourceType(int sourceType) {
        this.sourceType = sourceType;
    }
}