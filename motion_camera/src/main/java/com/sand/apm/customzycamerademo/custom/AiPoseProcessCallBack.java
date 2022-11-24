package com.sand.apm.customzycamerademo.custom;

import android.graphics.Bitmap;

import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.pose.Pose;
import com.tensorflow.lite.data.Person;

import java.util.ArrayList;

/**
 * @ProjectName: MyOpenCV
 * @Date: 2022/10/21
 * @Desc:
 */
public class AiPoseProcessCallBack {

    public void onSuccess(Pose pose,Bitmap bitmapSourceBig, Bitmap aiInput){

    }

    public void onSuccessDouble(Bitmap sourceBitmapTotal,Bitmap bitmapTotal, Pose poseLeft, Bitmap imageLeft, Pose poseRight, Bitmap imageRight){

    }

    public void onSuccessMoveNetSingle(Bitmap bitmapSource, Bitmap bitmapInput, ArrayList<Person> person){

    }


    public void onFail(Bitmap image){

    }

    public void onComplete(Task<Pose> task){

    }

}