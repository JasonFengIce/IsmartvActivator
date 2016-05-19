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
        new IsmartvActivator(this, new IsmartvActivator.Callback() {
            @Override
            public void onSuccess(Result result) {
                Log.i(TAG, "result: " + new Gson().toJson(result));
            }

            @Override
            public void onFailure(String msg) {

            }
        }).execute();
    }
}
