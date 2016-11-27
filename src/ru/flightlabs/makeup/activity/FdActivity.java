package ru.flightlabs.makeup.activity;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.AssetManager;
import android.content.res.Resources.NotFoundException;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.media.MediaActionSound;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import ru.flightlabs.makeup.adapter.CategoriesPagerAdapter;
import ru.flightlabs.makeup.adapter.ColorsPagerAdapter;
import ru.flightlabs.makeup.CommonI;
import ru.flightlabs.makeup.EditorEnvironment;
import ru.flightlabs.makeup.Filter;
import ru.flightlabs.makeup.adapter.FilterPagerAdapter;
import ru.flightlabs.makeup.utils.Helper;
import ru.flightlabs.makeup.R;
import ru.flightlabs.makeup.ResourcesApp;

public class FdActivity extends Activity implements CvCameraViewListener2, CommonI {

    EditorEnvironment editorEnvironment;
    boolean debug = false;
    Filter filter;
    public static final String DIRECTORY_SELFIE = "MakeUp";
    public static boolean makePhoto;

    ResourcesApp resourcesApp;

    int catgoryNum = 0;

    private static final String TAG = "FdActivity_class";

    private Mat mRgba;
    private Mat mGray;
    private CascadeClassifier mJavaDetector;

    private float mRelativeFaceSize = 0.5f;
    private int mAbsoluteFaceSize = 0;

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

                    File mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
                    Helper.resourceToFile(getResources().openRawResource(R.raw.haarcascade_frontalface_alt2), mCascadeFile);
                    detectorName = "/storage/extSdCard/mdl1.dat";
                    if (!new File(detectorName).exists()) {
                        detectorName = "/sdcard/mdl1.dat";
                    }

                    if (!new File(detectorName).exists()) {
                        Log.i(TAG, "LoadModel doInBackground66");
                        try {
                            File ertModel = new File(cascadeDir, "ert_model.dat");
                            InputStream ims = assetManager.open("mdl1.dat");//mdl_400_0.1_3_20_300.dat");//sp68.dat");
                            int bytes = Helper.resourceToFile(ims, ertModel);
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
                    ResourcesApp.filter = filter;
                    mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
                    //loadNewMakeUp(0);
                    editorEnvironment.init();
                    editorEnvironment.filter = filter;
                    // TODO refactor
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    public FdActivity() {
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

        resourcesApp = new ResourcesApp(this);
        editorEnvironment = new EditorEnvironment(getApplication().getApplicationContext(), resourcesApp);

        ViewPager viewPagerCategories = (ViewPager) findViewById(R.id.categories);
        CategoriesPagerAdapter pagerCategories = new CategoriesPagerAdapter(this, getResources().getStringArray(R.array.categories));
        viewPagerCategories.setAdapter(pagerCategories);


        changeCategory(0);

        findViewById(R.id.rotate_camera).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                swapCamera();
            }
        });
        findViewById(R.id.debug_enable).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                debug = !debug;
            }
        });
        ((SeekBar)findViewById(R.id.opacity)).setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                editorEnvironment.opacity[catgoryNum] = i;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

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
        Log.i(TAG, "onCameraFrame " + new Date());
        // if index of makeup is changed then we load new one
        // not good
        if (editorEnvironment.currentIndexItem[catgoryNum] != editorEnvironment.newIndexItem) {
            editorEnvironment.loadNewMakeUp(catgoryNum, editorEnvironment.newIndexItem);
        }
        editorEnvironment.currentIndexItem[catgoryNum] = editorEnvironment.newIndexItem;
        Mat rgbaTemp = inputFrame.rgba();
        Log.i(TAG, "onCameraFrame " + rgbaTemp.width() + ";" + rgbaTemp.height());

        mRgba = rgbaTemp;
        Imgproc.cvtColor(mRgba, mGray, Imgproc.COLOR_RGB2GRAY);

        // compute face size to detect
        if (mAbsoluteFaceSize == 0) {
            int height = mRgba.cols();
            if (Math.round(height * mRelativeFaceSize) > 0) {
                mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
            }
        }

        final MatOfRect faces = new MatOfRect();
        mJavaDetector.detectMultiScale(mGray, faces, 1.1, 2, 2, // TODO:
                // objdetect.CV_HAAR_SCALE_IMAGE
                new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
        final Rect[] facesArray = faces.toArray();
        if (facesArray.length > 0) {
            Rect face = facesArray[0];
            Point[] pointsOnFrame = filter.findEyes(mGray, face);
            ResourcesApp.pointsOnFrame = pointsOnFrame;

            if (makePhoto && facesArray.length > 0) {
                // TODO save original
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
                final File fileJpg = new File(newFile, "MakeUp_" + counter + " .jpg");

                Helper.saveMatToFile(mRgba, fileJpg);
                // TODO посмотреть альтернативные способы
                MediaScannerConnection.scanFile(this, new String[]{fileJpg.getPath()}, new String[]{"image/jpeg"}, null);
                final Activity d = this;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        borderCam.setVisibility(View.INVISIBLE);
                        cameraButton.setImageResource(R.drawable.ic_camera);
                        Intent intent = new Intent(d, PhotoEditor.class);
                        Bundle b = new Bundle();
                        b.putString("name", fileJpg.getPath()); //Your id
                        intent.putExtras(b); //Put your id to your next Intent
                        ResourcesApp.face = facesArray[0];
                        ResourcesApp.editor = editorEnvironment;
                        startActivity(intent);
                    }
                });
                Log.i(TAG, "saving end " + true);
            }

            if (debug) {
                for (Point p : pointsOnFrame) {
                    Imgproc.circle(mRgba, p, 1, new Scalar(255, 255, 255));
                }
            }

            editorEnvironment.editImage(mRgba, pointsOnFrame);
        }

        return mRgba;
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

    public void changeMask(int newMask) {
        editorEnvironment.newIndexItem = newMask;
    }

    public void changeCategory(int position) {
        int resourceId = R.array.colors_shadow;

        editorEnvironment.newIndexItem = 0;
        catgoryNum = position;
        ViewPager viewPager = (ViewPager) findViewById(R.id.elements);
        TypedArray iconsCategory = null;
        if (position == 0) {
            iconsCategory = resourcesApp.eyelashesSmall;
            resourceId = R.array.colors_eyelashes;
        } else if (position == 1) {
            iconsCategory = resourcesApp.eyeshadowSmall;
            resourceId = R.array.colors_shadow;
        } else if (position == 2) {
            resourceId = R.array.colors_eyelashes;
            iconsCategory = resourcesApp.eyelinesSmall;
        } else {
            iconsCategory = resourcesApp.lipsSmall;
            resourceId = R.array.colors_lips;
        }
        FilterPagerAdapter pager = new FilterPagerAdapter(this, iconsCategory);
        viewPager.setAdapter(pager);

        ViewPager viewPagerColors = (ViewPager) findViewById(R.id.colors);
        ColorsPagerAdapter pagerColors = new ColorsPagerAdapter(this, getResources().getIntArray(resourceId));
        viewPagerColors.setAdapter(pagerColors);
        ((SeekBar)findViewById(R.id.opacity)).setProgress(editorEnvironment.opacity[catgoryNum]);
    }

    public void changeColor(int color, int position) {
        if (position == 0) {
            editorEnvironment.currentColor[catgoryNum] = -1;
            return;
        }
        editorEnvironment.currentColor[catgoryNum] = color & 0xFFFFFF;
    }
}
