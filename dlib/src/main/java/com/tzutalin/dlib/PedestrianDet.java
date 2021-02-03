/*
*  Copyright (C) 2015 TzuTaLin
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.tzutalin.dlib;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.R.attr.path;

/**
 * Created by Tzutalin on 2015/10/20.
 */
public class PedestrianDet {

    // accessed by native methods
    @SuppressWarnings("unused")
    private long mNativeDetContext;
    private static final String TAG = "dlib";

    static {
        try {
            System.loadLibrary("android_dlib");
            Log.d(TAG, "jniNativeClassInit success");
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "library not found!");
        }
    }

    public PedestrianDet() {
        jniInit();
    }

    public List<VisionDetRet> detect( Bitmap bitmap) {
        VisionDetRet[] detRets = jniBitmapDetect(bitmap);
        return Arrays.asList(detRets);
    }

    public List<VisionDetRet> detect( final String path) {
        VisionDetRet[] detRets = jniDetect(path);
        return Arrays.asList(detRets);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        release();
    }

    public void release() {
        jniDeInit();
    }

    private native int jniInit();

    private synchronized native int jniDeInit();

    private synchronized native VisionDetRet[] jniDetect(String path);

    private synchronized native VisionDetRet[] jniBitmapDetect(Bitmap bitmap);

}
