package com.sand.apm.customzycamerademo;

import android.content.Context;
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
import com.sand.apm.customzycamerademo.custom.CameraDataGeterBase;
import com.sand.apm.customzycamerademo.custom.DetectResult;
import com.sand.apm.customzycamerademo.custom.OnImageCallBackListener;
import com.sand.apm.customzycamerademo.util.LurCacheMap;
import com.tensorflow.lite.data.Device;
import com.tensorflow.lite.data.Person;
import com.tensorflow.lite.ml.ModelType;
import com.tensorflow.lite.ml.MoveNet;
import com.tensorflow.lite.ml.MoveNetMultiPose;
import com.tensorflow.lite.ml.Type;

import org.opencv.core.Mat;
import org.opencv.core.Range;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
    private Camera2DataGeter camera2DataGeter;
    private static Camera2DataGeter.JavaCamera2Frame camera2Frame;
    public static int widthSource, heightSource, widthTarget, heightTarget;
    public static int cameraWidth = 1920, cameraHeight = 1080;

    public static Range rowRange = new Range(cameraHeight / 2 - cameraHeight / 4, cameraHeight / 2 + cameraHeight / 4);
    public static Range colRange = new Range(cameraWidth / 2 - cameraWidth / 4, cameraWidth / 2 + cameraWidth / 4);

    private static PoseDetectorOptions options = null;
    private static PoseDetector poseDetector = null;

    public static int frameCount = 0;
    public static Bitmap sourceBigBitmap = null;//相机输出的原画质(可能裁剪)

    //=========movenet=============
    private static MoveNet moveNet;
    private static MoveNetMultiPose moveNetMultiPose;
//    private static Executors executors = null;

    private static  ExecutorService executeService =null ;
    static {
        executeService=Executors.newCachedThreadPool();
    }

    /**
     * 正在识别
     */
    private static boolean processIng = false;
    private static DetectResult detectResult = new DetectResult(false);
    private static DetectResult detectResultDouble = new DetectResult(true);

    private Context context;

    public CameraHelper(Context context) {
        this.context = context;
    }

    public boolean isProcessIng() {
        return processIng;
    }


    public void toogleCamera() {
        camera2DataGeter.toogleCamera();
    }


    public void setResolution(int widthSource, int heightSource) {
        camera2DataGeter.setResolution(widthSource, heightSource);
    }

    public Camera2DataGeter getCamera2DataGeter() {
        return camera2DataGeter;
    }


    public Mat getSourceMat(boolean isGray, Image image) {
        if (isGray) {//是否处理灰度
            return getCamera2Frame(image).gray();
        } else {
            return getCamera2Frame(image).rgba();
        }
    }


    public void setWidthHeightSource(int widthSource, int heightSource) {
        CameraHelper.widthSource = widthSource;
        CameraHelper.heightSource = heightSource;
    }

    public void setWidthHeightTarget(int widthTarget, int heightTarget) {
        CameraHelper.widthTarget = widthTarget;
        CameraHelper.heightTarget = heightTarget;
    }

    public int getHeightSource() {
        return heightSource;
    }

    public void setHeightSource(int heightSource) {
        CameraHelper.heightSource = heightSource;
    }

    public static int getWidthTarget() {
        return widthTarget;
    }

    public static void setWidthTarget(int widthTarget) {
        CameraHelper.widthTarget = widthTarget;
    }

    public static int getHeightTarget() {
        return heightTarget;
    }

    public static void setHeightTarget(int heightTarget) {
        CameraHelper.heightTarget = heightTarget;
    }

    public void enablePreview() {
        if (null != camera2DataGeter) {
            camera2DataGeter.enableView();
        }
    }

    public void disablePreview() {
        if (null != camera2DataGeter) {
            camera2DataGeter.disableView();
        }
    }

    public void disconnectCamera() {
        if (null != camera2DataGeter) {
            camera2DataGeter.disconnectCamera();
        }
    }

    public void initCamera(OnImageCallBackListener onImageCallBackListener) {
        if (null == camera2DataGeter) {
            camera2DataGeter = new Camera2DataGeter(this.context, CameraDataGeterBase.CAMERA_ID_FRONT, CameraHelper.cameraWidth, CameraHelper.cameraHeight);
            camera2DataGeter.configOrientation(this.context.getResources().getConfiguration());
            camera2DataGeter.setOnImageCallBackListener(onImageCallBackListener);
        }

        initAi();
    }


    /**
     * 获取缓存
     *
     * @param targetWidth
     * @param targetHeight
     * @return
     */
    public static Bitmap getCacheBitmap(int targetWidth, int targetHeight, Bitmap.Config config) {
        return LurCacheMap.getBitmap(targetWidth, targetHeight, config);
    }


    /**
     * 初始化姿态识别器
     */
    public void initAi() {
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
        //movenet
        moveNet = MoveNet.Companion.create(context, Device.CPU, ModelType.Thunder);
        moveNetMultiPose = MoveNetMultiPose.Companion.create(context, Device.CPU, Type.Dynamic);


    }


    /**
     * 使用MoveNet的单人模式
     *
     * @param inputBitmap
     * @param aiPoseProcessCallBack
     */

    private static ArrayList<Person> persons = new ArrayList<>();

    public void processWithMoveNetSingle(Bitmap bitmapSource, Bitmap inputBitmap, AiPoseProcessCallBack aiPoseProcessCallBack) {
        if (null == moveNet) {
            return;
        }
        CameraHelper.processIng = true;

//        Future<List<Person>> futerTask = executeService.submit(new Callable<List<Person>>() {
//
//            @Override
//            public List<Person> call() throws Exception {
//                List<Person> result = moveNet.estimatePoses(inputBitmap);
//                return result;
//            }
//        });


        long startProcess = System.currentTimeMillis();
        List<Person> result = moveNet.estimatePoses(inputBitmap);
//        List<Person> result = null;
//        try {
//            result = futerTask.get();
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        Person targetePerson = null;
        persons.clear();
        if (null != result && !result.isEmpty()) {
            targetePerson = result.get(0);
            persons.add(targetePerson);
        }
        long cost = (System.currentTimeMillis() - startProcess);
        Log.d(App.tag, "Movenet 单人模型耗时:" + cost + " size:" + inputBitmap.getWidth() + "," + inputBitmap.getHeight());
        CameraHelper.processIng = false;
        if (null != aiPoseProcessCallBack) {
            aiPoseProcessCallBack.onSuccessMoveNetSingle(bitmapSource, inputBitmap, persons);
        }
    }

    public static void processWithMoveNetDouble(Bitmap bitmapSource, Bitmap inputBitmap, AiPoseProcessCallBack aiPoseProcessCallBack) {
        if (null == moveNet) {
            return;
        }
        CameraHelper.processIng = true;
        long startProcess = System.currentTimeMillis();
        List<Person> result = moveNetMultiPose.estimatePoses(inputBitmap);
        persons.clear();
        persons.addAll(result);

        long cost = (System.currentTimeMillis() - startProcess);
        Log.d(App.tag, "Movenet 多人模型耗时:" + cost + " size:" + inputBitmap.getWidth() + "," + inputBitmap.getHeight());
        CameraHelper.processIng = false;
        if (null != aiPoseProcessCallBack) {
            aiPoseProcessCallBack.onSuccessMoveNetSingle(bitmapSource, inputBitmap, persons);
        }
    }

    /**
     * google识别姿态
     *
     * @param bitmapSource
     * @param bitmap
     * @param aiPoseProcessCallBack
     */
    public void process(Bitmap bitmapSource, Bitmap bitmap, AiPoseProcessCallBack aiPoseProcessCallBack) {
        CameraHelper.processIng = true;
        CameraHelper.poseDetector.process(InputImage.fromBitmap(bitmap, 0)).addOnSuccessListener(pose -> {
            if (null != aiPoseProcessCallBack) {
                aiPoseProcessCallBack.onSuccess(pose, bitmapSource, bitmap);
            }
            CameraHelper.processIng = false;
        }).addOnFailureListener(e -> {
            if (null != aiPoseProcessCallBack) {
                aiPoseProcessCallBack.onFail(bitmap);
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
                    aiPoseProcessCallBackDouble.onSuccessDouble(sourceBigBitmap, totalBitmap2Doule, leftPose, timageLeft, rightPose, timageRight);
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

    public void process(Bitmap souceTotalBitmap, Bitmap totalBitmap, Bitmap imageLeft, Bitmap imageRight, AiPoseProcessCallBack aiPoseProcessCallBack) {

        try {

            if (processIng) {
                return;
            }
            processIng = true;
            aiPoseProcessCallBackDouble = aiPoseProcessCallBack;
            timageLeft = imageLeft;
            timageRight = imageRight;

            InputImage timageLeft = InputImage.fromBitmap(imageLeft, 0);
            InputImage timageRight = InputImage.fromBitmap(imageRight, 0);
            totalBitmap2Doule = totalBitmap;

            tleft = poseDetector.process(timageLeft).addOnSuccessListener(successListener).addOnCompleteListener(completeListener).addOnFailureListener(failureListener);
            tRight = poseDetector.process(timageRight).addOnSuccessListener(successListener).addOnCompleteListener(completeListener).addOnFailureListener(failureListener);

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

    private Camera2DataGeter.JavaCamera2Frame getCamera2Frame(Image image) {
        if (null == camera2Frame) {
            camera2Frame = new Camera2DataGeter.JavaCamera2Frame(image);
        } else {
            camera2Frame.setImage(image);
        }
        return camera2Frame;
    }

    /**
     * 直接处理相机的Image
     *
     * @param bitmapSource
     * @param bitmap
     * @param aiPoseProcessCallBack
     */
    public void processInputImage(Bitmap bitmapSource, Bitmap bitmap, AiPoseProcessCallBack aiPoseProcessCallBack) {
        process(bitmapSource, bitmap, aiPoseProcessCallBack);
    }

    public DetectResult getDetectResult() {
        return detectResult;
    }


    public DetectResult getDetectResultDouble() {
        return detectResultDouble;
    }

    public static void setDetectResultDouble(DetectResult detectResultDouble) {
        CameraHelper.detectResultDouble = detectResultDouble;
    }

    private static Bitmap leftBitmap;

    public static Bitmap getLeftBitmap(int width, int height) {
        if (null == leftBitmap || leftBitmap.getWidth() != width || leftBitmap.getHeight() != height) {
            if (null != leftBitmap) {
                leftBitmap.recycle();
                leftBitmap = null;
            }
            leftBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        }

        return leftBitmap;
    }


    private static Bitmap rightBitmap;

    public static Bitmap getRightBitmap(int width, int height) {
        if (null == rightBitmap || rightBitmap.getWidth() != width || rightBitmap.getHeight() != height) {
            if (null != rightBitmap) {
                rightBitmap.recycle();
                rightBitmap = null;
            }
            rightBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        }

        return rightBitmap;
    }


}