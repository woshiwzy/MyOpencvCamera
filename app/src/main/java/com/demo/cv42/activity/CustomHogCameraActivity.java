package com.demo.cv42.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.demo.cv42.App;
import com.demo.cv42.R;
import com.demo.cv42.view.CustomJavaCameraView;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.HOGDescriptor;

import java.util.List;
import java.util.Scanner;

import androidx.annotation.NonNull;

import static android.Manifest.permission.CAMERA;
import static org.opencv.core.CvType.CV_8U;
import static org.opencv.core.CvType.CV_8UC1;
import static org.opencv.core.CvType.CV_8UC3;

public class CustomHogCameraActivity extends Activity {

    private String tag = "cv42demo";
    private CustomJavaCameraView javaCameraView;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 200;
    private boolean isInitSuccess = false;
    private Button buttonSwitchCamera;
    private ImageView cameraViewImg;
    private HOGDescriptor hogDescriptor;

    private MatOfRect matOfRect;
    private MatOfDouble matOfDouble;
    private Scalar scalar=new Scalar(110,100,100,100);

    private Bitmap grayBitmap=null;

    private void initHog(){

        //配置特征采集器
//        int UNITWIDTH = 40, UNITHEIGHT = 60;
//        Size windowSize = new Size(UNITWIDTH, UNITHEIGHT);
//        Size blockSize = new Size(UNITWIDTH / 2, UNITHEIGHT / 2);
//        Size _blockStride = new Size(blockSize.width / 2, blockSize.height / 2);
//        Size _cellSize = _blockStride;
//        int _nbins = 4;
//        hogDescriptor = new HOGDescriptor(windowSize, blockSize, _blockStride, _cellSize, _nbins);
        hogDescriptor=new HOGDescriptor();
        hogDescriptor.setSVMDetector(HOGDescriptor.getDefaultPeopleDetector());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_custom_camera);
        cameraViewImg = findViewById(R.id.cameraViewImg);

        ((TextView) findViewById(R.id.textView)).setText("Opencv版本:" + OpenCVLoader.OPENCV_VERSION);
        javaCameraView = findViewById(R.id.cameraView);

        javaCameraView.setDrawUseDefaultMethod(true);
        javaCameraView.setUseGray(true);

        ((CheckBox) findViewById(R.id.checkboxFull)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                javaCameraView.setAutoFullScreen(isChecked);
            }
        });

        Configuration mConfiguration = this.getResources().getConfiguration(); //获取设置的配置信息
        int ori = mConfiguration.orientation; //获取屏幕方向
        javaCameraView.setPortrait(ori == mConfiguration.ORIENTATION_PORTRAIT);



        javaCameraView.setOnFrameReadCallBack(new CustomJavaCameraView.OnFrameReadCallBack() {
            @Override
            public void OnFrameRead(final Bitmap bitmap, final Mat mat) {

                boolean bitmapNull = (null == bitmap);
                boolean matNull = (null == mat);
                if(null!=hogDescriptor){
//
//                    int srcType=mat.type();
//                    Mat dst = new Mat(mat.rows(), mat.cols(), CvType.CV_8U);
//                    Imgproc.cvtColor(mat,dst, CvType.CV_8U);
//
//                    int targetType=dst.type();
//                    int sourceType=mat.type();

//                    Mat dst = new Mat(mat.rows(), mat.cols(), CV_8U);
//                    Imgproc.cvtColor(mat,dst,Imgproc.COLOR_BGR2BGRA);
//                    int bValue = 100;
//                    Imgproc.threshold(mat, dst, bValue, 255, Imgproc.THRESH_BINARY);
//                    Imgproc.cvtColor(mat,dst,Imgproc.COLOR_RGB2GRAY);

                    hogDescriptor.detectMultiScale(mat, matOfRect, matOfDouble);


                    List<Rect> rets = matOfRect.toList();
                    Log.d(App.tag,"found:"+rets.size());

                    for(Rect rect:rets){
                        Imgproc.rectangle(mat,rect, scalar,10);
                    }

                }

                if(null==grayBitmap){
                    grayBitmap=Bitmap.createBitmap(mat.cols(),mat.rows(), Bitmap.Config.RGB_565);
                }
                Utils.matToBitmap(mat, grayBitmap);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        cameraViewImg.setImageBitmap(grayBitmap);
                    }
                });

            }
        });

        requestPermission();
        ((TextView) findViewById(R.id.textViewTitleDesc)).setText("1自定义(解决，全屏，横竖屏切换，前后摄像头切换)");
        buttonSwitchCamera = findViewById(R.id.buttonSwitchCamera);
        buttonSwitchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                javaCameraView.swithCamera(!javaCameraView.isUseFrontCamera());
            }
        });
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        Configuration mConfiguration = this.getResources().getConfiguration(); //获取设置的配置信息
        int ori = mConfiguration.orientation; //获取屏幕方向
        javaCameraView.setPortrait(ori == mConfiguration.ORIENTATION_PORTRAIT);
        javaCameraView.restartCamera();

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

        if (null != javaCameraView) {

            javaCameraView.post(new Runnable() {
                @Override
                public void run() {

                    javaCameraView.setCameraPermissionGranted();//需要已经授权可以使用摄像头再调用这个方法
                    isInitSuccess = OpenCVLoader.initDebug();
                    javaCameraView.setCameraPermissionGranted();//需要已经授权可以使用摄像头再调用这个方法
                    javaCameraView.enableView();
                    javaCameraView.enableFpsMeter();

                    matOfRect = new MatOfRect();
                    matOfDouble = new MatOfDouble();

                    initHog();

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
