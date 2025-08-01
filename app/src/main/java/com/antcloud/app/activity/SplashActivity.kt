package com.antcloud.app.activity

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import com.antcloud.app.activity.NavActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.antcloud.app.common.AppUtils
import com.antcloud.app.common.AppUtils.Companion.clearCheck
import com.antcloud.app.common.AppUtils.Companion.navigateScreen
import com.antcloud.app.common.AppUtils.Companion.saveRefreshTokenData
import com.antcloud.app.common.GlobalData
import com.antcloud.app.components.makeToast
import com.antcloud.app.components.navigationRoutes
import com.antcloud.app.screen.auth.MainScreen
import com.antcloud.app.viewmodel.UserViewModel
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.messaging
import com.google.firebase.perf.FirebasePerformance
import com.antcloud.app.common.AnalyticsManager.Companion.removeAnalyticsUserId
import com.antcloud.app.common.AnalyticsManager.Companion.setAnalyticsUserId

import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileInputStream


@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : ComponentActivity(){
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
     private var flag = 0
     private var apiResp = false
     private lateinit var firebaseAnalytics: FirebaseAnalytics
     private var gameId = ""
     private var activityResultLauncher: ActivityResultLauncher<IntentSenderRequest>? = null
     private var updateAvailable = false
     private var appUpdateManager: AppUpdateManager? = null



    @Composable
    fun RegisterActivityResult(onResult: (ActivityResultLauncher<IntentSenderRequest>) -> Unit) {
        val res  = rememberLauncherForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result: ActivityResult ->
            when (result.resultCode) {
                com.google.android.play.core.install.model.ActivityResult.RESULT_IN_APP_UPDATE_FAILED -> {
                    this@SplashActivity.makeToast("Unable to update the app.")
                }
                RESULT_CANCELED -> {
                    this@SplashActivity.makeToast("To proceed ahead, please relaunch the app at your convenience to install the latest version.")
                }
                else -> {
                    updateAvailable = false
                }
            }
        }
        onResult(res)
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        actionBar?.hide()
//        WindowCompat.setDecorFitsSystemWindows(window, false)
        FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = false
        FirebaseApp.initializeApp(applicationContext)
        Firebase.messaging.isAutoInitEnabled = true
        firebaseAnalytics =  Firebase.analytics
        if(intent.hasExtra("flag"))
            flag =  intent.getIntExtra("flag" , 0)
        val extras = intent.extras
        if(extras != null) {
            navigationRoute = navigationRoutes(intent.getStringExtra("route").toString())
            if(intent.hasExtra("gameId")) {
                gameId = intent.getStringExtra("gameId").toString()
            }
        }

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                return@OnCompleteListener
            }
        })
        getToken()
        setContent {
            RegisterActivityResult { res ->
                activityResultLauncher = res
                appUpdateManager = AppUpdateManagerFactory.create(this)
                if(activityResultLauncher != null) {

                    val appUpdateInfoTask = appUpdateManager?.appUpdateInfo
                    //check for update
                    appUpdateInfoTask?.addOnSuccessListener { appUpdateInfo ->
                        if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                            && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
                        ) {
                            //start update
                            updateAvailable = true
                            appUpdateManager?.startUpdateFlowForResult(
                                appUpdateInfo,
                                activityResultLauncher!!,
                                AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
                            )
                        }
                    }
                }
            }
            if(appUpdateManager != null && activityResultLauncher != null){
                appUpdateManager?.appUpdateInfo?.addOnSuccessListener { appUpdateInfo ->
                    if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                        //start update
                        appUpdateManager?.startUpdateFlowForResult(
                            appUpdateInfo,
                            activityResultLauncher!!,
                            AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
                        )
                    }
                }
            }
            viewModel = hiltViewModel()
            if (!updateAvailable) {
                if (globalInstance.accountData.token != "") {
                    LaunchedEffect(Unit) {
                        callCheckUserApi()
                    }
                }
                else {
                    globalInstance.appStartWithoutToken = true
                    globalInstance.traceAppStartWithoutToken =
                        FirebasePerformance.getInstance().newTrace("app_start_without_token")
                    globalInstance.traceAppStartWithoutToken.start()
                    callMainScreen(activity, flag, apiResp)
                }
            }
            val checkUserState = viewModel.checkUserState.value
            val refreshTokenState = viewModel.refreshTokenState.value
            when (checkUserState.success) {

                1 -> {
                    if(globalInstance.checkUserApi){
                        globalInstance.traceCheckUserApi.stop()
                        globalInstance.checkUserApi = false
                    }
                    apiResp = true
                    LaunchedEffect(Unit) {
                        setAnalyticsUserId(globalInstance.accountData.id)
                        if (globalInstance.accountData.refreshToken != "") {
                            checkUserState.userData?.refreshToken =
                                globalInstance.accountData.refreshToken
                        }
                        globalInstance.accountData = checkUserState.userData!!
                        globalInstance.accountData.token = checkUserState.token!!
                        navigateScreen(activity, NavActivity::class.java)
                        if(!updateAvailable) {
                            finishAffinity()
                        }
                    }
                }
                0 -> {
                    if(globalInstance.checkUserApi){
                        globalInstance.traceCheckUserApi.stop()
                        globalInstance.checkUserApi = false
                    }
                    if (file.exists()) {
                        file.delete()
                    }
                    when (checkUserState.errorCode) {
                        401 -> {
                            val msg = checkUserState.error
                            if (msg == "Token Expired" && !calledRefresh) {
                                LaunchedEffect(Unit) {
//                                    if(isInternetAvailable(this@SplashActivity))
                                        viewModel.getRefreshTokenData("JWT " + globalInstance.accountData.refreshToken)

//                                    else
//                                        activity.makeToast("No internet connection. Please check your network.")
                                }
                            } else {
                                LaunchedEffect(Unit) {
                                    activity.makeToast("Error 307 : Unable to validate session. Kindly login again")
                                    calledRefresh = false
                                    clearCheck(activity)
                                }
                                callMainScreen(activity, flag, apiResp)

                            }
                        }
                        0 -> {
                            LaunchedEffect(Unit) {
                                if (checkUserState.error != "")
                                    activity.makeToast(checkUserState.error)
                                clearCheck(activity)
                            }
                            callMainScreen(activity, flag, apiResp)
                        }
                        else -> {
                            LaunchedEffect(Unit) {
                                if (checkUserState.error != "")
                                    activity.makeToast(checkUserState.error)
                                if (file2.exists()) {
                                    file2.delete()
                                }
                            }
                            callMainScreen(activity, flag, apiResp)

                        }
                    }
                }
            }
            when (refreshTokenState.success) {
                1 -> {
                    apiResp = true
                    LaunchedEffect(Unit) {
                        calledRefresh = true
                        if ((refreshTokenState.accessToken != "") && (refreshTokenState.refreshToken != "")) {
                            saveRefreshTokenData(activity, refreshTokenState.accessToken, refreshTokenState.refreshToken)
                            callCheckUserApi()
                        }
                    }
                }

                0 -> {

                    calledRefresh = true
                    when (refreshTokenState.errorCode) {
                        502 -> {
                            LaunchedEffect(Unit) {
                                if (refreshTokenState.error != "")
                                    activity.makeToast(refreshTokenState.error).toString()
                            }
                            callMainScreen(activity, flag, apiResp)
                        }

                        else -> {
                            LaunchedEffect(Unit) {
                                if (file2.exists()) {
                                    file2.delete()
                                }
                                if (refreshTokenState.error != "")
                                    activity.makeToast(refreshTokenState.error).toString()
                            }
                            callMainScreen(activity, flag, apiResp)

                        }
                    }
                }
            }
        }
    }

    private fun callCheckUserApi() {
      //  if(isInternetAvailable(this@SplashActivity)) {
            globalInstance.appStartToken = true
            globalInstance.traceAppStartToken = FirebasePerformance.getInstance().newTrace("app_start_with_token")
            globalInstance.traceAppStartToken.start()

            globalInstance.checkUserApi = true
            globalInstance.traceCheckUserApi = FirebasePerformance.getInstance().newTrace("check_user_api")
            globalInstance.traceCheckUserApi.start()
            viewModel.getCheckUserData("JWT " + globalInstance.accountData.token)
     //   }
//        else{
//            activity.makeToast("No internet connection. Please check your network.")
//        }
    }

    @Composable
    private fun callMainScreen(activity: SplashActivity, initialState : Int, apiResp: Boolean) {
        removeAnalyticsUserId()
        MainScreen(activity,flag,apiResp)
    }


    private fun getToken() {
        file = File(filesDir, "file.nk")
        file2 = File(filesDir, "file.lt")
        if (file.exists()) {
            globalInstance.accountData.token = getUserToken(file)
            if (globalInstance.accountData.token == "") {
               // activity.makeToast("Error 901 : Something Went Wrong.")
                clearCheck(activity)
            }
        }
        if (file2.exists()) {
            globalInstance.accountData.refreshToken = getUserToken(file2)
            if (globalInstance.accountData.refreshToken == "") {
               // activity.makeToast("Error 901 : Something Went Wrong.")
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