package com.android.backup.service;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.ConnectivityDiagnosticsManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RemoteViews;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.android.backup.FileItem;
import com.android.backup.R;
import com.android.backup.RequestToServer;
import com.android.backup.activity.MainActivity;
import com.android.backup.code.AsyncTaskDownload;
import com.android.backup.code.AsyncTaskUpload;
import com.android.backup.handleFile;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.android.backup.activity.HomePage.CHANNEL_ID;

public class ServiceAutoBackup extends Service {
    private IBinder binder = new BackupBinder();
    private ArrayList<FileItem> mListAllFile;
    private ArrayList<FileItem> mListSelected;
    public static final int MSG_ERRO = 12;
    AsyncTaskUpload myAsyncTaskCode;
    AsyncTaskDownload mAsyncTaskDownload;
    long mTotalSize = 0;
    long[] totalDetail = new long[3];
    ;
    String PATH_BACKUP_AUTO = "/DCIM/Test";

    @Override
    public void onCreate() {
        super.onCreate();
        senNotification();
    }

    public void updateUI() {
        mListAllFile = handleFile.loadFile(handleFile.PATH_ROOT + PATH_BACKUP_AUTO);
        String model = Build.MODEL;
        if (mListAllFile.size() != 0)
            onUploadAll(mListAllFile, null, null, model, PATH_BACKUP_AUTO);

    }

    public class BackupBinder extends Binder {
        public ServiceAutoBackup getMusicBinder() {
            return ServiceAutoBackup.this;
        }
    }

    public boolean isAsyncTaskRunning() {
        if (myAsyncTaskCode == null)
            return false;
        return myAsyncTaskCode.getStatus() == AsyncTask.Status.RUNNING;
    }

    public boolean isAsyncTaskDownloadRunning(AsyncTaskDownload a) {
        if (mAsyncTaskDownload == null)
            return false;
        return mAsyncTaskDownload.getStatus() == AsyncTask.Status.RUNNING;
    }

    int percenProgress = 0;

    public int getPercenProgress() {
        return percenProgress;
    }

    final Handler mHandler = new Handler();
    Runnable mRunnable;
    String namePathBackup = null;

    public String getNamePathBackup() {
        return namePathBackup;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    RemoteViews notificationLayout;
    NotificationCompat.Builder builder;
    NotificationManagerCompat notificationManager;

    public void senNotification() {
        notificationManager = NotificationManagerCompat.from(this);
        notificationLayout =
                new RemoteViews(getPackageName(), R.layout.notification_backup);
        builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        builder.setContentTitle("Picture Download")
                .setContentTitle("Title notification service")
                .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                .setContentText("Thong bao bat service auto backup")
                .setCustomContentView(notificationLayout)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setProgress(100, R.id.progress_bar_notification, true);
        startForeground(1, builder.build());
    }

    public void onUploadAll(ArrayList<FileItem> listAllFile, ProgressBar progressBar, TextView status, String namebackup, String pathAuto) {
        senNotification();
        mListSelected = listAllFile;
        mTotalSize = handleFile.totalCapacity(listAllFile);
        String nameFolderBackup = null;
        Handler handler = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                switch (msg.what) {
                    case MSG_ERRO:
                        if (mHandler != null && mRunnable != null)
                            mHandler.postDelayed(mRunnable, 300);
                        progressBar.setProgress(0);
                        status.setText("Upload lỗi ");
                        break;
                }
            }
        };


        Callback callback = new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                handler.sendEmptyMessage(MSG_ERRO);
                Log.d("Tiennvh", "onFailure: " + e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String fullname = response.body().string();
                    String namefile = fullname.substring(0, fullname.length() - 4);
                    for (int i = 0; i < listAllFile.size(); i++) {
                        if (namefile.equals(listAllFile.get(i).getName())) {
                            mListSelected.get(i).setType(1);
                            if (mCallbackService != null)
                                mCallbackService.callbackFinish(mListSelected);
                        }
                    }
                } else {
                    handler.sendEmptyMessage(MSG_ERRO);
                }
            }
        };

        for (int i = 0; i < listAllFile.size(); i++) {
            if (i == 0) {
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
                LocalDateTime now = LocalDateTime.now();
                nameFolderBackup = "Data" + dtf.format(now);
            }
            myAsyncTaskCode = new AsyncTaskUpload(this, listAllFile.get(i), nameFolderBackup, callback, totalDetail, pathAuto);
            myAsyncTaskCode.execute();
        }
        mRunnable = new Runnable() {
            @Override
            public void run() {
                float percensub = (totalDetail[0] / 100f) * totalDetail[1];
                float percen = 100f * percensub / mTotalSize;
                percenProgress = Math.round(percen);
                notificationLayout.setProgressBar(R.id.progress_bar_notification, 100, percenProgress, false);
                notificationLayout.setTextViewText(R.id.status_load_notification, "đang backuping " + percenProgress + "%");
                Log.d("Tiennvh", "percen: " + percenProgress);
                if (progressBar != null) {
                    progressBar.setProgress(percenProgress);
                    status.setText("Đang Backuping : " + percenProgress + "%");
                }
                if (Math.round(percenProgress) != 100) {
                    mHandler.postDelayed(this, 300);
                } else {
                    notificationLayout.setTextViewText(R.id.status_load_notification, "đã xong  ");
                    if (progressBar != null) {
                        progressBar.setProgress(percenProgress);
                        status.setText("đã xong");
                    }
                }
                startForeground(1, builder.build());
            }
        };

        mHandler.postDelayed(mRunnable, 100);

        Callback callback1 = new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.d("Tiennvh", "onFailure: " + e);

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String mJsonData = response.body().string();
                    Log.d("Tiennvh", "onResponse: " + mJsonData);
                }
            }
        };

        //Bkav Tiennvh: update DB
        if (nameFolderBackup != null) {
            SharedPreferences sharedPref = getSharedPreferences(MainActivity.SHAREPREFENCE, MODE_PRIVATE);
            JSONObject jsonObject = new JSONObject();
            String id_account = sharedPref.getString("id", "0");
            String pathBackUpRoot = "/root/Bkav/Data/" + id_account + "/" + nameFolderBackup;
            try {
                jsonObject.put("id", id_account);
                jsonObject.put("token", sharedPref.getString("token", "0"));
                jsonObject.put("namebackup", namebackup);
                jsonObject.put("namedevice", "Bphone 4");
                jsonObject.put("path", pathBackUpRoot);
                String path = "insertbackup";
                RequestToServer.post(path, jsonObject, callback1);
            } catch (JSONException e) {
                e.printStackTrace();
                Log.d("Tiennvh", "onCallbackBackup: " + e);
            }
        } else {
            Log.d("Tiennvh", "onCallbackBackup: Not OKE");
        }
    }

    public void onDownload(ArrayList<FileItem> listAllFile, ProgressBar progressBar, TextView status) {
        senNotification();
        mListSelected = listAllFile;
        for (int i = 0; i < listAllFile.size();i++ ) {
            if (mAsyncTaskDownload == null) {
                mAsyncTaskDownload = new AsyncTaskDownload(getBaseContext(), listAllFile.get(i), progressBar, status);

                mAsyncTaskDownload.execute();

            }
            Log.d("Tiennvh", "onDownload: "+(mAsyncTaskDownload.getStatus() == AsyncTask.Status.RUNNING) );
            /*if (mAsyncTaskDownload.getStatus() == AsyncTask.Status.RUNNING) {

            } else {

                mListSelected.get(i).setType(1);
                if (mCallbackService != null)
                    mCallbackService.callbackFinish(mListSelected);

            }*/
        }
        mRunnable = new Runnable() {
            @Override
            public void run() {
                Log.d("Tiennvh", "run: "+(mAsyncTaskDownload.getStatus() == AsyncTask.Status.RUNNING));
              if(mAsyncTaskDownload.getStatus() == AsyncTask.Status.RUNNING){
                  mHandler.postDelayed(this, 300);
              }else {

              }
            }
        };
        mHandler.postDelayed(mRunnable, 100);
    }

    callbackService mCallbackService;

    public void setmCallbackService(callbackService mCallbackService) {
        this.mCallbackService = mCallbackService;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public interface callbackService {
        void callbackFinish(ArrayList<FileItem> mListSelected);
    }

}
