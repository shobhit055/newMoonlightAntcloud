package com.limelight.screen.game

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.ActivityInfo
import android.util.DisplayMetrics
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonColors
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Store
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.limelight.R
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.limelight.activity.NavActivity
import com.limelight.common.DrawerScreens
import com.limelight.common.GlobalData
import com.limelight.components.CustomDialog
import com.limelight.components.Loading
import com.limelight.data.Game
import com.limelight.data.ListModel
import com.limelight.screen.account.AsyncImages
import com.limelight.theme.PinkGradient
import com.limelight.theme.heading
import com.limelight.theme.subtitle
import com.limelight.viewmodel.GameViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class Images(val image: Int) {
    FPS(R.drawable.crosshair),
    ADV(R.drawable.map),
    HRR(R.drawable.ghost),
    RTS(R.drawable.brain),
    SPRT(R.drawable.sport),
    RPG(R.drawable.roleplay),
    UHD(R.drawable.uhd),
    RTX(R.drawable.rtx),
    CONTROLLER(R.drawable.controller),
    CHILDREN(R.drawable.children)
}

enum class DialogState {
    LOADING, INFO, STORE, COMPLETE, PAIDGAME,MAINTENANCE //, REGIONS
}

fun gameDetailNav(navGraph: NavGraphBuilder, activity: NavActivity, updateToolbar: ((String) -> Unit), navigate: ((String) -> Unit)) {
    return navGraph.composable(
        DrawerScreens.GameDetails
        .route) {
        val viewModel : GameViewModel =  hiltViewModel()
        val gameId = GlobalData.getInstance().gameId
        viewModel.setGameId(gameId)
        viewModel.initializeGame()

        GameDetailsScreenLoading(
            viewModel = viewModel,
            activity = activity)
    }
}

@Composable
fun GameDetailsScreenLoading(
    viewModel: GameViewModel,
    activity: NavActivity) {
    if (viewModel.game != null) {
        GameDetailsScreen( viewModel, activity, viewModel.game!!)
    }
    else {
        Loading()
    }
}

fun Context.findActivity(): Activity? {
    var context = this
    while(context is ContextWrapper) {
        if(context is Activity) {
            return context
        }
        context = context.baseContext
    }
    return null
}

@Composable
fun GameDetailsScreen(
    viewModel: GameViewModel,
    activity: NavActivity,
    game: Game
) {

    var isOpen by remember {
        mutableStateOf(viewModel.trailerWindow)
    }

    val mediaItem: MediaItem =
        MediaItem.fromUri("https://antplay-gamedata.s3.ap-south-1.amazonaws.com/${game.gameId}/videoBg.mp4")

    val exoPlayer by remember {
        mutableStateOf(
            ExoPlayer.Builder(activity)
                .build()
                .also { exoPlayer ->
                    exoPlayer.setMediaItem(mediaItem)
                    viewModel.exoplayer = exoPlayer
                }
        )
    }

    val context = LocalContext.current

    DisposableEffect(Unit) {
        val window = context.findActivity()?.window ?: return@DisposableEffect onDispose {}
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)

        insetsController.apply {
            hide(WindowInsetsCompat.Type.statusBars())
            hide(WindowInsetsCompat.Type.navigationBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        onDispose {
            insetsController.apply {
                show(WindowInsetsCompat.Type.statusBars())
                show(WindowInsetsCompat.Type.navigationBars())
                systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
    }


    viewModel.subTrailerWindowState = {
        isOpen = it
        with(activity) {
            if (isOpen) {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
             //   setScreenOrientation(orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
                exoPlayer.prepare()
                exoPlayer.play()
            } else {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                exoPlayer.pause()
            }
        }
    }

    val openDialogCustom = remember {
        mutableStateOf(/*!game.isOurGame*/false)
    }

    var dialogState by remember {
        mutableStateOf(/*if (game.isOurGame) DialogState.INFO else*/ DialogState.INFO)
    }

    val coroutineScope = rememberCoroutineScope()
    fun delayClose() {
        coroutineScope.launch {
            delay(2000)
            openDialogCustom.value = false
            dialogState = DialogState.INFO
        }
    }

    /*var favoriteList by remember {
        mutableStateOf(viewModel.favoriteList)
    }*/

    val displayMetrics: DisplayMetrics = activity.resources.displayMetrics
    val screenWidth = (displayMetrics.widthPixels / displayMetrics.density.toInt())
    val landscape = screenWidth >= 600

    val bannerHeight = if(landscape) (LocalConfiguration.current.screenHeightDp / 2).dp else (LocalConfiguration.current.screenWidthDp / 1.777).dp
    val globalInstance = GlobalData.getInstance()

    /*viewModel.subFavoriteListState = {
        favoriteList = it
    }*/

    Box {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .verticalScroll(state = rememberScrollState())
                .background(Color.Black),
            horizontalAlignment = Alignment.CenterHorizontally) {
            CustomDialog(
                openDialogCustom = openDialogCustom,
                label = "GameDetails",
                onDismiss = { }) {
                when (dialogState) {
                    DialogState.PAIDGAME -> PaidDialog(onClick = {
                            openDialogCustom.value = false
                            dialogState = DialogState.INFO
                        })

                    DialogState.LOADING -> Column(modifier = Modifier.background(MaterialTheme.colors.surface)
                            .clip(RoundedCornerShape(10.dp))) {
                        Loading()
                    }

                    DialogState.INFO -> InfoDialog(
                        textToShow = "Please make sure you own the game you want to play via the appropriate store. For more details, please check out the FAQs.",

                        onClick = {
                            openDialogCustom.value = false
                            /*onEventHandler(GameDetailsEvent.OnPlayClick)
                            delayClose()
                            dialogState = DialogState.COMPLETE*/
//                           dialogState = DialogState.REGIONS
                        })

                    DialogState.STORE -> StoreDialog(viewModel,
                        onClick = {
                            onPlayClick(activity, viewModel)
                            delayClose()
                            dialogState = DialogState.COMPLETE
                        },
//                        onEventHandler = onEventHandler,
                        stores = game.services.map { it.name }
                    )

                    DialogState.COMPLETE -> Column(modifier = Modifier.background(MaterialTheme.colors.surface)
                            .clip(RoundedCornerShape(10.dp))) {
                        Loading("Starting your game ...")
                    }
                    DialogState.MAINTENANCE -> InfoDialog(
                        textToShow = globalInstance.remoteGamesMaintenance[0].maintenanceText,
                        onClick = {
                            openDialogCustom.value = false
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.size(20.dp))

            Row(modifier = Modifier.clip(RoundedCornerShape(25.dp)).fillMaxWidth(.9f)
                .height(bannerHeight)) {
                // GlobalData.getInstance().imageLoading = true
//                    globalInstance.traceImageLoading =  FirebasePerformance.getInstance().newTrace("image_loading")
//                    globalInstance.traceImageLoading.start()
                AsyncImages(
                    url = if (game.isOurGame) "https://antplay-gamedata.s3.ap-south-1.amazonaws.com/${game.gameId}.jpg"
                    else "https://antplay-gamedata.s3.ap-south-1.amazonaws.com/background.jpg",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.size(10.dp))

            customListView(activity)

            Spacer(modifier = Modifier.size(20.dp))

            Column(modifier = Modifier.fillMaxWidth(.9f)) {
                Text(
                    text = game.name,
                    style = heading.copy(fontSize = 30.sp, textAlign = TextAlign.Start),
                    modifier = Modifier.padding(bottom = 5.dp).fillMaxWidth(.7f))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()) {
                    repeat(game.genre.size) {
                        Text(
                            text = game.genre[it].genre.replaceFirstChar { _ -> game.genre[it].genre[0].uppercase() },
                            color = MaterialTheme.colors.secondary.copy(alpha = .6f),
                            style = subtitle)
                        if (game.genre.size - 1 != it) Icon(
                            imageVector = Icons.Filled.Circle,
                            contentDescription = "",
                            tint = MaterialTheme.colors.secondary.copy(alpha = .6f),
                            modifier = Modifier.size(15.dp).padding(vertical = 4.dp))
                    }
                }

                Spacer(modifier = Modifier.size(15.dp))

                Text(
                    text = game.description,
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.secondary.copy(alpha = 0.7f),
                    lineHeight = 23.sp,
//                  textAlign = TextAlign.Justify,
                    modifier = Modifier.fillMaxWidth(1f),
                    softWrap = true)

                Spacer(modifier = Modifier.size(20.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()) {
                    if (game.isOurGame) {
                        Button(
                            onClick = {
                            },
                            modifier = Modifier.weight(.4f).padding(end= 5.dp ,top = 10.dp),
                            shape = RoundedCornerShape(25.dp),
                            contentPadding = PaddingValues(vertical = 12.dp),
                            colors = ButtonDefaults.buttonColors(Color.White)) {
                            Text(text = "Learn More", style = subtitle.copy(color = Color.Black))
                        }
                    }
                    Button(
                        onClick = {
                            if (/*!game.isOurGame && */GlobalData.getInstance().accountData.currentPlan == "Basic") {
                                dialogState = DialogState.PAIDGAME
                                openDialogCustom.value = true
                            } else {
                                //AnalyticsManager.gameStreamButton()
                                globalInstance.gameStream = true
//                            globalInstance.traceGameStream =
//                                FirebasePerformance.getInstance().newTrace("game_stream")
//                            globalInstance.traceGameStream.start()

                                viewModel.selectedStore = game.gameId
                                onPlayClick(activity, viewModel)
//                            onEventHandler(GameDetailsEvent.OnPlayClick)
                                delayClose()
                                dialogState = DialogState.COMPLETE
                                openDialogCustom.value = true
                            }
                        },
                        modifier = Modifier.weight(.4f).padding(start= 5.dp ,top = 10.dp),
                        shape = RoundedCornerShape(25.dp),
                        contentPadding = PaddingValues(vertical = 12.dp),
                        colors = ButtonDefaults.buttonColors(PinkGradient)) {
                        Text(
                            text = "Launch",
                            style = subtitle.copy(color = Color.White))
                    }
                }

                /*    Spacer(modifier = Modifier.size(20.dp))
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    *//*game.services!!.forEach {
                        when (it.name) {
                            "Epic" -> PropertyIcon(image = Images.EPIC, subtitle = "Epic")
                            "Steam" -> PropertyIcon(image = Images.STEAM, subtitle = "Steam")
                            "Origin" -> PropertyIcon(image = Images.ORIGIN, subtitle = "Origin")
                            "Epic | Ubisoft" -> PropertyIcon(
                                image = Images.UBISOFT,
                                subtitle = "Ubisoft"
                            )

                            "EA" -> PropertyIcon(image = Images.EA, subtitle = "EA")
                        }
                    }*//*
                    game.properties?.forEach {
                        when (it.property) {
                            "4k" -> PropertyIcon(image = Images.UHD, subtitle = "4K UHD")
                            "rt4k" -> PropertyIcon(image = Images.RTX, subtitle = "Raytracing")
                            "childFriendly" -> PropertyIcon(
                                image = Images.CHILDREN,
                                subtitle = "Universal"
                            )

                            "controller" -> PropertyIcon(
                                image = Images.CONTROLLER,
                                subtitle = "Controller"
                            )
                        }
                    }
                }*/
            }
        }
        }
        if (isOpen) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth().fillMaxHeight()
                    .zIndex(if (isOpen) 1f else 0f)
                    .background(MaterialTheme.colors.background)) {
                VideoView(exoplayer = exoPlayer, activity = activity) {
                    viewModel.updateTrailerWindowState(isOpen)

//                    onEventHandler(GameDetailsEvent.OnWatchTrailerClicked(isOpen = false))
                }
            }
        }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun customListView(activity: NavActivity) {
    val courseList: ArrayList<ListModel> = ArrayList<ListModel>()
    courseList.add(ListModel("", R.drawable.controller))
    courseList.add(ListModel("", R.drawable.tshirt))
    courseList.add(ListModel("", R.drawable.windows))
    courseList.add(ListModel("", R.drawable.children))
    courseList.add(ListModel("", R.drawable.discord))
    courseList.add(ListModel("", R.drawable.instagram_icon))
    courseList.add(ListModel("", R.drawable.reload))

    LazyRow(modifier = Modifier.fillMaxWidth(.9f)) {
        itemsIndexed(courseList) { index, item ->
            Card(
                onClick = {},
                modifier = Modifier.padding(end = 8.dp).clip(RoundedCornerShape(12.dp)).width(120.dp), elevation = 6.dp) {
                Column(
                    modifier = Modifier.padding(8.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally) {

                    Spacer(modifier = Modifier.height(5.dp))

                    Image(
                        painter = painterResource(id = courseList[index].gameImage),
                        contentDescription = "img",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.height(60.dp).width(60.dp).padding(5.dp),
                        alignment = Alignment.Center)
                }
            }
        }
    }
}

fun onPlayClick(activity: NavActivity, viewModel: GameViewModel) {
//    val intent = Intent(activity, WebViewActivity::class.java)
//    intent.putExtra("url", "https://antcloud.co/stream?type=mobile&test=true&game=${viewModel.selectedStore}&resolution=${GlobalData.getInstance().accountData.resolution}&idToken=${GlobalData.getInstance().accountData.token}")
//    intent.putExtra("page" , "stream")
//    activity.startActivity(intent)
}

@Composable
fun PropertyIcon(image: Images, subtitle: String) {
    Column(
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 20.dp)) {
        
        Image(
            painter = painterResource(id = image.image),
            contentDescription = "",
            modifier = Modifier
                .height(54.dp)
                .alpha(.6f)
        )
        Spacer(modifier = Modifier.size(10.dp))
        Text(
            text = subtitle,
            color = MaterialTheme.colors.secondary.copy(alpha = .6f),
            textAlign = TextAlign.Center,
            fontSize = 14.sp
        )
    }
}

@Composable
fun PlayButton(text: String, icon: ImageVector, onClick: (() -> Unit), colors: ButtonColors, modifier: Modifier, shape: RoundedCornerShape) {
    PlayButton(
        text = text,
        icon = icon,
        onClick = onClick,
        colors = colors,
        disabled = false,
        modifier = modifier,
        shape = shape,
    )
}

@Composable
fun PlayButton(text: String, icon: ImageVector, onClick: (() -> Unit)) {
    PlayButton(
        text = text,
        icon = icon,
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondary),
        disabled = false,
        modifier = Modifier.fillMaxWidth(.9f),
        shape = RoundedCornerShape(10.dp),
    )
}

@Composable
fun PlayButton(modifier: Modifier, text: String, icon: ImageVector, onClick: (() -> Unit)) {
    PlayButton(
        text = text,
        icon = icon,
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondary),
        disabled = false,
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
    )
}

@Composable
fun PlayButton(text: String, icon: ImageVector, onClick: (() -> Unit), colors: ButtonColors, disabled: Boolean) {
    PlayButton(
        text = text,
        icon = icon,
        onClick = onClick,
        colors = colors,
        disabled = false,
        modifier = Modifier.fillMaxWidth(.9f),
        shape = RoundedCornerShape(10.dp),
    )
}

@Composable
fun PlayButton(text: String, icon: ImageVector, onClick: (() -> Unit), disabled: Boolean) {
    PlayButton(
        text = text,
        icon = icon,
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondary),
        disabled = disabled,
        modifier = Modifier.fillMaxWidth(.9f),
        shape = RoundedCornerShape(10.dp)
    )
}

@Composable
fun PlayButton(
    text: String,
    icon: ImageVector,
    onClick: (() -> Unit),
    colors: ButtonColors,
    disabled: Boolean,
    modifier: Modifier,
    shape: RoundedCornerShape
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = colors,
        shape = shape,
        enabled = !disabled
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                modifier = Modifier.size(25.dp),
                contentDescription = "",
                tint = MaterialTheme.colors.surface
            )
            Spacer(modifier = Modifier.size(10.dp))
            Text(
                text = text, color = MaterialTheme.colors.surface, fontSize = 14.sp, style = MaterialTheme.typography.button
            )
        }
    }
}

@Composable
fun VideoView(
    exoplayer: ExoPlayer?,
    activity: NavActivity,
    closePlayer: (() -> Unit)
) {
    DisposableEffect(
        AndroidView(factory = {
            StyledPlayerView(activity).apply {
                player = exoplayer
                setFullscreenButtonClickListener {
                    if (it) {
                        closePlayer()
                    }
                }
            }
        })
    ) {
        onDispose {
            activity.requestedOrientation  = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
//            activity.setScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        }
    }
}

/*@Composable
fun RegionsDialog(
    onClick: () -> Unit,
    onEventHandler: (GameDetailsEvent) -> Unit,
    regions: String,
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        elevation = 28.dp,
    ) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colors.surface)
        ) {
            RegionsGrid(regions = regions, onEventHandler = onEventHandler)
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .clickable { onClick() }
                    .background(MaterialTheme.colors.primary.copy(alpha = 0.5f)),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onClick) {
                    Text(
                        text = "Next",
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colors.secondary,
                        style = subtitle,
                    )
                    Spacer(modifier = Modifier.size(5.dp))
                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        contentDescription = "",
                        tint = MaterialTheme.colors.secondary,
                    )
                }
            }
        }

    }
}*/

@Composable
fun StoreButton(name: String, onClick: () -> Unit, selected: Boolean) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            if (selected) MaterialTheme.colors.secondary.copy(alpha = 0.1f)
            else MaterialTheme.colors.surface
        ),
        border = BorderStroke(
            if (selected) 1.dp
            else 0.dp,
            if (!selected) MaterialTheme.colors.secondary.copy(alpha = 0.1f)
            else MaterialTheme.colors.secondary
        ),
        modifier = Modifier.size(96.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxHeight()
        ) {
            Image(
                painter = painterResource(
                id = R.drawable.ant_cloud_white_icon
                /*id = when (name) {
                        "Epic" -> R.drawable.epic_games
                        "Steam" -> R.drawable.steam
                        "Origin" -> R.drawable.origin
                        "Epic | Ubisoft" -> R.drawable.ubisoft
                        "EA" -> R.drawable.ea_games
                        else -> R.drawable.icon
                    }*/
                ),
                contentDescription = "",
                modifier = Modifier
                    .height(54.dp)
                    .alpha(.6f)
            )
            Spacer(modifier = Modifier.size(10.dp))
            Text(
                text = name,
                color = MaterialTheme.colors.secondary.copy(alpha = .6f),
                textAlign = TextAlign.Center,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun StoreDialog(
    viewModel: GameViewModel,
    onClick: () -> Unit,
    stores: List<String>,
) {
    var selectedStore by remember {
        mutableStateOf(stores[0])
    }

    Card(
        shape = RoundedCornerShape(10.dp),
        elevation = 28.dp,
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Store,
                contentDescription = null,
                tint = MaterialTheme.colors.secondary,
            )
            Spacer(modifier = Modifier.size(10.dp))
            Text(
                text = "Choose Your Game Store",
                color = MaterialTheme.colors.secondary,
                style = subtitle,
                modifier = Modifier.padding(top = 5.dp, bottom = 5.dp),
            )
        }
        Column(
            modifier = Modifier
                .background(MaterialTheme.colors.surface)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 15.dp, horizontal = 20.dp)
            ) {
                items(stores) {
                    StoreButton(
                        name = it,
                        onClick = {
                            selectedStore = it
                            viewModel.selectedStore = it
//                            onEventHandler(GameDetailsEvent.OnStoreChanged(it))
                        },
                        selected = selectedStore == it
                    )
                }
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .background(MaterialTheme.colors.primary.copy(alpha = 0.5f))
                    .clickable { onClick() },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onClick) {
                    Text(
                        text = "Play Now ",
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colors.secondary,
                        style = subtitle,
                    )
                    Spacer(modifier = Modifier.size(5.dp))
                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        contentDescription = "",
                        tint = MaterialTheme.colors.secondary,
                    )
                }
            }
        }
    }
}

@Composable
fun InfoDialog(
    textToShow: String,
    onClick: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        elevation = 28.dp,
    ) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colors.surface)
        ) {
            Text(
//                text = "Please make sure you own the game you want to play via the appropriate store. For more details, please check out the FAQs.",
                text = textToShow,
                color = MaterialTheme.colors.secondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 15.dp, horizontal = 10.dp)
            )
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .clickable { onClick()  }
                    .background(MaterialTheme.colors.primary.copy(alpha = 0.5f)),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = {
                    onClick()
                }) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "",
                        tint = MaterialTheme.colors.secondary,
                    )
                    Spacer(modifier = Modifier.size(5.dp))
                    Text(
                        text = "OK ",
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colors.secondary,
                        style = subtitle,
                    )
                }
            }
        }

    }
}

@Composable
fun PaidDialog(
    onClick: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        elevation = 28.dp,
    ) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colors.surface)
        ) {
            Text(
                text = "Please upgrade your plan",
                color = MaterialTheme.colors.secondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 15.dp).align(Alignment.CenterHorizontally))
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .clickable { onClick() }
                    .background(MaterialTheme.colors.primary.copy(alpha = 0.5f)),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = {
                    onClick()
                }) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "",
                        tint = MaterialTheme.colors.secondary,
                    )
                    Spacer(modifier = Modifier.size(5.dp))
                    Text(
                        text = "OK ",
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colors.secondary,
                        style = subtitle,
                    )
                }
            }
        }

    }
}

/*@Composable
fun QualityGrid(
    quality: List<String>,
    onEventHandler: (GameDetailsEvent) -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Hd,
            contentDescription = null,
            tint = MaterialTheme.colors.secondary,
        )
        Spacer(modifier = Modifier.size(10.dp))
        Text(
            text = "Choose Your Resolution",
            color = MaterialTheme.colors.secondary,
            style = subtitle,
            modifier = Modifier.padding(top = 5.dp, bottom = 5.dp),
        )
    }

    var selectedQuality by remember {
        mutableStateOf(GlobalData.getInstance().accountData2.resolution)
    }
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 15.dp, horizontal = 20.dp)
    ) {
        items(quality) {
            CheckBox(title = it, selected = it == selectedQuality) {
                selectedQuality = it
                onEventHandler(GameDetailsEvent.OnQualityChanged(selectedQuality))
            }
        }
    }
}

@Composable
fun RegionsGrid(
    regions: String,
    onEventHandler: (GameDetailsEvent) -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Map,
            contentDescription = null,
            tint = MaterialTheme.colors.secondary,
        )
        Spacer(modifier = Modifier.size(10.dp))
        Text(
            text = "Choose Server Location",
            color = MaterialTheme.colors.secondary,
            style = subtitle,
            modifier = Modifier.padding(top = 5.dp, bottom = 5.dp),
        )
    }

    var selectedLocation by remember {
        mutableStateOf(GlobalData.getInstance().accountData2.region)
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 15.dp, horizontal = 20.dp)
    ) {
        items(1) {
            CheckBox(title = regions, selected = regions == selectedLocation) {
                selectedLocation = regions
                onEventHandler(GameDetailsEvent.OnLocationChanged(selectedLocation))
            }
        }
    }
}*/
