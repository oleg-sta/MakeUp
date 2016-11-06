package ru.flightlabs.masks.model.primitives;

public class Triangle {
    
    public int point1, point2, point3;
    public Triangle(int point1, int point2, int point3) {
        this.point1 = point1;
        this.point2 = point2;
        this.point3 = point3;
    }
    
    // TODO ������� ������
    public boolean pointInside(Point point, Point[] points) {
        int sign1 = getSide(point, points[point1], points[point2]);
        int sign2 = getSide(point, points[point2], points[point3]);
        int sign3 = getSide(point, points[point3], points[point1]);
        if ((sign1 >= 0 && sign2 >= 0 && sign3 >= 0) || (sign1 <= 0 && sign2 <= 0 && sign3 <= 0)) {
            return true;
        }
        return false;
    }
    
    private static int getSide(Point pointCheck, Point point1, Point point2) {
        if (point1.y != point2.y) {
            return (int)Math.signum((point2.x - point1.x) * (pointCheck.y - point1.y) / (point2.y - point1.y) + point1.x - pointCheck.x) * (int)Math.signum(point2.y - point1.y);
        } else {
            return (int)Math.signum(pointCheck.y - point1.y) * (int)Math.signum(point2.x - point1.x);
        }
    }

}
