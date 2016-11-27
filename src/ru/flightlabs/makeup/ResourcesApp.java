package ru.flightlabs.makeup;

import android.content.Context;
import android.content.res.TypedArray;

import org.opencv.core.Point;
import org.opencv.core.Rect;

/**
 * Created by sov on 27.11.2016.
 */

public class ResourcesApp {

    // unbelieveable!
    public static Filter filter;
    public static Rect face;
    public static Point[] pointsOnFrame;

    // FIXME make it small
    TypedArray eyelashesSmall;
    TypedArray eyeshadowSmall;
    TypedArray eyelinesSmall;
    TypedArray lipsSmall;

    public ResourcesApp(Context context) {
        eyelashesSmall = context.getResources().obtainTypedArray(R.array.eyelashes);
        eyeshadowSmall = context.getResources().obtainTypedArray(R.array.eyeshadow);
        eyelinesSmall = context.getResources().obtainTypedArray(R.array.eyelines);
        lipsSmall = context.getResources().obtainTypedArray(R.array.lips);
    }
}