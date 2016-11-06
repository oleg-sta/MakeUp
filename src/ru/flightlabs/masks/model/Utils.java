package ru.flightlabs.masks.model;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.imgproc.Imgproc;

import ru.flightlabs.masks.model.primitives.Point;

public class Utils {
    
    public static void upsideDown(Point[] points) {
        for (Point point: points) {
            point.y = - point.y;
        }
    }
    
    /**
     * ��������� ������ pointsTo ������� �� pointsWas �� ������ ������ ���� �����, ����� affinity-��������������
     * 
     * @param pointsWas
     * @param pointsTo
     * @return
     */
    public static org.opencv.core.Point[] completeModel(Point[] pointsWas, org.opencv.core.Point[] pointsTo, int[] pointsAffine) {
        org.opencv.core.Point[] returnPoints = new org.opencv.core.Point[pointsWas.length];
        MatOfPoint2f srcPoints = new MatOfPoint2f(new org.opencv.core.Point(pointsWas[pointsAffine[0]].x, pointsWas[pointsAffine[0]].y),
                new org.opencv.core.Point(pointsWas[pointsAffine[1]].x, pointsWas[pointsAffine[1]].y),
                new org.opencv.core.Point(pointsWas[pointsAffine[0]].x - (pointsWas[pointsAffine[1]].y - pointsWas[pointsAffine[0]].y),
                        pointsWas[pointsAffine[0]].y + (pointsWas[pointsAffine[1]].x - pointsWas[pointsAffine[0]].x)));
        
        MatOfPoint2f destPoints = new MatOfPoint2f(new org.opencv.core.Point(pointsTo[pointsAffine[0]].x, pointsTo[pointsAffine[0]].y),
                new org.opencv.core.Point(pointsTo[pointsAffine[1]].x, pointsTo[pointsAffine[1]].y),
                new org.opencv.core.Point(pointsTo[pointsAffine[0]].x - (pointsTo[pointsAffine[1]].y - pointsTo[pointsAffine[0]].y),
                        pointsTo[pointsAffine[0]].y + (pointsTo[pointsAffine[1]].x - pointsTo[pointsAffine[0]].x)));
        
        Mat affine = Imgproc.getAffineTransform(srcPoints, destPoints);
        for (int i = 0; i < pointsWas.length; i++) {
            if (i < pointsTo.length) {
                returnPoints[i] = pointsTo[i];
            } else {
                double origX = affine.get(0, 0)[0] * pointsWas[i].x + affine.get(0, 1)[0] * pointsWas[i].y + affine.get(0, 2)[0];
                double origY = affine.get(1, 0)[0] * pointsWas[i].x + affine.get(1, 1)[0] * pointsWas[i].y + affine.get(1, 2)[0];
                returnPoints[i] = new org.opencv.core.Point(origX, origY);
            }
        }
        return returnPoints;
        
    }

}
