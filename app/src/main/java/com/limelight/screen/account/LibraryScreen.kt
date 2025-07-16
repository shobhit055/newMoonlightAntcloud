package com.limelight.screen.account


import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.util.DisplayMetrics
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.constraintlayout.compose.ConstraintLayout
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
import com.limelight.components.Play
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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester.Companion.createRefs
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.google.firebase.perf.FirebasePerformance

import com.limelight.components.CustomDialog
import com.limelight.common.GlobalData
import com.limelight.components.Loading
import com.limelight.components.signOut
import com.limelight.theme.subtitle
import com.limelight.activity.NavActivity
import com.limelight.common.AnalyticsManager
import com.limelight.common.AppUtils.Companion.gradientColors
import com.limelight.common.AppUtils.Companion.saveRefreshTokenData
import com.limelight.data.PreferenceManager
import com.limelight.screen.price.globalInstance
import com.limelight.theme.dark_grey
import com.limelight.theme.heading
import com.limelight.theme.mainTitle
import com.limelight.theme.primaryGreen
import com.limelight.ui.AppView
import com.limelight.viewmodel.StreamViewModel
import kotlinx.coroutines.NonDisposableHandle.parent
import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import kotlin.math.absoluteValue


lateinit var currentAct: NavActivity
var globalInstance  =  GlobalData.getInstance()

@RequiresApi(Build.VERSION_CODES.O)
fun libraryNav(navGraph: NavGraphBuilder, activity: NavActivity, updateToolbar: ((String) -> Unit),
               navigate: ((String) -> Unit)) {
    return navGraph.composable(DrawerScreens.Library.route) {
        currentAct =  activity

        libraryScreen(navigate ,activity)

        if(globalInstance.appStartToken){
            globalInstance.traceAppStartToken.stop()
            globalInstance.appStartToken = false

        }
        if(globalInstance.emailClickLoginBtn){
            globalInstance.traceEmailClickLoginBtn.stop()
            globalInstance.emailClickLoginBtn = false
        }
        if(globalInstance.phoneClickLoginBtn){
            globalInstance.tracePhoneClickLoginBtn.stop()
            globalInstance.phoneClickLoginBtn = false
        }
        if(globalInstance.signUp){
            globalInstance.signUp =  false
            globalInstance.traceSignUp.stop()
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun libraryScreen(navigate: ((String) -> Unit) , activity: NavActivity) {
    var timeLeft : Int
    var timeLeftP : Int
    var timeLeftString : String
    var daysLeftP : Double = 0.0
    var daysLeft : Long
    var imageHeight = (LocalConfiguration.current.screenWidthDp / 1.5)
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
    val showUpdateDialog = remember {
        mutableStateOf(false)
    }
    val showMaintenanceDialog = remember {
        val temp = if(globalInstance.remoteAppMessage.isNotEmpty()) globalInstance.remoteAppMessage[0].showDialog else false
        mutableStateOf(temp)
    }

    val currentAppVersion = 1.23

    viewModel.subShowMaintenanceDialog = {
        showMaintenanceDialog.value = it
    }

    val pref = PreferenceManager(currentAct)
    showUpdateDialog.value = (currentAppVersion < globalInstance.androidData.playVersion)

    val showCard: @Composable (Int, Int,  Unit, Int, Boolean) -> Unit = { imageRes: Int, textRes: Int, clickRes: Unit, buttonTextRes: Int, landscape: Boolean ->
        Spacer(modifier = Modifier.size(15.dp))
        androidx.compose.material3.Card(
            modifier = if (landscape) Modifier.padding(10.dp) else Modifier.padding(
                start = 5.dp,
                top = 10.dp
            ),
            colors = CardDefaults.cardColors(containerColor = Color.Black)
        ) {
            if (landscape) {
                imageHeight = (LocalConfiguration.current.screenHeightDp / 2.0)
            }
            Box {
                Image(
                    modifier = if (landscape) Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .align(Alignment.Center)
                        .clip(RoundedCornerShape(10.dp))
                    else Modifier
                        .fillMaxWidth()
                        .height((imageHeight / 1.5).dp),
                    painter = painterResource(id = imageRes),
                    contentDescription = null,
                    contentScale = ContentScale.FillWidth,
                    colorFilter = if (landscape) ColorFilter.tint(
                        Color.Black.copy(alpha = 0.5f),
                        BlendMode.Darken
                    ) else null
                )
                Box(
                    modifier = Modifier
                        .padding(top = if (landscape) (imageHeight / 4).dp else (imageHeight / 6).dp)
                        .background(
                            color = if (landscape) Color.Transparent else Color.Black.copy(
                                alpha = 0.5f
                            )
                        )
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                ) {
                    androidx.compose.material.Text(
                        modifier = Modifier.align(Alignment.Center),
                        text = stringResource(id = textRes),
                        color = androidx.compose.material.MaterialTheme.colors.secondary,
                        style = androidx.compose.material.MaterialTheme.typography.body1.copy(
                            fontSize = if (landscape) 34.sp else 24.sp
                        )
                    )
                }
                Button(
                    modifier = Modifier
                        .fillMaxWidth(if (landscape) 0.2f else 0.4f)
                        .padding(bottom = 32.dp)
                        .align(Alignment.BottomCenter),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = androidx.compose.material.MaterialTheme.colors.primary
                    ),
                    onClick = {
                        clickRes
                    }
                ) {
                    androidx.compose.material.Text(
                        text = stringResource(id = buttonTextRes),
                        color = androidx.compose.material.MaterialTheme.colors.secondary
                    )
                }
            }
        }
    }


    val handlePcClick = {
        if (userData.value.currentPlan != "Basic") {
            if (!globalInstance.paymentStatus) {
                val launchPc = {
                    AnalyticsManager.desktopStreamButton()
                    globalInstance.gameStream = true
                    globalInstance.traceGameStream = FirebasePerformance
                        .getInstance()
                        .newTrace("game_stream")
                    globalInstance.traceGameStream.start()
                            val intent = Intent(currentAct,AppView::class.java)
                            intent.putExtra("connect" , "socket")
                            currentAct.startActivity(intent)
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
    val profileCard: @Composable (Boolean) -> Unit = { landscape: Boolean ->
        if(userData.value.currentPlan != "Basic") {
            val todayDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM dd,yyyy"))
            val convertedDate = if(userData.value.renewDate != "" && userData.value.renewDate != null) convertDate(userData.value.renewDate) else null
            val renewDate = if(convertedDate != null) formatDate(convertedDate) else ""
            timeLeft = if(userData.value.totalTimeMonth != 0) userData.value.totalTimeMonth - userData.value.timeUsedMonth else 0
            timeLeftP = if(userData.value.totalTimeMonth != 0) 100 - ((timeLeft/userData.value.totalTimeMonth)/60) * 100 else 0
            timeLeftString = "${timeLeft/60}H ${timeLeft.absoluteValue%60}M"
            daysLeft = if(renewDate != "") {
                dayBetween(todayDate, renewDate, "MM dd,yyyy")
            } else {
                -1
            }

            var plan = ""
            for(i in globalInstance.androidData.pricing.indices) {
                for (j in globalInstance.androidData.pricing[i].items.indices) {
                    if(globalInstance.androidData.pricing[i].items[j].code != "TopUp" && userData.value.currentPlan.lowercase() == globalInstance.androidData.pricing[i].items[j].userPlan.lowercase()) {
                        plan = globalInstance.androidData.pricing[i].items[j].display
                    }
                }
            }

            if(plan == "") {
                plan = userData.value.currentPlan
            }

            if(daysLeft>=0) {
                daysLeftP = 100 - ((30 - daysLeft.toDouble()) / 30 * 100)

                Spacer(modifier = Modifier.size(20.dp))
                Row(verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(start = 10.dp)
                        .clickable {
                            AnalyticsManager.libraryAccountButton()
                            navigate(DrawerScreens.Account.route)
                        }) {
                    androidx.compose.material.Text(
                        text = "Profile",
                        style = mainTitle.copy(fontWeight = FontWeight.Normal),
                    )
                    androidx.compose.material.Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        contentDescription = "",
                        tint = androidx.compose.material.MaterialTheme.colors.secondary
                    )
                }

                androidx.compose.material3.Card(
                    Modifier
                        .padding(start = 5.dp, top = 10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Black/*light_grey*/),
                ) {
                    /*androidx.compose.material3.Text(
                        text = stringResource(id = R.string.plan) + " : " + userData.currentPlan,
                        textAlign = TextAlign.Start,
                        modifier = Modifier
                            .padding(start = 15.dp, top = 15.dp)
                            .align(Alignment.Start),
                        color = MaterialTheme.colors.secondary,
                        style = mainTitle.copy(
                            fontSize = 18.sp,
                            textAlign = TextAlign.Start,
                            fontWeight = FontWeight.Light,

                            ),
                    )*/
                    ConstraintLayout {
                        val (row, text) = createRefs()

                        Row(
                            modifier = Modifier
                                .fillMaxWidth(1f)
                                .constrainAs(row) {
                                    start.linkTo(parent.start)
                                    end.linkTo(parent.end)
                                    top.linkTo(parent.top)
                                }
                        ) {
                            if (landscape) {
                                Spacer(modifier = Modifier.weight(0.25f))
                            }
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy((0).dp),
                                modifier = if (landscape) Modifier.weight(0.25f) else Modifier.weight(
                                    0.5f
                                )
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(Color(0x00ffffff))
                                        .height(IntrinsicSize.Min),
                                ) {
                                    Speedometer(
                                        timeLeftP,
                                        "Time Left",
                                        timeLeftString/*timeLeft.toString()*/
                                    )
                                }
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy((0).dp),
                                modifier = if (landscape) Modifier.weight(0.25f) else Modifier.weight(
                                    0.5f
                                )
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(Color(0x00ffffff))
                                        .height(IntrinsicSize.Min)
                                ) {
                                    if (daysLeft.toInt() == 0) {
                                        Speedometer(
                                            daysLeftP.toInt(),
                                            "Expiring Today",
                                            daysLeft.toString()
                                        )
                                    } else {
                                        Speedometer(
                                            daysLeftP.toInt(),
                                            "Days Left",
                                            daysLeft.toString()
                                        )
                                    }
                                }
                            }
                            if (landscape) {
                                Spacer(modifier = Modifier.weight(0.25f))
                            }
                        }
                        /*Text(
                            text = stringResource(id = R.string.resolution) + " : " + userData.resolution,
                            textAlign = TextAlign.Start,
                            modifier = Modifier
                                .padding(start = 15.dp, bottom = 15.dp),
                            color = MaterialTheme.colors.secondary,
                            style = mainTitle.copy(
                                fontSize = 16.sp,
                                textAlign = TextAlign.Start,
                                fontWeight = FontWeight.Light,

                                ),
                        )*/
                        androidx.compose.material.Text(
                            text = "${stringResource(id = R.string.plan)} : $plan",
                            textAlign = TextAlign.Start,
                            modifier = Modifier
                                .constrainAs(text) {
                                    start.linkTo(parent.start)
                                    bottom.linkTo(row.bottom)
                                }
                                .padding(start = 15.dp, bottom = if (landscape) 50.dp else 15.dp),
                            color = androidx.compose.material.MaterialTheme.colors.secondary,
                            style = subtitle.copy(
                                fontSize = 16.sp,
                                textAlign = TextAlign.Start,
                                fontWeight = FontWeight.Light
                            ),
                        )
                    }
                }
            }
            else {
                daysLeft = -1
                daysLeftP = 0.0

                showCard(R.drawable.onhold_banner, R.string.plan_hold_text, navigate(DrawerScreens.Report.route), R.string.plan_hold_button, landscape)
            }
        }
        else {
            showCard(R.drawable.basic_banner, R.string.basic_banner_text,   navigate(DrawerScreens.Pricing.route) , R.string.checkout_plans_button, landscape)
        }
    }


    Column(
        Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(dark_grey)
    ) {
        val displayMetrics: DisplayMetrics = currentAct.resources.displayMetrics
        val screenWidth = (displayMetrics.widthPixels / displayMetrics.density).toInt()
        val screenHeight = (displayMetrics.heightPixels / displayMetrics.density).toInt()

        /*if(refreshScreen.value) {
            currentAct.makeToast("refreshing user info")
            subRefreshScreen(!refreshScreen.value)
        } else {
            currentAct.makeToast("Refreshing user info")
        }*/

        if(screenWidth < 600) {
            currentAct.requestedOrientation= ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            PortraitLayoutLibrary(imageHeight = imageHeight, navigate = navigate, handlePcClick = handlePcClick, profileCard)
        } else {
            val modifierRow = Modifier.weight(if(screenHeight < 1200) 0.5f else 0.55f, true)
            val modifierCard = Modifier.weight(if(screenHeight < 1200) 0.5f else 0.45f, true)
            currentAct.requestedOrientation= ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            LandscapeLayoutLibrary(modifierRow = modifierRow, modifierCard = modifierCard,
                navigate = navigate, handlePcClick = handlePcClick, profileCard)
        }
    }




//        Column(
//            Modifier
//                .fillMaxWidth()
//                .fillMaxHeight()
//                .background(Color.Black)) {
//            Box(
//                modifier = Modifier
//                    .clickable {
//                        handlePcClick()
//                    }
//                    .padding(top = 10.dp, start = 10.dp, end = 10.dp)
//                    .fillMaxWidth()
//                    .aspectRatio(1.8f)
//                    .border(1.dp, Color.White, RoundedCornerShape(35.dp))
//                    .background(Color.DarkGray, RoundedCornerShape(35.dp)),
//                contentAlignment = Alignment.Center
//            ) {
//
//                Image(
//                    painter = painterResource(id = R.drawable.windows),
//                    contentDescription = null,
//                    contentScale = ContentScale.FillBounds,
//                    modifier = Modifier.clip(RoundedCornerShape(35.dp))
//                )
//                Box(
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .background(
//                            brush = Brush.verticalGradient(colors = gradientColors),
//                            RoundedCornerShape(35.dp)
//                        )
//                )
//
//
//                Column {
//                    Icon(
//                        modifier = Modifier
//                            .size(50.dp)
//                            .align(Alignment.CenterHorizontally),
//                        painter = painterResource(id = R.drawable.play_circle),
//                        contentDescription = "Launch PC Mode",
//                        tint = Color.White
//                    )
//                    Text(
//                        fontSize = 16.sp,
//                        text = "Connect",
//                        style = MaterialTheme.typography.bodyLarge,
//                        color = Color.White,
//                        modifier = Modifier.padding(top = 8.dp)
//                    )
//                }
//
//            }
//
//            Text(
//                text = "Quick Launch Games",
//                style = TextStyle(
//                    fontSize = 16.sp,
//                    fontWeight = FontWeight.Bold,
//                    color = Color.Black,
//                    textAlign = TextAlign.Start
//                ), color = Color.White,
//                modifier = Modifier.padding(start = 20.dp, top = 30.dp)
//            )
//        }



    if (showUpgradePlan.value) {
        CustomDialog(openDialogCustom = (showUpgradePlan.value), onDismiss = { /*TODO*/ }) {
            androidx.compose.material3.Card(
                modifier = Modifier.padding(start = 5.dp, top = 10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black)
            ) {
                Column(
                    modifier = Modifier
                        .background(dark_grey)
                ) {
                    androidx.compose.material.Text(
                        text = "Please upgrade your plan",
                        color = androidx.compose.material.MaterialTheme.colors.secondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(vertical = 15.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                            .background(
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
                    .background(dark_grey)
                    .clip(
                        RoundedCornerShape(10.dp)
                    )
            ) {
                Loading("testtt")
            }
        }

    }
    if(showUpdateDialog.value) {
        CustomDialog(openDialogCustom = showUpdateDialog.value, onDismiss = { /*TODO*/ }) {
            androidx.compose.material3.Card(
                modifier = Modifier.padding(start = 5.dp, top = 10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black)
            ) {
                Column(
                    modifier = Modifier
                        .background(dark_grey)
                ) {
                    androidx.compose.material.Text(
                        text = stringResource(id = R.string.update_app),
                        color = androidx.compose.material.MaterialTheme.colors.secondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(vertical = 15.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                            .background(
                                androidx.compose.material.MaterialTheme.colors.primary.copy(
                                    alpha = 0.5f
                                )
                            ),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = {
                            currentAct.startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://play.google.com/store/apps/details?id=com.antcloud.app")
                                )
                            )
                            showUpdateDialog.value = false
                        }) {
                            androidx.compose.material.Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = "",
                                tint = androidx.compose.material.MaterialTheme.colors.secondary,
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
    if(showMaintenanceDialog.value) {
        CustomDialog(openDialogCustom = showMaintenanceDialog.value, onDismiss = { /*TODO*/ }) {
            androidx.compose.material3.Card(
                modifier = Modifier
                    .padding(start = 5.dp, top = 10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black)
            ) {
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .background(dark_grey)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp, bottom = 20.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = globalInstance.remoteAppMessage[0].dialogTitle,
                            style = heading.copy(
                                fontFamily = Play,
                                textDecoration = TextDecoration.Underline
                            )
                        )
                    }
                    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
                    if (globalInstance.remoteAppMessage[0].useAnnotation && globalInstance.remoteAppMessage[0].annotatedMessage.isNotEmpty()) {
                        val annotatedString = buildAnnotatedString {
                            globalInstance.remoteAppMessage[0].annotatedMessage.forEachIndexed { index, annotatedMessage ->
                                if (annotatedMessage.isClickable) {
                                    pushStringAnnotation(
                                        tag = index.toString(),
                                        annotation = annotatedMessage.url
                                    )
                                    withStyle(
                                        style = SpanStyle(
                                            color = Color.Yellow,
                                            textDecoration = TextDecoration.Underline
                                        )
                                    ) {
                                        append(annotatedMessage.message)
                                    }
                                    pop()
                                } else {
                                    withStyle(style = SpanStyle(color = Color.White)) {
                                        append(annotatedMessage.message)
                                    }
                                }
                                append(" ")
                            }
                        }

                        androidx.compose.material.Text(
                            text = annotatedString,
                            modifier = Modifier
                                .pointerInput(Unit) {
                                    detectTapGestures { offset ->
                                        val layoutResult =
                                            textLayoutResult ?: return@detectTapGestures
                                        val position = layoutResult.getOffsetForPosition(offset)
                                        annotatedString
                                            .getStringAnnotations(start = position, end = position)
                                            .forEach { annotation ->
                                                if (annotation.tag.isNotEmpty()) {
                                                    val intent = Intent(
                                                        Intent.ACTION_VIEW,
                                                        Uri.parse(annotation.item)
                                                    )
                                                    currentAct.startActivity(intent)
                                                }
                                            }
                                    }
                                }
                                .padding(horizontal = 10.dp),
                            style = subtitle.copy(
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Light
                            ),
                            onTextLayout = { result ->
                                textLayoutResult = result
                            }
                        )
                    } else {
                        Text(
                            text = globalInstance.remoteAppMessage[0].messageText,
                            modifier = Modifier
                                .fillMaxWidth(),
                            color = androidx.compose.material.MaterialTheme.colors.secondary,
                            style = subtitle.copy(
                                fontWeight = FontWeight.Light,
                                textAlign = TextAlign.Center
                            )
                        )
                    }
                    if (globalInstance.remoteAppMessage[0].isDialogDismissible) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp)
                                .background(
                                    androidx.compose.material.MaterialTheme.colors.primary.copy(
                                        alpha = 0.5f
                                    )
                                ),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = {
                                /*currentAct.startActivity(
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("https://play.google.com/store/apps/details?id=com.antcloud.app")
                                    )
                                )*/
                                //showMaintenanceDialog.value = false
                                viewModel.updateShowMaintenanceDialog(false)
                            }) {
                                androidx.compose.material.Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = "",
                                    tint = androidx.compose.material.MaterialTheme.colors.secondary,
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
                    } else {
                        Spacer(Modifier.size(25.dp))
                    }
                }
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


fun formatDate(date: Date): String {
    return SimpleDateFormat("MM dd,yyyy").format(date)
}

fun convertDate(dateString: String): Date? {
    return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").parse(dateString)
}

fun dayBetween(date1: String?, date2: String?, pattern: String?): Long {
    val sdf = SimpleDateFormat(pattern, Locale.ENGLISH)
    var Date1: Date? = null
    var Date2: Date? = null
    try {
        Date1 = sdf.parse(date1!!)
        Date2 = sdf.parse(date2!!)
    } catch (e: Exception) {
        //e.printStackTrace()
        currentAct.makeToast("Error 802 : Something Went Wrong")
    }
    return (Date2!!.time - Date1!!.time) / (24 * 60 * 60 * 1000)
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
        modifier = Modifier
            .wrapContentHeight()
            .padding(top = if (orientationLandscape) 0.dp else 10.dp)) {
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
            Modifier
                .padding(vertical = 2.dp, horizontal = 5.dp)
                .clickable {
                    // AnalyticsManager.gameButton(game.gameId)
                    onClick()
                }
        else modifier
            .wrapContentSize()
            .padding(vertical = 10.dp, horizontal = 7.dp)
            .clickable {
                    AnalyticsManager.gameButton(game.gameId)
                onClick()
            }) {
        globalInstance.imageLoading = true
        globalInstance.traceImageLoading =  FirebasePerformance.getInstance().newTrace("image_loading")
        globalInstance.traceImageLoading.start()
        AsyncImages(
            url = if (!expand) "https://antplay-gamedata.s3.ap-south-1.amazonaws.com/${game.gameId}.jpg"
            else { if(orientationLandscape) "https://antplay-gamedata.s3.ap-south-1.amazonaws.com/background_landscape.jpg" else "https://antplay-gamedata.s3.ap-south-1.amazonaws.com/background.jpg"},
            modifier = if(orientationLandscape)
                Modifier
                    .fillMaxHeight(0.5f)
                    .clip(RoundedCornerShape(6.dp))
            else
                Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .width(213.dp)
                    .height(120.dp),
            orientationLandscape = orientationLandscape)

        if(!orientationLandscape){
            Row(Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(Brush.verticalGradient(colors = listOf(Color.Transparent, Color.Black)))
                .width(213.dp)
                .height(120.dp)) {}
            androidx.compose.material.Text(
                text = if (!expand) game.name else "View More",
                style = androidx.compose.material.MaterialTheme.typography.caption.copy(fontSize = 16.sp),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 10.dp, bottom = 2.dp),
                color = androidx.compose.material.MaterialTheme.colors.secondary.copy(alpha = .8f)
            )
        }
    }

}


@Composable
fun PortraitLayoutLibrary(imageHeight: Double, navigate: (String) -> Unit, handlePcClick: () -> Unit, profileCard: @Composable (Boolean) -> Unit) {
    //imageHeight = (LocalConfiguration.current.screenWidthDp / 1.5)
    Box(Modifier
        .clickable {
            handlePcClick()
        }
        .padding(top = 0.dp)
        .clip(RoundedCornerShape(5.dp))
    ) {
        Image(
            modifier = Modifier
                .fillMaxWidth()
                //.height(200.dp),
                .height(imageHeight.dp),
            painter = painterResource(id = R.drawable.banner_image),
            contentDescription = null,
            contentScale = ContentScale.FillBounds)

        if(globalInstance.remoteAppMessage.isNotEmpty() && globalInstance.remoteAppMessage[0].showMessage){
            Box(modifier = Modifier
                .fillMaxWidth()
                .background(androidx.compose.material.MaterialTheme.colors.background)
            ){
                var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
                if(globalInstance.remoteAppMessage[0].useAnnotation && globalInstance.remoteAppMessage[0].annotatedMessage.isNotEmpty()) {
                    val annotatedString = buildAnnotatedString {
                        globalInstance.remoteAppMessage[0].annotatedMessage.forEachIndexed { index, annotatedMessage ->
                            if(annotatedMessage.isClickable) {
                                pushStringAnnotation(tag = index.toString(), annotation = annotatedMessage.url)
                                withStyle(style = SpanStyle(color = Color.Yellow, textDecoration = TextDecoration.Underline)) {
                                    append(annotatedMessage.message)
                                }
                                pop()
                            } else {
                                withStyle(style = SpanStyle(color = Color.White)) {
                                    append(annotatedMessage.message)
                                }
                            }
                            append(" ")
                        }
                    }

                    androidx.compose.material.Text(
                        text = annotatedString,
                        modifier = Modifier.pointerInput(Unit) {
                            detectTapGestures { offset ->
                                val layoutResult = textLayoutResult ?: return@detectTapGestures
                                val position = layoutResult.getOffsetForPosition(offset)
                                annotatedString.getStringAnnotations(
                                    start = position,
                                    end = position
                                ).forEach { annotation ->
                                    if (annotation.tag.isNotEmpty()) {
                                        val intent =
                                            Intent(Intent.ACTION_VIEW, Uri.parse(annotation.item))
                                        currentAct.startActivity(intent)
                                    }
                                }
                            }
                        },
                        onTextLayout = { result ->
                            textLayoutResult = result
                        }
                    )
                }
                else {
                    Text(
                        text = globalInstance.remoteAppMessage[0].messageText,
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter),
                        color = androidx.compose.material.MaterialTheme.colors.secondary,
                        style = subtitle.copy(
                            fontWeight = FontWeight.Light,
                            textAlign = TextAlign.Center
                        )
                    )
                }
            }
        }
        androidx.compose.material.Text(
//            text = stringResource(id = R.string.pc),
            text = "Connect",
            //textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(top = (imageHeight / 4).dp)
                .align(Alignment.TopCenter),
            color = androidx.compose.material.MaterialTheme.colors.secondary,
            style = heading.copy(
//                    textAlign = TextAlign.Center,
                fontWeight = FontWeight.Light,
            )
        )
        androidx.compose.material.Icon(
            modifier = Modifier
                .padding(top = (imageHeight / 2.2).dp)
                .size(35.dp)
                .align(Alignment.TopCenter),
            imageVector = Icons.Filled.PowerSettingsNew,
            contentDescription = "",
            tint = androidx.compose.material.MaterialTheme.colors.secondary)
    }

    if(globalInstance.remoteGamesMaintenance[0].showGames){
        GamesRow(
            navigate,
            orientationLandscape = false,
            games = when (GlobalData.getInstance().ourGames[0].games.size > 6) {
                true -> GlobalData.getInstance().ourGames[0].games.subList(0, 6)
                    .toList()

                false -> GlobalData.getInstance().ourGames[0].games
            }
        )


    }
    profileCard(false)
}

@Composable
fun LandscapeLayoutLibrary(modifierRow: Modifier, modifierCard: Modifier,
                           navigate: (String) -> Unit, handlePcClick: () -> Unit, profileCard: @Composable (Boolean) -> Unit) {
    Row(modifierRow) {
        Box(Modifier
            .weight(0.5f, true)
            .clickable { handlePcClick() }
            .clip(RoundedCornerShape(5.dp))
        ) {
            val imageHeight = (LocalConfiguration.current.screenHeightDp / 2.0)
            Image(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                painter = painterResource(id = R.drawable.banner_image),
                contentDescription = null,
                contentScale = ContentScale.Crop //original is FillHeight
            )
            if (globalInstance.remoteAppMessage.isNotEmpty() && globalInstance.remoteAppMessage[0].showMessage) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(androidx.compose.material.MaterialTheme.colors.background)
                ) {
                    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
                    if (globalInstance.remoteAppMessage[0].useAnnotation && globalInstance.remoteAppMessage[0].annotatedMessage.isNotEmpty()) {
                        val annotatedString = buildAnnotatedString {
                            globalInstance.remoteAppMessage[0].annotatedMessage.forEachIndexed { index, annotatedMessage ->
                                if (annotatedMessage.isClickable) {
                                    pushStringAnnotation(
                                        tag = index.toString(),
                                        annotation = annotatedMessage.url
                                    )
                                    withStyle(
                                        style = SpanStyle(
                                            color = Color.Yellow,
                                            textDecoration = TextDecoration.Underline
                                        )
                                    ) {
                                        append(annotatedMessage.message)
                                    }
                                    pop()
                                } else {
                                    withStyle(style = SpanStyle(color = Color.White)) {
                                        append(annotatedMessage.message)
                                    }
                                }
                                append(" ")
                            }
                        }

                        androidx.compose.material.Text(
                            text = annotatedString,
                            modifier = Modifier.pointerInput(Unit) {
                                detectTapGestures { offset ->
                                    val layoutResult =
                                        textLayoutResult ?: return@detectTapGestures
                                    val position = layoutResult.getOffsetForPosition(offset)
                                    annotatedString.getStringAnnotations(
                                        start = position,
                                        end = position
                                    ).forEach { annotation ->
                                        if (annotation.tag.isNotEmpty()) {
                                            val intent = Intent(
                                                Intent.ACTION_VIEW,
                                                Uri.parse(annotation.item)
                                            )
                                            currentAct.startActivity(intent)
                                        }
                                    }
                                }
                            },
                            onTextLayout = { result ->
                                textLayoutResult = result
                            }
                        )
                    } else {
                        Text(
                            text = globalInstance.remoteAppMessage[0].messageText,
                            modifier = Modifier
                                .align(Alignment.TopCenter),
                            color = androidx.compose.material.MaterialTheme.colors.secondary,
                            style = subtitle.copy(
                                fontWeight = FontWeight.Light,
                                textAlign = TextAlign.Center,
                                background = androidx.compose.material.MaterialTheme.colors.background
                            )
                        )
                    }
                }
            }
            androidx.compose.material.Text(
                text = "Connect",
                //textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(top = (imageHeight / 3.8).dp)
                    .align(Alignment.TopCenter),
                color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary,
                style = heading.copy(
                    fontWeight = FontWeight.Light
                )
            )
            androidx.compose.material.Icon(
                modifier = Modifier
                    .padding(top = (imageHeight / 1.9).dp)
                    .size(35.dp)
                    .align(Alignment.TopCenter),
                imageVector = Icons.Filled.PowerSettingsNew,
                contentDescription = "",
                tint = androidx.compose.material.MaterialTheme.colors.secondary
            )
        }

        if (globalInstance.remoteGamesMaintenance[0].showGames) {
            Column(
                modifier = Modifier
                    .weight(0.5f, true)
                    .padding(start = 10.dp)
            ) {
                GamesRow(
                    navigate,
                    orientationLandscape = true,
                    games = if (GlobalData.getInstance().ourGames.isEmpty()) {
                        currentAct.makeToast("Something Went Wrong")
                        listOf()
                    } else {
                        when (GlobalData.getInstance().ourGames[0].games.size > 3) {
                            true -> GlobalData.getInstance().ourGames[0].games.subList(0, 3)
                                .toList()

                            false -> GlobalData.getInstance().ourGames[0].games
                        }
                    },

                    )
            }
        }
    }
    Column(modifierCard) {
        profileCard(true)
    }
}

@Composable
fun Speedometer(
    progress: Int,
    title : String ,
    centerText : String
) {
    val arcDegrees = 235
    val startArcAngle = 153
    val startStepAngle = 90
    val numberOfMarkers = 90
    val degreesMarkerStep = arcDegrees / numberOfMarkers
    // val textMeasurer = rememberTextMeasurer()
    val textMeasurer = rememberTextMeasurer()

    var textLayoutResult by remember{ mutableStateOf<TextLayoutResult?>(null) }
    var textLayoutResult2 by remember{ mutableStateOf<TextLayoutResult?>(null) }

    val style = TextStyle(
        fontSize =18.sp,
        color = androidx.compose.material.MaterialTheme.colors.secondary,
    )
    val textToDraw = "1:05 hours"

    val density = LocalDensity.current

    val textFontSize = with(density) { 20.dp.toPx() }
    val fontPadding = with(density) { 5.dp.toPx() }

    val context = LocalContext.current
    val textPaint = remember {
        android.graphics.Paint().apply {
            color = (0xFFFFFFFF).toInt()
            textSize = textFontSize
            textAlign = android.graphics.Paint.Align.CENTER
            typeface = ResourcesCompat.getFont(context, R.font.play_regular)
        }
    }
    val progressValue = when {
        progress < 0 -> {
            0
        }
        progress > 100 -> {
            100
        }
        else -> {
            progress
        }
    }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)
                textLayoutResult = textMeasurer.measure(
                    text = centerText,
                    style = TextStyle(fontSize = 20.sp,
                        fontFamily = ResourcesCompat
                            .getFont(context, R.font.play_regular)
                            ?.let {
                                FontFamily(it)
                            })
                )
                textLayoutResult2 = textMeasurer.measure(
                    text = title,
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontFamily = ResourcesCompat
                            .getFont(context, R.font.play_regular)
                            ?.let {
                                FontFamily(
                                    it
                                )
                            })
                )
                layout(placeable.width, placeable.height) {
                    placeable.placeRelative(0, 0)
                }
            }
            .aspectRatio(1f), onDraw = {
            val w = drawContext.size.width
            val h = drawContext.size.height

            val center = Offset(w / 2, h / 2)
            //val textY = center.y + textFontSize/2 + fontPadding

            // Drawing Center Arc background
            val (mainColor, secondaryColor) = primaryGreen to Color.White
//            val (mainColor, secondaryColor) = Purple40 to Color(0xffddcadd)
            /*when {
                progress < 20 -> // Red
                    Purple40 to Color(0xFFFFE0B2)
                progress < 40 -> // Orange
                    Purple40 to Color(0xFFFFE0B2)
                else -> // Green
                    Purple40 to Color(0xFFFFE0B2)

            }*/
            val centerArcSize = Size(4*w / 5f, 4*h / 5f)
            val centerArcStroke = Stroke(20f, 0f, StrokeCap.Round)
            val quarterOffset = Offset(center.x - centerArcSize.width/2, center.y - centerArcSize.height/2)
            drawArc(
                secondaryColor,
                startArcAngle.toFloat(),
                arcDegrees.toFloat(),
                false,
                quarterOffset,
                centerArcSize,
                style = centerArcStroke
            )
            // Drawing Center Arc progress
            drawArc(
                mainColor,
                startArcAngle.toFloat(),
                (arcDegrees * progressValue/100).toFloat(),
                false,
                quarterOffset,
                centerArcSize,
                style = centerArcStroke
            )
            //drawPoints(arrayOf(Offset(center.x, center.y)).toList(), PointMode.Points, color = Color.Green, strokeWidth = 10f, cap = StrokeCap.Round)
            //drawText(centerText, center.x+textFontSize/6, textY, textPaint)
            //drawText(title, center.x+textFontSize/6, textY*1.45f, textPaint)

            textLayoutResult?.let {
                drawText (
                    textLayoutResult = it,
                    color = secondaryColor,
                    topLeft = Offset(
                        x= center.x - 0.5f * it.size.width,
                        y = center.y - 0.5f * it.size.height
                    )
                )
            }
            textLayoutResult2?.let {
                drawText(
                    textLayoutResult = it,
                    color = secondaryColor,
                    topLeft = Offset(
                        x = center.x - 0.5f * it.size.width,
                        y = center.y + 0.1f*centerArcSize.height.dp.toPx() - 0.5f * it.size.height
                    )
                )
            }
        }
    )
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








