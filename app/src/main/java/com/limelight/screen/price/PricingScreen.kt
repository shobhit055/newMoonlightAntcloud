package com.limelight.screen.price




import android.content.Intent
import android.util.DisplayMetrics
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.limelight.R
import com.limelight.activity.NavActivity

import com.limelight.common.AppUtils.Companion.saveRefreshTokenData
import com.limelight.components.CustomDialog
import com.limelight.common.DrawerScreens
import com.limelight.common.GlobalData
import com.limelight.components.Loading
import com.limelight.components.Play
import com.limelight.components.isPinCodeValid
import com.limelight.components.makeToast
import com.limelight.components.novaSquare
import com.limelight.components.signOut
import com.limelight.data.ForgotPasswordReq
import com.limelight.data.Location
import com.limelight.data.Notes
import com.limelight.data.PricingCard
import com.limelight.data.PricingGroups
import com.limelight.data.PricingReq
import com.limelight.data.UpdateLocationReq
import com.limelight.screen.auth.TextTheme

import com.limelight.viewmodel.UserViewModel
import com.google.gson.Gson
import com.limelight.activity.WebViewActivity
import com.limelight.theme.heading
import com.limelight.theme.light_grey
import com.limelight.theme.mainTitle
import com.limelight.theme.subtitle
import com.limelight.viewmodel.PricingViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject


val globalInstance  =  GlobalData.getInstance()
enum class PricingImages(val id: Int) {
    H_1(R.drawable.pricing_1),
    H_2(R.drawable.pricing_2),
    M_1(R.drawable.pricing_5),
    M_2(R.drawable.pricing_4),
    M_3(R.drawable.pricing_3),
    M_4(R.drawable.pricing_3)
}

lateinit var coroutineScope : CoroutineScope
lateinit var currentActivity : NavActivity
var pagerState: PagerState? = null

@OptIn(ExperimentalFoundationApi::class)
fun pricingNav(t: NavGraphBuilder, activity: NavActivity, updateToolbar: ((String) -> Unit), navigate: ((String) -> Unit)) {
    return t.composable(DrawerScreens.Pricing.route) {
        currentActivity = activity
        val viewModel : PricingViewModel = hiltViewModel()
        LaunchedEffect(key1 = Unit) {
            viewModel.initializePricingState(GlobalData.getInstance().androidData.pricing)
            viewModel.getCheckPaymentAllowedData()
        }
        updateToolbar("Pricing")

        Column(modifier = Modifier.fillMaxSize()
            .padding(0.dp)
            .verticalScroll(state = rememberScrollState())
            .background(Color.Black)) {
            PricingScreen(activity = activity, viewModel = viewModel , navigate)
        }
    }
}

@ExperimentalFoundationApi
@Composable
fun PricingScreen(activity: NavActivity, viewModel: PricingViewModel, navigate: ((String) -> Unit)) {
    val userViewModel  :UserViewModel = hiltViewModel()
    var calledRefresh = false
    var apiName  =""
    val scrollState = rememberScrollState()
    GlobalData.getInstance().androidData.pricing =   GlobalData.getInstance().remoteDataPricing
    val pricingData =  GlobalData.getInstance().androidData.pricing
    val createPricingOrderState  = viewModel.createPricingOrderState.value
    val verifyCouponCodeState = viewModel.verifyCouponCodeState.value
    val checkPaymentAllowedState = viewModel.checkPaymentAllowedState.value
    val updateLocationState = viewModel.updateLocationState.value
    val addToWaitListState = viewModel.addToWaitListState.value
    val refreshTokenState = userViewModel.refreshTokenState.value


    when (refreshTokenState.success) {
        1 -> {
            LaunchedEffect(Unit) {
                calledRefresh = true
                if ((refreshTokenState.accessToken != "") && (refreshTokenState.refreshToken != "")) {
                    saveRefreshTokenData(activity,refreshTokenState.accessToken,refreshTokenState.refreshToken)
                    when (apiName) {
                        "createOrder" -> createPricingOrderApi(viewModel)
                        "coupSubmit" -> verifyCouponCodeApi(viewModel)
                        "locationUpdate" ->  updateLocationApi(viewModel)
                        "joinWaitList" -> addToWaitListApi(viewModel)
                    }
                }
            }
        }
        0 -> {
            LaunchedEffect(Unit) {
                calledRefresh = true
                signOut(activity, userViewModel)
                activity.makeToast(refreshTokenState.error)
            }
        }
    }
    when(createPricingOrderState.success){
        1 -> {
            LaunchedEffect(Unit) {
                val intent = Intent(activity, WebViewActivity::class.java)
                intent.putExtra("url", "https://antcloud.co/mobilePricing?order=${createPricingOrderState.id}&notes=${
                        Gson().toJson(createPricingOrderState.pricingRespDataNotes)}")
                intent.putExtra("page", "pricing")
                activity.startActivity(intent)
                viewModel.updateCouponState("")
            }
        }

        0 -> {
            LaunchedEffect(Unit) {
                viewModel.appliedCoupon = ""
                viewModel.selectedQuantity = 0
                viewModel.selectedPlan = ""
                val msg = createPricingOrderState.error
                when (createPricingOrderState.errorCode) {
                    400 -> activity.makeToast(msg)
                    401 -> {
                        if (!calledRefresh) {
                                apiName = "createOrder"
                                userViewModel.getRefreshTokenData("JWT ${GlobalData.getInstance().accountData.refreshToken}")
                        } else {
                            activity.makeToast(msg)
                            calledRefresh = false
                            signOut(activity, userViewModel)
                        }
                    }
                    403 -> {
                        activity.makeToast(msg)
                        signOut(activity, userViewModel)
                    }
                    else -> activity.makeToast(msg)
                }
            }
        }
    }
    when(verifyCouponCodeState.success){
        1 -> {
            LaunchedEffect(Unit) {
                viewModel.updateLoadingState(false)
                viewModel.appliedCoupon = viewModel.couponState.uppercase()
                viewModel.updateMessageState(true)
            }
        }
        0 -> {
            LaunchedEffect(Unit) {
                val msg = verifyCouponCodeState.error
                when (verifyCouponCodeState.errorCode) {
                    400 -> viewModel.updateMessageState(false)
                    401 -> {
                        if (!calledRefresh) {
                            apiName = "coupSubmit"
                            userViewModel.getRefreshTokenData("JWT ${GlobalData.getInstance().accountData.refreshToken}")
                        } else {
                            currentActivity.makeToast(msg)
                            calledRefresh = false
                            signOut(activity, userViewModel)
                        }
                    }

                    403 -> {
                        activity.makeToast(msg)
                        signOut(activity, userViewModel)
                    }

                    else -> activity.makeToast(msg)
                }
            }
        }
    }
    when(checkPaymentAllowedState.success){
        1 -> {
            LaunchedEffect(Unit) {
//                val jObject = checkPaymentAllowedState.message?.let { JSONObject(it) }
//                viewModel.updatePaymentsAllowedState(jObject!!.getBoolean("active"))
            }
        }
        0 -> {
            LaunchedEffect(Unit) {
                viewModel.updatePaymentsAllowedState(false)
                activity.makeToast(checkPaymentAllowedState.error)
            }
        }
    }
    when(updateLocationState.success){
        1 -> {
            LaunchedEffect(Unit) {
                activity.makeToast(updateLocationState.message)
                GlobalData.getInstance().accountData.location.State = viewModel.stateLocation
                GlobalData.getInstance().accountData.location.Pincode = viewModel.pinCodeState
            }
        }
        0 -> {
            LaunchedEffect(Unit) {
                val msg = updateLocationState.error
                if (updateLocationState.errorCode == 401) {
                    if (!calledRefresh) {
                            apiName = "locationUpdate"
                            userViewModel.getRefreshTokenData("JWT ${GlobalData.getInstance().accountData.refreshToken}")
                    } else {
                        activity.makeToast(msg)
                        calledRefresh = false
                        signOut(activity, userViewModel)
                    }
                }
                else if (updateLocationState.errorCode == 403) {
                    activity.makeToast(msg)
                    signOut(activity, userViewModel)
                }
                else activity.makeToast(msg)
            }

        }
    }

    when(addToWaitListState.success){
        1 -> {
            LaunchedEffect(Unit) {
                viewModel.updateShowWaitList(false)
                activity.makeToast("Joined Wait List Successfully.")
            }
        }
        0 -> {
            LaunchedEffect(Unit) {
                viewModel.updateShowWaitList(false)
                val msg  = addToWaitListState.error
                if(addToWaitListState.errorCode==401){
                    if(!calledRefresh){
                        apiName = "joinWaitList"
                        userViewModel.getRefreshTokenData("JWT ${GlobalData.getInstance().accountData.refreshToken}")
                    } else {
                        activity.makeToast(msg)
                        calledRefresh = false
                        signOut(activity,userViewModel)
                    }
                }
                else
                    activity.makeToast(msg)
            }
        }
    }

    var tabSelected by remember {
        mutableStateOf(2)
    }

    var pageState by remember {
        mutableStateOf(0)
    }

    var idToken by remember {
        mutableStateOf(viewModel.idToken)
    }

    var coupon by remember {
        mutableStateOf("")
    }

    var loading by remember {
        mutableStateOf(viewModel.loadingState)
    }

    var couponMessage by remember {
        mutableStateOf(viewModel.messageState)
    }

    /*var couponSuccessState by remember {
        mutableStateOf(viewModel.couponSuccessState)
    }*/

    viewModel.subIdToken = {
        idToken = it
    }

    /*viewModel.subCouponSuccessState = {
        couponSuccessState = it
    }*/

    viewModel.subLoadingState = {
        loading = it
    }

    viewModel.subMessageState = {
        couponMessage = it
    }

    viewModel.subCouponState = {
        coupon = it
    }

    viewModel.subSelectedTabState = {
        tabSelected = it!!.toInt()
        if(pagerState != null) {
            coroutineScope.launch {
                pagerState!!.scrollToPage(0)
            }
        }
    }

    viewModel.subPageState = {
        pageState = it
    }

    val openDialogCustom = remember {
        mutableStateOf(viewModel.openLoadingDialogState)
    }
    viewModel.subOpenLoadingDialogState = {
        openDialogCustom.value = it
    }
    coroutineScope = rememberCoroutineScope()

    val showCoupon = remember { mutableStateOf(false) }
    val checkingCoupon = remember {
        mutableStateOf(false)
    }
    val showPlanWarning = remember { mutableStateOf(false) }
    val showRenewWarning = remember { mutableStateOf(false) }
    val showUpdateLocationInformation = remember { mutableStateOf(false) }
    val showWaitList = remember { mutableStateOf(viewModel.showWaitList) }
    val showIntroWarning = remember { mutableStateOf(viewModel.showIntroWarning)}

    var stateLocation by remember {
        mutableStateOf(viewModel.stateLocation)
    }

    var pinCode by remember {
        mutableStateOf(viewModel.pinCodeState)
    }

    viewModel.subStateLocation = {
        stateLocation = it
    }

    viewModel.subPinCodeState = {
        pinCode = it
    }

    viewModel.subShowWaitList = {
        showWaitList.value = it
    }

    viewModel.subShowIntroWarning = {
        showIntroWarning.value = it
    }


    val displayMetrics: DisplayMetrics = activity.resources.displayMetrics
    val screenWidth = (displayMetrics.widthPixels / displayMetrics.density).toInt()
    val landscape = (screenWidth >= 600)
    var introUltimate: Boolean
    var advancedUltimate: Boolean

    if (pricingData == null) {
        Loading()
    }
    else {
        /*if (couponSuccessState) {
            callSubscriptionApi(intent, activity, onEventHandler)
        }*/

        if (openDialogCustom.value) {
            CustomDialog(
                openDialogCustom = openDialogCustom,
                label = "Pricing screen",
                onDismiss = {  }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .background(MaterialTheme.colors.surface)
                        .clip(RoundedCornerShape(10.dp))) {
                    Loading("Loading...")
                }
            }
        }
        Column(
            modifier = Modifier.fillMaxWidth()/*.verticalScroll(state = scrollState)*/,
            horizontalAlignment = Alignment.CenterHorizontally) {
            CustomDialog(
                openDialogCustom = showCoupon,
                label = "PG",
                onDismiss = {
                    if (checkingCoupon.value) {
                        checkingCoupon.value = false
                    }
                    if (viewModel.showCouponError) {
                        viewModel.updateShowCouponError(false)
                    }
                    viewModel.updateCouponState("")
                }) {
                Card(shape = RoundedCornerShape(10.dp),elevation = 28.dp) {
                    if (loading) {
                        Loading("Checking Coupon Validity ....", landscape = landscape)
                        checkingCoupon.value = true
                    }else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth(if (landscape) 0.8f else 0.9f)
                                .background(MaterialTheme.colors.surface),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if(!couponMessage){
                                Text(
                                    text = stringResource(id = R.string.coupon_dialog_title),
                                    color = MaterialTheme.colors.primary,
                                    fontSize = 18.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(top = 25.dp),
                                    style = heading
                                )
                                Text(
                                    text = stringResource(id = R.string.coupon_dialog_body),
                                    color = MaterialTheme.colors.secondary,
                                    fontSize = if (landscape) 16.sp else 12.sp,
                                    textAlign = TextAlign.Start,
                                    modifier = Modifier.padding(top = 15.dp, start = 10.dp, end = 10.dp),
                                    style = subtitle)

                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(top = 20.dp, bottom = 5.dp),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.CenterVertically) {
                                    InputCoupon(
                                        initialValue = coupon,
                                        isError = viewModel.showCouponError) {
                                        viewModel.updateCouponState(it)
                                        if (viewModel.showCouponError) {
                                            viewModel.updateShowCouponError(false)
                                        }
                                    }
                                }
                            }
                            if (checkingCoupon.value) {
                                Text(
                                    text = when (couponMessage) {
                                        true -> "Coupon code applied successfully, you can proceed with your plan purchase"
                                        false -> "Coupon code does not exist!"
                                    },
                                    color = if(couponMessage) MaterialTheme.colors.primary else MaterialTheme.colors.onError.copy(red = 0.9f),
                                    fontSize = if(couponMessage) 18.sp else { if (landscape) 16.sp else 14.sp },
                                    textAlign = if(landscape) TextAlign.Center else TextAlign.Left,
                                    modifier = Modifier
                                        .fillMaxWidth(0.9f)
                                        .padding(vertical = if (couponMessage) 15.dp else 5.dp))
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    //.background(MaterialTheme.colors.primary.copy(alpha = 0.5f))
                                    .padding(top = if (!checkingCoupon.value) 20.dp else 5.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                //verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    modifier = Modifier
                                        .fillMaxWidth(if (landscape) 0.3f else 1f)
                                        .padding(bottom = 20.dp),
                                    shape = RoundedCornerShape(if(landscape) 3.dp else 0.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        backgroundColor = MaterialTheme.colors.primary.copy(alpha = .6f),
                                        disabledBackgroundColor = light_grey),
                                    enabled = coupon.isNotEmpty(),
                                    onClick = {
                                        if(couponMessage) {
                                            showCoupon.value = false
                                            if (checkingCoupon.value) {
                                                checkingCoupon.value = false
                                            }
                                            if (viewModel.showCouponError) {
                                                viewModel.updateShowCouponError(false)
                                            }
                                            viewModel.updateMessageState(false)
                                            viewModel.updateCouponState("")
                                        }
                                        else if (coupon.isNotEmpty()) {
                                            viewModel.updateLoadingState(true)
                                            GlobalData.getInstance().couponSubmit = true
//                                            globalInstance.traceCouponSubmit =  FirebasePerformance.getInstance().newTrace("coupon_submit_api")
//                                            globalInstance.traceCouponSubmit.start()

                                            verifyCouponCodeApi(viewModel)



                                        }/* else {
                                            callSubscriptionApi(intent, activity, onEventHandler)
                                        }*/
                                    }) {
                                    Text(
                                        text = if (couponMessage) "Continue" else "Submit Coupon",
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colors.secondary,
                                        style = MaterialTheme.typography.button.copy(fontSize = 16.sp),
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            CustomDialog(
                openDialogCustom = showPlanWarning,
                label = "Plan Change Warning",
                onDismiss = {}
            ) {
                Card(
                    shape = RoundedCornerShape(10.dp),
                    elevation = 28.dp,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(if (landscape) 0.8f else 0.9f)
                            .background(MaterialTheme.colors.surface),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(id = if(showRenewWarning.value) R.string.renew_warning else R.string.plan_warning),
                            color = MaterialTheme.colors.secondary,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 20.dp, start = 15.dp, end = 15.dp),
                            style = subtitle.copy(fontWeight = FontWeight.Normal)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                //.background(MaterialTheme.colors.primary.copy(alpha = 0.5f))
                                .padding(top = 15.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            //verticalAlignment = Alignment.CenterVertically
                        ) {
                            Spacer(Modifier.weight(0.07f))
                            Button(
                                modifier = Modifier
                                    .weight(0.4f)
                                    .padding(bottom = 20.dp),
                                shape = RoundedCornerShape(3.dp),
                                colors = ButtonDefaults.buttonColors(backgroundColor = light_grey),
                                onClick = {
                                    showPlanWarning.value = false
                                    showRenewWarning.value = false
                                }) {
                                Text(
                                    text = "Go Back",
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colors.secondary,
                                    style = MaterialTheme.typography.button.copy(fontSize = 16.sp),
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                            Spacer(Modifier.weight(0.06f))
                            Button(
                                modifier = Modifier
                                    .weight(0.4f)
                                    .padding(bottom = 20.dp),
                                shape = RoundedCornerShape(3.dp),
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = MaterialTheme.colors.primary.copy(alpha = .6f),
                                    disabledBackgroundColor = light_grey),
                                onClick = {
//                                    val intent = Intent(activity, WebViewActivity::class.java)
//                                    intent.putExtra("url", "https://antcloud.co/mobilePricing?planName=${viewModel.selectedPlan}&quantity=${viewModel.selectedQuantity}&couponCode=${viewModel.appliedCoupon}&idToken=${globalInstance.accountData.token}")
//                                    intent.putExtra("page", "pricing")
//                                    activity.startActivity(intent)
//                                    viewModel.updateCouponState("")
//                                    delayClose(viewModel)
//                                    onPurcahsePlanClick(viewModel,activity)
//                                    showPlanWarning.value = false
//                                    showRenewWarning.value = false
                                }) {
                                Text(
                                    text = "Proceed",
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colors.secondary,
                                    style = MaterialTheme.typography.button.copy(fontSize = 16.sp),
                                    modifier = Modifier.padding(vertical = 8.dp))
                            }
                            Spacer(Modifier.weight(0.07f))
                        }
                    }
                }
            }
            CustomDialog(
                openDialogCustom = showWaitList,
                label = "Join WaitList",
                onDismiss = {}) {
                Card(
                    shape = RoundedCornerShape(10.dp),
                    elevation = 28.dp) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(if (landscape) 0.8f else 0.9f)
                            .background(MaterialTheme.colors.surface),
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(id = R.string.join_waitList),
                            color = MaterialTheme.colors.secondary,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 20.dp, start = 15.dp, end = 15.dp),
                            style = subtitle.copy(fontWeight = FontWeight.Normal))
                        //verticalAlignment = Alignment.CenterVertically
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 15.dp),
                                //.background(MaterialTheme.colors.primary.copy(alpha = 0.5f))
                            horizontalArrangement = Arrangement.SpaceEvenly) {
                            Spacer(Modifier.weight(0.07f))
                            Button(modifier = Modifier
                                .weight(0.4f)
                                .padding(bottom = 20.dp),
                                shape = RoundedCornerShape(3.dp),
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = MaterialTheme.colors.primary.copy(alpha = .6f),
                                    disabledBackgroundColor = light_grey),
                                onClick = {
                                    GlobalData.getInstance().joinWaitList = true
//                                    globalInstance.traceJoinWaitList =  FirebasePerformance.getInstance().newTrace("add_user_to_wait_list_api")
//                                    globalInstance.traceJoinWaitList.start()
                                    addToWaitListApi(viewModel)
                                }) {
                                Text(
                                    text = "Join WaitList",
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colors.secondary,
                                    style = MaterialTheme.typography.button.copy(fontSize = 16.sp),
                                    modifier = Modifier.padding(vertical = 8.dp))
                            }
                            Spacer(Modifier.weight(0.06f))
                            Button(
                                modifier = Modifier
                                    .weight(0.4f)
                                    .padding(bottom = 20.dp),
                                shape = RoundedCornerShape(3.dp),
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = light_grey
                                ),
                                onClick = {
                                    viewModel.updateShowWaitList(false)
                                }) {
                                Text(
                                    text = "Go Back",
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colors.secondary,
                                    style = MaterialTheme.typography.button.copy(fontSize = 16.sp),
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                            Spacer(Modifier.weight(0.07f))
                        }
                    }
                }
            }
            CustomDialog(
                openDialogCustom = showUpdateLocationInformation,
                label = "Update Location Information",
                onDismiss = {}
            ) {
                Card(
                    shape = RoundedCornerShape(10.dp),
                    elevation = 28.dp,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(if (landscape) 0.8f else 0.9f)
                            .background(MaterialTheme.colors.surface),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(id = R.string.plan_update_location),
                            color = MaterialTheme.colors.secondary,
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 25.dp, start = 15.dp, end = 15.dp),
                            style = subtitle.copy(fontWeight = FontWeight.Normal)
                        )
                        Text(
                            text = stringResource(id = R.string.plan_update_location_note),
                            color = MaterialTheme.colors.secondary,
                            fontSize = if(landscape) 16.sp else 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 15.dp, start = 10.dp, end = 10.dp),
                            style = subtitle.copy(fontWeight = FontWeight.Normal)
                        )
                        Spacer(modifier = Modifier.size(10.dp))
//                        StateLocationDropDownMenu(
//                            selectedValue = stateLocation,
//                            options = statesList,
//                            label = "State"
//                        ) {
//                            viewModel.updateStateLocation(it)
//                        }

                        Spacer(modifier = Modifier.size(10.dp))

//                        PinCodeTextField(initialValue = pinCode) {
//                            if(pinCode.length == 7) return
//                            viewModel.updatePinCodeState(pinCode)
//                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 15.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly) {
                            Button(
                                modifier = Modifier
                                    .weight(if (landscape) 0.3f else 1f)
                                    .padding(bottom = 20.dp),
                                shape = RoundedCornerShape(if(landscape) 3.dp else 0.dp),
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = MaterialTheme.colors.primary.copy(alpha = .6f),
                                    disabledBackgroundColor = light_grey
                                ),
                                enabled = stateLocation != "" && pinCode.isPinCodeValid(),
                                onClick = {
                                    showUpdateLocationInformation.value = false
                                    updateLocationApi(viewModel)
                                }) {
                                Text(
                                    text = "Update Information",
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colors.secondary,
                                    style = MaterialTheme.typography.button.copy(fontSize = 16.sp),
                                    modifier = Modifier.padding(vertical = 8.dp))
                            }
                        }
                    }
                }
            }
            CustomDialog(
                openDialogCustom = showIntroWarning,
                label = "Intro Plan Availability Warning",
                onDismiss = {}) {
                Card(
                    shape = RoundedCornerShape(10.dp),
                    elevation = 28.dp) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(if (landscape) 0.8f else 0.9f)
                            .background(MaterialTheme.colors.surface),
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(id = R.string.intro_warning),
                            color = MaterialTheme.colors.secondary,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 20.dp, start = 15.dp, end = 15.dp),
                            style = subtitle.copy(fontWeight = FontWeight.Normal))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 15.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly) {
                            Button(
                                modifier = Modifier
                                    .padding(bottom = 20.dp),
                                shape = RoundedCornerShape(3.dp),
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = light_grey),
                                onClick = {
                                    viewModel.updateShowIntroWarning(false)
                                }) {
                                Text(
                                    text = "OK",
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colors.secondary,
                                    style = MaterialTheme.typography.button.copy(fontSize = 16.sp),
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
            Row(
                modifier = Modifier
                    .padding(vertical = 15.dp)
                    .background(Color.Transparent)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly) {
                for(i in pricingData.indices) {
                    NavButton(viewModel, text = pricingData[i].name, id =  pricingData[i].id.toLongOrNull(), tabSelected = tabSelected)
                }
            }

            val pageCount = pricingData[tabSelected - 1].items.size
            pagerState = rememberPagerState(pageCount = { pageCount })
            val shouldShowPrevButton = remember {
                mutableStateOf(false)
            }
            val shouldShowNextButton = remember {
                mutableStateOf(false)
            }

            shouldShowPrevButton.value = pagerState!!.currentPage > 0
            shouldShowNextButton.value = pagerState!!.currentPage < (pageCount-1)

            Box {
                if (shouldShowPrevButton.value) {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                pagerState!!.animateScrollToPage(pagerState!!.currentPage - 1)
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            /*.background(MaterialTheme.colors.secondary.copy(alpha = 0.2f))
                            .height(150.dp)*/
                            .zIndex(3f)) {
                        Icon(
                            imageVector = Icons.Filled.ChevronLeft,
                            contentDescription = "",
                            modifier = Modifier.size(60.dp),
                            tint = MaterialTheme.colors.secondary.copy(alpha = 0.6f)
                        )
                    }
                }
                if (shouldShowNextButton.value) {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                pagerState?.animateScrollToPage(pagerState!!.currentPage + 1)
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            /*.background(MaterialTheme.colors.secondary.copy(alpha = 0.2f))
                            .height(150.dp)*/
                            .zIndex(3f)) {
                        Icon(
                            imageVector = Icons.Filled.ChevronRight,
                            contentDescription = "",
                            modifier = Modifier.size(60.dp),
                            tint = MaterialTheme.colors.secondary.copy(alpha = 0.6f)
                        )
                    }
                }
                HorizontalPager(state = pagerState!!) { page ->
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ){
                        val item = pricingData[tabSelected - 1].items[page]
                        PricingItem(
                            viewModel,
                            pricingData, tabSelected,
                            item = item,
                            modifier = Modifier.padding(end = if (item.id.toInt() == pricingData[tabSelected - 1].items.size) 15.dp else 0.dp),
                        ) {
                            /*var introTier = (globalInstance.accountData.currentPlan == pricingData[0].items[0].userPlan || globalInstance.accountData.currentPlan == pricingData[0].items[1].userPlan)
                            var advancedTier = (globalInstance.accountData.currentPlan == pricingData[1].items[0].userPlan || globalInstance.accountData.currentPlan == pricingData[1].items[1].userPlan)
                            var selectedAdvancedTier = (viewModel.selectedPlan == pricingData[1].items[0].code || viewModel.selectedPlan == pricingData[1].items[1].code)
                            var selectedIntroTier = (viewModel.selectedPlan == pricingData[0].items[0].code || viewModel.selectedPlan == pricingData[0].items[1].code)*/
                            introUltimate = globalInstance.accountData.currentPlan == pricingData[0].items[1].userPlan
                            advancedUltimate = globalInstance.accountData.currentPlan == pricingData[1].items[1].userPlan

                            if(globalInstance.accountData.location.State != "" && globalInstance.accountData.location.State != null){
                                //allowing users with vmId to purchase
                                if (viewModel.paymentsAllowed || (globalInstance.accountData.vmId != "" && globalInstance.accountData.vmId != null)) {
                                    viewModel.selectedPlan = item.code
                                    if (item.code != "TopUp") {
                                        viewModel.selectedQuantity = 1
                                    } else {
                                        if (viewModel.selectedQuantity <= 0) {
                                            viewModel.selectedQuantity = 1
                                        }
                                    }
                                    if (viewModel.selectedPlan == "TopUp" && globalInstance.accountData.currentPlan == "Basic") {
                                        activity.makeToast("You're only allowed to purchase Monthly plans!")
                                        delayClose(viewModel)
                                    } else if (((globalInstance.accountData.currentPlan == pricingData[0].items[0].userPlan || globalInstance.accountData.currentPlan == pricingData[0].items[1].userPlan)
                                                && (viewModel.selectedPlan == pricingData[1].items[0].code || viewModel.selectedPlan == pricingData[1].items[1].code))
                                        || ((globalInstance.accountData.currentPlan == pricingData[1].items[0].userPlan || globalInstance.accountData.currentPlan == pricingData[1].items[1].userPlan)
                                                && (viewModel.selectedPlan == pricingData[0].items[0].code || viewModel.selectedPlan == pricingData[0].items[1].code))
                                        || ((globalInstance.accountData.currentPlan == "Basic" && globalInstance.accountData.subscriptionStatus == "expired")
                                                && ((globalInstance.accountData.expiredPlan == pricingData[0].items[0].userPlan && (viewModel.selectedPlan == pricingData[1].items[0].code || viewModel.selectedPlan == pricingData[1].items[1].code))
                                                || (globalInstance.accountData.expiredPlan == pricingData[0].items[1].userPlan && (viewModel.selectedPlan == pricingData[1].items[0].code || viewModel.selectedPlan == pricingData[1].items[1].code))
                                                || (globalInstance.accountData.expiredPlan == pricingData[1].items[0].userPlan && (viewModel.selectedPlan == pricingData[0].items[0].code || viewModel.selectedPlan == pricingData[0].items[1].code))
                                                || (globalInstance.accountData.expiredPlan == pricingData[1].items[1].userPlan && (viewModel.selectedPlan == pricingData[0].items[0].code || viewModel.selectedPlan == pricingData[0].items[1].code))
                                                || (globalInstance.accountData.expiredPlan == "" || globalInstance.accountData.expiredPlan == null)
                                                ))
                                    ) {
                                        viewModel.subOpenLoadingDialogState?.invoke(false)
                                        showRenewWarning.value = false
                                        showPlanWarning.value = true
                                    } else if (viewModel.selectedPlan == globalInstance.accountData.currentPlan
                                        || ((introUltimate && (viewModel.selectedPlan == pricingData[0].items[0].code || viewModel.selectedPlan == pricingData[0].items[1].code))
                                            || (advancedUltimate && (viewModel.selectedPlan == pricingData[1].items[0].code || viewModel.selectedPlan == pricingData[1].items[1].code))
                                           )
                                        ) {
                                        viewModel.subOpenLoadingDialogState?.invoke(false)
                                        showRenewWarning.value = true
                                        showPlanWarning.value = true
                                    } else {
                                        globalInstance.paymentPlan = viewModel.selectedPlan
                                        if((viewModel.selectedPlan == pricingData[0].items[0].code || viewModel.selectedPlan == pricingData[0].items[1].code)) {
                                            if(viewModel.selectedPlan == pricingData[0].items[0].code) {
                                                globalInstance.paymentPrice = pricingData[0].items[0].price.toDouble()
                                            } else if(viewModel.selectedPlan == pricingData[0].items[1].code) {
                                                globalInstance.paymentPrice = pricingData[0].items[1].price.toDouble()
                                            }
                                        } else if((viewModel.selectedPlan == pricingData[1].items[0].code || viewModel.selectedPlan == pricingData[1].items[1].code)) {
                                            if(viewModel.selectedPlan == pricingData[1].items[0].code) {
                                                globalInstance.paymentPrice = pricingData[1].items[0].price.toDouble()
                                            } else if(viewModel.selectedPlan == pricingData[1].items[1].code) {
                                                globalInstance.paymentPrice = pricingData[1].items[1].price.toDouble()
                                            }
                                        } else if(viewModel.selectedPlan == pricingData[0].items[2].code) {//TopUp plan
                                            globalInstance.paymentPrice = (pricingData[0].items[2].price * viewModel.selectedQuantity).toDouble()
                                        }
                                        onPurcahsePlanClick(viewModel , activity)

                                    }
                                } else {
                                    //currentActivity.makeToast("Sorry! We're not accepting any new members. Please follow our social media handles for any updates.")
                                    showWaitList.value = true
                                }
                            } else {
                                viewModel.subOpenLoadingDialogState?.invoke(false)
                                showUpdateLocationInformation.value = true
                            }
                        }
                        Text(
                            text = stringResource(id = R.string.coupon_text),
                            modifier = Modifier
                                .wrapContentSize()
                                .align(Alignment.CenterHorizontally)
                                .padding(20.dp)
                                .clickable {
                                    showCoupon.value = true
                                },
                            style = mainTitle.copy(fontSize = 16.sp),
                            color = MaterialTheme.colors.primary
                        )
                        val notes = pricingData[0].notes
                        if (notes != null) {
                            NoteText(notes, landscape)
                        }
                    }
                }
            }
//            PricingIndicator(
//                count = pricingData[tabSelected - 1].items.size,
//                list = list,
//                modifier = Modifier.weight(1f)
//            )
        }
    }
}



fun onPurcahsePlanClick(viewModel: PricingViewModel, activity: NavActivity) {
//    val intent = Intent(activity, WebViewActivity::class.java)
//    intent.putExtra("url", "https://antcloud.co/mobilePricing?planName=${viewModel.selectedPlan}&quantity=${viewModel.selectedQuantity}&couponCode=${viewModel.appliedCoupon}&idToken=${globalInstance.accountData.token}")
//    intent.putExtra("page", "pricing")
//    activity.startActivity(intent)
//    viewModel.updateCouponState("")
//    delayClose(viewModel)
}


@Composable
fun PricingItem(viewModel: PricingViewModel ,pricingData: List<PricingGroups>, tabSelected: Int, item: PricingCard, modifier: Modifier, onButtonClick: (() -> Unit)) {
    Column(
        modifier = modifier
            .padding(
                start = (LocalConfiguration.current.screenWidthDp * 3 / 26).dp,
                end = (LocalConfiguration.current.screenWidthDp * 3 / 26).dp
            )
            .width((LocalConfiguration.current.screenWidthDp / 1.3).dp)
            .defaultMinSize(
                minHeight = (LocalConfiguration.current.screenHeightDp / 1.48).dp,
//                minWidth = (LocalConfiguration.current.screenWidthDp / 1.4).dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = item.name,
                fontSize = 24.sp,
                color = MaterialTheme.colors.secondary,
                letterSpacing = 2.sp,
                fontFamily = novaSquare,
                fontWeight = FontWeight.ExtraBold
            )

            Row {
                Text(
                    text = "₹ ${item.price}",
                    fontSize = 20.sp,
                    color = MaterialTheme.colors.secondary,
                    fontWeight = FontWeight.Bold,
                    fontFamily = Play
                )
                Text(
                    text = if(item.code != "TopUp") " / month" else "",
                    fontSize = 16.sp,
                    color = MaterialTheme.colors.secondary.copy(alpha = .6f),
                    fontFamily = Play
                )
            }
        }

        val displayMetrics: DisplayMetrics = currentActivity.resources.displayMetrics
        val screenWidth = (displayMetrics.widthPixels / displayMetrics.density).toInt()

        val image: @Composable (Modifier) -> Unit = { modifier: Modifier ->
            Image(
                painter = painterResource(
                    id = PricingImages.valueOf(item.img_id).id
                ),
                modifier = modifier,
                contentDescription = null,
                alignment = Alignment.Center
            )
        }

        val itemCard: @Composable () -> Unit = {
            PricingItemCard(viewModel, pricingData, tabSelected,
                item = item, onButtonClick = onButtonClick, modifier = Modifier
            )
        }

        if(screenWidth < 600){
            val mod = Modifier
                .size((LocalConfiguration.current.screenWidthDp / 1.8).dp)
            image(mod)
            itemCard()
        } else {
            Row {
                val mod = Modifier
                    .weight(0.5f)
                    .size((LocalConfiguration.current.screenWidthDp / 2.5).dp)
                image(mod)
                Spacer(modifier = Modifier.weight(0.05f))
                Box(modifier = Modifier
                    .weight(0.3f)
                    .padding(start = 5.dp)
                    .align(Alignment.CenterVertically)
                ) {
                    itemCard()
                }
                Spacer(modifier = Modifier.weight(0.15f))
            }
        }
    }
}

@Composable
fun PricingItemCard(
    viewModel: PricingViewModel,
    pricingData: List<PricingGroups>,
    tabSelected: Int,
    item: PricingCard,
    onButtonClick: () -> Unit,
    modifier: Modifier = Modifier,
    landscape: Boolean = false
) {
    val hoursSelected = remember {
        mutableStateOf(viewModel.selectedQuantity.toString())
    }
    var subUpdateHours:((String) -> Unit) = {
        hoursSelected.value = it
    }
    Column(
        verticalArrangement = Arrangement.SpaceEvenly,
        modifier = modifier
            .fillMaxWidth()
            .border(
                1.dp,
                MaterialTheme.colors.secondary.copy(alpha = .5f),
                RoundedCornerShape(10.dp)
            ),
    ) {
        val features = item.features
        repeat(features.size) {
            if(landscape) {
                FeatureText(text = features[it].feature, true)
            } else {
                FeatureText(text = features[it].feature, false)
            }
        }

        if(item.code == "TopUp") {
            Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                IconButton(
                    modifier = Modifier.align(Alignment.CenterVertically),
                    onClick = {
                        if(viewModel.selectedQuantity > 1) {
                            viewModel.selectedQuantity--
                            subUpdateHours(viewModel.selectedQuantity.toString())
                        }
                    }) {
                    Icon(
                        painterResource(id = R.drawable.minus),
                        contentDescription = "",
                        tint = MaterialTheme.colors.secondary,
                        modifier = Modifier.align(Alignment.CenterVertically))
                }

                Text(modifier = Modifier.align(Alignment.CenterVertically),
                    color = MaterialTheme.colors.secondary,
                    text = hoursSelected.value/*when {
                        viewModel.selectedQuantity != 0 -> viewModel.selectedQuantity.toString()
                        else -> "1"
                    }*/,
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp)

                IconButton(
                    modifier = Modifier.align(Alignment.CenterVertically),
                    onClick = {
                        /*if (viewModel.selectedQuantity == 0) {
                            viewModel.selectedQuantity++
                        }*/
                        if(viewModel.selectedQuantity < 700) {
                            viewModel.selectedQuantity++
                            subUpdateHours(viewModel.selectedQuantity.toString())
                        }
                    }) {
                    Icon(
                        painterResource(id = R.drawable.plus),
                        contentDescription = "",
                        tint = MaterialTheme.colors.secondary,
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .wrapContentSize())
                }
            }

            Text(modifier = Modifier.align(Alignment.CenterHorizontally),
                color = MaterialTheme.colors.secondary,
                text = stringResource(id = R.string.topup_text),
                textAlign = TextAlign.Center,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.size(10.dp))
        }

        Button(
            onClick = {
                if(viewModel.paymentsAllowed || (globalInstance.accountData.vmId != "" && globalInstance.accountData.vmId != null)) {
                    viewModel.subOpenLoadingDialogState?.invoke(true)



                    if (pricingData[0].id == tabSelected.toString()) {
//                        AnalyticsManager.pricingPlanButton(item.code, viewModel.selectedQuantity.toString())
                    } else {
//                        AnalyticsManager.pricingPlanButton(
//                            item.code,
//                            viewModel.selectedQuantity.toString()
//                        )
                    }
                }

                /*selectedPlan = item.code
                selectedQuantity = item.quantity
                showPG.value = true*/
                onButtonClick()
                coroutineScope.launch {
                    delay(2000)
                    subUpdateHours(viewModel.selectedQuantity.toString())
                }
            },
            //enabled = item.name != globalInstance.accountData.currentPlan,
            colors = ButtonDefaults.outlinedButtonColors(
                backgroundColor = if(!viewModel.paymentsAllowed)
                    light_grey
                else {
                     MaterialTheme.colors.primary
                },
                contentColor = MaterialTheme.colors.secondary,
                disabledContentColor = if(viewModel.paymentsAllowed) MaterialTheme.colors.primary else MaterialTheme.colors.error
            ),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 8.dp)
        ) {
            val currentPlan = globalInstance.accountData.currentPlan
            Text(
                text = if (item.code.lowercase() == currentPlan.lowercase()) "Renew Plan" else "Get Started",
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
fun FeatureText(text: String, landscape: Boolean = false) {
    val textArray: List<String> = text.split(":")
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(
                vertical = if (landscape) 10.dp else 4.dp,
                horizontal = if (landscape) 15.dp else 10.dp
            )
            .fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.Outlined.CheckCircle,
            contentDescription = "",
            tint = MaterialTheme.colors.secondary,
            modifier = Modifier
                .size(if (landscape) 40.dp else 32.dp)
                .padding(end = 14.dp)
        )
        Text(
            modifier = Modifier.weight(.44f),
            text = textArray[0],
            color = MaterialTheme.colors.secondary,
            fontSize = if(landscape) 20.sp else 16.sp,
            fontWeight = FontWeight.Light,
            textAlign = TextAlign.Start)
        Text(
            modifier = Modifier.weight(.02f),
            text = ":",
            color = MaterialTheme.colors.secondary,
            fontSize = if(landscape) 20.sp else 16.sp,
            fontWeight = FontWeight.Light,
            textAlign = TextAlign.Start)
        Text(
            modifier = Modifier.weight(.54f),
            text = textArray[1],
            color = MaterialTheme.colors.secondary,
            fontSize = if(landscape) 20.sp else 16.sp,
            fontWeight = if (text == "Non - Refundable") FontWeight.SemiBold else FontWeight.Light,
            textAlign = TextAlign.Center)
    }
}

@Composable
fun NoteText(notes: List<Notes>, landscape: Boolean = false) {
    Column(modifier = Modifier.padding(
        vertical = if (landscape) 15.dp else 10.dp,
        horizontal = if (landscape) 15.dp else 10.dp
    )){
        repeat(notes.size){
            Text(
                text = notes[it].note,
                color = MaterialTheme.colors.secondary,
                fontSize = if (landscape) 16.sp else 12.sp,
                modifier = Modifier.padding(bottom = 4.dp),
                fontWeight = FontWeight.Light
            )
        }
    }
}

@Composable
fun NavButton(viewModel: PricingViewModel, text: String, id: Long?, tabSelected: Int) {
    OutlinedButton(
        colors = ButtonDefaults.outlinedButtonColors(if (id?.toInt() == tabSelected) MaterialTheme.colors.primary else Color.Transparent),
        border = BorderStroke(
            1.dp,
            if (id?.toInt() != tabSelected) MaterialTheme.colors.secondary else MaterialTheme.colors.primary
        ),
        shape = RoundedCornerShape(5.dp),
        onClick = {
            if(text == "Intro" && !GlobalData.getInstance().remoteShowIntroPlans && (globalInstance.accountData.currentPlan == "Basic" && (globalInstance.accountData.subscriptionStatus == "" || globalInstance.accountData.subscriptionStatus == null))) {
                viewModel.updateShowIntroWarning(true)
            } else {
                viewModel.updateTabState(id)
                viewModel.updatePageState(0)

            }
        },
        contentPadding = PaddingValues(horizontal = 25.dp, vertical = 10.dp)
    ) {
        Text(
            text = text,
            color = if (id?.toInt() == tabSelected) Color.White else MaterialTheme.colors.secondary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Light,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.button,
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .padding(0.dp)
        )
    }
}

@Composable
fun InputCoupon(
    initialValue: String, isError: Boolean, onValChange: ((value: String) -> Unit)
) {
    val keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
    TextTheme {
        OutlinedTextField(
            value = initialValue,
            singleLine = true,
            onValueChange = { onValChange(it) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.CardGiftcard, contentDescription = ""
                )
            },
            isError = isError || (initialValue.isNotEmpty() && (initialValue.length < 3)),
            placeholder = { Text("Enter Coupon Code") },
            modifier = Modifier
                .fillMaxWidth(.9f)
                .wrapContentHeight()
                .padding(bottom = 0.dp, top = 0.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(MaterialTheme.colors.secondary),
            keyboardOptions = keyboardOptions
        )
    }
}
fun delayClose(viewModel: PricingViewModel) {
    coroutineScope.launch {
        delay(2000)
        viewModel.subOpenLoadingDialogState?.invoke(false)
        viewModel.appliedCoupon = ""
        viewModel.selectedQuantity = 1
        viewModel.selectedPlan = ""
    }


}

fun createPricingOrderApi(viewModel: PricingViewModel) {
    val dataModel = PricingReq(
        viewModel.selectedPlan,
        viewModel.selectedQuantity,
        viewModel.appliedCoupon,
        "Mobile")
    viewModel.getCreatePricingOrderData("JWT ${GlobalData.getInstance().accountData.token}", dataModel)
}
    fun verifyCouponCodeApi(viewModel: PricingViewModel) {
        viewModel.getVerifyCouponCodeData(
            "JWT ${GlobalData.getInstance().accountData.token}",
            viewModel.couponState.uppercase()
        )
    }

fun updateLocationApi(viewModel: PricingViewModel) {
    val location = Location()
    location.State = viewModel.stateLocation
    location.Pincode = viewModel.pinCodeState
    val updateLocationReq = UpdateLocationReq(location)
    viewModel.getUpdateLocationData(
        "JWT " +
                GlobalData.getInstance().accountData.token,
        GlobalData.getInstance().accountData.id,
        updateLocationReq
    )
}

fun addToWaitListApi(viewModel: PricingViewModel) {
    val dataModel =
        ForgotPasswordReq(GlobalData.getInstance().accountData.email.trim())
    viewModel.getAddToWishListData(
        "JWT ${GlobalData.getInstance().accountData.token}",
        dataModel
    )
}







