package cn.ismartv.activator;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import cn.ismartv.activator.core.http.HttpClientAPI;
import cn.ismartv.activator.core.http.HttpClientAPI.ExcuteActivator;
import cn.ismartv.activator.core.rsa.RSACoder;
import cn.ismartv.activator.core.rsa.SkyAESTool2;
import cn.ismartv.activator.data.Result;
import cn.ismartv.activator.utils.MD5Utils;
import cn.ismartv.log.interceptor.HttpLoggingInterceptor;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Callback;
import retrofit2.GsonConverterFactory;
import retrofit2.Response;
import retrofit2.Retrofit;


public class Activator {
    private static final String SKY_HOST = "http://peachtest.tvxio.com";
    private static final int DEFAULT_CONNECT_TIMEOUT = 2;
    private static final int DEFAULT_READ_TIMEOUT = 5;

    private static final String SIGN_FILE_NAME = "sign1";
    private static final String TAG = "Activator";
    private String sn, manufacture, kind, version, fingerprint, locationInfo;
    private Context mContext;
    private int actvieTryTime = 0;
    public boolean iswaiting;
    private Retrofit SKY_Retrofit;

    public interface ActiveCallback {
        void onSuccess(Result result);

        void onFailed(String message);
    }

    public Activator(Context context, String host) {
        mContext = context;
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(DEFAULT_CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(DEFAULT_READ_TIMEOUT, TimeUnit.SECONDS)
                .addInterceptor(interceptor)
                .build();

        SKY_Retrofit = new Retrofit.Builder()
                .client(client)
                .baseUrl(host)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public Activator(Context context) {
        this(context, SKY_HOST);
    }

    public void active(String manufacture, String kind, String version, String locationInfo, ActiveCallback activeCallback) {
        actvieTryTime = actvieTryTime + 1;
        iswaiting = true;
        if (actvieTryTime > 2) {
            iswaiting = false;
            activeCallback.onFailed("激活失败!!!");
        }

        this.locationInfo = locationInfo;
        try {
            this.sn = MD5Utils.encryptByMD5(getDeviceId() + Build.SERIAL);
            this.manufacture = manufacture;
            this.kind = kind.toLowerCase();
            this.version = version;
            this.fingerprint = MD5Utils.encryptByMD5(this.sn);
            if (!mContext.getFileStreamPath(SIGN_FILE_NAME).exists()) {
                FileOutputStream fs = mContext.openFileOutput("sn", Context.MODE_WORLD_READABLE);
                fs.write(this.sn.getBytes());
                fs.flush();
                fs.close();
                getLicence(activeCallback);
            } else {
                String content;
                File snfile = mContext.getFileStreamPath("sn");
                if (!snfile.exists()) {
                    content = sn;
                } else {
                    FileInputStream inputStream = mContext.openFileInput("sn");
                    int length = inputStream.available();
                    byte[] bytes = new byte[length];
                    inputStream.read(bytes);
                    content = new String(bytes, "UTF-8");
                    inputStream.close();
                }
                this.sn = content;
                this.fingerprint = MD5Utils.encryptByMD5(this.sn);
                activator(sn, manufacture, kind, version, fingerprint, activeCallback);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getLicence(final ActiveCallback activeCallback) {
        HttpClientAPI.GetLicence client = SKY_Retrofit.create(HttpClientAPI.GetLicence.class);
        client.excute(fingerprint, sn, manufacture, "1").enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Response<ResponseBody> response) {
                try {
                    if (response.body() != null) {
                        FileOutputStream fs = mContext.openFileOutput(SIGN_FILE_NAME, Context.MODE_WORLD_READABLE);
                        fs.write(response.body().bytes());
                        fs.flush();
                        fs.close();
                        activator(sn, manufacture, kind, version, fingerprint, activeCallback);
                    } else {
                        activeCallback.onFailed("get licence failure!!!");
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                iswaiting = false;
                activeCallback.onFailed("get licence failure!!!");
            }
        });
    }


    private void activator(String sn, final String manufacture, final String kind, final String version, String fingerprint, final ActiveCallback activeCallback) {
        String result = decrypt(sn, mContext.getFileStreamPath(SIGN_FILE_NAME).getAbsolutePath(), new DecryptCallback() {
            @Override
            public void onFailure() {
                active(manufacture, kind, version, locationInfo, activeCallback);
            }
        });
        String publicKey;
        try {
            publicKey = result.split("\\$\\$\\$")[1];
        } catch (Exception e) {
            iswaiting = false;
            activeCallback.onFailed(e.getMessage());
            return;
        }

        String sign = "ismartv=201415&kind=" + kind + "&sn=" + sn;
        String rsaEnResult = ecodeWithPublic(sign, publicKey);

        ExcuteActivator activator = SKY_Retrofit.create(ExcuteActivator.class);
        activator.excute(sn, manufacture, kind, version, rsaEnResult, fingerprint, "v3_0", getAndroidDevicesInfo()).enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Response<Result> response) {
                Result result = response.body();
                if (response.errorBody() != null) {
                    iswaiting = false;
                    activeCallback.onFailed("激活失败");
                    Log.e(TAG, response.errorBody().toString());
                } else {
                    iswaiting = false;
                    activeCallback.onSuccess(result);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                iswaiting = false;
                activeCallback.onFailed("激活失败");
            }
        });
    }

    private String getAndroidDevicesInfo() {
        try {
            JSONObject json = new JSONObject();
            String versionName = getAppVersionName();
            String serial = Build.SERIAL;
            String deviceId = getDeviceId();
            String ID = Build.ID;
            String hh = Build.ID + "//" + Build.SERIAL;
            Log.i("zjqtestismartv", "hh");
            MD5Utils.encryptByMD5(Build.SERIAL + Build.ID);
            json.put("fingerprintE", MD5Utils.encryptByMD5(Build.SERIAL + Build.ID));
            json.put("fingerprintD", hh);
            json.put("versionName", versionName);
            json.put("serial", serial);
            json.put("deviceId", deviceId);
            return json.toString() + "///" + this.locationInfo;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    private String getAppVersionName() {
        String versionName = "";
        try {
            PackageManager pm = mContext.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(mContext.getPackageName(), 0);
            versionName = pi.versionName;
            if (versionName == null || versionName.length() <= 0) {
                return "";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return versionName;
    }

    private String getDeviceId() {
        String deviceId = null;
        try {
            TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
            deviceId = tm.getDeviceId();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return deviceId;
    }

    public String decrypt(String key, String ContentPath, DecryptCallback callback) {
        File file = new File(ContentPath);
        if (file.exists()) {
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                int count = fileInputStream.available();
                byte[] bytes = new byte[count];
                fileInputStream.read(bytes);
                fileInputStream.close();
                return SkyAESTool2.decrypt(key.substring(0, 16), Base64.decode(bytes, Base64.URL_SAFE));
            } catch (Exception e) {
                file.delete();
                Log.e(TAG, "NativeManager decrypt Exception");
                callback.onFailure();
                return "error";
            }
        }
        return "";
    }

    public interface DecryptCallback {
        void onFailure();
    }

    private String ecodeWithPublic(String string, String publicKey) {
        try {
            String input = MD5Utils.encryptByMD5(string);
            byte[] rsaResult = RSACoder.encryptByPublicKey(input.getBytes(), publicKey);
            return Base64.encodeToString(rsaResult, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
