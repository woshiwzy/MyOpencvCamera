package com.sand.apm.customzycamerademo;

import android.graphics.Bitmap;
import android.graphics.PointF;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.mlkit.vision.pose.Pose;
import com.jf.lib.pose.JfPoseInfo;
import com.jf.lib.pose.JfPoseKeyPoint;
import com.jf.lib.pose.JfPoseSkeleton;
import com.sand.apm.customzycamerademo.custom.AiPoseProcessCallBack;
import com.sand.apm.customzycamerademo.custom.BaseDetectResult;
import com.sand.apm.customzycamerademo.custom.DetectResult;
import com.sand.apm.customzycamerademo.custom.OnImageCallBackListener;
import com.sand.apm.customzycamerademo.custom.PoseImageView;
import com.sand.apm.customzycamerademo.util.VisualizationUtils;
import com.tensorflow.lite.data.BodyPart;
import com.tensorflow.lite.data.KeyPoint;
import com.tensorflow.lite.data.Person;
import com.zy.sport.eu.ropeskip.EuRopeSkip;
import com.zy.sport.eu.ropeskip.EuRopeSkipCallback;
import com.zy.sport.eu.ropeskip.EuRopeSkipParam;
import com.zy.sport.eu.ropeskip.EuRopeSkipResult;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoWriter;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseTestActivity {

    public ImageView imageViewPreview;
    public PoseImageView imageViewShowTarget;
    public TextView textViewFps, textViewPhotoInfo, textViewScaleLabel, textViewPreviewInfo, textViewCameraOutputInfo;
    public CheckBox checkBoxMirrorH, checkBoxMirrorV, checkBoxGray, checkBoxShowSource, checkBoxGrayInput, checkBoxDouble, checkBoxMoveNet;
    public long lastProcessTime = 0, lastPhotoTime = 0;
    private int photoCount = 0;
    private SeekBar seekBar;
    private CameraHelper cameraHelper;
    private Button buttonClose;
    private TextView texteViewCountInfo;
    private int count=0;

    private JfPoseInfo poseInfo = new JfPoseInfo();
    private EuRopeSkip euRopeSkip=new EuRopeSkip(new EuRopeSkipCallback() {
        @Override
        public void onRopeSkipComplete(EuRopeSkipResult result) {
            count++;
            texteViewCountInfo.post(()->{
                texteViewCountInfo.setText("计数:"+ count);
            });
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        texteViewCountInfo=findViewById(R.id.texteViewCountInfo);
        buttonClose = findViewById(R.id.buttonClose);
        buttonClose.setOnClickListener(v -> finish());
        euRopeSkip.setRopeSkipParam(new EuRopeSkipParam());

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
            cameraHelper.setResolution(320, 240);
        });

        findViewById(R.id.button480).setOnClickListener(view -> { //480X640
            cameraHelper.setResolution(640, 480);
        });

        findViewById(R.id.button1280).setOnClickListener(view -> {
            cameraHelper.setResolution(1280, 720);
        });

        findViewById(R.id.button1080).setOnClickListener(view -> {
            cameraHelper.setResolution(1920, 1080);
        });

        findViewById(R.id.button2160).setOnClickListener(v -> cameraHelper.setResolution(3840, 2160));

        findViewById(R.id.buttonSwitchCamera).setOnClickListener(view -> {
            cameraHelper.toogleCamera();
        });

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
    }

    private void initCamera() {
        if (null == cameraHelper) {
            cameraHelper = new CameraHelper(this);
            cameraHelper.initCamera(new OnImageCallBackListener() {
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

                    processPose(image, checkBoxGray.isChecked(),
                            checkBoxMirrorH.isChecked(),
                            checkBoxMirrorV.isChecked(),
                            getScale(), checkBoxShowSource.isChecked(), checkBoxDouble.isChecked(), checkBoxGrayInput.isChecked(),
                            imageViewShowTarget.getWidth(),
                            imageViewShowTarget.getHeight(), checkBoxMoveNet.isChecked()
                    );//处理姿态识别方式1

                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraHelper.enablePreview();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraHelper.disablePreview();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraHelper.disconnectCamera();
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
    public void processPose(Image image, boolean gray, boolean mirrorH, boolean mirrorV, float scale, boolean showSource, boolean doubleModule, boolean inputGray, int targetWidth, int targetHeight, boolean moveNet) {
        if (cameraHelper.isProcessIng()) {
            return;
        }

        long startGetImg = System.currentTimeMillis();
        Mat sourceMat = cameraHelper.getSourceMat(gray, image);
        long costGetMat = System.currentTimeMillis() - startGetImg;
        Log.d(App.tag, "Mat-1 获取耗时:" + costGetMat);
        if (mirrorH) {//实处处理镜像
            Core.flip(sourceMat, sourceMat, 1);
        }

        if (mirrorV) {//垂直镜像
            Core.flip(sourceMat, sourceMat, -1);
        }
        //targetWidth 可以自由控制
        cameraHelper.setWidthHeightTarget(targetWidth, targetHeight);

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
        Mat bestMat = cameraHelper.fitMat2Target(sourceMat, targetWidth, targetHeight);
        long cropTime = System.currentTimeMillis() - startCrop;
        Log.d(App.tag, "最佳裁剪耗时：" + cropTime);
        if (showSource) {//要展示原图，而非缩放后的图片
            CameraHelper.sourceBigBitmap = CameraHelper.getCacheBitmap(bestMat.width(), bestMat.height(), Bitmap.Config.RGB_565);
            Utils.matToBitmap(bestMat, CameraHelper.sourceBigBitmap);//得到原始的最佳图像
        }

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
                cameraHelper.setWidthHeightSource(dstMat.width(), dstMat.height());
                Bitmap targetBitmap = CameraHelper.getCacheBitmap(dstMat.width(), dstMat.height(), Bitmap.Config.RGB_565);
                Utils.matToBitmap(dstMat, targetBitmap);
                dstMat.release();
                cameraHelper.processWithMoveNetSingle(CameraHelper.sourceBigBitmap, targetBitmap, new AiPoseProcessCallBack() {
                    @Override
                    public void onSuccessMoveNetSingle(Bitmap bitmapSource, Bitmap bitmapInput, ArrayList<Person> persons) {

                        runOnUiThread(() -> {
                            cameraHelper.getDetectResult().clear();
                            cameraHelper.getDetectResult().setShowSource(showSource);
                            cameraHelper.getDetectResult().setBitmap(showSource ? bitmapSource : bitmapInput);
                            cameraHelper.getDetectResult().setSourceType(BaseDetectResult.SOURCE_TYPE_MOVE_NET_SINGLE);
                            cameraHelper.getDetectResult().getPoseInfo().setDimenInfo(bitmapInput.getWidth(), bitmapInput.getHeight(), targetWidth, targetHeight);
                            cameraHelper.getDetectResult().setBitmap(VisualizationUtils.INSTANCE.drawBodyKeypoints(cameraHelper.getDetectResult().getBitmap(), persons, false));
                            onDetectResult(cameraHelper.getDetectResult());
                        });

                    }
                });

            } else {//双人模式
                cameraHelper.setWidthHeightSource(dstMat.width(), dstMat.height());
                Bitmap targetBitmap = CameraHelper.getCacheBitmap(dstMat.width(), dstMat.height(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(dstMat, targetBitmap);
                dstMat.release();
                CameraHelper.processWithMoveNetDouble(CameraHelper.sourceBigBitmap, targetBitmap, new AiPoseProcessCallBack() {
                    @Override
                    public void onSuccessMoveNetSingle(Bitmap bitmapSource, Bitmap bitmapInput, ArrayList<Person> persons) {
                        runOnUiThread(() -> {

                            String imageId=String.valueOf(System.currentTimeMillis());
                            poseInfo.setKey(imageId);
                            poseInfo.setImageWidth(bitmapInput.getWidth());
                            poseInfo.setImageHeight(bitmapInput.getHeight());
                            poseInfo.setPoseBoxing(null);
                            poseInfo.setFrom("");
                            poseInfo.setPlayer("");
                            poseInfo.getSkeletons().clear();
                            // 骨骼处理
                            for (Person person : persons) {
                                if (person.getScore() < 0.14) continue;
                                JfPoseSkeleton skeleton = new JfPoseSkeleton();
                                skeleton.setKey(imageId);
                                skeleton.setPlayer(String.valueOf(person.getId()));
                                skeleton.setImageWidth(bitmapInput.getWidth());
                                skeleton.setImageHeight(bitmapInput.getHeight());
                                skeleton.setFrom("");
                                fillKeyPoints(skeleton, person.getKeyPoints());
                                poseInfo.getSkeletons().add(skeleton);
                            }

                            if(null!=euRopeSkip){
                                euRopeSkip.onProcessor(poseInfo);
                            }

                            cameraHelper.getDetectResult().clear();
                            cameraHelper.getDetectResult().setShowSource(showSource);
                            cameraHelper.getDetectResult().setBitmap(showSource ? bitmapSource : bitmapInput);
                            cameraHelper.getDetectResult().setSourceType(BaseDetectResult.SOURCE_TYPE_MOVE_NET_DOUBLE);
                            cameraHelper.getDetectResult().getPoseInfo().setDimenInfo(bitmapInput.getWidth(), bitmapInput.getHeight(), targetWidth, targetHeight);
                            cameraHelper.getDetectResult().setBitmap(VisualizationUtils.INSTANCE.drawBodyKeypoints(cameraHelper.getDetectResult().getBitmap(), persons, false));
                            onDetectResult(cameraHelper.getDetectResult());
                        });
                    }
                });
            }
        } else {
            //默认的mp
            if (!doubleModule) {//单人模式
                long prepareStart = System.currentTimeMillis();
                cameraHelper.setWidthHeightSource(dstMat.width(), dstMat.height());
                Bitmap targetBitmap = cameraHelper.getCacheBitmap(dstMat.width(), dstMat.height(), Bitmap.Config.RGB_565);
                Utils.matToBitmap(dstMat, targetBitmap);
                dstMat.release();
                //送入AI前缩放处理结束------------
                //送入AI处理的图片永远需要缩放后的图片
                long prepareEnd = System.currentTimeMillis() - prepareStart;
                Log.d(App.tag, "Ai准备耗时:" + prepareEnd);
                long recStart = System.currentTimeMillis();
                cameraHelper.process(CameraHelper.sourceBigBitmap, targetBitmap, new AiPoseProcessCallBack() {

                    @Override
                    public void onSuccess(Pose pose, Bitmap bitmapSourceBig, Bitmap aiInput) {
                        long recEnd = System.currentTimeMillis();
                        Log.d(App.tag, "Ai识别耗时:" + (recEnd - recStart));

                        cameraHelper.getDetectResult().clear();
                        cameraHelper.getDetectResult().getPoseInfo().setPose(pose);
                        cameraHelper.getDetectResult().setShowSource(showSource);
                        cameraHelper.getDetectResult().getPoseInfo().setDimenInfo(aiInput.getWidth(), aiInput.getHeight(), targetWidth, targetHeight);
                        cameraHelper.getDetectResult().setSourceType(BaseDetectResult.SOURCE_TYPE_MP_SINGLE);
                        cameraHelper.getDetectResult().setBitmap(showSource ? bitmapSourceBig : aiInput);

                        onDetectResult(cameraHelper.getDetectResult());
                    }
                });

            } else if (doubleModule) {//双人模式
                Rect leftRect = new Rect(0, 0, dstMat.width() / 2, dstMat.height());
                Mat leftMat = dstMat.submat(leftRect);
                Bitmap leftBitmap = CameraHelper.getLeftBitmap(leftMat.width(), leftMat.height());
                Utils.matToBitmap(leftMat, leftBitmap);
                leftMat.release();

                Rect rightRect = new Rect(dstMat.width() / 2, 0, dstMat.width() / 2, dstMat.height());
                Mat rightMat = dstMat.submat(rightRect);
                Bitmap rightBitmap = CameraHelper.getRightBitmap(rightMat.width(), rightMat.height());
                Utils.matToBitmap(rightMat, rightBitmap);
                rightMat.release();

                Bitmap aitotalBitmap = CameraHelper.getCacheBitmap(dstMat.width(), dstMat.height(), Bitmap.Config.RGB_565);
                Utils.matToBitmap(dstMat, aitotalBitmap);
                dstMat.release();

                cameraHelper.process(CameraHelper.sourceBigBitmap, aitotalBitmap, leftBitmap, rightBitmap, aiPoseProcessCallBack);
            }
        }
    }



    // 填充骨骼点
    protected final boolean fillKeyPoints(JfPoseSkeleton skeleton, List<KeyPoint> list) {
        if (list == null || list.isEmpty()) return false;
        for (KeyPoint kp : list) {
            PointF p = kp.getCoordinate();
            float score = kp.getScore();
            String name = kp.getBodyPart().name();
            if (name.equalsIgnoreCase(BodyPart.NOSE.name())) {
                skeleton.setNose(toJfPoseKeyPoint(p.x, p.y, score));
            } else if (name.equalsIgnoreCase(BodyPart.LEFT_EYE.name())) {
                skeleton.setLEye(toJfPoseKeyPoint(p.x, p.y, score));
            } else if (name.equalsIgnoreCase(BodyPart.RIGHT_EYE.name())) {
                skeleton.setREye(toJfPoseKeyPoint(p.x, p.y, score));
            } else if (name.equalsIgnoreCase(BodyPart.RIGHT_EAR.name())) {
                skeleton.setREar(toJfPoseKeyPoint(p.x, p.y, score));
            } else if (name.equalsIgnoreCase(BodyPart.LEFT_EAR.name())) {
                skeleton.setLEar(toJfPoseKeyPoint(p.x, p.y, score));
            } else if (name.equalsIgnoreCase(BodyPart.LEFT_SHOULDER.name())) {
                skeleton.setLShoulder(toJfPoseKeyPoint(p.x, p.y, score));
            } else if (name.equalsIgnoreCase(BodyPart.RIGHT_SHOULDER.name())) {
                skeleton.setRShoulder(toJfPoseKeyPoint(p.x, p.y, score));
            } else if (name.equalsIgnoreCase(BodyPart.LEFT_ELBOW.name())) {
                skeleton.setLElbow(toJfPoseKeyPoint(p.x, p.y, score));
            } else if (name.equalsIgnoreCase(BodyPart.RIGHT_ELBOW.name())) {
                skeleton.setRElbow(toJfPoseKeyPoint(p.x, p.y, score));
            } else if (name.equalsIgnoreCase(BodyPart.LEFT_WRIST.name())) {
                skeleton.setLWrist(toJfPoseKeyPoint(p.x, p.y, score));
            } else if (name.equalsIgnoreCase(BodyPart.RIGHT_WRIST.name())) {
                skeleton.setRWrist(toJfPoseKeyPoint(p.x, p.y, score));
            } else if (name.equalsIgnoreCase(BodyPart.LEFT_HIP.name())) {
                skeleton.setLHip(toJfPoseKeyPoint(p.x, p.y, score));
            } else if (name.equalsIgnoreCase(BodyPart.RIGHT_HIP.name())) {
                skeleton.setRHip(toJfPoseKeyPoint(p.x, p.y, score));
            } else if (name.equalsIgnoreCase(BodyPart.LEFT_KNEE.name())) {
                skeleton.setLKnee(toJfPoseKeyPoint(p.x, p.y, score));
            } else if (name.equalsIgnoreCase(BodyPart.RIGHT_KNEE.name())) {
                skeleton.setRKnee(toJfPoseKeyPoint(p.x, p.y, score));
            } else if (name.equalsIgnoreCase(BodyPart.LEFT_ANKLE.name())) {
                skeleton.setLAnkle(toJfPoseKeyPoint(p.x, p.y, score));
            } else if (name.equalsIgnoreCase(BodyPart.RIGHT_ANKLE.name())) {
                skeleton.setRAnkle(toJfPoseKeyPoint(p.x, p.y, score));
            }
        }

        // 预测下巴点
//        skeleton.setChin(predictChin(skeleton));

        return true;
    }


    protected JfPoseKeyPoint toJfPoseKeyPoint(float x, float y, float score) {
        return new JfPoseKeyPoint(x, y, score);
    }

    /**
     * 双人回调处理结果
     */
    private AiPoseProcessCallBack aiPoseProcessCallBack = new AiPoseProcessCallBack() {

        @Override
        public void onSuccessDouble(Bitmap sourceBitmapTotal, Bitmap bitmapTotal, Pose poseLeft, Bitmap imageLeft, Pose poseRight, Bitmap imageRight) {

            int targetWidth = imageViewShowTarget.getWidth() >> 1;
            int targetHeight = imageViewShowTarget.getHeight();

            cameraHelper.getDetectResult().clear();
            cameraHelper.getDetectResultDouble().getLeftPoseInfo().setPose(poseLeft);
            cameraHelper.getDetectResultDouble().getLeftPoseInfo().setDimenInfo(imageLeft.getWidth(), imageLeft.getHeight(), targetWidth, targetHeight);
            //===========================================================================================
            cameraHelper.getDetectResultDouble().getRightPoseInfo().setPose(poseRight);
            cameraHelper.getDetectResultDouble().getRightPoseInfo().setDimenInfo(imageRight.getWidth(), imageRight.getHeight(), targetWidth, targetHeight);
            //============================================================================================
            cameraHelper.getDetectResultDouble().setBitmapTotal(bitmapTotal);
            cameraHelper.getDetectResultDouble().setSourceBitmapTotal(sourceBitmapTotal);
            cameraHelper.getDetectResultDouble().setShowSource(checkBoxShowSource.isChecked());
            cameraHelper.getDetectResultDouble().setSourceType(BaseDetectResult.SOURCE_TYPE_MP_DOUBLE);
            onDetectResult(cameraHelper.getDetectResultDouble());
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


}