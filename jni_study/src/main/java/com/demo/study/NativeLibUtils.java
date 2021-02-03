package com.demo.study;

import java.util.ArrayList;

class NativeLibUtils{


    /**
     * 把Bitmap以数组传到JNI层做灰度处理再返回
     * @param pixels
     * @param w
     * @param h
     * @return
     */
    public static native int[] bitmap2Gray(int []pixels, int w, int h);

    /**
     * 测试动态绑定JNI
     * @param dirPath
     */
    public static native ArrayList<String> scanFiles(String dirPath);




}