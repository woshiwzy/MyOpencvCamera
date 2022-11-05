package com.sand.apm.customzycamerademo.lru;

import android.graphics.Bitmap;
import android.util.LruCache;

/**
 * @ProjectName: MyOpenCV
 * @Date: 2022/11/5
 * @Desc:
 */
public class LurCacheMap {

    static LruCache<String, Bitmap> mLruCache;

    static {
        //获取手机最大内存,单位kb
        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        //一般都将1/8设为LruCache的最大缓存
        int cacheSize = maxMemory / 2;

        mLruCache = new LruCache<String, Bitmap>(cacheSize) {
            /**
             * 这个方法从源码中看出来是设置已用缓存的计算方式的
             * 默认返回的值是 1，也就是每缓存一张图片就将已用缓存大小加 1
             * 缓存图片看的是占用的内存的大小，每张图片的占用内存也是不一样的
             * 因此要重写这个方法，手动将这里改为本次缓存的图片的大小
             */
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount() / 1024;
            }
        };
    }


    public static Bitmap getBitmap(int targetWidth, int targetHeight) {
        String key = targetWidth + "x" + targetHeight;
        Bitmap bitmap = mLruCache.get(key);
        if (null == bitmap) {
            bitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.RGB_565);
            mLruCache.put(key, bitmap);
        }
        return bitmap;
    }


}