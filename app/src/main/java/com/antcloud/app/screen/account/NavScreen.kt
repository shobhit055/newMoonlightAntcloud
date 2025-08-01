package com.antcloud.app.screen.account

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

import com.antcloud.app.screen.support.PrivacyNav
import com.antcloud.app.screen.support.reportNav
import com.antcloud.app.screen.support.supportNav
import com.antcloud.app.screen.support.termsNav
import com.antcloud.app.screen.price.pricingNav


import com.antcloud.app.components.Drawer
import com.antcloud.app.common.DrawerScreens
import com.antcloud.app.common.GlobalData
import com.antcloud.app.activity.NavActivity
import com.antcloud.app.components.Toolbar
import com.antcloud.app.screen.game.gameDetailNav
import com.antcloud.app.screen.game.libraryDetailsNav
import com.antcloud.app.screen.support.FAQNav
import com.antcloud.app.viewmodel.GameViewModel
import com.antcloud.app.viewmodel.UserViewModel

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
                          DrawerScreens.Support,
                          DrawerScreens.Account,
                          DrawerScreens.FAQs,

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
                          supportNav(this, activity, updateToolbar, navigate)
                          accountNav(this, activity, updateToolbar, navigate)
                          reportNav(this, activity, updateToolbar)
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