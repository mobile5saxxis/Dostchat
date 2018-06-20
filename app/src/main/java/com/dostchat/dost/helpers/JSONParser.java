package com.dostchat.dost.helpers;



import com.dostchat.dost.models.Operator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by saxxis25 on 3/25/2017.
 */

public class JSONParser {

    public static ArrayList<Operator> getOperators(JSONArray jsonArray){

         ArrayList<Operator> mOperators =new ArrayList<Operator>();
         int length = jsonArray.length();

        for (int i = 0; i<length;i++){
            try {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String id = jsonObject.getString("op_id");
                String optype_name = jsonObject.getString("optype_name");
                String op_id = jsonObject.getString("op_id");
                String op_id2 = jsonObject.getString("op_id2");
                String op_code = jsonObject.getString("op_code");
                String dth_code = jsonObject.getString("dth_code");
                String op_dth = jsonObject.getString("operator_type");
                String imageLocation = jsonObject.getString("imageLocation");
                String type_description = jsonObject.getString("type_description");
                String published = jsonObject.getString("published");
                mOperators.add(new Operator(id,optype_name,op_id,op_id2,op_code,dth_code,op_dth,imageLocation,type_description,published));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return mOperators;
    }

    public static ArrayList<Operator> getDTHOperators(JSONArray jsonArray){

        ArrayList<Operator> mOperators =new ArrayList<Operator>();
        int length = jsonArray.length();

        for (int i = 0; i<length;i++){
            try {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String id = null;
                String optype_name = jsonObject.getString("optype_name");
                String op_id = jsonObject.getString("op_id");
                String op_id2 = null;
                String op_code = jsonObject.getString("op_code");
                String dth_code = jsonObject.getString("dth_code");
                String op_dth = jsonObject.getString("operator_type");
                String imageLocation = jsonObject.getString("imageLocation");
                String type_description = null;
                String published = null;
                mOperators.add(new Operator(null,optype_name,op_id, null,op_code,dth_code,op_dth, imageLocation, null, null));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return mOperators;
    }
}
