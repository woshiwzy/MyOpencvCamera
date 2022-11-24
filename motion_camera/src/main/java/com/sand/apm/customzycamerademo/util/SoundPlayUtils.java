package com.sand.apm.customzycamerademo.util;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

import com.sand.apm.customzycamerademo.App;
import com.sand.apm.customzycamerademo.R;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * @description:
 * @author: chenwei
 * @date :   2022/4/2 4:28 下午
 */

public class SoundPlayUtils {

    // SoundPool对象
    public static SoundPool mSoundPlayer = new SoundPool(10,
            AudioManager.STREAM_SYSTEM, 5);

    public static SoundPlayUtils soundPlayUtils;

    // 上下文
    static Context mContext = App.getApp();

    /**
     * 初始化
     */
    public static SoundPlayUtils init() {
        if (soundPlayUtils == null) {
            soundPlayUtils = new SoundPlayUtils();
        }

        mSoundPlayer.load(mContext, R.raw.duang, 1);// 1s
        mSoundPlayer.load(mContext, R.raw.complete, 1);// 1s
        mSoundPlayer.load(mContext, R.raw.faill, 1);// 1s
        mSoundPlayer.load(mContext, R.raw.interest_duang, 1);// 1s
        mSoundPlayer.load(mContext, R.raw.fire, 1);// 1s
        mSoundPlayer.load(mContext, R.raw.fire2, 1);// 1s
        mSoundPlayer.load(mContext, R.raw.explore, 1);// 1s //爆炸
        mSoundPlayer.load(mContext, R.raw.gete_cacke_success, 1);// 4s 得到蛋糕
        mSoundPlayer.load(mContext, R.raw.sinister_smile, 1);// 1s 奸笑
        mSoundPlayer.load(mContext, R.raw.di, 1);// 1s 统一测试的di声音

        return soundPlayUtils;
    }

    /**
     * 播放声音
     *
     * @param soundID
     */
    static ExecutorService singExctor;
    public static void play(int soundID) {
        if(null==singExctor){
            singExctor = Executors.newCachedThreadPool();
        }
        singExctor.submit(()->{
            mSoundPlayer.play(soundID, 1, 1, 0, 0, 1);
        });

    }

}


