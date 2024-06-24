package com.osaka.examples;

import android.content.Intent;
import android.net.Uri;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.osaka.sdk.Adjust;

public class ServiceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service);

        Intent intent = getIntent();
        Uri data = intent.getData();
        Adjust.appWillOpenUrl(data, getApplicationContext());
    }

    public void onServiceClick(View v) {
        Intent intent = new Intent(this, ServiceExample.class);
        startService(intent);
    }

    public void onReturnClick(View v) {
        finish();
    }
}
