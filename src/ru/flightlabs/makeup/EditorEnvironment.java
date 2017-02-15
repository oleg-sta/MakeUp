package ru.flightlabs.makeup;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import ru.flightlabs.makeup.utils.Helper;
import ru.flightlabs.masks.model.ImgLabModel;
import ru.flightlabs.masks.model.SimpleModel;
import ru.flightlabs.masks.model.primitives.Triangle;

/**
 * Created by sov on 27.11.2016.
 */

// TODO use all editor functions here
// TODO use normal architecture
public class EditorEnvironment {
    Context activity;
    ResourcesApp resourcesApp;
    public static int catgoryNum = 0;

    public static final int EYE_LASH = 0;
    public static final int EYE_SHADOW = 1;
    public static final int EYE_LINE = 2;
    public static final int LIPS = 3;
    public static final int FASHION = 4;

    public static int[] EYE_LASH_COLORS;
    public static int[] EYE_SHADOW_COLORS;
    public static int[] EYE_LINE_COLORS;
    public static int[] LIPS_COLORS;

    public static int[] prevIndexItem = {1, 1, 1, 1, 1};
    public static int[] currentIndexItem = {1, 1, 1, 1, 1};
    public static int[] currentColor = {-1, -1, -1, -1, -1};
    public static int[] opacity = {50, 50, 50, 50, 50};
    public static int newIndexItem = 0;

    private static final String TAG = "EditorEnvironment_class";
    // lips and eyes models points and triangles
    public static ru.flightlabs.masks.model.primitives.Point[] pointsLeftEye;
    public static Triangle[] trianglesLeftEye;
    public static ru.flightlabs.masks.model.primitives.Point[] pointsWasLips;
    public static Triangle[] trianglesLips;


    Mat leftEyeLash;
    Mat rightEyeLash;
    Mat leftEyeShadow;
    Mat rightEyeShadow;
    Mat leftEyeLine;
    Mat rightEyeLine;
    Mat lips;

    public EditorEnvironment(Context activity, ResourcesApp resourcesApp) {
        this.activity = activity;
        this.resourcesApp = resourcesApp;
    }

    public void init() {
        loadNewMakeUp(EYE_LASH, 0);
        loadNewMakeUp(EYE_SHADOW, 0);
        loadNewMakeUp(EYE_LINE, 0);
        loadNewMakeUp(LIPS, 0);
        EYE_LASH_COLORS = activity.getResources().getIntArray(R.array.colors_eyelashes);
        EYE_SHADOW_COLORS = activity.getResources().getIntArray(R.array.colors_shadow);
        EYE_LINE_COLORS = activity.getResources().getIntArray(R.array.colors_shadow);
        LIPS_COLORS = activity.getResources().getIntArray(R.array.colors_lips);
        loadModels();
    }

    private void loadModels() {
        try {
            File cascadeDir = activity.getDir("cascade", Context.MODE_PRIVATE);
            File fModel = new File(cascadeDir, "landmarks_eye.xml");
//            resourceToFile(getResources().openRawResource(R.raw.eye_real_landmarks), fModel);
            Helper.resourceToFile(activity.getResources().openRawResource(R.raw.eye_real_landmarks), fModel);
            File fModelLips = new File(cascadeDir, "landmarks_lips.xml");
            Helper.resourceToFile(activity.getResources().openRawResource(R.raw.lips_icon_landmarks), fModelLips);
            AssetManager assetManager = activity.getAssets();
            trianglesLeftEye = loadriangle(assetManager, "eye_real_triangles.txt");
            trianglesLips = loadriangle(assetManager, "lips_icon_triangles.txt");
            SimpleModel modelFrom = new ImgLabModel(fModel.getPath());
            pointsLeftEye = modelFrom.getPointsWas();
            SimpleModel modelFromLibs = new ImgLabModel(fModelLips.getPath());
            pointsWasLips = modelFromLibs.getPointsWas();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            throw new RuntimeException(e);
        }
    }

    // TODO: лучше делать асинхронно
    // загрузка png через java, т.к. Opencv не загружает alpha-канал из файла
    private Mat loadPngToMat(int index, boolean flip) {
        File cascadeDir = activity.getDir("cascade", Context.MODE_PRIVATE);
        File newEyeFile = new File(cascadeDir, "temp.png");
        Helper.resourceToFile(activity.getResources().openRawResource(index), newEyeFile);

        Mat newEyeTmp2 = Helper.loadPngToMat(newEyeFile);
        // load eye to Mat
        // используем загрузку через андроид, т.к. opencv ломает цвета
        Log.i(TAG, "loadNewEye2 " + index + " " + newEyeTmp2.type() + " " + newEyeTmp2.channels());
        Mat newEyeTmp;
        if (flip) {
            newEyeTmp = newEyeTmp2.t();
            Core.flip(newEyeTmp2.t(), newEyeTmp, 0);
            newEyeTmp2.release();
        } else {
            newEyeTmp = newEyeTmp2;
        }
        cascadeDir.delete();
        return newEyeTmp;
    }

    public void editImage(Mat mRgba, Point[] pointsOnFrame) {
        // we should use opengl
        Point[] onImageEyeLeft = getOnlyPoints(pointsOnFrame, 0, 6);
        Point[] onImageEyeRight = getOnlyPoints(pointsOnFrame, 6, 6);
        ru.flightlabs.masks.model.primitives.Point[] pointsRightEye = getReversePoint(pointsLeftEye, rightEyeLash.width(), new int[] {3, 2, 1, 0, 5, 4, 7, 6, 9, 8});
        Triangle[] trianglesRightEye = flipTriangles(trianglesLeftEye, new int[] {3, 2, 1, 0, 5, 4, 7, 6, 9, 8});
        Log.i(TAG, "saving Java_ru_flightlabs_makeup_Filter_nativeDrawMask6 java " + currentColor[3]);
        // TODO add checkbox for rgb or hsv bleding
        if (currentColor[EYE_SHADOW] != -1) {
            //filter.drawMask(leftEyeShadow, mRgba, pointsLeftEye, onImageEyeLeft, trianglesLeftEye, opacity[EYE_SHADOW] / 100.0, false, currentColor[1]);
            //filter.drawMask(rightEyeShadow, mRgba, pointsRightEye, onImageEyeRight, trianglesRightEye, opacity[EYE_SHADOW] / 100.0, true, currentColor[1]);
        }

        if (currentColor[EYE_LINE] != -1) {
            //filter.drawMask(leftEyeLine, mRgba, pointsLeftEye, onImageEyeLeft, trianglesLeftEye, opacity[EYE_LINE] / 100.0, false, currentColor[2]);
            //filter.drawMask(rightEyeLine, mRgba, pointsRightEye, onImageEyeRight, trianglesRightEye, opacity[EYE_LINE] / 100.0, false, currentColor[2]);
        }

        if (currentColor[EYE_LASH] != -1) {
            //filter.drawMask(leftEyeLash, mRgba, pointsLeftEye, onImageEyeLeft, trianglesLeftEye, opacity[EYE_LASH] / 100.0, false, currentColor[0]);
            //filter.drawMask(rightEyeLash, mRgba, pointsRightEye, onImageEyeRight, trianglesRightEye, opacity[EYE_LASH] / 100.0, false, currentColor[0]);
        }

        if (currentColor[LIPS] != -1) {
            //filter.drawMask(lips, mRgba, pointsWasLips, getOnlyPoints(pointsOnFrame, 12, 20), trianglesLips, opacity[LIPS] / 100.0, true, currentColor[3]);
        }
    }

    private ru.flightlabs.masks.model.primitives.Point[] getReversePoint(ru.flightlabs.masks.model.primitives.Point[] onImageEyeLeft, int width, int[] ints) {
        ru.flightlabs.masks.model.primitives.Point[] result = new ru.flightlabs.masks.model.primitives.Point[onImageEyeLeft.length];
        for (int i = 0; i < ints.length; i++) {
            ru.flightlabs.masks.model.primitives.Point p = onImageEyeLeft[ints[i]];
            ru.flightlabs.masks.model.primitives.Point newP = new ru.flightlabs.masks.model.primitives.Point(p.x, p.y);
            newP.x = width - newP.x;
            result[i] = newP;
        }
        return result;
    }

    private Triangle[] flipTriangles(Triangle[] tr, int[] ints) {
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


    private Triangle[] loadriangle(AssetManager assetManager, String assetName) throws IOException {
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

    // загрузка ресниц и губ
    // FIXME everything is wrong
    public void loadNewMakeUp(int category, int index) {
        currentIndexItem[category] = index;
        switch (category) {
            case EYE_LASH: leftEyeLash = loadPngToMat(resourcesApp.eyelashesSmall.getResourceId(index, 0), false);
                rightEyeLash = new Mat();
                Core.flip(leftEyeLash, rightEyeLash, 1);
                break;
            case EYE_SHADOW: leftEyeShadow = loadPngToMat(resourcesApp.eyeshadowSmall.getResourceId(index, 0), false);
                rightEyeShadow = new Mat();
                Core.flip(leftEyeShadow, rightEyeShadow, 1);
                break;
            case EYE_LINE: leftEyeLine = loadPngToMat(resourcesApp.eyelinesSmall.getResourceId(index, 0), false);
                rightEyeLine = new Mat();
                Core.flip(leftEyeLine, rightEyeLine, 1);
                break;
            default:
                lips = loadPngToMat(resourcesApp.lipsSmall.getResourceId(index, 0), false);
                break;
        }
    }

}
