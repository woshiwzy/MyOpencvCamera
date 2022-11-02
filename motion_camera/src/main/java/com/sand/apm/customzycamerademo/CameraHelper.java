package com.sand.apm.customzycamerademo;

import android.graphics.Bitmap;
import android.media.Image;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseDetection;
import com.google.mlkit.vision.pose.PoseDetector;
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions;
import com.sand.apm.customzycamerademo.custom.AiPoseProcessCallBack;
import com.sand.apm.customzycamerademo.custom.Camera2DataGeter;
import com.sand.apm.customzycamerademo.custom.DetectResult;
import com.sand.apm.customzycamerademo.custom.MyPoseInfo;

import org.opencv.core.Mat;
import org.opencv.core.Range;
import org.opencv.core.Size;

import java.util.HashMap;

import androidx.annotation.NonNull;

/**
 * @ProjectName: MyOpenCV
 * @Date: 2022/10/21
 * @Desc:
 */
public class CameraHelper {

    /**
     * //相机图片获取器
     */
    public static Camera2DataGeter camera2DataGeter;
    public static Mat mat;
    public static Camera2DataGeter.JavaCamera2Frame camera2Frame;
    public static int widthSource, heightSource, widthTarget, heightTarget;
    public static Bitmap mCacheBitmap, blurBitmap;
    public static int cameraWidth = 1920, cameraHeight = 1080;


    public Size blurSize = new Size(9, 9);
    public Size gaoSiBlurSize = new Size(15, 15);


    public static Range rowRange = new Range(cameraHeight / 2 - cameraHeight / 4, cameraHeight / 2 + cameraHeight / 4);
    public static Range colRange = new Range(cameraWidth / 2 - cameraWidth / 4, cameraWidth / 2 + cameraWidth / 4);

    public static PoseDetectorOptions options = null;
    public static PoseDetector poseDetector = null;
    public static HashMap<String, Bitmap> cacheBitmapPool = new HashMap<>();

    public static int frameCount = 0;

    public static Bitmap finalShowBitmap = null;


    /**
     * 正在识别
     */
    public static boolean processIng = false;
    public static MyPoseInfo myPoseInfo = new MyPoseInfo();
    public static DetectResult detectResult = new DetectResult();


    /**
     * 获取缓存
     *
     * @param targetWidth
     * @param targetHeight
     * @return
     */
    public static Bitmap getCacheBitmap(int targetWidth, int targetHeight) {
        String key = targetWidth + "x" + targetHeight;
        if (cacheBitmapPool.containsKey(key)) {
            return cacheBitmapPool.get(key);
        } else {
            Bitmap tempMap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.RGB_565);
            cacheBitmapPool.put(key, tempMap);
            return tempMap;
        }

    }


    /**
     * 初始化姿态识别器
     */
    public static void initAi() {
        // Base pose detector with streaming frames, when depending on the pose-detection sdk
        CameraHelper.options = new PoseDetectorOptions.Builder()
                .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
                .build();
        CameraHelper.poseDetector = PoseDetection.getClient(CameraHelper.options);


// Accurate pose detector on static images, when depending on the pose-detection-accurate sdk
//        options =
//                new AccuratePoseDetectorOptions.Builder()
//                        .setDetectorMode(AccuratePoseDetectorOptions.SINGLE_IMAGE_MODE)
//                        .build();
    }


    /**
     * google识别姿态
     *
     * @param image
     * @param aiPoseProcessCallBack
     */
    public static void process(InputImage image, AiPoseProcessCallBack aiPoseProcessCallBack) {
        CameraHelper.processIng = true;
        CameraHelper.poseDetector.process(image).addOnSuccessListener(pose -> {
            if (null != aiPoseProcessCallBack) {
                aiPoseProcessCallBack.onSuccess(pose, image);
            }
            CameraHelper.processIng = false;
        }).addOnFailureListener(e -> {
            if (null != aiPoseProcessCallBack) {
                aiPoseProcessCallBack.onFail(image);
            }
            CameraHelper.processIng = false;
        }).addOnCompleteListener(new OnCompleteListener<Pose>() {
            @Override
            public void onComplete(@NonNull Task<Pose> task) {
                CameraHelper.processIng = false;
                if (null != aiPoseProcessCallBack) {
                    aiPoseProcessCallBack.onComplete(task);
                }
            }
        });


    }


    /**
     * 将图像转换成目标大小,并检测其中的姿态
     *
     * @param sourceMat
     * @param targetWidth
     * @param targetHeight
     */
    public static Mat fitMat2Target(Mat sourceMat, int targetWidth, int targetHeight) {
        if (widthSource == 1920 && targetWidth == 1921 && heightSource == targetHeight) {
            //特殊情况
            return sourceMat;
        }

        int widthSource = sourceMat.width();
        int heightSource = sourceMat.height();

        float widthFraction = widthSource * 1.0f / targetWidth;
        float heightFraction = heightSource * 1.0f / targetHeight;
        float minFraction = Math.min(widthFraction, heightFraction);
//        float maxFraction = Math.max(widthFraction, heightFraction);
//        Log.d(App.tag, "fraction:" + widthFraction + " - " + heightFraction + " min :" + minFraction);

        Mat targetMat = null;

        if (minFraction == 1) {
            //刚好一样大
            targetMat = sourceMat;
        } else if (minFraction > 1) {

            if (widthSource < 3840) {//原则上小于4K
                //原图太大，居中裁剪
                CameraHelper.rowRange.start = (heightSource >> 1) - (targetHeight >> 1);
                CameraHelper.rowRange.end = CameraHelper.rowRange.start + targetHeight;
                CameraHelper.colRange.start = (widthSource >> 1) - (targetWidth >> 1);
                CameraHelper.colRange.end = CameraHelper.colRange.start + targetWidth;
                targetMat = new Mat(sourceMat, CameraHelper.rowRange, CameraHelper.colRange);
                sourceMat.release();
            } else {//如果是大于等于4K，找到比例然后按最大图片区域，而且比例始终来裁剪
                if (Math.abs(widthFraction - heightFraction) < 0.01) {//认为其比例一样，证明只是等比缩放而已
                    targetMat = sourceMat;
                } else {
                    //找到居中最大的比例图，暂未处理
                }
            }

        } else {
            //倍数和比例差不超过0.01,认为是一样大
            if ((Math.abs(1 - minFraction) < 0.01f) && (Math.abs(widthFraction - heightFraction) < 0.01f)) {//认为比例一样大
                targetMat = sourceMat;
            } else {
                //原图太小，居中按比例裁剪,最好不处理这种情况，这种情况把照相机分辨率设置大就可以了
                int ftargetWidth = (int) (targetWidth * minFraction);
                int ftargetHeight = (int) (targetHeight * minFraction);

                CameraHelper.rowRange.start = (heightSource >> 1) - (ftargetHeight >> 1);
                CameraHelper.rowRange.end = CameraHelper.rowRange.start + ftargetHeight;
                CameraHelper.colRange.start = (widthSource >> 1) - (ftargetWidth >> 1);
                CameraHelper.colRange.end = CameraHelper.colRange.start + ftargetWidth;
                targetMat = new Mat(sourceMat, CameraHelper.rowRange, CameraHelper.colRange);
                sourceMat.release();
            }

        }

        return targetMat;
    }


    /**
     * 直接处理相机的Image
     *
     * @param cameraImage
     * @param degree
     * @param aiPoseProcessCallBack
     */
    public static void processInputImage(Image cameraImage, int degree, AiPoseProcessCallBack aiPoseProcessCallBack) {
        InputImage inputImage = InputImage.fromMediaImage(cameraImage, degree);
        CameraHelper.process(inputImage, aiPoseProcessCallBack);
    }

}