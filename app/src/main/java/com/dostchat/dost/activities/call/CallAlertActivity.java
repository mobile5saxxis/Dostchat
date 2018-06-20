package com.dostchat.dost.activities.call;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.dostchat.dost.R;
import com.dostchat.dost.app.AppConstants;
import com.dostchat.dost.helpers.AppHelper;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Abderrahim El imame on 12/20/16.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */

public class CallAlertActivity extends AppCompatActivity {

    @BindView(R.id.couldnt_msg)
    TextView couldntMsg;
    @BindView(R.id.finishBtn)
    TextView finishBtn;
    @BindView(R.id.couldnt_txt)
    TextView couldntTxt;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_alert);
        ButterKnife.bind(this);
        setTypeFaces();
    }


    private void setTypeFaces() {
        if (AppConstants.ENABLE_FONTS_TYPES) {
            couldntMsg.setTypeface(AppHelper.setTypeFace(this, "Futura"));
            finishBtn.setTypeface(AppHelper.setTypeFace(this, "Futura"));
            couldntTxt.setTypeface(AppHelper.setTypeFace(this, "Futura"));
        }
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.finishBtn)
    void finishActivity() {
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
