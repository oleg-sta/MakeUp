#include "ModelClass.h"
#include <dlib/image_processing/render_face_detections.h>

using namespace dlib;

ModelClass::ModelClass(const char *s) {
	deserialize(s) >> sp;
}

full_object_detection ModelClass::getsp(array2d<rgb_pixel> &img, dlib::rectangle &d) {
	return sp(img, d);
}

full_object_detection ModelClass::getsp(array2d<int> &img, dlib::rectangle &d) {
	return sp(img, d);
}
