package com.eadded.universalshare.CommonLib;

import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.content.CursorLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileHandler {


    public static List<File> getAllByCategory(AppCompatActivity activity, String cat) {
        switch (cat) {
            case "Images":
                return getFilesFromMediaStore(activity, new String[]{MediaStore.Images.Media.DATA}, MediaStore.Images.Media.DATE_TAKEN + " DESC", MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            case "Videos":
                return getFilesFromMediaStore(activity, new String[]{MediaStore.Video.Media.DATA}, MediaStore.Video.Media.DATE_TAKEN + " DESC", MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
            case "Music":
                return getFilesFromMediaStore(activity, new String[]{MediaStore.Audio.Media.DATA}, MediaStore.Audio.Media.DATE_ADDED + " DESC", MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
            default:
                return null;
        }
    }


    public static List<File> getFilesFromMediaStore(AppCompatActivity context, String[] columns, String orderBy, Uri uri) {
        List<File> files = new ArrayList<>();
        Cursor cursor = new CursorLoader(context, uri, columns, null, null, orderBy).loadInBackground();

        int dataColumnIndex = cursor.getColumnIndex(columns[0]);

        for (int i = 0; i < cursor.getCount(); i++) {
            cursor.moveToPosition(i);
            files.add(new File(cursor.getString(dataColumnIndex)));
        }
        return files;
    }
}
