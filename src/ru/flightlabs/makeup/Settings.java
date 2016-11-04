package ru.flightlabs.makeup;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

public class Settings extends Activity {
    
    public static final String PREFS = "Filter";
    public static final String DEBUG_MODE = "debugMode";
    public static final String MULTI_MODE = "multiMode";
    public static final String PUPILS_MODE = "pupilsMode";
    public static final String COUNTER_PHOTO = "photoCounter";
    public static final String MODEL_PATH = "modelPath";
    private static final String TAG = "Settings_class";

}
