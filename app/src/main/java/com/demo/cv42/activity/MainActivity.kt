package com.demo.cv42.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.demo.cv42.R
import com.demo.cv42.gpu.GPUActivity

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_entry)


//        val bitmap = BitmapFactory.decodeFile("/sdcard/test.png")
//        val faceDet = FaceDet(Constants.getFaceShapeModelPath())
//        val results = faceDet.detect(bitmap)
//        for (ret in results) {
//            val label = ret.label
//            val rectLeft = ret.left
//            val rectTop = ret.top
//            val rectRight = ret.right
//            val rectBottom = ret.bottom
//            val landmarks = ret.faceLandmarks
//            Log.e(App.tag,"=====land mark size:"+landmarks.size)
//        }
//        faceDet.release()
    }

    fun onClickBack(view: View?) {
        startActivity(Intent(this, CameraActivity::class.java))
    }

    fun onClickBackLand(view: View?) {
        startActivity(Intent(this, BackCameraActivity::class.java))
    }

    fun onClickSwitch(view: View?) {}
    fun onClickFrontCamera(view: View?) {
        startActivity(Intent(this, FrontCameraLandActivity::class.java))
    }

    fun onClickPortriatCamera(view: View?) {
        startActivity(Intent(this, FrontCameraPortraitActivity::class.java))
    }

    fun onClickPortriatFullCamera(view: View?) {
        startActivity(Intent(this, FrontCameraPortraitFullScreenActivity::class.java))
    }

    fun onClickCustomCamera(view: View?) {
        startActivity(Intent(this, CustomCameraActivity::class.java))
    }

    fun onClickCustomCameraDlib(view: View?) {
        startActivity(Intent(this, CustomCameraDlibActivity::class.java))
    }
    fun onClickCustomCameraDlib2(view: View?) {
        startActivity(Intent(this, CustomCameraDlibActivity2::class.java))
    }

    fun onClickCustomCameraGPU(view: View?) {
        startActivity(Intent(this, GPUActivity::class.java))
    }

    fun onClickGotoCustomCameraView(view: View?){
        startActivity(Intent(this, SimpleCusCameraActivity::class.java))
    }

    fun onClickGotoCustom2CameraView(view: View?){
        startActivity(Intent(this, Camer2StudyActivty::class.java))
    }
    fun onClickGotoCameraX(view: View?){
        startActivity(Intent(this, CameraXActivity::class.java))
    }

    fun onClickGotoCustomCameraDlibView(view: View?){
        startActivity(Intent(this, SimpleCusCameraDlibActivity::class.java))
    }
}