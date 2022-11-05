package com.sand.apm.customzycamerademo.custom;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;

import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseLandmark;
import com.sand.apm.customzycamerademo.pose.JfPoseBoxing;
import com.sand.apm.customzycamerademo.pose.JfPoseInfo;
import com.sand.apm.customzycamerademo.pose.JfPoseKeyPoint;
import com.sand.apm.customzycamerademo.pose.JfPoseSkeleton;

import java.util.Iterator;
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
    private boolean showTwo;


    private int radius = 4;
    private float[] newPoint = new float[2];
    private JfPoseInfo mCurrentBody;
    private Bitmap marker;
    private boolean drawMarker;

    public PoseImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }


    private void init() {
        paint = new Paint();
        paint.setColor(Color.YELLOW);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(3);
    }

    public void setDetectResult(DetectResult detectResult) {
        this.detectResult = detectResult;
        if (detectResult.isDouble()) {
            setImageBitmap(detectResult.bitmapTotal);
        } else {
            setImageBitmap(detectResult.bitmap);
        }

    }

    private float fx, fy;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (null != detectResult && detectResult.isOk()) {
            if (detectResult.isDouble()) {

            } else {
                Pose pose = detectResult.getPoseInfo().getPose();
                JfPoseInfo poseSkeleton = poseLandmarkToSkeleton("", "", detectResult.getBitmap().getWidth(), detectResult.getBitmap().getHeight(), null, pose.getAllPoseLandmarks());
                List<PoseLandmark> marks = detectResult.getPoseInfo().getPose().getAllPoseLandmarks();
                if (null != marks) {
                    fx = detectResult.getPoseInfo().getFractionWidth();
                    fy = detectResult.getPoseInfo().getFractionHeight();
                    drawBody(canvas, poseSkeleton.defaultSkeleton());

                }
            }
        }

        if (isShowTwo()) {
            canvas.drawLine(canvas.getWidth() / 2, 0, canvas.getWidth() / 2, getHeight(), paint);
        }

    }

    public boolean isShowTwo() {
        return showTwo;
    }

    public void setShowTwo(boolean showTwo) {
        this.showTwo = showTwo;
        postInvalidate();
    }


    protected void drawBody(Canvas canvas, JfPoseSkeleton body) {

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
        canvas.drawCircle(site.getX() * fx, site.getY() * fy, radius, paint);
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
            canvas.drawLine(s.x * fx, s.y * fy, e.x * fx, e.y * fy, paint);
        }

        if (isCycle) {
            s = sites[0];
            e = sites[sites.length - 1];
            canvas.drawLine(s.x * fx, s.y * fy, e.x * fx, e.y * fy, paint);
        }
    }


    private static JfPoseInfo poseLandmarkToSkeleton(String imageId, String player, int width, int height, JfPoseBoxing poseBoxing, List<PoseLandmark> list) {
        JfPoseInfo poseInfo = new JfPoseInfo();
        poseInfo.setKey(imageId);
        poseInfo.setImageWidth(width);
        poseInfo.setImageHeight(height);
        poseInfo.setPoseBoxing(poseBoxing);
        poseInfo.setFrom("GoogleML");
        poseInfo.setPlayer(player);
        JfPoseSkeleton skeleton = new JfPoseSkeleton();
        skeleton.setKey(imageId);
        skeleton.setPlayer("0");
        skeleton.setImageWidth(width);
        skeleton.setImageHeight(height);
        skeleton.setFrom("GoogleML");
        Iterator var8 = list.iterator();

        while (var8.hasNext()) {
            PoseLandmark p = (PoseLandmark) var8.next();
            int type = p.getLandmarkType();
            if (type == 7) {
                skeleton.setLEar(toPoint(p));
            } else if (type == 8) {
                skeleton.setREar(toPoint(p));
            } else if (type == 2) {
                skeleton.setLEye(toPoint(p));
            } else if (type == 5) {
                skeleton.setREye(toPoint(p));
            } else if (type == 0) {
                skeleton.setNose(toPoint(p));
            } else if (type == 11) {
                skeleton.setLShoulder(toPoint(p));
            } else if (type == 12) {
                skeleton.setRShoulder(toPoint(p));
            } else if (type == 13) {
                skeleton.setLElbow(toPoint(p));
            } else if (type == 14) {
                skeleton.setRElbow(toPoint(p));
            } else if (type == 15) {
                skeleton.setLWrist(toPoint(p));
            } else if (type == 16) {
                skeleton.setRWrist(toPoint(p));
            } else if (type == 23) {
                skeleton.setLHip(toPoint(p));
            } else if (type == 24) {
                skeleton.setRHip(toPoint(p));
            } else if (type == 25) {
                skeleton.setLKnee(toPoint(p));
            } else if (type == 26) {
                skeleton.setRKnee(toPoint(p));
            } else if (type == 27) {
                skeleton.setLAnkle(toPoint(p));
            } else if (type == 28) {
                skeleton.setRAnkle(toPoint(p));
            } else if (type == 29) {
                skeleton.setLHeel(toPoint(p));
            } else if (type == 30) {
                skeleton.setRHeel(toPoint(p));
            } else if (type == 31) {
                skeleton.setLBigToe(toPoint(p));
            } else if (type == 32) {
                skeleton.setRBigToe(toPoint(p));
            }
        }

        skeleton.setChin(predictChin(skeleton));
        poseInfo.getSkeletons().add(skeleton);
        return poseInfo;
    }


    protected static final JfPoseKeyPoint predictChin(JfPoseSkeleton skeleton) {
        float shoulderLHeight = readY(skeleton.getLShoulder());
        float shoulderRHeight = readY(skeleton.getRShoulder());
        float sh = Math.max(shoulderLHeight, shoulderRHeight);
        float trunkLHeight = readY(skeleton.getLHip());
        float trunkRHeight = readY(skeleton.getRHip());
        float th = Math.max(trunkLHeight, trunkRHeight);
        float noseY = readY(skeleton.getNose());
        float lEyeY = readY(skeleton.getLEye());
        float rEyeY = readY(skeleton.getREye());
        float lEyeX = readX(skeleton.getLEye());
        float rEyeX = readX(skeleton.getREye());
        float eyeWidth = Math.abs(rEyeX - lEyeX);
        float eh = Math.max(lEyeY, rEyeY);
        float h0 = Math.abs(noseY - eh);
        float h1 = Math.abs(th - sh);
        if (eyeWidth > h0 * 1.6F) {
            float top = (readY(skeleton.getLEye()) + readY(skeleton.getREye())) / 2.0F;
            float yc = top + h0 * 4.4F;
            float xc = (readX(skeleton.getLEye()) + readX(skeleton.getREye())) / 2.0F;
            return new JfPoseKeyPoint(xc, yc, 0.5F);
        } else {
            return null;
        }
    }

    protected static float readX(JfPoseKeyPoint point) {
        return point != null ? point.x : 0.0F;
    }

    protected static float readY(JfPoseKeyPoint point) {
        return point != null ? point.y : 0.0F;
    }


    private static JfPoseKeyPoint toPoint(PoseLandmark p) {
        if (p == null) {
            return null;
        } else {
            PointF pf = p.getPosition();
            return new JfPoseKeyPoint(pf.x, pf.y, p.getInFrameLikelihood());
        }
    }

}