package com.antcloud.app.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.antcloud.app.theme.AntCloudNewFlowTheme
import com.antcloud.app.theme.dark_grey
import com.antcloud.app.theme.subtitle

@Composable
fun Loading() {
    Loading(text = "")
}
@Composable
fun Loading(text: String, increaseSize: Boolean = false, landscape: Boolean = false) {
    val content: @Composable () -> Unit = {
        Text(text, style = subtitle.copy(fontSize = if(!increaseSize) 20.sp else 40.sp))
        Spacer(modifier = Modifier.size(if(!increaseSize) 30.dp else 40.dp))
        CircularProgressIndicator(color = MaterialTheme.colors.secondary,
            modifier = if(!increaseSize)
                Modifier else Modifier.size(40.dp))
        Spacer(modifier = Modifier.size(if(!increaseSize) 30.dp else 40.dp))
    }
    AntCloudNewFlowTheme {
        if(landscape) {
            Row(
                modifier = Modifier.fillMaxWidth().fillMaxHeight(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center) {
                content()
            }
        }
        else {
            Column(
                modifier = Modifier.fillMaxWidth().fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center) {
                rememberSystemUiController().setStatusBarColor(
                    color = dark_grey,
                    darkIcons = true
                )
                content()
            }
        }
    }
}