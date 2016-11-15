package ru.flightlabs.makeup;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoWriter;

import ru.flightlabs.masks.model.ImgLabModel;
import ru.flightlabs.masks.model.SimpleModel;
import ru.flightlabs.masks.model.primitives.Triangle;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.AssetManager;
import android.content.res.Resources.NotFoundException;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaActionSound;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class FdActivity extends Activity implements CvCameraViewListener2 {

    Filter filter;
    public static final String DIRECTORY_SELFIE = "MakeUp";
    //public static int counter;
    public static boolean makePhoto;
    public static boolean preMakePhoto;

    TypedArray eyesResourcesSmall;
    TypedArray masks_eyes;
    TypedArray masks_eyes_landamarks;
    TypedArray masks_lips;
    TypedArray masks_lips_landmarks;
    // lips and eyes
    ru.flightlabs.masks.model.primitives.Point[] pointsLeftEye;
    Triangle[] trianglesLeftEye;
    ru.flightlabs.masks.model.primitives.Point[] pointsWasLips;
    Triangle[] trianglesLips;
    Mat leftEye;
    Mat lips;

    private static final String TAG = "FdActivity_class";
    private static final Scalar FACE_RECT_COLOR = new Scalar(0, 255, 0, 255);
    private static final Scalar EYE_RECT_COLOR = new Scalar(0, 0, 255, 255);
    public static final int JAVA_DETECTOR = 0;
    public static final int NATIVE_DETECTOR = 1;

    private Mat mRgba;
    private Mat mGray;
    private File mCascadeFile;
    private CascadeClassifier mJavaDetector;
    private boolean loadModel = false;

    private boolean debugMode = false;
    private boolean showEyes = true;
    private String[] mEysOnOff;

    private int mDetectorType = JAVA_DETECTOR;
    private String[] mDetectorName;

    private float mRelativeFaceSize = 0.5f;
    private int mAbsoluteFaceSize = 0;

    private final static int maxSizeEyeWidth = 135;
    private final static int maxSizeLipsWidth = 70;

    private boolean makeNewFace;


    int currentIndexEye = -1;
    int newIndexEye = 0;

    ImageView noPerson;
    ProgressBar progressBar;

    double lastCount = 0.5f;

    int cameraIndex;
    int numberOfCameras;
    boolean cameraFacing;

    private CameraBridgeViewBase mOpenCvCameraView;

    MediaActionSound sound = new MediaActionSound();
    boolean playSound = true;
    View borderCam;
    ImageView cameraButton;

    int availableProcessors = 1;

    String detectorName;

    SeekBar sbWeight;
    int param = 50;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");

                    // Load native library after(!) OpenCV initialization
                    System.loadLibrary("filter");

                    mOpenCvCameraView.enableView();

                    try {
                        // load cascade file from application resources
                        Log.e(TAG, "findEyes onManagerConnected");
                        throw new IOException(); // РЅСѓ РІС‹С…РѕРґ С‚Р°РєРѕР№:)
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
                    }
                    Runtime info = Runtime.getRuntime();
                    availableProcessors = info.availableProcessors();

                    File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                    AssetManager assetManager = getAssets();

                    mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
                    resourceToFile(getResources().openRawResource(R.raw.haarcascade_frontalface_alt2), mCascadeFile);
                    detectorName = "/storage/extSdCard/mdl1.dat";

                    if (!new File(detectorName).exists()) {
                        Log.i(TAG, "LoadModel doInBackground66");
                        try {
                            File ertModel = new File(cascadeDir, "ert_model.dat");
                            InputStream ims = assetManager.open("mdl1.dat");//mdl_400_0.1_3_20_300.dat");//sp68.dat");
                            int bytes = resourceToFile(ims, ertModel);
                            ims.close();
                            detectorName = ertModel.getAbsolutePath();
                            Log.i(TAG, "LoadModel doInBackground66 " + detectorName + " " + ertModel.exists() + " " + ertModel.length() + " " + bytes);
                        } catch (NotFoundException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                            Log.i(TAG, "LoadModel doInBackground667", e);
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                            Log.i(TAG, "LoadModel doInBackground667", e);
                        }
                    }
                    filter = new Filter(mCascadeFile.getAbsolutePath(), 0, detectorName);
                    mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
                    loadNewMakeUp(0);
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

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

    protected boolean drawMask;


    public static int resourceToFile(InputStream is, File toFile) {
        int res = 0;
        try {
            FileOutputStream os;
            os = new FileOutputStream(toFile);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
                res += bytesRead;
            }
            os.flush();
            is.close();
            os.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return res;
    }

    public FdActivity() {
        mDetectorName = new String[2];
        mDetectorName[JAVA_DETECTOR] = "Java";
        mDetectorName[NATIVE_DETECTOR] = "Native (tracking)";

        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate " + getApplicationContext().getResources().getDisplayMetrics().density);
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        final Activity d = this;
        setContentView(R.layout.face_detect_surface_view);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.fd_activity_surface_view);
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);
        mOpenCvCameraView.getLayoutParams().width = outMetrics.widthPixels;
        mOpenCvCameraView.getLayoutParams().height = mOpenCvCameraView.getLayoutParams().width;
        mOpenCvCameraView.requestLayout();
        cameraIndex = 0;
        numberOfCameras = android.hardware.Camera.getNumberOfCameras();
        android.hardware.Camera.CameraInfo cameraInfo = new android.hardware.Camera.CameraInfo();
        for (int i = 0; i < numberOfCameras; i++) {
            android.hardware.Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT) {
                cameraFacing = true;
                cameraIndex = i;
            }
        }

        mOpenCvCameraView.setCameraIndex(cameraIndex);
        mOpenCvCameraView.setCvCameraViewListener(this);

        borderCam = findViewById(R.id.border);
        cameraButton = (ImageView) findViewById(R.id.camera_button);
        cameraButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "saving true");
                if (!makePhoto) {
                    makePhoto = true;
                    // MediaActionSound sound = new MediaActionSound();
                    cameraButton.setImageResource(R.drawable.ic_camera_r);
                    borderCam.setVisibility(View.VISIBLE);
                    if (playSound) {
                        sound.play(MediaActionSound.SHUTTER_CLICK);
                    }
                }
            }
        });
        cameraButton.setSoundEffectsEnabled(false);

        sbWeight = (SeekBar) findViewById(R.id.sbWeight);
        sbWeight.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                param = progress;

            }
        });

        eyesResourcesSmall = getResources().obtainTypedArray(R.array.effects_small_png);
        masks_eyes = getResources().obtainTypedArray(R.array.masks_eyes);
        masks_eyes_landamarks = getResources().obtainTypedArray(R.array.masks_eyes_landamarks);
        masks_lips = getResources().obtainTypedArray(R.array.masks_lips);
        masks_lips_landmarks = getResources().obtainTypedArray(R.array.masks_lips_landmarks);

        ViewPager viewPager = (ViewPager) findViewById(R.id.photo_pager);
        FilterPagerAdapter pager = new FilterPagerAdapter(this, eyesResourcesSmall);
        viewPager.setAdapter(pager);

        findViewById(R.id.rotate_camera).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                swapCamera();
            }
        });

    }

    // загрузка ресниц и губ
    private void loadNewMakeUp(int index) {
        try {
            int indexLips = index;
            if (indexLips >= getResources().getStringArray(R.array.masks_lips_triangles).length) {
                indexLips = getResources().getStringArray(R.array.masks_lips_triangles).length - 1;
            }
            String eyesTriangle = getResources().getStringArray(R.array.masks_eyes_triangles)[index];
            String lipsTriangle = getResources().getStringArray(R.array.masks_lips_triangles)[indexLips];
            File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
            File fModel = new File(cascadeDir, "landmarks_eye.xml");
            resourceToFile(getResources().openRawResource(masks_eyes_landamarks.getResourceId(index, 0)), fModel);
            File fModelLips = new File(cascadeDir, "landmarks_lips.xml");
            resourceToFile(getResources().openRawResource(masks_lips_landmarks.getResourceId(indexLips, 0)), fModelLips);
            AssetManager assetManager = getAssets();
            trianglesLeftEye = loadriangle(assetManager, eyesTriangle);
            trianglesLips = loadriangle(assetManager, lipsTriangle);
            SimpleModel modelFrom = new ImgLabModel(fModel.getPath());
            pointsLeftEye = modelFrom.getPointsWas();
            SimpleModel modelFromLibs = new ImgLabModel(fModelLips.getPath());
            pointsWasLips = modelFromLibs.getPointsWas();
            leftEye = loadPngToMat(masks_eyes.getResourceId(index, 0), false);
            lips = loadPngToMat(masks_lips.getResourceId(indexLips, 0), false);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    // TODO: лучше делать асинхронно
    // загрузка png через java, т.к. Opencv не загружает alpha-канал из файла
    private Mat loadPngToMat(int index, boolean flip) {
        File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
        File newEyeFile = new File(cascadeDir, "temp.png");
        resourceToFile(getResources().openRawResource(index), newEyeFile);
        // load eye to Mat
        // используем загрузку через андроид, т.к. opencv ломает цвета
        Bitmap bmp = BitmapFactory.decodeFile(newEyeFile.getAbsolutePath());
        Mat newEyeTmp2 = new Mat(bmp.getHeight(), bmp.getWidth(), CvType.CV_8UC4);
        Utils.bitmapToMat(bmp, newEyeTmp2, true);
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

    void changeMask(int newMask) {
        newIndexEye = newMask;
    }

    @Override
    public void onPause() {
        Log.i(TAG, "onPause");
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
        Log.i(TAG, "onResume");
        super.onResume();

        OpenCVLoader.initDebug();
        mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
    }

    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        super.onDestroy();
        mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        Log.i(TAG, "onCameraViewStarted");
        mGray = new Mat();
        mRgba = new Mat();
    }

    public void onCameraViewStopped() {
        Log.i(TAG, "onCameraViewStopped");
        mGray.release();
        mRgba.release();
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        Log.i(TAG, "onCameraFrame " + new Date() + " count " + lastCount);
        // if index of makeup is changed then we load new one
        if (currentIndexEye != newIndexEye) {
            loadNewMakeUp(newIndexEye);
        }
        currentIndexEye = newIndexEye;
        Mat rgbaTemp = inputFrame.rgba();
        Log.i(TAG, "onCameraFrame " + rgbaTemp.width() + ";" + rgbaTemp.width());

        if (mRgba != null) {
            mRgba.release();
        }
        mRgba = rgbaTemp.submat(new Rect(0, (rgbaTemp.height() - rgbaTemp.width()) / 2, rgbaTemp.width(), rgbaTemp.width()));
        Imgproc.cvtColor(mRgba, mGray, Imgproc.COLOR_RGB2GRAY);

        // compute face size to detect
        if (mAbsoluteFaceSize == 0) {
            int height = mRgba.cols();
            if (Math.round(height * mRelativeFaceSize) > 0) {
                mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
            }
        }

        MatOfRect faces = new MatOfRect();
        mJavaDetector.detectMultiScale(mGray, faces, 1.1, 2, 2, // TODO:
                // objdetect.CV_HAAR_SCALE_IMAGE
                new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
        Rect[] facesArray = faces.toArray();
        if (facesArray.length > 0) {
            Rect face = facesArray[0];
            Point[] points = filter.findEyes(mGray, face);
            filter.drawMask(leftEye, mRgba, pointsLeftEye, points, trianglesLeftEye, lips, pointsWasLips, trianglesLips);
        }

        if (makePhoto) {
            makePhoto = false;
            Log.i(TAG, "saving start " + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath());
            File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
            File newFile = new File(file, DIRECTORY_SELFIE);
            if (!newFile.exists()) {
                newFile.mkdirs();
            }
            final SharedPreferences prefs = getSharedPreferences(Settings.PREFS, Context.MODE_PRIVATE);
            int counter = prefs.getInt(Settings.COUNTER_PHOTO, 0);
            counter++;
            Editor editor = prefs.edit();
            editor.putInt(Settings.COUNTER_PHOTO, counter);
            editor.commit();
            File fileJpg = new File(newFile, "Filter_" + counter + " .jpg");

            Bitmap bitmap = Bitmap.createBitmap(mRgba.cols(), mRgba.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(mRgba, bitmap);
            saveBitmap(fileJpg.getPath(), bitmap);
            bitmap.recycle();
            // TODO посмотреть альтернативные способы
            MediaScannerConnection.scanFile(this, new String[]{fileJpg.getPath()}, new String[]{"image/jpeg"}, null);
            final Activity d = this;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    borderCam.setVisibility(View.INVISIBLE);
                    cameraButton.setImageResource(R.drawable.ic_camera);
                    startActivity(new Intent(d, Gallery.class));
                }
            });
            Log.i(TAG, "saving end " + true);
        }
        return mRgba;
    }

    private void saveBitmap(String toFile, Bitmap bmp) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(toFile);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, out); // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void swapCamera() {
        cameraIndex++;
        if (cameraIndex >= numberOfCameras) {
            cameraIndex = 0;
        }
        android.hardware.Camera.CameraInfo cameraInfo = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraIndex, cameraInfo);
        cameraFacing = false;
        if (cameraInfo.facing == android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT) {
            cameraFacing = true;
        }
        mOpenCvCameraView.disableView();
        mOpenCvCameraView.setCameraIndex(cameraIndex);
        mOpenCvCameraView.enableView();
    }

}
