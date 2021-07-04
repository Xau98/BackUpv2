package com.android.backup;

import android.util.Log;
import android.webkit.MimeTypeMap;

import org.json.JSONObject;

import java.io.File;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

import static com.android.backup.RequestToServer.JSON;

public class RequestBuilder {

    //Login request body
    public static RequestBody LoginBody(String username, String password, String token) {
        return new FormBody.Builder()
                .add("action", "login")
                .add("format", "json")
                .add("username", username)
                .add("password", password)
                .add("logintoken", token)
                .build();
    }

    public static MultipartBody uploadRequestBody(String id, String token, String pathSave, File file) {
        String content_type = getMimeType(file.getPath());
        String file_path = file.getAbsolutePath();
        //test
        if (content_type == null)
            content_type = "text/plain";
        RequestBody file_body = RequestBody.create(MediaType.parse(content_type), file);
        return new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("type", content_type)
                .addFormDataPart("uploaded_file", file_path.substring(file_path.lastIndexOf("/") + 1), file_body)
                .addFormDataPart("id", id)
                .addFormDataPart("token", token)
                .addFormDataPart("pathsave", pathSave)
                .build();
    }

    private static String getMimeType(String path) {
        String extension = MimeTypeMap.getFileExtensionFromUrl(path);
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);

    }
}