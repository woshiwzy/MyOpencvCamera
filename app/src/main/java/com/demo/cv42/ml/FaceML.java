package com.demo.cv42.ml;

import android.util.Log;

import com.demo.cv42.App;
import com.tzutalin.dlib.VisionDetRet;
import com.wangzy.face.DbController;
import com.wangzy.face.FeatureUtils;
import com.wangzy.face.People;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.ml.KNearest;
import org.opencv.ml.TrainData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FaceML {


    private static FaceML faceML;
    private static HashMap<Long, People> peoplesMap;

    public static FaceML getInstance() {
        if (null == faceML) {
            faceML = new FaceML();
        }
        return faceML;
    }


    private KNearest kNearest;

    private FaceML() {

        this.loadData();
    }

    public void reload() {
        peoplesMap.clear();
        loadData();
    }

    public int getSampleSize() {
        return peoplesMap.size();
    }

    public void loadData() {
        if (null == peoplesMap) {
            peoplesMap = new HashMap<>();
        }
        peoplesMap.clear();


        List<People> peoples = DbController.getInstance(App.Companion.getApp()).getSession().getPeopleDao().loadAll();

        if (peoples.isEmpty()) {
            return;
        }

        this.kNearest = KNearest.create();
        this.kNearest.setDefaultK(1);
        this.kNearest.setIsClassifier(true);


        int col = peoples.get(0).getVector().size();
        Log.e(App.tag, "找到已经登记的人脸数据:" + peoples.size() + " 纬度：" + col);

        Mat responseMat = new Mat(peoples.size(), 1, CvType.CV_32F);//label mat
        Mat samplesMat = new Mat(0, col, CvType.CV_32F);//sample


        for (int i = 0, isize = peoples.size(); i < isize; i++) {
            People people = peoples.get(i);
            peoplesMap.put(people.getId(), people);
            List<Float> vec = people.getVector();

            MatOfFloat matOfFloat = new MatOfFloat();
            matOfFloat.fromList(vec);

            int mc = matOfFloat.cols();
            int mr = matOfFloat.rows();
            int mt = matOfFloat.type();

            Mat nv = matOfFloat.reshape(0, 1);

            int nvc = nv.cols();
            int nvr = nv.rows();
            int nvt = nv.type();
            samplesMat.push_back(nv);
//            Log.e(App.tag,"nv.shape"+nv.cols()+" "+nv.rows()+" "+nv.type()+" -->"+samplesMat.type());
            responseMat.put(i, 0, people.getId());
        }


        int t1 = samplesMat.type();
        int t2 = responseMat.type();

        TrainData trainData = TrainData.create(samplesMat, 0, responseMat);
        this.kNearest.train(trainData);
    }

//    public long predicate(VisionDetRet visionDetRet) {
//        ArrayList<Float> vec = FeatureUtils.comptuteFeature(visionDetRet);
//        MatOfFloat matOfFloat = new MatOfFloat();
//        matOfFloat.fromList(vec);
//        Mat preInputMat = matOfFloat.reshape(0, 1);
//        Mat result = new Mat();
//        float responId = this.kNearest.predict(preInputMat, result);
//        return (long) responId;
//    }

    public People predicate2(ArrayList<Float> vec) {
        MatOfFloat matOfFloat = new MatOfFloat();
        matOfFloat.fromList(vec);

        Mat result = new Mat();
        Mat input = matOfFloat.reshape(0, 1);

        long responId = (long) this.kNearest.findNearest(input, 1, result);

//        long responId = (long) this.kNearest.predict(input, result);

//        Log.e(App.tag, "结果矩阵:" + result.cols() + " x " + result.rows() + "   ==> id:" + responId);

        return peoplesMap.get(responId);

    }

    public long predicate(VisionDetRet visionDetRet, Mat faceMat) {
        ArrayList<Float> vec = FeatureUtils.comptuteFeature(visionDetRet, faceMat);
        MatOfFloat matOfFloat = new MatOfFloat();
        matOfFloat.fromList(vec);
        Mat preInputMat = matOfFloat.reshape(0, 1);
        Mat result = new Mat();
        float responId = this.kNearest.predict(preInputMat, result);
        return (long) responId;
    }


}
