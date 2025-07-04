package com.limelight.screen.account

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DrawerValue
import androidx.compose.material.ModalDrawer
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController

import com.limelight.screen.support.PrivacyNav
import com.limelight.screen.support.reportNav
import com.limelight.screen.support.supportNav
import com.limelight.screen.support.termsNav
import com.limelight.screen.price.pricingNav


import com.limelight.components.Drawer
import com.limelight.common.DrawerScreens
import com.limelight.common.GlobalData
import com.limelight.activity.NavActivity
import com.limelight.components.Toolbar
import com.limelight.screen.game.gameDetailNav
import com.limelight.screen.game.libraryDetailsNav
import com.limelight.screen.support.FAQNav
import com.limelight.viewmodel.GameViewModel
import com.limelight.viewmodel.UserViewModel

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@Composable
fun NavScreen(activity: NavActivity, navigationRoute: String, viewModel: GameViewModel) {
    val userViewModel : UserViewModel =  hiltViewModel()
    var routeValue = DrawerScreens.Library.route
    val navController = rememberNavController()
    Surface(color = Color.Black) {
        val drawerState = rememberDrawerState(DrawerValue.Closed)
        val scope = rememberCoroutineScope()
        val openDrawer = {
            scope.launch(Dispatchers.IO) {
                drawerState.open()
            }
        }
        var toolbarVisible by remember {
            mutableStateOf(viewModel.toolbarState)
        }
        viewModel.subToolbarState={
            toolbarVisible= it
        }

//        var navigationRoute by remember {
//            mutableStateOf(userViewModel.navRoute)
//        }
//        userViewModel.subNavRoute={
//            navigationRoute= it
//        }
//        val toggleToolbar: ((Boolean) -> Unit) = {
//            toolbarVisible = it
//        }
        var toolbarText by remember {
            mutableStateOf("")
        }
        var current by remember {
            mutableStateOf("")
        }
        val updateToolbar: ((String) -> Unit) = {
            toolbarText = it
        }
        navController.addOnDestinationChangedListener { _, dest, _ ->
            current = "${dest.route}"
        }

  Log.i("test" , "cvwvcwv")
        val navigate: ((String) -> Unit) = { uri: String -> navController.navigate(uri)}
        ModalDrawer(scrimColor = Color.Transparent,
            drawerShape = RoundedCornerShape(15.dp),
            drawerState = drawerState,
            gesturesEnabled = true,
            drawerContent = {
                Drawer(current = current, onDestinationClicked = { route ->
                        scope.launch {
                            drawerState.close()
                        }
                    GlobalData.getInstance().toolbarInvisible =  false
                    if(route=="product")
                        GlobalData.getInstance().gemsHistoryFlag =  1

                    if (route == "product"|| route == "gems")
                        viewModel.updateToolbarState(false)
                    else
                        viewModel.updateToolbarState(true)


                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }, screens = listOf(
                          DrawerScreens.Library,
                          DrawerScreens.Pricing,
//                          DrawerScreens.OrderHistory,
                          DrawerScreens.Support,
                          DrawerScreens.Account,
                          DrawerScreens.FAQs,
//                          DrawerScreens.Product,
//                          DrawerScreens.EarnGames,
//                        DrawerScreens.Policy,
//                        DrawerScreens.Terms,
//                        DrawerScreens.Tutorials,
//                        DrawerScreens.PingTest
                    ), activity = activity)
            }) {
            Box {
                Column(modifier = Modifier.padding(top = 0.dp)) {
                    if (toolbarVisible == true) {
                        Toolbar(
                            text = toolbarText,
                            modifier = Modifier,
                            icon = {},
                            navigate = navigate,
                            viewModel = viewModel,
                            openDrawer = { openDrawer() },
                            iconName = Icons.Filled.CurrencyRupee) }

                    NavHost(
                        navController = navController,
                        startDestination = navigationRoute) {
                          libraryNav(this, activity, updateToolbar, navigate)
                          pricingNav(this, activity, updateToolbar,navigate)
//                          orderHistoryNav(this, activity, updateToolbar,navigate)
                          supportNav(this, activity, updateToolbar, navigate)
                          accountNav(this, activity, updateToolbar, navigate)
                          reportNav(this, activity, updateToolbar)
//                          productNav(this, activity, updateToolbar , navigate) { openDrawer() }
//                          earnGemsNav(this, activity, updateToolbar,navigate,navController){ openDrawer() }
//                          earnGamesHistoryNav(this, activity, updateToolbar){ openDrawer() }
                          FAQNav(this, activity, updateToolbar,navigate)
                          PrivacyNav(this, activity, updateToolbar)
                          termsNav(this, activity, updateToolbar)
                          libraryDetailsNav(this, activity, updateToolbar,navigate)
                          gameDetailNav(this, activity, updateToolbar,navigate)
                    }
                }
            }
        }
    }
}