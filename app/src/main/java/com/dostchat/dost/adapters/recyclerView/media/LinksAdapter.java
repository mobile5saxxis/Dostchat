package com.dostchat.dost.adapters.recyclerView.media;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.leocardz.link.preview.library.LinkPreviewCallback;
import com.leocardz.link.preview.library.SourceContent;
import com.leocardz.link.preview.library.TextCrawler;
import com.dostchat.dost.R;
import com.dostchat.dost.app.AppConstants;
import com.dostchat.dost.helpers.AppHelper;
import com.dostchat.dost.helpers.UtilsString;
import com.dostchat.dost.helpers.images.DostChatImageLoader;
import com.dostchat.dost.models.messages.MessagesModel;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Abderrahim El imame on 11/03/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class LinksAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Activity mActivity;
    private List<MessagesModel> mMessagesModel;
    private LayoutInflater mInflater;

    public LinksAdapter(Activity mActivity) {
        this.mActivity = mActivity;
        mInflater = LayoutInflater.from(mActivity);
    }

    public void setMessages(List<MessagesModel> mMessagesList) {
        this.mMessagesModel = mMessagesList;
        notifyDataSetChanged();
    }


    public List<MessagesModel> getMessages() {
        return mMessagesModel;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.row_links, parent, false);
        return new LinksViewHolder(view);

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final LinksViewHolder linksViewHolder = (LinksViewHolder) holder;
        final MessagesModel messagesModel = this.mMessagesModel.get(position);
        try {
            if (messagesModel.getMessage() != null) {
                if (UtilsString.checkForUrls(messagesModel.getMessage())) {
                    String url = UtilsString.getUrl(messagesModel.getMessage());
                    if (url != null)
                        if (!url.startsWith("http://")) {
                            if (!url.startsWith("https://")) {
                                url = (new StringBuilder()).append("http://").append(url).toString();
                            }
                        }
                    AppHelper.LogCat(" valid " + url);
                    linksViewHolder.textCrawler.makePreview(linksViewHolder.linkPreviewCallback, url);


                }
            }

        } catch (Exception e) {
            AppHelper.LogCat("" + e.getMessage());
        }

    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        if (mMessagesModel != null) {
            return mMessagesModel.size();
        } else {
            return 0;
        }
    }


    public class LinksViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.image_preview)
        ImageView imagePreview;

        @BindView(R.id.title_link)
        TextView titleLink;

        @BindView(R.id.description)
        TextView descriptionLink;

        @BindView(R.id.url)
        TextView urlLink;


        LinkPreviewCallback linkPreviewCallback;
        TextCrawler textCrawler;


        LinksViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            setTypeFaces();
            itemView.setOnClickListener(this);
            textCrawler = new TextCrawler();
            linkPreviewCallback = new LinkPreviewCallback() {
                @Override
                public void onPre() {
                    //   AppHelper.LogCat("onPre");
                }

                @Override
                public void onPos(SourceContent sourceContent, boolean b) {

                    MessagesModel messagesModel = mMessagesModel.get(getAdapterPosition());
                    if (messagesModel.getMessage() != null) {
                        if (UtilsString.checkForUrls(messagesModel.getMessage())) {


                            titleLink.setText(sourceContent.getTitle());
                            urlLink.setText(sourceContent.getUrl());

                            if (sourceContent.getDescription().length() > 80) {
                                descriptionLink.setText(sourceContent.getDescription().substring(0, 80) + "... " + "");
                            } else {
                                descriptionLink.setText(sourceContent.getDescription());
                            }


                            if (sourceContent.getImages().size() > 0) {
                                BitmapImageViewTarget target = new BitmapImageViewTarget(imagePreview) {
                                    @Override
                                    public void onResourceReady(final Bitmap bitmap, GlideAnimation anim) {
                                        super.onResourceReady(bitmap, anim);
                                        imagePreview.setImageBitmap(bitmap);
                                    }

                                    @Override
                                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
                                        super.onLoadFailed(e, errorDrawable);
                                        imagePreview.setImageDrawable(errorDrawable);
                                    }

                                    @Override
                                    public void onLoadStarted(Drawable placeHolderDrawable) {
                                        super.onLoadStarted(placeHolderDrawable);
                                        imagePreview.setImageDrawable(placeHolderDrawable);
                                    }
                                };

                                DostChatImageLoader.loadSimpleImage(mActivity, sourceContent.getImages().get(0), target, null, AppConstants.MESSAGE_IMAGE_SIZE);

                            }
                        }
                    }
                }
            };
        }


        private void setTypeFaces() {
            if (AppConstants.ENABLE_FONTS_TYPES) {
                titleLink.setTypeface(AppHelper.setTypeFace(mActivity, "Futura"));
                descriptionLink.setTypeface(AppHelper.setTypeFace(mActivity, "Futura"));
                urlLink.setTypeface(AppHelper.setTypeFace(mActivity, "Futura"));
            }
        }

        @Override
        public void onClick(View view) {
            MessagesModel messagesModel = mMessagesModel.get(getAdapterPosition());
            String url = UtilsString.getUrl(messagesModel.getMessage());
            if (url != null)
                if (!url.startsWith("http://")) {
                    if (!url.startsWith("https://")) {
                        url = (new StringBuilder()).append("http://").append(url).toString();
                    }
                }

            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            mActivity.startActivity(intent);
        }
    }
}

