package com.dostchat.dost.activities.recharge;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.dostchat.dost.R;
import com.dostchat.dost.app.AppConstants;
import com.dostchat.dost.app.DostChatApp;
import com.dostchat.dost.app.UserPref;
import com.dostchat.dost.fragments.OperatorFragment;
import com.dostchat.dost.helpers.AppHelper;
import com.dostchat.dost.helpers.JSONParser;
import com.dostchat.dost.models.Category;
import com.dostchat.dost.models.Operator;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.dostchat.dost.app.AppConstants.PLAN_REQUEST;

public class DCActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.cl_dc)
    CoordinatorLayout clLayout;


    @BindView(R.id.dc_number)
    EditText etDcNum;
    @BindView(R.id.dc_operator)
    TextView dcOperator;
    @BindView(R.id.amount_dc)
    EditText etAmount;
    @BindView(R.id.proceed_to_pay_dc)
    TextView proceedPay;

    @BindView(R.id.dc_browse_plans)
    TextView dcPlans;

    private String operator,dcNumber,serviceid,amount=null,servicename;

    private UserPref mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dc);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mUser = new UserPref(this);


    }


    @OnClick(R.id.dc_browse_plans)
    void browseplans(){
        if(dcPlans.isEnabled()) {
            ArrayList<Category> mPlans = AppConstants.getDTHPlans();
            Intent i = new Intent(DCActivity.this, BrowsePlansActivity.class);
            i.putExtra("serviceid", serviceid);
            i.putExtra("servicename", servicename);
            i.putParcelableArrayListExtra("plans", mPlans);
            startActivityForResult(i, PLAN_REQUEST);
        }else{
            AlertDialog.Builder builder = new AlertDialog.Builder(DCActivity.this);
            builder.setTitle("Select Operator to view Plans");
            builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    @OnClick(R.id.dc_operator)
    void selectOperator(){
        AppHelper.showDialog(DCActivity.this,"Fetching Operators.......");
        StringRequest request=new StringRequest(Request.Method.GET, AppConstants.OPERATOR_URL+"3", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                System.out.println(response);
                try{
                    JSONObject jsonObject = new JSONObject(response);
                    String status = jsonObject.getString("status");
                    if (status.equals("ok")){
                        JSONArray jsonArray = jsonObject.getJSONArray("data");
                        final ArrayList<Operator> mList = JSONParser.getDTHOperators(jsonArray);
                        AppHelper.hideDialog();
                        showOperatorDialog(mList);
//                        List<CharSequence> charSequences = new ArrayList<>();
//                        for (int i = 0; i<mList.size();i++){
//                            charSequences.add(mList.get(i).getOptype_name());
//                        }
//                        final CharSequence[] charSequenceArray = charSequences.toArray(new
//                                CharSequence[charSequences.size()]);
//                        AppHelper.hideDialog();
//                        AlertDialog.Builder builder = new AlertDialog.Builder(DataCardActivity.this);
//                        builder.setTitle("Make your selection");
//                        builder.setItems(charSequenceArray, new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int item) {
//                                // Do something with the selection
//                                dcOperator.setText(charSequenceArray[item]);
//                                operator = mList.get(item).getOp_id();
//                                serviceid = mList.get(item).getId();
//                                dcPlans.setEnabled(true);
//                            }
//                        });
//                        AlertDialog alert = builder.create();
//                        alert.show();
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
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Cookie",mUser.getSessionId());
                return headers;
            }
        };
        DostChatApp.getInstance().addToRequestQueue(request);
    }

    @OnClick(R.id.proceed_to_pay_dc)
    void ProceedToPay(){

        dcNumber = etDcNum.getText().toString().trim();
        if (amount==null)
            amount = etAmount.getText().toString().trim();

        if(dcNumber.isEmpty()){
            AppHelper.Snackbar(this,clLayout,"Enter DataCard Number",AppConstants.MESSAGE_COLOR_ERROR,AppConstants.TEXT_COLOR);
        }else if(amount.isEmpty()){
            AppHelper.Snackbar(this,clLayout,"Enter the recharge amount",AppConstants.MESSAGE_COLOR_ERROR,AppConstants.TEXT_COLOR);
        }else if(operator.isEmpty()){
            AppHelper.Snackbar(this,clLayout,"Select Operator",AppConstants.MESSAGE_COLOR_ERROR,AppConstants.TEXT_COLOR);
        }else{
            Intent i = new Intent(DCActivity.this, CompletePaymentActivity.class);
            i.putExtra("operator", operator);
            i.putExtra("number", dcNumber);
            i.putExtra("amount", amount);
            i.putExtra("type","dc");
            startActivity(i);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PLAN_REQUEST){
            if(resultCode == RESULT_OK) {
                amount = data.getStringExtra("amount");
                etAmount.setText(amount);
            }
        }
    }

    private void showOperatorDialog(ArrayList<Operator> mList){

        OperatorFragment op = OperatorFragment.newInstance(mList,"dc");
        op.show(getSupportFragmentManager(),"op");


    }


    public void updateOperator(Operator op) {
        servicename = op.getOptype_name();
        dcOperator.setText(op.getOptype_name());
        operator = op.getOp_id();
        serviceid = op.getId();
        dcPlans.setEnabled(true);
    }



}
