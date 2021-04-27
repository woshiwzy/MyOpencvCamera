package com.demo.cv42.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.widget.SeekBar
import android.widget.Toast
import com.demo.cv42.App
import com.demo.cv42.R
import com.demo.cv42.custom.CameraDataGeter
import com.demo.cv42.custom.CameraDataGeterBase
import com.demo.cv42.custom.CvCameraViewListener2Adapter
import com.demo.cv42.face.FaceHogTool
import com.demo.cv42.face.FaceML
import com.demo.cv42.face.MyMl
import com.demo.cv42.face.VectorTool
import com.demo.cv42.utils.recordPerson
import com.tzutalin.dlib.Constants
import com.tzutalin.dlib.FaceDet
import com.tzutalin.dlib.FileUtils
import com.tzutalin.dlib.VisionDetRet
import kotlinx.android.synthetic.main.activity_simple_cus_camera_dlib.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Rect
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import java.io.File

/**
 * 在自定义相机的基础上进行人脸识别
 */
class SimpleCusCameraDlibActivity : Activity() {


    private var faceMl: FaceML? = null
    private lateinit var faceDet: FaceDet
    private var javaCameraView: CameraDataGeter? = null
    private var lastFrameTime: Long = 0
    private var frameCount = 0
    private var globalBitmap: Bitmap? = null

    private var rect = Rect()
    private var rectCenterFace = Rect()
    private var rectHog = Rect()

    private var throld = 0.9f//人脸识别阀值
    private var recordCout = 0

    private val scalar = Scalar(240.0, 88.0, 200.0)
    private val scalarHog = Scalar(255.0, 100.0, 200.0)
    private val scalarName = Scalar(200.0, 55.0, 55.0)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_simple_cus_camera_dlib)

        buttonSwitchCamera?.setOnClickListener { javaCameraView!!.toogleCamera() }

        javaCameraView = CameraDataGeter(this, CameraDataGeterBase.CAMERA_ID_FRONT, 640, 480)
        javaCameraView?.setCvCameraViewListener(object : CvCameraViewListener2Adapter() {

            override fun onCameraFrame(rgbaMat: Mat): Mat {
                Log.e(App.tag, "获得Mat数据===>>:" + rgbaMat.width() + " X " + rgbaMat.height() + " thread:" + Thread.currentThread().name)
                if (checkBoxGray.isChecked) {
                    Imgproc.cvtColor(rgbaMat, rgbaMat, Imgproc.COLOR_BGR2GRAY)
                }

//                if (null == globalBitmap || globalBitmap?.width != rgbaMat.width() || globalBitmap?.height != rgbaMat.height()) {
//                    globalBitmap = Bitmap.createBitmap(rgbaMat.width(), rgbaMat.height(), Bitmap.Config.RGB_565)
//                }


                globalBitmap = Bitmap.createBitmap(rgbaMat.width(), rgbaMat.height(), Bitmap.Config.RGB_565)
                Utils.matToBitmap(rgbaMat, globalBitmap)
                //得到最终的mat============================================================
                var visiRets = faceDet?.detect(globalBitmap)

                visiRets?.forEach {
                    rect.x = it.left
                    rect.y = it.top
                    rect.width = it.right - it.left
                    rect.height = it.bottom - it.top

                    try {
                        setFaceHogRect(it)
                        var faceMat = Mat(rgbaMat, rectHog)//rect 必须小于mat大小，否则会崩溃
                        var hogFaceFeatures = FaceHogTool.compte(faceMat)

                        var sbf = StringBuffer();
                        hogFaceFeatures.forEach {
                            sbf.append("$it,")
                        }

                        var hogFeatureString = sbf.toString().substring(0, sbf.length - 1);

                        //如果是登记模式
                        if ((checkBoxRecord.isChecked && editTextName.text.isNotEmpty()) || (checkBoxRepeat.isChecked && editTextName.text.isNotEmpty())) {
                            var name = editTextName.text.toString()
                            if (checkBoxRecord.isChecked) {//单次登记
                                recordPerson(name, hogFeatureString)

                                GlobalScope.launch(Dispatchers.Main) {
                                    checkBoxRecord.isChecked = false
                                    Toast.makeText(App.app, "登记成功", Toast.LENGTH_SHORT).show();
                                }
                            } else {//连续登记

                                var rc = checkBoxRepeat.tag.toString().toInt()
                                recordPerson(name, hogFeatureString)
                                recordCout++
                                if (recordCout == rc) {
                                    GlobalScope.launch(Dispatchers.Main) {
                                        Toast.makeText(App.app, "登记成功", Toast.LENGTH_SHORT).show();
                                        checkBoxRepeat.isChecked = false
                                    }
                                }
                            }

                        } else {
                            if (faceMl?.sampleSize!! <= 1) {
                                Log.e(App.tag, "样本数不够，请勾选登记")
                            } else {
                                var featurs = hogFaceFeatures//只用hog特征
                                if (radioButtonCv.isChecked) {
                                    var people = faceMl?.predicate2(featurs)
                                    if (null != people) {
                                        var percentDistance = VectorTool.computeSimilarity2(featurs, people.vector);
                                        if (percentDistance > throld) {
                                            Log.e(App.tag, "find people:" + people.name + "," + percentDistance);
                                            Imgproc.putText(rgbaMat, people.name + "_" + percentDistance, rect.tl(), 1, 2.0, scalarName)
                                        }
                                    }
                                } else {
                                    var ret = MyMl.getInstance(App.app).findNears(1, featurs)[1]!!
                                    if (ret.distance > throld) {
                                        Log.e(App.tag, "find people:" + ret.people.name + "," + ret.distance);
                                        Imgproc.putText(rgbaMat, ret.people.name + "_" + ret.distance, rect.tl(), 1, 2.0, scalarName)
                                    }
                                }

                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Log.e(App.tag, "出现异常==>：" + e.localizedMessage)
                    }

                    Imgproc.rectangle(rgbaMat, rect, scalar, 2)

                    var showPoint30 = false
                    if (showPoint30) {

                        var p = it.faceLandmarks[30]
                        Imgproc.circle(rgbaMat, Point(p.x.toDouble(), p.y.toDouble()), 2, scalar, 3)

                        var tw = rect.width / 2
                        var th = rect.height / 2

                        rectCenterFace.x = p.x - tw / 2
                        rectCenterFace.y = p.y - th / 2

                        rectCenterFace.width = tw
                        rectCenterFace.height = th

                        Imgproc.rectangle(rgbaMat, rectCenterFace, scalar, 3)
                    }

                    var showAllPoint = false
                    if (showAllPoint) {
                        for ((index, p) in it.faceLandmarks.withIndex()) {
                            Imgproc.putText(rgbaMat, index.toString(), Point(p.x.toDouble(), p.y.toDouble()), 1, 1.0, scalar)
                            Imgproc.circle(rgbaMat, Point(p.x.toDouble(), p.y.toDouble()), 2, scalar, 3)
                        }
                    }
                    Imgproc.rectangle(rgbaMat, rectHog, scalarHog, 2)
                }

                globalBitmap = Bitmap.createBitmap(rgbaMat.width(), rgbaMat.height(), Bitmap.Config.RGB_565)
                Utils.matToBitmap(rgbaMat, globalBitmap)
                onBitmapGet(globalBitmap!!)
                return rgbaMat
            }
        })

        button480.setOnClickListener { javaCameraView?.setResolution(640, 480) }
        button1920.setOnClickListener { javaCameraView?.setResolution(1920, 1080) }
        buttonReloadModule.setOnClickListener {
            faceMl?.reload()
        }

        buttonList.setOnClickListener {
            var intent = Intent(this, RecordListActivity::class.java)
            startActivity(intent)
        }


        checkBoxRepeat.setOnCheckedChangeListener { buttonView, isChecked -> recordCout = 0 }

        seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                throld = (progress * 1.0 / seekBar!!.max).toFloat();
                textViewDesc.text = "阀值:" + throld
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        requestPermission()
    }

    private fun onBitmapGet(bitmap: Bitmap) {

        runOnUiThread {
            frameCount++
            if (frameCount == 10) {
                val delta = (System.currentTimeMillis() - lastFrameTime).toDouble()
                textViewTips!!.text = "fps:" + (1000f / (delta / frameCount)).toInt() + " 图片大小:" + bitmap.width + " X " + bitmap.height
                frameCount = 0
                lastFrameTime = System.currentTimeMillis()
            }

            imageViewPreview.setImageBitmap(bitmap)
        }
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE), SimpleCusCameraDlibActivity.CAMERA_PERMISSION_REQUEST_CODE)
            } else {
                initCamera()
            }
        } else {
            initCamera()
        }
    }


    override fun onResume() {
        super.onResume()
        if (null != javaCameraView) {
            javaCameraView!!.enableView()
        }
    }

    override fun onPause() {
        super.onPause()
        if (null != javaCameraView) {
            javaCameraView!!.disableView()
        }
    }

    private fun initCamera() {
        initFaceML()
        if (null != javaCameraView) {
            javaCameraView!!.enableView()
        }
    }

    private fun initFaceML() {
        GlobalScope.run {
            //1.拷贝模型文件
            if (!File(Constants.getFaceShapeModelPath()).exists()) {
                FileUtils.copyFileFromRawToOthers(this@SimpleCusCameraDlibActivity, R.raw.shape_predictor_68_face_landmarks, Constants.getFaceShapeModelPath())
            }
            faceDet = FaceDet(Constants.getFaceShapeModelPath())
            //2.初始化faceml
            faceMl = FaceML.getInstance()
            Log.e(App.tag, "init faceml done")
        }
    }


    fun getSqureRect(w: Int, h: Int): Rect {

        val min = Math.min(w, h)
        val rect = Rect()
        rect.x = w / 2 - min / 2
        rect.y = h / 2 - min / 2
        rect.width = min
        rect.height = min
        return rect
    }

    /**
     * 得到面部从眉毛到下巴的主要图片
     */
    fun setFaceHogRect(it: VisionDetRet) {

        var lx = it.faceLandmarks.get(0).x
        var ly = it.faceLandmarks.get(19).y

        var rx = it.faceLandmarks.get(15).x
        var ry = it.faceLandmarks.get(8).y

        var hogWidth = rx - lx
        var hogHeight = ry - ly

        rectHog.x = lx
        rectHog.y = ly

        rectHog.width = hogWidth
        rectHog.height = hogHeight

    }


    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        javaCameraView!!.onConfigurationChanged(newConfig)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            initCamera()
        }
    }

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 200
    }
}