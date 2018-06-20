package com.dostchat.dost.helpers;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.dostchat.dost.models.groups.MembersGroupModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Abderrahim El imame on 20/03/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class PreferenceManager {


    private static SharedPreferences mSharedPreferences;
    private static final String KEY_USER_PREF = "KEY_USER_PREFERENCES";


    private static final String KEY_MEMBERS_SELECTED = "KEY_MEMBERS_SELECTED";
    private static final String KEY_IS_WAITING_FOR_SMS = "KEY_IS_WAITING_FOR_SMS";
    private static final String KEY_MOBILE_NUMBER = "KEY_MOBILE_NUMBER";
    private static final String KEY_LAST_BACKUP = "KEY_LAST_BACKUP";
    private static final String KEY_VERSION_APP = "KEY_VERSION_APP";
    private static final String KEY_NEW_USER = "KEY_NEW_USER";
    private static final String KEY_WALLPAPER_USER = "KEY_WALLPAPER_USER";
    private static final String KEY_LANGUAGE = "KEY_LANGUAGE";
    private static final String KEY_APP_IS_OUT_DATE = "KEY_APP_IS_OUT_DATE";


    /**
     * method to set Language
     *
     * @param lang     this is the first parameter for setLanguage  method
     * @param mContext this is the second parameter for setLanguage  method
     * @return return value
     */
    public static boolean setLanguage(Context mContext, String lang) {
        mSharedPreferences = mContext.getSharedPreferences(KEY_USER_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(KEY_LANGUAGE, lang);
        return editor.commit();
    }

    /**
     * method to get Language
     *
     * @return return value
     */
    public static String getLanguage(Context mContext) {
        mSharedPreferences = mContext.getSharedPreferences(KEY_USER_PREF, Context.MODE_PRIVATE);
        return mSharedPreferences.getString(KEY_LANGUAGE, "");
    }

    /**
     * method to set wallpaper
     *
     * @param wallpaper this is the first parameter for setWallpaper  method
     * @param mContext  this is the second parameter for setWallpaper  method
     * @return return value
     */
    public static boolean setWallpaper(Context mContext, String wallpaper) {
        mSharedPreferences = mContext.getSharedPreferences(KEY_USER_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(KEY_WALLPAPER_USER, wallpaper);
        return editor.commit();
    }

    /**
     * method to get wallpaper
     *
     * @return return value
     */
    public static String getWallpaper(Context mContext) {
        mSharedPreferences = mContext.getSharedPreferences(KEY_USER_PREF, Context.MODE_PRIVATE);
        return mSharedPreferences.getString(KEY_WALLPAPER_USER, null);
    }

    /**
     * method to set token
     *
     * @param token    this is the first parameter for setToken  method
     * @param mContext this is the second parameter for setToken  method
     * @return return value
     */
    public static boolean setToken(Context mContext, String token) {
        mSharedPreferences = mContext.getSharedPreferences(KEY_USER_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString("token", token);
        return editor.commit();
    }

    /**
     * method to get token
     *
     * @return return value
     */
    public static String getToken(Context mContext) {
        mSharedPreferences = mContext.getSharedPreferences(KEY_USER_PREF, Context.MODE_PRIVATE);
        return mSharedPreferences.getString("token", null);
    }


    /**
     * method to setID
     *
     * @param ID this is the first parameter for setID  method
     * @return return value
     */
    public static boolean setID(Context mContext, int ID) {
        mSharedPreferences = mContext.getSharedPreferences(KEY_USER_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt("id", ID);
        return editor.commit();
    }

    /**
     * method to getID
     *
     * @return return value
     */
    public static int getID(Context mContext) {
        mSharedPreferences = mContext.getSharedPreferences(KEY_USER_PREF, Context.MODE_PRIVATE);
        return mSharedPreferences.getInt("id", 0);
    }


    /**
     * method to getPhone
     *
     * @return return value
     */
    public static String getPhone(Context mContext) {
        mSharedPreferences = mContext.getSharedPreferences(KEY_USER_PREF, Context.MODE_PRIVATE);
        return mSharedPreferences.getString("phone", null);
    }

    /**
     * method to setPhone
     *
     * @param Phone this is the first parameter for setID  method
     * @return return value
     */
    public static boolean setPhone(Context mContext, String Phone) {
        mSharedPreferences = mContext.getSharedPreferences(KEY_USER_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString("phone", Phone);
        return editor.commit();
    }

    /**
     * method to setSocketID
     *
     * @param ID this is the first parameter for setID  method
     * @return return value
     */
    public static boolean setSocketID(Context mContext, String ID) {
        mSharedPreferences = mContext.getSharedPreferences(KEY_USER_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString("socketId", ID);
        return editor.commit();
    }

    /**
     * method to getID
     *
     * @return return value
     */
    public static String getSocketID(Context mContext) {
        mSharedPreferences = mContext.getSharedPreferences(KEY_USER_PREF, Context.MODE_PRIVATE);
        return mSharedPreferences.getString("socketId", null);
    }

    /**
     * method to set contacts size
     *
     * @param size this is the first parameter for setContactSize  method
     * @return return value
     */
    public static boolean setContactSize(Context mContext, int size) {
        mSharedPreferences = mContext.getSharedPreferences(KEY_USER_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt("size", size);
        return editor.commit();
    }

    /**
     * method to get contacts size
     *
     * @return return value
     */
    public static int getContactSize(Context mContext) {
        mSharedPreferences = mContext.getSharedPreferences(KEY_USER_PREF, Context.MODE_PRIVATE);
        return mSharedPreferences.getInt("size", 0);

    }


    /**
     * method to save new members to group
     *
     * @param membersGroupModels this is the second parameter for saveMembers  method
     */
    private static void saveMembers(Context mContext, List<MembersGroupModel> membersGroupModels) {
        //SharedPreferences settings;
        // SharedPreferences.Editor editor;

        mSharedPreferences = mContext.getSharedPreferences(KEY_USER_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();

        Gson gson = new Gson();
        String jsonMembers = gson.toJson(membersGroupModels);

        editor.putString(KEY_MEMBERS_SELECTED, jsonMembers);

        editor.apply();
    }

    /**
     * method to add member
     *
     * @param membersGroupModel this is the second parameter for addMember  method
     */
    public static void addMember(Context mContext, MembersGroupModel membersGroupModel) {
        List<MembersGroupModel> membersGroupModelArrayList = getMembers(mContext);
        if (membersGroupModelArrayList == null)
            membersGroupModelArrayList = new ArrayList<MembersGroupModel>();
        membersGroupModelArrayList.add(membersGroupModel);
        saveMembers(mContext, membersGroupModelArrayList);
    }

    /**
     * method to remove member
     *
     * @param membersGroupModel this is the second parameter for removeMember  method
     */
    public static void removeMember(Context mContext, MembersGroupModel membersGroupModel) {
        ArrayList<MembersGroupModel> membersGroupModelArrayList = getMembers(mContext);
        if (membersGroupModelArrayList != null) {
            membersGroupModelArrayList.remove(membersGroupModel);
            saveMembers(mContext, membersGroupModelArrayList);
        }
    }

    /**
     * method to clear members
     */
    public static void clearMembers(Context mContext) {
        mSharedPreferences = mContext.getSharedPreferences(KEY_USER_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(KEY_MEMBERS_SELECTED, null);
        editor.apply();
    }

    /**
     * method to get all members
     *
     * @return return value
     */
    public static ArrayList<MembersGroupModel> getMembers(Context mContext) {
        try {
            List<MembersGroupModel> membersGroupModels;
            mSharedPreferences = mContext.getSharedPreferences(KEY_USER_PREF, Context.MODE_PRIVATE);
            if (mSharedPreferences.contains(KEY_MEMBERS_SELECTED)) {
                String jsonMembers = mSharedPreferences.getString(KEY_MEMBERS_SELECTED, null);
                Gson gson = new Gson();
                MembersGroupModel[] membersItems = gson.fromJson(jsonMembers, MembersGroupModel[].class);
                membersGroupModels = Arrays.asList(membersItems);
                return new ArrayList<>(membersGroupModels);
            } else {
                return null;
            }

        } catch (Exception e) {
            AppHelper.LogCat("getMembers Exception " + e.getMessage());
            return null;
        }
    }


    /**
     * method to setUnitInterstitialAdID
     *
     * @param UnitId this is the first parameter for setUnitInterstitialAdID  method
     * @return return value
     */
    public static boolean setUnitInterstitialAdID(Context mContext, String UnitId) {
        mSharedPreferences = mContext.getSharedPreferences(KEY_USER_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString("InterstitialUnitId", UnitId);
        return editor.commit();
    }

    /**
     * method to getUnitInterstitialAdID
     *
     * @return return value
     */
    public static String getUnitInterstitialAdID(Context mContext) {
        mSharedPreferences = mContext.getSharedPreferences(KEY_USER_PREF, Context.MODE_PRIVATE);
        return mSharedPreferences.getString("InterstitialUnitId", null);
    }

    /**
     * method to setShowInterstitialAds
     *
     * @param UnitId this is the first parameter for setShowInterstitialAds  method
     * @return return value
     */
    public static boolean setShowInterstitialAds(Context mContext, Boolean UnitId) {
        mSharedPreferences = mContext.getSharedPreferences(KEY_USER_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean("ShowInterstitialAds", UnitId);
        return editor.commit();
    }

    /**
     * method to ShowInterstitialrAds
     *
     * @return return value
     */
    public static Boolean ShowInterstitialrAds(Context mContext) {
        mSharedPreferences = mContext.getSharedPreferences(KEY_USER_PREF, Context.MODE_PRIVATE);
        return mSharedPreferences.getBoolean("ShowInterstitialAds", false);
    }

    /**
     * method to setUnitBannerAdsID
     *
     * @param UnitId this is the first parameter for setUnitBannerAdsID  method
     * @return return value
     */
    public static boolean setUnitBannerAdsID(Context mContext, String UnitId) {
        mSharedPreferences = mContext.getSharedPreferences(KEY_USER_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString("BannerUnitId", UnitId);
        return editor.commit();
    }

    /**
     * method to getUnitBannerAdsID
     *
     * @return return value
     */
    public static String getUnitBannerAdsID(Context mContext) {
        mSharedPreferences = mContext.getSharedPreferences(KEY_USER_PREF, Context.MODE_PRIVATE);
        return mSharedPreferences.getString("BannerUnitId", null);
    }


    /**
     * method to setShowBannerAds
     *
     * @param UnitId this is the first parameter for setShowBannerAds  method
     * @return return value
     */
    public static boolean setShowBannerAds(Context mContext, Boolean UnitId) {
        mSharedPreferences = mContext.getSharedPreferences(KEY_USER_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean("ShowBannerAds", UnitId);
        return editor.commit();
    }

    /**
     * method to ShowBannerAds
     *
     * @return return value
     */
    public static Boolean ShowBannerAds(Context mContext) {
        mSharedPreferences = mContext.getSharedPreferences(KEY_USER_PREF, Context.MODE_PRIVATE);
        return mSharedPreferences.getBoolean("ShowBannerAds", false);
    }


    /**
     * method to set user waiting for SMS (code verification)
     *
     * @param isWaiting this is parameter for setIsWaitingForSms  method
     */
    public static boolean setIsWaitingForSms(Context mContext, Boolean isWaiting) {
        mSharedPreferences = mContext.getSharedPreferences(KEY_USER_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(KEY_IS_WAITING_FOR_SMS, isWaiting);
        return editor.commit();
    }

    /**
     * method to check if user is waiting for SMS
     *
     * @return return value
     */
    public static Boolean isWaitingForSms(Context mContext) {
        mSharedPreferences = mContext.getSharedPreferences(KEY_USER_PREF, Context.MODE_PRIVATE);
        return mSharedPreferences.getBoolean(KEY_IS_WAITING_FOR_SMS, false);
    }

    /**
     * method to set mobile phone
     *
     * @param mobileNumber this is parameter for setMobileNumber  method
     */
    public static boolean setMobileNumber(Context mContext, String mobileNumber) {
        mSharedPreferences = mContext.getSharedPreferences(KEY_USER_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(KEY_MOBILE_NUMBER, mobileNumber);
        return editor.commit();
    }

    /**
     * method to get mobile phone
     *
     * @return return value
     */
    public static String getMobileNumber(Context mContext) {
        mSharedPreferences = mContext.getSharedPreferences(KEY_USER_PREF, Context.MODE_PRIVATE);
        return mSharedPreferences.getString(KEY_MOBILE_NUMBER, null);
    }


    /**
     * method to set last backup
     *
     * @param hasBackup this is parameter for setLastBackup  method
     */
    public static boolean setLastBackup(Context mContext, String hasBackup) {
        mSharedPreferences = mContext.getSharedPreferences(KEY_USER_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(KEY_LAST_BACKUP, hasBackup);
        return editor.commit();
    }

    /**
     * method to get last backup
     *
     * @return return value
     */
    public static String lastBackup(Context mContext) {
        mSharedPreferences = mContext.getSharedPreferences(KEY_USER_PREF, Context.MODE_PRIVATE);
        return mSharedPreferences.getString(KEY_LAST_BACKUP, null);
    }

    /**
     * method to set var as the user is new on the app
     *
     * @param isNew this is parameter for setIsNewUser  method
     */
    public static boolean setIsNewUser(Context mContext, Boolean isNew) {
        mSharedPreferences = mContext.getSharedPreferences(KEY_USER_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(KEY_NEW_USER, isNew);
        return editor.commit();
    }

    /**
     * method to check if user is new here the app
     *
     * @return return value
     */
    public static Boolean isNewUser(Context mContext) {
        mSharedPreferences = mContext.getSharedPreferences(KEY_USER_PREF, Context.MODE_PRIVATE);
        return mSharedPreferences.getBoolean(KEY_NEW_USER, false);
    }


    /**
     * method to set last backup
     *
     * @param version this is parameter for setLastBackup  method
     */
    public static boolean setVersionApp(Context mContext, int version) {
        mSharedPreferences = mContext.getSharedPreferences(KEY_USER_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt(KEY_VERSION_APP, version);
        return editor.commit();
    }

    /**
     * method to get last backup
     *
     * @return return value
     */
    public static int getVersionApp(Context mContext) {
        mSharedPreferences = mContext.getSharedPreferences(KEY_USER_PREF, Context.MODE_PRIVATE);
        return mSharedPreferences.getInt(KEY_VERSION_APP, 0);
    }

    /**
     * method to set the app is out date
     *
     * @param isNew this is parameter for setIsOutDate  method
     */
    public static boolean setIsOutDate(Context mContext, Boolean isNew) {
        mSharedPreferences = mContext.getSharedPreferences(KEY_USER_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(KEY_APP_IS_OUT_DATE, isNew);
        return editor.commit();
    }

    /**
     * method to check if the app is out date
     *
     * @return return value
     */
    public static Boolean isOutDate(Context mContext) {
        mSharedPreferences = mContext.getSharedPreferences(KEY_USER_PREF, Context.MODE_PRIVATE);
        return mSharedPreferences.getBoolean(KEY_APP_IS_OUT_DATE, false);
    }

    /**
     * method to check if the app is out date
     *
     * @return return value
     */
    public static void clearPreferences(Context mContext) {
        mSharedPreferences = mContext.getSharedPreferences(KEY_USER_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
}
