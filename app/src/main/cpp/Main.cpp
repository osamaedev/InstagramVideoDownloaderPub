

#include <jni.h>
#include <string>


extern "C" jstring
Java_com_instagram_video_downloader_VideoDownloaderApp_get1(JNIEnv *env, jobject thiz) {
    std::string uniqueId = "1f59cd68-fc52-c24f-c9df-1a6f5f39776b";
    return env->NewStringUTF(uniqueId.c_str());
}

extern "C" jstring
Java_com_instagram_video_downloader_VideoDownloaderApp_get2(JNIEnv *env, jobject thiz) {
    std::string secret = "T9Re0wkRiM7WIcVscIvTVHQMYsg9W2inFxVMgolc";
    return env->NewStringUTF(secret.c_str());
}

extern "C" jstring
Java_com_instagram_video_downloader_VideoDownloaderApp_get3(JNIEnv *env, jobject thiz) {
    std::string url = "https://vd.YouApiHere.com/";
    return env->NewStringUTF(url.c_str());
}

extern "C" jstring
Java_com_instagram_video_downloader_VideoDownloaderApp_get4(JNIEnv *env, jobject thiz) {
    std::string key = "12e610c9949eb8c125fb9b2f0c47e20e";
    return env->NewStringUTF(key.c_str());
}