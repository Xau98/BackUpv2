package com.android.backup.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.android.backup.R;
import com.android.backup.RequestToServer;
import com.android.backup.activity.MainActivity;
import com.android.backup.handleFile;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ProfileAccount extends Activity {
    Button mBTLogout, mBTResetPassword;
    TextView mName, mEmail, mCreateDate, mID;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_account);
        mBTLogout = findViewById(R.id.logout_account);
        mID = findViewById(R.id.id_account);
        mName = findViewById(R.id.name_account);
        mEmail = findViewById(R.id.email_account);
        mCreateDate = findViewById(R.id.date_create_account);
        mBTResetPassword = findViewById(R.id.reset_password);
        SharedPreferences sharedPref = getSharedPreferences(MainActivity.SHAREPREFENCE, MODE_PRIVATE);
        mName.setText(sharedPref.getString("name", "Name_account"));
        mID.setText(sharedPref.getString("id", null));
        mEmail.setText(sharedPref.getString("email", "email@gmail.com"));
        mCreateDate.setText(sharedPref.getString("date_create", "24/05/1998"));
        Callback mCallback = new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String mJsonData = response.body().string();
                    if (!mJsonData.equals("True")) {
                        Toast.makeText(getBaseContext(), "Đăng xuất thất bại", Toast.LENGTH_SHORT).show();

                    } else {
                        String path = handleFile.PATH_ROOT + "/" + sharedPref.getString("id_devices", null) + ".txt";
                        handleFile.readHistoryDownloadFile(path);
                        //Bkav TienNVh :Xoa SharePre
                        sharedPref.edit().clear().commit();
                        Intent intent = new Intent(getBaseContext(), MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                }
            }
        };
        mBTLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("id", sharedPref.getString("id", null));
                    jsonObject.put("token", sharedPref.getString("token", null));
                    String path = "logout";
                    RequestToServer.post(path, jsonObject, mCallback);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        mBTResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), ResetPassword.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
    }
}
