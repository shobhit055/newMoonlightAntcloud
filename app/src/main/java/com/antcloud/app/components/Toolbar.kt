package com.antcloud.app.components


import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.antcloud.app.viewmodel.GameViewModel
import com.antcloud.app.R


@Composable
fun Toolbar(modifier: Modifier, text: String, iconName: ImageVector, viewModel: GameViewModel, navigate: ((String) -> Unit), icon: @Composable () -> Unit) {
    Toolbar(modifier = modifier, text = text, iconName = iconName, viewModel = viewModel, navigate = navigate, openDrawer = {}, icon = icon)
}


@Composable
fun Toolbar(modifier: Modifier, text: String, viewModel: GameViewModel, navigate: ((String) -> Unit), iconName: ImageVector,
                       openDrawer: () -> Unit, icon: @Composable () -> Unit) {
    TopAppBar(modifier = modifier.statusBarsPadding(),
        backgroundColor = Color.Black,
        contentColor = Color.White,
        elevation = 0.dp,
        title = {
            Box(modifier = Modifier
                .fillMaxSize()) {
                IconButton(
                    onClick = openDrawer, modifier = Modifier.align(Alignment.TopStart)
                        .size(45.dp)
                        .padding(top = 6.dp)) {
                    Icon(
                        imageVector = Icons.Filled.Menu,
                        contentDescription = "",
                        tint = Color.White)
                }
                Image(modifier = Modifier.size(60.dp).align(Alignment.Center),
                    painter = painterResource(id = R.drawable.ant_cloud_white_icon),
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds)

//                androidx.compose.material3.Button(
//                    modifier = Modifier.height(35.dp).width(120.dp).align(Alignment.CenterEnd)
//                        .padding(start = 2.dp, end = 2.dp)
//                        .background(Color.White, shape = RoundedCornerShape(16.dp))
//                        .height(ButtonDefaults.MinHeight),
//                    onClick = {
//                        viewModel.updateToolbarState(false)
//                        navigate(DrawerScreens.EarnGames.route)
//                    },
                 //   colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)) {
//                    Row(verticalAlignment = Alignment.CenterVertically) {
//                        androidx.compose.material3.Icon(
//                            painter = painterResource(id = R.drawable.amethyst_icon),
//                            contentDescription = "Currency Icon",
//                            tint = Color(0xFFDB40E8),
//                            modifier = Modifier.padding(start = 1.dp).size(18.dp))
//                        Spacer(modifier = Modifier.size(2.dp))
//                        Text(
//                            text = "200+", color = MaterialTheme.colors.surface, fontSize = 12.sp, style = MaterialTheme.typography.button)
//                    }
              //  }
            }
        },
        actions = {
            icon()
        })
}

