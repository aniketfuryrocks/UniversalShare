package com.eadded.universalshare.Network.Universal;

import android.os.Environment;
import android.util.Log;
import com.eadded.universalshare.CommonLib.Common;
import com.eadded.universalshare.CommonLib.CustFile;
import com.eadded.universalshare.Network.FileAuthEvent;
import com.eadded.universalshare.Network.FileAuthNoti;
import com.eadded.universalshare.Network.FileProg;
import com.eadded.universalshare.Network.UniAssetFiles;
import eAddedWebEngine.HttpHeader;
import eAddedWebEngine.HttpResHeader;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.Socket;

public abstract class UniRecClient extends Thread {

    private final Socket socket;
    private final DataInputStream inputStream;
    private final OutputStream outputStream;

    UniRecClient(Socket socket) throws Exception {
        this.socket = socket;
        this.inputStream = new DataInputStream(socket.getInputStream());
        this.outputStream = socket.getOutputStream();
    }

    @Override
    public void run() {
        Log.i("UniUploadClient", "new Client");
        try {
            final HttpHeader httpHeader = eAddedWebEngine.Common.getHeader(inputStream);
            if (httpHeader.method.equals("GET")) {
                Common.managePage(socket.getOutputStream(), new HttpResHeader(), UniAssetFiles.uploadHTML);
            } else if (httpHeader.method.equals("POST")) {
                switch (httpHeader.uri.getPath()) {
                    case "/rec":
                        //getting title
                        String webBound = inputStream.readLine();
                        String line = "";
                        int totalToRemove = webBound.getBytes().length + "\r\n".getBytes().length;
                        totalToRemove *= 2;//there are 2 webbounds
                        totalToRemove += "--".getBytes().length;//last line has extra --
                        while (!(line = inputStream.readLine()).isEmpty()) {
                            totalToRemove += line.getBytes().length + "\r\n".getBytes().length;
                        }
                        //there are two extra lines at begining and end
                        totalToRemove += ("\r\n".getBytes().length) * 2;
                        httpHeader.contentLength -= totalToRemove;
                        String title = httpHeader.cookies.get("UniShare_FN");
                        String user = httpHeader.cookies.get("UniShare_Name");
                        if (user == null)
                            user = "";
                        if (user.isEmpty()) {
                            user = "unknown";
                            httpHeader.cookies.put("UniShare_Name", user);
                        }
                        if (title == null)
                            title = "";
                        if (title.isEmpty())
                            title = "unnamed(" + eAddedWebEngine.Common.createRandomString(10) + ")";

                        if (httpHeader.contentLength > Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsoluteFile().getUsableSpace())
                            Common.makeToast("Not Enough space");
                        else if (isPending(title, user))
                            eAddedWebEngine.Common.writeHeaderContent("F_P".getBytes(), new HttpResHeader(), outputStream);
                        else if (isDownloading(new FileProg(new CustFile(new File(title)), user, 0)))
                            eAddedWebEngine.Common.writeHeaderContent("A_D".getBytes(), new HttpResHeader(), outputStream);
                        else {
                            showFileNoti(new FileAuthNoti(title, user, new FileAuthEvent() {
                                @Override
                                public void onFileAuth(File file, boolean toSave) {
                                    if (toSave) {
                                        FileProg fileProg = new FileProg(new CustFile(file, null, file.getName()), httpHeader.cookies.get("UniShare_Name"), httpHeader.contentLength);
                                        try {
                                            long leftToRead = httpHeader.contentLength;
                                            if (file.createNewFile()) {
                                                Common.makeToast("Receiving file  " + file.getName());
                                                addDownload(fileProg);
                                                FileOutputStream fos = new FileOutputStream(file);
                                                int read = 0;
                                                byte[] packet = new byte[20000];
                                                while (fileProg.workLoop) {
                                                    if ((read = inputStream.read(packet)) <= 0)
                                                        break;
                                                    Log.i("ass", "Read " + (read));
                                                    if (read > leftToRead)
                                                        read = (int) leftToRead;

                                                    fos.write(packet, 0, read);
                                                    leftToRead -= read;
                                                    fileProg.fileProgChanged((int) (httpHeader.contentLength - leftToRead));
                                                    if (leftToRead <= 0)
                                                        break;
                                                }
                                                if (leftToRead <= 0) {
                                                    Common.makeToast("File downloaded " + file.getName());
                                                    Common.managePage(outputStream, new HttpResHeader(), UniAssetFiles.uploadHTML);
                                                } else {
                                                    if (fileProg.workLoop) {
                                                        Common.makeToast("Unable to download full file\n" + file.getName());
                                                        fileProg.fileProgCancel();
                                                    } else
                                                        Common.makeToast("Download canceled\n" + file.getName());
                                                    fos.close();
                                                    file.delete();
                                                }
                                            } else
                                                Common.makeToast("Unable to create file");
                                        } catch (Exception ex) {
                                            fileProg.fileProgCancel();
                                            ex.printStackTrace();
                                            Common.makeToast("Something went wrong");
                                        }
                                    } else {
                                        try {
                                            Common.makeToast("Denied");
                                            eAddedWebEngine.Common.writeHeaderContent("F_D".getBytes(), new HttpResHeader(), outputStream);//File denied
                                        } catch (Throwable ex) {
                                            Log.e("UniUploadClient", "Error while sending F_D message\n" + ex);
                                        }
                                    }
                                    try {
                                        exit();
                                    } catch (Throwable ex) {
                                        Log.e("UniUploadClient", "Error while exiting\n" + ex);
                                    }
                                }
                            }));
                            return;
                        }
                }
            } else {
                Log.e("UniUploadClient", "Unexpected request from client -> " + httpHeader.method);
            }
        } catch (Exception ex) {
            Log.e("UniUploadClient", "Error in client thread\n" + ex);
        }
        try {
            exit();
        } catch (Throwable ex) {
            Log.e("UniUploadClient", "Error while exiting\n" + ex);
        }
    }

    public void exit() throws Throwable {
        inputStream.close();
        outputStream.close();
        socket.close();
        this.finalize();
    }

    protected abstract void showFileNoti(FileAuthNoti fileAuthNoti);

    protected abstract boolean isPending(String name, String user);

    protected abstract boolean isDownloading(FileProg fileProg);

    protected abstract void addDownload(FileProg fileProg);

}