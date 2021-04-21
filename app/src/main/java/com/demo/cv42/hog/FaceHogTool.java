package com.demo.cv42.hog;

import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.HOGDescriptor;

import java.util.List;

public class FaceHogTool {

    public final static int normal_width = 100, normal_height = 100;
    public static HOGDescriptor hogDescriptor;

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

    public static List<Float> compte(Mat input) {
        Mat dstMat = new Mat(normal_width, normal_height, input.type());//
        Imgproc.resize(input, dstMat, new Size(normal_width, normal_height));//归一化，把所有的图片大小调整成一样大，得到的特征值才会是一样的
        MatOfFloat matf = new MatOfFloat();
        getHogDescriptor().compute(dstMat, matf);//计算特征
        List<Float> vectors = matf.toList();
        return vectors;
    }
}
