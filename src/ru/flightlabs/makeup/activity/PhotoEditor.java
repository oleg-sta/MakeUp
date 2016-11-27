package ru.flightlabs.makeup.activity;

import android.app.Activity;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;

import org.opencv.core.Mat;

import java.io.File;

import ru.flightlabs.makeup.adapter.CategoriesPagerAdapter;
import ru.flightlabs.makeup.adapter.ColorsPagerAdapter;
import ru.flightlabs.makeup.CommonI;
import ru.flightlabs.makeup.EditorEnvironment;
import ru.flightlabs.makeup.adapter.FilterPagerAdapter;
import ru.flightlabs.makeup.utils.Helper;
import ru.flightlabs.makeup.R;
import ru.flightlabs.makeup.ResourcesApp;

/**
 * Created by sov on 27.11.2016.
 */

public class PhotoEditor extends Activity implements CommonI {
    private static final String TAG = "PhotoEditor_class";

    Mat origPhoto;
    Mat editedPhoto;
    ImageView resultEdit;

    ResourcesApp resourcesApp;
    EditorEnvironment editorEnvironment;
    int catgoryNum = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.face_editor);

        Bundle b = getIntent().getExtras();
        final String value = b.getString("name");

        origPhoto = Helper.loadPngToMat(new File(value));
        resultEdit = (ImageView) findViewById(R.id.edited_image);

        // TODO there are code past
        resourcesApp = new ResourcesApp(this);
        // TODO if new should be created
        editorEnvironment = ResourcesApp.editor;

        ViewPager viewPagerCategories = (ViewPager) findViewById(R.id.categories);
        CategoriesPagerAdapter pagerCategories = new CategoriesPagerAdapter(this, getResources().getStringArray(R.array.categories));
        viewPagerCategories.setAdapter(pagerCategories);

        ((SeekBar)findViewById(R.id.opacity)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                editorEnvironment.opacity[catgoryNum] = i;
                rechangePhoto();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        findViewById(R.id.apply).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Helper.saveMatToFile(editedPhoto, new File(value));
                // TODO make share button
                finish();
            }
        });
        findViewById(R.id.not_apply).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new File(value).deleteOnExit();
                finish();
            }
        });
        rechangePhoto();

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

    public void changeMask(int newMask) {
        editorEnvironment.newIndexItem = newMask;
        rechangePhoto();
    }

    public void changeColor(int color, int position) {
        if (position == 0) {
            editorEnvironment.currentColor[catgoryNum] = -1;
            return;
        }
        editorEnvironment.currentColor[catgoryNum] = color & 0xFFFFFF;
        rechangePhoto();
    }

    private void rechangePhoto() {
        if (editorEnvironment.currentIndexItem[catgoryNum] != editorEnvironment.newIndexItem) {
            editorEnvironment.loadNewMakeUp(catgoryNum, editorEnvironment.newIndexItem);
        }
        editorEnvironment.currentIndexItem[catgoryNum] = editorEnvironment.newIndexItem;

        Mat mat = origPhoto.clone();
        editorEnvironment.editImage(mat, ResourcesApp.pointsOnFrame);
        Bitmap bmp = Helper.matToBmp(mat);
        resultEdit.setImageBitmap(bmp);
        editedPhoto = mat;
    }

 }
