package com.dostchat.dost.app;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationManager;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.multidex.MultiDex;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.orhanobut.logger.Logger;
import com.dostchat.dost.BuildConfig;
import com.dostchat.dost.R;
import com.dostchat.dost.api.APIAuthentication;
import com.dostchat.dost.api.APIService;
import com.dostchat.dost.helpers.AppHelper;
import com.dostchat.dost.helpers.ExceptionHandler;
import com.dostchat.dost.helpers.Files.backup.Backup;
import com.dostchat.dost.helpers.Files.backup.BackupHandler;
import com.dostchat.dost.helpers.ForegroundRuning;
import com.dostchat.dost.helpers.LocationUpdateManager;
import com.dostchat.dost.helpers.PreferenceManager;
import com.dostchat.dost.helpers.notifications.NotificationsManager;
import com.dostchat.dost.interfaces.NetworkListener;
import com.dostchat.dost.models.LocationModel;
import com.dostchat.dost.receivers.NetworkChangeListener;
import com.dostchat.dost.services.BootService;
import com.dostchat.dost.video.CurrentUserSettings;
import com.dostchat.dost.video.WorkerThread;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

import io.fabric.sdk.android.Fabric;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.socket.client.IO;
import io.socket.client.Socket;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Abderrahim El imame on 20/02/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class DostChatApp extends Application {
    static DostChatApp mInstance;
    public static final long TIMEOUT = 10 * 1000;
    private static Socket mSocket = null;
    private RequestQueue mRequestQueue;
    public static TextToSpeech tts;

    public static LocationUpdateManager locationUpdateManager;
    public static LocationManager locationManager;

    public static void connectSocket() {
        IO.Options options = new IO.Options();
        options.forceNew = true;
        options.timeout = TIMEOUT; //set -1 to  disable it
        options.reconnection = true;
        options.reconnectionDelay = (long) 3000;
        options.reconnectionDelayMax = (long) 6000;
        options.reconnectionAttempts = 2;
        options.query = "token=" + AppConstants.APP_KEY_SECRET;
        try {
            mSocket = IO.socket(new URI(EndPoints.CHAT_SERVER_URL), options);
        } catch (URISyntaxException e) {
            AppHelper.LogCat(e);
        }

        if (!mSocket.connected())
            mSocket.connect();
    }


    public Socket getSocket() {
        return mSocket;
    }

    public static synchronized DostChatApp getInstance() {
        return mInstance;
    }

    public void setmInstance(DostChatApp mInstance) {
        DostChatApp.mInstance = mInstance;
    }

    public static void setupCrashlytics() {
        Crashlytics crashlyticsKit = new Crashlytics.Builder().core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build()).build();
        Fabric.with(mInstance, crashlyticsKit, new Crashlytics());
        Crashlytics.setUserEmail(PreferenceManager.getPhone(getInstance()));
        Crashlytics.setUserName(PreferenceManager.getPhone(getInstance()));
        Crashlytics.setUserIdentifier(String.valueOf(PreferenceManager.getID(getInstance())));
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setmInstance(this);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (AppConstants.CRASH_LYTICS)
            DostChatApp.setupCrashlytics();
        Realm.init(this);
        ForegroundRuning.init(this);

        startService(new Intent(this, BootService.class));
        if (AppConstants.DEBUGGING_MODE)
            Logger.init(AppConstants.TAG).hideThreadInfo();

        if (AppConstants.ENABLE_CRASH_HANDLER)
            Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());

        if (!PreferenceManager.getLanguage(this).equals(""))
            setDefaultLocale(this, new Locale(PreferenceManager.getLanguage(this)));
        else {
            if (Locale.getDefault().toString().equals("en_US")) {
                PreferenceManager.setLanguage(this, "en");
            }
        }

        tts = new TextToSpeech(this, status -> {

            if (status == TextToSpeech.SUCCESS) {

                try {
                    int result = tts.setLanguage(Locale.UK);

                    if (result == TextToSpeech.LANG_MISSING_DATA
                            || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "This Language is not supported");
                    } else {
                        Log.i("TTS", "This Language is  supported");

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else {
                Log.e("TTS", "Initilization Failed!");
            }

        });


    }

    public void locationExchange(Location currentLocation, int senderId, int recipientId, TextView updateView){
        APIAuthentication mAPIAuthentication = APIService.RootService(APIAuthentication.class, EndPoints.LOCATION_URL);
        Call<LocationModel> locationCall = mAPIAuthentication.location(PreferenceManager.getID(getApplicationContext()),currentLocation.getLatitude(),currentLocation.getLongitude(),1);

        locationCall.enqueue(new Callback<LocationModel>() {
            @Override
            public void onResponse(Call<LocationModel> call, Response<LocationModel> response) {
                Log.i("location Response",response.body().toString());
                LocationModel locationModel = response.body();
                String gpsString = locationModel.result.gps;
                updateView.setText(locationUpdateManager.getRecepentLocationArea(gpsString));
            }

            @Override
            public void onFailure(Call<LocationModel> call, Throwable t) {
                Log.i("location Response","Failed");
                t.printStackTrace();
            }
        });


    }

    public void startTrackingLocation(AppCompatActivity activity) {
        locationUpdateManager = new LocationUpdateManager(getApplicationContext());
        locationUpdateManager.startLocation(activity);
    }

    public static void speek(String message) {
        tts.speak(message, TextToSpeech.QUEUE_FLUSH, null);

    }


    @SuppressWarnings("deprecation")
    protected void setDefaultLocale(Context context, Locale locale) {
        Locale.setDefault(locale);
        Configuration appConfig = new Configuration();
        appConfig.locale = locale;
        context.getResources().updateConfiguration(appConfig, context.getResources().getDisplayMetrics());

    }

    public void setConnectivityListener(NetworkListener listener) {
        NetworkChangeListener.networkListener = listener;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        NotificationsManager.SetupBadger(this);
    }

    @NonNull
    public static Backup getBackup() {
        return new BackupHandler();
    }


    private static RealmConfiguration getRealmDatabaseConfiguration() {
        return new RealmConfiguration.Builder().name(getInstance().getString(R.string.app_name) + PreferenceManager.getToken(getInstance()) + ".realm").deleteRealmIfMigrationNeeded().build();
    }

    public static Realm getRealmDatabaseInstance() {
        return Realm.getInstance(getRealmDatabaseConfiguration());
    }

    public static boolean DeleteRealmDatabaseInstance() {
        return Realm.deleteRealm(getRealmDatabaseConfiguration());
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(this);
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

    private WorkerThread mWorkerThread;

    public synchronized void initWorkerThread() {
        if (mWorkerThread == null) {
            mWorkerThread = new WorkerThread(getApplicationContext());
            mWorkerThread.start();

            mWorkerThread.waitForReady();
        }
    }

    public synchronized WorkerThread getWorkerThread() {
        return mWorkerThread;
    }

    public synchronized void deInitWorkerThread() {
        mWorkerThread.exit();
        try {
            mWorkerThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mWorkerThread = null;
    }

    public static final CurrentUserSettings mVideoSettings = new CurrentUserSettings();
}
