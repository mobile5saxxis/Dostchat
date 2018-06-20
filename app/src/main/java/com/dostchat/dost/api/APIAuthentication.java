package com.dostchat.dost.api;


import com.dostchat.dost.models.Login;
import com.dostchat.dost.app.EndPoints;
import com.dostchat.dost.models.JoinModel;
import com.dostchat.dost.models.LocationModel;
import com.dostchat.dost.models.NetworkModel;
import com.dostchat.dost.models.RequestResponse;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Created by Abderrahim El imame on 01/11/2015.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/bencherif_el
 */
public interface APIAuthentication {

    @FormUrlEncoded
    @POST(EndPoints.JOIN)
    Call<JoinModel> join(@Field("phone") String phone, @Field("country") String country);

    @GET(EndPoints.JOIN)
    Call<Login> join(@Query("mobile") String mobile);

    @GET(EndPoints.REGISTER)
    Call<RequestResponse> register(@Query("mobile") String mobile);

    /**
     * method to resend SMS request
     *
     * @param phone this is parameter for resend method
     */

    @FormUrlEncoded
    @POST(EndPoints.RESEND_REQUEST_SMS)
    Call<JoinModel> resend(@Field("phone") String phone);

    /**
     * method to verify the user code
     *
     * @param code this is parameter for verifyUser method
     * @return this is what method will return
     */
    @FormUrlEncoded
    @POST(EndPoints.VERIFY_USER)
    Call<JoinModel> verifyUser(@Field("code") String code);

    @GET("api.php?cmd=getUserLocation")
    Call<LocationModel> location(@Query("userId") int usetId, @Query("latitude") double latitude, @Query("longitude") double longitude, @Query("receiverUserId") int receiverUserId);


    /**
     * method to check network state
     *
     * @return this is return value
     */
    @GET(EndPoints.CHECK_NETWORK)
    Call<NetworkModel> checkNetwork();
}
