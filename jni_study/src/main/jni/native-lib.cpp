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

JNIEXPORT jstring JNICALL
Java_com_demo_study_MainActivity_getCVVersion(JNIEnv *env, jobject /* this */) {

    LOGD("测试LOG IN JNI");

    std::string hello = "Hello from C++ " + cv::getVersionString();

    return env->NewStringUTF(hello.c_str());

}


JNIEXPORT jintArray JNICALL
Java_com_demo_study_NativeLibUtils_bitmap2Gray(JNIEnv *env, jclass claz, jintArray pixels, jint w,
                                               jint h) {

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


/**
 * Java调用System.loadLibrary()加载一个库的时候，会首先在库中搜索JNI_OnLoad()函数，如果该函数存在，则执行它
 * @param jvm
 * @param reserved
 * @return
 */
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *jvm, void *reserved) {
    JNIEnv *env = NULL;
    jint result = -1;

    LOGD("测试LOG IN JNI JNI_OnLoad");

    if (jvm->GetEnv((void **) &env, JNI_VERSION_1_4) != JNI_OK) {
        return -1;
    }

    result = JNI_VERSION_1_4;
    return result;
}

/**
 * 根据JNI文档的描述，当GC回收了加载这个库的ClassLoader时，该函数被调用
 * @param vm
 * @param reserved
 */
JNIEXPORT void JNICALL JNI_OnUnload(JavaVM *vm, void *reserved) {

    LOGD("测试LOG IN JNI JNI_OnUnload");
}


}
