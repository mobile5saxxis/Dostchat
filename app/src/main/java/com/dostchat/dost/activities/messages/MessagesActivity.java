package com.dostchat.dost.activities.messages;

import android.Manifest;
import android.animation.Animator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActivityOptions;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.os.SystemClock;
import android.os.Vibrator;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.design.widget.TextInputEditText;
import android.support.transition.TransitionManager;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.method.ScrollingMovementMethod;
import android.util.Pair;
import android.view.ActionMode;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Scroller;
import android.widget.TextView;

import com.dostchat.dost.R;
import com.dostchat.dost.activities.popups.MessagesPopupActivity;
import com.dostchat.dost.activities.profile.ProfileActivity;
import com.dostchat.dost.activities.settings.PreferenceSettingsManager;
import com.dostchat.dost.adapters.others.TextWatcherAdapter;
import com.dostchat.dost.adapters.recyclerView.messages.MessagesAdapter;
import com.dostchat.dost.animations.AnimationsUtil;
import com.dostchat.dost.animations.ViewAudioProxy;
import com.dostchat.dost.app.AppConstants;
import com.dostchat.dost.app.DostChatApp;
import com.dostchat.dost.app.EndPoints;
import com.dostchat.dost.helpers.AppHelper;
import com.dostchat.dost.helpers.Files.FilesManager;
import com.dostchat.dost.helpers.Files.backup.RealmBackupRestore;
import com.dostchat.dost.helpers.Files.cache.ImageLoader;
import com.dostchat.dost.helpers.Files.cache.MemoryCache;
import com.dostchat.dost.helpers.PermissionHandler;
import com.dostchat.dost.helpers.PreferenceManager;
import com.dostchat.dost.helpers.UtilsPhone;
import com.dostchat.dost.helpers.UtilsString;
import com.dostchat.dost.helpers.UtilsTime;
import com.dostchat.dost.helpers.call.CallManager;
import com.dostchat.dost.helpers.images.DostChatImageLoader;
import com.dostchat.dost.helpers.notifications.NotificationsManager;
import com.dostchat.dost.interfaces.LoadingData;
import com.dostchat.dost.interfaces.NetworkListener;
import com.dostchat.dost.models.groups.GroupsModel;
import com.dostchat.dost.models.groups.MembersGroupModel;
import com.dostchat.dost.models.messages.ConversationsModel;
import com.dostchat.dost.models.messages.MessagesModel;
import com.dostchat.dost.models.notifications.NotificationsModel;
import com.dostchat.dost.models.users.Pusher;
import com.dostchat.dost.models.users.contacts.ContactsModel;
import com.dostchat.dost.models.users.contacts.UsersBlockModel;
import com.dostchat.dost.presenters.messages.MessagesPresenter;
import com.dostchat.dost.services.MainService;
import com.dostchat.dost.video.LiveRoomActivity;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;
import hani.momanii.supernova_emoji_library.Helper.EmojiconTextView;
import io.codetail.animation.ViewAnimationUtils;
import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;
import io.socket.client.Ack;
import io.socket.client.Socket;

import static com.dostchat.dost.app.AppConstants.EVENT_BUS_DELETE_CONVERSATION_ITEM;
import static com.dostchat.dost.app.AppConstants.EVENT_BUS_MESSAGE_COUNTER;
import static com.dostchat.dost.video.ConstantApp.ACTION_KEY_CHANNEL_NAME;
import static com.dostchat.dost.video.ConstantApp.ACTION_KEY_ENCRYPTION_KEY;
import static com.dostchat.dost.video.ConstantApp.ACTION_KEY_ENCRYPTION_MODE;


/**
 * Created by Abderrahim El imame on 05/03/2016.
 * Email : abderrahim.elimame@gmail.com
 */

@SuppressLint("SetTextI18n")
public class MessagesActivity extends AppCompatActivity implements LoadingData, RecyclerView.OnItemTouchListener, ActionMode.Callback, View.OnClickListener, NetworkListener {

    @BindView(R.id.activity_messages)
    LinearLayout mView;
    @BindView(R.id.listMessages)
    RecyclerView messagesList;
    @BindView(R.id.add_contact)
    TextView AddContactBtn;
    @BindView(R.id.block_user)
    TextView BlockContactBtn;
    @BindView(R.id.unblock_user)
    TextView UnBlockContactBtn;
    @BindView(R.id.block_layout)
    FrameLayout blockLayout;
    @BindView(R.id.send_button)
    ImageButton SendButton;
    @BindView(R.id.send_record_button)
    ImageButton SendRecordButton;
    @BindView(R.id.pictureBtn)
    ImageButton PictureButton;

    @BindView(R.id.emoticonBtn)
    ImageView EmoticonButton;

    @BindView(R.id.keyboradBtn)
    ImageView keyboradBtn;

    @BindView(R.id.MessageWrapper)
    EmojiconEditText messageWrapper;

    @BindView(R.id.toolbar_title)
    EmojiconTextView ToolbarTitle;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.toolbar_image)
    ImageView ToolbarImage;

    @BindView(R.id.toolbar_status)
    TextView statusUser;
    @BindView(R.id.toolbarLinear)
    LinearLayout ToolbarLinearLayout;
    @BindView(R.id.progress_bar)
    ProgressBar mProgressBar;
    @BindView(R.id.arrow_back)
    LinearLayout BackButton;
    @BindView(R.id.send_message)
    LinearLayout SendMessageLayout;
    @BindView(R.id.groupSend)
    LinearLayout groupLeftSendMessageLayout;
    @BindView(R.id.send_message_panel)
    View sendMessagePanel;


    EmojIconActions emojIcon;

    final int MIN_INTERVAL_TIME = 2000;
    long mStartTime;
    private boolean emoticonShown = false;
    public Intent mIntent = null;
    private MessagesAdapter mMessagesAdapter;
    //public Context context;
    private String messageTransfer = null;
    private ContactsModel mUsersModel;
    private GroupsModel mGroupsModel;
    private ContactsModel mUsersModelRecipient;
    private String FileSize = "0";
    private String Duration = "0";
    private String FileImagePath = null;
    private String FileVideoThumbnailPath = null;
    private String FileVideoPath = null;
    private String FileAudioPath = null;
    private String FileDocumentPath = null;
    private MessagesPresenter mMessagesPresenter;
    private int ConversationID;
    private int groupID;
    private boolean isGroup;

    //for sockets
    private Socket mSocket;
    private int senderId;
    private int recipientId;
    private static final int TYPING_TIMER_LENGTH = 600;
    private boolean isTyping = false;
    private Handler mTypingHandler = new Handler();
    private boolean isSeen = false;
    private boolean isOpen;
    private Realm realm;

    //for audio
    @BindView(R.id.recording_time_text)
    TextView recordTimeText;
    @BindView(R.id.record_panel)
    View recordPanel;
    @BindView(R.id.slide_text_container)
    View slideTextContainer;
    @BindView(R.id.slideToCancelText)
    TextView slideToCancelText;
    private MediaRecorder mMediaRecorder = null;
    private float startedDraggingX = -1;
    private float distCanMove = convertToDp(80);
    private long startTime = 0L;
    private Timer recordTimer;

    /* for serach */
    @BindView(R.id.close_btn_search_view)
    ImageView closeBtn;
    @BindView(R.id.search_input)
    TextInputEditText searchInput;
    @BindView(R.id.clear_btn_search_view)
    ImageView clearBtn;
    @BindView(R.id.app_bar_search_view)
    View searchView;


    /**
     * For Attachment container
     */
    @BindView(R.id.items_container)
    LinearLayout mFrameLayoutReveal;
    @BindView(R.id.attach_camera)
    ImageView attachCamera;
    @BindView(R.id.attach_image)
    ImageView attachImage;
    @BindView(R.id.attach_audio)
    ImageView attachAudio;
    @BindView(R.id.attach_document)
    ImageView attachDocument;
    @BindView(R.id.attach_video)
    ImageView attachVideo;
    @BindView(R.id.attach_record_video)
    ImageView attachRecordVideo;
    @BindView(R.id.location)
    TextView location;

    private Animator.AnimatorListener mAnimatorListenerOpen, mAnimatorListenerClose;
    private GestureDetectorCompat gestureDetector;
    private ActionMode actionMode;
    private boolean isLeft = false;
    private MemoryCache memoryCache;
    private PackageManager packageManager;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);
        ButterKnife.bind(this);
        realm = DostChatApp.getRealmDatabaseInstance();
        memoryCache = new MemoryCache();
        if (getIntent().getExtras() != null) {
            if (getIntent().hasExtra("recipientID")) {
                recipientId = getIntent().getExtras().getInt("recipientID");
            }
            if (getIntent().hasExtra("groupID")) {
                groupID = getIntent().getExtras().getInt("groupID");
            }

            if (getIntent().hasExtra("conversationID")) {
                ConversationID = getIntent().getExtras().getInt("conversationID");
            }
            if (getIntent().hasExtra("isGroup")) {
                isGroup = getIntent().getExtras().getBoolean("isGroup");
            }

        }

        connectToChatServer();
        senderId = PreferenceManager.getID(this);
        initializerSearchView(searchInput, clearBtn);
        initializerView();
        setTypeFaces();
        packageManager = getPackageManager();
        mMessagesPresenter = new MessagesPresenter(this);
        mMessagesPresenter.onCreate();
        if (isGroup) {
            try {
                if (mGroupsModel.getMembers() != null) {
                    int arraySize = mGroupsModel.getMembers().size();
                    for (int x = 0; x < arraySize; x++) {
                        if (mGroupsModel.getMembers().get(x).getUserId() == PreferenceManager.getID(this)) {
                            if (mGroupsModel.getMembers().get(x).isLeft()) {
                                isLeft = true;
                                groupLeftSendMessageLayout.setVisibility(View.VISIBLE);
                                SendMessageLayout.setVisibility(View.GONE);
                            } else {
                                isLeft = false;
                                groupLeftSendMessageLayout.setVisibility(View.GONE);
                                SendMessageLayout.setVisibility(View.VISIBLE);
                            }
                            break;

                        }

                    }
                }
            } catch (Exception e) {
                AppHelper.LogCat(e);
            }

        }else {
            location.setVisibility(View.VISIBLE);
            //DostChatApp.locationUpdateManager.locationExchange(senderId,recipientId,location);
            DostChatApp.locationUpdateManager.locationExchange(location);
        }

        initializerMessageWrapper();


        if (getIntent().getExtras() != null) {
            if (getIntent().hasExtra("messageCopied")) {
                ArrayList<String> messageCopied = getIntent().getExtras().getStringArrayList("messageCopied");
                for (String message : messageCopied) {
                    messageTransfer = message;
                    new Handler().postDelayed(this::sendMessage, 50);
                }
            } else if (getIntent().hasExtra("filePathList")) {
                ArrayList<String> filePathList = getIntent().getExtras().getStringArrayList("filePathList");
                File fileVideo = null;
                for (String filepath : filePathList) {
                    if (FilesManager.getMimeType(filepath).equals("video/mp4")) {
                        FileVideoPath = filepath;
                        MediaPlayer mp = MediaPlayer.create(this, Uri.parse(FileVideoPath));
                        int duration = mp.getDuration();
                        Duration = String.valueOf(duration);
                        mp.release();
                        File file = new File(FileVideoPath);
                        FileSize = String.valueOf(file.length());
                        Bitmap thumbnailBitmap = ThumbnailUtils.createVideoThumbnail(FileVideoPath, MediaStore.Video.Thumbnails.MINI_KIND);
                        try {
                            fileVideo = FilesManager.getFileThumbnail(this, thumbnailBitmap);
                        } catch (IOException e) {
                            AppHelper.LogCat("IOException video thumbnail " + e.getMessage());
                        }
                        FileVideoThumbnailPath = FilesManager.getPath(getApplicationContext(), Uri.fromFile(fileVideo));
                    } else if (FilesManager.getMimeType(filepath).equals("audio/mp3")) {
                        FileAudioPath = filepath;
                        MediaPlayer mp = MediaPlayer.create(this, Uri.parse(FileAudioPath));
                        int duration = mp.getDuration();
                        Duration = String.valueOf(duration);
                        mp.release();
                    } else if (FilesManager.getMimeType(filepath).equals("application/pdf")) {
                        FileDocumentPath = filepath;
                        File file = null;
                        if (FileDocumentPath != null) {
                            file = new File(FileDocumentPath);
                        }
                        if (file != null) {
                            FileSize = String.valueOf(file.length());

                        }
                    } else if (FilesManager.getMimeType(filepath).equals("image/jpeg") || FilesManager.getMimeType(filepath).equals("image/png")) {
                        FileImagePath = filepath;
                        File file = null;
                        if (FileImagePath != null) {
                            file = new File(FileImagePath);
                        }
                        if (file != null) {
                            FileSize = String.valueOf(file.length());

                        }


                    }
                    sendMessage();
                }
            } else if (getIntent().hasExtra("filePath")) {
                String filepath = getIntent().getExtras().getString("filePath");
                File fileVideo = null;
                if (FilesManager.getMimeType(filepath).equals("video/mp4")) {
                    FileVideoPath = filepath;
                    MediaPlayer mp = MediaPlayer.create(this, Uri.parse(FileVideoPath));
                    int duration = mp.getDuration();
                    Duration = String.valueOf(duration);
                    mp.release();
                    File file = new File(FileVideoPath);
                    FileSize = String.valueOf(file.length());
                    Bitmap thumbnailBitmap = ThumbnailUtils.createVideoThumbnail(FileVideoPath, MediaStore.Video.Thumbnails.MINI_KIND);
                    try {
                        fileVideo = FilesManager.getFileThumbnail(this, thumbnailBitmap);
                    } catch (IOException e) {
                        AppHelper.LogCat("IOException video thumbnail " + e.getMessage());
                    }
                    FileVideoThumbnailPath = FilesManager.getPath(getApplicationContext(), Uri.fromFile(fileVideo));
                } else if (FilesManager.getMimeType(filepath).equals("audio/mp3")) {
                    FileAudioPath = filepath;
                    MediaPlayer mp = MediaPlayer.create(this, Uri.parse(FileAudioPath));
                    int duration = mp.getDuration();
                    Duration = String.valueOf(duration);
                    mp.release();
                } else if (FilesManager.getMimeType(filepath).equals("application/pdf")) {
                    FileDocumentPath = filepath;
                    File file = null;
                    if (FileDocumentPath != null) {
                        file = new File(FileDocumentPath);
                    }
                    if (file != null) {
                        FileSize = String.valueOf(file.length());

                    }
                } else if (FilesManager.getMimeType(filepath).equals("image/jpeg") || FilesManager.getMimeType(filepath).equals("image/png")) {
                    FileImagePath = filepath;
                    File file = null;
                    if (FileImagePath != null) {
                        file = new File(FileImagePath);
                    }
                    if (file != null) {
                        FileSize = String.valueOf(file.length());

                    }
                }
                sendMessage();
            }

        }

        mAnimatorListenerOpen = new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                mFrameLayoutReveal.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }
        };

        mAnimatorListenerClose = new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                mFrameLayoutReveal.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        };

        mFrameLayoutReveal.setOnClickListener(view -> {
            if (isOpen) {
                isOpen = false;
                animateItems(false);

            }
        });
        emitUserIsOnline();

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

    }

/*

    private String getImageCompressedSize(String path) {
        byte[] fileLength;
        fileLength = ImageUtils.compressImage(path);
        return String.valueOf(fileLength.length);
    }
*/


    /**
     * method to animate the attachment items
     *
     * @param opened
     */
    private void animateItems(boolean opened) {
        float startRadius = 0.0f;
        float endRadius = Math.max(mFrameLayoutReveal.getWidth(), mFrameLayoutReveal.getHeight());
        if (opened) {
            int cy = mFrameLayoutReveal.getRight();
            int dx = mFrameLayoutReveal.getTop();
            Animator supportAnimator = ViewAnimationUtils.createCircularReveal(mFrameLayoutReveal, cy, dx, startRadius, endRadius);
            supportAnimator.setInterpolator(new AccelerateInterpolator());
            supportAnimator.setDuration(400);
            supportAnimator.addListener(mAnimatorListenerOpen);
            supportAnimator.start();
        } else {
            int cy = mFrameLayoutReveal.getRight();
            int dx = mFrameLayoutReveal.getTop();
            Animator supportAnimator2 = ViewAnimationUtils.createCircularReveal(mFrameLayoutReveal, cy, dx, endRadius, startRadius);
            supportAnimator2.setInterpolator(new DecelerateInterpolator());
            supportAnimator2.setDuration(400);
            supportAnimator2.addListener(mAnimatorListenerClose);
            supportAnimator2.start();
        }
    }


    private void setTypeFaces() {
        if (AppConstants.ENABLE_FONTS_TYPES) {
            slideToCancelText.setTypeface(AppHelper.setTypeFace(this, "Futura"));
            messageWrapper.setTypeface(AppHelper.setTypeFace(this, "Futura"));
            searchInput.setTypeface(AppHelper.setTypeFace(this, "Futura"));
            ToolbarTitle.setTypeface(AppHelper.setTypeFace(this, "Futura"));
            statusUser.setTypeface(AppHelper.setTypeFace(this, "Futura"));
            recordTimeText.setTypeface(AppHelper.setTypeFace(this, "Futura"));
        }
    }

    /**
     * method initialize the view
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void initializerView() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        mMessagesAdapter = new MessagesAdapter(this, realm);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.setStackFromEnd(true);
        messagesList.setLayoutManager(layoutManager);
        messagesList.setAdapter(mMessagesAdapter);
        messagesList.setItemAnimator(new DefaultItemAnimator());
        messagesList.getItemAnimator().setChangeDuration(0);
        messagesList.setHasFixedSize(true);
        messagesList.setItemViewCacheSize(10);
        messagesList.setDrawingCacheEnabled(true);
        messagesList.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        messagesList.addOnItemTouchListener(this);
        gestureDetector = new GestureDetectorCompat(this, new RecyclerViewBenOnGestureListener());
        String ImageUrl = PreferenceManager.getWallpaper(this);
        if (ImageUrl != null) {
            Bitmap bitmap = ImageLoader.GetCachedBitmapImage(memoryCache, ImageUrl, this, PreferenceManager.getID(this), AppConstants.USER, AppConstants.ROW_WALLPAPER);
            if (bitmap != null) {
                BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(), bitmap);
                mView.setBackground(bitmapDrawable);
            } else {
                mView.setBackground(AppHelper.getDrawable(this, R.drawable.bg_msgs));
            }
        } else {
            mView.setBackground(AppHelper.getDrawable(this, R.drawable.bg_msgs));
        }


        EmoticonButton.setOnClickListener(v -> {
            if (!emoticonShown) {
                emoticonShown = true;
                emojIcon = new EmojIconActions(MessagesActivity.this, mView, messageWrapper, EmoticonButton);
                emojIcon.setIconsIds(R.drawable.ic_keyboard_gray_24dp, R.drawable.ic_emoticon_24dp);
                emojIcon.ShowEmojIcon();

            }

        });
        slideToCancelText.setText(R.string.slide_to_cancel_audio);


        SendButton.setOnClickListener(v -> sendMessage());
        AddContactBtn.setOnClickListener(v -> addNewContact());

        BlockContactBtn.setOnClickListener(v -> {
            blockContact();
        });
        UnBlockContactBtn.setOnClickListener(v -> {
            unBlockContact();
        });
        SendRecordButton.setOnTouchListener((view, motionEvent) -> {
            setDraggingAnimation(motionEvent, view);
            return true;

        });
        PictureButton.setOnClickListener(v -> launchAttachCamera());
        attachCamera.setOnClickListener(view -> launchAttachCamera());
        attachImage.setOnClickListener(view -> launchImageChooser());
        attachVideo.setOnClickListener(view -> launchVideoChooser());
        attachRecordVideo.setOnClickListener(view -> launchAttachRecordVideo());
        attachDocument.setOnClickListener(view -> launchDocumentChooser());
        attachAudio.setOnClickListener(view -> launchAudioChooser());

        ToolbarLinearLayout.setOnClickListener(v -> {
            if (isGroup) {
                if (AppHelper.isAndroid5()) {
                    ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this, new Pair<>(ToolbarImage, "userAvatar"), new Pair<>(ToolbarTitle, "userName"));
                    mIntent = new Intent(this, ProfileActivity.class);
                    mIntent.putExtra("groupID", groupID);
                    mIntent.putExtra("isGroup", true);
                    startActivity(mIntent, options.toBundle());
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                } else {
                    mIntent = new Intent(this, ProfileActivity.class);
                    mIntent.putExtra("groupID", groupID);
                    mIntent.putExtra("isGroup", true);
                    startActivity(mIntent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
            } else {
                if (AppHelper.isAndroid5()) {
                    ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this, new Pair<>(ToolbarImage, "userAvatar"), new Pair<>(ToolbarTitle, "userName"));
                    mIntent = new Intent(this, ProfileActivity.class);
                    mIntent.putExtra("userID", recipientId);
                    mIntent.putExtra("isGroup", false);
                    startActivity(mIntent, options.toBundle());
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                } else {
                    mIntent = new Intent(this, ProfileActivity.class);
                    mIntent.putExtra("userID", recipientId);
                    mIntent.putExtra("isGroup", false);
                    startActivity(mIntent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
            }
        });
        BackButton.setOnClickListener(v -> {
            mMessagesAdapter.stopAudio();

            if (NotificationsManager.getManager()) {
                if (isGroup)
                    NotificationsManager.cancelNotification(groupID);
                else
                    NotificationsManager.cancelNotification(recipientId);
            }
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);


        });


    }


    private void unBlockContact() {

        Realm realmUnblock = DostChatApp.getRealmDatabaseInstance();
        AlertDialog.Builder builderUnblock = new AlertDialog.Builder(this);
        builderUnblock.setMessage(R.string.unblock_user_make_sure);
        builderUnblock.setPositiveButton(R.string.Yes, (dialog, whichButton) -> {
            realmUnblock.executeTransactionAsync(realm1 -> {
                UsersBlockModel usersBlockModel = realm1.where(UsersBlockModel.class).equalTo("contactsModel.id", recipientId).findFirst();
                usersBlockModel.deleteFromRealm();

            }, () -> {
                refreshMenu();
                if (AddContactBtn.getVisibility() == View.VISIBLE) {
                    UnBlockContactBtn.setVisibility(View.GONE);
                    BlockContactBtn.setVisibility(View.VISIBLE);
                }
            }, error -> {
                AppHelper.LogCat("Block user" + error.getMessage());

            });


        });

        builderUnblock.setNegativeButton(R.string.No, (dialog, whichButton) -> {

        });

        builderUnblock.show();
        if (!realmUnblock.isClosed())
            realmUnblock.close();
    }

    private void blockContact() {

        Realm realm2 = DostChatApp.getRealmDatabaseInstance();
        AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
        builder2.setMessage(R.string.block_user_make_sure);
        builder2.setPositiveButton(R.string.Yes, (dialog, whichButton) -> {
            realm2.executeTransactionAsync(realm1 -> {
                ContactsModel contactsModel = realm1.where(ContactsModel.class).equalTo("id", recipientId).findFirst();
                UsersBlockModel usersBlockModel = new UsersBlockModel();
                usersBlockModel.setId(RealmBackupRestore.getBlockUserLastId());
                usersBlockModel.setContactsModel(contactsModel);
                realm1.copyToRealmOrUpdate(usersBlockModel);
            }, () -> {
                refreshMenu();
                if (AddContactBtn.getVisibility() == View.VISIBLE) {
                    BlockContactBtn.setVisibility(View.GONE);
                    UnBlockContactBtn.setVisibility(View.VISIBLE);
                }
            }, error -> {
                AppHelper.LogCat("Block user" + error.getMessage());

            });


        });

        builder2.setNegativeButton(R.string.No, (dialog, whichButton) -> {

        });

        builder2.show();
        if (!realm2.isClosed())
            realm2.close();
    }

    private void addNewContact() {
        try {
            Intent mIntent = new Intent(Intent.ACTION_INSERT);
            mIntent.setType(ContactsContract.Contacts.CONTENT_TYPE);
            mIntent.putExtra(ContactsContract.Intents.Insert.PHONE, mUsersModelRecipient.getPhone());
            startActivityForResult(mIntent, AppConstants.SELECT_ADD_NEW_CONTACT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * method to launch the camera preview
     */
    private void launchAttachCamera() {
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            if (PermissionHandler.checkPermission(this, Manifest.permission.CAMERA)) {
                AppHelper.LogCat("camera permission already granted.");

                if (isOpen) {
                    isOpen = false;
                    animateItems(false);
                }
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, AppConstants.SELECT_MESSAGES_CAMERA);
            } else {
                AppHelper.LogCat("Please request camera  permission.");
                PermissionHandler.requestPermission(this, Manifest.permission.CAMERA);
            }
        }

    }

    /**
     * method to launch the image chooser
     */
    private void launchImageChooser() {
        if (isOpen) {
            isOpen = false;
            animateItems(false);
        }


        if (PermissionHandler.checkPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            AppHelper.LogCat("Read data permission already granted.");

            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(
                    Intent.createChooser(intent, "Choose An image"),
                    AppConstants.UPLOAD_PICTURE_REQUEST_CODE);
        } else {
            AppHelper.LogCat("Please request Read data permission.");
            PermissionHandler.requestPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        }

    }

    /**
     * method  to launch a video preview
     */
    private void launchAttachRecordVideo() {
        if (isOpen) {
            isOpen = false;
            animateItems(false);
        }

        if (PermissionHandler.checkPermission(this, Manifest.permission.CAMERA)) {
            AppHelper.LogCat("Camera permission already granted.");

            Intent cameraIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            startActivityForResult(cameraIntent, AppConstants.SELECT_MESSAGES_RECORD_VIDEO);
        } else {
            AppHelper.LogCat("Please request camera  permission.");
            PermissionHandler.requestPermission(this, Manifest.permission.CAMERA);
        }
    }

    /**
     * method to launch a video chooser
     */
    private void launchVideoChooser() {
        if (isOpen) {
            isOpen = false;
            animateItems(false);
        }


        if (PermissionHandler.checkPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            AppHelper.LogCat("Read data permission already granted.");
            Intent intent = new Intent();
            intent.setType("video/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(
                    Intent.createChooser(intent, "Choose video"),
                    AppConstants.UPLOAD_VIDEO_REQUEST_CODE);
        } else {
            AppHelper.LogCat("Please request Read data permission.");
            PermissionHandler.requestPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        }

    }

    /**
     * method to launch a document chooser
     */
    private void launchDocumentChooser() {
        if (isOpen) {
            isOpen = false;
            animateItems(false);
        }


        if (PermissionHandler.checkPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            AppHelper.LogCat("Read data permission already granted.");
            Intent intent = new Intent();
            intent.setType("application/pdf");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            try {
                startActivityForResult(
                        Intent.createChooser(intent, "Choose  document"),
                        AppConstants.UPLOAD_DOCUMENT_REQUEST_CODE);
            } catch (ActivityNotFoundException ex) {
                AppHelper.CustomToast(MessagesActivity.this, "Please install a File Manager.");
            }
        } else {
            AppHelper.LogCat("Please request Read data permission.");
            PermissionHandler.requestPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        }

    }

    /**
     * method to launch audio chooser
     */
    private void launchAudioChooser() {
        if (isOpen) {
            isOpen = false;
            animateItems(false);
        }
        if (PermissionHandler.checkPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            AppHelper.LogCat("Read data permission already granted.");
            Intent intent = new Intent();
            intent.setType("audio/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(
                    Intent.createChooser(intent, "Choose an audio"),
                    AppConstants.UPLOAD_AUDIO_REQUEST_CODE);
        } else {
            AppHelper.LogCat("Please request Read data permission.");
            PermissionHandler.requestPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        }

    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        File fileVideo = null;
        // Get file from file name
        File file = null;
        Bitmap thumbnailBitmap;
        if (resultCode == RESULT_OK) {
            if (PermissionHandler.checkPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                AppHelper.LogCat("Read contact data permission already granted.");
                switch (requestCode) {
                    case AppConstants.UPLOAD_PICTURE_REQUEST_CODE:
                        FileImagePath = FilesManager.getPath(getApplicationContext(), data.getData());
                        if (FileImagePath != null) {
                            file = new File(FileImagePath);
                        }
                        if (file != null) {
                            FileSize = String.valueOf(file.length());

                        }
                        sendMessage();
                        break;
                    case AppConstants.SELECT_MESSAGES_CAMERA:
                        if (data != null) {
                            FileImagePath = FilesManager.getPath(getApplicationContext(), data.getData());
                            if (FileImagePath != null) {
                                file = new File(FileImagePath);
                            }
                            if (file != null) {
                                FileSize = String.valueOf(file.length());

                            }
                            sendMessage();
                        } else {
                            try {
                                String[] projection = new String[]{MediaStore.Images.ImageColumns._ID, MediaStore.Images.ImageColumns.DATA, MediaStore
                                        .Images.ImageColumns.BUCKET_DISPLAY_NAME, MediaStore.Images.ImageColumns.DATE_TAKEN, MediaStore.Images
                                        .ImageColumns.MIME_TYPE};
                                final Cursor cursor = getApplicationContext().getContentResolver()
                                        .query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, MediaStore.Images.ImageColumns
                                                .DATE_TAKEN + " DESC");

                                if (cursor != null && cursor.moveToFirst()) {
                                    String imageLocation = cursor.getString(1);
                                    cursor.close();
                                    File imageFile = new File(imageLocation);
                                    if (imageFile.exists()) {
                                        FileImagePath = imageFile.getPath();
                                        AppHelper.LogCat("FileImagePath  11 " + imageFile.getPath());
                                        AppHelper.LogCat("FileImagePath 22 " + FilesManager.getPath(this, Uri.fromFile(imageFile)));
                                        file = new File(FileImagePath);
                                        FileImagePath = AppHelper.fixRotation(file);
                                        FileSize = String.valueOf(file.length());
                                        sendMessage();
                                    }
                                }
                            } catch (Exception e) {
                                AppHelper.LogCat("error" + e);
                                return;
                            }
                        }
                        break;
                    case AppConstants.UPLOAD_VIDEO_REQUEST_CODE:
                        try {
                            FileVideoPath = FilesManager.getPath(getApplicationContext(), data.getData());
                            if (FileVideoPath != null) {
                                file = new File(FileVideoPath);
                                MediaPlayer mp = MediaPlayer.create(this, Uri.parse(FileVideoPath));
                                int duration = mp.getDuration();
                                Duration = String.valueOf(duration);
                                mp.release();
                            }
                            if (file != null) {
                                FileSize = String.valueOf(file.length());
                            }
                            thumbnailBitmap = ThumbnailUtils.createVideoThumbnail(FileVideoPath, MediaStore.Video.Thumbnails.MINI_KIND);
                            try {
                                fileVideo = FilesManager.getFileThumbnail(this, thumbnailBitmap);
                            } catch (IOException e) {
                                AppHelper.LogCat("IOException video thumbnail " + e.getMessage());
                            }
                            FileVideoThumbnailPath = FilesManager.getPath(getApplicationContext(), Uri.fromFile(fileVideo));
                            sendMessage();
                        } catch (Exception e) {
                            AppHelper.LogCat(" Exception " + e.getMessage());
                            return;
                        }

                        break;
                    case AppConstants.SELECT_MESSAGES_RECORD_VIDEO:

                        if (data.getData() != null) {
                            try {
                                FileVideoPath = FilesManager.getPath(getApplicationContext(), data.getData());
                                if (FileVideoPath != null) {
                                    file = new File(FileVideoPath);
                                    MediaPlayer mp = MediaPlayer.create(this, Uri.parse(FileVideoPath));
                                    int duration = mp.getDuration();
                                    Duration = String.valueOf(duration);
                                    mp.release();
                                }
                                if (file != null) {
                                    FileSize = String.valueOf(file.length());
                                }
                                thumbnailBitmap = ThumbnailUtils.createVideoThumbnail(FileVideoPath, MediaStore.Video.Thumbnails.MINI_KIND);
                                try {
                                    fileVideo = FilesManager.getFileThumbnail(this, thumbnailBitmap);
                                } catch (IOException e) {
                                    AppHelper.LogCat("IOException video thumbnail " + e.getMessage());
                                }
                                FileVideoThumbnailPath = FilesManager.getPath(getApplicationContext(), Uri.fromFile(fileVideo));
                                sendMessage();
                            } catch (Exception e) {
                                AppHelper.LogCat(" Exception " + e.getMessage());
                                return;
                            }

                        } else {
                            try {
                                String[] projection = new String[]{MediaStore.Video.VideoColumns._ID, MediaStore.Video.VideoColumns.DATA, MediaStore
                                        .Video.VideoColumns.BUCKET_DISPLAY_NAME, MediaStore.Video.VideoColumns.DATE_TAKEN, MediaStore.Video
                                        .VideoColumns.MIME_TYPE};
                                final Cursor cursor = getApplicationContext().getContentResolver()
                                        .query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection, null, null, MediaStore.Video.VideoColumns
                                                .DATE_TAKEN + " DESC");

                                if (cursor != null && cursor.moveToFirst()) {
                                    String videoLocation = cursor.getString(1);
                                    cursor.close();
                                    File videoFile = new File(videoLocation);
                                    if (videoFile.exists()) {
                                        FileVideoPath = videoFile.getPath();
                                        MediaPlayer mp = MediaPlayer.create(this, Uri.parse(FileVideoPath));
                                        int duration = mp.getDuration();
                                        Duration = String.valueOf(duration);
                                        mp.release();
                                        file = new File(FileVideoPath);
                                        FileSize = String.valueOf(file.length());

                                        thumbnailBitmap = ThumbnailUtils.createVideoThumbnail(FileVideoPath, MediaStore.Video.Thumbnails.MINI_KIND);
                                        try {
                                            fileVideo = FilesManager.getFileThumbnail(this, thumbnailBitmap);
                                        } catch (IOException e) {
                                            AppHelper.LogCat("IOException video thumbnail " + e.getMessage());
                                        }
                                        FileVideoThumbnailPath = FilesManager.getPath(getApplicationContext(), Uri.fromFile(fileVideo));
                                        sendMessage();
                                    }
                                }
                            } catch (Exception e) {
                                AppHelper.LogCat("Exception videoLocation MessagesPopupActivity " + e);
                                return;
                            }
                        }
                        break;
                    case AppConstants.UPLOAD_AUDIO_REQUEST_CODE:
                        try {
                            FileAudioPath = FilesManager.getPath(getApplicationContext(), data.getData());
                            MediaPlayer mp = MediaPlayer.create(this, Uri.parse(FileAudioPath));
                            int duration = mp.getDuration();
                            Duration = String.valueOf(duration);
                            mp.release();
                            sendMessage();
                        } catch (Exception e) {
                            AppHelper.LogCat(" Exception " + e.getMessage());
                            return;
                        }


                        break;
                    case AppConstants.UPLOAD_DOCUMENT_REQUEST_CODE:
                        FileDocumentPath = FilesManager.getPath(getApplicationContext(), data.getData());
                        if (FileDocumentPath != null) {
                            file = new File(FileDocumentPath);
                        }
                        if (file != null) {
                            FileSize = String.valueOf(file.length());
                        }
                        sendMessage();
                        break;
                    case AppConstants.SELECT_ADD_NEW_CONTACT:
                        EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_NEW_CONTACT_ADDED));
                        mMessagesPresenter.getRecipientInfo();
                        break;

                }
            } else {
                AppHelper.LogCat("Please request Read contact data permission.");
                PermissionHandler.requestPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            }


        }
    }


    /**
     * method to initialize the massage wrapper
     */

    private void initializerMessageWrapper() {


        final Context context = this;
        messageWrapper.setFocusable(true);
        messageWrapper.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            } else {
                AppHelper.LogCat("Has focused");
                emitMessageSeen();
                if (isGroup) {
                    new Handler().postDelayed(() -> mMessagesPresenter.updateGroupConversationStatus(), 500);
                } else {
                    new Handler().postDelayed(() -> mMessagesPresenter.updateConversationStatus(), 500);
                }
            }

        });

        messageWrapper.setOnClickListener(v1 -> {
            if (emoticonShown) {
                emoticonShown = false;
                emojIcon.closeEmojIcon();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            }

        });
        messageWrapper.addTextChangedListener(new TextWatcherAdapter() {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                SendRecordButton.setVisibility(View.VISIBLE);
                SendButton.setVisibility(View.GONE);
                PictureButton.setVisibility(View.VISIBLE);

            }

            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (messageWrapper.getLineCount() >= 6) {
                    messageWrapper.setScroller(new Scroller(MessagesActivity.this));
                    messageWrapper.setMaxLines(6);
                    messageWrapper.setVerticalScrollBarEnabled(true);
                    messageWrapper.setMovementMethod(new ScrollingMovementMethod());
                }

                if (!isSeen)
                    emitMessageSeen();
                isSeen = true;
                SendRecordButton.setVisibility(View.GONE);
                SendButton.setVisibility(View.VISIBLE);
                PictureButton.setVisibility(View.GONE);


                if (!mSocket.connected()) return;
                if (isGroup) {
                    try {
                        if (mGroupsModel.getMembers() != null && mGroupsModel.getMembers().size() != 0) {
                            for (MembersGroupModel membersGroupModel : mGroupsModel.getMembers()) {
                                if (!isTyping && s.length() != 0) {
                                    isTyping = true;
                                    JSONObject data = new JSONObject();
                                    try {
                                        data.put("recipientId", membersGroupModel.getUserId());
                                        data.put("senderId", senderId);
                                        data.put("groupId", groupID);
                                    } catch (JSONException e) {
                                        AppHelper.LogCat(e);
                                    }
                                    mSocket.emit(AppConstants.SOCKET_IS_MEMBER_TYPING, data);
                                }

                                mTypingHandler.removeCallbacks(onTypingTimeout);
                                mTypingHandler.postDelayed(onTypingTimeout, TYPING_TIMER_LENGTH);
                            }
                        }
                    } catch (Exception e) {
                        AppHelper.LogCat(e);
                    }
                } else {

                    if (!isTyping && s.length() != 0) {
                        isTyping = true;
                        JSONObject data = new JSONObject();
                        try {
                            data.put("recipientId", recipientId);
                            data.put("senderId", senderId);
                        } catch (JSONException e) {
                            AppHelper.LogCat(e);
                        }
                        mSocket.emit(AppConstants.SOCKET_IS_TYPING, data);
                    }

                    mTypingHandler.removeCallbacks(onTypingTimeout);
                    mTypingHandler.postDelayed(onTypingTimeout, TYPING_TIMER_LENGTH);

                }

                if (PreferenceSettingsManager.enter_send(MessagesActivity.this)) {
                    messageWrapper.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT);
                    messageWrapper.setSingleLine(true);
                    messageWrapper.setOnEditorActionListener((v, actionId, event) -> {
                        if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_SEND)) {
                            sendMessage();
                        }
                        return false;
                    });
                }

            }

            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void afterTextChanged(Editable s) {

                if (s.length() == 0) {
                    SendRecordButton.setVisibility(View.VISIBLE);
                    SendButton.setVisibility(View.GONE);
                    PictureButton.setVisibility(View.VISIBLE);
                }

            }
        });
        messageWrapper.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT);
        messageWrapper.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT);
        messageWrapper.setSingleLine(false);
    }

    private Runnable onTypingTimeout = new Runnable() {
        @Override
        public void run() {
            if (!isTyping) return;

            isTyping = false;
            if (isGroup) {

                for (MembersGroupModel membersGroupModel : mGroupsModel.getMembers()) {
                    JSONObject json = new JSONObject();
                    try {
                        json.put("recipientId", membersGroupModel.getUserId());
                        json.put("senderId", senderId);
                        json.put("groupId", groupID);
                    } catch (JSONException e) {
                        AppHelper.LogCat(e);
                    }
                    mSocket.emit(AppConstants.SOCKET_IS_MEMBER_STOP_TYPING, json);
                    isTyping = false;


                }
            } else {
                JSONObject json = new JSONObject();
                try {
                    json.put("recipientId", recipientId);
                    json.put("senderId", senderId);
                } catch (JSONException e) {
                    AppHelper.LogCat(e);
                }
                mSocket.emit(AppConstants.SOCKET_IS_STOP_TYPING, json);
                isTyping = false;

            }
        }
    };

    /**
     * method to send the new message
     */
    private void sendMessage() {

        isSeen = false;
        if (isGroup) {
            new Handler().postDelayed(() -> mMessagesPresenter.updateGroupConversationStatus(), 500);
        } else {
            new Handler().postDelayed(() -> mMessagesPresenter.updateConversationStatus(), 500);
        }

        EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_START_CONVERSATION));//for change viewpager current item to 0
        String messageBody = UtilsString.escapeJava(messageWrapper.getText().toString().trim());
        if (messageTransfer != null)
            messageBody = messageTransfer;

        if (FileImagePath == null && FileAudioPath == null && FileDocumentPath == null && FileVideoPath == null) {
            if (messageBody.isEmpty()) return;
        }
        DateTime current = new DateTime();
        String sendTime = String.valueOf(current);

        if (isGroup) {
            final JSONObject messageGroup = new JSONObject();
            try {
                messageGroup.put("messageBody", messageBody);
                messageGroup.put("senderId", senderId);
                try {

                    messageGroup.put("senderName", "null");

                    messageGroup.put("phone", mUsersModel.getPhone());
                    if (mGroupsModel.getGroupImage() != null)
                        messageGroup.put("GroupImage", mGroupsModel.getGroupImage());
                    else
                        messageGroup.put("GroupImage", "null");
                    if (mGroupsModel.getGroupName() != null)
                        messageGroup.put("GroupName", mGroupsModel.getGroupName());
                    else
                        messageGroup.put("GroupName", "null");
                } catch (Exception e) {
                    AppHelper.LogCat(e);
                }

                messageGroup.put("groupID", groupID);
                messageGroup.put("date", sendTime);
                messageGroup.put("isGroup", true);

                if (FileImagePath != null)
                    messageGroup.put("image", FileImagePath);
                else
                    messageGroup.put("image", "null");

                if (FileVideoPath != null)
                    messageGroup.put("video", FileVideoPath);
                else
                    messageGroup.put("video", "null");

                if (FileVideoThumbnailPath != null)
                    messageGroup.put("thumbnail", FileVideoThumbnailPath);
                else
                    messageGroup.put("thumbnail", "null");

                if (FileAudioPath != null)
                    messageGroup.put("audio", FileAudioPath);
                else
                    messageGroup.put("audio", "null");

                if (FileDocumentPath != null)
                    messageGroup.put("document", FileDocumentPath);
                else
                    messageGroup.put("document", "null");

                if (!FileSize.equals("0"))
                    messageGroup.put("fileSize", FileSize);
                else
                    messageGroup.put("fileSize", "0");

                if (!Duration.equals("0"))
                    messageGroup.put("duration", Duration);
                else
                    messageGroup.put("duration", "0");

                messageGroup.put("userToken", PreferenceManager.getToken(this));
            } catch (JSONException e) {
                AppHelper.LogCat("send group message " + e.getMessage());
            }
            unSentMessagesGroup(groupID);
            new Handler().postDelayed(() -> setStatusAsWaiting(messageGroup, true), 100);
            AppHelper.LogCat("send group message to");

        } else {
            final JSONObject message = new JSONObject();
            try {
                message.put("messageBody", messageBody);
                message.put("recipientId", recipientId);
                message.put("senderId", senderId);
                try {

                    message.put("senderName", "null");

                    if (mUsersModel.getImage() != null)
                        message.put("senderImage", mUsersModel.getImage());
                    else
                        message.put("senderImage", "null");
                    message.put("phone", mUsersModel.getPhone());
                } catch (Exception e) {
                    AppHelper.LogCat("Sender name " + e.getMessage());
                }


                message.put("date", sendTime);
                message.put("isGroup", false);
                message.put("conversationId", ConversationID);
                if (FileImagePath != null)
                    message.put("image", FileImagePath);
                else
                    message.put("image", "null");

                if (FileVideoPath != null)
                    message.put("video", FileVideoPath);
                else
                    message.put("video", "null");

                if (FileVideoThumbnailPath != null)
                    message.put("thumbnail", FileVideoThumbnailPath);
                else
                    message.put("thumbnail", "null");

                if (FileAudioPath != null)
                    message.put("audio", FileAudioPath);
                else
                    message.put("audio", "null");


                if (FileDocumentPath != null)
                    message.put("document", FileDocumentPath);
                else
                    message.put("document", "null");


                if (!FileSize.equals("0"))
                    message.put("fileSize", FileSize);
                else
                    message.put("fileSize", "0");

                if (!Duration.equals("0"))
                    message.put("duration", Duration);
                else
                    message.put("duration", "0");

                message.put("userToken", PreferenceManager.getToken(this));
            } catch (JSONException e) {
                AppHelper.LogCat("send message " + e.getMessage());
            }
            unSentMessagesForARecipient(recipientId, false);
            new Handler().postDelayed(() -> setStatusAsWaiting(message, false), 100);
        }
        messageWrapper.setText("");
        messageTransfer = null;


    }

    /**
     * method to check  for unsent messages group
     *
     * @param groupID this parameter of  unSentMessagesGroup  method
     */
    private void unSentMessagesGroup(int groupID) {
        Realm realm = DostChatApp.getRealmDatabaseInstance();

        List<MessagesModel> messagesModelsList = realm.where(MessagesModel.class)
                .equalTo("status", AppConstants.IS_WAITING)
                .equalTo("isGroup", true)
                .equalTo("groupID", groupID)
                .equalTo("isFileUpload", true)
                .equalTo("senderID", PreferenceManager.getID(this))
                .findAllSorted("id", Sort.ASCENDING);
        AppHelper.LogCat("size " + messagesModelsList.size());
        if (messagesModelsList.size() != 0) {
            for (MessagesModel messagesModel : messagesModelsList) {
                MainService.sendMessagesGroup(this, mUsersModel, mGroupsModel, messagesModel);
            }
        }
        realm.close();
    }


    /**
     * method to check for unsent user messages
     *
     * @param recipientID this is parameter of unSentMessagesForARecipient method
     */
    private void unSentMessagesForARecipient(int recipientID, boolean forFiles) {
        Realm realm = DostChatApp.getRealmDatabaseInstance();
        List<MessagesModel> messagesModelsList = realm.where(MessagesModel.class)
                .equalTo("status", AppConstants.IS_WAITING)
                .equalTo("recipientID", recipientID)
                .equalTo("isFileUpload", true)
                .equalTo("isGroup", false)
                .equalTo("senderID", PreferenceManager.getID(this))
                .findAllSorted("id", Sort.ASCENDING);
        AppHelper.LogCat("size " + messagesModelsList.size());
        if (messagesModelsList.size() != 0) {
            if (forFiles) {
                for (MessagesModel messagesModel : messagesModelsList) {
                    MainService.sendMessagesFiles(this, messagesModel);
                }
            } else {
                for (MessagesModel messagesModel : messagesModelsList) {
                    MainService.sendMessages(messagesModel);
                }
            }
        }
        realm.close();

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
                    .endGroup().findFirst();
            return conversationsModelNew.getId();
        } catch (Exception e) {
            AppHelper.LogCat("Get conversation id Exception MessagesPopupActivity " + e.getMessage());
            return 0;
        }
    }


    /**
     * method to save new message as waitng messages
     *
     * @param data    this is the first parameter for setStatusAsWaiting method
     * @param isgroup this is the second parameter for setStatusAsWaiting method
     */
    private void setStatusAsWaiting(JSONObject data, boolean isgroup) {
        Realm realm = DostChatApp.getRealmDatabaseInstance();
        try {
            if (isgroup) {
                int senderId = data.getInt("senderId");
                String messageBody = data.getString("messageBody");
                String senderName = data.getString("senderName");
                String senderPhone = data.getString("phone");
                String GroupImage = data.getString("GroupImage");
                String GroupName = data.getString("GroupName");
                String dateTmp = data.getString("date");
                String video = data.getString("video");
                String thumbnail = data.getString("thumbnail");
                boolean isGroup = data.getBoolean("isGroup");
                String image = data.getString("image");
                String audio = data.getString("audio");
                String document = data.getString("document");
                String fileSize = data.getString("fileSize");
                String duration = data.getString("duration");
                int groupID = data.getInt("groupID");
                realm.executeTransactionAsync(realm1 -> {

                    int lastID = RealmBackupRestore.getMessageLastId();
                    ConversationsModel conversationsModel = realm1.where(ConversationsModel.class).equalTo("groupID", groupID).findFirst();
                    RealmList<MessagesModel> messagesModelRealmList = conversationsModel.getMessages();
                    MessagesModel messagesModel = new MessagesModel();
                    messagesModel.setId(lastID);
                    messagesModel.setDate(dateTmp);
                    messagesModel.setStatus(AppConstants.IS_WAITING);
                    messagesModel.setUsername(senderName);
                    messagesModel.setSenderID(PreferenceManager.getID(this));
                    messagesModel.setGroup(isGroup);
                    messagesModel.setMessage(messageBody);
                    messagesModel.setGroupID(groupID);
                    messagesModel.setImageFile(image);
                    messagesModel.setVideoFile(video);
                    messagesModel.setAudioFile(audio);
                    messagesModel.setDocumentFile(document);
                    messagesModel.setFileSize(fileSize);
                    messagesModel.setDuration(duration);
                    messagesModel.setVideoThumbnailFile(thumbnail);
                    if (!image.equals("null") || !video.equals("null") || !audio.equals("null") || !document.equals("null") || !thumbnail.equals("null")) {
                        messagesModel.setFileUpload(false);

                    } else {
                        messagesModel.setFileUpload(true);
                    }
                    messagesModel.setFileDownLoad(true);
                    messagesModel.setConversationID(conversationsModel.getId());
                    messagesModelRealmList.add(messagesModel);
                    conversationsModel.setLastMessage(messageBody);
                    conversationsModel.setLastMessageId(lastID);
                    conversationsModel.setMessages(messagesModelRealmList);
                    conversationsModel.setStatus(AppConstants.IS_WAITING);
                    conversationsModel.setUnreadMessageCounter("0");
                    conversationsModel.setRecipientID(0);
                    realm1.copyToRealmOrUpdate(conversationsModel);
                    runOnUiThread(() -> addMessage(messagesModel));


                }, () -> {
                    if (!image.equals("null") || !video.equals("null") || !audio.equals("null") || !document.equals("null") || !thumbnail.equals("null"))
                        return;

                    mSocket.emit(AppConstants.SOCKET_SAVE_NEW_MESSAGE_GROUP, data, (Ack) argObjects -> {
                        JSONObject dataString = (JSONObject) argObjects[0];
                        runOnUiThread(() -> {
                            List<MembersGroupModel> contactsModelList = mGroupsModel.getMembers();
                            int arraySize = contactsModelList.size();
                            for (int i = 0; i < arraySize; i++) {

                                if (!contactsModelList.get(i).isDeleted()) {
                                    int recipientID = contactsModelList.get(i).getUserId();
                                    new Handler().postDelayed(() -> runOnUiThread(() -> {
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


                                            mSocket.emit(AppConstants.SOCKET_NEW_MESSAGE_GROUP, jsonObject);
                                            EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_NEW_MESSAGE_CONVERSATION_OLD_ROW, ConversationID));
                                        } catch (JSONException e) {
                                            AppHelper.LogCat("JSONException group " + e.getMessage());
                                        }

                                    }), 200);
                                }
                            }
                        });


                    });


                }, error -> {
                    AppHelper.LogCat("Save group message failed MessagesPopupActivity " + error.getMessage());
                });


            } else {
                AppHelper.LogCat("esedd message ");

                int senderId = data.getInt("senderId");
                int recipientId = data.getInt("recipientId");
                String messageBody = data.getString("messageBody");
                String senderName = data.getString("senderName");
                String dateTmp = data.getString("date");
                String video = data.getString("video");
                String thumbnail = data.getString("thumbnail");
                boolean isGroup = data.getBoolean("isGroup");
                String image = data.getString("image");
                String audio = data.getString("audio");
                String document = data.getString("document");
                String phone = data.getString("phone");
                String fileSize = data.getString("fileSize");
                String duration = data.getString("duration");

                String recipientName = mUsersModelRecipient.getUsername();
                String recipientImage = mUsersModelRecipient.getImage();
                String recipientPhone = mUsersModelRecipient.getPhone();
                int conversationID = getConversationId(recipientId, senderId, realm);
                if (conversationID == 0) {
                    realm.executeTransactionAsync(realm1 -> {


                        int lastConversationID = RealmBackupRestore.getConversationLastId();
                        int lastID = RealmBackupRestore.getMessageLastId();
                        RealmList<MessagesModel> messagesModelRealmList = new RealmList<MessagesModel>();
                        MessagesModel messagesModel = new MessagesModel();
                        messagesModel.setId(lastID);
                        messagesModel.setUsername(senderName);
                        messagesModel.setRecipientID(recipientId);
                        messagesModel.setDate(dateTmp);
                        messagesModel.setStatus(AppConstants.IS_WAITING);
                        messagesModel.setGroup(isGroup);
                        messagesModel.setSenderID(senderId);
                        messagesModel.setConversationID(lastConversationID);
                        messagesModel.setMessage(messageBody);
                        messagesModel.setImageFile(image);
                        messagesModel.setVideoFile(video);
                        messagesModel.setAudioFile(audio);
                        messagesModel.setDocumentFile(document);
                        messagesModel.setFileSize(fileSize);
                        messagesModel.setDuration(duration);
                        messagesModel.setVideoThumbnailFile(thumbnail);
                        if (!image.equals("null") || !video.equals("null") || !audio.equals("null") || !document.equals("null") || !thumbnail.equals("null")) {
                            messagesModel.setFileUpload(false);

                        } else {
                            messagesModel.setFileUpload(true);
                        }
                        messagesModel.setFileDownLoad(true);
                        messagesModel.setPhone(phone);
                        messagesModelRealmList.add(messagesModel);
                        ConversationsModel conversationsModel1 = new ConversationsModel();
                        conversationsModel1.setRecipientID(recipientId);
                        conversationsModel1.setLastMessage(messageBody);
                        conversationsModel1.setRecipientUsername(recipientName);
                        conversationsModel1.setRecipientImage(recipientImage);
                        conversationsModel1.setMessageDate(dateTmp);
                        conversationsModel1.setId(lastConversationID);
                        conversationsModel1.setStatus(AppConstants.IS_WAITING);
                        conversationsModel1.setRecipientPhone(recipientPhone);
                        conversationsModel1.setMessages(messagesModelRealmList);
                        conversationsModel1.setUnreadMessageCounter("0");
                        conversationsModel1.setLastMessageId(lastID);
                        conversationsModel1.setCreatedOnline(true);
                        realm1.copyToRealmOrUpdate(conversationsModel1);
                        ConversationID = lastConversationID;
                        runOnUiThread(() -> addMessage(messagesModel));
                        try {
                            data.put("messageId", lastID);
                        } catch (JSONException e) {
                            AppHelper.LogCat("last id");
                        }
                    }, () -> {

                        if (!image.equals("null") || !video.equals("null") || !audio.equals("null") || !document.equals("null") || !thumbnail.equals("null"))
                            return;
                        mSocket.emit(AppConstants.SOCKET_NEW_MESSAGE, data);
                        mSocket.emit(AppConstants.SOCKET_SAVE_NEW_MESSAGE, data);
                        EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_NEW_MESSAGE_CONVERSATION_NEW_ROW, ConversationID));
                    }, error -> AppHelper.LogCat("Error  conversation id MessagesActivity " + error.getMessage()));


                } else {

                    realm.executeTransactionAsync(realm1 -> {
                        try {


                            int lastID = RealmBackupRestore.getMessageLastId();

                            AppHelper.LogCat("last ID  message   MessagesActivity" + lastID);
                            ConversationsModel conversationsModel;
                            RealmQuery<ConversationsModel> conversationsModelRealmQuery = realm1.where(ConversationsModel.class).equalTo("id", conversationID);
                            conversationsModel = conversationsModelRealmQuery.findAll().first();
                            MessagesModel messagesModel = new MessagesModel();
                            messagesModel.setId(lastID);
                            messagesModel.setUsername(senderName);
                            messagesModel.setRecipientID(recipientId);
                            messagesModel.setDate(dateTmp);
                            messagesModel.setStatus(AppConstants.IS_WAITING);
                            messagesModel.setGroup(isGroup);
                            messagesModel.setSenderID(senderId);
                            messagesModel.setConversationID(conversationID);
                            messagesModel.setMessage(messageBody);
                            messagesModel.setImageFile(image);
                            messagesModel.setVideoFile(video);
                            messagesModel.setAudioFile(audio);
                            messagesModel.setDocumentFile(document);
                            messagesModel.setFileSize(fileSize);
                            messagesModel.setDuration(duration);
                            messagesModel.setVideoThumbnailFile(thumbnail);
                            if (!image.equals("null") || !video.equals("null") || !audio.equals("null") || !document.equals("null") || !thumbnail.equals("null")) {
                                messagesModel.setFileUpload(false);

                            } else {
                                messagesModel.setFileUpload(true);
                            }
                            messagesModel.setFileDownLoad(true);
                            messagesModel.setPhone(phone);
                            conversationsModel.getMessages().add(messagesModel);
                            conversationsModel.setLastMessageId(lastID);
                            conversationsModel.setLastMessage(messageBody);
                            conversationsModel.setMessageDate(dateTmp);
                            conversationsModel.setCreatedOnline(true);
                            realm1.copyToRealmOrUpdate(conversationsModel);
                            runOnUiThread(() -> addMessage(messagesModel));
                            try {
                                data.put("messageId", lastID);
                            } catch (JSONException e) {
                                AppHelper.LogCat("last id");
                            }
                        } catch (Exception e) {
                            AppHelper.LogCat("Exception  last id message  MessagesActivity " + e.getMessage());
                        }
                    }, () -> {

                        if (!image.equals("null") || !video.equals("null") || !audio.equals("null") || !document.equals("null") || !thumbnail.equals("null"))
                            return;
                        mSocket.emit(AppConstants.SOCKET_NEW_MESSAGE, data);
                        mSocket.emit(AppConstants.SOCKET_SAVE_NEW_MESSAGE, data);
                        EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_NEW_MESSAGE_CONVERSATION_OLD_ROW, conversationID));
                    }, error -> AppHelper.LogCat("Error  last id  MessagesActivity " + error.getMessage()));
                }
            }


        } catch (JSONException e) {
            AppHelper.LogCat("JSONException  MessagesActivity " + e);
        }

        FileAudioPath = null;
        FileVideoPath = null;
        FileDocumentPath = null;
        FileImagePath = null;
        FileVideoThumbnailPath = null;
        FileSize = "0";
        Duration = "0";
        if (!realm.isClosed())
            realm.close();
    }


    private boolean checkIfUserBlockedExist(int userId, Realm realm) {
        RealmQuery<UsersBlockModel> query = realm.where(UsersBlockModel.class).equalTo("contactsModel.id", userId);
        return query.count() != 0;
    }

    /**
     * refresh the menu for new contact
     * doesn't exist in contactModel
     */
    public void refreshMenu() {
        invalidateOptionsMenu();
        supportInvalidateOptionsMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        invalidateOptionsMenu();
        supportInvalidateOptionsMenu();
        if (isGroup) {
            if (isLeft)
                getMenuInflater().inflate(R.menu.groups_menu_user_left, menu);
            else
                getMenuInflater().inflate(R.menu.groups_menu, menu);
        } else {
            try {
                if (mUsersModelRecipient.getPhone() != null && UtilsPhone.checkIfContactExist(this, mUsersModelRecipient.getPhone())) {
                    if (checkIfUserBlockedExist(recipientId, realm)) {
                        getMenuInflater().inflate(R.menu.messages_menu_unblock, menu);
                    } else {
                        getMenuInflater().inflate(R.menu.messages_menu, menu);
                    }

                } else {
                    if (checkIfUserBlockedExist(recipientId, realm)) {
                        getMenuInflater().inflate(R.menu.messages_menu_user_not_exist_unblock, menu);
                    } else {
                        getMenuInflater().inflate(R.menu.messages_menu_user_not_exist, menu);
                    }

                }
            } catch (Exception e) {
                AppHelper.LogCat(" Exception mUsersModelRecipient.getPhone() " + e.getMessage());
            }


        }

        super.onCreateOptionsMenu(menu);
        return true;
    }


    private void makeCall() {
        AlertDialog myDialog;
        String[] items = {"Voice", "Video"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setItems(items, (dialog, which) -> {
            if (items.length > 0) {
                if (items[which].equals("Voice")) {
                    CallManager.callContact(MessagesActivity.this, true, false, recipientId);
                } else if (items[which].equals("Video")) {
                    CallManager.callContact(MessagesActivity.this, true, true, recipientId);
                }
            }
        });
        builder.setCancelable(true);
        myDialog = builder.create();
        myDialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (isGroup) {

            String name = UtilsString.unescapeJava(mGroupsModel.getGroupName());
            if (name.length() > 13)
                name = name.substring(0, 10) + "... " + "";

            switch (item.getItemId()) {

                case R.id.video_call_group:
                    System.out.println("Called");
                    Intent i = new Intent(MessagesActivity.this, LiveRoomActivity.class);
                    i.putExtra(ACTION_KEY_CHANNEL_NAME, name);
                    i.putExtra(ACTION_KEY_ENCRYPTION_KEY, "key");
                    i.putExtra(ACTION_KEY_ENCRYPTION_MODE, getResources().getStringArray(R.array.encryption_mode_values)[0]);
                    startActivity(i);

                    break;

                case R.id.attach_file:
                    if (!isOpen) {
                        isOpen = true;
                        animateItems(true);

                    } else {
                        isOpen = false;
                        animateItems(false);
                    }
                    break;
                case R.id.search_messages_group:
                    launcherSearchView();
                    break;
                case R.id.view_group:
                    mIntent = new Intent(this, ProfileActivity.class);
                    mIntent.putExtra("groupID", groupID);
                    mIntent.putExtra("isGroup", true);
                    startActivity(mIntent);
                    break;
            }
        } else {

            switch (item.getItemId()) {
                case R.id.attach_file:
                    if (!isOpen) {
                        isOpen = true;
                        animateItems(true);

                    } else {
                        isOpen = false;
                        animateItems(false);
                    }
                    break;
                case R.id.call_contact:
                    if (isOpen) {
                        isOpen = false;
                        animateItems(false);
                    }
                    makeCall();
                    break;
                case R.id.search_messages:
                    launcherSearchView();
                    break;
                case R.id.add_contact:
                    addNewContact();
                    break;
                case R.id.view_contact:
                    mIntent = new Intent(this, ProfileActivity.class);
                    mIntent.putExtra("userID", recipientId);
                    mIntent.putExtra("isGroup", false);
                    startActivity(mIntent);
                    break;
                case R.id.clear_chat:
                    Realm realm = DostChatApp.getRealmDatabaseInstance();
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(R.string.clear_chat);
                    builder.setPositiveButton(R.string.Yes, (dialog, whichButton) -> {
                        AppHelper.showDialog(this, getString(R.string.clear_chat));
                        EventBus.getDefault().post(new Pusher(EVENT_BUS_DELETE_CONVERSATION_ITEM, ConversationID));
                        realm.executeTransactionAsync(realm1 -> {
                            RealmResults<MessagesModel> messagesModel1 = realm1.where(MessagesModel.class).equalTo("conversationID", ConversationID).findAll();
                            messagesModel1.deleteAllFromRealm();
                        }, () -> {
                            AppHelper.LogCat("Message Deleted  successfully  MessagesPopupActivity");

                            RealmResults<MessagesModel> messagesModel1 = realm.where(MessagesModel.class).equalTo("conversationID", ConversationID).findAll();
                            if (messagesModel1.size() == 0) {
                                realm.executeTransactionAsync(realm1 -> {
                                    ConversationsModel conversationsModel1 = realm1.where(ConversationsModel.class).equalTo("id", ConversationID).findFirst();
                                    conversationsModel1.deleteFromRealm();
                                }, () -> {
                                    AppHelper.LogCat("Conversation deleted successfully MessagesPopupActivity");

                                    EventBus.getDefault().post(new Pusher(EVENT_BUS_MESSAGE_COUNTER));
                                    NotificationsManager.SetupBadger(this);
                                    finish();
                                }, error -> {
                                    AppHelper.LogCat("Delete conversation failed  MessagesPopupActivity" + error.getMessage());

                                });
                            } else {
                                MessagesModel lastMessage = realm.where(MessagesModel.class).equalTo("conversationID", ConversationID).findAll().last();
                                realm.executeTransactionAsync(realm1 -> {
                                    ConversationsModel conversationsModel1 = realm1.where(ConversationsModel.class).equalTo("id", ConversationID).findFirst();
                                    conversationsModel1.setLastMessage(lastMessage.getMessage());
                                    conversationsModel1.setLastMessageId(lastMessage.getId());
                                    realm1.copyToRealmOrUpdate(conversationsModel1);
                                }, () -> {
                                    AppHelper.LogCat("Conversation deleted successfully MessagesPopupActivity ");
                                    EventBus.getDefault().post(new Pusher(EVENT_BUS_MESSAGE_COUNTER));
                                    NotificationsManager.SetupBadger(this);
                                    finish();
                                }, error -> {
                                    AppHelper.LogCat("Delete conversation failed  MessagesPopupActivity" + error.getMessage());

                                });
                            }
                        }, error -> {
                            AppHelper.LogCat("Delete message failed MessagesPopupActivity" + error.getMessage());

                        });
                        AppHelper.hideDialog();

                    });

                    builder.setNegativeButton(R.string.No, (dialog, whichButton) -> {

                    });

                    builder.show();

                    if (!realm.isClosed())
                        realm.close();
                    break;
                case R.id.block_user:
                    blockContact();
                    break;
                case R.id.unblock_user:
                    unBlockContact();
                    break;


            }
        }
        return true;
    }

    /**
     * method to close the searchview with animation
     */
    @SuppressWarnings("unused")
    @OnClick(R.id.close_btn_search_view)
    public void closeSearchView() {
        final Animation animation = AnimationUtils.loadAnimation(MessagesActivity.this, R.anim.scale_for_button_animtion_exit);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                searchView.setVisibility(View.GONE);
                toolbar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        searchView.startAnimation(animation);
    }

    /**
     * method to clear/reset search view
     */
    @SuppressWarnings("unused")
    @OnClick(R.id.clear_btn_search_view)
    public void clearSearchView() {
        searchInput.setText("");
    }

    private void launcherSearchView() {
        final Animation animation = AnimationUtils.loadAnimation(MessagesActivity.this, R.anim.scale_for_button_animtion_enter);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                searchView.setVisibility(View.VISIBLE);
                toolbar.setVisibility(View.GONE);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        searchView.startAnimation(animation);
    }

    /**
     * method to initialize the search view
     *
     * @param searchInput    this is the  first parameter for initializerSearchView method
     * @param clearSearchBtn this is the second parameter for initializerSearchView method
     */
    public void initializerSearchView(TextInputEditText searchInput, ImageView clearSearchBtn) {

        final Context context = this;
        searchInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }

        });
        searchInput.addTextChangedListener(new TextWatcherAdapter() {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                clearSearchBtn.setVisibility(View.GONE);
            }

            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mMessagesAdapter.setString(s.toString());
                Search(s.toString().trim());
                clearSearchBtn.setVisibility(View.VISIBLE);
            }

            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void afterTextChanged(Editable s) {

                if (s.length() == 0) {
                    clearSearchBtn.setVisibility(View.GONE);
                }
            }
        });

    }

    /**
     * method to start searching
     *
     * @param string this  is the parameter for Search method
     */
    public void Search(String string) {
        final List<MessagesModel> filteredModelList;
        filteredModelList = FilterList(string);
        if (filteredModelList.size() != 0) {
            mMessagesAdapter.animateTo(filteredModelList);
            messagesList.scrollToPosition(0);
        }
    }


    /**
     * method to filter the list
     *
     * @param query this is parameter for FilterList method
     * @return this what method will return
     */
    private List<MessagesModel> FilterList(String query) {
        Realm realm = DostChatApp.getRealmDatabaseInstance();
        List<MessagesModel> messagesModels = null;
        if (isGroup) {

            messagesModels = realm.where(MessagesModel.class)
                    .contains("message", query, Case.INSENSITIVE)
                    .equalTo("conversationID", ConversationID)
                    .equalTo("isGroup", true).findAllSorted("id", Sort.ASCENDING);
        } else {


            if (ConversationID == 0) {
                try {
                    ConversationsModel conversationsModel = realm.where(ConversationsModel.class)
                            .beginGroup()
                            .equalTo("RecipientID", recipientId)
                            .or()
                            .equalTo("RecipientID", senderId)
                            .endGroup().findAll().first();

                    messagesModels = realm.where(MessagesModel.class)
                            .contains("message", query, Case.INSENSITIVE)
                            .equalTo("conversationID", conversationsModel.getId())
                            .equalTo("isGroup", false)
                            .findAllSorted("id", Sort.ASCENDING);

                } catch (Exception e) {
                    AppHelper.LogCat(" Conversation Exception MessagesPopupActivity" + e.getMessage());
                }
            } else {
                messagesModels = realm.where(MessagesModel.class)
                        .equalTo("conversationID", ConversationID)
                        .contains("message", query, Case.INSENSITIVE)
                        .equalTo("isGroup", false)
                        .findAllSorted("id", Sort.ASCENDING);
            }

        }
        realm.close();

        return messagesModels;
    }


    /**
     * method to emit that message are seen by user
     */
    private void emitMessageSeen() {
        JSONObject json = new JSONObject();
        try {
            json.put("recipientId", recipientId);
            json.put("senderId", senderId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mSocket.emit(AppConstants.SOCKET_IS_MESSAGE_SEEN, json);
    }

    /**
     * method to show all user messages
     *
     * @param messagesModels this is parameter for ShowMessages method
     */
    public void ShowMessages(List<MessagesModel> messagesModels) {

        RealmList<MessagesModel> mMessagesList = new RealmList<MessagesModel>();
        for (MessagesModel messagesModel : messagesModels) {
            mMessagesList.add(messagesModel);
            ConversationID = messagesModel.getConversationID();
        }
        mMessagesAdapter.setMessages(mMessagesList);
    }

    /**
     * method to update  contact information
     *
     * @param contactsModels this is parameter for updateContact method
     */
    public void updateContact(ContactsModel contactsModels) {
        mUsersModel = contactsModels;
    }

    /**
     * method to update group information
     *
     * @param groupsModel
     */
    public void updateGroupInfo(GroupsModel groupsModel) {
        mGroupsModel = groupsModel;
        Bitmap bitmap = ImageLoader.GetCachedBitmapImage(memoryCache, groupsModel.getGroupImage(), this, groupID, AppConstants.GROUP, AppConstants.ROW_PROFILE);
        if (bitmap != null) {
            ImageLoader.SetBitmapImage(bitmap, ToolbarImage);
        } else {
            DostChatImageLoader.loadCircleImageGroup(this, EndPoints.ROWS_IMAGE_URL + groupsModel.getGroupImage(), ToolbarImage, R.drawable.image_holder_gr_circle, AppConstants.ROWS_IMAGE_SIZE);
        }
        String name = UtilsString.unescapeJava(groupsModel.getGroupName());
        if (name.length() > 13)
            ToolbarTitle.setText(name.substring(0, 10) + "... " + "");
        else
            ToolbarTitle.setText(name);

        Realm realm = DostChatApp.getRealmDatabaseInstance();
        List<MembersGroupModel> groups = realm.where(MembersGroupModel.class).equalTo("groupID", groupID).equalTo("Deleted", false).findAll();
        int arraySize = groups.size();
        StringBuilder names = new StringBuilder();
        for (int x = 0; x <= arraySize - 1; x++) {
            if (x <= 1) {
                String finalName;
                if (groups.get(x).getUserId() == PreferenceManager.getID(this)) {
                    finalName = getString(R.string.you);
                } else {
                    String phone = UtilsPhone.getContactName(this, groups.get(x).getPhone());
                    if (phone != null) {
                        try {
                            finalName = phone.substring(0, 5);
                        } catch (Exception e) {
                            AppHelper.LogCat(e);
                            finalName = phone;
                        }
                    } else {
                        finalName = groups.get(x).getPhone().substring(0, 5);
                    }

                }
                names.append(finalName);
                names.append(",");
            }

        }
        String groupsNames = UtilsString.removelastString(names.toString());
        statusUser.setVisibility(View.VISIBLE);
        statusUser.setText(groupsNames);
        AnimationsUtil.slideStatus(statusUser);

        if (!realm.isClosed()) realm.close();
    }

    public void updateContactRecipient(ContactsModel contactsModels) {
        mUsersModelRecipient = contactsModels;
        refreshMenu();
        try {

            if (UtilsPhone.checkIfContactExist(this, contactsModels.getPhone())) {
                AddContactBtn.setVisibility(View.GONE);
                blockLayout.setVisibility(View.GONE);
            } else {
                AddContactBtn.setVisibility(View.VISIBLE);
                blockLayout.setVisibility(View.VISIBLE);
                if (checkIfUserBlockedExist(recipientId, realm)) {
                    UnBlockContactBtn.setVisibility(View.VISIBLE);
                    BlockContactBtn.setVisibility(View.GONE);
                } else {
                    UnBlockContactBtn.setVisibility(View.GONE);
                    BlockContactBtn.setVisibility(View.VISIBLE);
                }
            }

            String name = UtilsPhone.getContactName(this, contactsModels.getPhone());
            if (name != null) {
                ToolbarTitle.setText(name);
            } else {
                ToolbarTitle.setText(contactsModels.getPhone());
            }

        } catch (Exception e) {
            AppHelper.LogCat(" Recipient username  is null MessagesPopupActivity" + e.getMessage());
        }

        Bitmap bitmap = ImageLoader.GetCachedBitmapImage(memoryCache, contactsModels.getImage(), this, recipientId, AppConstants.USER, AppConstants.ROW_PROFILE);
        if (bitmap != null) {
            ImageLoader.SetBitmapImage(bitmap, ToolbarImage);
        } else {
            DostChatImageLoader.loadCircleImage(this, EndPoints.ROWS_IMAGE_URL + contactsModels.getImage(), ToolbarImage, R.drawable.image_holder_ur_circle, AppConstants.ROWS_IMAGE_SIZE);
        }
        if (contactsModels.getUserState() != null) {
            if (contactsModels.getUserState().equals(getString(R.string.isOnline)))
                updateUserStatus(AppConstants.STATUS_USER_CONNECTED, null);
            else if (contactsModels.getUserState().equals(getString(R.string.isOffline)))
                updateUserStatus(AppConstants.STATUS_USER_DISCONNECTED, null);
            else if (contactsModels.getUserState().equals(getString(R.string.lastSeen)))
                updateUserStatus(AppConstants.STATUS_USER_LAST_SEEN, null);
        }
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onBackPressed() {
        if (isOpen) {
            isOpen = false;
            animateItems(false);
        } else if (emoticonShown) {
            emoticonShown = false;
            emojIcon.closeEmojIcon();
            SendMessageLayout.setBackground(getResources().getDrawable(android.R.color.transparent));
        } else {
            super.onBackPressed();
            mMessagesAdapter.stopAudio();
            if (NotificationsManager.getManager()) {
                if (isGroup)
                    NotificationsManager.cancelNotification(groupID);
                else
                    NotificationsManager.cancelNotification(recipientId);
            }
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        }


    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!isGroup)
            LastSeenTimeEmit();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
        mMessagesPresenter.onDestroy();
        if (emojIcon != null) {
            emojIcon.closeEmojIcon();
            emojIcon = null;
        }

    }

    @Override
    public void onShowLoading() {

    }

    @Override
    public void onHideLoading() {

    }

    @Override
    public void onErrorLoading(Throwable throwable) {
        AppHelper.LogCat("Messages " + throwable.getMessage());
    }


    /**
     * method to connect to the chat sever by socket
     */
    private void connectToChatServer() {

        DostChatApp app = (DostChatApp) getApplication();
        mSocket = app.getSocket();

        if (mSocket == null) {
            DostChatApp.connectSocket();
            mSocket = app.getSocket();
        }
        if (!mSocket.connected())
            mSocket.connect();
        emitUserIsOnline();

        setTypingEvent();
        if (isGroup) {
            //  AppHelper.LogCat("here group seen");
        } else {
            if (!checkIfUserBlockedExist(recipientId, realm)) {
                checkIfUserIsOnline();
                emitMessageSeen();
            }
        }


    }

    private void emitUserIsOnline() {

        if (!checkIfUserBlockedExist(recipientId, realm)) {

            JSONObject json = new JSONObject();
            try {
                json.put("connected", true);
                json.put("senderId", senderId);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            mSocket.emit(AppConstants.SOCKET_IS_ONLINE, json);
        }
    }


    /**
     * method to check if user is online or not
     */
    private void checkIfUserIsOnline() {
        mSocket.on(AppConstants.SOCKET_IS_ONLINE, args -> runOnUiThread(() -> {
            final JSONObject data = (JSONObject) args[0];
            try {
                int senderID = data.getInt("senderId");
                if (senderID == recipientId) {
                    if (data.getBoolean("connected")) {
                        updateUserStatus(AppConstants.STATUS_USER_CONNECTED, null);
                    } else {
                        updateUserStatus(AppConstants.STATUS_USER_DISCONNECTED, null);
                    }
                }
            } catch (JSONException e) {
                AppHelper.LogCat(e);
            }

        }));
    }

    /**
     * method to set user typing event
     */
    private void setTypingEvent() {
        Realm realm = DostChatApp.getRealmDatabaseInstance();

        if (isGroup) {
            mSocket.on(AppConstants.SOCKET_IS_MEMBER_TYPING, args -> runOnUiThread(() -> {

                JSONObject data = (JSONObject) args[0];
                try {

                    int senderID = data.getInt("senderId");

                    if (!checkIfUserBlockedExist(senderID, realm)) {
                        ContactsModel contactsModel = realm.where(ContactsModel.class).equalTo("id", senderID).findFirst();
                        String finalName;
                        String name = UtilsPhone.getContactName(this, contactsModel.getPhone());
                        if (name != null) {
                            finalName = name;
                        } else {
                            finalName = contactsModel.getPhone();
                        }
                        int groupId = data.getInt("groupId");
                        if (groupId == groupID) {
                            if (senderID == PreferenceManager.getID(this)) return;
                            updateGroupMemberStatus(AppConstants.STATUS_USER_TYPING, finalName);
                        }
                    }

                } catch (Exception e) {
                    AppHelper.LogCat(e);
                }
            }));

            mSocket.on(AppConstants.SOCKET_IS_MEMBER_STOP_TYPING, args -> runOnUiThread(() -> {
                updateGroupMemberStatus(AppConstants.STATUS_USER_STOP_TYPING, null);

            }));
        } else {
            if (!checkIfUserBlockedExist(recipientId, realm)) {
                mSocket.on(AppConstants.SOCKET_IS_TYPING, args -> runOnUiThread(() -> {
                    AppHelper.LogCat("SOCKET_IS_TYPING ");
                    JSONObject data = (JSONObject) args[0];
                    try {

                        int senderID = data.getInt("senderId");
                        int recipientID = data.getInt("recipientId");
                        if (senderID == recipientId && recipientID == senderId) {
                            updateUserStatus(AppConstants.STATUS_USER_TYPING, null);
                        }

                    } catch (Exception e) {
                        AppHelper.LogCat(e);
                    }
                }));

                mSocket.on(AppConstants.SOCKET_IS_STOP_TYPING, args -> runOnUiThread(() -> {
                    AppHelper.LogCat("SOCKET_IS_STOP_TYPING ");
                    JSONObject data = (JSONObject) args[0];
                    try {
                        int senderID = data.getInt("senderId");
                        int recipientID = data.getInt("recipientId");
                        if (senderID == recipientId && recipientID == senderId) {
                            updateUserStatus(AppConstants.STATUS_USER_STOP_TYPING, null);
                        }
                    } catch (Exception e) {
                        AppHelper.LogCat(e);
                    }
                }));
                mSocket.on(AppConstants.SOCKET_IS_LAST_SEEN, args -> runOnUiThread(() -> {
                    JSONObject data = (JSONObject) args[0];
                    try {
                        int senderID = data.getInt("senderId");
                        int recipientID = data.getInt("recipientId");
                        String lastSeen = data.getString("lastSeen");
                        if (senderID == recipientId && recipientID == senderId) {
                            DateTime messageDate = UtilsTime.getCorrectDate(lastSeen);
                            String finalDate = UtilsTime.convertDateToString(this, messageDate);
                            updateUserStatus(AppConstants.STATUS_USER_LAST_SEEN, finalDate);
                        }
                    } catch (Exception e) {
                        AppHelper.LogCat(e);
                    }
                }));
            }
        }
        if (!realm.isClosed())
            realm.close();

    }

    /**
     * method to emit last seen of conversation
     */
    private void LastSeenTimeEmit() {
        DateTime current = new DateTime();
        String lastTime = String.valueOf(current);
        JSONObject data = new JSONObject();
        try {
            data.put("senderId", PreferenceManager.getID(this));
            data.put("recipientId", recipientId);
            data.put("lastSeen", lastTime);
        } catch (JSONException e) {
            AppHelper.LogCat(e);
        }
        mSocket.emit(AppConstants.SOCKET_IS_LAST_SEEN, data);
    }

    /**
     * method to update  group members  to show them on toolbar status
     *
     * @param statusUserTyping this is the first parameter for  updateGroupMemberStatus method
     * @param memberName       this is the second parameter for updateGroupMemberStatus method
     */
    private void updateGroupMemberStatus(int statusUserTyping, String memberName) {
        StringBuilder names = new StringBuilder();
        Realm realm = DostChatApp.getRealmDatabaseInstance();
        List<MembersGroupModel> groups = realm.where(MembersGroupModel.class).equalTo("groupID", groupID).equalTo("Deleted", false).findAll();

        int arraySize = groups.size();
        if (arraySize != 0) {
            for (int x = 0; x < arraySize; x++) {
                if (x <= 1) {
                    String finalName;
                    if (groups.get(x).getUserId() == PreferenceManager.getID(this)) {
                        finalName = "You";

                    } else {
                        String phone = UtilsPhone.getContactName(this, groups.get(x).getPhone());
                        if (phone != null) {
                            try {
                                finalName = phone.substring(0, 7);
                            } catch (Exception e) {
                                AppHelper.LogCat(e);
                                finalName = phone;
                            }

                        } else {
                            finalName = groups.get(x).getPhone().substring(0, 7);
                        }

                    }
                    names.append(finalName);
                    names.append(",");

                }

            }
        } else {
            names.append("");
        }

        String groupsNames = UtilsString.removelastString(names.toString());
        switch (statusUserTyping) {
            case AppConstants.STATUS_USER_TYPING:
                statusUser.setVisibility(View.VISIBLE);
                statusUser.setText(memberName + " " + getString(R.string.isTyping));
                break;
            case AppConstants.STATUS_USER_STOP_TYPING:
                statusUser.setVisibility(View.VISIBLE);
                statusUser.setText(groupsNames);
                break;
            default:
                statusUser.setVisibility(View.VISIBLE);
                statusUser.setText(groupsNames);
                break;
        }

        if (!realm.isClosed()) realm.close();
    }

    private void showStatus() {
        TransitionManager.beginDelayedTransition(mView);
        statusUser.setVisibility(View.VISIBLE);
    }

    private void hideStatus() {
        TransitionManager.beginDelayedTransition(mView);
        statusUser.setVisibility(View.GONE);
    }

    /**
     * method to update user status
     *
     * @param statusUserTyping this is the first parameter for  updateUserStatus method
     * @param lastTime         this is the second parameter for  updateUserStatus method
     */
    private void updateUserStatus(int statusUserTyping, String lastTime) {
        Realm realm = DostChatApp.getRealmDatabaseInstance();
        if (!checkIfUserBlockedExist(recipientId, realm)) {
            if (mSocket != null && !mSocket.connected())
                mSocket.connect();

            switch (statusUserTyping) {
                case AppConstants.STATUS_USER_TYPING:
                    showStatus();
                    statusUser.setText(getString(R.string.isTyping));
                    AppHelper.LogCat("typing...");
                    break;
                case AppConstants.STATUS_USER_DISCONNECTED:
                    showStatus();
                    realm.executeTransaction(realm1 -> {
                        ContactsModel contactsModel = realm1.where(ContactsModel.class).equalTo("id", recipientId).findFirst();
                        contactsModel.setUserState(getString(R.string.isOffline));
                        realm.copyToRealmOrUpdate(contactsModel);

                    });
                    statusUser.setText(getString(R.string.isOffline));
                    AppHelper.LogCat("Offline...");
                    break;
                case AppConstants.STATUS_USER_CONNECTED:
                    showStatus();
                    realm.executeTransaction(realm1 -> {
                        ContactsModel contactsModel = realm1.where(ContactsModel.class).equalTo("id", recipientId).findFirst();
                        contactsModel.setUserState(getString(R.string.isOnline));
                        realm.copyToRealmOrUpdate(contactsModel);

                    });
                    statusUser.setText(getString(R.string.isOnline));
                    AnimationsUtil.slideStatus(statusUser);
                    AppHelper.LogCat("Online...");
                    break;
                case AppConstants.STATUS_USER_STOP_TYPING:
                    showStatus();
                    realm.executeTransaction(realm1 -> {
                        ContactsModel contactsModel = realm1.where(ContactsModel.class).equalTo("id", recipientId).findFirst();
                        contactsModel.setUserState(getString(R.string.isOnline));
                        realm.copyToRealmOrUpdate(contactsModel);

                    });
                    statusUser.setText(getString(R.string.isOnline));
                    break;
                case AppConstants.STATUS_USER_LAST_SEEN:
                    showStatus();
                    realm.executeTransaction(realm1 -> {
                        ContactsModel contactsModel = realm1.where(ContactsModel.class).equalTo("id", recipientId).findFirst();
                        contactsModel.setUserState(getString(R.string.lastSeen) + " " + lastTime);
                        realm.copyToRealmOrUpdate(contactsModel);

                    });
                    statusUser.setText(getString(R.string.lastSeen) + " " + lastTime);
                    AnimationsUtil.slideStatus(statusUser);
                    break;


            }
        }
        if (!realm.isClosed())
            realm.close();

    }


    /**
     * method of EventBus
     *
     * @param pusher this is parameter of onEventMainThread method
     */
    @SuppressWarnings("unused")
    public void onEventMainThread(Pusher pusher) {
        switch (pusher.getAction()) {
            case AppConstants.EVENT_BUS_NEW_MESSAGE_MESSAGES_NEW_ROW:
                MessagesModel messagesModel = pusher.getMessagesModel();
                if (messagesModel.getSenderID() == recipientId && messagesModel.getRecipientID() == senderId) {
                    AppHelper.playSound(this, "audio/incoming_message.wav");
                    addMessage(messagesModel);
                    new Handler().postDelayed(() -> mMessagesPresenter.updateConversationStatus(), 500);
                }
                break;
            case AppConstants.EVENT_BUS_NEW_GROUP_MESSAGE_MESSAGES_NEW_ROW:
                if (isGroup) {
                    MessagesModel messagesModel1 = pusher.getMessagesModel();
                    if (messagesModel1.getSenderID() != PreferenceManager.getID(this)) {
                        addMessage(messagesModel1);
                        new Handler().postDelayed(() -> mMessagesPresenter.updateGroupConversationStatus(), 500);
                    }
                }
                break;


            case AppConstants.EVENT_BUS_MESSAGE_IS_DELIVERED_FOR_MESSAGES:
            case AppConstants.EVENT_BUS_MESSAGE_IS_SENT_FOR_MESSAGES:
            case AppConstants.EVENT_BUS_MESSAGE_IS_SEEN_FOR_MESSAGES:
                new Handler().postDelayed(() -> mMessagesAdapter.updateStatusMessageItem(pusher.getMessageId()), 500);
                break;
            case AppConstants.EVENT_BUS_UPLOAD_MESSAGE_FILES:
                if (pusher.getMessagesModel().isGroup())
                    unSentMessagesGroup(pusher.getMessagesModel().getGroupID());
                else
                    unSentMessagesForARecipient(pusher.getMessagesModel().getRecipientID(), true);
                break;
            case AppConstants.EVENT_BUS_UPDATE_USER_STATE:
                if (pusher.getData().equals(getString(R.string.isOnline)))
                    updateUserStatus(AppConstants.STATUS_USER_CONNECTED, null);
                else if (pusher.getData().equals(getString(R.string.isOffline)))
                    updateUserStatus(AppConstants.STATUS_USER_DISCONNECTED, null);
                break;
            case AppConstants.EVENT_BUS_ITEM_IS_ACTIVATED_MESSAGES:
                int idx = messagesList.getChildAdapterPosition(pusher.getView());
                if (actionMode != null) {
                    ToggleSelection(idx);
                    return;
                }
                break;

            case AppConstants.EVENT_BUS_NEW_USER_NOTIFICATION:
                NotificationsModel newUserNotification = pusher.getNotificationsModel();
                if (newUserNotification.getRecipientId() == recipientId) {
                    return;
                } else {

                    if (newUserNotification.getAppName() != null && newUserNotification.getAppName().equals(getApplicationContext().getPackageName())) {

                        /**
                         * this for default activity
                         */
                        Intent messagingIntent = new Intent(getApplicationContext(), MessagesActivity.class);
                        messagingIntent.putExtra("conversationID", newUserNotification.getConversationID());
                        messagingIntent.putExtra("recipientID", newUserNotification.getRecipientId());
                        messagingIntent.putExtra("isGroup", newUserNotification.isGroup());
                        messagingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        /**
                         * this for popup activity
                         */
                        Intent messagingPopupIntent = new Intent(getApplicationContext(), MessagesPopupActivity.class);
                        messagingPopupIntent.putExtra("conversationID", newUserNotification.getConversationID());
                        messagingPopupIntent.putExtra("recipientID", newUserNotification.getRecipientId());
                        messagingPopupIntent.putExtra("isGroup", newUserNotification.isGroup());
                        messagingPopupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        if (newUserNotification.getFile() != null) {
                            NotificationsManager.showUserNotification(getApplicationContext(), messagingIntent, messagingPopupIntent, newUserNotification.getPhone(), newUserNotification.getFile(), recipientId, newUserNotification.getImage());
                        } else {
                            NotificationsManager.showUserNotification(getApplicationContext(), messagingIntent, messagingPopupIntent, newUserNotification.getPhone(), newUserNotification.getMessage(), recipientId, newUserNotification.getImage());
                        }
                    }

                }

                break;
            case AppConstants.EVENT_BUS_NEW_GROUP_NOTIFICATION:
                NotificationsModel newGroupNotification = pusher.getNotificationsModel();
                if (newGroupNotification.getGroupID() == groupID) {
                    return;
                } else {
                    if (newGroupNotification.getAppName() != null && newGroupNotification.getAppName().equals(getApplicationContext().getPackageName())) {

                        /**
                         * this for default activity
                         */
                        Intent messagingGroupIntent = new Intent(getApplicationContext(), MessagesActivity.class);
                        messagingGroupIntent.putExtra("conversationID", newGroupNotification.getConversationID());
                        messagingGroupIntent.putExtra("groupID", newGroupNotification.getGroupID());
                        messagingGroupIntent.putExtra("isGroup", newGroupNotification.isGroup());
                        messagingGroupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        /**
                         * this for popup activity
                         */
                        Intent messagingGroupPopupIntent = new Intent(getApplicationContext(), MessagesPopupActivity.class);
                        messagingGroupPopupIntent.putExtra("conversationID", newGroupNotification.getConversationID());
                        messagingGroupPopupIntent.putExtra("groupID", newGroupNotification.getGroupID());
                        messagingGroupPopupIntent.putExtra("isGroup", newGroupNotification.isGroup());
                        messagingGroupPopupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                        String message;
                        String userName = UtilsPhone.getContactName(getApplicationContext(), newGroupNotification.getPhone());
                        switch (newGroupNotification.getMessage()) {
                            case AppConstants.CREATE_GROUP:
                                if (userName != null) {
                                    message = "" + userName + getApplicationContext().getString(R.string.he_created_this_group);
                                } else {
                                    message = "" + newGroupNotification.getPhone() + getApplicationContext().getString(R.string.he_created_this_group);
                                }


                                break;
                            case AppConstants.LEFT_GROUP:
                                if (userName != null) {
                                    message = "" + userName + getApplicationContext().getString(R.string.he_left);
                                } else {
                                    message = "" + newGroupNotification.getPhone() + getApplicationContext().getString(R.string.he_left);
                                }

                                break;
                            default:
                                message = newGroupNotification.getMessage();
                                break;
                        }
                        if (newGroupNotification.getFile() != null) {
                            NotificationsManager.showGroupNotification(getApplicationContext(), messagingGroupIntent, messagingGroupPopupIntent, newGroupNotification.getGroupName(), newGroupNotification.getMemberName() + " : " + newGroupNotification.getFile(), newGroupNotification.getGroupID(), newGroupNotification.getImage());
                        } else {
                            NotificationsManager.showGroupNotification(getApplicationContext(), messagingGroupIntent, messagingGroupPopupIntent, newGroupNotification.getGroupName(), newGroupNotification.getMemberName() + " : " + message, newGroupNotification.getGroupID(), newGroupNotification.getImage());
                        }
                    }
                }

                break;

        }
    }


    /**
     * method to add a new message to list messages
     *
     * @param newMsg this is the parameter for addMessage
     */

    private void addMessage(MessagesModel newMsg) {

        mMessagesAdapter.addMessage(newMsg);
        scrollToBottom();
    }

    /**
     * method to scroll to the bottom of list
     */
    private void scrollToBottom() {
        messagesList.scrollToPosition(mMessagesAdapter.getItemCount() - 1);
    }

    /**
     * method to set teh draging animation for audio layout
     *
     * @param motionEvent this is the first parameter for setDraggingAnimation  method
     * @param view        this the second parameter for  setDraggingAnimation  method
     * @return this is what method will return
     */
    private boolean setDraggingAnimation(MotionEvent motionEvent, View view) {

        sendMessagePanel.setVisibility(View.GONE);
        recordPanel.setVisibility(View.VISIBLE);
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) slideTextContainer
                    .getLayoutParams();
            params.leftMargin = convertToDp(30);
            slideTextContainer.setLayoutParams(params);
            ViewAudioProxy.setAlpha(slideTextContainer, 1);
            startedDraggingX = -1;
            mStartTime = System.currentTimeMillis();
            startRecording();
            SendRecordButton.getParent().requestDisallowInterceptTouchEvent(true);
            recordPanel.setVisibility(View.VISIBLE);
        } else if (motionEvent.getAction() == MotionEvent.ACTION_UP || motionEvent.getAction() == MotionEvent.ACTION_CANCEL) {
            startedDraggingX = -1;
            recordPanel.setVisibility(View.GONE);
            sendMessagePanel.setVisibility(View.VISIBLE);

            long intervalTime = System.currentTimeMillis() - mStartTime;
            if (intervalTime < MIN_INTERVAL_TIME) {

                messageWrapper.setError(getString(R.string.hold_to_record));
                try {
                    if (FilesManager.isFileRecordExists(FileAudioPath)) {
                        boolean deleted = FilesManager.getFileRecord(FileAudioPath).delete();
                        if (deleted)
                            FileAudioPath = null;
                    }
                } catch (Exception e) {
                    AppHelper.LogCat("Exception record path file  MessagesPopupActivity");
                }
            } else {

                sendMessage();
                FileAudioPath = null;

            }
            stopRecording();
        } else if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
            float x = motionEvent.getX();
            if (x < -distCanMove) {
                AppHelper.LogCat("here we will delete  the file ");
                try {
                    if (FilesManager.isFileRecordExists(FileAudioPath)) {
                        boolean deleted = FilesManager.getFileRecord(FileAudioPath).delete();
                        if (deleted)
                            FileAudioPath = null;
                    }


                } catch (Exception e) {
                    AppHelper.LogCat("Exception exist record  " + e.getMessage());
                }
                FileAudioPath = null;
                stopRecording();
            }
            x = x + ViewAudioProxy.getX(SendRecordButton);
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) slideTextContainer
                    .getLayoutParams();
            if (startedDraggingX != -1) {
                float dist = (x - startedDraggingX);
                params.leftMargin = convertToDp(30) + (int) dist;
                slideTextContainer.setLayoutParams(params);
                float alpha = 1.0f + dist / distCanMove;
                if (alpha > 1) {
                    alpha = 1;
                } else if (alpha < 0) {
                    alpha = 0;
                }
                ViewAudioProxy.setAlpha(slideTextContainer, alpha);
            }
            if (x <= ViewAudioProxy.getX(slideTextContainer) + slideTextContainer.getWidth()
                    + convertToDp(30)) {
                if (startedDraggingX == -1) {
                    startedDraggingX = x;
                    distCanMove = (recordPanel.getMeasuredWidth()
                            - slideTextContainer.getMeasuredWidth() - convertToDp(48)) / 2.0f;
                    if (distCanMove <= 0) {
                        distCanMove = convertToDp(80);
                    } else if (distCanMove > convertToDp(80)) {
                        distCanMove = convertToDp(80);
                    }
                }
            }
            if (params.leftMargin > convertToDp(30)) {
                params.leftMargin = convertToDp(30);
                slideTextContainer.setLayoutParams(params);
                ViewAudioProxy.setAlpha(slideTextContainer, 1);
                startedDraggingX = -1;
            }
        }

        view.onTouchEvent(motionEvent);
        return true;
    }

    /**
     * method to start recording audio
     */
    private void startRecording() {

        if (PermissionHandler.checkPermission(this, Manifest.permission.RECORD_AUDIO)) {
            AppHelper.LogCat("Record audio permission already granted.");
        } else {
            AppHelper.LogCat("Please request Record audio permission.");
            PermissionHandler.requestPermission(this, Manifest.permission.RECORD_AUDIO);
        }

        if (PermissionHandler.checkPermission(this, Manifest.permission.MODIFY_AUDIO_SETTINGS)) {
            AppHelper.LogCat("Record audio permission already granted.");
        } else {
            AppHelper.LogCat("Please request Record audio permission.");
            PermissionHandler.requestPermission(this, Manifest.permission.MODIFY_AUDIO_SETTINGS);
        }


        if (PermissionHandler.checkPermission(this, Manifest.permission.VIBRATE)) {
            AppHelper.LogCat("Vibrate permission already granted.");
        } else {
            AppHelper.LogCat("Please request Vibrate permission.");
            PermissionHandler.requestPermission(this, Manifest.permission.VIBRATE);
        }
        try {
            startRecordingAudio();
            startTime = SystemClock.uptimeMillis();
            recordTimer = new Timer();
            UpdaterTimerTask updaterTimerTask = new UpdaterTimerTask();
            recordTimer.schedule(updaterTimerTask, 1000, 1000);
            vibrate();
        } catch (Exception e) {
            AppHelper.LogCat("IOException start audio " + e.getMessage());
        }


    }


    /**
     * method to stop recording auido
     */
    @SuppressLint("SetTextI18n")
    private void stopRecording() {
        if (recordTimer != null) {
            recordTimer.cancel();
        }
        if (recordTimeText.getText().toString().equals("00:00")) {
            return;
        }
        recordTimeText.setText("00:00");
        vibrate();
        recordPanel.setVisibility(View.GONE);
        sendMessagePanel.setVisibility(View.VISIBLE);
        stopRecordingAudio();


    }

    /**
     * method to initialize the audio for start recording
     *
     * @throws IOException
     */
    @SuppressLint("SetTextI18n")
    private void startRecordingAudio() throws IOException {
        stopRecordingAudio();
        FileAudioPath = FilesManager.getFileRecordPath(this);
        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mMediaRecorder.setOutputFile(FileAudioPath);
        mMediaRecorder.setOnErrorListener(errorListener);
        mMediaRecorder.setOnInfoListener(infoListener);
        mMediaRecorder.prepare();
        mMediaRecorder.start();

    }

    /**
     * method to reset and clear media recorder
     */
    private void stopRecordingAudio() {
        try {
            if (mMediaRecorder != null) {
                mMediaRecorder.stop();
                mMediaRecorder.reset();
                mMediaRecorder.release();
                mMediaRecorder = null;
                FileAudioPath = null;
            }
        } catch (Exception e) {
            AppHelper.LogCat("Exception stop recording " + e.getMessage());
        }

    }

    private MediaRecorder.OnErrorListener errorListener = (mr, what, extra) -> AppHelper.LogCat("Error: " + what + ", " + extra);

    private MediaRecorder.OnInfoListener infoListener = (mr, what, extra) -> AppHelper.LogCat("Warning: " + what + ", " + extra);

    /**
     * method to make device vibrate when user start recording
     */
    private void vibrate() {
        try {
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(200);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private int convertToDp(float value) {
        return (int) Math.ceil(1 * value);
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        gestureDetector.onTouchEvent(e);
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {

    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }

    /**
     * method to toggle the selection
     *
     * @param position this is parameter for  ToggleSelection method
     */
    private void ToggleSelection(int position) {
        mMessagesAdapter.toggleSelection(position);
        String title = String.format("%s selected", mMessagesAdapter.getSelectedItemCount());
        actionMode.setTitle(title);
    }

    @Override
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
        MenuInflater inflater = actionMode.getMenuInflater();
       /* if (isGroup)
            inflater.inflate(R.menu.select_share_messages_group_menu, menu);
        else*/
        inflater.inflate(R.menu.select_share_messages_menu, menu);

        getSupportActionBar().hide();
        if (AppHelper.isAndroid5()) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(AppHelper.getColor(this, R.color.colorAccent));
        }
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {

        int arraySize = mMessagesAdapter.getSelectedItems().size();
        int currentPosition;
        switch (menuItem.getItemId()) {
            case R.id.share_content:
                if (arraySize != 0 && arraySize == 1) {
                    for (int x = 0; x < arraySize; x++) {
                        currentPosition = mMessagesAdapter.getSelectedItems().get(x);
                        MessagesModel messagesModel = mMessagesAdapter.getItem(currentPosition);
                        if (messagesModel.getMessage() != null) {
                            if (messagesModel.getSenderID() == PreferenceManager.getID(this)) {
                                if (messagesModel.getVideoFile() != null && !messagesModel.getVideoFile().equals("null")) {
                                    if (FilesManager.isFileVideosSentExists(this, FilesManager.getVideo(messagesModel.getVideoFile()))) {
                                        File file = FilesManager.getFileVideoSent(this, messagesModel.getVideoFile());
                                        AppHelper.shareIntent(file, this, messagesModel.getMessage(), AppConstants.SENT_VIDEOS);
                                    } else {
                                        AppHelper.CustomToast(MessagesActivity.this, getString(R.string.this_video_is_not_exist));
                                    }
                                } else if (messagesModel.getAudioFile() != null && !messagesModel.getAudioFile().equals("null")) {
                                    if (FilesManager.isFileAudiosSentExists(this, FilesManager.getAudio(messagesModel.getAudioFile()))) {
                                        File file = FilesManager.getFileAudioSent(this, messagesModel.getAudioFile());
                                        AppHelper.shareIntent(file, this, messagesModel.getMessage(), AppConstants.SENT_AUDIO);
                                    } else {
                                        AppHelper.CustomToast(MessagesActivity.this, getString(R.string.this_audio_is_not_exist));
                                    }
                                } else if (messagesModel.getImageFile() != null && !messagesModel.getImageFile().equals("null")) {
                                    if (FilesManager.isFileImagesSentExists(this, FilesManager.getImage(messagesModel.getImageFile()))) {
                                        File file = FilesManager.getFileImageSent(this, messagesModel.getImageFile());
                                        AppHelper.shareIntent(file, this, messagesModel.getMessage(), AppConstants.SENT_IMAGES);
                                    } else {
                                        AppHelper.CustomToast(MessagesActivity.this, getString(R.string.this_image_is_not_exist));
                                    }
                                } else if (messagesModel.getDocumentFile() != null && !messagesModel.getDocumentFile().equals("null")) {
                                    if (FilesManager.isFileDocumentsSentExists(this, FilesManager.getDocument(messagesModel.getDocumentFile()))) {
                                        File file = FilesManager.getFileDocumentSent(this, messagesModel.getDocumentFile());
                                        AppHelper.shareIntent(file, this, messagesModel.getMessage(), AppConstants.SENT_DOCUMENTS);
                                    } else {
                                        AppHelper.CustomToast(MessagesActivity.this, getString(R.string.this_document_is_not_exist));
                                    }
                                } else {
                                    AppHelper.shareIntent(null, this, messagesModel.getMessage(), AppConstants.SENT_TEXT);
                                }
                            } else {
                                if (messagesModel.getVideoFile() != null && !messagesModel.getVideoFile().equals("null")) {
                                    if (FilesManager.isFileVideosExists(this, FilesManager.getVideo(messagesModel.getVideoFile()))) {
                                        File file = FilesManager.getFileVideo(this, messagesModel.getVideoFile());
                                        AppHelper.shareIntent(file, this, messagesModel.getMessage(), AppConstants.SENT_VIDEOS);
                                    } else {
                                        AppHelper.CustomToast(MessagesActivity.this, getString(R.string.this_video_is_not_exist));
                                    }
                                } else if (messagesModel.getAudioFile() != null && !messagesModel.getAudioFile().equals("null")) {
                                    if (FilesManager.isFileAudioExists(this, FilesManager.getAudio(messagesModel.getAudioFile()))) {
                                        File file = FilesManager.getFileAudio(this, messagesModel.getAudioFile());
                                        AppHelper.shareIntent(file, this, messagesModel.getMessage(), AppConstants.SENT_AUDIO);
                                    } else {
                                        AppHelper.CustomToast(MessagesActivity.this, getString(R.string.this_audio_is_not_exist));
                                    }
                                } else if (messagesModel.getImageFile() != null && !messagesModel.getImageFile().equals("null")) {
                                    if (FilesManager.isFileImagesExists(this, FilesManager.getImage(messagesModel.getImageFile()))) {
                                        File file = FilesManager.getFileImage(this, messagesModel.getImageFile());
                                        AppHelper.shareIntent(file, this, messagesModel.getMessage(), AppConstants.SENT_IMAGES);
                                    } else {
                                        AppHelper.CustomToast(MessagesActivity.this, getString(R.string.this_image_is_not_exist));
                                    }
                                } else if (messagesModel.getDocumentFile() != null && !messagesModel.getDocumentFile().equals("null")) {
                                    if (FilesManager.isFileDocumentsExists(this, FilesManager.getDocument(messagesModel.getDocumentFile()))) {
                                        File file = FilesManager.getFileDocument(this, messagesModel.getDocumentFile());
                                        AppHelper.shareIntent(file, this, messagesModel.getMessage(), AppConstants.SENT_DOCUMENTS);
                                    } else {
                                        AppHelper.CustomToast(MessagesActivity.this, getString(R.string.this_document_is_not_exist));
                                    }
                                } else {
                                    AppHelper.shareIntent(null, this, messagesModel.getMessage(), AppConstants.SENT_TEXT);
                                }
                            }

                        } else {
                            if (messagesModel.getSenderID() == PreferenceManager.getID(this)) {
                                if (messagesModel.getVideoFile() != null && !messagesModel.getVideoFile().equals("null")) {
                                    if (FilesManager.isFileVideosSentExists(this, FilesManager.getVideo(messagesModel.getVideoFile()))) {
                                        File file = FilesManager.getFileVideoSent(this, messagesModel.getVideoFile());
                                        AppHelper.shareIntent(file, this, null, AppConstants.SENT_VIDEOS);
                                    } else {
                                        AppHelper.CustomToast(MessagesActivity.this, getString(R.string.this_video_is_not_exist));
                                    }
                                } else if (messagesModel.getAudioFile() != null && !messagesModel.getAudioFile().equals("null")) {
                                    if (FilesManager.isFileAudiosSentExists(this, FilesManager.getAudio(messagesModel.getAudioFile()))) {
                                        File file = FilesManager.getFileAudioSent(this, messagesModel.getAudioFile());
                                        AppHelper.shareIntent(file, this, null, AppConstants.SENT_AUDIO);
                                    } else {
                                        AppHelper.CustomToast(MessagesActivity.this, getString(R.string.this_audio_is_not_exist));
                                    }
                                } else if (messagesModel.getImageFile() != null && !messagesModel.getImageFile().equals("null")) {
                                    if (FilesManager.isFileImagesSentExists(this, FilesManager.getImage(messagesModel.getImageFile()))) {
                                        File file = FilesManager.getFileImageSent(this, messagesModel.getImageFile());
                                        AppHelper.shareIntent(file, this, null, AppConstants.SENT_IMAGES);
                                    } else {
                                        AppHelper.CustomToast(MessagesActivity.this, getString(R.string.this_image_is_not_exist));
                                    }
                                } else if (messagesModel.getDocumentFile() != null && !messagesModel.getDocumentFile().equals("null")) {
                                    if (FilesManager.isFileDocumentsSentExists(this, FilesManager.getDocument(messagesModel.getDocumentFile()))) {
                                        File file = FilesManager.getFileDocumentSent(this, messagesModel.getDocumentFile());
                                        AppHelper.shareIntent(file, this, null, AppConstants.SENT_DOCUMENTS);
                                    } else {
                                        AppHelper.CustomToast(MessagesActivity.this, getString(R.string.this_document_is_not_exist));
                                    }
                                }
                            } else {
                                if (messagesModel.getVideoFile() != null && !messagesModel.getVideoFile().equals("null")) {
                                    if (FilesManager.isFileVideosExists(this, FilesManager.getVideo(messagesModel.getVideoFile()))) {
                                        File file = FilesManager.getFileVideo(this, messagesModel.getVideoFile());
                                        AppHelper.shareIntent(file, this, null, AppConstants.SENT_VIDEOS);
                                    } else {
                                        AppHelper.CustomToast(MessagesActivity.this, getString(R.string.this_video_is_not_exist));
                                    }
                                } else if (messagesModel.getAudioFile() != null && !messagesModel.getAudioFile().equals("null")) {
                                    if (FilesManager.isFileAudioExists(this, FilesManager.getAudio(messagesModel.getAudioFile()))) {
                                        File file = FilesManager.getFileAudio(this, messagesModel.getAudioFile());
                                        AppHelper.shareIntent(file, this, null, AppConstants.SENT_AUDIO);
                                    } else {
                                        AppHelper.CustomToast(MessagesActivity.this, getString(R.string.this_audio_is_not_exist));
                                    }
                                } else if (messagesModel.getImageFile() != null && !messagesModel.getImageFile().equals("null")) {
                                    if (FilesManager.isFileImagesExists(this, FilesManager.getImage(messagesModel.getImageFile()))) {
                                        File file = FilesManager.getFileImage(this, messagesModel.getImageFile());
                                        AppHelper.shareIntent(file, this, null, AppConstants.SENT_IMAGES);
                                    } else {
                                        AppHelper.CustomToast(MessagesActivity.this, getString(R.string.this_image_is_not_exist));
                                    }
                                } else if (messagesModel.getDocumentFile() != null && !messagesModel.getDocumentFile().equals("null")) {
                                    if (FilesManager.isFileDocumentsExists(this, FilesManager.getDocument(messagesModel.getDocumentFile()))) {
                                        File file = FilesManager.getFileDocument(this, messagesModel.getDocumentFile());
                                        AppHelper.shareIntent(file, this, null, AppConstants.SENT_DOCUMENTS);
                                    } else {
                                        AppHelper.CustomToast(MessagesActivity.this, getString(R.string.this_document_is_not_exist));
                                    }
                                }
                            }
                        }

                        break;
                    }
                } else {
                    AppHelper.CustomToast(MessagesActivity.this, getString(R.string.you_can_share_more_then_one));
                }

                break;
            case R.id.copy_message:
                if (arraySize != 0 && arraySize == 1) {
                    for (int x = 0; x < arraySize; x++) {
                        currentPosition = mMessagesAdapter.getSelectedItems().get(x);
                        MessagesModel messagesModel = mMessagesAdapter.getItem(currentPosition);
                        if (messagesModel.getMessage() != null) {
                            if (AppHelper.copyText(this, messagesModel)) {
                                AppHelper.CustomToast(MessagesActivity.this, getString(R.string.message_is_copied));
                                if (actionMode != null) {
                                    mMessagesAdapter.clearSelections();
                                    actionMode.finish();
                                    getSupportActionBar().show();
                                }
                            }
                        } else {
                            AppHelper.CustomToast(MessagesActivity.this, getString(R.string.this_message_empty));
                        }
                    }

                } else {
                    if (actionMode != null) {
                        mMessagesAdapter.clearSelections();
                        actionMode.finish();
                        getSupportActionBar().show();
                    }
                    AppHelper.CustomToast(MessagesActivity.this, getString(R.string.you_can_copy_more_then_one));
                }

                break;
            case R.id.delete_message:
                if (arraySize != 0) {
                    Realm realm = DostChatApp.getRealmDatabaseInstance();
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(R.string.message_delete);

                    builder.setPositiveButton(R.string.Yes, (dialog, whichButton) -> {
                        AppHelper.showDialog(this, getString(R.string.deleting_chat));
                        for (int x = 0; x < arraySize; x++) {
                            int currentPosition1 = mMessagesAdapter.getSelectedItems().get(x);
                            MessagesModel messagesModel = mMessagesAdapter.getItem(currentPosition1);
                            EventBus.getDefault().post(new Pusher(EVENT_BUS_DELETE_CONVERSATION_ITEM, ConversationID));
                            int messageId = messagesModel.getId();
                            realm.executeTransactionAsync(realm1 -> {
                                MessagesModel messagesModel1 = realm1.where(MessagesModel.class).equalTo("id", messageId).equalTo("conversationID", ConversationID).findFirst();
                                messagesModel1.deleteFromRealm();
                            }, () -> {
                                AppHelper.LogCat("Message deleted successfully MessagesActivity ");
                                mMessagesAdapter.removeMessageItem(currentPosition1);
                                RealmResults<MessagesModel> messagesModel1 = realm.where(MessagesModel.class).equalTo("conversationID", ConversationID).findAll();
                                if (messagesModel1.size() == 0) {
                                    realm.executeTransactionAsync(realm1 -> {
                                        ConversationsModel conversationsModel1 = realm1.where(ConversationsModel.class).equalTo("id", ConversationID).findFirst();
                                        conversationsModel1.deleteFromRealm();
                                    }, () -> {
                                        AppHelper.LogCat("Conversation deleted successfully MessagesActivity ");
                                        EventBus.getDefault().post(new Pusher(EVENT_BUS_MESSAGE_COUNTER));
                                        NotificationsManager.SetupBadger(this);
                                    }, error -> {
                                        AppHelper.LogCat("delete conversation failed MessagesActivity " + error.getMessage());

                                    });
                                } else {
                                    MessagesModel lastMessage = realm.where(MessagesModel.class).equalTo("conversationID", ConversationID).findAll().last();
                                    if (!lastMessage.isValid()) return;
                                    realm.executeTransactionAsync(realm1 -> {
                                        ConversationsModel conversationsModel1 = realm1.where(ConversationsModel.class).equalTo("id", ConversationID).findFirst();
                                        conversationsModel1.setLastMessage(lastMessage.getMessage());
                                        conversationsModel1.setLastMessageId(lastMessage.getId());
                                        realm1.copyToRealmOrUpdate(conversationsModel1);
                                    }, () -> {
                                        AppHelper.LogCat("Conversation deleted successfully  MessagesActivity ");
                                        EventBus.getDefault().post(new Pusher(EVENT_BUS_MESSAGE_COUNTER));
                                        NotificationsManager.SetupBadger(this);
                                    }, error -> {
                                        AppHelper.LogCat("delete conversation failed  MessagesActivity" + error.getMessage());

                                    });
                                }
                            }, error -> {
                                AppHelper.LogCat("delete message failed  MessagesActivity" + error.getMessage());

                            });

                        }
                        AppHelper.hideDialog();

                        if (actionMode != null) {
                            mMessagesAdapter.clearSelections();
                            actionMode.finish();
                            getSupportActionBar().show();
                        }

                    });

                    builder.setNegativeButton(R.string.No, (dialog, whichButton) -> {

                    });

                    builder.show();
                    realm.close();
                }
                break;
            case R.id.transfer_message:
                if (arraySize != 0) {
                    ArrayList<String> messagesModelList = new ArrayList<>();
                    for (int x = 0; x < arraySize; x++) {
                        currentPosition = mMessagesAdapter.getSelectedItems().get(x);
                        MessagesModel messagesModel = mMessagesAdapter.getItem(currentPosition);
                        String message = UtilsString.unescapeJava(messagesModel.getMessage());
                        messagesModelList.add(message);
                    }
                    if (messagesModelList.size() != 0) {
                        Intent intent = new Intent(this, TransferMessageContactsActivity.class);
                        intent.putExtra("messageCopied", messagesModelList);
                        startActivity(intent);
                        finish();
                    } else {
                        AppHelper.CustomToast(MessagesActivity.this, getString(R.string.this_message_empty));
                    }
                }
                break;
            default:
                return false;
        }


        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode actionMode) {
        this.actionMode = null;
        mMessagesAdapter.clearSelections();
        getSupportActionBar().show();
        if (AppHelper.isAndroid5()) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(AppHelper.getColor(this, R.color.colorPrimaryDark));
        }
    }

    @Override
    public void onClick(View view) {
        int position = messagesList.getChildAdapterPosition(view);
        if (actionMode != null) {
            ToggleSelection(position);
        }
    }


    private class RecyclerViewBenOnGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            return super.onSingleTapConfirmed(e);
        }

        public void onLongPress(MotionEvent e) {
            AppHelper.LogCat(" onLongPress ");
            try {
                View view = messagesList.findChildViewUnder(e.getX(), e.getY());
                int currentPosition = messagesList.getChildAdapterPosition(view);
                MessagesModel messagesModel = mMessagesAdapter.getItem(currentPosition);
                if (messagesModel.isFileUpload() && messagesModel.isFileDownLoad()) {
                    if (actionMode != null) {
                        return;
                    }
                    actionMode = startActionMode(MessagesActivity.this);
                    ToggleSelection(currentPosition);
                }
                super.onLongPress(e);
            } catch (Exception e1) {
                AppHelper.LogCat(" onLongPress " + e1.getMessage());
            }
        }

    }


    private class UpdaterTimerTask extends TimerTask {

        @Override
        public void run() {
            long timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
            long timeSwapBuff = 0L;
            long updatedTime = timeSwapBuff + timeInMilliseconds;
            Duration = String.valueOf(updatedTime);
            final String recordTime = UtilsTime.getFileTime(updatedTime);
            runOnUiThread(() -> {
                try {
                    if (recordTimeText != null) {
                        recordTimeText.setText(recordTime);
                    }

                } catch (Exception e) {
                    AppHelper.LogCat("Exception record MessagesPopupActivity");
                }

            });
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        DostChatApp.getInstance().setConnectivityListener(this);
        connectToChatServer();
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
            if (isGroup)
                new Handler().postDelayed(() -> unSentMessagesGroup(groupID), 1000);
            else
                new Handler().postDelayed(() -> unSentMessagesForARecipient(recipientId, false), 1000);
        } else {
            AppHelper.Snackbar(this, mView, getString(R.string.waiting_for_network), AppConstants.MESSAGE_COLOR_WARNING, AppConstants.TEXT_COLOR);

        }
    }


}
