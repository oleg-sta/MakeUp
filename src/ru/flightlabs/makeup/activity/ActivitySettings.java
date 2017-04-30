package ru.flightlabs.makeup.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;

import ru.oramalabs.beautykit.BuildConfig;
import ru.oramalabs.beautykit.R;


/**
 * Created by sov on 13.02.2017.
 */

public class ActivitySettings extends Activity {

    private FirebaseAnalytics mFirebaseAnalytics;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_makeup);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        findViewById(R.id.back_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        ((TextView)findViewById(R.id.text_version)).setText(String.format(getString(R.string.version_app), BuildConfig.VERSION_NAME, BuildConfig.BUILD_TIME));
    }

    public void rateApp(View view) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "rate app");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "app");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id="
                + getPackageName()));
        startActivity(intent);
    }
    public void shareApp(View view) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "share app");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "app");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT,
                "https://play.google.com/store/apps/details?id=" + getPackageName());
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }

    public void gotoUrl(View view) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "goto url");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "app");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://flightlabs.ru"));
        startActivity(browserIntent);
    }
}
