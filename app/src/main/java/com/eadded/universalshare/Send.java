package com.eadded.universalshare;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.eadded.universalshare.CommonLib.Common;
import com.eadded.universalshare.CommonLib.CustFile;
import com.eadded.universalshare.Network.AuthNoti;
import com.eadded.universalshare.Network.FileAuthNoti;
import com.eadded.universalshare.Network.Server;
import com.eadded.universalshare.Network.Universal.UniRecServer;
import com.eadded.universalshare.Network.Universal.UniSendServer;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Send extends AppCompatActivity {

    private static Server server;
    private boolean isActive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);
        if (Common.sharedPreferences == null) {
            Common.applicationContext = getApplicationContext();
            Common.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        }
        if (!Common.sharedPreferences.getBoolean("SendInfo", false))
            Common.showInro("SendInfo", "For faster file transfer we recommend using hotspot (Wifi Direct)", this);
        start();
    }

    private void start() {
        isActive = true;
        updateLink();
        setEvents();
        try {
            if (server == null)
                startServ(getIntent().getIntExtra("type", 0));
            if (server.hasPending())
                server.showNoti();
        } catch (Exception ex) {
            ex.printStackTrace();
            Common.makeToast("Something went wrong !");
        }
    }

    private void setEvents() {
        findViewById(R.id.sendTransferBt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Common.vibrate();
                server.showPopup();
            }
        });
        findViewById(R.id.sendBackBt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Common.vibrate();
                onBackPressed();
            }
        });
        findViewById(R.id.sendInfoTv).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final Intent intent = new Intent(Intent.ACTION_MAIN, null);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                final ComponentName cn = new ComponentName("com.android.settings", "com.android.settings.TetherSettings");
                intent.setComponent(cn);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;
            }
        });
    }

    private void stop() {
        isActive = false;
        if (server != null) {
            server.closeNoti();
            server.closePupup();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stop();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        start();
    }

    private void startServ(int type) throws Exception {
        switch (type) {
            case 0:
                Intent intent = getIntent();
                String action = intent.getAction();
                LinkedList<CustFile> custFiles = null;
                if (Intent.ACTION_SEND.equals(action) && intent.getType() != null) {
                    try {
                        Uri imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                        custFiles = new LinkedList<>();
                        custFiles.add(new CustFile(imageUri, imageUri.getLastPathSegment()));

                    } catch (Exception e) {
                        e.printStackTrace();
                        Common.makeToast("Something went wrong !");
                        finish();
                        return;
                    }
                } else if (Intent.ACTION_SEND_MULTIPLE.equals(action)) {
                    custFiles = new LinkedList<>();
                    ArrayList<Uri> list = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
                    for (int i = 0; i < list.size(); i++) {
                        Uri uri = list.get(i);
                        custFiles.add(new CustFile(uri, uri.getLastPathSegment()));
                    }
                } else
                    custFiles = FileExplorer.selectedFiles;

                if (custFiles.size() == 0) {
                    Common.makeToast("Something went wrong !");
                    finish();
                    return;
                }
                startUniSendServer(custFiles);
                break;
            case 1:
                startUniRecServer();
                break;
            default:
                Log.e("Send", "Unknown server type " + type);
                Common.makeToast("Something went wrong !");
        }
    }

    private void startUniSendServer(List<CustFile> files) throws IOException {
        server = new UniSendServer(6600, files, this) {

            private Dialog dialog;
            private BottomSheetDialog bottomSheetDialog;

            @Override
            public void toAuth(AuthNoti authNoti) {
                pending.add(authNoti);
                if (pending.size() == 1)
                    showNoti();
            }

            @Override
            public void closeNoti() {
                if (dialog != null) {
                    if (dialog.isShowing())
                        dialog.dismiss();
                }
            }

            @Override
            public void showNoti() {
                if (!isActive)
                    return;
                if (Looper.getMainLooper().getThread() != Thread.currentThread()) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            showNoti();
                        }
                    });
                    return;
                }
                dialog = new Dialog(context);
                dialog.getWindow().setBackgroundDrawableResource(R.color.transparent);
                dialog.setCanceledOnTouchOutside(false);
                View alert = LayoutInflater.from(context).inflate(R.layout.unishare_auth_noti, null, false);
                dialog.setContentView(alert);
                ((TextView) alert.findViewById(R.id.authName)).setText("Allow " + pending.getFirst().name + " to join ?");
                alert.findViewById(R.id.authAccept).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final AuthNoti authNoti = pending.poll();
                        if (!pending.isEmpty())
                            showNoti();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                authNoti.authEvent.onAuth(authNoti, true);
                            }
                        }).start();
                        dialog.dismiss();
                    }
                });
                dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        final AuthNoti authNoti = pending.poll();
                        if (!pending.isEmpty())
                            showNoti();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                authNoti.authEvent.onAuth(authNoti, false);
                            }
                        }).start();
                        dialog.dismiss();
                    }
                });
                alert.findViewById(R.id.authReject).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.cancel();
                    }
                });
                alert.findViewById(R.id.authBlock).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final AuthNoti authNoti = pending.poll();
                        if (!pending.isEmpty())
                            showNoti();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                authNoti.authEvent.onAuth(authNoti, false);
                                block(authNoti.sess);
                            }
                        }).start();
                        dialog.dismiss();
                    }
                });
                dialog.show();
                Common.vibrate();
            }

            @Override
            public void showPopup() {
                if (sendPopUpAdapter.fileProgs.isEmpty()) {
                    Common.makeToast("Nothing to show");
                    return;
                }
                RecyclerView recyclerView = (RecyclerView) LayoutInflater.from(context).inflate(R.layout.send_popup_card, null, false);
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
                recyclerView.setAdapter(sendPopUpAdapter);
                bottomSheetDialog = new BottomSheetDialog(context);
                bottomSheetDialog.setCanceledOnTouchOutside(true);
                bottomSheetDialog.setContentView(recyclerView);
                bottomSheetDialog.getWindow().setBackgroundDrawableResource(R.color.transparent);
                bottomSheetDialog.show();
            }

            @Override
            public void closePupup() {
                if (bottomSheetDialog != null)
                    if (bottomSheetDialog.isShowing())
                        bottomSheetDialog.dismiss();
            }
        };
        new Thread((Runnable) server).start();
    }

    private void startUniRecServer() throws IOException {
        server = new UniRecServer((short) 6600, this) {
            BottomSheetDialog bottomSheetDialog;
            private Dialog dialog;

            @Override
            public boolean hasPending() {
                return !pending.isEmpty();
            }

            @Override
            protected void toSave(FileAuthNoti fileAuthNoti) {
                pending.add(fileAuthNoti);
                if (pending.size() == 1)
                    showNoti();
            }

            @Override
            public void showNoti() {
                if (!isActive)
                    return;
                if (Looper.getMainLooper().getThread() != Thread.currentThread()) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            showNoti();
                        }
                    });
                    return;
                }
                dialog = new Dialog(context);
                dialog.setContentView(LayoutInflater.from(context).inflate(R.layout.unirec_file_noti, null, false));
                dialog.setCanceledOnTouchOutside(false);
                dialog.getWindow().setBackgroundDrawableResource(R.drawable.button_border);
                dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        pending.poll().fileAuthEvent.onFileAuth(null, false);
                        dialog.dismiss();
                    }
                });
                ((TextView) dialog.findViewById(R.id.recFileTitle)).append(pending.getFirst().user + " ?");
                ((EditText) dialog.findViewById(R.id.recFileName)).setText(pending.getFirst().name);
                dialog.findViewById(R.id.recFileYes).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String name = ((EditText) dialog.findViewById(R.id.recFileName)).getText().toString() + "";
                        if (name.isEmpty()) {
                            Common.makeToast("Please enter file name");
                            return;
                        }
                        final File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + name);
                        if (file.exists())
                            Common.makeToast("File already exists");
                        else {
                            final FileAuthNoti fileAuthNoti = pending.poll();
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    fileAuthNoti.fileAuthEvent.onFileAuth(file, true);
                                }
                            }).start();
                            dialog.dismiss();
                            if (!pending.isEmpty())
                                showNoti();
                        }
                    }
                });
                dialog.findViewById(R.id.recFileNo).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.cancel();
                    }
                });
                dialog.show();
                Common.vibrate();
            }

            @Override
            public void closeNoti() {
                if (dialog != null)
                    dialog.dismiss();
            }

            @Override
            public void showPopup() {
                if (sendPopUpAdapter.fileProgs.isEmpty()) {
                    Common.makeToast("Nothing to show");
                    return;
                }
                RecyclerView recyclerView = (RecyclerView) LayoutInflater.from(context).inflate(R.layout.send_popup_card, null, false);
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
                recyclerView.setAdapter(sendPopUpAdapter);
                bottomSheetDialog = new BottomSheetDialog(context);
                bottomSheetDialog.setCanceledOnTouchOutside(true);
                bottomSheetDialog.setContentView(recyclerView);
                bottomSheetDialog.getWindow().setBackgroundDrawableResource(R.color.transparent);
                bottomSheetDialog.show();
            }

            @Override
            public void closePupup() {
                if (bottomSheetDialog != null)
                    if (bottomSheetDialog.isShowing())
                        bottomSheetDialog.dismiss();
            }
        };
        new Thread((Runnable) server).start();
    }

    private void updateLink() {
        String ip = "http://" + Common.getIp();
        ((ImageView) findViewById(R.id.sendQR)).setImageBitmap(Common.TextToImageEncode(ip));
        ((TextView) findViewById(R.id.sendLink)).setText(ip);
    }

    @Override
    public void onBackPressed() {
        if (server.isWorking()) {
            try {
                server.exit();
                server = null;
                super.onBackPressed();
            } catch (Throwable th) {
                Common.makeToast("Error exiting server");
            }
        } else {
            server = null;
            super.onBackPressed();
        }
    }
}