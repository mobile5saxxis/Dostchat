package com.dostchat.dost.activities.main;

import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.dostchat.dost.R;
import com.dostchat.dost.adapters.recyclerView.SOSAdapter;
import com.dostchat.dost.app.DostChatApp;
import com.dostchat.dost.helpers.PreferenceManager;
import com.dostchat.dost.interfaces.RetrofitService;
import com.dostchat.dost.models.sos.SOSAddResponse;
import com.dostchat.dost.models.sos.SOSResponse;
import com.dostchat.dost.services.RetrofitInstance;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SOSActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int RESULT_PICK_CONTACT = 2065;
    private RetrofitService service;
    private String number;
    private SOSAdapter sosAdapter;
    private RelativeLayout rl_empty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sos);

        Toolbar toolbar_sos = (Toolbar) findViewById(R.id.toolbar_sos);
        toolbar_sos.setTitle(R.string.sos);
        setSupportActionBar(toolbar_sos);

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        number = PreferenceManager.getPhone(this);

        service = RetrofitInstance.createService(RetrofitService.class);

        findViewById(R.id.fab_add_contact).setOnClickListener(this);
        rl_empty = (RelativeLayout) findViewById(R.id.rl_empty);
        rl_empty.setOnClickListener(this);
        RecyclerView list_contacts = (RecyclerView) findViewById(R.id.list_contacts);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        list_contacts.setLayoutManager(layoutManager);

        sosAdapter = new SOSAdapter(this, number, rl_empty);
        list_contacts.setAdapter(sosAdapter);

        getContacts();
    }

    private void getContacts() {
        service.getSOSContacts("GetSOSContacts", number).enqueue(new Callback<SOSResponse>() {
            @Override
            public void onResponse(Call<SOSResponse> call, Response<SOSResponse> response) {
                if (response.isSuccessful()) {
                    sosAdapter.resetItems();

                    if (response.isSuccessful()) {
                        SOSResponse sosResponse = response.body();

                        if (sosResponse.getResult().size() > 0) {
                            rl_empty.setVisibility(View.GONE);
                            sosAdapter.addItems(sosResponse.getResult());
                        } else {
                            rl_empty.setVisibility(View.VISIBLE);
                        }
                    }

                }
            }

            @Override
            public void onFailure(Call<SOSResponse> call, Throwable t) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {

            switch (requestCode) {
                case RESULT_PICK_CONTACT:
                    contactPicked(data);
                    break;
            }
        }
    }

    private void contactPicked(Intent data) {
        try {
            Uri uri = data.getData();
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);

            if (cursor != null) {
                cursor.moveToFirst();
                int phoneIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                String phoneNo = cursor.getString(phoneIndex);
                String name = cursor.getString(nameIndex);

                if (number != null) {
                    service.addSOSContact("InsertSOSContacts", number, phoneNo, name).enqueue(new Callback<SOSAddResponse>() {
                        @Override
                        public void onResponse(Call<SOSAddResponse> call, Response<SOSAddResponse> response) {
                            if (response.isSuccessful()) {
                                SOSAddResponse sosAddResponse = response.body();

                                if (sosAddResponse != null) {
                                    Toast.makeText(SOSActivity.this, sosAddResponse.getResult(), Toast.LENGTH_SHORT).show();
                                }

                                if (sosAddResponse.getSuccess()) {
                                    getContacts();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<SOSAddResponse> call, Throwable t) {

                        }
                    });
                }
            } else {
                Toast.makeText(this, "Failed to pick contact", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                break;
            case R.id.action_sos:
                if (DostChatApp.locationUpdateManager != null) {
                    Location location = DostChatApp.locationUpdateManager.getCurrentLocation();

                    if (location != null) {
                        String loc = location.getLatitude() + "," + location.getLongitude();
                        service.sendSOSContacts("SendSOSContacts", number, loc).enqueue(new Callback<SOSAddResponse>() {
                            @Override
                            public void onResponse(Call<SOSAddResponse> call, Response<SOSAddResponse> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    Toast.makeText(SOSActivity.this, response.body().getResult(), Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<SOSAddResponse> call, Throwable t) {

                            }
                        });
                    }
                }

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rl_empty:
            case R.id.fab_add_contact:
                Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                startActivityForResult(contactPickerIntent, RESULT_PICK_CONTACT);
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_sos, menu);
        return super.onCreateOptionsMenu(menu);
    }
}
