package ru.flightlabs.makeup;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.Log;

import org.opencv.core.Point;
import org.opencv.core.Rect;

import ru.flightlabs.masks.Static;

/**
 * Created by sov on 27.11.2016.
 */

public class ResourcesApp {

    // FIXME make it small
    public TypedArray eyelashesSmall;
    public TypedArray eyeshadowSmall;
    public TypedArray eyelinesSmall;
    public TypedArray lipsSmall;
    public TypedArray fashionSmall;
    public String[] fashions;

    public ResourcesApp(Context context) {
        eyelashesSmall = context.getResources().obtainTypedArray(R.array.eyelashes);
        eyeshadowSmall = context.getResources().obtainTypedArray(R.array.eyeshadow);
        eyelinesSmall = context.getResources().obtainTypedArray(R.array.eyelines);
        lipsSmall = context.getResources().obtainTypedArray(R.array.lips);
        fashionSmall = context.getResources().obtainTypedArray(R.array.fashion_ic);
        fashions = context.getResources().getStringArray(R.array.fashion_ic1);
    }
}
