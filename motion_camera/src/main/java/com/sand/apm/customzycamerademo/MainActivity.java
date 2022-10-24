package com.sand.apm.customzycamerademo;

import android.app.Activity;
import android.graphics.Bitmap;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.mlkit.vision.pose.Pose;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.sand.apm.customzycamerademo.custom.AiPoseProcessCallBack;
import com.sand.apm.customzycamerademo.custom.Camera2DataGeter;
import com.sand.apm.customzycamerademo.custom.CameraDataGeterBase;
import com.sand.apm.customzycamerademo.custom.DetectResult;
import com.sand.apm.customzycamerademo.custom.OnImageCallBackListener;
import com.sand.apm.customzycamerademo.custom.PoseImageView;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.util.List;

public class MainActivity extends Activity {

    public ImageView imageViewPreview;
    public PoseImageView imageViewShowTarget;
    public TextView textViewFps;
    public CheckBox checkBoxMirror,checkBoxGray;
    public long lastTime = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        textViewFps = findViewById(R.id.textViewFps);
        imageViewPreview = findViewById(R.id.imageViewPreview);
        imageViewShowTarget = findViewById(R.id.imageViewCenter);
        checkBoxMirror=findViewById(R.id.checkBoxMirror);
        checkBoxGray=findViewById(R.id.checkBoxGray);

        findViewById(R.id.button240).setOnClickListener(view -> {
            //240X320
            CameraHelper.camera2DataGeter.setResolution(320, 240);
        });

        findViewById(R.id.button480).setOnClickListener(view -> {
            //480X640
            CameraHelper.camera2DataGeter.setResolution(640, 480);
        });

        findViewById(R.id.button1280).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CameraHelper.camera2DataGeter.setResolution(1280, 720);
            }
        });

        findViewById(R.id.button1080).setOnClickListener(view -> {
            //1080X1920
            CameraHelper.camera2DataGeter.setResolution(1920, 1080);
        });


        findViewById(R.id.buttonSwitchCamera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CameraHelper.camera2DataGeter.toogleCamera();
            }
        });

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
                            CameraHelper.initAi();
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


    private void initCamera() {

        CameraHelper.camera2DataGeter = new Camera2DataGeter(this, CameraDataGeterBase.CAMERA_ID_FRONT, CameraHelper.cameraWidth, CameraHelper.cameraHeight);
        CameraHelper.camera2DataGeter.configOrientation(getResources().getConfiguration());
        CameraHelper.camera2DataGeter.setOnImageCallBackListener(new OnImageCallBackListener() {
            @Override
            public void onImageCatch(Image image) {
                if (CameraHelper.processIng) {
                    return;
                }
                if (null == CameraHelper.camera2Frame) {
                    CameraHelper.camera2Frame = new Camera2DataGeter.JavaCamera2Frame(image);
                } else {
                    CameraHelper.camera2Frame.setImage(image);
                }

                if (CameraHelper.widthTarget == 0 || CameraHelper.heightTarget == 0) {
                    CameraHelper.widthTarget = imageViewShowTarget.getWidth();
                    CameraHelper.heightTarget = imageViewShowTarget.getHeight();
                }

                Mat sourceMat = CameraHelper.camera2Frame.rgba();
                if(checkBoxMirror.isChecked()){//实处处理镜像
                    Core.flip(sourceMat, sourceMat, 1);
                }

                if(checkBoxGray.isChecked()){//是否处理灰度
                    Imgproc.cvtColor(sourceMat,sourceMat,Imgproc.COLOR_RGB2GRAY);
                }

                CameraHelper.widthSource = sourceMat.width();
                CameraHelper.heightSource = sourceMat.height();

                //targetWidth 可以自由控制
                int targetWidth = imageViewShowTarget.getWidth();
                int targetHeight = imageViewShowTarget.getHeight();
                if (targetWidth <= 0 || targetHeight <= 0) {
                    return;
                }

                CameraHelper.fitMat2TargetAndDetect(sourceMat, CameraHelper.widthSource, CameraHelper.heightSource, targetWidth, targetHeight,
                        new AiPoseProcessCallBack() {
                            @Override
                            public void onSuccess(Pose poseInfo) {

                                CameraHelper.myPoseInfo.setPose(poseInfo);
                                CameraHelper.myPoseInfo.setSourceWidth(CameraHelper.mCacheBitmap.getWidth());
                                CameraHelper.myPoseInfo.setSourceHeight(CameraHelper.mCacheBitmap.getHeight());

                                CameraHelper.myPoseInfo.setTargetWidth(targetWidth);
                                CameraHelper.myPoseInfo.setTargetHeight(targetHeight);

                                CameraHelper.detectResult.setBitmap(CameraHelper.mCacheBitmap);
                                CameraHelper.detectResult.setPoseInfo(CameraHelper.myPoseInfo);

                                onDetectResult(CameraHelper.detectResult);
                            }
                        }
                );

            }
        });


        if (null != CameraHelper.camera2DataGeter) {
            CameraHelper.mCacheBitmap = Bitmap.createBitmap(CameraHelper.cameraWidth, CameraHelper.cameraHeight, Bitmap.Config.ARGB_8888);
            //3.打开相机
            CameraHelper.mat = new Mat();
            CameraHelper.camera2DataGeter.enableView();
            CameraHelper.blurBitmap = Bitmap.createBitmap(CameraHelper.cameraWidth / 2, CameraHelper.cameraHeight / 2, Bitmap.Config.ARGB_8888);
        }

    }


    private void onDetectResult(DetectResult result) {
        runOnUiThread(() -> {

            long now = System.currentTimeMillis();
            long delta = now - lastTime;
            if (delta >= 1000) {
                int fps = CameraHelper.frameCount;
                String fpsAndPixs = "摄像头信息:" + CameraHelper.widthSource + "x" + CameraHelper.heightSource + "  fps: " + fps;
                textViewFps.setText(fpsAndPixs);
                lastTime = now;
                CameraHelper.frameCount = 0;
            }
            CameraHelper.frameCount++;
            imageViewShowTarget.setDetectResult(result);
        });
    }

}