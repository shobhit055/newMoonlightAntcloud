@file:OptIn(ExperimentalMaterialApi::class)

package com.antcloud.app.screen.account

import android.util.DisplayMetrics
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Hd
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.Color.Companion.Red
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.antcloud.app.screen.game.PlayButton
import com.antcloud.app.screen.support.CheckBox
import com.antcloud.app.R
import com.antcloud.app.Theme

import com.antcloud.app.common.AppUtils.Companion.saveRefreshTokenData

import com.antcloud.app.components.Loading

import com.antcloud.app.components.OTP_VIEW_TYPE_BOX
import com.antcloud.app.components.OtpView
import com.antcloud.app.components.isPhoneNumberValid
import com.antcloud.app.components.makeToast
import com.antcloud.app.components.signOut
import com.antcloud.app.data.ForgotPasswordReq
import com.antcloud.app.data.PhoneOtpReq
import com.antcloud.app.data.PhoneVerifyReq
import com.antcloud.app.data.UpdatePhoneReq
import com.antcloud.app.data.UpdateResolutionReq
import com.antcloud.app.screen.auth.LoginComplete
import com.antcloud.app.viewmodel.UserViewModel
import com.google.firebase.perf.FirebasePerformance
import com.antcloud.app.activity.NavActivity
import com.antcloud.app.common.AnalyticsManager
import com.antcloud.app.common.DrawerScreens
import com.antcloud.app.common.GlobalData
import com.antcloud.app.theme.BlueGradient
import com.antcloud.app.theme.dark_grey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue


enum class BottomSheetState {
    LOADING, EDIT_PLAN, EDIT_RESOLUTION, EDIT_PASSWORD,  EDIT_PHONE, COMPLETE, EDIT_EMAIL, ENTER_OTP;
}

var currentPlan: String = ""
var plan = ""
private lateinit var currentActivity: NavActivity

@OptIn(ExperimentalMaterialApi::class)
fun accountNav(
    navGraph: NavGraphBuilder,
    activity: NavActivity,
    updateToolbar: ((String) -> Unit),
    navigate: ((String) -> Unit)) {
    return navGraph.composable(DrawerScreens.Account.route) {
        val viewModel: UserViewModel = hiltViewModel()
        val bottomSheetState = rememberModalBottomSheetState(
            initialValue = ModalBottomSheetValue.Hidden, animationSpec = tween(
                durationMillis = 200, delayMillis = 0, easing = FastOutLinearInEasing))
        currentActivity = activity
        val coroutineScope = rememberCoroutineScope()
        val toggle: ((Boolean) -> Unit) = {
            coroutineScope.launch {
                if (it) {
                    bottomSheetState.show()
                }
                else {
                    bottomSheetState.hide()
                }
            }
        }

        LaunchedEffect(key1 = Unit) {
            viewModel.initializeAccountData()
        }
        updateToolbar("Account")
        var data by remember { mutableStateOf(viewModel.accountData) }
        viewModel.subAccountData = { data = it }
        var sheetState by remember { mutableStateOf(viewModel.bottomSheetState) }

        viewModel.subBottomSheetState = { sheetState = it }


        Theme {
            ModalBottomSheetLayout(
                sheetState = bottomSheetState,
                sheetContent = {
                    BottomSheet(activity, toggle, sheetState = sheetState, viewModel = viewModel)
                },
                scrimColor = Transparent,
                sheetBackgroundColor = Black,
                sheetElevation = 20.dp,
                sheetShape = RoundedCornerShape(20.dp)) {
//                LazyColumn {
//                    item {
                AccountScreen(activity = activity, viewModel = viewModel, navigate = navigate, toggle)
                BackHandler(enabled = bottomSheetState.isVisible) {
                    coroutineScope.launch {
                        bottomSheetState.hide()
                    }
                }
            }


        }
    }
}


@Composable
fun AccountScreen(
    activity: NavActivity,
    viewModel: UserViewModel,
    navigate: (String) -> Unit,
    toggle: (Boolean) -> Unit) {
    val postPhoneOtpState =  viewModel.postPhoneOtpState.value
    val postPhoneVerifyState = viewModel.postPhoneVerifyState.value
    val updatePhoneState =  viewModel.updatePhoneState.value
    val updateResolutionState =  viewModel.updateResolutionState.value
    val forgotState =  viewModel.forgotState.value
    val resendVerificationEmailState = viewModel.resendVerificationEmailState.value
    var calledRefresh = false
    val refreshTokenState = viewModel.refreshTokenState.value
    var apiName  =""

    when (refreshTokenState.success) {
        1 -> {
            LaunchedEffect(Unit) {
                calledRefresh = true
                if ((refreshTokenState.accessToken != "") && (refreshTokenState.refreshToken != "")) {
                    saveRefreshTokenData(activity,refreshTokenState.accessToken,refreshTokenState.refreshToken)
                    when (apiName) {
                        "phoneUpdate" -> updatePhoneNumber(viewModel)
                        "resUpdate" ->  updateResolution(viewModel)
                        "resendMail" -> resendVerificationEmail(viewModel)

                    }
                }
            }
        }
        0 -> {
            LaunchedEffect(Unit) {
                calledRefresh = true
                signOut(activity, viewModel)
                activity.makeToast(refreshTokenState.error)
            }
        }
    }

    when(postPhoneOtpState.success){
        1->{
            LaunchedEffect(key1 = Unit) {
                onAreaChanged(toggle, viewModel, BottomSheetState.ENTER_OTP)
            }
        }
        0->{
            LaunchedEffect(Unit) {
                onAreaChanged(toggle , viewModel , BottomSheetState.EDIT_PHONE)
                when(postPhoneOtpState.errorCode){
                    400 -> {
                        val message  = postPhoneOtpState.error
                        Log.i("test" , "" +message)
                        if(message.contains("Error 803"))
                            activity.makeToast(message)
                        else if (message.equals("This number already exists!")) {
                            activity.makeToast("Error 104A : Number already in use.")
                        } else if (message.equals("Invalid Number provided!")) {
                            activity.makeToast("Error 104A1 : Please enter a valid number")
                        } else {
                            activity.makeToast("Error 104A2 : Something went wrong. Please try again after sometime.")
                        }
                    }
                    else -> {
                        if(postPhoneOtpState.error!="")
                            activity.makeToast(postPhoneOtpState.error)
                    }
                }
            }
        }
    }

    when(postPhoneVerifyState.success) {
        1 -> {
            LaunchedEffect(Unit) {
                if(globalInstance.otpSubmit){
                    globalInstance.otpSubmit =  false
                    globalInstance.traceOtpSubmit.stop()
                }
                globalInstance.updateUserPhone = true
                globalInstance.traceUpdateUserPhone=  FirebasePerformance.getInstance().newTrace("update_user_phone_api")
                globalInstance.traceUpdateUserPhone.start()
                onAreaChanged(toggle , viewModel , BottomSheetState.LOADING)
                updatePhoneNumber(viewModel)
            }
        }
        0 -> {
            LaunchedEffect(Unit) {
                if(globalInstance.otpSubmit){
                    globalInstance.otpSubmit =  false
                    globalInstance.traceOtpSubmit.stop()
                }
                onAreaChanged(toggle, viewModel, BottomSheetState.ENTER_OTP)
                when (postPhoneVerifyState.errorCode) {
                    400 ->  {
                        val message = postPhoneVerifyState.error
                        if (message.contains("Error 804")) {
                            activity.makeToast(message)
                        } else if (message.equals("Invalid OTP")) {
                            activity.makeToast("Error 105A : Please enter a valid OTP")
                        } else if (message.equals("OTP Expired!")) {
                            activity.makeToast("Error 105A1 : OTP has expired")
                        } else {
                            activity.makeToast("Error 105A2 : Something Went Wrong")
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

    when(updateResolutionState.success){
        1->{
            LaunchedEffect(Unit) {
                if(globalInstance.updateResolution){
                    globalInstance.updateResolution = false
                    globalInstance.traceUpdateResolution.stop()
                }

                viewModel.updateResolution(updateResolutionState.resolution)
                AnalyticsManager.changeResolutionButton(viewModel.selectedResolution)
                activity.makeToast(updateResolutionState.message)
                onAreaChanged(toggle, viewModel, BottomSheetState.COMPLETE)
            }
        }
        0->{
            if(globalInstance.updateResolution){
                globalInstance.updateResolution = false
                globalInstance.traceUpdateResolution.stop()
            }
            LaunchedEffect(Unit) {
                onAreaChanged(toggle, viewModel, BottomSheetState.EDIT_RESOLUTION)
                val msg = updateResolutionState.error
                if (updateResolutionState.errorCode == 401) {
                    if (!calledRefresh) {
                        apiName = "resUpdate"
                        viewModel.getRefreshTokenData("JWT ${GlobalData.getInstance().accountData.refreshToken}")
                    }
                    else {
                        activity.makeToast(msg)
                        calledRefresh = false
                        signOut(activity, viewModel)
                    }
                } else if (updateResolutionState.errorCode == 403) {
                    activity.makeToast(msg)
                    signOut(activity, viewModel)
                } else {
                    if (msg != "")
                        activity.makeToast(msg)
                }
            }
        }
    }

    when(forgotState.success){
        1->{
            LaunchedEffect(Unit) {
                onAreaChanged(toggle, viewModel, BottomSheetState.COMPLETE)
                //Log.i("testt" , "" +forgotState.message)
                activity.makeToast("Reset password email sent")
            }
        }
        0->{
            LaunchedEffect(Unit) {
                onAreaChanged(toggle, viewModel, BottomSheetState.EDIT_PASSWORD)
                if (forgotState.error != "")
                    activity.makeToast(forgotState.error)
            }
        }

    }

    when(resendVerificationEmailState.success){
        1->{
            LaunchedEffect(Unit) {
                onAreaChanged(toggle , viewModel , BottomSheetState.COMPLETE)
                activity.makeToast(resendVerificationEmailState.message)
            }
        }
        0->{
            LaunchedEffect(Unit) {
                onAreaChanged(toggle, viewModel, BottomSheetState.EDIT_EMAIL)
                val msg = resendVerificationEmailState.error
                if (resendVerificationEmailState.errorCode == 401) {
                    if (!calledRefresh) {
                        apiName = "resendMail"
                        viewModel.getRefreshTokenData("JWT ${GlobalData.getInstance().accountData.refreshToken}")
                    } else {
                        activity.makeToast(msg)
                        calledRefresh = false
                        signOut(activity, viewModel)
                    }
                } else if (resendVerificationEmailState.errorCode == 403) {
                    activity.makeToast(msg)
                    signOut(activity, viewModel)
                } else {
                    if (msg != "")
                        activity.makeToast(msg)
                }
            }
        }
    }

    var user by remember {
        mutableStateOf(viewModel.accountData)
    }
    viewModel.subAccountData = {
        user = it
    }
    when(updatePhoneState.success){
        1->{
            LaunchedEffect(Unit) {
                if(globalInstance.updateUserPhone){
                    globalInstance.updateUserPhone = false
                    globalInstance.traceUpdateUserPhone.stop()
                }
                user.phone = updatePhoneState.phone
                activity.makeToast(updatePhoneState.message)
                onAreaChanged(toggle, viewModel, BottomSheetState.COMPLETE)
            }
        }
        0->{
            LaunchedEffect(Unit) {
                if(globalInstance.updateUserPhone){
                    globalInstance.updateUserPhone = false
                    globalInstance.traceUpdateUserPhone.stop()
                }
                onAreaChanged(toggle, viewModel, BottomSheetState.EDIT_PHONE)
                val msg = updatePhoneState.error
                if (updatePhoneState.errorCode == 401) {
                    if (!calledRefresh) {
                        apiName = "phoneUpdate"
                        viewModel.getRefreshTokenData("JWT ${GlobalData.getInstance().accountData.refreshToken}")

                    } else {
                        activity.makeToast(msg)
                        calledRefresh = false
                        signOut(activity, viewModel)
                    }
                }
                else if (updatePhoneState.errorCode == 403) {
                    activity.makeToast(msg)
                    signOut(activity, viewModel)
                }
                else {
                    if (msg != "")
                        activity.makeToast(msg)
                }
            }
        }
    }




    var editable by remember {
        mutableStateOf(false)
    }

    val scrollState = rememberScrollState()

    var currentResolution by remember {
        mutableStateOf(viewModel.currentRes)
    }

    viewModel.subCurrentRes = {
        currentResolution = it
    }
    val displayMetrics: DisplayMetrics = activity.resources.displayMetrics
    val screenWidth = (displayMetrics.widthPixels / displayMetrics.density).toInt()
    val landscape = screenWidth >= 600

    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
            .verticalScroll(state = scrollState),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.size(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth(0.9f),
            verticalAlignment = Alignment.CenterVertically) {
            AsyncImages(
                url = "https://api.dicebear.com/7.x/initials/svg?backgroundColor=2EDBD0&radius=50&seed=${user.firstName} ${user.lastName}",
                modifier = Modifier.size(90.dp))
            Spacer(modifier = Modifier.size(20.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.SpaceBetween) {
                Text(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    text = "${user.firstName} ${user.lastName}",
                    fontSize = 20.sp,
                    color = MaterialTheme.colors.secondary,
                    fontWeight = FontWeight.Medium)

                Spacer(modifier = Modifier.size(10.dp))

                PlayButton(modifier = if (landscape) Modifier.align(Alignment.End).fillMaxWidth(0.3f)
                else Modifier.fillMaxWidth(1f),
                    text = if (editable) "Save Changes" else "Edit Profile",
                    icon = if (editable) Icons.Filled.Save else Icons.Filled.Edit) {
                    AnalyticsManager.accountEditButton(editable)
                    editable = !editable
                }
            }
        }
        Column(
            modifier = Modifier.fillMaxWidth(.9f),
            horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.size(15.dp))
            Divider(color = MaterialTheme.colors.secondary.copy(alpha = .5f), thickness = 1.dp)
            Spacer(modifier = Modifier.size(10.dp))

            DataRow(heading = "Email",
                text = user.email,
                editable = editable,
                onClick = {
                    if(user.emailVerified) {}
                    else
                        onAreaChanged(toggle,viewModel, BottomSheetState.EDIT_EMAIL)
                })
            DataRow(heading = "Phone",
                text = user.phone,
                editable = editable,
                onClick = {
                onAreaChanged(toggle,viewModel, BottomSheetState.EDIT_PHONE)
            })
            DataRow(
                heading = "Password",
                text = "*********",
                editable = editable,
                onClick = {
                    onAreaChanged(toggle,viewModel, BottomSheetState.EDIT_PASSWORD)
                })
            Spacer(modifier = Modifier.size(10.dp))
            Divider(color = MaterialTheme.colors.secondary.copy(alpha = .5f), thickness = 1.dp)
            Spacer(modifier = Modifier.size(10.dp))

            currentPlan = user.currentPlan
            var upcomingPlan = ""
            if (user.upcomingPlans.isNotEmpty()) {
                upcomingPlan = user.upcomingPlans[0].planName
            }

            for (i in GlobalData.getInstance().androidData.pricing.indices) {
                for (j in GlobalData.getInstance().androidData.pricing[i].items.indices) {
                    if (currentPlan == GlobalData.getInstance().androidData.pricing[i].items[j].userPlan) {
                        plan = GlobalData.getInstance().androidData.pricing[i].items[j].display
                    }
                    if (upcomingPlan == GlobalData.getInstance().androidData.pricing[i].items[j].userPlan) {
                        upcomingPlan =
                            GlobalData.getInstance().androidData.pricing[i].items[j].display
                    }
                }
            }

            if (plan == "") {
                plan = currentPlan
            }

            if(currentPlan != "Basic") {
                DataRow(
                    heading = "Plan",
                    editable = editable,
                    text = plan.uppercase(),
                    onClick = {
                        onAreaChanged(toggle,viewModel, BottomSheetState.EDIT_PLAN)
                    })
                DataRow(
                    heading = "Resolution",
                    editable = editable,
                    text = if(currentResolution == "1440") "2K" else if(currentResolution == "2160") "4K" else currentResolution,
                    onClick = {
                        onAreaChanged(toggle,viewModel, BottomSheetState.EDIT_RESOLUTION)
                    })
                if(user.upcomingPlans.isNotEmpty()) {
                    DataRow(
                        heading = "Upcoming Plan",
                        text = upcomingPlan.uppercase(),
                        onClick = {  },
                        editable = editable)
                }
                Spacer(modifier = Modifier.size(10.dp))
                Divider(color = MaterialTheme.colors.secondary.copy(alpha = .5f), thickness = 1.dp)
                Spacer(modifier = Modifier.size(25.dp))
            }
            else {
                PlayButton(
                    text = stringResource(id = R.string.checkout_plans_button),
                    icon = Icons.Filled.Payments,
                    onClick = {
                        AnalyticsManager.checkoutPlan()
                        navigate(DrawerScreens.Pricing.route)
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondary),
                    modifier = Modifier.fillMaxWidth(0.9f),
                    shape = RoundedCornerShape(10.dp))
                Spacer(modifier = Modifier.size(5.dp))
            }

            if (editable) {
//                    PlayButton(text = "Save Changes", icon = Icons.Filled.Save) {
//                        editable = false
//                    }
            }
            else {
//                Row(modifier = Modifier
//                    .wrapContentSize(Alignment.Center)
//                    .align(Alignment.CenterHorizontally)
//                    .padding(top = 0.dp)
//                ) {
//                    Button(
//                        onClick = {
//                             AnalyticsManager.controllerMappingButton()
//
//                        },
//                        shape = RoundedCornerShape(5.dp),
//                        modifier = if (landscape) Modifier.fillMaxWidth(0.4f) else Modifier.weight(1f),
//                        colors = ButtonDefaults.buttonColors(
//                            backgroundColor = PinkGradient)) {
//                        Text(
//                            text = "Controller Mapping",
//                            fontSize = 12.sp,
//                            modifier = Modifier
//                                .padding(6.dp)
//                                .align(Alignment.CenterVertically)
//                                .fillMaxWidth(0.9f),
//                            style = mainTitle.copy(fontWeight = FontWeight.Normal),
//                            color = MaterialTheme.colors.secondary)
//                    }
//                }
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 30.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly) {
                    NavButton(location = DrawerScreens.Policy) {
                        AnalyticsManager.privacyPolicyButton()
                        navigate(DrawerScreens.Policy.route)
                    }
                    NavButton(location = DrawerScreens.FAQs) {
                        AnalyticsManager.faqButton()
                        navigate(DrawerScreens.FAQs.route)
                    }
                    NavButton(location = DrawerScreens.Terms) {
                        AnalyticsManager.tcButton()
                        navigate(DrawerScreens.Terms.route)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.size(25.dp))
    }
}

@Composable
fun NavButton(location: DrawerScreens, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(Color.Transparent),
        modifier = Modifier
            .size(width = 110.dp, height = 85.dp),
        elevation = ButtonDefaults.elevation(defaultElevation = 0.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxHeight()
                .shadow(elevation = 0.dp)
        ) {
            Icon(imageVector = location.icon,
                contentDescription = "",
                tint = MaterialTheme.colors.secondary,
                modifier = Modifier
                    .size(width = 40.dp, height = 30.dp)
                    .padding(bottom = 5.dp)
            )
            Text(fontSize = 11.sp,
                text = location.title,
                color = MaterialTheme.colors.secondary,
                style = MaterialTheme.typography.button,
                textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun DataRow(heading: String, text: String, onClick: (() -> Unit), editable: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = if (editable) 0.dp else 15.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {
        Text(
            fontSize = 16.sp,
            text = heading,
            color = MaterialTheme.colors.secondary,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1F))

        if (editable) {
            OutlinedButton(
                onClick = onClick,
                modifier = Modifier.weight(2F),
                border = BorderStroke(1.dp, MaterialTheme.colors.secondary.copy(alpha = 0.5f))) {
                Text(
                    text = text,
                    color = MaterialTheme.colors.secondary,
                    fontSize = 16.sp)
            }
        }
        else {
            Text(
                text = text,
                color = MaterialTheme.colors.secondary,
                modifier = Modifier.weight(2F),
                fontSize = 12.sp)
        }
    }
}

@Composable
fun BottomSheet(
    activity: NavActivity,
    toggle: (Boolean) -> Unit,
    sheetState: BottomSheetState,
    viewModel: UserViewModel) {
    val displayMetrics: DisplayMetrics = currentActivity.resources.displayMetrics
    val screenWidth = (displayMetrics.widthPixels / displayMetrics.density).toInt()
    val landscape = screenWidth >= 600

    if (sheetState != BottomSheetState.EDIT_RESOLUTION) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)) {
            when (sheetState) {
                BottomSheetState.LOADING -> Loading()

                BottomSheetState.EDIT_EMAIL -> BottomSheetBoilerPlate(
                    heading = "Verify Your Email",
                    icon = Icons.Filled.Email) {
                    EditEmail(toggle,viewModel)
                }

                BottomSheetState.EDIT_PHONE -> BottomSheetBoilerPlate(
                    heading = "Change Your Number",
                    icon = Icons.Filled.Phone) {
                    EditPhone(toggle ,  viewModel , activity)
                }

                BottomSheetState.EDIT_PASSWORD -> BottomSheetBoilerPlate(
                    heading = "Change Your Password",
                    icon = Icons.Filled.Password) {
                    EditPassword(toggle , viewModel)
                }

                BottomSheetState.EDIT_PLAN -> BottomSheetBoilerPlate(
                    heading = "Your Plan Details",
                    icon = Icons.Filled.Hd) {
                    EditPlan()
                }

                BottomSheetState.EDIT_RESOLUTION -> BottomSheetBoilerPlate(
                    heading = "Change Your Resolution",
                    icon = Icons.Filled.Hd) {
                    EditResolution(toggle ,viewModel)
                }

                BottomSheetState.ENTER_OTP -> BottomSheetBoilerPlate(heading = "Enter OTP",
                    icon = Icons.Filled.Security) {
                    EnterOtp(toggle,viewModel)
                }

                BottomSheetState.COMPLETE -> LoginComplete(landscape = landscape)
            }
        }
    }

    else {
        Column(modifier = Modifier.fillMaxWidth()
                .height((LocalConfiguration.current.screenHeightDp * 0.45).dp)) {
            BottomSheetBoilerPlate(
                heading = "Change Your Resolution",
                icon = Icons.Filled.Hd) {
                EditResolution( toggle,  viewModel)
            }
        }
    }


}

@Composable
fun BottomSheetBoilerPlate(heading: String, icon: ImageVector, content: @Composable (() -> Unit)) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
//        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxHeight()) {
        Row(
            modifier = Modifier.background(MaterialTheme.colors.primary).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center) {
            Icon(
                imageVector = icon,
                contentDescription = "",
                tint = MaterialTheme.colors.secondary)
            Text(
                text = heading,
                color = MaterialTheme.colors.secondary,
                fontSize = 24.sp,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                textAlign = TextAlign.Center)
        }
        Divider(color = MaterialTheme.colors.secondary.copy(alpha = .5f), thickness = 1.dp)
        content()
    }
}

@Composable
fun EditEmail(toggle: (Boolean) -> Unit, viewModel: UserViewModel) {

    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.size(20.dp))
        Text(
            text = "Press the button below resent verification link.",
            color = MaterialTheme.colors.secondary,
        )
        Spacer(modifier = Modifier.size(20.dp))
        PlayButton(
            text = "Resend Verification Link",
            icon = Icons.Filled.Send) {
            AnalyticsManager.emailVerificationButton()
            onAreaChanged(toggle , viewModel , BottomSheetState.LOADING)
           resendVerificationEmail(viewModel)
        }
        Spacer(modifier = Modifier.size(20.dp))
    }
}

@Composable
fun EditPhone(toggle: (Boolean) -> Unit, viewModel: UserViewModel, activity: NavActivity) {

    var phoneNumber =  viewModel.accountData.phone
    if(phoneNumber.contains("+91")){
        phoneNumber =  phoneNumber.substring(3)
    }
    var number by remember {
        mutableStateOf(phoneNumber)
    }
    Spacer(modifier = Modifier.size(20.dp))

    var isErrorPhone by remember { mutableStateOf(true) }
    val phoneColor = if (isErrorPhone) White else Red
    BasicTextField(
        value = number,
        onValueChange = { newValue ->
            number = newValue
            viewModel.newNumber = newValue
            isErrorPhone = newValue.length == 10
        },
        textStyle = TextStyle(color = White, fontSize = 14.sp),
        cursorBrush = SolidColor(BlueGradient),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier =  Modifier.fillMaxWidth(0.8f).border(1.dp, phoneColor, RoundedCornerShape(16.dp)).padding(12.dp),
        singleLine = true,
        decorationBox = { innerTextField ->
            Box(Modifier.padding(start = 2.dp)) {
                if (phoneNumber.isEmpty()) {
                    androidx.compose.material3.Text("Phone Number", color = Color.White)
                }
                innerTextField()
            }
        })

    Spacer(modifier = Modifier.size(10.dp))


    PlayButton(
        text = "Generate OTP",
        icon = Icons.AutoMirrored.Filled.Send,
        onClick = {
            if(number.length == 10) {
                onAreaChanged(toggle, viewModel, BottomSheetState.LOADING)
                val dataModel = PhoneOtpReq("+91$number", true)
                viewModel.getPostPhoneOtpData(dataModel, "account")
            }
            else
                activity.makeToast("please enter valid phone number")
        },
        colors = ButtonDefaults.buttonColors(
            backgroundColor = MaterialTheme.colors.secondary,
            disabledBackgroundColor = MaterialTheme.colors.secondary.copy(
                alpha = 0.5f
            ), disabledContentColor = dark_grey
        ),
         disabled = !number.isPhoneNumberValid()
    )
    Spacer(modifier = Modifier.size(20.dp))
}

@Composable
fun EditPassword(toggle: (Boolean) -> Unit, viewModel: UserViewModel) {

    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.size(20.dp))
        Text(
            text = "Press the button below to set a new password.",
            color = MaterialTheme.colors.secondary,
        )
        Spacer(modifier = Modifier.size(20.dp))
        PlayButton(
            text = "Reset Password",
            icon = Icons.Filled.Send
        ) {
            globalInstance.forgotPassword  =  true
            globalInstance.traceForgotPassword=  FirebasePerformance.getInstance().newTrace("forgot_password")
            globalInstance.traceForgotPassword.start()

            onAreaChanged(toggle , viewModel , BottomSheetState.LOADING)
            val dataModel = ForgotPasswordReq(viewModel.accountData.email)
            viewModel.getForgotData(dataModel,"account")
        }
        Spacer(modifier = Modifier.size(20.dp))
    }
}


@Composable
fun EditPlan() {
    val user = GlobalData.getInstance().accountData
    var date = user.renewDate
    var dt = "N/A"
    if(date != "") {
        date = date.substring(0, 10)
        val y = date.substring(0, 4)
        var m = date.substring(5, 7)
        val d = date.substring(8, 10)
        m = when (m) {
            "01" -> "January"
            "02" -> "February"
            "03" -> "March"
            "04" -> "April"
            "05" -> "May"
            "06" -> "June"
            "07" -> "July"
            "08" -> "August"
            "09" -> "September"
            "10" -> "October"
            "11" -> "November"
            "12" -> "December"
            else -> "Undefined"
        }
        dt = "$d $m $y"
    }

    val timeDiff = user.totalTimeMonth - user.timeUsedMonth
    val h = timeDiff / 60
    val min = timeDiff % 60
    val timeLeft = "$h hours and ${min.absoluteValue} minutes"
    /*val timeLeft = if (currentPlan == "Trial" && user.totalTimeMonth == 0) {
        "0";
    } else if (currentPlan == "Unlimited") {
        "Unlimited"
    } else {
        val h = floor(timeDiff.toDouble() / 60)
        val min = timeDiff % 60
        val t = "$h hours and $min minutes"
        t
    }*/

    Spacer(modifier = Modifier.size(20.dp))
    Text(
        text = "Current Plan: $plan",
        color = MaterialTheme.colors.secondary,
    )
    Spacer(modifier = Modifier.size(10.dp))
    Text(
        text = "Time Left: $timeLeft",
        color = MaterialTheme.colors.secondary,
    )
    Spacer(modifier = Modifier.size(10.dp))
    Text(
        text = "Expires On: $dt",
        color = MaterialTheme.colors.secondary,
    )
    Spacer(modifier = Modifier.size(20.dp))
}

@Composable
fun EditResolution(toggle: (Boolean) -> Unit, viewModel: UserViewModel) {

    Spacer(modifier = Modifier.size(20.dp))
    Text(
        text = "Choose your preferred resolution from the option below.",
        color = MaterialTheme.colors.secondary,
    )
    Spacer(modifier = Modifier.size(10.dp))
    GlobalData.getInstance().androidData.quality = GlobalData.getInstance().remoteDataResolution
    val resolution =  GlobalData.getInstance().androidData.quality
    var selectedResolution by remember {
        mutableStateOf(viewModel.selectedResolution)
    }

    var disabled = false
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 15.dp, horizontal = 20.dp)
    ) {
        items(resolution) {
            disabled = if (GlobalData.getInstance().accountData.currentPlan ==  GlobalData.getInstance().androidData.pricing[0].items[0].userPlan && it.quality != "720") {
                true
            }
            else  GlobalData.getInstance().accountData.currentPlan !=  GlobalData.getInstance().androidData.pricing[1].items[1].userPlan
                    && (it.quality == "1440" || it.quality == "2160")


            CheckBox(title =
                    if(it.quality == "1440") "2K"
                    else if(it.quality == "2160") "4K"
                    else it.quality,
                selected = it.quality == selectedResolution,
                enabled = !disabled,
                activitiy = currentActivity
            ) {
                selectedResolution = it.quality
                viewModel.selectedResolution = it.quality
            }
        }
    }
    Spacer(modifier = Modifier.size(10.dp))
    PlayButton(
        text = "Change Resolution",
        icon = Icons.Filled.Send,
        onClick = {
            globalInstance.updateResolution = true
            globalInstance.traceUpdateResolution=  FirebasePerformance.getInstance().newTrace("update_resolution")
            globalInstance.traceUpdateResolution.start()
            onAreaChanged(toggle , viewModel , BottomSheetState.LOADING)
            updateResolution(viewModel)
        })
    Spacer(modifier = Modifier.size(20.dp))
}



@Composable
fun EnterOtp(toggle: (Boolean) -> Unit, viewModel: UserViewModel) {
    var otp by remember {
        mutableStateOf("")
    }
    Spacer(modifier = Modifier.size(20.dp))
    OtpView(
        otpText = otp,
        onOtpTextChange = {
            otp = it
            viewModel.otpEntered = it
        },
        type = OTP_VIEW_TYPE_BOX,
        otpCount = 6,
        containerSize = 48.dp,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        strokeColor = White,
        charColor = White)

    Spacer(modifier = Modifier.size(10.dp))
    PlayButton(
        text = "Submit OTP",
        icon = Icons.Filled.Send,
        onClick = {
            onAreaChanged(toggle , viewModel , BottomSheetState.LOADING)
            val dataModel = PhoneVerifyReq("+91${viewModel.newNumber.trim()}",viewModel.otpEntered.trim())
            viewModel.getPostPhoneVerifyData(dataModel,"account")
        },
        colors = ButtonDefaults.buttonColors(
            backgroundColor = MaterialTheme.colors.secondary,
            disabledBackgroundColor = MaterialTheme.colors.secondary.copy(
                alpha = 0.5f
            ), disabledContentColor = dark_grey
        ),
        disabled = (otp.length != 6)
    )
    Spacer(modifier = Modifier.size(20.dp))
}



private fun onAreaChanged(
    toggle: (Boolean) -> Unit,
    viewModel: UserViewModel,
    state: BottomSheetState
) {

    toggle(true)
    viewModel.updateBottomSheetState(state)
    if (state == BottomSheetState.COMPLETE) {
        CoroutineScope(Dispatchers.Main).launch {
            delay(2600)
            toggle(false)
            viewModel.initializeAccountData()
        }
    }
}

fun updatePhoneNumber(viewModel: UserViewModel) {
    val updatePhoneModel = UpdatePhoneReq("+91${viewModel.newNumber}")
    viewModel.getUpdatePhoneData("JWT " + GlobalData.getInstance().accountData.token,
        GlobalData.getInstance().accountData.id, updatePhoneModel)
}

fun updateResolution(viewModel: UserViewModel) {
    val updateResolutionReq = UpdateResolutionReq(viewModel.selectedResolution)
    viewModel.getUpdateResolutionData(
        "JWT " + GlobalData.getInstance().accountData.token,
        GlobalData.getInstance().accountData.id, updateResolutionReq)
}

fun resendVerificationEmail(viewModel: UserViewModel){
    viewModel.getResendVerificationEmailData("JWT ${GlobalData.getInstance().accountData.token}")
}

