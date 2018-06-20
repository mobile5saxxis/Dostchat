package com.dostchat.dost.activities.groups;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.dostchat.dost.R;
import com.dostchat.dost.adapters.recyclerView.groups.AddMembersToGroupAdapter;
import com.dostchat.dost.adapters.recyclerView.groups.AddMembersToGroupSelectorAdapter;
import com.dostchat.dost.app.AppConstants;
import com.dostchat.dost.app.DostChatApp;
import com.dostchat.dost.helpers.AppHelper;
import com.dostchat.dost.helpers.PreferenceManager;
import com.dostchat.dost.interfaces.NetworkListener;
import com.dostchat.dost.models.groups.MembersGroupModel;
import com.dostchat.dost.models.users.Pusher;
import com.dostchat.dost.models.users.contacts.ContactsModel;
import com.dostchat.dost.presenters.groups.AddMembersToGroupPresenter;
import com.dostchat.dost.ui.RecyclerViewFastScroller;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import io.realm.Realm;

/**
 * Created by Abderrahim El imame on 20/03/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class AddMembersToGroupActivity extends AppCompatActivity implements RecyclerView.OnItemTouchListener, View.OnClickListener, NetworkListener {
    @BindView(R.id.ContactsList)
    RecyclerView ContactsList;
    @BindView(R.id.ParentLayoutAddContact)
    RelativeLayout ParentLayoutAddContact;
    @BindView(R.id.app_bar)
    Toolbar toolbar;
    @BindView(R.id.fab)
    FloatingActionButton floatingActionButton;
    @BindView(R.id.ContactsListHeader)
    RecyclerView ContactsListHeader;
    @BindView(R.id.fastscroller)
    RecyclerViewFastScroller fastScroller;

    private List<ContactsModel> mContactsModelList;
    private AddMembersToGroupAdapter mAddMembersToGroupListAdapter;
    private AddMembersToGroupSelectorAdapter mAddMembersToGroupSelectorAdapter;
    private GestureDetectorCompat gestureDetector;
    private AddMembersToGroupPresenter mAddMembersToGroupPresenter;
    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_members_to_group);
        ButterKnife.bind(this);
        realm = DostChatApp.getRealmDatabaseInstance();
        mAddMembersToGroupPresenter = new AddMembersToGroupPresenter(this);
        initializeView();
        setupToolbar();
        EventBus.getDefault().register(this);
    }

    /**
     * method to initialize the view
     */
    private void initializeView() {
        mAddMembersToGroupPresenter.onCreate();
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(getApplicationContext());
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        ContactsList.setLayoutManager(mLinearLayoutManager);
        mAddMembersToGroupListAdapter = new AddMembersToGroupAdapter(this, mContactsModelList);
        ContactsList.setAdapter(mAddMembersToGroupListAdapter);
        // set recycler view to fastScroller
        fastScroller.setRecyclerView(ContactsList);
        fastScroller.setViewsToUse(R.layout.contacts_fragment_fast_scroller, R.id.fastscroller_bubble, R.id.fastscroller_handle);
        ContactsList.setItemAnimator(new DefaultItemAnimator());
        ContactsList.addOnItemTouchListener(this);
        gestureDetector = new GestureDetectorCompat(this, new RecyclerViewBenOnGestureListener());
        floatingActionButton.setOnClickListener(this);


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        ContactsListHeader.setLayoutManager(linearLayoutManager);
        mAddMembersToGroupSelectorAdapter = new AddMembersToGroupSelectorAdapter(this);
        ContactsListHeader.setAdapter(mAddMembersToGroupSelectorAdapter);
    }

    /**
     * method to show contacts
     *
     * @param contactsModels this  parameter of ShowContacts method
     */
    public void ShowContacts(List<ContactsModel> contactsModels) {
        mContactsModelList = contactsModels;
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
        String title = String.format(" %s " + getResources().getString(R.string.of) + " %s " + getResources().getString(R.string.selected), mAddMembersToGroupListAdapter.getSelectedItemCount(), mAddMembersToGroupListAdapter.getContacts().size());
        toolbar.setSubtitle(title);
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
     * @param position this is parameter of ToggleSelection method
     */
    private void ToggleSelection(int position) {
        mAddMembersToGroupListAdapter.toggleSelection(position);
        String title = String.format(" %s " + getResources().getString(R.string.of) + " %s " + getResources().getString(R.string.selected), mAddMembersToGroupListAdapter.getSelectedItemCount(), mAddMembersToGroupListAdapter.getContacts().size());
        toolbar.setSubtitle(title);

    }


    @Override
    public void onClick(View v) {
        try {
            if (v.getId() == R.id.container_list_item) {

                int position = ContactsList.getChildAdapterPosition(v);
                ToggleSelection(position);


            } else if (v.getId() == R.id.fab) {
                if (mAddMembersToGroupListAdapter.getSelectedItemCount() != 0) {
                    int arraySize = mAddMembersToGroupListAdapter.getSelectedItems().size();
                    for (int x = 0; x < arraySize; x++) {
                        MembersGroupModel membersGroupModel = new MembersGroupModel();
                        int position = mAddMembersToGroupListAdapter.getSelectedItems().get(x);
                        int id = mAddMembersToGroupListAdapter.getContacts().get(position).getId();
                        String username = mAddMembersToGroupListAdapter.getContacts().get(position).getUsername();
                        String phone = mAddMembersToGroupListAdapter.getContacts().get(position).getPhone();
                        String status = mAddMembersToGroupListAdapter.getContacts().get(position).getStatus();
                        String statusDate = mAddMembersToGroupListAdapter.getContacts().get(position).getStatus_date();
                        String userImage = mAddMembersToGroupListAdapter.getContacts().get(position).getImage();
                        String role = "member";
                        membersGroupModel.setUserId(id);
                        membersGroupModel.setUsername(username);
                        membersGroupModel.setPhone(phone);
                        membersGroupModel.setStatus(status);
                        membersGroupModel.setStatus_date(statusDate);
                        membersGroupModel.setImage(userImage);
                        membersGroupModel.setRole(role);
                        PreferenceManager.addMember(this, membersGroupModel);
                    }
                    AppHelper.LaunchActivity(this, CreateGroupActivity.class);
                    finish();
                } else {
                    AppHelper.Snackbar(this, ParentLayoutAddContact, getString(R.string.select_one_at_least), AppConstants.MESSAGE_COLOR_ERROR, AppConstants.TEXT_COLOR);

                }


            }
        } catch (Exception e) {
            AppHelper.LogCat(" Touch Exception AddMembersToGroupActivity " + e.getMessage());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private class RecyclerViewBenOnGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            View view = ContactsList.findChildViewUnder(e.getX(), e.getY());
            onClick(view);
            return super.onSingleTapConfirmed(e);
        }

    }

    /**
     * method to scroll to the bottom of recyclerView
     */
    private void scrollToBottom() {
        ContactsListHeader.scrollToPosition(mAddMembersToGroupSelectorAdapter.getItemCount() - 1);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (mAddMembersToGroupListAdapter.getSelectedItemCount() != 0) {
                mAddMembersToGroupListAdapter.clearSelections();
            }
            PreferenceManager.clearMembers(this);
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * method of EventBus
     *
     * @param pusher this is parameter of onEventMainThread method
     */
    @SuppressWarnings("unused")
    public void onEventMainThread(Pusher pusher) {
        switch (pusher.getAction()) {
            case AppConstants.EVENT_BUS_REMOVE_CREATE_MEMBER:
                mAddMembersToGroupSelectorAdapter.remove(pusher.getContactsModel());
                if (mAddMembersToGroupSelectorAdapter.getContacts().size() == 0) {
                    ContactsListHeader.setVisibility(View.GONE);
                }
                break;
            case AppConstants.EVENT_BUS_ADD_CREATE_MEMBER:
                ContactsListHeader.setVisibility(View.VISIBLE);
                mAddMembersToGroupSelectorAdapter.add(pusher.getContactsModel());
                scrollToBottom();
                break;
            case AppConstants.EVENT_BUS_DELETE_CREATE_MEMBER:
                int position = mAddMembersToGroupListAdapter.getItemPosition(pusher.getContactsModel());
                ToggleSelection(position);
                break;
            case AppConstants.EVENT_BUS_CREATE_GROUP:
                finish();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mAddMembersToGroupListAdapter.getSelectedItemCount() != 0) {
            mAddMembersToGroupListAdapter.clearSelections();
        }
        PreferenceManager.clearMembers(this);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }


    @Override
    protected void onResume() {
        super.onResume();
        DostChatApp.getInstance().setConnectivityListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mAddMembersToGroupPresenter.onDestroy();
        EventBus.getDefault().unregister(this);
        realm.close();
    }

    /**
     * Callback will be triggered when there is change in
     * network connection
     */
    @Override
    public void onNetworkConnectionChanged(boolean isConnecting, boolean isConnected) {
        if (!isConnecting && !isConnected) {
            AppHelper.Snackbar(this, ParentLayoutAddContact, getString(R.string.connection_is_not_available), AppConstants.MESSAGE_COLOR_ERROR, AppConstants.TEXT_COLOR);
        } else if (isConnecting && isConnected) {
            AppHelper.Snackbar(this, ParentLayoutAddContact, getString(R.string.connection_is_available), AppConstants.MESSAGE_COLOR_SUCCESS, AppConstants.TEXT_COLOR);
        } else {
            AppHelper.Snackbar(this, ParentLayoutAddContact, getString(R.string.waiting_for_network), AppConstants.MESSAGE_COLOR_WARNING, AppConstants.TEXT_COLOR);
        }

    }
}
