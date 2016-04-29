package com.example.huaijie.ismartvactivatorsample;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.google.gson.Gson;

import cn.ismartv.activator.Activator;
import cn.ismartv.activator.data.Result;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Activator activator = new Activator(this);

        activator.active(Build.BRAND, Build.PRODUCT.replaceAll(" ", "_").toLowerCase(), "1", "SH", new Activator.ActiveCallback() {
            @Override
            public void onSuccess(Result result) {
                Log.i(TAG, "active: " + new Gson().toJson(result));
            }

            @Override
            public void onFailed(String message) {

            }
        });

    }
}
