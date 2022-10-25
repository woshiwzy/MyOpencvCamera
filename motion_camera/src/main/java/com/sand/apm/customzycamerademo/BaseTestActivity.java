package com.sand.apm.customzycamerademo;

import android.app.Activity;
import android.media.Image;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.pose.Pose;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.sand.apm.customzycamerademo.custom.AiPoseProcessCallBack;
import com.sand.apm.customzycamerademo.custom.Camera2DataGeter;
import com.sand.apm.customzycamerademo.custom.DetectResult;

import org.opencv.core.Core;
import org.opencv.core.Mat;

import java.util.List;

/**
 * @ProjectName: MyOpenCV
 * @Date: 2022/10/24
 * @Desc:
 */
public class BaseTestActivity extends Activity {



    public void onCameraGranted(){

    }




    public void onDetectResult(DetectResult result){


    }


    /**
     * 权限请求打开相机
     */
    public void requestPermissionAndInitCamera() {
        XXPermissions.with(this)
                .permission(Permission.CAMERA)
                .permission(Permission.WRITE_EXTERNAL_STORAGE)
                .permission(Permission.READ_EXTERNAL_STORAGE)
                // 申请单个权限
//                .permission(Permission.RECORD_AUDIO)
                // 申请多个权限
                //.interceptor(new PermissionInterceptor())
                .request(new OnPermissionCallback() {
                    @Override
                    public void onGranted(List<String> permissions, boolean all) {
                        if (all) {


                            onCameraGranted();

                        } else {
//                            toast("获取部分权限成功，但部分权限未正常授予");
                        }
                    }

                    @Override
                    public void onDenied(List<String> permissions, boolean never) {
                        if (never) {
//                            toast("被永久拒绝授权，请手动授予录音和日历权限");
                            // 如果是被永久拒绝就跳转到应用权限系统设置页面
                            XXPermissions.startPermissionActivity(BaseTestActivity.this, permissions);
                        } else {
//                            toast("获取录音和日历权限失败");
                        }
                    }
                });
    }

}