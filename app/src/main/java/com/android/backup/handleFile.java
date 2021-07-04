package com.android.backup;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.android.backup.activity.MainActivity;
import com.github.tamir7.contacts.Contact;
import com.github.tamir7.contacts.Contacts;
import com.github.tamir7.contacts.PhoneNumber;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import me.everything.providers.android.calllog.CallsProvider;

public class handleFile {
    public static String PATH_ROOT = Environment.getExternalStorageDirectory().toString();
    public static String PATH_FOLDER = PATH_ROOT + "/CompressionFile";

    // Bkav TienNVh :Load File
    //TODO: Lọc các thư mục cần thiết để backup (ko up lên cả)
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static ArrayList<FileItem> loadFile(String path) {
        ArrayList<FileItem> list = new ArrayList<>();
        File directory = new File(path);
        File[] files = directory.listFiles();
        if (files != null) {
            Log.d("Tiennvh", "loadFile: " + files.length);
            for (int i = 0; i < files.length; i++) {
                FileItem fileItem = new FileItem(path + "/" + files[i].getName(), 0);
                Log.d("Tiennvh", "loadFile2: " + path + "/" + files[i].getName());
                if (fileItem.getSize() > 0)
                    list.add(fileItem);

            }
            Log.d("Tiennvh", "loadFile1: " + list.size());
            return list;
        } else {
            // listAllFile.add(new FileItem(path, 0));
            return null;
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void listFilesForFolder(final File folder, Context context) {
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry, context);
            } else {
                String path = fileEntry.getPath();
                int index = 35;
                String pathfile = handleFile.PATH_ROOT + path.substring(index, path.length());
                Log.d("Tiennvh", "listFilesForFolder: " + pathfile);
                File file = new File(pathfile);
                if (file.exists()) {
                    if (isSameFile(path, pathfile)) {
                        deleteFile(path);
                    } else {
                        File file1 = new File(path);
                        File file2 = new File(pathfile);
                        file2.getParentFile().mkdirs();
                        file1.renameTo(file2);
                        writeHistoryDownloadFile(file2.getPath(), context);
                    }
                } else {
                    File file1 = new File(path);
                    File file2 = new File(pathfile);
                    file2.getParentFile().mkdirs();
                    file1.renameTo(file2);
                    writeHistoryDownloadFile(file2.getPath(), context);
                }

            }
        }

    }

    public static void writeHistoryDownloadFile(String data, Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(MainActivity.SHAREPREFENCE, context.MODE_PRIVATE);
        try {
            File file = new File(PATH_ROOT + "/" + sharedPref.getString("id_devices", null) + ".txt");
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(file, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter pw = new PrintWriter(bw);
            //This will add a new line to the file content
            pw.println(data);
            pw.close();
        } catch (IOException ioe) {
            System.out.println("Exception occurred:" + ioe);
            ioe.printStackTrace();
        }

    }

    public static void readHistoryDownloadFile(String path) {
        try {
            File file = new File(path);
            Reader reader = new FileReader(file);
            BufferedReader br = new BufferedReader(reader);
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
                File file2 = new File(line);
                Log.d("Tiennvh", file2.exists() + "readHistoryDownloadFile: " + line);
                if (file2.exists()) {
                    file2.delete();
                }
            }
            br.close();
            file.delete();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static long totalCapacity(ArrayList<FileItem> list) {
        long total = 0;
        for (int i = 0; i < list.size(); i++) {
            FileItem file = new FileItem(list.get(i).getPath(), 0);
            total += file.getSize();
        }
        return total;
    }

    public static Float KBToMB(long KB) {
        return (float) KB / (1024 * 1024);
    }


    //
    public static boolean duplicateFileItem(ArrayList<FileItem> list, FileItem fileItem) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getName().equals(fileItem.getName())) {
                return false;
            }
        }
        return true;
    }

    public static ArrayList<FileItem> removeFileItem(ArrayList<FileItem> list, FileItem fileItem) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getName().equals(fileItem.getName())) {
                list.remove(i);
            }
        }
        return list;
    }


    public static void deleteFile(String path) {
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static boolean isSameFile(String file1, String file2) {
        try {
            File f1 = new File(file1);// OUTFILE
            File f2 = new File(file2);// INPUT

            FileReader fR1 = new FileReader(f1);
            FileReader fR2 = new FileReader(f2);

            BufferedReader reader1 = new BufferedReader(fR1);
            BufferedReader reader2 = new BufferedReader(fR2);

            String line1 = null;
            String line2 = null;
            int flag = 1;
            while ((flag == 1) && ((line1 = reader1.readLine()) != null)
                    && ((line2 = reader2.readLine()) != null)) {
                if (!line1.equalsIgnoreCase(line2))
                    flag = 0;
            }
            reader1.close();
            reader2.close();
            if (flag == 1)
                return true;
            return false;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
/*
    public static void createFolderCompression(){
        File f=new File(PATH_FOLDER);
        if(!f.exists())
        {
            f.mkdir();
        }
        else{
            Log.d("tiennvh", "createFolderCompression: ");
        }
    }

  //Bkav Tiennvh Get contact
    public void getcontact(){
        List<Contact> contacts = Contacts.getQuery().find();
        for (int i=0;i<contacts.size();i++){
            List<PhoneNumber> ct1=contacts.get(i).getPhoneNumbers();
            for(int j=0;j<ct1.size();j++){
                Log.d("Tiennvh", "getcontact: "+ ct1.get(j).getNumber());
            }

        }
    }
    public void getCallContact(Context context){
        CallsProvider callsProvider = new CallsProvider(context);
        List<me.everything.providers.android.calllog.Call> a=  callsProvider.getCalls().getList();
        for(int i=0;i<a.size();i++){
            Log.d("Tiennvh", "getCallContact: "+ a.get(i).number);

        }
    }

    public static void fileToCompression(String path){
        try {
            File afile = new File(path);

            if (afile.renameTo(new File(PATH_FOLDER+"/" + afile.getName()))) {
                Log.d("Tiennvh", "File is moved successful: ");
            } else {
                Log.d("Tiennvh", "File is failed to move!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
*  private void saveData() {
        String data = mUsername.getText().toString();
        try {
            // Open Stream to write file.
            FileOutputStream out = this.openFileOutput(simpleFileName, MODE_PRIVATE);
            // Ghi dữ liệu.
            out.write(data.getBytes());
            out.close();
            Log.d("Tiennvh", "saveData: ");
            Toast.makeText(this,"File saved!",Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.d("Tiennvh", "saveData: "+e);
            Toast.makeText(this,"Error:"+ e.getMessage(),Toast.LENGTH_SHORT).show();
        }
    }
    private String simpleFileName = "note.txt";
    private void readData() {
        try {
            // Open stream to read file.
            FileInputStream in = this.openFileInput(simpleFileName);

            BufferedReader br= new BufferedReader(new InputStreamReader(in));

            StringBuilder sb= new StringBuilder();
            String s= null;
            while((s= br.readLine())!= null)  {
                sb.append(s).append("\n");
            }
            this.mPassword.setText(sb.toString());

        } catch (Exception e) {
            Toast.makeText(this,"Error:"+ e.getMessage(),Toast.LENGTH_SHORT).show();
        }
    }*/

}
