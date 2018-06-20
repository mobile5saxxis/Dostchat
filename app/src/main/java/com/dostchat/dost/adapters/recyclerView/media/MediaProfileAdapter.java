package com.dostchat.dost.adapters.recyclerView.media;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.dostchat.dost.R;
import com.dostchat.dost.app.AppConstants;
import com.dostchat.dost.app.EndPoints;
import com.dostchat.dost.helpers.AppHelper;
import com.dostchat.dost.helpers.Files.FilesManager;
import com.dostchat.dost.helpers.Files.cache.ImageLoader;
import com.dostchat.dost.helpers.Files.cache.MemoryCache;
import com.dostchat.dost.helpers.PreferenceManager;
import com.dostchat.dost.helpers.images.DostChatImageLoader;
import com.dostchat.dost.models.messages.MessagesModel;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Abderrahim El imame on 11/03/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class MediaProfileAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Activity mActivity;
    private List<MessagesModel> mMessagesModel;
    private LayoutInflater mInflater;
    private MemoryCache memoryCache;

    public MediaProfileAdapter(Activity mActivity) {
        this.mActivity = mActivity;
        mInflater = LayoutInflater.from(mActivity);
        this.memoryCache = new MemoryCache();
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
        View view = mInflater.inflate(R.layout.row_media_profile, parent, false);
        return new MediaProfileViewHolder(view);

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final MediaProfileViewHolder mediaProfileViewHolder = (MediaProfileViewHolder) holder;
        final MessagesModel messagesModel = this.mMessagesModel.get(position);
        try {
            if (messagesModel.getImageFile() != null && !messagesModel.getImageFile().equals("null")) {
                mediaProfileViewHolder.imageFile.setVisibility(View.VISIBLE);
                mediaProfileViewHolder.setImage(messagesModel);
            } else {
                mediaProfileViewHolder.imageFile.setVisibility(View.GONE);
            }

            if (messagesModel.getAudioFile() != null && !messagesModel.getAudioFile().equals("null")) {
                mediaProfileViewHolder.mediaAudio.setVisibility(View.VISIBLE);
            } else {
                mediaProfileViewHolder.mediaAudio.setVisibility(View.GONE);
            }
            if (messagesModel.getDocumentFile() != null && !messagesModel.getDocumentFile().equals("null")) {
                mediaProfileViewHolder.mediaDocument.setVisibility(View.VISIBLE);
            } else {
                mediaProfileViewHolder.mediaDocument.setVisibility(View.GONE);
            }

            if (messagesModel.getVideoFile() != null && !messagesModel.getVideoFile().equals("null")) {
                mediaProfileViewHolder.mediaVideo.setVisibility(View.VISIBLE);
                mediaProfileViewHolder.setMediaVideoThumbnail(messagesModel);
            } else {
                mediaProfileViewHolder.mediaVideo.setVisibility(View.GONE);
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

    public class MediaProfileViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.media_image)
        ImageView imageFile;
        @BindView(R.id.media_audio)
        ImageView mediaAudio;
        @BindView(R.id.media_document)
        ImageView mediaDocument;
        @BindView(R.id.media_video_thumbnail)
        ImageView mediaVideoThumbnail;
        @BindView(R.id.media_video)
        FrameLayout mediaVideo;
        @BindView(R.id.play_btn_video)
        ImageButton playVideo;


        MediaProfileViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            imageFile.setOnClickListener(this);
            mediaVideo.setOnClickListener(this);
            mediaAudio.setOnClickListener(this);
            mediaDocument.setOnClickListener(this);
            playVideo.setOnClickListener(this);

        }


        void setImage(MessagesModel messagesModel) {
            if (messagesModel.getSenderID() == PreferenceManager.getID(mActivity)) {
                Bitmap bitmap = ImageLoader.GetCachedBitmapImage(memoryCache, messagesModel.getImageFile(), mActivity, messagesModel.getId(), AppConstants.USER, AppConstants.ROW_MESSAGES_AFTER);
                if (bitmap != null) {
                    imageFile.setImageBitmap(bitmap);
                } else {
                    if (FilesManager.isFileImagesSentExists(mActivity, FilesManager.getImage(messagesModel.getImageFile()))) {
                        DostChatImageLoader.loadSimpleImage(mActivity, FilesManager.getFileImageSent(mActivity, messagesModel.getImageFile()), imageFile, AppConstants.ROWS_IMAGE_SIZE);
                    } else {
                        BitmapImageViewTarget target = new BitmapImageViewTarget(imageFile) {
                            @Override
                            public void onResourceReady(final Bitmap bitmap, GlideAnimation anim) {
                                super.onResourceReady(bitmap, anim);
                                imageFile.setImageBitmap(bitmap);
                            }

                            @Override
                            public void onLoadFailed(Exception e, Drawable errorDrawable) {
                                super.onLoadFailed(e, errorDrawable);
                                imageFile.setImageDrawable(errorDrawable);
                            }

                            @Override
                            public void onLoadStarted(Drawable placeholder) {
                                super.onLoadStarted(placeholder);
                                imageFile.setImageDrawable(placeholder);
                            }
                        };

                        DostChatImageLoader.loadSimpleImage(mActivity, EndPoints.MESSAGE_IMAGE_URL + messagesModel.getImageFile(), target, AppConstants.ROWS_IMAGE_SIZE);

                    }
                }
            } else {
                Bitmap bitmap = ImageLoader.GetCachedBitmapImage(memoryCache, messagesModel.getImageFile(), mActivity, messagesModel.getId(), AppConstants.USER, AppConstants.ROW_MESSAGES_AFTER);
                if (bitmap != null) {
                    imageFile.setImageBitmap(bitmap);
                } else {
                    if (FilesManager.isFileImagesExists(mActivity, FilesManager.getImage(messagesModel.getImageFile()))) {
                        DostChatImageLoader.loadSimpleImage(mActivity, FilesManager.getFileImage(mActivity, messagesModel.getImageFile()), imageFile, AppConstants.ROWS_IMAGE_SIZE);
                    } else {


                        BitmapImageViewTarget target = new BitmapImageViewTarget(imageFile) {
                            @Override
                            public void onResourceReady(final Bitmap bitmap, GlideAnimation anim) {
                                super.onResourceReady(bitmap, anim);
                                imageFile.setImageBitmap(bitmap);
                            }

                            @Override
                            public void onLoadFailed(Exception e, Drawable errorDrawable) {
                                super.onLoadFailed(e, errorDrawable);
                                imageFile.setImageDrawable(errorDrawable);
                            }

                            @Override
                            public void onLoadStarted(Drawable placeholder) {
                                super.onLoadStarted(placeholder);
                                imageFile.setImageDrawable(placeholder);
                            }
                        };
                        DostChatImageLoader.loadSimpleImage(mActivity, EndPoints.MESSAGE_IMAGE_URL + messagesModel.getImageFile(), target, AppConstants.ROWS_IMAGE_SIZE);
                    }
                }
            }


        }


        void setMediaVideoThumbnail(MessagesModel messagesModel) {

            if (messagesModel.getSenderID() == PreferenceManager.getID(mActivity)) {
                Bitmap bitmap = ImageLoader.GetCachedBitmapImage(memoryCache, messagesModel.getVideoThumbnailFile(), mActivity, messagesModel.getId(), AppConstants.USER, AppConstants.ROW_MESSAGES_AFTER);
                if (bitmap != null) {
                    mediaVideoThumbnail.setImageBitmap(bitmap);
                } else {
                    if (FilesManager.isFileImagesSentExists(mActivity, FilesManager.getImage(messagesModel.getVideoThumbnailFile()))) {
                        DostChatImageLoader.loadSimpleImage(mActivity, FilesManager.getFileImageSent(mActivity, messagesModel.getVideoThumbnailFile()), mediaVideoThumbnail, AppConstants.ROWS_IMAGE_SIZE);
                    } else {

                        BitmapImageViewTarget target = new BitmapImageViewTarget(mediaVideoThumbnail) {
                            @Override
                            public void onResourceReady(final Bitmap bitmap, GlideAnimation anim) {
                                super.onResourceReady(bitmap, anim);
                                mediaVideoThumbnail.setImageBitmap(bitmap);
                                FilesManager.downloadMediaFile(mActivity, bitmap, messagesModel.getVideoThumbnailFile(), AppConstants.SENT_IMAGE);

                            }

                            @Override
                            public void onLoadFailed(Exception e, Drawable errorDrawable) {
                                super.onLoadFailed(e, errorDrawable);
                                mediaVideoThumbnail.setImageDrawable(errorDrawable);
                            }

                            @Override
                            public void onLoadStarted(Drawable placeholder) {
                                super.onLoadStarted(placeholder);
                                mediaVideoThumbnail.setImageDrawable(placeholder);
                            }
                        };
                        DostChatImageLoader.loadSimpleImage(mActivity, EndPoints.MESSAGE_VIDEO_THUMBNAIL_URL + messagesModel.getVideoThumbnailFile(), target, AppConstants.ROWS_IMAGE_SIZE);
                    }
                }
            } else {
                Bitmap bitmap = ImageLoader.GetCachedBitmapImage(memoryCache, messagesModel.getVideoThumbnailFile(), mActivity, messagesModel.getId(), AppConstants.USER, AppConstants.ROW_MESSAGES_AFTER);
                if (bitmap != null) {
                    mediaVideoThumbnail.setImageBitmap(bitmap);
                } else {
                    if (FilesManager.isFileImagesExists(mActivity, FilesManager.getImage(messagesModel.getVideoThumbnailFile()))) {
                        DostChatImageLoader.loadSimpleImage(mActivity, FilesManager.getFileImage(mActivity, messagesModel.getVideoThumbnailFile()), mediaVideoThumbnail, AppConstants.ROWS_IMAGE_SIZE);
                    } else {

                        BitmapImageViewTarget target = new BitmapImageViewTarget(mediaVideoThumbnail) {
                            @Override
                            public void onResourceReady(final Bitmap bitmap, GlideAnimation anim) {
                                super.onResourceReady(bitmap, anim);
                                mediaVideoThumbnail.setImageBitmap(bitmap);
                                FilesManager.downloadMediaFile(mActivity, bitmap, messagesModel.getVideoThumbnailFile(), AppConstants.RECEIVED_IMAGE);

                            }

                            @Override
                            public void onLoadFailed(Exception e, Drawable errorDrawable) {
                                super.onLoadFailed(e, errorDrawable);
                                mediaVideoThumbnail.setImageDrawable(errorDrawable);
                            }

                            @Override
                            public void onLoadStarted(Drawable placeholder) {
                                super.onLoadStarted(placeholder);
                                mediaVideoThumbnail.setImageDrawable(placeholder);
                            }
                        };
                        DostChatImageLoader.loadSimpleImage(mActivity, EndPoints.MESSAGE_VIDEO_THUMBNAIL_URL + messagesModel.getVideoThumbnailFile(), target, AppConstants.ROWS_IMAGE_SIZE);
                    }
                }
            }

        }


        @Override
        public void onClick(View view) {
            MessagesModel messagesModel = mMessagesModel.get(getAdapterPosition());
            switch (view.getId()) {
                case R.id.media_audio:
                    playingAudio(messagesModel);
                    break;

                case R.id.media_video:
                    playingVideo(messagesModel);
                    break;
                case R.id.play_btn_video:
                    playingVideo(messagesModel);
                    break;

                case R.id.media_document:
                    if (messagesModel.getSenderID() == PreferenceManager.getID(mActivity)) {
                        if (FilesManager.isFileDocumentsSentExists(mActivity, FilesManager.getDocument(messagesModel.getDocumentFile()))) {
                            openDocument(FilesManager.getFileDocumentSent(mActivity, messagesModel.getDocumentFile()));
                        } else {
                            File file = new File(EndPoints.MESSAGE_DOCUMENT_URL + messagesModel.getDocumentFile());
                            openDocument(file);
                        }
                    } else {
                        if (FilesManager.isFileDocumentsExists(mActivity, FilesManager.getDocument(messagesModel.getDocumentFile()))) {
                            openDocument(FilesManager.getFileDocument(mActivity, messagesModel.getDocumentFile()));
                        } else {
                            File file = new File(EndPoints.MESSAGE_DOCUMENT_URL + messagesModel.getDocumentFile());
                            openDocument(file);
                        }
                    }

                    break;

                case R.id.media_image:
                    showImage(messagesModel);
                    break;
            }

        }

    }

    private void playingVideo(MessagesModel messagesModel) {
        String video = messagesModel.getVideoFile();

        if (messagesModel.getSenderID() == PreferenceManager.getID(mActivity)) {

            if (FilesManager.isFileVideosSentExists(mActivity, FilesManager.getVideo(video))) {
                AppHelper.LaunchVideoPreviewActivity(mActivity, video, true);
            } else {
                AppHelper.CustomToast(mActivity, mActivity.getString(R.string.this_video_is_not_exist));
            }
        } else {

            if (FilesManager.isFileVideosExists(mActivity, FilesManager.getVideo(video))) {
                AppHelper.LaunchVideoPreviewActivity(mActivity, video, false);
            } else {
                AppHelper.CustomToast(mActivity, mActivity.getString(R.string.this_video_is_not_exist));
            }
        }
    }

    private void showImage(MessagesModel messagesModel) {
        if (messagesModel.getSenderID() == PreferenceManager.getID(mActivity)) {

            if (FilesManager.isFileImagesSentExists(mActivity, FilesManager.getImage(messagesModel.getImageFile()))) {
                AppHelper.LaunchImagePreviewActivity(mActivity, AppConstants.SENT_IMAGE, messagesModel.getImageFile());
            } else {
                if (messagesModel.getImageFile() != null)
                    AppHelper.LaunchImagePreviewActivity(mActivity, AppConstants.SENT_IMAGE_FROM_SERVER, messagesModel.getImageFile());
            }
        } else {

            if (FilesManager.isFileImagesExists(mActivity, FilesManager.getImage(messagesModel.getImageFile()))) {
                AppHelper.LaunchImagePreviewActivity(mActivity, AppConstants.RECEIVED_IMAGE, messagesModel.getImageFile());

            } else {
                if (messagesModel.getImageFile() != null)
                    AppHelper.LaunchImagePreviewActivity(mActivity, AppConstants.RECEIVED_IMAGE_FROM_SERVER, messagesModel.getImageFile());
            }
        }
    }

    private void playingAudio(MessagesModel messagesModel) {
        String audioFile = messagesModel.getAudioFile();

        if (messagesModel.getSenderID() == PreferenceManager.getID(mActivity)) {

            if (FilesManager.isFileAudiosSentExists(mActivity, FilesManager.getAudio(audioFile))) {
                File fileAudio = FilesManager.getFileAudioSent(mActivity, audioFile);
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
                Uri data = Uri.fromFile(fileAudio);
                intent.setDataAndType(data, "audio/*");
                try {
                    mActivity.startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    AppHelper.CustomToast(mActivity, mActivity.getString(R.string.no_app_to_play_audio));
                }

            } else {
                AppHelper.CustomToast(mActivity, mActivity.getString(R.string.this_audio_is_not_exist));
            }
        } else {

            if (FilesManager.isFileAudioExists(mActivity, FilesManager.getAudio(audioFile))) {
                File fileAudio = FilesManager.getFileAudio(mActivity, audioFile);
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
                Uri data = Uri.fromFile(fileAudio);
                intent.setDataAndType(data, "audio/*");
                try {
                    mActivity.startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    AppHelper.CustomToast(mActivity, mActivity.getString(R.string.no_app_to_play_audio));
                }

            } else {
                AppHelper.CustomToast(mActivity, mActivity.getString(R.string.this_audio_is_not_exist));
            }
        }

    }

    private void openDocument(File file) {
        if (file.exists()) {
            Uri path = Uri.fromFile(file);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(path, "application/pdf");
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            try {
                mActivity.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                AppHelper.CustomToast(mActivity, mActivity.getString(R.string.no_application_to_view_pdf));
            }
        }
    }

}

