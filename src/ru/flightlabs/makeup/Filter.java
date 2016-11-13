package ru.flightlabs.makeup;

import java.io.File;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;

import ru.flightlabs.masks.model.primitives.Triangle;

import android.util.Log;

public class Filter {
    
    private long mNativeObj = 0;
    private Long mNativeModel = null;
    
    public Filter(String cascadeName, int minFaceSize, String modelSp) {
        mNativeObj = nativeCreateObject(cascadeName, minFaceSize);
        if (new File(modelSp).exists()) {
            Log.e("DetectionBasedTracker", "findEyes DetectionBasedTracker !" + modelSp);
            long nat = nativeCreateModel(modelSp);
            if (nat != 0) {
                mNativeModel = nat;
            }
        } else {
            Log.e("DetectionBasedTracker", "findEyes file doesn't exists !" + modelSp);
        }
        Log.e("DetectionBasedTracker", "findEyes mNativeModel " + mNativeModel);
    }
    
    public Point[] findEyes(Mat imageGray, Rect face) {
        if (mNativeModel != null) {
//            return findEyes(mNativeObj, imageGray.getNativeObjAddr(), face.x, face.y, face.width, face.height, mNativeModel);
            return findEyes(mNativeObj, imageGray.getNativeObjAddr(), face.x, face.y, face.width, face.height, mNativeModel);
        } else {
            return new Point[0];
        }
    }

    // draw mask on face using triangulation model
    public void drawMask(Mat currentMaskLandScaped, Mat mRgba, ru.flightlabs.masks.model.primitives.Point[] pointsWas, Point[] foundEyes, Triangle[] trianlges, Mat lips, ru.flightlabs.masks.model.primitives.Point[] pointsWasLips, Triangle[] trianlgesLips) {
        nativeDrawMask(currentMaskLandScaped.getNativeObjAddr(), mRgba.getNativeObjAddr(), pointsWas,
                foundEyes, trianlges, lips.getNativeObjAddr(), pointsWasLips, trianlgesLips);
     }
    
    private static native long nativeCreateObject(String cascadeName, int minFaceSize);
    private static native long nativeCreateModel(String cascadeName);
    private static native Point[] findEyes(long thiz, long inputImage, int x, int y, int height, int width, long modelSp);
    private static native void nativeDrawMask(long maskImage, long toImage, ru.flightlabs.masks.model.primitives.Point[] pointsWas,
            Point[] foundEyes, Triangle[] trianlges, long lips, ru.flightlabs.masks.model.primitives.Point[] pointsWasLips, Triangle[] trianlgesLips);

}
