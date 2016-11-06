package ru.flightlabs.masks.model.primitives;

public class Line {
    public int pointStart;
    public int pointEnd;
    public boolean solid;
    
    public Line(int pointStart, int pointEnd, boolean solid) {
        this.pointStart = pointStart;
        this.pointEnd = pointEnd;
        this.solid = solid;
    }
    
    public Line(int pointStart, int pointEnd) {
        this(pointStart, pointEnd, false);
    }
    
    public boolean same(Line line) {
        if ((line.pointStart == pointStart && line.pointEnd == pointEnd) || (line.pointStart == pointEnd && line.pointEnd == pointStart)) {
            return true;
        }
        return false;
    }

}
