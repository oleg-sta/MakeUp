package ru.flightlabs.makeup.shader;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

import org.opencv.core.Point;

import java.util.Arrays;

import ru.flightlabs.makeup.StateEditor;
import ru.flightlabs.makeup.activity.ActivityMakeUp;
import ru.flightlabs.makeup.utils.ModelUtils;
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

    private int[] eyeShadowTextureId = new int[5];
    private int[] eyeShadowTextureIdWas = new int[5];
    private int eyeLashesTextureId;
    private int eyeLineTextureId;
    private int lipsTextureId;

    private final StateEditor editEnv;

    public ShaderEffectMakeUp(Context contex, StateEditor editEnv) {
        super(contex);
        this.editEnv = editEnv;
    }

    public void init() {
        super.init();
        loadTexures(eyeShadowTextureId, editEnv.getResourceIds(StateEditor.EYE_SHADOW));
        eyeLashesTextureId = OpenGlHelper.loadTexture(context, editEnv.getResourceId(StateEditor.EYE_LASH));
        eyeLineTextureId = OpenGlHelper.loadTexture(context, editEnv.getResourceId(StateEditor.EYE_LINE));
        lipsTextureId = OpenGlHelper.loadTexture(context, editEnv.getResourceId(StateEditor.LIPS));
    }

    private void loadTexures(int[] openglTexts, int[] resources) {
        eyeShadowTextureIdWas = resources;
        for (int i = 0; i < openglTexts.length; i++) {
            if (i < resources.length && resources[i] > 0) {
                openglTexts[i] = OpenGlHelper.loadTexture(context, resources[i]);
            } else {
                openglTexts[i] = OpenGlHelper.loadTexture(context, resources[0]); // TODO fix
            }
        }
    }

    private void changeTexures(int[] openglTexts, int[] resources) {
        eyeShadowTextureIdWas = resources;
        for (int i = 0; i < resources.length; i++) {
            if (resources[i] > 0) {
                OpenGlHelper.changeTexture(context, resources[i], openglTexts[i]);
            }
        }
    }

    public void makeShaderMask(int indexEye, PoseHelper.PoseResult poseResult, int width, int height, int texIn, long time, int iGlobTime) {
        Log.i(TAG, "onDrawFrame6 draw maekup");
        int vPos2 = GLES20.glGetAttribLocation(program2dJustCopy, "vPosition");
        int vTex2 = GLES20.glGetAttribLocation(program2dJustCopy, "vTexCoord");
        GLES20.glEnableVertexAttribArray(vPos2);
        GLES20.glEnableVertexAttribArray(vTex2);
        ShaderEffectHelper.shaderEffect2dWholeScreen(poseResult.leftEye, poseResult.rightEye, texIn, program2dJustCopy, vPos2, vTex2);

        if (poseResult.foundLandmarks != null) {
            Point[] onImageEyeLeft = ModelUtils.getOnlyPoints(poseResult.foundLandmarks, 36, 6);
            Point[] onImageEyeRight = ModelUtils.getOnlyPoints(poseResult.foundLandmarks, 36 + 6, 6);
            // TODO add checkbox for rgb or hsv bleding
            Log.i(TAG, "onDrawFrame6 draw maekup2");
            int vPos22 = GLES20.glGetAttribLocation(program2dTriangles, "vPosition");
            int vTex22 = GLES20.glGetAttribLocation(program2dTriangles, "vTexCoord");
            GLES20.glEnableVertexAttribArray(vPos22);
            GLES20.glEnableVertexAttribArray(vTex22);
            // TODO use blendshape for eyes

            if (editEnv.changed(StateEditor.EYE_SHADOW)) {
                Log.i(TAG, "changed texture " + Arrays.toString(editEnv.getResourceIds(StateEditor.EYE_SHADOW)));
                changeTexures(eyeShadowTextureId, editEnv.getResourceIds(StateEditor.EYE_SHADOW));
            }
            if (editEnv.changed(StateEditor.EYE_LASH)) {
                OpenGlHelper.changeTexture(context, editEnv.getResourceId(StateEditor.EYE_LASH), eyeLashesTextureId);
            }
            if (editEnv.changed(StateEditor.EYE_LINE)) {
                OpenGlHelper.changeTexture(context, editEnv.getResourceId(StateEditor.EYE_LINE), eyeLineTextureId);
            }
            if (editEnv.changed(StateEditor.LIPS)) {
                OpenGlHelper.changeTexture(context, editEnv.getResourceId(StateEditor.LIPS), lipsTextureId);
            }

            Point[] onImage = PointsConverter.completePointsByAffine(onImageEyeLeft, PointsConverter.convertToOpencvPoints(StateEditor.pointsLeftEyeNew), new int[]{0, 1, 2, 3, 4, 5});
            // TODO use blendshapes
            onImage = PointsConverter.replacePoints(onImage, onImageEyeLeft, new int[]{0, 1, 2, 3, 4, 5});
            int[] shadowColor = editEnv.getColors(StateEditor.EYE_SHADOW);

            int[] hsv = new int[] {ActivityMakeUp.useHsv ? 1 : 2, 0, 0};
            hsv[0] = 3;
            if (ActivityMakeUp.useAlphaCol) {
                hsv[0] = 0;
                hsv[0] = 4;
            }


            ShaderEffectHelper.effect2dTriangles(program2dTriangles, texIn, setSignShadow(eyeShadowTextureId, eyeShadowTextureIdWas), PointsConverter.convertFromPointsGlCoord(onImage, width, height), PointsConverter.convertFromPointsGlCoord(StateEditor.pointsLeftEyeNew, 512, 512), vPos22, vTex22, PointsConverter.convertTriangle(StateEditor.trianglesLeftEye), eyeLashesTextureId, eyeLineTextureId,
                    hsv,
                    PointsConverter.convertTovec3(shadowColor[0]),
                    PointsConverter.convertTovec3(editEnv.getColor(StateEditor.EYE_LASH)),
                    PointsConverter.convertTovec3(editEnv.getColor(StateEditor.EYE_LINE)),
                    editEnv.getOpacityFloat(StateEditor.EYE_SHADOW), editEnv.getOpacityFloat(StateEditor.EYE_LASH), editEnv.getOpacityFloat(StateEditor.EYE_LINE), getColor(shadowColor));

            Point[] onImageRight = PointsConverter.completePointsByAffine(PointsConverter.reallocateAndCut(onImageEyeRight, new int[] {3, 2, 1, 0 , 5, 4}), PointsConverter.convertToOpencvPoints(StateEditor.pointsLeftEyeNew), new int[]{0, 1, 2, 3, 4, 5});
            //onImageRight = PointsConverter.replacePoints(onImageRight, onImageEyeRight, new int[]{3, 2, 1, 0 , 5, 4});
            // FIXME flip triangle on right eyes, cause left and right triangles are not the same
            ShaderEffectHelper.effect2dTriangles(program2dTriangles, texIn, setSignShadow(eyeShadowTextureId, eyeShadowTextureIdWas), PointsConverter.convertFromPointsGlCoord(onImageRight, width, height), PointsConverter.convertFromPointsGlCoord(StateEditor.pointsLeftEyeNew, 512, 512), vPos22, vTex22, PointsConverter.convertTriangle(StateEditor.trianglesLeftEye), eyeLashesTextureId, eyeLineTextureId,
                    hsv,
                    PointsConverter.convertTovec3(shadowColor[0]),
                    PointsConverter.convertTovec3(editEnv.getColor(StateEditor.EYE_LASH)),
                    PointsConverter.convertTovec3(editEnv.getColor(StateEditor.EYE_LINE)),
                    editEnv.getOpacityFloat(StateEditor.EYE_SHADOW), editEnv.getOpacityFloat(StateEditor.EYE_LASH), editEnv.getOpacityFloat(StateEditor.EYE_LINE), getColor(shadowColor));

            Point[] onImageLips = ModelUtils.getOnlyPoints(poseResult.foundLandmarks, 48, 20);
            Point[] onImageLipsConv = PointsConverter.completePointsByAffine(onImageLips, PointsConverter.convertToOpencvPoints(StateEditor.pointsWasLipsNew), new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19});
            onImageLipsConv = PointsConverter.replacePoints(onImageLipsConv, onImageLips, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19});
            onImageLipsConv[24] = avg(onImageLipsConv, 13, 19);
            onImageLipsConv[25] = avg(onImageLipsConv, 14, 18);
            onImageLipsConv[26] = avg(onImageLipsConv, 15, 17);

            float alphaLips = 1;
            // workaround
            if (editEnv.getColorIndex() == 0) {
                alphaLips = 0;
            }
            ShaderEffectHelper.effect2dTriangles(program2dTriangles, texIn, new int[]{lipsTextureId, -1, -1, -1, -1}, PointsConverter.convertFromPointsGlCoord(onImageLipsConv, width, height), PointsConverter.convertFromPointsGlCoord(StateEditor.pointsWasLipsNew, 512, 512), vPos22, vTex22, PointsConverter.convertTriangle(StateEditor.trianglesLips), lipsTextureId, lipsTextureId,
                    new int[] {ActivityMakeUp.useHsv ? 1 : 2, -1, -1},
                    PointsConverter.convertTovec3(editEnv.getColor(StateEditor.LIPS)), null, null,
                    editEnv.getOpacityFloat(StateEditor.LIPS) * alphaLips, 0, 0, new float[0]);

            // FIXME elements erase each other
        }
    }

    private static Point avg(Point[] onImageLipsConv, int i, int i1) {
        Point p1 = onImageLipsConv[i];
        Point p2 = onImageLipsConv[i1];
        Point avg = new Point((p1.x + p2.x) / 2, (p1.y + p2.y) / 2);
        double dist = len(p1, p2);
        if (dist < 10) {
            onImageLipsConv[i] = avg;
            onImageLipsConv[i1] = avg;
        }
        return avg;
    }

    private static double len(Point p1, Point p2) {
        return Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
    }

    private int[] setSignShadow(int[] eyeShadowTextureId, int[] eyeShadowTextureIdWas) {
        int[] res = new int[eyeShadowTextureId.length];
        for (int i = 0; i < eyeShadowTextureId.length; i++ ) {
            if (i >= eyeShadowTextureIdWas.length || eyeShadowTextureIdWas[i] < 0) {
                res[i] = -1;
            } else {
                res[i] = eyeShadowTextureId[i];
            }
        }
        return res;
    }

    private static float[] getColor(int color[]) {
        final int num = 2 + 2; // 4 слоя тени(1ый слой был уже учтен)
        float[] res = new float[num * 3]; // RGB на каждый слой
        for (int i = 0; i < num; i++) {
            float[] r;
            if (i + 1 < color.length) {
                r = PointsConverter.convertTovec3(color[i]);
            } else {
                r = PointsConverter.convertTovec3(color[0]);
            }
            res[i * 3] = r[0];
            res[i * 3 + 1] = r[1];
            res[i * 3 + 2] = r[2];
        }
        return res;
    }
}