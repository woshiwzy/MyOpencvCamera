package com.demo.cv42.activity

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import com.demo.cv42.App
import com.demo.cv42.R
import com.demo.cv42.view.CustomJavaCameraView
import com.demo.cv42.view.CustomJavaCameraView.OnFrameReadCallBack
import com.tzutalin.dlib.Constants
import com.tzutalin.dlib.FaceDet
import com.tzutalin.dlib.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Rect
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import java.io.File

open class CustomCameraDlibActivity : Activity() {

    private val tag = "cv42demo"
    private var javaCameraView: CustomJavaCameraView? = null
    private var isInitSuccess = false
    private var buttonSwitchCamera: Button? = null
    private var cameraViewImg: ImageView? = null
    private var faceDet: FaceDet? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_custom_camera_dlib)
        cameraViewImg = findViewById(R.id.cameraViewImg)
        (findViewById<View>(R.id.textView) as TextView).text = "Opencv版本:" + OpenCVLoader.OPENCV_VERSION
        javaCameraView = findViewById(R.id.cameraView)

        (findViewById<View>(R.id.checkboxFull) as CheckBox).setOnCheckedChangeListener { buttonView, isChecked -> javaCameraView?.setAutoFullScreen(isChecked) }
        val mConfiguration = this.resources.configuration //获取设置的配置信息
        val ori = mConfiguration.orientation //获取屏幕方向
        if (ori == Configuration.ORIENTATION_LANDSCAPE) {
            javaCameraView?.isPortrait = false
            //横屏
        } else if (ori == Configuration.ORIENTATION_PORTRAIT) {
            //竖屏
            javaCameraView?.isPortrait = true
        }

        val scalar = Scalar(200.0, 200.0, 200.0)
        var rect = Rect()
        var bmpCanny: Bitmap? = null

        javaCameraView?.onFrameReadCallBack = OnFrameReadCallBack { bitmap, srcMat ->
            runOnUiThread {

                var mat = Mat()
                Utils.bitmapToMat(bitmap, mat);
                var visiRets = faceDet?.detect(bitmap)
                visiRets?.forEach {

                    rect.x = it.left
                    rect.y = it.top
                    rect.width = it.right - it.left
                    rect.height = it.bottom - it.top
                    Imgproc.rectangle(mat, rect, scalar, 5)
                    it.faceLandmarks.forEach {
                        Imgproc.circle(mat, Point(it.x.toDouble(), it.y.toDouble()), 2, scalar, 5)
                    }
                }
                if (null == bmpCanny) {
                    bmpCanny = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.RGB_565)
                }
                Utils.matToBitmap(mat, bmpCanny)
                cameraViewImg?.setImageBitmap(bmpCanny)

            }


        }
        requestPermission()
        (findViewById<View>(R.id.textViewTitleDesc) as TextView).text = "自定义(解决，全屏，横竖屏切换，前后摄像头切换)"
        buttonSwitchCamera = findViewById(R.id.buttonSwitchCamera)
        buttonSwitchCamera?.setOnClickListener {
            javaCameraView?.swithCamera(!javaCameraView?.isUseFrontCamera!!)
        }

        initFaceDet()
    }

    fun initFaceDet() {
        GlobalScope.launch(Dispatchers.IO) {

            if (!File(Constants.getFaceShapeModelPath()).exists()) {
                FileUtils.copyFileFromRawToOthers(this@CustomCameraDlibActivity, R.raw.shape_predictor_68_face_landmarks, Constants.getFaceShapeModelPath())
            }
            faceDet = FaceDet(Constants.getFaceShapeModelPath())
        }
    }


    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val mConfiguration = this.resources.configuration //获取设置的配置信息
        val ori = mConfiguration.orientation //获取屏幕方向
        if (ori == Configuration.ORIENTATION_LANDSCAPE) {
            javaCameraView!!.isPortrait = false
            //横屏
        } else if (ori == Configuration.ORIENTATION_PORTRAIT) {
            //竖屏
            javaCameraView!!.isPortrait = true
        }
        javaCameraView!!.restartCamera()
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
            } else {
                initCamera()
            }
        } else {
            initCamera()
        }
    }

    override fun onResume() {
        super.onResume()
        if (null != javaCameraView && isInitSuccess) {
            javaCameraView!!.enableView()
        }
    }

    override fun onPause() {
        super.onPause()
        if (null != javaCameraView) {
            javaCameraView!!.disableView()
        }
    }

    private fun initCamera(): Boolean {
        Log.e(App.tag, "isinit success:$isInitSuccess")
        if (null != javaCameraView) {
            javaCameraView!!.post {
                javaCameraView!!.setCameraPermissionGranted() //需要已经授权可以使用摄像头再调用这个方法
                isInitSuccess = OpenCVLoader.initDebug()
                javaCameraView!!.setCameraPermissionGranted() //需要已经授权可以使用摄像头再调用这个方法
                javaCameraView!!.enableView()
                javaCameraView!!.enableFpsMeter()
            }
        }
        return isInitSuccess
    }

    @TargetApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE && grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initCamera()
        }
    }

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 200
    }
}