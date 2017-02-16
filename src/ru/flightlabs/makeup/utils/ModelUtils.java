package ru.flightlabs.makeup.utils;

import android.content.res.AssetManager;

import org.opencv.core.Point;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import ru.flightlabs.masks.model.primitives.Triangle;

/**
 * Created by sov on 16.02.2017.
 */

public class ModelUtils {
    public static ru.flightlabs.masks.model.primitives.Point[] getReversePoint(ru.flightlabs.masks.model.primitives.Point[] onImageEyeLeft, int width, int[] ints) {
        ru.flightlabs.masks.model.primitives.Point[] result = new ru.flightlabs.masks.model.primitives.Point[onImageEyeLeft.length];
        for (int i = 0; i < ints.length; i++) {
            ru.flightlabs.masks.model.primitives.Point p = onImageEyeLeft[ints[i]];
            ru.flightlabs.masks.model.primitives.Point newP = new ru.flightlabs.masks.model.primitives.Point(p.x, p.y);
            newP.x = width - newP.x;
            result[i] = newP;
        }
        return result;
    }

    public static Triangle[] flipTriangles(Triangle[] tr, int[] ints) {
        Triangle[] result = new Triangle[tr.length];
        for (int i = 0; i < tr.length; i++) {
            Triangle d = tr[i];
            result[i] = new Triangle(ints[d.point1], ints[d.point2], ints[d.point3]);
        }
        return result;
    }

    public static Point[] getOnlyPoints(Point[] pointsOnFrame, int indexStart, int number) {
        Point[] result = new Point[number];
        for (int i = 0; i < number; i++) {
            result[i] = pointsOnFrame[indexStart + i];
        }
        return result;
    }


    public static Triangle[] loadriangle(AssetManager assetManager, String assetName) throws IOException {
        List<Triangle> triangleArr = new ArrayList<Triangle>();
        InputStream ims;
        ims = assetManager.open(assetName);
        BufferedReader in = new BufferedReader(new InputStreamReader(ims));
        String line = null;
        while ((line = in.readLine()) != null) {
            String[] spl = line.split(";");
            if (spl.length == 3) {
                triangleArr.add(new Triangle(Integer.parseInt(spl[0]), Integer.parseInt(spl[1]), Integer
                        .parseInt(spl[2])));
            }
        }
        ims.close();
        return triangleArr.toArray(new Triangle[0]);
    }
}
