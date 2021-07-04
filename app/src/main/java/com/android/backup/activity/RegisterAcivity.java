package com.android.backup.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.android.backup.R;
import com.android.backup.RequestToServer;
import com.android.backup.code.Code;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class RegisterAcivity extends Activity {

    EditText mEmail, mPassword, mREPassword, mName, mUsername;
    Button mBTRegister;
    Callback mCallback;
    Handler mHandler;
    String mJsonData;
    String mToken;
    String mID_account;
    public static final int MSG_SUCCESS_SEND = 6;
    public static final int MSG_SUCCESS = 8;
    public static final int MSG_ERROR = 7;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity);
        mName = findViewById(R.id.register_name);
        mUsername = findViewById(R.id.register_username);
        mEmail = findViewById(R.id.register_email);
        mPassword = findViewById(R.id.register_password);
        mREPassword = findViewById(R.id.register_prepassword);
        mBTRegister = findViewById(R.id.bt_confirm_regidter);
        mHandler = new Handler() {
            @SuppressLint("HandlerLeak")
            @Override
            public void handleMessage(@NonNull Message msg) {
                switch (msg.what) {
                    case MSG_SUCCESS_SEND:
                        try {
                            JSONObject Jobject = new JSONObject(mJsonData);
                            mID_account = (String) Jobject.get("_id");
                            mToken = (String) Jobject.get("token");
                            showDiaglog();

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getBaseContext(), "Lỗi:code " + e, Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case MSG_SUCCESS:
                        try {
                            JSONObject Jobject = new JSONObject(mJsonData);
                            String success = Jobject.get("success").toString();
                            Log.d("Tiennvh", "handleMessage: "+success);
                            if (success.equals(true)) {
                                onBackPressed();
                                break;
                            } else {
                                Toast.makeText(getBaseContext(), "Lỗi: code " + Jobject.get("code") + "  Reasion " + Jobject.get("result"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getBaseContext(), "Lỗi:code " + e, Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case MSG_ERROR:
                        Toast.makeText(getBaseContext(), "Lỗi server ", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };
        mCallback = new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                mHandler.sendEmptyMessage(MSG_ERROR);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    mJsonData = response.body().string();
                    Log.d("Tiennvh", "onResponse: "+mJsonData);
                    try {
                        JSONObject Jobject = new JSONObject(mJsonData);
                        boolean is_success = Jobject.get("success").toString().equals("true");
                        if (!Jobject.get("result").toString().equals("null")&& is_success) {
                            mJsonData = Jobject.get("result").toString();
                            mHandler.sendEmptyMessage(MSG_SUCCESS_SEND);
                        } else {
                            mHandler.sendEmptyMessage(MSG_SUCCESS);
                        }
                    } catch (JSONException e) {
                        mHandler.sendEmptyMessage(MSG_ERROR);
                        e.printStackTrace();
                    }
                }else {
                    mHandler.sendEmptyMessage(MSG_ERROR);
                }
            }
        };

        mBTRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = mName.getText().toString();
                String username = mUsername.getText().toString();
                String email = mEmail.getText().toString();
                String pass = mPassword.getText().toString();
                String repass = mREPassword.getText().toString();
                if (email.equals("") | pass.equals("") | repass.equals("")) {
                    Toast.makeText(getBaseContext(), " Nhập thiếu thông tin ", Toast.LENGTH_SHORT);
                    return;
                }
                if (pass.equals(repass)) {
                    // sinh ma
                    UUID uuid = UUID.randomUUID();
                    String secretKey = pass;
                    String originalString = uuid.toString();
                    String encryptedString = Code.encryptString(originalString, secretKey);
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("email", email);
                        jsonObject.put("name", name);
                        jsonObject.put("username", username);
                        jsonObject.put("password", pass);
                        jsonObject.put("keysecret", encryptedString);
                        String path = "register";
                        RequestToServer.post(path, jsonObject, mCallback);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(getBaseContext(), " Mật khẩu không khớp ", Toast.LENGTH_SHORT);
                }
            }
        });
    }

    public void showDiaglog() {
        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.dialog_confirm_code_email, null);
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setView(alertLayout);
        alert.setCancelable(false);
        EditText tile = alertLayout.findViewById(R.id.editext_code);

        alert.setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getBaseContext(), "Cancel clicked", Toast.LENGTH_SHORT).show();
            }
        });

        alert.setPositiveButton("Xác Nhận", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String email = mEmail.getText().toString();
                String code = tile.getText().toString();
                if (code.equals("") || mID_account.isEmpty() || mToken.isEmpty() || email.isEmpty()) {
                    alert.create().dismiss();
                    Toast.makeText(getBaseContext(), " Nhập thiếu thông tin ", Toast.LENGTH_SHORT);
                } else {

                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("_id", mID_account);
                        jsonObject.put("token", mToken);
                        jsonObject.put("code", code);
                        jsonObject.put("email", email);
                        jsonObject.put("type", "signup");
                        String path = "user/verify";
                        RequestToServer.post(path, jsonObject, mCallback);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        alert.show();
    }
}
