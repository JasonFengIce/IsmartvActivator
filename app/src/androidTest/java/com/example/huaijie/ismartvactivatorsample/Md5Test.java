package com.example.huaijie.ismartvactivatorsample;

import android.test.AndroidTestCase;
import android.util.Log;

import cn.ismartv.boringssl.Md5;

/**
 * Created by huibin on 5/19/16.
 */
public class Md5Test extends AndroidTestCase {
    private static final String TAG = "UrlTest";

    public void testMd5() {
        for (; ; ) {
            String result = Md5.md5("hello world!!!");
            Log.i(TAG, "result: " + result);
            String result2 = Md5.md5("hello world!!!111");
            Log.i(TAG, "result2: " + result2);
        }
    }
}
