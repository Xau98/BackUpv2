package com.android.backup.code;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.backup.activity.MainActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class Code {

    static String SALT = "AT130156";
    static String SALT_PASSWORD =null;
    static String mPassword = null;

    public static void setmPassword(String mPassword) {
        Code.mPassword = mPassword;
    }

    public static void encrypt(Context context, String pathInput, String pathOutput) throws IOException, NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidKeyException {
        try {
            String pass = null;
            FileInputStream fileInputStream = new FileInputStream(pathInput);
            FileOutputStream fileOutputStream = new FileOutputStream(pathOutput);
            SharedPreferences sharedPreferences = context.getSharedPreferences(MainActivity.SHAREPREFENCE, Context.MODE_PRIVATE);
            String key_screct = sharedPreferences.getString("key_secret", null);
            if(mPassword!=null||key_screct!=null){
                pass = decryptString(key_screct,mPassword);
            }else {
                Log.d("Tiennvh", "decrypt: loi key null!! ");
                throw new Exception();
            }
            if(pass== null){
                Log.d("Tiennvh", "decrypt: loi key null!! ");
                throw new Exception();
            }
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(pass.toCharArray(), SALT.getBytes(), 65536, 256);
            SecretKey key = factory.generateSecret(spec);
            SecretKeySpec sks = new SecretKeySpec(key.getEncoded(), "AES");
            Log.d("Tiennvh", "encrypt: " + key.getEncoded());
            // Create cipher
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, sks);
            // Wrap the output stream
            CipherOutputStream cos = new CipherOutputStream(fileOutputStream, cipher);
            int b;
            byte[] d = new byte[8];
            while ((b = fileInputStream.read(d)) != -1) {
                cos.write(d, 0, b);
            }
            // Flush and close streams.
            cos.flush();
            cos.close();
            fileInputStream.close();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //"MyDifficultPassw"
    public static void decrypt(Context context, String pathInput, String pathOutput, ProgressBar progressBar, TextView statusload) throws IOException, NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidKeyException {
        try {
            String pass = null;
            File file = new File(pathInput);
            FileInputStream fileInputStream = new FileInputStream(file);
            FileOutputStream fileOutputStream = new FileOutputStream(pathOutput);
            SharedPreferences sharedPreferences = context.getSharedPreferences(MainActivity.SHAREPREFENCE, Context.MODE_PRIVATE);
            String key_screct = sharedPreferences.getString("key_secret", null);
            if(mPassword!=null||key_screct!=null){
                pass = decryptString(key_screct,mPassword);
            }else {
                Log.d("Tiennvh", "decrypt: loi key null!! ");
                throw new Exception();
            }
            if(pass== null){
                Log.d("Tiennvh", "decrypt: loi key null!! ");
                throw new Exception();
            }
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(pass.toCharArray(), SALT.getBytes(), 65536, 256);
            SecretKey key = factory.generateSecret(spec);
            Log.d("Tiennvh", "decrypt: " + key.getEncoded());
            SecretKeySpec sks = new SecretKeySpec(key.getEncoded(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, sks);
            CipherInputStream cis = new CipherInputStream(fileInputStream, cipher);
            int b;
            byte[] d = new byte[8];

            long totalLength = file.length();
            double lengthPerPercent = 100.0 / totalLength;
            long readLength = 0;
            while ((b = cis.read(d)) != -1) {
                fileOutputStream.write(d, 0, b);
                readLength += b;
                int percent = (int) Math.round(lengthPerPercent * readLength);
                progressBar.setProgress(percent);
                //statusload.setText("Khôi phục " + percent + "%");
            }
            fileOutputStream.flush();
            fileOutputStream.close();
            cis.close();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String encryptString(String strToEncrypt, String myKey) {
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-1");
            byte[] key = myKey.getBytes("UTF-8");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16);
            SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes("UTF-8")));
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        return null;
    }

    public static String decryptString(String strToDecrypt , String myKey ){
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-1");
            byte[] key = myKey.getBytes("UTF-8");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16);
            SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            String result = new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
            return result;
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        return null;
    }

}
