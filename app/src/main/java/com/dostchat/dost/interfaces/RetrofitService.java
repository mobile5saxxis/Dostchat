package com.dostchat.dost.interfaces;

import com.dostchat.dost.models.sos.SOSAddResponse;
import com.dostchat.dost.models.sos.SOSResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.DELETE;

public interface RetrofitService {
    @GET("api.php")
    Call<SOSAddResponse> addSOSContact(@Query("cmd") String cmd, @Query("phone") String phone, @Query("contact_number") String contact_number, @Query("contact_name") String contact_name);

    @GET("api.php")
    Call<SOSResponse> getSOSContacts(@Query("cmd") String cmd, @Query("phone") String phone);

    @GET("api.php")
    Call<SOSAddResponse> deleteSOSContacts(@Query("cmd") String cmd, @Query("phone") String phone, @Query("contact_number") String contact_number);

    @GET("api.php")
    Call<SOSAddResponse> sendSOSContacts(@Query("cmd") String cmd, @Query("phone") String phone, @Query("gps") String gps);

}
