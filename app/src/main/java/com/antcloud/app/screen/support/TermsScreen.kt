package com.antcloud.app.screen.support

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.antcloud.app.activity.NavActivity
import com.antcloud.app.common.DrawerScreens
import com.antcloud.app.common.GlobalData
import com.antcloud.app.data.TermsResp
import androidx.compose.foundation.lazy.items
import com.antcloud.app.common.AnalyticsManager

fun termsNav(navGraph: NavGraphBuilder, activity: NavActivity, updateToolbar: ((String) -> Unit)) {
    return navGraph.composable(DrawerScreens.Terms.route) {
        updateToolbar("Terms & Conditions")
        AnalyticsManager.tcNavButton()
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TermsScreen(terms = GlobalData.getInstance().androidData.terms)
        }
    }
}

@Composable
fun TermsScreen(terms: List<TermsResp>) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth().fillMaxHeight().background(MaterialTheme.colors.surface).padding(bottom = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(terms) {
            PrivacyHeading(text = it.title)
            PrivacyContent(text = it.term)
            if (it.subTitle != null) {
                for (i in 0 until it.subTitle!!.size) {
                    PrivacySubHeading(text = it.subTitle?.get(i)!!.title)
                    PrivacyContent(text = it.subTitle?.get(i)!!.term)
                }
            }
        }
    }
}
