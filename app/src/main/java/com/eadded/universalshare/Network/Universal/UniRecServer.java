package com.eadded.universalshare.Network.Universal;

import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import com.eadded.universalshare.Adapters.SendPopUpAdapter;
import com.eadded.universalshare.Network.FileAuthNoti;
import com.eadded.universalshare.Network.FileProg;
import com.eadded.universalshare.Network.Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.LinkedList;

public abstract class UniRecServer implements Runnable, Server {

    protected final AppCompatActivity context;
    protected final LinkedList<FileAuthNoti> pending = new LinkedList<>();
    protected final SendPopUpAdapter sendPopUpAdapter = new SendPopUpAdapter(new LinkedList(), 1);
    private final ServerSocket serverSocket;
    private boolean runStatus;

    public UniRecServer(short port, AppCompatActivity context) throws IOException {
        serverSocket = new ServerSocket(port);
        runStatus = true;
        this.context = context;
    }

    protected abstract void toSave(FileAuthNoti fileAuthNoti);

    @Override
    public void run() {
        while (runStatus) {
            try {
                UniRecClient uniUploadClient = new UniRecClient(serverSocket.accept()) {
                    @Override
                    protected boolean isPending(String name, String user) {
                        return pending.contains(new FileAuthNoti(name, user, null));
                    }

                    @Override
                    protected boolean isDownloading(FileProg fileProg) {
                        return sendPopUpAdapter.fileProgs.contains(fileProg);
                    }

                    @Override
                    protected void addDownload(FileProg fileProg) {
                        sendPopUpAdapter.addItem(fileProg);
                    }

                    @Override
                    protected void showFileNoti(FileAuthNoti fileAuthNoti) {
                        toSave(fileAuthNoti);
                    }

                };
                uniUploadClient.start();
            } catch (Exception ex) {
                Log.e("UniUploadServer", "Exception with client\n" + ex);
            }
        }
    }

    public void exit() throws Throwable {
        Log.e("UniUploadServer", "Closing");
        runStatus = false;
        serverSocket.close();
        this.finalize();
    }

    public boolean isWorking() {
        return runStatus;
    }
}