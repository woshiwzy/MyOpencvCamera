package com.demo.cv42.cv;

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
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import static android.Manifest.permission.CAMERA;

public class FrontCameraPortraitFullScreenActivity extends Activity {

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

        ((TextView)findViewById(R.id.textViewTitleDesc)).setText("前置摄像头竖屏全屏");
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

        final Point centerPoint = new Point();

        if (null != javaCameraView) {

            javaCameraView.post(new Runnable() {
                @Override
                public void run() {

                    javaCameraView.setCameraPermissionGranted();//需要已经授权可以使用摄像头再调用这个方法
                    isInitSuccess = OpenCVLoader.initDebug();

                    final int cameraViewWidth = javaCameraView.getWidth();
                    final int cameraViewHeight = javaCameraView.getHeight();

                    Log.e(App.tag, "onCameraView:" + cameraViewWidth + " x " + cameraViewHeight);

                    javaCameraView.setCvCameraViewListener(new CameraBridgeViewBase.CvCameraViewListener2() {
                        @Override
                        public void onCameraViewStarted(int width, int height) {
                            Log.e(App.tag, "onCameraViewStarted:" + width + " x " + height);
//                            javaCameraView.AllocateCache2(height, width);//注意这里width*height是反的，原因是原生的CameraView 绘制的时候 Mat转Bitmap需要构造旋转后的大小的Bitmap
                            javaCameraView.AllocateCache2(cameraViewWidth, cameraViewHeight);//注意这里width*height是反的，原因是原生的CameraView 绘制的时候 Mat转Bitmap需要构造旋转后的大小的Bitmap

                        }

                        @Override
                        public void onCameraViewStopped() {

                        }

                        @Override
                        public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {


                            final Mat src = inputFrame.rgba();

//                            Log.e(App.tag, "实际分辨率:" + src.cols() + " row:" + src.rows());

                            Mat rotatedMat = null;
                            //厂商把后置摄像头映射成前置摄像头了
                            Core.flip(src, src, 1);//使用了前置摄像头，又设置了drawSource=false,需要翻转左右，不然旋转90度之后会有问题
                            rotatedMat = new Mat(src.cols(), src.rows(), src.type());
                            Core.rotate(src, rotatedMat, Core.ROTATE_90_CLOCKWISE);//旋转之后得到正确的方向预览图像
                            //如果要铺满屏幕必须和AllocateCache2生成的bitmap缓存接受区域一样大的Mat

                            int sourceWidth = rotatedMat.cols();
                            int sourceHeight = rotatedMat.rows();

                            float scaleWidth = cameraViewWidth * 1.0f / sourceWidth;
                            float scaleHeight = cameraViewHeight * 1.0f / sourceHeight;

                            float maxScale = Math.max(scaleWidth, scaleHeight);//得到最大的放大比例按照最大的比例放大，保证高宽都能铺满
                            Log.d(App.tag, "scale:" + maxScale);

                            float resize = maxScale;
                            Size dstSize = new Size((int) (rotatedMat.cols() * resize), (int) (rotatedMat.rows() * resize));//缩放到目标大小

                            Mat resizeDst = new Mat((int) dstSize.height, (int) dstSize.width, rotatedMat.type());

                            Imgproc.resize(rotatedMat, resizeDst, dstSize);
                            boolean cropMid = true;

                            Rect rect = null;

                            if (cropMid) {
                                rect = new Rect(0, 0, cameraViewWidth, cameraViewHeight);//取左上角（0,0）到(cameraViewWidth, cameraViewHeight)的图像
                            } else {
                                centerPoint.x = resizeDst.cols() / 2;
                                centerPoint.y = resizeDst.rows() / 2;
                                int left = (int) (centerPoint.x - cameraViewWidth / 2);
                                int top = (int) (centerPoint.y - cameraViewHeight / 2);
                                rect = new Rect(left, top, cameraViewWidth, cameraViewHeight);//取中间
                            }

                            Mat resultMat = new Mat(resizeDst, rect);
                            rotatedMat.release();
                            resizeDst.release();
                            src.release();

                            return resultMat;
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
