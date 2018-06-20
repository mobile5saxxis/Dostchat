package com.dostchat.dost.adapters.recyclerView;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dostchat.dost.R;
import com.dostchat.dost.activities.popups.StatusDelete;
import com.dostchat.dost.app.AppConstants;
import com.dostchat.dost.helpers.AppHelper;
import com.dostchat.dost.helpers.UtilsString;
import com.dostchat.dost.models.users.status.StatusModel;
import com.dostchat.dost.presenters.users.StatusPresenter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import hani.momanii.supernova_emoji_library.Helper.EmojiconTextView;


/**
 * Created by Abderrahim El imame on 28/04/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class StatusAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Activity mActivity;
    private List<StatusModel> mStatusModel;
    private StatusPresenter statusPresenter;

    public void setStatus(List<StatusModel> statusModelList) {
        this.mStatusModel = statusModelList;
        notifyDataSetChanged();
    }


    public StatusAdapter(@NonNull Activity mActivity, List<StatusModel> mStatusModel, StatusPresenter statusPresenter) {
        this.mActivity = mActivity;
        this.mStatusModel = mStatusModel;
        this.statusPresenter = statusPresenter;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mActivity).inflate(R.layout.row_status, parent, false);
        return new StatusViewHolder(itemView);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        StatusViewHolder statusViewHolder = (StatusViewHolder) holder;
        StatusModel statusModel = mStatusModel.get(position);
        try {


            if (statusModel.getStatus() != null) {

                statusViewHolder.setStatus(statusModel.getStatus());
            }


        } catch (Exception e) {
            AppHelper.LogCat("" + e.getMessage());
        }
        statusViewHolder.setOnLongClickListener(v -> {
            Intent mIntent = new Intent(mActivity, StatusDelete.class);
            mIntent.putExtra("statusID", statusModel.getId());
            mIntent.putExtra("status", statusModel.getStatus());
            mActivity.startActivity(mIntent);
            return true;
        });
        statusViewHolder.setOnClickListener(v -> statusPresenter.UpdateCurrentStatus(statusModel.getStatus(), statusModel.getId()));

    }

    private void removeStatusItem(int position) {
        if (position != 0) {
            try {
                mStatusModel.remove(position);
                notifyItemRemoved(position);
            } catch (Exception e) {
                AppHelper.LogCat(e);
            }
        }
    }

    public void DeleteStatusItem(int StatusID) {
        try {
            int arraySize = mStatusModel.size();
            for (int i = 0; i < arraySize; i++) {
                StatusModel model = mStatusModel.get(i);
                if (model.isValid()) {
                    if (StatusID == model.getId()) {
                        removeStatusItem(i);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            AppHelper.LogCat(e);
        }
    }

    @Override
    public int getItemCount() {
        if (mStatusModel != null)
            return mStatusModel.size();
        else
            return 0;
    }

    class StatusViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.status)
        EmojiconTextView status;

        StatusViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            setTypeFaces();
        }


        private void setTypeFaces() {
            if (AppConstants.ENABLE_FONTS_TYPES) {
                status.setTypeface(AppHelper.setTypeFace(mActivity, "Futura"));
            }
        }

        void setStatus(String Status) {
            String finalStatus = UtilsString.unescapeJava(Status);
            if (finalStatus.length() > 27)
                status.setText(finalStatus.substring(0, 27) + "... " + "");
            else
                status.setText(finalStatus);
        }

        void setStatusColorCurrent() {
            status.setTextColor(mActivity.getResources().getColor(R.color.colorBlueLight));
        }

        void setStatusColor() {
            status.setTextColor(mActivity.getResources().getColor(R.color.colorBlack));
        }

        void setOnClickListener(View.OnClickListener listener) {
            itemView.setOnClickListener(listener);

        }

        void setOnLongClickListener(View.OnLongClickListener listener) {
            itemView.setOnLongClickListener(listener);

        }
    }


}
