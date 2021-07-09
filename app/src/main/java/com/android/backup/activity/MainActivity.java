package com.android.backup.activity;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.backup.code.Code;
import com.android.backup.service.ConditionBackup;
import com.android.backup.Permission;
import com.android.backup.R;
import com.android.backup.RequestToServer;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static android.widget.Toast.LENGTH_SHORT;

public class MainActivity extends Activity {
    public static final String TAG = "TienNVh";
    public static final String SHAREPREFENCE = "SHAREPREFENCE_ACCOUNT";
    /*    public static final String ID_CLIENT_GG = "797362158064-h1cjks1abj7vqphiu6rc0429jsd3puet.apps.googleusercontent.com";
        public static final String TOKENT_CLIENT = "tokent_client";*/
    public static final int MSG_LOGIN = 1;
    public static final int ERRO_LOGIN = 2;
    Callback mCallback;
    Handler mHandler;
    String mJsonData;
    Button bt_login, mBTRegister;
    ProgressBar mProgressBar;
    EditText mUsername, mPassword;
    private SharedPreferences mPreferences;
    TextView mBtForget;
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mProgressBar = findViewById(R.id.progressBar);
        bt_login = findViewById(R.id.bt_loginAC);
        mUsername = findViewById(R.id.username);
        mPassword = findViewById(R.id.password);
        mBTRegister = findViewById(R.id.bt_register);
        mPreferences = getSharedPreferences(SHAREPREFENCE, MODE_PRIVATE);
        Permission permission = new Permission(this, this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && permission.isReadStoragePermissionGranted() && permission.isWriteStoragePermissionGranted() && permission.isReadContactsPermissionGranted()
                && permission.isReadSMSPermissionGranted() && permission.isReadCallLogPermissionGranted()
        ) {
        }
        //encrypt: kn/wfUfSz+Xww5Luc9E/MenmERpJ+FWwurgY7VXz7XBKhbOfQlS5glVeZoOKfFxk//Tiennvh123@
        Log.d("Tiennvh", "onCreate: "+Code.decryptString("kn/wfUfSz+Xww5Luc9E/MenmERpJ+FWwurgY7VXz7XBKhbOfQlS5glVeZoOKfFxk","Tiennvh123@"));
        String id_account = mPreferences.getString("id", null);
        String token = mPreferences.getString("token", null);
        String idDevices = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        mBTRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), RegisterAcivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        bt_login.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                onLoginAcoount();
            }
        });


        mHandler = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                switch (msg.what) {
                    case MSG_LOGIN:
                        try {
                            JSONObject Jobject0 = new JSONObject(mJsonData);
                            String result = Jobject0.get("result").toString();
                            Log.d("Tiennvh", "handleMessage: "+ Jobject0);
                            if (Jobject0.get("success").toString().equals("true")) {
                                JSONObject Jobject = new JSONObject(result);
                                SharedPreferences.Editor editor = mPreferences.edit();
                                editor.putString("id", Jobject.getString("_id"));
                                editor.putString("name", Jobject.getString("username"));
                                editor.putString("token", Jobject.getString("token"));
                                editor.putString("email", Jobject.getString("email"));
                                editor.putString("date_create", Jobject.getString("date_create"));
                                editor.putString("key_secret", Jobject.getString("keysecret"));
                                editor.putString("id_devices", idDevices);
                                Log.d("Tiennvh", "handleMessage: "+ Jobject.getString("keysecret"));
                                editor.commit();
                                Intent intent = new Intent(getBaseContext(), HomePage.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                Toast.makeText(getBaseContext(), "Đăng nhập thành công", LENGTH_SHORT).show();
                                break;
                            } else {
                                if (mProgressBar != null)
                                    mProgressBar.setVisibility(View.GONE);
                                String resultWrong = Jobject0.get("result").toString();
                                Toast.makeText(getApplicationContext(), "Đăng nhập thất bại.  "+resultWrong, LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            if (mProgressBar != null)
                                mProgressBar.setVisibility(View.GONE);
                            Log.d("Tiennvh", "handleMessage: " + e);
                        }
                        break;
                    case ERRO_LOGIN:
                        mProgressBar.setVisibility(View.GONE);
                        if (id_account == null || token == null)
                            Toast.makeText(getApplicationContext(), "Lỗi server !!! ", LENGTH_SHORT).show();
                        break;
                }
            }
        };

        mCallback = new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                mHandler.sendEmptyMessage(ERRO_LOGIN);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

                if (response.isSuccessful()) {
                    mJsonData = response.body().string();
                    mHandler.sendEmptyMessage(MSG_LOGIN);
                } else {

                    mHandler.sendEmptyMessage(ERRO_LOGIN);
                }
            }
        };
        if (id_account != null && token != null) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("_id", id_account);
                jsonObject.put("token", token);
                String path = "logintoken";
                RequestToServer.post(path, jsonObject, mCallback);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            mProgressBar.setVisibility(View.GONE);
        }
    }

    // Bkav TienNVh : callback Permisson
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Permission.PER_READ:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //resume tasks needing this permission
                    Log.d("Tiennvh", "onRequestPermissionsResult:READ ");

                } else {
                    Log.d("Tiennvh", "onRequestPermissionsResult: FALSE");
                }
                break;
            case Permission.PER_WRITE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.v(TAG, "Permission: " + permissions[0] + "was " + grantResults[0]);
                    //resume tasks needing this permission
                    Log.d("Tiennvh", "onRequestPermissionsResult: WRITE ");
                } else {
                    Log.d("Tiennvh", "onRequestPermissionsResult: FALSE");
                }
                break;
        }
    }

    // Bkav TienNVh : login
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void onLoginAcoount() {
        String username = mUsername.getText().toString();
        String password = mPassword.getText().toString();
        if (ConditionBackup.isNetworkConnected(this)) {
            if (username.equals("") || password.equals("")) {
                Toast.makeText(this, "Nhập đầy đủ thông tin", LENGTH_SHORT).show();
            } else {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("username", username);
                    jsonObject.put("password", password);
                    String path = "loginaccount";
                    RequestToServer.post(path, jsonObject, mCallback);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            /*
           Intent intent = new Intent(getBaseContext(), HomePage.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            */
        } else {
            Toast.makeText(getBaseContext(), "Not Connect Internet", LENGTH_SHORT).show();
        }
    }


}