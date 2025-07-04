package com.limelight.viewmodel

import android.os.CountDownTimer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel

import com.limelight.common.GlobalData

class WebViewModel : ViewModel() {

    val countdownTimer = object: CountDownTimer(90000, 10000) {
        override fun onTick(millisUntilFinished: Long) {
            GlobalData.getInstance().paymentPcTimerMins = (millisUntilFinished.toInt() / 1000) / 60
            GlobalData.getInstance().paymentPcTimerSecs = (millisUntilFinished.toInt() / 1000) % 60
        }
        override fun onFinish() {
            GlobalData.getInstance().paymentStatus = false
        }
    }
}