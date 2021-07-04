package com.android.backup;

import java.io.Serializable;

public class ItemListRestore implements Serializable {
    private int ID;
    private String name;
    private String dateBackup;
    private String devices;
    private int type;
    private String path;

    public ItemListRestore(int ID, String name, String dateBackup, String devices, int type, String path) {
        this.ID = ID;
        this.name = name;
        this.dateBackup = dateBackup;
        this.devices = devices;
        this.type = type;
        this.path = path;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDateBackup() {
        return dateBackup;
    }

    public void setDateBackup(String dateBackup) {
        this.dateBackup = dateBackup;
    }

    public String getDevices() {
        return devices;
    }

    public void setDevices(String devices) {
        this.devices = devices;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
