package com.eadded.universalshare.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.eadded.universalshare.CommonLib.Common;
import com.eadded.universalshare.CommonLib.CustFile;
import com.eadded.universalshare.FileExplorer;
import com.eadded.universalshare.R;

public class SelectedFilesAdapter extends RecyclerView.Adapter {

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new IcoLinearHolder(LayoutInflater.from(Common.applicationContext).inflate(R.layout.file_exp_linear, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int i) {
        IcoLinearHolder holder = (IcoLinearHolder) viewHolder;
        final CustFile custFile = FileExplorer.selectedFiles.get(i);
        custFile.icon.into(holder.icon);

        holder.h1.setText(custFile.name);
        holder.h2.setText(Common.formatFileSize(custFile.file.length()));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int index = FileExplorer.selectedFiles.indexOf(custFile);
                if (index == -1) {
                    FileExplorer.selectedFiles.add(custFile);
                } else {
                    FileExplorer.selectedFiles.remove(index);
                }
                FileExplorer.countChanged();
                notifyDataSetChanged();
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Common.openFile(custFile.file);
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return FileExplorer.selectedFiles.size();
    }
}

class IcoLinearHolder extends RecyclerView.ViewHolder {
    public final ImageView icon;
    public final TextView h1;
    public final TextView h2;

    public IcoLinearHolder(@NonNull View itemView) {
        super(itemView);
        icon = itemView.findViewById(R.id.fileExpLinearIcon);
        h1 = itemView.findViewById(R.id.fileExpLinearH1);
        h2 = itemView.findViewById(R.id.fileExpLinearH2);
    }
}