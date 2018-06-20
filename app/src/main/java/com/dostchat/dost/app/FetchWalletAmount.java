package com.dostchat.dost.app;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.dostchat.dost.activities.main.MainActivity;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by saxxis25 on 4/5/2017.
 */

public class FetchWalletAmount {

    public static void getAmount(final Context cont){
        final UserPref mUser = new UserPref(cont);
        StringRequest request=new StringRequest(Request.Method.GET, AppConstants.WALLET_URL+"&userid="+mUser.getUserId(), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                System.out.println(response);
                try{
                    JSONObject jsonObject = new JSONObject(response);
                    String status = jsonObject.getString("status");
                    if (status.equals("ok")) {
                        int amt = jsonObject.getInt("data");
                        mUser.setWalletAmount(amt);
                    }else if(status.equals("ko")) {
                        new AlertDialog.Builder(cont)
                                .setMessage("User Session Timed Out...Login Again")
                                .setPositiveButton("Login", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        mUser.logoutUser();
                                        Intent intent = new Intent(cont, MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
                                        cont.startActivity(intent);
                                        Toast.makeText(cont,"Logout Successfull",Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                }catch (Exception ignored){
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Cookie",mUser.getSessionId());
                return headers;
            }
        };
        DostChatApp.getInstance().addToRequestQueue(request);
    }
}
