package com.demo.cv42.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.demo.cv42.App;
import com.demo.cv42.R;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCamera2View;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;

import static android.Manifest.permission.CAMERA;

public class FrontCameraPortraitActivity extends Activity {

    private String tag = "cv42demo";
    private JavaCamera2View javaCameraView;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 200;
    private boolean isInitSuccess = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_front_camera);
        ((TextView) findViewById(R.id.textView)).setText("Opencv版本:" + OpenCVLoader.OPENCV_VERSION);
        javaCameraView = findViewById(R.id.cameraView);
        javaCameraView.setCameraIndex(1);
        requestPermission();

        ((TextView)findViewById(R.id.textViewTitleDesc)).setText("默认竖屏不能铺满全屏");
    }

    private void requestPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
            } else {
                initCamera();
            }
        } else {
            initCamera();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (null != javaCameraView && isInitSuccess) {
            javaCameraView.enableView();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (null != javaCameraView) {
            javaCameraView.disableView();
        }
    }


    private boolean initCamera() {
        Log.e(App.Companion.getTag(), "isinit success:" + isInitSuccess);
        if (null != javaCameraView) {

            javaCameraView.post(new Runnable() {
                @Override
                public void run() {

                    javaCameraView.setCameraPermissionGranted();//需要已经授权可以使用摄像头再调用这个方法
                    isInitSuccess = OpenCVLoader.initDebug();

                    final int cameraViewWidth = javaCameraView.getWidth();
                    final int cameraViewHeight = javaCameraView.getHeight();

                    Log.e(App.Companion.getTag(), "onCameraView:" + cameraViewWidth + " x " + cameraViewHeight);

                    javaCameraView.setCvCameraViewListener(new CameraBridgeViewBase.CvCameraViewListener2() {
                        @Override
                        public void onCameraViewStarted(int width, int height) {
                            javaCameraView.AllocateCache2(height, width);//注意这里width*height是反的，原因是原生的CameraView 绘制的时候 Mat转Bitmap需要构造旋转后的大小的Bitmap
//                            javaCameraView.AllocateCache2(cameraViewWidth, cameraViewHeight);//注意这里width*height是反的，原因是原生的CameraView 绘制的时候 Mat转Bitmap需要构造旋转后的大小的Bitmap

                        }

                        @Override
                        public void onCameraViewStopped() {

                        }

                        @Override
                        public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {


                            final Mat src = inputFrame.rgba();
                            Mat rotatedMat = null;
                            //厂商把后置摄像头映射成前置摄像头了
                            Core.flip(src, src, 1);//使用了前置摄像头，又设置了drawSource=false,需要翻转左右，不然旋转90度之后会有问题
                            rotatedMat = new Mat(src.cols(), src.rows(), src.type());
                            Core.rotate(src, rotatedMat, Core.ROTATE_90_CLOCKWISE);//旋转之后得到正确的预览图像

//                            float resize = 5f;
//                            Size dstSize = new Size((int) (rotatedMat.rows() * resize), (int) (rotatedMat.cols() * resize));
//                            Mat resizeDst = new Mat((int)dstSize.width,(int)dstSize.height,rotatedMat.type());
//                            Imgproc.resize(rotatedMat, resizeDst, dstSize);
//
//                            Rect rect = new Rect(0, 0, cameraViewHeight,cameraViewWidth);
//
//                            Mat resultMat = new Mat(resizeDst, rect);
                            return rotatedMat;
                        }

                    });
                    javaCameraView.setCameraPermissionGranted();//需要已经授权可以使用摄像头再调用这个方法
                    javaCameraView.enableView();
                    javaCameraView.enableFpsMeter();

                }
            });

        }

        return isInitSuccess;
    }


    @Override
    @TargetApi(Build.VERSION_CODES.M)
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initCamera();
        }
    }

}
