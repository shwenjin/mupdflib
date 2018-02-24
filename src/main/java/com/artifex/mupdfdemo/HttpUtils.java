package com.artifex.mupdfdemo;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Created by wenjin on 2018/2/24.
 */

public class HttpUtils {
    private static HttpUtils instance =null;
    private String folder;
    private String url;
    private HttpDownloadListener httpDownloadListener;
    private ExecutorService executorService=Executors.newFixedThreadPool(1);
    private HttpUtils(){
        folder=Environment.getExternalStorageDirectory() + "/mupdf" ;
    }
    public static synchronized HttpUtils getInstance(){
        if(instance==null){
            synchronized (HttpUtils.class){
                instance=new HttpUtils();
            }
        }
        return instance;
    }

    public void createUrl(String url){
        this.url=url;
    }

    private void setFolder(String folder){
        if(!TextUtils.isEmpty(folder)){
            this.folder=folder;
        }
    }

    public  void setHttpDownloadListener(HttpDownloadListener httpDownloadListener){
        this.httpDownloadListener=httpDownloadListener;
    }

    public void download(){
        executorService.execute(new TaskRunable());
    }

    private class TaskRunable implements Runnable{

        @Override
        public void run() {
            if(TextUtils.isEmpty(url)){
                throw new NullPointerException("url is null!");
            }
            InputStream input = null;
            try {
                FileUtils fileUtil = new FileUtils(handler);
                if (!fileUtil.isFileExist(folder)) {
                    //不存在，创建此文件
                    fileUtil.createDir(folder);
                }
                HttpURLConnection urlConn = (HttpURLConnection) new URL(url).openConnection();
                input = urlConn.getInputStream();
                File resultFile = fileUtil.write2SDFromInput(folder,fileUtil.getFileName(url),input,urlConn.getContentLength());
                if (resultFile != null) {
                    //下载成功
                    Message message=new Message();
                    message.obj=resultFile.getAbsolutePath();
                    message.what=2;
                    handler.sendMessage(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
                //下载失败
                handler.sendEmptyMessage(3);
            }
            finally {
                try {
                    if(input!=null) {
                        input.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    //下载失败
                    handler.sendEmptyMessage(3);
                }
            }
        }
    }

    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:
                    //progress
                    int currentSize=msg.arg1;
                    int totalSize=msg.arg2;
                    if(httpDownloadListener!=null){
                        httpDownloadListener.updateProgress(currentSize,totalSize);
                    }
                    break;
                case 2:
                    //success
                    if(httpDownloadListener!=null){
                        httpDownloadListener.onSuccess((String) msg.obj);
                    }
                    break;
                case 3:
                    //failure
                    if(httpDownloadListener!=null){
                        httpDownloadListener.onFailure();
                    }
                    break;
            }
        }
    };

    public interface HttpDownloadListener{
        public void updateProgress(long currentSize,long totalSize);
        public void onSuccess(String path);
        public void onFailure();
    }
}
