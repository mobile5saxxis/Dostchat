package com.dostchat.dost.helpers.images;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.Target;

import java.io.File;

import jp.wasabeef.glide.transformations.CropCircleTransformation;


/**
 * Created by Abderrahim El imame on 9/26/16.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/bencherif_el
 */

public class DostChatImageLoader {


    /*********************************************
     * ************** For users ******************
     *******************************************/
    public static void loadCircleImage(Context mContext, String file, ImageView imageView, int placeHolder, int dimens) {

        Glide.with(mContext.getApplicationContext())
                .load(file)
                .asBitmap()
                .centerCrop()
                .transform(new CropCircleTransformation(mContext))
                .placeholder(placeHolder)
                .error(placeHolder)
                .override(dimens, dimens)
                .into(imageView);
    }

    public static void loadCircleImage(Context mContext, int file, ImageView imageView, int placeHolder, int dimens) {
        Glide.with(mContext.getApplicationContext())
                .load(file)
                .asBitmap()
                .centerCrop()
                .transform(new CropCircleTransformation(mContext))
                .placeholder(placeHolder)
                .error(placeHolder)
                .override(dimens, dimens)
                .into(imageView);
    }


    public static void loadCircleImage(Context mContext, String ImageUrl, BitmapImageViewTarget target, int placeHolder, int dimens) {
        Glide.with(mContext.getApplicationContext())
                .load(ImageUrl)
                .asBitmap()
                .centerCrop()
                .transform(new CropCircleTransformation(mContext))
                .placeholder(placeHolder)
                .error(placeHolder)
                .override(dimens, dimens)
                .into(target);
    }

    public static void loadSimpleImage(Context mContext, String ImageUrl, BitmapImageViewTarget target, int dimens) {

        Glide.with(mContext.getApplicationContext())
                .load(ImageUrl)
                .asBitmap()
                .centerCrop()
                .override(dimens, dimens)
                .into(target);
    }

    public static void loadSimpleImage(Context mContext, File ImageUrl, ImageView imageView, int dimens) {
        Glide.with(mContext.getApplicationContext())
                .load(ImageUrl)
                .asBitmap()
                .centerCrop()
                .override(dimens, dimens)
                .into(imageView);
    }


    public static void loadSimpleImage(Context mContext, String ImageUrl, BitmapImageViewTarget target, Drawable placeHolder, int dimens) {

        Glide.with(mContext.getApplicationContext())
                .load(ImageUrl)
                .asBitmap()
                .centerCrop()
                .placeholder(placeHolder)
                .error(placeHolder)
                .override(dimens, dimens)
                .into(target);
    }


    /*********************************************
     * ************** For Groups ******************
     *******************************************/


    public static void loadCircleImageGroup(Context mContext, String file, ImageView imageView, int placeHolder, int dimens) {

        Glide.with(mContext.getApplicationContext())
                .load(file)
                .asBitmap()
                .centerCrop()
                .transform(new CropCircleTransformation(mContext))
                .placeholder(placeHolder)
                .error(placeHolder)
                .override(dimens, dimens)
                .into(imageView);
    }


    public static void loadCircleImageGroup(Context mContext, String ImageUrl, BitmapImageViewTarget target, int placeHolder, int dimens) {
        Glide.with(mContext.getApplicationContext())
                .load(ImageUrl)
                .asBitmap()
                .centerCrop()
                .transform(new CropCircleTransformation(mContext))
                .placeholder(placeHolder)
                .error(placeHolder)
                .override(dimens, dimens)
                .into(target);
    }

    public static void loadSimpleImageGroup(Context mContext, String ImageUrl, BitmapImageViewTarget target, Drawable placeHolder, int dimens) {
        Glide.with(mContext.getApplicationContext())
                .load(ImageUrl)
                .asBitmap()
                .centerCrop()
                .placeholder(placeHolder)
                .error(placeHolder)
                .override(dimens, dimens)
                .into(target);
    }


}
