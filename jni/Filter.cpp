#include <Filter.h>
#include <opencv2/core/core.hpp>

#include <string>
#include <vector>
#include <queue>
#include <pthread.h>

#include <android/log.h>

#include "ModelClass.cpp"

#define LOG_TAG "FaceDetection/DetectionBasedTracker"
#define LOGD(...) ((void)__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__))

#include <dlib/image_processing/frontal_face_detector.h>
#include <dlib/image_processing/render_face_detections.h>
#include <dlib/image_processing.h>
#include <dlib/gui_widgets.h>
#include <dlib/image_io.h>
#include <dlib/opencv.h>

using namespace std;
using namespace cv;
using namespace dlib;

JNIEXPORT jlong JNICALL Java_ru_flightlabs_makeup_Filter_nativeCreateObject
(JNIEnv * jenv, jclass, jstring jFileName, jint faceSize)
{
    LOGD("Java_org_opencv_samples_facedetect_DetectionBasedTracker_nativeCreateObject enter");
    const char* jnamestr = jenv->GetStringUTFChars(jFileName, NULL);
    string stdFileName(jnamestr);
    jlong result = 0;

    try
    {
//        DetectionBasedTracker::Parameters DetectorParams;
//        if (faceSize > 0)
//            DetectorParams.minObjectSize = faceSize;
//        result = (jlong)new DetectionBasedTracker(stdFileName, DetectorParams);
    }
    catch(cv::Exception& e)
    {
        LOGD("nativeCreateObject caught cv::Exception: %s", e.what());
        jclass je = jenv->FindClass("org/opencv/core/CvException");
        if(!je)
            je = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(je, e.what());
    }
    catch (...)
    {
        LOGD("nativeCreateObject caught unknown exception");
        jclass je = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(je, "Unknown exception in JNI code of DetectionBasedTracker.nativeCreateObject()");
        return 0;
    }

    LOGD("Java_org_opencv_samples_facedetect_DetectionBasedTracker_nativeCreateObject exit");
    return result;
}

JNIEXPORT jlong JNICALL Java_ru_flightlabs_makeup_Filter_nativeCreateModel
(JNIEnv * jenv, jclass, jstring jFileName)
{
	LOGD("findEyes119 dd");
	jlong result = 0;
	result = (jlong)new ModelClass(jenv->GetStringUTFChars(jFileName, NULL));
	LOGD("findEyes119 dde111 %i", result);
	return result;
}

JNIEXPORT jobjectArray JNICALL Java_ru_flightlabs_makeup_Filter_findEyes
(JNIEnv * jenv, jclass, jlong thiz, jlong imageGray, jint x, jint y, jint width, jint height, jlong thizModel)
{
	LOGD("Java_ru_flightlabs_masks_DetectionBasedTracker_findEyes");

	cv::Mat imageGrayInner = *((Mat*)imageGray);
	cv::Rect faceRect(x, y,  width, height);
	LOGD("findEyes imageGray %d %d", imageGrayInner.rows, imageGrayInner.cols);
	LOGD("findEyes face %d %d %d %d", faceRect.x, faceRect.y, faceRect.height, faceRect.width);
	std::vector<cv::Point> pixels;
	findEyes(imageGrayInner, faceRect, pixels, (ModelClass*)thizModel);


	jclass clsPoint = jenv->FindClass("org/opencv/core/Point");
	jobjectArray jobAr = jenv->NewObjectArray((int)pixels.size(), clsPoint, NULL);

	jmethodID constructorPoint = jenv->GetMethodID(clsPoint, "<init>", "(DD)V");
	int i = 0;
	for (std::vector<cv::Point>::iterator it = pixels.begin() ; it != pixels.end(); ++it) {
		jobject objPoint = jenv->NewObject(clsPoint, constructorPoint, (double)(*it).x, (double)(*it).y);
		jenv->SetObjectArrayElement(jobAr, i, objPoint);
		i++;
	}

	return jobAr;
}

void clamRGB(cv::Vec4b &pixel) {
	pixel[0] = clamRGB1(pixel[0]);
	pixel[1] = clamRGB1(pixel[1]);
	pixel[2] = clamRGB1(pixel[2]);
}

int clamRGB1(int val) {
	if (val < 0) {
		return 0;
	} else if (val > 255) {
		return 255;
	}
	return val;
}

void findEyes(cv::Mat frame_gray, cv::Rect face, std::vector<cv::Point> &pixels, ModelClass *modelClass) {
	LOGD("findEyes111");
	shape_predictor sp;
	LOGD("findEyes112");
	//array2d<int> img;
	LOGD("findEyes1121 %i", frame_gray.type());

//  assign_image(img, cv_image<uchar>(frame_gray));
	// т.к. предыдущий метод cv_image не работает(может неправильно использую), то делаем преобразование кадра из opencv в dlib вручную
	array2d<int> img;
	img.set_size(frame_gray.rows, frame_gray.cols); // for grey
	LOGD("findEyes1122");
	for (int i = 0; i < frame_gray.rows; i++) {
		//LOGD("findEyes1124");
		for (int j = 0; j < frame_gray.cols; j++) {
			//LOGD("findEyes115");
			img[i][j] = frame_gray.at<uchar>(i, j);
		}
	}
	////  cv_image<bgr_pixel> image(frame_gray);
	LOGD("findEyes114");
	//std::vector<dlib::rectangle> dets;
	//dets.push_back(dlib::rectangle);
	dlib::rectangle d(face.x, face.y, face.x + face.width,
			face.y + face.height);
	LOGD("findEyes115");
	//deserialize(s) >> sp;
	LOGD("findEyes113");
	//  full_object_detection shape = sp(img, d);
	full_object_detection shape = modelClass->getsp(img, d);
	LOGD("findEyes116 %i", shape.num_parts());
	if (shape.num_parts() > 2) {
		LOGD("findEyes116 %i %i", shape.part(0).x(), shape.part(0).y());
		LOGD("findEyes116 %i %i", shape.part(1).x(), shape.part(1).y());
	}
	for (int i = 0; i < shape.num_parts(); i++) {
		pixels.push_back(cv::Point(shape.part(i).x(), shape.part(i).y()));
	}
	LOGD("findEyes116");

	LOGD("findEyes116");
}
