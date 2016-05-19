package com.example.huaijie.ismartvactivatorsample;

import android.test.AndroidTestCase;
import android.util.Base64;
import android.util.Log;

import cn.ismartv.activator.core.rsa.SkyAESTool2;

/**
 * Created by huibin on 5/19/16.
 */
public class ActiveTest extends AndroidTestCase {
    private static final String TAG = "ActiveTest";

    String content = "6agAVjgwOrsy5dSZeD7WwL5X7N2dDRUGVH1lnKGSMMgpCvPgqkL4Q1Go8liX_PBAbVo8vhUOO_fVmdaaHfIl8xwGCzweAcuxR00Xpn2RiOZh677zETe-EwuYjjCRGBumAoKqnmWt6bTvbtFlNiVdgzvTClRYul7N_h8YKsN5B8Vh67yG07ZHJHA7wi2w1bAsYKB-lqWbaPDcppLxGLAr9C5l4N8KV0kiG2TGPgLENftC-KlEtQ-A81dy5L2IMgCOMRG_QBsSv9Ty9dwihXVTsnaunojEVP8GlVR05PQStfXDBopZxXMuMu1VZprzcDoddnDFtouSlYXkuglyobQkrcaIiEj6cSE6bm6J0j4LdMJl0jGIzFEeMYyvJ0VKvdHc";


    public void testDescrypt() {
        for (; ; ) {
            String result = SkyAESTool2.decrypt("a3a6f8d31054d3e6127f9c78e69fb528".substring(0, 16), Base64.decode(content, Base64.URL_SAFE));
            Log.i(TAG, "result: " + result);
        }
    }
}
