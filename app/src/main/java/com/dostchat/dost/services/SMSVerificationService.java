package com.dostchat.dost.services;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.dostchat.dost.activities.main.MainActivity;
import com.dostchat.dost.activities.main.PreMainActivity;
import com.dostchat.dost.api.APIAuthentication;
import com.dostchat.dost.api.APIContact;
import com.dostchat.dost.api.APIService;
import com.dostchat.dost.app.EndPoints;
import com.dostchat.dost.helpers.AppHelper;
import com.dostchat.dost.helpers.PreferenceManager;
import com.dostchat.dost.models.JoinModel;
import com.dostchat.dost.models.users.status.StatusResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Abderrahim El imame on 23/02/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class SMSVerificationService extends IntentService {


    public SMSVerificationService() {
        super(SMSVerificationService.class.getSimpleName());
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            String code = intent.getStringExtra("code");
            boolean registration = intent.getBooleanExtra("register", true);
            verifyUser(code, registration);
        }
    }

    private void verifyUser(String code, boolean registration) {
        if (registration) {
            APIAuthentication mAPIAuthentication = APIService.RootService(APIAuthentication.class, EndPoints.BASE_URL);
            Call<JoinModel> VerifyUser = mAPIAuthentication.verifyUser(code);
            VerifyUser.enqueue(new Callback<JoinModel>() {
                                   @Override
                                   public void onResponse(Call<JoinModel> call, Response<JoinModel> response) {
                                       if (response.isSuccessful()) {
                                           if (response.body().isSuccess()) {
                                               PreferenceManager.setIsNewUser(SMSVerificationService.this, true);
                                               PreferenceManager.setID(SMSVerificationService.this, response.body().getUserID());
                                               PreferenceManager.setToken(SMSVerificationService.this, response.body().getToken());
                                               PreferenceManager.setIsWaitingForSms(SMSVerificationService.this, false);
                                               PreferenceManager.setPhone(SMSVerificationService.this, PreferenceManager.getMobileNumber(SMSVerificationService.this));
                                               if (response.body().isHasBackup()) {
                                                   Intent intent = new Intent(SMSVerificationService.this, PreMainActivity.class);
                                                   intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                   startActivity(intent);
                                               } else {
                                                   Intent intent = new Intent(SMSVerificationService.this, MainActivity.class);
                                                   intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                   startActivity(intent);
                                                   if (PreferenceManager.getToken(SMSVerificationService.this) != null) {
                                                       startService(new Intent(SMSVerificationService.this, MainService.class));
                                                   }
                                               }
                                               LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(SMSVerificationService.this);
                                               localBroadcastManager.sendBroadcast(new Intent(getPackageName() + "closeWelcomeActivity"));

                                           } else {
                                               AppHelper.CustomToast(SMSVerificationService.this, response.body().getMessage());
                                           }
                                       } else {
                                           AppHelper.CustomToast(SMSVerificationService.this, response.message());
                                       }
                                   }

                                   @Override
                                   public void onFailure(Call<JoinModel> call, Throwable t) {
                                       AppHelper.LogCat("SMS verification failure  SMSVerificationService" + t.getMessage());

                                   }
                               }

            );
        } else {
            APIService apiService = new APIService(SMSVerificationService.this);
            APIContact apiContact = apiService.RootService(APIContact.class, PreferenceManager.getToken(SMSVerificationService.this), EndPoints.BASE_URL);
            Call<StatusResponse> deleteConfirmation = apiContact.deleteAccountConfirmation(code);
            deleteConfirmation.enqueue(new Callback<StatusResponse>() {
                @Override
                public void onResponse(Call<StatusResponse> call, Response<StatusResponse> response) {

                    if (response.isSuccessful()) {
                        if (response.body().isSuccess()) {
                            LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(SMSVerificationService.this);
                            localBroadcastManager.sendBroadcast(new Intent(getPackageName() + "closeDeleteAccountActivity"));
                        } else {
                            AppHelper.CustomToast(SMSVerificationService.this, response.body().getMessage());
                        }
                    } else {
                        AppHelper.hideDialog();
                        AppHelper.CustomToast(SMSVerificationService.this, response.message());
                    }
                }

                @Override
                public void onFailure(Call<StatusResponse> call, Throwable t) {
                    AppHelper.LogCat("SMS verification failure  SMSVerificationService" + t.getMessage());

                }
            });
        }
    }
}
