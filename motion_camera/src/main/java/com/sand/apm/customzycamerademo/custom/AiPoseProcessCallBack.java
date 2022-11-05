package com.sand.apm.customzycamerademo.custom;

import android.graphics.Bitmap;
import android.media.Image;

import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.pose.Pose;

/**
 * @ProjectName: MyOpenCV
 * @Date: 2022/10/21
 * @Desc:
 */
public class AiPoseProcessCallBack {

    public void onSuccess(Pose pose, InputImage image){

    }

    public void onSuccessDouble(Bitmap bitmapTotal, Pose poseLeft, Bitmap imageLeft, Pose poseRight, Bitmap imageRight){

    }
    public void onFail(InputImage image){

    }

    public void onComplete(Task<Pose> task){

    }

}