package ru.flightlabs.makeup.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.analytics.FirebaseAnalytics;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import ru.flightlabs.masks.CompModel;
import ru.flightlabs.masks.ModelLoaderTask;
import ru.flightlabs.masks.Static;
import ru.oramalabs.beautykit.R;

/**
 * Created by sov on 29.04.2017.
 */

public class SplashScreen extends Activity implements ModelLoaderTask.Callback {

    private static final String TAG = "SplashScreen";
    private FirebaseAnalytics mFirebaseAnalytics;

    static CompModel compModel;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    if (Static.LOG_MODE) Log.i(TAG, "OpenCV loaded successfully");
                    // Load native library after(!) OpenCV initialization
                    System.loadLibrary("detection_based_tracker");

                    Static.libsLoaded = true;
                    // load cascade file from application resources
                    if (Static.LOG_MODE) Log.e(TAG, "findLandMarks onManagerConnected");
                    compModel.loadHaarModel(Static.resourceDetector[0]);
                    compModel.load3lbpModels(R.raw.lbp_frontal_face, R.raw.lbp_left_face, R.raw.lbp_right_face);
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        compModel = new CompModel();
        compModel.context = getApplicationContext();
    }

    @Override
    public void onResume() {
        super.onResume();
        Static.libsLoaded = false;
        OpenCVLoader.initDebug();
        mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        if (compModel.mNativeDetector == null) {
            new ModelLoaderTask(this).execute(compModel);
        }
    }


    @Override
    public void onModelLoaded() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(getApplication(), ActivityMakeUp.class);
                startActivity(i);
                finish();
            }
        });
    }
}
