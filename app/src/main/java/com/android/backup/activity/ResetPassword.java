package com.android.backup.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.backup.R;
import com.android.backup.RequestToServer;
import com.android.backup.code.Code;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static android.widget.Toast.LENGTH_SHORT;

public class ResetPassword extends Activity {

    Button bt_reset;
    String keynew;
    EditText mPasswordOld, mPasswordNew, mRetypePassword;
    public static final int MSG_SUCCESS = 1;
    public static final int MSG_ERROR = 0;
    String jsonData;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reset_password);
        bt_reset = findViewById(R.id.bt_reset);
        mPasswordOld = findViewById(R.id.pass_old);
        mPasswordNew = findViewById(R.id.pass_new);
        mRetypePassword = findViewById(R.id.retype_pass);
        SharedPreferences sharedPref = getSharedPreferences(MainActivity.SHAREPREFENCE, MODE_PRIVATE);


        Handler mHandler = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                switch (msg.what) {
                    case MSG_SUCCESS:
                        //Bkav TienNVh :Xoa SharePre
                        sharedPref.edit().clear().commit();
                        Intent intent = new Intent(getBaseContext(), MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        break;
                    case MSG_ERROR:
                        Toast.makeText(getBaseContext(), "Lỗi đường truyền! ", LENGTH_SHORT).show();
                        break;
                }
            }
        };
        Callback mCallback = new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                mHandler.sendEmptyMessage(MSG_ERROR);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    jsonData = response.body().string();
                    mHandler.sendEmptyMessage(MSG_SUCCESS);
                } else {
                    mHandler.sendEmptyMessage(MSG_ERROR);
                }
            }
        };
        bt_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String passwordold = mPasswordOld.getText().toString();
                String passwordnew = mPasswordNew.getText().toString();
                String retypepassword = mRetypePassword.getText().toString();
                if (passwordold.equals("") || passwordnew.equals("") || retypepassword.equals("")) {
                    Toast.makeText(getBaseContext(), "Nhập thiếu thông tin", Toast.LENGTH_LONG).show();
                } else {
                    if (retypepassword.equals(passwordnew)) {
                        String keysecret = sharedPref.getString("key_secret", null);
                        if (keysecret != null) {
                            String getkeyencode = Code.decryptString(keysecret, passwordold);
                            if (getkeyencode != null) {
                                keynew = Code.encryptString(getkeyencode, passwordold);
                                JSONObject jsonObject = new JSONObject();

                                try {
                                    jsonObject.put("_id", sharedPref.getString("id", null));
                                    jsonObject.put("token", sharedPref.getString("token", null));
                                    jsonObject.put("passold", passwordold);
                                    jsonObject.put("passnew", passwordnew);
                                    jsonObject.put("keysecret", keynew);
                                    String path = "resetpassword";
                                    RequestToServer.post(path, jsonObject, mCallback);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                Toast.makeText(getBaseContext(), "Lỗi hệ thống, vui lòng đăng xuất và đăng nhập lại! ", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(getBaseContext(), "Lỗi hệ thống, vui lòng đăng xuất và đăng nhập lại! ", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(getBaseContext(), "Mật khẩu nhập lại không đúng!", Toast.LENGTH_LONG).show();
                    }
                }


            }
        });

    }
}
