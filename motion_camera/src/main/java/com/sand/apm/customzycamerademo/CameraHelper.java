package com.sand.apm.customzycamerademo;

import android.graphics.Bitmap;
import android.media.Image;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
    public static DetectResult detectResult = new DetectResult(false);


    public static MyPoseInfo myPoseInfoLeft = new MyPoseInfo();
    public static MyPoseInfo myPoseInfoRight = new MyPoseInfo();
    public static DetectResult detectResultDouble = new DetectResult(true);
    public static Bitmap bitmapTotal = null;


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


    static OnSuccessListener<Pose> successListener = new OnSuccessListener<Pose>() {
        @Override
        public void onSuccess(Pose pose) {
            Log.d(App.tag, "onSuccess:" + pose);
        }
    };

    static OnFailureListener failureListener = new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
            Log.d(App.tag, "onFailure:" + e);
        }
    };

    static OnCompleteListener<Pose> completeListener = new OnCompleteListener<Pose>() {
        @Override
        public void onComplete(@NonNull Task<Pose> task) {
            Log.d(App.tag, "onComplete:" + task.getResult());

            if (task == tleft) {
                leftPose = task.getResult();
            } else if (task == tRight) {
                rightPose = task.getResult();
            }
            if (null != leftPose && null != rightPose) {

                Log.d(App.tag, "成功了");

                if (null != aiPoseProcessCallBackDouble) {
                    aiPoseProcessCallBackDouble.onSuccessDouble(totalBitmap2Doule, leftPose, timageLeft, rightPose, timageRight);
                }

                leftPose = null;
                rightPose = null;

                tleft = null;
                tRight = null;

                timageLeft = null;
                timageRight = null;
                totalBitmap2Doule = null;

                processIng = false;
            }

        }
    };


    private static Pose leftPose, rightPose;
    private static Bitmap timageLeft, timageRight;
    private static Task<Pose> tleft, tRight;
    private static AiPoseProcessCallBack aiPoseProcessCallBackDouble;
    private static Bitmap totalBitmap2Doule = null;

    public static void process(Bitmap totalBitmap, Bitmap imageLeft, Bitmap imageRight, AiPoseProcessCallBack aiPoseProcessCallBack) {

        try {

            if (processIng) {
                return;
            }
            processIng = true;
            aiPoseProcessCallBackDouble = aiPoseProcessCallBack;
            timageLeft=imageLeft;
            timageRight=imageRight;

            InputImage timageLeft = InputImage.fromBitmap(imageLeft, 0);
            InputImage timageRight = InputImage.fromBitmap(imageRight, 0);
            totalBitmap2Doule = totalBitmap;

            tleft = poseDetector.process(timageLeft).addOnSuccessListener(successListener).addOnCompleteListener(completeListener).addOnFailureListener(failureListener);
            tRight = poseDetector.process(timageRight).addOnSuccessListener(successListener).addOnCompleteListener(completeListener).addOnFailureListener(failureListener);

            Pose leftResult = tleft.getResult();
            Pose rightResult = tRight.getResult();

            Log.d(App.tag, "是否都有结果:" + leftResult + " - " + rightResult);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return;
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

        boolean b = (Math.abs(1 - minFraction) < 0.01f) && (Math.abs(widthFraction - heightFraction) < 0.01f);
        if (((widthFraction == heightFraction)) || b) {
            //刚好一样大,或者，倍数差别很小
            targetMat = sourceMat;

        } else if (minFraction > 1) {
            //原图太大
            if (Math.abs(widthFraction - heightFraction) < 0.01) {
                //长宽比例，差距非常小，我认为其比例始一致的，直接原图返回，有2个好处，第一节省裁剪时间，第二活的更高清的图片效果更好
                targetMat = sourceMat;
            } else {
                //寻找最合适的内切比例来裁剪
                if (minFraction == widthFraction) {
                    //保留宽度,居中裁剪
                    int cropWidth = widthSource;
                    int cropHeight = (int) (heightTarget * minFraction);

                    CameraHelper.rowRange.start = (heightSource >> 1) - (cropHeight >> 1);
                    CameraHelper.rowRange.end = CameraHelper.rowRange.start + cropHeight;

                    CameraHelper.colRange.start = 0;
                    CameraHelper.colRange.end = CameraHelper.colRange.start + cropWidth;

                    targetMat = new Mat(sourceMat, CameraHelper.rowRange, CameraHelper.colRange);

                } else if (minFraction == heightFraction) {
                    //保留高度,居中裁剪
                    int cropHeight = heightSource;
                    int cropWidth = (int) (widthTarget * minFraction);

                    CameraHelper.rowRange.start = 0;
                    CameraHelper.rowRange.end = CameraHelper.rowRange.start + cropHeight;

                    CameraHelper.colRange.start = (widthSource >> 1) - (cropWidth >> 1);
                    CameraHelper.colRange.end = CameraHelper.colRange.start + cropWidth;

                    targetMat = new Mat(sourceMat, CameraHelper.rowRange, CameraHelper.colRange);

                }
                sourceMat.release();

            }

        } else {
            //倍数和比例差不超过0.01,认为是一样大
            if (b) {//认为比例一样大
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