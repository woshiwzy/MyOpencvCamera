package com.sand.apm.customzycamerademo.pose;

public interface JfSkeletonProcessor {
    String getPlayer();

    void onFailure(String imageId, String player);

    void onProcessor(JfPoseInfo poseInfo);
}
