package com.dostchat.dost.activities.main;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatImageView;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.dostchat.dost.R;
import com.dostchat.dost.api.APIService;
import com.dostchat.dost.api.FilesDownloadService;
import com.dostchat.dost.app.AppConstants;
import com.dostchat.dost.app.EndPoints;
import com.dostchat.dost.helpers.AppHelper;
import com.dostchat.dost.helpers.Files.DownloadBackupFileHelper;
import com.dostchat.dost.helpers.Files.backup.RealmBackupRestore;
import com.dostchat.dost.helpers.PreferenceManager;
import com.dostchat.dost.helpers.UtilsTime;
import com.dostchat.dost.interfaces.DownloadCallbacks;
import com.dostchat.dost.models.BackupModel;
import com.dostchat.dost.models.messages.MessagesModel;
import com.dostchat.dost.services.MainService;

import org.joda.time.DateTime;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Abderrahim El imame on 10/31/16.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */

public class PreMainActivity extends Activity {

    @BindView(R.id.restore_backup)
    AppCompatImageView restoreBackupBtn;
    @BindView(R.id.skip_backup)
    AppCompatImageView skipBackupBtn;
    @BindView(R.id.progress_bar_backup)
    AppCompatImageView ProgressBarBackupBtn;

    @BindView(R.id.title_toolbar)
    TextView titleToolbar;

    @BindView(R.id.restore_data_text)
    TextView restoreDataText;

    @BindView(R.id.restore_data_msg)
    TextView restoreDataMsg;

    @BindView(R.id.restore_backup_text)
    TextView restoreBackupText;

    @BindView(R.id.skip_backup_txt)
    TextView skipBackupTxt;

    @BindView(R.id.percent_backup)
    TextView percentBackup;

    private APIService mApiService;
    private FilesDownloadService downloadService;
    private DownloadCallbacks mDownloadCallbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre_main);
        ButterKnife.bind(this);
        setTypeFaces();
        skipBackupBtn.setOnClickListener(v -> {
            if (PreferenceManager.getToken(PreMainActivity.this) != null) {
                startService(new Intent(this, MainService.class));
            }
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        });
        this.mApiService = new APIService(this);
        mDownloadCallbacks = new DownloadCallbacks() {
            @Override
            public void onUpdate(int percentage, String type) {
                if (type.equals("backup")) {
                    startCircleProgressAnimation();
                    percentBackup.setText("Restoring your backup " + percentage + " . Please wait .... ");
                }
            }

            @Override
            public void onError(String type) {
                if (type.equals("backup")) {
                    stopCircleProgressAnimation();
                    percentBackup.setText(R.string.failed_to_restore_the_backup);
                    AlertDialog.Builder alert = new AlertDialog.Builder(PreMainActivity.this);
                    alert.setMessage(R.string.failed_to_restore_the_backup_try);
                    alert.setPositiveButton(R.string.ok, (dialog, which) -> {

                    });
                    alert.setCancelable(false);
                    alert.show();
                }
            }

            @Override
            public void onFinish(String type, MessagesModel messagesModel) {
                if (type.equals("backup")) {
                    stopCircleProgressAnimation();
                    DateTime current = new DateTime();
                    String finalDate = UtilsTime.convertDateToString(PreMainActivity.this, current);
                    percentBackup.setText(getString(R.string.restore_is_done) + finalDate);
                    AlertDialog.Builder alert = new AlertDialog.Builder(PreMainActivity.this);
                    alert.setMessage(getString(R.string.restore_is_done));
                    alert.setPositiveButton(R.string.next, (dialog, which) -> {
                        AppHelper.showDialog(PreMainActivity.this, getString(R.string.please_wait_a_moment), false);
                        new Handler().postDelayed(() -> {
                            AppHelper.hideDialog();
                            if (PreferenceManager.getToken(PreMainActivity.this) != null) {
                                startService(new Intent(PreMainActivity.this, MainService.class));
                            }
                            Intent intent = new Intent(PreMainActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }, 1000);

                    });
                    alert.setCancelable(false);
                    alert.show();

                }
            }
        };
    }

    private void setTypeFaces() {
        if (AppConstants.ENABLE_FONTS_TYPES) {
            titleToolbar.setTypeface(AppHelper.setTypeFace(this, "Futura"));
            restoreDataText.setTypeface(AppHelper.setTypeFace(this, "Futura"));
            restoreDataMsg.setTypeface(AppHelper.setTypeFace(this, "Futura"));
            restoreBackupText.setTypeface(AppHelper.setTypeFace(this, "Futura"));
            skipBackupTxt.setTypeface(AppHelper.setTypeFace(this, "Futura"));

        }
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
    public void onBackPressed() {

    }

    @SuppressWarnings("unused")
    @OnClick(R.id.restore_backup)
    public void getBackupUrl() {
        downloadService = mApiService.RootService(FilesDownloadService.class, PreferenceManager.getToken(this), EndPoints.BASE_URL);
        Call<BackupModel> backupModelCall = downloadService.getBackupUrl();
        backupModelCall.enqueue(new Callback<BackupModel>() {
            @Override
            public void onResponse(Call<BackupModel> call, Response<BackupModel> response) {
                if (response.isSuccessful()) {
                    if (response.body().isSuccess()) {
                        String fileUrl = EndPoints.MESSAGE_BACKUP_DOWNLOAD_URL + response.body().getMessage();
                        downloadBackup(fileUrl);
                    } else {
                        AppHelper.CustomToast(PreMainActivity.this, getString(R.string.oops_something));
                    }
                }

            }

            @Override
            public void onFailure(Call<BackupModel> call, Throwable t) {

            }
        });
    }

    public void downloadBackup(String finalFileUrl) {
        Call<ResponseBody> downloadResponseCall = downloadService.downloadLargeFileSizeUrlSync(finalFileUrl);
        new AsyncTask<Void, Long, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                downloadResponseCall.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            AppHelper.LogCat("server contacted and has file");
                            DownloadBackupFileHelper downloadFilesHelper = new DownloadBackupFileHelper(response.body(), "backup", mDownloadCallbacks);
                            boolean writtenToDisk = downloadFilesHelper.writeResponseBodyToDisk(PreMainActivity.this);
                            if (writtenToDisk) {
                                if (RealmBackupRestore.restore(PreMainActivity.this)) {
                                    mDownloadCallbacks.onFinish("backup", null);
                                }
                            } else {
                                mDownloadCallbacks.onError("backup");
                            }

                        } else {
                            AppHelper.LogCat("server contact failed");
                            mDownloadCallbacks.onError("backup");
                        }

                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        AppHelper.LogCat("download is failed " + t.getMessage());
                        mDownloadCallbacks.onError("backup");
                    }


                });
                return null;
            }
        }.execute();
    }

}
