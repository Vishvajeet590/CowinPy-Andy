package com.vishvajeet590.python.console;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.chaquo.python.console.R;

public class Launch extends AppCompatActivity {
    Button grant,skip;
    private static final int SMS_PERMISSION_CODE = 100;
     boolean res;
     TextView text1,text2,text3;
     ImageView image;
     ProgressBar bar ;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        ConstraintLayout layout = findViewById(R.id.launchLa);
        grant = findViewById(R.id.grant);
        skip = findViewById(R.id.skip);
        text1 = findViewById(R.id.textView2);
        text2 = findViewById(R.id.textView3);
        text3 = findViewById(R.id.textView4);
        image = findViewById(R.id.imageView);
        bar = findViewById(R.id.progressBar);

        boolean result = false;
        result = checkPermission(Manifest.permission.RECEIVE_SMS,SMS_PERMISSION_CODE);

        if (result == false){
            grant.setVisibility(View.VISIBLE);
            skip.setVisibility(View.VISIBLE);
            text1.setVisibility(View.VISIBLE);
            text2.setVisibility(View.VISIBLE);
            text3.setVisibility(View.VISIBLE);
            image.setVisibility(View.VISIBLE);
            bar.setVisibility(View.INVISIBLE);
        }

        grant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean r = checkPermission(Manifest.permission.RECEIVE_SMS,SMS_PERMISSION_CODE);
            }
        });

        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNewActivity();
            }
        });
    }

    public boolean checkPermission(String permission, int requestCode)
    {
        res = false;
        boolean result = false;
        // Checking if permission is not granted
        if (ContextCompat.checkSelfPermission(Launch.this, permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(Launch.this, new String[] { permission }, requestCode);
        }
        else {
            res = true;
            result = true;
            openNewActivity();
            //Toast.makeText(Launch.this, "Permission already granted", Toast.LENGTH_SHORT).show();
        }
        return result;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == SMS_PERMISSION_CODE) {

            // Checking whether user granted the permission or not.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                // Showing the toast message
                Toast.makeText(Launch.this, "SMS Permission Granted", Toast.LENGTH_SHORT).show();
                res = true;
                openNewActivity();
            } else {
                res = false;
                Toast.makeText(Launch.this, "SMS Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
    public void openNewActivity(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}