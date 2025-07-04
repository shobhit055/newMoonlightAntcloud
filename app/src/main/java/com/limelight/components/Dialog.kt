
package com.limelight.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun CustomDialog(
    openDialogCustom: MutableState<Boolean>,
    label: String,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit) {
    var dismissOutside = false
    if(label == "PG" || label == "Plan Change Warning") {
        dismissOutside = true
    }
    if (openDialogCustom.value) {
        Dialog(
            onDismissRequest = {
                openDialogCustom.value = false
                onDismiss()
            },
            content = content,
            properties = DialogProperties(dismissOnClickOutside = dismissOutside, usePlatformDefaultWidth = false))
    }
}

@Composable
fun CustomDialog(openDialogCustom: Boolean,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit) {
    if (openDialogCustom) {
        Dialog(onDismissRequest = {
                onDismiss()
            }, content = content,
            properties = DialogProperties(dismissOnClickOutside = false))
    }
}

@Composable
fun CustomDialog(
    openDialogCustom: Boolean,
    modifier: Modifier,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    if (openDialogCustom) {
        /*Box(
            modifier = modifier.clickable(enabled = true, onClick = { Log.d("webView", "box clicked")})
        ) {
            content()
        }*/
        Dialog(
            onDismissRequest = {
                onDismiss()
            },
            properties = DialogProperties(dismissOnClickOutside = false, dismissOnBackPress = true, usePlatformDefaultWidth = false),
            content = content
        ) /*{
            Box(modifier = Modifier.fillMaxWidth()) {
                content()
            }
        }*/
    }
}