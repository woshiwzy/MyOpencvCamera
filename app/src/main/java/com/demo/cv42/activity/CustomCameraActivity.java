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
import org.opencv.core.Mat;

import androidx.annotation.NonNull;

import static android.Manifest.permission.CAMERA;

public class CustomCameraActivity extends Activity {

    private String tag = "cv42demo";
    private CustomJavaCameraView javaCameraView;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 200;
    private boolean isInitSuccess = false;
    private Button buttonSwitchCamera;
    private ImageView cameraViewImg;


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

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        cameraViewImg.setImageBitmap(bitmap);
                    }
                });

            }
        });

        requestPermission();
        ((TextView) findViewById(R.id.textViewTitleDesc)).setText("自定义(解决，全屏，横竖屏切换，前后摄像头切换)");
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
        Log.e(App.Companion.getTag(), "isinit success:" + isInitSuccess);

        if (null != javaCameraView) {

            javaCameraView.post(new Runnable() {
                @Override
                public void run() {

                    javaCameraView.setCameraPermissionGranted();//需要已经授权可以使用摄像头再调用这个方法
                    isInitSuccess = OpenCVLoader.initDebug();
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
