package com.limelight.screen.support


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

import com.limelight.activity.NavActivity
import com.limelight.common.DrawerScreens
import com.limelight.common.GlobalData
import com.limelight.components.Loading
import com.limelight.components.VerticalGrid
import com.limelight.data.FAQCard
import com.limelight.theme.mainTitle
import com.limelight.viewmodel.FAQViewModel


fun FAQNav(navGraph: NavGraphBuilder, activity: NavActivity,
           updateToolbar: ((String) -> Unit), navigate: ((String) -> Unit)) {
    return navGraph.composable(DrawerScreens.FAQs.route) {
        val viewModel: FAQViewModel = hiltViewModel()
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
    navigate: ((String) -> Unit) , viewModel: FAQViewModel, activity: NavActivity) {
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
    navigate: ((String) -> Unit), viewModel: FAQViewModel, activity: NavActivity) {
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
    } else {
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

//@Composable
//fun ExpandableCard(faq:String ,card: FAQCard, onCardArrowClick: () -> Unit, expanded: Boolean, landscape: Boolean = false) {
//    val cardExpandedBackgroundColor = MaterialTheme.colors.primary
//    val cardCollapsedBackgroundColor = MaterialTheme.colors.surface
//    val transition = updateTransition(targetState = !expanded, label = "")
//
//    val cardBgColor by transition.animateColor({
//        tween(durationMillis = EXPAND_ANIMATION_DURATION)
//    }, label = "") {
//        if (!it) /*cardExpandedBackgroundColor*/cardCollapsedBackgroundColor else cardCollapsedBackgroundColor
//    }
//
//    val cardElevation by transition.animateDp({
//        tween(durationMillis = EXPAND_ANIMATION_DURATION)
//    }, label = "") {
//        if (it) 18.dp else 12.dp
//    }
//
//    val cardRoundedCorners by transition.animateDp({
//        tween(
//            durationMillis = EXPAND_ANIMATION_DURATION, easing = FastOutSlowInEasing)
//    }, label = "") {
//        if (it) 4.dp else 8.dp
//    }
//
//    val arrowRotationDegree by transition.animateFloat({
//        tween(durationMillis = EXPAND_ANIMATION_DURATION)
//    }, label = "") {
//        if (!it) 0f else 180f
//    }
//
//    val contentColour by transition.animateColor({
//        tween(durationMillis = 400)
//    }, label = "") {
//        if (!it) MaterialTheme.colors.secondary else MaterialTheme.colors.primary
//    }
//
//    Card(
//        backgroundColor = cardBgColor, contentColor = contentColour, elevation = cardElevation,
//        shape = RoundedCornerShape(cardRoundedCorners),
//        modifier = Modifier.fillMaxWidth().wrapContentHeight()
//            .padding(start = 15.dp, end = 15.dp, bottom = 7.dp,
//                top = if (landscape) 0.dp else {
//                    if (card.id == 1) 4.dp else 0.dp
//                })) {
//        Column(
//            modifier = Modifier.fillMaxWidth(.9f)
//                .clickable(onClick = onCardArrowClick),
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.Center) {
//            Row(
//                horizontalArrangement = Arrangement.SpaceEvenly,
//                modifier = Modifier.fillMaxWidth(),
//                verticalAlignment = Alignment.CenterVertically) {
//                CardArrow(
//                    degrees = arrowRotationDegree, onClick = onCardArrowClick)
//                CardTitle(title = card.title)
//            }
//            ExpandableContent(
//                visible = expanded, initialVisibility = expanded, content = card.content)
//        }
//    }
//}

//@Composable
//fun CardTitle(title: String) {
//    Text(
//        text = title,
//        style = heading.copy(fontSize = 16.sp, fontWeight = FontWeight.Light, textAlign = TextAlign.Start),
//        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
//        color = MaterialTheme.colors.primary)
//}
//
//@Composable
//fun CardArrow(
//    degrees: Float, onClick: () -> Unit
//) {
//    IconButton(onClick = onClick, content = {
//        Icon(
//            imageVector = Icons.Filled.KeyboardArrowUp,
//            contentDescription = "",
//            modifier = Modifier
//                .size(20.dp)
//                .rotate(degrees))
//    })
//}
//
//@Composable
//fun ExpandableContent(visible: Boolean = true, initialVisibility: Boolean = false, content: String) {
//    val enterTransition = remember {
//        expandVertically(
//            expandFrom = Alignment.Top, animationSpec = tween(EXPANSION_TRANSITION_DURATION)
//        ) + fadeIn(
//            initialAlpha = 0.3f, animationSpec = tween(EXPANSION_TRANSITION_DURATION))
//    }
//    val exitTransition = remember {
//        shrinkVertically(
//            shrinkTowards = Alignment.Top, animationSpec = tween(EXPANSION_TRANSITION_DURATION)
//        ) + fadeOut(
//            animationSpec = tween(EXPANSION_TRANSITION_DURATION))
//    }
//
//    AnimatedVisibility(visibleState = remember {
//        MutableTransitionState(initialState = initialVisibility)
//    }.apply { targetState = visible },
//        modifier = Modifier,
//        enter = enterTransition,
//        exit = exitTransition) {
//        Column(modifier = Modifier.padding(bottom = 16.dp, start = 16.dp, end = 16.dp)) {
//            Text(
//                text = content,
//                style = MaterialTheme.typography.body1,
//                fontWeight = FontWeight.Light,
//                color = MaterialTheme.colors.primary)
//        }
//    }
//}