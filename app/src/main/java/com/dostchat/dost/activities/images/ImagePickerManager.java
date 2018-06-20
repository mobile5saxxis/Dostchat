package com.dostchat.dost.activities.images;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;

import com.dostchat.dost.R;

/**
 * Created by Abderrahim El imame on 1/11/17.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */

public class ImagePickerManager extends PickerManager {

    public ImagePickerManager(Activity activity) {
        super(activity);
    }

    protected void sendToExternalApp() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");

        activity.startActivityForResult(Intent.createChooser(intent, activity.getString(R.string.select_picture)),
                REQUEST_CODE_SELECT_IMAGE);
    }

    @Override
    public void setUri(Uri uri) {
        mProcessingPhotoUri = uri;
    }

}