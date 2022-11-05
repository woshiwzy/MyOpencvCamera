package com.sand.apm.customzycamerademo.pose;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

/**
 * 可绘制对象主类
 * 1. 绘制人脸识别区域
 * 2. 绘制运动区域
 * 3. 骨骼事件向下调用处理
 * 4. 或者扩展能力
 */
public class SportOverlay extends View {

    public static final int StatusNone = 0;// 未初始化
    public static final int StatusSportSet = StatusNone + 1; // 设置参数(人脸，识别区域)
    public static final int StatusSportDoing = StatusSportSet + 1; // 运动过程

    private Paint mPaint;
    private Paint mRectPaint;
    private boolean mInit;
    protected int mStatus = StatusNone;
    protected boolean mDebug;
    private String mExtendInfo;
    private boolean mEnableDraw;
    private boolean mEnableDrawBox;
    private boolean mEnableDrawSkeleton;
    protected boolean mEnableFace;


    private Paint paint = new Paint();
    private int radius = 4;
    private float[] newPoint = new float[2];
    private JfPoseInfo mCurrentBody;
    private Bitmap marker;
    private boolean drawMarker;

    private JfPoseSkeleton body;


    public SportOverlay(Context context) {
        super(context);
        init();
    }

    public SportOverlay(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setTextSize(30);
        mPaint.setColor(Color.RED);
        mPaint.setTypeface(Typeface.DEFAULT_BOLD);

        mRectPaint = new Paint();
        mRectPaint.setAntiAlias(true);
        mRectPaint.setColor(Color.RED);
        mRectPaint.setStrokeWidth(6);
        mRectPaint.setStyle(Paint.Style.STROKE);

        enableDebug(false);
        enableDraw(true);
        enableFace(true);


        paint.setAntiAlias(true);
        paint.setColor(Color.YELLOW);
        paint.setStrokeWidth(4);
        radius = 4;
    }


    public void enableDebug(boolean debug) {
        mDebug = debug;
    }

    public void enableDrawBox(boolean enable) {
        mEnableDrawBox = enable;
    }

    public void enableDraw(boolean enable) {
        mEnableDraw = enable;
        postInvalidate();
    }

    public void enableDrawSkeleton(boolean enable) {
        mEnableDrawSkeleton = enable;
    }

    public void enableFace(boolean enable) {
        mEnableFace = enable;
    }


    @Override
    protected final void onDraw(Canvas canvas) {
        canvas.drawColor(Color.TRANSPARENT);
        if (null != body) {
            drawBody(canvas, body);
        }

    }

    protected void drawBody(Canvas canvas,JfPoseSkeleton body) {

        // 眼睛
        drawCircle(canvas, body.getLEye());
        drawCircle(canvas, body.getREye());

        // 鼻子
        drawCircle(canvas, body.getNose());

        // 下巴
        drawCircle(canvas, body.getChin());

        // 头部线条
        drawLine(canvas, true, body.getLEye(), body.getREye(), body.getNose());

        // 鼻子肩膀的连接线
        drawLine(canvas, body.getNose(), body.getLShoulder());
        drawLine(canvas, body.getNose(), body.getRShoulder());

        // 脖子
        drawCircle(canvas, body.getNeck());

        // 肩膀
        drawLine(canvas, body.getLShoulder(), body.getRShoulder(), body.getNeck());
        drawCircle(canvas, body.getLShoulder());
        drawCircle(canvas, body.getRShoulder());

        // 胳膊线条
        drawLine(canvas, body.getLShoulder(), body.getLElbow(), body.getLWrist());
        drawLine(canvas, body.getRShoulder(), body.getRElbow(), body.getRWrist());

        // 手肘
        drawCircle(canvas, body.getLElbow());
        drawCircle(canvas, body.getRElbow());

        // 手腕
        drawCircle(canvas, body.getLWrist());
        drawCircle(canvas, body.getRWrist());

        // 手指
        drawCircle(canvas, body.getLFinger());
        drawCircle(canvas, body.getRFinger());

        // 上下连接
        drawLine(canvas, body.getLShoulder(), body.getLHip());
        drawLine(canvas, body.getRShoulder(), body.getRHip());

        // 臀部
        drawLine(canvas, body.getLHip(), body.getRHip());
        drawCircle(canvas, body.getLHip());
        drawCircle(canvas, body.getRHip());
        drawCircle(canvas, body.getMidHip());

        // 腿部线条
        drawLine(canvas, body.getLHip(), body.getLKnee(), body.getLAnkle());
        drawLine(canvas, body.getRHip(), body.getRKnee(), body.getRAnkle());

        // 膝
        drawCircle(canvas, body.getLKnee());
        drawCircle(canvas, body.getRKnee());

        // 脚踝
        drawCircle(canvas, body.getLAnkle());
        drawCircle(canvas, body.getRAnkle());

        // 脚后跟
        drawLine(canvas, body.getLAnkle(), body.getLHeel());
        drawLine(canvas, body.getRAnkle(), body.getRHeel());

        // 脚大拇指
        drawLine(canvas, body.getLAnkle(), body.getLBigToe());
        drawLine(canvas, body.getRAnkle(), body.getRBigToe());

        // 脚后跟
        drawCircle(canvas, body.getLHeel());
        drawCircle(canvas, body.getRHeel());

        // 大脚趾
        drawCircle(canvas, body.getLBigToe());
        drawCircle(canvas, body.getRBigToe());
    }


    private void drawCircle(Canvas canvas, JfPoseKeyPoint site) {
        if (site == null) return;
        canvas.drawCircle(site.getX(), site.getY(), radius, paint);
    }

    private void drawLine(Canvas canvas, JfPoseKeyPoint... sites) {
        drawLine(canvas, false, sites);
    }

    private void drawLine(Canvas canvas, boolean isCycle, JfPoseKeyPoint... sites) {
        if (sites == null || sites.length <= 1) return;
        JfPoseKeyPoint s;
        JfPoseKeyPoint e;
        for (int i = 1; i < sites.length; i++) {

            s = sites[i - 1];
            e = sites[i];
            if (s == null || e == null) return;
            canvas.drawLine(s.x, s.y, e.x, e.y, paint);
        }

        if (isCycle) {
            s = sites[0];
            e = sites[sites.length - 1];
            canvas.drawLine(s.x, s.y, e.x, e.y, paint);
        }
    }


    public JfPoseSkeleton getBody() {
        return body;
    }

    public void setBody(JfPoseSkeleton body) {
        this.body = body;
        postInvalidate();
    }

    public boolean saveConfig() {
        return false;
    }


    public void updateConfig() {

    }

    protected void drawScene(Canvas canvas) {

    }

    public void restoreSportSetting() {

    }

    public void restoreFaceSetting() {

    }

    public boolean isInPoint() {
        return false;
    }
}
