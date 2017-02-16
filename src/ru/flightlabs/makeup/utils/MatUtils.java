package ru.flightlabs.makeup.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.io.File;

/**
 * Created by sov on 16.02.2017.
 */

public class MatUtils {
    public static Mat loadPngToMat(File file) {
        Bitmap bmp = BitmapFactory.decodeFile(file.getAbsolutePath());
        Mat mat = new Mat(bmp.getHeight(), bmp.getWidth(), CvType.CV_8UC4);
        Utils.bitmapToMat(bmp, mat, true);
        return mat;
    }

    public static void saveMatToFile(Mat mRgba, File fileJpg) {
        Bitmap bitmap = Bitmap.createBitmap(mRgba.cols(), mRgba.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mRgba, bitmap);
        BitmapUtils.saveBitmap(fileJpg.getPath(), bitmap);
        bitmap.recycle();
    }
}
