package com.limelight.screen.support

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable

import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.limelight.activity.NavActivity
import com.limelight.common.DrawerScreens
import com.limelight.common.GlobalData
import com.limelight.data.PolicyListResp
import com.limelight.theme.mainTitle
import com.limelight.theme.subtitle


fun PrivacyNav(navGraph: NavGraphBuilder, activity: NavActivity, updateToolbar: ((String) -> Unit)) {
    return navGraph.composable(DrawerScreens.Policy.route) {
        GlobalData.getInstance().androidData.policy =  GlobalData.getInstance().remoteDataPolicy
        val policy: List<PolicyListResp> = GlobalData.getInstance().androidData.policy
        updateToolbar("Privacy Policy")
     /*   AnalyticsManager.privacyPolicyNavButton()*/
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PrivacyScreen(policy = policy)
        }
    }
}

@Composable
fun PrivacyScreen(policy: List<PolicyListResp>) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth().fillMaxHeight().background(MaterialTheme.colors.surface).padding(bottom = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(policy) {
            PrivacyHeading(text = it.title)
            PrivacyContent(text = it.policy)
            if (it.subTitle != null) {
                for (i in 0 until it.subTitle!!.size) {
                    PrivacySubHeading(text = it.subTitle?.get(i)!!.title)
                    PrivacyContent(text = it.subTitle?.get(i)!!.term)
                }
            }
        }
    }
}

@Composable
fun PrivacyHeading(text: String) {
    Text(
        text = text,
        style = mainTitle.copy(textAlign = TextAlign.Start, fontSize = 24.sp),
        color = MaterialTheme.colors.secondary,
        modifier = Modifier
            .padding(top = 10.dp)
            .fillMaxWidth(.94f)
    )
}

@Composable
fun PrivacySubHeading(text: String) {
    Text(
        text = text,
        style = mainTitle.copy(textAlign = TextAlign.Start, fontSize = 20.sp),
        color = MaterialTheme.colors.secondary,
        modifier = Modifier
            .padding(top = 10.dp)
            .fillMaxWidth(.94f)
    )
}

@Composable
fun PrivacyContent(text: String) {
    Text(
        text = text,
        style = subtitle.copy(textAlign = TextAlign.Justify),
        color = MaterialTheme.colors.secondary,
        modifier = Modifier
            .alpha(if (MaterialTheme.colors.isLight) .9f else .6f)
            .padding(top = 6.dp)
            .fillMaxWidth(.94f)
    )
}