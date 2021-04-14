package com.wangzy.face;

import android.graphics.Point;
import android.util.Log;

import com.tzutalin.dlib.VisionDetRet;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;


public class FeatureUtils {

    public static final String tag = "ff";

    public static void comptuteFeature(List<VisionDetRet> visionDetRets, Mat faceMat) {
        if (null != visionDetRets) {
            for (int i = 0, isize = visionDetRets.size(); i < isize; i++) {
                comptuteFeature(visionDetRets.get(i), faceMat);
            }
        }
    }

    public static ArrayList<Float> comptuteFeature(VisionDetRet visionDetRet, Mat faceMat) {
        if (null != visionDetRet && null != visionDetRet.getFaceLandmarks()) {
            ArrayList<Point> marks = visionDetRet.getFaceLandmarks();
            //1.第0点到其他67个点的距离
            ArrayList<Float> dises = new ArrayList<>();
            Point p0 = marks.get(0);
            for (int i = 1; i <= 67; i++) {
                float dis = distance(p0, marks.get(i));
                dises.add(dis);
            }
            Log.e(tag, "第一波得到的距离个数:" + dises.size());
            //2.计算特征长度比例
            ArrayList<Float> ratioDistance = new ArrayList<Float>();
            for (int x = 0, xsize = dises.size(); x < xsize - 1; x++) {
                float rateX = dises.get(x) * 1.0f / dises.get(x + 1);
                ratioDistance.add(rateX);
            }
            Log.e(tag, "第二波得到的特征距离比:" + ratioDistance.size());

            ratioDistance.add(computeMean(faceMat));
            return ratioDistance;
        }
        return null;
    }

    public static String comptuteFeature2(VisionDetRet visionDetRet, Mat faceMat) {
        if (null != visionDetRet && null != visionDetRet.getFaceLandmarks()) {
            ArrayList<Point> marks = visionDetRet.getFaceLandmarks();
            //1.第0点到其他67个点的距离
            ArrayList<Float> dises = new ArrayList<>();
            Point p0 = marks.get(0);
            for (int i = 1; i <= 67; i++) {
                float dis = distance(p0, marks.get(i));
                dises.add(dis);
            }
            Log.e(tag, "第一波得到的距离个数:" + dises.size());
            //2.计算特征长度比例
            StringBuffer sbf = new StringBuffer();
            for (int x = 0, xsize = dises.size(); x < xsize - 1; x++) {
                float rateX = dises.get(x) * 1.0f / dises.get(x + 1);
                sbf.append(rateX + ",");
            }
            sbf.append(computeMean(faceMat));
            String rest = sbf.toString();
            return rest;
        }
        return null;
    }


    private static float computeMean(Mat mat) {
        int chnnel = mat.channels();
        if (chnnel != 1) {
            Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2GRAY);
        }
        Scalar scalar = Core.mean(mat);
        float result = (float) scalar.val[0];
        return result;
    }

    private static float distance(Point p1, Point p2) {
        return (float) Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
    }

    public static float computeDistancePercent(ArrayList<Float> inputFeature, People inPutPeople) {
        List<Float> inputPeopleFeatures = inPutPeople.getVector();
        float peopleTotalFeature = 0;
        float distanceTotal = 0;
        for (int i = 0, isize = inputPeopleFeatures.size(); i < isize; i++) {
            peopleTotalFeature += inPutPeople.getVector().get(i);
            distanceTotal+=Math.sqrt(Math.pow(inPutPeople.getVector().get(i)-inputFeature.get(i),2));
        }
        float distancePercent=distanceTotal/peopleTotalFeature;
        return 1-distancePercent;
    }


}
