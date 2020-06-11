package com.demo.cv42;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCamera2View;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import static android.Manifest.permission.CAMERA;

public class SwitchCameraActivity extends Activity {

    private String tag = "cv42demo";
    private JavaCamera2View javaCameraView;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 200;
    private boolean isInitSuccess=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_camera);
        ((TextView) findViewById(R.id.textView)).setText("Opencv版本:" + OpenCVLoader.OPENCV_VERSION);
        javaCameraView = findViewById(R.id.cameraView);
        javaCameraView.setCameraIndex(0);
        requestPermission();
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
        Log.e(App.tag, "isinit success:" + isInitSuccess);
        if (  null != javaCameraView) {

            javaCameraView.post(new Runnable() {
                @Override
                public void run() {

                    javaCameraView.setCameraPermissionGranted();//需要已经授权可以使用摄像头再调用这个方法
                    isInitSuccess = OpenCVLoader.initDebug();

                    final int cameraViewWidth = javaCameraView.getWidth();
                    final int cameraViewHeight = javaCameraView.getHeight();

                    javaCameraView.setCvCameraViewListener(new CameraBridgeViewBase.CvCameraViewListener2() {
                        @Override
                        public void onCameraViewStarted(int width, int height) {
                            Log.e(App.tag, "onCameraViewStarted:" + width + " x " + height);
                            javaCameraView.AllocateCache2(cameraViewWidth, cameraViewHeight);
                        }

                        @Override
                        public void onCameraViewStopped() {

                        }

                        @Override
                        public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

                            final Mat src = inputFrame.rgba();
                            Mat rotatedMat = null;

                        //如果在这里改变了图片的宽度和高度就需要调用javaCameraView.AllocateCache2() 修改缓存图片的大小
                        ////厂商把后置摄像头映射成前置摄像头了
                        ////Core.flip(src, src, 1);//使用了前置摄像头，又设置了drawSource=false,需要翻转左右，不然旋转90度之后会有问题
                            rotatedMat = new Mat(src.cols(), src.rows(), src.type());
                            Core.rotate(src, rotatedMat, Core.ROTATE_90_CLOCKWISE);//旋转之后得到正确的预览图像
                            if (rotatedMat.width() < 0 || rotatedMat.height() < 0) {
                                return null;
                            }

                            Mat newMat = new Mat(cameraViewHeight, cameraViewWidth, src.type());
                            Imgproc.resize(rotatedMat, newMat, new Size(cameraViewWidth, cameraViewHeight));
                            return newMat;
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
