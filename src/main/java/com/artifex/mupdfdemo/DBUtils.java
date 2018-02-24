package com.artifex.mupdfdemo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by wenjin on 2018/1/19.
 */

public class DBUtils {
    private static DBUtils instance;
    private DataBaseHelp dataBaseHelp = null;
    private Context context = null;

    private DBUtils(Context context) {
        dataBaseHelp = DataBaseHelp.getInstance(context);
        this.context = context;
    }

    public synchronized static DBUtils getInstance(Context context) {
        if (instance == null)
            instance = new DBUtils(context);
        return instance;
    }

    /**
     * 检测缓存表中是否存在此条记录
     * @param url
     * @return
     */
    private  boolean checkCacheUrl(String url) {
        SQLiteDatabase db = dataBaseHelp.openDatabase();
        String sql = "select " + DataBaseHelp.URL + " from " + DataBaseHelp.TABLE_PATH + " where " + DataBaseHelp.URL
                + "=\'" + url + "\'";
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.getCount() > 0) {
            cursor.close();
            dataBaseHelp.closeDatabase();
            return true;
        } else {
            cursor.close();
            dataBaseHelp.closeDatabase();
            return false;
        }
    }

    private  void insertCacheUrl(String url, String path){
        SQLiteDatabase db = dataBaseHelp.openDatabase();
        String sql="insert into "+DataBaseHelp.TABLE_PATH+" ( "+DataBaseHelp.URL+","+DataBaseHelp.PATH+" ) VALUES ("
                +"\""+url+"\""+","
                +"\""+path+"\""+")";
        db.execSQL(sql);
        dataBaseHelp.closeDatabase();
    }

    /**
     *
     * @param url
     */
    public void addCacheURL(String url){
        if(!checkCacheUrl(url)){
            insertCacheUrl(url,"");
        }
    }

    /**
     * 获取Path
     * @param url
     * @return
     */
    public String selectCacheUrl(String url) {
        SQLiteDatabase db = dataBaseHelp.openDatabase();
        String sql = "select " + DataBaseHelp.PATH + " from " + DataBaseHelp.TABLE_PATH + " where " + DataBaseHelp.URL
                + "=\'" + url + "\'";
        Cursor cursor = db.rawQuery(sql, null);
        String path="";
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                path = cursor.getString(cursor.getColumnIndex(DataBaseHelp.PATH));
            }
        }
        cursor.close();
        dataBaseHelp.closeDatabase();
        return path;
    }

    /**
     * 更新cache的下载路径和状态
     * @param url
     * @param path
     */
    public void updateCacheURL(String url, String path){
        SQLiteDatabase db = dataBaseHelp.openDatabase();
        ContentValues values = new ContentValues();
        values.put(DataBaseHelp.PATH, path);
        String sql = DataBaseHelp.URL + "=\'" + url + "\'";
        int rowNumber = db.update(DataBaseHelp.TABLE_PATH, values, sql, null);
        dataBaseHelp.closeDatabase();
    }
}
