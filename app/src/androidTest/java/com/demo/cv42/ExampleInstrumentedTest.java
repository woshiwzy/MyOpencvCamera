package com.demo.cv42;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.util.Log;

import com.tzutalin.dlib.Constants;
import com.tzutalin.dlib.FaceDet;
import com.tzutalin.dlib.VisionDetRet;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.demo.cv42", appContext.getPackageName());
    }

    @Test
    public void testDecFace() {

        FaceDet faceDet = new FaceDet(Constants.getFaceShapeModelPath());
        Bitmap bitmap = BitmapFactory.decodeFile("/sdcard/test.jpg");
        List<VisionDetRet> rets = faceDet.detect(bitmap);
        Log.e(App.tag, "rets" + rets.size());

    }


    @Test
    public void testDetBitmapFaceLandmarkDect() {
        Bitmap bitmap = BitmapFactory.decodeFile("/sdcard/test.jpg");
        assertThat(bitmap, notNullValue());
        FaceDet faceDet = new FaceDet(Constants.getFaceShapeModelPath());
        List<VisionDetRet> results = faceDet.detect(bitmap);
        for (final VisionDetRet ret : results) {
            String label = ret.getLabel();
            int rectLeft = ret.getLeft();
            int rectTop = ret.getTop();
            int rectRight = ret.getRight();
            int rectBottom = ret.getBottom();
            ArrayList<Point> landmarks = ret.getFaceLandmarks();
            assertThat(label, is("face"));
            Assert.assertTrue(landmarks.size() > 0);
            Assert.assertTrue(rectLeft > 0);
        }
        faceDet.release();
    }

}
