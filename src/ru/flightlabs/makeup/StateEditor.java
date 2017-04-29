package ru.flightlabs.makeup;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Color;

import java.io.File;
import java.io.IOException;

import ru.flightlabs.makeup.utils.BitmapUtils;
import ru.flightlabs.makeup.utils.ModelUtils;
import ru.flightlabs.masks.model.ImgLabModel;
import ru.flightlabs.masks.model.SimpleModel;
import ru.flightlabs.masks.model.primitives.Triangle;

/**
 * Created by sov on 27.11.2016.
 */

// TODO use all editor functions here
// TODO use normal architecture
public class StateEditor {
    Context activity;
    ResourcesApp resourcesApp;

    public static final int EYE_LASH = 0;
    public static final int EYE_SHADOW = 1;
    public static final int EYE_LINE = 2;
    public static final int LIPS = 3;
    public static final int FASHION = 4;

    private int[] EYE_LASH_COLORS;
    private String[] EYE_SHADOW_COLORS;
    private int[] EYE_LINE_COLORS;
    private int[] LIPS_COLORS;

    private int[] prevIndexItem = {1, 1, 1, 1, 1};
    private int[] currentIndexItem = {1, 1, 1, 1, 5};
    private int[] currentColorIndex = {-1, -1, -1, -1, -1};
    private int[] opacity = {50, 50, 50, 50, 50};

    private static final String TAG = "EditorEnvironment_class";
    // lips and eyes models points and triangles
    public static ru.flightlabs.masks.model.primitives.Point[] pointsLeftEye;
    public static ru.flightlabs.masks.model.primitives.Point[] pointsLeftEyeNew;
    public static Triangle[] trianglesLeftEye;
    public static ru.flightlabs.masks.model.primitives.Point[] pointsWasLips;
    public static ru.flightlabs.masks.model.primitives.Point[] pointsWasLipsNew;
    public static Triangle[] trianglesLips;

    public StateEditor(Context activity, ResourcesApp resourcesApp) {
        this.activity = activity;
        this.resourcesApp = resourcesApp;
    }

    public void init() {
        EYE_LASH_COLORS = activity.getResources().getIntArray(R.array.colors_eyelashes);
        EYE_SHADOW_COLORS = activity.getResources().getStringArray(R.array.colors_shadow);
        EYE_LINE_COLORS = activity.getResources().getIntArray(R.array.colors_eyelashes);
        LIPS_COLORS = activity.getResources().getIntArray(R.array.colors_lips);
        loadModels();
    }

    private void loadModels() {
        try {
            File cascadeDir = activity.getDir("cascade", Context.MODE_PRIVATE);
//            resourceToFile(getResources().openRawResource(R.raw.eye_real_landmarks), fModel);
            AssetManager assetManager = activity.getAssets();
            trianglesLeftEye = ModelUtils.loadriangle(assetManager, "eye_real_triangles.txt");
            trianglesLips = ModelUtils.loadriangle(assetManager, "lips_icon_triangles.txt");
            pointsLeftEye = loadPointsFromImglab(cascadeDir, R.raw.eye_real_landmarks);
            pointsLeftEyeNew = loadPointsFromImglab(cascadeDir, R.raw.eye_real_landmarks_new);
            pointsWasLips = loadPointsFromImglab(cascadeDir, R.raw.lips_icon_landmarks);
            pointsWasLipsNew = loadPointsFromImglab(cascadeDir, R.raw.lips_icon_landmarks_new);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            throw new RuntimeException(e);
        }
    }

    private ru.flightlabs.masks.model.primitives.Point[] loadPointsFromImglab(File cascadeDir, int idResource) {
        File fModel = new File(cascadeDir, "temp_imglab.xml");
        BitmapUtils.resourceToFile(activity.getResources().openRawResource(idResource), fModel);
        SimpleModel modelFrom = new ImgLabModel(fModel.getPath());
        fModel.delete();
        return  modelFrom.getPointsWas();
    }


    public String[] getFashionNames() {
        String[] names = new String[resourcesApp.fashions.length];
        for ( int i = 0; i < names.length; i++) {
            names[i] = resourcesApp.fashions[i].split(";")[8];
        }
        return names;
    }

    public boolean changed(int itemNum) {
        if (currentIndexItem[itemNum] != prevIndexItem[itemNum]) {
            prevIndexItem[itemNum] = currentIndexItem[itemNum];
            return true;
        }
        return false;
    }

    public int getResourceId(int itemNum) {
        switch (itemNum) {
            case EYE_LASH:
                return resourcesApp.eyelashesTextures.getResourceId(currentIndexItem[EYE_LASH], 0);
            case EYE_SHADOW:
                // TODO it has multiple layers
                return resourcesApp.eyeshadowTextures.getResourceId(currentIndexItem[EYE_SHADOW], 0);
            case EYE_LINE:
                return resourcesApp.eyelinesTextures.getResourceId(currentIndexItem[EYE_LINE], 0);
            case LIPS:
                return resourcesApp.lipsSmall.getResourceId(currentIndexItem[LIPS], 0);
            default:
                throw new RuntimeException("Unsupported element");
        }
    }

    public int getColorIndex() {
        return currentColorIndex[LIPS];
    }

    public int[] getResourceIds(int itemNum) {
        switch (itemNum) {
            case EYE_SHADOW:
                if (resourcesApp.eyeshadowTextures.getString(currentIndexItem[EYE_SHADOW]).contains(";")) {
                    String[] w = resourcesApp.eyeshadowTextures.getString(currentIndexItem[EYE_SHADOW]).split(";");
                    int[] resources = new int[w.length];
                    for (int i = 0; i < w.length; i++) {
                        resources[i] = activity.getResources().getIdentifier(w[i], "raw", activity.getPackageName());
                    }
                    return resources;
                } else {
                    return new int[] {resourcesApp.eyeshadowTextures.getResourceId(currentIndexItem[EYE_SHADOW], 0)};
                }
            default:
                throw new RuntimeException("Unsupported element");
        }
    }

    public int[] getColors(int itemNum) {
        switch (itemNum) {
            case EYE_SHADOW:
                String[] colorStr = EYE_SHADOW_COLORS[currentColorIndex[StateEditor.EYE_SHADOW]].split(";");
                int[] res = new int[colorStr.length];
                for (int i = 0; i < colorStr.length; i++) {
                     res[i] = Color.parseColor(colorStr[i]);
                }
                return res;
            default:
                throw new RuntimeException("Unsupported element");
        }
    }

    private static int[] convertToSimpleColor(String[] colors) {
        int[] res = new int[colors.length];
        for (int i = 0; i < colors.length; i++) {
            res[i] = Color.parseColor(colors[i].split(";")[0]);
        }
        return res;
    }
    public int[] getAllColors(int itemNum) {
        switch (itemNum) {
            case EYE_LASH:
                return EYE_LASH_COLORS;
            case EYE_SHADOW:
                return convertToSimpleColor(EYE_SHADOW_COLORS);
            case EYE_LINE:
                return EYE_LINE_COLORS;
            case LIPS:
                return LIPS_COLORS;
            case FASHION:
                return new int[0];
            default:
                throw new RuntimeException("Unsupported element");
        }
    }

    public int getColor(int itemNum) {
        switch (itemNum) {
            case EYE_LASH:
                return EYE_LASH_COLORS[currentColorIndex[StateEditor.EYE_LASH]];
            case EYE_SHADOW:
                String colorStr = EYE_SHADOW_COLORS[currentColorIndex[StateEditor.EYE_SHADOW]].split(";")[0];
                return Color.parseColor(colorStr);
            case EYE_LINE:
                return EYE_LINE_COLORS[currentColorIndex[StateEditor.EYE_LINE]];
            case LIPS:
                return LIPS_COLORS[currentColorIndex[StateEditor.LIPS]];
            default:
                throw new RuntimeException("Unsupported element");
        }
    }

    public float getOpacityFloat(int itemNum) {
        return getOpacity(itemNum) / 100f;
    }

    public int getCurrentIndex(int itemNum) {
        return currentIndexItem[itemNum];
    }

    public int getOpacity(int itemNum) {
        return opacity[itemNum];
    }

    public void setOpacity(int itemNum, int i) {
        opacity[itemNum] = i;
        if (itemNum == FASHION) {
            for (int j = 0; j < opacity.length; j++) {
                opacity[j] = i;
            }
        }
    }

    public void setParametersFromFashion(int newItem) {
        String[] fash = resourcesApp.fashions[newItem].split(";");
        currentIndexItem[StateEditor.EYE_LASH] = Integer.parseInt(fash[0]);
        currentIndexItem[StateEditor.EYE_SHADOW] = Integer.parseInt(fash[1]);
        currentIndexItem[StateEditor.EYE_LINE] = Integer.parseInt(fash[2]);
        currentIndexItem[StateEditor.LIPS] = Integer.parseInt(fash[3]);
        currentColorIndex[StateEditor.EYE_LASH] = Integer.parseInt(fash[4]);
        currentColorIndex[StateEditor.EYE_SHADOW] = Integer.parseInt(fash[5]);
        currentColorIndex[StateEditor.EYE_LINE] = Integer.parseInt(fash[6]);
        currentColorIndex[StateEditor.LIPS] = Integer.parseInt(fash[7]);
    }

    public void setCurrentIndexItem(int currentCategory, int newItem) {
        currentIndexItem[currentCategory] = newItem;
    }

    public void setCurrentColor(int currentCategory, int position) {
        currentColorIndex[currentCategory] = position;
    }
}
