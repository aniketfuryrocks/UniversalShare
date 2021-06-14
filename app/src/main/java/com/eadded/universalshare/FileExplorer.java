package com.eadded.universalshare;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.eadded.universalshare.Adapters.*;
import com.eadded.universalshare.CommonLib.Common;
import com.eadded.universalshare.CommonLib.CustFile;
import com.eadded.universalshare.CommonLib.FileHandler;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class FileExplorer extends AppCompatActivity {

    public static final LinkedList<CustFile> selectedFiles = new LinkedList<>();
    private static TextView countTV;
    private RecyclerView popUpCard;
    private int current = -1;
    private Bundle bundle;

    public static void countChanged() {
        countTV.setText("" + selectedFiles.size());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_explorer);
        popUpCard = findViewById(R.id.FileExpPopUpCard);
        countTV = findViewById(R.id.FileExpSendCount);
        countChanged();
        current = -2;
        bundle = getIntent().getExtras();
        setEventHandlers();
        setupDeviceStorageCard();
        if (!Common.sharedPreferences.getBoolean("FileExpIntro", false))
            Common.showInro("FileExpIntro", "Select files you want to share using the underlying FileExplorer", this);

    }

    private void setupDeviceStorageCard() {
        LinearLayout linearLayout = findViewById(R.id.FileExpStorageCont);
        File[] f = ContextCompat.getExternalFilesDirs(getApplicationContext(), null);
        for (int i = 0; i < f.length; i++) {
            try {
                String par = f[i].getParent();
                if (par == null)
                    return;
                final File file = new File(f[i].getParent().replace("/Android/data/", "").replace(getPackageName(), "") + "/");
                if (!(file.isDirectory() && file.exists() && file.canRead())) {
                    File filee = new File(file, "hi.txt");
                    filee.createNewFile();
                    continue;
                }

                LinearLayout device = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.file_exp_devices, null, false);
                String total = Common.formatFileSize(file.getTotalSpace());
                String used = Common.formatFileSize(file.getTotalSpace() - file.getFreeSpace());
                CircularProgressBar circularProgressBar = device.findViewById(R.id.FileExpStorageProg);
                circularProgressBar.setProgressMax(Float.parseFloat(total.substring(0, total.indexOf(' '))));
                circularProgressBar.setProgress(Float.parseFloat(used.substring(0, used.indexOf(' '))));
                TextView title = device.findViewById(R.id.FileExpStorageTitleTV);
                title.setText(i == 0 ? "Internal Storage" : file.getName());
                TextView textView = device.findViewById(R.id.FileExpStorageSizeTV);
                textView.setText(used + " of " + total);
                linearLayout.addView(device);
                device.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showViewByBt(6);
                        setDirPop(file);
                    }
                });
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void setEventHandlers() {

        findViewById(R.id.FileExpBackBt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Common.vibrate();
                onBackPressed();
            }
        });

        findViewById(R.id.FileExpSend).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Common.vibrate();
                if (selectedFiles.size() == 0)
                    Toast.makeText(Common.applicationContext, "Nothing was selected", Toast.LENGTH_SHORT).show();
                else {
                    v.setOnClickListener(null);
                    if (bundle == null)
                        startActivity(new Intent(Common.applicationContext, Send.class));
                    else
                        startActivity(new Intent(Common.applicationContext, Send.class).putExtras(bundle));
                    finish();
                }
            }
        });

        findViewById(R.id.FileExpSend).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Common.vibrate();
                if (selectedFiles.size() == 0)
                    Toast.makeText(Common.applicationContext, "Nothing was selected", Toast.LENGTH_SHORT).show();
                else {
                    if (!Common.sharedPreferences.getBoolean("FileExpSelectListInfo", false))
                        Common.showInro("FileExpSelectListInfo", "Tap once on any item to remove it from the list, long press to preview it", FileExplorer.this);
                    setSelectedPop();
                    showViewByBt(-1);
                }
                return true;
            }
        });


        findViewById(R.id.FileExpImageBt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<File> images = FileHandler.getAllByCategory((AppCompatActivity) getContext(), "Images");
                if (images.isEmpty())
                    Common.makeToast("Nothing to show");
                else if (showViewByBt(0))
                    setGridPop(images);
            }
        });

        findViewById(R.id.FileExpVideoBt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<File> videos = FileHandler.getAllByCategory((AppCompatActivity) getContext(), "Videos");
                if (videos.isEmpty())
                    Common.makeToast("Nothing to show");
                else if (showViewByBt(1))
                    setGridPop(videos);
            }
        });
        findViewById(R.id.FileExpMusicBt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<File> music = FileHandler.getAllByCategory((AppCompatActivity) getContext(), "Music");
                if (music.isEmpty())
                    Common.makeToast("Nothing to show");
                else if (showViewByBt(2))
                    setMusicPop(music);
            }
        });
        findViewById(R.id.FileExpAppsBt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showViewByBt(3);
                setAppPop();
            }
        });
        findViewById(R.id.FileExpDownBt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showViewByBt(4);
                setDirPop(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS));
            }
        });
        findViewById(R.id.FileExpAllFilesBt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showViewByBt(5);
                setDirPop(Environment.getRootDirectory());
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (popUpCard != null) {
            RecyclerView.Adapter adapter = popUpCard.getAdapter();
            if (adapter != null) {
                if (adapter.getClass() == FileExpDirAdapter.class)
                    if (((FileExpDirAdapter) popUpCard.getAdapter()).backPressed())
                        return;
                if (popUpCard.getVisibility() == View.VISIBLE) {
                    if (!Common.sharedPreferences.getBoolean("FileExpSendIntro", false))
                        Common.showInro("FileExpSendIntro", "Tap on send icon to send selected files or long press it to see selected files", this);
                    Common.closeCard(popUpCard);
                    return;
                }
            }
        }
        super.onBackPressed();
    }

    private boolean showViewByBt(int i) {
        Common.vibrate();
        if (!Common.sharedPreferences.getBoolean("FileExpSelectIntro", false))
            Common.showInro("FileExpSelectIntro", "Tap once on any file to select it, Long press to preview it", this);
        Common.openCard(popUpCard);
        if (current == i)
            return false;
        current = i;
        return true;
    }

    private void setGridPop(List<File> files) {
        setPopupView(new LinearLayoutManager(getApplicationContext(), RecyclerView.VERTICAL, false),
                new FileExpImagesAdapter(files));
    }

    private void setMusicPop(List<File> files) {
        setPopupView(new LinearLayoutManager(this, RecyclerView.VERTICAL, false),
                new FileExpMusicAdapter(files));
    }

    private void setSelectedPop() {
        setPopupView(new LinearLayoutManager(this, RecyclerView.VERTICAL, false),
                new SelectedFilesAdapter());
    }

    private void setAppPop() {
        setPopupView(new LinearLayoutManager(this, RecyclerView.VERTICAL, false),
                new FileExpAppAdapter(this));
    }

    private void setDirPop(File dir) {
        File[] lis = dir.listFiles();
        if (lis == null)
            Common.makeToast("Nothing to show");
        else
            setPopupView(new LinearLayoutManager(this, RecyclerView.VERTICAL, false),
                    new FileExpDirAdapter(dir, lis));
    }

    private void setPopupView(RecyclerView.LayoutManager manager, RecyclerView.Adapter adapter) {
        popUpCard.setLayoutManager(manager);
        popUpCard.setAdapter(adapter);
    }

    private Context getContext() {
        return this;
    }
}