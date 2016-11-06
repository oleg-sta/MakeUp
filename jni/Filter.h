#include <jni.h>
#include <opencv2/core/core.hpp>
#include "ModelClass.h"

#ifndef _Included_org_opencv_samples_fd_filter
#define _Included_org_opencv_samples_fd_filter
#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT void JNICALL Java_ru_flightlabs_makeup_Filter_nativeDrawMask(JNIEnv * jenv, jclass, jlong imageFrom, jlong imageTo, jobjectArray pointsWas1, jobjectArray pointsTo1, jobjectArray triangle1);
JNIEXPORT jlong JNICALL Java_ru_flightlabs_makeup_Filter_nativeCreateObject(JNIEnv *, jclass, jstring, jint);
JNIEXPORT jlong JNICALL Java_ru_flightlabs_makeup_Filter_nativeCreateModel(JNIEnv *, jclass, jstring);
JNIEXPORT jobjectArray JNICALL Java_ru_flightlabs_makeup_Filter_findEyes(JNIEnv * jenv, jclass, jlong thiz, jlong imageGray, jint x, jint y, jint height, jint width, jlong);

void clamRGB(cv::Vec4b &pixel);
int clamRGB1(int val);
void findEyes(cv::Mat frame_gray, cv::Rect face, std::vector<cv::Point> &pixels,  ModelClass *modelClass);
double getObjectFieldD(JNIEnv* env, jobject obj, jclass clsFeature, const char* name);
int getObjectFieldI(JNIEnv* env, jobject obj, jclass clsFeature, const char* name);

#ifdef __cplusplus
}
#endif
#endif
