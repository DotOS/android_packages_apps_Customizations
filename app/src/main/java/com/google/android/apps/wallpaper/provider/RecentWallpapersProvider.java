package com.google.android.apps.wallpaper.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

public class RecentWallpapersProvider extends ContentProvider {

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public String getType(Uri uri) {
        return "vnd.android.cursor.dir/recent_wallpapers";
    }

    @Override
    public Cursor query(Uri uri, String[] strArr, String str, String[] strArr2, String str2) {
        if (!"/list_recent".equals(uri.getPath())) {
            return null;
        }
        return new MatrixCursor(new String[]{"id", "placeholder_color", "component", "title"});
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String str, String[] strArr) {
        if (!"/set_recent_wallpaper".equals(uri.getPath())) {
            return 0;
        }
        return 1;
    }

    @Override
    public int delete(Uri uri, String str, String[] strArr) {
        return 0;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        return null;
    }
}