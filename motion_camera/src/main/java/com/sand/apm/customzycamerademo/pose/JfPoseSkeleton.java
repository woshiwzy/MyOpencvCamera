package com.sand.apm.customzycamerademo.pose;

import android.graphics.RectF;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class JfPoseSkeleton {
    private String player;
    private String key;
    private int imageWidth;
    private int imageHeight;
    private String from;
    private RectF rectF;
    public JfPoseKeyPoint REye;
    public JfPoseKeyPoint LEye;
    public JfPoseKeyPoint Nose;
    public JfPoseKeyPoint REar;
    public JfPoseKeyPoint LEar;
    public JfPoseKeyPoint Chin;
    public JfPoseKeyPoint Neck;
    public JfPoseKeyPoint RShoulder;
    public JfPoseKeyPoint LShoulder;
    public JfPoseKeyPoint RElbow;
    public JfPoseKeyPoint LElbow;
    public JfPoseKeyPoint LWrist;
    public JfPoseKeyPoint RWrist;
    public JfPoseKeyPoint LFinger;
    public JfPoseKeyPoint RFinger;
    public JfPoseKeyPoint RHip;
    public JfPoseKeyPoint MidHip;
    public JfPoseKeyPoint LHip;
    public JfPoseKeyPoint RKnee;
    public JfPoseKeyPoint LKnee;
    public JfPoseKeyPoint RAnkle;
    public JfPoseKeyPoint LAnkle;
    public JfPoseKeyPoint LHeel;
    public JfPoseKeyPoint RHeel;
    public JfPoseKeyPoint LBigToe;
    public JfPoseKeyPoint RBigToe;
    public JfPoseKeyPoint LSmallToe;
    public JfPoseKeyPoint RSmallToe;

    public JfPoseSkeleton() {
    }

    public RectF getRectF() {
        return this.rectF;
    }

    public void setRectF(RectF rectF) {
        this.rectF = rectF;
    }

    public JfPoseKeyPoint getREye() {
        return this.REye;
    }

    public void setREye(JfPoseKeyPoint REye) {
        this.REye = REye;
    }

    public JfPoseKeyPoint getLEye() {
        return this.LEye;
    }

    public void setLEye(JfPoseKeyPoint LEye) {
        this.LEye = LEye;
    }

    public JfPoseKeyPoint getNose() {
        return this.Nose;
    }

    public void setNose(JfPoseKeyPoint nose) {
        this.Nose = nose;
    }

    public JfPoseKeyPoint getREar() {
        return this.REar;
    }

    public void setREar(JfPoseKeyPoint REar) {
        this.REar = REar;
    }

    public JfPoseKeyPoint getLEar() {
        return this.LEar;
    }

    public void setLEar(JfPoseKeyPoint LEar) {
        this.LEar = LEar;
    }

    public JfPoseKeyPoint getChin() {
        return this.Chin;
    }

    public void setChin(JfPoseKeyPoint chin) {
        this.Chin = chin;
    }

    public JfPoseKeyPoint getNeck() {
        return this.Neck;
    }

    public void setNeck(JfPoseKeyPoint neck) {
        this.Neck = neck;
    }

    public JfPoseKeyPoint getRShoulder() {
        return this.RShoulder;
    }

    public void setRShoulder(JfPoseKeyPoint RShoulder) {
        this.RShoulder = RShoulder;
    }

    public JfPoseKeyPoint getLShoulder() {
        return this.LShoulder;
    }

    public void setLShoulder(JfPoseKeyPoint LShoulder) {
        this.LShoulder = LShoulder;
    }

    public JfPoseKeyPoint getRElbow() {
        return this.RElbow;
    }

    public void setRElbow(JfPoseKeyPoint RElbow) {
        this.RElbow = RElbow;
    }

    public JfPoseKeyPoint getLElbow() {
        return this.LElbow;
    }

    public void setLElbow(JfPoseKeyPoint LElbow) {
        this.LElbow = LElbow;
    }

    public JfPoseKeyPoint getLWrist() {
        return this.LWrist;
    }

    public void setLWrist(JfPoseKeyPoint LWrist) {
        this.LWrist = LWrist;
    }

    public JfPoseKeyPoint getRWrist() {
        return this.RWrist;
    }

    public void setRWrist(JfPoseKeyPoint RWrist) {
        this.RWrist = RWrist;
    }

    public JfPoseKeyPoint getLFinger() {
        return this.LFinger;
    }

    public void setLFinger(JfPoseKeyPoint LFinger) {
        this.LFinger = LFinger;
    }

    public JfPoseKeyPoint getRFinger() {
        return this.RFinger;
    }

    public void setRFinger(JfPoseKeyPoint RFinger) {
        this.RFinger = RFinger;
    }

    public JfPoseKeyPoint getRHip() {
        return this.RHip;
    }

    public void setRHip(JfPoseKeyPoint RHip) {
        this.RHip = RHip;
    }

    public JfPoseKeyPoint getMidHip() {
        return this.MidHip;
    }

    public void setMidHip(JfPoseKeyPoint midHip) {
        this.MidHip = midHip;
    }

    public JfPoseKeyPoint getLHip() {
        return this.LHip;
    }

    public void setLHip(JfPoseKeyPoint LHip) {
        this.LHip = LHip;
    }

    public JfPoseKeyPoint getRKnee() {
        return this.RKnee;
    }

    public void setRKnee(JfPoseKeyPoint RKnee) {
        this.RKnee = RKnee;
    }

    public JfPoseKeyPoint getLKnee() {
        return this.LKnee;
    }

    public void setLKnee(JfPoseKeyPoint LKnee) {
        this.LKnee = LKnee;
    }

    public JfPoseKeyPoint getRAnkle() {
        return this.RAnkle;
    }

    public void setRAnkle(JfPoseKeyPoint RAnkle) {
        this.RAnkle = RAnkle;
    }

    public JfPoseKeyPoint getLAnkle() {
        return this.LAnkle;
    }

    public void setLAnkle(JfPoseKeyPoint LAnkle) {
        this.LAnkle = LAnkle;
    }

    public JfPoseKeyPoint getLHeel() {
        return this.LHeel;
    }

    public void setLHeel(JfPoseKeyPoint LHeel) {
        this.LHeel = LHeel;
    }

    public JfPoseKeyPoint getRHeel() {
        return this.RHeel;
    }

    public void setRHeel(JfPoseKeyPoint RHeel) {
        this.RHeel = RHeel;
    }

    public JfPoseKeyPoint getLBigToe() {
        return this.LBigToe;
    }

    public void setLBigToe(JfPoseKeyPoint LBigToe) {
        this.LBigToe = LBigToe;
    }

    public JfPoseKeyPoint getRBigToe() {
        return this.RBigToe;
    }

    public void setRBigToe(JfPoseKeyPoint RBigToe) {
        this.RBigToe = RBigToe;
    }

    public JfPoseKeyPoint getLSmallToe() {
        return this.LSmallToe;
    }

    public void setLSmallToe(JfPoseKeyPoint LSmallToe) {
        this.LSmallToe = LSmallToe;
    }

    public JfPoseKeyPoint getRSmallToe() {
        return this.RSmallToe;
    }

    public void setRSmallToe(JfPoseKeyPoint RSmallToe) {
        this.RSmallToe = RSmallToe;
    }

    public String getKey() {
        return this.key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getPlayer() {
        return this.player;
    }

    public void setPlayer(String player) {
        this.player = player;
    }

    public int getImageWidth() {
        return this.imageWidth;
    }

    public void setImageWidth(int imageWidth) {
        this.imageWidth = imageWidth;
    }

    public int getImageHeight() {
        return this.imageHeight;
    }

    public void setImageHeight(int imageHeight) {
        this.imageHeight = imageHeight;
    }

    public String getFrom() {
        return this.from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public static String toStr(JfPoseKeyPoint p) {
        return p != null ? "x=" + p.x + ",y=" + p.y : "";
    }

    public float footCenterY() {
        if (this.LAnkle != null && this.RAnkle != null) {
            return (this.LAnkle.y + this.RAnkle.y) / 2.0F;
        } else if (this.LAnkle != null) {
            return this.LAnkle.y;
        } else {
            return this.RAnkle != null ? this.RAnkle.y : 0.0F;
        }
    }

    public float footCenterX() {
        if (this.LAnkle != null && this.RAnkle != null) {
            return (this.LAnkle.x + this.RAnkle.x) / 2.0F;
        } else if (this.LAnkle != null) {
            return this.LAnkle.x;
        } else {
            return this.RAnkle != null ? this.RAnkle.x : 0.0F;
        }
    }

    public final JfPoseSkeleton copyNew() {
        JfPoseSkeleton sk = new JfPoseSkeleton();
        sk.setPlayer(this.player);
        sk.setKey(this.key);
        sk.setImageWidth(this.imageWidth);
        sk.setImageHeight(this.imageHeight);
        sk.setFrom(this.from);
        sk.setLEye(this.LEye);
        sk.setREye(this.REye);
        sk.setNose(this.Nose);
        sk.setLEar(this.LEar);
        sk.setREar(this.REar);
        sk.setChin(this.Chin);
        sk.setNeck(this.Neck);
        sk.setLShoulder(this.LShoulder);
        sk.setRShoulder(this.RShoulder);
        sk.setLElbow(this.LElbow);
        sk.setRElbow(this.RElbow);
        sk.setLWrist(this.LWrist);
        sk.setRWrist(this.RWrist);
        sk.setLFinger(this.LFinger);
        sk.setRFinger(this.RFinger);
        sk.setLHip(this.LHip);
        sk.setRHip(this.RHip);
        sk.setLKnee(this.LKnee);
        sk.setRKnee(this.RKnee);
        sk.setLAnkle(this.LAnkle);
        sk.setRAnkle(this.RAnkle);
        sk.setLHeel(this.LHeel);
        sk.setRHeel(this.RHeel);
        sk.setLBigToe(this.LBigToe);
        sk.setRBigToe(this.RBigToe);
        sk.setLSmallToe(this.LSmallToe);
        sk.setRSmallToe(this.RSmallToe);
        return sk;
    }

    public final JfPoseBoxing getPoseBoxing() {
        JfPoseBoxing tracker = new JfPoseBoxing();
        tracker.setKey(this.key);
        tracker.setImageWidth(this.imageWidth);
        tracker.setImageHeight(this.imageHeight);
        tracker.setPlayer(this.player);
        tracker.setFrom(this.from);
        List<JfPoseKeyPoint> all = new ArrayList();
        this.addPoint(this.LEar, all);
        this.addPoint(this.REar, all);
        this.addPoint(this.LEye, all);
        this.addPoint(this.REye, all);
        this.addPoint(this.Nose, all);
        this.addPoint(this.Chin, all);
        this.addPoint(this.Neck, all);
        this.addPoint(this.LShoulder, all);
        this.addPoint(this.RShoulder, all);
        this.addPoint(this.LElbow, all);
        this.addPoint(this.RElbow, all);
        this.addPoint(this.LWrist, all);
        this.addPoint(this.RWrist, all);
        this.addPoint(this.LFinger, all);
        this.addPoint(this.RFinger, all);
        this.addPoint(this.LHip, all);
        this.addPoint(this.RHip, all);
        this.addPoint(this.LKnee, all);
        this.addPoint(this.RKnee, all);
        this.addPoint(this.LAnkle, all);
        this.addPoint(this.RAnkle, all);
        this.addPoint(this.LHeel, all);
        this.addPoint(this.RHeel, all);
        this.addPoint(this.LBigToe, all);
        this.addPoint(this.RBigToe, all);
        this.addPoint(this.LSmallToe, all);
        this.addPoint(this.RSmallToe, all);
        float minX = 3.4028235E38F;
        float minY = 3.4028235E38F;
        float maxX = 0.0F;
        float maxY = 0.0F;

        JfPoseKeyPoint point;
        for(Iterator var7 = all.iterator(); var7.hasNext(); maxY = Math.max(point.y, maxY)) {
            point = (JfPoseKeyPoint)var7.next();
            minX = Math.min(point.x, minX);
            minY = Math.min(point.y, minY);
            maxX = Math.max(point.x, maxX);
        }

        float w = Math.abs(maxX - minX);
        float h = Math.abs(maxY - minY);
        tracker.set(minX, minY, minX + w, minY + h);
        return tracker;
    }

    private void addPoint(JfPoseKeyPoint point, List<JfPoseKeyPoint> all) {
        if (point != null) {
            all.add(point);
        }

    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("from: " + this.from);
        builder.append("左眼: (" + toStr(this.LEye) + "),");
        builder.append("右眼: (" + toStr(this.REye) + "),");
        builder.append("鼻子: (" + toStr(this.Nose) + "),");
        builder.append("下巴: (" + toStr(this.Chin) + "),");
        builder.append("左肩: (" + toStr(this.LShoulder) + "),");
        builder.append("右肩: (" + toStr(this.RShoulder) + "),");
        return builder.toString();
    }
}