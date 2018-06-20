package com.dostchat.dost.activities.search;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;

import com.dostchat.dost.R;
import com.dostchat.dost.adapters.others.TextWatcherAdapter;
import com.dostchat.dost.adapters.recyclerView.messages.ConversationsAdapter;
import com.dostchat.dost.app.AppConstants;
import com.dostchat.dost.app.DostChatApp;
import com.dostchat.dost.helpers.AppHelper;
import com.dostchat.dost.models.messages.ConversationsModel;
import com.dostchat.dost.presenters.messages.SearchConversationsPresenter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmList;

/**
 * Created by Abderrahim El imame on 8/12/16.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/bencherif_el
 */

public class SearchConversationsActivity extends AppCompatActivity {


    @BindView(R.id.close_btn_search_view)
    ImageView closeBtn;
    @BindView(R.id.search_input)
    TextInputEditText searchInput;
    @BindView(R.id.clear_btn_search_view)
    ImageView clearBtn;
    @BindView(R.id.searchList)
    RecyclerView searchList;

    private ConversationsAdapter mConversationsAdapter;
    private SearchConversationsPresenter mSearchConversationsPresenter ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ButterKnife.bind(this);
        searchInput.setFocusable(true);
        initializerSearchView(searchInput, clearBtn);
        initializerView();
        setTypeFaces();
        mSearchConversationsPresenter = new SearchConversationsPresenter(this);
        mSearchConversationsPresenter.onCreate();
    }


    private void setTypeFaces() {
        if (AppConstants.ENABLE_FONTS_TYPES) {
            searchInput.setTypeface(AppHelper.setTypeFace(this, "Futura"));
        }
    }
    /**
     * method to initialize the view
     */
    private void initializerView() {
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(getApplicationContext());
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        searchList.setLayoutManager(mLinearLayoutManager);
        mConversationsAdapter = new ConversationsAdapter(this);
        searchList.setAdapter(mConversationsAdapter);
        closeBtn.setOnClickListener(v -> closeSearchView());
        clearBtn.setOnClickListener(v -> clearSearchView());
    }

    /**
     * method to show conversations list
     *
     * @param conversationsModels this is the parameter for  ShowConversation  method
     */
    public void ShowConversation(List<ConversationsModel> conversationsModels) {
        RealmList<ConversationsModel> conversationsModels1 = new RealmList<ConversationsModel>();
        for (ConversationsModel conversationsModel : conversationsModels) {
            conversationsModels1.add(conversationsModel);
        }
        mConversationsAdapter.setConversations(conversationsModels1);
    }

    /**
     * method to clear/reset   the search view
     */
    public void clearSearchView() {
        if (searchInput.getText() != null)
            searchInput.setText("");
    }

    /**
     * method to close the search view
     */
    public void closeSearchView() {
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSearchConversationsPresenter.onDestroy();
    }


    /**
     * method to initialize the search view
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
                mConversationsAdapter.setString(s.toString());
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

    public void onErrorLoading(Throwable throwable) {
        AppHelper.LogCat("Conversation search " + throwable.getMessage());
    }

    /**
     * method to start searching
     *
     * @param string this is parameter for  Search  method
     */
    public void Search(String string) {

        final List<ConversationsModel> filteredModelList;
        filteredModelList = FilterList(string);
        if (filteredModelList.size() != 0) {
            mConversationsAdapter.animateTo(filteredModelList);
            searchList.scrollToPosition(0);
        }
    }

    /**
     * method to filter the conversation list
     *
     * @param query this is parameter for   method
     * @return this is what  method will return
     */
    private List<ConversationsModel> FilterList(String query) {
        Realm realm = DostChatApp.getRealmDatabaseInstance();
        List<ConversationsModel> conversationsModels = realm.where(ConversationsModel.class)
                .contains("RecipientUsername", query, Case.INSENSITIVE)
                .findAll();
        return conversationsModels;
    }
}
