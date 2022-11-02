package com.sand.apm.customzycamerademo.custom;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;

import com.google.mlkit.vision.pose.PoseLandmark;

import java.util.List;

import androidx.annotation.Nullable;

/**
 * @ProjectName: MyOpenCV
 * @Date: 2022/9/1
 * @Desc:
 */
public class PoseImageView extends androidx.appcompat.widget.AppCompatImageView {

    private DetectResult detectResult;
    private Paint paint;

    public PoseImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }


    private void init() {
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setAntiAlias(true);
    }

    public void setDetectResult(DetectResult detectResult) {
        this.detectResult = detectResult;
        setImageBitmap(detectResult.bitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (null!= detectResult &&detectResult.isOk()) {

            List<PoseLandmark> marks = detectResult.getPoseInfo().getPose().getAllPoseLandmarks();
            if (null != marks) {

                for (PoseLandmark poseLandmark : marks) {
                    PointF point = poseLandmark.getPosition();
                    float fx=detectResult.getPoseInfo().getFractionWidth();
                    float fy=detectResult.getPoseInfo().getFractionHeight();
                    canvas.drawCircle(point.x * fx, point.y *fy, 5, paint);
                }
            }
        }
    }
}