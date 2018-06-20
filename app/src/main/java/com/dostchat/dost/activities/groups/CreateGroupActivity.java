package com.dostchat.dost.activities.groups;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.transition.Fade;
import android.transition.Transition;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dostchat.dost.R;
import com.dostchat.dost.adapters.recyclerView.groups.CreateGroupMembersToGroupAdapter;
import com.dostchat.dost.app.AppConstants;
import com.dostchat.dost.app.DostChatApp;
import com.dostchat.dost.helpers.AppHelper;
import com.dostchat.dost.helpers.Files.FilesManager;
import com.dostchat.dost.helpers.Files.backup.RealmBackupRestore;
import com.dostchat.dost.helpers.PermissionHandler;
import com.dostchat.dost.helpers.PreferenceManager;
import com.dostchat.dost.helpers.UtilsString;
import com.dostchat.dost.interfaces.NetworkListener;
import com.dostchat.dost.models.messages.ConversationsModel;
import com.dostchat.dost.models.messages.MessagesModel;
import com.dostchat.dost.models.users.Pusher;
import com.dostchat.dost.models.users.contacts.ContactsModel;
import com.dostchat.dost.ui.CropSquareTransformation;
import com.squareup.picasso.Picasso;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;
import io.realm.Realm;
import io.realm.RealmList;


/**
 * Created by Abderrahim El imame on 20/03/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class CreateGroupActivity extends AppCompatActivity implements NetworkListener {


    @BindView(R.id.subject_wrapper)
    EmojiconEditText subjectWrapper;


    @BindView(R.id.emoticonBtn)
    ImageView EmoticonButton;


    @BindView(R.id.group_image)
    ImageView groupImage;
    @BindView(R.id.add_image_group)
    ImageView addImageGroup;
    @BindView(R.id.fab)
    FloatingActionButton doneBtn;

    @BindView(R.id.ContactsList)
    RecyclerView ContactsList;
    @BindView(R.id.participantCounter)
    TextView participantCounter;
    @BindView(R.id.app_bar)
    Toolbar toolbar;
    @BindView(R.id.create_group)
    LinearLayout mView;

    private CreateGroupMembersToGroupAdapter mAddMembersToGroupListAdapter;
    private boolean emoticonShown = false;
    private String selectedImagePath = null;
    private Realm realm;
    private int lastConversationID;


    EmojIconActions emojIcon;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        ButterKnife.bind(this);
        realm = DostChatApp.getRealmDatabaseInstance();
        initializeView();
        setupToolbar();
        loadData();
        setTypeFaces();

        subjectWrapper.setOnClickListener(v1 -> {
            if (emoticonShown) {
                emoticonShown = false;
                emojIcon.closeEmojIcon();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            }
        });
        EmoticonButton.setOnClickListener(v -> {
            if (!emoticonShown) {
                emoticonShown = true;
                emojIcon = new EmojIconActions(CreateGroupActivity.this, mView, subjectWrapper, EmoticonButton);
                emojIcon.setIconsIds(R.drawable.ic_keyboard_gray_24dp, R.drawable.ic_emoticon_24dp);
                emojIcon.ShowEmojIcon();

            }

        });


    }
    private void setTypeFaces() {
        if (AppConstants.ENABLE_FONTS_TYPES) {
            participantCounter.setTypeface(AppHelper.setTypeFace(this, "Futura"));

        }
    }
    /**
     * method to loadCircleImage members form shared preference
     */
    private void loadData() {
        List<ContactsModel> contactsModels = new ArrayList<>();
        int arraySize = PreferenceManager.getMembers(this).size();

        int id;
        for (int x = 0; x < arraySize; x++) {
            id = PreferenceManager.getMembers(this).get(x).getUserId();
            ContactsModel contactsModel = realm.where(ContactsModel.class).equalTo("id", id).findFirst();
            contactsModels.add(contactsModel);
        }
        mAddMembersToGroupListAdapter.setContacts(contactsModels);

        String text = String.format(getString(R.string.participants) + " %s/%s ", mAddMembersToGroupListAdapter.getItemCount(), PreferenceManager.getContactSize(this));
        participantCounter.setText(text);
    }

    /**
     * method to setup the toolbar
     */
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(R.string.title_activity_add_members_to_group);
    }


    /**
     * method to initialize  the view
     */
    private void initializeView() {
        GridLayoutManager mLinearLayoutManager = new GridLayoutManager(getApplicationContext(), 4);
        ContactsList.setLayoutManager(mLinearLayoutManager);
        mAddMembersToGroupListAdapter = new CreateGroupMembersToGroupAdapter(this);
        ContactsList.setAdapter(mAddMembersToGroupListAdapter);
        doneBtn.setOnClickListener(v -> createGroupOffline());
        addImageGroup.setOnClickListener(v -> launchImageChooser());
        if (AppHelper.isAndroid5()) {
            Transition enterTrans = new Fade();
            getWindow().setEnterTransition(enterTrans);
            enterTrans.setDuration(300);
        } else {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 16, 0);
            params.gravity = Gravity.RIGHT | Gravity.BOTTOM;
            doneBtn.setLayoutParams(params);
        }


    }

    /**
     * method to select an image
     */
    private void launchImageChooser() {
        Intent mIntent = new Intent();
        mIntent.setType("image/*");
        mIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(
                Intent.createChooser(mIntent, getString(R.string.select_picture)),
                AppConstants.UPLOAD_PICTURE_REQUEST_CODE);
    }

    /**
     * method to create group in offline mode
     */
    private void createGroupOffline() {
        String groupName = UtilsString.escapeJava(subjectWrapper.getText().toString().trim());
        if (groupName.length() <= 3) {
            subjectWrapper.setError(getString(R.string.name_is_too_short));
        } else {
            DateTime current = new DateTime();
            String createTime = String.valueOf(current);
            realm.executeTransactionAsync(realm1 -> {

                int lastConversationID = RealmBackupRestore.getConversationLastId();
                int lastID = RealmBackupRestore.getMessageLastId();
                RealmList<MessagesModel> messagesModelRealmList = new RealmList<MessagesModel>();
                MessagesModel messagesModel = new MessagesModel();
                messagesModel.setId(lastID);
                messagesModel.setDate(createTime);
                messagesModel.setSenderID(PreferenceManager.getID(this));
                messagesModel.setRecipientID(0);
                messagesModel.setPhone(PreferenceManager.getPhone(this));
                messagesModel.setStatus(AppConstants.IS_SEEN);
                messagesModel.setUsername(null);
                messagesModel.setGroup(true);
                messagesModel.setMessage(AppConstants.CREATE_GROUP);
                messagesModel.setGroupID(lastConversationID);
                messagesModel.setConversationID(lastConversationID);
                messagesModel.setImageFile("null");
                messagesModel.setVideoFile("null");
                messagesModel.setAudioFile("null");
                messagesModel.setDocumentFile("null");
                messagesModel.setVideoThumbnailFile("null");
                messagesModel.setFileUpload(true);
                messagesModel.setFileDownLoad(true);
                messagesModel.setFileSize("0");
                messagesModel.setDuration("0");
                messagesModelRealmList.add(messagesModel);
                ConversationsModel conversationsModel = new ConversationsModel();
                conversationsModel.setLastMessage(AppConstants.CREATE_GROUP);
                conversationsModel.setLastMessageId(lastID);
                conversationsModel.setCreatorID(PreferenceManager.getID(this));
                conversationsModel.setRecipientID(0);
                conversationsModel.setRecipientUsername(groupName);
                conversationsModel.setRecipientImage(selectedImagePath);
                conversationsModel.setGroupID(lastConversationID);
                conversationsModel.setMessageDate(createTime);
                conversationsModel.setId(lastConversationID);
                conversationsModel.setGroup(true);
                conversationsModel.setMessages(messagesModelRealmList);
                conversationsModel.setStatus(AppConstants.IS_SEEN);
                conversationsModel.setUnreadMessageCounter("0");
                conversationsModel.setCreatedOnline(false);
                realm1.copyToRealm(conversationsModel);
                this.lastConversationID = lastConversationID;

            }, () -> {

                AppHelper.Snackbar(this, findViewById(R.id.create_group), getString(R.string.group_created_successfully), AppConstants.MESSAGE_COLOR_SUCCESS, AppConstants.TEXT_COLOR);
                new Handler().postDelayed(() -> {
                    EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_NEW_MESSAGE_CONVERSATION_NEW_ROW, lastConversationID));
                    EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_ADD_MEMBER, lastConversationID));
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                    finish();
                }, 200);
            }, error -> {
                AppHelper.LogCat("Realm Error create group offline CreateGroupActivity " + error.getMessage());
                AppHelper.Snackbar(this, findViewById(R.id.create_group), getString(R.string.create_group_failed), AppConstants.MESSAGE_COLOR_ERROR, AppConstants.TEXT_COLOR);

            });


        }
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == AppConstants.UPLOAD_PICTURE_REQUEST_CODE) {

                if (PermissionHandler.checkPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    AppHelper.LogCat("Read contact data permission already granted.");
                    selectedImagePath = FilesManager.getPath(this, data.getData());
                    Picasso.with(this)
                            .load(data.getData())
                            .transform(new CropSquareTransformation())
                            .resize(AppConstants.ROWS_IMAGE_SIZE, AppConstants.ROWS_IMAGE_SIZE)
                            .placeholder(R.drawable.image_holder_gr_circle)
                            .error(R.drawable.image_holder_gr_circle)
                            .into(groupImage);
                    if (groupImage.getVisibility() != View.VISIBLE) {
                        groupImage.setVisibility(View.VISIBLE);
                    }
                } else {
                    AppHelper.LogCat("Please request Read contact data permission.");
                    PermissionHandler.requestPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
                }


            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
        if (emojIcon != null) {
            emojIcon.closeEmojIcon();
            emojIcon = null;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (mAddMembersToGroupListAdapter.getContacts().size() != 0) {
                PreferenceManager.clearMembers(this);
                mAddMembersToGroupListAdapter.getContacts().clear();

            }
            finish();

            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mAddMembersToGroupListAdapter.getContacts().size() != 0) {
            PreferenceManager.clearMembers(this);
            mAddMembersToGroupListAdapter.getContacts().clear();

        }
        finish();

        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }


    @Override
    protected void onResume() {
        super.onResume();
        DostChatApp.getInstance().setConnectivityListener(this);
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


}
