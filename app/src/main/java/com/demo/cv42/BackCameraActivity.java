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

public class BackCameraActivity extends Activity {

    private String tag = "cv42demo";
    private JavaCamera2View javaCameraView;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 200;
    private boolean isInitSuccess=false;


//    static {
//        Log.e(App.tag, "isinit success:" + OpenCVLoader.initDebug());
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_back_camera);
        ((TextView) findViewById(R.id.textView)).setText("Opencv版本:" + OpenCVLoader.OPENCV_VERSION);
        javaCameraView = findViewById(R.id.cameraView);
        javaCameraView.setCameraIndex(0);
        requestPermission();

        ((TextView)findViewById(R.id.textViewTitleDesc)).setText("后置横屏");
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
//                            javaCameraView.AllocateCache2(cameraViewWidth, cameraViewHeight);
                        }

                        @Override
                        public void onCameraViewStopped() {

                        }

                        @Override
                        public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

                            final Mat src = inputFrame.rgba();

                            return src;
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
