package ru.flightlabs.masks.model.primitives;

public class Point {
    public double x, y;
    public Integer origNumPoint;
    
    public Point(double x, double y, Integer origNumPoint) {
        this.x = x;
        this.y = y;
        this.origNumPoint = origNumPoint;
    }
    
    public Point(double x, double y) {
        this(x, y, null);
    }
}
