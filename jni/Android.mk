LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

OPENCV_CAMERA_MODULES:=on
OPENCV_INSTALL_MODULES:=on
OPENCV_MODULES:=contrib legacy stitching superres ocl objdetect ml ts videostab video photo calib3d features2d highgui imgproc flann androidcamera core java
OPENCV_LIB_TYPE:=SHARED
#OPENCV_LIB_TYPE:=STATIC
#include ../../sdk/native/jni/OpenCV.mk
include f:/openCV-android-sdk-3.1.0/sdk/native/jni/OpenCV.mk

LOCAL_SRC_FILES  := Filter.cpp
LOCAL_C_INCLUDES += $(LOCAL_PATH)
LOCAL_LDLIBS     += -llog -ldl

LOCAL_MODULE     := filter

include $(BUILD_SHARED_LIBRARY)
