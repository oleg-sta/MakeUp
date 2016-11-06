#include <stdio.h>
#include <opencv2/opencv.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <dlib/image_processing/frontal_face_detector.h>
#include <dlib/image_processing/render_face_detections.h>
#include <dlib/image_processing.h>
#include <dlib/gui_widgets.h>
#include <dlib/image_io.h>
#include <dlib/opencv.h>
#include <iostream>
#include <string>
//#include "Line.h"
#include "Line.cpp"
#include "Triangle.h"



#include <android/log.h>

#include "ModelClass.cpp"

#define LOG_TAG "FaceDetection/DetectionBasedTracker"
#define LOGD(...) ((void)__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__))

/*
 library for face changed, load models from file
*/
using namespace dlib;
using namespace std;

void convertPoints(std::vector<cv::Point>& src, std::vector<cv::Point>& dst, int pointss[], int pointss1[]); // add points to image from origin
void flipPo(std::vector<cv::Point>& src, std::vector<cv::Point>& dst, int width, int p2[]); // flip points and change order of points after flip

void change_faces(cv::Mat& imageFromMat, cv::Mat& imageToMat, std::vector<cv::Point>&, std::vector<cv::Point>&, std::vector<Triangle>& triangles); // change face by triangulation
void change_faces(cv::Mat& imageFromMat, cv::Mat& imageToMat, std::vector<cv::Point>&, full_object_detection& oldShape, std::vector<Triangle>& triangles);
void getBorder(cv::Point& p1, cv::Point& p2, cv::Point& opposite, int x, int* minMax);

void change_faces(cv::Mat& imageFromMat, cv::Mat& imageToMat, std::vector<cv::Point>& pointsMask, full_object_detection& oldShape, std::vector<Triangle>& triangles)
{
  std::vector<cv::Point> oldShape22;
            for (int sh = 0; sh < oldShape.num_parts(); sh++)
            {
                oldShape22.push_back(cv::Point(oldShape.part(sh).x(), oldShape.part(sh).y()));
            }
  change_faces(imageFromMat, imageToMat, pointsMask, oldShape22, triangles);
}

// imageToMat must be 4-channels(otherwise scale would be incorrect), imageFromMat - 4 channels
void change_faces(cv::Mat& imageFromMat, cv::Mat& imageToMat, std::vector<cv::Point>& pointsMask, std::vector<cv::Point>& oldShape, std::vector<Triangle>& triangles)
{
	int width = imageToMat.cols;
	cout << "Width "<< width << endl;
	LOGD("change_faces1");
	for(int i = 0; i < triangles.size(); i++) 
	{
		Triangle t = triangles[i];
		cv::Point p1 = oldShape[t.point1];
		cv::Point p2 = oldShape[t.point2];
		cv::Point p3 = oldShape[t.point3];

		t.minX = std::min(std::min(p1.x, p2.x), p3.x);
		t.maxX = std::max(std::max(p1.x, p2.x), p3.x);
		t.minY = std::min(std::min(p1.y, p2.y), p3.y);
		t.maxY = std::max(std::max(p1.y, p2.y), p3.y);
	}
	LOGD("change_faces2");
	cout << "Max min triangles set" << endl;
	for (int k = 0; k < triangles.size(); k++)
	{
		Triangle triangle = triangles[k];
		cv::Point p1 = oldShape[triangle.point1];
		cv::Point p2 = oldShape[triangle.point2];
		cv::Point p3 = oldShape[triangle.point3];
		triangle.minX = std::min(std::min(p1.x, p2.x), p3.x);
		triangle.maxX = std::max(std::max(p1.x, p2.x), p3.x);
		triangle.minY = std::min(std::min(p1.y, p2.y), p3.y);
		triangle.maxY = std::max(std::max(p1.y, p2.y), p3.y);


		cv::Point2f srcTri[3];
		cv::Point2f dstTri[3];
		cv::Mat affine(2, 3, CV_32FC1);
		
		cv::Point ps1 = oldShape[triangle.point1];
		cv::Point ps2 = oldShape[triangle.point2];
		cv::Point ps3 = oldShape[triangle.point3];

		srcTri[0] = cv::Point2f(ps1.x,
				ps1.y);
		srcTri[1] = cv::Point2f(ps2.x,
				ps2.y);
		srcTri[2] = cv::Point2f(ps3.x,
				ps3.y);

		cv::Point pd1 = pointsMask[triangle.point1];
		cv::Point pd2 = pointsMask[triangle.point2];
		cv::Point pd3 = pointsMask[triangle.point3];

		dstTri[0] = cv::Point2f(pd1.x,
				pd1.y);
		dstTri[1] = cv::Point2f(pd2.x,
				pd2.y);
		dstTri[2] = cv::Point2f(pd3.x,
				pd3.y);

		affine = cv::getAffineTransform(srcTri, dstTri);
		
		for (int i = triangle.minX; i < triangle.maxX; i++) 
		{
			LOGD("change_faces3");
			int* minMax = new int[4];
			minMax[2] = -1;
			minMax[3] = -1;

			getBorder(ps1, ps2, ps3, i, minMax);
			getBorder(ps2, ps3, ps1, i, minMax);
			getBorder(ps3, ps1, ps2, i, minMax);
			if (minMax[0] >= triangle.minY && minMax[1] <= triangle.maxY) 
			{
					for (int j = minMax[0]; j <= minMax[1]; j++) 
					{
						double origX = affine.at<double>(0, 0) * i
								+ affine.at<double>(0, 1) * j + affine.at<double>(0, 2);
						double origY = affine.at<double>(1, 0) * i
								+ affine.at<double>(1, 1) * j + affine.at<double>(1, 2);
						if( (origX >=0 && origX < imageFromMat.cols) && (origY >=0 && origY < imageFromMat.rows) && j >=0 && j < imageToMat.rows && i >=0 && i < imageToMat.cols)
						{						
								cv::Vec4b pixelFrom = imageFromMat.at<cv::Vec4b>(origY, origX);
								cv::Vec4b pixelTo = imageToMat.at<cv::Vec4b>(j, i);
								int alpha = pixelFrom[3];
//                        alpha = 250;
								// ��������� �� ���� �������(RGB)
								for (int ij = 0; ij < 3; ij++) 
								{
									pixelTo[ij] = (pixelTo[ij] * (255 - alpha)
											+ pixelFrom[ij] * alpha) / 255;
								}
								imageToMat.at<cv::Vec4b>(j, i) = pixelTo;
						}
					}
			}
			else
				cout << "Epic fail!" <<endl;
			
		}
	}
}

void getBorder(cv::Point& p1, cv::Point& p2, cv::Point& opposite, int x, int* minMax)
{
	if (p2.x != p1.x)
	{
		double y = p1.y + (x - p1.x) * (p2.y - p1.y) / (p2.x - p1.x);
		double y2 = p1.y
				+ (opposite.x - p1.x) * (p2.y - p1.y) / (p2.x - p1.x);
		if (opposite.y > y2) {
			// TODO possible error
			if (minMax[2] == -1) {
				minMax[2] = 0;
				minMax[0] = (int) y;
			}
			minMax[0] = std::max(minMax[0], (int) y);
		} else {
			if (minMax[3] == -1) {
				minMax[3] = 0;
				minMax[1] = (int) y;
			}
			minMax[1] = std::min(minMax[1], (int) y);
		}

	}
}

void flipPo(std::vector<cv::Point>& src, std::vector<cv::Point>& dst, int width, int p2[])
{
   for (int i = 0; i < src.size(); i++)
   {
	   cv::Point p = src[p2[i]];
	   dst.push_back(cv::Point(width - p.x, p.y));
   }
}
// src - points on eye, dst - points on image
void convertPoints(std::vector<cv::Point>& src, std::vector<cv::Point>& dst, int pointss[], int pointss1[]) //std::vector<cv::Point>& src2, std::vector<cv::Point>& dst2)
{
		cv::Point2f srcTri[3];
		cv::Point2f dstTri[3];
		cv::Mat affine(2, 3, CV_32FC1);
		
		cv::Point p0 = src[pointss[0]];
		cv::Point p1 = src[pointss[1]];
		cv::Point p2 = src[pointss[2]];

		srcTri[0] = cv::Point2f(p0.x,
				p0.y);
		srcTri[1] = cv::Point2f(p1.x,
				p1.y);
		srcTri[2] = cv::Point2f(p2.x,
				p2.y);

		cv::Point pd0 = dst[pointss[0]];
		cv::Point pd1 = dst[pointss[1]];
		cv::Point pd2 = dst[pointss[2]];

		dstTri[0] = cv::Point2f(pd0.x,
				pd0.y);
		dstTri[1] = cv::Point2f(pd1.x,
				pd1.y);
		dstTri[2] = cv::Point2f(pd2.x,
				pd2.y);
		affine = cv::getAffineTransform(srcTri, dstTri);
std::vector<cv::Point> added;
std::vector<cv::Point> added2;
for (int i = dst.size(); i < src.size(); i++)
{
	cv::Point pd = src[i];
    added.push_back(pd);
}
      cv::transform(added, added2, affine);
for (int i = 0; i < added2.size(); i++)
{
	cv::Point pd = added2[i];
    dst.push_back(pd);
}
}
