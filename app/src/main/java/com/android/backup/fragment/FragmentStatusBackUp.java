package com.android.backup.fragment;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.PasswordTransformationMethod;
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
import com.android.backup.fragment.FragmentBackuping;
import com.android.backup.handleFile;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class FragmentStatusBackUp extends Fragment {
    ImageButton mBTBackup;
    TextView mShowCapacity;
    FragmentBackuping mFragmentBackuping;
    boolean isStatus;
    long mCapacity = 0;
    Dialog dialog;
    Callback mCallback;
    String mJsonData;
    Handler mHandler;
    public static final int MSG_ERRO = 14;
    public static final int MSG_SUCCES = 15;
    public static final int MSG_PASSWORNG = 16;
    public FragmentStatusBackUp(Dialog dialog, long capacity) {
        this.dialog = dialog;
        mCapacity = capacity;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_choose, container, false);
        mBTBackup = view.findViewById(R.id.bt_confirm_save);
        mShowCapacity = view.findViewById(R.id.show_capacity);
        mShowCapacity.setText(Math.ceil((handleFile.KBToMB(mCapacity)) * 10) / 10 + " MB");
        mBTBackup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater inflater = getLayoutInflater();
                if (!mShowCapacity.getText().equals("0")) {
                    showDiaglog();
                } else {
                    dialog.showDialog(getContext(), inflater, "Vui l??ng ch???n th?? m???c backup ", false, 0);
                }
            }
        });
        mHandler = new Handler() {
            @SuppressLint("HandlerLeak")
            @Override
            public void handleMessage(@NonNull Message msg) {
                switch (msg.what) {
                    case MSG_SUCCES:
                        String title = "H??y x??c nh???n b???n mu???n b???t ?????u qu?? tr??nh sao l??u d??? li???u ?";
                        dialog.showDialog(getContext(), inflater, title, true, 0);
                        break;
                    case MSG_PASSWORNG:
                        String titlePassWrong = "M???t kh???u sai.";
                        dialog.showDialog(getContext(), inflater, titlePassWrong, false, 0);
                        break;
                    case MSG_ERRO:
                        Toast.makeText(getContext(), " Serrver l???i !!! ", Toast.LENGTH_SHORT);
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
        editText.setHint("Nh???p password");
        TextView title = alertLayout.findViewById(R.id.title_dialog);
        title.setText("Vui l??ng nh???p m???t kh???u");
        alert.setNegativeButton("H???y", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        alert.setPositiveButton("X??c Nh???n", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences sharedPref = getActivity().getSharedPreferences(MainActivity.SHAREPREFENCE, getActivity().MODE_PRIVATE);
                String mID_account = sharedPref.getString("id", null);
                String mToken = sharedPref.getString("token", null);
                String password = editText.getText().toString();
                if (password.equals("") || mID_account.isEmpty()) {
                    alert.create().dismiss();
                    Toast.makeText(getContext(), " Nh???p thi???u th??ng tin ", Toast.LENGTH_SHORT);
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
