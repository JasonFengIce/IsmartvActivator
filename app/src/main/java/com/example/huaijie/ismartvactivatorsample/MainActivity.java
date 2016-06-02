package com.example.huaijie.ismartvactivatorsample;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.gson.Gson;

import cn.ismartv.activator.IsmartvActivator;
import cn.ismartv.activator.data.Result;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


    }

    public void active(View view) {
        IsmartvActivator activator = new IsmartvActivator(this);
        activator.setManufacture("sharp");
        activator.setKind("lcd_s3a01");
        activator.setLocation("SH");
        activator.execute(new IsmartvActivator.Callback() {
            @Override
            public void onSuccess(Result result) {

            }

            @Override
            public void onFailure(String msg) {

            }
        });
    }
}
