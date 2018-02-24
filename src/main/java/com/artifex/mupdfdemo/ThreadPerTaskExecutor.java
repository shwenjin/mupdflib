package com.artifex.mupdfdemo;

import java.util.concurrent.Executor;

/**
 * Created by wenjin on 2018/1/19.
 */

public class ThreadPerTaskExecutor implements Executor {
    public void execute(Runnable r) {
        new Thread(r).start();
    }
}
