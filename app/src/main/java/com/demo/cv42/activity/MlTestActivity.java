package com.demo.cv42.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.demo.cv42.App;
import com.demo.cv42.R;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.ml.KNearest;
import org.opencv.ml.TrainData;

import androidx.appcompat.app.AppCompatActivity;

/**
 * 机器学习测试页面
 */
public class MlTestActivity extends AppCompatActivity {


    static {

        OpenCVLoader.initDebug();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ml_test);

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                test();
            }
        });
    }

    private void test() {


        KNearest kNearest = KNearest.create();
        kNearest.setDefaultK(1);
        kNearest.setIsClassifier(true);

        Mat responseMat = new Mat(10, 1, CvType.CV_32F);
        Mat samplesMat = new Mat(0, 10, CvType.CV_32F);

        for (int i = 0; i < 10; i++) {

            if (i == 5) {
                MatOfFloat matOfFloat = new MatOfFloat();
                matOfFloat.fromArray(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
                Mat nv = matOfFloat.reshape(0, 1);
                samplesMat.push_back(nv);
                responseMat.put(i, 0, i);
            } else {

                MatOfFloat matOfFloat = new MatOfFloat();
                matOfFloat.fromArray(2, 2, 2, 3, 5, 6, 7, 8, 9, 7);
                Mat nv = matOfFloat.reshape(0, 1);
                samplesMat.push_back(nv);
                responseMat.put(i, 0, i);
            }

        }


        TrainData trainData = TrainData.create(samplesMat, 0, responseMat);

        kNearest.train(trainData);

        //==========
        MatOfFloat input = new MatOfFloat();
        input.fromArray(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        Mat ninput = input.reshape(0, 1);

        float res = kNearest.predict(ninput);
        Log.e(App.Companion.getTag(), "预测结果2===>>>" + res);


    }
}