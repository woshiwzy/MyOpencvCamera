package com.sand.apm.customzycamerademo;

import android.app.Application;

import com.sand.apm.customzycamerademo.util.SoundPlayUtils;

/**
 * @ProjectName: MyOpenCV
 * @Date: 2022/8/15
 * @Desc:
 */
public class App extends Application {

    public static String tag="cv";

    public static App app;

    public static App getApp() {
        return app;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        app=this;
        SoundPlayUtils.init();
    }
}