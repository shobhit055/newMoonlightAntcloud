package com.limelight.activity

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.limelight.common.AppUtils
import com.limelight.common.AppUtils.Companion.clearCheck
import com.limelight.common.AppUtils.Companion.hideStatusBar
import com.limelight.common.AppUtils.Companion.navigateScreen
import com.limelight.common.AppUtils.Companion.saveRefreshTokenData
import com.limelight.common.GlobalData
import com.limelight.components.makeToast
import com.limelight.components.navigationRoutes
import com.limelight.screen.auth.MainScreen
import com.limelight.viewmodel.UserViewModel
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.messaging.messaging
import com.limelight.common.AnalyticsManager.Companion.removeAnalyticsUserId
import com.limelight.common.AnalyticsManager.Companion.setAnalyticsUserId
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileInputStream


@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class  SplashActivity : ComponentActivity(){
     private var navigationRoute = navigationRoutes("")
     private var encryptedDataSize: Int = 0
     private var encryptedData: ByteArray? = null
     private var totalData = 0
     lateinit var file : File
     private lateinit var file2: File
     private var globalInstance = GlobalData.getInstance()
     var activity  = this@SplashActivity
     var calledRefresh = false
     lateinit var viewModel: UserViewModel
//     private lateinit var firebaseAnalytics: FirebaseAnalytics
     private var flag = 0
     private var apiResp = false
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       // enableEdgeToEdge()
        actionBar?.hide()
        setContent {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            hideStatusBar(activity)
            viewModel = hiltViewModel()
            FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = false
            FirebaseApp.initializeApp(applicationContext)
            Firebase.messaging.isAutoInitEnabled = true
            firebaseAnalytics =  Firebase.analytics
            if(intent.hasExtra("flag"))
                flag =  intent.getIntExtra("flag" , 0)
        //    startActivity(Intent(this@SplashActivity, AppView::class.java))
            getToken()
            val checkUserState = viewModel.checkUserState.value
            val refreshTokenState = viewModel.refreshTokenState.value
            when (checkUserState.success){
                1 -> {
                    apiResp = true
//                    MainScreen(activity,flag,apiResp)
                    LaunchedEffect(Unit) {
                        setAnalyticsUserId(globalInstance.accountData.id)
                        if (globalInstance.accountData.refreshToken != "") {
                            checkUserState.userData?.refreshToken =
                                globalInstance.accountData.refreshToken
                        }
                        globalInstance.accountData = checkUserState.userData!!
                        globalInstance.accountData.token = checkUserState.token!!
                        navigateScreen(activity, NavActivity::class.java)
                        finishAffinity()
                    }
                }
                0 -> {
                    removeAnalyticsUserId()
                    if(file.exists()) {
                        file.delete()
                    }
                    when (checkUserState.errorCode) {
                        401 -> {
                            val msg =  checkUserState.error
                            if(msg == "Token Expired" && !calledRefresh) {
                                LaunchedEffect(Unit) {
                                    viewModel.getRefreshTokenData("JWT " + globalInstance.accountData.refreshToken)
                                }
                            }
                            else {
                                LaunchedEffect(Unit) {
                                    activity.makeToast("Error 307 : Unable to validate session. Kindly login again")
                                    calledRefresh = false
                                    clearCheck(activity)
                                }
                                MainScreen(activity,flag,apiResp)
                            }
                        }
                        0->{
                            LaunchedEffect(Unit) {
                                if (checkUserState.error != "")
                                    activity.makeToast(checkUserState.error)
                                clearCheck(activity)
                            }
                            MainScreen(activity,flag,apiResp)
                        }
                        else -> {
                            LaunchedEffect(Unit) {
                                if (checkUserState.error != "")
                                    activity.makeToast(checkUserState.error)
                                if (file2.exists()) {
                                    file2.delete()
                                }
                            }
                            MainScreen(activity,flag,apiResp)
                        }
                    }
                }
            }
            when (refreshTokenState.success){
                1 -> {
                    apiResp = true
//                    MainScreen(activity,flag,apiResp)
                    LaunchedEffect(Unit) {
                        calledRefresh = true
                        if ((refreshTokenState.accessToken != "") && (refreshTokenState.refreshToken!= "")) {
                            saveRefreshTokenData(activity,refreshTokenState.accessToken,refreshTokenState.refreshToken)
                            viewModel.getCheckUserData("JWT " + globalInstance.accountData.token)
                        }
                    }
                }
                0 -> {
                    removeAnalyticsUserId()
                    calledRefresh = true
                    when (refreshTokenState.errorCode) {
                        502 -> {
                            LaunchedEffect(Unit) {
                                if (refreshTokenState.error != "")
                                    activity.makeToast(refreshTokenState.error).toString()
                            }
                            MainScreen(activity,flag,apiResp)
                        }
                        else -> {
                            LaunchedEffect(Unit) {
                                if (file2.exists()) {
                                    file2.delete()
                                }
                                if (refreshTokenState.error != "")
                                    activity.makeToast(refreshTokenState.error).toString()
                            }
                            MainScreen(activity,flag,apiResp)
                        }
                    }
                }
            }
            if(globalInstance.accountData.token != ""){
                LaunchedEffect(Unit) {
                    viewModel.getCheckUserData("JWT " + globalInstance.accountData.token)
                }
            }
            else {
                removeAnalyticsUserId()
                MainScreen(activity,flag,apiResp)
            }
        }
    }

    private fun getToken() {
        file = File(filesDir, "file.nk")
        file2 = File(filesDir, "file.lt")
        if (file.exists()) {
            globalInstance.accountData.token = getUserToken(file)
            if (globalInstance.accountData.token == "") {
                activity.makeToast("Error 901 : Something Went Wrong.")
                clearCheck(activity)
            }
        }
        if (file2.exists()) {
            globalInstance.accountData.refreshToken = getUserToken(file2)
            if (globalInstance.accountData.refreshToken == "") {
                activity.makeToast("Error 901 : Something Went Wrong.")
                clearCheck(activity)
            }
        }
    }

    private fun getUserToken(inputFile: File): String {
        totalData = 0
        encryptedDataSize = 0
        encryptedData = ByteArray(400)
        val fis = openFileInput(inputFile.name)
        val ivSize = fis.read()
        if(ivSize < 0) {
            activity.makeToast("Error 902 : Something Went Wrong")
            fis.close()
            clearCheck(activity)
            return ""
        }
        val iv = ByteArray(ivSize)
        fis.read(iv)
        while(doRead(fis) != -1) {
            totalData += encryptedDataSize
        }
        val enData = ByteArray(totalData)
        for (i in 0 until  totalData) {
            enData[i] = encryptedData!![i]
        }
        fis.close()
        val decryptedData = AppUtils.decryptData(iv, enData)
        encryptedData = null
        return decryptedData
    }

    private fun doRead(fis: FileInputStream): Int {
        encryptedDataSize = fis.read(encryptedData)
        return encryptedDataSize
    }

}