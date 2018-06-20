package com.dostchat.dost.fragments.media;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dostchat.dost.R;
import com.dostchat.dost.adapters.recyclerView.media.MediaAdapter;
import com.dostchat.dost.helpers.AppHelper;
import com.dostchat.dost.models.messages.MessagesModel;
import com.dostchat.dost.presenters.users.ProfilePresenter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Abderrahim El imame on 1/25/17.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */

public class MediaFragment extends Fragment {


    private View mView;
    private MediaAdapter mediaAdapter;
    private ProfilePresenter mProfilePresenter;

    @BindView(R.id.mediaList)
    RecyclerView mediaList;

    public static MediaFragment newInstance(String tag) {
        MediaFragment mediaFragment = new MediaFragment();
        Bundle args = new Bundle();
        args.putString("tag", tag);
        mediaFragment.setArguments(args);
        return mediaFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_media, container, false);
        ButterKnife.bind(this, mView);
        initializerView();
        mProfilePresenter = new ProfilePresenter(this);
        mProfilePresenter.onCreate();

        return mView;
    }


    public void initializerView() {
        mediaList.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        mediaAdapter = new MediaAdapter(getActivity());
        mediaList.setAdapter(mediaAdapter);
    }

    public void ShowMedia(List<MessagesModel> messagesModel) {
        if (messagesModel.size() != 0) {
            mediaAdapter.setMessages(messagesModel);
        }

    }

    public void onErrorLoading(Throwable throwable) {
        AppHelper.LogCat("MediaFragment throwable " + throwable.getMessage());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mProfilePresenter.onDestroy();
    }


}
