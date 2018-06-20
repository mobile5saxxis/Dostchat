package com.dostchat.dost.activities.settings;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.dostchat.dost.R;
import com.dostchat.dost.app.AppConstants;
import com.dostchat.dost.helpers.AppHelper;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Abderrahim El imame on 8/19/16.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/bencherif_el
 */

public class AboutActivity extends AppCompatActivity {

    @BindView(R.id.version)
    TextView version;
    @BindView(R.id.about_enjoy_it)
    TextView aboutEnjoyIt;
    @BindView(R.id.about_app_name)
    TextView appName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ButterKnife.bind(this);
        setTypeFaces();
        String appVersion = AppHelper.getAppVersion(this);
        version.setText(getString(R.string.app_version) + " " + appVersion);

    }

    private void setTypeFaces() {
        if (AppConstants.ENABLE_FONTS_TYPES) {
            version.setTypeface(AppHelper.setTypeFace(this, "Futura"));
            aboutEnjoyIt.setTypeface(AppHelper.setTypeFace(this, "Futura"));
            appName.setTypeface(AppHelper.setTypeFace(this, "Futura"));
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

}
