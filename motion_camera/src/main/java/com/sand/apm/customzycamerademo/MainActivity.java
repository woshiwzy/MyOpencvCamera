package com.sand.apm.customzycamerademo;

import android.graphics.Bitmap;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
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
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class MainActivity extends BaseTestActivity {

    public ImageView imageViewPreview;
    public PoseImageView imageViewShowTarget;
    public TextView textViewFps, textViewPhotoInfo, textViewScaleLabel, textViewPreviewInfo, textViewCameraOutputInfo;
    public CheckBox checkBoxMirrorH, checkBoxMirrorV, checkBoxGray, checkBoxShowSource, checkBoxGrayInput, checkBoxDouble;
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
        checkBoxDouble = findViewById(R.id.checkBoxDouble);

        checkBoxDouble.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                imageViewShowTarget.setShowTwo(isChecked);
            }
        });

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

        if (null == CameraHelper.camera2DataGeter) {
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
        }


        if (null != CameraHelper.camera2DataGeter) {
            //3.打开相机
            CameraHelper.camera2DataGeter.enableView();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
//        initCamera();

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (null != CameraHelper.camera2DataGeter) {
            CameraHelper.camera2DataGeter.disableView();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != CameraHelper.camera2DataGeter) {
            CameraHelper.camera2DataGeter.disconnectCamera();
        }
    }


    /**
     * 处理姿态识别
     *
     * @param image
     */
    Mat sourceMat = null;

    public void processPose1(Image image) {
        if (CameraHelper.processIng) {
            return;
        }
        if (null == CameraHelper.camera2Frame) {
            CameraHelper.camera2Frame = new Camera2DataGeter.JavaCamera2Frame(image);
        } else {
            CameraHelper.camera2Frame.setImage(image);
        }

        long startGetImg = System.currentTimeMillis();

        if (checkBoxGray.isChecked()) {//是否处理灰度
            sourceMat = CameraHelper.camera2Frame.gray();
        } else {
            sourceMat = CameraHelper.camera2Frame.rgba();
        }

        long costGetMat = System.currentTimeMillis() - startGetImg;
        Log.d(App.tag, "Mat-1 获取耗时:" + costGetMat);


//        long yuvGetStart=System.currentTimeMillis();
//        if(null==yuvBuffer){
//            int i420Size = image.getWidth() * image.getHeight() * 3 / 2;
//            yuvBuffer=new byte[i420Size];
//        }
//
//        yuvToRgbConverter.imageToByteBuffer(image,yuvBuffer);
//        long yuvCost=System.currentTimeMillis()-yuvGetStart;
//        Log.d(App.tag, "Mat-2 获取耗时:" + yuvCost);


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

        long startCrop = System.currentTimeMillis();
        //将图像按照目标显示View的大小做一个最佳调整
        Mat bestMat = CameraHelper.fitMat2Target(sourceMat, targetWidth, targetHeight);
        long cropTime = System.currentTimeMillis() - startCrop;
        Log.d(App.tag, "最佳裁剪耗时：" + cropTime);
        if (checkBoxShowSource.isChecked()) {//要展示原图，而非缩放后的图片
            CameraHelper.finalShowBitmap = CameraHelper.getCacheBitmap(bestMat.width(), bestMat.height());
            Utils.matToBitmap(bestMat, CameraHelper.finalShowBitmap);//得到原始的最佳图像
        }

//        if (checkBoxShowSource.isChecked()) {//要展示原图，而非缩放后的图片
//            CameraHelper.finalShowBitmap = CameraHelper.getCacheBitmap(bestMat.width(), bestMat.height());
//            Utils.matToBitmap(bestMat, CameraHelper.finalShowBitmap);//得到原始的最佳图像
//        }


        long aiFitStart = System.currentTimeMillis();
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

        long aiCostTime = System.currentTimeMillis() - aiFitStart;
        Log.d(App.tag, "AI裁剪耗时:" + (aiCostTime));

//        //如果没有进行灰度处理，但是输入需要灰度
        if (checkBoxGrayInput.isChecked() && !checkBoxGray.isChecked()) {
            Imgproc.cvtColor(dstMat, dstMat, Imgproc.COLOR_RGB2GRAY);
        }

        //rgb转yuv数组
//        Imgproc.cvtColor(dstMat,dstMat,Imgproc.COLOR_YUV2RGB_NV21);
//        byte [] bytes_data=new byte[(int)dstMat.total()];
//        dstMat.get(0,0,bytes_data);


        if (!checkBoxDouble.isChecked()) {//单人模式

            long prepareStart = System.currentTimeMillis();
            CameraHelper.widthSource = dstMat.width();
            CameraHelper.heightSource = dstMat.height();
            Bitmap targetBitmap = CameraHelper.getCacheBitmap(dstMat.width(), dstMat.height());
            Utils.matToBitmap(dstMat, targetBitmap);
            dstMat.release();
            //送入AI前缩放处理结束------------
            //送入AI处理的图片永远需要缩放后的图片
            InputImage inputImage = InputImage.fromBitmap(targetBitmap, 0);
            long prepareEnd = System.currentTimeMillis() - prepareStart;
            Log.d(App.tag, "Ai准备耗时:" + prepareEnd);

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


        } else if (checkBoxDouble.isChecked()) {//双人模式

            Rect leftRect = new Rect(0, 0, dstMat.width() / 2, dstMat.height());
            Mat leftMat = dstMat.submat(leftRect);
            Bitmap leftBitmap = getLeftBitmap(leftMat.width(), leftMat.height());
            Utils.matToBitmap(leftMat, leftBitmap);
            leftMat.release();

            Rect rightRect = new Rect(dstMat.width() / 2, 0, dstMat.width() / 2, dstMat.height());
            Mat rightMat = dstMat.submat(rightRect);
            Bitmap rightBitmap = getRightBitmap(rightMat.width(), rightMat.height());
            Utils.matToBitmap(rightMat, rightBitmap);
            rightMat.release();

            Bitmap aitotalBitmap = CameraHelper.getCacheBitmap(dstMat.width(), dstMat.height());
            Utils.matToBitmap(dstMat, aitotalBitmap);
            dstMat.release();

            CameraHelper.process(CameraHelper.finalShowBitmap, aitotalBitmap, leftBitmap, rightBitmap, aiPoseProcessCallBack);
        }

    }

    /**
     * 双人回调处理结果
     */
    private AiPoseProcessCallBack aiPoseProcessCallBack = new AiPoseProcessCallBack() {

        @Override
        public void onSuccessDouble(Bitmap sourceBitmapTotal, Bitmap bitmapTotal, Pose poseLeft, Bitmap imageLeft, Pose poseRight, Bitmap imageRight) {

            int targetWidth = imageViewShowTarget.getWidth() >> 1;
            int targetHeight = imageViewShowTarget.getHeight();

            CameraHelper.detectResultDouble.getLeftPoseInfo().setPose(poseLeft);
            CameraHelper.detectResultDouble.getLeftPoseInfo().setSourceWidth(imageLeft.getWidth());
            CameraHelper.detectResultDouble.getLeftPoseInfo().setSourceHeight(imageLeft.getHeight());
            CameraHelper.detectResultDouble.getLeftPoseInfo().setTargetWidth(targetWidth);
            CameraHelper.detectResultDouble.getLeftPoseInfo().setTargetHeight(targetHeight);

            //===========================================================================================
            CameraHelper.detectResultDouble.getRightPoseInfo().setPose(poseRight);
            CameraHelper.detectResultDouble.getRightPoseInfo().setSourceWidth(imageRight.getWidth());
            CameraHelper.detectResultDouble.getRightPoseInfo().setSourceHeight(imageRight.getHeight());
            CameraHelper.detectResultDouble.getRightPoseInfo().setTargetWidth(targetWidth);
            CameraHelper.detectResultDouble.getRightPoseInfo().setTargetHeight(targetHeight);

            //============================================================================================
            CameraHelper.detectResultDouble.setBitmapTotal(bitmapTotal);
            CameraHelper.detectResultDouble.setSourceBitmapTotal(sourceBitmapTotal);
            CameraHelper.detectResultDouble.setShowSource(checkBoxShowSource.isChecked());

            onDetectResult(CameraHelper.detectResultDouble);

        }
    };


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

                String fpsAndPixs = null;
                if (!result.isDouble()) {

                    fpsAndPixs = "AI消耗分辨率:" + CameraHelper.widthSource + "x" + CameraHelper.heightSource + "\n处理速度fps: " +
                            fps + "\n显示的图像大小:" + result.getBitmap().getWidth() + "X" + result.getBitmap().getHeight();
//                String fpsAndPixs = "处理速度: " + fps + "fps";

                } else {

                    fpsAndPixs = "AI消耗分辨率:" + result.getBitmapTotal().getWidth() + "x" + result.getBitmapTotal().getHeight() + "\n处理速度fps: " +
                            fps + "\n显示的图像大小:2X(" + result.getLeftPoseInfo().getTargetWidth() + "x" + result.getLeftPoseInfo().getTargetHeight() + ")";
//                String fpsAndPixs = "处理速度: " + fps + "fps";

                }

                textViewFps.setText(fpsAndPixs);
                lastProcessTime = now;
                CameraHelper.frameCount = 0;
            }
            imageViewShowTarget.setDetectResult(result);


        });
    }


    private Bitmap leftBitmap;

    private Bitmap getLeftBitmap(int width, int height) {
        if (null == leftBitmap || leftBitmap.getWidth() != width || leftBitmap.getHeight() != height) {
            if (null != leftBitmap) {
                leftBitmap.recycle();
                leftBitmap = null;
            }
            leftBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        }

        return leftBitmap;
    }


    private Bitmap rightBitmap;

    private Bitmap getRightBitmap(int width, int height) {
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