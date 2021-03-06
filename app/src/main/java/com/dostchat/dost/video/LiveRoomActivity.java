package com.dostchat.dost.video;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewParent;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.dostchat.dost.R;
import com.dostchat.dost.activities.main.MainActivity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.lang.ref.SoftReference;
import java.util.HashMap;

import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;

import static android.graphics.PorterDuff.Mode.MULTIPLY;
import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static android.support.v7.widget.LinearLayoutManager.HORIZONTAL;
import static android.view.View.DRAWING_CACHE_QUALITY_AUTO;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.dostchat.dost.video.Constant.SHOW_VIDEO_INFO;
import static com.dostchat.dost.video.ConstantApp.ACTION_KEY_CHANNEL_NAME;
import static com.dostchat.dost.video.ConstantApp.ACTION_KEY_ENCRYPTION_KEY;
import static com.dostchat.dost.video.ConstantApp.ACTION_KEY_ENCRYPTION_MODE;
import static com.dostchat.dost.video.ConstantApp.AppError.NO_NETWORK_CONNECTION;
import static com.dostchat.dost.video.ConstantApp.DEFAULT_PROFILE_IDX;
import static com.dostchat.dost.video.ConstantApp.PrefManager.PREF_PROPERTY_PROFILE_IDX;
import static com.dostchat.dost.video.ConstantApp.VIDEO_PROFILES;
import static com.dostchat.dost.video.UserStatusData.AUDIO_MUTED;
import static com.dostchat.dost.video.UserStatusData.DEFAULT_STATUS;
import static com.dostchat.dost.video.UserStatusData.VIDEO_MUTED;
import static io.agora.rtc.IRtcEngineEventHandler.AudioVolumeInfo;
import static io.agora.rtc.IRtcEngineEventHandler.RemoteVideoStats;
import static io.agora.rtc.RtcEngine.CreateRendererView;
import static io.agora.rtc.video.VideoCanvas.RENDER_MODE_HIDDEN;



public class LiveRoomActivity extends BaseActivity implements AGEventHandler {

    private final static Logger log = LoggerFactory.getLogger(LiveRoomActivity.class);

    private GridVideoViewContainer mGridVideoViewContainer;

    private RelativeLayout mSmallVideoViewDock;

    private final HashMap<Integer, SoftReference<SurfaceView>> mUidsList = new HashMap<>(); // uid = 0 || uid == EngineConfig.mUid
    private volatile boolean mVideoMuted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_room);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mGridVideoViewContainer = (GridVideoViewContainer) findViewById(R.id.ime_background);



    }

    @Override
    protected void initUIandEvent() throws Exception {
        event().addEventHandler(this);

        Intent i = getIntent();

        String channelName = i.getStringExtra(ACTION_KEY_CHANNEL_NAME);

        final String encryptionKey = getIntent().getStringExtra(ACTION_KEY_ENCRYPTION_KEY);

        final String encryptionMode = getIntent().getStringExtra(ACTION_KEY_ENCRYPTION_MODE);

        doConfigEngine(encryptionKey, encryptionMode);

        mGridVideoViewContainer.setItemEventHandler(new VideoViewEventListener() {
            @Override
            public void onItemDoubleClick(View v, Object item) {

                if (mUidsList.size() < 2) {
                    return;
                }

                UserStatusData user = (UserStatusData) item;
                int uid = (user.mUid == 0) ? config().mUid : user.mUid;

                if (mLayoutType == LAYOUT_TYPE_DEFAULT && mUidsList.size() != 1) {
                    switchToSmallVideoView(uid);
                } else {
                    switchToDefaultVideoView();
                }
            }
        });

        SurfaceView surfaceV = CreateRendererView(getApplicationContext());
        rtcEngine().setupLocalVideo(new VideoCanvas(surfaceV, RENDER_MODE_HIDDEN, 0));
        surfaceV.setZOrderOnTop(false);
        surfaceV.setZOrderMediaOverlay(false);

        mUidsList.put(0, new SoftReference<>(surfaceV)); // get first surface view

        mGridVideoViewContainer.initViewContainer(getApplicationContext(), 0, mUidsList); // first is now full view
        worker().preview(true, surfaceV, 0);

        worker().joinChannel(channelName, config().mUid);


    }



    private int mDataStreamId;

    private void sendChannelMsg(String msgStr) {
        RtcEngine rtcEngine = rtcEngine();
        if (mDataStreamId <= 0) {
            mDataStreamId = rtcEngine.createDataStream(true, true); // boolean reliable, boolean ordered
        }

        if (mDataStreamId < 0) {
            String errorMsg = "Create data stream error happened " + mDataStreamId;
            showLongToast(errorMsg);
            return;
        }

        byte[] encodedMsg;
        try {
            encodedMsg = msgStr.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            encodedMsg = msgStr.getBytes();
        }

        rtcEngine.sendStreamMessage(mDataStreamId, encodedMsg);
    }


    private int getVideoProfileIndex() {
        SharedPreferences pref = getDefaultSharedPreferences(this);
        int profileIndex = pref.getInt(PREF_PROPERTY_PROFILE_IDX, DEFAULT_PROFILE_IDX);
        if (profileIndex > VIDEO_PROFILES.length - 1) {
            profileIndex = DEFAULT_PROFILE_IDX;

            // save the new value
            SharedPreferences.Editor editor = pref.edit();
            editor.putInt(PREF_PROPERTY_PROFILE_IDX, profileIndex);
            editor.apply();
        }
        return profileIndex;
    }

    private void doConfigEngine(String encryptionKey, String encryptionMode) throws Exception {
        int vProfile = VIDEO_PROFILES[getVideoProfileIndex()];

        worker().configEngine(vProfile, encryptionKey, encryptionMode);
    }



    public void onCustomizedFunctionClicked(View view) {
        if (mVideoMuted) {
            onSwitchSpeakerClicked();
        } else {
            onSwitchCameraClicked();
        }
    }

    private void onSwitchCameraClicked() {
        RtcEngine rtcEngine = rtcEngine();
        rtcEngine.switchCamera();
    }

    private void onSwitchSpeakerClicked() {
//        RtcEngine rtcEngine = rtcEngine();
//        rtcEngine.setEnableSpeakerphone(!(mEarpiece = !mEarpiece));
//
//        ImageView iv = (ImageView) findViewById(R.id.customized_function_id);
//        if (mEarpiece) {
//            iv.clearColorFilter();
//        } else {
//            iv.setColorFilter(getResources().getColor(R.color.agora_blue), PorterDuff.Mode.MULTIPLY);
//        }
    }

    @Override
    protected void deInitUIandEvent() throws Exception {

        doLeaveChannel();
        event().removeEventHandler(this);

        mUidsList.clear();
    }

    private void doLeaveChannel() throws Exception {
        worker().leaveChannel(config().mChannel);
        worker().preview(false, null, 0);
    }

    public void onEndCallClicked(View view) {

        quitCall();
    }


    private void quitCall() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);

        finish();
    }

    private VideoPreProcessing mVideoPreProcessing;

    public void onBtnNClicked(View view) {
        if (mVideoPreProcessing == null) {
            mVideoPreProcessing = new VideoPreProcessing();
        }

        ImageView iv = (ImageView) view;
        Object showing = view.getTag();
        if (showing != null && (Boolean) showing) {
            mVideoPreProcessing.enablePreProcessing(false);
            iv.setTag(null);
            iv.clearColorFilter();
        } else {
            mVideoPreProcessing.enablePreProcessing(true);
            iv.setTag(true);
            iv.setColorFilter(getResources().getColor(R.color.colorBlueGroup), MULTIPLY);
        }
    }

    public void onVoiceChatClicked(View view) {
        if (mUidsList.size() == 0) {
            return;
        }

        SurfaceView surfaceV = getLocalView();
        ViewParent parent;
        if (surfaceV == null || (parent = surfaceV.getParent()) == null) {
            return;
        }

        RtcEngine rtcEngine = rtcEngine();
        rtcEngine.muteLocalVideoStream(mVideoMuted = !mVideoMuted);


        hideLocalView(mVideoMuted);

        if (mVideoMuted) {
            resetEarpiece();
        } else {
            resetCamera();
        }
    }

    private SurfaceView getLocalView() {
        for (HashMap.Entry<Integer, SoftReference<SurfaceView>> entry : mUidsList.entrySet()) {
            if (entry.getKey() == 0 || entry.getKey() == config().mUid) {
                return entry.getValue().get();
            }
        }

        return null;
    }

    private void hideLocalView(boolean hide) {
        int uid = config().mUid;
        doHideTargetView(uid, hide);
    }

    private void doHideTargetView(int targetUid, boolean hide) {
        HashMap<Integer, Integer> status = new HashMap<>();
        status.put(targetUid, hide ? VIDEO_MUTED : DEFAULT_STATUS);
        if (mLayoutType == LAYOUT_TYPE_DEFAULT) {
            mGridVideoViewContainer.notifyUiChanged(mUidsList, targetUid, status, null);
        } else if (mLayoutType == LAYOUT_TYPE_SMALL) {
            UserStatusData bigBgUser = mGridVideoViewContainer.getItem(0);
            if (bigBgUser.mUid == targetUid) { // big background is target view
                mGridVideoViewContainer.notifyUiChanged(mUidsList, targetUid, status, null);
            } else { // find target view in small video view list
                mSmallVideoViewAdapter.notifyUiChanged(mUidsList, bigBgUser.mUid, status, null);
            }
        }
    }

    private void resetCamera() {

    }

    private void resetEarpiece() {

    }

    public void onVoiceMuteClicked(View view) {
        if (mUidsList.size() == 0) {
            return;
        }


    }

    @Override
    public void onFirstRemoteVideoDecoded(int uid, int width, int height, int elapsed) {
        doRenderRemoteUi(uid);
    }

    private void doRenderRemoteUi(final int uid) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isFinishing()) {
                    return;
                }

                if (mUidsList.containsKey(uid)) {
                    return;
                }

                SurfaceView surfaceV = CreateRendererView(getApplicationContext());
                mUidsList.put(uid, new SoftReference<>(surfaceV));

                boolean useDefaultLayout = mLayoutType == LAYOUT_TYPE_DEFAULT && mUidsList.size() != 2;

                surfaceV.setZOrderOnTop(!useDefaultLayout);
                surfaceV.setZOrderMediaOverlay(!useDefaultLayout);

                rtcEngine().setupRemoteVideo(new VideoCanvas(surfaceV, RENDER_MODE_HIDDEN, uid));

                if (useDefaultLayout) {
                    switchToDefaultVideoView();
                } else {
                    int bigBgUid = mSmallVideoViewAdapter == null ? uid : mSmallVideoViewAdapter.getExceptedUid();
                    switchToSmallVideoView(bigBgUid);
                }
            }
        });
    }

    @Override
    public void onJoinChannelSuccess(String channel, final int uid, int elapsed) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isFinishing()) {
                    return;
                }

                SoftReference<SurfaceView> local = mUidsList.remove(0);

                if (local == null) {
                    return;
                }

                mUidsList.put(uid, local);


                worker().getRtcEngine().setEnableSpeakerphone(false);
                worker().getRtcEngine().disableAudio();
            }
        });
    }

    @Override
    public void onUserOffline(int uid, int reason) {
        doRemoveRemoteUi(uid);
    }

    @Override
    public void onExtraCallback(final int type, final Object... data) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isFinishing()) {
                    return;
                }

                doHandleExtraCallback(type, data);
            }
        });
    }

    private void doHandleExtraCallback(int type, Object... data) {
        int peerUid;
        boolean muted;

        switch (type) {
            case EVENT_TYPE_ON_USER_AUDIO_MUTED:
                peerUid = (Integer) data[0];
                muted = (boolean) data[1];

                if (mLayoutType == LAYOUT_TYPE_DEFAULT) {
                    HashMap<Integer, Integer> status = new HashMap<>();
                    status.put(peerUid, muted ? AUDIO_MUTED : DEFAULT_STATUS);
                    mGridVideoViewContainer.notifyUiChanged(mUidsList, config().mUid, status, null);
                }

                break;

            case EVENT_TYPE_ON_USER_VIDEO_MUTED:
                peerUid = (Integer) data[0];
                muted = (boolean) data[1];

                doHideTargetView(peerUid, muted);

                break;

            case EVENT_TYPE_ON_USER_VIDEO_STATS:
                RemoteVideoStats stats = (RemoteVideoStats) data[0];

                if (SHOW_VIDEO_INFO) {
                    if (mLayoutType == LAYOUT_TYPE_DEFAULT) {
                        mGridVideoViewContainer.addVideoInfo(stats.uid, new VideoInfoData(stats.width, stats.height, stats.delay, stats.receivedFrameRate, stats.receivedBitrate));
                        int uid = config().mUid;
                        int profileIndex = getVideoProfileIndex();
                        String resolution = getResources().getStringArray(R.array.string_array_resolutions)[profileIndex];
                        String fps = getResources().getStringArray(R.array.string_array_frame_rate)[profileIndex];
                        String bitrate = getResources().getStringArray(R.array.string_array_bit_rate)[profileIndex];

                        String[] rwh = resolution.split("x");
                        int width = Integer.valueOf(rwh[0]);
                        int height = Integer.valueOf(rwh[1]);

                        mGridVideoViewContainer.addVideoInfo(uid, new VideoInfoData(width > height ? width : height,
                                width > height ? height : width,
                                0, Integer.valueOf(fps), Integer.valueOf(bitrate)));
                    }
                } else {
                    mGridVideoViewContainer.cleanVideoInfo();
                }

                break;

            case EVENT_TYPE_ON_SPEAKER_STATS:
                AudioVolumeInfo[] infos = (AudioVolumeInfo[]) data[0];

                if (infos.length == 1 && infos[0].uid == 0) { // local guy, ignore it
                    break;
                }

                if (mLayoutType == LAYOUT_TYPE_DEFAULT) {
                    HashMap<Integer, Integer> volume = new HashMap<>();

                    for (AudioVolumeInfo each : infos) {
                        peerUid = each.uid;
                        int peerVolume = each.volume;

                        if (peerUid == 0) {
                            continue;
                        }
                        volume.put(peerUid, peerVolume);
                    }
                    mGridVideoViewContainer.notifyUiChanged(mUidsList, config().mUid, null, volume);
                }

                break;

            case EVENT_TYPE_ON_APP_ERROR:
                int subType = (int) data[0];

                if (subType == NO_NETWORK_CONNECTION) {
                   // showLongToast(getString(R.string.msg_no_network_connection));
                }

                break;

            case EVENT_TYPE_ON_DATA_CHANNEL_MSG:

                peerUid = (Integer) data[0];
                final byte[] content = (byte[]) data[1];
                //notifyMessageChanged(new Message(new User(peerUid, valueOf(peerUid)), new String(content)));

                break;

            case EVENT_TYPE_ON_AGORA_MEDIA_ERROR: {
                int error = (int) data[0];
                String description = (String) data[1];

                //notifyMessageChanged(new Message(new User(0, null), error + " " + description));
                break;
            }
        }
    }

    private void requestRemoteStreamType(final int currentHostCount) {
    }

    private void doRemoveRemoteUi(final int uid) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isFinishing()) {
                    return;
                }

                Object target = mUidsList.remove(uid);
                if (target == null) {
                    return;
                }

                int bigBgUid = -1;
                if (mSmallVideoViewAdapter != null) {
                    bigBgUid = mSmallVideoViewAdapter.getExceptedUid();
                }


                if (mLayoutType == LAYOUT_TYPE_DEFAULT || uid == bigBgUid) {
                    switchToDefaultVideoView();
                } else {
                    switchToSmallVideoView(bigBgUid);
                }
            }
        });
    }

    private SmallVideoViewAdapter mSmallVideoViewAdapter;

    private void switchToDefaultVideoView() {
        if (mSmallVideoViewDock != null) {
            mSmallVideoViewDock.setVisibility(GONE);
        }
        mGridVideoViewContainer.initViewContainer(getApplicationContext(), config().mUid, mUidsList);

        mLayoutType = LAYOUT_TYPE_DEFAULT;
    }

    private void switchToSmallVideoView(int bigBgUid) {
        HashMap<Integer, SoftReference<SurfaceView>> slice = new HashMap<>(1);
        slice.put(bigBgUid, mUidsList.get(bigBgUid));
        mGridVideoViewContainer.initViewContainer(getApplicationContext(), bigBgUid, slice);

        bindToSmallVideoView(bigBgUid);

        mLayoutType = LAYOUT_TYPE_SMALL;

        requestRemoteStreamType(mUidsList.size());
    }

    public int mLayoutType = LAYOUT_TYPE_DEFAULT;

    public static final int LAYOUT_TYPE_DEFAULT = 0;

    public static final int LAYOUT_TYPE_SMALL = 1;

    private void bindToSmallVideoView(int exceptUid) {
        if (mSmallVideoViewDock == null) {
            ViewStub stub = (ViewStub) findViewById(R.id.small_video_view_dock);
            mSmallVideoViewDock = (RelativeLayout) stub.inflate();
        }

        boolean twoWayVideoCall = mUidsList.size() == 2;

        RecyclerView recycler = (RecyclerView) findViewById(R.id.small_video_view_container);

        boolean create = false;

        if (mSmallVideoViewAdapter == null) {
            create = true;
            mSmallVideoViewAdapter = new SmallVideoViewAdapter(this, config().mUid, exceptUid, mUidsList, new VideoViewEventListener() {
                @Override
                public void onItemDoubleClick(View v, Object item) {
                    switchToDefaultVideoView();
                }
            });
            mSmallVideoViewAdapter.setHasStableIds(true);
        }
        recycler.setHasFixedSize(true);


        if (twoWayVideoCall) {
            recycler.setLayoutManager(new RtlLinearLayoutManager(this, HORIZONTAL, false));
        } else {
            recycler.setLayoutManager(new LinearLayoutManager(this, HORIZONTAL, false));
        }
        recycler.addItemDecoration(new SmallVideoViewDecoration());
        recycler.setAdapter(mSmallVideoViewAdapter);

        recycler.setDrawingCacheEnabled(true);
        recycler.setDrawingCacheQuality(DRAWING_CACHE_QUALITY_AUTO);

        if (!create) {
            mSmallVideoViewAdapter.setLocalUid(config().mUid);
            mSmallVideoViewAdapter.notifyUiChanged(mUidsList, exceptUid, null, null);
        }
        recycler.setVisibility(VISIBLE);
        mSmallVideoViewDock.setVisibility(VISIBLE);
    }

}
