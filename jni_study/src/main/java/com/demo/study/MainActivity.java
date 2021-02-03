package com.demo.study;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.demo.jni_study.R;

import java.io.File;
import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    static {
        System.loadLibrary("native-lib");
    }

    TextView textViewVersion;
    ImageView imageViewIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textViewVersion = findViewById(R.id.textViewVersion);
        imageViewIcon = findViewById(R.id.imageViewIcon);

        findViewById(R.id.buttonGo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                textViewVersion.setText(getCVVersion());
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.test2);
                imageViewIcon.setImageBitmap(rgb2Gray(bitmap));

                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        ArrayList<File> results = new ArrayList<>();

                        String entry = "/sdcard/face_demo";

                        long s1 = System.currentTimeMillis();
                        FileTool.scanDir(new File(entry), results);
                        long e1 = System.currentTimeMillis();

                        long s2 = System.currentTimeMillis();
                        ArrayList<String> ret = NativeLibUtils.scanFiles(entry);
                        long e2 = System.currentTimeMillis();

                        Log.e(App.tag, "JAVA扫描耗时:" + (e1 - s1) + " JNI扫描耗时:" + (e2 - s2));

                        Log.e(App.tag, "JAVA文件数:" + results.size());
                        Log.e(App.tag, "JNI文件数:" + ret.size());



                    }
                }).start();


            }
        });
    }

    public native String getCVVersion();


    /**
     * 调用JNI图片灰度处理
     *
     * @param srcBitmap
     * @return
     */
    private Bitmap rgb2Gray(Bitmap srcBitmap) {
        int w = srcBitmap.getWidth();
        int h = srcBitmap.getHeight();
        int[] pixels = new int[w * h];
        srcBitmap.getPixels(pixels, 0, w, 0, 0, w, h);
        int[] afterRet = NativeLibUtils.bitmap2Gray(pixels, w, h);

        Bitmap resultImage = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        resultImage.setPixels(afterRet, 0, w, 0, 0, w, h);
        return resultImage;

    }


}
