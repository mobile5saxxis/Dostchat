package com.dostchat.dost.activities.call;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.widget.AppCompatImageView;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dostchat.dost.app.DostChatApp;
import com.facebook.network.connectionclass.ConnectionClassManager;
import com.facebook.network.connectionclass.ConnectionQuality;
import com.facebook.network.connectionclass.DeviceBandwidthSampler;
import com.dostchat.dost.R;
import com.dostchat.dost.app.AppConstants;
import com.dostchat.dost.app.EndPoints;
import com.dostchat.dost.helpers.AppHelper;
import com.dostchat.dost.helpers.Files.backup.RealmBackupRestore;
import com.dostchat.dost.helpers.Files.cache.ImageLoader;
import com.dostchat.dost.helpers.Files.cache.MemoryCache;
import com.dostchat.dost.helpers.PreferenceManager;
import com.dostchat.dost.helpers.UtilsPhone;
import com.dostchat.dost.helpers.call.PeerConnectionParameters;
import com.dostchat.dost.helpers.call.WebRtcClient;
import com.dostchat.dost.models.calls.CallPusher;
import com.dostchat.dost.models.calls.CallsInfoModel;
import com.dostchat.dost.models.calls.CallsModel;
import com.dostchat.dost.models.users.Pusher;
import com.dostchat.dost.models.users.contacts.ContactsModel;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.MediaStream;
import org.webrtc.NetworkMonitorAutoDetect;
import org.webrtc.RendererCommon;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoRendererGui;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmQuery;
import pl.bclogic.pulsator4droid.library.PulsatorLayout;


/**
 * Created by Abderrahim El imame on 10/20/16.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */

public class CallActivity extends Activity {
    private static final String VIDEO_CODEC_VP9 = "VP8";
    private static final String AUDIO_CODEC_OPUS = "opus";
    // Local preview screen position before call is connected.
    private static final int LOCAL_X_CONNECTING = 0;
    private static final int LOCAL_Y_CONNECTING = 0;
    private static final int LOCAL_WIDTH_CONNECTING = 100;
    private static final int LOCAL_HEIGHT_CONNECTING = 100;
    // Local preview screen position after call is connected.
    private static final int LOCAL_X_CONNECTED = 72;
    private static final int LOCAL_Y_CONNECTED = 72;
    private static final int LOCAL_WIDTH_CONNECTED = 25;
    private static final int LOCAL_HEIGHT_CONNECTED = 25;
    // Remote video screen position
    private static final int REMOTE_X = 0;
    private static final int REMOTE_Y = 0;
    private static final int REMOTE_WIDTH = 100;
    private static final int REMOTE_HEIGHT = 100;
    private RendererCommon.ScalingType scalingType = RendererCommon.ScalingType.SCALE_ASPECT_FILL;
    private VideoRenderer.Callbacks localRender;
    private VideoRenderer.Callbacks remoteRender;
    private static WebRtcClient webRtcClient;

    private String userPhone;
    private String userSocketId = "";
    private String callerPhone = "";
    private String callerPhoneAccept = "";
    private String callerImage = "";
    private String userImage = "";
    private String callerSocketId = "";
    private boolean isVideoCall = false;
    private boolean isAccepted = false;
    private boolean backPressed = false;
    private Thread backPressedThread = null;

    Timer timer;
    long autoHangupDelay = 60 * 1000;

    private Handler customHandler = new Handler();
    private long startTime = 0L;
    long timeInMilliseconds = 0L;
    long timeSwapBuff = 0L;
    long updatedTime = 0L;

    //connection
    private ConnectionClassManager mConnectionClassManager;
    private DeviceBandwidthSampler mDeviceBandwidthSampler;
    private ConnectionChangedListener mListener;
    private ConnectionQuality mConnectionClass = ConnectionQuality.UNKNOWN;
    private int mTries = 0;
    private String mURL = "http://www.happynovisad.com/slike/stripovi/zavrsni-inkal-500.jpg";

    @BindView(R.id.gl_surface_view)
    GLSurfaceView glSurfaceView;

    @BindView(R.id.call_timer)
    TextView callTimer;

    @BindView(R.id.call_status)
    TextView callStatus;

    @BindView(R.id.connection_status)
    TextView connectionStatus;

    @BindView(R.id.video_call_layout)
    RelativeLayout videoCallLayout;

    @BindView(R.id.calling_layout)
    FrameLayout callingLayout;


    @BindView(R.id.caller_image)
    ImageView callerImageView;

    @BindView(R.id.caller_phone)
    TextView callerPhoneField;

    @BindView(R.id.call_title)
    TextView callTitle;

    @BindView(R.id.voice_pulsator)
    PulsatorLayout voicePulsator;

    @BindView(R.id.switch_camera)
    AppCompatImageView switchCamera;

    @BindView(R.id.hang_up)
    AppCompatImageView hangUpBtn;

    @BindView(R.id.call_record)
    AppCompatImageView callRecordBtn;

    @BindView(R.id.hang_up_layout)
    FrameLayout hangUpLayout;

    @BindView(R.id.mic_toggle)
    AppCompatImageView micToggle;

    @BindView(R.id.location)
    TextView location;

    private NetworkMonitorAutoDetect networkMonitorAutoDetect;
    private int callerID;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(
                LayoutParams.FLAG_FULLSCREEN
                        | LayoutParams.FLAG_KEEP_SCREEN_ON
                        | LayoutParams.FLAG_DISMISS_KEYGUARD
                        | LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | LayoutParams.FLAG_TURN_SCREEN_ON);
        setContentView(R.layout.activity_call);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        if (AppHelper.isAndroid5()) {
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        Bundle extras = getIntent().getExtras();
        userSocketId = extras.getString(AppConstants.USER_SOCKET_ID);
        callerSocketId = extras.getString(AppConstants.CALLER_SOCKET_ID);
        userPhone = extras.getString(AppConstants.USER_PHONE);
        callerImage = extras.getString(AppConstants.CALLER_IMAGE);
        userImage = extras.getString(AppConstants.USER_IMAGE);
        callerPhone = extras.getString(AppConstants.CALLER_PHONE);
        callerPhoneAccept = extras.getString(AppConstants.CALLER_PHONE_ACCEPT);
        isVideoCall = extras.getBoolean(AppConstants.IS_VIDEO_CALL);
        isAccepted = extras.getBoolean(AppConstants.IS_ACCEPTED_CALL);
        callerID = extras.getInt(AppConstants.CALLER_ID);
        initializerView();
        setTypeFaces();

        DostChatApp.locationUpdateManager.locationExchange(PreferenceManager.getID(this),callerID,location);


        mConnectionClassManager = ConnectionClassManager.getInstance();
        mDeviceBandwidthSampler = DeviceBandwidthSampler.getInstance();
        mListener = new ConnectionChangedListener();
        new DownloadImage().execute(mURL);
    }

    private void initializerView() {

        timer = new Timer();
        timer.schedule(new TimerTask() {

            public void run() {
                hangUp();
            }

        }, autoHangupDelay);

        getCallerInfo();
        if (isAccepted) {
            callStatus.setText(getString(R.string.incoming_call));
            String name = UtilsPhone.getContactName(this, callerPhoneAccept);
            if (name != null) {
                callerPhoneField.setVisibility(View.VISIBLE);
                callerPhoneField.setText(name);
            } else {
                callerPhoneField.setVisibility(View.VISIBLE);
                callerPhoneField.setText(callerPhoneAccept);
            }
        } else {
            callStatus.setText(getString(R.string.calling));
            String name = UtilsPhone.getContactName(this, callerPhone);
            if (name != null) {
                callerPhoneField.setVisibility(View.VISIBLE);
                callerPhoneField.setText(name);
            } else {
                callerPhoneField.setVisibility(View.VISIBLE);
                callerPhoneField.setText(callerPhone);
            }
            saveCallToLocalDB();
        }


        if (isVideoCall) {
            //Set sources for video renderer in view
            glSurfaceView.setPreserveEGLContextOnPause(true);
            glSurfaceView.setKeepScreenOn(true);

            videoCallLayout.setVisibility(View.VISIBLE);
            callingLayout.setVisibility(View.GONE);
            callTitle.setText(R.string.video_call);
            VideoRendererGui.setView(glSurfaceView, this::initializeWebRtc);
            // local and remote render
            remoteRender = VideoRendererGui.create(
                    REMOTE_X, REMOTE_Y,
                    REMOTE_WIDTH, REMOTE_HEIGHT, scalingType, false);
            localRender = VideoRendererGui.create(
                    LOCAL_X_CONNECTING, LOCAL_Y_CONNECTING,
                    LOCAL_WIDTH_CONNECTING, LOCAL_HEIGHT_CONNECTING, scalingType, true);

        } else {
            callingLayout.setVisibility(View.VISIBLE);
            switchCamera.setVisibility(View.INVISIBLE);
            switchCamera.setClickable(false);
            videoCallLayout.setVisibility(View.GONE);
            callTitle.setText(R.string.voice_call);
            initializeWebRtc();
        }


        switchCamera.setOnClickListener(v -> {
            if (webRtcClient != null) {
                webRtcClient.switchCamera(this);
            }
        });
        hangUpBtn.setOnClickListener(v -> {
            hangUp();
        });

        micToggle.setOnClickListener(v -> {
            if (webRtcClient != null) {
                if (webRtcClient.toggleMic()) {
                    micToggle.setImageDrawable(AppHelper.getVectorDrawable(this, R.drawable.ic_mic_white_active_24dp));
                } else {
                    micToggle.setImageDrawable(AppHelper.getVectorDrawable(this, R.drawable.ic_mic_off_white_24dp));
                }
            }
            callRecordBtn.setOnClickListener(v1 -> {

            });
        });
        networkDetection();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mConnectionClassManager.remove(mListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mConnectionClassManager.register(mListener);
    }

    /**
     * Listener to update the UI upon connectionclass change.
     */
    private class ConnectionChangedListener implements ConnectionClassManager.ConnectionClassStateChangeListener {

        @Override
        public void onBandwidthStateChange(ConnectionQuality bandwidthState) {
            mConnectionClass = bandwidthState;
            AppHelper.LogCat("ConnectionChangedListener " + mConnectionClass.toString());
            runOnUiThread(() -> {
                connectionStatus.setVisibility(View.VISIBLE);
                switch (bandwidthState) {
                    case EXCELLENT:
                        connectionStatus.setVisibility(View.GONE);
                        break;
                    case MODERATE:
                        connectionStatus.setText(R.string.you_are_using_a_moderate_connection);
                        new Handler().postDelayed(() -> {
                            connectionStatus.setVisibility(View.GONE);
                        }, 4000);
                        break;
                    case POOR:
                        connectionStatus.setText(R.string.you_are_using_a_poor_connection);
                        new Handler().postDelayed(() -> {
                            connectionStatus.setVisibility(View.GONE);
                        }, 4000);
                        break;
                    case GOOD:
                        connectionStatus.setText(R.string.you_are_using_a_good_connection);
                        new Handler().postDelayed(() -> {
                            connectionStatus.setVisibility(View.GONE);
                        }, 4000);
                        break;
                    case UNKNOWN:
                        connectionStatus.setText(R.string.connection_is_not_available);
                        new Handler().postDelayed(() -> {
                            connectionStatus.setVisibility(View.GONE);
                        }, 4000);
                        hangUp();
                        break;
                }
            });
        }
    }

    private void setTypeFaces() {
        if (AppConstants.ENABLE_FONTS_TYPES) {
            callTimer.setTypeface(AppHelper.setTypeFace(this, "Futura"));
            callTitle.setTypeface(AppHelper.setTypeFace(this, "Futura"));
            callStatus.setTypeface(AppHelper.setTypeFace(this, "Futura"));
            connectionStatus.setTypeface(AppHelper.setTypeFace(this, "Futura"));
            callerPhoneField.setTypeface(AppHelper.setTypeFace(this, "Futura"));
        }
    }


    private void networkDetection() {
        networkMonitorAutoDetect = new NetworkMonitorAutoDetect(connectionType -> {
            AppHelper.LogCat("onConnectionTypeChanged " + connectionType.name());
            connectionStatus.setVisibility(View.VISIBLE);
            switch (connectionType) {
                case CONNECTION_WIFI:
                    connectionStatus.setVisibility(View.GONE);
                    break;
                case CONNECTION_4G:
                    connectionStatus.setVisibility(View.GONE);
                    break;
                case CONNECTION_3G:
                    connectionStatus.setVisibility(View.GONE);
                    break;
                case CONNECTION_2G:
                    connectionStatus.setText(R.string.you_are_using_a_slower_connection);
                    new Handler().postDelayed(() -> {
                        connectionStatus.setVisibility(View.GONE);
                    }, 4000);
                    break;
                case CONNECTION_NONE:
                case CONNECTION_UNKNOWN:
                    connectionStatus.setText(R.string.connection_is_not_available);
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                    }
                    hangUp();
                    break;
            }

        }, this);
    }

    private void saveCallToLocalDB() {

        DateTime current = new DateTime();
        String callTime = String.valueOf(current);
        Realm realm = DostChatApp.getRealmDatabaseInstance();

        int historyCallId = getHistoryCallId(PreferenceManager.getID(this), callerID, isVideoCall, realm);

        if (historyCallId == 0) {
            realm.executeTransaction(realm1 -> {
                ContactsModel contactsModel1;
                if (isAccepted) {
                    contactsModel1 = realm1.where(ContactsModel.class).equalTo("phone", callerPhoneAccept).findFirst();
                } else {
                    contactsModel1 = realm1.where(ContactsModel.class).equalTo("phone", callerPhone).findFirst();
                }

                int lastID = RealmBackupRestore.getCallLastId();
                CallsModel callsModel = new CallsModel();
                callsModel.setId(lastID);
                if (isVideoCall)
                    callsModel.setType(AppConstants.VIDEO_CALL);
                else
                    callsModel.setType(AppConstants.VOICE_CALL);
                callsModel.setContactsModel(contactsModel1);
                if (isAccepted)
                    callsModel.setPhone(callerPhoneAccept);
                else
                    callsModel.setPhone(callerPhone);
                callsModel.setFrom(PreferenceManager.getID(this));
                callsModel.setTo(contactsModel1.getId());
                callsModel.setDuration("00:00");
                callsModel.setCounter(1);
                callsModel.setDate(callTime);
                callsModel.setReceived(false);

                CallsInfoModel callsInfoModel = new CallsInfoModel();
                RealmList<CallsInfoModel> callsInfoModelRealmList = new RealmList<CallsInfoModel>();
                int lastInfoID = RealmBackupRestore.getCallInfoLastId();
                callsInfoModel.setId(lastInfoID);
                if (isVideoCall)
                    callsInfoModel.setType(AppConstants.VIDEO_CALL);
                else
                    callsInfoModel.setType(AppConstants.VOICE_CALL);
                callsInfoModel.setContactsModel(contactsModel1);
                if (isAccepted)
                    callsInfoModel.setPhone(callerPhoneAccept);
                else
                    callsInfoModel.setPhone(callerPhone);
                callsInfoModel.setFrom(PreferenceManager.getID(this));
                callsInfoModel.setCallId(lastID);
                callsInfoModel.setTo(contactsModel1.getId());
                callsInfoModel.setDuration("00:00");
                callsInfoModel.setDate(callTime);
                callsInfoModel.setReceived(false);
                callsInfoModelRealmList.add(callsInfoModel);
                callsModel.setCallsInfoModels(callsInfoModelRealmList);
                realm1.copyToRealmOrUpdate(callsModel);
                EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_CALL_NEW_ROW, lastID));
            });
        } else {

            realm.executeTransaction(realm1 -> {
                ContactsModel contactsModel1;
                if (isAccepted) {
                    contactsModel1 = realm1.where(ContactsModel.class).equalTo("phone", callerPhoneAccept).findFirst();
                } else {
                    contactsModel1 = realm1.where(ContactsModel.class).equalTo("phone", callerPhone).findFirst();
                }


                int callCounter;
                CallsModel callsModel;
                RealmQuery<CallsModel> callsModelRealmQuery = realm1.where(CallsModel.class).equalTo("id", historyCallId);
                callsModel = callsModelRealmQuery.findAll().first();

                callCounter = callsModel.getCounter();
                callCounter++;
                callsModel.setDate(callTime);
                callsModel.setCounter(callCounter);
                CallsInfoModel callsInfoModel = new CallsInfoModel();
                RealmList<CallsInfoModel> callsInfoModelRealmList = callsModel.getCallsInfoModels();
                int lastInfoID = RealmBackupRestore.getCallInfoLastId();
                callsInfoModel.setId(lastInfoID);
                if (isVideoCall)
                    callsInfoModel.setType(AppConstants.VIDEO_CALL);
                else
                    callsInfoModel.setType(AppConstants.VOICE_CALL);
                callsInfoModel.setContactsModel(contactsModel1);
                if (isAccepted)
                    callsInfoModel.setPhone(callerPhoneAccept);
                else
                    callsInfoModel.setPhone(callerPhone);
                callsInfoModel.setFrom(PreferenceManager.getID(this));
                callsInfoModel.setTo(contactsModel1.getId());
                callsInfoModel.setCallId(callsModel.getId());
                callsInfoModel.setDuration("00:00");
                callsInfoModel.setDate(callTime);
                callsInfoModel.setReceived(false);
                callsInfoModelRealmList.add(callsInfoModel);
                callsModel.setCallsInfoModels(callsInfoModelRealmList);

                realm1.copyToRealmOrUpdate(callsModel);
                EventBus.getDefault().post(new Pusher(AppConstants.EVENT_UPDATE_CALL_OLD_ROW, historyCallId));
            });
        }

        realm.close();
    }

    private int getHistoryCallId(int fromId, int toId, boolean isVideoCall, Realm realm) {
        String type;
        CallsModel callsModel;
        if (isVideoCall)
            type = AppConstants.VIDEO_CALL;
        else
            type = AppConstants.VOICE_CALL;


        try {

            callsModel = realm.where(CallsModel.class)
                    .equalTo("from", fromId)
                    .equalTo("to", toId)
                    .equalTo("received", false)
                    .equalTo("type", type)
                    .findAll().first();
            return callsModel.getId();
        } catch (Exception e) {
            AppHelper.LogCat("call history id Exception MainService" + e.getMessage());
            return 0;
        }
    }

    private void getCallerInfo() {
        MemoryCache memoryCache = new MemoryCache();
        Realm realm = DostChatApp.getRealmDatabaseInstance();
        ContactsModel mContactsModel;
        if (isAccepted) {
            mContactsModel = realm.where(ContactsModel.class).equalTo("phone", callerPhoneAccept).findFirst();
        } else {
            mContactsModel = realm.where(ContactsModel.class).equalTo("phone", callerPhone).findFirst();
        }
        Bitmap bitmap;
        if (mContactsModel != null) {
            bitmap = ImageLoader.GetCachedBitmapImage(memoryCache, mContactsModel.getImage(), this, mContactsModel.getId(), AppConstants.USER, AppConstants.FULL_PROFILE);
        } else {
            bitmap = null;
        }
        if (bitmap != null) {
            callerImageView.setImageBitmap(bitmap);
        } else {
            Target target = new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    callerImageView.setImageBitmap(bitmap);

                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {
                    callerImageView.setImageDrawable(errorDrawable);
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {
                    callerImageView.setImageDrawable(placeHolderDrawable);
                }
            };
            callerImageView.setTag(target);
            Picasso.with(this)
                    .load(EndPoints.PROFILE_IMAGE_URL + callerImage)
                    .placeholder(R.drawable.image_holder_up)
                    .error(R.drawable.image_holder_up)
                    .into(target);
        }

        realm.close();
    }


    /**
     * Initialize webrtc webRtcClient
     * Set up the peer connection parameters get some video information and then pass these information to WebrtcClient class.
     */
    private void initializeWebRtc() {
        Point displaySize = new Point();
        getWindowManager().getDefaultDisplay().getSize(displaySize);
        PeerConnectionParameters params = new PeerConnectionParameters(isVideoCall, false, displaySize.x, displaySize.y, 30, 1, VIDEO_CODEC_VP9, true, 1, AUDIO_CODEC_OPUS, true);
        webRtcClient = new WebRtcClient(this, params, userSocketId, callerSocketId, isVideoCall, isAccepted);


    }


    /**
     * Handle when people click hangUp button
     * Destroy all video resources and connection
     */
    public void hangUp() {

        if (webRtcClient != null) {
            stopTimer();//// TODO: 12/3/16 make time blink animation
            updateUserCall();
            try {
                JSONObject messageJSON = new JSONObject();
                messageJSON.put("callerSocketId", callerSocketId);
                messageJSON.put("userSocketId", PreferenceManager.getSocketID(this));
                if (webRtcClient == null) return;
                webRtcClient.hangUpCall(messageJSON);

                if (webRtcClient != null) {
                    webRtcClient.closeAllConnections();
                    webRtcClient = null;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
            }
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        } else {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
            }
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        }
    }


    /**
     * Handle onDestroy event which is implement by RtcListener class
     * Destroy the video source
     */
    @Override
    public void onDestroy() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        stopTimer();

        if (networkMonitorAutoDetect != null)
            networkMonitorAutoDetect.destroy();
        if (webRtcClient != null) {
            webRtcClient.closeAllConnections();
            webRtcClient = null;
        }


        if (voicePulsator.isStarted())
            voicePulsator.stop();

        EventBus.getDefault().unregister(this);


        AppHelper.restartService();

        super.onDestroy();
    }


    public void onCallReady(String callId) {
        AppHelper.LogCat("onCallReady " + callId);
        if (callerPhone != null) {
            AppHelper.LogCat("callerPhone hmm" + callerPhone);
            if (webRtcClient != null) {
                webRtcClient.startNewCall(callerSocketId, callId, userPhone, userImage, PreferenceManager.getID(this), isVideoCall);
            } else {
                hangUp();
            }
        } else {
            AppHelper.LogCat(" answer start camera callerPhone null");
        }
    }


    /**
     * This function is being call to answer call from other user
     * send init signal to the caller and connect
     * start the camera
     *
     * @param callerId the id of the caler
     */
    public void answer(String callerId) throws JSONException {
        if (webRtcClient != null) {
            webRtcClient.signalingServer(callerId, "init", null);
        } else {
            hangUp();
        }
    }


    private void updateUserCall() {
        try {
            Realm realm = DostChatApp.getRealmDatabaseInstance();
            realm.executeTransaction(realm1 -> {
                ContactsModel contactsModel;
                CallsInfoModel callsInfoModel;
                CallsModel callsModel;

                RealmQuery<CallsModel> callsModelRealmQuery;
                RealmQuery<CallsInfoModel> callsInfoModelRealmQuery;
                if (isAccepted) {
                    contactsModel = realm.where(ContactsModel.class).equalTo("phone", callerPhoneAccept).findFirst();
                    if (isVideoCall)
                        callsModelRealmQuery = realm1.where(CallsModel.class).equalTo("type", AppConstants.VIDEO_CALL).equalTo("from", contactsModel.getId());
                    else
                        callsModelRealmQuery = realm1.where(CallsModel.class).equalTo("type", AppConstants.VOICE_CALL).equalTo("from", contactsModel.getId());
                } else {
                    contactsModel = realm.where(ContactsModel.class).equalTo("phone", callerPhone).findFirst();
                    if (isVideoCall)
                        callsModelRealmQuery = realm1.where(CallsModel.class).equalTo("type", AppConstants.VIDEO_CALL).equalTo("to", contactsModel.getId());
                    else
                        callsModelRealmQuery = realm1.where(CallsModel.class).equalTo("type", AppConstants.VOICE_CALL).equalTo("to", contactsModel.getId());
                }

                callsModel = callsModelRealmQuery.findAll().first();
                callsModel.setDuration(getTimer());
                callsInfoModelRealmQuery = realm1.where(CallsInfoModel.class).equalTo("callId", callsModel.getId());
                callsInfoModel = callsInfoModelRealmQuery.findAll().last();
                callsInfoModel.setDuration(getTimer());
                realm1.copyToRealmOrUpdate(callsModel);
                EventBus.getDefault().post(new Pusher(AppConstants.EVENT_UPDATE_CALL_OLD_ROW, callsInfoModel.getCallId()));
            });
            realm.close();
        }catch (Exception e){
            e.printStackTrace();
        }

    }


    public void onReject() {
        if (webRtcClient != null) {
            webRtcClient.closeAllConnections();
            webRtcClient = null;
        }
        updateUserCall();

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
        }

        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    public void onHangUp() {
        if (webRtcClient != null) {
            webRtcClient.closeAllConnections();
            webRtcClient = null;
        }
        updateUserCall();
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
        }
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }


    public void onAcceptCall(String callId) {
        try {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
            }
            answer(callId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String getTimer() {
        return callTimer.getText().toString().trim();
    }

    private void setTimer() {
        runOnUiThread(() -> callTimer.setVisibility(View.VISIBLE));
        startTime = SystemClock.uptimeMillis();
        customHandler.postDelayed(updateTimerThread, 0);
    }

    private void stopTimer() {
        runOnUiThread(() -> callTimer.setVisibility(View.GONE));
        timeSwapBuff += timeInMilliseconds;
        customHandler.removeCallbacks(updateTimerThread);
    }


    public void onStatusChanged(String newStatus) {
        runOnUiThread(() -> {
            callStatus.setVisibility(View.VISIBLE);
            switch (newStatus) {
                case AppConstants.USER_DISCONNECT:
                    callStatus.setText(newStatus);
                    callStatus.setTextColor(AppHelper.getColor(this, R.color.colorRedDark));
                    break;
                case AppConstants.USER_CONNECTING:
                    callStatus.setText(newStatus);
                    callStatus.setTextColor(AppHelper.getColor(this, R.color.colorWhite));
                    break;
                case AppConstants.USER_CLOSED:
                    hangUp();
                    break;
                case AppConstants.USER_COMPLETED:

                    if (timer != null) {
                        timer.cancel();
                        timer = null;
                    }

                    callStatus.setText(AppConstants.USER_CONNECTED);
                    callStatus.setTextColor(AppHelper.getColor(this, R.color.colorWhite));
                    if (!isVideoCall) {
                        voicePulsator.setVisibility(View.VISIBLE);
                        voicePulsator.start();
                    }


                    break;
                case AppConstants.USER_CONNECTED:
                    connectionStatus.setVisibility(View.GONE);
                    if (timer != null) {
                        timer.cancel();
                        timer = null;
                    }
                    setTimer();

                    callStatus.setText(newStatus);
                    callStatus.setTextColor(AppHelper.getColor(this, R.color.colorWhite));

                    new Handler().postDelayed(() -> {
                        animateHideElement(switchCamera);
                        animateHideElement(micToggle);
                        animateHideElement(hangUpLayout);

                    }, 3000);
                    break;
            }


        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // If we've received a touch notification that the user has touched
        //make buttons showing
        if (MotionEvent.ACTION_DOWN == event.getAction()) {

            animateShowElement(switchCamera);
            animateShowElement(micToggle);
            animateShowElement(hangUpLayout);

            return true;
        } else if (MotionEvent.ACTION_CANCEL == event.getAction()) {

            new Handler().postDelayed(() -> {
                animateHideElement(callStatus);
                animateHideElement(switchCamera);
                animateHideElement(micToggle);
                animateHideElement(hangUpLayout);

            }, 3000);
        }

        return super.onTouchEvent(event);
    }

    private void animateHideElement(View view) {
        view.animate()
                .translationY(0)
                .alpha(0.0f)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        view.setVisibility(View.GONE);
                    }
                });
    }

    private void animateShowElement(View view) {

        // Start the animation
        view.animate()
                .translationY(0)
                .alpha(1.0f)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        view.setVisibility(View.VISIBLE);
                    }
                });
    }


    public void onLocalStream(MediaStream localStream) {
        if (isVideoCall) {
            AppHelper.LogCat(" onLocalStream videoTracks: " + localStream.videoTracks.size());
            if (localStream.videoTracks.size() == 0) return;
            localStream.videoTracks.get(0).addRenderer(new VideoRenderer(localRender));
            VideoRendererGui.update(localRender,
                    LOCAL_X_CONNECTING, LOCAL_Y_CONNECTING,
                    LOCAL_WIDTH_CONNECTING, LOCAL_HEIGHT_CONNECTING,
                    scalingType, false);

        }
    }

    public void onAddRemoteStream(MediaStream remoteStream, int endPoint) {
        if (isVideoCall) {
            AppHelper.LogCat(" onAddRemoteStream videoTracks: " + remoteStream.videoTracks.size());
            if (remoteStream.audioTracks.size() > 1 || remoteStream.videoTracks.size() > 1) {
                AppHelper.LogCat(" stream: " + remoteStream.toString());
                return;
            }
            if (remoteStream.videoTracks.size() == 1) {
                remoteStream.videoTracks.get(0).addRenderer(new VideoRenderer(remoteRender));
                VideoRendererGui.update(remoteRender, REMOTE_X, REMOTE_Y, REMOTE_WIDTH, REMOTE_HEIGHT, scalingType, false);
            }
            VideoRendererGui.update(localRender, LOCAL_X_CONNECTED, LOCAL_Y_CONNECTED, LOCAL_WIDTH_CONNECTED, LOCAL_HEIGHT_CONNECTED, scalingType, false);

        }
    }


    public void onRemoveRemoteStream(int endPoint) {
        if (isVideoCall) {
            VideoRendererGui.update(localRender,
                    LOCAL_X_CONNECTING, LOCAL_Y_CONNECTING,
                    LOCAL_WIDTH_CONNECTING, LOCAL_HEIGHT_CONNECTING,
                    scalingType, false);//);
        }
    }


    @Override
    public void onBackPressed() {
        if (!this.backPressed) {
            this.backPressed = true;
            AppHelper.CustomToast(this, "Press again to end the call.");
            this.backPressedThread = new Thread(() -> {
                try {
                    hangUp();
                    Thread.sleep(5000);
                    backPressed = false;
                } catch (InterruptedException e) {
                    AppHelper.LogCat(" Successfully interrupted");
                }
            });
            this.backPressedThread.start();
            return;
        }
        if (this.backPressedThread != null)
            this.backPressedThread.interrupt();
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private Runnable updateTimerThread = new Runnable() {
        public void run() {

            timeInMilliseconds = SystemClock.uptimeMillis() - startTime;


            updatedTime = timeSwapBuff + timeInMilliseconds;


            int secs = (int) (updatedTime / 1000);

            int mins = secs / 60;

            secs = secs % 60;


            callTimer.setText("" + mins + ":" + String.format(Locale.getDefault(), "%02d", secs));
            customHandler.postDelayed(this, 0);

        }

    };


    /**
     * AsyncTask for handling downloading and making calls to the timer.
     */
    private class DownloadImage extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            mDeviceBandwidthSampler.startSampling();
        }

        @Override
        protected Void doInBackground(String... url) {
            String imageURL = url[0];
            try {
                // Open a stream to download the image from our URL.
                URLConnection connection = new URL(imageURL).openConnection();
                connection.setUseCaches(false);
                connection.connect();
                InputStream input = connection.getInputStream();
                try {
                    byte[] buffer = new byte[1024];

                    // Do some busy waiting while the stream is open.
                    while (input.read(buffer) != -1) {
                    }
                } finally {
                    input.close();
                }
            } catch (IOException e) {
                AppHelper.LogCat("Error while downloading image.");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            mDeviceBandwidthSampler.stopSampling();
            // Retry for up to 10 times until we find a ConnectionClass.
            if (mConnectionClass == ConnectionQuality.UNKNOWN && mTries < 10) {
                mTries++;
                new DownloadImage().execute(mURL);
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
    public void onEventMainThread(CallPusher pusher) {
        switch (pusher.getEvent()) {
            case AppConstants.EVENT_BUS_ACCEPT_CALL:
                onAcceptCall(pusher.getCallId());
                break;
            case AppConstants.EVENT_BUS_REJECT_CALL:
                onReject();
                break;
            case AppConstants.EVENT_BUS_HANG_UP:
                onHangUp();
                break;
            case AppConstants.EVENT_BUS_CALL_READY:
                onCallReady(pusher.getCallId());
                break;
            case AppConstants.EVENT_BUS_STATUS_CHANGED:
                onStatusChanged(pusher.getConnectionStatus());
                break;
            case AppConstants.EVENT_BUS_LOCAL_STREAM:
                onLocalStream(pusher.getMediaStream());
                break;
            case AppConstants.EVENT_BUS_ADD_REMOTE_STREAM:
                onAddRemoteStream(pusher.getMediaStream(), pusher.getEndPoint());
                break;
            case AppConstants.EVENT_BUS_REMOVE_REMOTE_STREAM:
                onRemoveRemoteStream(pusher.getEndPoint());
                break;
            case AppConstants.EVENT_BUS_ON_PEER_CLOSED:
                onPeerConnectionClosed();
                break;

        }


    }

    public void onPeerConnectionClosed() {
        AppHelper.LogCat("onPeerConnectionClosed ");
        runOnUiThread(() -> {
            if (callStatus.getVisibility() == View.GONE)
                callStatus.setVisibility(View.VISIBLE);
            callStatus.setTextColor(AppHelper.getColor(this, R.color.colorRedDark));
            callStatus.setText(R.string.call_ended);
        });

        if (webRtcClient != null)
            webRtcClient = null;

    }


}
