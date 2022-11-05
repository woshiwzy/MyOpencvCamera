package com.sand.apm.customzycamerademo.pose;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class JfPoseInfo implements Serializable {
    private String key;
    private int imageWidth;
    private int imageHeight;
    private String from;
    private String player;
    private JfPoseBoxing poseBoxing;
    private final List<JfPoseSkeleton> skeletons = new ArrayList();
    public final List<JfExtendObject> extendObject = new ArrayList();

    public JfPoseInfo() {
        this.setPlayer("None");
    }

    public String getKey() {
        return this.key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getImageWidth() {
        return this.imageWidth;
    }

    public void setImageWidth(int imageWidth) {
        this.imageWidth = imageWidth;
    }

    public int getImageHeight() {
        return this.imageHeight;
    }

    public void setImageHeight(int imageHeight) {
        this.imageHeight = imageHeight;
    }

    public String getPlayer() {
        return this.player;
    }

    public void setPlayer(String player) {
        this.player = player;
    }

    public JfPoseBoxing getPoseBoxing() {
        return this.poseBoxing;
    }

    public void setPoseBoxing(JfPoseBoxing poseBoxing) {
        this.poseBoxing = poseBoxing;
    }

    public List<JfPoseSkeleton> getSkeletons() {
        return this.skeletons;
    }

    public String getFrom() {
        return this.from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public List<JfExtendObject> getExtendObject() {
        return this.extendObject;
    }

    public boolean isOk() {
        return this.skeletons.size() > 0;
    }

    public JfPoseSkeleton defaultSkeleton() {
        return this.skeletons.size() > 0 ? (JfPoseSkeleton)this.skeletons.get(0) : null;
    }
}