package com.dostchat.dost.app;

/**
 * Created by Abderrahim El imame on 02/03/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class EndPoints {

    public static final String BASE_URL = "http://192.185.130.219/APP/";//so this is th mbase url for the app

    public static final String LOCATION_URL = "http://192.185.130.219/APP/";

    /**
     * Chat server URLs
     */
    public static final String CHAT_SERVER_URL = "http://192.185.130.219:9008";// this for chat server i choice 9000 as port for the chat server  you can change it

    /**
     * Authentication
     */
    public static final String JOIN = "Join";
    public static final String REGISTER = "index.php?option=com_jbackend&view=request&action=post&module=user&resource=regwithmobile&lang=en&lang=en";
    public static final String RESEND_REQUEST_SMS = "Resend";
    public static final String VERIFY_USER = "VerifyUser";
    public static final String CHECK_NETWORK = "CheckNetwork";
    /**
     * Groups
     */
    public static final String LOCATION_EXCHANGE = "api.php?cmd=getUserLocation";
    public static final String CREATE_GROUP = "Groups/createGroup";
    public static final String ADD_MEMBERS_TO_GROUP = "Groups/addMembersToGroup";
    public static final String REMOVE_MEMBER_FROM_GROUP = "Groups/removeMemberFromGroup";
    public static final String MAKE_MEMBER_AS_ADMIN = "Groups/makeMemberAdmin";
    public static final String REMOVE_MEMBER_AS_ADMIN = "Groups/makeAdminMember";
    public static final String GROUPS_lIST = "Groups/all";
    public static final String GROUP_MEMBERS_lIST = "GetGroupMembers/{groupID}";
    public static final String EXIT_GROUP = "ExitGroup/{groupID}";
    public static final String DELETE_GROUP = "DeleteGroup/{groupID}";
    public static final String GET_GROUP = "GetGroup/{groupID}";
    public static final String UPLOAD_GROUP_PROFILE_IMAGE = "uploadGroupImage";
    public static final String EDIT_GROUP_NAME = "EditGroupName";


    /**
     * Download and upload files
     */
    public static final String UPLOAD_MESSAGES_IMAGE = "uploadMessagesImage";
    public static final String UPLOAD_MESSAGES_VIDEO = "uploadMessagesVideo";
    public static final String UPLOAD_MESSAGES_AUDIO = "uploadMessagesAudio";
    public static final String UPLOAD_MESSAGES_DOCUMENT = "uploadMessagesDocument";
    public static final String UPLOAD_MESSAGES_BACKUP = "uploadMessagesBackup";


    /**
     * Contacts
     */
    public static final String SEND_CONTACTS = "SendContacts";
    public static final String GET_CONTACT = "GetContact/{userID}";
    public static final String GET_STATUS = "GetStatus";
    public static final String DELETE_ALL_STATUS = "DeleteAllStatus";
    public static final String DELETE_STATUS = "DeleteStatus/{status}";
    public static final String UPDATE_STATUS = "UpdateStatus/{statusID}";
    public static final String EDIT_STATUS = "EditStatus";
    public static final String EDIT_NAME = "EditName";
    public static final String UPLOAD_PROFILE_IMAGE = "uploadImage";
    public static final String DELETE_ACCOUNT = "DeleteAccount";
    public static final String DELETE_ACCOUNT_CONFIRMATION = "DeleteUserAccountConfirmation";


    /**
     * Admob
     */
    public static final String GET_ADS_INFORMATION = "GetAdmobInformation";
    public static final String GET_INTERSTITIAL_INFORMATION = "GetAdmobInterstitialInformation";


    /**
     * Files Get URL
     */
    public static final String PROFILE_IMAGE_URL = BASE_URL + "image/profile/";
    public static final String PROFILE_PREVIEW_IMAGE_URL = BASE_URL + "image/profilePreview/";
    public static final String PROFILE_PREVIEW_HOLDER_IMAGE_URL = BASE_URL + "image/profilePreviewHolder/";
    public static final String ROWS_IMAGE_URL = BASE_URL + "image/rowImage/";
    public static final String SETTINGS_IMAGE_URL = BASE_URL + "image/settings/";
    public static final String EDIT_PROFILE_IMAGE_URL = BASE_URL + "image/editProfile/";

    public static final String MESSAGE_DOCUMENT_URL = BASE_URL + "document/messageDocument/";
    public static final String MESSAGE_HOLDER_IMAGE_URL = BASE_URL + "image/messageImageHolder/";
    public static final String MESSAGE_IMAGE_URL = BASE_URL + "image/messageImage/";
    public static final String MESSAGE_VIDEO_THUMBNAIL_URL = BASE_URL + "video/messageVideoThumbnail/";
    public static final String MESSAGE_AUDIO_URL = BASE_URL + "audio/messageAudio/";

    /**
     * Files Downloads URL
     */
    public static final String MESSAGE_DOCUMENT_DOWNLOAD_URL = "document/messageDocument/";
    public static final String MESSAGE_BACKUP_DOWNLOAD_URL = "backup/messageBackup/";
    public static final String MESSAGE_IMAGE_DOWNLOAD_URL = "image/messageImage/";
    public static final String MESSAGE_VIDEO_DOWNLOAD_THUMBNAIL_URL = "video/messageVideoThumbnail/";
    public static final String MESSAGE_VIDEO_DOWNLOAD_URL = "video/messageVideo/";
    public static final String MESSAGE_AUDIO_DOWNLOAD_URL = "audio/messageAudio/";
    public static final String GET_BACKUP_URL = "getBackupUrl";

    /**
     * APPLICATION
     */
    public static final String GET_APPLICATION_VERSION = "GetApplicationVersion";
    public static final String GET_APPLICATION_PRIVACY = "GetApplicationPrivacy";
}
