package com.artifex.mupdfdemo;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by wenjin on 2018/1/19.
 */

public class DataBaseHelp  extends SQLiteOpenHelper {
    private static int connectionCount = 0;
    private SQLiteDatabase db = null;
    public static DataBaseHelp INSTANCE = null;

    public static String TABLE_PATH="table_path";//退出登录不需要清理
    public static String URL="url";//资料的网络路径
    public static String PATH="path";//资料本地路径

    public DataBaseHelp(Context context) {
        super(context, "mupdf.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_PATH + " (" + ""
                +URL               +" text PRIMARY KEY  ,"+""
                +PATH            +" text "+ ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public static synchronized DataBaseHelp getInstance(Context context) {
        if(null == INSTANCE) {
            INSTANCE = new DataBaseHelp(context);
        }
        return INSTANCE;
    }

    public SQLiteDatabase openDatabase() {
        synchronized (this) {
            if (db == null) {
                db = getWritableDatabase();
            }
            if(false == db.isOpen()) {
                db = getWritableDatabase();
            }
            connectionCount++;
            return db;
        }
    }

    public void closeDatabase() {
        synchronized (this) {
            connectionCount--;
            if (connectionCount <= 0) {
                db.close();
                db = null;
            }
        }
    }
}
