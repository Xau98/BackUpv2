package com.android.backup;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class CompressionFile {

    public static final byte[] BUFFER = new byte[1024];
    public static final String PATH_COMPRESSION = handleFile.PATH_ROOT + "/data.zip";
/*    File outputZipFile = new File(handleFile.PATH_ROOT+"/Android/demo.zip");
    File inputDir = new File( handleFile.PATH_ROOT+"/Android/data");
   CompressionFile.zipDirectory(inputDir, outputZipFile);*/

    /**
     * Nén tất cả các tập tin và thư mục trong thư mục đầu vào
     */
    public static void zipDirectory(String pathInput, String pathOutput) {
        File outputZipFile = new File(pathOutput);
        File inputDir = new File(pathInput);
        // Tạo thư mục cha cho file đầu ra (output file).
        outputZipFile.getParentFile().mkdirs();
        String inputDirPath = inputDir.getAbsolutePath();
        FileOutputStream fos = null;
        ZipOutputStream zipOs = null;
        try {

            List<File> allFiles = listChildFiles(inputDir);
            // Tạo đối tượng ZipOutputStream để ghi file zip.
            fos = new FileOutputStream(outputZipFile);
            zipOs = new ZipOutputStream(fos);
            for (File file : allFiles) {
                String filePath = file.getAbsolutePath();
                // entryName: is a relative path.
                String entryName = filePath.substring(inputDirPath.length() + 1);

                ZipEntry ze = new ZipEntry(entryName);
                // Thêm entry vào file zip.
                zipOs.putNextEntry(ze);
                // Đọc dữ liệu của file và ghi vào ZipOutputStream.
                FileInputStream fileIs = new FileInputStream(filePath);
                int len;
                while ((len = fileIs.read(BUFFER)) > 0) {
                    zipOs.write(BUFFER, 0, len);
                }
                fileIs.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("Tiennvh", "zipDirectory: " + e);
        } finally {
            closeStream(zipOs);
            closeStream(fos);
        }

    }

    private static void closeStream(OutputStream out) {
        try {
            out.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Lấy danh sách các file trong thư mục:
     * bao gồm tất cả các file con, cháu,.. của thư mục đầu vào.
     */
    private static List<File> listChildFiles(File dir) throws IOException {
        List<File> allFiles = new ArrayList<>();

        File[] childFiles = dir.listFiles();

        for (File file : childFiles) {
            if (file.isFile()) {
                allFiles.add(file);
            } else {
                List<File> files = listChildFiles(file);
                allFiles.addAll(files);
            }
        }
        return allFiles;
    }

    //=============================================giải nén=======================================
    public static void unZip(String FILE_PATH, String OUTPUT_FOLDER) {
        //final String OUTPUT_FOLDER = "C:/output";
        //String FILE_PATH = "C:/test/datas.zip";

        // Tạo thư mục Output nếu nó không tồn tại.
        File folder = new File(OUTPUT_FOLDER);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        // Tạo một buffer (Bộ đệm).
        byte[] buffer = new byte[1024];

        ZipInputStream zipIs = null;

        try {
            // Tạo đối tượng ZipInputStream để đọc file từ 1 đường dẫn (path).
            zipIs = new ZipInputStream(new FileInputStream(FILE_PATH));

            ZipEntry entry = null;
            // Duyệt từng Entry (Từ trên xuống dưới cho tới hết)
            while ((entry = zipIs.getNextEntry()) != null) {
                String entryName = entry.getName();
                String outFileName = OUTPUT_FOLDER + File.separator + entryName;
                System.out.println("Unzip: " + outFileName);

                if (entry.isDirectory()) {
                    // Tạo các thư mục.
                    new File(outFileName).mkdirs();
                } else {
                    // Tạo một Stream để ghi dữ liệu vào file.
                    new File(outFileName).getParentFile().mkdirs();
                    File file = new File(outFileName);
                    FileOutputStream fos = new FileOutputStream(file);

                    int len;
                    // Đọc dữ liệu trên Entry hiện tại.
                    while ((len = zipIs.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }

                    fos.close();
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                zipIs.close();
            } catch (Exception e) {
            }
        }
    }


}
