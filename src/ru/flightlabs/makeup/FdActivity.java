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
    public static final String DIRECTORY_SELFIE = "Filter";
    //public static int counter;
    public static boolean makePhoto;
    public static boolean preMakePhoto;
    // Size constants of eye
    int kEyePercentTop = 25;
    int kEyePercentSide = 13;
    int kEyePercentHeight = 30;
    int kEyePercentWidth = 35;
    
    ru.flightlabs.masks.model.primitives.Point[] pointsWas;
    Triangle[] trianlges;

    // Algorithm Parameters
    int kFastEyeWidth = 50;
    int kWeightBlurSize = 5;
    boolean kEnableWeight = true;
    float kWeightDivisor = 1.0f;
    double kGradientThreshold = 50.0;

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
    
    TypedArray eyesResourcesSmall;
    
    int currentIndexEye = -1;
    int newIndexEye = 0;
    
    ImageView noPerson;
    ProgressBar progressBar;
    
    int frameCount;
    long timeStart;
    double lastCount = 0.5f;
   
    int cameraIndex;
    int numberOfCameras;
    boolean cameraFacing;
    Mat leftEye;
    Mat lips;

    private CameraBridgeViewBase mOpenCvCameraView;
    
    MediaActionSound sound = new MediaActionSound();
    boolean playSound = true;
    View borderCam;
    ImageView cameraButton;
    
    int availableProcessors = 1;
    
    String detectorName;
    
    VideoWriter videoWriter;
    VideoWriter videoWriterOrig;
    boolean videoWriterStart;
    
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
                
//                Display display = getWindowManager().getDefaultDisplay(); 
//                int width = display.getWidth();
//                int height = display.getHeight();
//                mOpenCvCameraView.setMaxFrameSize(width, width);

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
                File fModel = new File(cascadeDir, "testing_with_face_landmarks.xml");
                AssetManager assetManager = getAssets();
                
                mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
                resourceToFile(getResources().openRawResource(R.raw.haarcascade_frontalface_alt2), mCascadeFile);
                detectorName = "/storage/extSdCard/mdl1.dat";


                if (!new File(detectorName).exists()) {
                    Log.i(TAG, "LoadModel doInBackground66");
                    try {
                        File ertModel = new File(cascadeDir, "ert_model.dat"); 
                        InputStream ims = assetManager.open("sp_mouth.dat");//mdl_400_0.1_3_20_300.dat");//sp68.dat");
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
                leftEye = loadNewEye(R.raw.eyelash_8, false);
                lips = loadNewEye(R.raw.lips1, true);
                
                try {
                    List<Triangle> triangleArr = new ArrayList<Triangle>();
                    InputStream ims;
                    ims = assetManager.open("eyelash_8_triangles.txt");
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
                    trianlges = triangleArr.toArray(new Triangle[0]);
                    
                    try {
                        resourceToFile(getResources().openRawResource(R.raw.eyelash_8_landmarks), fModel);
                    } catch (NotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } 
                    Log.i(TAG, "LoadModel doInBackground1");
                    SimpleModel modelFrom = new ImgLabModel(fModel.getPath());
                    Log.i(TAG, "LoadModel doInBackground2");
                    pointsWas = modelFrom.getPointsWas();
                    Log.i(TAG, "LoadModel doInBackground2 "  + pointsWas.length);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
                break;
            default: {
                super.onManagerConnected(status);
            }
                break;
            }
        }
    };
    
    
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

    /** Called when the activity is first created. */
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
        cameraButton = (ImageView)findViewById(R.id.camera_button);
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
    
    // TODO: лучше делать асинхронно
    private Mat loadNewEye(int index, boolean flip) {
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
        //Log.i(TAG, "loadNewEye " + currentEye.type() + " " + currentEye.channels());
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
        currentIndexEye = newIndexEye;
        Mat rgbaTemp = inputFrame.rgba();
        Log.i(TAG, "onCameraFrame " + rgbaTemp.width() + ";" + rgbaTemp.width());
        
        //Mat rgba = new Mat(rgbaTemp, new Rect(0, (rgbaTemp.height() - rgbaTemp.width()) / 2, rgbaTemp.width(), rgbaTemp.width()));
        if (mRgba != null) {
            mRgba.release();
        }
        mRgba = rgbaTemp.submat(new Rect(0, (rgbaTemp.height() - rgbaTemp.width()) / 2, rgbaTemp.width(), rgbaTemp.width()));
        Imgproc.cvtColor(mRgba, mGray, Imgproc.COLOR_RGB2GRAY);
        
        if (mAbsoluteFaceSize == 0) {
            int height = mRgba.cols();
            if (Math.round(height * mRelativeFaceSize) > 0) {
                mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
            }
            //mNativeDetector.setMinFaceSize(mAbsoluteFaceSize);
        }
        
        MatOfRect faces = new MatOfRect();
        mJavaDetector.detectMultiScale(mGray, faces, 1.1, 2, 2, // TODO:
                // objdetect.CV_HAAR_SCALE_IMAGE
new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
        Rect[] facesArray = faces.toArray();
        if (facesArray.length > 0) {
            Rect face = facesArray[0];
            Point[] points = filter.findEyes(mGray, face);
            //Point leftEyeLeft = points[0];
            //Point leftEyeRight = points[3];
            //drawEye(mRgba, leftEyeRight, leftEyeLeft, leftEye, maxSizeEyeWidth);
            
            Log.i(TAG, "onCameraFrame before drawMask");
            filter.drawMask(leftEye, mRgba, pointsWas, points, trianlges);
            
            Point rightEyeLeft = points[6];
            Point rightEyeRight = points[9];
            //drawEye(mRgba, rightEyeRight, rightEyeLeft, leftEye, maxSizeEyeWidth);
            
            Point lipsLeft = points[12];
            Point lipsRight = points[18];
            drawEye(mRgba, lipsRight, lipsLeft, lips, maxSizeLipsWidth);
            for (Point point : points) {
                Imgproc.circle(mRgba, point, 2, new Scalar(0, 255, 0, 255));
            }
        }
        
        if (makePhoto) {
            makePhoto = false;
            Log.i(TAG, "saving start " + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath());
            File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
            File newFile=new File(file, DIRECTORY_SELFIE);
            if (!newFile.exists()) {
                newFile.mkdirs();
            }
            final SharedPreferences prefs = getSharedPreferences(Settings.PREFS, Context.MODE_PRIVATE);
            int counter = prefs.getInt(Settings.COUNTER_PHOTO, 0);
            counter++;
            Editor editor = prefs.edit();
            editor.putInt(Settings.COUNTER_PHOTO, counter);
            editor.commit();
            File fileJpg = new File(newFile, "Filter_" +counter + " .jpg");
            
            Bitmap bitmap = Bitmap.createBitmap(mRgba.cols(), mRgba.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(mRgba, bitmap);
            saveBitmap(fileJpg.getPath(),bitmap);
            bitmap.recycle();
            // TODO посмотреть альтернативные способы
            MediaScannerConnection.scanFile(this, new String[] { fileJpg.getPath() }, new String[] { "image/jpeg" }, null);
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
    
    private void drawEye(Mat mRgba, Point rEye, Point lEye, Mat currentEye, int size) {
        Point centerEye = new Point((rEye.x + lEye.x) / 2, (rEye.y + lEye.y) / 2);
        double distanceEye = Math.sqrt(Math.pow(rEye.x - lEye.x, 2) +  Math.pow(rEye.y - lEye.y, 2));
        double scale = distanceEye / size;
        double angle = -angleOfYx(lEye, rEye);
        
        Point centerEyePic = new Point(currentEye.cols() / 2, currentEye.rows() / 2);
        Rect bbox = new RotatedRect(centerEyePic, new Size(currentEye.size().width * scale, currentEye.size().height * scale), angle).boundingRect();
        
        Mat affineMat = Imgproc.getRotationMatrix2D(centerEyePic, angle, scale);
        double[] x1 = affineMat.get(0, 2);
        double[] y1 = affineMat.get(1, 2);
        x1[0] = x1[0] + bbox.width / 2.0 - centerEyePic.x;
        y1[0] = y1[0] + bbox.height / 2.0 - centerEyePic.y;
        Point leftPoint = new Point(centerEye.x - bbox.width / 2.0, centerEye.y - bbox.height / 2.0);
        if (leftPoint.y < 0) {
            bbox.height = (int)(bbox.height + leftPoint.y);
            y1[0] = y1[0] + leftPoint.y;
            leftPoint.y = 0;
        }
        if (leftPoint.x < 0) {
            bbox.width = (int)(bbox.width + leftPoint.x);
            x1[0] = x1[0] + leftPoint.x;
            leftPoint.x = 0;
        }
        if ((leftPoint.y + bbox.height) > mRgba.height()) {
            int delta = (int)(leftPoint.y + bbox.height - mRgba.height());
            bbox.height = bbox.height - delta;
        }
        if ((leftPoint.x + bbox.width) > mRgba.width()) {
            int delta = (int)(leftPoint.x + bbox.width - mRgba.width());
            bbox.width = bbox.width - delta;
        }
        affineMat.put(0, 2, x1);
        affineMat.put(1, 2, y1);
        
        Size newSize = new Size(bbox.size().width, bbox.size().height);
        Mat sizedRotatedEye = new Mat(newSize, currentEye.type());
        Imgproc.warpAffine(currentEye, sizedRotatedEye, affineMat, newSize);
        
        int newEyeHeight = sizedRotatedEye.height();
        int newEyeWidth = sizedRotatedEye.width();
        Rect r = new Rect((int) (leftPoint.x), (int) (leftPoint.y),
                newEyeWidth, newEyeHeight);
        Mat rgbaInnerWindow = mRgba.submat(r.y, r.y + r.height, r.x, r.x + r.width);
        
        List<Mat> layers = new ArrayList<Mat>();
        Core.split(sizedRotatedEye, layers);
        sizedRotatedEye.copyTo(rgbaInnerWindow, layers.get(3)); // копируем повернутый глаз по альфа-каналу(4-й слой)
        rgbaInnerWindow.release();
        sizedRotatedEye.release();
        
        if (debugMode) {
            Imgproc.rectangle(mRgba, new Point(r.x, r.y), new Point(r.x + r.width, r.y + r.height), FACE_RECT_COLOR, 3);
            Imgproc.circle(mRgba, lEye, 9, FACE_RECT_COLOR, 4);
            Imgproc.circle(mRgba, rEye, 9, FACE_RECT_COLOR, 4);
        }
    }
    
    public static double angleOfYx(Point p1, Point p2) {
        // NOTE: Remember that most math has the Y axis as positive above the X.
        // However, for screens we have Y as positive below. For this reason, 
        // the Y values are inverted to get the expected results.
        final double deltaX = (p1.y - p2.y);
        final double deltaY = (p2.x - p1.x);
        final double result = Math.toDegrees(Math.atan2(deltaY, deltaX)); 
        return (result < 0) ? (360d + result) : result;
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
    
    private void setMinFaceSize(float faceSize) {
        mRelativeFaceSize = faceSize;
        mAbsoluteFaceSize = 0;
    }
}
