package com.sand.apm.customzycamerademo;

import android.graphics.Bitmap;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
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
import com.sand.apm.customzycamerademo.custom.DetectResult;
import com.sand.apm.customzycamerademo.custom.OnImageCallBackListener;
import com.sand.apm.customzycamerademo.custom.PoseImageView;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class MainActivity extends BaseTestActivity {

    public ImageView imageViewPreview;
    public PoseImageView imageViewShowTarget;
    public TextView textViewFps, textViewPhotoInfo, textViewScaleLabel, textViewPreviewInfo, textViewCameraOutputInfo;
    public CheckBox checkBoxMirrorH, checkBoxMirrorV, checkBoxGray, checkBoxShowSource, checkBoxGrayInput,checkBoxDouble;
    public long lastProcessTime = 0, lastPhotoTime = 0;
    private SeekBar seekBar;
    private int photoCount = 0;

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

        checkBoxMirrorH = findViewById(R.id.checkBoxMirrorH);
        checkBoxMirrorV = findViewById(R.id.checkBoxMirrorV);
        checkBoxGray = findViewById(R.id.checkBoxGray);
        checkBoxGrayInput = findViewById(R.id.checkBoxGrayInput);
        checkBoxDouble=findViewById(R.id.checkBoxDouble);

        checkBoxShowSource = findViewById(R.id.checkBoxShowSource);
        textViewPreviewInfo = findViewById(R.id.textViewPreviewInfo);
        textViewPhotoInfo = findViewById(R.id.textViewPhotoInfo);
        textViewCameraOutputInfo = findViewById(R.id.textViewCameraOutputInfo);

        imageViewShowTarget.post(() -> {
            textViewPreviewInfo.setText("预览View大小:" + imageViewShowTarget.getWidth() + "X" + imageViewShowTarget.getHeight());
        });

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

        findViewById(R.id.button2160).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CameraHelper.camera2DataGeter.setResolution(3840, 2160);
            }
        });

        findViewById(R.id.buttonSwitchCamera).setOnClickListener(view -> CameraHelper.camera2DataGeter.toogleCamera());

        seekBar = findViewById(R.id.seekBarScale);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                textViewScaleLabel.setText("级别:" + i + "/" + seekBar.getMax());
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
                photoCount++;
                long now = System.currentTimeMillis();
                if (lastPhotoTime == 0) {
                    lastPhotoTime = now;
                } else if ((now - lastPhotoTime) >= 1000) {
                    int fps = photoCount;
                    textViewPhotoInfo.post(() -> {
                        textViewPhotoInfo.setText("相机出图:" + fps + "fps");
                    });
                    photoCount = 0;
                    lastPhotoTime = now;

                }


                processPose1(image);//处理姿态识别方式1
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

        Mat sourceMat = null;
        if (checkBoxGray.isChecked()) {//是否处理灰度
            sourceMat = CameraHelper.camera2Frame.gray();
        } else {
            sourceMat = CameraHelper.camera2Frame.rgba();
        }
        if (checkBoxMirrorH.isChecked()) {//实处处理镜像
            Core.flip(sourceMat, sourceMat, 1);
        }

        if (checkBoxMirrorV.isChecked()) {//垂直镜像
            Core.flip(sourceMat, sourceMat, -1);
        }


        //targetWidth 可以自由控制
        int targetWidth = imageViewShowTarget.getWidth();
        int targetHeight = imageViewShowTarget.getHeight();

        CameraHelper.widthTarget = targetWidth;
        CameraHelper.heightTarget = targetHeight;

        if (targetWidth <= 0 || targetHeight <= 0) {
            return;
        }

        int cameraOutputWidth = sourceMat.width();
        int cameraOutputHeight = sourceMat.height();

        textViewCameraOutputInfo.post(() -> {
            String cameraInfo = cameraOutputWidth + "X" + cameraOutputHeight;
            textViewCameraOutputInfo.setText("相机输出图片大小:" + cameraInfo);
        });

        //将图像按照目标显示View的大小做一个最佳调整
        Mat bestMat = CameraHelper.fitMat2Target(sourceMat, targetWidth, targetHeight);


        if (checkBoxShowSource.isChecked()) {//要展示原图，而非缩放后的图片
            CameraHelper.finalShowBitmap = CameraHelper.getCacheBitmap(bestMat.width(), bestMat.height());
            Utils.matToBitmap(bestMat, CameraHelper.finalShowBitmap);//得到原始的最佳图像
        }

        //送入AI前缩放处理开始------------
        Mat dstMat = null;
        if (null != seekBar && 0 != getScale() && 1 != getScale()) {//在这个做缩放处理
            //这里还可以做Mat缓存等优化
            int dstWidth = (int) (bestMat.cols() * getScale());
            int dstHeight = (int) (bestMat.rows() * getScale());
            dstMat = new Mat(dstHeight, dstWidth, bestMat.type());
            Imgproc.resize(bestMat, dstMat, new Size(dstWidth, dstHeight));
            bestMat.release();
        } else {
            dstMat = bestMat;
        }

//        //如果没有进行灰度处理，但是输入需要灰度
        if (checkBoxGrayInput.isChecked() && !checkBoxGray.isChecked()) {
            Imgproc.cvtColor(dstMat, dstMat, Imgproc.COLOR_RGB2GRAY);
        }

        CameraHelper.widthSource = dstMat.width();
        CameraHelper.heightSource = dstMat.height();
        Bitmap targetBitmap = CameraHelper.getCacheBitmap(dstMat.width(), dstMat.height());
        Utils.matToBitmap(dstMat, targetBitmap);
        dstMat.release();
        //送入AI前缩放处理结束------------
        //送入AI处理的图片永远需要缩放后的图片


        //rgb转yuv数组
//        Imgproc.cvtColor(dstMat,dstMat,Imgproc.COLOR_YUV2RGB_NV21);
//        byte [] bytes_data=new byte[(int)dstMat.total()];
//        dstMat.get(0,0,bytes_data);

        InputImage inputImage = InputImage.fromBitmap(targetBitmap, 0);

        CameraHelper.process(inputImage, new AiPoseProcessCallBack() {
            @Override
            public void onSuccess(Pose poseInfo, InputImage image1) {

                CameraHelper.myPoseInfo.setPose(poseInfo);
                CameraHelper.myPoseInfo.setSourceWidth(targetBitmap.getWidth());
                CameraHelper.myPoseInfo.setSourceHeight(targetBitmap.getHeight());

                CameraHelper.myPoseInfo.setTargetWidth(targetWidth);
                CameraHelper.myPoseInfo.setTargetHeight(targetHeight);

                if (checkBoxShowSource.isChecked()) {
                    CameraHelper.detectResult.setBitmap(CameraHelper.finalShowBitmap);
                } else {
                    CameraHelper.detectResult.setBitmap(targetBitmap);
                }

                CameraHelper.detectResult.setPoseInfo(CameraHelper.myPoseInfo);
                onDetectResult(CameraHelper.detectResult);
            }
        });


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
            long delta = now - lastProcessTime;
            CameraHelper.frameCount++;
            if (delta >= 1000) {
                int fps = CameraHelper.frameCount;
                String fpsAndPixs = "AI消耗分辨率:" + CameraHelper.widthSource + "x" + CameraHelper.heightSource + "\n处理速度fps: " +
                        fps+"\n显示的图像大小:"+result.getBitmap().getWidth()+"X"+result.getBitmap().getHeight();
//                String fpsAndPixs = "处理速度: " + fps + "fps";
                textViewFps.setText(fpsAndPixs);
                lastProcessTime = now;
                CameraHelper.frameCount = 0;
            }

            imageViewShowTarget.setDetectResult(result);
        });
    }


}