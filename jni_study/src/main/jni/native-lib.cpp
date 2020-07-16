#include <jni.h>
#include <string>
#include <opencv2/opencv.hpp>
#include <opencv2/core.hpp>
#include <iostream>
#include <string>
#include <stdio.h>
#include <opencv2/imgproc/imgproc.hpp>
using namespace std;
using namespace cv;
extern "C"

JNIEXPORT jstring JNICALL
Java_com_demo_jni_1study_MainActivity_getCVVersion(
        JNIEnv* env,
        jobject /* this */) {

    std::string hello = "Hello from C++ "+cv::getVersionString();

    return env->NewStringUTF(hello.c_str());
}