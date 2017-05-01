package ru.flightlabs.makeup.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import ru.flightlabs.commonlib.Settings;
import ru.flightlabs.makeup.adapter.PhotoPagerAdapter;
import ru.oramalabs.beautykit.BeautyKit;
import ru.oramalabs.beautykit.R;


/**
 * Created by sov on 13.02.2017.
 */

public class ActivityPhoto extends Activity {

    private Tracker mTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.after_photo_makeup);

        BeautyKit application = (BeautyKit) getApplication();
        mTracker = application.getDefaultTracker();

        // TODO add pager
        Bundle extras = getIntent().getExtras();
        final String fileName = extras.getString(Settings.PHOTO);

        final ViewPager pager = (ViewPager)findViewById(R.id.pager);
        // TODO список
        File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        File newFile = new File(file, Settings.DIRECTORY_SELFIE);
        if(!newFile.exists()){
            newFile.mkdirs();
        }
        File[] files = newFile.listFiles();

        Arrays.sort(files, new Comparator<File>() {
            public int compare(File f1, File f2) {
                return -Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
            }
        });

        final List<String> photos = new ArrayList<String>();
        for (File f : files) {
            photos.add(f.getPath());
        }
        final PhotoPagerAdapter adapter = new PhotoPagerAdapter(this, photos);
        pager.setAdapter(adapter);

        findViewById(R.id.back_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        findViewById(R.id.thrash_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Action")
                        .setAction("DeletePhoto")
                        .build());

                // TODO chekc for last photo
                new File(adapter.photos.get(pager.getCurrentItem())).delete();
                photos.remove(adapter.photos.get(pager.getCurrentItem()));
                adapter.notifyDataSetChanged();
                // TODO if last photo then exit after delete
            }
        });
        findViewById(R.id.share_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Action")
                        .setAction("SharePhoto")
                        .build());

                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(adapter.photos.get(pager.getCurrentItem()))));
                shareIntent.setType("image/jpeg");
                startActivity(Intent.createChooser(shareIntent, getString(R.string.send_photo_with)));
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        mTracker.setScreenName("ActivityPhoto");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }
}