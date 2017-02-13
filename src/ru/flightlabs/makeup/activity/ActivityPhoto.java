package ru.flightlabs.makeup.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import java.io.File;

import ru.flightlabs.commonlib.Settings;
import ru.flightlabs.makeup.R;


/**
 * Created by sov on 13.02.2017.
 */

public class ActivityPhoto extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.after_photo_makeup);

        // TODO add pager
        Bundle extras = getIntent().getExtras();
        final String fileName = extras.getString(Settings.PHOTO);

        Bitmap photo = BitmapFactory.decodeFile(fileName);
        ImageView imageView = (ImageView) findViewById(R.id.main_image);
        imageView.setImageBitmap(photo);

        findViewById(R.id.back_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        findViewById(R.id.thrash_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO use adapter
                new File(fileName).delete();
                onBackPressed();
            }
        });
        findViewById(R.id.share_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(fileName)));
                shareIntent.setType("image/jpeg");
                startActivity(Intent.createChooser(shareIntent, getString(R.string.send_photo_with)));
            }
        });

    }
}