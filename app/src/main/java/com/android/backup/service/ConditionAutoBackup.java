package com.android.backup.service;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.android.backup.FileItem;
import com.android.backup.code.AsyncTaskUpload;
import com.android.backup.handleFile;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static android.content.Context.JOB_SCHEDULER_SERVICE;
import static com.facebook.FacebookSdk.getApplicationContext;

public class ConditionAutoBackup extends BroadcastReceiver {

    private long mScheduleTime;
    private Context mContext;
    private ArrayList<FileItem> mListAllFile;


    public ConditionAutoBackup(long scheduleTime) {
        mScheduleTime = scheduleTime;
    }

    public ConditionAutoBackup() {
    }

    boolean statusBack = false;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null) {
            Log.d("Tiennvh", ConditionBackup.isCharging(context) + "//" + ConditionBackup.isNetworkConnected(context) + "//" + ConditionBackup.isScreenLock(context) + "onReceive: " + statusBack);
            if (/*ConditionBackup.isCharging(context)&&*/ ConditionBackup.isNetworkConnected(context) && ConditionBackup.isScreenLock(context) && !statusBack) {
                statusBack = true;
                callbackConditionBackup.onCallback();

                ComponentName componentName = new ComponentName(getApplicationContext(), JobService.class);
                JobInfo info = new JobInfo.Builder(123, componentName)
                        .setRequiresCharging(true)
                        .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
                        .setPersisted(true)
                        .setPeriodic(mScheduleTime)
                        .build();
                JobScheduler scheduler = (JobScheduler) context.getSystemService(JOB_SCHEDULER_SERVICE);
                int result = scheduler.schedule(info);
                if (/*result == JobScheduler.RESULT_SUCCESS*/false) {
                    mListAllFile = handleFile.loadFile(handleFile.PATH_ROOT);
                    Log.d("Tiennvh", "onClick:OKE ");
                    AsyncTaskUpload myAsyncTaskCode;
                    String namePathBackup = "false";
                    Callback callback = new Callback() {
                        @Override
                        public void onFailure(@NotNull Call call, @NotNull IOException e) {
                            Log.d("Tiennvh", "onFailure: " + e);
                            //mCallbackBackup.onCallbackBackup("False");

                        }

                        @Override
                        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                            if (response.isSuccessful()) {
                                String mJsonData = response.body().string();
                                Log.d("Tiennvh", "onResponse: " + mJsonData);
                                // mHandler.sendEmptyMessage(MSG_BACKUP);
                            }//else
                            //mCallbackBackup.onCallbackBackup("False");
                        }
                    };
                    for (int i = 0; i < mListAllFile.size(); i++) {
                        if (i == 0) {
                            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
                            LocalDateTime now = LocalDateTime.now();
                            namePathBackup = "Data" + dtf.format(now);
                        }
                        //myAsyncTaskCode = new AsyncTaskUpload(mContext, mListAllFile.get(i), namePathBackup, callback, mProgressBar, mStatusLoad);
                        //myAsyncTaskCode.execute();
                    }

                } else {
                    Log.d("Tiennvh", "onClick: not OKE ");
                }

            }
        }

    }

    callbackConditionBackup callbackConditionBackup;

    public void setCallbackConditionBackup(callbackConditionBackup callbackConditionBackup) {
        this.callbackConditionBackup = callbackConditionBackup;
    }

    public interface callbackConditionBackup {
        void onCallback();
    }
}
