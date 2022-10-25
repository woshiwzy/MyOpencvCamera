package com.sand.apm.customzycamerademo;

import android.graphics.Bitmap;
import android.media.Image;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.pose.Pose;
import com.sand.apm.customzycamerademo.custom.AiPoseProcessCallBack;
import com.sand.apm.customzycamerademo.custom.Camera2DataGeter;
import com.sand.apm.customzycamerademo.custom.CameraDataGeterBase;
import com.sand.apm.customzycamerademo.custom.CameraImageUtil;
import com.sand.apm.customzycamerademo.custom.DetectResult;
import com.sand.apm.customzycamerademo.custom.OnImageCallBackListener;
import com.sand.apm.customzycamerademo.custom.PoseImageView;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class MainActivity extends BaseTestActivity {

    public ImageView imageViewPreview;
    public PoseImageView imageViewShowTarget;
    public TextView textViewFps, textViewScaleLabel;
    public CheckBox checkBoxMirror, checkBoxGray;
    public long lastTime = 0;
    private SeekBar seekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        textViewScaleLabel = findViewById(R.id.textViewScaleLabel);
        textViewFps = findViewById(R.id.textViewFps);
        imageViewPreview = findViewById(R.id.imageViewPreview);
        imageViewShowTarget = findViewById(R.id.imageViewCenter);
        checkBoxMirror = findViewById(R.id.checkBoxMirror);
        checkBoxGray = findViewById(R.id.checkBoxGray);

        findViewById(R.id.button240).setOnClickListener(view -> {
            CameraHelper.camera2DataGeter.setResolution(320, 240);//240X320
        });

        findViewById(R.id.button480).setOnClickListener(view -> { //480X640
            CameraHelper.camera2DataGeter.setResolution(640, 480);
        });

        findViewById(R.id.button1280).setOnClickListener(view -> CameraHelper.camera2DataGeter.setResolution(1280, 720));

        findViewById(R.id.button1080).setOnClickListener(view -> {
            CameraHelper.camera2DataGeter.setResolution(1920, 1080);//1080X1920
        });

        findViewById(R.id.buttonSwitchCamera).setOnClickListener(view -> CameraHelper.camera2DataGeter.toogleCamera());

        seekBar = findViewById(R.id.seekBarScale);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                textViewScaleLabel.setText("缩放级别:" + i + "/" + seekBar.getMax());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        textViewScaleLabel.setText("缩放级别:" + seekBar.getProgress() + "/" + seekBar.getMax());
        requestPermissionAndInitCamera();
    }

    @Override
    public void onCameraGranted() {
        initCamera();
        CameraHelper.initAi();
    }

    private void initCamera() {

        CameraHelper.camera2DataGeter = new Camera2DataGeter(this, CameraDataGeterBase.CAMERA_ID_FRONT, CameraHelper.cameraWidth, CameraHelper.cameraHeight);
        CameraHelper.camera2DataGeter.configOrientation(getResources().getConfiguration());
        CameraHelper.camera2DataGeter.setOnImageCallBackListener(new OnImageCallBackListener() {
            @Override
            public void onImageCatch(Image image) {
                processPose1(image);//处理姿态识别方式1
//                processPose2(image);//处理姿态识别方式2
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


    private void processPose2(Image image) {

        CameraHelper.processInputImage(image, 0, new AiPoseProcessCallBack() {

            @Override
            public void onSuccess(Pose pose, InputImage inputImage) {
                if (null != pose) {
                    runOnUiThread(() -> {

                        byte[] bytes = CameraImageUtil.getBytesFromImageAsType(image, CameraImageUtil.YUV420SP);
                        int[] rgb = CameraImageUtil.decodeYUV420SP(bytes, inputImage.getWidth(), inputImage.getHeight());
//                        Bitmap resultMap= BitmapFactory.decodeByteArray(rgb,0,bytes.length);
                        Bitmap resultMap = null;
                        long now = System.currentTimeMillis();
                        long delta = now - lastTime;
                        if (delta >= 1000) {
                            int fps = CameraHelper.frameCount;
                            String fpsAndPixs = "图像信息:" + CameraHelper.widthSource + "x" + CameraHelper.heightSource + "  fps: " + fps;
                            textViewFps.setText(fpsAndPixs);
                            lastTime = now;
                            CameraHelper.frameCount = 0;
                        }
                        CameraHelper.frameCount++;
                        imageViewShowTarget.setImageBitmap(resultMap);
                    });

                }
            }

            @Override
            public void onFail(InputImage image) {

            }
        });

    }


    /**
     * 处理姿态识别
     *
     * @param image
     */
    public void processPose1(Image image) {
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

        Mat sourceMat = null;
        if (checkBoxGray.isChecked()) {//是否处理灰度
            sourceMat = CameraHelper.camera2Frame.gray();
        } else {
            sourceMat = CameraHelper.camera2Frame.rgba();
        }

        if (checkBoxMirror.isChecked()) {//实处处理镜像
            Core.flip(sourceMat, sourceMat, 1);
        }

        CameraHelper.widthSource = sourceMat.width();
        CameraHelper.heightSource = sourceMat.height();

        //targetWidth 可以自由控制
        int targetWidth = imageViewShowTarget.getWidth();
        int targetHeight = imageViewShowTarget.getHeight();
        if (targetWidth <= 0 || targetHeight <= 0) {
            return;
        }

        Mat dstMat = null;
        if (null != seekBar && 0 != getScale()) {
            //这里还可以做Mat缓存等优化
            int dstWidth = (int) (sourceMat.cols() * getScale());
            int dstHeight = (int) (sourceMat.rows() * getScale());
            dstMat = new Mat(dstHeight, dstWidth, sourceMat.type());
            Imgproc.resize(sourceMat, dstMat, new Size(dstWidth, dstHeight));
            CameraHelper.widthSource = dstMat.width();
            CameraHelper.heightSource = dstMat.height();
            sourceMat.release();
        } else {
            dstMat = sourceMat;
        }

        CameraHelper.fitMat2TargetAndDetect(dstMat, CameraHelper.widthSource, CameraHelper.heightSource, targetWidth, targetHeight,

                new AiPoseProcessCallBack() {

                    @Override
                    public void onSuccess(Pose poseInfo, InputImage image1) {

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


    private float getScale() {
        return (float) (seekBar.getProgress() * 1.0 / seekBar.getMax());
    }

    /**
     * 姿态检测结果
     *
     * @param result
     */
    public void onDetectResult(DetectResult result) {
        runOnUiThread(() -> {
            long now = System.currentTimeMillis();
            long delta = now - lastTime;
            if (delta >= 1000) {
                int fps = CameraHelper.frameCount;
                String fpsAndPixs = "图像信息:" + CameraHelper.widthSource + "x" + CameraHelper.heightSource + "  fps: " + fps;
                textViewFps.setText(fpsAndPixs);
                lastTime = now;
                CameraHelper.frameCount = 0;
            }
            CameraHelper.frameCount++;
            imageViewShowTarget.setDetectResult(result);
        });
    }


}