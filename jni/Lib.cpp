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
#include "Line.cpp"
#include "Triangle.h"

/*
 library for face changed, load models from file
 this librry should work under android compiler
*/
using namespace dlib;
using namespace std;
using namespace cv;

void convertPoints ( std::vector<cv::Point>& src, std::vector<cv::Point>& dst, int pointss[], int pointss1[], bool useTwoPoints ); // add points to image from origin
void flipPo ( std::vector<cv::Point>& src, std::vector<cv::Point>& dst, int width, int p2[] ); // flip points and change order of points after flip
void flipTriangles ( std::vector<Triangle>& trianglesIn, std::vector<Triangle>& trianglesOut, int p2[] );

void change_faces ( cv::Mat& imageFromMat, cv::Mat& imageToMat, std::vector<cv::Point>&, std::vector<cv::Point>&, std::vector<Triangle>& triangles, double op = 1, bool useHsv = false);
// change face by triangulation
void change_faces ( cv::Mat& imageFromMat, cv::Mat& imageToMat, std::vector<cv::Point>&, full_object_detection& oldShape, std::vector<Triangle>& triangles , double op = 1, bool useHsv = false);
std::vector<Line> get_lines_from_file ( std::string path );
std::vector<Triangle> get_triangles_from_file ( string path );
void split ( const string& s, char delim,std::vector<string>& v );
void getBorder ( cv::Point& p1, cv::Point& p2, cv::Point& opposite, int x, int* minMax );
void loadImgLab ( string path, std::vector<cv::Point>& points );
std::string getAttribute ( std::string line, std::string );

void change_faces ( cv::Mat& imageFromMat, cv::Mat& imageToMat, std::vector<cv::Point>& pointsMask, full_object_detection& oldShape, std::vector<Triangle>& triangles, double op, bool useHsv )
{
    std::vector<cv::Point> oldShape22;
    for ( int sh = 0; sh < oldShape.num_parts(); sh++ )
    {
        oldShape22.push_back ( cv::Point ( oldShape.part ( sh ).x(), oldShape.part ( sh ).y() ) );
    }
    change_faces ( imageFromMat, imageToMat, pointsMask, oldShape22, triangles, op, useHsv );
}
void change_faces ( cv::Mat& imageFromMat, cv::Mat& imageToMat, std::vector<cv::Point>& pointsMask, std::vector<cv::Point>& oldShape, std::vector<Triangle>& triangles, double op, bool useHsv)
{
    int width = imageToMat.cols;
    cout << "Width "<< width << endl;

    for ( int i = 0; i < triangles.size(); i++ )
    {
        triangles[i].minX = std::min ( std::min ( oldShape[triangles[i].point1].x, oldShape[triangles[i].point2].x ), oldShape[triangles[i].point3].x );
        triangles[i].maxX = std::max ( std::max ( oldShape[triangles[i].point1].x, oldShape[triangles[i].point2].x ), oldShape[triangles[i].point3].x );
        triangles[i].minY = std::min ( std::min ( oldShape[triangles[i].point1].y, oldShape[triangles[i].point2].y ), oldShape[triangles[i].point3].y );
        triangles[i].maxY = std::max ( std::max ( oldShape[triangles[i].point1].y, oldShape[triangles[i].point2].y ), oldShape[triangles[i].point3].y );
    }
    cout << "Max min triangles set" << endl;
    for ( int k = 0; k < triangles.size(); k++ )
    {
        Triangle triangle = triangles[k];
        cv::Point2f srcTri[3];
        cv::Point2f dstTri[3];
        cv::Mat affine ( 2, 3, CV_32FC1 );

        srcTri[0] = cv::Point2f ( oldShape[triangle.point1].x,
                                  oldShape[triangle.point1].y );
        srcTri[1] = cv::Point2f ( oldShape[triangle.point2].x,
                                  oldShape[triangle.point2].y );
        srcTri[2] = cv::Point2f ( oldShape[triangle.point3].x,
                                  oldShape[triangle.point3].y );

        dstTri[0] = cv::Point2f ( pointsMask[triangle.point1].x,
                                  pointsMask[triangle.point1].y );
        dstTri[1] = cv::Point2f ( pointsMask[triangle.point2].x,
                                  pointsMask[triangle.point2].y );
        dstTri[2] = cv::Point2f ( pointsMask[triangle.point3].x,
                                  pointsMask[triangle.point3].y );

        affine = cv::getAffineTransform ( srcTri, dstTri );

        for ( int i = triangle.minX; i <= triangle.maxX; i++ )
        {
            int* minMax = new int[4];
            minMax[2] = -1;
            minMax[3] = -1;
            getBorder ( oldShape[triangle.point1], oldShape[triangle.point2], oldShape[triangle.point3], i, minMax );
            getBorder ( oldShape[triangle.point2], oldShape[triangle.point3], oldShape[triangle.point1], i, minMax );
            getBorder ( oldShape[triangle.point3], oldShape[triangle.point1], oldShape[triangle.point2], i, minMax );
            if ( minMax[0] >= triangle.minY && minMax[1] <= triangle.maxY )
            {
                for ( int j = minMax[0]; j < minMax[1]; j++ )
                {
                    double origX = affine.at<double> ( 0, 0 ) * i
                                   + affine.at<double> ( 0, 1 ) * j + affine.at<double> ( 0, 2 );
                    double origY = affine.at<double> ( 1, 0 ) * i
                                   + affine.at<double> ( 1, 1 ) * j + affine.at<double> ( 1, 2 );
				  
                    if ( ( origX >=0 && origX < imageFromMat.cols ) && ( origY >=0 && origY < imageFromMat.rows ) && j >=0 && j < imageToMat.rows && i >=0 && i < imageToMat.cols )
                    {
                        cv::Vec4b pixelFrom = imageFromMat.at<cv::Vec4b> ( origY, origX );
                        cv::Vec4b pixelTo = imageToMat.at<cv::Vec4b> ( j, i );
			
			int alpha = pixelFrom[3] * op;
			// TEST HSV
                        if ( useHsv )
                        {
                            Mat HSVto;
                            Mat RGBto = imageToMat ( Rect ( i, j, 1, 1 ) );
                            Mat HSVfrom;
                            Mat RGBfrom = imageFromMat ( Rect ( origX, origY, 1, 1 ) );
                            cvtColor ( RGBfrom, HSVfrom, CV_BGR2HSV );
                            cvtColor ( RGBto, HSVto, CV_BGR2HSV );
                            Vec3b hsvTo = HSVto.at<Vec3b> ( 0, 0 );
                            Vec3b hsvFrom = HSVfrom.at<Vec3b> ( 0, 0 );
                            hsvTo[0] = hsvFrom[0];
                            hsvTo[1] = hsvFrom[1];
                            HSVto.at<Vec3b> ( 0, 0 ) = hsvTo;
                            cvtColor ( HSVto, RGBto, CV_HSV2BGR );
                            cv::Vec3b  pixelFrom2 = RGBto.at<cv::Vec3b> ( 0, 0 );
                            pixelFrom[0] = pixelFrom2[0];
                            pixelFrom[1] = pixelFrom2[1];
                            pixelFrom[2] = pixelFrom2[2];
                        }
			//imageToMat.at<cv::Vec3b> ( j, i ) = RGBto.at<Vec3b>(0, 0);
			//continue;
			
                        // ��������� �� ���� �������(RGB)
                        for ( int ij = 0; ij < 3; ij++ )
                        {
                            pixelTo[ij] = ( pixelTo[ij] * ( 255 - alpha )
                                            + pixelFrom[ij] * alpha ) / 255;
                        }
                        imageToMat.at<cv::Vec4b> ( j, i ) = pixelTo;
                    }
                }
            }
            else
                cout << "Epic fail!" <<endl;

        }
    }
    cout << "Exit " << endl;
}

void getBorder ( cv::Point& p1, cv::Point& p2, cv::Point& opposite, int x, int* minMax )
{
    if ( p2.x != p1.x )
    {
        double y = p1.y + ( x - p1.x ) * ( p2.y - p1.y ) / ( p2.x - p1.x );
        double y2 = p1.y
                    + ( opposite.x - p1.x ) * ( p2.y - p1.y ) / ( p2.x - p1.x );
        if ( opposite.y > y2 )
        {
            // TODO possible error
            if ( minMax[2] == -1 )
            {
                minMax[2] = 0;
                minMax[0] = ( int ) y;
            }
            minMax[0] = std::max ( minMax[0], ( int ) y );
        }
        else
        {
            if ( minMax[3] == -1 )
            {
                minMax[3] = 0;
                minMax[1] = ( int ) y;
            }
            minMax[1] = std::min ( minMax[1], ( int ) y );
        }

    }
}

std::vector<Line> get_lines_from_file ( std::string path )
{
    std::vector<Line> out;
    std::ifstream file ( path.c_str() );
    std::string str;
    char delim = ';';
    while ( std::getline ( file,str ) )
    {
        std::vector<string> vals;
        split ( str,';',vals );
        out.push_back ( Line ( atoi ( vals[0].c_str() ),atoi ( vals[1].c_str() ) ) );
    }
    return out;
}
std::vector<Triangle> get_triangles_from_file ( string path )
{
    std::vector<Triangle> out;
    std::ifstream file ( path.c_str() );
    std::string str;
    char delim = ';';
    while ( std::getline ( file,str ) )
    {
        std::vector<string> vals;
        split ( str,';',vals );
        out.push_back ( Triangle ( atoi ( vals[0].c_str() ),atoi ( vals[1].c_str() ),atoi ( vals[2].c_str() ) ) );
    }
    return out;
}

void loadImgLab ( string path, std::vector<cv::Point>& points )
{
    cout << "1" << endl;
    std::ifstream file ( path.c_str() );
    std::string str;
    while ( std::getline ( file, str ) )
    {
        cout << "line " << str << endl;
        std::size_t p1 = str.find ( "part name" );
        if ( p1 != std::string::npos )
        {
            points.push_back ( cv::Point ( atoi ( getAttribute ( str, "x" ).c_str() ), atoi ( getAttribute ( str, "y" ).c_str() ) ) );
        }

    }

}

std::string getAttribute ( std::string line, std::string attribute )
{
    std::size_t p1 = line.find ( attribute + "='" );
    return line.substr ( p1 + attribute.length() + 2, line.find ( "'", p1 + attribute.length() + 2 ) - p1 - attribute.length() - 2 );
}

void split ( const string& s, char delim,std::vector<string>& v )
{
    int i = 0;
    int pos = s.find ( delim );
    while ( pos != string::npos )
    {
        v.push_back ( s.substr ( i, pos-i ) );
        i = ++pos;
        pos = s.find ( delim, pos );

        if ( pos == string::npos )
            v.push_back ( s.substr ( i, s.length() ) );
    }
}

void flipPo ( std::vector<cv::Point>& src, std::vector<cv::Point>& dst, int width, int p2[] )
{
    for ( int i = 0; i < src.size(); i++ )
    {
        dst.push_back ( cv::Point ( width - src[p2[i]].x, src[p2[i]].y ) );
    }
}

void flipTriangles ( std::vector<Triangle>& trianglesIn, std::vector<Triangle>& trianglesOut, int p2[] )
{
    for ( int i = 0; i < trianglesIn.size(); i++ )
    {
      Triangle tr = trianglesIn[i];
        trianglesOut.push_back ( Triangle (p2[tr.point1], p2[tr.point2], p2[tr.point3] ) );
    }
}
// src - points on eye, dst - points on image
void convertPoints ( std::vector<cv::Point>& src, std::vector<cv::Point>& dst, int pointss[], int pointss1[], bool useTwoPoints ) //std::vector<cv::Point>& src2, std::vector<cv::Point>& dst2)
{
    cv::Point2f srcTri[3];
    cv::Point2f dstTri[3];
    cv::Mat affine ( 2, 3, CV_32FC1 );


    srcTri[0] = cv::Point2f ( src[pointss[0]].x,
                              src[pointss[0]].y );
    srcTri[1] = cv::Point2f ( src[pointss[1]].x,
                              src[pointss[1]].y );
    if ( !useTwoPoints )
    {
        srcTri[2] = cv::Point2f ( src[pointss[2]].x,
                                  src[pointss[2]].y );
        dstTri[2] = cv::Point2f ( dst[pointss1[2]].x,
                                  dst[pointss1[2]].y );
    }
    else
    {
        srcTri[2] = cv::Point2f ( src[pointss[1]].x + ( src[pointss[1]].y - src[pointss[0]].y ),
                                  src[pointss[1]].y - ( src[pointss[1]].x - src[pointss[0]].x ) );
        dstTri[2] = cv::Point2f ( dst[pointss1[1]].x + ( dst[pointss1[1]].y - dst[pointss1[0]].y ),
                                  dst[pointss1[1]].y - ( dst[pointss1[1]].x - dst[pointss1[0]].x ) );
    }

    dstTri[0] = cv::Point2f ( dst[pointss1[0]].x,
                              dst[pointss1[0]].y );
    dstTri[1] = cv::Point2f ( dst[pointss1[1]].x,
                              dst[pointss1[1]].y );

    affine = cv::getAffineTransform ( srcTri, dstTri );
    std::vector<cv::Point> added;
    std::vector<cv::Point> added2;
    for ( int i = dst.size(); i < src.size(); i++ )
    {
        added.push_back ( src[i] );
    }
    cv::transform ( added, added2, affine );
    for ( int i = 0; i < added2.size(); i++ )
    {
        dst.push_back ( added2[i] );
    }
}
