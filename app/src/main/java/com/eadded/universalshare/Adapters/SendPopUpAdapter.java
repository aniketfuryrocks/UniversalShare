package com.eadded.universalshare.Adapters;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.eadded.universalshare.CommonLib.Common;
import com.eadded.universalshare.Network.FileProg;
import com.eadded.universalshare.Network.FileProgChangeEvent;
import com.eadded.universalshare.R;

import java.util.LinkedList;

public class SendPopUpAdapter extends RecyclerView.Adapter {

    public final LinkedList<FileProg> fileProgs;
    private final int type;

    public SendPopUpAdapter(LinkedList fileProgs, int type) {
        this.fileProgs = fileProgs;
        this.type = type;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new SendPopupItemHolder(LayoutInflater.from(Common.applicationContext).inflate(R.layout.send_popup_item, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int i) {
        final SendPopupItemHolder sendPopupItemHolder = (SendPopupItemHolder) viewHolder;
        sendPopupItemHolder.progressBar.setMax(fileProgs.get(i).totalProg);
        sendPopupItemHolder.titTV.setText(fileProgs.get(i).custFile.name);
        sendPopupItemHolder.userTV.setText(fileProgs.get(i).user);
        sendPopupItemHolder.progressBar.setProgress(fileProgs.get(i).prog);
        sendPopupItemHolder.progTV.setText(Common.formatFileSize(fileProgs.get(i).prog) + " / " + Common.formatFileSize(sendPopupItemHolder.progressBar.getMax()));
        if (type == 0) {
            fileProgs.get(i).custFile.icon.into(sendPopupItemHolder.icon);
        }
        if (fileProgs.get(i).prog < fileProgs.get(i).totalProg) {
            if (type == 1)
                Common.getIconFromName(fileProgs.get(i).custFile.name).into(sendPopupItemHolder.icon);
            if (!fileProgs.get(i).workLoop) {
                Glide.with(Common.applicationContext).load(R.drawable.error).into(sendPopupItemHolder.statIco);
                return;
            }
            fileProgs.get(i).setOnFileProgChange(new FileProgChangeEvent() {
                @Override
                public void fileProgressChanged(final int to) {
                    if (Looper.getMainLooper().getThread() != Thread.currentThread()) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                sendPopupItemHolder.progressBar.setProgress(to);
                                sendPopupItemHolder.progTV.setText(Common.formatFileSize(to) + " / " + Common.formatFileSize(sendPopupItemHolder.progressBar.getMax()));
                                if (to == fileProgs.get(i).totalProg) {
                                    if (type == 1) {
                                        Glide.with(Common.applicationContext).load(fileProgs.get(i).custFile.file).into(sendPopupItemHolder.icon);
                                        sendPopupItemHolder.icon.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                Common.openFile(fileProgs.get(i).custFile.file);
                                            }
                                        });
                                    }
                                    Glide.with(Common.applicationContext).load(R.drawable.done).into(sendPopupItemHolder.statIco);
                                    sendPopupItemHolder.statIco.setOnClickListener(null);
                                }
                            }
                        });
                        return;
                    }
                    sendPopupItemHolder.progressBar.setProgress(to);
                    if (to == fileProgs.get(i).totalProg) {
                        if (type == 1) {
                            Glide.with(Common.applicationContext).load(fileProgs.get(i).custFile.file).into(sendPopupItemHolder.icon);
                            sendPopupItemHolder.icon.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Common.openFile(fileProgs.get(i).custFile.file);
                                }
                            });
                        }
                        Glide.with(Common.applicationContext).load(R.drawable.done).into(sendPopupItemHolder.statIco);
                        sendPopupItemHolder.statIco.setOnClickListener(null);
                    }
                }

                @Override
                public void fileProgressCancel() {
                    if (Looper.getMainLooper().getThread() != Thread.currentThread()) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                Glide.with(Common.applicationContext).load(R.drawable.error).into(sendPopupItemHolder.statIco);
                                sendPopupItemHolder.statIco.setOnClickListener(null);
                            }
                        });
                        return;
                    }
                    Glide.with(Common.applicationContext).load(R.drawable.error).into(sendPopupItemHolder.statIco);
                    sendPopupItemHolder.statIco.setOnClickListener(null);
                }
            });
            sendPopupItemHolder.progressBar.setProgress(fileProgs.get(i).prog);
            Glide.with(Common.applicationContext).load(R.drawable.cancel).into(sendPopupItemHolder.statIco);
            if (fileProgs.get(i).workLoop)
                sendPopupItemHolder.statIco.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        v.setOnClickListener(null);
                        Glide.with(Common.applicationContext).load(R.drawable.error).into((ImageView) v);
                        fileProgs.get(i).fileProgCancel();
                        Common.makeToast("Cancelled");
                    }
                });
            else
                sendPopupItemHolder.statIco.setOnClickListener(null);

        } else {
            if (type == 1) {
                Glide.with(Common.applicationContext).load(fileProgs.get(i).custFile.file).into(sendPopupItemHolder.icon);
                sendPopupItemHolder.icon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Common.openFile(fileProgs.get(i).custFile.file);
                    }
                });
            }
            Glide.with(Common.applicationContext).load(R.drawable.done).into(sendPopupItemHolder.statIco);
            sendPopupItemHolder.statIco.setOnClickListener(null);
        }


    }

    @Override
    public int getItemCount() {
        return fileProgs.size();
    }

    public void addItem(FileProg fileProg) {
        fileProgs.addFirst(fileProg);
        if (Looper.getMainLooper().getThread() != Thread.currentThread()) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    notifyDataSetChanged();
                }
            });
        } else
            notifyDataSetChanged();
    }

    public void removeItem(FileProg fileProg) {
        fileProgs.remove(fileProg);
        if (Looper.getMainLooper().getThread() != Thread.currentThread()) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    notifyDataSetChanged();
                }
            });
        } else
            notifyDataSetChanged();
    }
}

class SendPopupItemHolder extends RecyclerView.ViewHolder {

    public final TextView titTV;
    public final TextView userTV;
    public final TextView progTV;
    public final ProgressBar progressBar;
    public final ImageView statIco;
    public final ImageView icon;

    SendPopupItemHolder(View view) {
        super(view);
        this.progTV = view.findViewById(R.id.sendPopupProg);
        this.titTV = view.findViewById(R.id.sendPopupTitle);
        this.userTV = view.findViewById(R.id.sendPopupUser);
        this.progressBar = view.findViewById(R.id.sendPopupBar);
        this.statIco = view.findViewById(R.id.sendPopupStatImg);
        this.icon = view.findViewById(R.id.sendPopupIco);
    }
}