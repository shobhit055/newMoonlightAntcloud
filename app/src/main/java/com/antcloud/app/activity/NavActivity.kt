package com.antcloud.app.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProgressIndicatorDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.antcloud.app.R
import com.antcloud.app.Theme
import com.antcloud.app.common.GlobalData
import com.antcloud.app.common.MyApplication
import com.antcloud.app.components.makeToast
import com.antcloud.app.components.navigationRoutes
import com.antcloud.app.components.LoadingScreen
import com.antcloud.app.components.signOut
import com.antcloud.app.data.DocumentListData
import com.antcloud.app.data.GameState
import com.antcloud.app.screen.account.NavScreen
import com.antcloud.app.viewmodel.GameViewModel
import com.antcloud.app.viewmodel.UserViewModel
import com.antcloud.app.theme.dark_grey
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@AndroidEntryPoint
class NavActivity : ComponentActivity() {
    val globalInstance = GlobalData.getInstance()
    var activity = this@NavActivity
    private var navigationRoute = navigationRoutes("")
    private var gameId = ""
    lateinit  var viewModel : GameViewModel
    lateinit  var userViewModel : UserViewModel
    var updateProgressBar: ((String) -> Unit)? = null
    var showReloadScreen: ((Boolean) -> Unit)? = null
    var calledRefresh = false
    var gameState : GameState? = null
    private val resumedFlag = mutableStateOf(false)


    override fun onCreate(savedInstanceState: Bundle?) {
        if (intent.hasExtra("route")) {
            navigationRoute = intent.getStringExtra("route").toString()
        }
        if (intent.hasExtra("gameId")) {
            gameId = intent.getStringExtra("gameId").toString()
        }
        super.onCreate(savedInstanceState)
        setContent {
            viewModel = hiltViewModel()
            userViewModel = hiltViewModel()
//            val systemUiController = rememberSystemUiController()
//            LaunchedEffect(Unit) {
//                systemUiController.setStatusBarColor(color = dark_grey, darkIcons = true)
//            }
            initialize()
            var progress by remember { mutableFloatStateOf(0.0f) }
            var loadingData by remember { mutableStateOf("Loading Your Preferences ...") }
            var reloadScreen by remember { mutableStateOf(false) }
            val animatedProgress = animateFloatAsState(
                targetValue = progress,
                animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec, label = "").value
            updateProgressBar = {
                loadingData = it
                progress += 0.34f
            }
            showReloadScreen = { reloadScreen = it }
            Surface(modifier = Modifier.fillMaxWidth().fillMaxHeight(), color = dark_grey) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    if (!reloadScreen) {
                        LoadingScreen(animatedProgress, loadingData,"login")
                    }
                    else {
                        Text(text = stringResource(id = R.string.loading_error_text),
                            modifier = Modifier.fillMaxWidth(0.8f).align(Alignment.CenterHorizontally),
                            textAlign = TextAlign.Justify,
                            color = MaterialTheme.colors.secondary,
                            style = MaterialTheme.typography.body1.copy(fontSize = 20.sp))

                        Spacer(modifier = Modifier.size(15.dp))

                        TextButton(
                            onClick = { this@NavActivity.recreate() },
                            shape = RoundedCornerShape(5.dp),
                            colors = ButtonDefaults.buttonColors(MaterialTheme.colors.primary),
                            modifier = Modifier.wrapContentWidth().align(Alignment.CenterHorizontally)) {
                            Text(text = stringResource(id = R.string.loading_error_button),
                                color = MaterialTheme.colors.secondary,
                                style = MaterialTheme.typography.button.copy(fontSize = 16.sp))
                        }
                    }
                }
            }
        }
    }

    fun onBackClick() {
        this.onBackPressed()
    }

    @Composable
    private fun initialize() {
        fetchRemote()
        updateProgressBar?.invoke("Loading Other Data ...")
        loadGamesData {
            updateProgressBar?.invoke("Loading Games Data ...")
            CoroutineScope(Dispatchers.Main).launch {
                delay(700)
                loadAndroidData {
                    if(globalInstance.ourGames.isEmpty()) {
                        showReloadScreen?.invoke(true)
                    }
                    else {
                        updateProgressBar?.invoke("Starting Up ...")
                        CoroutineScope(Dispatchers.Main).launch {
                            delay(500)
                            finishLoading()
                        }
                    }
                }
            }
        }
    }

    private fun fetchRemote() {
        val remoteConfig = MyApplication.getRemoteConfig()
        remoteConfig.fetchAndActivate().addOnCompleteListener(this@NavActivity) { task ->
            if (task.isSuccessful) {
                remoteConfig.all.forEach { (key, _) -> MyApplication.mapData(key) }
            } else {
                this@NavActivity.makeToast("Error 602 : Unable to fetch data, please try again after some time")
            }
        }
    }

    private fun loadAndroidData(onComplete: (() -> Unit)) {
        try{
            if(globalInstance.remoteDataFaq.isEmpty() || globalInstance.remoteDataPolicy.isEmpty()
                || globalInstance.remoteDataPricing.isEmpty() || globalInstance.remoteDataResolution.isEmpty()
                || globalInstance.remoteDataSupport.isEmpty() || globalInstance.remoteDataTerms.isEmpty()
                || globalInstance.remoteDataTutorial.isEmpty() || globalInstance.remotePlayVersion.equals(0.0)
                || globalInstance.ourGames.isEmpty()){
                activity.makeToast("Error 811 : Something Went Wrong")
                showReloadScreen?.invoke(true)
            }
            else {
                globalInstance.androidData = DocumentListData(
                    globalInstance.remoteDataFaq,
                    globalInstance.remoteDataPolicy,
                    globalInstance.remoteDataPricing,
                    globalInstance.remoteDataResolution,
                    globalInstance.remoteDataSupport,
                    globalInstance.remoteDataTerms,
                    globalInstance.remoteDataTutorial,
                    globalInstance.remotePlayVersion)
                onComplete()
            }
        }
        catch (e: Exception) {
            activity.makeToast("Error 811 : Something Went Wrong")
            showReloadScreen?.invoke(true)
        }
    }

    @Composable
    private fun loadGamesData(onComplete: (() -> Unit)) {
        LaunchedEffect(Unit){
            viewModel.getGameData("JWT " + GlobalData.getInstance().accountData.token)
        }
        gameState =  viewModel.gameState.value
        when(gameState?.success) {
            1 -> {
                LaunchedEffect(Unit) {
                    globalInstance.ourGames = gameState!!.mobileGames!!
                    globalInstance.ourGames[0].games.forEach {
                        it.isOurGame = true
                    }
                    onComplete()
                }
            }
            0 -> {
                LaunchedEffect(Unit) {
                    val msg = gameState?.error
                    showReloadScreen?.invoke(true)
                    if (gameState?.errorCode == 401) {
                        if (gameState?.error == "Token Expired" && !calledRefresh) {
                            userViewModel.getRefreshTokenData("JWT ${GlobalData.getInstance().accountData.refreshToken}")

                        }else {
                            activity.makeToast(msg!!)
                            calledRefresh = false
                            signOut(activity, userViewModel)
                        }
                    }
                    else if (gameState?.errorCode == 403) {
                        activity.makeToast(gameState?.error!!)
                        signOut(activity, userViewModel)
                    }
                    else {
                        activity.makeToast(gameState?.error!!)
                    }
                }
            }
        }
    }

    fun finishLoading() {
        if(globalInstance.ourGames.isEmpty()) {
            showReloadScreen?.invoke(true)
        }
        else {
            setContent {
//                hideStatusBar(this@NavActivity)
                Theme {
                    if (gameId != "")
                        GlobalData.getInstance().gameId = gameId
                    if(resumedFlag.value){
                        navigationRoute=  "support"
                    }
                    NavScreen(activity = this,navigationRoute,viewModel)
                }
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if(userViewModel.toggle)
            userViewModel.toggle = false

        if(globalInstance.toolbarInvisible || globalInstance.gemsHistoryFlag==3) {
            viewModel.updateToolbarState(false)
            if(globalInstance.gemsHistoryFlag==3){
                globalInstance.gemsHistoryFlag--
            }
            else{
                globalInstance.toolbarInvisible = false
                globalInstance.gemsHistoryFlag--
            }
        }
        else {
            globalInstance.gemsHistoryFlag = 0
            viewModel.updateToolbarState(true)
        }
    }


    override fun onRestart() {
        super.onRestart()
        if(globalInstance.openSupportScreen)
            resumedFlag.value = true
    }
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if(intent.hasExtra("route")){
            navigationRoute = navigationRoutes(intent.getStringExtra("route").toString())
        }
        if(intent.hasExtra("gameId")) {
            gameId = intent.getStringExtra("gameId").toString()
        }
        finishLoading()
    }
}

