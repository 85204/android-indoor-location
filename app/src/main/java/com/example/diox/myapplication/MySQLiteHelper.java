package com.example.diox.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Diox on 2016/4/12.
 */
public class MySQLiteHelper extends SQLiteOpenHelper {

    public MySQLiteHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public MySQLiteHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version,
                          DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    public Cursor queryAll() {
        return getReadableDatabase().query("myTable", new String[]{"x_id", "y_id", "mac", "ori", "signal"},
                null, null, null, null, "x_id, y_id, ori asc, signal desc");
    }

    public Cursor queryByMac(String mac, String ori) {
        return getReadableDatabase().query("myTable", new String[]{"x_id", "y_id", "mac", "signal"},
                "mac=? and ori=?", new String[]{mac, ori}, null, null, "x_id, y_id asc, signal desc");

    }

    public Cursor queryById(int x, int y, String ori) {
        return getReadableDatabase().query("myTable", new String[]{"x_id", "y_id", "ori"}, "x_id=? and y_id=? and ori=?",
                new String[]{"" + x, "" + y, "" + ori}, null, null, "signal desc");

    }

    public void insert(ContentValues cv) {
        getWritableDatabase().insert("myTable", null, cv);
    }

    public void update(ContentValues cv) {
        String[] string=new String[]{
                cv.get("x_id").toString(), cv.get("y_id").toString(),
                cv.get("mac").toString(), cv.get("ori").toString()};
        if (getReadableDatabase().query("myTable", new String[]{"x_id", "y_id", "mac", "ori"},
                "x_id=? and y_id=? and mac=? and ori=?", string,
                null, null, null).moveToNext()) {
            getWritableDatabase().update("myTable", cv, "x_id=? and y_id=? and mac=? and ori=?",
                    string);
        } else {
            insert(cv);
        }
    }

    public void close() {
        getReadableDatabase().close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table myTable(id integer PRIMARY KEY, x_id integer, y_id integer, ori char(5), mac char(17), signal integer)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
