package com.antcloud.app.screen.support


import android.content.pm.ActivityInfo
import android.util.DisplayMetrics
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

import com.antcloud.app.activity.NavActivity
import com.antcloud.app.common.AnalyticsManager
import com.antcloud.app.common.DrawerScreens
import com.antcloud.app.common.GlobalData
import com.antcloud.app.components.Loading
import com.antcloud.app.components.VerticalGrid
import com.antcloud.app.data.FAQCard
import com.antcloud.app.theme.mainTitle
import com.antcloud.app.viewmodel.FAQViewModel


fun FAQNav(navGraph: NavGraphBuilder, activity: NavActivity,
           updateToolbar: ((String) -> Unit), navigate: ((String) -> Unit)) {
    return navGraph.composable(DrawerScreens.FAQs.route) {
        val viewModel: FAQViewModel = hiltViewModel()
        AnalyticsManager.faqNavButton()
        LaunchedEffect(key1 = Unit) {
        }
        updateToolbar("FAQs")
        Column {
            FAQScreen(navigate , viewModel = viewModel, activity)
        }
    }
}

@Composable
fun FAQScreen(
    navigate: ((String) -> Unit) , viewModel: FAQViewModel, activity: NavActivity
) {
    var data = GlobalData.getInstance().androidData.faqs
    data =  GlobalData.getInstance().remoteDataFaq
    viewModel.initializeCardsList(data)
    val cards = viewModel.cardsList
    if (cards == null) {
        Loading()
    } else {
        CardsScreen(navigate, viewModel = viewModel, activity = activity)
    }
}

@Composable
fun CardsScreen(
    navigate: ((String) -> Unit), viewModel: FAQViewModel, activity: NavActivity
) {
    val cards = viewModel.cardsList!!
    var expanded by remember {
        mutableStateOf(0)
    }

    viewModel.subExpandedCardsState = {
        expanded = it
    }

    val headingElement: @Composable () -> Unit = {
        Text(
            text = "Frequently Asked Questions ",
            color = MaterialTheme.colors.secondary,
            style = mainTitle.copy(fontSize = 20.sp,
                textAlign = TextAlign.Start,
                fontWeight = FontWeight.Light),
            modifier = Modifier.padding(start = 10.dp, top = 15.dp, bottom = 12.dp))
    }

    val displayMetrics: DisplayMetrics = activity.resources.displayMetrics
    val screenWidth = (displayMetrics.widthPixels / displayMetrics.density).toInt()

    if(screenWidth < 600){
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        LazyColumn(
            modifier = Modifier
//            .fillMaxWidth(.99f)
                .background(MaterialTheme.colors.background),
//        horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                headingElement()
            }
            items(cards, FAQCard::id) {
                ExpandableCard(
                    card = it,
                    onCardArrowClick = {

                        if (it.id == viewModel.expandedCardState) {
                            viewModel.updateCardsState(0)

                        }
                        else
                        viewModel.updateCardsState(it.id)
                                       },
                    expanded = (expanded == it.id),
                    landscape = false
                )
            }

        }
    }
    else {
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        Column(Modifier.verticalScroll(state = rememberScrollState()).background(MaterialTheme.colors.background)) {
            headingElement()
            VerticalGrid(modifier = Modifier.padding(end = 15.dp), columns = 2) {
                cards.forEach {
                    ExpandableCard(card = it,
                        onCardArrowClick = {
                            if (it.id == viewModel.expandedCardState) {
                                viewModel.updateCardsState(0)

                            }
                            else
                                viewModel.updateCardsState(it.id)
                        },
                        expanded = (expanded == it.id),
                        landscape = true)
                }
            }
        }
    }
}
