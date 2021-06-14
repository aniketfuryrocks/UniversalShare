package com.eadded.universalshare.Adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Space;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.eadded.universalshare.CommonLib.Common;
import com.eadded.universalshare.CommonLib.CustFile;
import com.eadded.universalshare.FileExplorer;
import com.eadded.universalshare.R;

import java.io.File;
import java.util.List;

public class FileExpImagesAdapter extends RecyclerView.Adapter {

    private final List<File> list;
    private final int amount;
    private final int w;

    public FileExpImagesAdapter(List<File> list) {
        this.list = list;
        w = (int) ((Common.applicationContext.getResources().getDisplayMetrics().scaledDensity * 75) + 20);
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
        return new ImageViewHolder(linearLayout, amount, w);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int index) {
        final ImageViewHolder imageViewHolder = (ImageViewHolder) viewHolder;
        int times = index < (list.size() / amount) ? amount : list.size() % amount;
        for (int i = 0; i < times; i++) {
            imageViewHolder.imageViews[i].setVisibility(View.VISIBLE);
            final CustFile custFile = new CustFile(list.get((index * amount) + i));
            Glide.with(Common.applicationContext).load(custFile.file).placeholder(R.drawable.loading).error(R.drawable.error_loading).into(imageViewHolder.imageViews[i]);
            if (FileExplorer.selectedFiles.contains(custFile))
                imageViewHolder.imageViews[i].setBackgroundResource(R.drawable.selected);
            else
                imageViewHolder.imageViews[i].setBackgroundColor(Color.WHITE);
            imageViewHolder.imageViews[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
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
            imageViewHolder.imageViews[i].setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Common.openFile(custFile.file);
                    return true;
                }
            });
        }

        for (int i = 0; i < amount - times; i++)
            imageViewHolder.imageViews[i + times].setVisibility(View.INVISIBLE);
    }

    @Override
    public int getItemCount() {
        int size = list.size() / amount;
        if (list.size() % amount > 0)
            size++;
        return size;
    }
}

class ImageViewHolder extends RecyclerView.ViewHolder {
    public final ImageView[] imageViews;
    public final Space[] spaces;

    public ImageViewHolder(@NonNull LinearLayout itemView, int amount, int w) {
        super(itemView);
        imageViews = new ImageView[amount];
        spaces = new Space[amount + 1];
        for (int i = 0; i < amount; i++) {
            imageViews[i] = new ImageView(itemView.getContext());
            imageViews[i].setMinimumHeight(w);
            imageViews[i].setMinimumWidth(w);
            imageViews[i].setMaxWidth(w);
            imageViews[i].setMaxHeight(w);
            imageViews[i].setPadding(10, 10, 10, 10);
            spaces[i] = (Space) LayoutInflater.from(itemView.getContext()).inflate(R.layout.space, itemView, false);
            itemView.addView(spaces[i]);
            itemView.addView(imageViews[i]);
        }
        spaces[amount] = (Space) LayoutInflater.from(itemView.getContext()).inflate(R.layout.space, itemView, false);
        itemView.addView(spaces[amount]);
    }
}