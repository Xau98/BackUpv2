package com.android.backup.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.backup.ItemListRestore;
import com.android.backup.R;

import java.util.ArrayList;

public class AdapterListFileRestore extends RecyclerView.Adapter<AdapterListFileRestore.ViewHolder> {
    private Context mContext;
    private ArrayList<ItemListRestore> mList;
    private onCallBackRestore onCallBackRestore;

    public void setOnCallBackRestore(AdapterListFileRestore.onCallBackRestore onCallBackRestore) {
        this.onCallBackRestore = onCallBackRestore;
    }

    public ArrayList<ItemListRestore> getmList() {
        return mList;
    }

    public void setmList(ArrayList<ItemListRestore> mList) {
        this.mList = mList;
    }

    public AdapterListFileRestore(Context mContext, ArrayList<ItemListRestore> mList) {
        this.mContext = mContext;
        this.mList = mList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View view = layoutInflater.inflate(R.layout.item_list_restore, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.mNameBackup.setText(mList.get(position).getName());
        holder.mDateBackup.setText(mList.get(position).getDateBackup());
        int type = mList.get(position).getType();
        switch (type) {
            case 0:
                //Bkav Tiennvh: mặc định
                holder.mDelete.setVisibility(View.GONE);
                holder.mCheckBox.setVisibility(View.GONE);
                break;
            case 1:
                //Bkav Tiennvh : Vuốt trái máy hiện tại
                holder.mCheckBox.setVisibility(View.VISIBLE);
                holder.mDelete.setVisibility(View.GONE);
                break;
            case 2:
                //Bkav Tiennvh vuốt phải máy hiện tại
                holder.mDelete.setVisibility(View.VISIBLE);
                holder.mCheckBox.setVisibility(View.GONE);
                break;
            case 3:
                //Bkav Tiennvh : select checkbox
                holder.mCheckBox.setChecked(true);
                break;
            case 4:
                //Bkav Tiennvh: bỏ checkbox
                holder.mCheckBox.setVisibility(View.VISIBLE);
                holder.mDelete.setVisibility(View.GONE);
                holder.mCheckBox.setChecked(false);
                break;
        }
        holder.mCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Tiennvh", holder.mCheckBox.isChecked() + "onClick: " + position);
                if (holder.mCheckBox.isChecked()) {
                    mList.get(position).setType(3);
                } else
                    mList.get(position).setType(0);
            }
        });
        holder.mDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCallBackRestore.onConfirmDeleteRestore(mList.get(position).getID());
            }
        });
        holder.mItemRestore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCallBackRestore.onClickItemRestore(mList.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView mNameBackup, mDateBackup;
        CheckBox mCheckBox;
        ImageButton mDelete;
        LinearLayout mItemRestore;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mNameBackup = itemView.findViewById(R.id.name_restore);
            mDateBackup = itemView.findViewById(R.id.date_restore);
            mCheckBox = itemView.findViewById(R.id.checkbox_restore);
            mDelete = itemView.findViewById(R.id.bt_delete_restore);
            mItemRestore = itemView.findViewById(R.id.item_restore);
        }
    }

    public interface onCallBackRestore {
        void onConfirmDeleteRestore(int position);

        void onClickItemRestore(ItemListRestore itemListRestore);
    }
}
