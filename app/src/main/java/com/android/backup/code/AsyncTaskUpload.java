package com.android.backup.code;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.backup.CompressionFile;
import com.android.backup.ConvertNameFile;
import com.android.backup.FileItem;
import com.android.backup.RequestToServer;
import com.android.backup.code.Code;
import com.android.backup.handleFile;

import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.NoSuchPaddingException;

import okhttp3.Callback;

public class AsyncTaskUpload extends AsyncTask<Void, String, String> {

    FileItem mFileItem;
    Context mContext;
    String mConvertName;
    long[] mTotalDetail;
    Callback mCallback;
    String mPathsave;
    String mPathAuto;

    public AsyncTaskUpload(Context context, FileItem fileItem, String pathSave, Callback callback, long[] totalDetail, String pathAuto) {
        this.mFileItem = fileItem;
        mContext = context;
        mTotalDetail = totalDetail;
        mCallback = callback;
        mPathsave = pathSave;
        mPathAuto = pathAuto;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        //Bkav TienNVh : Comment mTotalDetail[1] =FileItem.getDirectorySizeLegacy(new File(handleFile.PATH_ROOT+"/DCIM/"+mFileItem.getName()));
        mTotalDetail[1] = FileItem.getDirectorySizeLegacy(new File(handleFile.PATH_ROOT + mPathAuto + "/" + mFileItem.getName()));
        mConvertName = ConvertNameFile.NameFolderToFile(mFileItem.getName().toString());
        String pathout = handleFile.PATH_ROOT + "/CompressionFile/";
        File file = new File(pathout);
        if (!file.exists())
            file.getParentFile().mkdirs();
        //Bkav TienNVh :add DCIM/
        CompressionFile.zipDirectory(handleFile.PATH_ROOT + mPathAuto + "/" + mFileItem.getName(), pathout + mConvertName + ".zip");
    }

    @Override
    protected String doInBackground(Void... voids) {
        try {
            Code.encrypt(mContext, handleFile.PATH_ROOT + "/CompressionFile/" + mConvertName + ".zip", handleFile.PATH_ROOT + "/CompressionFile/" + mConvertName + ".txt");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        return "Xong";
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        String namePath = handleFile.PATH_ROOT + "/CompressionFile/" + mConvertName + ".txt";
        RequestToServer.upload(mContext, mPathsave, "uploadfile", namePath, mCallback, mTotalDetail);
        handleFile.deleteFile(handleFile.PATH_ROOT + "/CompressionFile/" + mConvertName + ".zip");
    }
}
