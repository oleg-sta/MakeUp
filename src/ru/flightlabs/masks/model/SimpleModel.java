package ru.flightlabs.masks.model;

import ru.flightlabs.masks.model.primitives.Line;
import ru.flightlabs.masks.model.primitives.Point;

public interface SimpleModel {
    Point[] getPointsWas();
    Point[] getPointsTo();
    Line[] getLines();
    

}
