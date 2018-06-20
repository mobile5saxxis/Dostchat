package com.dostchat.dost.activities.main;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.dostchat.dost.R;
import com.dostchat.dost.helpers.AppHelper;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Abderrahim El imame on 1/9/17.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */

public class SplashScreenActivity extends AbstractPermissionActivity {

    int SPLASH_TIME_OUT = 2000;

    @BindView(R.id.splash_app_name)
    TextView splashAppName;
    @BindView(R.id.splash_message)
    TextView splashMessage;

    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };
    private static final int REQUEST_LOCATION = 1;

    @Override
    protected String[] getDesiredPermissions() {
        return PERMISSIONS_STORAGE;
    }

    @Override
    protected void onPermissionDenied() {
        finish();
        Toast.makeText(this, "App scannot open if any permission is denied", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onReady(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        if (AppHelper.isAndroid5()) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            getWindow().setStatusBarColor(AppHelper.getColor(this, R.color.colorBlackLight));
        }
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);
        setTypeFaces();
        new Handler().postDelayed(this::launchWelcomeActivity, SPLASH_TIME_OUT);
    }


    private void setTypeFaces() {
        splashAppName.setTypeface(AppHelper.setTypeFace(this, "Futura"));
        splashMessage.setTypeface(AppHelper.setTypeFace(this, "Futura"));
    }

    public void launchWelcomeActivity() {
        Intent mainIntent = new Intent(this, WelcomeActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(mainIntent);
        finish();

    }
}