package com.antcloud.app.screen.auth

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.util.DisplayMetrics
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.text.isDigitsOnly
import androidx.hilt.navigation.compose.hiltViewModel

import com.antcloud.app.data.Screen
import com.antcloud.app.theme.BlueGradient
import com.antcloud.app.theme.PinkGradient
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.VerticalPager
import com.google.accompanist.pager.rememberPagerState
import com.google.firebase.perf.FirebasePerformance

import com.antcloud.app.components.isEmailValid
import com.antcloud.app.components.isPhoneNumberValid
import com.antcloud.app.components.makeToast
import com.antcloud.app.data.CheckUserInDB
import com.antcloud.app.data.PhoneOtpReq
import com.antcloud.app.theme.subtitle
import com.antcloud.app.theme.titleText

import com.antcloud.app.viewmodel.AuthenticateViewModel
import com.antcloud.app.activity.SplashActivity
import com.antcloud.app.common.AppUtils.Companion.gradientColors
import com.antcloud.app.common.AppUtils
import com.antcloud.app.logic.auth.LoginErrors
import com.antcloud.app.screen.account.globalInstance
import com.antcloud.app.R


@OptIn(ExperimentalPagerApi::class)
@Composable
fun MainScreen(activity: SplashActivity, initialState : Int, apiResp: Boolean) {
    val viewModel: AuthenticateViewModel = hiltViewModel()
    var isScrollEnabled by remember { mutableStateOf(true) }
    viewModel.updatePagerState(initialState)
    var pagerState by remember { mutableIntStateOf(viewModel.pagerInitialState) }

    viewModel.subPagerInitialState = {
        pagerState = it
    }

    val pagerState1 = rememberPagerState(initialPage = viewModel.pagerInitialState)
    val screenList = ArrayList<Screen>()
    screenList.add(Screen("splash"))
    screenList.add(Screen("welcome"))
    if(pagerState==1 || apiResp)
        isScrollEnabled =  false

    Log.i("test" ,"wefwf")
    Box(modifier = Modifier.fillMaxSize()) {
        VerticalPager(count = screenList.size, state = pagerState1 , userScrollEnabled = isScrollEnabled) {
            PagerScreen(it , activity,apiResp ,viewModel)
        }
    }
}

@Composable
fun PagerScreen(it : Int , activity: SplashActivity, apiResp: Boolean, viewModel: AuthenticateViewModel) {
    if(it==0) {
        Box(modifier = Modifier.fillMaxSize()
            .background(brush = Brush.horizontalGradient(colors = gradientColors))) {
            Image(modifier = Modifier.align(Alignment.Center),
                painter = painterResource(id = R.drawable.logo_text),
                contentDescription = null,
                contentScale = ContentScale.FillBounds)
            if(!apiResp) {
                Icon(
                    modifier = Modifier.wrapContentSize().align(Alignment.BottomCenter)
                        .padding(bottom = 40.dp),
                    painter = painterResource(id = R.drawable.top_arrow),
                    contentDescription = "Launch PC Mode",
                    tint = Color.White
                )
                Text(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 10.dp),
                    text = "Swipe up to Continue",
                    style = subtitle.copy(fontSize = 14.sp)
                )
            }
        }
    }
    if(it==1){
        viewModel.updatePagerState(1)

        WelcomeScreen(activity = activity, viewModel)
    }
}
@SuppressLint("SuspiciousIndentation")
@Composable
private fun WelcomeScreen(activity: SplashActivity, viewModel: AuthenticateViewModel) {
    var emailMobile by remember { mutableStateOf("") }
    var progressBarVisible by remember { mutableStateOf(false) }

    val postPhoneOtpState = viewModel.postPhoneOtpState.value
    var checkUserInDBState = viewModel.checkUserInDBState.value

    when(checkUserInDBState.success){
        1->{
            if(emailMobile.isDigitsOnly()) {
                LaunchedEffect(Unit) {
                    if(emailMobile.isPhoneNumberValid()) {
                        globalInstance.traceGenerateOTPApi =  FirebasePerformance.getInstance().newTrace("generate_otp_btn")
                        globalInstance.traceGenerateOTPApi.start()
                        viewModel.updateLoginLoadingText("Sending the OTP.....")
                        val dataModel = PhoneOtpReq("+91${emailMobile.trim()}", false)
                        viewModel.getPostPhoneOtpData(dataModel, "login")
                    }
                }
            }
            else {
                LaunchedEffect(Unit) {
                    progressBarVisible = false
                    AppUtils.navigateLoginScreen(activity, emailMobile, "email")
                }
            }
        }
        0->{
            LaunchedEffect(Unit) {
                progressBarVisible = false
                AppUtils.navigateSignupScreen(activity,emailMobile)
            }
        }
    }
    when(postPhoneOtpState.success){
        1->{
            LaunchedEffect(Unit) {
                globalInstance.traceGenerateOTPApi.stop()
                progressBarVisible = false
                AppUtils.navigateLoginScreen(activity, emailMobile,"otp")
            }
        }
        0->{
            LaunchedEffect(Unit) {
                globalInstance.traceGenerateOTPApi.stop()
                progressBarVisible = false
                when (postPhoneOtpState.errorCode) {
                    400 -> {
                        val message = postPhoneOtpState.error
                        if (message.contains("Error 807"))
                            activity.makeToast(message)
                        else if (message.equals("This number does not exist!")) {
                            viewModel.updateLoginError(LoginErrors.WRONG_NUMBER)
                            activity.makeToast("Error 104C : No such user found!")
                        } else {
                            viewModel.updateLoginError(LoginErrors.TRY_LATER)
                            activity.makeToast("Error 104C1 : Something went wrong. Please try again after sometime.")
                        }
                    }

                    else -> {
                        if (postPhoneOtpState.error != "")
                            activity.makeToast(postPhoneOtpState.error)
                    }
                }
            }
        }
    }

    var isError by remember { mutableStateOf(false) }
    var textVisible by remember { mutableStateOf(false) }

    val displayMetrics: DisplayMetrics = activity.resources.displayMetrics
    val screenWidth = (displayMetrics.widthPixels / displayMetrics.density).toInt()
    val landscape = screenWidth >= 600

    if(!landscape){
    Column(modifier = Modifier
        .fillMaxSize()
        .background(brush = Brush.horizontalGradient(colors = gradientColors)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally) {

        var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
        if (globalInstance.remoteAppMessage.isNotEmpty() && globalInstance.remoteAppMessage[0].showMessage) {
            Box(modifier = Modifier.fillMaxWidth().padding(top = 20.dp)/*.background(Black)*/) {
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
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                            .pointerInput(Unit) {
                                detectTapGestures { offset ->
                                    val layoutResult = textLayoutResult ?: return@detectTapGestures
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
                                            currentActivity.startActivity(intent)
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
                }
            }
        }

            Image(
                modifier = Modifier.wrapContentWidth().padding(top = 0.dp),
                painter = painterResource(id = R.drawable.ant_cloud_white_icon),
                contentDescription = null, contentScale = ContentScale.FillBounds
            )

            Text(
                modifier = Modifier,
                text = "Ready to Dive in?",
                style = titleText.copy(fontSize = 16.sp)
            )

            Spacer(modifier = Modifier.height(30.dp))

            var color: Color? = null
            if (!progressBarVisible) {
                color = if (isError)
                    Color.Red
                else
                    Color.White

                Row(
                    modifier = Modifier.fillMaxWidth(0.8f).border(
                        1.dp, color,
                        RoundedCornerShape(16.dp)
                    ).padding(12.dp)
                ) {
                    if (textVisible)
                        Text(
                            modifier = Modifier.padding(start = 8.dp),
                            text = "+91 ",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    BasicTextField(
                        value = emailMobile,
                        onValueChange = { newValue ->
                            emailMobile = newValue
                            if (newValue.isEmpty())
                                textVisible = false
                            if (newValue.isDigitsOnly()) {
                                if (newValue.length == 4)
                                    textVisible = true
                                isError = emailMobile != "" && !emailMobile.isPhoneNumberValid()
                            } else
                                isError = emailMobile != "" && !emailMobile.isEmailValid()

                        },
                        textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
                        cursorBrush = SolidColor(BlueGradient),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        decorationBox = { innerTextField ->
                            Box(Modifier.padding(start = 4.dp)) {
                                if (emailMobile.isEmpty()) {
                                    Text("Enter your Email/Mobile", color = Color.White)
                                }
                                innerTextField()
                            }
                        })
                }


                Spacer(modifier = Modifier.height(20.dp))


                Button(
                    onClick = {
                        if (emailMobile.isDigitsOnly()) {
                            if (emailMobile.isPhoneNumberValid()) {

                                viewModel.updateLoginLoadingText("please wait ....")
                                progressBarVisible = true
                                val dataModel = CheckUserInDB("", "+91${emailMobile.trim()}")
                                viewModel.checkUserInDB(dataModel)
                            } else
                                activity.makeToast("Please enter a valid number")


                        } else {
                            if (emailMobile.isEmailValid()) {
                              //  AppUtils.navigateLoginScreen(activity, emailMobile, "email")
                        viewModel.updateLoginLoadingText("please wait ....")
                        progressBarVisible = true
                        val dataModel = CheckUserInDB(emailMobile.trim(), "")
                        viewModel.checkUserInDB(dataModel)
                            } else
                                activity.makeToast("Please enter a valid Email")

                        }

                    },
                    modifier = Modifier.fillMaxWidth(.8f).padding(top = 20.dp),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                ) {
                    Text(text = "Next", style = subtitle.copy(color = Color.Black))

                }

            } else {
                Text(
                    modifier = Modifier,
                    text = viewModel.loginLoadingTextState,
                    style = titleText.copy(fontSize = 20.sp)
                )
                CircularProgressIndicator(
                    modifier = Modifier.size(60.dp).padding(top = 20.dp),
                    color = PinkGradient,
                    strokeWidth = 10.dp
                )
            }
        }
    }

    else{
        Column(modifier = Modifier
            .fillMaxSize().verticalScroll(rememberScrollState())
            .background(brush = Brush.horizontalGradient(colors = gradientColors)),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {

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
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                val layoutResult = textLayoutResult ?: return@detectTapGestures
                                val position = layoutResult.getOffsetForPosition(offset)
                                annotatedString.getStringAnnotations(start = position, end = position).forEach { annotation ->
                                    if(annotation.tag.isNotEmpty()) {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(annotation.item))
                                        currentActivity.startActivity(intent)
                                    }
                                }
                            }
                        }
                        .padding(horizontal = 10.dp),
                    style = subtitle.copy(
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Light
                    ),
                    onTextLayout = {result ->
                        textLayoutResult = result
                    }
                )
            }

            Image(modifier = Modifier.wrapContentWidth().padding(top=0.dp),
                painter = painterResource(id = R.drawable.ant_cloud_white_icon),
                contentDescription = null, contentScale = ContentScale.FillBounds)

            Text(modifier = Modifier, text = "Ready to Dive in?", style = titleText.copy(fontSize = 16.sp))

            Spacer(modifier = Modifier.height(10.dp))

            var color : Color? = null
            if(!progressBarVisible){
                color = if(isError)
                    Color.Red
                else
                    Color.White

                Row(modifier =  Modifier.
                fillMaxWidth(0.8f).
                border(1.dp, color!!,
                    RoundedCornerShape(16.dp)).padding(horizontal = 15.dp,vertical = 10.dp)){
                    if(textVisible)
                        Text(modifier = Modifier.padding(start=8.dp), text = "+91 " , color = Color.White, fontSize = 14.sp)
                    BasicTextField(
                        value = emailMobile,
                        onValueChange = { newValue ->
                            emailMobile = newValue
                            if(newValue.isEmpty())
                                textVisible = false
                            if(newValue.isDigitsOnly()){
                                if(newValue.length==4)
                                    textVisible = true
                                isError = emailMobile != "" && !emailMobile.isPhoneNumberValid()
                            }
                            else
                                isError = emailMobile != "" && !emailMobile.isEmailValid()

                        },
                        textStyle = TextStyle(color = Color.White, fontSize = 14.sp ),
                        cursorBrush = SolidColor(BlueGradient),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        decorationBox = { innerTextField ->
                            Box(Modifier.padding(start = 4.dp)) {
                                if (emailMobile.isEmpty()) {
                                    Text("Enter your Email/Mobile", color = Color.White)
                                }
                                innerTextField() } })
                }


                Spacer(modifier = Modifier.height(20.dp))


                Button(onClick = {
                    if (emailMobile.isDigitsOnly()) {
                        if (emailMobile.isPhoneNumberValid()) {
                            viewModel.updateLoginLoadingText("please wait ....")
                            progressBarVisible = true
                            val dataModel = CheckUserInDB("", "+91${emailMobile.trim()}")
                            viewModel.checkUserInDB(dataModel)
                        }
                        else
                            activity.makeToast("Please enter a valid number")


                    }
                    else {
                        if(emailMobile.isEmailValid()) {
                            AppUtils.navigateLoginScreen(activity, emailMobile, "email")
//                        viewModel.updateLoginLoadingText("please wait ....")
//                        progressBarVisible = true
//                        val dataModel = CheckUserInDB(emailMobile.trim(), "")
//                        viewModel.checkUserInDB(dataModel)
                        }
                        else
                            activity.makeToast("Please enter a valid Email")

                    }

                },
                    modifier = Modifier.fillMaxWidth(.8f).padding(vertical = 0.dp).height(35.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White) ) {
                    Text(text = "Next", style = subtitle.copy(color = Color.Black))

                }
                Spacer(modifier = Modifier.height(10.dp))

            }
            else{
                Text(modifier = Modifier, text = viewModel.loginLoadingTextState, style = titleText.copy(fontSize = 20.sp))
                CircularProgressIndicator(
                    modifier = Modifier.size(60.dp).padding(top=20.dp),
                    color  = PinkGradient,
                    strokeWidth = 10.dp
                )
            }

        }
    }
    }
