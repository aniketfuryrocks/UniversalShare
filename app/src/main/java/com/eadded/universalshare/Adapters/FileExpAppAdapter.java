package com.eadded.universalshare.Adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.eadded.universalshare.CommonLib.Common;
import com.eadded.universalshare.CommonLib.CustFile;
import com.eadded.universalshare.FileExplorer;
import com.eadded.universalshare.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileExpAppAdapter extends RecyclerView.Adapter {
    private static List<CustFile> apps = null;
    private final int amount;
    private final int w;
    private PackageManager packageManager = Common.applicationContext.getPackageManager();

    public FileExpAppAdapter(Context context) {
        if (apps == null) {
            final ProgressDialog progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("Loading..."); // Setting Message
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL); // Progress Dialog Style Spinner
            progressDialog.setCancelable(false);
            progressDialog.show(); // Display Progress Dialog
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(Intent.ACTION_MAIN, null);
                    intent.addCategory(Intent.CATEGORY_LAUNCHER);
                    List<ResolveInfo> aps = packageManager.queryIntentActivities(intent, PackageManager.GET_META_DATA);
                    progressDialog.setMax(aps.size());
                    apps = new ArrayList<>(aps.size());
                    for (ResolveInfo resolveInfo : aps) {
                        progressDialog.incrementProgressBy(1);
                        apps.add(new CustFile(new File(resolveInfo.activityInfo.applicationInfo.publicSourceDir), Glide.with(Common.applicationContext).load(resolveInfo.loadIcon(packageManager)), resolveInfo.loadLabel(packageManager).toString() + ".apk"));
                    }
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            notifyDataSetChanged();
                            try {
                                progressDialog.dismiss();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    });
                }
            });
            thread.start();
        }
        w = (int) ((Common.applicationContext.getResources().getDisplayMetrics().scaledDensity * 60) + 20);
        amount = Common.applicationContext.getResources().getDisplayMetrics().widthPixels / w;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LinearLayout linearLayout = new LinearLayout(Common.applicationContext);
        linearLayout.setMinimumHeight(200);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setMinimumWidth(viewGroup.getWidth());
        linearLayout.setPadding(0, 20, 0, 50);
        linearLayout.setClickable(false);
        return new AppViewHolder(linearLayout, amount, w);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int index) {
        AppViewHolder appViewHolder = (AppViewHolder) viewHolder;
        int times = index < (apps.size() / amount) ? amount : apps.size() % amount;
        for (int i = 0; i < times; i++) {
            appViewHolder.linearLayouts[i].setVisibility(View.VISIBLE);
            final CustFile custFile = apps.get((index * amount) + i);
            final ImageView logo = (ImageView) appViewHolder.linearLayouts[i].getChildAt(0);
            custFile.icon.into(logo);
            ((TextView) appViewHolder.linearLayouts[i].getChildAt(1)).setText(custFile.name.substring(0, custFile.name.lastIndexOf(".")));
            if (FileExplorer.selectedFiles.indexOf(custFile) != -1)
                logo.setBackgroundResource(R.drawable.selected);
            else
                logo.setBackgroundColor(Color.WHITE);
            logo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int index = FileExplorer.selectedFiles.indexOf(custFile);
                    if (index == -1) {
                        logo.setBackgroundResource(R.drawable.selected);
                        FileExplorer.selectedFiles.add(custFile);
                    } else {
                        logo.setBackgroundColor(Color.WHITE);
                        FileExplorer.selectedFiles.remove(index);
                    }
                    FileExplorer.countChanged();
                }
            });
        }
        for (int i = 0; i < amount - times; i++)
            appViewHolder.linearLayouts[i + times].setVisibility(View.INVISIBLE);
    }

    @Override
    public int getItemCount() {
        if (apps == null)
            return 0;
        int size = apps.size() / amount;
        if (apps.size() % amount > 0)
            size++;
        return size;
    }
}

class AppViewHolder extends RecyclerView.ViewHolder {
    public final LinearLayout[] linearLayouts;
    public final Space[] spaces;

    public AppViewHolder(@NonNull LinearLayout itemView, int amount, int w) {
        super(itemView);
        this.linearLayouts = new LinearLayout[amount];
        spaces = new Space[amount + 1];
        ImageView imageView;
        TextView textView;
        for (int i = 0; i < amount; i++) {
            linearLayouts[i] = new LinearLayout(itemView.getContext());
            linearLayouts[i].setOrientation(LinearLayout.VERTICAL);
            linearLayouts[i].setMinimumWidth(w);
            linearLayouts[i].setGravity(Gravity.CENTER);
            linearLayouts[i].setPadding(10, 10, 10, 10);
            imageView = new ImageView(itemView.getContext());
            imageView.setMinimumHeight(w - 20);
            imageView.setMinimumWidth(w - 20);
            textView = new TextView(itemView.getContext());
            textView.setWidth(w - 20);
            textView.setTextColor(Color.BLACK);
            textView.setEllipsize(TextUtils.TruncateAt.END);
            textView.setGravity(Gravity.CENTER);
            spaces[i] = (Space) LayoutInflater.from(itemView.getContext()).inflate(R.layout.space, itemView, false);
            linearLayouts[i].addView(imageView, 0);
            linearLayouts[i].addView(textView, 1);
            itemView.addView(spaces[i]);
            itemView.addView(linearLayouts[i]);
        }
        spaces[amount] = (Space) LayoutInflater.from(itemView.getContext()).inflate(R.layout.space, itemView, false);
        itemView.addView(spaces[amount]);
    }
}
