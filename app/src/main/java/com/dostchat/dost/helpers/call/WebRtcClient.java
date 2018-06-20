package com.dostchat.dost.helpers.call;

import android.Manifest;
import android.app.Activity;
import android.util.Log;

import com.dostchat.dost.app.AppConstants;
import com.dostchat.dost.app.DostChatApp;
import com.dostchat.dost.helpers.AppHelper;
import com.dostchat.dost.helpers.PermissionHandler;
import com.dostchat.dost.helpers.PreferenceManager;
import com.dostchat.dost.models.calls.CallPusher;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.CameraEnumerationAndroid;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoCapturerAndroid;
import org.webrtc.VideoSource;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import de.greenrobot.event.EventBus;
import io.socket.client.Ack;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Created by Abderrahim El imame on 10/21/16.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */

public class WebRtcClient {
    private final static String TAG = WebRtcClient.class.getCanonicalName();
    private final static int MAX_PEER_CONNECTIONS = 2;
    private boolean[] endPoints = new boolean[MAX_PEER_CONNECTIONS];
    private PeerConnectionFactory peerConnectionFactory;
    private HashMap<String, Peer> peers = new HashMap<>();
    private LinkedList<PeerConnection.IceServer> iceServers = new LinkedList<>();
    private PeerConnectionParameters peerConnectionParameters;
    private MediaConstraints mediaConstraints = new MediaConstraints();
    private MediaStream mediaStream;
    private int numberOfCameras;
    private boolean cameraIsOpened = false;
    private VideoCapturerAndroid videoCapturerAndroid;
    private VideoCapturer videoCapturer;
    private VideoCapturerAndroid.CameraSwitchHandler cameraSwitchHandler;
    private VideoSource videoSource;
    private AudioSource audioSource;
    private Socket mSocket;
    private static final String PEER_CONNECTION_ID = "ARDAMS";
    private static final String VIDEO_TRACK_ID = "ARDAMSv0";
    private static final String AUDIO_TRACK_ID = "ARDAMSa0";
    private String mFrontCameraName;
    private String mBackCameraName;
    private Activity mActivity;
    private boolean isAccepted;


    private static final String AUDIO_ECHO_CANCELLATION_CONSTRAINT = "googEchoCancellation";
    private static final String AUDIO_ECHO_CANCELLATION2_CONSTRAINT = "googEchoCancellation2";
    private static final String AUDIO_AUTO_GAIN_CONTROL_CONSTRAINT = "googAutoGainControl";
    private static final String AUDIO_AUTO_GAIN_CONTROL2_CONSTRAINT = "googAutoGainControl2";
    private static final String AUDIO_HIGH_PASS_FILTER_CONSTRAINT = "googHighpassFilter";
    private static final String AUDIO_NOISE_SUPPRESSION_CONSTRAINT = "googNoiseSuppression";
    private static final String AUDIO_NOISE_SUPPRESSION2_CONSTRAINT = "googNoisesuppression2";
    private static final String MAX_VIDEO_WIDTH_CONSTRAINT = "maxWidth";
    private static final String MIN_VIDEO_WIDTH_CONSTRAINT = "minWidth";
    private static final String MAX_VIDEO_HEIGHT_CONSTRAINT = "maxHeight";
    private static final String MIN_VIDEO_HEIGHT_CONSTRAINT = "minHeight";
    private static final String MAX_VIDEO_FPS_CONSTRAINT = "maxFrameRate";
    private static final String MIN_VIDEO_FPS_CONSTRAINT = "minFrameRate";


    private interface Command {
        void execute(String peerId, JSONObject payload) throws JSONException;
    }

    private class CreateOfferCommand implements Command {
        public void execute(String peerId, JSONObject payload) throws JSONException {
            Log.d(TAG, "CreateOfferCommand");
            Peer peer = peers.get(peerId);
            peer.peerConnection.createOffer(peer, mediaConstraints);
        }
    }


    private class CreateAnswerCommand implements Command {
        public void execute(String peerId, JSONObject payload) throws JSONException {
            Log.d(TAG, "CreateAnswerCommand");
            Peer peer = peers.get(peerId);
            SessionDescription sdp = new SessionDescription(
                    SessionDescription.Type.fromCanonicalForm(payload.getString("type")),
                    payload.getString("sdp")
            );
            peer.peerConnection.setRemoteDescription(peer, sdp);
            peer.peerConnection.createAnswer(peer, mediaConstraints);
        }
    }

    private class SetRemoteSDPCommand implements Command {
        public void execute(String peerId, JSONObject payload) throws JSONException {
            Log.d(TAG, "SetRemoteSDPCommand");
            Peer peer = peers.get(peerId);
            SessionDescription sdp = new SessionDescription(
                    SessionDescription.Type.fromCanonicalForm(payload.getString("type")),
                    payload.getString("sdp")
            );
            peer.peerConnection.setRemoteDescription(peer, sdp);
        }
    }

    private class AddIceCandidateCommand implements Command {
        public void execute(String peerId, JSONObject payload) throws JSONException {
            Log.d(TAG, "AddIceCandidateCommand");
            PeerConnection pc = peers.get(peerId).peerConnection;
            if (pc.getRemoteDescription() != null) {
                IceCandidate candidate = new IceCandidate(
                        payload.getString("sdpMid"),
                        payload.getInt("sdpMLineIndex"),
                        payload.getString("candidate")
                );
                pc.addIceCandidate(candidate);
            }
        }
    }

    /**
     * Send a signal through the signaling server
     *
     * @param to      id this the first parameters of signalingServer method
     * @param type    type  his the second parameters of signalingServer method
     * @param payload payload his the thirded parameters of signalingServer method
     * @throws JSONException
     */
    public void signalingServer(String to, String type, JSONObject payload) throws JSONException {
        JSONObject message = new JSONObject();
        message.put("to", to);
        message.put("from", PreferenceManager.getSocketID(DostChatApp.getInstance()));
        message.put("type", type);
        message.put("payload", payload);
        mSocket.emit(AppConstants.SOCKET_SIGNALING_SERVER, message);

    }

    private class SignalingServerHandler {
        private HashMap<String, Command> commandMap;

        private SignalingServerHandler() {
            this.commandMap = new HashMap<>();
            commandMap.put("init", new CreateOfferCommand());
            commandMap.put("offer", new CreateAnswerCommand());
            commandMap.put("answer", new SetRemoteSDPCommand());
            commandMap.put("candidate", new AddIceCandidateCommand());
        }

        private Emitter.Listener onSignalingServerResponse = new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject) args[0];
                try {

                    String from = data.getString("from");
                    String type = data.getString("type");

                    // if unknown command - just skip it
                    if (!commandMap.containsKey(type)) {
                        return;
                    }
                    JSONObject payload = new JSONObject();
                    if (!type.equals("init")) {
                        payload = data.getJSONObject("payload");
                    }

                    // if peer is unknown, try to add him
                    if (!peers.containsKey(from)) {
                        // if MAX_PEER is reach, ignore the call
                        int endPoint = findEndPoint();
                        if (endPoint != MAX_PEER_CONNECTIONS) {
                            addPeer(from, endPoint);
                            commandMap.get(type).execute(from, payload);
                        }
                    } else {
                        commandMap.get(type).execute(from, payload);
                    }
                } catch (JSONException e) {
                    AppHelper.LogCat(" onSignalingServerResponse JSONException " + e.getMessage());

                }
            }
        };

        private Emitter.Listener onRejectResponse = args -> {

            JSONObject data = (JSONObject) args[0];
            try {
                String from = data.getString("userSocketId");
                if (from.equals(PreferenceManager.getSocketID(DostChatApp.getInstance())))
                    return;
                EventBus.getDefault().post(new CallPusher(AppConstants.EVENT_BUS_REJECT_CALL));
            } catch (JSONException e) {
                AppHelper.LogCat(" onRejectResponse JSONException " + e.getMessage());

            }
        };

        private Emitter.Listener onHangUpCallResponse = args -> {

            JSONObject data = (JSONObject) args[0];
            try {
                String from = data.getString("userSocketId");
                if (from.equals(PreferenceManager.getSocketID(DostChatApp.getInstance())))
                    return;
                EventBus.getDefault().post(new CallPusher(AppConstants.EVENT_BUS_HANG_UP));
            } catch (JSONException e) {
                AppHelper.LogCat(" onHangUpCallResponse JSONException " + e.getMessage());

            }
        };

        private Emitter.Listener onAcceptResponse = args -> {
            JSONObject data = (JSONObject) args[0];
            try {
                String from = data.getString("userSocketId");
                EventBus.getDefault().post(new CallPusher(AppConstants.EVENT_BUS_ACCEPT_CALL, from));
            } catch (JSONException e) {
                AppHelper.LogCat(" onAcceptResponse JSONException " + e.getMessage());

            }

        };


    }

    private class Peer implements SdpObserver, PeerConnection.Observer {
        private PeerConnection peerConnection;
        private String id;
        private int endPoint;


        @Override
        public void onCreateSuccess(final SessionDescription sdp) {
            try {
                JSONObject payload = new JSONObject();
                payload.put("type", sdp.type.canonicalForm());
                payload.put("sdp", sdp.description);
                AppHelper.LogCat("onCreateSuccess type " + sdp.type.canonicalForm());
                signalingServer(id, sdp.type.canonicalForm(), payload);
                peerConnection.setLocalDescription(Peer.this, sdp);
            } catch (JSONException e) {
                AppHelper.LogCat("onCreateSuccess JSONException " + e.getMessage());

            }
        }

        @Override
        public void onSetSuccess() {
            AppHelper.LogCat("onSetSuccess ");
        }

        @Override
        public void onCreateFailure(String s) {
            AppHelper.LogCat("onCreateFailure " + s);
        }

        @Override
        public void onSetFailure(String s) {
            AppHelper.LogCat("onSetFailure " + s);

        }

        @Override
        public void onSignalingChange(PeerConnection.SignalingState signalingState) {
            AppHelper.LogCat("onSignalingChange " + signalingState);
            if (signalingState == PeerConnection.SignalingState.CLOSED) {
                removePeer(id);
                EventBus.getDefault().post(new CallPusher(AppConstants.EVENT_BUS_ON_PEER_CLOSED));
            }
        }

        @Override
        public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
            AppHelper.LogCat("onIceConnectionChange " + iceConnectionState);

            if (iceConnectionState == PeerConnection.IceConnectionState.NEW || iceConnectionState == PeerConnection.IceConnectionState.CHECKING) {
                EventBus.getDefault().post(new CallPusher(AppConstants.EVENT_BUS_STATUS_CHANGED, AppConstants.USER_CONNECTING, null));
            } else if (iceConnectionState == PeerConnection.IceConnectionState.CONNECTED) {
                EventBus.getDefault().post(new CallPusher(AppConstants.EVENT_BUS_STATUS_CHANGED, AppConstants.USER_CONNECTED, null));
            } else if (iceConnectionState == PeerConnection.IceConnectionState.COMPLETED) {
                EventBus.getDefault().post(new CallPusher(AppConstants.EVENT_BUS_STATUS_CHANGED, AppConstants.USER_COMPLETED, null));
            } else if (iceConnectionState == PeerConnection.IceConnectionState.DISCONNECTED) {
                removePeer(id);
                EventBus.getDefault().post(new CallPusher(AppConstants.EVENT_BUS_STATUS_CHANGED, AppConstants.USER_DISCONNECT, null));
            } else if (iceConnectionState == PeerConnection.IceConnectionState.FAILED || iceConnectionState == PeerConnection.IceConnectionState.CLOSED) {
                EventBus.getDefault().post(new CallPusher(AppConstants.EVENT_BUS_STATUS_CHANGED, AppConstants.USER_CLOSED, null));
            }
        }

        @Override
        public void onIceConnectionReceivingChange(boolean b) {

        }

        @Override
        public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
            AppHelper.LogCat("onIceGatheringChange " + iceGatheringState);
        }

        @Override
        public void onIceCandidate(final IceCandidate candidate) {
            try {
                JSONObject payload = new JSONObject();
                payload.put("sdpMLineIndex", candidate.sdpMLineIndex);
                payload.put("sdpMid", candidate.sdpMid);
                payload.put("candidate", candidate.sdp);
                signalingServer(id, "candidate", payload);
            } catch (JSONException e) {
                AppHelper.LogCat(" onIceCandidate JSONException " + e.getMessage());
            }
        }

        @Override
        public void onAddStream(MediaStream mediaStream) {
            AppHelper.LogCat("onAddStream " + mediaStream.label());
            // remote streams are displayed from 1 to MAX_PEER_CONNECTIONS (0 is localStream)
            EventBus.getDefault().post(new CallPusher(AppConstants.EVENT_BUS_ADD_REMOTE_STREAM, endPoint + 1, mediaStream));
        }

        @Override
        public void onRemoveStream(MediaStream mediaStream) {
            Log.d(TAG, "onRemoveStream " + mediaStream.label());
            removePeer(id);
        }

        @Override
        public void onDataChannel(DataChannel dataChannel) {
        }

        @Override
        public void onRenegotiationNeeded() {
            AppHelper.LogCat("onRenegotiationNeeded");

        }

        Peer(String id, int endPoint) {
            AppHelper.LogCat("Peer new Peer: " + id + " " + endPoint);
            peerConnection = peerConnectionFactory.createPeerConnection(iceServers, mediaConstraints, this);
            this.id = id;
            this.endPoint = endPoint;
            peerConnection.addStream(mediaStream);
            EventBus.getDefault().post(new CallPusher(AppConstants.EVENT_BUS_STATUS_CHANGED, AppConstants.USER_CONNECTING, null));
        }
    }

    private Peer addPeer(String id, int endPoint) {
        Peer peer = new Peer(id, endPoint);
        peers.put(id, peer);
        endPoints[endPoint] = true;
        return peer;
    }

    private void removePeer(String id) {
        Peer peer = peers.get(id);
        EventBus.getDefault().post(new CallPusher(AppConstants.EVENT_BUS_REMOVE_REMOTE_STREAM, peer.endPoint));
        peer.peerConnection.close();
        peers.remove(peer.id);
        endPoints[peer.endPoint] = false;
    }

    public WebRtcClient(Activity mActivity, PeerConnectionParameters params, String currentUserConnectedID, String callerSocketId, boolean isVideoCall, boolean isAccepted) {
        this.mActivity = mActivity;
        this.isAccepted = isAccepted;
        peerConnectionParameters = params;
        //Params are context, initAudio,initVideo and videoCodecHwAcceleration
        PeerConnectionFactory.initializeAndroidGlobals(mActivity, true, isVideoCall, params.videoCodecHwAcceleration);
        peerConnectionFactory = new PeerConnectionFactory();
        SignalingServerHandler signalingServerHandler = new SignalingServerHandler();

        DostChatApp app = (DostChatApp) mActivity.getApplication();
        mSocket = app.getSocket();
        if (mSocket == null) {
            DostChatApp.connectSocket();
            mSocket = app.getSocket();
        }

        mSocket.on(AppConstants.SOCKET_REJECT_NEW_CALL, signalingServerHandler.onRejectResponse);
        mSocket.on(AppConstants.SOCKET_ACCEPT_NEW_CALL, signalingServerHandler.onAcceptResponse);
        mSocket.on(AppConstants.SOCKET_HANGUP_CALL, signalingServerHandler.onHangUpCallResponse);
        mSocket.on(AppConstants.SOCKET_SIGNALING_SERVER, signalingServerHandler.onSignalingServerResponse);
        if (mSocket == null) return;
        if (!mSocket.connected())
            mSocket.connect();
        if (this.isAccepted) {
            start(currentUserConnectedID, callerSocketId);
        } else {

            try {
                JSONObject message = new JSONObject();
                message.put("userSocketId", currentUserConnectedID);
                mSocket.emit(AppConstants.SOCKET_RESET_SOCKET_ID, message, (Ack) args -> {
                    JSONObject data = (JSONObject) args[0];
                    try {
                        String id = data.getString("userSocketId");
                        if (!id.equals(PreferenceManager.getSocketID(DostChatApp.getInstance()))) {
                            mActivity.finish();
                        } else {
                            start(id, callerSocketId);
                            EventBus.getDefault().post(new CallPusher(AppConstants.EVENT_BUS_CALL_READY, id));
                        }

                    } catch (JSONException e) {
                        AppHelper.LogCat(" onGetUserSocketId JSONException " + e.getMessage());
                    }
                });
            } catch (JSONException e) {
                AppHelper.LogCat("JSONException " + e.getMessage());
            }
        }


        //   defaultIceServers();

        iceServers.add(new PeerConnection.IceServer("stun:stun.l.google.com:19302"));
        iceServers.add(new PeerConnection.IceServer("stun:stun1.l.google.com:19302"));
        iceServers.add(new PeerConnection.IceServer("stun:stun2.l.google.com:19302"));
        iceServers.add(new PeerConnection.IceServer("stun:stun3.l.google.com:19302"));
        iceServers.add(new PeerConnection.IceServer("stun:stun4.l.google.com:19302"));

        mediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        mediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", isVideoCall ? "true" : "false"));
        mediaConstraints.optional.add(new MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"));


    }


    private int findEndPoint() {
        for (int i = 0; i < MAX_PEER_CONNECTIONS; i++) if (!endPoints[i]) return i;
        return MAX_PEER_CONNECTIONS;
    }


    private void closeConnection(String id) {
        if (!this.peers.containsKey(id)) return;
        removePeer(id);
    }

    /**
     * Close connections (hangup) on all open connections.
     */
    public void closeAllConnections() {
        Iterator<String> peerIds = this.peers.keySet().iterator();
        while (peerIds.hasNext()) {
            closeConnection(peerIds.next());
        }
        stopMedia();

    }


    public void hangUpCall(JSONObject message) {
        if (mSocket == null) return;
        mSocket.emit(AppConstants.SOCKET_HANGUP_CALL, message);
    }

    /**
     * method to initialize a new call with the second peer
     *
     * @param callerSocketId this the first parameter of startNewCall method
     * @param from           this the first parameter of startNewCall method
     * @param callerPhone    this the second parameter of   startNewCall method
     */
    public void startNewCall(String callerSocketId, String from, String callerPhone, String callerImage, int callerID, boolean isVideoCall) {
        JSONObject message = new JSONObject();
        try {
            message.put("to", callerSocketId);
            message.put("callerPhone", callerPhone);
            if (callerImage == null)
                message.put("callerImage", "null");
            else
                message.put("callerImage", callerImage);
            message.put("from", from);
            message.put("callerID", callerID);
            message.put("isVideoCall", isVideoCall);
            mSocket.emit(AppConstants.SOCKET_MAKE_NEW_CALL, message);
        } catch (JSONException e) {
            AppHelper.LogCat(" startNewCall JSONException " + e.getMessage());
        }
    }

    public void start(String userSocketId, String callerSocketId) {
        setMedia(callerSocketId, userSocketId);
    }

    private void stopMedia() {
        for (Peer peer : peers.values()) {
            peer.peerConnection.dispose();
        }

        if (peerConnectionParameters.videoCallEnabled) {

            if (videoCapturer != null && !videoCapturerAndroid.isReleased()) {
                AppHelper.LogCat("stopMedia");
                videoCapturer = null;
                videoCapturerAndroid = null;
            }
            if (videoSource != null) {
                videoSource.stop();
                videoSource = null;
            }
        }

        if (audioSource != null) {
            audioSource = null;
        }

    }

    private void setMedia(String callerSocketId, String userSocketId) {
        mediaStream = peerConnectionFactory.createLocalMediaStream(PEER_CONNECTION_ID);
        if (peerConnectionParameters.videoCallEnabled) {
            if (PermissionHandler.checkPermission(mActivity, Manifest.permission.CAMERA)) {

                AppHelper.LogCat("camera permission already granted.");
                if (hasCameraDevice()) {
                    MediaConstraints videoConstraints = new MediaConstraints();
                    videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair(MAX_VIDEO_HEIGHT_CONSTRAINT, Integer.toString(peerConnectionParameters.videoHeight)));
                    videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair(MAX_VIDEO_WIDTH_CONSTRAINT, Integer.toString(peerConnectionParameters.videoWidth)));
                    //videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair(MIN_VIDEO_WIDTH_CONSTRAINT, Integer.toString(peerConnectionParameters.videoWidth)));
                    // videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair(MIN_VIDEO_HEIGHT_CONSTRAINT, Integer.toString(peerConnectionParameters.videoHeight)));
                    videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair(MAX_VIDEO_FPS_CONSTRAINT, Integer.toString(peerConnectionParameters.videoFps)));
                    videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair(MIN_VIDEO_FPS_CONSTRAINT, Integer.toString(peerConnectionParameters.videoFps)));

                    videoCapturer = getVideoCapturer(true, userSocketId, callerSocketId);
                    if (videoCapturer != null) {
                        videoSource = peerConnectionFactory.createVideoSource(videoCapturer, videoConstraints);
                        mediaStream.addTrack(peerConnectionFactory.createVideoTrack(VIDEO_TRACK_ID, videoSource));
                    } else {
                        AppHelper.LogCat("videoCapturer is null ");
                    }
                } else {
                    hangUpCall(callerSocketId, userSocketId, AppConstants.NO_CAMERA);
                }

                cameraSwitchHandler = new VideoCapturerAndroid.CameraSwitchHandler() {
                    @Override
                    public void onCameraSwitchDone(boolean b) {
                        AppHelper.LogCat("onCameraSwitchDone " + b);
                    }

                    @Override
                    public void onCameraSwitchError(String s) {
                        AppHelper.LogCat("onCameraSwitchError " + s);
                    }
                };
            } else {
                AppHelper.LogCat("Please request camera  permission.");
                PermissionHandler.requestPermission(mActivity, Manifest.permission.CAMERA);
                hangUpCall(callerSocketId, userSocketId, AppConstants.AN_EXECPTION);
            }
        }

        if (PermissionHandler.checkPermission(mActivity, Manifest.permission.RECORD_AUDIO)) {
            AppHelper.LogCat("Record audio permission already granted.");
            MediaConstraints audioConstraints = new MediaConstraints();
            audioConstraints.mandatory.add(new MediaConstraints.KeyValuePair(AUDIO_ECHO_CANCELLATION_CONSTRAINT, "true"));
            audioConstraints.mandatory.add(new MediaConstraints.KeyValuePair(AUDIO_ECHO_CANCELLATION2_CONSTRAINT, "true"));
            audioConstraints.mandatory.add(new MediaConstraints.KeyValuePair(AUDIO_AUTO_GAIN_CONTROL_CONSTRAINT, "true"));
            audioConstraints.mandatory.add(new MediaConstraints.KeyValuePair(AUDIO_AUTO_GAIN_CONTROL2_CONSTRAINT, "true"));
            audioConstraints.mandatory.add(new MediaConstraints.KeyValuePair(AUDIO_NOISE_SUPPRESSION_CONSTRAINT, "true"));
            audioConstraints.mandatory.add(new MediaConstraints.KeyValuePair(AUDIO_NOISE_SUPPRESSION2_CONSTRAINT, "true"));
            audioConstraints.mandatory.add(new MediaConstraints.KeyValuePair(AUDIO_HIGH_PASS_FILTER_CONSTRAINT, "true"));

            audioSource = peerConnectionFactory.createAudioSource(audioConstraints);
            mediaStream.addTrack(peerConnectionFactory.createAudioTrack(AUDIO_TRACK_ID, audioSource));

        } else {
            AppHelper.LogCat("Please request Record audio permission.");
            PermissionHandler.requestPermission(mActivity, Manifest.permission.RECORD_AUDIO);
            hangUpCall(callerSocketId, userSocketId, AppConstants.AN_EXECPTION);
        }
        EventBus.getDefault().post(new CallPusher(AppConstants.EVENT_BUS_LOCAL_STREAM, mediaStream));

    }

    private void hangUpCall(String callerSocketId, String userSocketId, String reason) {
        try {
            JSONObject messageJSON = new JSONObject();
            messageJSON.put("callerSocketId", callerSocketId);
            messageJSON.put("userSocketId", userSocketId);
            messageJSON.put("reason", reason);
            hangUpCall(messageJSON);
            this.mActivity.finish();
        } catch (JSONException e) {
            AppHelper.LogCat("JSONException webrtc rejectCall " + e.getMessage());
        }
    }


    private VideoCapturerAndroid getVideoCapturer(boolean isFrontCameraName, String userSocketId, String callerSocketId) {
        if (!cameraIsOpened) {
            videoCapturerAndroid = VideoCapturerAndroid.create(isFrontCameraName ? mFrontCameraName : mBackCameraName, new VideoCapturerAndroid.CameraEventsHandler() {
                @Override
                public void onCameraError(String s) {
                    AppHelper.LogCat("onCameraError " + s);
                    cameraIsOpened = false;
                }

                @Override
                public void onCameraFreezed(String s) {
                    AppHelper.LogCat("onCameraFreezed " + s);
                    cameraIsOpened = false;
                }

                @Override
                public void onCameraOpening(int i) {
                    AppHelper.LogCat("onCameraOpening " + i);
                    cameraIsOpened = i != 0;
                    if (cameraIsOpened && isAccepted) {
                        try {
                            JSONObject message = new JSONObject();
                            message.put("userSocketId", userSocketId);
                            message.put("callerSocketId", callerSocketId);
                            mSocket.emit(AppConstants.SOCKET_ACCEPT_NEW_CALL, message);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onFirstFrameAvailable() {
                    AppHelper.LogCat("onFirstFrameAvailable ");
                    cameraIsOpened = true;
                }

                @Override
                public void onCameraClosed() {
                    AppHelper.LogCat("onCameraClosed ");
                    cameraIsOpened = false;
                }
            });
            return videoCapturerAndroid;
        } else {
            return null;
        }
    }


    /**
     * @return true if the device has a camera device
     */

    private boolean hasCameraDevice() {
        try {
            mFrontCameraName = CameraEnumerationAndroid.getNameOfFrontFacingDevice();
            mBackCameraName = CameraEnumerationAndroid.getNameOfBackFacingDevice();
            numberOfCameras = CameraEnumerationAndroid.getDeviceCount();
        } catch (Exception e) {

            AppHelper.LogCat(" hasCameraDevice Exception " + e.getMessage());
        }

        return (mFrontCameraName != null) || (mBackCameraName != null);
    }


    private void switchCameraInternal() {
        if (!peerConnectionParameters.videoCallEnabled || numberOfCameras < 2) {
            AppHelper.LogCat("Failed to switch camera. Video: " + peerConnectionParameters.videoCallEnabled + ". Number of cameras: " + numberOfCameras);
            return;  // No video is sent or only one camera is available or error happened.
        }
        if (videoCapturerAndroid != null)
            videoCapturerAndroid.switchCamera(cameraSwitchHandler);


    }

    public boolean toggleMic() {
        LinkedList<AudioTrack> audioTracks = mediaStream.audioTracks;
        if (audioTracks != null) {
            if (audioTracks.size() != 0) {
                for (AudioTrack audioTrack : audioTracks) {
                    if (audioTrack.enabled()) {
                        audioTrack.setEnabled(false);
                        return false;
                    } else {
                        audioTrack.setEnabled(true);
                        return true;
                    }
                }

            } else {
                mActivity.runOnUiThread(() -> AppHelper.CustomToast(mActivity, "You can't disable/enable mic"));
                return false;
            }
        }
        return false;
    }

    public void switchCamera(Activity activity) {
        activity.runOnUiThread(this::switchCameraInternal);
    }

}
