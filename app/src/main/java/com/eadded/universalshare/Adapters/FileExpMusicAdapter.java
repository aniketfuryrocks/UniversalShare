package com.eadded.universalshare.Adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.eadded.universalshare.CommonLib.Common;
import com.eadded.universalshare.CommonLib.CustFile;
import com.eadded.universalshare.FileExplorer;
import com.eadded.universalshare.R;

import java.io.File;
import java.util.List;

public class FileExpMusicAdapter extends RecyclerView.Adapter {

    private List<File> files;

    public FileExpMusicAdapter(List<File> files) {
        this.files = files;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new IcoLinearHolder(LayoutInflater.from(Common.applicationContext).inflate(R.layout.file_exp_linear, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int i) {
        IcoLinearHolder holder = (IcoLinearHolder) viewHolder;
        Glide.with(Common.applicationContext).load(R.drawable.music).into(holder.icon);

        if (FileExplorer.selectedFiles.contains(new CustFile(files.get(i), null, files.get(i).getName())))
            holder.itemView.setBackgroundResource(R.drawable.selected);
        else
            holder.itemView.setBackgroundColor(Color.WHITE);

        holder.h1.setText(files.get(i).getName());
        holder.h2.setText(Common.formatFileSize(files.get(i).length()));

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Common.openFile(files.get(i));
                return true;
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustFile custFile = new CustFile(files.get(i));
                int index = FileExplorer.selectedFiles.indexOf(custFile);
                if (index == -1) {
                    v.setBackgroundResource(R.drawable.selected);
                    FileExplorer.selectedFiles.add(custFile);
                } else {
                    v.setBackgroundColor(Color.WHITE);
                    FileExplorer.selectedFiles.remove(index);
                }
                FileExplorer.countChanged();
            }
        });
    }


    @Override
    public int getItemCount() {
        return files.size();
    }
}