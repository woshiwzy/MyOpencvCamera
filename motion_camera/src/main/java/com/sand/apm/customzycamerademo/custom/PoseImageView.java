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
import com.jf.lib.pose.JfPoseBoxing;
import com.jf.lib.pose.JfPoseInfo;
import com.jf.lib.pose.JfPoseKeyPoint;
import com.jf.lib.pose.JfPoseSkeleton;

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
            if (detectResult.isShowSource() && null != detectResult.getSourceBitmapTotal()) {
                setImageBitmap(detectResult.getSourceBitmapTotal());
            } else {
                setImageBitmap(detectResult.getBitmapTotal());
            }
        } else {
            setImageBitmap(detectResult.getBitmap());
        }

    }

    private float fx, fy;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (null != detectResult) {

            if (detectResult.isDouble()) {

                //======左侧===
                MyPoseInfo leftPose = detectResult.getLeftPoseInfo();
                fx = leftPose.getFractionWidth();
                fy = leftPose.getFractionHeight();
                JfPoseInfo poseSkeletonLeft = poseLandmarkToSkeleton("", "", leftPose.getSourceWidth(), leftPose.getSourceHeight(), null, leftPose.getPose().getAllPoseLandmarks());
                drawBody(canvas, poseSkeletonLeft.defaultSkeleton(), false);
                //======右侧===
                MyPoseInfo rightPose = detectResult.getRightPoseInfo();
                JfPoseInfo poseSkeletonRight = poseLandmarkToSkeleton("", "", rightPose.getSourceWidth(), rightPose.getSourceHeight(), null, rightPose.getPose().getAllPoseLandmarks());
                drawBody(canvas, poseSkeletonRight.defaultSkeleton(), true);

            } else if (detectResult.isSingleOk()) {

                Pose pose = detectResult.getPoseInfo().getPose();
                JfPoseInfo poseSkeleton = poseLandmarkToSkeleton("", "", detectResult.getBitmap().getWidth(), detectResult.getBitmap().getHeight(), null, pose.getAllPoseLandmarks());
                List<PoseLandmark> marks = detectResult.getPoseInfo().getPose().getAllPoseLandmarks();
                if (null != marks) {
                    fx = detectResult.getPoseInfo().getFractionWidth();
                    fy = detectResult.getPoseInfo().getFractionHeight();
                    drawBody(canvas, poseSkeleton.defaultSkeleton(), false);
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


    protected void drawBody(Canvas canvas, JfPoseSkeleton body, boolean isRight) {

        // 眼睛
        drawCircle(canvas, body.getLEye(), isRight);
        drawCircle(canvas, body.getREye(), isRight);

        // 鼻子
        drawCircle(canvas, body.getNose(), isRight);

        // 下巴
        drawCircle(canvas, body.getChin(), isRight);

        // 头部线条
        drawHead(canvas, isRight, body.getLEye(), body.getREye(), body.getNose());
//        drawLine(canvas, isRight, body.getLEye(), body.getREye(), body.getNose());

        // 鼻子肩膀的连接线
        drawLine(canvas, isRight, body.getNose(), body.getLShoulder());
        drawLine(canvas, isRight, body.getNose(), body.getRShoulder());

        // 脖子
        drawCircle(canvas, body.getNeck(), isRight);

        // 肩膀
        drawLine(canvas, isRight, body.getLShoulder(), body.getRShoulder(), body.getNeck());
        drawCircle(canvas, body.getLShoulder(), isRight);
        drawCircle(canvas, body.getRShoulder(), isRight);

        // 胳膊线条
        drawLine(canvas, isRight, body.getLShoulder(), body.getLElbow(), body.getLWrist());
        drawLine(canvas, isRight, body.getRShoulder(), body.getRElbow(), body.getRWrist());

        // 手肘
        drawCircle(canvas, body.getLElbow(), isRight);
        drawCircle(canvas, body.getRElbow(), isRight);

        // 手腕
        drawCircle(canvas, body.getLWrist(), isRight);
        drawCircle(canvas, body.getRWrist(), isRight);

        // 手指
        drawCircle(canvas, body.getLFinger(), isRight);
        drawCircle(canvas, body.getRFinger(), isRight);

        // 上下连接
        drawLine(canvas, isRight, body.getLShoulder(), body.getLHip());
        drawLine(canvas, isRight, body.getRShoulder(), body.getRHip());

        // 臀部
        drawLine(canvas, isRight, body.getLHip(), body.getRHip());
        drawCircle(canvas, body.getLHip(), isRight);
        drawCircle(canvas, body.getRHip(), isRight);
        drawCircle(canvas, body.getMidHip(), isRight);

        // 腿部线条
        drawLine(canvas, isRight, body.getLHip(), body.getLKnee(), body.getLAnkle());
        drawLine(canvas, isRight, body.getRHip(), body.getRKnee(), body.getRAnkle());

        // 膝
        drawCircle(canvas, body.getLKnee(), isRight);
        drawCircle(canvas, body.getRKnee(), isRight);

        // 脚踝
        drawCircle(canvas, body.getLAnkle(), isRight);
        drawCircle(canvas, body.getRAnkle(), isRight);

        // 脚后跟
        drawLine(canvas, isRight, body.getLAnkle(), body.getLHeel());
        drawLine(canvas, isRight, body.getRAnkle(), body.getRHeel());

        // 脚大拇指
        drawLine(canvas, isRight, body.getLAnkle(), body.getLBigToe());
        drawLine(canvas, isRight, body.getRAnkle(), body.getRBigToe());

        // 脚后跟
        drawCircle(canvas, body.getLHeel(), isRight);
        drawCircle(canvas, body.getRHeel(), isRight);

        // 大脚趾
        drawCircle(canvas, body.getLBigToe(), isRight);
        drawCircle(canvas, body.getRBigToe(), isRight);
    }

    private void drawHead(Canvas canvas, boolean isRight, JfPoseKeyPoint leye, JfPoseKeyPoint reye, JfPoseKeyPoint nose) {

        drawLine(canvas, isRight, leye, reye);
        drawLine(canvas, isRight, leye, nose);
        drawLine(canvas, isRight, reye, nose);

    }


    int deltaRight = 0;

    private int getRigtDetal(boolean isRight) {
        if (isRight) {
            if (deltaRight == 0) {
                deltaRight = getWidth() >> 1;
            }
            return deltaRight;

        } else {
            return 0;
        }
    }

    private void drawCircle(Canvas canvas, JfPoseKeyPoint site, boolean isRight) {
        if (site == null) return;

        canvas.drawCircle(site.getX() * fx + getRigtDetal(isRight), site.getY() * fy, radius, paint);
    }

    private void drawLine(Canvas canvas, boolean isRight, JfPoseKeyPoint... sites) {
        drawLine(canvas, false, isRight, sites);
    }

    private void drawLine(Canvas canvas, boolean isCycle, boolean isRight, JfPoseKeyPoint... sites) {
        if (sites == null || sites.length <= 1) return;
        JfPoseKeyPoint s;
        JfPoseKeyPoint e;
        for (int i = 1; i < sites.length; i++) {

            s = sites[i - 1];
            e = sites[i];
            if (s == null || e == null) return;
            canvas.drawLine(s.x * fx + getRigtDetal(isRight), s.y * fy, e.x * fx + getRigtDetal(isRight), e.y * fy, paint);
        }

        if (isCycle) {
            s = sites[0];
            e = sites[sites.length - 1];
            canvas.drawLine(s.x * fx + getRigtDetal(isRight), s.y * fy, e.x * fx + getRigtDetal(isRight), e.y * fy, paint);
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