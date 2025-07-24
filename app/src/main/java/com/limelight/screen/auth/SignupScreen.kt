package com.limelight.screen.auth


import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Build.VERSION.SDK_INT
import android.util.DisplayMetrics
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.MaterialTheme.shapes
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
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
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.Color.Companion.DarkGray
import androidx.compose.ui.graphics.Color.Companion.Red
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
import androidx.core.text.isDigitsOnly
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.google.firebase.perf.FirebasePerformance


import com.limelight.components.Loading
import com.limelight.common.OAuthLogic
import com.limelight.components.OTP_VIEW_TYPE_BOX
import com.limelight.components.OtpView
import com.limelight.components.isEmailValid
import com.limelight.components.isPhoneNumberValid
import com.limelight.components.isPinCodeValid
import com.limelight.components.makeToast
import com.limelight.data.Location
import com.limelight.data.PhoneOtpReq
import com.limelight.data.PhoneVerifyReq
import com.limelight.data.UserRegisterReq
import com.limelight.data.UserSignUp
import com.limelight.data.statesList
import com.limelight.theme.BlueGradient
import com.limelight.theme.mainTitle
import com.limelight.theme.subtitle
import com.limelight.theme.textColorWhite
import com.limelight.theme.titleText
import com.limelight.viewmodel.AuthenticateViewModel
import com.limelight.DarkColorPalette
import com.limelight.R
import com.limelight.activity.SignupActivity
import com.limelight.common.AppUtils.Companion.gradientColors
import com.limelight.common.AppUtils.Companion.navigateNavScreen
import com.limelight.Theme
import com.limelight.activity.LoginActivity
import com.limelight.common.AnalyticsManager
import com.limelight.common.AppUtils.Companion.navigateSplashActivity
import com.limelight.components.CustomDialog
import com.limelight.components.Play
import com.limelight.screen.account.globalInstance
import com.limelight.theme.dark_grey
import com.limelight.theme.heading


private lateinit var oAuthLogic: OAuthLogic


enum class SignupErrors {
    NULL, BLOCKED, WRONG_OTP, WRONG_NUMBER, USERNAME_TAKEN, EMAIL_TAKEN, NUMBER_TAKEN, OAUTH_ERROR, TRY_LATER, OTP_EXPIRED
}


val location = Location()
private lateinit var user : UserSignUp

@Composable
fun SignupScreen(activity: SignupActivity, viewModel: AuthenticateViewModel, emailMobileValue: String) {

    oAuthLogic = OAuthLogic(
        activity = activity,
        onSuccess = { _, _ -> },
        onError = { viewModel.updateSignupError(SignupErrors.OAUTH_ERROR) }, isLogin = false)

    user = oAuthLogic.createUserProfile(viewModel.nameState, viewModel.emailState, viewModel.phoneNumberState, location)

    val signupState = viewModel.signUpState.value
    val postPhoneOtpState = viewModel.postPhoneOtpState.value
    val postPhoneVerifyState = viewModel.postPhoneVerifyState.value

    if (emailMobileValue.isDigitsOnly())
        viewModel.updatePhoneNumber(emailMobileValue)
    else
        viewModel.updateEmailState(emailMobileValue)

    var loadingText by remember {
        mutableStateOf(viewModel.signupLoadingTextState)
    }

    viewModel.signupSubLoadingState = {
        loadingText = it
    }

    var state by remember {
        mutableStateOf(viewModel.signUpStateText)
    }

    viewModel.subSignUpStateText = {
        state = it
    }
    val showMaintenanceDialog = remember {
        mutableStateOf(false)
    }

    when (signupState.success) {
        1 -> {
            LaunchedEffect(Unit) {
                AnalyticsManager.signupSuccess()
                navigateNavScreen(activity, signupState.userData!!)
            }
        }
        0 -> {
            LaunchedEffect(Unit) {
                viewModel.updateSignUpState("email")
                when (signupState.errorCode) {
                    400 -> {
                        val message = signupState.error
                        if (message != "") {
                            activity.makeToast("Error 103 : $message")
                        } else if (message.contains("Error 810") ||
                            message.contains("Error 203") ||
                            message.contains("Error 403")
                        ) {
                            activity.makeToast(signupState.error)
                        } else {
                            activity.makeToast("Error 103B : Something Went Wrong, Unable To Register")
                        }
                        if (message.equals("A user with the given email is already registered")) {
                            viewModel.updateSignupError(SignupErrors.EMAIL_TAKEN)
                        }
                    }

                    else -> {
                        if (signupState.error != "")
                            activity.makeToast(signupState.error)
                    }
                }
            }
        }
    }

    when (postPhoneOtpState.success) {
        1 -> {
            LaunchedEffect(Unit) {
                globalInstance.traceGenerateOTPApi.stop()
                if (!viewModel.flag) {
                    viewModel.updateSignUpState("otp")
                    viewModel.flag = true
                }
            }
        }
        0 -> {
            LaunchedEffect(Unit) {
                viewModel.updateSignUpState("email")
                when (postPhoneOtpState.errorCode) {
                    400 -> {
                        val message = postPhoneOtpState.error
                        if (message.contains("Error 808")) {
                            activity.makeToast(message)
                        }
                        if (message.equals("This number already exists!")) {
                            viewModel.updateSignupError(SignupErrors.NUMBER_TAKEN)
                            activity.makeToast("Error 104B : Number already in use.")
                        } else if (message.equals("Invalid Number provided!")) {
                            viewModel.updateSignupError(SignupErrors.WRONG_NUMBER)
                            activity.makeToast("Error 104B1 : Please enter a valid number")
                        } else {
                            viewModel.updateSignupError(SignupErrors.TRY_LATER)
                            activity.makeToast("Error 104B2 : Something went wrong. Please try again after sometime.")
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

    when (postPhoneVerifyState.success) {
        1 -> {
            LaunchedEffect(Unit) {
                if(globalInstance.verifyOTPBtn){
                    globalInstance.verifyOTPBtn =  false
                    globalInstance.traceVerifyOTPBtn.stop()
                }
                globalInstance.signUp = true
                globalInstance.traceSignUp=  FirebasePerformance.getInstance().newTrace("signup_after_verify_otp")
                globalInstance.traceSignUp.start()

                globalInstance.userRegisterApi = true
                globalInstance.traceUserRegisterApi=  FirebasePerformance.getInstance().newTrace("user_register_api")
                globalInstance.traceUserRegisterApi.start()
                val dataModel = UserRegisterReq(
                    viewModel.emailState.trim(), viewModel.passwordState.trim(),
                    "+91${user.phone}", user.firstName, user.lastName, user.location, user.source
                )
                viewModel.getSignUpData(dataModel)
            }
        }

        0 -> {
            LaunchedEffect(Unit) {
                if(globalInstance.verifyOTPBtn){
                    globalInstance.verifyOTPBtn =  false
                    globalInstance.traceVerifyOTPBtn.stop()
                }
                viewModel.otpState = ""
                viewModel.updateSignUpState("otp")
                when (postPhoneVerifyState.errorCode) {
                    400 -> {
                        val message = postPhoneVerifyState.error
                        if (message.contains("Error 809")) {
                            activity.makeToast(message)
                        } else if (message.equals("Invalid OTP")) {
                            viewModel.updateSignupError(SignupErrors.WRONG_OTP)
                            activity.makeToast("Error 105B : Please enter a valid OTP")
                        } else if (message.equals("OTP Expired!")) {
                            viewModel.updateSignupError(SignupErrors.OTP_EXPIRED)
                            activity.makeToast("Error 105B1 : OTP has expired")
                        } else {
                            activity.makeToast("Error 105B2 : Something Went Wrong")
                        }
                    }

                    else -> {
                        if (postPhoneVerifyState.error != "")
                            activity.makeToast(postPhoneVerifyState.error)
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
                        .background(dark_grey)
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
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        Column(
            modifier = Modifier.padding().fillMaxSize()
                .background(brush = Brush.horizontalGradient(colors = gradientColors)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (globalInstance.remoteAppMessage.isNotEmpty() && globalInstance.remoteAppMessage[0].showMessage) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Black)
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
                                .align(Alignment.TopCenter)
                                .padding(top = 5.dp),
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
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    modifier = Modifier.wrapContentWidth().padding(top = 20.dp),
                    painter = painterResource(id = R.drawable.ant_cloud_white_icon),
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds
                )

                Text(
                    modifier = Modifier,
                    text = "Create an Account",
                    style = titleText.copy(fontSize = 16.sp)
                )

                when (state) {
                    "loading" -> Column(modifier = Modifier.size((LocalConfiguration.current.screenWidthDp).dp)) {
                        Loading(loadingText, landscape = false)
                    }

                    "email" -> EnterInfo(
                        emailMobileValue,
                        activity = activity,
                        showPassword = true,
                        viewModel = viewModel,
                        landscape = false
                    )

                    "otp" -> EnterOTP(activity, viewModel = viewModel, landscape = false)
                }
            }
            Spacer(modifier = Modifier.size(10.dp))
        }
    }
    else{
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        Column(
            modifier = Modifier.padding().fillMaxSize().verticalScroll(rememberScrollState())
                .background(brush = Brush.horizontalGradient(colors = gradientColors)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (globalInstance.remoteAppMessage.isNotEmpty() && globalInstance.remoteAppMessage[0].showMessage) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Black)
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
                                .align(Alignment.TopCenter)
                                .padding(top = 5.dp),
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
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    modifier = Modifier.wrapContentWidth().padding(top = 10.dp),
                    painter = painterResource(id = R.drawable.ant_cloud_white_icon),
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds
                )

                Text(
                    modifier = Modifier,
                    text = "Create an Account",
                    style = titleText.copy(fontSize = 16.sp)
                )

                when (state) {
                    "loading" -> Column(modifier = Modifier.size((LocalConfiguration.current.screenWidthDp).dp)) {
                        Loading(loadingText, landscape = false)
                    }

                    "email" -> EnterInfo(
                        emailMobileValue,
                        activity = activity,
                        showPassword = true,
                        viewModel = viewModel,
                        landscape = false
                    )

                    "otp" -> EnterOTP(activity, viewModel = viewModel, landscape = false)
                }
            }
            Spacer(modifier = Modifier.size(10.dp))
        }
    }
}


@Composable
fun EnterInfo(emailMobileValue: String, activity: SignupActivity, showPassword: Boolean, viewModel: AuthenticateViewModel,
              landscape: Boolean = false) {

    var showStateDropDown by remember { mutableStateOf(false) }

    var isPasswordVisible by remember { mutableStateOf(false) }

//    var selectedDate by remember { mutableStateOf("Select Date") }

    var fullName by remember { mutableStateOf(viewModel.nameState) }
    var phoneNumber by remember { mutableStateOf(viewModel.phoneNumberState) }
    var email by remember { mutableStateOf(viewModel.emailState) }
    var state by remember { mutableStateOf(viewModel.stateLocation) }
    var pincode by remember { mutableStateOf(viewModel.pinCodeState) }
    var password by remember { mutableStateOf(viewModel.passwordState) }

    viewModel.subNameState = {
        fullName = it
    }

    viewModel.subEmailState = {
        email = it
    }

    viewModel.subPhoneNumberState = {
        phoneNumber = it
    }

    /*viewModel.subCityState = {
        city = it
    }*/

    viewModel.subStateLocation = {
        state = it
    }

    viewModel.subPinCodeState = {
        pincode = it
    }

    viewModel.subPasswordState = {
        password = it
    }

    var signupError by remember {
        mutableStateOf(viewModel.signupErrorState)
    }
    viewModel.subSignupErrorState = {
        signupError = it
    }

    var isErrorName by remember { mutableStateOf(false) }
    var isErrorPhone by remember { mutableStateOf(false) }
    var isErrorEmail by remember { mutableStateOf(false) }
    var isErrorPincode by remember { mutableStateOf(false) }
    var isErrorDate by remember { mutableStateOf(false) }
    var isErrorPassword by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(top = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {

        val nameColor = if(isErrorName || signupError == SignupErrors.USERNAME_TAKEN) Red else White
        BasicTextField(value = fullName,
            onValueChange = {
                fullName = it
                viewModel.updateNameState(fullName)
                isErrorName = fullName == ""
                if (viewModel.signupErrorState == SignupErrors.USERNAME_TAKEN) {
                    viewModel.updateSignupError(SignupErrors.NULL)
                }
            },
            textStyle = TextStyle(color = White, fontSize = 14.sp),
            cursorBrush = SolidColor(BlueGradient),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            modifier = Modifier.fillMaxWidth(0.8f).border(1.dp, nameColor, RoundedCornerShape(16.dp)).padding(12.dp),
            singleLine = true,
            decorationBox = { innerTextField ->
                Box(Modifier.padding(start = 8.dp)) {
                    if (fullName.isEmpty()) {
                        Text("Full Name", color = White)
                    }
                    innerTextField()
                } })

        Spacer(modifier = Modifier.height(10.dp))
        val wrongNumber = (signupError == SignupErrors.NUMBER_TAKEN || signupError == SignupErrors.WRONG_NUMBER)

        val phoneColor  = if(isErrorPhone||wrongNumber) Red else White
        Row(modifier = Modifier.fillMaxWidth(0.8f).
                        border(1.dp, phoneColor, RoundedCornerShape(16.dp)).
                padding(12.dp)) {
            Text(modifier = Modifier.padding(start = 8.dp), text = "+91 ", color = White, fontSize = 14.sp)
            BasicTextField(
                value = phoneNumber,
                onValueChange = {
                    phoneNumber = it
                    viewModel.updatePhoneNumber(phoneNumber)
                    isErrorPhone = !phoneNumber.isPhoneNumberValid()
                },
                textStyle = TextStyle(color = White, fontSize = 14.sp),
                cursorBrush = SolidColor(BlueGradient),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                decorationBox = { innerTextField ->
                    Box(Modifier.padding(start = 2.dp)) {
                        if (phoneNumber.isEmpty()) {
                            Text("Phone Number", color = Color.White)
                        }
                        innerTextField()
                    }
                })
        }

        Spacer(modifier = Modifier.height(10.dp))
        val emailColor = if(isErrorEmail||signupError == SignupErrors.EMAIL_TAKEN) Red else White
        BasicTextField(
            value = email,
            onValueChange = {
                email = it
                viewModel.updateEmailState(email)
                if (viewModel.signupErrorState == SignupErrors.EMAIL_TAKEN) {
                    viewModel.updateSignupError(SignupErrors.NULL)
                }
                isErrorEmail = !email.isEmailValid()
            },
            textStyle = TextStyle(color = White, fontSize = 14.sp ),
            cursorBrush = SolidColor(BlueGradient),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(0.8f).border(1.dp, emailColor, RoundedCornerShape(16.dp)).padding(12.dp),
            singleLine = true,
            decorationBox = { innerTextField ->
                Box(Modifier.padding(start = 8.dp)) {
                    if (email.isEmpty()) {
                        Text("Email Address", color = White)
                    }
                    innerTextField()
                } })

        Spacer(modifier = Modifier.height(10.dp))

            Box(modifier = Modifier.fillMaxWidth(0.8f).border(1.dp, White, RoundedCornerShape(16.dp)).padding(8.dp)){
                StateLocationDropDownMenu(state ,options = statesList, label = "State",viewModel)
            }

        Spacer(modifier = Modifier.height(10.dp))

        val pinCodeColor = if(isErrorPincode) Red else White
        BasicTextField(
            value = pincode,
            onValueChange = {
                pincode = it
                viewModel.updatePincode(pincode)
                isErrorPincode = pincode != "" && !pincode.isPinCodeValid()
            },
            textStyle = TextStyle(color = White, fontSize = 14.sp ),
            cursorBrush = SolidColor(BlueGradient),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(0.8f).border(1.dp, pinCodeColor, RoundedCornerShape(16.dp)).padding(12.dp),
            singleLine = true,
            decorationBox = { innerTextField ->
                Box(Modifier.padding(start = 8.dp)) {
                    if (pincode.isEmpty()) {
                        Text("Pincode", color = Color.White)
                    }
                    innerTextField()
                } })
        Spacer(modifier = Modifier.height(10.dp))



      /*  val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)


        val datePickerDialog = DatePickerDialog(LocalContext.current,{ _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDay: Int ->
                selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear" // Month is 0-indexed
            }, year, month, day)


        Box(
            modifier = Modifier.
                    fillMaxWidth(0.8f)
                    .border(
                        1.dp,
                        White, RoundedCornerShape(16.dp)).padding(3.dp)){
                Text(
                    modifier = Modifier.padding(start= 12.dp).align(Alignment.CenterStart),
                    text = selectedDate,
                    fontSize = 12.sp,
                    color = White
                )
                IconButton(
                    onClick = { datePickerDialog.show() },
                    modifier = Modifier.size(36.dp).align(Alignment.CenterEnd).padding(end =8.dp)
                ) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_my_calendar),
                        contentDescription = "Open Calendar",
                        tint = Color.Black)
                }
            }
       Spacer(modifier = Modifier.height(10.dp))
       */

        val passColor = if(isErrorPassword) Red else White
        BasicTextField(
            value = password,
            onValueChange = {
                password= it
                viewModel.updatePasswordState(password)
                isErrorPassword = password.length<6
            },
            textStyle = TextStyle(color = White, fontSize = 14.sp),
            cursorBrush = SolidColor(BlueGradient),
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .border(
                    1.dp,
                   passColor, RoundedCornerShape(16.dp)).padding(12.dp),
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        if (password.isEmpty()) {
                            Text("Enter Password", color = White)
                        }
                        innerTextField()
                    }
                    Icon(
                        tint = White,
                        imageVector = if (isPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = if (isPasswordVisible) "Hide Password" else "Show Password",
                        modifier = Modifier
                            .clickable { isPasswordVisible = !isPasswordVisible }
                            .padding(start = 8.dp))
                }
            }
        )

        Spacer(modifier = Modifier.height(30.dp))

        Button(
            onClick = {
                if(fullName.isEmpty())
                    activity.makeToast("please enter name")
                else if(!email.isEmailValid())
                    activity.makeToast("please enter valid emailId")
                else if (phoneNumber.isEmpty())
                    activity.makeToast("please enter number")
                else if(phoneNumber.length<10)
                    activity.makeToast("please enter valid number")
                else if(state.isEmpty())
                    activity.makeToast("please select state")
                else if (pincode.isEmpty())
                    activity.makeToast("please enter pincode")
                else if(!pincode.isPinCodeValid())
                    activity.makeToast("please enter valid pincode")
                else if (password.isEmpty())
                    activity.makeToast("please enter password")
                else if(password.length<6)
                    activity.makeToast("please enter valid password")
                else {
                    AnalyticsManager.signupButton()
                    globalInstance.traceGenerateOTPApi =  FirebasePerformance.getInstance().newTrace("generate_otp_btn")
                    globalInstance.traceGenerateOTPApi.start()
                    location.State = state
                    location.Pincode = pincode
                    viewModel.updateSignUpState("loading")
                    viewModel.updateSignUpLoadingText("Sending the OTP.....")
                    val dataModel = PhoneOtpReq("+91${viewModel.phoneNumberState.trim()}", true)
                    viewModel.getPostPhoneOtpData(dataModel, "signup")
                }
            },
            modifier = Modifier.fillMaxWidth(.8f).padding(top = 10.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = White) ) {
            Text(text = "Signup", style = subtitle.copy(color = Black))
        }
        Spacer(modifier = Modifier.height(20.dp))

        Row {
            androidx.compose.material.Text(
                text = "Already a User? ",
                style = mainTitle.copy(fontSize = 16.sp))

            androidx.compose.material.Text(
                text = "Sign In",
                style = subtitle.copy(fontSize = 16.sp, color = MaterialTheme.colors.primary),
                modifier = Modifier.clickable { navigateSplashActivity(activity) })
        }
    }
}


@Composable
fun EnterOTP(activity : SignupActivity, viewModel: AuthenticateViewModel, landscape: Boolean =false) {
    var resentOTPText by remember { mutableStateOf(viewModel.resendOTPText) }
    viewModel.subResendOTPText = { resentOTPText = it }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally) {

        var otp by remember { mutableStateOf(viewModel.otpState) }

        viewModel.subOtpState = {
            otp = it
        }

        Spacer(modifier = Modifier.size(20.dp))
        val wrongOtp = (viewModel.signupErrorState == SignupErrors.WRONG_OTP)
        var colorStroke:Color? =null
        colorStroke = if (!wrongOtp) White else Red
        OtpView(
            otpText = otp,
            onOtpTextChange = {
                otp = it
                if(otp.length==6)
                    viewModel.updateOtpState(otp)
            },
            type = OTP_VIEW_TYPE_BOX,
            otpCount = 6,
            containerSize = 48.dp,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            strokeColor = colorStroke,
            charColor = White)

        Spacer(modifier = Modifier.size(20.dp))
        var text by remember { mutableStateOf("") }
        var totalTime: Int = 60
        if(resentOTPText){
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
        }
        else
            text =  "Resend OTP"

        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth(.8f)) {
            androidx.compose.material.Text(
                text = text,
                textDecoration = TextDecoration.Underline,
                style = subtitle,
                modifier = Modifier.clickable {
                    if(!resentOTPText) {
                        viewModel.updateResendText(true)
                        val dataModel = PhoneOtpReq("+91${viewModel.phoneNumberState.trim()}", true)
                        viewModel.getPostPhoneOtpData(dataModel, "signup")
                    }
                })
        }

        Spacer(modifier = Modifier.size(20.dp))

        Button(onClick = {
            if(viewModel.otpState.trim().length<6){
                activity.makeToast("please enter otp")
            }
            else {
                AnalyticsManager.signupOTPButton()
                globalInstance.verifyOTPBtn = true
                globalInstance.traceVerifyOTPBtn = FirebasePerformance.getInstance().newTrace("verify_otp_btn")
                globalInstance.traceVerifyOTPBtn.start()
                viewModel.updateSignUpState("loading")
                viewModel.updateSignUpLoadingText("Verify the OTP.....")
                val dataModel = PhoneVerifyReq("+91" + viewModel.phoneNumberState.trim(), viewModel.otpState.trim())
                viewModel.getPostPhoneVerifyData(dataModel, "signup")
            }
        },
            modifier = Modifier.fillMaxWidth(.8f).padding(top = 10.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White)
            /*disabledContainerColor = Color.White)*/
        ) {
            Text(text = "Sign up", style = subtitle.copy(color = Color.Black))
        }
    }
}

@Composable
fun LoginComplete(landscape: Boolean = false) {
    val imageLoader = ImageLoader.Builder(LocalContext.current).components {
        if (SDK_INT >= 28) {
            add(ImageDecoderDecoder.Factory())
        } else {
            add(GifDecoder.Factory())
        }
    }.build()
    Theme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = rememberAsyncImagePainter(R.drawable.complete, imageLoader),
                contentDescription = null,
                modifier = Modifier.width(if(landscape) (LocalConfiguration.current.screenWidthDp / 1.5).dp
                else (LocalConfiguration.current.screenWidthDp).dp)
                    .height(if(landscape) (LocalConfiguration.current.screenWidthDp / 4.5).dp
                    else (LocalConfiguration.current.screenWidthDp).dp)
                // .then(size)
                ,
                contentScale = ContentScale.Crop
            )
        }
    }
}





@Composable
fun TextTheme(
    darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = DarkColorPalette.copy(
            primary = MaterialTheme.colors.secondary, onSurface = MaterialTheme.colors.secondary
        ), typography = typography, content = content, shapes = shapes
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StateLocationDropDownMenu(
    state: String,
    options: List<String>,
    label: String,
    viewModel: AuthenticateViewModel
) {
    var text by remember { mutableStateOf(state) }
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }) {
        Box {
            if (text.isEmpty()) {
                Text(text = label, color = White, modifier = Modifier.padding(start = 12.dp, top = 4.dp))
            }

            BasicTextField(
                readOnly = true,
                value = text,
                onValueChange = { newText ->
                    text = newText
                    expanded = true
                },
                modifier = Modifier.fillMaxWidth().menuAnchor().padding(start = 12.dp, top = 4.dp),
                textStyle = TextStyle(color = White, fontSize = 14.sp),
                singleLine = true)
        }

        ExposedDropdownMenu(
            modifier = Modifier.background(DarkGray),
            expanded = expanded,
            onDismissRequest = { expanded = false }) {
            options.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item, color = textColorWhite) },
                    onClick = {
                        text = item
                        expanded = false
                        viewModel.updateStateLocation(item)
                    })
            }
        }
    }
}

