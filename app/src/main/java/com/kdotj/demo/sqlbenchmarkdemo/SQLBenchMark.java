package com.kdotj.demo.sqlbenchmarkdemo;

import android.app.Application;

import com.facebook.stetho.Stetho;

/**
 * Created by kyle.jablonski on 4/11/17.
 */

public class SQLBenchMark extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
    }
}
