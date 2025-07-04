package com.limelight.screen

import android.os.Build
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.limelight.activity.WebViewActivity
import com.limelight.components.CustomDialog
import com.limelight.components.Loading
import com.limelight.theme.subtitle
import com.limelight.viewmodel.WebViewModel

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filterIsInstance
import java.text.DecimalFormat
import java.time.LocalDateTime
import java.time.ZoneOffset


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WebViewScreen(url: String,
                  backPressed: Boolean,
                  loading: Boolean,
                  joyStick: Boolean,
                  gyroscope :Boolean,
                  dragEnabled: Boolean,
                  updateFpsControls: Boolean,
                  activity: WebViewActivity,
                  webView: WebView,
                  viewModel: WebViewModel?) {

    Box {
        AndroidView(factory = {
            webView
        }, update = {
            it.loadUrl(url)
        })
        if(loading) {
            Dialog(onDismissRequest = {
                activity.subBackPressed?.invoke(true)
//                activity.subLoading?.invoke(false)
//                activity.subLoading?.invoke(true)
                },
                properties = DialogProperties(
                    usePlatformDefaultWidth = false,
                    dismissOnClickOutside = false,
                    dismissOnBackPress = true)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .background(MaterialTheme.colors.surface)
                        .clip(
                            RoundedCornerShape(10.dp)
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center) {
                    Loading("Loading ...", true)
                }
            }
        }

//        if (joyStick && viewModel != null) {
//            var lastControlsMessage by remember {
//                mutableStateOf(fpsControlsMessage().toString())
//            }
//
//            var gamePadComponents by remember {
//                //mutableStateListOf(*viewModel.addedGamePadComponents.map { it }.toTypedArray())
//                mutableStateOf(viewModel.addedGamePadComponents)
//            }
//
//            viewModel.subAddedGamePadComponentsPosition = { index, position ->
//                gamePadComponents[index].position = position
//            }
//
//            viewModel.subAddedGamePadComponentsKeys = { index, key ->
//                gamePadComponents[index].key = key
//            }
//
//            viewModel.subAddedGamePadComponentsToggleState = { index, toggleState ->
//                gamePadComponents[index].toggled = toggleState
//            }
//
//            /*viewModel.subAddGamePadComponent = {
//                Log.d("webView", "gamepadComponents size: ${gamePadComponents.size}")
//                gamePadComponents.add(it)
//            }*/
//
//            viewModel.subRemoveGamePadComponent = {
//                gamePadComponents.remove(it)
//            }
//
//            /*var subGamePadComponents: ((SnapshotStateList<gamePadUiComponents>) -> Unit) = {
//                gamePadComponents = it
//            }*/
//
//            var stateUpdate = remember {
//                mutableStateOf(false)
//            }
//            var subStateUpdate : ((Boolean) -> Unit) = {
//                stateUpdate.value = it
//            }
//
//            /*val selectedKeyDropdown = remember {
//                mutableStateListOf(*gamePadComponents.map { it.key }.toTypedArray())
//            }*/
//
//            val selectedKeysDropdown = remember {
//                mutableStateMapOf<String, joystickKeys>().apply {
//                    gamePadComponents.forEach {
//                        this[it.name] = it.key
//                    }
//                }
//            }
//
//            val buttonToggleState = remember {
//                mutableStateMapOf<String, Boolean>().apply {
//                    gamePadComponents.forEach {
//                        this[it.name] = it.toggled
//                    }
//                }
//            }
//
//            val screenWidth = with(LocalDensity.current) { LocalConfiguration.current.screenWidthDp.dp.toPx() }
//
//            val screenHeight = with(LocalDensity.current) { LocalConfiguration.current.screenHeightDp.dp.toPx() }
//
//            LaunchedEffect(Unit) {
//                //check if local joystick config exists
//                val sharedJoystickConfigHelper = sharedJoystickConfigHelper(activity)
//                if (sharedJoystickConfigHelper.checkConfig()) {
//                    //Log.d("webView", "reading from shared config")
//                    val sharedJoystickComponents = sharedJoystickConfigHelper.getConfig()
//                    if (sharedJoystickComponents.size != 0) {
//                        viewModel.clearGamePadComponents()
//                        gamePadComponents.clear()
//                        sharedJoystickComponents.forEach {
//                            viewModel.addGamePadComponent(it)
//                            if (!it.isJoyStick) {
//                                updateControlsMessageKey(it.name, it.key)
//                                selectedKeysDropdown[it.name] = it.key
//                                buttonToggleState[it.name] = it.toggled
//                            }
//                        }
//                    }
//                } else {
//                    //Log.d("webView", "storing shared config")
//                    sharedJoystickConfigHelper.saveConfig(viewModel.addedGamePadComponents)
//                }
//            }
//
//            LaunchedEffect(Unit) {
//                while (joyStick) {
//                    //Log.d("webView", "${lastControlsMessage != controlsMessage.toString()}")
//                    if (lastControlsMessage != controlsMessage.toString()) {
//                        //activity.subLastControlsMessage?.invoke(controlsMessage)
//                        //viewModel?.updateLastControlsMessage(controlsMessage)
//                        lastControlsMessage = controlsMessage.toString()
//                        activity.sendUpdatedControls(controlsMessage)
//                    }
//                    delay(50)
//                }
//            }
//
//            val buttonInteractionHandlers = ArrayList<ButtonInteractionHandler>()
//            controlsMessage::class.declaredMemberProperties.filter { it.returnType.classifier == buttonMessage::class }.forEach {
//                property ->
//                    val propertyClass = property.getter.call(controlsMessage)
//                    if(propertyClass is buttonMessage) {
//                        val interactionSource = remember { MutableInteractionSource() }
//                        val toggleState = remember { mutableStateOf(false)}
//                        val toggleReleaseState = remember { mutableStateOf(false)}
//                        buttonInteractionHandlers.add(
//                            ButtonInteractionHandler(property.name, propertyClass, interactionSource,
//                                interactionSource.collectIsPressedAsState(), toggleState, toggleReleaseState))
//                    }
//            }
//
//            DraggableScreen(modifier = Modifier.fillMaxSize(), update = stateUpdate.value) {
//                val decimalFormat = remember { DecimalFormat("#.###") }
//                Box(modifier = Modifier.fillMaxHeight()) {
//                    if (gamePadComponents.contains(gamePadUiComponents("rj", isJoyStick = true))) {
//                        DragButton(
//                            modifier = Modifier.fillMaxSize(),
//                            enableDrag = false,
//                            position = Offset.Zero,
//                            index = gamePadComponents.indexOf(gamePadUiComponents("rj", isJoyStick = true)),
//                            viewModel = viewModel
//                        ) {
//                            PointerMotionEventsSample()
//                        }
//                    }
//                    gamePadComponents.forEachIndexed { index, component ->
//                        LaunchedEffect(component.position) {
//                            //Log.d("webView", "updated position ${component.name}: ${component.position}")
//                        }
//                        if(component.isJoyStick) {
//                            if(component.name == "lj") {
//                                DragButton(
//                                    modifier = Modifier.size(140.dp),
//                                    enableDrag = (dragEnabled),
//                                    position = Offset(
//                                        if (component.position.x == -100f) {
//                                            30.dp.value * 2
//                                        } else {
//                                            Dp(component.position.x).value
//                                        }, if (component.position.y == -100f) {
//                                            screenHeight - 300
//                                        } else {
//                                            Dp(component.position.y).value
//                                        }
//                                    ),
//                                    index = index,
//                                    viewModel = viewModel
//                                ) {
//                                    if (component.name == "lj") {
//                                        Joystick(
//                                            modifier = Modifier,
//                                            stickSize = 140.dp,
//                                            dotSize = 60.dp,
//                                            dragEnabled = dragEnabled
//                                        ) { xPercent, yPercent ->
//                                            controlsMessage.lj.x =
//                                                decimalFormat.format(xPercent).toDouble() * (1)
//                                            controlsMessage.lj.y =
//                                                decimalFormat.format(yPercent).toDouble() * (-1)
//                                        }
//                                    }
//                                }
//                            }
//                        } else {
//                            if(component.name != "prevWeapon" && component.name != "nextWeapon") {
//                                DragButton(
//                                    modifier = Modifier,
//                                    enableDrag = dragEnabled,
//                                    position = when (component.name) {
//                                        "shoot" -> {
//                                            //Log.d("webView", "current shoot: ${component.position}")
//                                            if(component.position == Offset((-100f), (-100f))) {
//                                                Offset(screenWidth * 0.85f, screenHeight * 0.7f)
//                                            } else {
//                                                Offset(component.position.x, component.position.y)
//                                            }
//                                        }
//
//                                        "scope" -> {
//                                            if(component.position == Offset((-100f), (-100f))) {
//                                                Offset(screenWidth * 0.75f, screenHeight * 0.52f)
//                                            } else {
//                                                Offset(component.position.x, component.position.y)
//                                            }
//                                        }
//
//                                        "reload" -> {
//                                            if(component.position == Offset((-100f), (-100f))) {
//                                                Offset(screenWidth * 0.7f, screenHeight * 0.93f)
//                                            } else {
//                                                Offset(component.position.x, component.position.y)
//                                            }
//                                        }
//
//                                        "jump" -> {
//                                            if(component.position == Offset((-100f), (-100f))) {
//                                                Offset(screenWidth * 0.9f, screenHeight * 0.55f)
//                                            } else {
//                                                Offset(component.position.x, component.position.y)
//                                            }
//                                        }
//
//                                        "crouch" -> {
//                                            if(component.position == Offset((-100f), (-100f))) {
//                                                Offset(screenWidth * 0.9f, screenHeight * 0.94f)
//                                            } else {
//                                                Offset(component.position.x, component.position.y)
//                                            }
//                                        }
//
//                                        "sprint" -> {
//                                            if(component.position == Offset((-100f), (-100f))) {
//                                                Offset(screenWidth * 0.25f, screenHeight * 0.9f)
//                                            } else {
//                                                Offset(component.position.x, component.position.y)
//                                            }
//                                        }
//
//                                        "melee" -> {
//                                            if(component.position == Offset((-100f), (-100f))) {
//                                                Offset(screenWidth * 0.72f, screenHeight * 0.75f)
//                                            } else {
//                                                Offset(component.position.x, component.position.y)
//                                            }
//                                        }
//
//                                        "useItem" -> {
//                                            if(component.position == Offset((-100f), (-100f))) {
//                                                Offset(screenWidth * 0.82f, screenHeight * 0.5f)
//                                            } else {
//                                                Offset(component.position.x, component.position.y)
//                                            }
//                                        }
//
//                                        "heal" -> {
//                                            if(component.position == Offset((-100f), (-100f))) {
//                                                Offset(screenWidth * 0.8f, screenHeight * 0.95f)
//                                            } else {
//                                                Offset(component.position.x, component.position.y)
//                                            }
//                                        }
//
//                                        "menu" -> {
//                                            if(component.position == Offset((-100f), (-100f))) {
//                                                Offset(screenWidth * 0.02f, screenHeight * 0.01f)
//                                            } else {
//                                                Offset(component.position.x, component.position.y)
//                                            }
//                                        }
//
//                                        "score" -> {
//                                            if(component.position == Offset((-100f), (-100f))) {
//                                                Offset(screenWidth * 0.02f, screenHeight * 0.18f)
//                                            } else {
//                                                Offset(component.position.x, component.position.y)
//                                            }
//                                        }
//
//                                        "dropWeapon" -> {
//                                            if(component.position == Offset((-100f), (-100f))) {
//                                                Offset(screenWidth * 0.58f, screenHeight * 0.98f)
//                                            } else {
//                                                Offset(component.position.x, component.position.y)
//                                            }
//                                        }
//
//                                        else -> {
//                                            Offset.Zero
//                                        }
//                                    },
//                                    index = index,
//                                    viewModel = viewModel
//                                ) {
//                                    CircularImageWithClick(
//                                        imageResId = component.iconRes,
//                                        modifier = Modifier
//                                            .size(if (component.name == "shoot") 70.dp else 45.dp)
//                                            .clip(CircleShape)
//                                            .border(
//                                                1.dp,
//                                                MaterialTheme.colors.secondary.copy(alpha = 0.3f),
//                                                shape = CircleShape
//                                            )
//                                            .clickable(
//                                                interactionSource = buttonInteractionHandlers.first { it.name == component.name }.interactionSource,
//                                                indication = rememberRipple(bounded = true),
//                                                onClick = {})
//                                    )
//                                }
//                            } else {
//                                DragButton(
//                                    modifier = Modifier,
//                                    //viewModel = viewModel,
//                                    enableDrag = dragEnabled,
//                                    position = when (component.name) {
//                                        "prevWeapon" -> {
//                                            if(component.position == Offset((-100f), (-100f))) {
//                                                Offset(screenWidth * 0.4f, screenHeight * 0.99f)
//                                            } else {
//                                                Offset(component.position.x, component.position.y)
//                                            }
//                                        }
//                                        "nextWeapon" -> {
//                                            if(component.position == Offset((-100f), (-100f))) {
//                                                Offset(screenWidth * 0.5f, screenHeight * 0.99f)
//                                            } else {
//                                                Offset(component.position.x, component.position.y)
//                                            }
//                                        }
//                                        else -> {
//                                            Offset.Zero
//                                        }
//                                    },
//                                    index = index,
//                                    viewModel = viewModel
//                                ) {
//                                    IconButton(
//                                        onClick = {},
//                                        modifier = Modifier.size(50.dp),
//                                        interactionSource = buttonInteractionHandlers
//                                            .first { it.name == component.name }.interactionSource) {
//                                        Row(verticalAlignment = Alignment.CenterVertically) {
//                                            Icon(
//                                                painter = painterResource(id = component.iconRes),
//                                                contentDescription = "",
//                                                modifier = Modifier
//                                                    .size(20.dp)
//                                                    .scale(
//                                                        if (component.name == "prevWeapon") 1f else -1f,
//                                                        1f
//                                                    ),
//                                                tint = MaterialTheme.colors.secondary.copy(alpha = 0.6f)
//                                            )
//                                            Spacer(modifier = Modifier.size(5.dp))
//                                            Text(text = if(component.name == "prevWeapon") "Prev." else "Next", style = mainTitle.copy(
//                                                fontSize = 10.sp,
//                                                textAlign = TextAlign.Start,
//                                                fontWeight = FontWeight.Light))
//                                        }
//                                    }
//                                }
//                            }
//                            buttonInteractionHandlers.forEach { buttonInteractionHandler ->
//                                LaunchedEffect(buttonInteractionHandler.interactionSource, dragEnabled) {
//                                    buttonInteractionHandler.interactionSource.interactions
//                                        .filterIsInstance<PressInteraction>()
//                                        .collect {
//                                            if(!dragEnabled){
//                                                when (it) {
//                                                    is PressInteraction.Press -> {
//                                                        //Log.d("webView", "${buttonInteractionHandler.name} pressed")
//                                                        if(buttonToggleState[buttonInteractionHandler.name] == true && !buttonInteractionHandler.toggleReleaseState.value){
//                                                            buttonInteractionHandler.toggleReleaseState.value = true
//                                                            buttonInteractionHandler.toggleState.value = !buttonInteractionHandler.toggleState.value
//                                                            //Log.d("webView", "${buttonInteractionHandler.name} toggle: ${buttonInteractionHandler.toggleState.value}")
//                                                        }
//                                                        buttonInteractionHandler.button.pressed = true
//                                                    }
//
//                                                    is PressInteraction.Release, is PressInteraction.Cancel -> {
//                                                        //Log.d("webView", "${buttonInteractionHandler.name} released: ${buttonInteractionHandler.toggleState.value}")
//                                                        if(!buttonInteractionHandler.toggleState.value) {
//                                                            buttonInteractionHandler.button.pressed = false
//                                                        }
//                                                        buttonInteractionHandler.toggleReleaseState.value = false
//                                                    }
//                                                }
//                                            }
//                                        }
//                                }
//                            }
//                        }
//                    }
//
//                    /*if(!joyStick) {
//                        PointerMotionEventsSample()
//                        Joystick(
//                            modifier = Modifier
//                                .align(Alignment.BottomStart)
//                                .offset(x = 10.dp, y = (-20).dp),
//                            stickSize = 140.dp,
//                            dotSize = 60.dp,
//                            dragEnabled = false
//                        ) { xPercent, yPercent ->
//                            controlsMessage.lj.x = decimalFormat.format(xPercent).toDouble()
//                            controlsMessage.lj.y = decimalFormat.format(yPercent).toDouble()
//                        }
//                        Row(modifier = Modifier.align(Alignment.BottomCenter)) {
//                            IconButton(
//                                onClick = {},
//                                modifier = Modifier
//                                    .align(Alignment.CenterVertically)
//                                    .size(width = 50.dp, height = 25.dp)
//                                    .background(MaterialTheme.colors.primary.copy(alpha = 0.3f))
//                                    .padding(top = 2.dp, end = 0.dp),
//                                interactionSource = buttonInteractionHandlers
//                                    .first { it.name == "dropWeapon" }.interactionSource
//                            ) {
//                                Icon(
//                                    painter = painterResource(id = R.drawable.drop_weapon),
//                                    contentDescription = "",
//                                    modifier = Modifier.size(20.dp),
//                                    tint = MaterialTheme.colors.secondary.copy(alpha = 0.6f)
//                                )
//                            }
//                            Spacer(modifier = Modifier.size(1.dp))
//                            IconButton(onClick = {}, modifier = Modifier
//                                .size(width = 80.dp, height = 25.dp)
//                                .background(MaterialTheme.colors.primary.copy(alpha = 0.3f))
//                                .padding(top = 0.dp, end = 0.dp),
//                                interactionSource = buttonInteractionHandlers
//                                    .first { it.name == "prevWeapon" }.interactionSource
//                            ) {
//                                Row(verticalAlignment = Alignment.CenterVertically) {
//                                    Icon(
//                                        painter = painterResource(id = R.drawable.left_gun),
//                                        contentDescription = "",
//                                        modifier = Modifier.size(20.dp),
//                                        tint = MaterialTheme.colors.secondary.copy(alpha = 0.6f)
//                                    )
//                                    Spacer(modifier = Modifier.size(5.dp))
//                                    Text(
//                                        text = "Prev.", style = mainTitle.copy(
//                                            fontSize = 10.sp,
//                                            textAlign = TextAlign.Start,
//                                            fontWeight = FontWeight.Light
//                                        )
//                                    )
//                                }
//                            }
//                            Spacer(modifier = Modifier.size(1.dp))
//                            IconButton(onClick = {}, modifier = Modifier
//                                .size(width = 80.dp, height = 25.dp)
//                                .background(MaterialTheme.colors.primary.copy(alpha = 0.3f))
//                                .padding(),
//                                interactionSource = buttonInteractionHandlers
//                                    .first { it.name == "nextWeapon" }.interactionSource
//                            ) {
//                                Row(verticalAlignment = Alignment.CenterVertically) {
//                                    Icon(
//                                        painter = painterResource(id = R.drawable.right_gun),
//                                        contentDescription = "",
//                                        modifier = Modifier
//                                            .size(20.dp)
//                                            .scale(-1f, 1f),
//                                        tint = MaterialTheme.colors.secondary.copy(alpha = 0.6f)
//                                    )
//                                    Spacer(modifier = Modifier.size(5.dp))
//                                    Text(
//                                        text = "Next", style = mainTitle.copy(
//                                            fontSize = 10.sp,
//                                            textAlign = TextAlign.Start,
//                                            fontWeight = FontWeight.Light
//                                        )
//                                    )
//                                }
//                            }
//                        }
//                        Icon(
//                            imageVector = Icons.Filled.Settings,
//                            contentDescription = "",
//                            modifier = Modifier
//                                .align(Alignment.TopEnd)
//                                //.padding(top = (screenHeight * 0.01f), end = (screenWidth * 0.02f))
//                                .size(26.dp)
//                                .clickable { },
//                            tint = MaterialTheme.colors.secondary.copy(alpha = 0.6f)
//                        )
//                        CircularImageWithClick(
//                            imageResId = R.drawable.list,
//                            modifier = Modifier
//                                .align(Alignment.TopStart)
//                                .padding(start = 20.dp, top = 10.dp)
//                                .size(40.dp)
//                                .clip(CircleShape)
//                                .border(
//                                    1.dp,
//                                    MaterialTheme.colors.secondary.copy(alpha = 0.3f),
//                                    shape = CircleShape
//                                )
//                                .clickable(
//                                    interactionSource = buttonInteractionHandlers.first { it.name == "menu" }.interactionSource,
//                                    indication = LocalIndication.current,
//                                    onClick = {})
//                        )
//                        CircularImageWithClick(
//                            imageResId = R.drawable.select_icon,
//                            modifier = Modifier
//                                .align(Alignment.TopStart)
//                                .padding(start = 70.dp, top = 10.dp)
//                                .scale(-1f, 1f)
//                                .size(40.dp)
//                                .clip(CircleShape)
//                                .border(
//                                    1.dp,
//                                    MaterialTheme.colors.secondary.copy(alpha = 0.3f),
//                                    shape = CircleShape
//                                )
//                                .clickable(
//                                    interactionSource = buttonInteractionHandlers.first { it.name == "score" }.interactionSource,
//                                    indication = LocalIndication.current,
//                                    onClick = {})
//                        )
//                        CircularImageWithClick(
//                            imageResId = R.drawable.reload_gun_barrel,
//                            modifier = Modifier
//                                .align(Alignment.BottomEnd)
////                                .padding(
////                                    bottom = (screenHeight * 0.07f),
////                                    end = (screenWidth * 0.18f)
////                                )
//                                .size(40.dp)
//                                .clip(CircleShape)
//                                .border(
//                                    1.dp,
//                                    MaterialTheme.colors.secondary.copy(alpha = 0.3f),
//                                    shape = CircleShape
//                                )
//                                .clickable(
//                                    interactionSource = buttonInteractionHandlers.first { it.name == "reload" }.interactionSource,
//                                    indication = LocalIndication.current,
//                                    onClick = {})
//                        )
//                        CircularImageWithClick(
//                            imageResId = R.drawable.fire_weapon,
//                            modifier = Modifier
//                                .align(Alignment.BottomEnd)
////                                .padding(
////                                    bottom = (screenHeight * 0.2f),
////                                    end = (screenWidth * 0.08f)
////                                )
//                                .size(70.dp)
//                                .clip(CircleShape)
//                                .border(
//                                    1.dp,
//                                    MaterialTheme.colors.secondary.copy(alpha = 0.3f),
//                                    shape = CircleShape
//                                )
//                                .clickable(
//                                    interactionSource = buttonInteractionHandlers.first { it.name == "shoot" }.interactionSource,
//                                    indication = LocalIndication.current,
//                                    onClick = {})
//                        )
//                        CircularImageWithClick(
//                            imageResId = R.drawable.jump,
//                            modifier = Modifier
//                                .align(Alignment.TopEnd)
//                                //.padding(top = (screenHeight * 0.55f), end = (screenWidth * 0.03f))
//                                .size(40.dp)
//                                .clip(CircleShape)
//                                .border(
//                                    1.dp,
//                                    MaterialTheme.colors.secondary.copy(alpha = 0.3f),
//                                    shape = CircleShape
//                                )
//                                .clickable(
//                                    interactionSource = buttonInteractionHandlers.first { it.name == "jump" }.interactionSource,
//                                    indication = LocalIndication.current,
//                                    onClick = {})
//                        )
//                        CircularImageWithClick(
//                            imageResId = R.drawable.use_item,
//                            modifier = Modifier
//                                .align(Alignment.BottomEnd)
////                                .padding(
////                                    bottom = (screenHeight * 0.18f),
////                                    end = (screenWidth * 0.25f)
////                                )
//                                .size(40.dp)
//                                .clip(CircleShape)
//                                .border(
//                                    1.dp,
//                                    MaterialTheme.colors.secondary.copy(alpha = 0.3f),
//                                    shape = CircleShape
//                                )
//                                .clickable(
//                                    interactionSource = buttonInteractionHandlers.first { it.name == "useItem" }.interactionSource,
//                                    indication = LocalIndication.current,
//                                    onClick = {})
//                        )
//                        CircularImageWithClick(
//                            imageResId = R.drawable.first_aid,
//                            modifier = Modifier
//                                .align(Alignment.BottomEnd)
////                                .padding(
////                                    bottom = (screenHeight * 0.05f),
////                                    end = (screenWidth * 0.3f)
////                                )
//                                .size(40.dp)
//                                .clip(CircleShape)
//                                .border(
//                                    1.dp,
//                                    MaterialTheme.colors.secondary.copy(alpha = 0.3f),
//                                    shape = CircleShape
//                                )
//                                .clickable(
//                                    interactionSource = buttonInteractionHandlers.first { it.name == "heal" }.interactionSource,
//                                    indication = LocalIndication.current,
//                                    onClick = {})
//                        )
//                        CircularImageWithClick(
//                            imageResId = R.drawable.punch,
//                            modifier = Modifier
//                                .align(Alignment.TopEnd)
//                                //.padding(top = (screenHeight * 0.4f), end = (screenWidth * 0.05f))
//                                .size(40.dp)
//                                .clip(CircleShape)
//                                .border(
//                                    1.dp,
//                                    MaterialTheme.colors.secondary.copy(alpha = 0.3f),
//                                    shape = CircleShape
//                                )
//                                .clickable(
//                                    interactionSource = buttonInteractionHandlers.first { it.name == "melee" }.interactionSource,
//                                    indication = LocalIndication.current,
//                                    onClick = {})
//                        )
//                        CircularImageWithClick(
//                            imageResId = R.drawable.crouch, modifier = Modifier
//                                .align(Alignment.BottomEnd)
////                                .padding(
////                                    bottom = (screenHeight * 0.06f),
////                                    end = (screenWidth * 0.03f)
////                                )
//                                .size(40.dp)
//                                .clip(CircleShape)
//                                .border(
//                                    1.dp,
//                                    MaterialTheme.colors.secondary.copy(alpha = 0.3f),
//                                    shape = CircleShape
//                                )
//                                .clickable(
//                                    interactionSource = buttonInteractionHandlers.first { it.name == "crouch" }.interactionSource,
//                                    indication = LocalIndication.current,
//                                    onClick = {})
//                        )
//                        CircularImageWithClick(
//                            imageResId = R.drawable.target_aim, modifier = Modifier
//                                .align(Alignment.TopEnd)
//                                //.padding(top = (screenHeight * 0.52f), end = (screenWidth * 0.15f))
//                                .size(50.dp)
//                                .clip(CircleShape)
//                                .border(
//                                    1.dp,
//                                    MaterialTheme.colors.secondary.copy(alpha = 0.3f),
//                                    shape = CircleShape
//                                )
//                                .clickable(
//                                    interactionSource = buttonInteractionHandlers.first { it.name == "scope" }.interactionSource,
//                                    indication = LocalIndication.current,
//                                    onClick = {})
//                        )
//                        CircularImageWithClick(
//                            imageResId = R.drawable.run,
//                            modifier = Modifier
//                                .align(Alignment.BottomStart)
////                                .padding(
////                                    bottom = (screenHeight * 0.1f),
////                                    start = (screenWidth * 0.2f)
////                                )
//                                .size(40.dp)
//                                .clip(CircleShape)
//                                .border(
//                                    1.dp,
//                                    MaterialTheme.colors.secondary.copy(alpha = 0.3f),
//                                    shape = CircleShape
//                                )
//                                .clickable(
//                                    interactionSource = buttonInteractionHandlers.first { it.name == "sprint" }.interactionSource,
//                                    indication = LocalIndication.current,
//                                    onClick = {})
//                        )
//                        buttonInteractionHandlers.forEach { buttonInteractionHandler ->
//                            LaunchedEffect(buttonInteractionHandler.interactionSource) {
//                                buttonInteractionHandler.interactionSource.interactions
//                                    .filterIsInstance<PressInteraction>()
//                                    .collect {
//                                        when (it) {
//                                            is PressInteraction.Press -> {
//                                                //Log.d("webView", "${buttonInteractionHandler.name} pressed")
//                                                buttonInteractionHandler.button.pressed = true
//                                            }
//
//                                            is PressInteraction.Release, is PressInteraction.Cancel -> {
//                                                //Log.d("webView", "${buttonInteractionHandler.name} released")
//                                                buttonInteractionHandler.button.pressed = false
//                                            }
//                                        }
//                                    }
//                            }
//                        }
//                    }*/
//                }
//            }
//
//            IconButton(
//                onClick = {activity.showStreamSettings()},
//                colors = IconButtonColors(
//                    contentColor = MaterialTheme.colors.secondary,
//                    containerColor = MaterialTheme.colors.secondaryVariant.copy(alpha = 0f),
//                    disabledContainerColor = MaterialTheme.colors.secondaryVariant,
//                    disabledContentColor = MaterialTheme.colors.secondary
//                ),
//                enabled = true,
//                modifier = Modifier.align(Alignment.TopEnd)
//            ) {
//                Icon(
//                    imageVector = Icons.Filled.Settings,
//                    modifier = Modifier.size(25.dp),
//                    contentDescription = "Settings Button"
//                    )
//            }
//
//            if(dragEnabled) {
//                Row(modifier = Modifier.align(Alignment.TopCenter)) {
//                    Button(modifier = Modifier,
//                        onClick = {
//                            /*gamePadComponents.forEach { component ->
//                                Log.d("webView", "saving ${component.name}: ${component.position}")
//                            }*/
//                            sharedJoystickConfigHelper(activity).saveConfig(gamePadComponents)
//                            activity.toggleDragState()
//                            subStateUpdate.invoke(!stateUpdate.value)
//
//                        }
//                    ) {
//                        Text(text = "Save Changes",
//                            style = MaterialTheme.typography.button,
//                            textAlign = TextAlign.Center)
//                    }
//                    Spacer(modifier = Modifier.size(5.dp))
//                    Button(modifier = Modifier,
//                        onClick = {
//                            for (i in 0 until gamePadComponents.size) {
//                                if(viewModel.addedGamePadComponents[i].name != "rj") {
//                                    viewModel.updateAddedGamePadComponents(i, Offset((-100f), (-100f)))
//                                }
//                            }
//                            activity.toggleDragState()
//                            subStateUpdate.invoke(!stateUpdate.value)
//                            sharedJoystickConfigHelper(activity).clearConfig()
//                        }
//                    ) {
//                        Text(text = "Reset",
//                            style = MaterialTheme.typography.button,
//                            textAlign = TextAlign.Center)
//                    }
//                }
//            }
//
//            CustomDialog(
//                openDialogCustom = updateFpsControls,
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .fillMaxHeight(0.8f)
//                    .align(Alignment.Center)
//                    .background(MaterialTheme.colors.background.copy(alpha = 0.1f)),
//                onDismiss = {activity.toggleFpsConfigEditor()}
//            ) {
//
//                Card(
//                    modifier = Modifier
//                        .fillMaxWidth(0.95f)
//                        .fillMaxHeight(0.95f),
//                    shape = RoundedCornerShape(10.dp),
//                    elevation = 28.dp,
//                ) {
//                    var showAddDialog = remember {
//                        mutableStateOf(false)
//                    }
//
//                    Box(modifier = Modifier
//                        .fillMaxWidth()
//                        .fillMaxHeight()
//                        .background(MaterialTheme.colors.secondaryVariant.copy(alpha = 0.8f))
//                        .verticalScroll(rememberScrollState())
//                        .border(
//                            0.1.dp,
//                            MaterialTheme.colors.primary,
//                            RectangleShape
//                        )
//                    ) {
//                        Text(
//                            text = "Configure keys for gamepad buttons",
//                            textAlign = TextAlign.Center,
//                            color = MaterialTheme.colors.secondary,
//                            modifier = Modifier
//                                .padding(top = 5.dp, bottom = 10.dp)
//                                .align(Alignment.TopCenter)
//                        )
//                        VerticalGrid(modifier = Modifier.padding(top = 30.dp), columns = 2) {
//                            viewModel.addedGamePadComponents.forEachIndexed { index, component ->
//                                if(!component.isJoyStick) {
//                                    Column(
//                                        modifier = Modifier
//                                            .fillMaxWidth(0.3f)
//                                            .height(60.dp)
//                                            .border(
//                                                0.5.dp,
//                                                MaterialTheme.colors.primary,
//                                                RectangleShape
//                                            ),
//                                        verticalArrangement = Arrangement.SpaceAround,
//                                        horizontalAlignment = Alignment.CenterHorizontally
//                                    ) {
//                                        Row(
//                                            modifier = Modifier,
//                                            verticalAlignment = Alignment.CenterVertically,
//                                            horizontalArrangement = Arrangement.SpaceBetween
//                                        ) {
//                                            if (!component.isJoyStick) {
//                                                CircularImageWithClick(
//                                                    imageResId = component.iconRes,
//                                                    modifier = Modifier
//                                                        .size(35.dp)
//                                                        .padding(start = 5.dp)
//                                                        .clip(CircleShape)
//                                                )
//                                            }
//                                            Spacer(modifier = Modifier.width(if (component.isJoyStick) 50.dp else 20.dp))
//                                            Box(
//                                                modifier = Modifier.width(70.dp),
//                                                contentAlignment = Alignment.Center
//                                            ) {
//                                                Text(
//                                                    text = userFriendlyComponentNames[component.name] ?: "Unknown",
//                                                    textAlign = TextAlign.Center,
//                                                    color = MaterialTheme.colors.secondary
//                                                )
//                                            }
//                                            Spacer(modifier = Modifier.width(if (component.isJoyStick) 130.dp else 20.dp))
//                                            if (!component.isJoyStick) {
//                                                KeySelectorMenu(
//                                                    selectedKey = selectedKeysDropdown[component.name]!!,
//                                                    keyList = enumValues<joystickKeys>().toList().dropLast(1),
//                                                    modifier = Modifier.width(110.dp),
//                                                    onValueChanged = {
//                                                        selectedKeysDropdown[component.name] = it
//                                                        viewModel.updateAddedGamePadComponents(
//                                                            index,
//                                                            it
//                                                        )
//                                                        updateControlsMessageKey(
//                                                            component.name,
//                                                            it
//                                                        )
//                                                    }
//                                                )
//                                            }
//                                            Spacer(modifier = Modifier.width(10.dp))
//                                            Column(verticalArrangement = Arrangement.Center) {
//                                                Text(
//                                                    text = "Toggle",
//                                                    textAlign = TextAlign.Center,
//                                                    color = MaterialTheme.colors.secondary
//                                                )
//                                                Switch(
//                                                    checked = buttonToggleState[component.name]!!,
//                                                    onCheckedChange = {
//                                                        buttonToggleState[component.name] = it
//                                                        viewModel.updateAddedGamePadComponents(
//                                                            index,
//                                                            it
//                                                        )
//                                                    },
//                                                    colors = SwitchDefaults.colors().copy(
//                                                        checkedThumbColor = MaterialTheme.colors.secondary,
//                                                        checkedTrackColor = MaterialTheme.colors.primary,
//                                                        uncheckedTrackColor = MaterialTheme.colors.secondaryVariant
//                                                    )
//                                                )
//                                            }
//                                            /*Spacer(modifier = Modifier.width(0.dp))
//                                            IconButton(onClick = {
//                                                viewModel.removeGamePadComponent(
//                                                    component
//                                                )
//                                            }) {
//                                                Icon(
//                                                    imageVector = Icons.Filled.RemoveCircleOutline,
//                                                    contentDescription = "Remove gamepad component"
//                                                )
//                                            }*/
//                                        }
//                                        Spacer(modifier = Modifier.size(10.dp))
//                                    }
//                                }
//                            }
//                            /*IconButton(onClick={showAddDialog.value = true}) {
//                                Row(modifier = Modifier.width(70.dp), horizontalArrangement = Arrangement.SpaceEvenly){
//                                    Text(
//                                        text = "ADD",
//                                        textAlign = TextAlign.Center
//                                    )
//                                    Icon(imageVector = Icons.Filled.AddCircleOutline, contentDescription = "Add gamepad components")
//                                }
//                            }*/
//                            Button(
//                                modifier = Modifier
//                                    .padding(start = 5.dp, top = 5.dp),
//                                colors = ButtonDefaults.buttonColors().copy(containerColor = primaryGreen),
//                                onClick = {
//                                    sharedJoystickConfigHelper(activity).saveKeys(gamePadComponents)
//                                    activity.toggleFpsConfigEditor()
//                                }
//                            ) {
//                                Text(text = "Save Changes",
//                                    color = MaterialTheme.colors.secondary,
//                                    style = MaterialTheme.typography.button,
//                                    textAlign = TextAlign.Center)
//                            }
//                        }
//                        /*if(showAddDialog.value) {
//                            Box(modifier = Modifier.fillMaxSize(0.8f).background(MaterialTheme.colors.primary)) {
//                                VerticalGrid(modifier = Modifier.padding(top = 20.dp), columns = 3) {
//                                    gamePadConfig.forEach { component ->
//                                        Column(modifier = Modifier
//                                            .fillMaxWidth(0.3f)
//                                            .height(60.dp)
//                                            .border(0.5.dp, Color.Gray, RectangleShape)) {
//                                            Row(modifier = Modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
//                                                if (!component.isJoyStick) {
//                                                    CircularImageWithClick(
//                                                        imageResId = component.iconRes,
//                                                        modifier = Modifier
//                                                            .size(35.dp)
//                                                            .padding(start = 5.dp)
//                                                            .clip(CircleShape)
//                                                    )
//                                                }
//                                                Spacer(modifier = Modifier.width(if (component.isJoyStick) 50.dp else 20.dp))
//                                                Box(modifier = Modifier.width(70.dp), contentAlignment = Alignment.Center){
//                                                    Text(
//                                                        text = component.name.uppercase(),
//                                                        textAlign = TextAlign.Center
//                                                    )
//                                                }
//                                                Spacer(modifier = Modifier.width(20.dp))
//                                                IconButton(onClick = {
//                                                    val res = viewModel.addedGamePadComponents.find {
//                                                        it.name == component.name
//                                                    }
//                                                    if(res == null) {
//                                                        component.position = Offset(-100f, -100f)
//                                                        viewModel.addGamePadComponent(component)
//                                                        showAddDialog.value = false
//                                                    } else {
//                                                        activity.makeToast("Component already added.")
//                                                    }
//                                                }) {
//                                                    Icon(
//                                                        imageVector = Icons.Filled.AddCircleOutline,
//                                                        contentDescription = "Add gamepad component"
//                                                    )
//                                                }
//                                            }
//                                            Spacer(modifier = Modifier.size(10.dp))
//                                        }
//                                    }
//                                }
//                            }
//                        }*/
//                    }
//                }
//            }
//        }

        CustomDialog(
            openDialogCustom = backPressed,
            onDismiss = { }
        ) {
            Card(
                shape = RoundedCornerShape(10.dp),
                elevation = 28.dp,
            ) {
                Column(
                    modifier = Modifier
                        .background(MaterialTheme.colors.surface),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Do you want to exit the stream?",
                        color = MaterialTheme.colors.secondary,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 15.dp, horizontal = 10.dp)
                    )
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                            .background(MaterialTheme.colors.primary.copy(alpha = 0.5f)),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = {
//                            activity.setScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                            if(activity.pcStream) {
                                activity.pref!!.setPcExit(true)
                                activity.pref!!.setExitTime(
                                    LocalDateTime
                                        .now(ZoneOffset.UTC)
                                        .toEpochSecond(ZoneOffset.UTC)
                                )
                            }
                            activity.finish()
                        }) {
                            Spacer(modifier = Modifier.size(5.dp))
                            Text(
                                text = "Exit",
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colors.secondary,
                                style = subtitle,
                            )
                        }
                        TextButton(onClick = {
                            activity.subBackPressed?.invoke(false)
                        }) {
                            Spacer(modifier = Modifier.size(5.dp))
                            Text(
                                text = "Cancel",
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colors.secondary,
                                style = subtitle,
                            )
                        }
                    }
                }
            }
        }

//        var KeyText by remember {
//            mutableStateOf("")
//        }
//        Column(modifier = Modifier.padding(16.dp)) {
//            Spacer(modifier = Modifier.height(16.dp))
//            androidx.compose.material3.Text(
//                "X Axis: ${String.format("%.1f", xAxis)}",
//                style = androidx.compose.material3.MaterialTheme.typography.headlineLarge)
//            androidx.compose.material3.Text(
//                "Y Axis: ${String.format("%.1f", yAxis)}",
//                style = androidx.compose.material3.MaterialTheme.typography.headlineLarge)
//            androidx.compose.material3.Text(
//                "Z Axis: ${String.format("%.1f", zAxis)}",
//                style = androidx.compose.material3.MaterialTheme.typography.headlineLarge)
//            androidx.compose.material3.Text(
//                "key :  ${value}",
//                style = androidx.compose.material3.MaterialTheme.typography.headlineLarge)
//            Spacer(modifier = Modifier.height(16.dp))
//            GaugeBearing(
//                azimuth  = 360.0,
//                modifier = Modifier
//                    .size(300.dp))
//            Button(onClick = {
//                x =  xAxis
//                y =  yAxis
//                z =  zAxis
//                Log.d("testtt ",""+ x+y+z)
//                btnClick =  true
//                keyValue = ""
//            },
//                modifier = Modifier.align(Alignment.CenterHorizontally)) {
//                androidx.compose.material3.Text("set value ")
//            }
//        }
    }
}

@Composable
fun WebViewScreen(url: String, webView: WebView? = null) {
    AndroidView(factory = {
        webView
            ?: WebView(it).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT)
                webViewClient = WebViewClient()
                settings.javaScriptEnabled = true
                loadUrl(url)
            }
    }, update = {
        it.loadUrl(url)
    })
}


//private fun initSensor() {
//    curactivity?.lifecycleScope?.launch {
//    }
//}
//
//fun updateValues(values: FloatArray) {
//    fusedOrientation = if (meanFilterEnabled) {
//        meanFilter.filter(values)
//    }
//    else {
//        values.also { fusedOrientation = it }
//    }
//    if (logData) {
//        dataLogger?.setRotation(fusedOrientation)
//    }
//}
//
//@Composable
//fun WASDControl(rotationX: Float, rotationY: Float, rotationZ: Float) {
//        Log.d("Gyroscope", "XAxis  $rotationX ")
//        Log.d("Gyroscope", "YAxis  $rotationY ")
//
////        XRotation...
//        if(rotationX < (x+0.8) && rotationX > (x-0.8)){
//            keyValue = ""
//            rotationXValue = 0F
//        }
//        else if (rotationX > (x-0.8)) {
//            keyValue = "A"
//            if (rotationX < x+rotationXMarginRange) {
//                rotationXValue = (rotationX- x)/rotationXMarginRange
//                Log.d("Gyroscope", " value Y1 $rotationX")
//            }
//            else{
//                rotationXValue = 1F
//            }
//        }
//        else if (rotationX< (x +0.8)) {
//            keyValue = "D"
//            if (rotationX > x-rotationXMarginRange) {
//                rotationXValue = (x-rotationX)/rotationXMarginRange
//                Log.d("Gyroscope", " value x1 $rotationX")
//            }
//            else{
//                rotationXValue = 1F
//            }
//        }
//
////        YRotation...
//        if (rotationY < (y+0.8) && rotationY > (y-0.8)) {
//            keyValue+= ""
//            rotationYValue = 0F
//        }
//        else if (rotationY > (y-0.8)) {
//            keyValue += " S"
//            if (rotationY < y+rotationYMarginRange) {
//                Log.d("Gyroscope", "ys  $y ")
//                rotationYValue = (rotationY- y)/rotationYMarginRange
//                Log.d("Gyroscope", " value Y1 $rotationY")
//            }
//            else{
//                rotationYValue = 1F
//            }
//        }
//        else if (rotationY < (y+0.8)) {
//            keyValue += " W"
//            if (rotationY > y-rotationYMarginRange) {
//                Log.d("Gyroscope", "yw  $y ")
//                rotationYValue = (y-rotationY)/rotationYMarginRange
//                Log.d("Gyroscope", " value Y2 $rotationY")
//            }
//            else{
//                rotationYValue = 1F
//            }
//        }
//
//        Log.d("Gyroscope", " rotationXValue $rotationXValue rotationYValue $rotationYValue ")
//
//}
//private fun startDataLog() {
//    logData = true
//    dataLogger?.startDataLog()
//}
//
//private fun stopDataLog() {
//    logData = false
//    val path = dataLogger?.stopDataLog()
//    Toast.makeText(curactivity, "File Written to: $path", Toast.LENGTH_SHORT).show()
//}
//
//
//
//private fun showHelpDialog() {
//    // Show help dialog
//}
//private fun toggleLogging() {
//    if (logData) {
//        stopDataLog()
//    } else {
//
//    }
//}
//
//private fun readPrefs(): Mode {
//    meanFilterEnabled = false
//    val complimentaryFilterEnabled: Boolean = false
//    val kalmanFilterEnabled: Boolean = false
//    if (meanFilterEnabled) {
//        meanFilter.setTimeConstant(0.5f)
//    }
//    val mode: Mode
//    mode = if (!complimentaryFilterEnabled && !kalmanFilterEnabled) {
//        Mode.GYROSCOPE_ONLY
//    }
//    else if (complimentaryFilterEnabled) {
//        Mode.COMPLIMENTARY_FILTER
//    } else {
//        Mode.KALMAN_FILTER
//    }
//    return mode
//}

private enum class Mode {
    GYROSCOPE_ONLY,
    COMPLIMENTARY_FILTER,
    KALMAN_FILTER
}



