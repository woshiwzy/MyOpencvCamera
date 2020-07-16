package com.demo.study;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.demo.jni_study.R;

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
            }
        });
    }

    public native String getCVVersion();


    /**
     * 调用JNI图片灰度处理
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
