package com.android.backup;


import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

public class Dialog {
    onConfirmBackup onConfirmBackup;

    public void setConfirmListener(onConfirmBackup onConfirmBackup) {
        this.onConfirmBackup = onConfirmBackup;

    }

    public void showDialog(Context context, LayoutInflater inflater, String title, boolean showconfirm, int type) {
        View alertLayout = inflater.inflate(R.layout.dialog_confirm, null);
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setView(alertLayout);
        alert.setCancelable(false);
        TextView tile = alertLayout.findViewById(R.id.title_dialog);
        tile.setText(title);
        alert.setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(context, "Cancel clicked", Toast.LENGTH_SHORT).show();
            }
        });
        if (showconfirm) {
            alert.setPositiveButton("Xác Nhận", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // code for matching password
                    Log.d("Tiennvh", "setConfirmListener: " + onConfirmBackup);
                    onConfirmBackup.onConfirm(type);
                }
            });
        }
        alert.show();
    }


    public interface onConfirmBackup {
        void onConfirm(int type);
    }
}
