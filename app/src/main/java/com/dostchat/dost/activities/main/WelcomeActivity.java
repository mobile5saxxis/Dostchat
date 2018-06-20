package com.dostchat.dost.activities.main;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.design.widget.TextInputEditText;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.AppCompatImageView;
import android.text.Editable;
import android.text.InputFilter;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.dostchat.dost.R;
import com.dostchat.dost.activities.CountryActivity;
import com.dostchat.dost.adapters.others.TextWatcherAdapter;
import com.dostchat.dost.api.APIAuthentication;
import com.dostchat.dost.api.APIService;
import com.dostchat.dost.api.apiServices.UsersContacts;
import com.dostchat.dost.app.AppConstants;
import com.dostchat.dost.app.EndPoints;
import com.dostchat.dost.app.DostChatApp;
import com.dostchat.dost.helpers.AppHelper;
import com.dostchat.dost.helpers.CountriesFetcher;
import com.dostchat.dost.helpers.PermissionHandler;
import com.dostchat.dost.helpers.PhoneNumberWatcher;
import com.dostchat.dost.helpers.PreferenceManager;
import com.dostchat.dost.helpers.notifications.NotificationsManager;
import com.dostchat.dost.models.CountriesModel;
import com.dostchat.dost.models.JoinModel;
import com.dostchat.dost.services.SMSVerificationService;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Abderrahim El imame on 09/02/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class WelcomeActivity extends AccountAuthenticatorActivity implements View.OnClickListener {
    @BindView(R.id.numberPhone)
    TextInputEditText phoneNumberWrapper;
    @BindView(R.id.inputOtpWrapper)
    TextInputEditText inputOtpWrapper;
    @BindView(R.id.btn_request_sms)
    TextView btnNext;
    @BindView(R.id.btn_change_number)
    AppCompatImageView changeNumberBtn;
    @BindView(R.id.btn_verify_otp)
    AppCompatImageView btnVerifyOtp;
    @BindView(R.id.viewPagerVertical)
    ViewPager viewPager;
    @BindView(R.id.TimeCount)
    TextView textViewShowTime;
    @BindView(R.id.Resend)
    TextView ResendBtn;
    @BindView(R.id.country_name)
    TextView countryName;
    @BindView(R.id.current_mobile_number)
    TextView currentMobileNumber;
    @BindView(R.id.numberPhone_layout_sv)
    ScrollView numberPhoneLayoutSv;
    @BindView(R.id.layout_verification_sv)
    ScrollView layoutVerificationSv;


    private CountDownTimer countDownTimer;
    private long totalTimeCountInMilliseconds;


    //for account manager
    private String mOldAccountType;


    private final String DEFAULT_COUNTRY = Locale.getDefault().getCountry();
    private PhoneNumberWatcher mPhoneNumberWatcher = new PhoneNumberWatcher(DEFAULT_COUNTRY);
    private PhoneNumberUtil mPhoneUtil = PhoneNumberUtil.getInstance();
    private CountriesModel mSelectedCountry;
    private CountriesFetcher.CountryList mCountries;
    LocalBroadcastManager mLocalBroadcastManager;
    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(getPackageName() + "closeWelcomeActivity")) {
                finish();
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        ButterKnife.bind(this);
        initializerView();
        setTypeFaces();
        NotificationsManager.SetupBadger(this);
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(getPackageName() + "closeWelcomeActivity");
        mLocalBroadcastManager.registerReceiver(mBroadcastReceiver, mIntentFilter);

    }


    private void setTypeFaces() {
        if (AppConstants.ENABLE_FONTS_TYPES) {
            textViewShowTime.setTypeface(AppHelper.setTypeFace(this, "Futura"));
            ResendBtn.setTypeface(AppHelper.setTypeFace(this, "Futura"));
            countryName.setTypeface(AppHelper.setTypeFace(this, "Futura"));
            currentMobileNumber.setTypeface(AppHelper.setTypeFace(this, "Futura"));
        }
    }

    public void getAdsInformation() {
        APIService mApiService = APIService.with(this);
        UsersContacts mUsersContacts = new UsersContacts(this, mApiService);
        mUsersContacts.getAdsInformation().subscribe(statusResponse -> {
            PreferenceManager.setUnitBannerAdsID(this, statusResponse.getMessage());
            PreferenceManager.setShowBannerAds(this, statusResponse.isSuccess());
        }, throwable -> {
            AppHelper.LogCat("Error get ads info MainActivity " + throwable.getMessage());
        });
    }

    public void getInterstitialAdInformation() {
        APIService mApiService = APIService.with(this);
        UsersContacts mUsersContacts = new UsersContacts(this, mApiService);
        mUsersContacts.getInterstitialAdInformation().subscribe(statusResponse -> {
            PreferenceManager.setUnitInterstitialAdID(this, statusResponse.getMessage());
            PreferenceManager.setShowInterstitialAds(this, statusResponse.isSuccess());
        }, throwable -> {
            AppHelper.LogCat("Error get ads info MainActivity " + throwable.getMessage());
        });
    }


    private void checkAppVersion() {
        Realm realm = DostChatApp.getRealmDatabaseInstance();
        APIService mApiService = APIService.with(this);
        UsersContacts mUsersContacts = new UsersContacts(realm, this, mApiService);
        mUsersContacts.getApplicationVersion().subscribe(versionResponse -> {

            int currentAppVersion;
            if (PreferenceManager.getVersionApp(DostChatApp.getInstance()) != 0) {
                currentAppVersion = PreferenceManager.getVersionApp(DostChatApp.getInstance());
            } else {
                currentAppVersion = AppHelper.getAppVersionCode(DostChatApp.getInstance());
            }
            if (currentAppVersion != 0 && currentAppVersion < versionResponse.getMessage()) {
                PreferenceManager.setVersionApp(this, currentAppVersion);
                PreferenceManager.setIsOutDate(this, true);
            } else {
                PreferenceManager.setIsOutDate(this, false);
            }

        }, throwable -> {
            AppHelper.LogCat("Error get app version info  WelcomeActivity " + throwable.getMessage());
        });
    }

    /**
     * method to initialize the view
     */
    private void initializerView() {
        hideKeyboard();
        checkAppVersion();
        if (getIntent().hasExtra(AccountManager.KEY_ACCOUNT_TYPE)) {
            mOldAccountType = getIntent().getStringExtra(AccountManager.KEY_ACCOUNT_TYPE);
            AppHelper.LogCat("IntentData is not null WelcomeActivity " + mOldAccountType);
            CreateSyncAccount();
        } else {
            AppHelper.LogCat("IntentData is null WelcomeActivity ");
            /**
             * Checking if user already connected
             */

            if (PreferenceManager.getToken(this) != null) {
                getAdsInformation();
                getInterstitialAdInformation();
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.mainfadein, R.anim.splashfadeout);
            }

        }

//        phoneNumberWrapper.setEnabled(false);
//        phoneNumberWrapper.setOnClickListener(view -> {
//            if (!phoneNumberWrapper.isEnabled()) {
//                AppHelper.CustomToast(this, getString(R.string.please_select_y_country));
//            }
//        });
//        btnNext.setEnabled(false);
        mCountries = CountriesFetcher.getCountries(this);

        phoneNumberWrapper.addTextChangedListener(mPhoneNumberWatcher);
        phoneNumberWrapper.setEnabled(true);
        numberPhoneLayoutSv.pageScroll(View.FOCUS_DOWN);
        btnNext.setEnabled(true);
        String codeIso1 = "IN";
        String countryName1 = "India";
        int defaultIdx = mCountries.indexOfIso(codeIso1);
        mSelectedCountry = mCountries.get(defaultIdx);
        this.countryName.setTextColor(AppHelper.getColor(this, R.color.colorBlack));
        this.countryName.setText(countryName1);
        mPhoneNumberWatcher = new PhoneNumberWatcher(codeIso1);
        setHint();
        btnNext.setOnClickListener(this);
        countryName.setTextColor(AppHelper.getColor(this, R.color.colorRedDark));
        countryName.setOnClickListener(this);
        btnVerifyOtp.setOnClickListener(this);
        ResendBtn.setOnClickListener(this);
        changeNumberBtn.setOnClickListener(this);
        ViewPagerAdapter adapter = new ViewPagerAdapter();
        viewPager.setAdapter(adapter);
        inputOtpWrapper.addTextChangedListener(new TextWatcherAdapter() {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 6) {
                    verificationOfCode();
                }
            }
        });
        /**
         * Checking if the device is waiting for sms
         * showing the user OTP screen
         */
        if (PreferenceManager.isWaitingForSms(this)) {
            viewPager.setCurrentItem(1);
            setTimer();
            resumeTimer();
        }

        if (viewPager.getCurrentItem() == 1) {
            setOnKeyboardCodeDone();

            if (PermissionHandler.checkPermission(this, Manifest.permission.RECEIVE_SMS)) {
                AppHelper.LogCat("RECEIVE SMS permission already granted.");
            } else {
                AppHelper.LogCat("Please request RECEIVE SMS permission.");
                PermissionHandler.requestPermission(this, Manifest.permission.RECEIVE_SMS);
            }
            if (PermissionHandler.checkPermission(this, Manifest.permission.READ_SMS)) {
                AppHelper.LogCat("READ SMS permission already granted.");
            } else {
                AppHelper.LogCat("Please request READ SMS permission.");
                PermissionHandler.requestPermission(this, Manifest.permission.READ_SMS);
            }

        } else {
            setOnKeyboardDone();
        }


    }

    /**
     * method to validate user information
     */
    private void validateInformation() {
        hideKeyboard();
        Phonenumber.PhoneNumber phoneNumber = getPhoneNumber();
        if (phoneNumber != null) {
            String phoneNumberFinal = mPhoneUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);
            if (isValid()) {
                String internationalFormat = phoneNumberFinal.replace("-", "");
                String finalResult = internationalFormat.replace(" ", "");
                PreferenceManager.setMobileNumber(this, finalResult);
                requestForSMS(finalResult, mSelectedCountry.getName());
            } else {
                phoneNumberWrapper.setError(getString(R.string.enter_a_val_number));
            }
        } else {
            phoneNumberWrapper.setError(getString(R.string.enter_a_val_number));
        }
    }

    /**
     * method to resend a request for SMS
     *
     * @param mobile this is parameter of ResendRequestForSMS method
     */
    private void ResendRequestForSMS(String mobile) {

        APIAuthentication mAPIAuthentication = APIService.RootService(APIAuthentication.class, EndPoints.BASE_URL);
        Call<JoinModel> ResendModelCall = mAPIAuthentication.resend(mobile);
        ResendModelCall.enqueue(new Callback<JoinModel>() {
            @Override
            public void onResponse(Call<JoinModel> call, Response<JoinModel> response) {
                if (response.isSuccessful()) {
                    if (response.body().isSuccess()) {
                        ResendBtn.setVisibility(View.GONE);
                        textViewShowTime.setVisibility(View.VISIBLE);
                        setTimer();
                        startTimer();
                        PreferenceManager.setIsWaitingForSms(WelcomeActivity.this, true);
                        viewPager.setCurrentItem(1);
                        currentMobileNumber.setText(PreferenceManager.getMobileNumber(WelcomeActivity.this));
                    } else {
                        AppHelper.CustomToast(WelcomeActivity.this, response.body().getMessage());
                    }
                } else {
                    AppHelper.CustomToast(WelcomeActivity.this, response.message());
                }
            }

            @Override
            public void onFailure(Call<JoinModel> call, Throwable t) {
                AppHelper.CustomToast(WelcomeActivity.this, getString(R.string.unexpected_reponse_from_server));
            }
        });
    }

    /**
     * method to send an SMS request to provider
     *
     * @param mobile  this the first parameter of  requestForSMS method
     * @param country this the second parameter of requestForSMS  method
     */
    private void requestForSMS(String mobile, String country) {
        APIAuthentication mAPIAuthentication = APIService.RootService(APIAuthentication.class, EndPoints.BASE_URL);
        Call<JoinModel> JoinModelCall = mAPIAuthentication.join(mobile, country);
        AppHelper.showDialog(this, getString(R.string.set_back_and_keep_calm_you_will_receive_an_sms_of_verification));
        JoinModelCall.enqueue(new Callback<JoinModel>() {
            @Override
            public void onResponse(Call<JoinModel> call, Response<JoinModel> response) {
                if (response.isSuccessful()) {
                    if (response.body().isSuccess()) {
                        AppHelper.hideDialog();
                        String accountType = AppConstants.ACCOUNT_TYPE;
                        AccountManager accountManager = AccountManager.get(WelcomeActivity.this);

                        // This is the magic that add the account to the Android Account Manager
                        final Account account = new Account(getResources().getString(R.string.app_name), accountType);
                        accountManager.addAccountExplicitly(account, PreferenceManager.getMobileNumber(WelcomeActivity.this), null);

                        // Now we tell our caller, could be the Android Account Manager or even our own application
                        // that the process was successful
                        final Intent intent = new Intent();
                        intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, getResources().getString(R.string.app_name));
                        intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, accountType);
                        intent.putExtra(AccountManager.KEY_AUTHTOKEN, accountType);
                        setAccountAuthenticatorResult(intent.getExtras());
                        setResult(RESULT_OK, intent);

                        if (!response.body().isSmsVerification()) {
                            PreferenceManager.setIsWaitingForSms(WelcomeActivity.this, false);
                            currentMobileNumber.setText(PreferenceManager.getMobileNumber(WelcomeActivity.this));
                            smsVerification(response.body().getCode());
                        } else {
                            setTimer();
                            startTimer();
                            PreferenceManager.setIsWaitingForSms(WelcomeActivity.this, true);
                            viewPager.setCurrentItem(1);
                            currentMobileNumber.setText(PreferenceManager.getMobileNumber(WelcomeActivity.this));
                        }

                    } else {
                        AppHelper.hideDialog();
                        AppHelper.CustomToast(WelcomeActivity.this, response.body().getMessage());
                    }
                } else {
                    AppHelper.hideDialog();
                    AppHelper.CustomToast(WelcomeActivity.this, response.message());
                }
            }

            @Override
            public void onFailure(Call<JoinModel> call, Throwable t) {
                AppHelper.hideDialog();
                AppHelper.LogCat("Failed to create your account " + t.getMessage());
                AppHelper.CustomToast(WelcomeActivity.this, getString(R.string.unexpected_reponse_from_server));
                hideKeyboard();
            }
        });

    }

    /**
     * this if you disabled verification by sms
     *
     * @param code
     */
    private void smsVerification(String code) {
        if (!code.isEmpty()) {
            Intent otpIntent = new Intent(getApplicationContext(), SMSVerificationService.class);
            otpIntent.putExtra("code", code);
            otpIntent.putExtra("register", true);
            startService(otpIntent);
        } else {
            AppHelper.CustomToast(WelcomeActivity.this, getString(R.string.please_enter_your_ver_code));
        }
    }

    /**
     * method to verify the code received by user then activating the user
     */
    private void verificationOfCode() {
        hideKeyboard();
        String code = inputOtpWrapper.getText().toString().trim();
        if (!code.isEmpty()) {
            Intent otpIntent = new Intent(getApplicationContext(), SMSVerificationService.class);
            otpIntent.putExtra("code", code);
            otpIntent.putExtra("register", true);
            startService(otpIntent);
        } else {
            AppHelper.CustomToast(WelcomeActivity.this, getString(R.string.please_enter_your_ver_code));
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_request_sms:
                validateInformation();
                break;
            case R.id.country_name:
                Intent mIntent = new Intent(this, CountryActivity.class);
                startActivityForResult(mIntent, AppConstants.SELECT_COUNTRY);
                break;

            case R.id.btn_verify_otp:
                verificationOfCode();
                break;

            case R.id.btn_change_number:
                viewPager.setCurrentItem(0);
                stopTimer();
                PreferenceManager.setID(this, 0);
                PreferenceManager.setToken(this, null);
                PreferenceManager.setMobileNumber(this, null);
                PreferenceManager.setIsWaitingForSms(this, false);
                break;

            case R.id.Resend:
                viewPager.setCurrentItem(1);
                ResendRequestForSMS(PreferenceManager.getMobileNumber(this));
                break;
        }
    }

    private class ViewPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == ((View) object);
        }

        public Object instantiateItem(View collection, int position) {

            int resId = 0;
            switch (position) {
                case 0:
                    resId = R.id.numberPhone_layout;
                    break;
                case 1:
                    resId = R.id.layout_verification;
                    break;
            }
            return findViewById(resId);
        }
    }

    private void setTimer() {
        int time = 1;
        totalTimeCountInMilliseconds = 60 * time * 1000;

    }

    private void startTimer() {
        countDownTimer = new WhatsCloneCounter(totalTimeCountInMilliseconds, 500).start();
    }

    public void stopTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    public void resumeTimer() {
        textViewShowTime.setVisibility(View.VISIBLE);
        countDownTimer = new WhatsCloneCounter(totalTimeCountInMilliseconds, 500).start();
    }


    public class WhatsCloneCounter extends CountDownTimer {

        WhatsCloneCounter(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long leftTimeInMilliseconds) {
            long seconds = leftTimeInMilliseconds / 1000;
            textViewShowTime.setText(String.format(Locale.getDefault(), "%02d", seconds / 60) + ":" + String.format(Locale.getDefault(), "%02d", seconds % 60));
        }

        @Override
        public void onFinish() {
            textViewShowTime.setVisibility(View.GONE);
            ResendBtn.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocalBroadcastManager.unregisterReceiver(mBroadcastReceiver);
    }


    /**
     * Create a new  account for the sync adapter
     */
    public Account CreateSyncAccount() {
        if (mOldAccountType != null) {
            // Create the account type and default account
            Account newAccount = new Account(getString(R.string.app_name), mOldAccountType);
            // Get an instance of the Android account manager
            AccountManager accountManager = (AccountManager) getSystemService(ACCOUNT_SERVICE);

            if (!accountManager.addAccountExplicitly(newAccount, null, null)) {
                AppHelper.CustomToast(this, getString(R.string.app_name) + getString(R.string.account_added_already));
                finish();

            }
            return newAccount;
        } else {
            return null;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == AppConstants.SELECT_COUNTRY) {
                phoneNumberWrapper.setEnabled(true);
                numberPhoneLayoutSv.pageScroll(View.FOCUS_DOWN);
                btnNext.setEnabled(true);
                String codeIso = data.getStringExtra("countryIso");
                String countryName = data.getStringExtra("countryName");
                int defaultIdx = mCountries.indexOfIso(codeIso);
                mSelectedCountry = mCountries.get(defaultIdx);
                this.countryName.setTextColor(AppHelper.getColor(this, R.color.colorBlack));
                this.countryName.setText(countryName);
                mPhoneNumberWatcher = new PhoneNumberWatcher(codeIso);
                setHint();
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * Hide keyboard from phoneEdit field
     */
    public void hideKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(phoneNumberWrapper.getWindowToken(), 0);
    }


    /**
     * Set hint number for country
     */
    private void setHint() {

        if (phoneNumberWrapper != null && mSelectedCountry != null && mSelectedCountry.getCode() != null) {
            Phonenumber.PhoneNumber phoneNumber = mPhoneUtil.getExampleNumberForType(mSelectedCountry.getCode(), PhoneNumberUtil.PhoneNumberType.MOBILE);
            if (phoneNumber != null) {
                String internationalNumber = mPhoneUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);
                phoneNumberWrapper.setHint(internationalNumber);
                int numberLength = internationalNumber.length();
                InputFilter[] fArray = new InputFilter[1];
                fArray[0] = new InputFilter.LengthFilter(numberLength);
                phoneNumberWrapper.setFilters(fArray);

            }
        }

    }


    /**
     * Get PhoneNumber object
     *
     * @return PhoneNumber | null on error
     */
    @SuppressWarnings("unused")
    public Phonenumber.PhoneNumber getPhoneNumber() {
        try {
            String iso = null;
            if (mSelectedCountry != null) {
                iso = mSelectedCountry.getCode();
            }
            return mPhoneUtil.parse(phoneNumberWrapper.getText().toString(), iso);
        } catch (NumberParseException ignored) {
            return null;
        }
    }


    /**
     * Check if number is valid
     *
     * @return boolean
     */
    @SuppressWarnings("unused")
    public boolean isValid() {
        Phonenumber.PhoneNumber phoneNumber = getPhoneNumber();
        return phoneNumber != null && mPhoneUtil.isValidNumber(phoneNumber);
    }

    public void setOnKeyboardDone() {
        phoneNumberWrapper.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                validateInformation();
            }
            return false;
        });
    }

    public void setOnKeyboardCodeDone() {
        inputOtpWrapper.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                verificationOfCode();
            }
            return false;
        });
    }


}
