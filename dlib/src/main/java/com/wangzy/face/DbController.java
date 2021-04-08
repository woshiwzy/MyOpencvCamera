package com.wangzy.face;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class DbController {
    /**
     * Helper
     */
    private MyUpgradeHelper mHelper;//获取Helper对象
    /**
     * 数据库
     */
    private SQLiteDatabase db;
    /**
     * DaoMaster
     */
    private DaoMaster mDaoMaster;
    /**
     * DaoSession
     */
    private DaoSession mDaoSession;
    /**
     * 上下文
     */
    private Context context;
    /**
     * dao
     */

    private static DbController mDbController;

    private String dbName = "person.db";

    /**
     * 获取单例
     */
    public static DbController getInstance(Context context) {
        if (mDbController == null) {
            synchronized (DbController.class) {
                if (mDbController == null) {
                    mDbController = new DbController(context);
                }
            }
        }
        return mDbController;
    }

    /**
     * 初始化
     *
     * @param context
     */
    public DbController(Context context) {
        this.context = context;
        mHelper = new MyUpgradeHelper(context, dbName);
        mDaoMaster = new DaoMaster(getWritableDatabase());
        mDaoSession = mDaoMaster.newSession();
    }

    /**
     * 获取可读数据库
     */
    private SQLiteDatabase getReadableDatabase() {
        if (mHelper == null) {
            mHelper = new MyUpgradeHelper(context, dbName);
        }
        SQLiteDatabase db = mHelper.getReadableDatabase();
        return db;
    }

    /**
     * 获取可写数据库
     *
     * @return
     */
    private SQLiteDatabase getWritableDatabase() {
        if (mHelper == null) {
            mHelper = new MyUpgradeHelper(context, dbName);
        }
        SQLiteDatabase db = mHelper.getWritableDatabase();
        return db;
    }

    public DaoSession getSession() {
        mDaoSession.clear();

        return mDaoSession;
    }

}