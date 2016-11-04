#ifndef _MODELLCASS_H_
#define _MODELLCASS_H_

#include <dlib/image_processing.h>

using namespace dlib;

class ModelClass {
public:
	shape_predictor sp;
	ModelClass(const char *s);
	full_object_detection getsp(array2d<rgb_pixel> &img, dlib::rectangle &d);
	full_object_detection getsp(array2d<int> &img, dlib::rectangle &d);
};


#endif _MODELLCASS_H_
