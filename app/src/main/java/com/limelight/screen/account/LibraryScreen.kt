package com.limelight.screen.account


import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.util.DisplayMetrics
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.limelight.R

import com.limelight.common.DrawerScreens
import com.limelight.components.VerticalGrid
import com.limelight.components.makeToast
import com.limelight.data.Game
import com.limelight.theme.BlueGradient
import com.limelight.theme.PinkGradient
import com.limelight.viewmodel.UserViewModel
import androidx.compose.foundation.lazy.items
import androidx.compose.material.TextButton
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder

import com.limelight.components.CustomDialog
import com.limelight.common.GlobalData
import com.limelight.components.Loading
import com.limelight.components.signOut
import com.limelight.theme.subtitle
import com.limelight.activity.NavActivity
import com.limelight.common.AppUtils.Companion.gradientColors
import com.limelight.common.AppUtils.Companion.saveRefreshTokenData
import com.limelight.data.PreferenceManager
import com.limelight.screen.price.globalInstance
import com.limelight.theme.dark_grey
import com.limelight.theme.mainTitle
import com.limelight.ui.AppView
import com.limelight.viewmodel.StreamViewModel
import java.io.File
import java.time.LocalDateTime
import java.time.ZoneOffset


lateinit var currentAct: NavActivity

@RequiresApi(Build.VERSION_CODES.O)
fun libraryNav(navGraph: NavGraphBuilder, activity: NavActivity, updateToolbar: ((String) -> Unit),
               navigate: ((String) -> Unit)) {
    return navGraph.composable(DrawerScreens.Library.route) {
        currentAct =  activity

        libraryScreen(navigate ,activity)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun libraryScreen(navigate: ((String) -> Unit) , activity: NavActivity) {
    var calledRefresh = false
    val userViewModel: UserViewModel = hiltViewModel()
    LaunchedEffect(key1 = Unit) {
        userViewModel.getCheckUserData("JWT ${GlobalData.getInstance().accountData.token}")
    }
    val socketViewModel: StreamViewModel = hiltViewModel()
    val checkUserState = userViewModel.checkUserState.value
    val refreshTokenState = userViewModel.refreshTokenState.value
    val vmStatusState = socketViewModel.getVMStatus.value


    when (vmStatusState.success) {
        1 -> {
            LaunchedEffect(Unit) {
//                if(vmStatusState.status=="stopped"){
//                    socketViewModel.callSocket()
//                }
//                else if(vmStatusState.status=="running"){
//                    activity.makeToast("your machine is already running")
//                }
            }
        }

        0 -> {
        }
    }




    when (checkUserState.success) {
        1 -> {
            LaunchedEffect(Unit) {
                if (GlobalData.getInstance().accountData.refreshToken != "") {
                    checkUserState.userData?.refreshToken =
                        GlobalData.getInstance().accountData.refreshToken
                }
                GlobalData.getInstance().accountData = checkUserState.userData!!
                GlobalData.getInstance().accountData.token = checkUserState.token!!
                userViewModel.updateUserInfo(GlobalData.getInstance().accountData)
                Log.i("test ", "userdata" + checkUserState.userData)
            }
        }

        0 -> {
            LaunchedEffect(Unit) {
                val file = File(activity.filesDir, "file.nk")
                if (file.exists()) {
                    file.delete()
                }
                when (checkUserState.errorCode) {
                    401 -> {
                        val msg = checkUserState.error
                        if (msg == "Token Expired" && !calledRefresh) {
                            userViewModel.getRefreshTokenData("JWT " + GlobalData.getInstance().accountData.refreshToken)
                        } else {
                            activity.makeToast("Error 307 : Unable to validate session. Kindly login again")
                            calledRefresh = false
                            signOut(activity, userViewModel)

                        }
                    }

                    else -> {
                        Log.i("test", "qefqe" + checkUserState.errorCode)
                        if (checkUserState.error != "")
                            activity.makeToast(checkUserState.error)
                        signOut(activity, userViewModel)
                    }
                }
            }
        }
    }
    when (refreshTokenState.success) {
        1 -> {
            LaunchedEffect(Unit) {
                calledRefresh = true
                if ((refreshTokenState.accessToken != "") && (refreshTokenState.refreshToken != "")) {
                    saveRefreshTokenData(
                        activity,
                        refreshTokenState.accessToken,
                        refreshTokenState.refreshToken
                    )
                }
            }
        }

        0 -> {
            LaunchedEffect(Unit) {
                calledRefresh = true
                if (refreshTokenState.errorCode != -1) {
                    val msg = refreshTokenState.error
                    if (msg != "")
                        activity.makeToast(msg)
                    signOut(activity, userViewModel)
                }
            }
        }
    }

    val scrollState = rememberScrollState()
    val openDialogCustom = remember {
        mutableStateOf(false)
    }
    val viewModel: UserViewModel = hiltViewModel()

    val userData = remember {
        mutableStateOf(viewModel.userData)
    }
    val showUpgradePlan = remember {
        mutableStateOf(false)
    }
    val pref = PreferenceManager(currentAct)
    val handlePcClick = {
        if (userData.value.currentPlan != "Basic") {
            if (!globalInstance.paymentStatus) {
                val launchPc = {
                    if (userData.value.currentPlan != "Basic") {
                        if (!GlobalData.getInstance().paymentStatus) {
                            val intent = Intent(currentAct,AppView::class.java)
                            intent.putExtra("connect" , "socket")
                            currentAct.startActivity(intent)
                        } else {
                            currentAct.makeToast("Thank you for your payment. Your account is being set up right now. It will be available in ${GlobalData.getInstance().paymentPcTimerMins} : ${GlobalData.getInstance().paymentPcTimerSecs} minutes.")
                        }
                    }
                    else
                        showUpgradePlan.value = true
                }
                if(pref.getProperExit() || pref.getPcExit()) {
                    val currentTime = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC)
                    if(pref.getProperExit()) {
                        if ((currentTime - pref.getExitTime()) <= 60) {
                            currentAct.makeToast("Your PC is being reset. Please wait for a minute and try again")
                        } else {
                            pref.clearPrefs()
                            launchPc()
                        }
                    }
                    else if(pref.getPcExit()) {
                        if ((currentTime - pref.getExitTime()) in 55..120) {
                            currentAct.makeToast("Your PC is being reset. Please wait for a minute and try again")
                        } else {
                            pref.clearPrefs()
                            launchPc()
                        }
                    }
                }
                else {
                    launchPc()
                }
            }
            else {
                currentAct.makeToast("Thank you for your payment. Your account is being set up right now." +
                        " It will be available in ${globalInstance.paymentPcTimerMins} : ${globalInstance.paymentPcTimerSecs} minutes.")
            }
        }
        else {
            showUpgradePlan.value = true
        }
    }

        Column(
            Modifier.fillMaxWidth().fillMaxHeight()
                .background(Color.Black)) {
            Box(
                modifier = Modifier.clickable {
                   handlePcClick()
                }.padding(top = 10.dp, start = 10.dp, end = 10.dp)
                    .fillMaxWidth()
                    .aspectRatio(1.8f)
                    .border(1.dp, Color.White, RoundedCornerShape(35.dp))
                    .background(Color.DarkGray, RoundedCornerShape(35.dp)),
                contentAlignment = Alignment.Center
            ) {

                Image(
                    painter = painterResource(id = R.drawable.windows),
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.clip(RoundedCornerShape(35.dp)))
                Box(
                    modifier = Modifier.fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(colors = gradientColors),
                            RoundedCornerShape(35.dp)
                        )
                )


                Column {
                    Icon(
                        modifier = Modifier.size(50.dp)
                            .align(Alignment.CenterHorizontally),
                        painter = painterResource(id = R.drawable.play_circle),
                        contentDescription = "Launch PC Mode",
                        tint = Color.White
                    )
                    Text(
                        fontSize = 16.sp,
                        text = "Connect",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

            }

            Text(
                text = "Quick Launch Games",
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.Start), color = Color.White,
                modifier = Modifier.padding(start = 20.dp, top = 30.dp))

            GamesRow(
                navigate,
                orientationLandscape = false,
                games = when (GlobalData.getInstance().ourGames[0].games.size > 6) {
                    true -> GlobalData.getInstance().ourGames[0].games.subList(0, 6)
                        .toList()

                    false -> GlobalData.getInstance().ourGames[0].games
                })
        }



    if (showUpgradePlan.value) {
        CustomDialog(openDialogCustom = (showUpgradePlan.value), onDismiss = { /*TODO*/ }) {
            androidx.compose.material3.Card(
                modifier = Modifier.padding(start = 5.dp, top = 10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black)
            ) {
                Column(
                    modifier = Modifier
                        .background(androidx.compose.material.MaterialTheme.colors.surface)
                ) {
                    androidx.compose.material.Text(
                        text = "Please upgrade your plan",
                        color = androidx.compose.material.MaterialTheme.colors.secondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 15.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                    Row(
                        Modifier.fillMaxWidth().padding(top = 10.dp).background(
                            androidx.compose.material.MaterialTheme.colors.primary.copy(alpha = 0.5f)
                        ),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = {
                            navigate(DrawerScreens.Pricing.route)
                            showUpgradePlan.value = false
                        }) {
                            androidx.compose.material.Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = "",
                                tint = androidx.compose.material.MaterialTheme.colors.secondary
                            )
                            Spacer(modifier = Modifier.size(5.dp))
                            androidx.compose.material.Text(
                                text = "OK ",
                                fontWeight = FontWeight.Medium,
                                color = androidx.compose.material.MaterialTheme.colors.secondary,
                                style = subtitle,
                            )
                        }
                    }
                }

            }
        }
    }
    if (openDialogCustom.value) {
        CustomDialog(
            openDialogCustom = openDialogCustom,
            label = "",
            onDismiss = { }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(androidx.compose.material.MaterialTheme.colors.surface)
                    .clip(
                        RoundedCornerShape(10.dp)
                    )
            ) {
                Loading("testtt")
            }
        }

    }

}











@Composable
fun AsyncImages(url: String, modifier: Modifier) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current).data(url)
            .decoderFactory(SvgDecoder.Factory()).crossfade(true)
            .build(),
        contentDescription = url,
        contentScale = ContentScale.FillBounds,
        modifier = modifier,
        placeholder = painterResource(id = R.drawable.image_loading),
        onError = { Toast.makeText(currentAct,"Something Went Wrong.", Toast.LENGTH_LONG).show() },
        onSuccess = {},
        alignment = Alignment.Center)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GamesRow(navigate: ((String) -> Unit),orientationLandscape: Boolean, games: List<Game>) {
    if(games.isNotEmpty()) {
        if (orientationLandscape) {
            if (games.size == 3) {
                games[2].expandable = true
            }
        }
        else {
            if (games.size == 5) {
                games[4].expandable = true
            }
        }
    }
    Column(
        modifier = Modifier.wrapContentHeight().padding(top = if (orientationLandscape) 0.dp else 10.dp)) {
        if(orientationLandscape) {
            VerticalGrid(columns = 2) {
                games.forEach { game ->
                    GameTile(game = game, modifier = Modifier, orientationLandscape = true) {
                        if(games.isNotEmpty() && game.gameId!="") {
                            GlobalData.getInstance().gameId= game.gameId
                            navigate(DrawerScreens.GameDetails.route)
                        }
                        else{
                            currentAct.makeToast("data is load after sometimes, please wait...")
                        }
                    }
                    if(game.expandable) {
                        GameTile(game = game, modifier = Modifier, expand = true, orientationLandscape = true) {
                            navigate(DrawerScreens.LibraryDetails.route)
                        }
                    }
                }
            }
        }
        else {
            LazyRow {
                items(games, key = { it.gameId }) {
                    GameTile(game = it, modifier = Modifier.animateItemPlacement()) {
                        if(games.isNotEmpty() && it.gameId!="") {
                            GlobalData.getInstance().gameId= it.gameId
                            navigate(DrawerScreens.GameDetails.route)

                        }
                        else{
                            currentAct.makeToast("data is load after sometimes, please wait...")
                        }
                    }
                    if (it.expandable) {
                        GameTile(game = it, modifier = Modifier.animateItemPlacement(), expand = true) {
                            navigate(DrawerScreens.LibraryDetails.route)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GameTile(game: Game, modifier: Modifier, expand: Boolean = false, orientationLandscape: Boolean = false, onClick: (() -> Unit)) {
    Box(
        modifier =
        if(orientationLandscape)
            Modifier.padding(vertical = 2.dp, horizontal = 5.dp)
                .clickable {
                    // AnalyticsManager.gameButton(game.gameId)
                    onClick()
                }
        else modifier.wrapContentSize().padding(vertical = 10.dp, horizontal = 7.dp)
            .clickable {
                //    AnalyticsManager.gameButton(game.gameId)
                onClick()
            }) {
//        globalInstance.imageLoading = true
//        globalInstance.traceImageLoading =  FirebasePerformance.getInstance().newTrace("image_loading")
//        globalInstance.traceImageLoading.start()
        AsyncImages(
            url = if (!expand) "https://antplay-gamedata.s3.ap-south-1.amazonaws.com/${game.gameId}.jpg"
            else { if(orientationLandscape) "https://antplay-gamedata.s3.ap-south-1.amazonaws.com/background_landscape.jpg" else "https://antplay-gamedata.s3.ap-south-1.amazonaws.com/background.jpg"},
            modifier = if(orientationLandscape)
                Modifier.fillMaxHeight(0.5f).clip(RoundedCornerShape(6.dp))
            else
                Modifier.clip(RoundedCornerShape(6.dp)).width(213.dp).height(120.dp),
            orientationLandscape = orientationLandscape)

        if(!orientationLandscape){
            Row(Modifier.clip(RoundedCornerShape(6.dp))
                .background(Brush.verticalGradient(colors = listOf(Color.Transparent, Color.Black)))
                .width(213.dp).height(120.dp)) {}
            androidx.compose.material.Text(
                text = if (!expand) game.name else "View More",
                style = androidx.compose.material.MaterialTheme.typography.caption.copy(fontSize = 16.sp),
                modifier = Modifier.align(Alignment.BottomStart)
                    .padding(start = 10.dp, bottom = 2.dp),
                color = androidx.compose.material.MaterialTheme.colors.secondary.copy(alpha = .8f)
            )
        }
    }

}



@Composable
fun AsyncImages(url: String, modifier: Modifier, orientationLandscape: Boolean = false) {
    AsyncImage(model = ImageRequest.Builder(LocalContext.current).data(url)
        .decoderFactory(SvgDecoder.Factory()).crossfade(true).build(),
        contentDescription = url,
        contentScale = if(orientationLandscape) ContentScale.FillBounds else ContentScale.FillBounds,
        modifier = modifier,
        placeholder = painterResource(id = R.drawable.image_loading),
        onError = { currentAct.makeToast("Error 501 : Something Went Wrong.")
        }, onSuccess = {
        }, alignment = Alignment.Center)


}








