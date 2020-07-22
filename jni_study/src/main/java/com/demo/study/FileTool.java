package com.demo.study;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by wangzy on 2020/7/22
 * description:
 */
public class FileTool {


    /**
     * 扫描某文件夹下的所有目录
     *
     * @param entryFile
     * @param result
     */
    public static void scanDir(File entryFile, ArrayList<File> result) {

        if (entryFile.isDirectory()) {

            File[] subFiles = entryFile.listFiles();
            for (File subFle : subFiles) {
                if (subFle.isDirectory()) {
                    scanDir(subFle, result);
                } else if (subFle.isFile()) {
                    result.add(entryFile);
                }
            }
        } else if (entryFile.isFile()) {
            result.add(entryFile);
        }


    }


}
