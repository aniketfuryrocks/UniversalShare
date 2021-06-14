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
import java.net.URLConnection;

public class FileExpDirAdapter extends RecyclerView.Adapter {

    private File currentDir;
    private File[] files;
    private File lastDir;

    public FileExpDirAdapter(File dirCurrent, File[] files) {
        this.currentDir = dirCurrent;
        this.files = files;
        this.lastDir = dirCurrent.getParentFile();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new IcoLinearHolder(LayoutInflater.from(Common.applicationContext).inflate(R.layout.file_exp_linear, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int i) {
        IcoLinearHolder holder = (IcoLinearHolder) viewHolder;
        if (files[i].isDirectory())
            Glide.with(Common.applicationContext).load(R.drawable.folder).into(holder.icon);
        else {
            String type = URLConnection.guessContentTypeFromName(files[i].getName()) + "";
            if (type.startsWith("image") || type.startsWith("video"))
                Glide.with(Common.applicationContext).load(files[i]).into(holder.icon);
            else
                Common.getIconFromName(files[i].getName()).into(holder.icon);
        }

        if (FileExplorer.selectedFiles.contains(new CustFile(files[i], null, files[i].getName())))
            holder.itemView.setBackgroundResource(R.drawable.selected);
        else
            holder.itemView.setBackgroundColor(Color.WHITE);

        holder.h1.setText(files[i].getName());
        if (files[i].isFile())
            holder.h2.setText(Common.formatFileSize(files[i].length()));
        else
            holder.h2.setText("");
        if (files[i].isFile()) {
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Common.openFile(files[i]);
                    return true;
                }
            });
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CustFile custFile = new CustFile(files[i]);
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
        } else if (files[i].isDirectory()) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    currentDir = files[i];
                    File[] f = currentDir.listFiles();
                    if (f == null)
                        Common.makeToast("Folder empty");
                    else {
                        files = f;
                        notifyDataSetChanged();
                    }
                }
            });
        }
    }

    public boolean backPressed() {
        File par = currentDir.getParentFile();
        if (par == null)
            return false;
        if (par.getAbsolutePath().equals(lastDir.getAbsolutePath()))
            return false;
        currentDir = par;
        files = currentDir.listFiles();
        notifyDataSetChanged();
        return true;
    }

    @Override
    public int getItemCount() {
        return files.length;
    }
}