package com.limelight.screen.support

import android.util.DisplayMetrics
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Report
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.limelight.viewmodel.SupportViewModel
import com.limelight.activity.NavActivity
import com.limelight.common.DrawerScreens

import com.limelight.common.GlobalData
import com.limelight.components.VerticalGrid
import com.limelight.data.FAQCard
import com.limelight.data.SupportCard
import com.limelight.theme.heading
import com.limelight.theme.mainTitle
import com.limelight.theme.subtitle
import androidx.compose.foundation.lazy.items
import com.limelight.common.AnalyticsManager


const val EXPAND_ANIMATION_DURATION = 400
const val EXPANSION_TRANSITION_DURATION = 400
fun supportNav(navGraph: NavGraphBuilder, activity: NavActivity, updateToolbar: ((String) -> Unit), navigate: ((String) -> Unit)) {
    return navGraph.composable(DrawerScreens.Support.route) {
        updateToolbar("Support")
        val viewModel : SupportViewModel = hiltViewModel()
        LaunchedEffect(key1 = Unit) {
            GlobalData.getInstance().androidData.support =  GlobalData.getInstance().remoteDataSupport
            viewModel.initializeCardsList(GlobalData.getInstance().androidData.support)

        }
        AnalyticsManager.supportNavButton()
        Column{
            SupportScreen(activity,navigate,  viewModel)
        }

    }
}

@Composable
fun SupportScreen(activity: NavActivity, navigate: (String) -> Unit, viewModel: SupportViewModel) {
    if(GlobalData.getInstance().openSupportScreen){
        GlobalData.getInstance().openSupportScreen =  false
    }
    val cardList = GlobalData.getInstance().androidData.support
    SupportScreenCards(navigate ,viewModel = viewModel, activity = activity)
}

@Composable
fun SupportScreenCards(
    navigate: (String) -> Unit,
    viewModel: SupportViewModel,
    activity: NavActivity) {
//    val cardList = viewModel.cardsList as List<SupportCard>
    val cardList = GlobalData.getInstance().androidData.support

    var selected by remember {
        mutableStateOf(0)
    }

    viewModel.subSelectedState = {
        selected = it.toInt()
    }

    val displayMetrics: DisplayMetrics = activity.resources.displayMetrics
    val screenWidth = (displayMetrics.widthPixels / displayMetrics.density).toInt()

    Column (Modifier.fillMaxWidth()
        .fillMaxHeight()
        .background(Color.Black)) {
        val headingElement: @Composable () -> Unit = {
            Spacer(modifier = Modifier.size(10.dp))
            Text(
                text = "Try These First",
                style = mainTitle.copy(fontSize = 20.sp, textAlign = TextAlign.Start, fontWeight = FontWeight.Light),
                modifier = Modifier.padding(bottom = 6.dp).fillMaxWidth(.92f),
                color = MaterialTheme.colors.secondary
            )
            Text(
                text = "See if you are facing any of the following common issues. Touch to see the solutions.",
                modifier = Modifier.fillMaxWidth(.92f).padding(bottom = 12.dp).alpha(.8f),
                style = subtitle.copy(fontSize = 16.sp),
                color = MaterialTheme.colors.secondary
            )
        }
        val footerElement: @Composable (Boolean) -> Unit = { landscape: Boolean ->
            Text(
                text = "Didn't Work? Send Report",
                style = mainTitle.copy(fontSize = 20.sp, textAlign = TextAlign.Start, fontWeight = FontWeight.Light),
                modifier = Modifier.padding(bottom = 6.dp, top = 20.dp).fillMaxWidth(.92f),
                color = MaterialTheme.colors.secondary
            )
            Text(
                text = "If you are still not satisfied, click the button below to contact us directly and we will try to solve your issue ASAP.",
                modifier = Modifier.fillMaxWidth(.92f).padding(bottom = 12.dp).alpha(.8f),
                style = subtitle.copy(fontSize = 16.sp),
                color = MaterialTheme.colors.secondary)
            Button(
                onClick = {
                    AnalyticsManager.reportButton()
                    navigate(DrawerScreens.Report.route)
                          },
                modifier = Modifier.fillMaxWidth(if (landscape) 0.4f else 0.92f).padding(top = 10.dp)) {
                Icon(
                    imageVector = Icons.Filled.Report,
                    contentDescription = "",
                    tint = MaterialTheme.colors.secondary)
                Text(
                    text = "  Report Issue/ Give Feedback",
                    color = MaterialTheme.colors.secondary)
            }
            Spacer(modifier = Modifier.size(20.dp))
        }




        if (screenWidth < 600) {
            // activity.setScreenOrientation(orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
            LazyColumn(horizontalAlignment = Alignment.CenterHorizontally) {
                item {
                    headingElement()
                }
                items(cardList, SupportCard::id) {
                    ExpandableSupportCard(
                        card = it,
                        onCardArrowClick = {
                            if (it.id == viewModel.selectedState)
                                viewModel.updateSelectedState(0)
                            else
                                viewModel.updateSelectedState(it.id)
                        },
                        expanded = (selected == it.id),
                        landscape = false)
                }
                item {
                    footerElement(false)
                }
            }
        } else {
            //  activity.setScreenOrientation(orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
            Column(
                modifier = Modifier.verticalScroll(state = rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                headingElement()
                VerticalGrid(modifier = Modifier.padding(end = 15.dp), columns = 2) {
                    cardList.forEach {
                        ExpandableSupportCard(
                            card = it,
                              onCardArrowClick = {
                                  if (it.id == viewModel.selectedState)
                                      viewModel.updateSelectedState(0)
                                  else
                                      viewModel.updateSelectedState(it.id)
                              },
                            expanded = (selected == it.id),
                            landscape = true,
                        )
                    }
                }
                footerElement(true)
            }
        }
    }

    }




@Composable
fun ExpandableSupportCard(
    card: SupportCard,
    onCardArrowClick: () -> Unit,
    expanded: Boolean,
    landscape: Boolean
) {
    val faqCard = FAQCard(card.id, card.title, card.content)
    ExpandableCard(
        card = faqCard,
        onCardArrowClick = onCardArrowClick,
        expanded = expanded,
        landscape = landscape
    )
}

@Composable
fun ExpandableCard(card: FAQCard, onCardArrowClick: () -> Unit, expanded: Boolean, landscape: Boolean = false) {
    val cardExpandedBackgroundColor = MaterialTheme.colors.primary
    val cardCollapsedBackgroundColor = MaterialTheme.colors.surface
    val transition = updateTransition(targetState = !expanded, label = "")

    val cardBgColor by transition.animateColor({
        tween(durationMillis = EXPAND_ANIMATION_DURATION)
    }, label = "") {
        if (!it) /*cardExpandedBackgroundColor*/cardCollapsedBackgroundColor else cardCollapsedBackgroundColor
    }

    val cardElevation by transition.animateDp({
        tween(durationMillis = EXPAND_ANIMATION_DURATION)
    }, label = "") {
        if (it) 18.dp else 12.dp
    }

    val cardRoundedCorners by transition.animateDp({
        tween(
            durationMillis = EXPAND_ANIMATION_DURATION, easing = FastOutSlowInEasing
        )
    }, label = "") {
        if (it) 4.dp else 8.dp
    }

    val arrowRotationDegree by transition.animateFloat({
        tween(durationMillis = EXPAND_ANIMATION_DURATION)
    }, label = "") {
        if (!it) 0f else 180f
    }

    val contentColour by transition.animateColor({
        tween(durationMillis = 400)
    }, label = "") {
        if (!it) MaterialTheme.colors.secondary else MaterialTheme.colors.primary
    }

    Card(
        backgroundColor = cardBgColor, contentColor = contentColour, elevation = cardElevation,
        shape = RoundedCornerShape(cardRoundedCorners),
        modifier = Modifier.fillMaxWidth().wrapContentHeight()
            .padding(start = 15.dp, end = 15.dp, bottom = 7.dp,
                top = if (landscape) 0.dp else {
                    if (card.id == 1) 4.dp else 0.dp
                })) {
        Column(
            modifier = Modifier.fillMaxWidth(.9f)
                .clickable(onClick = onCardArrowClick),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically) {
                CardArrow(
                    degrees = arrowRotationDegree, onClick = onCardArrowClick)
                CardTitle(title = card.title)
            }
            ExpandableContent(
                visible = expanded, initialVisibility = expanded, content = card.content)
        }
    }
}


@Composable
fun CardTitle(title: String) {
    Text(text = title,
        style = heading.copy(fontSize = 16.sp, fontWeight = FontWeight.Light, textAlign = TextAlign.Start),
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        color = Color.White)
}

@Composable
fun CardArrow(degrees: Float, onClick: () -> Unit) {
    IconButton(onClick = onClick, content = {
        Icon(
            imageVector = Icons.Filled.KeyboardArrowUp,
            contentDescription = "",
            tint = Color.White,
            modifier = Modifier
                .size(20.dp)
                .rotate(degrees))
    })
}

@Composable
fun ExpandableContent(visible: Boolean = true, initialVisibility: Boolean = false, content: String) {
    val enterTransition = remember {
        expandVertically(
            expandFrom = Alignment.Top, animationSpec = tween(EXPANSION_TRANSITION_DURATION)
        ) + fadeIn(
            initialAlpha = 0.3f, animationSpec = tween(EXPANSION_TRANSITION_DURATION))
    }
    val exitTransition = remember {
        shrinkVertically(
            shrinkTowards = Alignment.Top, animationSpec = tween(EXPANSION_TRANSITION_DURATION)
        ) + fadeOut(
            animationSpec = tween(EXPANSION_TRANSITION_DURATION))
    }

    AnimatedVisibility(visibleState = remember {
        MutableTransitionState(initialState = initialVisibility)
    }.apply { targetState = visible },
        modifier = Modifier,
        enter = enterTransition,
        exit = exitTransition) {
        Column(modifier = Modifier.padding(bottom = 16.dp, start = 16.dp, end = 16.dp)) {
            Text(
                text = content,
                style = MaterialTheme.typography.body1,
                fontWeight = FontWeight.Light,
                color = Color.White)
        }
    }
}
