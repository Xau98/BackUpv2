package com.android.backup.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.backup.code.AsyncTaskDownload;
import com.android.backup.code.AsyncTaskUpload;
import com.android.backup.code.Code;
import com.android.backup.ItemListRestore;
import com.android.backup.RequestToServer;
import com.android.backup.adapter.AdapterItemFile;
import com.android.backup.Dialog;
import com.android.backup.FileItem;
import com.android.backup.fragment.FragmentBackuping;
import com.android.backup.fragment.FragmentRestoring;
import com.android.backup.fragment.FragmentStatusBackUp;
import com.android.backup.R;
import com.android.backup.handleFile;
import com.android.backup.service.ServiceAutoBackup;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import javax.crypto.NoSuchPaddingException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static android.widget.Toast.LENGTH_SHORT;

public class BackupActivity extends AppCompatActivity implements Dialog.onConfirmBackup, AdapterItemFile.isChooseFolder {
    FragmentStatusBackUp fragmentStatusBackUp;
    FragmentRestoring mFragmentRestoring;
    FragmentBackuping fragmentBackuping;
    RecyclerView mRecyclerView;
    ArrayList<FileItem> mListAllFile;
    Button mBTBackHome;
    ImageButton mBTUpdateName;
    EditText mNameBackup;
    Dialog dialog;
    Callback mCallback;
    boolean isRestore = false;
    long mTotalCapacityChecked = 0;
    ArrayList<FileItem> mListFileChecked;
    TextView mNameTab, mShowDateBackup;
    ItemListRestore itemListRestore;
    Handler mHandler;
    String mJsonData;
    AdapterItemFile adapterListFile;
    SharedPreferences mSharedPref;
    public static final int MSG_LISTDATA = 9;
    public static final int MSG_ERROR = 10;
    ServiceAutoBackup mServiceBackup;
    boolean mBound = false;
    public ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            ServiceAutoBackup.BackupBinder binder = (ServiceAutoBackup.BackupBinder) iBinder;
            mServiceBackup = binder.getMusicBinder();
            Log.d("Tiennvh", "onServiceConnected: " + mServiceBackup);
            mBound = true;
            mServiceBackup.setmCallbackService(new ServiceAutoBackup.callbackService() {
                @Override
                public void callbackFinish(ArrayList<FileItem> mListSelected) {
                    //Bkav TienNVh :
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AdapterItemFile adapterListFile = new AdapterItemFile(getApplicationContext(), mListSelected, false, false);
                            mRecyclerView.setAdapter(adapterListFile);
                        }
                    });
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBound = false;
            Log.d("Tiennvh", "onServiceDisconnected: " + componentName);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.backup_activity);
        dialog = new Dialog();
        dialog.setConfirmListener(this);
        mNameBackup = findViewById(R.id.edittext_name_backup);
        mNameTab = findViewById(R.id.name_tab_backup_activity);
        mBTUpdateName = findViewById(R.id.bt_change_name);
        mShowDateBackup = findViewById(R.id.show_date_backup);
        mListFileChecked = new ArrayList<>();
        mRecyclerView = findViewById(R.id.recyclerview_backup);
        mListAllFile = new ArrayList<>();
        Intent intent = new Intent(this, ServiceAutoBackup.class);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
        startForegroundService(intent);
        mHandler = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                switch (msg.what) {
                    case MSG_LISTDATA:
                        if (!mJsonData.equals("False")) {
                            try {
                                JSONObject Jobject = new JSONObject(mJsonData);
                                JSONArray listData = Jobject.getJSONArray("list2");
                                for (int i = 0; i < listData.length(); i++) {
                                    String ob = listData.get(i).toString();
                                    JSONObject Jobject1 = new JSONObject(ob);
                                    String name = Jobject1.getString("name");
                                    String name1 = name.substring(0, name.length() - 4);
                                    long size = Long.parseLong(Jobject1.getString("size"));
                                    FileItem fileItem = new FileItem(name1, itemListRestore.getPath() + "/" + name, size);
                                    mListAllFile.add(fileItem);
                                    adapterListFile.notifyDataSetChanged();
                                }
                            } catch (JSONException e) {
                                Log.d("Tiennvh", "ERROR: " + e);
                            }
                            break;
                        } else {
                            Toast.makeText(getApplicationContext(), "Đăng nhập thất bại ", LENGTH_SHORT).show();
                        }
                        break;
                    case MSG_ERROR:
                        Toast.makeText(getApplicationContext(), "Server error!", LENGTH_SHORT).show();
                        break;
                }
            }
        };
        mCallback = new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                mHandler.sendEmptyMessage(MSG_ERROR);
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    mJsonData = response.body().string();
                    if (!mJsonData.equals("True"))
                        mHandler.sendEmptyMessage(MSG_LISTDATA);
                } else {
                    mHandler.sendEmptyMessage(MSG_ERROR);
                }
            }
        };
        mSharedPref = getSharedPreferences(MainActivity.SHAREPREFENCE, MODE_PRIVATE);
        String id = mSharedPref.getString("id", null);
        String token = mSharedPref.getString("token", null);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            isRestore = bundle.getBoolean("restore");
        }
        if (isRestore) {
            itemListRestore = (ItemListRestore) bundle.getSerializable("ItemListRestore");
            mNameBackup.setText(itemListRestore.getName());
            mShowDateBackup.setText(itemListRestore.getDateBackup());
            mNameTab.setText(bundle.getString("NameTab"));
            mBTUpdateName.setVisibility(View.VISIBLE);
            mFragmentRestoring = new FragmentRestoring(dialog, 0);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.FagmentBackup, mFragmentRestoring).commit();
            adapterListFile = new AdapterItemFile(this, mListAllFile, true, true);
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("id", id);
                jsonObject.put("token", token);
                jsonObject.put("path", itemListRestore.getPath());
                String path = "getlistdata";
                RequestToServer.post(path, jsonObject, mCallback);
            } catch (JSONException e) {
                e.printStackTrace();
                Log.d("Tiennvh", "onCallbackBackup: " + e);
            }

        } else {
            String nameDevice = android.os.Build.MODEL;
            mNameBackup.setText("Bản sao lưu của " + nameDevice);
            initFile();
            mBTUpdateName.setVisibility(View.GONE);
            fragmentStatusBackUp = new FragmentStatusBackUp(dialog, 0);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.FagmentBackup, fragmentStatusBackUp).commit();
            adapterListFile = new AdapterItemFile(this, mListAllFile, true, false);
        }
        mBTUpdateName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("id", id);
                    jsonObject.put("token", token);
                    jsonObject.put("newname", mNameBackup.getText().toString());
                    jsonObject.put("id_history", itemListRestore.getID() + "");
                    String path = "renamebackup";
                    RequestToServer.post(path, jsonObject, mCallback);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("Tiennvh", "onCallbackBackup: " + e);
                }
            }
        });

        mRecyclerView.setAdapter(adapterListFile);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapterListFile.setChooseFolder((AdapterItemFile.isChooseFolder) this);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        mBTBackHome = findViewById(R.id.bt_backhome);
        mBTBackHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void initFile() {
        mListAllFile = handleFile.loadFile(handleFile.PATH_ROOT);

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onConfirm(int type) {
        if (type == 0) {
            if (isRestore) {
                fragmentBackuping = new FragmentBackuping(mListFileChecked, mServiceBackup, dialog, true, null);
                fragmentBackuping.setTotalCapacity(mTotalCapacityChecked);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.FagmentBackup, fragmentBackuping).commit();
                AdapterItemFile adapterListFile = new AdapterItemFile(this, mListFileChecked, false, true);
                mRecyclerView.setAdapter(adapterListFile);
            } else {
                mShowDateBackup.setVisibility(View.VISIBLE);
                mShowDateBackup.setText(getDateCurrent());
                fragmentBackuping = new FragmentBackuping(mListFileChecked, mServiceBackup, dialog, false, mNameBackup.getText().toString());
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.FagmentBackup, fragmentBackuping).commit();
                AdapterItemFile adapterListFile = new AdapterItemFile(this, mListFileChecked, false, false);
                mRecyclerView.setAdapter(adapterListFile);
            }
            mBTUpdateName.setVisibility(View.GONE);
            mNameBackup.setEnabled(false);
        } else {
            Log.d("Tiennvh", "onConfirm: " + type);
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void getTotalCapacity(FileItem fileItem, boolean ischecked) {
        if (isRestore) {
            if (handleFile.duplicateFileItem(mListFileChecked, fileItem) && ischecked) {
                mListFileChecked.add(fileItem);
                mTotalCapacityChecked += fileItem.getSize();
            }
            if (!ischecked) {
                mListFileChecked = handleFile.removeFileItem(mListFileChecked, fileItem);
                mTotalCapacityChecked -= fileItem.getSize();
            }
            long t = handleFile.totalCapacity(mListFileChecked);
            mFragmentRestoring = new FragmentRestoring(dialog, mTotalCapacityChecked);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.FagmentBackup, mFragmentRestoring).commit();
        } else {
            if (handleFile.duplicateFileItem(mListFileChecked, fileItem) && ischecked) {
                mListFileChecked.add(fileItem);
                mTotalCapacityChecked += fileItem.getSize();
            }
            if (!ischecked) {
                mListFileChecked = handleFile.removeFileItem(mListFileChecked, fileItem);
                mTotalCapacityChecked -= fileItem.getSize();
            }
            fragmentStatusBackUp = new FragmentStatusBackUp(dialog, mTotalCapacityChecked);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.FagmentBackup, fragmentStatusBackUp).commit();
        }
    }

    public String getDateCurrent() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        return dtf.format(now);
    }
}
