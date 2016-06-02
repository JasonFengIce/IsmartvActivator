package com.example.huaijie.ismartvactivatorsample;

import android.test.AndroidTestCase;
import android.util.Log;

import cn.ismartv.activator.IsmartvActivator;

/**
 * Created by huibin on 5/19/16.
 */
public class Md5Test extends AndroidTestCase {
    private static final String TAG = "UrlTest";

    public void testMd5() {
        String mac = new IsmartvActivator(getContext()).getMacAddress();
        Log.i(TAG, "mac: " + mac);

    }
}
