package com.example.bleconnected01;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {
    /**記得開手機定位啊(笑)*/
    /**別忘了你就職第一週被卡了四天的恥辱*/
    private Button btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.v("BT","這是首頁");
        btn = (Button) findViewById(R.id.nt01);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,DeviceScanActivity.class);
                startActivity(intent);

            }
        });




    }//onCreate()
}
