package com.demo.cv42.hog;

import android.util.Log;

import com.demo.cv42.App;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.HOGDescriptor;

import java.util.ArrayList;

public class FaceHogTool {

    private final static int normal_width = 120, normal_height = 120;

    private static HOGDescriptor hogDescriptor;

    static {
        Size windowSize = new Size(normal_width, normal_width);
        Size blockSize = new Size(normal_width / 2, normal_height / 2);
        Size _blockStride = new Size(blockSize.width / 2, blockSize.height / 2);
        Size _cellSize = _blockStride;
        int _nbins = 4;
        hogDescriptor = new HOGDescriptor(windowSize, blockSize, _blockStride, _cellSize, _nbins);
    }

    private static HOGDescriptor getHogDescriptor() {
        return hogDescriptor;
    }

    public static ArrayList<Float> compte(Mat input) {

        Mat dstMat = new Mat(normal_width, normal_height, CvType.CV_8UC3);//
        Imgproc.resize(input, dstMat, new Size(normal_width, normal_height));//归一化，把所有的图片大小调整成一样大，得到的特征值才会是一样的

        Imgproc.cvtColor(dstMat, dstMat, Imgproc.COLOR_BGR2GRAY);

        MatOfFloat matf = new MatOfFloat();
        getHogDescriptor().compute(dstMat, matf);//计算特征

        ArrayList<Float> vecs = new ArrayList<>(matf.toList());

        return vecs;

//        StringBuffer sbf = new StringBuffer();
//        for (float v : vectors) {
//            sbf.append("" + v + ",");
//        }
//
//        String rest = sbf.toString().substring(0, sbf.length() - 1);
//        return rest;
    }
}
