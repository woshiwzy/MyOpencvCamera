package com.sand.apm.customzycamerademo;

import android.graphics.Bitmap;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.pose.PoseDetection;
import com.google.mlkit.vision.pose.PoseDetector;
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions;
import com.sand.apm.customzycamerademo.custom.AiPoseProcessCallBack;
import com.sand.apm.customzycamerademo.custom.Camera2DataGeter;
import com.sand.apm.customzycamerademo.custom.DetectResult;
import com.sand.apm.customzycamerademo.custom.MyPoseInfo;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Range;
import org.opencv.core.Size;

import java.util.concurrent.ExecutorService;

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
        if (null == mCacheBitmap || mCacheBitmap.getWidth() != targetWidth || mCacheBitmap.getHeight() != targetHeight) {
            mCacheBitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.RGB_565);
        }
        return mCacheBitmap;
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
                aiPoseProcessCallBack.onSuccess(pose);
            }
            CameraHelper.processIng = false;
        }).addOnFailureListener(e -> {
            if (null != aiPoseProcessCallBack) {
                aiPoseProcessCallBack.onFail();
            }
            CameraHelper.processIng = false;
        });


    }



    /**
     * 识别姿态
     *
     * @param sourceMat
     * @param widthSource
     * @param heightSource
     * @param targetWidth
     * @param targetHeight
     * @param aiPoseProcessCallBack
     */
    public static void fitMat2TargetAndDetect(Mat sourceMat, int widthSource, int heightSource, int targetWidth, int targetHeight, AiPoseProcessCallBack aiPoseProcessCallBack) {

        float widthFraction = widthSource * 1.0f / targetWidth;
        float heightFraction = heightSource * 1.0f / targetHeight;
        float minFraction = Math.min(widthFraction, heightFraction);
//        float maxFraction = Math.max(widthFraction, heightFraction);
//        Log.d(App.tag, "fraction:" + widthFraction + " - " + heightFraction + " min :" + minFraction);

        if (minFraction == 1) {
            //刚好一样大
            CameraHelper.mCacheBitmap = CameraHelper.getCacheBitmap(targetWidth, targetHeight);
            Utils.matToBitmap(sourceMat, CameraHelper.mCacheBitmap);
        } else if (minFraction > 1) {
            //原图太大，居中裁剪
            CameraHelper.rowRange.start = heightSource / 2 - targetHeight / 2;
            CameraHelper.rowRange.end = CameraHelper.rowRange.start + targetHeight;
            CameraHelper.colRange.start = widthSource / 2 - targetWidth / 2;
            CameraHelper.colRange.end = CameraHelper.colRange.start + targetWidth;
            Mat targetMat = new Mat(sourceMat, CameraHelper.rowRange, CameraHelper.colRange);
            CameraHelper.mCacheBitmap = CameraHelper.getCacheBitmap(targetWidth, targetHeight);
            Utils.matToBitmap(targetMat, CameraHelper.mCacheBitmap);
            targetMat.release();
        } else {
            //原图太小，居中按比例裁剪,最好不处理这种情况，这种情况把照相机分辨率设置大就可以了
            int ftargetWidth = (int) (targetWidth * minFraction);
            int ftargetHeight = (int) (targetHeight * minFraction);

            CameraHelper.rowRange.start = heightSource / 2 - ftargetHeight / 2;
            CameraHelper.rowRange.end = CameraHelper.rowRange.start + ftargetHeight;
            CameraHelper.colRange.start = widthSource / 2 - ftargetWidth / 2;
            CameraHelper.colRange.end = CameraHelper.colRange.start + ftargetWidth;


            Mat targetMat = new Mat(sourceMat, CameraHelper.rowRange, CameraHelper.colRange);
            CameraHelper.mCacheBitmap = CameraHelper.getCacheBitmap(ftargetWidth, ftargetHeight);
            Utils.matToBitmap(targetMat, CameraHelper.mCacheBitmap);
            targetMat.release();
        }
        sourceMat.release();

        InputImage inputImage = InputImage.fromBitmap(CameraHelper.mCacheBitmap, 0);//原始图
        CameraHelper.process(inputImage, aiPoseProcessCallBack);

    }
}