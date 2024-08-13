package com.moutamid.beam.utilis;

import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.content.Context;

public class FileUtils {

    public static String getFileName(Context context, Uri uri) {
        String fileName = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int i = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    fileName = cursor.getString(i);
                }
            }
        }

        if (fileName == null) {
            fileName = uri.getPath();
            int cut = fileName.lastIndexOf('/');
            if (cut != -1) {
                fileName = fileName.substring(cut + 1);
            }
        }

        return fileName;
    }

    public static String getFileExtension(String fileName) {
        String extension = null;
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex != -1) {
            extension = fileName.substring(dotIndex + 1);
        }
        return extension;
    }
}

