package com.demo.cv42;

import android.app.Application;

/**
 * Created by wangzy on 2020-04-22
 * description:
 */
public class App extends Application {

    public static App app;
    public static String tag="cv4";

    @Override
    public void onCreate() {
        super.onCreate();
        app=this;
    }
}
