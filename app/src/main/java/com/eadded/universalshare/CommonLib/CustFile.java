package com.eadded.universalshare.CommonLib;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.eadded.universalshare.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

public class CustFile {

    public String name;
    public File file;
    public RequestBuilder<Drawable> icon;
    public Uri uri;

    public CustFile(File file, RequestBuilder<Drawable> requestBuilder, String name) {
        this.file = file;
        this.icon = requestBuilder;
        this.name = name;
        this.uri = null;
    }

    public CustFile(File file) {
        this.file = file;
        this.name = file.getName();
        this.uri = null;
        setIcon();
    }

    public CustFile(Uri uri, String name) {
        this.uri = uri;
        this.name = name;
        this.file = null;
        setIcon();
    }

    public InputStream getStream() throws IOException {
        if (file == null)
            return Common.applicationContext.getContentResolver().openInputStream(uri);
        else
            return new FileInputStream(file);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null) {
            CustFile custFile = (CustFile) obj;
            return custFile.file.equals(file) && custFile.name.equals(name);
        }
        return false;
    }

    private void setIcon() {
        String type = URLConnection.guessContentTypeFromName(this.name);
        if (type != null) {
            if (type.startsWith("image") || type.startsWith("video")) {

                this.icon = Glide.with(Common.applicationContext).load(this.file == null ? this.uri : this.file);
            } else
                this.icon = Common.getIconFromName(this.name);
        } else
            this.icon = Glide.with(Common.applicationContext).load(R.drawable.docs);
    }
}
