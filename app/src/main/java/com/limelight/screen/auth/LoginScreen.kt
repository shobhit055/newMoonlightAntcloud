package com.limelight.screen.auth

import android.content.Intent
import android.net.Uri
import android.util.DisplayMetrics
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Red
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.perf.FirebasePerformance
import com.limelight.components.OTP_VIEW_TYPE_BOX
import com.limelight.components.OtpView
import com.limelight.components.isEmailValid
import com.limelight.components.makeToast
import com.limelight.data.ForgotPasswordReq
import com.limelight.data.LoginReqData
import com.limelight.data.LoginState
import com.limelight.data.PhoneOtpReq
import com.limelight.data.PhoneVerifyReq
import com.limelight.theme.BlueGradient
import com.limelight.theme.PinkGradient
import com.limelight.theme.mainTitle
import com.limelight.theme.subtitle
import com.limelight.theme.titleText
import com.limelight.viewmodel.AuthenticateViewModel
import com.limelight.R
import com.limelight.activity.LoginActivity
import com.limelight.common.AnalyticsManager
import com.limelight.common.AnalyticsManager.Companion.emailLoginButton
import com.limelight.common.AppUtils
import com.limelight.common.AppUtils.Companion.gradientColors
import com.limelight.components.CustomDialog
import com.limelight.components.Play
import com.limelight.logic.auth.LoginErrors
import com.limelight.screen.account.globalInstance
import com.limelight.theme.heading



lateinit var currentActivity : LoginActivity
@Composable
fun LoginScreen(activity: LoginActivity, emailMobileValue: String, viewModel: AuthenticateViewModel) {
    currentActivity  = activity
    var email by remember { mutableStateOf(emailMobileValue) }
    var password by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(viewModel.type) }
    viewModel.subType = { type = it }
    var otpValue by remember {  mutableStateOf("")}
    var isTextVisible by remember { mutableStateOf(viewModel.textVisible) }
    viewModel.subTextVisible = { isTextVisible = it }
    var successText by remember { mutableStateOf(viewModel.forgotSuccessText) }
    viewModel.subForgotSuccessText = { successText = it }
    var text = ""
    var isPasswordVisible by remember { mutableStateOf(false) }
    val postPhoneState = viewModel.postPhoneState.value
    val loginState = viewModel.loginState.value
    val forgotState = viewModel.forgotState.value
    val showMaintenanceDialog = remember {
        mutableStateOf(false)
    }

    when (loginState.success) {
        1 ->
            LaunchedEffect(Unit) {
                if(globalInstance.emailLoginApi){
                    globalInstance.emailLoginApi =  false
                    globalInstance.traceEmailLoginApi.stop()
                }
                AnalyticsManager.emailLoginSuccess()
                AppUtils.navigateNavScreen(activity, loginState.userData!!)
            }
        0 -> {
            LaunchedEffect(Unit){
                if(globalInstance.emailLoginApi){
                    globalInstance.emailLoginApi =  false
                    globalInstance.traceEmailLoginApi.stop()
                }
                viewModel.updateType("email")
                when (loginState.errorCode) {
                    401 -> {
                        val message = loginState.error
                        if (message.contains("Error 805")) {
                            activity.makeToast(message)
                        }else if (message == "The email or password provided is incorrect.") {
                            viewModel.updateLoginError(LoginErrors.WRONG_PASSWORD)
                            activity.makeToast("Error 301 : Incorrect credentials entered")
                        }else {
                            viewModel.updateLoginError(LoginErrors.TRY_LATER)
                            activity.makeToast("Error 301A : Something Went Wrong. Incorrect credentials entered")
                        }
                    }
                    else -> {
                        if(loginState.error!="")
                            activity.makeToast(loginState.error)
                    }
                }
            }
        }
    }
    when (forgotState.success) {
        1 -> {
            LaunchedEffect(Unit) {
                viewModel.updateType("forgot")
                viewModel.emailState = ""
                viewModel.updateSuccessText(true)
            }
        }
        0 -> {
            LaunchedEffect(Unit) {
                viewModel.updateType("forgot")
                if (forgotState.error != "")
                    activity.makeToast(forgotState.error)
            }
        }
    }
    when (postPhoneState.success) {
        1 -> {
            LaunchedEffect(Unit) {
                if(globalInstance.phoneLoginApi){
                    globalInstance.phoneLoginApi =  false
                    globalInstance.tracePhoneLoginApi.stop()
                }
                activity.makeToast("Login successful")
                AnalyticsManager.phoneLoginSuccess()
                AppUtils.navigateNavScreen(activity, postPhoneState.userData!!)
            }
        }

        0 -> {
            LaunchedEffect(Unit) {
                if(globalInstance.phoneLoginApi){
                    globalInstance.phoneLoginApi =  false
                    globalInstance.tracePhoneLoginApi.stop()
                }
                viewModel.otpState = ""
                viewModel.updateType("otp")
                when (postPhoneState.errorCode) {
                    400 -> {
                        val message = postPhoneState.error
                        if (message.equals("Invalid OTP!")) {
                            viewModel.updateLoginError(LoginErrors.WRONG_OTP)
                            activity.makeToast("Error 102 : Please enter a valid OTP")
                        } else if (message.equals("OTP Expired!")) {
                            viewModel.updateLoginError(LoginErrors.OTP_EXPIRED)
                            activity.makeToast("Error 102A : OTP has expired")
                        } else {
                            viewModel.updateLoginError(LoginErrors.TRY_LATER)
                            activity.makeToast("Error 102B : Something Went Wrong. Please try again later.")
                        }
                    }

                    else -> {
                        if (postPhoneState.error != "")
                            activity.makeToast(postPhoneState.error)
                    }
                }
            }
        }
    }

    showMaintenanceDialog.value = if(globalInstance.remoteAppMessage.isNotEmpty()) globalInstance.remoteAppMessage[0].showDialog else false

    val displayMetrics: DisplayMetrics = activity.resources.displayMetrics
    val screenWidth = (displayMetrics.widthPixels / displayMetrics.density).toInt()
    val landscape = screenWidth >= 600

    if(showMaintenanceDialog.value) {
        CustomDialog(openDialogCustom = showMaintenanceDialog.value, onDismiss = { /*TODO*/ }) {
            Card(
                modifier = Modifier
                    .padding(start = 5.dp, top = 10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black)
            ) {
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .background(MaterialTheme.colors.surface)
                ) {
                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 20.dp),
                        horizontalArrangement = Arrangement.Center
                    ){
                        Text(
                            text = globalInstance.remoteAppMessage[0].dialogTitle,
                            style = heading.copy(fontFamily = Play, textDecoration = TextDecoration.Underline)
                        )
                    }
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
                            modifier = Modifier
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
                    else {
                        Text(
                            text = globalInstance.remoteAppMessage[0].messageText,
                            modifier = Modifier
                                .fillMaxWidth(),
                            color = MaterialTheme.colors.secondary,
                            style = subtitle.copy(
                                fontWeight = FontWeight.Light,
                                textAlign = TextAlign.Center
                            )
                        )
                    }
                    if(globalInstance.remoteAppMessage[0].isDialogDismissible){
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp)
                                .background(MaterialTheme.colors.primary.copy(alpha = 0.5f)),
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
                                showMaintenanceDialog.value = false
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = "",
                                    tint = MaterialTheme.colors.secondary,
                                )

                                Spacer(modifier = Modifier.size(5.dp))

                                androidx.compose.material.Text(
                                    text = "OK ",
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colors.secondary,
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

    if(!landscape) {
        Column(
                modifier = Modifier.fillMaxSize()
                    .background(brush = Brush.horizontalGradient(colors = gradientColors)),
                horizontalAlignment = Alignment.CenterHorizontally,verticalArrangement = Arrangement.Center
        ) {

                if (globalInstance.remoteAppMessage.isNotEmpty() && globalInstance.remoteAppMessage[0].showMessage) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            /*.background(Black)*/
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
                                        withStyle(style = SpanStyle(color = White)) {
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
                                                currentActivity.startActivity(intent)
                                            }
                                        }
                                    }
                                },
                                style = subtitle.copy(
                                    fontWeight = FontWeight.Light,
                                    textAlign = TextAlign.Center
                                ),
                                onTextLayout = { result ->
                                    textLayoutResult = result
                                }
                            )
                        } else {
                            Text(
                                text = globalInstance.remoteAppMessage[0].messageText,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.TopCenter),
                                color = MaterialTheme.colors.secondary,
                                style = subtitle.copy(
                                    fontWeight = FontWeight.Light,
                                    textAlign = TextAlign.Center
                                )
                            )
                        }
                    }
                }


                    Image(
                        modifier = Modifier.wrapContentWidth(),
                        painter = painterResource(id = R.drawable.ant_cloud_white_icon),
                        contentDescription = null,
                        contentScale = ContentScale.FillBounds
                    )




                    if (type != "loading") {
                        text = if (type == "email")
                            "Enter your Password"
                        else if (type == "otp")
                            "Enter OTP"
                        else
                            "Forgot Password"
                    }


                    Text(modifier = Modifier, text = text, style = titleText.copy(fontSize = 16.sp))
                    Spacer(modifier = Modifier.height(30.dp))

                    if (type == "email" || type == "forgot") {
                        var isError by remember { mutableStateOf(false) }
                        var isErrorPassword by remember { mutableStateOf(false) }

                        val color = if (isError) Red else White

                        BasicTextField(
                            value = email,
                            onValueChange = {
                                email = it
                                isError = email != "" && !email.isEmailValid()
                                viewModel.updateEmailState(email)
                                if (viewModel.loginError == LoginErrors.USER_NOT_REGISTERED) {
                                    viewModel.updateLoginError(LoginErrors.NULL)
                                }
                            },
                            textStyle = TextStyle(color = White, fontSize = 14.sp),
                            cursorBrush = SolidColor(BlueGradient),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth(0.8f)
                                .border(1.dp, color, RoundedCornerShape(16.dp)).padding(12.dp),
                            singleLine = true,
                            decorationBox = { innerTextField ->
                                Box(Modifier.padding(start = 8.dp)) {
                                    if (email.isEmpty()) {
                                        Text("Enter Email Address", color = White)
                                    }
                                    innerTextField()
                                }
                            })

                        Spacer(modifier = Modifier.height(10.dp))
                        val passColor = if (isErrorPassword) Red else White
                        if (isTextVisible) {
                            BasicTextField(
                                value = password,
                                onValueChange = {
                                    password = it
                                    isErrorPassword = password.length < 6
                                    viewModel.updatePasswordState(password)
                                    if (viewModel.loginError == LoginErrors.WRONG_PASSWORD) {
                                        viewModel.updateLoginError(LoginErrors.NULL)
                                    }
                                },
                                textStyle = TextStyle(color = White, fontSize = 14.sp),
                                cursorBrush = SolidColor(BlueGradient),
                                modifier = Modifier
                                    .fillMaxWidth(0.8f)
                                    .border(1.dp, passColor, RoundedCornerShape(16.dp))
                                    .padding(12.dp),
                                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                decorationBox = { innerTextField ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(modifier = Modifier.weight(1f)) {
                                            if (password.isEmpty()) {
                                                Text("Enter Password", color = White)
                                            }
                                            innerTextField()
                                        }
                                        Icon(
                                            imageVector = if (isPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                            contentDescription = if (isPasswordVisible) "Hide Password" else "Show Password",
                                            tint = White,
                                            modifier = Modifier.clickable {
                                                isPasswordVisible = !isPasswordVisible
                                            }
                                                .padding(start = 8.dp))
                                    }
                                })
                            Spacer(modifier = Modifier.height(10.dp))
                            if (viewModel.loginError == LoginErrors.WRONG_PASSWORD) {
                                androidx.compose.material.Text(
                                    text = "The email or password provided is incorrect",
                                    modifier = Modifier
                                        .fillMaxWidth(.8f)
                                        .padding(top = 0.dp),
                                    color = MaterialTheme.colors.error,
                                    style = MaterialTheme.typography.body1.copy(
                                        fontSize = 12.sp,
                                        textAlign = TextAlign.Start,
                                        fontWeight = FontWeight.Light
                                    )
                                )
                            }

                            Row(
                                horizontalArrangement = Arrangement.End,
                                modifier = Modifier.fillMaxWidth(.8f)
                            ) {
                                androidx.compose.material.Text(text = stringResource(id = R.string.forgot_pass_button),
                                    textDecoration = TextDecoration.Underline,
                                    style = subtitle,
                                    modifier = Modifier.clickable {
                                        viewModel._loginState.value = LoginState(success = -1)
                                        viewModel.updateSuccessText(false)
                                        viewModel.updateTextVisible(false)
                                        viewModel.updateType("forgot")
                                    })
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                if (type == "email") {
                                    if (!email.isEmailValid())
                                        activity.makeToast("please enter valid emailId")
                                    else if (password.length < 6)
                                        activity.makeToast("please enter valid password")
                                    else {
                                        emailLoginButton()
                                        globalInstance.emailClickLoginBtn = true
                                        globalInstance.traceEmailClickLoginBtn =
                                            FirebasePerformance.getInstance().newTrace("email_click_login_btn")
                                        globalInstance.traceEmailClickLoginBtn.start()
                                        globalInstance.emailLoginApi = true
                                        globalInstance.traceEmailLoginApi =
                                            FirebasePerformance.getInstance().newTrace("email_login_api")
                                        globalInstance.traceEmailLoginApi.start()
                                        viewModel.updateType("loading")
                                        viewModel.updateLoginLoadingText("Signing You In....")
                                        val dataModel = LoginReqData(email, password)
                                        viewModel.getUserLogin(dataModel)
                                    }
                                } else if (type == "forgot") {
                                    if (email.isEmailValid()) {
                                        viewModel.updateType("loading")
                                        viewModel.updateLoginLoadingText("Sending...")
                                        val dataModel = ForgotPasswordReq(email.trim())
                                        viewModel.getForgotData(dataModel, "Login")
                                    } else
                                        activity.makeToast("please enter valid emailId")
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth(.8f)
                                .padding(top = 10.dp),
                            contentPadding = PaddingValues(vertical = 12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = White)
                        ) {
                            var text = ""
                            if (type == "email")
                                text = "Login"
                            else if (type == "forgot")
                                text = "Send Password Reset Link"

                            Text(text = text, style = subtitle.copy(color = Color.Black))
                        }

                        if (successText && type == "forgot") {
                            androidx.compose.material.Text(text = "An e-mail with the reset link has been sent. Please check your registered Email",
                                style = mainTitle.copy(
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colors.primary
                                ),
                                modifier = Modifier
                                    .fillMaxWidth(.8f)
                                    .clickable {
                                        //onEventHandler(LoginEvent.OnForgotPasswsordClicked)
                                    }
                                    .padding(top = 20.dp, start = 10.dp, end = 10.dp))
                        }

                        Row(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(top = 20.dp, bottom = 20.dp)
                        ) {
                            androidx.compose.material.Text(
                                text = "Don't have an Account? ",
                                style = subtitle.copy(fontSize = 16.sp)
                            )
                            androidx.compose.material.Text(text = "Sign Up",
                                style = subtitle.copy(
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colors.primary
                                ),
                                modifier = Modifier.clickable {
                                    AppUtils.navigateSignupScreen(activity, email)
                                })
                        }

                    } else if (type == "otp") {
                        var resentOTPText by remember { mutableStateOf(viewModel.resendOTPText) }
                        viewModel.subResendOTPText = { resentOTPText = it }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            var otp by remember {
                                mutableStateOf(viewModel.otpState)
                            }
                            viewModel.subOtpState = {
                                otp = it
                            }
                            Spacer(modifier = Modifier.size(20.dp))
                            val wrongOtp = (viewModel.loginError == LoginErrors.WRONG_OTP)
                            var colorStroke: Color? = null
                            colorStroke = if (!wrongOtp) White else Red
                            OtpView(
                                otpText = otp,
                                onOtpTextChange = {
                                    otp = it
                                },
                                type = OTP_VIEW_TYPE_BOX,
                                otpCount = 6,
                                containerSize = 48.dp,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                strokeColor = colorStroke,
                                charColor = White
                            )
                            Spacer(modifier = Modifier.size(20.dp))
                            var text by remember { mutableStateOf("") }
                            val totalTime: Int = 60
                            if (resentOTPText) {
                                var timeRemaining by remember { mutableStateOf(totalTime) }
                                var isTimerRunning by remember { mutableStateOf(true) }
                                LaunchedEffect(isTimerRunning) {
                                    if (isTimerRunning) {
                                        for (time in totalTime downTo 0) {
                                            timeRemaining = time
                                            kotlinx.coroutines.delay(1000L)
                                            text = "Retry in $timeRemaining secs"
                                        }
                                        viewModel.updateResendText(false)
                                        isTimerRunning = false
                                    }
                                }
                            } else
                                text = "Resend OTP"
                            Row(
                                horizontalArrangement = Arrangement.End,
                                modifier = Modifier.fillMaxWidth(.8f)
                            ) {
                                androidx.compose.material.Text(
                                    text = text,
                                    style = subtitle,
                                    modifier = Modifier.clickable {
                                        if (!resentOTPText) {
                                            viewModel.updateResendText(true)
                                            val dataModel = PhoneOtpReq("+91${email}", false)
                                            viewModel.getPostPhoneOtpData(dataModel, "login")
                                        }
                                    })
                            }
                            Spacer(modifier = Modifier.size(20.dp))
                            Button(
                                onClick = {
                                    if (otp.length == 6) {
                                        AnalyticsManager.phoneLoginButton()
                                        globalInstance.phoneClickLoginBtn = true
                                        globalInstance.tracePhoneClickLoginBtn =
                                            FirebasePerformance.getInstance().newTrace("phone_click_login_btn")
                                        globalInstance.tracePhoneClickLoginBtn.start()
                                        globalInstance.phoneLoginApi = true
                                        globalInstance.tracePhoneLoginApi =
                                            FirebasePerformance.getInstance().newTrace("phone_login_api")
                                        globalInstance.tracePhoneLoginApi.start()
                                        viewModel.updateType("loading")
                                        viewModel.updateLoginLoadingText("Verifying Your OTP ....")
                                        val dataModel =
                                            PhoneVerifyReq("+91${email.trim()}", otp.trim())
                                        viewModel.getPostPhoneData(dataModel)
                                    } else
                                        activity.makeToast("please enter 6 digit otp")
                                },
                                modifier = Modifier.fillMaxWidth(.8f).padding(top = 10.dp),
                                contentPadding = PaddingValues(vertical = 12.dp),
                                enabled = (otp.length == 6),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = White,
                                    disabledContainerColor = White
                                )
                            ) {
                                Text(text = "Login", style = subtitle.copy(color = Color.Black))
                            }
                        }
                    } else {
                        Text(
                            modifier = Modifier,
                            text = viewModel.loginLoadingTextState,
                            style = titleText.copy(fontSize = 20.sp)
                        )

                        CircularProgressIndicator(
                            modifier = Modifier.size(60.dp).padding(top = 10.dp),
                            color = PinkGradient,
                            strokeWidth = 10.dp
                        )
                    }
                }
            }


    else{
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .background(brush = Brush.horizontalGradient(colors = gradientColors)),
            horizontalAlignment = Alignment.CenterHorizontally) {

            if (globalInstance.remoteAppMessage.isNotEmpty() && globalInstance.remoteAppMessage[0].showMessage) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
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
                                            currentActivity.startActivity(intent)
                                        }
                                    }
                                }
                            },
                            style = subtitle.copy(
                                fontWeight = FontWeight.Light,
                                textAlign = TextAlign.Center
                            ),
                            onTextLayout = { result ->
                                textLayoutResult = result
                            }
                        )
                    } else {
                        Text(
                            text = globalInstance.remoteAppMessage[0].messageText,
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.TopCenter),
                            color = MaterialTheme.colors.secondary,
                            style = subtitle.copy(
                                fontWeight = FontWeight.Light,
                                textAlign = TextAlign.Center
                            )
                        )
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center) {
                Image(
                    modifier = Modifier.wrapContentWidth(),
                    painter = painterResource(id = R.drawable.ant_cloud_white_icon),
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds
                )




                if (type != "loading") {
                    text = if (type == "email")
                        "Enter your Password"
                    else if (type == "otp")
                        "Enter OTP"
                    else
                        "Forgot Password"
                }


                Text(modifier = Modifier, text = text, style = titleText.copy(fontSize = 16.sp))
                Spacer(modifier = Modifier.height(30.dp))

                if (type == "email" || type == "forgot") {
                    var isError by remember { mutableStateOf(false) }
                    var isErrorPassword by remember { mutableStateOf(false) }

                    val color = if (isError) Red else White

                    BasicTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            isError = email != "" && !email.isEmailValid()
                            viewModel.updateEmailState(email)
                            if (viewModel.loginError == LoginErrors.USER_NOT_REGISTERED) {
                                viewModel.updateLoginError(LoginErrors.NULL)
                            }
                        },
                        textStyle = TextStyle(color = White, fontSize = 14.sp),
                        cursorBrush = SolidColor(BlueGradient),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(0.8f)
                            .border(1.dp, color, RoundedCornerShape(16.dp)).padding(12.dp),
                        singleLine = true,
                        decorationBox = { innerTextField ->
                            Box(Modifier.padding(start = 8.dp)) {
                                if (email.isEmpty()) {
                                    Text("Enter Email Address", color = White)
                                }
                                innerTextField()
                            }
                        })

                    Spacer(modifier = Modifier.height(10.dp))
                    val passColor = if (isErrorPassword) Red else White
                    if (isTextVisible) {
                        BasicTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                isErrorPassword = password.length < 6
                                viewModel.updatePasswordState(password)
                                if (viewModel.loginError == LoginErrors.WRONG_PASSWORD) {
                                    viewModel.updateLoginError(LoginErrors.NULL)
                                }
                            },
                            textStyle = TextStyle(color = White, fontSize = 14.sp),
                            cursorBrush = SolidColor(BlueGradient),
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .border(1.dp, passColor, RoundedCornerShape(16.dp))
                                .padding(12.dp),
                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            decorationBox = { innerTextField ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        if (password.isEmpty()) {
                                            Text("Enter Password", color = White)
                                        }
                                        innerTextField()
                                    }
                                    Icon(
                                        imageVector = if (isPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                        tint = White,
                                        contentDescription = if (isPasswordVisible) "Hide Password" else "Show Password",
                                        modifier = Modifier.clickable {
                                            isPasswordVisible = !isPasswordVisible
                                        }
                                            .padding(start = 8.dp))
                                }
                            })
                        Spacer(modifier = Modifier.height(10.dp))
                        if (viewModel.loginError == LoginErrors.WRONG_PASSWORD) {
                            androidx.compose.material.Text(
                                text = "The email or password provided is incorrect",
                                modifier = Modifier
                                    .fillMaxWidth(.8f)
                                    .padding(top = 0.dp),
                                color = MaterialTheme.colors.error,
                                style = MaterialTheme.typography.body1.copy(
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Start,
                                    fontWeight = FontWeight.Light
                                )
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.fillMaxWidth(.8f)
                        ) {
                            androidx.compose.material.Text(text = stringResource(id = R.string.forgot_pass_button),
                                textDecoration = TextDecoration.Underline,
                                style = subtitle,
                                modifier = Modifier.clickable {
                                    viewModel._loginState.value = LoginState(success = -1)
                                    viewModel.updateSuccessText(false)
                                    viewModel.updateTextVisible(false)
                                    viewModel.updateType("forgot")
                                })
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (type == "email") {
                                if (!email.isEmailValid())
                                    activity.makeToast("please enter valid emailId")
                                else if (password.length < 6)
                                    activity.makeToast("please enter valid password")
                                else {
                                    emailLoginButton()
                                    globalInstance.emailClickLoginBtn = true
                                    globalInstance.traceEmailClickLoginBtn =
                                        FirebasePerformance.getInstance().newTrace("email_click_login_btn")
                                    globalInstance.traceEmailClickLoginBtn.start()
                                    globalInstance.emailLoginApi = true
                                    globalInstance.traceEmailLoginApi =
                                        FirebasePerformance.getInstance().newTrace("email_login_api")
                                    globalInstance.traceEmailLoginApi.start()
                                    viewModel.updateType("loading")
                                    viewModel.updateLoginLoadingText("Signing You In....")
                                    val dataModel = LoginReqData(email, password)
                                    viewModel.getUserLogin(dataModel)
                                }
                            } else if (type == "forgot") {
                                if (email.isEmailValid()) {
                                    viewModel.updateType("loading")
                                    viewModel.updateLoginLoadingText("Sending...")
                                    val dataModel = ForgotPasswordReq(email.trim())
                                    viewModel.getForgotData(dataModel, "Login")
                                } else
                                    activity.makeToast("please enter valid emailId")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth(.8f)
                            .padding(top = 10.dp),
                        contentPadding = PaddingValues(vertical = 12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = White)
                    ) {
                        var text = ""
                        if (type == "email")
                            text = "Login"
                        else if (type == "forgot")
                            text = "Send Password Reset Link"

                        Text(text = text, style = subtitle.copy(color = Color.Black))
                    }

                    if (successText && type == "forgot") {
                        androidx.compose.material.Text(text = "An e-mail with the reset link has been sent. Please check your registered Email",
                            style = mainTitle.copy(
                                fontSize = 11.sp,
                                color = MaterialTheme.colors.primary
                            ),
                            modifier = Modifier
                                .fillMaxWidth(.8f)
                                .clickable {
                                    //onEventHandler(LoginEvent.OnForgotPasswsordClicked)
                                }
                                .padding(top = 20.dp, start = 10.dp, end = 10.dp))
                    }

                    Row(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 20.dp, bottom = 20.dp)
                    ) {
                        androidx.compose.material.Text(
                            text = "Don't have an Account? ",
                            style = subtitle.copy(fontSize = 16.sp)
                        )
                        androidx.compose.material.Text(text = "Sign Up",
                            style = subtitle.copy(
                                fontSize = 16.sp,
                                color = MaterialTheme.colors.primary
                            ),
                            modifier = Modifier.clickable {
                                AppUtils.navigateSignupScreen(activity, email)
                            })
                    }

                } else if (type == "otp") {
                    var resentOTPText by remember { mutableStateOf(viewModel.resendOTPText) }
                    viewModel.subResendOTPText = { resentOTPText = it }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        var otp by remember {
                            mutableStateOf(viewModel.otpState)
                        }
                        viewModel.subOtpState = {
                            otp = it
                        }
                        Spacer(modifier = Modifier.size(20.dp))
                        val wrongOtp = (viewModel.loginError == LoginErrors.WRONG_OTP)
                        var colorStroke: Color? = null
                        colorStroke = if (!wrongOtp) White else Red
                        OtpView(
                            otpText = otp,
                            onOtpTextChange = {
                                otp = it
                            },
                            type = OTP_VIEW_TYPE_BOX,
                            otpCount = 6,
                            containerSize = 48.dp,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            strokeColor = colorStroke!!,
                            charColor = White
                        )
                        Spacer(modifier = Modifier.size(20.dp))
                        var text by remember { mutableStateOf("") }
                        val totalTime: Int = 60
                        if (resentOTPText) {
                            var timeRemaining by remember { mutableStateOf(totalTime) }
                            var isTimerRunning by remember { mutableStateOf(true) }
                            LaunchedEffect(isTimerRunning) {
                                if (isTimerRunning) {
                                    for (time in totalTime downTo 0) {
                                        timeRemaining = time
                                        kotlinx.coroutines.delay(1000L)
                                        text = "Retry in $timeRemaining secs"
                                    }
                                    viewModel.updateResendText(false)
                                    isTimerRunning = false
                                }
                            }
                        } else
                            text = "Resend OTP"
                        Row(
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.fillMaxWidth(.8f)
                        ) {
                            androidx.compose.material.Text(
                                text = text,
                                style = subtitle,
                                modifier = Modifier.clickable {
                                    if (!resentOTPText) {
                                        viewModel.updateResendText(true)
                                        val dataModel = PhoneOtpReq("+91${email}", false)
                                        viewModel.getPostPhoneOtpData(dataModel, "login")
                                    }
                                })
                        }
                        Spacer(modifier = Modifier.size(20.dp))
                        Button(
                            onClick = {
                                if (otp.length == 6) {
                                    AnalyticsManager.phoneLoginButton()
                                    globalInstance.phoneClickLoginBtn = true
                                    globalInstance.tracePhoneClickLoginBtn =
                                        FirebasePerformance.getInstance().newTrace("phone_click_login_btn")
                                    globalInstance.tracePhoneClickLoginBtn.start()
                                    globalInstance.phoneLoginApi = true
                                    globalInstance.tracePhoneLoginApi =
                                        FirebasePerformance.getInstance().newTrace("phone_login_api")
                                    globalInstance.tracePhoneLoginApi.start()
                                    viewModel.updateType("loading")
                                    viewModel.updateLoginLoadingText("Verifying Your OTP ....")
                                    val dataModel =
                                        PhoneVerifyReq("+91${email.trim()}", otp.trim())
                                    viewModel.getPostPhoneData(dataModel)
                                } else
                                    activity.makeToast("please enter 6 digit otp")
                            },
                            modifier = Modifier.fillMaxWidth(.8f).padding(top = 10.dp),
                            contentPadding = PaddingValues(vertical = 12.dp),
                            enabled = (otp.length == 6),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = White,
                                disabledContainerColor = White
                            )
                        ) {
                            Text(text = "Login", style = subtitle.copy(color = Color.Black))
                        }
                    }
                } else {
                    Text(
                        modifier = Modifier,
                        text = viewModel.loginLoadingTextState,
                        style = titleText.copy(fontSize = 20.sp)
                    )

                    CircularProgressIndicator(
                        modifier = Modifier.size(60.dp).padding(top = 10.dp),
                        color = PinkGradient,
                        strokeWidth = 10.dp
                    )
                }
            }
        }
    }
}





