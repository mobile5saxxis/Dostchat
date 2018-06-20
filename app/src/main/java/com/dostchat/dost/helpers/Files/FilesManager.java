package com.dostchat.dost.helpers.Files;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.webkit.MimeTypeMap;

import com.dostchat.dost.R;
import com.dostchat.dost.api.APIService;
import com.dostchat.dost.api.FilesDownloadService;
import com.dostchat.dost.app.AppConstants;
import com.dostchat.dost.app.DostChatApp;
import com.dostchat.dost.app.EndPoints;
import com.dostchat.dost.helpers.AppHelper;
import com.dostchat.dost.helpers.PreferenceManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Abderrahim El imame on 6/12/16.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/bencherif_el
 */

public class FilesManager {


    /**
     * ********************************************************************************* ************************************************
     * *************************************************** Methods to create  Files path ************************************************
     * **********************************************************************************************************************************
     */

    /**
     * method to create root  directory
     *
     * @param mContext
     * @return root directory
     */
    private static File getMainPath(Context mContext) {

        // External sdcard location
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), mContext.getApplicationContext().getString(R.string.app_name));
        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                AppHelper.LogCat("Oops! Failed create " + mContext.getApplicationContext().getString(R.string.app_name) + " directory");
                return null;
            }
        }

        return mediaStorageDir;
    }


    /* --------------------------  cached fies       ---------------------------------------*/


    /**
     * method to create cached images directory
     *
     * @param mContext
     * @return return value
     */
    public static File getImagesCachePath(Context mContext) {

        // External sdcard location
        File mediaStorageDir = new File(mContext.getCacheDir(), mContext.getApplicationContext().getString(R.string.app_name) + "_" + mContext.getApplicationContext().getString(R.string.data_cache_directory));
        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                AppHelper.LogCat("Oops! Failed create " + mContext.getApplicationContext().getString(R.string.app_name) + "_" + mContext.getApplicationContext().getString(R.string.data_cache_directory) + " directory");
                return null;
            }
        }

        return mediaStorageDir;
    }
    /* --------------------------  received fies       ---------------------------------------*/

    /**
     * method to create root images directory
     *
     * @param mContext
     * @return return value
     */
    public static File getImagesPath(Context mContext) {

        // External sdcard location
        File mediaStorageDir = new File(getMainPath(mContext), mContext.getApplicationContext().getString(R.string.app_name) + " " + mContext.getApplicationContext().getString(R.string.images_directory));
        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                AppHelper.LogCat("Oops! Failed create " + mContext.getApplicationContext().getString(R.string.app_name) + " directory");
                return null;
            }
        }

        return mediaStorageDir;
    }

    /**
     * method to create root videos directory
     *
     * @param mContext
     * @return return value
     */
    private static File getVideosPath(Context mContext) {

        // External sdcard location
        File mediaStorageDir = new File(getMainPath(mContext), mContext.getApplicationContext().getString(R.string.app_name) + " " + mContext.getApplicationContext().getString(R.string.videos_directory));
        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                AppHelper.LogCat("Oops! Failed create " + mContext.getApplicationContext().getString(R.string.app_name) + " directory");
                return null;
            }
        }

        return mediaStorageDir;
    }

    /**
     * method to create root audio directory
     *
     * @param mContext
     * @return return value
     */
    private static File getAudiosPath(Context mContext) {

        // External sdcard location
        File mediaStorageDir = new File(getMainPath(mContext), mContext.getApplicationContext().getString(R.string.app_name) + " " + mContext.getApplicationContext().getString(R.string.audios_directory));
        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                AppHelper.LogCat("Oops! Failed create " + mContext.getApplicationContext().getString(R.string.app_name) + " directory");
                return null;
            }
        }

        return mediaStorageDir;
    }

    /**
     * method to create root documents directory
     *
     * @param mContext
     * @return return value
     */
    private static File getDocumentsPath(Context mContext) {

        // External sdcard location
        File mediaStorageDir = new File(getMainPath(mContext), mContext.getApplicationContext().getString(R.string.app_name) + " " + mContext.getApplicationContext().getString(R.string.documents_directory));
        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                AppHelper.LogCat("Oops! Failed create " + mContext.getApplicationContext().getString(R.string.app_name) + " directory");
                return null;
            }
        }

        return mediaStorageDir;
    }

    /**
     * method to create root wallpaper directory
     *
     * @return return value
     */
    private static File getWallpaperPath(Context mContext) {

        // External sdcard location
        File mediaStorageDir = new File(getMainPath(mContext), mContext.getApplicationContext().getString(R.string.directory_wallpaper));
        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                AppHelper.LogCat("Oops! Failed create " + mContext.getApplicationContext().getString(R.string.app_name) + " directory");
                return null;
            }
        }

        return mediaStorageDir;
    }

    /**
     * method to create images profile directory
     *
     * @param mContext
     * @return return value
     */
    private static File getProfilePhotosPath(Context mContext) {

        // External sdcard location
        File mediaStorageDir = new File(getMainPath(mContext), mContext.getApplicationContext().getString(R.string.app_name) + " " + mContext.getApplicationContext().getString(R.string.profile_photos));
        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                AppHelper.LogCat("Oops! Failed create " + mContext.getApplicationContext().getString(R.string.app_name) + " directory");
                return null;
            }
        }

        return mediaStorageDir;
    }


        /* --------------------------  sent fies       ---------------------------------------*/

    /**
     * method to create sent images  directory
     *
     * @param mContext
     * @return return value
     */
    private static File getImagesSentPath(Context mContext) {

        // External sdcard location
        File mediaStorageDir = new File(getImagesPath(mContext), mContext.getApplicationContext().getString(R.string.app_name) + " " + mContext.getApplicationContext().getString(R.string.directory_sent));
        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                AppHelper.LogCat("Oops! Failed create " + mContext.getApplicationContext().getString(R.string.app_name) + " directory");
                return null;
            }
        }

        return mediaStorageDir;
    }

    /**
     * method to create sent videos  directory
     *
     * @param mContext
     * @return return value
     */
    private static File getVideosSentPath(Context mContext) {

        // External sdcard location
        File mediaStorageDir = new File(getVideosPath(mContext), mContext.getApplicationContext().getString(R.string.app_name) + " " + mContext.getApplicationContext().getString(R.string.directory_sent));
        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                AppHelper.LogCat("Oops! Failed create " + mContext.getApplicationContext().getString(R.string.app_name) + " directory");
                return null;
            }
        }

        return mediaStorageDir;
    }

    /**
     * method to create thumb videos  directory
     *
     * @param mContext
     * @return return value
     */
    private static File getVideosThumbnailPath(Context mContext) {

        // External sdcard location
        File mediaStorageDir = new File(getVideosPath(mContext), mContext.getApplicationContext().getString(R.string.app_name) + " " + mContext.getApplicationContext().getString(R.string.video_thumbnail));
        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                AppHelper.LogCat("Oops! Failed create " + mContext.getApplicationContext().getString(R.string.app_name) + " directory");
                return null;
            }
        }

        return mediaStorageDir;
    }

    /**
     * method to create sent audio  directory
     *
     * @param mContext
     * @return return value
     */
    private static File getAudiosSentPath(Context mContext) {

        // External sdcard location
        File mediaStorageDir = new File(getAudiosPath(mContext), mContext.getApplicationContext().getString(R.string.app_name) + " " + mContext.getApplicationContext().getString(R.string.directory_sent));
        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                AppHelper.LogCat("Oops! Failed create " + mContext.getApplicationContext().getString(R.string.app_name) + " directory");
                return null;
            }
        }

        return mediaStorageDir;
    }

    /**
     * method to create sent images  directory
     *
     * @param mContext
     * @return return value
     */
    private static File getDocumentsSentPath(Context mContext) {

        // External sdcard location
        File mediaStorageDir = new File(getDocumentsPath(mContext), mContext.getApplicationContext().getString(R.string.app_name) + " " + mContext.getApplicationContext().getString(R.string.directory_sent));
        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                AppHelper.LogCat("Oops! Failed create " + mContext.getApplicationContext().getString(R.string.app_name) + " directory");
                return null;
            }
        }

        return mediaStorageDir;
    }
    /**
     * ********************************************************************************* ************************************************
     * *************************************************** Methods to get Files absolute path string ************************************
     * **********************************************************************************************************************************
     */

    /**
     * @param mContext
     * @return Wallpaper path string
     */
    private static String getWallpaperPathString(Context mContext) {
        return String.valueOf(getWallpaperPath(mContext));
    }

        /* --------------------------  received fies       ---------------------------------------*/

    /**
     * @param mContext
     * @return Videos path string
     */
    private static String getVideosPathString(Context mContext) {
        return String.valueOf(getVideosPath(mContext));
    }

    /**
     * @param mContext
     * @return Images path string
     */
    public static String getImagesPathString(Context mContext) {
        return String.valueOf(getImagesPath(mContext));
    }

    /**
     * @param mContext
     * @return Audios path string
     */
    private static String getAudiosPathString(Context mContext) {
        return String.valueOf(getAudiosPath(mContext));
    }

    /**
     * @param mContext
     * @return Documents path string
     */
    private static String getDocumentsPathString(Context mContext) {
        return String.valueOf(getDocumentsPath(mContext));
    }

    /**
     * @param mContext
     * @return Images profile path string
     */
    public static String getProfilePhotosPathString(Context mContext) {
        return String.valueOf(getProfilePhotosPath(mContext));
    }

    /**
     * @param mContext
     * @return Images profile path string
     */
    public static String getDataCachedPathString(Context mContext) {
        return String.valueOf(getImagesCachePath(mContext));
    }
    /* --------------------------  sent fies       ---------------------------------------*/

    /**
     * @param mContext
     * @return sent Images path string
     */
    private static String getImagesSentPathString(Context mContext) {
        return String.valueOf(getImagesSentPath(mContext));
    }

    /**
     * @param mContext
     * @return sent Document path string
     */
    private static String getDocumentsSentPathString(Context mContext) {
        return String.valueOf(getDocumentsSentPath(mContext));
    }

    /**
     * @param mContext
     * @return sent Videos  path string
     */
    private static String getVideosSentPathString(Context mContext) {
        return String.valueOf(getVideosSentPath(mContext));
    }

    /**
     * @param mContext
     * @return thumbnail Videos  path string
     */
    private static String getVideosThumbnailPathString(Context mContext) {
        return String.valueOf(getVideosThumbnailPath(mContext));
    }

    /**
     * @param mContext
     * @return sent Audio path string
     */
    private static String getAudiosSentPathString(Context mContext) {
        return String.valueOf(getAudiosSentPath(mContext));
    }


/**
 * ********************************************************************************* ************************************************
 * *************************************************** Methods to Check if Files exists *********************************************
 * **********************************************************************************************************************************
 */
    /**
     * Check file if exists method
     *
     * @param Id       this is the first parameter isFilePhotoProfileExists method
     * @param mContext this is the second parameter isFilePhotoProfileExists method
     * @return Boolean
     */
    public static boolean isFilePhotoProfileExists(Context mContext, String Id) {
        File file = new File(getProfilePhotosPathString(mContext), Id);
        return file.exists();
    }

    /* --------------------------  received fies       ---------------------------------------*/

    /**
     * Check file if exists method
     *
     * @param Id       this is the first parameter isFileVideosExists method
     * @param mContext this is the second parameter isFileVideosExists method
     * @return Boolean
     */
    public static boolean isFileVideosExists(Context mContext, String Id) {
        File file = new File(getVideosPathString(mContext), Id);
        return file.exists();
    }

    /**
     * Check file if exists method
     *
     * @param Id       this is the first parameter isFileVideosExists method
     * @param mContext this is the second parameter isFileVideosExists method
     * @return Boolean
     */
    public static boolean isFileAudioExists(Context mContext, String Id) {
        File file = new File(getAudiosPathString(mContext), Id);
        return file.exists();
    }

    /**
     * Check file if exists method
     *
     * @param Path this is the first parameter isFileVideosExists method
     * @return Boolean
     */
    public static boolean isFileRecordExists(String Path) {
        File file = new File(Path);
        return file.exists();
    }

    /**
     * Check file if exists method
     *
     * @param Id       this is the first parameter isFileVideosExists method
     * @param mContext this is the second parameter isFileVideosExists method
     * @return Boolean
     */
    public static boolean isFileDocumentsExists(Context mContext, String Id) {
        File file = new File(getDocumentsPathString(mContext), Id);
        return file.exists();
    }

    /**
     * Check file if exists method
     *
     * @param Id       this is the first parameter isFileImagesExists method
     * @param mContext this is the second parameter isFileImagesExists method
     * @return Boolean
     */
    public static boolean isFileImagesExists(Context mContext, String Id) {
        File file = new File(getImagesPathString(mContext), Id);
        return file.exists();
    }

    /**
     * Check file if exists method
     *
     * @param Id       this is the first parameter isFileImagesExists method
     * @param mContext this is the second parameter isFileImagesExists method
     * @return Boolean
     */
    public static boolean isFileWallpaperExists(Context mContext, String Id) {
        File file = new File(getWallpaperPathString(mContext), Id);
        return file.exists();
    }


        /* --------------------------  sent fies       ---------------------------------------*/

    /**
     * Check file if exists method
     *
     * @param Id       this is the first parameter isFileDocumentsSentExists method
     * @param mContext this is the second parameter isFileDocumentsSentExists method
     * @return Boolean
     */
    public static boolean isFileDataCachedExists(Context mContext, String Id) {

        File file = new File(getDataCachedPathString(mContext), Id);
        return file.exists();
    }

    /**
     * Check file if exists method
     *
     * @param Id       this is the first parameter isFileDocumentsSentExists method
     * @param mContext this is the second parameter isFileDocumentsSentExists method
     * @return Boolean
     */
    public static boolean isFileDocumentsSentExists(Context mContext, String Id) {

        File file = new File(getDocumentsSentPathString(mContext), Id);
        return file.exists();
    }


    /**
     * Check file if exists method
     *
     * @param Id       this is the first parameter isFileImagesSentExists method
     * @param mContext this is the second parameter isFileImagesSentExists method
     * @return Boolean
     */
    public static boolean isFileImagesSentExists(Context mContext, String Id) {
        File file = new File(getImagesSentPathString(mContext), Id);
        return file.exists();
    }

    /**
     * Check file if exists method
     *
     * @param Id       this is the first parameter isFileAudiosSentExists method
     * @param mContext this is the second parameter isFileAudiosSentExists method
     * @return Boolean
     */
    public static boolean isFileAudiosSentExists(Context mContext, String Id) {
        File file = new File(getAudiosSentPathString(mContext), Id);
        return file.exists();
    }

    /**
     * Check file if exists method
     *
     * @param Id       this is the first parameter isFileVideosSentExists method
     * @param mContext this is the second parameter isFileVideosSentExists method
     * @return Boolean
     */
    public static boolean isFileVideosSentExists(Context mContext, String Id) {
        File file = new File(getVideosSentPathString(mContext), Id);
        return file.exists();
    }


    /**
     * ********************************************************************************* ************************************************
     * *************************************************** Methods to get Files *********************************************************
     * **********************************************************************************************************************************
     */

    /**
     * method to get sent file
     *
     * @param Identifier this is  parameter of getFileImageSent method
     * @return file
     */
    public static File getFileWallpaper(Context mContext, String Identifier) {
        return new File(getFileWallpaperPath(mContext, Identifier));
    }

    /**
     * method to get sent file
     *
     * @param Identifier this is  parameter of getFileImageSent method
     * @return file
     */
    public static File getFileDataCached(Context mContext, String Identifier) {
        return new File(getFileDataCachedPath(mContext, Identifier));
    }

    /**
     * method to get sent file
     *
     * @param Identifier this is  parameter of getFileImageSent method
     * @return file
     */
    public static File getFileProfilePhoto(Context mContext, String Identifier) {
        return new File(getFileProfilePhotoPath(mContext, Identifier));
    }

    /* --------------------------  sent fies       ---------------------------------------*/


    /**
     * method to get sent file
     *
     * @param Identifier this is  parameter of getFileImageSent method
     * @return file
     */
    public static File getFileImageSent(Context mContext, String Identifier) {
        return new File(getFileImagesSentPath(mContext, Identifier));
    }


    /**
     * method to get sent file
     *
     * @param Identifier this is  parameter of getFileVideoSent method
     * @return file
     */
    public static File getFileVideoSent(Context mContext, String Identifier) {
        return new File(getFileVideosSentPath(mContext, Identifier));
    }


    /**
     * method to get sent file
     *
     * @param Identifier this is  parameter of getFileAudioSent method
     * @return file
     */
    public static File getFileAudioSent(Context mContext, String Identifier) {
        return new File(getFileAudiosSentPath(mContext, Identifier));
    }


    /**
     * method to get sent file
     *
     * @param Identifier this is  parameter of getFileDocumentSent method
     * @return file
     */
    public static File getFileDocumentSent(Context mContext, String Identifier) {
        return new File(getFileDocumentsSentPath(mContext, Identifier));
    }

    /* --------------------------  received fies       ---------------------------------------*/

    /**
     * method to get file
     *
     * @param Identifier this is  parameter of getFileImage method
     * @return file
     */
    public static File getFileImage(Context mContext, String Identifier) {
        return new File(getFileImagesPath(mContext, Identifier));
    }

    /**
     * method to get file
     *
     * @param Identifier this is  parameter of getFileVideo method
     * @return file
     */
    public static File getFileVideo(Context mContext, String Identifier) {
        return new File(getFileVideoPath(mContext, Identifier));
    }

    /**
     * method to get file
     *
     * @param Identifier this is  parameter of getFileAudio method
     * @return file
     */
    public static File getFileAudio(Context mContext, String Identifier) {
        return new File(getFileAudioPath(mContext, Identifier));
    }

    /**
     * method to get file
     *
     * @param Path this is a parameter of getFileRecord method
     * @return file
     */
    public static File getFileRecord(String Path) {
        return new File(Path);
    }

    /**
     * method to get file
     *
     * @param Identifier this is  parameter of getFileAudio method
     * @return file
     */
    public static File getFileDocument(Context mContext, String Identifier) {
        return new File(getFileDocumentsPath(mContext, Identifier));
    }

    /**
     * ********************************************************************************* ************************************************
     * *************************************************** Methods to get Files Paths (use those methods in other classes to check the file path) **************
     * **********************************************************************************************************************************
     */

    public static String getDataCached(String Identifier) {
        return String.format("Data-%s", Identifier);
    }

    public static String getWallpaper(String Identifier) {
        return String.format("WP-%s", Identifier + ".jpg");
    }

    public static String getProfileImage(String Identifier) {
        return String.format("IMG-Profile-%s", Identifier + ".jpg");
    }

    public static String getImage(String Identifier) {
        return String.format("IMG-%s", Identifier + ".jpg");
    }

    public static String getAudio(String Identifier) {
        return String.format("AUD-%s", Identifier + ".mp3");
    }

    public static String getDocument(String Identifier) {
        return String.format("DOC-%s", Identifier + ".pdf");
    }

    public static String getVideo(String Identifier) {
        return String.format("VID-%s", Identifier + ".mp4");
    }

    public static File getFileThumbnail(Context mContext, Bitmap bmp) throws java.io.IOException {
        File file = new File(getFileThumbnailPath(mContext));
        file.getParentFile().mkdirs();
        FileOutputStream out = new FileOutputStream(file);
        bmp.compress(Bitmap.CompressFormat.JPEG, 90, out);
        out.close();
        return file;
    }

    /**
     * **************************************************************** *****************************************************************
     * *************************************************** Methods to get String Paths **************************************************
     * **********************************************************************************************************************************
     */

    /**
     * @param Identifier this is parameter of getFileImagesSentPath method
     * @return String path
     */
    public static String getFileDataCachedPath(Context mContext, String Identifier) {
        return String.format(getDataCachedPathString(mContext) + File.separator + "Data-%s", Identifier);
    }

    /**
     * @param Identifier this is parameter of getFileImagesSentPath method
     * @return String path
     */
    public static String getFileProfilePhotoPath(Context mContext, String Identifier) {
        return String.format(getProfilePhotosPathString(mContext) + File.separator + "IMG-Profile-%s", Identifier + ".jpg");
    }

    /**
     * @param Identifier this is parameter of getFileImagesSentPath method
     * @return String path
     */
    public static String getFileImagesSentPath(Context mContext, String Identifier) {
        return String.format(getImagesSentPathString(mContext) + File.separator + "IMG-%s", Identifier + ".jpg");
    }

    /**
     * @param Identifier this is parameter of getFileVideosSentPath method
     * @return String path
     */
    public static String getFileVideosSentPath(Context mContext, String Identifier) {
        return String.format(getVideosSentPathString(mContext) + File.separator + "VID-%s", Identifier + ".mp4");
    }

    /**
     * @param Identifier this is parameter of getFileImagesSentPath method
     * @return String path
     */
    public static String getFileAudiosSentPath(Context mContext, String Identifier) {
        return String.format(getAudiosSentPathString(mContext) + File.separator + "AUD-%s", Identifier + ".mp3");
    }

    /**
     * @param Identifier this is parameter of getFileImagesSentPath method
     * @return String path
     */
    public static String getFileDocumentsSentPath(Context mContext, String Identifier) {
        return String.format(getDocumentsSentPathString(mContext) + File.separator + "DOC-%s", Identifier + ".pdf");
    }


    /**
     * @param Identifier this is first parameter of getFileVideoPath method
     * @return String path
     */
    public static String getFileVideoPath(Context mContext, String Identifier) {
        return String.format(getVideosPathString(mContext) + File.separator + "VID-%s", Identifier + ".mp4");
    }

    /**
     * @param Identifier this is first parameter of getFileAudioPath method
     * @return String path
     */
    public static String getFileAudioPath(Context mContext, String Identifier) {
        return String.format(getAudiosPathString(mContext) + File.separator + "AUD-%s", Identifier + ".mp3");
    }

    /**
     * @return String path
     */
    public static String getFileRecordPath(Context mContext) {
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd-HHmmss", Locale.getDefault()).format(new Date());
        return String.format(getAudiosSentPathString(mContext) + File.separator + "record-%s", timeStamp + ".mp3");
    }

    /**
     * @param Identifier this is first parameter of getFileImagesPath method
     * @return String path
     */
    public static String getFileImagesPath(Context mContext, String Identifier) {
        return String.format(getImagesPathString(mContext) + File.separator + "IMG-%s", Identifier + ".jpg");
    }

    /**
     * @param Identifier this is first parameter of getFileWallpaperPath method
     * @return String path
     */
    public static String getFileWallpaperPath(Context mContext, String Identifier) {
        return String.format(getWallpaperPathString(mContext) + File.separator + "WP-%s", Identifier + ".jpg");
    }

    /**
     * @param Identifier this is first parameter of getFileDocumentsPath method
     * @return String path
     */
    public static String getFileDocumentsPath(Context mContext, String Identifier) {
        return String.format(getDocumentsPathString(mContext) + File.separator + "DOC-%s", Identifier + ".pdf");
    }

    /**
     * @param mContext
     * @return String path
     */
    public static String getFileThumbnailPath(Context mContext) {
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        return String.format(getVideosThumbnailPathString(mContext) + File.separator + "THUMB_%s", timeStamp + ".jpg");
    }

    /**
     * **************************************************************** *****************************************************************
     * *************************************************** Methods to get downloads files ***********************************************
     * **********************************************************************************************************************************
     */


    public static void downloadMediaFile(Context mContext, Bitmap bitmap, String Identifier, String type) {
        try {
            boolean deleted = true;
            if (isFileImagesSentExists(mContext, FilesManager.getImage(Identifier))) {
                deleted = getFileImageSent(mContext, Identifier).delete();
            } else if (isFileImagesExists(mContext, FilesManager.getImage(Identifier))) {
                deleted = getFileImage(mContext, Identifier).delete();
            }

            if (!deleted) {
                AppHelper.LogCat(" not deleted downloadMediaFile");
            } else {
                AppHelper.LogCat("deleted downloadMediaFile");
                String filePath = null;
                switch (type) {
                    case AppConstants.SENT_IMAGE:
                        filePath = getFileImagesSentPath(mContext, Identifier);
                        break;
                    case AppConstants.RECEIVED_IMAGE:
                        filePath = getFileImagesPath(mContext, Identifier);
                        break;
                    case AppConstants.PROFILE_IMAGE:
                        filePath = getFileProfilePhotoPath(mContext, Identifier);
                        break;
                }
                final String finalPath = filePath;
                Observable.create((ObservableOnSubscribe<String>) subscriber -> {
                    try {
                        FileOutputStream out = new FileOutputStream(finalPath);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                        out.close();
                        subscriber.onNext("The is saved :" + Identifier);
                        subscriber.onComplete();
                    } catch (Exception e) {
                        subscriber.onError(e);
                    }
                }).ignoreElements()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(() -> {

                        }, AppHelper::LogCat);
            }

        } catch (Exception e) {
            AppHelper.LogCat("save file Exception" + e);
        }

    }


    /**
     * method to do
     *
     * @param mContext   this is the first parameter downloadFilesToDevice method
     * @param fileUrl    this is the second parameter downloadFilesToDevice method
     * @param Identifier this is the third parameter downloadFilesToDevice method
     */
    public static void downloadFilesToDevice(Context mContext, String fileUrl, String Identifier, String type) {

        APIService apiService = new APIService(mContext);
        final FilesDownloadService downloadService = apiService.RootService(FilesDownloadService.class, PreferenceManager.getToken(mContext), EndPoints.BASE_URL);

        new AsyncTask<Void, Long, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                Call<ResponseBody> call = downloadService.downloadSmallFileSizeUrlSync(fileUrl);
                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            AppHelper.LogCat("server contacted and has file");
                            try {
                                writeResponseBodyToDisk(mContext, response.body(), Identifier, type);
                            } catch (Exception e) {
                                AppHelper.LogCat("file download was a failed");
                            }


                            //AppHelper.LogCat("file download was a success? " + writtenToDisk);
                        } else {
                            AppHelper.LogCat("server contact failed");
                        }

                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        AppHelper.LogCat("download failed " + t.getMessage());
                    }


                });

                return null;
            }
        }.execute();
    }

    /**
     * @param body       this is the first parameter writeResponseBodyToDisk method
     * @param Identifier this is the first parameter writeResponseBodyToDisk method
     * @return boolean
     */
    private static boolean writeResponseBodyToDisk(Context mContext, ResponseBody body, String Identifier, String type) {
        boolean deleted = true;
        if (isFileImagesSentExists(mContext, FilesManager.getImage(Identifier))) {
            deleted = getFileImageSent(mContext, Identifier).delete();
        } else if (isFileVideosSentExists(mContext, FilesManager.getVideo(Identifier))) {
            deleted = getFileVideoSent(mContext, Identifier).delete();
        } else if (isFileAudiosSentExists(mContext, FilesManager.getAudio(Identifier))) {
            deleted = getFileAudioSent(mContext, Identifier).delete();
        } else if (isFileDocumentsSentExists(mContext, FilesManager.getDocument(Identifier))) {
            deleted = getFileDocumentSent(mContext, Identifier).delete();
        } else if (isFileDataCachedExists(mContext, FilesManager.getDataCached(Identifier))) {
            deleted = getFileDataCached(mContext, Identifier).delete();
        }

        if (!deleted) {
            AppHelper.LogCat(" not deleted ");
            return false;
        } else {
            AppHelper.LogCat("deleted");
            File downloadedFile = null;
            switch (type) {
                case AppConstants.SENT_IMAGES:
                    downloadedFile = new File(getFileImagesSentPath(mContext, Identifier));
                    break;
                case AppConstants.SENT_AUDIO:
                    downloadedFile = new File(getFileAudiosSentPath(mContext, Identifier));
                    break;
                case AppConstants.SENT_DOCUMENTS:
                    downloadedFile = new File(getFileDocumentsSentPath(mContext, Identifier));
                    break;
                case AppConstants.SENT_VIDEOS:
                    downloadedFile = new File(getFileVideosSentPath(mContext, Identifier));
                    break;
                case AppConstants.DATA_CACHED:
                    downloadedFile = new File(getFileDataCachedPath(mContext, Identifier));
                    break;
            }

            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                byte[] fileReader = new byte[4096];

            /*long fileSize = body.contentLength();
            long fileSizeDownloaded = 0;*/

                inputStream = body.byteStream();
                try {
                    if (downloadedFile != null) {
                        outputStream = new FileOutputStream(downloadedFile);
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                while (true) {
                    int read = 0;
                    try {
                        read = inputStream.read(fileReader);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (read == -1) {
                        break;
                    }

                    try {
                        if (outputStream != null) {
                            outputStream.write(fileReader, 0, read);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                /*fileSizeDownloaded += read;*/

                /*AppHelper.LogCat("file download: " + fileSizeDownloaded + " of " + fileSize);*/
                }

                try {
                    if (outputStream != null) {
                        outputStream.flush();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return true;
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * method to get mime type of files
     *
     * @param url
     * @return
     */
    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }


    public static String getFileSize(long size) {
        if (size <= 0) return "0";
        final String[] units = new String[]{"B", "kB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }


    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri     The Uri to query.
     */
    @SuppressLint("NewApi")
    public static String getPath(final Context context, final Uri uri) {


        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    public static Uri getImageFile() {
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String imagePathStr = Environment.getExternalStorageDirectory() + "/" + Environment.DIRECTORY_DCIM + "/" + DostChatApp.getInstance().getString(R.string.app_name);

        File path = new File(imagePathStr);
        if (!path.exists()) {
            path.mkdir();
        }

        String finalPhotoName = "IMG_" + timeStamp + ".jpg";

        File photo = new File(path, finalPhotoName);
        return Uri.fromFile(photo);
    }

    public static Uri getVideoFile() {
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String imagePathStr = Environment.getExternalStorageDirectory() + "/" + Environment.DIRECTORY_DCIM + "/" + DostChatApp.getInstance().getString(R.string.app_name);

        File path = new File(imagePathStr);
        if (!path.exists()) {
            path.mkdir();
        }

        String finalPhotoName = "VID_" + timeStamp + ".mp4";

        File photo = new File(path, finalPhotoName);
        return Uri.fromFile(photo);
    }


    public static List<File> getListFiles(File parentDir) {
        ArrayList<File> inFiles = new ArrayList<File>();
        File[] files = parentDir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                inFiles.addAll(getListFiles(file));
            } else {
                if (file.getName().endsWith(".jpg")) { //change to your image extension
                    inFiles.add(file);
                }
            }
        }
        return inFiles;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    private static String getDataColumn(Context context, Uri uri, String selection,
                                        String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static void copyFile(File src, File dst) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(src);
        FileOutputStream fileOutputStream = new FileOutputStream(dst);
        byte[] var4 = new byte[1024];

        int var5;
        while ((var5 = fileInputStream.read(var4)) > 0) {
            fileOutputStream.write(var4, 0, var5);
        }
        fileInputStream.close();
        fileOutputStream.close();
    }


}
