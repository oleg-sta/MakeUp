package ru.flightlabs.makeup.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;

import ru.flightlabs.commonlib.Settings;
import ru.flightlabs.makeup.ResourcesApp;
import ru.flightlabs.makeup.StateEditor;
import ru.flightlabs.makeup.adapter.AdaptersNotifier;
import ru.flightlabs.makeup.adapter.CategoriesNamePagerAdapter;
import ru.flightlabs.makeup.adapter.CategoriesNewAdapter;
import ru.flightlabs.makeup.adapter.ColorsNewPagerAdapter;
import ru.flightlabs.makeup.adapter.TextNewPagerAdapter;
import ru.flightlabs.makeup.shader.ShaderEffectMakeUp;
import ru.flightlabs.masks.Static;
import ru.flightlabs.masks.camera.FastCameraView;
import ru.flightlabs.masks.renderer.MaskRenderer;
import ru.oramalabs.beautykit.R;
import us.feras.ecogallery.EcoGallery;
import us.feras.ecogallery.EcoGalleryAdapterView;

/**
 * We should separate view from business logic
 */
public class ActivityMakeUp extends Activity implements AdaptersNotifier, CategoriesNamePagerAdapter.Notification {

    private int currentCategory;

    public static boolean useHsv = false; // false - use colorized
    public static boolean useAlphaCol= true;

    private StateEditor editorEnvironment;
    ResourcesApp resourcesApp;
    ProgressBar progressBar;
    GLSurfaceView gLSurfaceView;
    FastCameraView cameraView;
    MaskRenderer maskRender;
    ImageView rotateCamera;
    ImageView backButton;
    ImageView buttonCamera;
    private PowerManager.WakeLock wakeLock;

    private static final String TAG = "ActivityFast";

    View borderFashion;
    View borderElement;
    View elements;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_makeup);

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "PreviewWorking");

        cameraView = (FastCameraView) findViewById(R.id.fd_fase_surface_view);

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);

        resourcesApp = new ResourcesApp(this);

        initDebug();
        rotateCamera = (ImageView)findViewById(R.id.rotate_camera);
        rotateCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cameraView.swapCamera();
            }
        });
        final EcoGallery viewPagerCategories = (EcoGallery) findViewById(R.id.categories);
        final TextNewPagerAdapter pagerCategories = new TextNewPagerAdapter(this, getResources().getStringArray(R.array.categories));
        pagerCategories.selected = StateEditor.FASHION;
        viewPagerCategories.setAdapter(pagerCategories);
        viewPagerCategories.setSelection(StateEditor.FASHION);
        viewPagerCategories.setOnItemSelectedListener(new EcoGalleryAdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(EcoGalleryAdapterView<?> parent, View view, final int position, long id) {
                //mContext.getResources().getColor(R.color.main_text)
                //pagerCategories.current.setTextColor(Color.BLACK);
                pagerCategories.selected = position;
                selectedCategory(position);
                //((TextView)view.findViewById(R.id.item_text)).setTextColor(Color.RED);
                // set color
            }

            @Override
            public void onNothingSelected(EcoGalleryAdapterView<?> parent) {

            }
        });

        editorEnvironment = new StateEditor(getApplication().getApplicationContext(), resourcesApp);
        editorEnvironment.init();
        borderFashion = findViewById(R.id.border_fashion);
        borderElement = findViewById(R.id.border_element);
        elements = findViewById(R.id.elements);
        ((SeekBar)findViewById(R.id.opacity)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (Static.LOG_MODE) Log.i(TAG, "opacity " + i);
                editorEnvironment.setOpacity(currentCategory, i);
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
        maskRender = new MaskRenderer(this, SplashScreen.compModel, new ShaderEffectMakeUp(this, editorEnvironment));
        gLSurfaceView.setEGLContextClientVersion(2);
        gLSurfaceView.setRenderer(maskRender);
        gLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        maskRender.frameCamera = cameraView.frameCamera;

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
                if (!maskRender.staticView) {
                    changeToOnlyEditMode();
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

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float dpHeight = displayMetrics.heightPixels / displayMetrics.density;
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        Log.i(TAG, "screen size in dp " + dpWidth + " " + dpHeight);
        // init
        changeCategory(StateEditor.FASHION);
        changeItemInCategory(3);
    }

    @Deprecated
    private void initDebug() {
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
        ((CheckBox)findViewById(R.id.useCoorized)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                useHsv = b;
            }
        });
        ((CheckBox)findViewById(R.id.useAlphaColor)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                useAlphaCol = b;
            }
        });
        ((CheckBox)findViewById(R.id.useAlphaColor)).setChecked(true);
    }

    @Override
    public void onResume() {
        if (Static.LOG_MODE) Log.i(TAG, "onResume");
        super.onResume();
        wakeLock.acquire();
        gLSurfaceView.onResume();
        Settings.clazz = ActivityPhoto.class;
        // FIXME wrong way
        if (maskRender.staticView) {
            startCameraView();
        }
    }

    private void changeToOnlyEditMode() {
        maskRender.staticView = true;
        gLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        cameraView.disableView();
        buttonCamera.setImageResource(R.drawable.ic_save);
        backButton.setVisibility(View.VISIBLE);
        rotateCamera.setVisibility(View.INVISIBLE);
        gLSurfaceView.requestRender(); // FIXME tis is workaournd
    }

    private void startCameraView() {
        maskRender.staticView = false;
        gLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        buttonCamera.setImageResource(R.drawable.ic_photo);
        backButton.setVisibility(View.GONE);
        rotateCamera.setVisibility(View.VISIBLE);
        cameraView.enableView(); // FIXME not good
    }


    @Override
    protected void onPause() {
        if (Static.LOG_MODE) Log.i(TAG, "onPause");
        super.onPause();
        wakeLock.release();
        gLSurfaceView.onPause();
        //TODO has something todo with FastCameraView (rlease, close etc.)
        cameraView.disableView();
    }

    public void changeCategory(final int position) {
        // FIXME current position not equal current category
        currentCategory = position;
        int resourceId = R.array.colors_shadow;

        boolean fashion = false;
        EcoGallery viewPager = (EcoGallery) findViewById(R.id.elements);
        TypedArray iconsCategory = null;
        if (position == StateEditor.EYE_LASH) {
            iconsCategory = resourcesApp.eyelashesIcons;
            resourceId = R.array.colors_eyelashes;
        } else if (position == StateEditor.EYE_SHADOW) {
            iconsCategory = resourcesApp.eyeshadowIcons;
            resourceId = R.array.colors_shadow;
        } else if (position == StateEditor.EYE_LINE) {
            iconsCategory = resourcesApp.eyelinesIcons;
            resourceId = R.array.colors_eyelashes;
        } else if (position == StateEditor.LIPS) {
            iconsCategory = resourcesApp.lipsSmall;
            resourceId = R.array.colors_lips;
        } else  {
            iconsCategory = resourcesApp.fashionIcons;
            resourceId = R.array.colors_none;
            fashion = true;
        }
        if (fashion) {
            borderFashion.setVisibility(View.VISIBLE);
            borderElement.setVisibility(View.INVISIBLE);
        } else {
            borderFashion.setVisibility(View.INVISIBLE);
            borderElement.setVisibility(View.VISIBLE);
        }
        String[] names = null;
        if (position == StateEditor.FASHION) {
            names = editorEnvironment.getFashionNames();
        }
        final CategoriesNewAdapter pager = new CategoriesNewAdapter(this, iconsCategory, names);
        pager.selected = editorEnvironment.getCurrentIndex(currentCategory);
        viewPager.setAdapter(pager);
        viewPager.setOnItemClickListener(new EcoGalleryAdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(EcoGalleryAdapterView<?> parent, View view, int position, long id) {
                pager.selected = position;
                changeItemInCategory(position);
                //pager.notifyDataSetChanged();
            }
        });
        viewPager.setSelection(editorEnvironment.getCurrentIndex(currentCategory));
        viewPager.setOnItemSelectedListener(new EcoGalleryAdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(EcoGalleryAdapterView<?> parent, View view, int position, long id) {
                pager.selected = position;
                changeItemInCategory(position);
                //pager.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(EcoGalleryAdapterView<?> parent) {

            }
        });

        //ViewPager viewPagerColors = (ViewPager) findViewById(R.id.colors);
        if (position == StateEditor.LIPS) {
            //viewPagerColors.setVisibility(View.VISIBLE);
            ColorsNewPagerAdapter pagerColorsNew = new ColorsNewPagerAdapter(this, editorEnvironment.getAllColors(position));
            viewPager.setAdapter(pagerColorsNew);
            pagerColorsNew.selected = editorEnvironment.getColorIndex();
            viewPager.setSelection(editorEnvironment.getColorIndex());
            viewPager.setOnItemSelectedListener(new EcoGalleryAdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(EcoGalleryAdapterView<?> parent, View view, int position2, long id) {
                    //pager.selected = position2;
                    changeColor(editorEnvironment.getAllColors(position)[position2], position2);
                    //pager.notifyDataSetChanged();
                }

                @Override
                public void onNothingSelected(EcoGalleryAdapterView<?> parent) {

                }
            });
            viewPager.setOnItemClickListener(new EcoGalleryAdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(EcoGalleryAdapterView<?> parent, View view, int position2, long id) {
                    changeColor(editorEnvironment.getAllColors(position)[position2], position2);
                    //pager.notifyDataSetChanged();
                }
            });
            //viewPager.setVisibility(View.INVISIBLE);
            //borderFashion.setVisibility(View.INVISIBLE);
            //borderElement.setVisibility(View.INVISIBLE);
        } else {
            //viewPagerColors.setVisibility(View.INVISIBLE);
            viewPager.setVisibility(View.VISIBLE);
        }
        //ColorsPagerAdapter pagerColors = new ColorsPagerAdapter(this, editorEnvironment.getAllColors(position));//getResources().getIntArray(resourceId));
        //viewPagerColors.setAdapter(pagerColors);
        ((SeekBar)findViewById(R.id.opacity)).setProgress(editorEnvironment.getOpacity(currentCategory));
    }

    @Override
    public void onBackPressed() {
        if (maskRender.staticView) {
            startCameraView();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void changeItemInCategory(int newItem) {
        if (Static.LOG_MODE) Log.i(TAG, "changeItemInCategory " + newItem);
        editorEnvironment.setCurrentIndexItem(currentCategory, newItem);
        if (currentCategory == StateEditor.FASHION) {
            if (Static.LOG_MODE) Log.i(TAG, "changeItemInCategory ");
            String[] fashions = getResources().getStringArray(R.array.fashion_ic1);
            if (Static.LOG_MODE) Log.i(TAG, "changeItemInCategory " + fashions[newItem]);
            editorEnvironment.setParametersFromFashion(newItem);
        }
        gLSurfaceView.requestRender();
    }

    public void changeColor(int color, int position) {
        if (Static.LOG_MODE) Log.i(TAG, "changeColor " + position);
        editorEnvironment.setCurrentColor(currentCategory, position);
        if (maskRender.staticView) {
            gLSurfaceView.requestRender();
        }
    }

    @Override
    public void selectedCategory(int position) {
        changeCategory(position);
    }
}
