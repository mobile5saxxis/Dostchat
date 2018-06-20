package com.dostchat.dost.services;

import android.app.Activity;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;

import com.dostchat.dost.R;
import com.dostchat.dost.activities.call.IncomingCallActivity;
import com.dostchat.dost.activities.messages.MessagesActivity;
import com.dostchat.dost.activities.popups.MessagesPopupActivity;
import com.dostchat.dost.app.AppConstants;
import com.dostchat.dost.app.DostChatApp;
import com.dostchat.dost.helpers.AppHelper;
import com.dostchat.dost.helpers.Files.backup.RealmBackupRestore;
import com.dostchat.dost.helpers.PreferenceManager;
import com.dostchat.dost.helpers.UtilsPhone;
import com.dostchat.dost.helpers.UtilsString;
import com.dostchat.dost.helpers.notifications.NotificationsManager;
import com.dostchat.dost.models.groups.GroupsModel;
import com.dostchat.dost.models.groups.MembersGroupModel;
import com.dostchat.dost.models.messages.ConversationsModel;
import com.dostchat.dost.models.messages.MessagesModel;
import com.dostchat.dost.models.notifications.NotificationsModel;
import com.dostchat.dost.models.users.Pusher;
import com.dostchat.dost.models.users.contacts.ContactsModel;
import com.dostchat.dost.models.users.contacts.PusherContacts;
import com.dostchat.dost.models.users.contacts.UsersBlockModel;
import com.dostchat.dost.receivers.MessagesReceiverBroadcast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import de.greenrobot.event.EventBus;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;
import io.socket.client.Ack;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import static com.dostchat.dost.app.AppConstants.EVENT_BUS_MESSAGE_COUNTER;
import static com.dostchat.dost.app.AppConstants.EVENT_BUS_MESSAGE_IS_DELIVERED_FOR_CONVERSATIONS;
import static com.dostchat.dost.app.AppConstants.EVENT_BUS_MESSAGE_IS_SEEN_FOR_CONVERSATIONS;
import static com.dostchat.dost.app.AppConstants.EVENT_BUS_NEW_MESSAGE_IS_SENT_FOR_CONVERSATIONS;


/**
 * Created by Abderrahim El imame on 6/21/16.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/bencherif_el
 */

public class MainService extends IntentService {

    private Context mContext;
    public static Socket mSocket;
    private MessagesReceiverBroadcast mChangeListener;
    private Intent mIntent;
    private static Handler handler;
    private int mTries = 0;
    //private static Handler handlerUserCheckIfOnline;
    // private static final int NOTIFY_INTERVAL = 60 * 1000; // 60 seconds

    private static boolean isUnSentMessagesExecuted = false;


    //to keep socket connected
    PowerManager.WakeLock wakeLock;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public MainService() {
        super(AppConstants.TAG);
    }


    /**
     * method to disconnect user form server
     */
    public void disconnectSocket() {

        if (mSocket != null) {

            JSONObject jsonConnected = new JSONObject();
            try {
                jsonConnected.put("connected", false);
                jsonConnected.put("connectedId", PreferenceManager.getID(DostChatApp.getInstance()));
                jsonConnected.put("userToken", PreferenceManager.getToken(DostChatApp.getInstance()));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (mSocket != null)
                mSocket.emit(AppConstants.SOCKET_CONNECTED, jsonConnected);

            mSocket.off(Socket.EVENT_CONNECT);
            mSocket.off(Socket.EVENT_DISCONNECT);
            mSocket.off(Socket.EVENT_CONNECT_TIMEOUT);
            mSocket.off(Socket.EVENT_RECONNECT);

            mSocket.off(AppConstants.SOCKET_EVENT_PING, onPing);
            mSocket.off(AppConstants.SOCKET_EVENT_PONG);
            mSocket.off(AppConstants.SOCKET_IS_ONLINE);
            mSocket.off(AppConstants.SOCKET_NEW_MESSAGE);
            //user messages

            mSocket.off(AppConstants.SOCKET_SAVE_NEW_MESSAGE);
            mSocket.off(AppConstants.SOCKET_IS_LAST_SEEN);
            mSocket.off(AppConstants.SOCKET_IS_MESSAGE_DELIVERED);
            mSocket.off(AppConstants.SOCKET_IS_MESSAGE_SEEN);
            mSocket.off(AppConstants.SOCKET_IS_STOP_TYPING);
            mSocket.off(AppConstants.SOCKET_IS_TYPING);
            mSocket.off(AppConstants.SOCKET_IS_MESSAGE_SENT);
            mSocket.off(AppConstants.SOCKET_USER_PING);
            mSocket.off(AppConstants.SOCKET_CONNECTED);
            mSocket.off(AppConstants.SOCKET_DISCONNECTED);
            mSocket.off(AppConstants.SOCKET_NEW_USER_JOINED);
            mSocket.off(AppConstants.SOCKET_IMAGE_PROFILE_UPDATED);
            mSocket.off(AppConstants.SOCKET_IMAGE_GROUP_UPDATED);
            //groups
            mSocket.off(AppConstants.SOCKET_USER_PING_GROUP);
            mSocket.off(AppConstants.SOCKET_IS_MESSAGE_GROUP_SEND);
            mSocket.off(AppConstants.SOCKET_USER_PINGED_GROUP);
            mSocket.off(AppConstants.SOCKET_NEW_MESSAGE_GROUP);
            mSocket.off(AppConstants.SOCKET_NEW_MESSAGE_GROUP_SERVER);
            mSocket.off(AppConstants.SOCKET_SAVE_NEW_MESSAGE_GROUP);
            mSocket.off(AppConstants.SOCKET_IS_MEMBER_STOP_TYPING);
            mSocket.off(AppConstants.SOCKET_IS_MEMBER_TYPING);
            mSocket.off(AppConstants.SOCKET_IS_MESSAGE_GROUP_DELIVERED);
            mSocket.off(AppConstants.SOCKET_IS_MESSAGE_GROUP_SENT);
            mSocket.off(AppConstants.SOCKET_NEW_MESSAGE_SERVER);

            //calls
            mSocket.off(AppConstants.SOCKET_CALL_USER_PING);
            mSocket.off(AppConstants.SOCKET_RESET_SOCKET_ID);
            mSocket.off(AppConstants.SOCKET_SIGNALING_SERVER);
            mSocket.off(AppConstants.SOCKET_MAKE_NEW_CALL);
            mSocket.off(AppConstants.SOCKET_RECEIVE_NEW_CALL);
            mSocket.off(AppConstants.SOCKET_REJECT_NEW_CALL);
            mSocket.off(AppConstants.SOCKET_ACCEPT_NEW_CALL);
            mSocket.off(AppConstants.SOCKET_HANGUP_CALL);


            mSocket.disconnect();
            mSocket.close();
            mSocket = null;

        }
        AppHelper.LogCat("disconnect in service");
    }

    /**
     * method for server connection initialization
     */
    public void connectToServer(Context mContext) {
        DostChatApp.connectSocket();
        DostChatApp app = (DostChatApp) getApplication();

        mSocket = app.getSocket();
        mSocket.once(Socket.EVENT_CONNECT, args -> {
            AppHelper.LogCat("New Connection chat is created ");
            mTries = 0;
            JSONObject json = new JSONObject();
            try {
                json.put("connected", true);
                json.put("connectedId", PreferenceManager.getID(mContext));
                json.put("userToken", PreferenceManager.getToken(mContext));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (mSocket != null)
                mSocket.emit(AppConstants.SOCKET_CONNECTED, json);


            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("connected", true);
                jsonObject.put("senderId", PreferenceManager.getID(mContext));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (mSocket != null)
                mSocket.emit(AppConstants.SOCKET_IS_ONLINE, jsonObject);

            isUserConnected(mContext);

        }).on(Socket.EVENT_DISCONNECT, args -> {
            AppHelper.LogCat("You  lost connection to chat server ");
            JSONObject json = new JSONObject();
            try {
                json.put("connected", false);
                json.put("senderId", PreferenceManager.getID(mContext));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (mSocket != null)
                mSocket.emit(AppConstants.SOCKET_IS_ONLINE, json);


            JSONObject jsonConnected = new JSONObject();
            try {
                jsonConnected.put("connectedId", PreferenceManager.getID(mContext));
                jsonConnected.put("userToken", PreferenceManager.getToken(mContext));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (mSocket != null)
                mSocket.emit(AppConstants.SOCKET_DISCONNECTED, jsonConnected);

        }).on(Socket.EVENT_CONNECT_TIMEOUT, args -> {
            AppHelper.LogCat("Socket timeout ");
            reconnect(mContext);
        }).on(Socket.EVENT_RECONNECT, args -> {
            AppHelper.LogCat("Reconnect");
            reconnect(mContext);
        }).on(AppConstants.SOCKET_EVENT_PING, onPing);

        onReceivedNewMessage();
        onReceivedNewMessageGroup();
        sendPongToSender();
        SenderMarkMessageAsDelivered();
        SenderMarkMessageAsSeen();
        MemberMarkMessageAsSent();
        MemberMarkMessageAsDelivered();
        notifyOtherUser();
        getNotifyFromOtherNewUser();
        getNotifyForImageProfileChanged();
        onReceiveNewCall();
        checkIfUserIsOnline();


    }

    /**
     * method to reconnect sockets
     */
    public void reconnect(Context mContext) {
        if (mTries < 5) {
            mTries++;
            AppHelper.restartService();
            handler.postDelayed(() -> updateStatusDeliveredOffline(mContext), 1500);
        }

    }

    private Emitter.Listener onPing = args -> {
//        AppHelper.LogCat("socket ping");
        if (mSocket == null) {
            DostChatApp.connectSocket();
            DostChatApp app = (DostChatApp) getApplication();
            mSocket = app.getSocket();
        }


        if (mSocket != null) {
            if (!mSocket.connected())
                mSocket.connect();

            JSONObject data = (JSONObject) args[0];
            String ping;
            try {
                ping = data.getString("beat");
            } catch (JSONException e) {
                return;
            }
            if (ping.equals("1")) {
                mSocket.emit(AppConstants.SOCKET_EVENT_PONG);
            }

        }
    };

    /**
     * method to receive notification if a new user Joined
     */
    private void getNotifyFromOtherNewUser() {
        mSocket.on(AppConstants.SOCKET_NEW_USER_JOINED, args -> {
            final JSONObject jsonObject = (JSONObject) args[0];
            try {
                int senderId = jsonObject.getInt("senderId");
                String phone = jsonObject.getString("phone");
                if (senderId == PreferenceManager.getID(mContext)) return;
                if (UtilsPhone.checkIfContactExist(mContext, phone)) {
                    EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_NEW_USER_JOINED, jsonObject));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }

    private boolean checkIfGroupExist(int groupId, Realm realm) {
        RealmQuery<GroupsModel> query = realm.where(GroupsModel.class).equalTo("id", groupId);
        return query.count() != 0;

    }

    /**
     * method when a user change the image profile
     */
    private void getNotifyForImageProfileChanged() {
        mSocket.on(AppConstants.SOCKET_IMAGE_PROFILE_UPDATED, args -> {
            final JSONObject jsonObject = (JSONObject) args[0];
            try {
                int senderId = jsonObject.getInt("senderId");
                String phone = jsonObject.getString("phone");
                if (senderId == PreferenceManager.getID(mContext)) return;
                if (UtilsPhone.checkIfContactExist(mContext, phone)) {
                    EventBus.getDefault().post(new PusherContacts(AppConstants.EVENT_BUS_IMAGE_PROFILE_UPDATED));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
        mSocket.on(AppConstants.SOCKET_IMAGE_GROUP_UPDATED, args -> {
            final JSONObject jsonObject = (JSONObject) args[0];
            Realm realm = DostChatApp.getRealmDatabaseInstance();
            try {
                int groupId = jsonObject.getInt("groupId");
                if (!checkIfGroupExist(groupId, realm)) return;
                EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_IMAGE_GROUP_UPDATED, groupId));

            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (!realm.isClosed()) realm.close();
        });
    }

    /**
     * method to send notification if i join to the app
     */
    private void notifyOtherUser() {
        if (PreferenceManager.isNewUser(mContext)) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("senderId", PreferenceManager.getID(mContext));
                jsonObject.put("phone", PreferenceManager.getPhone(mContext));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            mSocket.emit(AppConstants.SOCKET_NEW_USER_JOINED, jsonObject);
            PreferenceManager.setIsNewUser(mContext, false);
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();

        PowerManager pMgr = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pMgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "WhatsCloneWakeLock");
        wakeLock.acquire();
    }

/*
    *//**
     * method to emit that  user is connect to server
     *
     * @param mContext
     *//*
    private void pingConnectToServer(Context mContext) {
        int delay = NOTIFY_INTERVAL;
        DostChatApp app = (DostChatApp) getApplication();
        mSocket = app.getSocket();
        handlerUserCheckIfOnline.postDelayed(new Runnable() {
            public void run() {
                if (!mSocket.connected())
                    mSocket.connect();
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("connected", true);
                    jsonObject.put("senderId", PreferenceManager.getID(mContext));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (mSocket != null)
                    mSocket.emit(AppConstants.SOCKET_IS_ONLINE, jsonObject);
                handlerUserCheckIfOnline.postDelayed(this, delay);
            }
        }, delay);
    }*/

    /**
     * method to check if user is online or not
     */
    private void checkIfUserIsOnline() {
        if (mSocket != null) {
            mSocket.on(AppConstants.SOCKET_IS_ONLINE, args -> {
                Realm realm = DostChatApp.getRealmDatabaseInstance();
                RealmResults<ContactsModel> contactsModels = realm.where(ContactsModel.class).notEqualTo("id", PreferenceManager.getID(mContext)).equalTo("Exist", true).equalTo("Linked", true).equalTo("Activate", true).findAllSorted("username", Sort.ASCENDING);
                final JSONObject data = (JSONObject) args[0];
                try {
                    int senderID = data.getInt("senderId");
                    for (ContactsModel contactsModels1 : contactsModels) {
                        if (senderID == contactsModels1.getId()) {
                            if (data.getBoolean("connected")) {
                                realm.executeTransaction(realm1 -> {
                                    ContactsModel userModel = realm1.where(ContactsModel.class).equalTo("id", contactsModels1.getId()).findFirst();
                                    userModel.setUserState(mContext.getString(R.string.isOnline));
                                    realm.copyToRealmOrUpdate(userModel);
                                });
                            } else {
                                realm.executeTransaction(realm1 -> {
                                    ContactsModel userModel = realm1.where(ContactsModel.class).equalTo("id", contactsModels1.getId()).findFirst();
                                    userModel.setUserState(mContext.getString(R.string.isOffline));
                                    realm.copyToRealmOrUpdate(userModel);
                                });
                            }
                        }
                    }
                } catch (JSONException e) {
                    AppHelper.LogCat(e);
                }
                realm.close();
            });
        }
    }

    /**
     * method to check if user is connected to server
     *
     * @param mContext
     */
    private static void isUserConnected(Context mContext) {
        if (mSocket != null) {
            mSocket.on(AppConstants.SOCKET_CONNECTED, args -> {
                final JSONObject data = (JSONObject) args[0];
                try {
                    int connectedId = data.getInt("connectedId");
                    String socketId = data.getString("socketId");
                    boolean connected = data.getBoolean("connected");

                    if (connectedId != PreferenceManager.getID(mContext)) {
                        try {
                            Realm realm = DostChatApp.getRealmDatabaseInstance();
                            ContactsModel contactsModel = realm.where(ContactsModel.class).equalTo("id", connectedId).findFirst();
                            if (contactsModel != null && UtilsPhone.checkIfContactExist(mContext, contactsModel.getPhone())) {
                                if (connected) {
                                    AppHelper.LogCat("User with id  --> " + connectedId + " is connected <---");
                                    EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_UPDATE_USER_STATE, mContext.getString(R.string.isOnline)));
                                    if (!isUnSentMessagesExecuted)
                                        unSentMessages(mContext, connectedId);


                                } else {
                                    EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_UPDATE_USER_STATE, mContext.getString(R.string.isOffline)));
                                    AppHelper.LogCat("User with id  --> " + connectedId + " is disconnected  <---");
                                }
                            }
                            realm.close();
                        } catch (Exception e) {
                            AppHelper.LogCat(" isUserConnected Exception mainService " + e.getMessage());
                        }
                    } else {
                        if (connected)
                            PreferenceManager.setSocketID(mContext, socketId);
                        else
                            PreferenceManager.setSocketID(mContext, null);
                        AppHelper.LogCat("You  are connected with socket id --> " + PreferenceManager.getSocketID(mContext) + " <---");
                    }

                } catch (JSONException e) {
                    AppHelper.LogCat(e);
                }

            });
        }
    }

    private static boolean checkIfUnsentMessagesExist(int recipientId, Realm realm, Context mContext) {
        RealmQuery<MessagesModel> query = realm.where(MessagesModel.class)
                .equalTo("status", AppConstants.IS_WAITING)
                .equalTo("recipientID", recipientId)
                .equalTo("isGroup", false)
                .equalTo("isFileUpload", true)
                .equalTo("senderID", PreferenceManager.getID(mContext));

        return query.count() != 0;

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        AppHelper.LogCat("MainService  has started");

        if (PreferenceManager.getToken(mContext) != null) {
            handler = new Handler();
            // handlerUserCheckIfOnline = new Handler();
            connectToServer(mContext);
            mChangeListener = new MessagesReceiverBroadcast() {
                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                @Override
                protected void MessageReceived(Context context, Intent intent) {
                    String action = intent.getAction();
                    switch (action) {
                        case "new_user_message_notification_whatsclone":
                            String Application = intent.getExtras().getString("app");
                            String file = intent.getExtras().getString("file");
                            String userphone = intent.getExtras().getString("phone");
                            String messageBody = intent.getExtras().getString("message");
                            int recipientId = intent.getExtras().getInt("recipientID");
                            int senderId = intent.getExtras().getInt("senderId");
                            int conversationID = intent.getExtras().getInt("conversationID");
                            String userImage = intent.getExtras().getString("userImage");

                            /**
                             * this for default activity
                             */
                            Intent messagingIntent = new Intent(mContext, MessagesActivity.class);
                            messagingIntent.putExtra("conversationID", conversationID);
                            messagingIntent.putExtra("recipientID", recipientId);
                            messagingIntent.putExtra("isGroup", false);
                            messagingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            /**
                             * this for popup activity
                             */
                            Intent messagingPopupIntent = new Intent(mContext, MessagesPopupActivity.class);
                            messagingPopupIntent.putExtra("conversationID", conversationID);
                            messagingPopupIntent.putExtra("recipientID", recipientId);
                            messagingPopupIntent.putExtra("isGroup", false);
                            messagingPopupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                            if (Application != null && Application.equals(mContext.getPackageName())) {
                                if (AppHelper.isActivityRunning(mContext, "activities.messages.MessagesActivity")) {
                                    NotificationsModel notificationsModel = new NotificationsModel();
                                    notificationsModel.setConversationID(conversationID);
                                    notificationsModel.setFile(file);
                                    notificationsModel.setGroup(false);
                                    notificationsModel.setImage(userImage);
                                    notificationsModel.setPhone(userphone);
                                    notificationsModel.setMessage(messageBody);
                                    notificationsModel.setRecipientId(recipientId);
                                    notificationsModel.setSenderId(senderId);
                                    notificationsModel.setAppName(Application);
                                    EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_NEW_USER_NOTIFICATION, notificationsModel));
                                } else {
                                    if (file != null) {
                                        NotificationsManager.showUserNotification(mContext, messagingIntent, messagingPopupIntent, userphone, file, recipientId, userImage);
                                    } else {
                                        NotificationsManager.showUserNotification(mContext, messagingIntent, messagingPopupIntent, userphone, messageBody, recipientId, userImage);
                                    }
                                }
                            }


                            break;
                        case "new_group_message_notification_whatsclone":
                            String application = intent.getExtras().getString("app");
                            String File = intent.getExtras().getString("file");
                            String userPhone = intent.getExtras().getString("senderPhone");
                            String groupName = UtilsString.unescapeJava(intent.getExtras().getString("groupName"));
                            String messageGroupBody = intent.getExtras().getString("message");
                            int groupID = intent.getExtras().getInt("groupID");
                            String groupImage = intent.getExtras().getString("groupImage");
                            int conversationId = intent.getExtras().getInt("conversationID");
                            String memberName;
                            String name = UtilsPhone.getContactName(mContext, userPhone);
                            if (name != null) {
                                memberName = name;
                            } else {
                                memberName = userPhone;
                            }


                            String message;
                            String userName = UtilsPhone.getContactName(mContext, userPhone);
                            switch (messageGroupBody) {
                                case AppConstants.CREATE_GROUP:
                                    if (userName != null) {
                                        message = "" + userName + mContext.getString(R.string.he_created_this_group);
                                    } else {
                                        message = "" + userPhone + mContext.getString(R.string.he_created_this_group);
                                    }


                                    break;
                                case AppConstants.LEFT_GROUP:
                                    if (userName != null) {
                                        message = "" + userName + mContext.getString(R.string.he_left);
                                    } else {
                                        message = "" + userPhone + mContext.getString(R.string.he_left);
                                    }


                                    break;
                                default:
                                    message = messageGroupBody;
                                    break;
                            }

                            /**
                             * this for default activity
                             */
                            Intent messagingGroupIntent = new Intent(mContext, MessagesActivity.class);
                            messagingGroupIntent.putExtra("conversationID", conversationId);
                            messagingGroupIntent.putExtra("groupID", groupID);
                            messagingGroupIntent.putExtra("isGroup", true);
                            messagingGroupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            /**
                             * this for popup activity
                             */
                            Intent messagingGroupPopupIntent = new Intent(mContext, MessagesPopupActivity.class);
                            messagingGroupPopupIntent.putExtra("conversationID", conversationId);
                            messagingGroupPopupIntent.putExtra("groupID", groupID);
                            messagingGroupPopupIntent.putExtra("isGroup", true);
                            messagingGroupPopupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                            if (application != null && application.equals(mContext.getPackageName())) {
                                if (AppHelper.isActivityRunning(mContext, "activities.messages.MessagesActivity")) {
                                    NotificationsModel notificationsModel = new NotificationsModel();
                                    notificationsModel.setConversationID(conversationId);
                                    notificationsModel.setFile(File);
                                    notificationsModel.setGroup(true);
                                    notificationsModel.setImage(groupImage);
                                    notificationsModel.setPhone(userPhone);
                                    notificationsModel.setMessage(messageGroupBody);
                                    notificationsModel.setMemberName(memberName);
                                    notificationsModel.setGroupID(groupID);
                                    notificationsModel.setGroupName(groupName);
                                    notificationsModel.setAppName(application);
                                    EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_NEW_GROUP_NOTIFICATION, notificationsModel));
                                } else {
                                    if (File != null) {
                                        NotificationsManager.showGroupNotification(mContext, messagingGroupIntent, messagingGroupPopupIntent, groupName, memberName + " : " + File, groupID, groupImage);
                                    } else {
                                        NotificationsManager.showGroupNotification(mContext, messagingGroupIntent, messagingGroupPopupIntent, groupName, memberName + " : " + message, groupID, groupImage);
                                    }
                                }
                            }


                            break;
                        case "new_user_joined_notification_whatsclone":
                            String Userphone = intent.getExtras().getString("phone");
                            String MessageBody = intent.getExtras().getString("message");
                            int RecipientId = intent.getExtras().getInt("recipientID");
                            int ConversationID = intent.getExtras().getInt("conversationID");
                            /**
                             * this for default activity
                             */
                            Intent MessagingIntent = new Intent(mContext, MessagesActivity.class);
                            MessagingIntent.putExtra("conversationID", ConversationID);
                            MessagingIntent.putExtra("recipientID", RecipientId);
                            MessagingIntent.putExtra("isGroup", false);
                            MessagingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            /**
                             * this for popup activity
                             */
                            Intent MessagingPopupIntent = new Intent(mContext, MessagesPopupActivity.class);
                            MessagingPopupIntent.putExtra("conversationID", ConversationID);
                            MessagingPopupIntent.putExtra("recipientID", RecipientId);
                            MessagingPopupIntent.putExtra("isGroup", false);
                            MessagingPopupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            NotificationsManager.showUserNotification(mContext, MessagingIntent, MessagingPopupIntent, Userphone, MessageBody, RecipientId, null);
                            break;
                    }

                }
            };


            getApplication().registerReceiver(mChangeListener, new IntentFilter("new_user_message_notification_whatsclone"));
            getApplication().registerReceiver(mChangeListener, new IntentFilter("new_group_message_notification_whatsclone"));
            getApplication().registerReceiver(mChangeListener, new IntentFilter("new_user_joined_notification_whatsclone"));

        }
        return START_STICKY;
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (wakeLock != null) {
            wakeLock.release();
            wakeLock = null;
        }
        AppHelper.LogCat("MainService has stopped");
        NotificationsManager.SetupBadger(mContext);
        // service finished
        if (mChangeListener != null)
            mContext.unregisterReceiver(mChangeListener);
        disconnectSocket();
        handler.removeCallbacksAndMessages(null);
        // handlerUserCheckIfOnline.removeCallbacksAndMessages(null);

    }


    /**
     * method to check  for unsent messages
     */
    public synchronized static void unSentMessages(Context mContext, int recipientId) {
        Realm realm = DostChatApp.getRealmDatabaseInstance();

        List<MessagesModel> messagesModelsList = realm.where(MessagesModel.class)
                .equalTo("status", AppConstants.IS_WAITING)
                .equalTo("recipientID", recipientId)
                .equalTo("isGroup", false)
                .equalTo("isFileUpload", true)
                .equalTo("senderID", PreferenceManager.getID(mContext))
                .findAllSorted("id", Sort.ASCENDING);
        AppHelper.LogCat("size " + messagesModelsList.size());

        if (messagesModelsList.size() != 0) {

            for (MessagesModel messagesModel : messagesModelsList) {
                sendMessages(messagesModel);
            }
        }
        realm.close();
        isUnSentMessagesExecuted = true;

    }

    /**
     * method to send unsentMessages
     *
     * @param messagesModel this i parameter for sendMessages method
     */
    public static void sendMessages(MessagesModel messagesModel) {
        final JSONObject message = new JSONObject();
        try {
            message.put("messageBody", messagesModel.getMessage());
            message.put("messageId", messagesModel.getId());
            message.put("recipientId", messagesModel.getRecipientID());
            message.put("senderId", messagesModel.getSenderID());
            message.put("senderName", messagesModel.getUsername());
            message.put("phone", messagesModel.getPhone());
            message.put("date", messagesModel.getDate());
            message.put("senderImage", "null");
            message.put("isGroup", messagesModel.isGroup());
            message.put("conversationId", messagesModel.getConversationID());
            message.put("image", messagesModel.getImageFile());
            message.put("video", messagesModel.getVideoFile());
            message.put("thumbnail", messagesModel.getVideoThumbnailFile());
            message.put("audio", messagesModel.getAudioFile());
            message.put("document", messagesModel.getDocumentFile());
            message.put("duration", messagesModel.getDuration());
            message.put("fileSize", messagesModel.getFileSize());
        } catch (JSONException e) {
            AppHelper.LogCat(e);
        }
        if (!messagesModel.isFileUpload()) return;
        if (mSocket != null)
            mSocket.emit(AppConstants.SOCKET_NEW_MESSAGE, message);
    }

    /**
     * method to send unsentMessages who has files
     *
     * @param messagesModel this i parameter for sendMessages method
     */
    public static void sendMessagesFiles(Context mContext, MessagesModel messagesModel) {
        final JSONObject message = new JSONObject();
        try {
            message.put("messageBody", messagesModel.getMessage());
            message.put("messageId", messagesModel.getId());
            message.put("recipientId", messagesModel.getRecipientID());
            message.put("senderId", messagesModel.getSenderID());
            message.put("senderName", messagesModel.getUsername());
            message.put("phone", messagesModel.getPhone());
            message.put("date", messagesModel.getDate());
            message.put("senderImage", "null");
            message.put("isGroup", messagesModel.isGroup());
            message.put("conversationId", messagesModel.getConversationID());
            message.put("image", messagesModel.getImageFile());
            message.put("video", messagesModel.getVideoFile());
            message.put("thumbnail", messagesModel.getVideoThumbnailFile());
            message.put("audio", messagesModel.getAudioFile());
            message.put("document", messagesModel.getDocumentFile());
            message.put("duration", messagesModel.getDuration());
            message.put("fileSize", messagesModel.getFileSize());
            message.put("userToken", PreferenceManager.getToken(mContext));
        } catch (JSONException e) {
            AppHelper.LogCat(e);
        }
        if (!messagesModel.isFileUpload()) return;
        if (mSocket != null) {
            mSocket.emit(AppConstants.SOCKET_NEW_MESSAGE, message);
            mSocket.emit(AppConstants.SOCKET_SAVE_NEW_MESSAGE, message);
        }
    }


    /**
     * method to  update status delivered when user was offline and come online
     * and he has a new messages (unread)
     *
     * @param mContext
     */

    private static void updateStatusDeliveredOffline(Context mContext) {
        Realm realm = DostChatApp.getRealmDatabaseInstance();
        List<MessagesModel> messagesModels = realm.where(MessagesModel.class)
                .notEqualTo("recipientID", PreferenceManager.getID(mContext))
                .equalTo("status", AppConstants.IS_WAITING).findAll();
        if (messagesModels.size() != 0) {
            for (MessagesModel messagesModel : messagesModels) {
                RecipientMarkMessageAsDelivered(mContext, messagesModel.getId());
            }
        }
    }

    /**
     * method to mark messages as delivered by recipient
     *
     * @param mContext
     * @param messageId this is the  parameter for RecipientMarkMessageAsDelivered method
     */
    private static void RecipientMarkMessageAsDelivered(Context mContext, int messageId) {
        try {
            JSONObject json = new JSONObject();
            json.put("senderId", PreferenceManager.getID(mContext));
            json.put("messageId", messageId);
            if (mSocket != null)
                mSocket.emit(AppConstants.SOCKET_IS_MESSAGE_DELIVERED, json);
        } catch (Exception e) {
            AppHelper.LogCat(e);
        }
        AppHelper.LogCat("--> Recipient mark message as  delivered <--");
    }

    /**
     * method to update status for a specific  message (as delivered by sender)
     */
    private void SenderMarkMessageAsDelivered() {

        mSocket.on(AppConstants.SOCKET_IS_MESSAGE_DELIVERED, args -> {

            JSONObject data = (JSONObject) args[0];
            try {
                int senderId = data.getInt("senderId");
                if (senderId == PreferenceManager.getID(mContext))
                    return;
                updateDeliveredStatus(data);
                AppHelper.LogCat("--> Sender mark message as  delivered: update status  <--");

            } catch (Exception e) {
                AppHelper.LogCat(e);
            }

        });
    }


    /**
     * method to update status for a specific  message (as delivered by sender) in realm database
     *
     * @param data this is parameter for  updateDeliveredStatus
     */
    private void updateDeliveredStatus(JSONObject data) {
        isUnSentMessagesExecuted = false;
        try {
            int messageId = data.getInt("messageId");
            int senderId = data.getInt("senderId");
            if (senderId == PreferenceManager.getID(mContext)) return;
            Realm realm = DostChatApp.getRealmDatabaseInstance();
            realm.executeTransaction(realm1 -> {
                MessagesModel messagesModel = realm1.where(MessagesModel.class).equalTo("id", messageId).equalTo("status", AppConstants.IS_SENT).findFirst();
                if (messagesModel != null) {
                    messagesModel.setStatus(AppConstants.IS_DELIVERED);
                    realm1.copyToRealmOrUpdate(messagesModel);
                    AppHelper.LogCat("Delivered successfully");
                    EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_MESSAGE_IS_DELIVERED_FOR_MESSAGES, messageId));
                    EventBus.getDefault().post(new Pusher(EVENT_BUS_MESSAGE_IS_DELIVERED_FOR_CONVERSATIONS, messagesModel.getConversationID()));
                } else {
                    AppHelper.LogCat("Delivered failed ");
                }
            });
            realm.close();
        } catch (JSONException e) {
            AppHelper.LogCat("Save data to realm delivered JSONException " + e.getMessage());
        }
    }

    /**
     * method to check if a group conversation exist
     *
     * @param groupID this is the first parameter for  checkIfGroupConversationExist method
     * @param realm   this is the second parameter for  checkIfGroupConversationExist  method
     * @return return value
     */
    private boolean checkIfGroupConversationExist(int groupID, Realm realm) {
        RealmQuery<ConversationsModel> query = realm.where(ConversationsModel.class).equalTo("groupID", groupID);
        return query.count() != 0;

    }

    private boolean checkIfCreatedGroupMessageExist(int groupId, Realm realm, String message) {
        RealmQuery<MessagesModel> query = realm.where(MessagesModel.class).equalTo("groupID", groupId).equalTo("isGroup", true).equalTo("message", message);
        return query.count() != 0;

    }

    /**
     * method to save the incoming message and mark him as waiting
     *
     * @param data this is the parameter for saveNewMessage method
     */
    private void saveNewMessageGroup(JSONObject data) {
        Realm realm = DostChatApp.getRealmDatabaseInstance();

        try {

            int senderId = data.getInt("senderId");
            int recipientId = data.getInt("recipientId");
            String messageBody = data.getString("messageBody");
            String senderName = data.getString("senderName");
            String senderPhone = data.getString("phone");
            String groupImage = data.getString("GroupImage");
            String groupName = data.getString("GroupName");
            String dateTmp = data.getString("date");
            String video = data.getString("video");
            String thumbnail = data.getString("thumbnail");
            boolean isGroup = true;
            String image = data.getString("image");
            String audio = data.getString("audio");
            String document = data.getString("document");
            String duration = data.getString("duration");
            String fileSize = data.getString("fileSize");
            int groupID = data.getInt("groupID");

            if (senderId == PreferenceManager.getID(mContext)) return;

            if (!checkIfGroupConversationExist(groupID, realm)) {
                realm.executeTransaction(realm1 -> {
                    int lastConversationID = RealmBackupRestore.getConversationLastId();
                    int lastID = RealmBackupRestore.getMessageLastId();
                    int UnreadMessageCounter = 0;
                    UnreadMessageCounter++;
                    ConversationsModel conversationsModel = new ConversationsModel();
                    RealmList<MessagesModel> messagesModelRealmList = new RealmList<MessagesModel>();
                    MessagesModel messagesModel = null;
                    messagesModel = new MessagesModel();
                    messagesModel.setId(lastID);
                    messagesModel.setDate(dateTmp);
                    messagesModel.setSenderID(senderId);
                    messagesModel.setUsername(senderName);
                    messagesModel.setPhone(senderPhone);
                    messagesModel.setRecipientID(0);
                    messagesModel.setStatus(AppConstants.IS_WAITING);
                    messagesModel.setGroup(true);
                    messagesModel.setGroupID(groupID);
                    messagesModel.setImageFile(image);
                    messagesModel.setVideoFile(video);
                    messagesModel.setAudioFile(audio);
                    messagesModel.setDocumentFile(document);
                    messagesModel.setVideoThumbnailFile(thumbnail);
                    messagesModel.setFileUpload(true);
                    if (!image.equals("null") || !video.equals("null") || !audio.equals("null") || !document.equals("null") || !thumbnail.equals("null")) {
                        messagesModel.setFileDownLoad(false);

                    } else {
                        messagesModel.setFileDownLoad(true);
                    }
                    messagesModel.setDuration(duration);
                    messagesModel.setFileSize(fileSize);
                    messagesModel.setConversationID(lastConversationID);
                    messagesModel.setMessage(messageBody);
                    messagesModelRealmList.add(messagesModel);
                    conversationsModel.setLastMessageId(lastID);
                    conversationsModel.setRecipientID(0);
                    conversationsModel.setRecipientUsername(groupName);
                    conversationsModel.setRecipientImage(groupImage);
                    conversationsModel.setGroupID(groupID);
                    conversationsModel.setMessageDate(dateTmp);
                    conversationsModel.setId(lastConversationID);
                    conversationsModel.setGroup(isGroup);
                    conversationsModel.setMessages(messagesModelRealmList);
                    conversationsModel.setLastMessage(messageBody);
                    conversationsModel.setStatus(AppConstants.IS_WAITING);
                    conversationsModel.setUnreadMessageCounter(String.valueOf(UnreadMessageCounter));
                    conversationsModel.setCreatedOnline(true);
                    realm1.copyToRealmOrUpdate(conversationsModel);


                    EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_NEW_MESSAGE_CONVERSATION_NEW_ROW, lastConversationID));
                    EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_NEW_MESSAGE_GROUP_CONVERSATION_NEW_ROW, groupID));

                    String FileType = null;
                    if (!messagesModel.getImageFile().equals("null")) {
                        FileType = "Image";
                    } else if (!messagesModel.getVideoFile().equals("null")) {
                        FileType = "Video";
                    } else if (!messagesModel.getAudioFile().equals("null")) {
                        FileType = "Audio";
                    } else if (!messagesModel.getDocumentFile().equals("null")) {
                        FileType = "Document";
                    }


                    mIntent = new Intent("new_group_message_notification_whatsclone");
                    mIntent.putExtra("conversationID", lastConversationID);
                    mIntent.putExtra("recipientID", senderId);
                    mIntent.putExtra("groupID", groupID);
                    mIntent.putExtra("groupImage", groupImage);
                    mIntent.putExtra("username", senderName);
                    mIntent.putExtra("file", FileType);
                    mIntent.putExtra("senderPhone", senderPhone);
                    mIntent.putExtra("groupName", groupName);
                    mIntent.putExtra("message", messageBody);
                    mIntent.putExtra("app", mContext.getPackageName());
                    sendBroadcast(mIntent);
                });

            } else {
                if (messageBody.equals(AppConstants.CREATE_GROUP) && checkIfCreatedGroupMessageExist(groupID, realm, messageBody))
                    return;
                realm.executeTransaction(realm1 -> {
                    int lastID = RealmBackupRestore.getMessageLastId();
                    int UnreadMessageCounter = 0;


                    ConversationsModel conversationsModel = realm1.where(ConversationsModel.class).equalTo("groupID", groupID).findFirst();
                    UnreadMessageCounter = Integer.parseInt(conversationsModel.getUnreadMessageCounter());
                    UnreadMessageCounter++;

                    RealmList<MessagesModel> messagesModelRealmList = conversationsModel.getMessages();
                    MessagesModel messagesModel = new MessagesModel();
                    messagesModel.setId(lastID);
                    messagesModel.setDate(dateTmp);
                    messagesModel.setRecipientID(0);
                    messagesModel.setStatus(AppConstants.IS_WAITING);
                    messagesModel.setUsername(senderName);
                    messagesModel.setPhone(senderPhone);
                    messagesModel.setSenderID(senderId);
                    messagesModel.setGroup(true);
                    messagesModel.setMessage(messageBody);
                    messagesModel.setImageFile(image);
                    messagesModel.setVideoFile(video);
                    messagesModel.setAudioFile(audio);
                    messagesModel.setDocumentFile(document);
                    messagesModel.setVideoThumbnailFile(thumbnail);
                    messagesModel.setFileUpload(true);
                    if (!image.equals("null") || !video.equals("null") || !audio.equals("null") || !document.equals("null") || !thumbnail.equals("null")) {
                        messagesModel.setFileDownLoad(false);

                    } else {
                        messagesModel.setFileDownLoad(true);
                    }
                    messagesModel.setFileSize(fileSize);
                    messagesModel.setDuration(duration);
                    messagesModel.setGroupID(groupID);
                    messagesModel.setConversationID(conversationsModel.getId());
                    messagesModelRealmList.add(messagesModel);
                    conversationsModel.setLastMessageId(lastID);
                    conversationsModel.setRecipientUsername(groupName);
                    conversationsModel.setRecipientImage(groupImage);
                    conversationsModel.setGroupID(groupID);
                    conversationsModel.setRecipientID(0);
                    conversationsModel.setMessages(messagesModelRealmList);
                    conversationsModel.setLastMessage(messageBody);
                    conversationsModel.setGroup(true);
                    conversationsModel.setCreatedOnline(true);
                    conversationsModel.setStatus(AppConstants.IS_WAITING);
                    conversationsModel.setUnreadMessageCounter(String.valueOf(UnreadMessageCounter));
                    realm1.copyToRealmOrUpdate(conversationsModel);
                    EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_NEW_GROUP_MESSAGE_MESSAGES_NEW_ROW, messagesModel));

                    EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_NEW_MESSAGE_CONVERSATION_OLD_ROW, conversationsModel.getId()));


                    String FileType = null;
                    if (!messagesModel.getImageFile().equals("null")) {
                        FileType = "Image";
                    } else if (!messagesModel.getVideoFile().equals("null")) {
                        FileType = "Video";
                    } else if (!messagesModel.getAudioFile().equals("null")) {
                        FileType = "Audio";
                    } else if (!messagesModel.getDocumentFile().equals("null")) {
                        FileType = "Document";
                    }

                    mIntent = new Intent("new_group_message_notification_whatsclone");
                    mIntent.putExtra("conversationID", conversationsModel.getId());
                    mIntent.putExtra("recipientID", senderId);
                    mIntent.putExtra("groupID", groupID);
                    mIntent.putExtra("groupImage", groupImage);
                    mIntent.putExtra("username", senderName);
                    mIntent.putExtra("file", FileType);
                    mIntent.putExtra("senderPhone", senderPhone);
                    mIntent.putExtra("groupName", groupName);
                    mIntent.putExtra("message", messageBody);
                    mIntent.putExtra("app", mContext.getPackageName());
                    sendBroadcast(mIntent);


                });
            }


        } catch (JSONException e) {
            AppHelper.LogCat(e.getMessage());
        }


        realm.close();
        EventBus.getDefault().post(new Pusher(EVENT_BUS_MESSAGE_COUNTER));
        NotificationsManager.SetupBadger(mContext);
    }

    /**
     * method to when user receive a new message from a group
     */
    private void onReceivedNewMessageGroup() {

        mSocket.on(AppConstants.SOCKET_NEW_MESSAGE_GROUP_SERVER, args -> {
            JSONObject data = (JSONObject) args[0];
            try {
                int recipientId = data.getInt("recipientId");
                int senderID = data.getInt("senderId");

                data.put("pingedId", recipientId);
                data.put("pinged", false);

                if (recipientId == PreferenceManager.getID(mContext)) {
                    data.put("pingedId", recipientId);
                    data.put("pinged", true);
                }

                data.put("socketId", PreferenceManager.getSocketID(mContext));
                if (mSocket != null)
                    mSocket.emit(AppConstants.SOCKET_USER_PING_GROUP, data);
            } catch (Exception e) {
                AppHelper.LogCat("New group message MainService " + e.getMessage());
            }
        });
        mSocket.on(AppConstants.SOCKET_USER_PINGED_GROUP, args -> {
            JSONObject dataString = (JSONObject) args[0];
            try {
                int recipientID = dataString.getInt("recipientId");
                int senderId = dataString.getInt("senderId");
                boolean pinged = dataString.getBoolean("pinged");
                int pingedID = dataString.getInt("pingedId");
                dataString.put("userToken", PreferenceManager.getToken(mContext));
                if (pinged) {
                    AppHelper.LogCat("User connected MainService (group)" + dataString.getInt("recipientId"));
                    dataString.put("isSent", 1);
                    Realm realm = DostChatApp.getRealmDatabaseInstance();
                    if (!checkIfUserBlockedExist(senderId, realm)) {
                        if (!realm.isClosed())
                            realm.close();
                        if (mSocket != null)
                            mSocket.emit(AppConstants.SOCKET_IS_MESSAGE_GROUP_SEND, dataString);
                        if (recipientID == PreferenceManager.getID(mContext))
                            saveNewMessageGroup(dataString);
                    }
                } else {

                    if (pingedID == senderId) return;
                    AppHelper.LogCat("User not  connected  MainService (group)" + dataString.getInt("recipientId"));
                    dataString.put("isSent", 0);
                    if (mSocket != null)
                        mSocket.emit(AppConstants.SOCKET_IS_MESSAGE_GROUP_SEND, dataString);
                }
            } catch (JSONException e) {
                AppHelper.LogCat("Group message received JSONException  MainService" + e.getMessage());
            }
        });

    }

    private boolean checkIfUserBlockedExist(int userId, Realm realm) {
        RealmQuery<UsersBlockModel> query = realm.where(UsersBlockModel.class).equalTo("contactsModel.id", userId);
        return query.count() != 0;
    }

    /**
     * method to when user receive a new message
     */
    private void onReceivedNewMessage() {

        mSocket.on(AppConstants.SOCKET_NEW_MESSAGE_SERVER, args -> {
            JSONObject data = (JSONObject) args[0];
            AppHelper.LogCat("new_message_server ");
            try {
                int recipientId = data.getInt("recipientId");

                data.put("pingedId", PreferenceManager.getID(mContext));
                data.put("pinged", false);

                if (recipientId == PreferenceManager.getID(mContext)) {
                    data.put("pingedId", recipientId);
                    data.put("pinged", true);
                }
                data.put("socketId", PreferenceManager.getSocketID(mContext));
                if (mSocket != null)
                    mSocket.emit(AppConstants.SOCKET_USER_PING, data, (Ack) argObjects -> {

                        JSONObject dataString = (JSONObject) argObjects[0];
                        try {
                            int recipientID = dataString.getInt("recipientId");
                            int senderId = data.getInt("senderId");
                            boolean pinged = dataString.getBoolean("pinged");
                            int pingedID = dataString.getInt("pingedId");

                            if (pinged && pingedID == recipientID) {
                                AppHelper.LogCat("User  connected " + dataString.getInt("pingedId"));
                                Realm realm = DostChatApp.getRealmDatabaseInstance();
                                if (!checkIfUserBlockedExist(senderId, realm)) {
                                    if (!realm.isClosed())
                                        realm.close();
                                    if (mSocket != null)
                                        mSocket.emit(AppConstants.SOCKET_IS_MESSAGE_SENT, dataString);
                                    saveNewMessage(dataString);
                                }
                            } else {
                                AppHelper.LogCat("User not  connected " + dataString.getInt("pingedId"));

                            }
                        } catch (JSONException e) {
                            AppHelper.LogCat("User message received MainService JSONException" + e.getMessage());
                        }
                    });
            } catch (Exception e) {
                AppHelper.LogCat("User received  new message  Exception MainService" + e.getMessage());
            }
        });

    }

    /**
     * method to send a confirmation that the recipient user is connected
     */
    private void sendPongToSender() {

        mSocket.on(AppConstants.SOCKET_IS_MESSAGE_SENT, args -> {
            JSONObject dataOn = (JSONObject) args[0];

            try {
                int SenderID = dataOn.getInt("senderId");
                if (SenderID != PreferenceManager.getID(mContext))
                    return;
                updateStatusAsSentBySender(dataOn, AppConstants.IS_SENT);
            } catch (JSONException e) {
                AppHelper.LogCat("Recipient is online  MainService" + e.getMessage());
            }

        });
    }

    /**
     * method to update status as seen by sender (if recipient have been seen the message)
     */
    private void SenderMarkMessageAsSeen() {
        mSocket.on(AppConstants.SOCKET_IS_MESSAGE_SEEN, args -> {
            JSONObject data = (JSONObject) args[0];
            updateSeenStatus(data);
        });

    }


    /**
     * method to get a conversation id by groupId
     *
     * @param groupId this is the first parameter for getConversationId method
     * @param realm   this is the second parameter for getConversationId method
     * @return conversation id
     */
    private int getConversationIdByGroupId(int groupId, Realm realm) {
        try {
            ConversationsModel conversationsModelNew = realm.where(ConversationsModel.class)
                    .equalTo("groupID", groupId)
                    .findAll().first();
            return conversationsModelNew.getId();
        } catch (Exception e) {
            AppHelper.LogCat("Conversation id  (group) Exception MainService  " + e.getMessage());
            return 0;
        }
    }

    /**
     * method to update status as seen by sender (if recipient have been seen the message)
     */
    private void MemberMarkMessageAsSent() {
        mSocket.on(AppConstants.SOCKET_IS_MESSAGE_GROUP_SENT, args -> {
            JSONObject data = (JSONObject) args[0];
            updateGroupSentStatus(data);
        });

    }

    /**
     * method to update status as delivered by sender (if recipient have been seen the message)
     */
    private void MemberMarkMessageAsDelivered() {
        mSocket.on(AppConstants.SOCKET_IS_MESSAGE_GROUP_DELIVERED, args -> {
            JSONObject data = (JSONObject) args[0];
            AppHelper.LogCat("SOCKET_IS_MESSAGE_GROUP_DELIVERED ");
            updateGroupDeliveredStatus(data);
        });

    }

    /**
     * method to update status as delivered by sender
     *
     * @param data this is parameter for updateSeenStatus method
     */
    private void updateGroupDeliveredStatus(JSONObject data) {


        try {
            int groupId = data.getInt("groupId");
            int senderId = data.getInt("senderId");
            AppHelper.LogCat("groupId " + groupId);
            AppHelper.LogCat("sen hhh " + senderId);
            if (senderId != PreferenceManager.getID(mContext)) return;
            Realm realm = DostChatApp.getRealmDatabaseInstance();
            int ConversationID = getConversationIdByGroupId(groupId, realm);
            AppHelper.LogCat("conversation  id seen " + ConversationID);
            List<MessagesModel> messagesModelsRealm = realm.where(MessagesModel.class)
                    .equalTo("conversationID", ConversationID)
                    .equalTo("isGroup", true)
                    .equalTo("status", AppConstants.IS_SENT)
                    .findAll();
            if (messagesModelsRealm.size() != 0) {
                for (MessagesModel messagesModel1 : messagesModelsRealm) {

                    realm.executeTransaction(realm1 -> {
                        MessagesModel messagesModel = realm1.where(MessagesModel.class)
                                .equalTo("groupID", groupId)
                                .equalTo("senderID", senderId)
                                .equalTo("id", messagesModel1.getId())
                                .equalTo("status", AppConstants.IS_SENT).findFirst();
                        if (messagesModel != null) {
                            messagesModel.setStatus(AppConstants.IS_DELIVERED);
                            realm1.copyToRealmOrUpdate(messagesModel);
                            AppHelper.LogCat("Delivered successfully MainService");

                            EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_MESSAGE_IS_DELIVERED_FOR_MESSAGES, messagesModel.getId()));

                            handler.postDelayed(() -> updateGroupSeenStatus(data), 2000);
                        } else {
                            AppHelper.LogCat("Seen  failed MainService ");
                        }
                    });

                }
            }
            realm.close();
            EventBus.getDefault().post(new Pusher(EVENT_BUS_MESSAGE_IS_DELIVERED_FOR_CONVERSATIONS, ConversationID));

        } catch (JSONException e) {
            AppHelper.LogCat("Save to realm seen MainService " + e.getMessage());
        }

    }

    /**
     * method to update status as seen by sender (group)
     *
     * @param data this is parameter for updateSeenStatus method
     */
    private void updateGroupSeenStatus(JSONObject data) {


        try {
            int groupId = data.getInt("groupId");
            int senderId = data.getInt("senderId");
            AppHelper.LogCat("groupId " + groupId);
            AppHelper.LogCat("sen " + senderId);
            if (senderId != PreferenceManager.getID(mContext)) return;
            Realm realm = DostChatApp.getRealmDatabaseInstance();
            int ConversationID = getConversationIdByGroupId(groupId, realm);
            AppHelper.LogCat("conversation  id seen " + ConversationID);
            List<MessagesModel> messagesModelsRealm = realm.where(MessagesModel.class)
                    .equalTo("conversationID", ConversationID)
                    .equalTo("isGroup", true)
                    .equalTo("status", AppConstants.IS_DELIVERED)
                    .findAll();
            if (messagesModelsRealm.size() != 0) {
                for (MessagesModel messagesModel1 : messagesModelsRealm) {

                    realm.executeTransaction(realm1 -> {
                        MessagesModel messagesModel = realm1.where(MessagesModel.class)
                                .equalTo("groupID", groupId)
                                .equalTo("senderID", senderId)
                                .equalTo("id", messagesModel1.getId())
                                .equalTo("status", AppConstants.IS_DELIVERED).findFirst();
                        if (messagesModel != null) {
                            messagesModel.setStatus(AppConstants.IS_SEEN);
                            realm1.copyToRealmOrUpdate(messagesModel);
                            AppHelper.LogCat("seen successfully");
                            EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_MESSAGE_IS_SEEN_FOR_MESSAGES, messagesModel.getId()));

                        } else {
                            AppHelper.LogCat("Seen  failed MainService (group)");
                        }
                    });
                }
            }
            realm.close();
            EventBus.getDefault().post(new Pusher(EVENT_BUS_MESSAGE_IS_SEEN_FOR_CONVERSATIONS, ConversationID));

        } catch (JSONException e) {
            AppHelper.LogCat("Save to realm seen " + e);
        }

    }


    /**
     * method to update status as sent by sender
     *
     * @param data this is parameter for updateSeenStatus method
     */
    private void updateGroupSentStatus(JSONObject data) {
        try {
            int groupId = data.getInt("groupId");
            int senderId = data.getInt("senderId");
            if (senderId != PreferenceManager.getID(mContext)) return;
            Realm realm = DostChatApp.getRealmDatabaseInstance();
            int ConversationID = getConversationIdByGroupId(groupId, realm);
            List<MessagesModel> messagesModelsRealm = realm.where(MessagesModel.class)
                    .equalTo("conversationID", ConversationID)
                    .equalTo("isGroup", true)
                    .equalTo("status", AppConstants.IS_WAITING)
                    .findAll();
            if (messagesModelsRealm.size() != 0) {
                for (MessagesModel messagesModel1 : messagesModelsRealm) {

                    realm.executeTransaction(realm1 -> {
                        MessagesModel messagesModel = realm1.where(MessagesModel.class)
                                .equalTo("groupID", groupId)
                                .equalTo("senderID", senderId)
                                .equalTo("id", messagesModel1.getId())
                                .equalTo("status", AppConstants.IS_WAITING).findFirst();
                        if (messagesModel != null) {
                            messagesModel.setStatus(AppConstants.IS_SENT);
                            realm1.copyToRealmOrUpdate(messagesModel);
                            AppHelper.LogCat("Sent successfully MainService");
                            EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_MESSAGE_IS_SENT_FOR_MESSAGES, messagesModel.getId()));
                        } else {
                            AppHelper.LogCat("Sent  failed  MainService");
                        }
                        EventBus.getDefault().post(new Pusher(EVENT_BUS_NEW_MESSAGE_IS_SENT_FOR_CONVERSATIONS, ConversationID));
                    });

                }
            }

            realm.close();
        } catch (JSONException e) {
            AppHelper.LogCat("Save to realm sent Exception MainService " + e.getMessage());
        }

    }

    /**
     * method to update status as seen by sender (if recipient have been seen the message)  in realm database
     *
     * @param data this is parameter for updateSeenStatus method
     */
    private void updateSeenStatus(JSONObject data) {

        try {
            int recipientId = data.getInt("recipientId");
            int senderId = data.getInt("senderId");
            if (senderId == PreferenceManager.getID(mContext)) return;
            Realm realm = DostChatApp.getRealmDatabaseInstance();
            int ConversationID = getConversationId(senderId, recipientId, realm);
            List<MessagesModel> messagesModelsRealm = realm.where(MessagesModel.class)
                    .equalTo("conversationID", ConversationID)
                    .equalTo("isGroup", false)
                    .equalTo("status", AppConstants.IS_DELIVERED)
                    .findAll();
            if (messagesModelsRealm.size() != 0) {
                for (MessagesModel messagesModel1 : messagesModelsRealm) {

                    realm.executeTransaction(realm1 -> {
                        MessagesModel messagesModel = realm1.where(MessagesModel.class)
                                .equalTo("recipientID", senderId)
                                .equalTo("senderID", recipientId)
                                .equalTo("id", messagesModel1.getId())
                                .equalTo("status", AppConstants.IS_DELIVERED).findFirst();
                        if (messagesModel != null) {
                            messagesModel.setStatus(AppConstants.IS_SEEN);
                            realm1.copyToRealmOrUpdate(messagesModel);
                            AppHelper.LogCat("Seen successfully MainService");
                            EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_MESSAGE_IS_SEEN_FOR_MESSAGES, messagesModel.getId()));
                        } else {
                            AppHelper.LogCat("Seen  failed  MainService");
                        }
                    });
                }
            }
            EventBus.getDefault().post(new Pusher(EVENT_BUS_MESSAGE_IS_SEEN_FOR_CONVERSATIONS, ConversationID));
            realm.close();
        } catch (JSONException e) {
            AppHelper.LogCat("Save to realm seen  Exception" + e.getMessage());
        }

    }

    /**
     * method to get a conversation id
     *
     * @param recipientId this is the first parameter for getConversationId method
     * @param senderId    this is the second parameter for getConversationId method
     * @param realm       this is the thirded parameter for getConversationId method
     * @return conversation id
     */
    private int getConversationId(int recipientId, int senderId, Realm realm) {
        try {
            ConversationsModel conversationsModelNew = realm.where(ConversationsModel.class)
                    .beginGroup()
                    .equalTo("RecipientID", recipientId)
                    .or()
                    .equalTo("RecipientID", senderId)
                    .endGroup().findAll().first();
            return conversationsModelNew.getId();
        } catch (Exception e) {
            AppHelper.LogCat("Conversation id Exception MainService" + e.getMessage());
            return 0;
        }
    }


    /**
     * method to update status for the send message by sender  (as sent message ) in realm  database
     *
     * @param data   this is the first parameter for updateStatusAsSentBySender method
     * @param isSent this is the second parameter for updateStatusAsSentBySender method
     */
    private void updateStatusAsSentBySender(JSONObject data, int isSent) {
        isUnSentMessagesExecuted = false;
        try {
            int messageId = data.getInt("messageId");

            try {
                Realm realm = DostChatApp.getRealmDatabaseInstance();
                try {
                    realm.executeTransaction(realm1 -> {
                        MessagesModel messagesModel = realm1.where(MessagesModel.class).equalTo("id", messageId).findFirst();
                        messagesModel.setStatus(isSent);
                        realm1.copyToRealmOrUpdate(messagesModel);
                        EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_MESSAGE_IS_SENT_FOR_MESSAGES, messageId));
                        EventBus.getDefault().post(new Pusher(EVENT_BUS_NEW_MESSAGE_IS_SENT_FOR_CONVERSATIONS, messagesModel.getConversationID()));
                    });
                } catch (Exception e) {
                    AppHelper.LogCat(" Is sent messages Realm Error" + e.getMessage());
                }

                realm.close();

            } catch (Exception e) {
                AppHelper.LogCat("null object Exception MainService" + e.getMessage());
            }


        } catch (JSONException e) {
            AppHelper.LogCat("UpdateStatusAsSentBySender error  MainService" + e.getMessage());
        }
    }

    /**
     * method to save the incoming message and mark him as waiting
     *
     * @param data this is the parameter for saveNewMessage method
     */
    private void saveNewMessage(JSONObject data) {

        Realm realm = DostChatApp.getRealmDatabaseInstance();

        try {
            int recipientId = data.getInt("recipientId");
            int senderId = data.getInt("senderId");
            int messageId = data.getInt("messageId");
            String phone = data.getString("phone");
            String messageBody = data.getString("messageBody");
            String senderName = data.getString("senderName");
            String senderImage = data.getString("senderImage");
            String date = data.getString("date");
            String video = data.getString("video");
            String thumbnail = data.getString("thumbnail");
            boolean isGroup = false;
            String image = data.getString("image");
            String audio = data.getString("audio");
            String document = data.getString("document");
            String duration = data.getString("duration");
            String fileSize = data.getString("fileSize");

            if (senderId == PreferenceManager.getID(mContext)) return;

            int conversationID = getConversationId(recipientId, senderId, realm);
            if (conversationID == 0) {
                realm.executeTransaction(realm1 -> {

                    int lastConversationID = RealmBackupRestore.getConversationLastId();
                    int lastID = RealmBackupRestore.getMessageLastId();
                    int UnreadMessageCounter = 0;
                    UnreadMessageCounter++;


                    RealmList<MessagesModel> messagesModelRealmList = new RealmList<MessagesModel>();
                    MessagesModel messagesModel = new MessagesModel();
                    messagesModel.setId(lastID);
                    messagesModel.setUsername(senderName);
                    messagesModel.setRecipientID(recipientId);
                    messagesModel.setDate(date);
                    messagesModel.setStatus(AppConstants.IS_WAITING);
                    messagesModel.setGroup(isGroup);
                    messagesModel.setSenderID(senderId);
                    messagesModel.setFileUpload(true);
                    if (!image.equals("null") || !video.equals("null") || !audio.equals("null") || !document.equals("null") || !thumbnail.equals("null")) {
                        messagesModel.setFileDownLoad(false);

                    } else {
                        messagesModel.setFileDownLoad(true);
                    }

                    messagesModel.setDuration(duration);
                    messagesModel.setFileSize(fileSize);
                    messagesModel.setConversationID(lastConversationID);
                    messagesModel.setMessage(messageBody);
                    messagesModel.setImageFile(image);
                    messagesModel.setVideoFile(video);
                    messagesModel.setAudioFile(audio);
                    messagesModel.setDocumentFile(document);
                    messagesModel.setVideoThumbnailFile(thumbnail);
                    messagesModel.setPhone(phone);
                    messagesModelRealmList.add(messagesModel);
                    ConversationsModel conversationsModel1 = new ConversationsModel();
                    conversationsModel1.setRecipientID(senderId);
                    conversationsModel1.setLastMessage(messageBody);
                    conversationsModel1.setRecipientUsername(senderName);
                    if (!UtilsPhone.checkIfContactExist(mContext, phone)) {
                        if (!senderImage.equals("null"))
                            conversationsModel1.setRecipientImage(senderImage);
                        else
                            conversationsModel1.setRecipientImage(null);
                    }
                    conversationsModel1.setMessageDate(date);
                    conversationsModel1.setId(lastConversationID);
                    conversationsModel1.setStatus(AppConstants.IS_WAITING);
                    conversationsModel1.setRecipientPhone(phone);
                    conversationsModel1.setGroup(isGroup);
                    conversationsModel1.setMessages(messagesModelRealmList);
                    conversationsModel1.setUnreadMessageCounter(String.valueOf(UnreadMessageCounter));
                    conversationsModel1.setLastMessageId(lastID);
                    conversationsModel1.setCreatedOnline(true);
                    realm1.copyToRealmOrUpdate(conversationsModel1);


                    String FileType = null;
                    if (!messagesModel.getImageFile().equals("null")) {
                        FileType = "Image";
                    } else if (!messagesModel.getVideoFile().equals("null")) {
                        FileType = "Video";
                    } else if (!messagesModel.getAudioFile().equals("null")) {
                        FileType = "Audio";
                    } else if (!messagesModel.getDocumentFile().equals("null")) {
                        FileType = "Document";
                    }

                    EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_NEW_MESSAGE_MESSAGES_NEW_ROW, messagesModel));
                    EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_NEW_MESSAGE_CONVERSATION_NEW_ROW, lastConversationID));

                    mIntent = new Intent("new_user_message_notification_whatsclone");
                    mIntent.putExtra("conversationID", lastConversationID);
                    mIntent.putExtra("recipientID", senderId);
                    mIntent.putExtra("senderId", recipientId);
                    mIntent.putExtra("userImage", senderImage);
                    mIntent.putExtra("username", senderName);
                    mIntent.putExtra("file", FileType);
                    mIntent.putExtra("phone", phone);
                    mIntent.putExtra("messageId", messageId);
                    mIntent.putExtra("message", messageBody);
                    mIntent.putExtra("app", mContext.getPackageName());
                    sendBroadcast(mIntent);
                });
            } else {

                realm.executeTransaction(realm1 -> {

                    int UnreadMessageCounter = 0;
                    int lastID = RealmBackupRestore.getMessageLastId();

                    ConversationsModel conversationsModel;
                    RealmQuery<ConversationsModel> conversationsModelRealmQuery = realm1.where(ConversationsModel.class).equalTo("id", conversationID);
                    conversationsModel = conversationsModelRealmQuery.findAll().first();

                    UnreadMessageCounter = Integer.parseInt(conversationsModel.getUnreadMessageCounter());
                    UnreadMessageCounter++;
                    MessagesModel messagesModel = new MessagesModel();
                    messagesModel.setId(lastID);
                    messagesModel.setUsername(senderName);
                    messagesModel.setRecipientID(recipientId);
                    messagesModel.setDate(date);
                    messagesModel.setStatus(AppConstants.IS_WAITING);
                    messagesModel.setGroup(isGroup);
                    messagesModel.setSenderID(senderId);
                    messagesModel.setFileUpload(true);
                    if (!image.equals("null") || !video.equals("null") || !audio.equals("null") || !document.equals("null") || !thumbnail.equals("null")) {
                        messagesModel.setFileDownLoad(false);

                    } else {
                        messagesModel.setFileDownLoad(true);
                    }
                    messagesModel.setFileSize(fileSize);
                    messagesModel.setDuration(duration);
                    messagesModel.setConversationID(conversationID);
                    messagesModel.setMessage(messageBody);
                    messagesModel.setImageFile(image);
                    messagesModel.setVideoFile(video);
                    messagesModel.setAudioFile(audio);
                    messagesModel.setDocumentFile(document);
                    messagesModel.setVideoThumbnailFile(thumbnail);
                    messagesModel.setPhone(phone);
                    conversationsModel.getMessages().add(messagesModel);
                    conversationsModel.setLastMessageId(lastID);
                    conversationsModel.setRecipientID(senderId);
                    conversationsModel.setLastMessage(messageBody);
                    conversationsModel.setMessageDate(date);
                    conversationsModel.setCreatedOnline(true);
                    conversationsModel.setRecipientUsername(senderName);
                    if (!UtilsPhone.checkIfContactExist(mContext, phone)) {
                        if (!senderImage.equals("null"))
                            conversationsModel.setRecipientImage(senderImage);
                        else
                            conversationsModel.setRecipientImage(null);
                    }
                    conversationsModel.setRecipientPhone(phone);
                    conversationsModel.setGroup(isGroup);
                    conversationsModel.setStatus(AppConstants.IS_WAITING);
                    conversationsModel.setUnreadMessageCounter(String.valueOf(UnreadMessageCounter));
                    realm1.copyToRealmOrUpdate(conversationsModel);


                    String FileType = null;
                    if (!messagesModel.getImageFile().equals("null")) {
                        FileType = "Image";
                    } else if (!messagesModel.getVideoFile().equals("null")) {
                        FileType = "Video";
                    } else if (!messagesModel.getAudioFile().equals("null")) {
                        FileType = "Audio";
                    } else if (!messagesModel.getDocumentFile().equals("null")) {
                        FileType = "Document";
                    }

                    EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_NEW_MESSAGE_MESSAGES_NEW_ROW, messagesModel));

                    EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_NEW_MESSAGE_CONVERSATION_OLD_ROW, conversationID));


                    mIntent = new Intent("new_user_message_notification_whatsclone");
                    mIntent.putExtra("conversationID", conversationID);
                    mIntent.putExtra("recipientID", senderId);
                    mIntent.putExtra("senderId", recipientId);
                    mIntent.putExtra("userImage", senderImage);
                    mIntent.putExtra("username", senderName);
                    mIntent.putExtra("file", FileType);
                    mIntent.putExtra("phone", phone);
                    mIntent.putExtra("messageId", messageId);
                    mIntent.putExtra("message", messageBody);
                    mIntent.putExtra("app", mContext.getPackageName());
                    sendBroadcast(mIntent);

                });

            }
            handler.postDelayed(() -> RecipientMarkMessageAsDelivered(mContext, messageId), 1500);


        } catch (JSONException e) {
            AppHelper.LogCat("save message Exception MainService" + e.getMessage());
        }

        realm.close();
        EventBus.getDefault().post(new Pusher(EVENT_BUS_MESSAGE_COUNTER));
        NotificationsManager.SetupBadger(mContext);
    }


    /**
     * method to send group messages
     *
     * @param messagesModel this is parameter of sendMessagesGroup method
     */
    public static void sendMessagesGroup(Activity activity, ContactsModel mUsersModel, GroupsModel mGroupsModel, MessagesModel messagesModel) {

        JSONObject message = new JSONObject();
        try {

            if (mUsersModel != null && mUsersModel.getUsername() != null) {
                message.put("senderName", mUsersModel.getUsername());
            } else {
                message.put("senderName", "null");
            }
            if (mUsersModel != null)
                message.put("phone", mUsersModel.getPhone());
            else
                message.put("phone", null);


            if (mGroupsModel != null && mGroupsModel.getGroupImage() != null)
                message.put("GroupImage", mGroupsModel.getGroupImage());
            else
                message.put("GroupImage", "null");
            if (mGroupsModel != null && mGroupsModel.getGroupName() != null)
                message.put("GroupName", mGroupsModel.getGroupName());
            else
                message.put("GroupName", "null");

            message.put("messageBody", messagesModel.getMessage());
            message.put("senderId", messagesModel.getSenderID());
            if (mGroupsModel != null && mGroupsModel.getGroupName() != null)
                message.put("groupID", mGroupsModel.getId());
            else
                message.put("groupID", messagesModel.getGroupID());
            message.put("date", messagesModel.getDate());
            message.put("isGroup", true);
            message.put("image", messagesModel.getImageFile());
            message.put("video", messagesModel.getVideoFile());
            message.put("audio", messagesModel.getAudioFile());
            message.put("thumbnail", messagesModel.getVideoThumbnailFile());
            message.put("document", messagesModel.getDocumentFile());

            if (!messagesModel.getFileSize().equals("0"))
                message.put("fileSize", messagesModel.getFileSize());
            else
                message.put("fileSize", "0");

            if (!messagesModel.getDuration().equals("0"))
                message.put("duration", messagesModel.getDuration());
            else
                message.put("duration", "0");

            message.put("userToken", PreferenceManager.getToken(activity));

            int senderId = message.getInt("senderId");
            String messageBody = message.getString("messageBody");
            String senderName = message.getString("senderName");
            String senderPhone = message.getString("phone");
            String GroupImage = message.getString("GroupImage");
            String GroupName = message.getString("GroupName");
            String dateTmp = message.getString("date");
            String video = message.getString("video");
            String thumbnail = message.getString("thumbnail");
            boolean isGroup = message.getBoolean("isGroup");
            String image = message.getString("image");
            String audio = message.getString("audio");
            String document = message.getString("document");
            int groupID = message.getInt("groupID");
            String fileSize = message.getString("fileSize");
            String duration = message.getString("duration");
            if (mSocket != null)
                mSocket.emit(AppConstants.SOCKET_SAVE_NEW_MESSAGE_GROUP, message, (Ack) argObjects -> {
                    JSONObject dataString = (JSONObject) argObjects[0];
                    activity.runOnUiThread(() -> {
                        Realm realm = DostChatApp.getRealmDatabaseInstance();
                        GroupsModel groupsModel = realm.where(GroupsModel.class).equalTo("id", groupID).findFirst();
                        List<MembersGroupModel> contactsModelList = groupsModel.getMembers();
                        int arraySize = contactsModelList.size();
                        for (int i = 0; i < arraySize; i++) {
                            if (!contactsModelList.get(i).isDeleted()) {
                                int recipientID = contactsModelList.get(i).getUserId();
                                handler.postDelayed(() -> {
                                    try {
                                        int messageId = dataString.getInt("messageId");
                                        JSONObject jsonObject = new JSONObject();
                                        jsonObject.put("messageId", messageId);
                                        jsonObject.put("recipientId", recipientID);
                                        jsonObject.put("messageBody", messageBody);
                                        jsonObject.put("senderId", senderId);
                                        jsonObject.put("senderName", senderName);
                                        jsonObject.put("phone", senderPhone);
                                        jsonObject.put("GroupImage", GroupImage);
                                        jsonObject.put("GroupName", GroupName);
                                        jsonObject.put("groupID", groupID);
                                        jsonObject.put("date", dateTmp);
                                        jsonObject.put("isGroup", isGroup);
                                        jsonObject.put("image", image);
                                        jsonObject.put("video", video);
                                        jsonObject.put("thumbnail", thumbnail);
                                        jsonObject.put("audio", audio);
                                        jsonObject.put("document", document);
                                        jsonObject.put("fileSize", fileSize);
                                        jsonObject.put("duration", duration);

                                        if (mSocket != null)
                                            mSocket.emit(AppConstants.SOCKET_NEW_MESSAGE_GROUP, jsonObject);
                                    } catch (JSONException e) {
                                        AppHelper.LogCat("JSONException " + e.getMessage());
                                    }
                                }, 200);
                            }

                        }
                        realm.close();
                    });


                });

        } catch (JSONException e) {
            AppHelper.LogCat(e.getMessage());
        }


    }


    private void onReceiveNewCall() {
        mSocket.on(AppConstants.SOCKET_RECEIVE_NEW_CALL, onReceiveNewCall);
    }

    /**
     * Receive call emitter callback when others call you.
     *
     * @param args json value contain callerid, userid and caller name
     */
    private Emitter.Listener onReceiveNewCall = args -> {
        AppHelper.LogCat("onReceiveNewCall called");
        JSONObject data = (JSONObject) args[0];
        try {
            String callerSocketId = data.getString("from");
            String callerPhone = data.getString("callerPhone");
            int callerID = data.getInt("callerID");
            String callerImage = data.getString("callerImage");
            boolean isVideoCall = data.getBoolean("isVideoCall");

            Realm realm = DostChatApp.getRealmDatabaseInstance();
            if (!checkIfUserBlockedExist(callerID, realm)) {
                if (!realm.isClosed())
                    realm.close();
                Intent intent = new Intent(getApplicationContext(), IncomingCallActivity.class);
                intent.setAction(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(AppConstants.CALLER_SOCKET_ID, callerSocketId);
                intent.putExtra(AppConstants.USER_SOCKET_ID, PreferenceManager.getSocketID(this));
                intent.putExtra(AppConstants.CALLER_PHONE, callerPhone);
                intent.putExtra(AppConstants.CALLER_IMAGE, callerImage);
                intent.putExtra(AppConstants.CALLER_ID, callerID);
                intent.putExtra(AppConstants.IS_VIDEO_CALL, isVideoCall);
                intent.putExtra(AppConstants.USER_PHONE, PreferenceManager.getPhone(this));
                getApplicationContext().startActivity(intent);
            } else {
                try {
                    JSONObject message = new JSONObject();
                    message.put("userSocketId", PreferenceManager.getSocketID(this));
                    message.put("callerSocketId", callerSocketId);
                    message.put("reason", AppConstants.NO_ANSWER);
                    mSocket.emit(AppConstants.SOCKET_REJECT_NEW_CALL, message);
                } catch (JSONException e) {
                    AppHelper.LogCat(" JSONException IncomingCallActivity rejectCall " + e.getMessage());
                }
            }
        } catch (JSONException e) {
            AppHelper.LogCat("JSONException Call" + e.getMessage());
        }

    };


}