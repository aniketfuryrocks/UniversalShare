package com.eadded.universalshare.Network.Universal;

import android.graphics.Bitmap;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import com.eadded.universalshare.Adapters.SendPopUpAdapter;
import com.eadded.universalshare.CommonLib.Common;
import com.eadded.universalshare.CommonLib.CustFile;
import com.eadded.universalshare.Network.*;
import eAddedWebEngine.HttpHeader;
import eAddedWebEngine.HttpResHeader;
import eAddedWebEngine.Router;

import java.io.*;
import java.net.Socket;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import static com.eadded.universalshare.CommonLib.Common.applicationContext;


public abstract class UniSendServer implements Runnable, Server {

    protected final SendPopUpAdapter sendPopUpAdapter = new SendPopUpAdapter(new LinkedList(), 0);
    protected final LinkedList<AuthNoti> pending = new LinkedList<>();
    protected final AppCompatActivity context;
    private final eAddedWebEngine.Server server;
    private final List<CustFile> files;
    private final LinkedList<String> blocked = new LinkedList<>();
    private final HashMap<String, String> auths = new HashMap();

    protected UniSendServer(int port, List<CustFile> files, AppCompatActivity context) throws IOException {
        this.files = files;
        this.context = context;
        this.server = new eAddedWebEngine.Server(port, new Router() {
            boolean shouldWait = false;

            @Override
            public boolean acceptConnection(Socket socket) {
                return true;
            }

            @Override
            public void onGET(HttpHeader httpHeader, Socket socke, DataInputStream dis, OutputStream outputStream) {
                try {
                    switch (httpHeader.uri.getPath()) {
                        case "/":
                            String sess = httpHeader.cookies.get("US-S") + "";
                            HttpResHeader httpResHeader = Common.addCommonProperties(new HttpResHeader(), -1);
                            httpResHeader.properties.put("Cache-Control", "no-store");
                            if (isAuth(httpHeader)) {
                                eAddedWebEngine.Common.redirect(outputStream, httpResHeader, "/downloads");
                            } else if (pending.contains(new AuthNoti(null, sess, null))) {
                                Common.managePage(outputStream, httpResHeader, UniAssetFiles.pendingHTML);
                            } else if (blocked.contains(sess)) {
                                Common.managePage(outputStream, httpResHeader, UniAssetFiles.blockedHTML);
                            } else {
                                if (sess.length() != 50)
                                    httpResHeader.properties.put("Set-Cookie", "US-S=" + eAddedWebEngine.Common.createRandomString(50));
                                Common.managePage(outputStream, httpResHeader, UniAssetFiles.indexHTML);
                            }
                            break;
                        case "/downloads":
                            if (isAuth(httpHeader)) {
                                Log.i("", httpHeader.uri.getQuery() + "");
                                String query = httpHeader.uri.getQuery();
                                if (query == null || query.isEmpty()) {
                                    HttpResHeader httpHead = Common.addCommonProperties(new HttpResHeader(), -1);
                                    httpHead.properties.put("Content-Type", "text/html");
                                    httpHead.properties.put("Cache-Control", "no-store");
                                    HashMap<String, String> hashMap = new HashMap<>();
                                    hashMap.put("name", "eAdded");
                                    String file = eAddedWebEngine.Common.template(new DataInputStream(applicationContext.getAssets().open(UniAssetFiles.downloadHTML.getFile())), "&!#@", hashMap);
                                    eAddedWebEngine.Log.o(file);
                                    httpHead.properties.put("Content-Length", file.length() + "");
                                    eAddedWebEngine.Common.writeHeaderContent(file.getBytes(), httpHead, outputStream);
                                    // eAddedWebEngine.Common.template(),outputStream,addCommonProperties(new HttpResHeader(),-1),"&!#1",new String[]{"yo"});
                                } else {
                                    try {
                                        manageFile(outputStream, Integer.parseInt(query), httpHeader.cookies.get("US-N"));
                                    } catch (NumberFormatException ex) {
                                        Log.e("sa", "NUmber format occured");
                                        eAddedWebEngine.Common.writeHeader(outputStream, new HttpResHeader((short) 500));
                                        ex.printStackTrace();
                                        //exception due to number parse
                                    }
                                }
                            } else {
                                eAddedWebEngine.Common.redirect(outputStream, Common.addCommonProperties(new HttpResHeader(), -1), "/");
                            }
                            break;
                        case "/icon":
                            if (isAuth(httpHeader)) {
                                CustFile custFile = getFile(Integer.parseInt(httpHeader.uri.getQuery()));
                                if (custFile == null)
                                    eAddedWebEngine.Common.writeHeader(outputStream, new HttpResHeader((short) 404));
                                else {
                                    try {
                                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                                        Common.getBitmapForCustFile(custFile).compress(Bitmap.CompressFormat.PNG, 0, bos);
                                        byte[] bitmapdata = bos.toByteArray();
                                        HttpResHeader head = new HttpResHeader();
                                        head.properties.put("Content-Type", "image/png");
                                        head.properties.put("Content-Length", bitmapdata.length + "");
                                        eAddedWebEngine.Common.writeHeader(outputStream, Common.addCommonProperties(head, -1));
                                        outputStream.write(bitmapdata);
                                    } catch (Exception e) {
                                        eAddedWebEngine.Common.writeHeader(outputStream, Common.addCommonProperties(new HttpResHeader((short) 500), 0));
                                        e.printStackTrace();
                                    }
                                }
                            } else
                                eAddedWebEngine.Common.writeHeader(outputStream, Common.addCommonProperties(new HttpResHeader((short) 403), 0));
                            break;
                        default:
                            eAddedWebEngine.Common.redirect(outputStream, Common.addCommonProperties(new HttpResHeader(), -1), "/");
                    }
                    outputStream.flush();
                } catch (IOException ex) {
                    ex.printStackTrace();
                    //exception while getting output stream from socket
                }
            }

            @Override
            public void onPOST(final HttpHeader httpHeader, final Socket socket, DataInputStream dis, final OutputStream outputStream) {
                try {
                    switch (httpHeader.uri.getPath()) {
                        case "/init":
                            String name = eAddedWebEngine.Common.readContentAsString(0, httpHeader.contentLength, dis) + "";
                            if (name.isEmpty())
                                eAddedWebEngine.Common.writeHeaderContent("Name can't be empty".getBytes(), new HttpResHeader(), outputStream);
                            else {
                                String sess = httpHeader.cookies.get("US-S") + "";
                                HttpResHeader httpResHeader = Common.addCommonProperties(new HttpResHeader(), -1);
                                if (sess.length() != 50) {
                                    httpResHeader.properties.put("Set-Cookie", "US-S" + eAddedWebEngine.Common.createRandomString(50));
                                    Common.managePage(outputStream, httpResHeader, UniAssetFiles.wengtWrongHTML);
                                } else if (isAuth(httpHeader)) {
                                    eAddedWebEngine.Common.redirect(outputStream, httpResHeader, "/downloads");
                                } else if (pending.contains(new AuthNoti(null, sess, null))) {
                                    httpHeader.properties.put("Cache-Control", "no-store");
                                    Common.managePage(outputStream, httpResHeader, UniAssetFiles.pendingHTML);
                                } else if (blocked.contains(sess)) {
                                    httpHeader.properties.put("Cache-Control", "no-store");
                                    Common.managePage(outputStream, httpResHeader, UniAssetFiles.blockedHTML);
                                } else {
                                    final Thread thread = Thread.currentThread();
                                    shouldWait = true;
                                    toAuth(new AuthNoti(name, sess, new AuthEvent() {
                                        @Override
                                        public void onAuth(AuthNoti authNoti, boolean authStat) {
                                            try {
                                                HttpResHeader httpResHeader = Common.addCommonProperties(new HttpResHeader(), -1);
                                                if (authStat) {
                                                    auths.put(authNoti.sess, authNoti.name);
                                                    eAddedWebEngine.Common.redirect(outputStream, Common.addCommonProperties(new HttpResHeader(), 0), "/downloads");
                                                } else {
                                                    httpHeader.properties.put("Cache-Control", "no-store");
                                                    Common.managePage(outputStream, httpResHeader, UniAssetFiles.deniedHTML);
                                                }
                                            } catch (Exception ex) {
                                                Log.i("asdasd", "eeeeexception");
                                                ex.printStackTrace();
                                            }
                                            synchronized (thread) {
                                                try {
                                                    shouldWait = false;
                                                    thread.notify();
                                                } catch (Exception ex) {
                                                    ex.printStackTrace();
                                                }
                                            }
                                        }
                                    }));
                                    synchronized (thread) {
                                        while (shouldWait) {
                                            try {
                                                thread.wait();
                                            } catch (Exception ex) {
                                                ex.printStackTrace();
                                            }
                                        }
                                    }
                                }
                            }
                            break;
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();//output stream error
                }
            }

            @Override
            public void onDELETE(HttpHeader httpHeader, Socket socket, DataInputStream dis, OutputStream outputStream) {

            }

            @Override
            public void onOther(HttpHeader httpHeader, Socket socket, DataInputStream dis, OutputStream outputStream) {

            }
        });
    }

    protected abstract void toAuth(AuthNoti authNoti);

    @Override
    public boolean hasPending() {
        return !pending.isEmpty();
    }


    public void block(String sess) {
        blocked.add(sess);
    }

    @Override
    public void run() {
        server.start();
        Log.e("Server", "Loop ended");
    }

    @Override
    public void exit() throws Throwable {
        server.stop();
    }

    public final boolean isWorking() {
        return server.isRunning();
    }

    private boolean isAuth(HttpHeader httpHeader) {
        return auths.containsKey(httpHeader.cookies.get("US-S") + "");
    }

    private void manageFile(OutputStream outputStream, int index, String user) {
        CustFile file = getFile(index);
        if (file == null) {
            Log.i("Client", "No such file found");
            try {
                eAddedWebEngine.Common.redirect(outputStream, Common.addCommonProperties(new HttpResHeader(), -1), "/downloads");
            } catch (Exception e) {
                Log.e("Client", "Error redirecting " + e);
            }
            return;
        }
        try {
            InputStream inputStream = file.getStream();
            HttpResHeader head = new HttpResHeader();
            String guess = URLConnection.guessContentTypeFromName(file.name);
            head.properties.put("Content-Type", guess == null ? "application/octet-stream" : guess);
            head.properties.put("Content-Length", file.file.length() + "");
            head.properties.put("Content-Disposition", "attachment; filename = \"" + file.name + "\"");
            eAddedWebEngine.Common.writeHeader(outputStream, head);
            FileProg fileProg = new FileProg(file, user, (int) file.file.length());
            try {
                sendPopUpAdapter.addItem(fileProg);
                Common.makeToast("Sending file " + file.name);
                Common.writeFile(inputStream, outputStream, fileProg);
            } catch (Exception ex) {
                fileProg.fileProgCancel();
                Log.i("Client", "Error writing file " + ex);
                Common.makeToast("Error while sending file");
            }
        } catch (Exception ex) {
            Log.i("Client", "Error while establishing file Stream or writing header " + ex);
            Common.makeToast("Error while sending file");
            try {
                eAddedWebEngine.Common.redirect(outputStream, Common.addCommonProperties(new HttpResHeader(), -1), "/downloads");
            } catch (Exception e) {
                Log.e("Client", "Error redirecting " + e);
            }

        }
    }

    private CustFile getFile(int i) {
        if (i >= files.size() || i < 0) {
            Log.i("Client", "File asked out of context");
            return null;
        }
        return files.get(i);
    }
}