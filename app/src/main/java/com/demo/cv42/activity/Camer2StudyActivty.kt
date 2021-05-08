package com.demo.cv42.activity

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.util.Log
import com.demo.cv42.App
import com.demo.cv42.R
import com.demo.cv42.custom.Camera2DataGeter
import com.demo.cv42.custom.CameraDataGeterBase
import com.demo.cv42.custom.CvCameraViewListener2Adapter
import com.face.lib.FaceML
import com.tzutalin.dlib.Constants
import com.tzutalin.dlib.FaceDet
import com.tzutalin.dlib.FileUtils
import kotlinx.android.synthetic.main.activity_camer3_study_activty.*
import kotlinx.coroutines.GlobalScope
import org.opencv.core.Mat
import java.io.File

class Camer2StudyActivty : Activity() {


    private lateinit var faceMl: FaceML
    private lateinit var mat: Mat
    private lateinit var faceDet: FaceDet
    private var javaCameraView: Camera2DataGeter? = null

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 200
    }

    private var holdBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camer3_study_activty)

        javaCameraView = Camera2DataGeter(this, CameraDataGeterBase.CAMERA_ID_FRONT, 1920, 1080)
        javaCameraView?.configOrientation(this.resources.configuration)

        javaCameraView!!.setCvCameraViewListener(object : CvCameraViewListener2Adapter() {
            override fun onCameraViewStarted(width: Int, height: Int) {}
            override fun onCameraViewStopped() {}
            override fun onCameraFrame(rgbaMat: Mat): Mat? {
                Log.e(App.tag, "得到预览数据：" + rgbaMat.width() + "," + rgbaMat.height())

//                if (null == holdBitmap || holdBitmap?.width != rgbaMat.cols() || holdBitmap?.height != rgbaMat.rows()) {
//                    if (null != holdBitmap && !holdBitmap?.isRecycled!!) {
//                        holdBitmap?.recycle()
//                    }
//                    holdBitmap = Bitmap.createBitmap(rgbaMat.cols(), rgbaMat.rows(), Bitmap.Config.RGB_565)
//                }

//                Utils.matToBitmap(rgbaMat, holdBitmap)
//                GlobalScope.launch(Dispatchers.Main) {
//                    imageViewPreview.setImageBitmap(holdBitmap)
//                }


                return null
            }
        })


        buttonSwitchCamera.setOnClickListener {
            javaCameraView?.toogleCamera()
        }

        requestPermission()
    }

    private fun initCamera() {

        if (null != javaCameraView) {

            //1.拷贝模型文件
            if (!File(Constants.getFaceShapeModelPath()).exists()) {
                FileUtils.copyFileFromRawToOthers(this@Camer2StudyActivty, R.raw.shape_predictor_68_face_landmarks, Constants.getFaceShapeModelPath())
            }
            faceDet = FaceDet(Constants.getFaceShapeModelPath())


            //3.打开相机
            mat = Mat()
            javaCameraView!!.enableView()

            initFaceML()
        }
    }


    private fun initFaceML() {
        GlobalScope.run {
            faceMl = FaceML.getInstance(App.app)
            Log.e(App.tag, "init faceml done")
        }
    }


    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        javaCameraView?.configOrientation(this.resources.configuration)
        javaCameraView!!.restartCamera()
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE), CAMERA_PERMISSION_REQUEST_CODE)
            } else {
                initCamera()
            }
        } else {
            initCamera()
        }
    }


}