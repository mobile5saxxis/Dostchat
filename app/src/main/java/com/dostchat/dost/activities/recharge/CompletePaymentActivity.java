package com.dostchat.dost.activities.recharge;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.dostchat.dost.R;
import com.dostchat.dost.activities.main.MainActivity;
import com.dostchat.dost.app.AppConstants;
import com.dostchat.dost.app.DostChatApp;
import com.dostchat.dost.app.FetchWalletAmount;
import com.dostchat.dost.app.UserPref;
import com.dostchat.dost.helpers.AppHelper;

import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

public class CompletePaymentActivity extends AppCompatActivity {

    @BindView(R.id.to_be_paid)
    TextView txtRechargeAmount;

    @BindView(R.id.current_balance)
    TextView txtCurrentBalance;

    @BindView(R.id.use_wallet_amount)
    TextView txtWalletAmount;


    @BindView(R.id.remaining_amount)
    TextView txtRemainingAmount;


    @BindView(R.id.complete_payment)
    Button btnCompletePayment;

    @BindView(R.id.addamount)
    Button btnAddAmount;

    @BindView(R.id.cl_comp_pay)
    CoordinatorLayout clLayout;

    @BindView(R.id.chck_wallet)
    CheckBox chckWallet;

    private String operator, payamount = null, number, type;
    private UserPref mUser;

    private boolean wallet = false;

    private int walletBalance, rechargeAmount, balance = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_payment);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ButterKnife.bind(this);
        Bundle extras = getIntent().getExtras();
        mUser = new UserPref(this);
        FetchWalletAmount.getAmount(this);
        if (extras != null) {
            operator = extras.getString("operator");
            number = extras.getString("number");
            payamount = extras.getString("amount");
            type = extras.getString("type");
        }

        txtRechargeAmount.setText(getResources().getString(R.string.rupee_display, payamount));
        txtRemainingAmount.setText(getResources().getString(R.string.rupee_display, payamount));
        btnAddAmount.setVisibility(View.VISIBLE);

        int temp = mUser.getWalletAmount();
        if (temp == -1) {
            walletBalance = 0;
        } else {
            walletBalance = temp;
        }

        txtCurrentBalance.setText("(Your Current Balance is Rs" + walletBalance + ")");

        if (payamount != null) {
            rechargeAmount = Integer.parseInt(payamount);
        } else {
            AppHelper.Snackbar(this, clLayout, "Enter Amount", AppConstants.MESSAGE_COLOR_ERROR, AppConstants.TEXT_COLOR);
        }
    }

    @OnCheckedChanged(R.id.chck_wallet)
    void checkListener() {
        if (chckWallet.isChecked()) {
            txtWalletAmount.setVisibility(View.VISIBLE);
            btnAddAmount.setVisibility(View.GONE);
            btnCompletePayment.setVisibility(View.VISIBLE);
            if (rechargeAmount < walletBalance) {
                wallet = true;
                txtWalletAmount.setText(getResources().getString(R.string.negative_rupee_display, String.valueOf(rechargeAmount)));
                txtRemainingAmount.setText(getResources().getString(R.string.rupee_display, "0"));
            } else {
                wallet = false;
                balance = rechargeAmount - walletBalance;
                txtRemainingAmount.setText(getResources().getString(R.string.rupee_display, String.valueOf(balance)));
                txtWalletAmount.setText(getResources().getString(R.string.negative_rupee_display, String.valueOf(walletBalance)));
            }

        } else {
            wallet = true;
            txtWalletAmount.setVisibility(View.GONE);
            txtRemainingAmount.setText(getResources().getString(R.string.rupee_display, payamount));
            btnAddAmount.setVisibility(View.VISIBLE);
            btnCompletePayment.setVisibility(View.GONE);
        }
    }

    @OnClick(R.id.complete_payment)
    void completePayment() {

        if (!wallet) {
            new AlertDialog.Builder(CompletePaymentActivity.this).setMessage("No Sufficient Funds..Add Money To Wallet and Recharge").setPositiveButton("close", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    CompletePaymentActivity.this.finish();
                }
            }).show();

        } else {
            String finalurl;
            switch (type) {
                case "mobile":
                    finalurl = AppConstants.RECHARGE_URL + "&userid=" + mUser.getUserId() + "&op_id=" + operator + "&mobile=" + number + "&amount=" + String.valueOf(rechargeAmount);
                    //finalurl = "http://api.billpaymart.com/apiservice.asmx/Recharge?apiToken=a415659e38904741a95cdef62f61f89a&mn=" + number+ "&op="+operator+"&amt="+String.valueOf(rechargeAmount)+"&reqid=65S4F6S4";
                    submitToServer(finalurl, "Recharge Successfull");


                    break;
                case "dth":
                    finalurl = AppConstants.DTH_RECHARGE_URL + "&op_id=" + operator + "&cid=" + number + "&amount=" + payamount;
                    submitToServer(finalurl, "DTH Payment Successfull");
                    break;
                case "dc":
                    finalurl = AppConstants.DC_RECHARGE_URL + "&amount=" + payamount + "&cardno=" + number + "&op_id=" + operator;
                    submitToServer(finalurl, "DataCard Payment Successfull");
                    break;


            }

        }
    }

    @OnClick(R.id.addamount)
    void AddMoney() {
        AppHelper.LaunchActivity(CompletePaymentActivity.this, AddMoneyActivity.class);

    }

    private void submitToServer(String finalurl, final String message) {
        switch (type) {
            case "mobile":
                AppHelper.showDialog(CompletePaymentActivity.this, "Recharge in Process Please wait", false);
                break;
            case "dth":
                AppHelper.showDialog(CompletePaymentActivity.this, "Recharge in Process Please wait", false);
                break;
            case "dc":
                AppHelper.showDialog(CompletePaymentActivity.this, "Recharge in Process Please wait", false);
                break;

        }

        StringRequest request = new StringRequest(Request.Method.GET, finalurl, new Response.Listener<String>() {
            @Override
            public void onResponse(final String response) {
                AppHelper.hideDialog();
                try {
                    System.out.println(response);
                    JSONObject jsonObject = new JSONObject(response);
                    String status = jsonObject.getString("status");
                    if (status.equals("ok")) {
                        AppHelper.Snackbar(CompletePaymentActivity.this, clLayout, message, AppConstants.MESSAGE_COLOR_SUCCESS, AppConstants.TEXT_COLOR);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = new Intent(CompletePaymentActivity.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                CompletePaymentActivity.this.finish();
                            }
                        }, 3000);
                    }
                } catch (Exception ignored) {

                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        })
//        {
//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                HashMap<String, String> headers = new HashMap<>();
//                headers.put("Cookie",mUser.getSessionId());
//                return headers;
//            }
//        }
                ;
        DostChatApp.getInstance().addToRequestQueue(request);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //walletBalance = mUser.getWalletAmount();
    }


}
