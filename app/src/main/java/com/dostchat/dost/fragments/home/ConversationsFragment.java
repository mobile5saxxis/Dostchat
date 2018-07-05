package com.dostchat.dost.fragments.home;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ActionMode;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.dostchat.dost.R;
import com.dostchat.dost.activities.UserContactActivity;
import com.dostchat.dost.adapters.recyclerView.messages.ConversationsAdapter;
import com.dostchat.dost.app.AppConstants;
import com.dostchat.dost.app.DostChatApp;
import com.dostchat.dost.helpers.AppHelper;
import com.dostchat.dost.helpers.PreferenceManager;
import com.dostchat.dost.helpers.notifications.NotificationsManager;
import com.dostchat.dost.interfaces.LoadingData;
import com.dostchat.dost.models.groups.GroupsModel;
import com.dostchat.dost.models.messages.ConversationsModel;
import com.dostchat.dost.models.messages.MessagesModel;
import com.dostchat.dost.models.users.Pusher;
import com.dostchat.dost.models.users.contacts.ContactsModel;
import com.dostchat.dost.presenters.messages.ConversationsPresenter;
import com.dostchat.dost.services.MainService;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;

import static com.dostchat.dost.app.AppConstants.EVENT_BUS_CONTACTS_PERMISSION;
import static com.dostchat.dost.app.AppConstants.EVENT_BUS_CREATE_NEW_GROUP;
import static com.dostchat.dost.app.AppConstants.EVENT_BUS_DELETE_CONVERSATION_ITEM;
import static com.dostchat.dost.app.AppConstants.EVENT_BUS_EXIT_NEW_GROUP;
import static com.dostchat.dost.app.AppConstants.EVENT_BUS_IMAGE_GROUP_UPDATED;
import static com.dostchat.dost.app.AppConstants.EVENT_BUS_ITEM_IS_ACTIVATED;
import static com.dostchat.dost.app.AppConstants.EVENT_BUS_MESSAGE_COUNTER;
import static com.dostchat.dost.app.AppConstants.EVENT_BUS_MESSAGE_IS_DELIVERED_FOR_CONVERSATIONS;
import static com.dostchat.dost.app.AppConstants.EVENT_BUS_MESSAGE_IS_READ;
import static com.dostchat.dost.app.AppConstants.EVENT_BUS_MESSAGE_IS_SEEN_FOR_CONVERSATIONS;
import static com.dostchat.dost.app.AppConstants.EVENT_BUS_NEW_MESSAGE_CONVERSATION_NEW_ROW;
import static com.dostchat.dost.app.AppConstants.EVENT_BUS_NEW_MESSAGE_CONVERSATION_OLD_ROW;
import static com.dostchat.dost.app.AppConstants.EVENT_BUS_NEW_MESSAGE_GROUP_CONVERSATION_NEW_ROW;
import static com.dostchat.dost.app.AppConstants.EVENT_BUS_NEW_MESSAGE_IS_SENT_FOR_CONVERSATIONS;
import static com.dostchat.dost.app.AppConstants.EVENT_UPDATE_CONVERSATION_OLD_ROW;

/**
 * Created by Abderrahim El imame  on 20/01/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class ConversationsFragment extends Fragment implements LoadingData, RecyclerView.OnItemTouchListener, ActionMode.Callback {


    @BindView(R.id.ConversationsList)
    RecyclerView ConversationList;
    @BindView(R.id.empty)
    LinearLayout emptyConversations;

    @BindView(R.id.swipeConversations)
    SwipeRefreshLayout mSwipeRefreshLayout;

    private ConversationsAdapter mConversationsAdapter;
    private ConversationsPresenter mConversationsPresenter;
    private GestureDetectorCompat gestureDetector;
    private ActionMode actionMode;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View mView = inflater.inflate(R.layout.fragment_conversations, container, false);
        ButterKnife.bind(this, mView);
        initializerView();
        mConversationsPresenter = new ConversationsPresenter(this);
        mConversationsPresenter.onCreate();

        mView.findViewById(R.id.fab1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), UserContactActivity.class));
            }
        });

        return mView;
    }

    /**
     * method to initialize the view
     */
    private void initializerView() {
        setHasOptionsMenu(true);
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mConversationsAdapter = new ConversationsAdapter(getActivity(), ConversationList);
        ConversationList.setLayoutManager(mLinearLayoutManager);
        ConversationList.setAdapter(mConversationsAdapter);
        ConversationList.setItemAnimator(new DefaultItemAnimator());
        ConversationList.getItemAnimator().setChangeDuration(0);
        ConversationList.setHasFixedSize(true);
        ConversationList.addOnItemTouchListener(this);
        gestureDetector = new GestureDetectorCompat(getActivity(), new RecyclerViewBenOnGestureListener());
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorAccent, R.color.colorGreenLight);
        mSwipeRefreshLayout.setOnRefreshListener(() -> mConversationsPresenter.onRefresh());

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
     * @param position
     */
    private void ToggleSelection(int position) {
        mConversationsAdapter.toggleSelection(position);
        String title = String.format("%s selected", mConversationsAdapter.getSelectedItemCount());
        actionMode.setTitle(title);


    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.select_conversation_menu, menu);
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_ACTION_MODE_STARTED));
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {


        switch (item.getItemId()) {
            case R.id.delete_conversations:

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());


                builder.setMessage(R.string.alert_message_delete_conversation);

                builder.setPositiveButton(R.string.Yes, (dialog, whichButton) -> {
                    int arraySize = mConversationsAdapter.getSelectedItems().size();
                    Realm realm = DostChatApp.getRealmDatabaseInstance();
                    AppHelper.LogCat("start delete " + arraySize);
                    if (arraySize != 0) {

                        AppHelper.showDialog(getActivity(), getString(R.string.deleting_chat));

                        for (int x = 0; x < arraySize; x++) {
                            int currentPosition = mConversationsAdapter.getSelectedItems().get(x);
                            try {
                                ConversationsModel conversationsModel = mConversationsAdapter.getItem(currentPosition);
                                int conversationID = getConversationId(conversationsModel.getRecipientID(), PreferenceManager.getID(getActivity()), realm);
                                realm.executeTransactionAsync(realm1 -> {
                                    RealmResults<MessagesModel> messagesModel1 = realm1.where(MessagesModel.class).equalTo("conversationID", conversationID).findAll();
                                    messagesModel1.deleteAllFromRealm();
                                }, () -> {
                                    AppHelper.LogCat("Message Deleted  successfully  ConversationsFragment");
                                    mConversationsAdapter.removeConversationItem(currentPosition);
                                    realm.executeTransactionAsync(realm1 -> {
                                        ConversationsModel conversationsModel1 = realm1.where(ConversationsModel.class).equalTo("id", conversationID).findFirst();
                                        conversationsModel1.deleteFromRealm();
                                    }, () -> {
                                        AppHelper.LogCat("Conversation deleted successfully ConversationsFragment");
                                        EventBus.getDefault().post(new Pusher(EVENT_BUS_MESSAGE_COUNTER));
                                        EventBus.getDefault().post(new Pusher(EVENT_BUS_DELETE_CONVERSATION_ITEM, conversationID));
                                        NotificationsManager.SetupBadger(getActivity());
                                    }, error -> {
                                        AppHelper.LogCat("Delete conversation failed  ConversationsFragment" + error.getMessage());

                                    });
                                }, error -> {
                                    AppHelper.LogCat("Delete message failed ConversationsFragment" + error.getMessage());

                                });

                            } catch (Exception e) {
                                AppHelper.LogCat(e);
                            }
                        }
                        AppHelper.LogCat("finish delete");
                        AppHelper.hideDialog();
                    } else {
                        AppHelper.CustomToast(getActivity(), "Delete conversation failed  ");
                    }
                    if (actionMode != null) {
                        mConversationsAdapter.clearSelections();
                        actionMode.finish();
                        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
                    }
                    realm.close();
                });


                builder.setNegativeButton(R.string.No, (dialog, whichButton) -> {

                });

                builder.show();
                return true;
            default:
                return false;
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

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        this.actionMode = null;
        mConversationsAdapter.clearSelections();
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
        EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_ACTION_MODE_DESTORYED));
    }

    public void sendGroupMessage(GroupsModel groupsModel, MessagesModel messagesModel) {
        Realm realmCreateGroup = DostChatApp.getRealmDatabaseInstance();
        ContactsModel contactsModel = realmCreateGroup.where(ContactsModel.class).equalTo("id", PreferenceManager.getID(getActivity())).findFirst();
        new Handler().postDelayed(() -> MainService.sendMessagesGroup(getActivity(), contactsModel, groupsModel, messagesModel), 500);
        realmCreateGroup.close();
    }

    public void sendGroupMessage(GroupsModel groupsModel, int conversationID) {
        new Handler().postDelayed(() -> {
            Realm realmCreateGroup = DostChatApp.getRealmDatabaseInstance();
            ContactsModel contactsModel = realmCreateGroup.where(ContactsModel.class).equalTo("id", PreferenceManager.getID(getActivity())).findFirst();
            MessagesModel messagesModel = realmCreateGroup.where(MessagesModel.class).equalTo("conversationID", conversationID).equalTo("isGroup", true).findFirst();
            MainService.sendMessagesGroup(getActivity(), contactsModel, groupsModel, messagesModel);
            realmCreateGroup.close();
        }, 500);

    }


    private class RecyclerViewBenOnGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            return super.onSingleTapConfirmed(e);
        }

        public void onLongPress(MotionEvent e) {
            try {
                View view = ConversationList.findChildViewUnder(e.getX(), e.getY());
                int currentPosition = ConversationList.getChildAdapterPosition(view);
                ConversationsModel conversationsModel = mConversationsAdapter.getItem(currentPosition);
                if (!conversationsModel.isGroup()) {
                    if (actionMode != null) {
                        return;
                    }
                    actionMode = getActivity().startActionMode(ConversationsFragment.this);
                    ToggleSelection(currentPosition);
                }
                super.onLongPress(e);
            } catch (Exception e1) {
                AppHelper.LogCat(" onLongPress " + e1.getMessage());
            }


        }

    }


    /**
     * method to show conversation list
     *
     * @param conversationsModels this is parameter for  ShowConversation  method
     */
    public void UpdateConversation(List<ConversationsModel> conversationsModels) {

        if (conversationsModels.size() != 0) {
            ConversationList.setVisibility(View.VISIBLE);
            emptyConversations.setVisibility(View.GONE);
            RealmList<ConversationsModel> conversationsModels1 = new RealmList<ConversationsModel>();
            for (ConversationsModel conversationsModel : conversationsModels) {
                conversationsModels1.add(conversationsModel);
            }
            mConversationsAdapter.setConversations(conversationsModels1);
        } else {
            ConversationList.setVisibility(View.GONE);
            emptyConversations.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mConversationsPresenter.onDestroy();
    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    public void onShowLoading() {
        if (!mSwipeRefreshLayout.isRefreshing())
            mSwipeRefreshLayout.setRefreshing(true);
    }

    @Override
    public void onHideLoading() {
        if (mSwipeRefreshLayout.isRefreshing())
            mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onErrorLoading(Throwable throwable) {
        AppHelper.LogCat(throwable);
        if (mSwipeRefreshLayout.isRefreshing())
            mSwipeRefreshLayout.setRefreshing(false);
    }

    /**
     * method to add a new message to list messages
     *
     * @param conversationId this is the parameter for addConversationEventMainThread
     */

    private void addConversationEventMainThread(int conversationId) {
        mConversationsAdapter.addConversationItem(conversationId);
        ConversationList.scrollToPosition(0);
    }

    /**
     * method of EventBus
     *
     * @param pusher this is parameter of onEventMainThread method
     */
    @SuppressWarnings("unused")
    public void onEventMainThread(Pusher pusher) {
        int messageId = pusher.getMessageId();
        switch (pusher.getAction()) {
            case EVENT_BUS_ITEM_IS_ACTIVATED:
                int idx = ConversationList.getChildAdapterPosition(pusher.getView());
                if (actionMode != null) {
                    ToggleSelection(idx);
                    return;
                }

                break;
            case EVENT_BUS_NEW_MESSAGE_CONVERSATION_NEW_ROW:
                new Handler().postDelayed(() -> addConversationEventMainThread(pusher.getConversationId()), 500);
                break;

            case EVENT_BUS_NEW_MESSAGE_GROUP_CONVERSATION_NEW_ROW:
                mConversationsPresenter.getGroupInfo(pusher.getGroupID());
                break;
            case EVENT_BUS_NEW_MESSAGE_CONVERSATION_OLD_ROW:
                new Handler().postDelayed(() -> mConversationsAdapter.updateConversationItem(pusher.getConversationId()), 500);
                break;
            case EVENT_BUS_MESSAGE_IS_READ:
            case EVENT_UPDATE_CONVERSATION_OLD_ROW:
            case EVENT_BUS_NEW_MESSAGE_IS_SENT_FOR_CONVERSATIONS:
            case EVENT_BUS_MESSAGE_IS_SEEN_FOR_CONVERSATIONS:
            case EVENT_BUS_MESSAGE_IS_DELIVERED_FOR_CONVERSATIONS:
                new Handler().postDelayed(() -> mConversationsAdapter.updateStatusConversationItem(pusher.getConversationId()), 500);
                break;
            case EVENT_BUS_DELETE_CONVERSATION_ITEM:
                mConversationsAdapter.DeleteConversationItem(pusher.getConversationId());
                break;
            case EVENT_BUS_CREATE_NEW_GROUP:
                mConversationsPresenter.getGroupInfo(pusher.getGroupID(), pusher.getConversationId());
                break;
            case EVENT_BUS_EXIT_NEW_GROUP:
                MessagesModel messagesModel2 = pusher.getMessagesModel();
                mConversationsPresenter.getGroupInfo(pusher.getGroupID(), messagesModel2);
                break;
            case EVENT_BUS_CONTACTS_PERMISSION:
                mConversationsPresenter.onRefresh();
                break;
            case EVENT_BUS_IMAGE_GROUP_UPDATED:
                mConversationsPresenter.getGroupInfo(pusher.getGroupID());
                break;

        }
    }


}
