package ru.flightlabs.makeup;

import android.content.Context;
import android.content.res.TypedArray;

/**
 * Created by sov on 27.11.2016.
 */

public class ResourcesApp {

    // FIXME make it small
    public TypedArray eyelashesTextures;
    public TypedArray eyelashesIcons;

    public TypedArray eyeshadowTextures;
    public TypedArray eyeshadowIcons;

    public TypedArray eyelinesTextures;
    public TypedArray eyelinesIcons;

    public TypedArray lipsSmall;
    public TypedArray fashionIcons;
    public String[] fashions;

    public ResourcesApp(Context context) {
        eyelashesTextures = context.getResources().obtainTypedArray(R.array.eyelashes);
        eyelashesIcons = context.getResources().obtainTypedArray(R.array.eyelashesIcon);

        eyeshadowTextures = context.getResources().obtainTypedArray(R.array.eyeshadow);
        eyeshadowIcons = context.getResources().obtainTypedArray(R.array.eyeshadowIcon);

        eyelinesTextures = context.getResources().obtainTypedArray(R.array.eyelines);
        eyelinesIcons = context.getResources().obtainTypedArray(R.array.eyelinesIcons);

        lipsSmall = context.getResources().obtainTypedArray(R.array.lips);
        fashionIcons = context.getResources().obtainTypedArray(R.array.fashion_ic);
        fashions = context.getResources().getStringArray(R.array.fashion_ic1);
    }
}
