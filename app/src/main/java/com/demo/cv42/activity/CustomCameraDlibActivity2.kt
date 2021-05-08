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
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.demo.cv42.App
import com.demo.cv42.R
import com.demo.cv42.utils.recordPerson
import com.demo.cv42.view.CustomJavaCameraView
import com.demo.cv42.view.CustomJavaCameraView.OnFrameReadCallBack
import com.face.lib.FaceHogTool
import com.face.lib.FaceML
import com.face.lib.MyMl
import com.tzutalin.dlib.Constants
import com.tzutalin.dlib.FaceDet
import com.tzutalin.dlib.FileUtils
import kotlinx.android.synthetic.main.activity_custom_camera_dlib2.*
import kotlinx.coroutines.GlobalScope
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Rect
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import java.io.File

/**
 * 人脸识别和注册
 */
open class CustomCameraDlibActivity2 : AppCompatActivity() {

    private var javaCameraView: CustomJavaCameraView? = null
    private var isInitSuccess = false
    private var buttonSwitchCamera: Button? = null
    private var cameraViewImgPreview: ImageView? = null
    private var faceDet: FaceDet? = null
    private lateinit var mat: Mat

    private val scalar = Scalar(0.0, 0.0, 200.0)
    private val scalarHog = Scalar(255.0, 0.0, 200.0)
    private val scalarName = Scalar(200.0, 0.0, 0.0)
    private var rect = Rect()
    private var rectCenterFace = Rect()
    private var rectHog = Rect()

    private lateinit var faceMl: FaceML

    private var throld = 0.9f//人脸识别阀值
    private var recordCout = 0

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 200
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_custom_camera_dlib2)
        cameraViewImgPreview = findViewById(R.id.cameraViewImgPreview)

        CustomJavaCameraView.setDefaultPreviewSize(640, 480);

        javaCameraView = findViewById(R.id.cameraView)
        javaCameraView?.isDrawUseDefaultMethod = false
        javaCameraView?.isUseFrontCamera = true
        javaCameraView?.isUseGray = checkBoxGray.isChecked

        checkBoxGray.setOnCheckedChangeListener { buttonView, isChecked -> javaCameraView?.isUseGray = isChecked }

        val mConfiguration = this.resources.configuration //获取设置的配置信息
        val ori = mConfiguration.orientation //获取屏幕方向

        javaCameraView?.isPortrait = Configuration.ORIENTATION_LANDSCAPE != ori

        var bmpCanny: Bitmap? = null
        javaCameraView?.onFrameReadCallBack = OnFrameReadCallBack { bitmap, srcMat ->

            runOnUiThread {

                textViewPicSize.text = "图片大小:" + bitmap.width + "," + bitmap.height

                Utils.bitmapToMat(bitmap, mat)
                var visiRets = faceDet?.detect(bitmap)

                visiRets?.forEach {

                    rect.x = it.left
                    rect.y = it.top
                    rect.width = it.right - it.left
                    rect.height = it.bottom - it.top

                    try {

                        var lx = it.faceLandmarks.get(0).x
                        var ly = it.faceLandmarks.get(19).y

                        var rx = it.faceLandmarks.get(15).x
//                        var ry = it.faceLandmarks.get(8).y
                        var ry = it.faceLandmarks.get(57).y

                        var hogWidth = rx - lx
                        var hogHeight = ry - ly


                        rectHog.x = lx
                        rectHog.y = ly

                        rectHog.width = hogWidth
                        rectHog.height = hogHeight


                        var faceMat = Mat(mat, rectHog)//rect 必须小于mat大小，否则会崩溃
                        var hogFaceFeatures = FaceHogTool.compte(faceMat)

                        var sbf = StringBuffer();
                        hogFaceFeatures.forEach {
                            sbf.append("$it,")
                        }
                        var hogFeatureString = sbf.toString().substring(0, sbf.length - 1);

                        //如果是登记模式
                        if ((checkBoxRecord.isChecked && !editTextName.text.isEmpty()) || (checkBoxRepeat.isChecked && !editTextName.text.isEmpty())) {

                            var name = editTextName.text.toString()
                            if (checkBoxRecord.isChecked) {//单次登记
//                            var featurs = FeatureUtils.comptuteFeature2(it, faceMat)//利用
                                recordPerson(name, hogFeatureString)
                                checkBoxRecord.isChecked = false
                                Toast.makeText(App.app, "登记成功", Toast.LENGTH_SHORT).show();
                            } else {//连续登记

                                var rc = checkBoxRepeat.tag.toString().toInt()
                                recordPerson(name, hogFeatureString)
                                recordCout++
                                if (recordCout == rc) {
                                    Toast.makeText(App.app, "登记成功", Toast.LENGTH_SHORT).show();
                                    checkBoxRepeat.isChecked = false
                                }
                            }

                        } else {
                            if (faceMl.sampleSize <= 1) {
                                Log.e(App.tag, "样本数不够，请勾选登记")
                            } else {
                                var featurs = hogFaceFeatures//只用hog特征
                                if (radioButtonCv.isChecked) {
                                    var peopleResult = faceMl.predicate2(featurs)
                                    if (null != peopleResult) {
                                        if (peopleResult.percent > throld) {
//                                            Log.e(App.tag, "find people:" + people.name + "," + percentDistance);
                                            Imgproc.putText(mat, peopleResult.people.name + "_" + peopleResult.percent, rect.tl(), 1, 2.0, scalarName)
                                        }
                                    }
                                } else {
                                    var ret = MyMl.getInstance(App.app).findNears(1, featurs)[1]!!
                                    if (ret.percent > throld) {
                                        //                                    Log.e(App.tag, "find people:" + ret.people.name + "," + ret.distance);
                                        Imgproc.putText(mat, ret.people.name + "_" + ret.percent, rect.tl(), 1, 2.0, scalarName)
                                    }

                                }

                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Log.e(App.tag, "可能是rect 超出边界：" + e.localizedMessage)
                    }

                    Imgproc.rectangle(mat, rect, scalar, 2)

                    var showPoint30 = false
                    if (showPoint30) {

                        var p = it.faceLandmarks[30]
                        Imgproc.circle(mat, Point(p.x.toDouble(), p.y.toDouble()), 2, scalar, 3)

                        var tw = rect.width / 2
                        var th = rect.height / 2

                        rectCenterFace.x = p.x - tw / 2
                        rectCenterFace.y = p.y - th / 2

                        rectCenterFace.width = tw
                        rectCenterFace.height = th

                        Imgproc.rectangle(mat, rectCenterFace, scalar, 3)
                    }

                    var showAllPoint = false
                    if (showAllPoint) {
                        for ((index, p) in it.faceLandmarks.withIndex()) {
                            Imgproc.putText(mat, index.toString(), Point(p.x.toDouble(), p.y.toDouble()), 1, 1.0, scalar)
                            Imgproc.circle(mat, Point(p.x.toDouble(), p.y.toDouble()), 2, scalar, 3)
                        }
                    }
                    Imgproc.rectangle(mat, rectHog, scalarHog, 2)
                }
                if (null == bmpCanny || bmpCanny?.width != mat.cols() || bmpCanny?.height != mat.height()) {
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
            faceMl.reload(App.app)
        }

        button240.setOnClickListener {
            javaCameraView?.setResolution2(320, 240)
        }

        button480.setOnClickListener {
            javaCameraView?.setResolution2(640, 480)
        }

        button1080.setOnClickListener {
            javaCameraView?.setResolution2(1920, 1080)
        }

        checkBoxMax.setOnCheckedChangeListener { buttonView, isChecked ->

            javaCameraView?.isUseMaxPreview = isChecked
            javaCameraView?.restartCamera()

        }

        checkBoxRepeat.setOnCheckedChangeListener { buttonView, isChecked -> recordCout = 0 }

        seekbarThreshold.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                throld = ((progress * 1.0 / seekbarThreshold.max).toFloat());
                textViewThreshold.text = "阀值：$throld"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        requestPermission()
    }


    private fun initFaceML() {
        GlobalScope.run {
            faceMl = FaceML.getInstance(App.app)
            Log.e(App.tag, "init faceml done")
        }
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

                checkBoxMax.text = "最大分辨率:" + javaCameraView?.maxPervieSize.toString()

                initFaceML()
            }
        }
        return isInitSuccess
    }


    @TargetApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
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

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val mConfiguration = this.resources.configuration //获取设置的配置信息
        val ori = mConfiguration.orientation //获取屏幕方向
        javaCameraView!!.isPortrait = ori == Configuration.ORIENTATION_PORTRAIT
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