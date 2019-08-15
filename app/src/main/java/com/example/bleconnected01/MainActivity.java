package com.example.bleconnected01;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.Objects;

public class MainActivity extends Activity {
    private static final int REQUEST_FINE_LOCATION_PERMISSION = 102;
    /**記得開手機定位啊(笑)*/
    /**
     * 別忘了你就職第一週被卡了四天的恥辱
     */
    private Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn = (Button) findViewById(R.id.nt01);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int hasGone = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
            if (hasGone != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_FINE_LOCATION_PERMISSION);
            }
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                            Intent intent = new Intent(MainActivity.this, DeviceScanActivity.class);
                            startActivity(intent);
                            finish();
                        }

                    });

            }

        }//onCreate()

    }



