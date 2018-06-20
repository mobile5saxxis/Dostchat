package com.dostchat.dost.models.calls;

import org.webrtc.MediaStream;

/**
 * Created by Abderrahim El imame on 3/8/17.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */

public class CallPusher {

    private String event;
    private String connectionStatus;
    private String callId;
    private int endPoint;
    private MediaStream mediaStream;

    public CallPusher(String event, String connectionStatus, String callId) {
        this.event = event;
        this.connectionStatus = connectionStatus;
        this.callId = callId;

    }

    public CallPusher(String event, MediaStream mediaStream) {
        this.event = event;
        this.mediaStream = mediaStream;
    }

    public CallPusher(String event, int endPoint, MediaStream mediaStream) {
        this.event = event;
        this.endPoint = endPoint;
        this.mediaStream = mediaStream;
    }

    public CallPusher(String event, int endPoint) {
        this.event = event;
        this.endPoint = endPoint;
    }

    public CallPusher(String event, String callId) {
        this.event = event;
        this.callId = callId;
    }

    public CallPusher(String event) {
        this.event = event;
    }

    public String getConnectionStatus() {
        return connectionStatus;
    }

    public void setConnectionStatus(String connectionStatus) {
        this.connectionStatus = connectionStatus;
    }

    public String getCallId() {
        return callId;
    }

    public void setCallId(String callId) {
        this.callId = callId;
    }

    public MediaStream getMediaStream() {
        return mediaStream;
    }

    public void setMediaStream(MediaStream mediaStream) {
        this.mediaStream = mediaStream;
    }

    public int getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(int endPoint) {
        this.endPoint = endPoint;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }
}
