package com.dostchat.dost.api;


import com.dostchat.dost.app.EndPoints;
import com.dostchat.dost.models.JoinModel;
import com.dostchat.dost.models.NetworkModel;
import com.dostchat.dost.models.users.VersionResponse;
import com.dostchat.dost.models.users.contacts.ContactsModel;
import com.dostchat.dost.models.users.contacts.ProfileResponse;
import com.dostchat.dost.models.users.contacts.SyncContacts;
import com.dostchat.dost.models.users.status.EditStatus;
import com.dostchat.dost.models.users.status.StatusModel;
import com.dostchat.dost.models.users.status.StatusResponse;

import java.util.List;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;

/**
 * Created by Abderrahim El imame on 02/03/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public interface APIContact {

    /**
     * method to  syncing all contacts
     *
     * @param listString this is parameter for  contacts method
     * @return this is return value
     */
    @POST(EndPoints.SEND_CONTACTS)
    Observable<List<ContactsModel>> contacts(@Body SyncContacts listString);

    /**
     * method to get contact info
     *
     * @param userID this is parameter for  contact method
     * @return this is return value
     */
    @GET(EndPoints.GET_CONTACT)
    Observable<ContactsModel> contact(@Path("userID") int userID);


    /**
     * method to get  user  status
     *
     * @return this is return value
     */
    @GET(EndPoints.GET_STATUS)
    Observable<List<StatusModel>> status();

    /**
     * method to delete user status
     *
     * @param status this is parameter for  delete status method
     * @return this is return value
     */
    @DELETE(EndPoints.DELETE_STATUS)
    Observable<StatusResponse> deleteStatus(@Path("status") String status);


    /**
     * method to delete all user status
     *
     * @return this is return value
     */
    @DELETE(EndPoints.DELETE_ALL_STATUS)
    Observable<StatusResponse> deleteAllStatus();

    /**
     * method to update user status
     *
     * @param statusID this is parameter for  update status method
     * @return this is return value
     */
    @PUT(EndPoints.UPDATE_STATUS)
    Observable<StatusResponse> updateStatus(@Path("statusID") int statusID);

    /**
     * method to edit user status
     *
     * @param editStatus this is parameter for  editStatus method
     * @return this is return value
     */
    @POST(EndPoints.EDIT_STATUS)
    Observable<StatusResponse> editStatus(@Body EditStatus editStatus);

    /**
     * method to edit username
     *
     * @param editStatus this is parameter for  editUsername method
     * @return this is return value
     */
    @POST(EndPoints.EDIT_NAME)
    Observable<StatusResponse> editUsername(@Body EditStatus editStatus);

    /**
     * method to edit group name
     *
     * @param editStatus this is parameter for  editGroupName method
     * @return this is return value
     */
    @POST(EndPoints.EDIT_GROUP_NAME)
    Observable<StatusResponse> editGroupName(@Body EditStatus editStatus);

    /**
     * method to edit user image
     *
     * @param image this is parameter for  uploadImage method
     * @return this is return value
     */
    @Multipart
    @POST(EndPoints.UPLOAD_PROFILE_IMAGE)
    Call<ProfileResponse> uploadImage(@Part("image\"; filename=\"profileImage\" ") RequestBody image);

    /**
     * method to delete account
     *
     * @param phone this is parameter for  uploadImage method
     * @return this is return value
     */

    @FormUrlEncoded
    @POST(EndPoints.DELETE_ACCOUNT)
    Observable<JoinModel> deleteAccount(@Field("phone") String phone, @Field("country") String country);


    /**
     * method to verify the user code
     *
     * @param code this is parameter for verifyUser method
     * @return this is what method will return
     */
    @FormUrlEncoded
    @POST(EndPoints.DELETE_ACCOUNT_CONFIRMATION)
    Call<StatusResponse> deleteAccountConfirmation(@Field("code") String code);

    /**
     * method to get ads info
     *
     * @return this is return value
     */
    @GET(EndPoints.GET_ADS_INFORMATION)
    Observable<StatusResponse> getAdsInformation();

    /**
     * method to get ads info
     *
     * @return this is return value
     */
    @GET(EndPoints.GET_INTERSTITIAL_INFORMATION)
    Observable<StatusResponse> getInterstitialAdInformation();

    /**
     * method to get app version info
     *
     * @return this is return value
     */
    @GET(EndPoints.GET_APPLICATION_VERSION)
    Observable<VersionResponse> getApplicationVersion();
    /**
     * method to get app privacy and terms
     *
     * @return this is return value
     */
    @GET(EndPoints.GET_APPLICATION_PRIVACY)
    Observable<StatusResponse> getPrivacyTerms();

    /**
     * method to check network state
     *
     * @return this is return value
     */
    @GET(EndPoints.CHECK_NETWORK)
    Observable<NetworkModel> checkNetwork();

}
