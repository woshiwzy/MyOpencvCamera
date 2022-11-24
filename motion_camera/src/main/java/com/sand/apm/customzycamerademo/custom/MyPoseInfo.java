package com.sand.apm.customzycamerademo.custom;

import com.google.mlkit.vision.pose.Pose;

/**
 * @ProjectName: MyOpenCV
 * @Date: 2022/9/1
 * @Desc:
 */
public class MyPoseInfo {

    private int sourceWidth,sourceHeight;
    private int targetWidth,targetHeight;

    private Pose pose;

    public MyPoseInfo(int targetWidth, int targetHeight, Pose pose) {
        this.targetWidth = targetWidth;
        this.targetHeight = targetHeight;
        this.pose = pose;
    }

    public MyPoseInfo() {
    }


    public void setDimenInfo(int sourceWidth,int sourceHeight,int targetWidth,int targetHeight){
        this.sourceWidth=sourceWidth;
        this.sourceHeight=sourceHeight;
        this.targetWidth=targetWidth;
        this.targetHeight=targetHeight;
    }



    public int getTargetWidth() {
        return targetWidth;
    }

    public void setTargetWidth(int targetWidth) {
        this.targetWidth = targetWidth;
    }

    public int getTargetHeight() {
        return targetHeight;
    }

    public void setTargetHeight(int targetHeight) {
        this.targetHeight = targetHeight;
    }

    public Pose getPose() {
        return pose;
    }

    public void setPose(Pose pose) {
        this.pose = pose;
    }

    public int getSourceWidth() {
        return sourceWidth;
    }

    public void setSourceWidth(int sourceWidth) {
        this.sourceWidth = sourceWidth;
    }

    public int getSourceHeight() {
        return sourceHeight;
    }

    public void setSourceHeight(int sourceHeight) {
        this.sourceHeight = sourceHeight;
    }

    public float getFractionWidth(){
        float fractionWidth=targetWidth*1.0f/sourceWidth;
        return fractionWidth;
    }

    public float getFractionHeight(){
        float fractionHeight=targetHeight*1.0f/sourceHeight;
        return fractionHeight;
    }
}