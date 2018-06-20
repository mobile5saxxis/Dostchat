package com.dostchat.dost.activities.groups;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dostchat.dost.R;
import com.dostchat.dost.app.AppConstants;
import com.dostchat.dost.app.DostChatApp;
import com.dostchat.dost.helpers.AppHelper;
import com.dostchat.dost.helpers.UtilsString;
import com.dostchat.dost.interfaces.NetworkListener;
import com.dostchat.dost.presenters.groups.EditGroupPresenter;

import butterknife.BindView;
import butterknife.ButterKnife;
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;

/**
 * Created by abderrahimelimame on 6/9/16.
 * Email : abderrahim.elimame@gmail.com
 */

public class EditGroupActivity extends AppCompatActivity implements NetworkListener {
    @BindView(R.id.cancelStatus)
    TextView cancelStatusBtn;
    @BindView(R.id.OkStatus)
    TextView OkStatusBtn;
    @BindView(R.id.StatusWrapper)
    EmojiconEditText StatusWrapper;
    @BindView(R.id.ParentLayoutStatusEdit)
    LinearLayout ParentLayoutStatusEdit;

    @BindView(R.id.app_bar)
    Toolbar toolbar;

    @BindView(R.id.emoticonBtn)
    ImageView EmoticonButton;

    EmojIconActions emojIcon;


    public boolean emoticonShown = false;
    private String oldName;
    private int groupID;
    private EditGroupPresenter mEditGroupPresenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_status);
        ButterKnife.bind(this);
        initializerView();
        setTypeFaces();
        mEditGroupPresenter = new EditGroupPresenter(this);
        mEditGroupPresenter.onCreate();
        if (getIntent().getExtras() != null) {
            oldName = getIntent().getStringExtra("currentGroupName");
            groupID = getIntent().getExtras().getInt("groupID");
        }
        String oldNameUnescape = UtilsString.unescapeJava(oldName);
        StatusWrapper.setText(oldNameUnescape);
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
                emojIcon = new EmojIconActions(EditGroupActivity.this, ParentLayoutStatusEdit, StatusWrapper, EmoticonButton);
                emojIcon.setIconsIds(R.drawable.ic_keyboard_gray_24dp, R.drawable.ic_emoticon_24dp);
                emojIcon.ShowEmojIcon();

            }

        });

    }

    private void setTypeFaces() {
        if (AppConstants.ENABLE_FONTS_TYPES) {
            cancelStatusBtn.setTypeface(AppHelper.setTypeFace(this, "Futura"));
            OkStatusBtn.setTypeface(AppHelper.setTypeFace(this, "Futura"));

        }
    }


    private void initializerView() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.title_activity_edit_name);
        cancelStatusBtn.setOnClickListener(v -> finish());
        OkStatusBtn.setOnClickListener(v -> {
            String insertedName = UtilsString.escapeJava(StatusWrapper.getText().toString().trim());
            try {
                mEditGroupPresenter.EditCurrentName(insertedName, groupID);
            } catch (Exception e) {
                AppHelper.LogCat("Edit group name Exception  EditGroupActivity " + e.getMessage());
            }

        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();

        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();

        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }


    @Override
    protected void onResume() {
        super.onResume();
        DostChatApp.getInstance().setConnectivityListener(this);
    }

    /**
     * Callback will be triggered when there is change in
     * network connection
     */
    @Override
    public void onNetworkConnectionChanged(boolean isConnecting, boolean isConnected) {

        if (!isConnecting && !isConnected) {
            AppHelper.Snackbar(this, ParentLayoutStatusEdit, getString(R.string.connection_is_not_available), AppConstants.MESSAGE_COLOR_ERROR, AppConstants.TEXT_COLOR);
        } else if (isConnecting && isConnected) {
            AppHelper.Snackbar(this, ParentLayoutStatusEdit, getString(R.string.connection_is_available), AppConstants.MESSAGE_COLOR_SUCCESS, AppConstants.TEXT_COLOR);
        } else {
            AppHelper.Snackbar(this, ParentLayoutStatusEdit, getString(R.string.waiting_for_network), AppConstants.MESSAGE_COLOR_WARNING, AppConstants.TEXT_COLOR);

        }
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
