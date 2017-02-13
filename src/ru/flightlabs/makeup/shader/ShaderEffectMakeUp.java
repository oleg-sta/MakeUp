package ru.flightlabs.makeup.shader;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

import org.opencv.core.Point;

import ru.flightlabs.makeup.EditorEnvironment;
import ru.flightlabs.makeup.R;
import ru.flightlabs.makeup.ResourcesApp;
import ru.flightlabs.masks.renderer.ShaderEffect;
import ru.flightlabs.masks.renderer.ShaderEffectHelper;
import ru.flightlabs.masks.utils.OpenGlHelper;
import ru.flightlabs.masks.utils.PointsConverter;
import ru.flightlabs.masks.utils.PoseHelper;

/**
 * Created by sov on 13.02.2017.
 */

public class ShaderEffectMakeUp extends ShaderEffect {
    private static final String TAG = "ShaderEffectMakeUp";

    private int eyeShadowTextureid;
    private int eyeLashesTextureid;
    private int eyeLineTextureid;
    private int lipsTextureid;

    public ShaderEffectMakeUp(Context contex) {
        super(contex);
    }

    public void init() {
        super.init();
        eyeShadowTextureid = OpenGlHelper.loadTexture(context, R.raw.eye2_0000_smokey_eeys);
        eyeLashesTextureid = OpenGlHelper.loadTexture(context, R.raw.eye2_0000_lash);
        eyeLineTextureid = OpenGlHelper.loadTexture(context, R.raw.eye2_0000_line);
        lipsTextureid = OpenGlHelper.loadTexture(context, R.raw.lips_icon);
    }

    public void makeShaderMask(int indexEye, PoseHelper.PoseResult poseResult, int width, int height, int texIn, long time, int iGlobTime) {
        Log.i(TAG, "onDrawFrame6 draw maekup");
        int vPos2 = GLES20.glGetAttribLocation(program2dJustCopy, "vPosition");
        int vTex2 = GLES20.glGetAttribLocation(program2dJustCopy, "vTexCoord");
        GLES20.glEnableVertexAttribArray(vPos2);
        GLES20.glEnableVertexAttribArray(vTex2);
        ShaderEffectHelper.shaderEffect2dWholeScreen(poseResult.leftEye, poseResult.rightEye, texIn, program2dJustCopy, vPos2, vTex2);

        if (poseResult.foundLandmarks != null) {
            Point[] onImageEyeLeft = EditorEnvironment.getOnlyPoints(poseResult.foundLandmarks, 36, 6);
            Point[] onImageEyeRight = EditorEnvironment.getOnlyPoints(poseResult.foundLandmarks, 36 + 6, 6);
            // TODO add checkbox for rgb or hsv bleding
            Log.i(TAG, "onDrawFrame6 draw maekup2");
            int vPos22 = GLES20.glGetAttribLocation(program2dTriangles, "vPosition");
            int vTex22 = GLES20.glGetAttribLocation(program2dTriangles, "vTexCoord");
            GLES20.glEnableVertexAttribArray(vPos22);
            GLES20.glEnableVertexAttribArray(vTex22);
            // TODO use blendshape for eyes

            if (EditorEnvironment.currentIndexItem[EditorEnvironment.EYE_SHADOW] != EditorEnvironment.newIndexItem && EditorEnvironment.EYE_SHADOW == EditorEnvironment.catgoryNum) {
                EditorEnvironment.currentIndexItem[EditorEnvironment.EYE_SHADOW] = EditorEnvironment.newIndexItem;
                OpenGlHelper.changeTexture(context, ResourcesApp.eyeshadowSmall.getResourceId(EditorEnvironment.currentIndexItem[EditorEnvironment.EYE_SHADOW], 0), eyeShadowTextureid);
            }
            if (EditorEnvironment.currentIndexItem[EditorEnvironment.EYE_LASH] != EditorEnvironment.newIndexItem && EditorEnvironment.EYE_LASH == EditorEnvironment.catgoryNum) {
                EditorEnvironment.currentIndexItem[EditorEnvironment.EYE_LASH] = EditorEnvironment.newIndexItem;
                OpenGlHelper.changeTexture(context, ResourcesApp.eyelashesSmall.getResourceId(EditorEnvironment.currentIndexItem[EditorEnvironment.EYE_LASH], 0), eyeLashesTextureid);
            }
            if (EditorEnvironment.currentIndexItem[EditorEnvironment.EYE_LINE] != EditorEnvironment.newIndexItem && EditorEnvironment.EYE_LINE == EditorEnvironment.catgoryNum) {
                EditorEnvironment.currentIndexItem[EditorEnvironment.EYE_LINE] = EditorEnvironment.newIndexItem;
                OpenGlHelper.changeTexture(context, ResourcesApp.eyelinesSmall.getResourceId(EditorEnvironment.currentIndexItem[EditorEnvironment.EYE_LINE], 0), eyeLineTextureid);
            }
            if (EditorEnvironment.currentIndexItem[EditorEnvironment.LIPS] != EditorEnvironment.newIndexItem && EditorEnvironment.LIPS == EditorEnvironment.catgoryNum) {
                EditorEnvironment.currentIndexItem[EditorEnvironment.LIPS] = EditorEnvironment.newIndexItem;
                OpenGlHelper.changeTexture(context, ResourcesApp.lipsSmall.getResourceId(EditorEnvironment.currentIndexItem[EditorEnvironment.LIPS], 0), lipsTextureid);
            }
            Point[] onImage = PointsConverter.completePointsByAffine(onImageEyeLeft, PointsConverter.convertToOpencvPoints(EditorEnvironment.pointsLeftEye), new int[]{0, 1, 2, 3, 4, 5});
            // TODO use blendshapes
            onImage = PointsConverter.replacePoints(onImage, onImageEyeLeft, new int[]{0, 1, 2, 3, 4, 5});
            ShaderEffectHelper.effect2dTriangles(program2dTriangles, texIn, eyeShadowTextureid, PointsConverter.convertFromPointsGlCoord(onImage, width, height), PointsConverter.convertFromPointsGlCoord(EditorEnvironment.pointsLeftEye, 512, 512), vPos22, vTex22, PointsConverter.convertTriangle(EditorEnvironment.trianglesLeftEye), eyeLashesTextureid, eyeLineTextureid, false,
                    PointsConverter.convertTovec3(EditorEnvironment.currentColor[EditorEnvironment.EYE_SHADOW]), PointsConverter.convertTovec3(EditorEnvironment.currentColor[EditorEnvironment.EYE_LASH]), PointsConverter.convertTovec3(EditorEnvironment.currentColor[EditorEnvironment.EYE_LINE]),
                    EditorEnvironment.opacity[EditorEnvironment.EYE_SHADOW] / 100f, EditorEnvironment.opacity[EditorEnvironment.EYE_LASH] / 100f, EditorEnvironment.opacity[EditorEnvironment.EYE_LINE] /100f);

            Point[] onImageRight = PointsConverter.completePointsByAffine(PointsConverter.reallocateAndCut(onImageEyeRight, new int[] {3, 2, 1, 0 , 5, 4}), PointsConverter.convertToOpencvPoints(EditorEnvironment.pointsLeftEye), new int[]{0, 1, 2, 3, 4, 5});
            //onImageRight = PointsConverter.replacePoints(onImageRight, onImageEyeRight, new int[]{3, 2, 1, 0 , 5, 4});
            // FIXME flip triangle on right eyes, cause left and right triangles are not the same
            ShaderEffectHelper.effect2dTriangles(program2dTriangles, texIn, eyeShadowTextureid, PointsConverter.convertFromPointsGlCoord(onImageRight, width, height), PointsConverter.convertFromPointsGlCoord(EditorEnvironment.pointsLeftEye, 512, 512), vPos22, vTex22, PointsConverter.convertTriangle(EditorEnvironment.trianglesLeftEye), eyeLashesTextureid, eyeLineTextureid, false,
                    PointsConverter.convertTovec3(EditorEnvironment.currentColor[EditorEnvironment.EYE_SHADOW]), PointsConverter.convertTovec3(EditorEnvironment.currentColor[EditorEnvironment.EYE_LASH]), PointsConverter.convertTovec3(EditorEnvironment.currentColor[EditorEnvironment.EYE_LINE]),
                    EditorEnvironment.opacity[EditorEnvironment.EYE_SHADOW] / 100f, EditorEnvironment.opacity[EditorEnvironment.EYE_LASH] / 100f, EditorEnvironment.opacity[EditorEnvironment.EYE_LINE] /100f);

            Point[] onImageLips = EditorEnvironment.getOnlyPoints(poseResult.foundLandmarks, 48, 20);
            Point[] onImageLipsConv = PointsConverter.completePointsByAffine(onImageLips, PointsConverter.convertToOpencvPoints(EditorEnvironment.pointsWasLips), new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19});
            onImageLipsConv = PointsConverter.replacePoints(onImageLipsConv, onImageLips, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19});
            ShaderEffectHelper.effect2dTriangles(program2dTriangles, texIn, lipsTextureid, PointsConverter.convertFromPointsGlCoord(onImageLipsConv, width, height), PointsConverter.convertFromPointsGlCoord(EditorEnvironment.pointsWasLips, 512, 512), vPos22, vTex22, PointsConverter.convertTriangle(EditorEnvironment.trianglesLips), lipsTextureid, lipsTextureid, true, null, null, null, 0, 0, 0);

            // TODO add right eye
            // FIXME elements erase each other
        }
    }
}
