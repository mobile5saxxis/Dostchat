package com.dostchat.dost.activities.status;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.dostchat.dost.R;
import com.dostchat.dost.adapters.recyclerView.StatusAdapter;
import com.dostchat.dost.app.AppConstants;
import com.dostchat.dost.app.DostChatApp;
import com.dostchat.dost.helpers.AppHelper;
import com.dostchat.dost.helpers.UtilsString;
import com.dostchat.dost.interfaces.NetworkListener;
import com.dostchat.dost.models.users.Pusher;
import com.dostchat.dost.models.users.status.StatusModel;
import com.dostchat.dost.presenters.users.StatusPresenter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import hani.momanii.supernova_emoji_library.Helper.EmojiconTextView;


/**
 * Created by Abderrahim El imame on 28/04/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class StatusActivity extends AppCompatActivity implements NetworkListener {

    @BindView(R.id.currentStatus)
    EmojiconTextView currentStatus;
    @BindView(R.id.editCurrentStatusBtn)
    ImageView editCurrentStatusBtn;
    @BindView(R.id.StatusList)
    RecyclerView StatusList;
    @BindView(R.id.ParentLayoutStatus)
    LinearLayout ParentLayoutStatus;

    private List<StatusModel> mStatusModelList;
    private StatusAdapter mStatusAdapter;
    private StatusPresenter mStatusPresenter;
    private int statusID;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);
        ButterKnife.bind(this);
        setTypeFaces();
        mStatusPresenter = new StatusPresenter(this);
        mStatusPresenter.onCreate();
        initializerView();
        setupToolbar();


    }


    private void setTypeFaces() {
        if (AppConstants.ENABLE_FONTS_TYPES) {
            currentStatus.setTypeface(AppHelper.setTypeFace(this, "Futura"));
        }
    }

    private void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /**
     * method to initialize the view
     */
    public void initializerView() {
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(getApplicationContext());
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mStatusAdapter = new StatusAdapter(this, mStatusModelList, mStatusPresenter);
        StatusList.setLayoutManager(mLinearLayoutManager);
        StatusList.setAdapter(mStatusAdapter);
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.editCurrentStatusBtn)
    void launchEditStatus(View v) {
        Intent mIntent = new Intent(this, EditStatusActivity.class);
        mIntent.putExtra("statusID", statusID);
        mIntent.putExtra("currentStatus", currentStatus.getText().toString().trim());
        startActivity(mIntent);
    }

    /**
     * method to show status list
     *
     * @param statusModels this is parameter for  ShowStatus   method
     */
    public void ShowStatus(List<StatusModel> statusModels) {
        mStatusModelList = statusModels;
        mStatusPresenter.getCurrentStatus();
    }

    /**
     * method to update status list
     *
     * @param statusModels this is parameter for  updateStatusList   method
     */
    public void updateStatusList(List<StatusModel> statusModels) {
        mStatusModelList = statusModels;
        mStatusAdapter.notifyDataSetChanged();
        mStatusPresenter.getCurrentStatus();
    }


    @SuppressWarnings("unused")
    public void onEventMainThread(Pusher pusher) {
        mStatusPresenter.onEventPush(pusher);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.status_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                break;
            case R.id.deleteStatus:
                mStatusPresenter.DeleteAllStatus();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mStatusPresenter.onDestroy();
    }

    /**
     * method to show the current status
     *
     * @param statusModel this is parameter for  ShowCurrentStatus   method
     */
    public void ShowCurrentStatus(String statusModel) {
        String status = UtilsString.unescapeJava(statusModel);
        currentStatus.setText(status);
    }

    /**
     * method to show the current status
     *
     * @param statusModel this is parameter for  ShowCurrentStatus   method
     */
    public void ShowCurrentStatus(StatusModel statusModel) {
        if (statusModel.getStatus() != null) {
            statusID = statusModel.getCurrentStatusID();
            String status = UtilsString.unescapeJava(statusModel.getStatus());
            currentStatus.setText(status);
        } else {
            AppHelper.Snackbar(this, ParentLayoutStatus, getString(R.string.failed_to_the_current_status), AppConstants.MESSAGE_COLOR_ERROR, AppConstants.TEXT_COLOR);
        }
    }

    public void onErrorLoading(Throwable throwable) {
        AppHelper.LogCat("status error" + throwable.getMessage());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mStatusPresenter.onResume();
        DostChatApp.getInstance().setConnectivityListener(this);
    }

    /**
     * Callback will be triggered when there is change in
     * network connection
     */
    @Override
    public void onNetworkConnectionChanged(boolean isConnecting, boolean isConnected) {
        if (!isConnecting && !isConnected) {
            AppHelper.Snackbar(this, ParentLayoutStatus, getString(R.string.connection_is_not_available), AppConstants.MESSAGE_COLOR_ERROR, AppConstants.TEXT_COLOR);
        } else if (isConnecting && isConnected) {
            AppHelper.Snackbar(this, ParentLayoutStatus, getString(R.string.connection_is_available), AppConstants.MESSAGE_COLOR_SUCCESS, AppConstants.TEXT_COLOR);
        } else {
            AppHelper.Snackbar(this, ParentLayoutStatus, getString(R.string.waiting_for_network), AppConstants.MESSAGE_COLOR_WARNING, AppConstants.TEXT_COLOR);

        }
    }

    public void deleteStatus(int statusID) {
        mStatusAdapter.DeleteStatusItem(statusID);
    }
}
