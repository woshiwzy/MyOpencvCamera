package com.sand.apm.customzycamerademo.pose;

import android.graphics.RectF;

public class JfPoseBoxing extends RectF {
    private String player;
    private String key;
    private int imageWidth;
    private int imageHeight;
    private String from;

    public JfPoseBoxing() {
    }

    public String getPlayer() {
        return this.player;
    }

    public void setPlayer(String player) {
        this.player = player;
    }

    public String getKey() {
        return this.key;
    }

    public void setKey(String key) {
        this.key = key;
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

    public void xCenterMoveTo(int cx, int maxWidth) {
        if (cx <= 0) {
            cx = 0;
        }

        int w = (int)this.width();
        int dx = (int)((float)cx - this.centerX());
        int left = (int)this.left;
        left += dx;
        if (left < 0) {
            left = 0;
        }

        if (left > maxWidth - w) {
            left = maxWidth - w;
        }

        if (left % 2 != 0) {
            ++left;
        }

        this.set((float)left, this.top, (float)(left + w), this.bottom);
    }

    public void xCenterAlign(JfPoseBoxing target, int maxWidth) {
        if (target != null) {
            int dx = (int)(target.centerX() - this.centerX());
            int w = (int)this.width();
            int left = (int)this.left;
            left += dx;
            if (left < 0) {
                left = 0;
            }

            if (left > maxWidth - w) {
                left = maxWidth - w;
            }

            if (left % 2 != 0) {
                ++left;
            }

            this.set((float)left, this.top, (float)(left + w), this.bottom);
        }
    }

    public static JfPoseBoxing getCropPoseBoxing(JfPoseBoxing src, int nv21Width, int nv21Height) {
        return getCropPoseBoxing(src, 0.3F, 0.45F, nv21Width, nv21Height);
    }

    public static JfPoseBoxing getCropPoseBoxing(JfPoseBoxing src, float hScale, float vScale, int nv21Width, int nv21Height) {
        if (src == null) {
            return null;
        } else {
            JfPoseBoxing out = new JfPoseBoxing();
            out.setKey(src.key);
            out.setPlayer(src.player);
            out.setImageWidth(src.imageWidth);
            out.setImageHeight(src.imageHeight);
            int boxWidth = (int)src.width();
            int boxHeight = (int)src.height();
            float cx = src.centerX();
            float cy = src.centerY();
            float paddingH = (float)boxWidth * hScale;
            float paddingV = (float)boxWidth * vScale;
            int targetWidth = (int)((float)boxWidth + paddingH * 2.0F);
            int targetHeight = (int)((float)boxHeight + paddingV * 2.0F);
            int cropX = (int)Math.max(cx - (float)(targetWidth / 2), 0.0F);
            int cropY = (int)Math.max(cy - (float)(targetHeight / 2) - paddingH * 0.2F, 0.0F);
            int cropWidth = Math.min(targetWidth, nv21Width - cropX);
            int cropHeight = Math.min(targetHeight, nv21Height - cropY);
            if (cropX % 2 != 0) {
                --cropX;
            }

            if (cropY % 2 != 0) {
                --cropY;
            }

            out.set((float)cropX, (float)cropY, (float)(cropX + cropWidth), (float)(cropY + cropHeight));
            return out;
        }
    }
}
