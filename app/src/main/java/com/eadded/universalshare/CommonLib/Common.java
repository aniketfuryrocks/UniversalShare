package com.eadded.universalshare.CommonLib;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.*;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.TextView;
import android.widget.Toast;
import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.eadded.universalshare.Network.FileProg;
import com.eadded.universalshare.Network.UniAssetFiles;
import com.eadded.universalshare.R;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import eAddedWebEngine.HttpResHeader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLConnection;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import static android.content.Context.WIFI_SERVICE;
import static eAddedWebEngine.Common.writeHeader;

public class Common {

    public static Context applicationContext;
    public static SharedPreferences sharedPreferences;

    public static void vibrate(int millisecond) {
        Vibrator v = (Vibrator) applicationContext.getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            v.vibrate(VibrationEffect.createOneShot(millisecond, VibrationEffect.DEFAULT_AMPLITUDE));
        else
            v.vibrate(millisecond);//deprecated in API 26
    }

    public static void vibrate() {
        vibrate(20);
    }

    public static String formatFileSize(long size) {
        double c = size;

        if (c < 1024)
            return (((int) (c * 10)) / 10.0f + " bytes");
        if ((c /= 1024.0) < 1024.0)
            return (((int) (c * 10)) / 10.0f + " KB");
        if ((c /= 1024.0) < 1024.0)
            return (((int) (c * 10)) / 10.0f + " MB");
        if ((c /= 1024.0) < 1024.0)
            return (((int) (c * 10)) / 10.0f + " GB");

        return (((int) ((c / 1024.0) * 100)) / 100.0f + " TB");
    }

    public static Bitmap TextToImageEncode(String string) {
        BitMatrix bitMatrix = null;
        try {
            bitMatrix = new MultiFormatWriter().encode(string, BarcodeFormat.QR_CODE, 250, 250);
        } catch (Exception ex) {
            Log.e("UniSend", "Error creating QR Code");
            return null;
        }
        int h = bitMatrix.getHeight();
        int w = bitMatrix.getWidth();

        int[] pixels = new int[h * w];
        for (int i = 0; i < h; i++) {
            int offset = i * w;
            for (int x = 0; x < w; x++) {
                pixels[offset + x] = bitMatrix.get(i, x) ? Color.BLACK : Color.WHITE;
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, w, 0, 0, w, h);
        return bitmap;
    }

    public static void makeToast(final String s) {
        if (Looper.getMainLooper().getThread() == Thread.currentThread())
            Toast.makeText(applicationContext, s, Toast.LENGTH_SHORT).show();
        else
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(applicationContext, s, Toast.LENGTH_SHORT).show();
                }
            });
    }


    public static void writeFile(InputStream is, OutputStream outputStream, FileProg fileProg) throws IOException {
        int read;
        byte[] packet = new byte[5000];
        long totalRreead = 0;
        while (fileProg.workLoop) {
            if ((read = is.read(packet)) <= 0)
                break;
            totalRreead += read;
            fileProg.fileProgChanged((int) totalRreead);
            outputStream.write(packet, 0, read);
        }
    }

    public static void managePage(OutputStream outputStream, HttpResHeader httpResHeader, UniAssetFiles uniAssetFiles) throws IOException {
        try {
            InputStream is = applicationContext.getAssets().open(uniAssetFiles.getFile());
            httpResHeader.properties.put("Content-Length", is.available() + "");
            httpResHeader.properties.put("Content-Type", eAddedWebEngine.Common.getContentTypeForFileName(uniAssetFiles.name()));
            writeHeader(outputStream, httpResHeader);//using default values
            writeFile(is, outputStream, new FileProg(null, null, 0));
        } catch (IOException ex) {
            Log.e("Client", "Error while reading " + uniAssetFiles + "\n" + ex);
            writeHeader(outputStream, new HttpResHeader((short) 500));//write internal server error
        }
        Log.i("Client", uniAssetFiles + " written to client");
    }

    public static String getIp() {
        int ip = ((WifiManager) applicationContext.getSystemService(WIFI_SERVICE)).getDhcpInfo().ipAddress;
        return (ip == 0 ? "192.168.43.1" : Formatter.formatIpAddress(ip)) + ":6600/";
    }

    public static void closeCard(final View v) {
        Animation animation = new ScaleAnimation(v.getScaleX(), 0.0f, v.getScaleY(), 0.0f);
        animation.setDuration(200);
        animation.setRepeatCount(0);
        animation.setRepeatMode(0);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                v.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        v.startAnimation(animation);
    }

    public static void openCard(View v) {
        v.setVisibility(CardView.VISIBLE);
        Animation animation = new ScaleAnimation(0.0f, v.getScaleX(), 0.0f, v.getScaleY());
        animation.setDuration(200);
        animation.setRepeatCount(0);
        v.startAnimation(animation);
    }

    public static RequestBuilder<Drawable> getIconFromName(String name) {
        String type = URLConnection.guessContentTypeFromName(name);
        if (type == null)
            return Glide.with(applicationContext).load(R.drawable.docs);
        else if (type.startsWith("image"))
            return Glide.with(applicationContext).load(R.drawable.image);
        else if (type.startsWith("video"))
            return Glide.with(applicationContext).load(R.drawable.video);
        else if (type.startsWith("audio"))
            return Glide.with(applicationContext).load(R.drawable.music);
        else if (name.endsWith(".pdf"))
            return Glide.with(applicationContext).load(R.drawable.pdf);
        else if (type.equals("text/html"))
            return Glide.with(applicationContext).load(R.drawable.html);
        else if (type.startsWith("text"))
            return Glide.with(applicationContext).load(R.drawable.txt);
        else if (name.endsWith(".apk") || name.endsWith(".exe"))
            return Glide.with(applicationContext).load(R.drawable.apps);
        else if (name.endsWith(".zip") || name.endsWith(".rar"))
            return Glide.with(applicationContext).load(R.drawable.zip);
        else
            return Glide.with(applicationContext).load(R.drawable.docs);
    }

    public static void openFile(File file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                uri = FileProvider.getUriForFile(applicationContext, applicationContext.getPackageName() + ".fileprovider", file);
            } catch (Exception ex) {
                ex.printStackTrace();
                makeToast("Can't preview file");
            }
        } else
            uri = Uri.fromFile(file);

        if (uri == null)
            return;
        intent.setDataAndType(uri, URLConnection.guessContentTypeFromName(file.getName()));
        PackageManager pm = applicationContext.getPackageManager();
        if (intent.resolveActivity(pm) != null) {
            applicationContext.startActivity(intent);
            vibrate();
        } else
            makeToast("Can't open file");
    }

    public static void showInro(final String key, String title, Context context) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(LayoutInflater.from(context).inflate(R.layout.intro_dia, null, false));
        dialog.setCanceledOnTouchOutside(true);
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.button_border);
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                sharedPreferences.edit().putBoolean(key, true).commit();
                dialog.dismiss();
            }
        });
        TextView introHead = dialog.findViewById(R.id.introHead);
        introHead.setText(title);
        dialog.show();
    }

    public static Bitmap getBitmapForCustFile(CustFile custFile) throws ExecutionException, InterruptedException {
        try {
            return ((BitmapDrawable) custFile.icon.into(50, 50).get()).getBitmap();
        } catch (Exception ex) {
            Bitmap bitmap = null;
            Drawable dr = custFile.icon.into(50, 50).get();
            bitmap = Bitmap.createBitmap(dr.getIntrinsicWidth(), dr.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            final Canvas canvas = new Canvas(bitmap);
            dr.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            dr.draw(canvas);
            return bitmap;
        }
    }

    public static HttpResHeader addCommonProperties(HttpResHeader httpResHeader, int contentLength) {
        httpResHeader.properties.put("Server", "Universal Share");
        if (contentLength != -1)
            httpResHeader.properties.put("Content-Length", contentLength + "");
        httpResHeader.properties.put("Date", new Date().toString());
        return httpResHeader;
    }
}
