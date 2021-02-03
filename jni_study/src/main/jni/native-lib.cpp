#include <jni.h>
#include <string>
#include <iostream>
#include <stdio.h>
#include <dirent.h>


#include <opencv2/opencv.hpp>
#include <opencv2/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/imgproc/types_c.h>
#include <android/log.h>

#include "LogUtils.h"
#include "StringUtils.h"


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



//动态注册示例
//jint RegisterNativeMethod(JNIEnv *env) {
//    jclass clazz = env->FindClass("com/demo/study/NativeLibUtils");
//    if (clazz == NULL) {
//        LOGE("con't find class com/demo/study/NativeLibUtils")
//        return JNI_ERR;
//    }
//
//    JNINativeMethod methods_binds[] = {
//            {"scanFiles", "(Ljava/lang/String;)V", (void *) scanFilesINNative}
//    };
//
//    return env->RegisterNatives(clazz, methods_binds,
//                                sizeof(methods_binds) / sizeof(methods_binds[0]));
//}



void scanDir(JNIEnv *env, jobject list, jmethodID adId, string dir_name) {
    if (dir_name.empty()) {
        LOGE("dir_name is null !");
        return;
    }
    DIR *dir = opendir(dir_name.c_str());
    // check is dir ?
    if (NULL == dir) {
        LOGE("Can not open dir. Check path or permission!");
        return;
    }
    struct dirent *file;
    // read all the files in dir
    while ((file = readdir(dir)) != NULL) {
        // skip "." and ".."
        if (strcmp(file->d_name, ".") == 0 || strcmp(file->d_name, "..") == 0) {
            LOGV("ignore . and ..");
            continue;
        }
        if (file->d_type == DT_DIR) {
            string filePath = dir_name + "/" + file->d_name;
            scanDir(env, list, adId, filePath); // 递归执行
        } else {
            // 如果需要把路径保存到集合中去，就在这里执行 add 的操作
//            char *name = (char *) malloc(strlen(dir_name.c_str()) + strlen(file->d_name));
//            sprintf(name, "%s%s", dir_name.c_str(), file->d_name);
//            env->CallBooleanMethod(list, adId, env->NewStringUTF(name));

            env->CallBooleanMethod(list, adId, env->NewStringUTF(contact(const_cast<char *>(dir_name.c_str()), file->d_name)));
        }
    }
    closedir(dir);
}


JNIEXPORT jobject JNICALL
Java_com_demo_study_NativeLibUtils_scanFiles(JNIEnv *env, jclass clazz, jstring dir_name) {
    const char *dirPath = env->GetStringUTFChars(dir_name, 0);

    jclass fileListClas = env->FindClass("java/util/ArrayList");
    jmethodID construct = env->GetMethodID(fileListClas, "<init>", "()V");
    jobject obj_list = env->NewObject(fileListClas, construct);

    jmethodID listAddMethodId = env->GetMethodID(fileListClas, "add", "(Ljava/lang/Object;)Z");
    scanDir(env, obj_list, listAddMethodId, string(dirPath));

//    env->CallBooleanMethod(obj_list, listAddMethodId, env->NewStringUTF("HHHHBBBACC"));

    env->ReleaseStringUTFChars(dir_name, dirPath);

    return obj_list;
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

//执行动态注册
//    jint ret = RegisterNativeMethod(env);

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