package com.dostchat.dost.activities.media;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.dostchat.dost.R;
import com.dostchat.dost.app.AppConstants;
import com.dostchat.dost.app.EndPoints;
import com.dostchat.dost.helpers.AppHelper;
import com.dostchat.dost.helpers.Files.FilesManager;
import com.dostchat.dost.helpers.images.ImageUtils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jp.wasabeef.picasso.transformations.BlurTransformation;

/**
 * Created by Abderrahim El imame on 9/28/16.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/bencherif_el
 */

public class ImagePreviewActivity extends AppCompatActivity {


    @BindView(R.id.image_file)
    ImageView imageView;
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;

    private String ImageType;
    private String Identifier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        if (AppHelper.isAndroid5()) {
            getWindow().setStatusBarColor(Color.BLACK);
        }
        setContentView(R.layout.activity_image_preview);
        ButterKnife.bind(this);
        if (getIntent().getExtras() != null) {
            ImageType = getIntent().getExtras().getString("ImageType");
            Identifier = getIntent().getExtras().getString("Identifier");
            boolean saveIntent = getIntent().getExtras().getBoolean("SaveIntent");
            if (saveIntent) {
                getImage(ImageType, Identifier, Identifier, true);
            } else {
                getImage(ImageType, Identifier, Identifier, false);
            }

        }

    }

    @SuppressWarnings("unused")
    @OnClick(R.id.backBtn)
    void back() {
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.shareBtn)
    void ShareContent() {
        switch (ImageType) {
            case AppConstants.SENT_IMAGE:
                startActivity(ImageUtils.getNativeShareIntent(this, Identifier, AppConstants.SENT_IMAGE));
                break;
            case AppConstants.RECEIVED_IMAGE:
                startActivity(ImageUtils.getNativeShareIntent(this, Identifier, AppConstants.RECEIVED_IMAGE));
                break;
            case AppConstants.PROFILE_IMAGE:
                startActivity(ImageUtils.getNativeShareIntent(this, Identifier, AppConstants.PROFILE_IMAGE));
                break;
            case AppConstants.SENT_IMAGE_FROM_SERVER:
                startActivity(ImageUtils.getNativeShareIntent(this, Identifier, AppConstants.SENT_IMAGE_FROM_SERVER));
                break;
            case AppConstants.RECEIVED_IMAGE_FROM_SERVER:
                startActivity(ImageUtils.getNativeShareIntent(this, Identifier, AppConstants.RECEIVED_IMAGE_FROM_SERVER));
                break;
            case AppConstants.PROFILE_IMAGE_FROM_SERVER:
                startActivity(ImageUtils.getNativeShareIntent(this, Identifier, AppConstants.PROFILE_IMAGE_FROM_SERVER));
                break;
        }

    }


    private void getImage(String ImageType, String ImageUrl, String ImageUrlHolder, boolean forSave) {
        progressBar.setVisibility(View.VISIBLE);
        String ImageUrlFinal;
        String ImageUrlHolderFinal;

        File fileUrl;
        switch (ImageType) {
            case AppConstants.SENT_IMAGE:
                fileUrl = FilesManager.getFileImageSent(this, ImageUrl);
                Picasso.with(this)
                        .load(fileUrl)
                        .transform(new BlurTransformation(getApplicationContext(), AppConstants.BLUR_RADIUS))
                        .centerCrop()
                        .placeholder(R.drawable.image_holder_full_screen)
                        .error(R.drawable.image_holder_full_screen)
                        .resize(AppConstants.PROFILE_PREVIEW_BLUR_IMAGE_SIZE, AppConstants.PROFILE_PREVIEW_BLUR_IMAGE_SIZE)
                        .into(imageView, new Callback() {
                            @Override
                            public void onSuccess() {
                                AppHelper.LogCat("onSuccess");
                                Picasso.with(ImagePreviewActivity.this)
                                        .load(fileUrl)
                                        .placeholder(R.drawable.image_holder_full_screen)
                                        .error(R.drawable.image_holder_full_screen)
                                        .into(imageView, new Callback() {
                                            @Override
                                            public void onSuccess() {
                                                progressBar.setVisibility(View.GONE);
                                            }

                                            @Override
                                            public void onError() {
                                                progressBar.setVisibility(View.GONE);
                                            }
                                        });
                            }

                            @Override
                            public void onError() {
                                progressBar.setVisibility(View.GONE);
                                AppHelper.LogCat("onError ");
                            }
                        });

                break;
            case AppConstants.RECEIVED_IMAGE:
                fileUrl = FilesManager.getFileImage(this, ImageUrl);
                Picasso.with(this)
                        .load(fileUrl)
                        .transform(new BlurTransformation(getApplicationContext(), AppConstants.BLUR_RADIUS))
                        .centerCrop()
                        .placeholder(R.drawable.image_holder_full_screen)
                        .error(R.drawable.image_holder_full_screen)
                        .resize(AppConstants.PROFILE_PREVIEW_BLUR_IMAGE_SIZE, AppConstants.PROFILE_PREVIEW_BLUR_IMAGE_SIZE)
                        .into(imageView, new Callback() {
                            @Override
                            public void onSuccess() {
                                AppHelper.LogCat("onSuccess");
                                Picasso.with(ImagePreviewActivity.this)
                                        .load(fileUrl)
                                        .placeholder(R.drawable.image_holder_full_screen)
                                        .error(R.drawable.image_holder_full_screen)
                                        .into(imageView, new Callback() {
                                            @Override
                                            public void onSuccess() {
                                                progressBar.setVisibility(View.GONE);
                                            }

                                            @Override
                                            public void onError() {
                                                progressBar.setVisibility(View.GONE);
                                            }
                                        });


                            }

                            @Override
                            public void onError() {
                                progressBar.setVisibility(View.GONE);
                                AppHelper.LogCat("onError ");
                            }
                        });
                break;
            case AppConstants.PROFILE_IMAGE:
                fileUrl = FilesManager.getFileProfilePhoto(this, ImageUrl);
                Picasso.with(this)
                        .load(fileUrl)
                        .transform(new BlurTransformation(getApplicationContext(), AppConstants.BLUR_RADIUS))
                        .centerCrop()
                        .placeholder(R.drawable.image_holder_full_screen)
                        .error(R.drawable.image_holder_full_screen)
                        .resize(AppConstants.PROFILE_PREVIEW_BLUR_IMAGE_SIZE, AppConstants.PROFILE_PREVIEW_BLUR_IMAGE_SIZE)
                        .into(imageView, new Callback() {
                            @Override
                            public void onSuccess() {

                                AppHelper.LogCat("onSuccess");
                                Picasso.with(ImagePreviewActivity.this)
                                        .load(fileUrl)
                                        .placeholder(R.drawable.image_holder_full_screen)
                                        .error(R.drawable.image_holder_full_screen)
                                        .into(imageView, new Callback() {
                                            @Override
                                            public void onSuccess() {
                                                progressBar.setVisibility(View.GONE);
                                            }

                                            @Override
                                            public void onError() {
                                                progressBar.setVisibility(View.GONE);
                                            }
                                        });

                            }

                            @Override
                            public void onError() {
                                progressBar.setVisibility(View.GONE);
                                AppHelper.LogCat("onError ");
                            }
                        });
                break;
            case AppConstants.SENT_IMAGE_FROM_SERVER:

                ImageUrlHolderFinal = EndPoints.MESSAGE_HOLDER_IMAGE_URL + ImageUrlHolder;
                ImageUrlFinal = EndPoints.MESSAGE_IMAGE_URL + ImageUrl;

                Picasso.with(this)
                        .load(ImageUrlHolderFinal)
                        .transform(new BlurTransformation(getApplicationContext(), AppConstants.BLUR_RADIUS))
                        .centerCrop()
                        .placeholder(R.drawable.image_holder_full_screen)
                        .error(R.drawable.image_holder_full_screen)
                        .resize(AppConstants.PROFILE_PREVIEW_BLUR_IMAGE_SIZE, AppConstants.PROFILE_PREVIEW_BLUR_IMAGE_SIZE)
                        .into(imageView, new Callback() {
                            @Override
                            public void onSuccess() {

                                AppHelper.LogCat("onSuccess");
                                Picasso.with(ImagePreviewActivity.this)
                                        .load(ImageUrlFinal)
                                        .placeholder(R.drawable.image_holder_full_screen)
                                        .error(R.drawable.image_holder_full_screen)
                                        .into(imageView, new Callback() {
                                            @Override
                                            public void onSuccess() {
                                                progressBar.setVisibility(View.GONE);
                                            }

                                            @Override
                                            public void onError() {
                                                progressBar.setVisibility(View.GONE);
                                            }
                                        });
                                if (forSave) {
                                    new Handler().postDelayed(() -> {
                                        Drawable drawable = imageView.getDrawable();
                                        Bitmap bitmap;
                                        if (drawable instanceof BitmapDrawable) {
                                            bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                                        } else {
                                            bitmap = null;
                                        }
                                        FilesManager.downloadMediaFile(ImagePreviewActivity.this, bitmap, Identifier, AppConstants.SENT_IMAGE);
                                        AppHelper.CustomToast(ImagePreviewActivity.this, getString(R.string.image_saved));
                                        finish();
                                    }, 1000);
                                }
                            }

                            @Override
                            public void onError() {
                                progressBar.setVisibility(View.GONE);
                                AppHelper.LogCat("onError ");
                            }
                        });
                break;
            case AppConstants.RECEIVED_IMAGE_FROM_SERVER:

                ImageUrlHolderFinal = EndPoints.MESSAGE_HOLDER_IMAGE_URL + ImageUrlHolder;
                ImageUrlFinal = EndPoints.MESSAGE_IMAGE_URL + ImageUrl;
                Picasso.with(this)
                        .load(ImageUrlHolderFinal)
                        .transform(new BlurTransformation(getApplicationContext(), AppConstants.BLUR_RADIUS))
                        .centerCrop()
                        .placeholder(R.drawable.image_holder_full_screen)
                        .error(R.drawable.image_holder_full_screen)
                        .resize(AppConstants.PROFILE_PREVIEW_BLUR_IMAGE_SIZE, AppConstants.PROFILE_PREVIEW_BLUR_IMAGE_SIZE)
                        .into(imageView, new Callback() {
                            @Override
                            public void onSuccess() {

                                AppHelper.LogCat("onSuccess");
                                Picasso.with(ImagePreviewActivity.this)
                                        .load(ImageUrlFinal)
                                        .placeholder(R.drawable.image_holder_full_screen)
                                        .error(R.drawable.image_holder_full_screen)
                                        .into(imageView, new Callback() {
                                            @Override
                                            public void onSuccess() {
                                                progressBar.setVisibility(View.GONE);
                                            }

                                            @Override
                                            public void onError() {
                                                progressBar.setVisibility(View.GONE);
                                            }
                                        });
                                if (forSave) {
                                    new Handler().postDelayed(() -> {
                                        Drawable drawable = imageView.getDrawable();
                                        Bitmap bitmap;
                                        if (drawable instanceof BitmapDrawable) {
                                            bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                                        } else {
                                            bitmap = null;
                                        }
                                        FilesManager.downloadMediaFile(ImagePreviewActivity.this, bitmap, Identifier, AppConstants.RECEIVED_IMAGE);
                                        AppHelper.CustomToast(ImagePreviewActivity.this, getString(R.string.image_saved));
                                        finish();
                                    }, 1000);
                                }
                            }

                            @Override
                            public void onError() {
                                progressBar.setVisibility(View.GONE);
                                AppHelper.LogCat("onError ");
                            }
                        });
                break;
            case AppConstants.PROFILE_IMAGE_FROM_SERVER:

                ImageUrlHolderFinal = EndPoints.PROFILE_PREVIEW_IMAGE_URL + ImageUrlHolder;
                ImageUrlFinal = EndPoints.PROFILE_IMAGE_URL + ImageUrl;
                Picasso.with(this)
                        .load(ImageUrlHolderFinal)
                        .transform(new BlurTransformation(getApplicationContext(), AppConstants.BLUR_RADIUS))
                        .centerCrop()
                        .placeholder(R.drawable.image_holder_full_screen)
                        .error(R.drawable.image_holder_full_screen)
                        .resize(AppConstants.PROFILE_PREVIEW_BLUR_IMAGE_SIZE, AppConstants.PROFILE_PREVIEW_BLUR_IMAGE_SIZE)
                        .into(imageView, new Callback() {
                            @Override
                            public void onSuccess() {

                                AppHelper.LogCat("onSuccess");
                                Picasso.with(ImagePreviewActivity.this)
                                        .load(ImageUrlFinal)
                                        .placeholder(R.drawable.image_holder_full_screen)
                                        .error(R.drawable.image_holder_full_screen)
                                        .into(imageView, new Callback() {
                                            @Override
                                            public void onSuccess() {
                                                progressBar.setVisibility(View.GONE);
                                            }

                                            @Override
                                            public void onError() {
                                                progressBar.setVisibility(View.GONE);
                                            }
                                        });
                                if (forSave) {
                                    new Handler().postDelayed(() -> {
                                        Drawable drawable = imageView.getDrawable();
                                        Bitmap bitmap;
                                        if (drawable instanceof BitmapDrawable) {
                                            bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                                        } else {
                                            bitmap = null;
                                        }
                                        FilesManager.downloadMediaFile(ImagePreviewActivity.this, bitmap, Identifier, AppConstants.PROFILE_IMAGE);
                                        AppHelper.CustomToast(ImagePreviewActivity.this, getString(R.string.image_saved));
                                        finish();
                                    }, 1000);
                                }
                            }

                            @Override
                            public void onError() {
                                progressBar.setVisibility(View.GONE);
                                AppHelper.LogCat("onError ");
                            }
                        });
                break;
            default:

                break;
        }

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
