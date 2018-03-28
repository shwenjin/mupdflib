package com.artifex.mupdfdemo;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by wenjin on 2018/2/24.
 */

public class FileUtils {
    private int bufferSize=10*1024*1024;//10M
    private Handler handler;
    public FileUtils(Handler handler) {
        this.handler=handler;
    }

    public FileUtils(String SDPATH){
        //得到外部存储设备的目录（/SDCARD）
        SDPATH = Environment.getExternalStorageDirectory() + "/" ;
    }

    public FileUtils(int bufferSize) {
        this.bufferSize=bufferSize;
    }

    /**
     * 在SD卡上创建文件
     * @param fileName
     * @return
     * @throws java.io.IOException
     */
    public File createSDFile(String fileName) throws IOException {
        File file = new File(fileName);
        file.createNewFile();
        return file;
    }

    /**
     * 在SD卡上创建目录
     * @param dirName 目录名字
     * @return 文件目录
     */
    public File createDir(String dirName){
        File dir = new File(dirName);
        dir.mkdir();
        return dir;
    }

    /**
     * 判断文件是否存在
     * @param fileName
     * @return
     */
    public boolean isFileExist(String fileName){
        File file = new File(fileName);
        return file.exists();
    }

    public File write2SDFromInput(String path,String fileName,InputStream input,int totalSize){
        File file = null;
        OutputStream output = null;

        try {
            file =createSDFile(path + fileName);
            output = new FileOutputStream(file);
            int currentSize=0;
            byte [] buffer = new byte[bufferSize];
            int count=0;
            while((count=input.read(buffer)) != -1){
                currentSize+=count;
                Log.d("tag", String.valueOf(currentSize+"/"+totalSize));
                Message message=new Message();
                message.arg1=currentSize;
                message.what=1;
                message.arg2=totalSize;
                handler.sendMessage(message);
                output.write(buffer,0,count);
            }
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                if(output!=null){
                    output.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }


    /**
     * 获取文件名
     * @param url
     * @return
     */
    public String getFileName(String url){
        return url.substring(url.lastIndexOf("/")+1,url.length());
    }
}
