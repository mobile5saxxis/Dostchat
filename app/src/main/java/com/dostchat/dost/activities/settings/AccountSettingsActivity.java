package com.dostchat.dost.activities.settings;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import com.dostchat.dost.R;
import com.dostchat.dost.activities.BlockedContactsActivity;
import com.dostchat.dost.app.AppConstants;
import com.dostchat.dost.helpers.AppHelper;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Abderrahim El imame on 8/17/16.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/bencherif_el
 */

public class AccountSettingsActivity extends AppCompatActivity {
    @BindView(R.id.app_bar)
    Toolbar toolbar;
    @BindView(R.id.delete_account_text)
    TextView deleteAccText;
    @BindView(R.id.blocked_contacts_text)
    TextView blockedContacts;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);
        ButterKnife.bind(this);
        setupToolbar();
        setTypeFaces();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }


    private void setTypeFaces() {
        if (AppConstants.ENABLE_FONTS_TYPES) {
            deleteAccText.setTypeface(AppHelper.setTypeFace(this, "Futura"));
            blockedContacts.setTypeface(AppHelper.setTypeFace(this, "Futura"));
        }
    }

/*

    @SuppressWarnings("unused")
    @OnClick(R.id.change_number)
    public void launchChangeNumber() {
        AppHelper.LaunchActivity(this, ChangeNumberActivity.class);
    }

*/

    @SuppressWarnings("unused")
    @OnClick(R.id.delete_account)
    public void launchDeleteAccount() {
        AppHelper.LaunchActivity(this, DeleteAccountActivity.class);
    }


    @SuppressWarnings("unused")
    @OnClick(R.id.blocked_contacts)
    public void launchBlockedContacts() {
        AppHelper.LaunchActivity(this, BlockedContactsActivity.class);
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
}
