package com.demo.cv42.activity

import android.Manifest
import android.annotation.TargetApi
import android.content.Intent
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
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.demo.cv42.App
import com.demo.cv42.R
import com.demo.cv42.ml.FaceML
import com.demo.cv42.view.CustomJavaCameraView
import com.demo.cv42.view.CustomJavaCameraView.OnFrameReadCallBack
import com.tzutalin.dlib.Constants
import com.tzutalin.dlib.FaceDet
import com.tzutalin.dlib.FileUtils
import com.wangzy.face.DbController
import com.wangzy.face.FeatureUtils
import com.wangzy.face.People
import kotlinx.android.synthetic.main.activity_custom_camera_dlib2.*
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

open class CustomCameraDlibActivity2 : AppCompatActivity() {

    private var javaCameraView: CustomJavaCameraView? = null
    private var isInitSuccess = false
    private var buttonSwitchCamera: Button? = null
    private var cameraViewImgPreview: ImageView? = null
    private var faceDet: FaceDet? = null
    private lateinit var mat: Mat

    private val scalar = Scalar(0.0, 0.0, 200.0)
    private val scalarName = Scalar(200.0, 0.0, 0.0)
    private var rect = Rect()

    private lateinit var faceMl: FaceML

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 200
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_custom_camera_dlib2)
        cameraViewImgPreview = findViewById(R.id.cameraViewImgPreview)
        (findViewById<View>(R.id.textView) as TextView).text = "Opencv版本:" + OpenCVLoader.OPENCV_VERSION
        javaCameraView = findViewById(R.id.cameraView)

        val mConfiguration = this.resources.configuration //获取设置的配置信息
        val ori = mConfiguration.orientation //获取屏幕方向
        if (ori == Configuration.ORIENTATION_LANDSCAPE) {
            javaCameraView?.isPortrait = false
            //横屏
        } else if (ori == Configuration.ORIENTATION_PORTRAIT) {
            //竖屏
            javaCameraView?.isPortrait = true
        }


        var bmpCanny: Bitmap? = null

        javaCameraView?.onFrameReadCallBack = OnFrameReadCallBack { bitmap, srcMat ->
            runOnUiThread {
//                Log.e(App.tag,"image size:"+bitmap.width+","+bitmap.height)
                Utils.bitmapToMat(bitmap, mat)
                var visiRets = faceDet?.detect(bitmap)

                visiRets?.forEach {

                    rect.x = it.left
                    rect.y = it.top
                    rect.width = it.right - it.left
                    rect.height = it.bottom - it.top



                    var faceMat = Mat(mat, rect);
                    //如果是登记模式
                    if (checkBoxRecord.isChecked && !editTextName.text.isEmpty()) {
                        var featurs = FeatureUtils.comptuteFeature2(it, faceMat)
                        GlobalScope.launch(Dispatchers.Main) {
                            var name = editTextName.text.toString()
                            var peop = People()
                            peop.name = name
                            peop.feature = featurs
                            DbController.getInstance(App.app).getSession().peopleDao.insertOrReplace(peop)
                            checkBoxRecord.isChecked = false
                        }
                    } else {

                        if (faceMl.sampleSize <= 1) {
                            Log.e(App.tag, "样本数不够");
                        } else {
                            var featurs = FeatureUtils.comptuteFeature(it, faceMat)
                            if (null != faceMl) {
                                var people = faceMl.predicate2(featurs)
                                if (null != people) {
                                    Log.e(App.tag, "find people:" + people.name)
                                    Imgproc.putText(mat, people.name, rect.tl(), 1, 2.0, scalarName)
                                }
                            }
                        }


                    }

                    Imgproc.rectangle(mat, rect, scalar, 2)

                    for ((index, p) in it.faceLandmarks.withIndex()) {
                        Imgproc.putText(mat, index.toString(), Point(p.x.toDouble(), p.y.toDouble()), 1, 1.0, scalar)
                        Imgproc.circle(mat, Point(p.x.toDouble(), p.y.toDouble()), 2, scalar, 3)
                    }

                }
                if (null == bmpCanny) {
                    bmpCanny = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.RGB_565)
                }
                Utils.matToBitmap(mat, bmpCanny)
                cameraViewImgPreview?.setImageBitmap(bmpCanny)
            }

        }

        buttonSwitchCamera = findViewById(R.id.buttonSwitchCamera)
        buttonSwitchCamera?.setOnClickListener {
            javaCameraView?.swithCamera(!javaCameraView?.isUseFrontCamera!!)
        }


        buttonList.setOnClickListener {
            var intent = Intent(this, RecordListActivity::class.java)
            startActivity(intent)
        }

        buttonReloadModule.setOnClickListener {

           faceMl.reload()
        }

        requestPermission()

    }


    private fun initFaceML() {
        GlobalScope.run {
            faceMl = FaceML.getInstance()
            Log.e(App.tag, "init faceml done")
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


    private fun initCamera(): Boolean {
        Log.e(App.tag, "isinit success:$isInitSuccess")
        if (null != javaCameraView) {
            javaCameraView!!.post {
                //1.拷贝模型文件
                if (!File(Constants.getFaceShapeModelPath()).exists()) {
                    FileUtils.copyFileFromRawToOthers(this@CustomCameraDlibActivity2, R.raw.shape_predictor_68_face_landmarks, Constants.getFaceShapeModelPath())
                }
                faceDet = FaceDet(Constants.getFaceShapeModelPath())

                //2.初始化opencv
                javaCameraView!!.setCameraPermissionGranted() //需要已经授权可以使用摄像头再调用这个方法
                isInitSuccess = OpenCVLoader.initDebug()

                //3.打开相机
                mat = Mat()
                javaCameraView!!.setCameraPermissionGranted() //需要已经授权可以使用摄像头再调用这个方法
                javaCameraView!!.enableView()
                javaCameraView!!.enableFpsMeter()

                initFaceML()
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