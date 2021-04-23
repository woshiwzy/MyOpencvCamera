package com.demo.cv42.ml;


import java.util.List;

public class VectorTool {

    /**
     * 计算2个向量相似度
     *
     * @param vector1
     * @param vector2
     * @return
     */
    public static float computeSimilarity(double[] vector1, double[] vector2) {

        double sum = 0.0;
        Double v1Len = 0.0;
        Double v2Len = 0.0;
        for (int i = 0; i < vector1.length; i++) {
            sum += vector1[i] * vector2[i];
            v1Len += vector1[i] * vector1[i];
        }
        for (int k = 0; k < vector2.length; k++) {
            v2Len += vector2[k] * vector2[k];
        }
        double simi = sum / (Math.sqrt(v1Len) * Math.sqrt(v2Len));
        return (float) simi;
    }


    public static float computeSimilarity2(List<Float> vector1, List<Float> vector2) {

        double sum = 0.0;
        Double v1Len = 0.0;
        Double v2Len = 0.0;
        for (int i = 0; i < vector1.size(); i++) {
            sum += vector1.get(i) * vector2.get(i);
            v1Len += vector1.get(i) * vector1.get(i);
        }
        for (int k = 0; k < vector2.size(); k++) {
            v2Len += vector2.get(k) * vector2.get(k);
        }
        double simi = sum / (Math.sqrt(v1Len) * Math.sqrt(v2Len));
        return (float) simi;
    }

}
