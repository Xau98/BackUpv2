package com.android.backup.fragment;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.android.backup.Dialog;
import com.android.backup.R;
import com.android.backup.RequestToServer;
import com.android.backup.activity.MainActivity;
import com.android.backup.code.Code;
import com.android.backup.handleFile;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class FragmentRestoring extends Fragment {

    TextView mCapationRestore;
    ImageButton mBTRestore;
    Dialog dialog;
    long mCapacity = 0;
    Callback mCallback;
    String mJsonData;
    Handler mHandler;
    public static final int MSG_ERRO = 14;
    public static final int MSG_SUCCES = 15;
    public static final int MSG_PASSWORNG = 16;
    public FragmentRestoring(Dialog dialog, long mCapacity) {
        this.dialog = dialog;
        this.mCapacity = mCapacity;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_restoreing, container, false);
        mCapationRestore = view.findViewById(R.id.show_capacity_restore);
        mBTRestore = view.findViewById(R.id.bt_confirm_restore);
        mCapationRestore.setText(Math.ceil((handleFile.KBToMB(mCapacity)) * 10) / 10 + " MB");
        mBTRestore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = getLayoutInflater();
                if (mCapacity != 0) {
                    showDiaglog();
                } else {
                    String title = "Bạn chưa chọn data dữ liệu để đồng bộ!";
                    dialog.showDialog(getContext(), inflater, title, false, 0);
                }
            }
        });

        mHandler = new Handler() {
            @SuppressLint("HandlerLeak")
            @Override
            public void handleMessage(@NonNull Message msg) {
                switch (msg.what) {
                    case MSG_SUCCES:
                        String title = "Bạn có chắc muốn đồng bộ dữ liệu hay không ?";
                        dialog.showDialog(getContext(), inflater, title, true, 0);
                        break;
                    case MSG_PASSWORNG:
                        String titlePassWrong = "Mật khẩu sai.";
                        dialog.showDialog(getContext(), inflater, titlePassWrong, false, 0);
                        break;
                    case MSG_ERRO:
                        Toast.makeText(getContext(), " Serrver lỗi !!! ", Toast.LENGTH_SHORT);
                        break;
                }
            }
        };

        mCallback = new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                mHandler.sendEmptyMessage(MSG_ERRO);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    mJsonData = response.body().string();
                    try {
                        JSONObject Jobject = new JSONObject(mJsonData);
                        if (Boolean.parseBoolean(Jobject.get("success").toString()) != false) {
                            mJsonData = Jobject.get("result").toString();
                            mHandler.sendEmptyMessage(MSG_SUCCES);
                        } else {
                            mHandler.sendEmptyMessage(MSG_PASSWORNG);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }else {
                    mHandler.sendEmptyMessage(MSG_ERRO);
                }
            }
        };
        return view;
    }

    public void showDiaglog(){
        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.dialog_confirm_code_email, null);
        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
        alert.setView(alertLayout);
        alert.setCancelable(false);
        EditText editText = alertLayout.findViewById(R.id.editext_code);
        editText.setTransformationMethod(new PasswordTransformationMethod());
        editText.setHint("Nhập password");
        TextView title = alertLayout.findViewById(R.id.title_dialog);
        title.setText("Vui lòng nhập mật khẩu");
        alert.setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        alert.setPositiveButton("Xác Nhận", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences sharedPref = getActivity().getSharedPreferences(MainActivity.SHAREPREFENCE, getActivity().MODE_PRIVATE);
                String mID_account = sharedPref.getString("id", null);
                String mToken = sharedPref.getString("token", null);
                String password = editText.getText().toString();
                if (password.equals("") || mID_account.isEmpty()) {
                    alert.create().dismiss();
                    Toast.makeText(getContext(), " Nhập thiếu thông tin ", Toast.LENGTH_SHORT);
                } else {
                    Code.setmPassword(password);
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("_id", mID_account);
                        jsonObject.put("token", mToken);
                        jsonObject.put("password", password);
                        String path = "verifypassword";
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
