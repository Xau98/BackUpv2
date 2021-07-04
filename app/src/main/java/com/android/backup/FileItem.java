package com.android.backup;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileItem {
    private String ID;

    private String name;

    // private String displayName;

    private int type;
    // type = 0 ; File đã biết định dạng
    // type = 3 ; File chưa biết định dạng (unknow)
    // type = 2 ; Là thư mục
    private String path;

    private long date;

    private long size;


    @RequiresApi(api = Build.VERSION_CODES.O)
    public FileItem(String path, int type) {
        File file = new File(path);
        name = file.getName();
        this.type = type;
        this.path = path;
        date = file.lastModified();
        size = getDirectorySizeLegacy(file);
    }

    public FileItem(String name, String path, long size) {
        this.name = name;
        this.path = path;
        this.size = size;
    }


    public static long getDirectorySizeLegacy(File dir) {

        long length = 0;
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile())
                    length += file.length();
                else
                    length += getDirectorySizeLegacy(file);
            }
        }
        return length;

    }

    public String getName() {
        return name;
    }

    public int getType() {
        return type;
    }

    public String getPath() {
        return path;
    }

    public long getDate() {
        return date;
    }

    public long getSize() {
        return size;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public void setSize(long size) {
        this.size = size;
    }
}
