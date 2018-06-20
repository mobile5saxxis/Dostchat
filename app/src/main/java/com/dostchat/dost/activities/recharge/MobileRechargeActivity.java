package com.dostchat.dost.activities.recharge;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dostchat.dost.R;
import com.dostchat.dost.api.APIGroups;
import com.dostchat.dost.api.APIService;
import com.dostchat.dost.app.AppConstants;
import com.dostchat.dost.app.EndPoints;
import com.dostchat.dost.app.UserPref;
import com.dostchat.dost.fragments.OperatorFragment;
import com.dostchat.dost.helpers.AppHelper;
import com.dostchat.dost.helpers.PermissionHandler;
import com.dostchat.dost.models.Category;
import com.dostchat.dost.models.Operator;
import com.dostchat.dost.models.OperatorResponse;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;

import static com.dostchat.dost.app.AppConstants.PICK_CONTACT;
import static com.dostchat.dost.app.AppConstants.PLAN_REQUEST;

public class MobileRechargeActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.cl_p_mobile_recharge)
    CoordinatorLayout clLayout;

    @BindView(R.id.p_mobile_num)
    EditText mobileNum;
    @BindView(R.id.p_contacts)
    ImageView contacts;
    @BindView(R.id.p_operator)
    TextView mobileRchrg;
    @BindView(R.id.p_amount_mble_rchg)
    EditText amountMble;
    @BindView(R.id.p_proceed_to_pay_mble)
    TextView proceedPay;

    @BindView(R.id.p_btn_browse_plans)
    TextView mobilePlans;

    private String mobilenumber, serviceid, servicename;

    private String operator = "empty";
    private String amount = "empty";


    private boolean status = false;

    private UserPref mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobile_recharge);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mUser = new UserPref(this);

        mobileNum.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 4 || s.length() == 8) {
                    if (!status) {
                        fetchOperator(s);
                        status = true;
                    }
                } else if (s.length() == 0) {
                    mobileRchrg.setText("Select Operator");
                    status = false;
                }
            }
        });
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
        }
        return super.onOptionsItemSelected(item);
    }

    private void fetchOperator(Editable s) {

        APIGroups apiGroups = APIService.RootService(APIGroups.class, EndPoints.BASE_URL);
        apiGroups.getOperators(s.toString()).enqueue(new Callback<OperatorResponse>() {
            @Override
            public void onResponse(Call<OperatorResponse> call, retrofit2.Response<OperatorResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    OperatorResponse operatorResponse = response.body();

                    if (operatorResponse.getData().size() > 0) {
                        Operator data = operatorResponse.getData().get(0);
                        servicename = data.getOptype_name();
                        mobileRchrg.setText(servicename);
                        operator = data.getOptype_name();
                        serviceid = data.getId();
                        mobilePlans.setEnabled(true);
                    }
                }
            }

            @Override
            public void onFailure(Call<OperatorResponse> call, Throwable t) {

            }
        });

    }


    @OnClick(R.id.p_contacts)
    void pickContact() {
        if (!PermissionHandler.checkPermission(this, Manifest.permission.READ_CONTACTS)) {
            PermissionHandler.requestPermission(this, Manifest.permission.READ_CONTACTS);
            return;
        }
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, PICK_CONTACT);
    }

    @OnClick(R.id.p_btn_browse_plans)
    void browseplans() {
        if (mobilePlans.isEnabled()) {
            ArrayList<Category> mPlans = AppConstants.getPlansCategories();
            Intent i = new Intent(MobileRechargeActivity.this, BrowsePlansActivity.class);
            i.putExtra("serviceid", serviceid);
            i.putExtra("servicename", servicename);
            i.putParcelableArrayListExtra("plans", mPlans);
            startActivityForResult(i, PLAN_REQUEST);
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(MobileRechargeActivity.this);
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

    @OnClick(R.id.p_operator)
    void selectOperator() {
        mobilenumber = mobileNum.getText().toString().trim();

        if (mobilenumber.isEmpty()) {
            AppHelper.Snackbar(this, clLayout, "Enter or Select Mobile Number", AppConstants.MESSAGE_COLOR_ERROR, AppConstants.TEXT_COLOR);
        } else {
            AppHelper.showDialog(MobileRechargeActivity.this, "Fetching Operators.......");

            APIGroups apiGroups = APIService.RootService(APIGroups.class, EndPoints.BASE_URL);
            apiGroups.getOperators(mobilenumber).enqueue(new Callback<OperatorResponse>() {
                @Override
                public void onResponse(Call<OperatorResponse> call, retrofit2.Response<OperatorResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        AppHelper.hideDialog();
                        showOperatorDialog(response.body().getData());
                    }
                }

                @Override
                public void onFailure(Call<OperatorResponse> call, Throwable t) {

                }
            });
        }
    }


    @OnClick(R.id.p_proceed_to_pay_mble)
    void proceedToPay() {
        mobilenumber = mobileNum.getText().toString().trim();
        amount = amountMble.getText().toString().trim();
        if (mobilenumber.isEmpty()) {
            AppHelper.Snackbar(this, clLayout, "Enter or Select Mobile Number", AppConstants.MESSAGE_COLOR_ERROR, AppConstants.TEXT_COLOR);
        } else if (amount.isEmpty() || amount.equals("empty")) {
            AppHelper.Snackbar(this, clLayout, "Enter the recharge amount", AppConstants.MESSAGE_COLOR_ERROR, AppConstants.TEXT_COLOR);
        } else if (operator.isEmpty() || operator.equals("empty")) {
            AppHelper.Snackbar(this, clLayout, "Select Operator", AppConstants.MESSAGE_COLOR_ERROR, AppConstants.TEXT_COLOR);
        } else {
            Intent i = new Intent(MobileRechargeActivity.this, CompletePaymentActivity.class);
            i.putExtra("operator", operator);
            i.putExtra("number", mobilenumber);
            i.putExtra("amount", amount);
            i.putExtra("type", "mobile");
            startActivity(i);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            switch (requestCode) {
                case PICK_CONTACT:
                    Uri contactData = data.getData();
                    Cursor cursor = getContentResolver().query(contactData, null, null, null, null);
                    cursor.moveToFirst();
                    String hasPhone = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER));
                    String contactId = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
                    if (hasPhone.equals("1")) {
                        Cursor phones = getContentResolver().query
                                (ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID
                                                + " = " + contactId, null, null);
                        while (phones.moveToNext()) {
                            mobilenumber = phones.getString(phones.getColumnIndex
                                    (ContactsContract.CommonDataKinds.Phone.NUMBER)).replaceAll("[-() ]", "");
                        }
                        phones.close();
                        mobileNum.setText(mobilenumber);
                    } else {
                        Toast.makeText(getApplicationContext(), "This contact has no phone number", Toast.LENGTH_LONG).show();
                    }
                    cursor.close();

                    break;
                case PLAN_REQUEST:
                    amount = bundle.getString("amount");
                    amountMble.setText(amount);

                    break;
            }
        }
    }

    private void showOperatorDialog(ArrayList<Operator> mList) {

        OperatorFragment op = OperatorFragment.newInstance(mList, "prepaid");
        op.show(getSupportFragmentManager(), "op");


    }

    public void updateOperator(Operator op) {
        servicename = op.getOptype_name();
        mobileRchrg.setText(servicename);
        operator = op.getOp_id();
        serviceid = op.getId();
        mobilePlans.setEnabled(true);
    }
}
