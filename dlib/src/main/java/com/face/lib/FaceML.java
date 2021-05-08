package com.face.lib;

import android.content.Context;

import com.tzutalin.dlib.VisionDetRet;
import com.wangzy.db.DbController;
import com.wangzy.db.FeatureUtils;
import com.wangzy.db.People;

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

    public static FaceML getInstance(Context context) {
        if (null == faceML) {
            faceML = new FaceML(context);
        }
        return faceML;
    }


    private KNearest kNearest;

    private FaceML(Context context) {

        this.loadData(context);
    }

    public void reload(Context context) {
        peoplesMap.clear();
        loadData(context);
    }

    public int getSampleSize() {
        return peoplesMap.size();
    }

    public void loadData(Context context) {
        if (null == peoplesMap) {
            peoplesMap = new HashMap<>();
        }
        peoplesMap.clear();
        List<People> peoples = DbController.getInstance(context).getSession().getPeopleDao().loadAll();
        if (peoples.isEmpty()) {
            return;
        }

        this.kNearest = KNearest.create();
        this.kNearest.setDefaultK(1);
        this.kNearest.setIsClassifier(true);

        int col = peoples.get(0).getVector().size();

        Mat responseMat = new Mat(peoples.size(), 1, CvType.CV_32F);//label mat
        Mat samplesMat = new Mat(0, col, CvType.CV_32F);//sample

        for (int i = 0, isize = peoples.size(); i < isize; i++) {
            People people = peoples.get(i);
            peoplesMap.put(people.getId(), people);
            List<Float> vec = people.getVector();

            MatOfFloat matOfFloat = new MatOfFloat();
            matOfFloat.fromList(vec);

            Mat nv = matOfFloat.reshape(0, 1);
            samplesMat.push_back(nv);
            responseMat.put(i, 0, people.getId());
        }


        TrainData trainData = TrainData.create(samplesMat, 0, responseMat);
        this.kNearest.train(trainData);
    }


    public RecResult predicate2(ArrayList<Float> vec) {

        MatOfFloat matOfFloat = new MatOfFloat();
        matOfFloat.fromList(vec);

        Mat result = new Mat();
        Mat input = matOfFloat.reshape(0, 1);

        long responId = (long) this.kNearest.findNearest(input, 1, result);
        People people = peoplesMap.get(responId);

        if (null != people) {
            float percent = VectorTool.computeSimilarity2(vec, people.getVector());
            RecResult recResult = new RecResult(people, percent);
            return recResult;
        }

        return null;

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
