package com.android.backup;

public class ConvertNameFile {
    public static String NameFolderToFile(String namefolder) {
        String nameFile = "";
        switch (namefolder) {
            case "Âm nhạc":
                nameFile = "Amnhac";
                break;
            case "Ảnh và video":
                nameFile = "Anhvavideo";
                break;
            case "Báo thức":
                nameFile = "Baothuc";
                break;
            case "Hình ảnh":
                nameFile = "Hinhanh";
                break;
            case "Nhạc chuông":
                nameFile = "Nhacchuong";
                break;
            case "Tải xuống":
                nameFile = "Taixuong";
                break;
            case "Thông báo":
                nameFile = "Thongbao";
                break;
            default:
                nameFile = namefolder;
                break;
        }
        return nameFile;
    }

    public static String NameFileToFolder(String namefile) {
        String nameFolder = "";
        switch (namefile) {
            case "Amnhac":
                nameFolder = "Âm nhạc";
                break;
            case "Anhvavideo":
                nameFolder = "Ảnh và video";
                break;
            case "Baothuc":
                nameFolder = "Báo thức";
                break;
            case "Hinhanh":
                nameFolder = "Hình ảnh";
                break;
            case "Nhacchuong":
                nameFolder = "Nhạc chuông";
                break;
            case "Taixuong":
                nameFolder = "Tải xuống";
                break;
            case "Thongbao":
                nameFolder = "Thông báo";
                break;
            default:
                nameFolder = namefile;
                break;
        }
        return nameFolder;
    }
}
