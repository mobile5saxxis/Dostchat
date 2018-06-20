package com.dostchat.dost.activities.settings;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dostchat.dost.R;
import com.dostchat.dost.activities.main.SplashScreenActivity;
import com.dostchat.dost.helpers.AppHelper;
import com.dostchat.dost.helpers.PreferenceManager;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Abderrahim El imame on 3/13/17.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */

public class PreferenceLanguageActivity extends AppCompatActivity {


    @BindView(R.id.indicator_english)
    TextView indicatorEnglish;
    @BindView(R.id.indicator_french)
    TextView indicatorFrench;
    @BindView(R.id.english_btn)
    LinearLayout EnglishBtn;
    @BindView(R.id.french_btn)
    LinearLayout FrenchBtn;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language);
        ButterKnife.bind(this);
        setupToolbar();
        EnglishBtn.setOnClickListener(view -> {
            if (indicatorFrench.getVisibility() == View.VISIBLE) {
                ChangeLanguage("en", "US");
            }
        });
        FrenchBtn.setOnClickListener(view -> {
            if (indicatorEnglish.getVisibility() == View.VISIBLE) {
                ChangeLanguage("fr", null);
            }
        });

        loadLocale();
    }


    /**
     * method to setup toolbar
     */
    private void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.title_language);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        AppHelper.LogCat("onConfigurationChanged " + newConfig.locale);
    }


    public void ChangeLanguage(String lang, String country) {
        if (lang.equalsIgnoreCase(""))
            return;
        saveLocale(lang);
        if (country == null)
            setDefaultLocale(this, new Locale(lang));
        else
            setDefaultLocale(this, new Locale(lang, country));
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setMessage(R.string.you_need_to_restart_the_application);
        alert.setPositiveButton(R.string.ok, (dialog, which) -> {
            Intent mainIntent = new Intent(this, SplashScreenActivity.class);
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(mainIntent);
            finish();
        });
        alert.setCancelable(false);
        alert.show();
    }

    @SuppressWarnings("deprecation")
    protected void setDefaultLocale(Context context, Locale locale) {
        Locale.setDefault(locale);
        Configuration appConfig = new Configuration();
        appConfig.locale = locale;
        context.getResources().updateConfiguration(appConfig, context.getResources().getDisplayMetrics());

    }

    public void reload() {
        Intent intent = getIntent();
        overridePendingTransition(0, 0);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        finish();

        overridePendingTransition(0, 0);
        startActivity(intent);
    }

    public void saveLocale(String lang) {
        PreferenceManager.setLanguage(this, lang);
    }

    public void loadLocale() {
        String language = PreferenceManager.getLanguage(this);
        AppHelper.LogCat("language " + language + " getDefault " + Locale.getDefault());
        if (language.equals("fr")) {
            indicatorEnglish.setVisibility(View.GONE);
            indicatorFrench.setVisibility(View.VISIBLE);
        } else if (language.equals("en")) {
            indicatorEnglish.setVisibility(View.VISIBLE);
            indicatorFrench.setVisibility(View.GONE);
        }

    }


}