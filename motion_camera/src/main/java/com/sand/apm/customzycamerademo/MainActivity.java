package com.sand.apm.customzycamerademo;

import android.graphics.Bitmap;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
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
import com.sand.apm.customzycamerademo.custom.BaseDetectResult;
import com.sand.apm.customzycamerademo.custom.Camera2DataGeter;
import com.sand.apm.customzycamerademo.custom.CameraDataGeterBase;
import com.sand.apm.customzycamerademo.custom.DetectResult;
import com.sand.apm.customzycamerademo.custom.OnImageCallBackListener;
import com.sand.apm.customzycamerademo.custom.PoseImageView;
import com.sand.apm.customzycamerademo.util.VisualizationUtils;
import com.tensorflow.lite.data.Person;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;

public class MainActivity extends BaseTestActivity {

    public ImageView imageViewPreview;
    public PoseImageView imageViewShowTarget;
    public TextView textViewFps, textViewPhotoInfo, textViewScaleLabel, textViewPreviewInfo, textViewCameraOutputInfo;
    public CheckBox checkBoxMirrorH, checkBoxMirrorV, checkBoxGray, checkBoxShowSource, checkBoxGrayInput, checkBoxDouble, checkBoxMoveNet;
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
        checkBoxMoveNet = findViewById(R.id.checkBoxMoveNet);

        checkBoxDouble.setOnCheckedChangeListener((buttonView, isChecked) -> imageViewShowTarget.setShowTwo(isChecked));

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
        CameraHelper.initAi(getApplicationContext());
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

                    processPose1(image, checkBoxGray.isChecked(),
                            checkBoxMirrorH.isChecked(),
                            checkBoxMirrorV.isChecked(),
                            getScale(), checkBoxShowSource.isChecked(), checkBoxDouble.isChecked(), checkBoxGrayInput.isChecked(),
                            imageViewShowTarget.getWidth(),
                            imageViewShowTarget.getHeight(), checkBoxMoveNet.isChecked()
                    );//处理姿态识别方式1
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
     * @param image        相机输入图
     * @param gray         是否进行灰度处理
     * @param mirrorH      是否处理水平镜像
     * @param mirrorV      是否处理垂直镜像
     * @param scale        缩放系数（缩放后传给AI）
     * @param showSource   是否显示原图（不是缩放后的图）
     * @param doubleModule 是否为双人模式
     * @param inputGray    是否让Ai处理灰度图
     * @param targetWidth  显示预览图的宽度
     * @param targetHeight 显示预览图的高度
     * @param moveNet      是否使用moveNet
     */
    public void processPose1(Image image, boolean gray, boolean mirrorH, boolean mirrorV, float scale, boolean showSource, boolean doubleModule, boolean inputGray, int targetWidth, int targetHeight, boolean moveNet) {
        if (CameraHelper.processIng) {
            return;
        }
        Mat sourceMat;

        long startGetImg = System.currentTimeMillis();

        if (null == CameraHelper.camera2Frame) {
            CameraHelper.camera2Frame = new Camera2DataGeter.JavaCamera2Frame(image);
        } else {
            CameraHelper.camera2Frame.setImage(image);
        }
        if (gray) {//是否处理灰度
            sourceMat = CameraHelper.camera2Frame.gray();
        } else {
            sourceMat = CameraHelper.camera2Frame.rgba();
        }

        long costGetMat = System.currentTimeMillis() - startGetImg;
        Log.d(App.tag, "Mat-1 获取耗时:" + costGetMat);

        if (mirrorH) {//实处处理镜像
            Core.flip(sourceMat, sourceMat, 1);
        }

        if (mirrorV) {//垂直镜像
            Core.flip(sourceMat, sourceMat, -1);
        }

        //targetWidth 可以自由控制
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
        if (showSource) {//要展示原图，而非缩放后的图片
            CameraHelper.finalShowBitmap = CameraHelper.getCacheBitmap(bestMat.width(), bestMat.height(),Bitmap.Config.RGB_565);
            Utils.matToBitmap(bestMat, CameraHelper.finalShowBitmap);//得到原始的最佳图像
        }

//        if (checkBoxShowSource.isChecked()) {//要展示原图，而非缩放后的图片
//            CameraHelper.finalShowBitmap = CameraHelper.getCacheBitmap(bestMat.width(), bestMat.height());
//            Utils.matToBitmap(bestMat, CameraHelper.finalShowBitmap);//得到原始的最佳图像
//        }


        long aiFitStart = System.currentTimeMillis();
        //送入AI前缩放处理开始------------
        Mat dstMat = null;
        if (0 != scale && 1 != scale) {//在这个做缩放处理
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
        //========================到此为止dstMat 就是送入AI的Mat=======================================
        //==========================================================================================
//        //如果没有进行灰度处理，但是输入需要灰度（貌似没啥效果）
        if (inputGray && gray) {
            Imgproc.cvtColor(dstMat, dstMat, Imgproc.COLOR_RGB2GRAY);
        }
        //rgb转yuv数组
//        Imgproc.cvtColor(dstMat,dstMat,Imgproc.COLOR_YUV2RGB_NV21);
//        byte [] bytes_data=new byte[(int)dstMat.total()];
//        dstMat.get(0,0,bytes_data);

        if (moveNet) {
            //使用movenet
            if (!doubleModule) {//单人模式
                CameraHelper.widthSource = dstMat.width();
                CameraHelper.heightSource = dstMat.height();
                Bitmap targetBitmap = CameraHelper.getCacheBitmap(dstMat.width(), dstMat.height(),Bitmap.Config.RGB_565);
                Utils.matToBitmap(dstMat, targetBitmap);
                dstMat.release();
                CameraHelper.processWithMoveNetSingle(CameraHelper.finalShowBitmap, targetBitmap, new AiPoseProcessCallBack() {
                    @Override
                    public void onSuccessMoveNetSingle(Bitmap bitmapSource, Bitmap bitmapInput, ArrayList<Person> persons) {

                        runOnUiThread(() -> {

                            CameraHelper.detectResult.clear();
                            CameraHelper.detectResult.setShowSource(showSource);
                            CameraHelper.detectResult.setBitmap(showSource ? bitmapSource : bitmapInput);

                            CameraHelper.detectResult.setSourceType(BaseDetectResult.SOURCE_TYPE_MOVE_NET_SINGLE);

                            CameraHelper.myPoseInfo.setSourceWidth(bitmapInput.getWidth());
                            CameraHelper.myPoseInfo.setSourceHeight(bitmapInput.getHeight());

                            CameraHelper.myPoseInfo.setTargetWidth(targetWidth);
                            CameraHelper.myPoseInfo.setTargetHeight(targetHeight);

                            CameraHelper.detectResult.setBitmap(VisualizationUtils.INSTANCE.drawBodyKeypoints(CameraHelper.detectResult.getBitmap(), persons, false));

                            onDetectResult(CameraHelper.detectResult);


                        });

                    }
                });


            } else {//双人模式
                CameraHelper.widthSource = dstMat.width();
                CameraHelper.heightSource = dstMat.height();
                Bitmap targetBitmap = CameraHelper.getCacheBitmap(dstMat.width(), dstMat.height(),Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(dstMat, targetBitmap);
                dstMat.release();
                CameraHelper.processWithMoveNetDouble(CameraHelper.finalShowBitmap, targetBitmap, new AiPoseProcessCallBack() {
                    @Override
                    public void onSuccessMoveNetSingle(Bitmap bitmapSource, Bitmap bitmapInput, ArrayList<Person> persons) {

                        runOnUiThread(() -> {

                            CameraHelper.detectResult.clear();
                            CameraHelper.detectResult.setShowSource(showSource);
                            CameraHelper.detectResult.setBitmap(showSource ? bitmapSource : bitmapInput);

                            CameraHelper.detectResult.setSourceType(BaseDetectResult.SOURCE_TYPE_MOVE_NET_DOUBLE);

                            CameraHelper.myPoseInfo.setSourceWidth(bitmapInput.getWidth());
                            CameraHelper.myPoseInfo.setSourceHeight(bitmapInput.getHeight());

                            CameraHelper.myPoseInfo.setTargetWidth(targetWidth);
                            CameraHelper.myPoseInfo.setTargetHeight(targetHeight);

                            CameraHelper.detectResult.setBitmap(VisualizationUtils.INSTANCE.drawBodyKeypoints(CameraHelper.detectResult.getBitmap(), persons, false));

                            onDetectResult(CameraHelper.detectResult);

                        });

                    }
                });


            }

        } else {
            //默认的mp
            if (!doubleModule) {//单人模式
                long prepareStart = System.currentTimeMillis();
                CameraHelper.widthSource = dstMat.width();
                CameraHelper.heightSource = dstMat.height();
                Bitmap targetBitmap = CameraHelper.getCacheBitmap(dstMat.width(), dstMat.height(),Bitmap.Config.RGB_565);
                Utils.matToBitmap(dstMat, targetBitmap);
                dstMat.release();
                //送入AI前缩放处理结束------------
                //送入AI处理的图片永远需要缩放后的图片
                InputImage inputImage = InputImage.fromBitmap(targetBitmap, 0);
                long prepareEnd = System.currentTimeMillis() - prepareStart;
                Log.d(App.tag, "Ai准备耗时:" + prepareEnd);
                long recStart = System.currentTimeMillis();
                CameraHelper.process(inputImage, new AiPoseProcessCallBack() {

                    @Override
                    public void onSuccess(Pose poseInfo, InputImage image1) {
                        long recEnd = System.currentTimeMillis();
                        Log.d(App.tag, "Ai识别耗时:" + (recEnd - recStart));

                        CameraHelper.detectResult.clear();
                        CameraHelper.myPoseInfo.setPose(poseInfo);
                        CameraHelper.myPoseInfo.setSourceWidth(targetBitmap.getWidth());
                        CameraHelper.myPoseInfo.setSourceHeight(targetBitmap.getHeight());

                        CameraHelper.myPoseInfo.setTargetWidth(targetWidth);
                        CameraHelper.myPoseInfo.setTargetHeight(targetHeight);
                        CameraHelper.detectResult.setShowSource(showSource);

                        CameraHelper.detectResult.setSourceType(BaseDetectResult.SOURCE_TYPE_MP_SINGLE);

                        if (showSource) {
                            CameraHelper.detectResult.setBitmap(CameraHelper.finalShowBitmap);
                        } else {
                            CameraHelper.detectResult.setBitmap(targetBitmap);
                        }

                        CameraHelper.detectResult.setPoseInfo(CameraHelper.myPoseInfo);
                        onDetectResult(CameraHelper.detectResult);
                    }
                });

            } else if (doubleModule) {//双人模式
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

                Bitmap aitotalBitmap = CameraHelper.getCacheBitmap(dstMat.width(), dstMat.height(),Bitmap.Config.RGB_565);
                Utils.matToBitmap(dstMat, aitotalBitmap);
                dstMat.release();
                CameraHelper.process(CameraHelper.finalShowBitmap, aitotalBitmap, leftBitmap, rightBitmap, aiPoseProcessCallBack);
            }
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

            CameraHelper.detectResult.clear();

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

            CameraHelper.detectResult.setSourceType(BaseDetectResult.SOURCE_TYPE_MP_DOUBLE);

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