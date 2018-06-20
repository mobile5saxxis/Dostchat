package com.dostchat.dost.activities.recharge;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
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
import com.dostchat.dost.helpers.PreferenceManager;
import com.dostchat.dost.interfaces.NetworkListener;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class WalletActivity extends AppCompatActivity implements NetworkListener {

    @BindView(R.id.loading_wallet)
    ProgressBar mProgress;

    @BindView(R.id.wallet_balance)
    TextView mBalance;

    @BindView(R.id.cl_wallet)
    CoordinatorLayout clLayout;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    private UserPref mUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.colorPrimary));

        FetchWalletAmount.getAmount(this);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        mUser = new UserPref(this);
        int temp = mUser.getWalletAmount();
        if (temp == -1) {
            temp = 0;
        }
        mBalance.setText(getResources().getString(R.string.Rs, String.valueOf(temp)));

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(ContextCompat.getDrawable(this, R.drawable.ic_back));
        }
    }

    @OnClick(R.id.wallet_add_money)
    void redirectPayment() {
        AppHelper.LaunchActivity(WalletActivity.this, AddMoneyActivity.class);
    }

    @OnClick(R.id.rl_prepaid)
    void redirectMobilePrepaid() {
        AppHelper.LaunchActivity(WalletActivity.this, MobileRechargeActivity.class);
    }

    @OnClick(R.id.rl_postpaid)
    void redirectMobilePostPaid() {
        AppHelper.LaunchActivity(WalletActivity.this, MobileRechargeActivity.class);
    }

    @OnClick(R.id.rl_dth)
    void redirectDTH() {
        AppHelper.LaunchActivity(WalletActivity.this, DTHActivity.class);
    }

    @OnClick(R.id.rl_data_card)
    void redirectDataCard() {
        AppHelper.LaunchActivity(WalletActivity.this, DCActivity.class);
    }

    @OnClick(R.id.rl_electricity)
    void redirectElectricity() {
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.refresh_menu, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                break;
            case R.id.refresh:
                refreshWallet();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void refreshWallet() {
        mProgress.setVisibility(View.VISIBLE);
        StringRequest request = new StringRequest(Request.Method.GET, AppConstants.WALLET_URL + "&sessionid" + PreferenceManager.getToken(this), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String status = jsonObject.getString("status");
                    mProgress.setVisibility(View.GONE);
                    if (status.equals("ok")) {
                        mBalance.setText(jsonObject.getString("walletamount"));
                    } else if (status.equals("ko")) {
                        new AlertDialog.Builder(WalletActivity.this)
                                .setMessage("User Session Timed Out...Login Again")
                                .setPositiveButton("Login", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        mUser.logoutUser();
                                        Intent intent = new Intent(WalletActivity.this, MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        Toast.makeText(WalletActivity.this, "Logout Successfull", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                } catch (Exception ignored) {

                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mProgress.setVisibility(View.GONE);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Cookie", mUser.getSessionId());
                return headers;
            }
        };
        DostChatApp.getInstance().addToRequestQueue(request);
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnecting, boolean isConnected) {
        if (!isConnecting && !isConnected) {
            AppHelper.Snackbar(this, clLayout, getString(R.string.connection_is_not_available), AppConstants.MESSAGE_COLOR_ERROR, AppConstants.TEXT_COLOR);
        } else if (isConnecting && isConnected) {
            AppHelper.Snackbar(this, clLayout, getString(R.string.connection_is_available), AppConstants.MESSAGE_COLOR_SUCCESS, AppConstants.TEXT_COLOR);
        } else {
            AppHelper.Snackbar(this, clLayout, getString(R.string.waiting_for_network), AppConstants.MESSAGE_COLOR_WARNING, AppConstants.TEXT_COLOR);

        }
    }

}
