package com.limelight.activity


import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.limelight.Theme
import com.limelight.common.AnalyticsManager.Companion.paymentFailed
import com.limelight.common.AnalyticsManager.Companion.paymentSuccess
import com.limelight.common.AnalyticsManager.Companion.pgResponse
import com.limelight.common.GlobalData
import com.limelight.components.makeToast
import com.limelight.components.setScreenOrientation
import com.limelight.data.PreferenceManager
import com.limelight.screen.WebViewScreen
import com.limelight.viewmodel.WebViewModel
import java.time.LocalDateTime
import java.time.ZoneOffset


class WebViewActivity : AppCompatActivity() {
    var subBackPressed: ((Boolean) -> Unit)? = null
    var subReceivedMsg: ((String) -> Unit)? = null
    var subLoading: ((Boolean) -> Unit)? = null
    var subJoyStick: ((Boolean) -> Unit)? = null
    var subDragEnalbed: ((Boolean) -> Unit)? = null
    var subShowFpsConfigEditor: ((Boolean) -> Unit)? = null
    private var webView: WebView? = null
    private var page: String? = null
    var pcStream = false
    var pref: PreferenceManager?= null
    var fpsGpad: Boolean = false
    var fpsGpadDrag: Boolean = false
    var fpsConfigEditor: Boolean = false
    private var viewModel: WebViewModel? = null
    var x: Float = 0f
    var y: Float = 0f
    var z: Float  = 0f
    var originValueFlag = false
    var btnClick = true
    var keyValue: String = ""
    var rotationYMarginRange = 20
    var rotationXMarginRange = 30
    var rotationXValue : Float = -1F
    var rotationYValue : Float = -1F
//    private var sensorObserver =  { values: FloatArray? -> updateValues(values!!) }
    private var meanFilterEnabled by mutableStateOf(false)
    private var fusedOrientation by mutableStateOf(floatArrayOf(0f, 0f, 0f))
    private var logData by mutableStateOf(false)
    private var gyroscope : Boolean =  true
    val globalInstance = GlobalData.getInstance()

    var curactivity : WebViewActivity? = null

    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pref = PreferenceManager(this)
        var url = intent.getStringExtra("url")
        page = intent.getStringExtra("page")
        if (url == null) {
            finish()
            makeToast("Something Went Wrong.")
        } else {
            webView = WebView(this).apply {
                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                webViewClient = WebViewClient()
                webChromeClient = object: WebChromeClient() {
                    override fun onConsoleMessage(msg: ConsoleMessage): Boolean {
                        Log.d("webView", "${msg.message()} -- From line ${msg.lineNumber()} of ${msg.sourceId()}")
                        return true
                    }
                }
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.mediaPlaybackRequiresUserGesture = false
                loadUrl(url)
                Log.d("webView", url)
            }
            webView!!.requestFocus()
            webView!!.addJavascriptInterface(MyJSInterface(), "Android")
            setContent {
                Theme {
                    Column {
                        if(page == "stream") {
                                    val backPressed = remember {
                                        mutableStateOf(false)
                                    }
                                    subBackPressed = {
                                        backPressed.value = it
                                    }

                                    val loading = remember {
                                        mutableStateOf(true)
                                    }

                                    subLoading = {
                                        loading.value = it
                                    }

                                    val joyStick = remember {
                                        mutableStateOf(fpsGpad)
                                    }

                                    subJoyStick = {
                                        joyStick.value = it
                                    }

                                    val dragEnabled = remember {
                                        mutableStateOf(fpsGpadDrag)
                                    }

                                    subDragEnalbed = {
                                        dragEnabled.value = it
                                    }

                                    val showFpsConfigEditor = remember {
                                        mutableStateOf(fpsConfigEditor)
                                    }

                                    subShowFpsConfigEditor = {
                                        showFpsConfigEditor.value = it
                                    }

                                    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

                                    /*if(joyStick.value) {
                                        viewModel = WebViewModel()
                                        WebViewScreen(url = url, backPressed = backPressed.value, loading = loading.value, joyStick = joyStick.value, dragEnabled = dragEnabled.value, updateFpsControls = showFpsConfigEditor.value, this@WebViewActivity, webView!!, viewModel!!)
                                    } else {*/
                                    // viewModel = WebViewModel()

//                            var xValue =  Math.toDegrees(fusedOrientation[1].toDouble()).toFloat()
//                            var yValue =  Math.toDegrees(fusedOrientation[2].toDouble()).toFloat()
//                            var zValue =  Math.toDegrees(fusedOrientation[0].toDouble()).toFloat()
//
//                            Log.i("testAxisValue" , "test  value $xValue $yValue $zValue")
//
//
//                                viewModel?.updateXAxis(xValue)
//                                viewModel?.updateYAxis(yValue)
//                                viewModel?.updateZAxis(zValue)



//                            x= viewModel?.xAxis!!
//                            y= viewModel?.yAxis!!
//                            z= viewModel?.zAxis!!

                                    if (gyroscope){
                                        var xValue = Math.toDegrees(fusedOrientation[1].toDouble()).toFloat()
                                        var yValue = Math.toDegrees(fusedOrientation[2].toDouble()).toFloat()
                                        var zValue = Math.toDegrees(fusedOrientation[0].toDouble()).toFloat()
//                                if (!originValueFlag) {
//                                    fSensor = GyroscopeSensor(this@WebViewActivity)
//                                    fSensor?.register(sensorObserver)
//                                    fSensor?.start()
//                                    if (xValue != 0.0F || yValue != 0.0F || zValue != 0.0F) {
//                                        x = xValue
//                                        y=  yValue
//                                        z=  zValue
//                                        originValueFlag = true
//                                    }
//                                }
                                        if (btnClick) {
                                            WASDControl(xValue, yValue, zValue)
                                        }
                                    }


                                    WebViewScreen(url = url,
                                        backPressed = backPressed.value,
                                        loading = false,
                                        joyStick = false,
                                        gyroscope = true,
                                        dragEnabled = false,
                                        updateFpsControls = false,
                                        this@WebViewActivity,
                                        webView!!,
                                        viewModel)


//                            if(btnClick) {
//                                WASDControl(
//                                    rotationX = xValue,
//                                    rotationY = yValue,
//                                    rotationZ = zValue
//                                )
//                            }

                                    if(url.contains("desktop")) {
                                        pcStream = true
                                    }
                                }
                            else {
                                if(page == "pricing"){
                                if (globalInstance.purchasePlan) {
//                                    globalInstance.tracePurchasePlan.stop()
                                    globalInstance.purchasePlan = false
                                    viewModel = WebViewModel()
                                    WebViewScreen(url = url, webView = webView)
                                }
                            }
                            else {
                                WebViewScreen(url = url, webView = webView)


                            }
                        }
                    }
                }

                if(page == "stream") {
                    this.setScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
                    val receivedMsg = remember {
                        mutableStateOf("")
                    }
                    subReceivedMsg = {
                        receivedMsg.value = it
                    }
                    sendMsgToWeb(receivedMsg.value)
                    //subLoading?.invoke(false)

                } else if (page == "Controller Mapping") {
                    this.setScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
                }
            }
        }
    }


    inner class MyJSInterface() {
        @RequiresApi(Build.VERSION_CODES.O)
        @JavascriptInterface
        fun showMessageInNative(msg: String) {
            if(page == "pricing") {
                when(msg) {
                    "ended" -> {
                        this@WebViewActivity.finish()
                    }
                    "success" -> {
                        pgResponse(msg)
                        paymentSuccess(globalInstance.paymentPlan, globalInstance.paymentPrice)
                        GlobalData.getInstance().paymentStatus = true
                        viewModel?.countdownTimer?.start()
                    }
                    "failed" -> {
                        pgResponse(msg)
                        paymentFailed(globalInstance.paymentPlan)
                    }
                }
            } else if (page == "stream") {
                Log.d("webView", msg)
                when (msg) {
                    "loaded" -> {
                        if(globalInstance.gameStream){
                            globalInstance.gameStream =  false
                            globalInstance.traceGameStream.stop()
                        }
                        subReceivedMsg?.invoke("start")
                    }
                    "started" -> {
                        subLoading?.invoke(false)
                    }
                    "ended" -> {
                        if(pcStream) {
                            pref!!.setProperExit(true)
                            pref!!.setExitTime(LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC))
                        }
                        this@WebViewActivity.finish()
                    }
                    "goBack" -> {
                        this@WebViewActivity.finish()
                    }
                    "toggleFpsGpad" -> {
                        fpsGpad = !fpsGpad
                        subJoyStick?.invoke(fpsGpad)
                    }
                    "toggleFpsGpadDrag" -> {
                        fpsGpadDrag = !fpsGpadDrag
                        subDragEnalbed?.invoke(fpsGpadDrag)
                    }
                    "toggleFpsConfigEditor" -> {
                        toggleFpsConfigEditor()
                    }
                }
            } else {
                //added for controller mapping page
                when(msg) {
                    "ended" -> {
                        this@WebViewActivity.finish()
                    }
                }
            }
        }
    }


    private fun sendMsgToWeb(msgToSend: String) {
        Log.d("webView", msgToSend)
        webView?.evaluateJavascript("javascript: window.dispatchEvent(new CustomEvent(\"webview\", {\"detail\": {\"$msgToSend\": true}}));", null)
    }

    private fun sendControlsToWeb(controlsMessage: String) {
        //Log.d("webView", controlsMessage)
        webView?.evaluateJavascript("javascript: window.dispatchEvent(new CustomEvent(\"webview\", {\"detail\": {controls:\"$controlsMessage\"}}));", null)
    }

    fun toggleDragState() {
        fpsGpadDrag = !fpsGpadDrag
        subDragEnalbed?.invoke(fpsGpadDrag)
        //Log.d("webView", "drag toggle")
        sendMsgToWeb("toggleDrag")
    }

//    private fun updateValues(values: FloatArray) {
//        fusedOrientation = if (meanFilterEnabled) {
//            meanFilter.filter(values)
//        }
//        else {
//            values.also { fusedOrientation = it }
//        }
//        if (logData) {
//            dataLogger.setRotation(fusedOrientation)
//        }
//    }
    fun toggleFpsConfigEditor() {
        fpsConfigEditor = !fpsConfigEditor
        subShowFpsConfigEditor?.invoke(fpsConfigEditor)
        //Log.d("webView", "fpsConfigEditor toggled")
        //sendMsgToWeb("toggleFpsConfigEditor")
    }

    fun showStreamSettings() {
        sendMsgToWeb("showSettings")
    }

    override fun onBackPressed() {
        if(page == "stream") {
            subBackPressed?.invoke(true)
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        webView!!.destroy()
        webView = null
        super.onDestroy()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        sendMsgToWeb("resumed")
        if(pcStream) {
            pref!!.setPcExit(false)
            pref!!.setExitTime(LocalDateTime.now(ZoneOffset.UTC).minusMinutes(5).toEpochSecond(ZoneOffset.UTC))
        }
        webView!!.onResume()
        super.onResume()


    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onPause() {
        sendMsgToWeb("paused")
        if(pcStream) {
            pref!!.setPcExit(true)
            pref!!.setExitTime(LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC))
        }
        webView!!.onPause()
        super.onPause()
    }



//    private fun stopDataLog() {
//        logData = false
//        val path = dataLogger.stopDataLog()
//        Toast.makeText(this, "File Written to: $path", Toast.LENGTH_SHORT).show()
//    }
//    private fun readPrefs(): Mode {
//        meanFilterEnabled = false
//        val complimentaryFilterEnabled: Boolean = false
//        val kalmanFilterEnabled: Boolean = false
//        if (meanFilterEnabled) {
//            meanFilter.setTimeConstant(0.5f)
//        }
//        val mode: Mode
//        mode = if (!complimentaryFilterEnabled && !kalmanFilterEnabled) {
//            Mode.GYROSCOPE_ONLY
//        } else if (complimentaryFilterEnabled) {
//            Mode.COMPLIMENTARY_FILTER
//        } else {
//            Mode.KALMAN_FILTER
//        }
//        return mode
//    }
//    private enum class Mode {
//        GYROSCOPE_ONLY,
//        COMPLIMENTARY_FILTER,
//        KALMAN_FILTER
//    }
//
private fun WASDControl(rotationX: Float, rotationY: Float, rotationZ: Float) {

//        XRotation...
        if(rotationX<(x+0.8) && rotationX>(x-0.8)){
            keyValue = ""
            rotationXValue = 0F
        }
        else if (rotationX > (x-0.8)) {
            keyValue = "A"
            if (rotationX < x+rotationXMarginRange)
                rotationXValue = (rotationX-x)/rotationXMarginRange
            else
                rotationXValue = 1F

        }
        else if (rotationX< (x+0.8)) {
            keyValue = "D"
            if (rotationX > x-rotationXMarginRange)
                rotationXValue = (x-rotationX)/rotationXMarginRange

            else
                rotationXValue = 1F

        }

//        YRotation...
        if (rotationY < (y+0.8) && rotationY > (y-0.8)) {
            keyValue+= ""
            rotationYValue = 0F
        }
        else if (rotationY > (y-0.8)) {
            keyValue += " S"
            if (rotationY < y+rotationYMarginRange)
                rotationYValue = (rotationY-y)/rotationYMarginRange
            else
                rotationYValue = 1F

        }
        else if (rotationY < (y+0.8)) {
            keyValue += " W"
            if (rotationY > y-rotationYMarginRange)
                rotationYValue = (y-rotationY)/rotationYMarginRange
            else
                rotationYValue = 1F

        }

        Log.d("Gyroscope", " rotationXValue $rotationXValue rotationYValue $rotationYValue ")

    }
}
