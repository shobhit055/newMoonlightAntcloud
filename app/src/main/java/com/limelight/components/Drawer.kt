package com.limelight.components


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.perf.FirebasePerformance

import com.limelight.common.AppUtils.Companion.clearCheck
import com.limelight.common.AppUtils.Companion.gradientColors
import com.limelight.common.AppUtils.Companion.navigateSplashActivity
import com.limelight.common.GlobalData
import com.limelight.data.User
import com.limelight.theme.BlueGradient
import com.limelight.theme.PinkGradient
import com.limelight.theme.subtitle
import com.limelight.viewmodel.UserViewModel
import com.limelight.R
import com.limelight.activity.NavActivity
import com.limelight.common.AnalyticsManager.Companion.removeAnalyticsUserId
import com.limelight.common.DrawerScreens
import kotlin.math.absoluteValue

val globalInstance  =  GlobalData.getInstance()
@Composable
fun Drawer(
    modifier: Modifier = Modifier,
    current: String?,
    activity: NavActivity,
    screens: List<DrawerScreens>,
    onDestinationClicked: (route: String) -> Unit) {
    val viewModel : UserViewModel =  hiltViewModel()

    val userData = remember {
        mutableStateOf(viewModel.userData)
    }
    Column(modifier.fillMaxWidth()
        .fillMaxHeight()
        .padding(top = 0.dp).background(Black),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Column(
            modifier = Modifier.weight(.8f).padding(top = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)){
                Image(
                    modifier = Modifier.size(60.dp).align(Alignment.Center),
                    painter = painterResource(id = R.drawable.ant_cloud_white_icon),
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds)

                if (GlobalData.getInstance().accountData.id != "") {
                    Spacer(modifier = Modifier.size(10.dp))
                    Column(modifier = Modifier.align(Alignment.CenterEnd),
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(onClick = {
                            signOut(activity,viewModel)
                        }){
                            Icon(imageVector = Icons.Outlined.Logout,
                                contentDescription = "",
                                tint = MaterialTheme.colors.primary,
                                modifier = Modifier.size(40.dp))
                        }
                        Text(text = "Sign Out",
                            style = MaterialTheme.typography.caption,
                            color = MaterialTheme.colors.primary)
                    }
                }
            }

          Column (Modifier.verticalScroll(state = rememberScrollState())){
              screens.forEachIndexed { _, screen ->
                  //  if (!(screen.route == DrawerScreens.Terms.route || screen.route == DrawerScreens.Policy.route || screen.route == DrawerScreens.Onboarding.route)) {
                  val gradientBrush = Brush.linearGradient(colors = gradientColors)
                  Row(modifier = Modifier
                      .fillMaxWidth()
                      .align(Alignment.CenterHorizontally)
                      .clip(RoundedCornerShape(5.dp))
                      .background(Color.Transparent)
                      .clickable {
                          onDestinationClicked(screen.route)
                      }
                      .padding(vertical = 15.dp, horizontal = 5.dp)) {
                      var myStyle: TextStyle? = null
                      myStyle = if (current == screen.route) {
                          TextStyle(
                              brush = gradientBrush,
                              fontSize = 22.sp,
                              fontWeight = FontWeight.Bold)
                      }
                      else
                          subtitle.copy(fontSize = 22.sp)
                      Text(style = myStyle,
                          modifier = Modifier.fillMaxWidth(),
                          text = screen.title,
                          textAlign = TextAlign.Center)
                  }
              }
          }

        }

//        Column(
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.Bottom,
//            modifier = Modifier
//                .weight(.2f)
//                .wrapContentWidth()
//                .padding(16.dp)){
//            Text(
//                text = "Time Remaining",
//                style = TextStyle(
//                    fontSize = 18.sp, fontWeight = FontWeight.Bold,
//                    color = White, textAlign = TextAlign.Center
//                ), modifier = Modifier.padding(bottom = 16.dp))
//            val gradientColors = listOf(White, PinkGradient, BlueGradient)
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(45.dp)
//                    .padding(bottom = 16.dp)
//                    .background(
//                        brush = Brush.horizontalGradient(colors = gradientColors),
//                        shape = RoundedCornerShape(16.dp)
//                    ),
//                verticalAlignment = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.Center) {
//                Box(
//                    modifier = Modifier
//                        .weight(.5f)
//                        .fillMaxHeight()
//                        .padding(top = 0.dp)
//                        .background(White, shape = RoundedCornerShape(16.dp))
//                        .align(Alignment.CenterVertically)
//                ) {
//                    Text(
//                        modifier = Modifier.align(Alignment.Center),
//                        text = userData.value.currentPlan,
//                        style = TextStyle(
//                            fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Black,
//                            textAlign = TextAlign.Center
//                        )
//                    )
//                }
//                val timeLeft : Int
//                var timeLeftString  = ""
//                if(userData.value.currentPlan!="Basic") {
//                    timeLeft = if(userData.value.totalTimeMonth != 0) userData.value.totalTimeMonth - userData.value.timeUsedMonth else 0
//                    timeLeftString = "${timeLeft/60} hrs. ${timeLeft.absoluteValue%60} mins."
//                }
//                else{
//                    timeLeftString = "0 mins"
//                }
//
//                Text(
//                    modifier = Modifier
//                        .weight(.5f)
//                        .padding()
//                        .wrapContentHeight(),
//
//                    text = timeLeftString,
//                    style = TextStyle(
//                        fontSize = 12.sp, fontWeight = FontWeight.Medium, color = White,
//                        textAlign = TextAlign.Center
//                    )
//                )
//            }
//        }
    }
}

fun signOut(activity: NavActivity, viewModel: UserViewModel) {
    globalInstance.logoutUserApi = true
    globalInstance.traceLogoutUserApi = FirebasePerformance.getInstance().newTrace("logout_user_api")
    globalInstance.traceLogoutUserApi.start()

    removeAnalyticsUserId()
    viewModel.getLogoutData("JWT ${GlobalData.getInstance().accountData.refreshToken}")
    val logoutState = viewModel.logoutState.value
    when(logoutState.success){
        1 -> activity.makeToast("Logout Successful")
        0 -> activity.makeToast(logoutState.error)
    }

    GlobalData.getInstance().accountData = User()
    clearCheck(activity)
    navigateSplashActivity(activity)
}
