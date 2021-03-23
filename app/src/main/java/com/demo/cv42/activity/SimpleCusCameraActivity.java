package com.demo.cv42.activity;

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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.demo.cv42.App;
import com.demo.cv42.R;
import com.demo.cv42.custom.CameraDataGeter;
import com.demo.cv42.custom.CameraDataGeterBase;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Rect;

import androidx.annotation.NonNull;

import static android.Manifest.permission.CAMERA;


public class SimpleCusCameraActivity extends Activity {

    private String tag = "cv42demo";
    private CameraDataGeter javaCameraView;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 200;
    private ImageView imageViewPreview;
    private CheckBox checkBoxSwithCamera, checkBoxSqure;
    private TextView textViewTips;
    private long lastFrameTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_simple_cus_camera);

        checkBoxSqure = findViewById(R.id.checkBoxSqure);
        textViewTips = findViewById(R.id.textViewTips);
        checkBoxSwithCamera = findViewById(R.id.checkBoxSwithCamera);
        checkBoxSwithCamera.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                javaCameraView.toogleCamera();
            }
        });

        imageViewPreview = findViewById(R.id.imageViewPreview);
        javaCameraView = new CameraDataGeter(this, CameraDataGeterBase.CAMERA_ID_FRONT, 1920, 1080);
        javaCameraView.setCvCameraViewListener(new CameraDataGeterBase.CvCameraViewListener2() {
            @Override
            public void onCameraViewStarted(int width, int height) {
                Log.e(App.tag, "onCameraViewStarted:" + width + "," + height);
                textViewTips.setText("onCameraViewStarted:" + width + "," + height);
            }

            @Override
            public void onCameraViewStopped() {
                Log.e(App.tag, "onCameraViewStopped");
            }

            @Override
            public Mat onCameraFrame(Mat rgba) {
                Log.e(App.tag, "获得数据===>>:" + rgba.width() + " X " + rgba.height());

                if (checkBoxSqure.isChecked()) {

                    int w = rgba.width();
                    int h = rgba.height();

                    int min = Math.min(w, h);

                    Rect rect = new Rect();

                    rect.x = w / 2 - min / 2;
                    rect.y = h / 2 - min / 2;
                    rect.width = min;
                    rect.height = min;

                    Mat squreMat = new Mat(rgba, rect);
                    final Bitmap Sqbitmap = Bitmap.createBitmap(squreMat.width(), squreMat.height(), Bitmap.Config.RGB_565);
                    Utils.matToBitmap(squreMat, Sqbitmap);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            imageViewPreview.setImageBitmap(Sqbitmap);

                            if (lastFrameTime != 0) {
                                double delta = (System.currentTimeMillis() - lastFrameTime) / 1000.0f;
                                double f = delta / 2.0f;
                                textViewTips.setText("fps:" + (int) (1 / f));
                            }
                            lastFrameTime = System.currentTimeMillis();
                        }
                    });

                }else {

                    final Bitmap bitmap = Bitmap.createBitmap(rgba.width(), rgba.height(), Bitmap.Config.RGB_565);
                    Utils.matToBitmap(rgba, bitmap);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            imageViewPreview.setImageBitmap(bitmap);
                            if (lastFrameTime != 0) {
                                double delta = (System.currentTimeMillis() - lastFrameTime) / 1000.0f;
                                double f = delta / 2.0f;
                                textViewTips.setText("fps:" + (int) (1 / f));
                            }
                            lastFrameTime = System.currentTimeMillis();

                        }
                    });
                }


                return rgba;
            }
        });


        findViewById(R.id.button480).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                javaCameraView.setResolution(640, 480);
            }
        });

        findViewById(R.id.button1920).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                javaCameraView.setResolution(1920, 1080);
            }
        });
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
        if (null != javaCameraView) {
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

    private void initCamera() {
        if (null != javaCameraView) {
            javaCameraView.enableView();
        }
    }


    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        javaCameraView.onConfigurationChanged(newConfig);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initCamera();
        }
    }


}
