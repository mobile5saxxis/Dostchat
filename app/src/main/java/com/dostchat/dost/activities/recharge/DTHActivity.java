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

public class DTHActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.cl_dth)
    CoordinatorLayout clLayout;


    @BindView(R.id.dth_number)
    EditText etDthNum;
    @BindView(R.id.operator)
    TextView dthOperator;
    @BindView(R.id.amount_dth)
    EditText etAmount;
    @BindView(R.id.proceed_to_pay_dth)
    TextView proceedPay;

    @BindView(R.id.dth_browse__plans)
    TextView dthPlans;

    private String operator,dthNumber,serviceid,amount=null,servicename;

    private UserPref mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dth);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mUser = new UserPref(this);

    }


    @OnClick(R.id.dth_browse__plans)
    void browseplans(){
        if(dthPlans.isEnabled()) {
            ArrayList<Category> mPlans = AppConstants.getDTHPlans();
            Intent i = new Intent(DTHActivity.this, BrowsePlansActivity.class);
            i.putExtra("serviceid", serviceid);
            i.putExtra("servicename", servicename);
            i.putParcelableArrayListExtra("plans", mPlans);
            startActivityForResult(i, PLAN_REQUEST);
        }else{
            AlertDialog.Builder builder = new AlertDialog.Builder(DTHActivity.this);
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

    @OnClick(R.id.operator)
    void selectOperator(){
        AppHelper.showDialog(DTHActivity.this,"Fetching Operators.......");
        StringRequest request=new StringRequest(Request.Method.GET, AppConstants.OPERATOR_URL+"2", new Response.Listener<String>() {
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
//                        AlertDialog.Builder builder = new AlertDialog.Builder(DthActivity.this);
//                        builder.setTitle("Make your selection");
//                        builder.setItems(charSequenceArray, new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int item) {
//                                // Do something with the selection
//                                dthOperator.setText(charSequenceArray[item]);
//                                operator = mList.get(item).getOp_id();
//                                serviceid = mList.get(item).getId();
//                                dthPlans.setEnabled(true);
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

    @OnClick(R.id.proceed_to_pay_dth)
    void ProceedToPay(){

        dthNumber = etDthNum.getText().toString().trim();
        if (amount==null)
            amount  = etAmount.getText().toString().trim();

        if(dthNumber.isEmpty()){
            AppHelper.Snackbar(this,clLayout,"Enter DTH Number",AppConstants.MESSAGE_COLOR_ERROR,AppConstants.TEXT_COLOR);
        }else if(amount.isEmpty()){
            AppHelper.Snackbar(this,clLayout,"Enter the recharge amount",AppConstants.MESSAGE_COLOR_ERROR,AppConstants.TEXT_COLOR);
        }else if(operator.isEmpty()){
            AppHelper.Snackbar(this,clLayout,"Select Operator",AppConstants.MESSAGE_COLOR_ERROR,AppConstants.TEXT_COLOR);
        }else{
            Intent i = new Intent(DTHActivity.this, CompletePaymentActivity.class);
            i.putExtra("operator", operator);
            i.putExtra("number", dthNumber);
            i.putExtra("amount", amount);
            i.putExtra("type","dth");
            startActivity(i);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PLAN_REQUEST){
            if(resultCode == RESULT_OK)
                etAmount.setText(data.getStringExtra("amount"));
        }
    }

    private void showOperatorDialog(ArrayList<Operator> mList){

        OperatorFragment op = OperatorFragment.newInstance(mList,"dth");
        op.show(getSupportFragmentManager(),"op");


    }


    public void updateOperator(Operator op) {
        servicename = op.getOptype_name();
        dthOperator.setText(op.getOptype_name());
        operator = op.getOp_id();
        serviceid = op.getId();
        dthPlans.setEnabled(true);
    }

}
