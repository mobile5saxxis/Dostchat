package com.dostchat.dost.activities;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.dostchat.dost.R;
import com.dostchat.dost.adapters.others.TextWatcherAdapter;
import com.dostchat.dost.adapters.recyclerView.CountriesAdapter;
import com.dostchat.dost.app.AppConstants;
import com.dostchat.dost.helpers.AppHelper;
import com.dostchat.dost.models.CountriesModel;
import com.dostchat.dost.ui.RecyclerViewFastScroller;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Abderrahim El imame on 10/30/16.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */

public class CountryActivity extends AppCompatActivity {

    private CountriesAdapter mCountriesAdapter;

    @BindView(R.id.search_input)
    TextInputEditText searchInput;
    @BindView(R.id.clear_btn_search_view)
    AppCompatImageView clearBtn;
    @BindView(R.id.close_btn_search_view)
    AppCompatImageView closeBtn;

    @BindView(R.id.CounrtriesList)
    RecyclerView CountriesList;
    @BindView(R.id.fastscroller)
    RecyclerViewFastScroller fastScroller;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_counrty);
        ButterKnife.bind(this);
        initializerView();
        setTypeFaces();
    }


    private void setTypeFaces() {
        if (AppConstants.ENABLE_FONTS_TYPES) {
            searchInput.setTypeface(AppHelper.setTypeFace(this, "Futura"));
        }
    }

    void initializerView() {

        initializerSearchView(searchInput, clearBtn);
        clearBtn.setOnClickListener(v -> clearSearchView());
        closeBtn.setOnClickListener(v -> closeSearchView());
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(getApplicationContext());
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        CountriesList.setLayoutManager(mLinearLayoutManager);
        mCountriesAdapter = new CountriesAdapter(this);
        CountriesList.setAdapter(mCountriesAdapter);
        Gson gson = new Gson();
        final List<CountriesModel> list = gson.fromJson(AppHelper.loadJSONFromAsset(this), new TypeToken<List<CountriesModel>>() {
        }.getType());
        mCountriesAdapter.setCountries(list);

        // set recycler view to fastScroller
        fastScroller.setRecyclerView(CountriesList);
        fastScroller.setViewsToUse(R.layout.contacts_fragment_fast_scroller, R.id.fastscroller_bubble, R.id.fastscroller_handle);
    }


    /**
     * method to clear/reset search view content
     */
    public void clearSearchView() {
        if (searchInput.getText() != null) {
            searchInput.setText("");
            Gson gson = new Gson();
            final List<CountriesModel> list = gson.fromJson(AppHelper.loadJSONFromAsset(this), new TypeToken<List<CountriesModel>>() {
            }.getType());
            mCountriesAdapter.setCountries(list);
        }

    }

    /**
     * method to initial the search view
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
                mCountriesAdapter.setString(s.toString());
                Search(s.toString().trim());
                clearSearchBtn.setVisibility(View.VISIBLE);
            }

            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void afterTextChanged(Editable s) {

                if (s.length() == 0) {
                    clearSearchBtn.setVisibility(View.GONE);
                    Gson gson = new Gson();
                    final List<CountriesModel> list = gson.fromJson(AppHelper.loadJSONFromAsset(CountryActivity.this), new TypeToken<List<CountriesModel>>() {
                    }.getType());
                    mCountriesAdapter.setCountries(list);
                }
            }
        });

    }

    /**
     * method to start searching
     *
     * @param string this is parameter of Search method
     */
    public void Search(String string) {

        final List<CountriesModel> filteredModelList;
        filteredModelList = FilterList(string);
        if (filteredModelList.size() != 0) {
            mCountriesAdapter.animateTo(filteredModelList);
            CountriesList.scrollToPosition(0);
        }
    }

    /**
     * method to filter the list
     *
     * @param query this is parameter of FilterList method
     * @return this for what method return
     */
    private List<CountriesModel> FilterList(String query) {
        query = query.toLowerCase();
        List<CountriesModel> countriesModelList = mCountriesAdapter.getCountries();
        final List<CountriesModel> filteredModelList = new ArrayList<>();
        for (CountriesModel countriesModel : countriesModelList) {
            final String name = countriesModel.getName().toLowerCase();
            if (name.contains(query)) {
                filteredModelList.add(countriesModel);
            }
        }
        return filteredModelList;
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
}
