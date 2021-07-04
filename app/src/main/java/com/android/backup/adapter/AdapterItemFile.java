package com.android.backup.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.backup.FileItem;
import com.android.backup.R;
import com.android.backup.handleFile;

import java.util.ArrayList;

public class AdapterItemFile extends RecyclerView.Adapter<AdapterItemFile.ViewHolder> {
    private Context mContext;
    private ArrayList<FileItem> mList;
    Boolean aBoolean = true;
    isChooseFolder isChooseFolder;
    long mTotalCapacity;
    boolean isRestore = false;

    public AdapterItemFile(Context mContext, ArrayList<FileItem> mList, Boolean b, boolean isRestore) {
        this.mContext = mContext;
        this.mList = mList;
        aBoolean = b;
        this.isRestore = isRestore;
    }

    public void setChooseFolder(isChooseFolder isChooseFolder) {
        this.isChooseFolder = isChooseFolder;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View file = inflater.inflate(R.layout.item_file, parent, false);
        ViewHolder viewHolder = new ViewHolder(file);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FileItem fileItem = mList.get(position);
        holder.nameFile.setText(fileItem.getName());
        holder.capacity.setText(Math.ceil((handleFile.KBToMB(fileItem.getSize())) * 10) / 10 + " MB");
        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.checkBox.isChecked()) {
                    mTotalCapacity += fileItem.getSize();
                } else {
                    mTotalCapacity -= fileItem.getSize();
                }
                isChooseFolder.getTotalCapacity(fileItem, holder.checkBox.isChecked());
            }
        });
        if (isRestore) {
            holder.statusBackup.setText("đang khôi phục ...");
        } else {
            if (fileItem.getType() == 1) {
                holder.statusBackup.setText("đã xong");
            }
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView nameFile, capacity;
        public TextView statusBackup;
        public long mTotalCapacity = 0;
        public CheckBox checkBox;
        public ViewHolder(View itemView) {
            super(itemView);
            nameFile = itemView.findViewById(R.id.name_app);
            capacity = itemView.findViewById(R.id.capacity);
            checkBox = itemView.findViewById(R.id.checkbox);
            statusBackup = itemView.findViewById(R.id.status_backup);
            if (aBoolean) {
                checkBox.setVisibility(View.VISIBLE);
                statusBackup.setVisibility(View.GONE);
            } else {
                checkBox.setVisibility(View.GONE);
                statusBackup.setVisibility(View.VISIBLE);
            }
        }
    }

    public interface isChooseFolder {
        void getTotalCapacity(FileItem fileItem, boolean isChecked);
    }
}
