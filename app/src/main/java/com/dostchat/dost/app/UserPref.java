package com.dostchat.dost.app;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by saxxis25 on 3/25/2017.
 */

public class UserPref {
    private String TAG = UserPref.class.getSimpleName();

    // Shared Preferences
    private SharedPreferences mPref;

    // Editor for Shared preferences
    private SharedPreferences.Editor mEditor;

    // Context
    private Context _context;

    // Shared pref mode
    private int PRIVATE_MODE = 0;

    // Sharedpref file name
    private static final String PREF_NAME = "ichat_user";

    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_NAME = "name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_MN = "mobile_number";
    private static final String KEY_IMAGE_PATH = "image_path";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_SESSION_ID = "session_id";
    private static final String KEY_LOGGED_IN = "isLogged";
    private static final String KEY_WALLET_AMOUNT = "walletamount";
    private static final String KEY_UNIQUE_ID = "uniqueid";

    private static final String KEY_REFFERAL_ID = "refferalId";


    public UserPref(Context context) {
        this._context = context;
        mPref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        mEditor = mPref.edit();
    }

    public boolean isLoggedIn(){
        return mPref.getBoolean(KEY_LOGGED_IN,false);
    }

    public void setLoggedIn(){
        mEditor.putBoolean(KEY_LOGGED_IN,true);
        mEditor.commit();
    }

    public void setUserId(String UserId) {
        mEditor.putString(KEY_USER_ID,UserId);
        mEditor.commit();
    }

    public void setName(String name){
        mEditor.putString(KEY_NAME,name);
        mEditor.commit();
    }

    public String getName(){
        return mPref.getString(KEY_NAME,"empty");
    }

    public void setEmail(String id){
        mEditor.putString(KEY_EMAIL,id);
        mEditor.commit();
    }

    public String getEmail(){
        return mPref.getString(KEY_EMAIL,"empty");
    }

    public void setMobileNumber(String id){
        mEditor.putString(KEY_MN,id);
        mEditor.commit();
    }

    public String getMobileNumber(){
        return mPref.getString(KEY_MN,"empty");
    }

    public void setImagePath(String id){
        mEditor.putString(KEY_IMAGE_PATH,id);
        mEditor.commit();
    }

    public String getImagePath(){
        return mPref.getString(KEY_IMAGE_PATH,"empty");
    }

    public String getUserId(){
        return mPref.getString(KEY_USER_ID,"empty");
    }

    public String getUserName(){
        return mPref.getString(KEY_USER_NAME,"empty");
    }

    public void setSessionId(String id){
        mEditor.putString(KEY_SESSION_ID,id);
        mEditor.commit();
    }

    public String getSessionId(){
        return mPref.getString(KEY_SESSION_ID,"empty");
    }

    public void setWalletAmount(int amount){
        mEditor.putInt(KEY_WALLET_AMOUNT,amount);
        mEditor.commit();
    }

    public int getWalletAmount(){
        return mPref.getInt(KEY_WALLET_AMOUNT,-1);
    }

    public void setReferralId(String id ){
        mEditor.putString(KEY_REFFERAL_ID,id);
        mEditor.commit();
    }

    public String getReferralId(){
        return mPref.getString(KEY_REFFERAL_ID,"empty");
    }

    public void setUniqueId(String id ){
        mEditor.putString(KEY_UNIQUE_ID,id);
        mEditor.commit();
    }

    public String getUniqueId(){
        return mPref.getString(KEY_UNIQUE_ID,"empty");
    }

    public void logoutUser(){
        mEditor.clear();
        mEditor.commit();

    }


}
