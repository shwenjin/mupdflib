package com.artifex.mupdfdemo;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;

/**
 * Created by wenjin on 2018/1/19.
 */
public class WJPdfView extends FrameLayout implements View.OnClickListener{
    private final String TAG=WJPdfView.class.getSimpleName();
    FrameLayout mFrameLayout;
    LinearLayout mLinearProgress;
    TextView mTxtPage;
    ImageView mImageEntireScreen;
    SpringProgressView mProgressBar;
    private OnPdfListener mOnPdfListener;
    private Context mContext;
    private boolean isDestroyed=false;
    enum TopBarMode {Main, Search, Annot, Delete, More, Accept};
    enum AcceptMode {Highlight, Underline, StrikeOut, Ink, CopyText};
    private MuPDFCore core;
    private MuPDFReaderView mDocView;
    private boolean      mButtonsVisible;
    private EditText mPasswordView;
    private TopBarMode mTopBarMode =TopBarMode.Main;
    private AlertDialog.Builder mAlertBuilder;
    private boolean mAlertsActive= false;
    private boolean mReflow = false;
    private AsyncTask<Void,Void,MuPDFAlert> mAlertTask;
    private AlertDialog mAlertDialog;
    private int mMaxPage=0;
    private int mCurrentPage=0;
    private int mHistoryPosition=0;
    private boolean isStop=false;
    private String mFolder;
    private String mUrl;
    public WJPdfView(Context context) {
        super(context);
        init(context);
    }

    public WJPdfView(Context context,AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public WJPdfView(Context context, AttributeSet attrs,int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context){
        View.inflate(context, R.layout.ecache_mypdfview,this);
        mFrameLayout=findViewById(R.id.frame_content);
        mLinearProgress=findViewById(R.id.linear_progress);
        mTxtPage=findViewById(R.id.txt_page);
        mImageEntireScreen=findViewById(R.id.image_entire_screen);
        mProgressBar=findViewById(R.id.spring_progress);
        mImageEntireScreen.setOnClickListener(this);
        this.mContext=context;
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.image_entire_screen) {
            if(mOnPdfListener!=null){
                mOnPdfListener.toggleScreen();
            }
        }
    }

    /**
     * 获取实际观看时间
     * @return
     */
    public int getReadingTime(){
        return mCurrentPage;
    }

    /**
     * 获取视频的总时间
     */
    public int getDuration(){
        return mMaxPage;
    }

    public void openPDF(String path){
        openPDF(path,0);
    }

    public void openPDF(String path,int position){
        this.mUrl=path;
        this.mHistoryPosition=position;
        mLinearProgress.setVisibility(View.VISIBLE);
        if(TextUtils.isEmpty(mUrl)){
            throw new NullPointerException();
        }

        if(mUrl.startsWith("http://")){
            String temp= DBUtils.getInstance(mContext).selectCacheUrl(mUrl);
            if(TextUtils.isEmpty(temp)){
                checkPermission();
            }else {
                File file=new File(temp);
                if(!file.exists()){
                    checkPermission();
                }else{
                    mLinearProgress.setVisibility(View.GONE);
                    initCore(temp);
                }
            }
        }else{
            mLinearProgress.setVisibility(View.GONE);
            initCore(mUrl);
        }
    }

    private void checkPermission(){
        if(ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED){
            download();
        }else{
            Toast.makeText(mContext,"request permission filed!",Toast.LENGTH_SHORT).show();
        }
    }

    private void download(){
        DBUtils.getInstance(mContext).addCacheURL(mUrl);
        HttpUtils httpUtils=HttpUtils.getInstance();
        httpUtils.createUrl(mUrl);
        httpUtils.download();
        httpUtils.setHttpDownloadListener(new HttpUtils.HttpDownloadListener() {
            @Override
            public void updateProgress(long currentSize, long totalSize) {
                mProgressBar.setMaxCount(totalSize);
                mProgressBar.setCurrentCount(currentSize);
            }

            @Override
            public void onSuccess(String path) {
                Log.d("tag","下载成功："+path);
                DBUtils.getInstance(mContext).updateCacheURL(mUrl,path);
                mLinearProgress.setVisibility(View.GONE);
                initCore(path);
            }

            @Override
            public void onFailure() {
                Log.d("tag","下载失败");
            }
        });
    }

    private void initCore(String mPath){
        if (core == null) {
            Uri uri = Uri.parse(mPath);
            core = openFile(Uri.decode(uri.getEncodedPath()));
            if (core != null && core.needsPassword()) {
                return;
            }
            if (core != null && core.countPages() == 0)
            {
                core = null;
            }
        }
        if (core == null)
        {
            AlertDialog alert = mAlertBuilder.create();
            alert.setTitle(R.string.cannot_open_document);
            alert.setButton(AlertDialog.BUTTON_POSITIVE, mContext.getString(R.string.dismiss),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            mOnPdfListener.finish();
                        }
                    });
            alert.setOnCancelListener(new DialogInterface.OnCancelListener() {

                @Override
                public void onCancel(DialogInterface dialog) {
                    mOnPdfListener.finish();
                }
            });
            alert.show();
            return;
        }

        createUI();
    }

    private void createUI() {
        if (core == null)
            return;
        mDocView = new MuPDFReaderView((Activity) mContext) {
            @Override
            protected void onMoveToChild(int i) {
                if (core == null)
                    return;
                mTxtPage.setText(String.format("%d/%d", i + 1,core.countPages()));
                mMaxPage=core.countPages();
                mCurrentPage=i + 1;
                if(core.countPages()==(mCurrentPage)){
                    mOnPdfListener.onCompletion();
                }
                super.onMoveToChild(i);
            }

            @Override
            protected void onTapMainDocArea() {
                if (!mButtonsVisible) {
                } else {
                    if (mTopBarMode == TopBarMode.Main){}
                }
            }

            @Override
            protected void onDocMotion() {
            }

            @Override
            protected void onHit(Hit item) {
                switch (mTopBarMode) {
                    case Annot:
                        if (item == Hit.Annotation) {
                            mTopBarMode = TopBarMode.Delete;
                        }
                        break;
                    case Delete:
                        mTopBarMode = TopBarMode.Annot;
                    default:
                        MuPDFView pageView = (MuPDFView) mDocView.getDisplayedView();
                        if (pageView != null)
                            pageView.deselectAnnotation();
                        break;
                }
            }
        };

        mDocView.setAdapter(new MuPDFPageAdapter(mContext, core));
        mDocView.setDisplayedViewIndex(mHistoryPosition);
        mDocView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        mDocView.setBackgroundColor(Color.rgb(255,255,255));
        mFrameLayout.removeAllViews();
        mFrameLayout.addView(mDocView);

        if (core != null) {
            core.startAlerts();
            createAlertWaiter();
        }
        if (core == null)
            return;
    }



    private void createAlertWaiter() {
        mAlertsActive = true;
        if (mAlertTask != null) {
            mAlertTask.cancel(true);
            mAlertTask = null;
        }
        if (mAlertDialog != null) {
            mAlertDialog.cancel();
            mAlertDialog = null;
        }
        mAlertTask = new AsyncTask<Void,Void,MuPDFAlert>() {

            @Override
            protected MuPDFAlert doInBackground(Void... arg0) {
                if (!mAlertsActive)
                    return null;

                return core.waitForAlert();
            }

            @Override
            protected void onPostExecute(final MuPDFAlert result) {
                // core.waitForAlert may return null when shutting down
                if (result == null)
                    return;
                final MuPDFAlert.ButtonPressed pressed[] = new MuPDFAlert.ButtonPressed[3];
                for(int i = 0; i < 3; i++)
                    pressed[i] = MuPDFAlert.ButtonPressed.None;
                DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mAlertDialog = null;
                        if (mAlertsActive) {
                            int index = 0;
                            switch (which) {
                                case AlertDialog.BUTTON1: index=0; break;
                                case AlertDialog.BUTTON2: index=1; break;
                                case AlertDialog.BUTTON3: index=2; break;
                            }
                            result.buttonPressed = pressed[index];
                            // Send the user's response to the core, so that it can
                            // continue processing.
                            core.replyToAlert(result);
                            // Create another alert-waiter to pick up the next alert.
                            createAlertWaiter();
                        }
                    }
                };
                mAlertDialog = mAlertBuilder.create();
                mAlertDialog.setTitle(result.title);
                mAlertDialog.setMessage(result.message);
                switch (result.iconType)
                {
                    case Error:
                        break;
                    case Warning:
                        break;
                    case Question:
                        break;
                    case Status:
                        break;
                }
                switch (result.buttonGroupType)
                {
                    case OkCancel:
                        mAlertDialog.setButton(AlertDialog.BUTTON2, mContext.getString(R.string.cancel), listener);
                        pressed[1] = MuPDFAlert.ButtonPressed.Cancel;
                    case Ok:
                        mAlertDialog.setButton(AlertDialog.BUTTON1, mContext.getString(R.string.okay), listener);
                        pressed[0] = MuPDFAlert.ButtonPressed.Ok;
                        break;
                    case YesNoCancel:
                        mAlertDialog.setButton(AlertDialog.BUTTON3, mContext.getString(R.string.cancel), listener);
                        pressed[2] = MuPDFAlert.ButtonPressed.Cancel;
                    case YesNo:
                        mAlertDialog.setButton(AlertDialog.BUTTON1, mContext.getString(R.string.yes), listener);
                        pressed[0] = MuPDFAlert.ButtonPressed.Yes;
                        mAlertDialog.setButton(AlertDialog.BUTTON2, mContext.getString(R.string.no), listener);
                        pressed[1] = MuPDFAlert.ButtonPressed.No;
                        break;
                }
                mAlertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    public void onCancel(DialogInterface dialog) {
                        mAlertDialog = null;
                        if (mAlertsActive) {
                            result.buttonPressed = MuPDFAlert.ButtonPressed.None;
                            core.replyToAlert(result);
                            createAlertWaiter();
                        }
                    }
                });

                mAlertDialog.show();
            }
        };

        mAlertTask.executeOnExecutor(new ThreadPerTaskExecutor());
    }

    private void destroyAlertWaiter() {
        mAlertsActive = false;
        if (mAlertDialog != null) {
            mAlertDialog.cancel();
            mAlertDialog = null;
        }
        if (mAlertTask != null) {
            mAlertTask.cancel(true);
            mAlertTask = null;
        }
    }

    private MuPDFCore openFile(String path)
    {
        int lastSlashPos = path.lastIndexOf('/');
        System.out.println("Trying to open "+path);
        try
        {
            core = new MuPDFCore(mContext, path);
            // New file: drop the old outline data
        }
        catch (Exception e)
        {
            System.out.println(e);
            return null;
        }
        return core;
    }

    private MuPDFCore openBuffer(byte buffer[])
    {
        System.out.println("Trying to open byte buffer");
        try {
            core = new MuPDFCore(mContext, buffer);
            // New file: drop the old outline data
        }
        catch (Exception e)
        {
            System.out.println(e);
            return null;
        }
        return core;
    }

    private Object onRetainNonConfigurationInstance()
    {
        MuPDFCore mycore = core;
        core = null;
        return mycore;
    }

    private void reflowModeSet(boolean reflow)
    {
        mReflow = reflow;
        mDocView.setAdapter(mReflow ? new MuPDFReflowAdapter(mContext, core) : new MuPDFPageAdapter(mContext, core));
        mDocView.refresh(mReflow);
    }

    private void toggleReflow() {
        reflowModeSet(!mReflow);
    }

    public void setOnPdfListener(OnPdfListener onPdfListener){
        this.mOnPdfListener=onPdfListener;
    }

    public void updateAdapter(){
        if(core!=null){
            mDocView.setAdapter(new MuPDFPageAdapter(mContext, core));
            mDocView.setDisplayedViewIndex(mCurrentPage-1);
        }
    }

    public void onDestroy(){
        if (core != null)
            core.onDestroy();
        if (mAlertTask != null) {
            mAlertTask.cancel(true);
            mAlertTask = null;
        }
        core = null;
    }

    public void onStart(){
        if(isStop&&mOnPdfListener!=null){
            mOnPdfListener.onstart();
        }
    }

    public void onStop() {
        isStop=true;
        if (core != null) {
            destroyAlertWaiter();
            core.stopAlerts();
        }
    }

    public interface OnPdfListener{
        public void finish();
        public void onCompletion();
        public void toggleScreen();
        public void onstart();
    }
}