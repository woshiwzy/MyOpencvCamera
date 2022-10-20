package com.sand.apm.customzycamerademo;

import android.app.Activity;
import android.graphics.Bitmap;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseDetection;
import com.google.mlkit.vision.pose.PoseDetector;
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.sand.apm.customzycamerademo.custom.Camera2DataGeter;
import com.sand.apm.customzycamerademo.custom.CameraDataGeterBase;
import com.sand.apm.customzycamerademo.custom.DetectResult;
import com.sand.apm.customzycamerademo.custom.MyPoseInfo;
import com.sand.apm.customzycamerademo.custom.OnImageCallBackListener;
import com.sand.apm.customzycamerademo.custom.PoseImageView;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Range;
import org.opencv.core.Size;

import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class MainActivity extends Activity {

    private Camera2DataGeter camera2DataGeter;
    private Mat mat;
    private Camera2DataGeter.JavaCamera2Frame camera2Frame;
    private int widthSource, heightSource, widthTarget, heightTarget;
    private Bitmap mCacheBitmap, blurBitmap;
    private int cameraWidth = 1920, cameraHeight = 1080;
    private ImageView imageViewPreview;
    private PoseImageView imageViewCenter;
    private TextView textViewFps;
    private long lastTime = 0;

    private Size blurSize = new Size(9, 9);
    private Size gaoSiBlurSize = new Size(15, 15);


    Range rowRange = new Range(cameraHeight / 2 - cameraHeight / 4, cameraHeight / 2 + cameraHeight / 4);
    Range colRange = new Range(cameraWidth / 2 - cameraWidth / 4, cameraWidth / 2 + cameraWidth / 4);

    PoseDetectorOptions options = null;
    PoseDetector poseDetector = null;
    private boolean processIng = false;
    private MyPoseInfo myPoseInfo = new MyPoseInfo();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        textViewFps = findViewById(R.id.textViewFps);

        imageViewPreview = findViewById(R.id.imageViewPreview);
        imageViewCenter = findViewById(R.id.imageViewCenter);

        requestPermissionAndInitCamera();
    }


    /**
     * 权限请求打开相机
     */
    private void requestPermissionAndInitCamera() {
        XXPermissions.with(this)
                .permission(Permission.CAMERA)
                .permission(Permission.WRITE_EXTERNAL_STORAGE)
                .permission(Permission.READ_EXTERNAL_STORAGE)
                // 申请单个权限
//                .permission(Permission.RECORD_AUDIO)
                // 申请多个权限
                //.interceptor(new PermissionInterceptor())
                .request(new OnPermissionCallback() {
                    @Override
                    public void onGranted(List<String> permissions, boolean all) {
                        if (all) {
//                            toast("获取录音和日历权限成功");
                            initCamera();
                            initAi();
                        } else {
//                            toast("获取部分权限成功，但部分权限未正常授予");
                        }
                    }

                    @Override
                    public void onDenied(List<String> permissions, boolean never) {
                        if (never) {
//                            toast("被永久拒绝授权，请手动授予录音和日历权限");
                            // 如果是被永久拒绝就跳转到应用权限系统设置页面
                            XXPermissions.startPermissionActivity(MainActivity.this, permissions);
                        } else {
//                            toast("获取录音和日历权限失败");
                        }
                    }
                });
    }


    private DetectResult detectResult = new DetectResult();

    private DetectResult fitMat2TargetAndDetect(Mat sourceMat, int widthSource, int heightSource, int targetWidth, int targetHeight) {

        float widthFraction = widthSource * 1.0f / targetWidth;
        float heightFraction = heightSource * 1.0f / targetHeight;
        float minFraction = Math.min(widthFraction, heightFraction);
//        float maxFraction = Math.max(widthFraction, heightFraction);
//        Log.d(App.tag, "fraction:" + widthFraction + " - " + heightFraction + " min :" + minFraction);

        if (minFraction == 1) {
            //刚好一样大
            mCacheBitmap = getCacheBitmap(targetWidth, targetHeight);
            Utils.matToBitmap(sourceMat, mCacheBitmap);
        } else if (minFraction > 1) {
            //原图太大，居中裁剪
            rowRange.start = heightSource / 2 - targetHeight / 2;
            rowRange.end = rowRange.start + targetHeight;
            colRange.start = widthSource / 2 - targetWidth / 2;
            colRange.end = colRange.start + targetWidth;
            Mat targetMat = new Mat(sourceMat, rowRange, colRange);
            mCacheBitmap = getCacheBitmap(targetWidth, targetHeight);
            Utils.matToBitmap(targetMat, mCacheBitmap);
            targetMat.release();
        } else {
            //原图太小，居中按比例裁剪,最好不处理这种情况，这种情况把照相机分辨率设置大就可以了
            int ftargetWidth = (int) (targetWidth * minFraction);
            int ftargetHeight = (int) (targetHeight * minFraction);

            rowRange.start = heightSource / 2 - ftargetHeight / 2;
            rowRange.end = rowRange.start + ftargetHeight;
            colRange.start = widthSource / 2 - ftargetWidth / 2;
            colRange.end = colRange.start + ftargetWidth;

            Mat targetMat = new Mat(sourceMat, rowRange, colRange);
            mCacheBitmap = getCacheBitmap(ftargetWidth, ftargetHeight);
            Utils.matToBitmap(targetMat, mCacheBitmap);
            targetMat.release();
        }
        sourceMat.release();

        long pStart = System.currentTimeMillis();
        InputImage inputImage = InputImage.fromBitmap(mCacheBitmap, 0);//原始图
        long pend1 = System.currentTimeMillis();
//        Log.d(App.tag,"cost rans:"+(pend1-pStart));

        Pose poseInfo = process(inputImage);

        myPoseInfo.setPose(poseInfo);
        myPoseInfo.setSourceWidth(mCacheBitmap.getWidth());
        myPoseInfo.setSourceHeight(mCacheBitmap.getHeight());

        myPoseInfo.setTargetWidth(targetWidth);
        myPoseInfo.setTargetHeight(targetHeight);

        long pEnd = System.currentTimeMillis();
        long cost = pEnd - pStart;
        Log.d(App.tag, "ret:" + poseInfo.getAllPoseLandmarks().size() + " cost time:" + (cost));

        detectResult.setBitmap(mCacheBitmap);
        detectResult.setPoseInfo(myPoseInfo);

        return detectResult;
    }


    private Bitmap getCacheBitmap(int targetWidth, int targetHeight) {
        if (null == mCacheBitmap || mCacheBitmap.getWidth() != targetWidth || mCacheBitmap.getHeight() != targetHeight) {
            mCacheBitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888);
        }
        return mCacheBitmap;
    }


    CyclicBarrier cyclicBarrier = new CyclicBarrier(2);

    private Pose process(InputImage image) {

        processIng = true;
        Task<Pose> result = poseDetector.process(image).addOnSuccessListener(pose -> {
            try {
                cyclicBarrier.await();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).addOnFailureListener(e -> {
            try {
                cyclicBarrier.await();
            } catch (InterruptedException | BrokenBarrierException e2) {
                e.printStackTrace();
            }
        });

        try {
            cyclicBarrier.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            e.printStackTrace();
        }
        Pose ret = result.getResult();
        processIng = false;

        return ret;

    }

    private void initAi() {
        // Base pose detector with streaming frames, when depending on the pose-detection sdk
        options = new PoseDetectorOptions.Builder().setDetectorMode(PoseDetectorOptions.STREAM_MODE).build();
        poseDetector = PoseDetection.getClient(options);


// Accurate pose detector on static images, when depending on the pose-detection-accurate sdk
//        options =
//                new AccuratePoseDetectorOptions.Builder()
//                        .setDetectorMode(AccuratePoseDetectorOptions.SINGLE_IMAGE_MODE)
//                        .build();
    }

    private void initCamera() {

        camera2DataGeter = new Camera2DataGeter(this, CameraDataGeterBase.CAMERA_ID_FRONT, cameraWidth, cameraHeight);
        camera2DataGeter.configOrientation(getResources().getConfiguration());
        camera2DataGeter.setOnImageCallBackListener(new OnImageCallBackListener() {
            @Override
            public void onImageCatch(Image image) {
                if (processIng) {
                    return;
                }
                if (null == camera2Frame) {
                    camera2Frame = new Camera2DataGeter.JavaCamera2Frame(image);
                } else {
                    camera2Frame.setImage(image);
                }

                if (widthTarget == 0 || heightTarget == 0) {
                    widthTarget = imageViewCenter.getWidth();
                    heightTarget = imageViewCenter.getHeight();
                }

                Mat sourceMat = camera2Frame.rgba();
                Core.flip(sourceMat, sourceMat, 1);
                widthSource = sourceMat.width();
                heightSource = sourceMat.height();

                //targetWidth 可以自由控制
                int targetWidth = imageViewCenter.getWidth();
                int targetHeight = imageViewCenter.getHeight();
                if (targetWidth <= 0 || targetHeight <= 0) {
                    return;
                }
                DetectResult result = fitMat2TargetAndDetect(sourceMat, widthSource, heightSource, targetWidth, targetHeight);
                runOnUiThread(() -> {
                    long now = System.currentTimeMillis();
                    long delta = now - lastTime;
                    if (delta != 0) {
                        int fps = (int) (1000 / delta);
                        String fpsAndPixs = "摄像头信息:" + widthSource + "x" + heightSource + " - " + String.valueOf(fps);
                        textViewFps.setText(fpsAndPixs);
                    }
                    imageViewCenter.setDetectResult(result);
                    lastTime = System.currentTimeMillis();
//                  imageViewPreview.setImageBitmap(blurBitmap);
                });

            }
        });


        if (null != camera2DataGeter) {
            mCacheBitmap = Bitmap.createBitmap(cameraWidth, cameraHeight, Bitmap.Config.ARGB_8888);
            //3.打开相机
            mat = new Mat();
            camera2DataGeter.enableView();
            blurBitmap = Bitmap.createBitmap(cameraWidth / 2, cameraHeight / 2, Bitmap.Config.ARGB_8888);
        }
    }

}