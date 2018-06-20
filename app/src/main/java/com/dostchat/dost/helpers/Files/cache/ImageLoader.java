package com.dostchat.dost.helpers.Files.cache;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.Build;
import android.os.Handler;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.widget.ImageView;

import com.dostchat.dost.app.AppConstants;
import com.dostchat.dost.helpers.AppHelper;
import com.dostchat.dost.helpers.Files.FilesManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by Abderrahim El imame on 10/31/16.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */

public class ImageLoader {


    /**
     * method to get bitmap image from memory cache or disk cache
     *
     * @param dataFile  this is the first parameter for   GetCachedBitmapImage  method
     * @param mActivity this is the second parameter for   GetCachedBitmapImage  method
     * @param profileId this is the thirded  parameter for   GetCachedBitmapImage  method
     * @param isGroup   this is the fourth parameter for   GetCachedBitmapImage  method
     * @return return value
     */
    public static Bitmap GetCachedBitmapImage(MemoryCache memoryCache, String dataFile, Context mActivity, int profileId, String isGroup, String type) {
        String key = dataFile + "" + isGroup + "" + profileId + "" + type;

        if (memoryCache.isExist(key)) {
            Bitmap bitmap = memoryCache.get(key);
            if (bitmap != null)
                return bitmap;
            else
                return null;
        } else if (FilesManager.isFileDataCachedExists(mActivity, FilesManager.getDataCached(key))) {
            File file;
            file = FilesManager.getFileDataCached(mActivity, key);
            Bitmap bitmap;
            bitmap = DecodeFile(file);
            if (bitmap != null)
                return bitmap;
            else
                return null;

        } else if (FilesManager.isFileWallpaperExists(mActivity, FilesManager.getWallpaper(key))) {
            File file;
            file = FilesManager.getFileWallpaper(mActivity, key);
            Bitmap bitmap;
            bitmap = DecodeFile(file);
            if (bitmap != null)
                return bitmap;
            else
                return null;

        } else {
            return null;
        }
    }

    /**
     * method to download bitmap image to memory cache and disk cache
     *
     * @param memoryCache
     * @param ImageUrl    this is the first parameter for   DownloadImage  method
     * @param dataFile    this is the second parameter for   DownloadImage  method
     * @param mActivity   this is the thirded  parameter for   DownloadImage  method
     * @param profileId   this is the fourth parameter for   DownloadImage  method
     * @param isGroup     this is the fifth  parameter for   DownloadImage  method
     */
    public static void DownloadImage(MemoryCache memoryCache, String ImageUrl, String dataFile, Activity mActivity, int profileId, String isGroup, String type) {


        String key = dataFile + "" + isGroup + "" + profileId + "" + type;
        try {
            FilesManager.downloadFilesToDevice(mActivity, ImageUrl, key, AppConstants.DATA_CACHED);
        } catch (Throwable ex) {
            ex.printStackTrace();
            if (ex instanceof OutOfMemoryError)
                memoryCache.clear();
        }

        new Handler().postDelayed(() -> {
            File file = null;
            Bitmap bitmap = null;
            if (FilesManager.isFileDataCachedExists(mActivity, FilesManager.getDataCached(key))) {
                file = FilesManager.getFileDataCached(mActivity, key);
            }
            if (file != null)
                bitmap = DecodeFile(file);
            if (bitmap != null)
                memoryCache.put(key, bitmap);
        }, 500);

    }

    /**
     * method to download bitmap image to memory cache and disk cache
     *
     * @param memoryCache
     * @param ImageUrl    this is the first parameter for   DownloadOfflineImage  method
     * @param dataFile    this is the second parameter for   DownloadOfflineImage  method
     * @param mActivity   this is the thirded  parameter for   DownloadOfflineImage  method
     * @param profileId   this is the fourth parameter for   DownloadOfflineImage  method
     * @param isGroup     this is the fifth  parameter for   DownloadOfflineImage  method
     */
    public static void DownloadOfflineImage(MemoryCache memoryCache, File ImageUrl, String dataFile, Activity mActivity, int profileId, String isGroup, String type) {
        String key = dataFile + "" + isGroup + "" + profileId + "" + type;
        try {
            if (type.equals(AppConstants.ROW_WALLPAPER)) {
                FilesManager.copyFile(ImageUrl, FilesManager.getFileWallpaper(mActivity, key));
            } else {
                FilesManager.copyFile(ImageUrl, FilesManager.getFileDataCached(mActivity, key));
            }

        } catch (Throwable ex) {
            ex.printStackTrace();
            if (ex instanceof OutOfMemoryError)
                memoryCache.clear();
        }

        if (type.equals(AppConstants.ROW_WALLPAPER)) {
            new Handler().postDelayed(() -> {
                File file = null;
                Bitmap bitmap = null;
                if (FilesManager.isFileWallpaperExists(mActivity, FilesManager.getWallpaper(key))) {
                    file = FilesManager.getFileWallpaper(mActivity, key);
                }
                if (file != null)
                    bitmap = DecodeFile(file);
                if (bitmap != null)
                    memoryCache.put(key, bitmap);
            }, 500);
        } else {
            new Handler().postDelayed(() -> {
                File file = null;
                Bitmap bitmap = null;
                if (FilesManager.isFileDataCachedExists(mActivity, FilesManager.getDataCached(key))) {
                    file = FilesManager.getFileDataCached(mActivity, key);
                }
                if (file != null)
                    bitmap = DecodeFile(file);
                if (bitmap != null)
                    memoryCache.put(key, bitmap);
            }, 500);
        }
    }

    /**
     * method to decode files
     *
     * @param file parameter for  DecodeFile method
     * @return return value
     */
    private static Bitmap DecodeFile(File file) {
        try {
            //decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            FileInputStream stream1 = new FileInputStream(file);
            BitmapFactory.decodeStream(stream1, null, o);
            stream1.close();

            //Find the correct scale value. It should be the power of 2.
            final int REQUIRED_SIZE = 1024;//Increase its value to get quality image but remember it should be the power of 2.
            int width_tmp = o.outWidth, height_tmp = o.outHeight;
            int scale = 1;
            while (true) {
                if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE)
                    break;
                width_tmp /= 2;
                height_tmp /= 2;
                scale *= 2;
            }

            //decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            FileInputStream stream2 = new FileInputStream(file);
            Bitmap bitmap = BitmapFactory.decodeStream(stream2, null, o2);
            stream2.close();
            return bitmap;

        } catch (FileNotFoundException e) {
            AppHelper.LogCat("FileNotFoundException " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Throwable ex) {
            ex.printStackTrace();
            if (ex instanceof OutOfMemoryError)
                return null;
        }
        return null;
    }


    /**
     * method to set a circular bitmap image
     *
     * @param bitmap    this is the first parameter for  SetBitmapImage  method
     * @param imageView this is the second parameter for  SetBitmapImage method
     */
    public static void SetBitmapImage(Bitmap bitmap, ImageView imageView) {
        Bitmap circleBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        BitmapShader shader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        Paint paint = new Paint();
        paint.setShader(shader);
        paint.setAntiAlias(true);
        Canvas c = new Canvas(circleBitmap);
        c.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2, bitmap.getWidth() / 2, paint);
        imageView.setImageBitmap(circleBitmap);
    }

    //Set the radius of the Blur. Supported range 0 < radius <= 25
    private static final float BLUR_RADIUS = 10f;


    public static Bitmap BlurBitmap(Bitmap image, Context mContext) {
        if (null == image) return null;
        if (!AppHelper.isJelly17()) return null;
        Bitmap outputBitmap = Bitmap.createBitmap(image);
        final RenderScript renderScript = RenderScript.create(mContext);
        Allocation tmpIn = Allocation.createFromBitmap(renderScript, image);
        Allocation tmpOut = Allocation.createFromBitmap(renderScript, outputBitmap);

        ScriptIntrinsicBlur theIntrinsic = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript));
        theIntrinsic.setRadius(BLUR_RADIUS);
        theIntrinsic.setInput(tmpIn);
        theIntrinsic.forEach(tmpOut);
        tmpOut.copyTo(outputBitmap);
        return outputBitmap;
    }

}
