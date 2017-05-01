package ru.flightlabs.makeup.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.firebase.analytics.FirebaseAnalytics;

import ru.oramalabs.beautykit.BeautyKit;
import ru.oramalabs.beautykit.BuildConfig;
import ru.oramalabs.beautykit.R;


/**
 * Created by sov on 13.02.2017.
 */

public class ActivitySettings extends Activity {

    private Tracker mTracker;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_makeup);
        BeautyKit application = (BeautyKit) getApplication();
        mTracker = application.getDefaultTracker();

        findViewById(R.id.back_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        ((TextView)findViewById(R.id.text_version)).setText(String.format(getString(R.string.version_app), BuildConfig.VERSION_NAME, BuildConfig.BUILD_TIME));
    }

    public void rateApp(View view) {
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("Action")
                .setAction("RateApp")
                .build());

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id="
                + getPackageName()));
        startActivity(intent);
    }
    public void shareApp(View view) {
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("Action")
                .setAction("ShareApp")
                .build());

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT,
                "https://play.google.com/store/apps/details?id=" + getPackageName());
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }

    public void gotoUrl(View view) {
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("Action")
                .setAction("GotoUrl")
                .build());

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://flightlabs.ru"));
        startActivity(browserIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTracker.setScreenName("ActivitySettings");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }
}
