package com.limelight.screen.support



import android.util.DisplayMetrics
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.google.firebase.perf.FirebasePerformance
import com.limelight.activity.NavActivity
import com.limelight.common.AnalyticsManager
import com.limelight.common.AppUtils.Companion.saveRefreshTokenData
import com.limelight.common.DrawerScreens
import com.limelight.common.GlobalData
import com.limelight.components.makeToast
import com.limelight.components.signOut
import com.limelight.data.PostSupportReq
import com.limelight.screen.auth.TextTheme
import com.limelight.theme.light_grey
import com.limelight.theme.mainTitle
import com.limelight.theme.subtitle

import com.limelight.viewmodel.SupportViewModel
import com.limelight.viewmodel.UserViewModel


var allowSubmission = true
val globalInstance  =  GlobalData.getInstance()
enum class IssueCodes(val title: String, val code: String) {
    Latency("Latency", "latency"),
    aGameControls("Game Controls", "gameControls"),
    GameLibrary("Game Library", "gameLibrary")
}

enum class InternetCodes(val title: String, val code: String) {
    WiFi("WiFi", "wifi"),
    Data("Data Connection", "data")
}

fun reportNav(
    navGraph: NavGraphBuilder,
    activity: NavActivity,
    updateToolbar: ((String) -> Unit)) {
    return navGraph.composable(DrawerScreens.Report.route) {
        val viewModel: SupportViewModel = hiltViewModel()

        var submitReport by remember {
            mutableStateOf(0)
        }
        viewModel.subReportSubmit = {
            submitReport = it
        }

        LaunchedEffect(key1 = Unit) {
            viewModel.initializeState()
            allowSubmission = true
        }
        updateToolbar("Report")
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
        ) {
            ReportScreen(viewModel = viewModel,
                         activity = activity)
            when(submitReport) {
                1 -> {
                    activity.makeToast("Feedback submitted successfully")
                    viewModel.subReportSubmit?.invoke(0)
                    allowSubmission = false
                }
                0 -> {}

            }
        }
    }
}

@Composable
fun ReportScreen(viewModel: SupportViewModel, activity: NavActivity) {
    val userViewModel : UserViewModel = hiltViewModel()
    var calledRefresh = false
    val supportState = viewModel.supportState.value

    var issues by remember {
        mutableStateOf(viewModel.selectedIssueState)
    }

    viewModel.subSelectedIssueState = {
        issues = it
    }

    var feedback by remember {
        mutableStateOf("")
    }

    viewModel.subFeedbackState = {
        feedback = it
    }

    var submitError by remember {
        mutableStateOf(false)
    }

    viewModel.subReportError = {
        submitError = it
    }

    var internetError by remember {
        mutableStateOf(false)
    }

    viewModel.subInternetError = {
        internetError = it
    }

    val refreshTokenState = userViewModel.refreshTokenState.value
    when (refreshTokenState.success) {
        1 -> {
            LaunchedEffect(Unit) {
                calledRefresh = true
                if ((refreshTokenState.accessToken != "") && (refreshTokenState.refreshToken != "")) {
                  saveRefreshTokenData(activity,refreshTokenState.accessToken,refreshTokenState.refreshToken)
                    onReportSubmit(viewModel)
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
    when (supportState.success) {
        1 -> {
            LaunchedEffect(Unit) {
                viewModel.subReportSubmit?.invoke(1)
            }
        }
        0 -> {
            LaunchedEffect(Unit) {
                val msg = supportState.error
                if (!calledRefresh) {
                        userViewModel.getRefreshTokenData("JWT ${GlobalData.getInstance().accountData.refreshToken}")
                } else {
                    activity.makeToast(msg)
                    calledRefresh = false
                    signOut(activity, userViewModel)
                }
            }
        }
    }

    val displayMetrics: DisplayMetrics = activity.resources.displayMetrics
    val screenWidth = (displayMetrics.widthPixels / displayMetrics.density).toInt()
    val landscape = screenWidth >= 600

    Column(
        modifier = Modifier
            .fillMaxWidth(.96f)
            .padding(top = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "Please provide us with a detailed feedback to help us understand the problem better",
            style = mainTitle.copy(fontSize = 18.sp, textAlign = TextAlign.Start),
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colors.secondary)

        Spacer(modifier = Modifier.size(15.dp))

        Text(text = "Is your issue related to (optional): ",
            style = subtitle,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colors.secondary)

        CheckBoxGrid(issues = issues, isIssue = true, viewModel)

        Spacer(modifier = Modifier.size(15.dp))

        Text(text = "Type of Internet Connection (required, select one) : ",
            style = subtitle,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colors.secondary)

        CheckBoxGrid(issues = issues,
            isIssue = false,
            viewModel,
            checkError = internetError)

        Spacer(modifier = Modifier.size(15.dp))

        Text(
            text = "Write To Us",
            style = mainTitle.copy(fontSize = 18.sp, textAlign = TextAlign.Start),
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colors.secondary
        )
        Spacer(modifier = Modifier.size(10.dp))
        SimpleOutlinedTextFieldSample(viewModel ,submitError)
        TextButton(
            enabled = allowSubmission,
            onClick = {
                AnalyticsManager.submitReportButton()
                globalInstance.postSupportData = true
                globalInstance.tracePostSupportData=  FirebasePerformance.getInstance().newTrace("post_support_data")
                globalInstance.tracePostSupportData.start()
                    onReportSubmit(viewModel)

            },
            modifier = Modifier.fillMaxWidth(if(landscape) 0.4f else 1f),
            colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primary),
        ) {
            Text(
                modifier = Modifier.padding(vertical = 5.dp),
                text = "Submit Report ",
                style = MaterialTheme.typography.button.copy(fontSize = 16.sp),
                color = MaterialTheme.colors.secondary
            )
        }
    }
}

@Composable
fun SimpleOutlinedTextFieldSample(viewModel: SupportViewModel, errorCheck: Boolean) {
    var text by remember { mutableStateOf("") }
    TextTheme {
        OutlinedTextField(
            value = text,
            onValueChange = {
                text = it
                viewModel.updateFeedbackState(it)
//                onEventHandler(ReportEvent.OnFeedbackChanged(it))

            },
            label = { Text("Feedback/ Issue") },
            isError = if(errorCheck) text.isEmpty() else false,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(MaterialTheme.colors.secondary)
        )
    }
}

@Composable
fun CheckBoxGrid(
    issues: List<String>,
    isIssue: Boolean,
    viewModel: SupportViewModel,
    checkError: Boolean = false) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        verticalArrangement = Arrangement.spacedBy(5.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        contentPadding = PaddingValues(vertical = 15.dp),
        modifier = Modifier.heightIn(max = (LocalConfiguration.current.screenHeightDp * 2).dp)) {
        if (isIssue) {
            items(IssueCodes.values()) {
                CheckBox(
                    title = it.title,
                    selected = issues.contains(it.code),
                    onClick = {
                        viewModel.updateIssueState(it.code)
                        if((it.code == "wifi" || it.code == "data"))
                            viewModel.subInternetError?.invoke(false)
                    }
                )
            }
        } else {
            items(InternetCodes.values()) {
                CheckBox(
                    title = it.title,
                    selected = issues.contains(it.code),
                    onClick = {
                        viewModel.updateIssueState(it.code)
                        if((it.code == "wifi" || it.code == "data"))
                            viewModel.subInternetError?.invoke(false)
                    },
                    onError = checkError)
            }
        }
    }
}

@Composable
fun CheckBox(
    title: String,
    selected: Boolean,
    onClick: (() -> Unit),
    onError: Boolean
) {
    val transition = updateTransition(targetState = !selected, label = "")
    val checkboxSelectedColor = MaterialTheme.colors.primary
    val checkboxColor = light_grey

    val bgColor by transition.animateColor(
        { tween(durationMillis = 200) },
        label = "CheckboxBgColor"
    ) {
        if (!it) checkboxSelectedColor else checkboxColor
    }

    val cardElevation by transition.animateDp({
        tween(durationMillis = EXPAND_ANIMATION_DURATION)
    }, label = "") {
        if (it) 8.dp else 12.dp
    }

    Card(
        backgroundColor = if(!onError) bgColor else Color(0.3f,0f,0f,0.7f),
        elevation = cardElevation,
        shape = RoundedCornerShape(5.dp)) {
        Row(
            modifier = Modifier
//                .padding(3.dp)
                .clickable {
                    onClick()
                },
            verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = selected,
                onCheckedChange = {
                    onClick()
                },
                enabled = true,
                colors = CheckboxDefaults.colors(
                    light_grey,
                    checkmarkColor = MaterialTheme.colors.secondary
                )
            )
//            Spacer(modifier = Modifier.size(0.5.dp))
            Text(
                text = title,
                style = subtitle,
                textAlign = TextAlign.Start,
                fontWeight = FontWeight.Medium,
                color = if(!onError) MaterialTheme.colors.secondary else MaterialTheme.colors.error)
        }
    }
}

@Composable
fun CheckBox(
    title: String,
    selected: Boolean,
    enabled: Boolean = true,
    activitiy: NavActivity ?= null,
    onClick: (() -> Unit)
) {
    val transition = updateTransition(targetState = !selected, label = "")
    val checkboxSelectedColor = MaterialTheme.colors.primary
    val checkboxColor = light_grey

    val bgColor by transition.animateColor(
        { tween(durationMillis = 200) },
        label = "CheckboxBgColor"
    ) {
        if (!it) checkboxSelectedColor else checkboxColor
    }

    val cardElevation by transition.animateDp({
        tween(durationMillis = EXPAND_ANIMATION_DURATION)
    }, label = "") {
        if (it) 8.dp else 12.dp
    }


    Card(
        backgroundColor = bgColor,
        elevation = cardElevation,
        shape = RoundedCornerShape(5.dp),
    ) {
        Row(
            modifier = Modifier
//                .padding(3.dp)
                .clickable {
                    if(enabled) {
                        onClick()
                    } else {
                        activitiy?.makeToast("Please upgrade your plan to access this resolution")
                    }
                },
            verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = selected,
                onCheckedChange = {
                    onClick()
                },
                enabled = enabled,
                colors = CheckboxDefaults.colors(light_grey, checkmarkColor = MaterialTheme.colors.secondary))
//            Spacer(modifier = Modifier.size(0.5.dp))
            Text(
                text = title,
                style = subtitle,
                textAlign = TextAlign.Start,
                fontWeight = FontWeight.Medium,
                color = /*if(selected)*/ MaterialTheme.colors.secondary /*else MaterialTheme.colors.error*/
            )
        }
    }
}

fun onReportSubmit(viewModel: SupportViewModel) {
    if((viewModel.reportIssues.data || viewModel.reportIssues.wifi) && viewModel.reportIssues.feedback != "") {
        viewModel.subInternetError?.invoke(false)
        viewModel.subReportError?.invoke(false)

        val internetValue = if(viewModel.reportIssues.data) "Data" else "WiFi"

        val currentList = ArrayList(viewModel.selectedIssueState)
        if(viewModel.selectedIssueState.contains("wifi")) {
            currentList.remove("wifi")
        } else if(viewModel.selectedIssueState.contains("data")) {
            currentList.remove("data")
        }
        if(currentList.contains("")) {
            currentList.remove("")
        }
        val dataModel = PostSupportReq(internetValue, viewModel.reportIssues.feedback, currentList.toList() ,"Mobile")
        viewModel.getSupportData("JWT ${GlobalData.getInstance().accountData.token}", dataModel)
    } else {
        if(!viewModel.reportIssues.data && !viewModel.reportIssues.wifi)
            viewModel.subInternetError?.invoke(true)
        else
            viewModel.subInternetError?.invoke(false)
        viewModel.subReportError?.invoke(true)
    }
}

