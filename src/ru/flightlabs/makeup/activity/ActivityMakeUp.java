package ru.flightlabs.makeup.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import ru.flightlabs.commonlib.Settings;
import ru.flightlabs.makeup.CommonI;
import ru.flightlabs.makeup.EditorEnvironment;
import ru.flightlabs.makeup.R;
import ru.flightlabs.makeup.ResourcesApp;
import ru.flightlabs.makeup.adapter.CategoriesNamePagerAdapter;
import ru.flightlabs.makeup.adapter.CategoriesNewAdapter;
import ru.flightlabs.makeup.adapter.ColorsPagerAdapter;
import ru.flightlabs.makeup.shader.ShaderEffectMakeUp;
import ru.flightlabs.masks.CompModel;
import ru.flightlabs.masks.ModelLoaderTask;
import ru.flightlabs.masks.Static;
import ru.flightlabs.masks.camera.FastCameraView;
import ru.flightlabs.masks.renderer.MaskRenderer;
import us.feras.ecogallery.EcoGallery;
import us.feras.ecogallery.EcoGalleryAdapterView;

/**
 * Created by sov on 08.02.2017.
 */

public class ActivityMakeUp extends Activity implements CommonI {

    public static EditorEnvironment editorEnvironment;
    ResourcesApp resourcesApp;
    CompModel compModel;
    ProgressBar progressBar;
    public static GLSurfaceView gLSurfaceView;
    FastCameraView camereView;
    MaskRenderer meRender;
    ImageView rotateCamera;
    ImageView backButton;
    ImageView buttonCamera;

    private static final String TAG = "ActivityFast";

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    // Load native library after(!) OpenCV initialization
                    System.loadLibrary("detection_based_tracker");

                    Static.libsLoaded = true;
                    // load cascade file from application resources
                    Log.e(TAG, "findLandMarks onManagerConnected");
                    compModel.loadHaarModel(Static.resourceDetector[0]);
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
        setContentView(R.layout.main_makeup);

        camereView = (FastCameraView) findViewById(R.id.fd_fase_surface_view);

        compModel = new CompModel();
        compModel.context = getApplicationContext();

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);

        resourcesApp = new ResourcesApp(this);

        ((CheckBox)findViewById(R.id.checkDebug)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Settings.debugMode = b;
            }
        });
        ((CheckBox)findViewById(R.id.checkBoxLinear)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Settings.useLinear = b;
            }
        });
        ((CheckBox)findViewById(R.id.useCalman)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Settings.useKalman = b;
            }
        });
        rotateCamera = (ImageView)findViewById(R.id.rotate_camera);
        rotateCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                camereView.swapCamera();
            }
        });
        ViewPager viewPagerCategories = (ViewPager) findViewById(R.id.categories);
        CategoriesNamePagerAdapter pagerCategories = new CategoriesNamePagerAdapter(this, getResources().getStringArray(R.array.categories));
        viewPagerCategories.setAdapter(pagerCategories);

        editorEnvironment = new EditorEnvironment(getApplication().getApplicationContext(), resourcesApp);
        changeCategory(EditorEnvironment.FASHION);
        ((SeekBar)findViewById(R.id.opacity)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                Log.i(TAG, "opacity " + editorEnvironment.catgoryNum + " " + i);
                editorEnvironment.opacity[editorEnvironment.catgoryNum] = i;
                gLSurfaceView.requestRender();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        gLSurfaceView = (GLSurfaceView)findViewById(R.id.fd_glsurface);
        gLSurfaceView.getHolder().setFormat(PixelFormat.TRANSPARENT);
        gLSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        //gLSurfaceView.setZOrderOnTop(true);
        meRender = new MaskRenderer(this, compModel, new ShaderEffectMakeUp(this));
        gLSurfaceView.setEGLContextClientVersion(2);
        gLSurfaceView.setRenderer(meRender);
        gLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);//RENDERMODE_WHEN_DIRTY);
        //gLSurfaceView.setZOrderOnTop(false);

        backButton = (ImageView)findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        buttonCamera = (ImageView)findViewById(R.id.camera_button);
        findViewById(R.id.camera_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!meRender.staticView) {
                    meRender.staticView = true;
                    gLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
                    camereView.disableView();
                    buttonCamera.setImageResource(R.drawable.ic_save);
                    backButton.setVisibility(View.VISIBLE);
                    rotateCamera.setVisibility(View.GONE);
                } else {
                    Static.makePhoto = true;
                    gLSurfaceView.requestRender();
                }
            }
        });
        findViewById(R.id.settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplication(), ActivitySettings.class));
            }
        });
    }

    @Override
    public void onResume() {
        Log.i(TAG, "onResume");
        super.onResume();
        final SharedPreferences prefs = getSharedPreferences(Settings.PREFS, Context.MODE_PRIVATE);
        //SettingsActivity.debugMode = prefs.getBoolean(SettingsActivity.DEBUG_MODE, true);
        Static.libsLoaded = false;
        OpenCVLoader.initDebug();
        mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        new ModelLoaderTask(progressBar).execute(compModel);
        gLSurfaceView.onResume();
        editorEnvironment.init();
        Settings.makeUp = true;
        Settings.clazz = ActivityPhoto.class;

        // FIXME wrong way
        if (meRender.staticView) {
            meRender.staticView = false;
            gLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
            buttonCamera.setImageResource(R.drawable.ic_photo);
            backButton.setVisibility(View.GONE);
            rotateCamera.setVisibility(View.VISIBLE);
            camereView.enableView(); // FIXME not good
        }
    }


    @Override
    protected void onPause() {
        Log.i(TAG, "onPause");
        super.onPause();
        gLSurfaceView.onPause();
        //TODO has something todo with FastCameraView (rlease, close etc.)
        camereView.disableView();
    }

    public void changeCategory(int position) {
        int resourceId = R.array.colors_shadow;

        editorEnvironment.newIndexItem = 0;
        editorEnvironment.catgoryNum = position;
        EcoGallery viewPager = (EcoGallery) findViewById(R.id.elements);
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
        } else if (position == 3) {
            iconsCategory = resourcesApp.lipsSmall;
            resourceId = R.array.colors_lips;
        } else  {
            iconsCategory = resourcesApp.fashionSmall;
            resourceId = R.array.colors_lips;
        }
        CategoriesNewAdapter pager = new CategoriesNewAdapter(this, iconsCategory);
        viewPager.setAdapter(pager);
        viewPager.setOnItemClickListener(new EcoGalleryAdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(EcoGalleryAdapterView<?> parent, View view, int position, long id) {
                changeItemInCategory(position);
            }
        });
        viewPager.setOnItemSelectedListener(new EcoGalleryAdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(EcoGalleryAdapterView<?> parent, View view, int position, long id) {
                changeItemInCategory(position);
            }

            @Override
            public void onNothingSelected(EcoGalleryAdapterView<?> parent) {

            }
        });

        ViewPager viewPagerColors = (ViewPager) findViewById(R.id.colors);
        ColorsPagerAdapter pagerColors = new ColorsPagerAdapter(this, getResources().getIntArray(resourceId));
        viewPagerColors.setAdapter(pagerColors);
        ((SeekBar)findViewById(R.id.opacity)).setProgress(editorEnvironment.opacity[editorEnvironment.catgoryNum]);
    }

    @Override
    public void onBackPressed() {
        if (meRender.staticView) {
            meRender.staticView = false;
            gLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
            buttonCamera.setImageResource(R.drawable.ic_photo);
            backButton.setVisibility(View.GONE);
            rotateCamera.setVisibility(View.VISIBLE);
            camereView.enableView();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void changeItemInCategory(int newItem) {
        editorEnvironment.newIndexItem = newItem;
        gLSurfaceView.requestRender();

    }

    public void changeColor(int color, int position) {
        if (position == 0) {
            editorEnvironment.currentColor[editorEnvironment.catgoryNum] = -1;
            return;
        }
        editorEnvironment.currentColor[editorEnvironment.catgoryNum] = color & 0xFFFFFF;
        gLSurfaceView.requestRender();
    }
}
