package com.dostchat.dost.helpers;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.dostchat.dost.interfaces.ContactMobileNumbQuery;
import com.dostchat.dost.models.users.contacts.ContactsModel;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by Abderrahim El imame on 03/03/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class UtilsPhone {


    private static ArrayList<ContactsModel> mListContacts = new ArrayList<ContactsModel>();
    private static PhoneNumberUtil mPhoneUtil = PhoneNumberUtil.getInstance();

    /**
     * method to retrieve all contacts from the book
     *
     * @return return value
     */
    public static ArrayList<ContactsModel> GetPhoneContacts(Context mContext) {
        ContentResolver contentResolver = mContext.getApplicationContext().getContentResolver();
        Cursor cur = contentResolver.query(ContactMobileNumbQuery.CONTENT_URI, ContactMobileNumbQuery.PROJECTION, ContactMobileNumbQuery.SELECTION, null, ContactMobileNumbQuery.SORT_ORDER);
        if (cur != null) {
            if (cur.getCount() > 0) {
                while (cur.moveToNext()) {
                    ContactsModel contactsModel = new ContactsModel();
                    String name = cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    String phoneNumber = cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    String id = cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID));
                    String image_uri = cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI));


                    //     AppHelper.LogCat("number phone --> " + phoneNumber);
                    if (name.contains("\\s+")) {
                        String[] nameArr = name.split("\\s+");
                        contactsModel.setUsername(nameArr[0] + nameArr[1]);
                        // AppHelper.LogCat("Fname --> " + nameArr[0]);
                        // AppHelper.LogCat("Lname --> " + nameArr[1]);
                    } else {
                        contactsModel.setUsername(name);
                        //AppHelper.LogCat("name" + name);
                    }
                    if (phoneNumber != null) {

                        String Regex = "[^\\d]";
                        String PhoneDigits = phoneNumber.replaceAll(Regex, "");
                        boolean isValid = !(PhoneDigits.length() < 6 || PhoneDigits.length() > 13);
                        String phNumberProto = PhoneDigits.replaceAll("-", "");
                        String PhoneNo;
                        if (PhoneDigits.length() != 10) {
                            PhoneNo = "+";
                            PhoneNo = PhoneNo.concat(phNumberProto);
                        } else {
                            PhoneNo = phNumberProto;
                        }
                        // AppHelper.LogCat("phoneNumber --> " + phoneNumber);
                        String phoneNumberTmpFinal;
                        Phonenumber.PhoneNumber phoneNumberInter = getPhoneNumber(phoneNumber);
                        if (phoneNumberInter != null) {
                            //  AppHelper.LogCat("phoneNumberInter --> " + phoneNumberInter.getNationalNumber());
                            phoneNumberTmpFinal = String.valueOf(phoneNumberInter.getNationalNumber());

                            // AppHelper.LogCat("phoneNumberTmpFinal --> " + phoneNumberTmpFinal);
                            if (isValid) {
                                //    AppHelper.LogCat("PhoneNo --> " + PhoneNo);
                                contactsModel.setPhoneTmp(phoneNumberTmpFinal);
                                contactsModel.setPhone(PhoneNo.trim());
                                contactsModel.setContactID(Integer.parseInt(id));
                                contactsModel.setImage(image_uri);

                                int flag = 0;
                                int arraySize = mListContacts.size();
                                if (arraySize == 0) {
                                    mListContacts.add(contactsModel);
                                }
                                //remove duplicate numbers
                                for (int i = 0; i < arraySize; i++) {

                                    if (!mListContacts.get(i).getPhone().trim().equals(PhoneNo.trim())) {
                                        flag = 1;

                                    } else {
                                        flag = 0;
                                        break;
                                    }
                                }

                                if (flag == 1) {
                                    mListContacts.add(contactsModel);
                                }


                            } else {
                                //   AppHelper.LogCat("invalid phone --> ");
                            }
                        }


                    }
                }
                cur.close();
            }
        }
        return mListContacts;
    }

    /**
     * Check if number is valid
     *
     * @return boolean
     */
    @SuppressWarnings("unused")
    public static boolean isValid(String phone) {
        Phonenumber.PhoneNumber phoneNumber = getPhoneNumber(phone);
        return phoneNumber != null && mPhoneUtil.isValidNumber(phoneNumber);
    }

    /**
     * Get PhoneNumber object
     *
     * @return PhoneNumber | null on error
     */
    @SuppressWarnings("unused")
    public static Phonenumber.PhoneNumber getPhoneNumber(String phone) {
        final String DEFAULT_COUNTRY = Locale.getDefault().getCountry();
        try {
            return mPhoneUtil.parse(phone, DEFAULT_COUNTRY);
        } catch (NumberParseException ignored) {
            return null;
        }
    }

    /**
     * method to get contact ID
     *
     * @param mContext this is the first parameter for getContactID  method
     * @param phone    this is the second parameter for getContactID  method
     * @return return value
     */
    public static long getContactID(Activity mContext, String phone) {
        if (PermissionHandler.checkPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            AppHelper.LogCat("Read contact data permission already granted.");
            // CONTENT_FILTER_URI allow to search contact by phone number
            Uri lookupUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phone));
            // This query will return NAME and ID of contact, associated with phone //number.
            Cursor mcursor = mContext.getContentResolver().query(lookupUri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup._ID}, null, null, null);
            //Now retrieve _ID from query result
            long idPhone = 0;
            try {
                if (mcursor != null) {
                    if (mcursor.moveToFirst()) {
                        idPhone = Long.valueOf(mcursor.getString(mcursor.getColumnIndex(ContactsContract.PhoneLookup._ID)));
                    }
                }
            } finally {
                mcursor.close();
            }
            return idPhone;
        } else {
            AppHelper.LogCat("Please request Read contact data permission.");
            PermissionHandler.requestPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE);
            return 0;
        }

    }


    /**
     * method to check for contact name
     *
     * @param mContext this is the first parameter for getContactName  method
     * @param phone    this is the second parameter for getContactName  method
     * @return return value
     */
    public static String getContactName(Context mContext, String phone) {
        try {

            // CONTENT_FILTER_URI allow to search contact by phone number
            Uri lookupUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phone));
            // This query will return NAME and ID of contact, associated with phone //number.
            Cursor mcursor = mContext.getContentResolver().query(lookupUri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup._ID}, null, null, null);
            //Now retrieve _ID from query result
            String name = null;
            try {
                if (mcursor != null) {
                    if (mcursor.moveToFirst()) {
                        name = mcursor.getString(mcursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
                    }
                }
            } finally {
                mcursor.close();
            }
            return name;
        } catch (Exception e) {
            return null;
        }

    }

    /**
     * method to check if user contact exist
     *
     * @param phone this is the second parameter for checkIfContactExist  method
     * @return return value
     */
    public static boolean checkIfContactExist(Context mContext, String phone) {
        try {
            // CONTENT_FILTER_URI allow to search contact by phone number
            Uri lookupUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phone));
            // This query will return NAME and ID of contact, associated with phone //number.
            Cursor mcursor = mContext.getApplicationContext().getContentResolver().query(lookupUri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup._ID}, null, null, null);
            //Now retrieve _ID from query result
            String name = null;
            try {
                if (mcursor != null) {
                    if (mcursor.moveToFirst()) {
                        name = mcursor.getString(mcursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
                    }
                }
            } finally {
                mcursor.close();
            }

            return name != null;
        } catch (Exception e) {
            AppHelper.LogCat(e);
            return false;
        }
    }
}
