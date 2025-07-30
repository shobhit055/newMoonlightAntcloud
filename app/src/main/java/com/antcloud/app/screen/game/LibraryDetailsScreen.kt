@file:OptIn(ExperimentalFoundationApi::class)

package com.antcloud.app.screen.game


import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.antcloud.app.activity.NavActivity
import com.antcloud.app.common.DrawerScreens
import com.antcloud.app.common.GlobalData
import com.antcloud.app.data.Game
import com.antcloud.app.screen.account.AsyncImages
import com.antcloud.app.theme.BlueGradient
import com.antcloud.app.theme.PinkGradient



fun libraryDetailsNav(navGraph: NavGraphBuilder, activity: NavActivity, updateToolbar: ((String) -> Unit), navigate: ((String) -> Unit)) {
    return navGraph.composable(DrawerScreens.LibraryDetails.route) {
        val globalData = GlobalData.getInstance()
        val detailsName = globalData.libraryDetailsName
        val games by remember {
            mutableStateOf(globalData.ourGames[0].games)
        }
        updateToolbar(detailsName)
        val gridState = rememberLazyGridState()
        Column(horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)) {
            LazyVerticalGrid(columns = GridCells.Fixed(1), state = gridState) {
                items(games, key = { it.gameId }) {
                    GameTileAllGames(game = it, modifier = Modifier.animateItem(), false, orientationLandscape = false) {
                        GlobalData.getInstance().gameId= it.gameId
                        navigate(DrawerScreens.GameDetails.route)
                    }
                }
            }
        }
    }
}

@Composable
fun GameTileAllGames(game: Game, modifier: Modifier, expand: Boolean = false, orientationLandscape: Boolean = false, onClick: (() -> Unit)) {
    val gradientColors = listOf(PinkGradient,BlueGradient)
    Box(
        modifier =
        if(orientationLandscape)
            Modifier.padding(start = 5.dp , end = 5.dp , top = 5.dp,)
                .clickable {
                    // AnalyticsManager.gameButton(game.gameId)
                    onClick()
                }
        else modifier.fillMaxWidth().padding(top = 20.dp)
            .clickable {
                //    AnalyticsManager.gameButton(game.gameId)
                onClick()
            }) {
//        globalInstance.imageLoading = true
//        globalInstance.traceImageLoading =  FirebasePerformance.getInstance().newTrace("image_loading")
//        globalInstance.traceImageLoading.start()
        AsyncImages(
//            url = if (!expand) "https://antplay-gamedata.s3.ap-south-1.amazonaws.com/${game.gameId}.jpg"
            url = if (!expand) "https://antplay-gamedata.s3.ap-south-1.amazonaws.com/"
            else {
                if(orientationLandscape)
                    "https://antplay-gamedata.s3.ap-south-1.amazonaws.com/background_landscape.jpg"
                else
                    "https://antplay-gamedata.s3.ap-south-1.amazonaws.com/background.jpg"
            },
            modifier = if(orientationLandscape)
                Modifier.fillMaxHeight(0.5f).clip(RoundedCornerShape(25.dp))
            else
                Modifier.clip(RoundedCornerShape(25.dp)).fillMaxWidth(.9f).height(150.dp).border(
                        brush = Brush.verticalGradient(gradientColors),
            width = 1.dp,
            shape = RoundedCornerShape(25.dp)).align(Alignment.Center),
            orientationLandscape = orientationLandscape)
    }
}
