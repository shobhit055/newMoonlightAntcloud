package com.limelight.components

import android.os.Build.VERSION.SDK_INT
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest

import com.limelight.theme.mainTitle
import com.limelight.R
import com.limelight.Theme


@Preview(device = Devices.PIXEL_3)
@Composable
fun LoadingPreview() {
    Theme {
        LoadingScreen(0.1f, "Hello","")
    }
}

@Composable
fun LoadingScreen(animatedProgress: Float, text: String,type:String) {
    Surface(
        color = Color.Black,
        modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(LocalContext.current).data(data = R.drawable.spinner).build(),
                    ImageLoader.Builder(LocalContext.current).components {
                        if(SDK_INT >= 28) {
                            add(ImageDecoderDecoder.Factory())
                        }
                        else {
                            add(GifDecoder.Factory())
                        }
                    }.build()),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                contentDescription = stringResource(R.string.logo_description))
            /*LinearProgressIndicator(
                progress = animatedProgress,
                color = MaterialTheme.colors.primary,
                modifier = Modifier
                    .fillMaxWidth(.8f)
                    .padding(top = 10.dp)

            )*/
            if(type != "login") {
                Text(
                    text = text,
                    style = mainTitle.copy(
                        color = MaterialTheme.colors.secondary,
                        fontSize = 20.sp
                    ),
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(top = 20.dp)
                )
            }
        }
    }
}

/*
@Composable
fun SpinnerAnimation(
    modifier: Modifier = Modifier,
    spinnerColor: Color = Color.Black,
    spinnerBackgroundColor: Color = Color.Transparent,
    spinnerSize: Int = 100 // Size in dp
) {
    var rotation by remember { mutableStateOf(0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "spinner")
    val rotationAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    LaunchedEffect(Unit) {
        rotation = with(LocalDensity.current) { spinnerSize.toPx() / 2 }
    }

    Canvas(
        modifier = modifier.fillMaxSize()
    ) {
        val radius = size.minDimension / 2

        // Draw background circle
        drawCircle(
            color = spinnerBackgroundColor,
            radius = radius
        )

        // Draw spinner
        rotate(rotation + rotationAnim) {
            drawCircle(
                color = spinnerColor,
                radius = radius / 2
            )
        }
    }
}*/