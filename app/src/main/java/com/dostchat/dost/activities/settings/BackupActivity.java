package com.dostchat.dost.activities.settings;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.dostchat.dost.R;
import com.dostchat.dost.api.APIService;
import com.dostchat.dost.api.FilesUploadService;
import com.dostchat.dost.app.AppConstants;
import com.dostchat.dost.app.EndPoints;
import com.dostchat.dost.helpers.AppHelper;
import com.dostchat.dost.helpers.Files.UploadFilesHelper;
import com.dostchat.dost.helpers.Files.backup.RealmBackupRestore;
import com.dostchat.dost.helpers.PreferenceManager;
import com.dostchat.dost.helpers.UtilsTime;
import com.dostchat.dost.interfaces.UploadCallbacks;
import com.dostchat.dost.models.BackupModel;
import com.dostchat.dost.models.messages.MessagesModel;

import org.joda.time.DateTime;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Abderrahim El imame on 11/1/16.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */

public class BackupActivity extends AppCompatActivity {

    @BindView(R.id.progress_bar_backup)
    AppCompatImageView ProgressBarBackupBtn;
    @BindView(R.id.percent_backup)
    TextView percentBackup;
    @BindView(R.id.start_backup)
    TextView startBackupBtn;
    @BindView(R.id.last_backup)
    TextView lastBackup;
    @BindView(R.id.backup_text)
    TextView backup_text;
    @BindView(R.id.backup_msg)
    TextView backup_msg;
    @BindView(R.id.backup_chat)
    TextView backup_chat;

    private APIService mApiService;
    private UploadCallbacks mUploadCallbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup);
        ButterKnife.bind(this);
        init();
        setupToolbar();
        setTypeFaces();
    }


    private void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getString(R.string.chat_backup));
        }

    }

    private void setTypeFaces() {
        if (AppConstants.ENABLE_FONTS_TYPES) {
            backup_msg.setTypeface(AppHelper.setTypeFace(this, "Futura"));
            backup_text.setTypeface(AppHelper.setTypeFace(this, "Futura"));
            lastBackup.setTypeface(AppHelper.setTypeFace(this, "Futura"));
            backup_chat.setTypeface(AppHelper.setTypeFace(this, "Futura"));
            startBackupBtn.setTypeface(AppHelper.setTypeFace(this, "Futura"));
            percentBackup.setTypeface(AppHelper.setTypeFace(this, "Futura"));
        }
    }


    public void init() {
        if (PreferenceManager.lastBackup(this) != null) {
            DateTime lastDate = UtilsTime.getCorrectDate(PreferenceManager.lastBackup(this));
            String finalDate = UtilsTime.convertDateToString(BackupActivity.this, lastDate);
            lastBackup.setText("You last back up is done at :" + finalDate);
        } else {
            lastBackup.setText("You didn't make a backup yet");
        }
        this.mApiService = new APIService(this);
        mUploadCallbacks = new UploadCallbacks() {
            @Override
            public void onUpdate(int percentage, String type) {
                if (type.equals("backup")) {
                    startCircleProgressAnimation();
                    percentBackup.setText("Creating a local backup (" + percentage + "%). Backups occur  ");
                }
            }

            @Override
            public void onError(String type) {
                if (type.equals("backup")) {
                    percentBackup.setText("Failed to backup your messages");
                    stopCircleProgressAnimation();
                }
            }

            @Override
            public void onFinish(String type, MessagesModel messagesModel) {
                if (type.equals("backup")) {
                    DateTime current = new DateTime();
                    String createTime = String.valueOf(current);
                    PreferenceManager.setLastBackup(BackupActivity.this, createTime);
                    String finalDate = UtilsTime.convertDateToString(BackupActivity.this, current);
                    percentBackup.setText("Back up is done successfully at : " + finalDate);
                    lastBackup.setText("You last backup is done at :" + finalDate);
                    stopCircleProgressAnimation();
                }
            }
        };

    }

    @SuppressWarnings("unused")
    @OnClick(R.id.start_backup)
    public void startBackingUp() {
        startBackupBtn.setVisibility(View.GONE);
        File exportRealmFile = RealmBackupRestore.backup(BackupActivity.this);
        UploadFilesHelper mUploadFilesHelper = new UploadFilesHelper(exportRealmFile, mUploadCallbacks, "multipart/form-data", null, "backup");
        FilesUploadService filesUploadService = mApiService.RootService(FilesUploadService.class, PreferenceManager.getToken(BackupActivity.this), EndPoints.BASE_URL);
        Call<BackupModel> filesResponseCall = filesUploadService.uploadMessageBackup(mUploadFilesHelper);
        new AsyncTask<Void, Long, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                filesResponseCall.enqueue(new Callback<BackupModel>() {
                    @Override
                    public void onResponse(Call<BackupModel> call, Response<BackupModel> response) {
                        startBackupBtn.setVisibility(View.VISIBLE);
                        if (response.isSuccessful()) {
                            if (response.body().isSuccess()) {
                                mUploadCallbacks.onFinish("backup", null);
                                AlertDialog.Builder alert = new AlertDialog.Builder(BackupActivity.this);
                                alert.setMessage(response.body().getMessage());
                                alert.setPositiveButton(R.string.finish, (dialog, which) -> {
                                });
                                alert.setCancelable(false);
                                alert.show();

                            } else {
                                AppHelper.LogCat("failed to upload document isNotSuccess" + response.body().getMessage());
                                AlertDialog.Builder alert = new AlertDialog.Builder(BackupActivity.this);
                                alert.setMessage(response.body().getMessage());
                                alert.setPositiveButton(R.string.ok, (dialog, which) -> {
                                });
                                alert.setCancelable(false);
                                alert.show();
                                mUploadCallbacks.onError("backup");
                            }
                        } else {
                            AppHelper.LogCat("failed to upload backup is Not Successful  ");
                            AlertDialog.Builder alert = new AlertDialog.Builder(BackupActivity.this);
                            alert.setMessage("Failed to upload the backup");
                            alert.setPositiveButton(R.string.ok, (dialog, which) -> {
                            });
                            alert.setCancelable(false);
                            alert.show();
                            mUploadCallbacks.onError("backup");
                        }
                    }

                    @Override
                    public void onFailure(Call<BackupModel> call, Throwable t) {
                        startBackupBtn.setVisibility(View.VISIBLE);
                        AlertDialog.Builder alert = new AlertDialog.Builder(BackupActivity.this);
                        alert.setMessage("Failed to upload the backup");
                        alert.setPositiveButton(R.string.ok, (dialog, which) -> {
                        });
                        alert.setCancelable(false);
                        alert.show();
                        mUploadCallbacks.onError("backup");
                        AppHelper.LogCat("failed to upload backup Throwable " + t.getMessage());

                    }
                });

                return null;
            }
        }.execute();
    }

    private void startCircleProgressAnimation() {
        this.ProgressBarBackupBtn.startAnimation(AnimationUtils.loadAnimation(this, R.anim.rotate_animation));
    }

    private void stopCircleProgressAnimation() {
        if (this.ProgressBarBackupBtn != null) {
            this.ProgressBarBackupBtn.clearAnimation();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        startCircleProgressAnimation();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
