package com.dostchat.dost.adapters.recyclerView.calls;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.dostchat.dost.R;
import com.dostchat.dost.activities.call.CallDetailsActivity;
import com.dostchat.dost.activities.profile.ProfilePreviewActivity;
import com.dostchat.dost.activities.settings.PreferenceSettingsManager;
import com.dostchat.dost.app.AppConstants;
import com.dostchat.dost.app.DostChatApp;
import com.dostchat.dost.app.EndPoints;
import com.dostchat.dost.helpers.AppHelper;
import com.dostchat.dost.helpers.Files.cache.ImageLoader;
import com.dostchat.dost.helpers.Files.cache.MemoryCache;
import com.dostchat.dost.helpers.UtilsPhone;
import com.dostchat.dost.helpers.UtilsTime;
import com.dostchat.dost.helpers.call.CallManager;
import com.dostchat.dost.helpers.images.DostChatImageLoader;
import com.dostchat.dost.models.calls.CallsModel;

import org.joda.time.DateTime;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmQuery;

/**
 * Created by Abderrahim El imame on 12/3/16.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */

public class CallsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    private Activity mActivity;
    private List<CallsModel> callsModelList;
    private MemoryCache memoryCache;
    private RecyclerView callList;
    private String SearchQuery;


    public CallsAdapter(@NonNull Activity mActivity, RecyclerView callList) {
        this.mActivity = mActivity;
        this.callList = callList;
        this.memoryCache = new MemoryCache();
    }

    public void setCalls(List<CallsModel> callsModelList) {
        this.callsModelList = callsModelList;
        notifyDataSetChanged();
    }


    //Methods for search start
    public void setString(String SearchQuery) {
        this.SearchQuery = SearchQuery;
        notifyDataSetChanged();
    }

    public void animateTo(List<CallsModel> models) {
        applyAndAnimateRemovals(models);
        applyAndAnimateAdditions(models);
        applyAndAnimateMovedItems(models);
    }

    private void applyAndAnimateRemovals(List<CallsModel> newModels) {
        int arraySize = callsModelList.size();
        for (int i = arraySize - 1; i >= 0; i--) {
            final CallsModel model = callsModelList.get(i);
            if (!newModels.contains(model)) {
                removeItem(i);
            }
        }
    }

    private void applyAndAnimateAdditions(List<CallsModel> newModels) {
        int arraySize = newModels.size();
        for (int i = 0; i < arraySize; i++) {
            final CallsModel model = newModels.get(i);
            if (!callsModelList.contains(model)) {
                addItem(i, model);
            }
        }
    }

    private void applyAndAnimateMovedItems(List<CallsModel> newModels) {
        int arraySize = newModels.size();
        for (int toPosition = arraySize - 1; toPosition >= 0; toPosition--) {
            final CallsModel model = newModels.get(toPosition);
            final int fromPosition = callsModelList.indexOf(model);
            if (fromPosition >= 0 && fromPosition != toPosition) {
                moveItem(fromPosition, toPosition);
            }
        }
    }

    private CallsModel removeItem(int position) {
        final CallsModel model = callsModelList.remove(position);
        notifyItemRemoved(position);
        return model;
    }

    private void addItem(int position, CallsModel model) {
        callsModelList.add(position, model);
        notifyItemInserted(position);
    }

    private void moveItem(int fromPosition, int toPosition) {
        final CallsModel model = callsModelList.remove(fromPosition);
        callsModelList.add(toPosition, model);
        notifyItemMoved(fromPosition, toPosition);
    }
    //Methods for search end


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mActivity).inflate(R.layout.row_calls, parent, false);
        return new CallsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        final CallsViewHolder callsViewHolder = (CallsViewHolder) holder;
        final CallsModel callsModel = callsModelList.get(position);
        try {
            String Username;
            String name = UtilsPhone.getContactName(mActivity, callsModel.getPhone());
            if (name != null) {
                Username = name;
            } else {
                Username = callsModel.getPhone();
            }

            SpannableString Message = SpannableString.valueOf(Username);
            if (SearchQuery != null) {
                int index = TextUtils.indexOf(Username.toLowerCase(), SearchQuery.toLowerCase());
                if (index >= 0) {
                    Message.setSpan(new ForegroundColorSpan(AppHelper.getColor(mActivity, R.color.colorBlueLightSearch)), index, index + SearchQuery.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                    Message.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), index, index + SearchQuery.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                }
                callsViewHolder.username.setText(Message, TextView.BufferType.SPANNABLE);
                callsViewHolder.username.setTextSize(PreferenceSettingsManager.getMessage_font_size(mActivity));
            } else {
                callsViewHolder.username.setText(Username, TextView.BufferType.NORMAL);
                callsViewHolder.username.setTextSize(PreferenceSettingsManager.getMessage_font_size(mActivity));
            }


            if (callsModel.isReceived()) {
                callsViewHolder.showIcon();
            } else {
                callsViewHolder.hideIcon();
            }
            if (callsModel.getType().equals(AppConstants.VIDEO_CALL)) {
                callsViewHolder.showVideoButton();
            } else if (callsModel.getType().equals(AppConstants.VOICE_CALL)) {
                callsViewHolder.hideVideoButton();
            }

            callsViewHolder.setUserImage(callsModel.getContactsModel().getImage(), callsModel.getContactsModel().getId());

            if (callsModel.getDate() != null) {
                DateTime callDate = UtilsTime.getCorrectDate(callsModel.getDate());
                String finalDate = UtilsTime.convertDateToString(mActivity, callDate);
                callsViewHolder.setCallDate(finalDate);
            }

            if (callsModel.getCounter() != 0 && callsModel.getCounter() > 1)
                callsViewHolder.setCallCounter(callsModel.getCounter());
            else
                callsViewHolder.counterCall.setVisibility(View.GONE);

        } catch (Exception e) {
            AppHelper.LogCat(e.getMessage());
        }
        callsViewHolder.setOnClickListener(v -> {
            switch (v.getId()) {
                case R.id.CallVideoBtn:
                    if (callsModel.isReceived())
                        CallManager.callContact(mActivity, false, true, callsModel.getFrom());
                    else
                        CallManager.callContact(mActivity, false, true, callsModel.getTo());
                    break;
                case R.id.CallBtn:
                    if (callsModel.isReceived())
                        CallManager.callContact(mActivity, false, false, callsModel.getFrom());
                    else
                        CallManager.callContact(mActivity, false, false, callsModel.getTo());
                    break;
                case R.id.user_image:
                    if (AppHelper.isAndroid5()) {
                        if (callsModel.getContactsModel().isLinked() && callsModel.getContactsModel().isActivate()) {
                            Intent mIntent = new Intent(mActivity, ProfilePreviewActivity.class);
                            mIntent.putExtra("userID", callsModel.getContactsModel().getId());
                            mIntent.putExtra("isGroup", false);
                            mActivity.startActivity(mIntent);
                        }
                    } else {
                        if (callsModel.getContactsModel().isLinked() && callsModel.getContactsModel().isActivate()) {
                            Intent mIntent = new Intent(mActivity, ProfilePreviewActivity.class);
                            mIntent.putExtra("userID", callsModel.getContactsModel().getId());
                            mActivity.startActivity(mIntent);
                            mActivity.overridePendingTransition(R.anim.push_down_in, R.anim.push_down_out);
                        }
                    }

                    break;
                default:
                    Intent mIntent = new Intent(mActivity, CallDetailsActivity.class);
                    mIntent.putExtra("userID", callsModel.getContactsModel().getId());
                    mIntent.putExtra("callID", callsModel.getId());
                    mActivity.startActivity(mIntent);
                    mActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    break;

            }
        });


    }


    @Override
    public int getItemCount() {
        if (callsModelList != null) return callsModelList.size();
        return 0;
    }


    public CallsModel getItem(int position) {
        return callsModelList.get(position);
    }


    /**
     * method to check if a  call exist
     *
     * @param callId this is the first parameter for  checkIfCallExist method
     * @param realm  this is the second parameter for  checkIfCallExist  method
     * @return return value
     */
    private boolean checkIfCallExist(int callId, Realm realm) {
        RealmQuery<CallsModel> query = realm.where(CallsModel.class).equalTo("id", callId);
        return query.count() != 0;

    }

    public void addCallItem(int conversationId) {
        try {
            Realm realm = DostChatApp.getRealmDatabaseInstance();
            CallsModel callsModel = realm.where(CallsModel.class).equalTo("id", conversationId).findFirst();
            if (!checkIfCallExist(callsModel.getId(), realm))
                addCallItem(0, callsModel);
            else
                return;
            realm.close();

        } catch (Exception e) {
            AppHelper.LogCat(e);
        }
    }

    private void addCallItem(int position, CallsModel callsModel) {
        if (position != 0) {
            try {
                this.callsModelList.add(position, callsModel);
                notifyItemInserted(position);
            } catch (Exception e) {
                AppHelper.LogCat(e);
            }
        }
    }


    public void updateCallItem(int callId) {
        try {
            Realm realm = DostChatApp.getRealmDatabaseInstance();
            int arraySize = callsModelList.size();
            for (int i = 0; i < arraySize; i++) {
                CallsModel model = callsModelList.get(i);
                if (callId == model.getId()) {
                    CallsModel callsModel = realm.where(CallsModel.class).equalTo("id", callId).findFirst();
                    changeItemAtPosition(i, callsModel);
                    if (i != 0)
                        MoveItemToPosition(i, 0);
                    break;
                }

            }
            realm.close();
        } catch (Exception e) {
            AppHelper.LogCat(e);
        }
    }

    private void changeItemAtPosition(int position, CallsModel callsModel) {
        callsModelList.set(position, callsModel);
        notifyItemChanged(position);
    }

    private void MoveItemToPosition(int fromPosition, int toPosition) {
        CallsModel model = callsModelList.remove(fromPosition);
        callsModelList.add(toPosition, model);
        notifyItemMoved(fromPosition, toPosition);
        callList.scrollToPosition(fromPosition);
    }

    public void removeCallItem(int position) {
        if (position != 0) {
            try {
                callsModelList.remove(position);
                notifyItemRemoved(position);
            } catch (Exception e) {
                AppHelper.LogCat(e);
            }
        }
    }

    public void DeleteCallItem(int callID) {
        try {
            int arraySize = callsModelList.size();
            for (int i = 0; i < arraySize; i++) {
                CallsModel model = callsModelList.get(i);
                if (model.isValid()) {
                    if (callID == model.getId()) {
                        removeCallItem(i);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            AppHelper.LogCat(e);
        }
    }

    public class CallsViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.user_image)
        ImageView userImage;
        @BindView(R.id.username)
        TextView username;
        @BindView(R.id.CallVideoBtn)
        AppCompatImageView CallVideoBtn;
        @BindView(R.id.CallBtn)
        AppCompatImageView CallBtn;
        @BindView(R.id.icon_made)
        AppCompatImageView IconMade;
        @BindView(R.id.icon_received)
        AppCompatImageView IconReceived;
        @BindView(R.id.date_call)
        TextView CallDate;
        @BindView(R.id.counter_call)
        TextView counterCall;

        public CallsViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            setTypeFaces();
        }


        private void setTypeFaces() {
            if (AppConstants.ENABLE_FONTS_TYPES) {
                counterCall.setTypeface(AppHelper.setTypeFace(mActivity, "Futura"));
                CallDate.setTypeface(AppHelper.setTypeFace(mActivity, "Futura"));
                username.setTypeface(AppHelper.setTypeFace(mActivity, "Futura"));
            }
        }


        void setUserImage(String ImageUrl, int recipientId) {
            Bitmap bitmap = ImageLoader.GetCachedBitmapImage(memoryCache, ImageUrl, mActivity, recipientId, AppConstants.USER, AppConstants.ROW_PROFILE);
            if (bitmap != null) {
                ImageLoader.SetBitmapImage(bitmap, userImage);
            } else {

                BitmapImageViewTarget target = new BitmapImageViewTarget(userImage) {
                    @Override
                    public void onResourceReady(final Bitmap bitmap, GlideAnimation anim) {
                        super.onResourceReady(bitmap, anim);
                        userImage.setImageBitmap(bitmap);

                    }

                    @Override
                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
                        super.onLoadFailed(e, errorDrawable);
                        userImage.setImageDrawable(errorDrawable);
                    }

                    @Override
                    public void onLoadStarted(Drawable placeHolderDrawable) {
                        super.onLoadStarted(placeHolderDrawable);
                        userImage.setImageDrawable(placeHolderDrawable);
                    }
                };
                DostChatImageLoader.loadCircleImage(mActivity, EndPoints.ROWS_IMAGE_URL + ImageUrl, target, R.drawable.image_holder_ur_circle, AppConstants.ROWS_IMAGE_SIZE);
            }
        }

        void hideIcon() {
            IconMade.setVisibility(View.VISIBLE);
            IconReceived.setVisibility(View.GONE);
        }

        void showIcon() {
            IconMade.setVisibility(View.GONE);
            IconReceived.setVisibility(View.VISIBLE);
        }

        void showVideoButton() {
            CallVideoBtn.setVisibility(View.VISIBLE);
            CallBtn.setVisibility(View.GONE);
        }

        void hideVideoButton() {
            CallVideoBtn.setVisibility(View.GONE);
            CallBtn.setVisibility(View.VISIBLE);
        }

        void setCallDate(String date) {
            CallDate.setText(date);
        }

        void setCallCounter(int counter) {
            counterCall.setVisibility(View.VISIBLE);
            counterCall.setText("(" + counter + ")");
        }


        void setOnClickListener(View.OnClickListener listener) {
            itemView.setOnClickListener(listener);
            userImage.setOnClickListener(listener);
            CallVideoBtn.setOnClickListener(listener);
            CallBtn.setOnClickListener(listener);
        }


    }
}
