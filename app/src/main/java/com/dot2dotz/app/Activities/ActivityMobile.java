package com.dot2dotz.app.Activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;


import com.dot2dotz.app.Helper.SharedHelper;
import com.dot2dotz.app.R;
import com.dot2dotz.app.Utils.CommonUtils;
import com.dot2dotz.app.Utils.MyTextView;
import com.dot2dotz.app.Utils.Utilities;


public class ActivityMobile extends AppCompatActivity {

    ImageView backArrow;
    FloatingActionButton nextICON;
    EditText email;
    MyTextView register, forgetPassword;
    LinearLayout lnrBegin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobile);
        CommonUtils.setLanguage(ActivityMobile.this);

        if (Build.VERSION.SDK_INT > 15) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        email = findViewById(R.id.enter_ur_mailID);
        nextICON = findViewById(R.id.nextICON);
        backArrow = findViewById(R.id.backArrow);
        register = findViewById(R.id.register);
        forgetPassword = findViewById(R.id.forgetPassword);
        lnrBegin = findViewById(R.id.lnrBegin);

        nextICON.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    //if (email.getText().toString().equals("") || email.getText().toString().equalsIgnoreCase(getString(R.string.sample_mail_id))) {
                    if (email.getText().toString().equals("")) {
                        displayMessage(getString(R.string.mobile_number_empty));
                    } else if (email.getText().toString().length() < 10) {
                        displayMessage(getString(R.string.phone_validation));
                    } else {
                    /*if ((!isValidEmail(email.getText().toString()))) {
                        displayMessage(getString(R.string.email_validation));
                    } else {*/
                        Utilities.hideKeyboard(ActivityMobile.this);
                        SharedHelper.putKey(ActivityMobile.this, "email", email.getText().toString());
                        Intent mainIntent = new Intent(ActivityMobile.this, ActivityPassword.class);
                        startActivity(mainIntent);
                        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                        /* }*/
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedHelper.putKey(ActivityMobile.this, "password", "");
                Utilities.hideKeyboard(ActivityMobile.this);
                Intent mainIntent = new Intent(ActivityMobile.this, RegisterActivity.class);
                mainIntent.putExtra("isFromMailActivity", true);
                startActivity(mainIntent);
                overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
            }
        });

        forgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedHelper.putKey(ActivityMobile.this, "password", "");
                Utilities.hideKeyboard(ActivityMobile.this);
                Intent mainIntent = new Intent(ActivityMobile.this, ForgetPassword.class);
                mainIntent.putExtra("isFromMailActivity", true);
                startActivity(mainIntent);
            }
        });

    }

    public void displayMessage(String toastString) {
        try {
            Snackbar.make(getCurrentFocus(), toastString, Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, toastString, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.activity_back_in, R.anim.activity_back_out);
    }
}