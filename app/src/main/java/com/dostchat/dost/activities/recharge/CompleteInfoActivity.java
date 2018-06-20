package com.dostchat.dost.activities.recharge;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.dostchat.dost.R;
import com.dostchat.dost.activities.main.MainActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CompleteInfoActivity extends AppCompatActivity {

    @BindView(R.id.response)
    TextView txtResponse;

    private String response=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_info);

        ButterKnife.bind(this);
        Bundle extras = getIntent().getExtras();
        response= extras.getString("response");
        txtResponse.setText(response);
        txtResponse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(CompleteInfoActivity.this, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
        });
    }
}
