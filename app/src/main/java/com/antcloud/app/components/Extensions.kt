package com.antcloud.app.components

import android.app.Activity
import android.content.pm.ActivityInfo
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.antcloud.app.common.DrawerScreens


internal fun Activity.makeToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

internal fun Long.toTime(): String {
    if (this >= 3600) return "+59:59"
    var minutes = ((this % 3600) / 60).toString()
    if (minutes.length == 1) minutes = "0$minutes"
    var seconds = (this % 60).toString()
    if (seconds.length == 1) seconds = "0$seconds"
    return String.format("$minutes:$seconds")
}

internal fun String.isEmailValid(): Boolean {
    return if (TextUtils.isEmpty(this)) {
        false
    } else {
        android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
    }
}

internal fun String.isPhoneNumberValid(): Boolean {
    return if (TextUtils.isEmpty(this)) {
        false
    } else {
        this.length == 10
    }
}

internal fun String.isPinCodeValid(): Boolean {
    return if (TextUtils.isEmpty(this)) {
        false
    } else {
        this.length == 6
    }
}

fun AppCompatActivity.setScreenOrientation(orientation: Int) {
    this.requestedOrientation = orientation
    if (orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
        hideSystemUi()
    } else {
        showSystemUi()
    }
}

fun AppCompatActivity.hideSystemUi() {
    val window = this.window ?: return
    WindowCompat.setDecorFitsSystemWindows(window, false)
    WindowInsetsControllerCompat(window, window.decorView).let { controller ->
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}

fun AppCompatActivity.showSystemUi() {
    val window = this.window ?: return
    WindowCompat.setDecorFitsSystemWindows(window, true)
    WindowInsetsControllerCompat(
        window, window.decorView
    ).show(WindowInsetsCompat.Type.systemBars())
}

//fun HashMap<String, Region>.getList(): List<String> {
//    return this.map {
//        it.value.code
//    }
//}

/*fun ArrayList<String>.getGameList(): ArrayList<Game> {
    val gameList = arrayListOf<Game>()
    this.forEach { id ->
        var game: Game? = null
        GlobalData.getInstance().ourGames.forEach { if (it.gameId == id) game = it }
        if (game == null) {
            GlobalData.getInstance().yourGames.forEach { if (it.gameId == id) game = it }
        }
        if (game != null) gameList.add(game!!)
    }
    return gameList
}*/

//fun List<String>.getGameList(): ArrayList<Game> {
//    val gameList = arrayListOf<Game>()
//    this.forEach { id ->
//        var game: Game? = null
//        GlobalData.getInstance().ourGames[0].games.forEach { if (it.gameId== id) game = it }
////        if (game == null) {
////            GlobalData.getInstance().yourGames.forEach { if (it.gameId == id) game = it }
////        }
//        if (game != null) gameList.add(game!!)
//    }
//    return gameList
//}
//
//fun ArrayList<Game>.toIdList(): ArrayList<String> {
//    val idList = arrayListOf<String>()
//    this.forEach {
//        idList.add(it.gameId)
//    }
//    return idList
//}

fun LazyListState.visibleItems(itemVisiblePercentThreshold: Float) =
    layoutInfo.visibleItemsInfo
        .filter {
            visibilityPercent(it) >= itemVisiblePercentThreshold
        }

fun LazyListState.visibilityPercent(info: LazyListItemInfo): Float {
    val cutTop = maxOf(0, layoutInfo.viewportStartOffset - info.offset)
    val cutBottom = maxOf(0, info.offset + info.size - layoutInfo.viewportEndOffset)
    return maxOf(0f, 100f - (cutTop + cutBottom) * 100f / info.size)
}

fun navigationRoutes(route: String): String {
    return when(route) {
        "price" -> DrawerScreens.Pricing.route
        "support" -> DrawerScreens.Support.route
        "faq" -> DrawerScreens.FAQs.route
        "tutorial" -> DrawerScreens.Tutorials.route
        "privacy" -> DrawerScreens.Policy.route
        "report" -> DrawerScreens.Report.route
        "terms" -> DrawerScreens.Terms.route
        //"loginHistory" -> DrawerScreens.LoginHistory.route
        "account" -> DrawerScreens.Account.route
        //"pingTest" -> DrawerScreens.PingTest.route
        "libraryDetails" -> DrawerScreens.LibraryDetails.route
        "gameDetails" -> DrawerScreens.GameDetails.route
        else -> DrawerScreens.Library.route
    }
}