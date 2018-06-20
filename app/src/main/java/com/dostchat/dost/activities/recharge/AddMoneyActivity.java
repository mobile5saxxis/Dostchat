package com.dostchat.dost.activities.recharge;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.Toolbar;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.ebs.android.sdk.Config;
import com.ebs.android.sdk.EBSPayment;
import com.ebs.android.sdk.PaymentRequest;
import com.dostchat.dost.R;
import com.dostchat.dost.app.AppConstants;
import com.dostchat.dost.app.UserPref;
import com.dostchat.dost.app.DostChatApp;
import com.dostchat.dost.helpers.PreferenceManager;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AddMoneyActivity extends AppCompatActivity {

    @BindView(R.id.add_money_et_money)
    EditText etMoney;

    @BindView(R.id.txt_1000)
    TextView txt1000;

    @BindView(R.id.txt_500)
    TextView txt500;

    @BindView(R.id.tv_wallet_balance)
    TextView tv_wallet_balance;

    @BindView(R.id.txt_100)
    TextView txt100;

    @BindView(R.id.add_money_btn)
    AppCompatButton btnAddMoney;


    String finalmoney = "0";

    private UserPref mUser;

    // Mandatory
    private static String HOST_NAME = "";

    ArrayList<HashMap<String, String>> custom_post_parameters;


    private static final int ACC_ID = 24140; // Provided by EBS

    private static final String SECRET_KEY = "933ce832f7216ed360f9ad0a3e1f7c67";// Provided by EBS

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_money);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ButterKnife.bind(this);
        mUser = new UserPref(this);
        setTitle(getString(R.string.add_money_to_dost_chat));
        toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.colorPrimary));

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(ContextCompat.getDrawable(this, R.drawable.ic_back));
        }

        HOST_NAME = getResources().getString(R.string.hostname);

        int temp = mUser.getWalletAmount();
        if (temp == -1){
            temp = 0;
        }

        tv_wallet_balance.setText(getResources().getString(R.string.wallet_balance,String.valueOf(temp)));
    }

    @OnClick(R.id.txt_1000)
    void add1000() {
        etMoney.setText("1000");
        finalmoney = "1000";
    }

    @OnClick(R.id.txt_500)
    void add500() {
        etMoney.setText("500");
        finalmoney = "500";
    }

    @OnClick(R.id.txt_100)
    void add100() {
        etMoney.setText("100");
        finalmoney = "100";
    }

    @OnClick(R.id.add_money_btn)
    void addmoney() {

        String amount = etMoney.getText().toString().trim();

        if (!finalmoney.equals("0")) {
            rediectToPaymentGateway(AddMoneyActivity.this, finalmoney);
        } else {
            if (amount.isEmpty()) {
                new AlertDialog.Builder(AddMoneyActivity.this)
                        .setMessage("Enter Amount")
                        .setPositiveButton("close", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();
            } else {
                rediectToPaymentGateway(AddMoneyActivity.this, amount);
            }
        }


    }

    private void rediectToPaymentGateway(final AddMoneyActivity addMoneyActivity, final String finalmoney) {

        String url = AppConstants.MONEY_ORDERID_REQ + "&amount=" + finalmoney + "&userid=" + PreferenceManager.getID(this);

        StringRequest str = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                System.out.println("Called");
                try {
                    JSONObject resp = new JSONObject(s);
                    String status = resp.getString("status");
                    if (status.equals("ok")) {
                        String orderId = resp.getString("message");
                        callEbsKit(addMoneyActivity, orderId, finalmoney);
                    }


                } catch (Exception e) {

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        });
        DostChatApp.getInstance().addToRequestQueue(str);


    }

    private void callEbsKit(AddMoneyActivity cpa, String orderId, String finalmoney) {
        /**
         * Set Parameters Before Initializing the EBS Gateway, All mandatory
         * values must be provided
         */

        /** Payment Amount Details */
        // Total Amount

        PaymentRequest.getInstance().setTransactionAmount(finalmoney);

        /** Mandatory */

        PaymentRequest.getInstance().setAccountId(ACC_ID);
        PaymentRequest.getInstance().setSecureKey(SECRET_KEY);

        // Reference No
        PaymentRequest.getInstance().setReferenceNo(orderId);
        /** Mandatory */

        // Email Id
        PaymentRequest.getInstance().setBillingEmail("test_tag@testmail.com");
        /** Mandatory */

        /**
         * Set failure id as 1 to display amount and reference number on failed
         * transaction page. set 0 to disable
         */
        /** Mandatory */

        // Currency
        PaymentRequest.getInstance().setCurrency("INR");
        /** Mandatory */


        // Your Reference No or Order Id for this transaction
        PaymentRequest.getInstance().setTransactionDescription(
                orderId);

        /** Billing Details */
        PaymentRequest.getInstance().setBillingName("Test_Name");
        /** Optional */
        PaymentRequest.getInstance().setBillingAddress("North Mada Street");
        /** Optional */
        PaymentRequest.getInstance().setBillingCity("Chennai");
        /** Optional */
        PaymentRequest.getInstance().setBillingPostalCode("600019");
        /** Optional */
        PaymentRequest.getInstance().setBillingState("Tamilnadu");
        /** Optional */
        PaymentRequest.getInstance().setBillingCountry("IND");
        // ** Optional */
        PaymentRequest.getInstance().setBillingPhone("01234567890");
        /** Optional */
        /** set custom message for failed transaction */


        /** Optional */
        /** Shipping Details */
        PaymentRequest.getInstance().setShippingName("Test_Name");
        /** Optional */
        PaymentRequest.getInstance().setShippingAddress("North Mada Street");
        /** Optional */
        PaymentRequest.getInstance().setShippingCity("Chennai");
        /** Optional */
        PaymentRequest.getInstance().setShippingPostalCode("600019");
        /** Optional */
        PaymentRequest.getInstance().setShippingState("Tamilnadu");
        /** Optional */
        PaymentRequest.getInstance().setShippingCountry("IND");
        /** Optional */
        PaymentRequest.getInstance().setShippingEmail("test@testmail.com");
        /** Optional */
        PaymentRequest.getInstance().setShippingPhone("01234567890");
        /** Optional */
        /* enable log by setting 1 and disable by setting 0 */

        /**
         * Initialise parameters for dyanmic values sending from merchant custom
         * values from merchant
         */

        custom_post_parameters = new ArrayList<HashMap<String, String>>();
        HashMap<String, String> hashpostvalues = new HashMap<String, String>();
        hashpostvalues.put("account_details", "saving");
        hashpostvalues.put("merchant_type", "gold");
        custom_post_parameters.add(hashpostvalues);

        PaymentRequest.getInstance()
                .setCustomPostValues(custom_post_parameters);
        /** Optional-Set dyanamic values */

        // PaymentRequest.getInstance().setFailuremessage(getResources().getString(R.string.payment_failure_message));

        EBSPayment.getInstance().init(cpa, ACC_ID, SECRET_KEY,
                Config.Mode.ENV_TEST, Config.Encryption.ALGORITHM_SHA512, HOST_NAME);

        // EBSPayment.getInstance().init(context, accId, secretkey, environment,
        // algorithm, host_name);

    }

}
