package com.dostchat.dost.activities.main;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.dostchat.dost.R;
import com.dostchat.dost.activities.NewConversationContactsActivity;
import com.dostchat.dost.activities.groups.AddMembersToGroupActivity;
import com.dostchat.dost.activities.messages.TransferMessageContactsActivity;
import com.dostchat.dost.activities.recharge.AddMoneyActivity;
import com.dostchat.dost.activities.recharge.DCActivity;
import com.dostchat.dost.activities.recharge.DTHActivity;
import com.dostchat.dost.activities.recharge.MobileRechargeActivity;
import com.dostchat.dost.activities.recharge.WalletActivity;
import com.dostchat.dost.activities.search.SearchCallsActivity;
import com.dostchat.dost.activities.search.SearchContactsActivity;
import com.dostchat.dost.activities.search.SearchConversationsActivity;
import com.dostchat.dost.activities.settings.SettingsActivity;
import com.dostchat.dost.activities.status.StatusActivity;
import com.dostchat.dost.adapters.others.HomeTabsAdapter;
import com.dostchat.dost.api.APIHelper;
import com.dostchat.dost.app.AppConstants;
import com.dostchat.dost.app.EndPoints;
import com.dostchat.dost.app.FetchWalletAmount;
import com.dostchat.dost.app.UserPref;
import com.dostchat.dost.app.DostChatApp;
import com.dostchat.dost.fragments.home.CameraFragment;
import com.dostchat.dost.helpers.AppHelper;
import com.dostchat.dost.helpers.Files.cache.ImageLoader;
import com.dostchat.dost.helpers.Files.cache.MemoryCache;
import com.dostchat.dost.helpers.ForegroundRuning;
import com.dostchat.dost.helpers.OutDateHelper;
import com.dostchat.dost.helpers.PermissionHandler;
import com.dostchat.dost.helpers.PreferenceManager;
import com.dostchat.dost.helpers.RateHelper;
import com.dostchat.dost.helpers.images.DostChatImageLoader;
import com.dostchat.dost.helpers.notifications.NotificationsManager;
import com.dostchat.dost.interfaces.NetworkListener;
import com.dostchat.dost.models.calls.CallsInfoModel;
import com.dostchat.dost.models.calls.CallsModel;
import com.dostchat.dost.models.messages.ConversationsModel;
import com.dostchat.dost.models.users.Pusher;
import com.dostchat.dost.models.users.contacts.ContactsModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import io.realm.Realm;
import io.realm.RealmResults;
import io.socket.client.Socket;

import static com.dostchat.dost.app.AppConstants.ACCOUNT_TYPE;
import static com.dostchat.dost.app.AppConstants.EVENT_BUS_MESSAGE_COUNTER;
import static com.dostchat.dost.app.AppConstants.EVENT_BUS_NEW_USER_JOINED;
import static com.dostchat.dost.app.AppConstants.EVENT_BUS_START_REFRESH;
import static com.dostchat.dost.app.AppConstants.EVENT_BUS_STOP_REFRESH;

/**
 * Created by Abderrahim El imame on 01/03/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class MainActivity extends AppCompatActivity implements NetworkListener, NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    @BindView(R.id.viewpager)
    ViewPager viewPager;
    @BindView(R.id.app_bar_main)
    View mView;
    @BindView(R.id.adParentLyout)
    LinearLayout adParentLyout;
    @BindView(R.id.tabs)
    TabLayout tabLayout;
    @BindView(R.id.app_bar)
    Toolbar toolbar;
    @BindView(R.id.main_view)
    LinearLayout MainView;
    @BindView(R.id.toolbar_progress_bar)
    ProgressBar toolbarProgressBar;

    @BindView(R.id.floatingBtnMain)
    FloatingActionButton floatingBtnMain;

    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawer;

    @BindView(R.id.iv_profile)
    ImageView iv_profile;

    ImageView iv_nav;

    InterstitialAd mInterstitialAd;
    private HomeTabsAdapter homeTabsAdapter;
    int REQUEST_CHECK_SETTINGS_GPS = 1;

    // Sync interval constants
    public static final long SECONDS_PER_MINUTE = 60L;
    public static final long SYNC_INTERVAL_IN_MINUTES = 60L;//
    public static final long SYNC_INTERVAL = SYNC_INTERVAL_IN_MINUTES * SECONDS_PER_MINUTE;// 3600L
    private Account mAccount;

    private Socket mSocket;

    private UserPref mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DostChatApp.getInstance().startTrackingLocation(this);


        ButterKnife.bind(this);
        Permissions();
        initializerView();
        setupToolbar();
        setTypeFaces();
        EventBus.getDefault().register(this);
        checkIfUserSession();
        loadCounter();
        NotificationsManager.SetupBadger(this);
        if (PreferenceManager.ShowInterstitialrAds(this)) {
            if (PreferenceManager.getUnitInterstitialAdID(this) != null) {
                initializerAds();
            }
        }

        mUser = new UserPref(this);

//        setupAccountInstance();
        startPeriodicSync();
        initRequestSync();
        showMainAds();
        RateHelper.appLaunched(this);
        connectToServer();

        setupuserforrecharge();
        FetchWalletAmount.getAmount(this);
    }

    public String getTimePeriod() {
        Calendar c = Calendar.getInstance();
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);

        if (timeOfDay >= 0 && timeOfDay < 12) {
            return "Good Morning";
        } else if (timeOfDay >= 12 && timeOfDay < 16) {
            return "Good Afternoon";
        } else if (timeOfDay >= 16 && timeOfDay < 21) {
            return "Good Evening";
        } else if (timeOfDay >= 21 && timeOfDay < 24) {
            return "Good Night";
        }
        return "";
    }


    private void setupuserforrecharge() {

        String number = PreferenceManager.getPhone(this);
        if (number != null) {

            String url = AppConstants.LOGIN_URL + number;

            StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    System.out.println(response);

                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String status = jsonObject.getString("status");
                        if (status.equals("ok")) {
                            String userid = jsonObject.getString("data");
                            System.out.println("Output : " + userid);
                            mUser.setUserId(userid);
                        } else if (status.equals("ko")) {

                        }

                    } catch (Exception ignored) {

                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                }
            });
            DostChatApp.getInstance().addToRequestQueue(request);

        }


    }

    private void setTypeFaces() {
        if (AppConstants.ENABLE_FONTS_TYPES) {
            ((TextView) findViewById(R.id.title_tabs_contacts)).setTypeface(AppHelper.setTypeFace(this, "Futura"));
            ((TextView) findViewById(R.id.title_tabs_messages)).setTypeface(AppHelper.setTypeFace(this, "Futura"));
            ((TextView) findViewById(R.id.title_tabs_calls)).setTypeface(AppHelper.setTypeFace(this, "Futura"));
        }
    }

    private void connectToServer() {

        DostChatApp app = (DostChatApp) getApplication();
        mSocket = app.getSocket();
        if (mSocket == null) {
            DostChatApp.connectSocket();
            mSocket = app.getSocket();
        }
        if (mSocket != null) {
            if (!mSocket.connected())
                mSocket.connect();

            JSONObject json = new JSONObject();
            try {
                json.put("connected", true);
                json.put("senderId", PreferenceManager.getID(this));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            mSocket.emit(AppConstants.SOCKET_IS_ONLINE, json);
        }
    }

    /**
     * method to setup your account
     */
    private void setupAccountInstance() {
        AccountManager manager = AccountManager.get(this);
        if (PermissionHandler.checkPermission(this, Manifest.permission.GET_ACCOUNTS)) {
            AppHelper.LogCat("GET ACCOUNTS  permission already granted.");
        } else {
            AppHelper.LogCat("Please request GET ACCOUNTS permission.");
            PermissionHandler.requestPermission(this, Manifest.permission.GET_ACCOUNTS);
        }
        Account[] accounts = manager.getAccountsByType(ACCOUNT_TYPE);
        if (accounts.length > 0) {
            mAccount = accounts[0];
        } else {
            mAccount = null;
            PreferenceManager.setToken(this, null);
            PreferenceManager.setID(this, 0);
            PreferenceManager.setSocketID(this, null);
            PreferenceManager.setPhone(this, null);
            PreferenceManager.setIsWaitingForSms(this, false);
            Intent intent = new Intent(this, WelcomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            AppHelper.LogCat("there is no account here you have to logout ");

        }
    }

    /**
     * method to start synchronization
     */
    private void startPeriodicSync() {
        if (mAccount != null) {
            ContentResolver.setIsSyncable(mAccount, ContactsContract.AUTHORITY, 1);
            ContentResolver.setSyncAutomatically(mAccount, ContactsContract.AUTHORITY, true);
            ContentResolver.addPeriodicSync(mAccount, ContactsContract.AUTHORITY, Bundle.EMPTY, SYNC_INTERVAL);

        }
    }

    /**
     * method to start a new RequestSync
     */
    public void initRequestSync() {
        // Pass the settings flags by inserting them in a bundle
        Bundle settingsBundle = new Bundle();
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        ContentResolver.requestSync(mAccount, ContactsContract.AUTHORITY, settingsBundle);
    }

    private void initializerAds() {
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(PreferenceManager.getUnitInterstitialAdID(this));
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitial();
                AppHelper.LaunchActivity(MainActivity.this, SettingsActivity.class);
            }
        });

        requestNewInterstitial();
    }


    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder().build();
        mInterstitialAd.loadAd(adRequest);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search_conversations:
                RateHelper.significantEvent(this);
                AppHelper.LaunchActivity(this, SearchConversationsActivity.class);
                break;
            case R.id.search_contacts:
                AppHelper.LaunchActivity(this, SearchContactsActivity.class);
                break;
            case R.id.search_calls:
                AppHelper.LaunchActivity(this, SearchCallsActivity.class);
                break;
            case R.id.new_group:
                RateHelper.significantEvent(this);
                PreferenceManager.clearMembers(this);
                AppHelper.LaunchActivity(this, AddMembersToGroupActivity.class);
                break;
            case R.id.settings:
                RateHelper.significantEvent(this);
                if (PreferenceManager.ShowInterstitialrAds(this)) {
                    if (mInterstitialAd.isLoaded()) {
                        mInterstitialAd.show();
                    } else {
                        AppHelper.LaunchActivity(this, SettingsActivity.class);
                    }
                } else {
                    AppHelper.LaunchActivity(this, SettingsActivity.class);
                }
                break;
            case R.id.status:
                RateHelper.significantEvent(this);
                AppHelper.LaunchActivity(this, StatusActivity.class);
                break;

            case R.id.clear_log_call:
                RateHelper.significantEvent(this);
                removeCallsLog();
                break;
            case R.id.aspect_ratio:
                Fragment fragment = homeTabsAdapter.getCameraFragment();

                if (fragment != null) {
                    CameraFragment cameraFragment = (CameraFragment) fragment;
                    cameraFragment.setAspectRatio();
                }
                return true;
            case R.id.switch_flash:
                Fragment flashFragment = homeTabsAdapter.getCameraFragment();

                if (flashFragment != null) {
                    CameraFragment cameraFragment = (CameraFragment) flashFragment;
                    cameraFragment.setFlash(item);
                }
                return true;
            case R.id.switch_camera:
                Fragment switchFragment = homeTabsAdapter.getCameraFragment();

                if (switchFragment != null) {
                    CameraFragment cameraFragment = (CameraFragment) switchFragment;
                    cameraFragment.switchCamera();
                }
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    private void removeCallsLog() {
        Realm realm = DostChatApp.getRealmDatabaseInstance();
        AppHelper.showDialog(this, getString(R.string.delete_call_dialog));
        realm.executeTransactionAsync(realm1 -> {
            RealmResults<CallsModel> callsModels = realm1.where(CallsModel.class).findAll();
            for (CallsModel callsModel : callsModels) {
                RealmResults<CallsInfoModel> callsInfoModel = realm1.where(CallsInfoModel.class).equalTo("callId", callsModel.getId()).findAll();
                callsInfoModel.deleteAllFromRealm();
            }
            callsModels.deleteAllFromRealm();
        }, () -> {
            AppHelper.hideDialog();
            EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_DELETE_CALL_ITEM, 0));
        }, error -> {
            AppHelper.LogCat(error.getMessage());
            AppHelper.hideDialog();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        switch (tabLayout.getSelectedTabPosition()) {
            case 0:
                getMenuInflater().inflate(R.menu.menu_camera, menu);
                break;
            case 1:
                getMenuInflater().inflate(R.menu.calls_menu, menu);
                break;
            case 2:
                getMenuInflater().inflate(R.menu.conversations_menu, menu);
                break;
            case 3:
                getMenuInflater().inflate(R.menu.contacts_menu, menu);
                break;

        }
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * method to setup toolbar
     */
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setHomeButtonEnabled(false);
        }
    }

    /**
     * method to initialize the view
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void initializerView() {

        if (PreferenceManager.isOutDate(this)) {
            OutDateHelper.appLaunched(this);
            OutDateHelper.significantEvent(this);
        }

        homeTabsAdapter = new HomeTabsAdapter(getSupportFragmentManager());
        viewPager.setAdapter(homeTabsAdapter);
        viewPager.setOffscreenPageLimit(3);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        viewPager.setCurrentItem(2);
        tabLayout.getTabAt(0).setCustomView(R.layout.custom_tab_camera);
        tabLayout.getTabAt(1).setCustomView(R.layout.custom_tab_calls);
        tabLayout.getTabAt(2).setCustomView(R.layout.custom_tab_messages);
        tabLayout.getTabAt(3).setCustomView(R.layout.custom_tab_contacts);
        ((TextView) findViewById(R.id.title_tabs_contacts)).setTextColor(AppHelper.getColor(this, R.color.colorUnSelected));
        ((TextView) findViewById(R.id.title_tabs_calls)).setTextColor(AppHelper.getColor(this, R.color.colorUnSelected));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Drawable icon = AppHelper.getVectorDrawable(MainActivity.this, R.drawable.ic_chat_white_24dp);
                switch (tab.getPosition()) {
                    case 0:
                        icon = AppHelper.getVectorDrawable(MainActivity.this, R.drawable.ic_call_white_24dp);
                        viewPager.setCurrentItem(0);
                        findViewById(R.id.counterTabMessages).setBackground(AppHelper.getDrawable(MainActivity.this, R.drawable.bg_circle_tab_counter_unselected));
                        findViewById(R.id.counterTabCalls).setBackground(AppHelper.getDrawable(MainActivity.this, R.drawable.bg_circle_tab_counter));
                        ((TextView) findViewById(R.id.title_tabs_calls)).setTextColor(AppHelper.getColor(MainActivity.this, R.color.colorWhite));
                        break;
                    case 1:
                        icon = AppHelper.getVectorDrawable(MainActivity.this, R.drawable.ic_chat_white_24dp);
                        viewPager.setCurrentItem(1);
                        findViewById(R.id.counterTabMessages).setBackground(AppHelper.getDrawable(MainActivity.this, R.drawable.bg_circle_tab_counter));
                        findViewById(R.id.counterTabCalls).setBackground(AppHelper.getDrawable(MainActivity.this, R.drawable.bg_circle_tab_counter_unselected));
                        ((TextView) findViewById(R.id.title_tabs_messages)).setTextColor(AppHelper.getColor(MainActivity.this, R.color.colorWhite));
                        break;
                    case 2:
                        icon = AppHelper.getVectorDrawable(MainActivity.this, R.drawable.ic_person_add_24dp);
                        viewPager.setCurrentItem(2);
                        findViewById(R.id.counterTabMessages).setBackground(AppHelper.getDrawable(MainActivity.this, R.drawable.bg_circle_tab_counter_unselected));
                        findViewById(R.id.counterTabCalls).setBackground(AppHelper.getDrawable(MainActivity.this, R.drawable.bg_circle_tab_counter_unselected));
                        ((TextView) findViewById(R.id.title_tabs_contacts)).setTextColor(AppHelper.getColor(MainActivity.this, R.color.colorWhite));
                        break;
                    default:
                        break;
                }
                // floatingBtnMain.setImageDrawable(icon);
                final Animation animation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.scale_for_button_animtion_enter);
                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        floatingBtnMain.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                //floatingBtnMain.startAnimation(animation);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        findViewById(R.id.counterTabCalls).setBackground(AppHelper.getDrawable(MainActivity.this, R.drawable.bg_circle_tab_counter_unselected));
                        ((TextView) findViewById(R.id.title_tabs_calls)).setTextColor(AppHelper.getColor(MainActivity.this, R.color.colorUnSelected));
                        break;
                    case 1:
                        findViewById(R.id.counterTabMessages).setBackground(AppHelper.getDrawable(MainActivity.this, R.drawable.bg_circle_tab_counter_unselected));
                        ((TextView) findViewById(R.id.title_tabs_messages)).setTextColor(AppHelper.getColor(MainActivity.this, R.color.colorUnSelected));
                        break;
                    case 2:
                        findViewById(R.id.counterTabMessages).setBackground(getResources().getDrawable(R.drawable.bg_circle_tab_counter_unselected));
                        findViewById(R.id.counterTabCalls).setBackground(getResources().getDrawable(R.drawable.bg_circle_tab_counter_unselected));
                        ((TextView) findViewById(R.id.title_tabs_contacts)).setTextColor(AppHelper.getColor(MainActivity.this, R.color.colorUnSelected));
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {


            }
        });

        floatingBtnMain.setOnClickListener(view -> {
            switch (tabLayout.getSelectedTabPosition()) {
                case 0:
                    RateHelper.significantEvent(this);
                    Intent intent = new Intent(this, TransferMessageContactsActivity.class);
                    intent.putExtra("forCall", true);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    break;
                case 1:
                    RateHelper.significantEvent(this);
                    AppHelper.LaunchActivity(this, NewConversationContactsActivity.class);
                    break;
                case 2:
                    RateHelper.significantEvent(this);
                    try {
                        Intent mIntent = new Intent(Intent.ACTION_INSERT);
                        mIntent.setType(ContactsContract.Contacts.CONTENT_TYPE);
                        mIntent.putExtra(ContactsContract.Intents.Insert.PHONE, "");
                        startActivityForResult(mIntent, 50);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }
        });

        /*findViewById(R.id.rl_chat).setOnClickListener(this);
        findViewById(R.id.rl_sos).setOnClickListener(this);
        findViewById(R.id.rl_wallet).setOnClickListener(this);
        findViewById(R.id.rl_settings).setOnClickListener(this);*/

        mDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        iv_profile.setOnClickListener(this);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        View headerView = navigationView.getHeaderView(0);
        TextView header_title = (TextView) headerView.findViewById(R.id.header_title);
        iv_nav = (ImageView) headerView.findViewById(R.id.iv_nav);

        ContactsModel contactsModel = DostChatApp.getRealmDatabaseInstance().where(ContactsModel.class).equalTo("id", PreferenceManager.getID(this)).findFirst();

        if (contactsModel != null) {
            if (contactsModel.getUsername() == null) {
                header_title.setText("Welcome");
            } else {
                header_title.setText("Welcome " + contactsModel.getUsername());
            }

            DostChatApp.speek(header_title.getText() + " " + getTimePeriod());
        }

        navigationView.setNavigationItemSelectedListener(this);

        if (contactsModel != null) {
            MemoryCache memoryCache = new MemoryCache();
            Bitmap bitmap = ImageLoader.GetCachedBitmapImage(memoryCache, contactsModel.getImage(), this, contactsModel.getId(), AppConstants.USER, AppConstants.SETTINGS_PROFILE);
            if (bitmap != null) {
                ImageLoader.SetBitmapImage(bitmap, iv_profile);
                ImageLoader.SetBitmapImage(bitmap, iv_nav);
            } else {
                BitmapImageViewTarget target = new BitmapImageViewTarget(iv_profile) {
                    @Override
                    public void onResourceReady(final Bitmap bitmap, GlideAnimation anim) {
                        super.onResourceReady(bitmap, anim);
                        iv_profile.setImageBitmap(bitmap);
                        iv_nav.setImageBitmap(bitmap);
                        ImageLoader.DownloadImage(memoryCache, EndPoints.SETTINGS_IMAGE_URL + contactsModel.getImage(), contactsModel.getImage(), MainActivity.this, contactsModel.getId(), AppConstants.USER, AppConstants.SETTINGS_PROFILE);
                        ImageLoader.DownloadImage(memoryCache, EndPoints.EDIT_PROFILE_IMAGE_URL + contactsModel.getImage(), contactsModel.getImage(), MainActivity.this, contactsModel.getId(), AppConstants.USER, AppConstants.EDIT_PROFILE);

                    }

                    @Override
                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
                        super.onLoadFailed(e, errorDrawable);

                    }

                    @Override
                    public void onLoadStarted(Drawable placeholder) {
                        super.onLoadStarted(placeholder);

                    }
                };
                DostChatImageLoader.loadCircleImage(this, EndPoints.SETTINGS_IMAGE_URL + contactsModel.getImage(), target, R.drawable.image_holder_ur_circle, AppConstants.SETTINGS_IMAGE_SIZE);

            }
        }
    }


    private void showMainAds() {
        if (PreferenceManager.ShowBannerAds(this)) {
            adParentLyout.setVisibility(View.VISIBLE);
            if (PreferenceManager.getUnitBannerAdsID(this) != null) {
                AdView mAdView = new AdView(this);
                mAdView.setAdSize(AdSize.BANNER);
                mAdView.setAdUnitId(PreferenceManager.getUnitBannerAdsID(this));
                AdRequest adRequest = new AdRequest.Builder()
                        .build();
                if (mAdView.getAdSize() != null || mAdView.getAdUnitId() != null)
                    mAdView.loadAd(adRequest);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                adParentLyout.addView(mAdView, params);
            }
        } else {
            adParentLyout.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        MainView.setVisibility(View.GONE);
        DostChatApp.getInstance().setConnectivityListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MainView.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onStop() {
        super.onStop();


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);

        DisConnectFromServer();
    }


    private void Permissions() {
        if (PermissionHandler.checkPermission(this, Manifest.permission.READ_CONTACTS)) {
            AppHelper.LogCat("Read contact data permission already granted.");
        } else {
            AppHelper.LogCat("Please request Read contact data permission.");
            AppHelper.showPermissionDialog(this);
            PermissionHandler.requestPermission(this, Manifest.permission.READ_CONTACTS);
        }
        if (PermissionHandler.checkPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            AppHelper.LogCat("Read storage data permission already granted.");
        } else {
            AppHelper.LogCat("Please request Read storage data permission.");
            PermissionHandler.requestPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == AppConstants.CONTACTS_PERMISSION_REQUEST_CODE) {
            AppHelper.hidePermissionsDialog();
            EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_CONTACTS_PERMISSION));
        }


        if (requestCode == REQUEST_CHECK_SETTINGS_GPS) {
            if (resultCode == Activity.RESULT_OK) {
                DostChatApp.locationUpdateManager.startLocation(this);
            } else if (resultCode == Activity.RESULT_CANCELED) {
                DostChatApp.locationUpdateManager.ennableLocation();
            }
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == AppConstants.CONTACTS_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                AppHelper.hidePermissionsDialog();
                EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_CONTACTS_PERMISSION));
            }
        }
    }

    /**
     * method of EventBus
     *
     * @param pusher this is parameter of onEventMainThread method
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @SuppressWarnings("unused")
    public void onEventMainThread(Pusher pusher) {
        switch (pusher.getAction()) {
            case EVENT_BUS_START_REFRESH:
                toolbarProgressBar.setVisibility(View.VISIBLE);
                toolbarProgressBar.getIndeterminateDrawable().setColorFilter(AppHelper.getColor(this, R.color.colorWhite), PorterDuff.Mode.SRC_IN);
                break;
            case EVENT_BUS_STOP_REFRESH:
                toolbarProgressBar.setVisibility(View.GONE);
                break;
            case EVENT_BUS_MESSAGE_COUNTER:
                new Handler().postDelayed(this::loadCounter, 500);
                break;
            case EVENT_BUS_NEW_USER_JOINED:
                setupAccountInstance();
                startPeriodicSync();
                initRequestSync();
                JSONObject jsonObject = pusher.getJsonObject();
                try {
                    String phone = jsonObject.getString("phone");
                    int senderId = jsonObject.getInt("senderId");
                    new Handler().postDelayed(() -> {
                        Intent mIntent = new Intent("new_user_joined_notification_whatsclone");
                        mIntent.putExtra("conversationID", 0);
                        mIntent.putExtra("recipientID", senderId);
                        mIntent.putExtra("phone", phone);
                        mIntent.putExtra("message", AppConstants.JOINED_MESSAGE_SMS);
                        sendBroadcast(mIntent);
                    }, 2500);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case AppConstants.EVENT_BUS_NEW_CONTACT_ADDED:
                setupAccountInstance();
                startPeriodicSync();
                initRequestSync();
                break;
            case AppConstants.EVENT_BUS_START_CONVERSATION:
                if (viewPager.getCurrentItem() == 3)
                    viewPager.setCurrentItem(1);
                break;
            case AppConstants.EVENT_BUS_ACTION_MODE_STARTED:
                tabLayout.setBackgroundColor(AppHelper.getColor(this, R.color.colorAccent));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Window window = getWindow();
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                    window.setStatusBarColor(AppHelper.getColor(this, R.color.colorAccent));
                }
                break;
            case AppConstants.EVENT_BUS_ACTION_MODE_DESTORYED:
                tabLayout.setBackgroundColor(AppHelper.getColor(this, R.color.colorPrimary));
                if (AppHelper.isAndroid5()) {
                    Window window = getWindow();
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                    window.setStatusBarColor(AppHelper.getColor(this, R.color.colorPrimaryDark));
                }
                break;

        }


    }

    /**
     * Callback will be triggered when there is change in
     * network connection
     */
    @Override
    public void onNetworkConnectionChanged(boolean isConnecting, boolean isConnected) {
        if (!isConnecting && !isConnected) {
            AppHelper.Snackbar(this, mView, getString(R.string.connection_is_not_available), AppConstants.MESSAGE_COLOR_ERROR, AppConstants.TEXT_COLOR);
        } else if (isConnecting && isConnected) {
            AppHelper.Snackbar(this, mView, getString(R.string.connection_is_available), AppConstants.MESSAGE_COLOR_SUCCESS, AppConstants.TEXT_COLOR);
        } else {
            AppHelper.Snackbar(this, mView, getString(R.string.waiting_for_network), AppConstants.MESSAGE_COLOR_WARNING, AppConstants.TEXT_COLOR);

        }
    }


    /**
     * methdo to loadCircleImage number of unread messages
     */
    private void loadCounter() {
        int messageCounter = 0;
        try {
            Realm realm = DostChatApp.getRealmDatabaseInstance();
            List<ConversationsModel> conversationsModel1 = realm.where(ConversationsModel.class)
                    .notEqualTo("UnreadMessageCounter", "0")
                    .findAll();
            if (conversationsModel1.size() != 0) {
                messageCounter = conversationsModel1.size();
            }

            if (messageCounter == 0) {
                findViewById(R.id.counterTabMessages).setVisibility(View.GONE);
            } else {
                findViewById(R.id.counterTabMessages).setVisibility(View.VISIBLE);
                ((TextView) findViewById(R.id.counterTabMessages)).setText(String.valueOf(messageCounter));
            }
            if (!realm.isClosed())
                realm.close();

        } catch (Exception e) {
            AppHelper.LogCat("loadCounter main activity " + e.getMessage());
        }


    }

    /**
     * method to disconnect from socket server
     */
    private void DisConnectFromServer() {

        try {
            JSONObject json = new JSONObject();
            try {
                json.put("connected", false);
                json.put("senderId", PreferenceManager.getID(this));

            } catch (JSONException e) {
                e.printStackTrace();
            }
            mSocket.emit(AppConstants.SOCKET_IS_ONLINE, json);
        } catch (Exception e) {
            AppHelper.LogCat("User is offline  Exception MainActivity" + e.getMessage());
        }
    }


    /**
     * method to check if user connect in an other device
     */
    public void checkIfUserSession() {
        APIHelper.initialApiUsersContacts().checkIfUserSession().subscribe(networkModel -> {
            if (!networkModel.isConnected()) {
                if (ForegroundRuning.get().isForeground()) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                    alert.setMessage(R.string.your_session_expired);
                    alert.setPositiveButton(R.string.ok, (dialog, which) -> {
                        PreferenceManager.setToken(MainActivity.this, null);
                        PreferenceManager.setID(MainActivity.this, 0);
                        PreferenceManager.setSocketID(MainActivity.this, null);
                        PreferenceManager.setPhone(MainActivity.this, null);
                        PreferenceManager.setIsWaitingForSms(MainActivity.this, false);
                        PreferenceManager.setMobileNumber(MainActivity.this, null);
                        Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    });
                    alert.setCancelable(false);
                    alert.show();
                }
            }
        }, throwable -> {
            AppHelper.LogCat("checkIfUserSession MainActivity " + throwable.getMessage());
        });
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {


        } else if (id == R.id.nav_sos) {

            AppHelper.LaunchActivity(MainActivity.this, SOSActivity.class);

        } /*else if (id == R.id.nav_mob_rec) {

            AppHelper.LaunchActivity(MainActivity.this, MobileRechargeActivity.class);

        } else if (id == R.id.nav_dth_rec) {
            AppHelper.LaunchActivity(MainActivity.this, DTHActivity.class);

        } else if (id == R.id.nav_dc_rec) {

            AppHelper.LaunchActivity(MainActivity.this, DCActivity.class);

        } else if (id == R.id.nav_ec_rec) {

            // AppHelper.LaunchActivity(MainActivity.this,OrdersActivity.class);
        }*/ else if (id == R.id.nav_wallet) {
            AppHelper.LaunchActivity(MainActivity.this, WalletActivity.class);
            //AppHelper.LaunchActivity(MainActivity.this, AddMoneyActivity.class);

        } else if (id == R.id.nav_settings) {

            AppHelper.LaunchActivity(this, SettingsActivity.class);
        } else if (id == R.id.nav_facebook) {

            Intent facebookIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/dostchat.in/"));
            startActivity(facebookIntent);
        } else if (id == R.id.nav_twitter) {

            Intent twitterIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/dostchat"));
            startActivity(twitterIntent);
        } else if (id == R.id.nav_youtube) {

            Intent youtubeIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/channel/UCXpXNdHEccYMm6NL02osmZA"));
            startActivity(youtubeIntent);
        } else if (id == R.id.nav_google_plus) {

            Intent googlePlusIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://business.google.com/b/116877284890183824170/dashboard/l/15299446350798356942"));
            startActivity(googlePlusIntent);
        }

        mDrawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            /*case R.id.rl_settings:
                AppHelper.LaunchActivity(this, SettingsActivity.class);
                break;
            case R.id.rl_sos:
                AppHelper.LaunchActivity(this, SOSActivity.class);
                break;
            case R.id.rl_wallet:
                AppHelper.LaunchActivity(MainActivity.this, WalletActivity.class);
                break;*/
            case R.id.iv_profile:
                mDrawer.openDrawer(Gravity.START);
                break;
        }
    }
}