package ru.flightlabs.makeup.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import ru.flightlabs.makeup.R;


/**
 * Created by sov on 13.02.2017.
 */

public class ActivitySettings extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_makeup);

        findViewById(R.id.back_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

    }
}
