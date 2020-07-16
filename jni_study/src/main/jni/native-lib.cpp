#include <jni.h>
#include <string>
#include <opencv2/opencv.hpp>
#include <opencv2/core.hpp>
#include <iostream>
#include <string>
#include <stdio.h>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/imgproc/types_c.h>
#include<android/log.h>


#ifndef LOG_TAG
#define LOG_TAG "study"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG ,__VA_ARGS__) // 定义LOGD类型
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,LOG_TAG ,__VA_ARGS__) // 定义LOGI类型
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN,LOG_TAG ,__VA_ARGS__) // 定义LOGW类型
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,LOG_TAG ,__VA_ARGS__) // 定义LOGE类型
#define LOGF(...) __android_log_print(ANDROID_LOG_FATAL,LOG_TAG ,__VA_ARGS__) // 定义LOGF类型
#endif


using namespace std;
using namespace cv;
extern "C"
{

JNIEXPORT jstring JNICALL Java_com_demo_study_MainActivity_getCVVersion(JNIEnv *env,jobject /* this */) {

    LOGD("测试LOG IN JNI");

    std::string hello = "Hello from C++ " + cv::getVersionString();

    return env->NewStringUTF(hello.c_str());

}


JNIEXPORT jintArray JNICALL Java_com_demo_study_NativeLibUtils_bitmap2Gray(JNIEnv *env, jclass claz, jintArray pixels,jint w, jint h) {

    jint *cur_array;

    jboolean isCopy = static_cast<jboolean>(false);

    cur_array = env->GetIntArrayElements(pixels, &isCopy);
    if (cur_array == NULL) {
        return 0;
    }

    Mat img(h, w, CV_8UC4, (unsigned char *) cur_array);

    cvtColor(img, img, CV_BGRA2GRAY);
    cvtColor(img, img, CV_GRAY2BGRA);

    int size = w * h;
    jintArray result = env->NewIntArray(size);
    env->SetIntArrayRegion(result, 0, size, (jint *) img.data);
    env->ReleaseIntArrayElements(pixels, cur_array, 0);
    return result;
}


}
