package com.dostchat.dost.activities.settings;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.design.widget.TextInputEditText;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ScrollView;
import android.widget.TextView;

import com.dostchat.dost.app.DostChatApp;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.dostchat.dost.R;
import com.dostchat.dost.activities.CountryActivity;
import com.dostchat.dost.activities.main.WelcomeActivity;
import com.dostchat.dost.adapters.others.TextWatcherAdapter;
import com.dostchat.dost.api.APIService;
import com.dostchat.dost.api.apiServices.UsersContacts;
import com.dostchat.dost.app.AppConstants;
import com.dostchat.dost.helpers.AppHelper;
import com.dostchat.dost.helpers.CountriesFetcher;
import com.dostchat.dost.helpers.Files.backup.RealmBackupRestore;
import com.dostchat.dost.helpers.PermissionHandler;
import com.dostchat.dost.helpers.PhoneNumberWatcher;
import com.dostchat.dost.helpers.PreferenceManager;
import com.dostchat.dost.helpers.notifications.NotificationsManager;
import com.dostchat.dost.models.CountriesModel;
import com.dostchat.dost.services.SMSVerificationService;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;

import static com.dostchat.dost.app.AppConstants.ACCOUNT_TYPE;

/**
 * Created by Abderrahim El imame on 09/02/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class DeleteAccountActivity extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.delete_account_btn)
    TextView btnNext;
    @BindView(R.id.numberPhone)
    TextInputEditText phoneNumberWrapper;
    @BindView(R.id.inputOtpWrapper)
    TextInputEditText inputOtpWrapper;
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

    @BindView(R.id.app_bar)
    Toolbar toolbar;

    private CountDownTimer countDownTimer;
    private long totalTimeCountInMilliseconds;

    private final String DEFAULT_COUNTRY = Locale.getDefault().getCountry();
    private PhoneNumberWatcher mPhoneNumberWatcher = new PhoneNumberWatcher(DEFAULT_COUNTRY);
    private PhoneNumberUtil mPhoneUtil = PhoneNumberUtil.getInstance();
    private CountriesModel mSelectedCountry;
    private CountriesFetcher.CountryList mCountries;
    private Realm realm;
    private UsersContacts mUsersContactsDelete;
    LocalBroadcastManager mLocalBroadcastManager;
    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(getPackageName() + "closeDeleteAccountActivity")) {
                AppHelper.showDialog(DeleteAccountActivity.this, getString(R.string.deleting));
                new Handler().postDelayed(() -> {
                    AppHelper.hideDialog();
                    PreferenceManager.setToken(DeleteAccountActivity.this, null);
                    PreferenceManager.setID(DeleteAccountActivity.this, 0);
                    PreferenceManager.setSocketID(DeleteAccountActivity.this, null);
                    PreferenceManager.setPhone(DeleteAccountActivity.this, null);
                    PreferenceManager.setIsWaitingForSms(DeleteAccountActivity.this, false);
                    PreferenceManager.setMobileNumber(DeleteAccountActivity.this, null);
                    PreferenceManager.setLastBackup(DeleteAccountActivity.this, null);
                    AccountLogout();
                    RealmBackupRestore.deleteData(DeleteAccountActivity.this);
                    NotificationsManager.SetupBadger(DeleteAccountActivity.this);
                    Intent mIntent1 = new Intent(DeleteAccountActivity.this, WelcomeActivity.class);
                    mIntent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(mIntent1);
                    finish();
                }, 1000);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_account);
        ButterKnife.bind(this);
        realm = DostChatApp.getRealmDatabaseInstance();
        initializerView();
        APIService mApiServiceDelete = APIService.with(this);
        mUsersContactsDelete = new UsersContacts(realm, this, mApiServiceDelete);
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(getPackageName() + "closeDeleteAccountActivity");
        mLocalBroadcastManager.registerReceiver(mBroadcastReceiver, mIntentFilter);
    }


    /**
     * method to initialize the view
     */
    private void initializerView() {
        hideKeyboard();
        phoneNumberWrapper.setEnabled(false);
        phoneNumberWrapper.setOnClickListener(view -> {
            if (!phoneNumberWrapper.isEnabled()) {
                AppHelper.CustomToast(this, getString(R.string.please_select_y_country));
            }
        });
        btnNext.setEnabled(false);
        mCountries = CountriesFetcher.getCountries(this);
        phoneNumberWrapper.addTextChangedListener(mPhoneNumberWatcher);
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
        setupToolbar();

    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
     * method to send an SMS request to provider
     *
     * @param mobile this the first parameter of  requestForSMS method
     */
    private void requestForSMS(String mobile, String country) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.alert_message_delete_account);
        builder.setPositiveButton(R.string.Yes, (dialog, whichButton) -> {
            AppHelper.showDialog(this, getString(R.string.set_back_and_keep_calm_you_will_receive_an_sms_of_verification));
            mUsersContactsDelete.deleteAccount(mobile, country).subscribe(response -> {
                if (response.isSuccess()) {
                    AppHelper.hideDialog();
                    if (!response.isSmsVerification()) {
                        smsVerification(response.getCode());
                    } else {
                        setTimer();
                        startTimer();
                        viewPager.setCurrentItem(1);
                    }
                } else {
                    AppHelper.hideDialog();
                    AppHelper.CustomToast(DeleteAccountActivity.this, response.getMessage());
                }
            }, throwable -> {
                AppHelper.hideDialog();
                hideKeyboard();
                AppHelper.LogCat("delete  account " + throwable.getMessage());
                AppHelper.CustomToast(DeleteAccountActivity.this, getString(R.string.delete_account_failed_please_try_later));
            });
        });
        builder.setNegativeButton(R.string.No, (dialog, whichButton) -> {

        });

        builder.show();
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
            otpIntent.putExtra("register", false);
            startService(otpIntent);
        } else {
            AppHelper.CustomToast(this, getString(R.string.please_enter_your_ver_code));
        }
    }

    /**
     * method to verify the code received by user then activating the user
     */

    private void verificationOfCode() {
        String code = inputOtpWrapper.getText().toString().trim();
        if (!code.isEmpty()) {
            Intent otpIntent = new Intent(getApplicationContext(), SMSVerificationService.class);
            otpIntent.putExtra("code", code);
            otpIntent.putExtra("register", false);
            startService(otpIntent);
        } else {
            AppHelper.CustomToast(DeleteAccountActivity.this, getString(R.string.please_enter_your_ver_code));
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.delete_account_btn:
                validateInformation();
                break;

            case R.id.btn_verify_otp:
                verificationOfCode();
                break;
            case R.id.country_name:
                Intent mIntent = new Intent(this, CountryActivity.class);
                startActivityForResult(mIntent, AppConstants.SELECT_COUNTRY);
                break;
            case R.id.btn_change_number:
                PreferenceManager.setMobileNumber(this, null);
                viewPager.setCurrentItem(0);
                stopTimer();
                break;


        }
    }

    class ViewPagerAdapter extends PagerAdapter {

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
        realm.close();
    }


    private void AccountLogout() {

        Account mAccount;
        AccountManager accountManager = AccountManager.get(this);
        if (PermissionHandler.checkPermission(this, Manifest.permission.GET_ACCOUNTS)) {
            AppHelper.LogCat("GET ACCOUNTS  permission already granted.");
        } else {
            AppHelper.LogCat("Please request GET ACCOUNTS permission.");
            PermissionHandler.requestPermission(this, Manifest.permission.GET_ACCOUNTS);
        }
        Account[] accounts = accountManager.getAccountsByType(ACCOUNT_TYPE);
        if (accounts.length > 0) {
            mAccount = accounts[0];
            accountManager.clearPassword(mAccount);
            if (Build.VERSION.SDK_INT < 23) {
                accountManager.removeAccount(mAccount, null, null);
            } else {
                accountManager.removeAccount(mAccount, this, null, null);
            }
        } else {
            AppHelper.LogCat("there is no account here you have to logout ");
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
