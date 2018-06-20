package com.dostchat.dost.activities.status;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dostchat.dost.R;
import com.dostchat.dost.app.AppConstants;
import com.dostchat.dost.helpers.AppHelper;
import com.dostchat.dost.helpers.UtilsString;
import com.dostchat.dost.presenters.users.StatusPresenter;

import butterknife.BindView;
import butterknife.ButterKnife;
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;


/**
 * Created by Abderrahim El imame on 28/04/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class EditStatusActivity extends AppCompatActivity {

    @BindView(R.id.cancelStatus)
    TextView cancelStatusBtn;
    @BindView(R.id.OkStatus)
    TextView OkStatusBtn;
    @BindView(R.id.StatusWrapper)
    EmojiconEditText StatusWrapper;
    @BindView(R.id.ParentLayoutStatusEdit)
    LinearLayout ParentLayoutStatusEdit;


    @BindView(R.id.emoticonBtn)
    ImageView EmoticonButton;

    EmojIconActions emojIcon;

    public boolean emoticonShown = false;

    private int statusID;
    private StatusPresenter statusPresenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_status);
        ButterKnife.bind(this);
        setTypeFaces();
        if (getIntent().getExtras() != null) {
            String oldStatus = getIntent().getStringExtra("currentStatus");
            statusID = getIntent().getExtras().getInt("statusID");
            StatusWrapper.setText(oldStatus);
        }
        initializerView();
        statusPresenter = new StatusPresenter(this);

        StatusWrapper.setOnEditorActionListener((v, actionId, event) -> {
            if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                String insertedStatus = UtilsString.escapeJava(StatusWrapper.getText().toString().trim());
                AppHelper.showDialog(this, getString(R.string.adding_new_status));
                statusPresenter.EditCurrentStatus(insertedStatus, statusID);
            }
            return false;
        });

        StatusWrapper.setOnClickListener(v1 -> {
            if (emoticonShown) {
                emoticonShown = false;
                emojIcon.closeEmojIcon();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            }
        });
        EmoticonButton.setOnClickListener(v -> {
            if (!emoticonShown) {
                emoticonShown = true;
                emojIcon = new EmojIconActions(EditStatusActivity.this, ParentLayoutStatusEdit, StatusWrapper, EmoticonButton);
                emojIcon.setIconsIds(R.drawable.ic_keyboard_gray_24dp, R.drawable.ic_emoticon_24dp);
                emojIcon.ShowEmojIcon();

            }

        });


    }


    private void setTypeFaces() {
        if (AppConstants.ENABLE_FONTS_TYPES) {
            cancelStatusBtn.setTypeface(AppHelper.setTypeFace(this, "Futura"));
            OkStatusBtn.setTypeface(AppHelper.setTypeFace(this, "Futura"));
            StatusWrapper.setTypeface(AppHelper.setTypeFace(this, "Futura"));
        }
    }

    /**
     * method to initialize the view
     */
    private void initializerView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.edit_status_activity_title);
        cancelStatusBtn.setOnClickListener(v -> finish());
        OkStatusBtn.setOnClickListener(v -> {
            String insertedStatus = UtilsString.escapeJava(StatusWrapper.getText().toString());
            AppHelper.showDialog(this, getString(R.string.adding_new_status));
            statusPresenter.EditCurrentStatus(insertedStatus, statusID);
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (emojIcon != null) {
            emojIcon.closeEmojIcon();
            emojIcon = null;
        }
    }
}
