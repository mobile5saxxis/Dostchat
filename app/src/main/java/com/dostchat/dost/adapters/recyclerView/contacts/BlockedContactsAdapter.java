package com.dostchat.dost.adapters.recyclerView.contacts;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.dostchat.dost.R;
import com.dostchat.dost.app.AppConstants;
import com.dostchat.dost.app.DostChatApp;
import com.dostchat.dost.app.EndPoints;
import com.dostchat.dost.helpers.AppHelper;
import com.dostchat.dost.helpers.Files.cache.ImageLoader;
import com.dostchat.dost.helpers.Files.cache.MemoryCache;
import com.dostchat.dost.helpers.UtilsPhone;
import com.dostchat.dost.helpers.UtilsString;
import com.dostchat.dost.helpers.images.DostChatImageLoader;
import com.dostchat.dost.models.users.contacts.UsersBlockModel;
import com.dostchat.dost.ui.RecyclerViewFastScroller;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import hani.momanii.supernova_emoji_library.Helper.EmojiconTextView;
import io.realm.Realm;


/**
 * Created by Abderrahim El imame on 20/02/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class BlockedContactsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements RecyclerViewFastScroller.BubbleTextGetter {
    private final Activity mActivity;
    private List<UsersBlockModel> mContactsModel;
    private MemoryCache memoryCache;
    private int userId;

    public void setContacts(List<UsersBlockModel> contactsModelList) {
        this.mContactsModel = contactsModelList;
        notifyDataSetChanged();
    }

    public BlockedContactsAdapter(@NonNull Activity mActivity, List<UsersBlockModel> mContactsModel) {
        this.mActivity = mActivity;
        this.mContactsModel = mContactsModel;
        this.memoryCache = new MemoryCache();
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mActivity).inflate(R.layout.row_contacts, parent, false);
        return new ContactsViewHolder(itemView);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ContactsViewHolder) {
            final ContactsViewHolder contactsViewHolder = (ContactsViewHolder) holder;
            final UsersBlockModel usersBlockModel = this.mContactsModel.get(position);
            try {
                contactsViewHolder.setUsername(usersBlockModel.getContactsModel().getPhone());


                if (usersBlockModel.getContactsModel().getStatus() != null) {
                    contactsViewHolder.setStatus(usersBlockModel.getContactsModel().getStatus());
                } else {
                    contactsViewHolder.setStatus(usersBlockModel.getContactsModel().getPhone());
                }

                if (usersBlockModel.getContactsModel().isLinked()) {
                    contactsViewHolder.hideInviteButton();
                } else {
                    contactsViewHolder.showInviteButton();
                }
                contactsViewHolder.setUserImage(usersBlockModel.getContactsModel().getImage(), usersBlockModel.getId());

            } catch (Exception e) {
                AppHelper.LogCat("" + e.getMessage());
            }

        }

    }


    @Override
    public int getItemCount() {
        return mContactsModel.size() > 0 ? mContactsModel.size() : 0;
    }

    @Override
    public String getTextToShowInBubble(int pos) {
        try {
            return mContactsModel.size() > pos ? Character.toString(mContactsModel.get(pos).getContactsModel().getUsername().charAt(0)) : null;
        } catch (Exception e) {
            AppHelper.LogCat(e.getMessage());
            return e.getMessage();
        }
    }


    public class ContactsViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.user_image)
        ImageView userImage;
        @BindView(R.id.username)
        TextView username;
        @BindView(R.id.status)
        EmojiconTextView status;
        @BindView(R.id.invite)
        TextView invite;

        ContactsViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            setTypeFaces();
            itemView.setOnClickListener(view -> {
                UsersBlockModel usersBlockModel = mContactsModel.get(getAdapterPosition());
                userId = usersBlockModel.getContactsModel().getId();
                //delete popup
                Realm realmUnblock = DostChatApp.getRealmDatabaseInstance();
                AlertDialog.Builder builderUnblock = new AlertDialog.Builder(mActivity);
                builderUnblock.setMessage(R.string.unblock_user_make_sure);
                builderUnblock.setPositiveButton(R.string.Yes, (dialog, whichButton) -> {
                    realmUnblock.executeTransactionAsync(realm1 -> {
                        UsersBlockModel usersBlockModel2 = realm1.where(UsersBlockModel.class).equalTo("contactsModel.id", userId).findFirst();
                        usersBlockModel2.deleteFromRealm();
                    }, () -> {
                        AppHelper.LogCat("unBlock user success");
                        notifyDataSetChanged();
                    }, error -> {
                        AppHelper.LogCat("unBlock user" + error.getMessage());

                    });


                });

                builderUnblock.setNegativeButton(R.string.No, (dialog, whichButton) -> {

                });

                builderUnblock.show();
                if (!realmUnblock.isClosed())
                    realmUnblock.close();
            });
        }

        private void setTypeFaces() {
            if (AppConstants.ENABLE_FONTS_TYPES) {
                status.setTypeface(AppHelper.setTypeFace(mActivity, "Futura"));
                username.setTypeface(AppHelper.setTypeFace(mActivity, "Futura"));
                invite.setTypeface(AppHelper.setTypeFace(mActivity, "Futura"));
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
                        ImageLoader.DownloadImage(memoryCache, EndPoints.ROWS_IMAGE_URL + ImageUrl, ImageUrl, mActivity, recipientId, AppConstants.USER, AppConstants.ROW_PROFILE);


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


        void hideInviteButton() {
            invite.setVisibility(View.GONE);
        }

        void showInviteButton() {
            invite.setVisibility(View.VISIBLE);
        }

        void setUsername(String phone) {
            String name = UtilsPhone.getContactName(mActivity, phone);
            if (name != null) {
                username.setText(name);
            } else {
                username.setText(phone);
            }

        }

        void setStatus(String Status) {
            String user = UtilsString.unescapeJava(Status);
            if (user.length() > 18)
                status.setText(user.substring(0, 18) + "... " + "");
            else
                status.setText(user);
        }


    }
}
