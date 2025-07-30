package com.antcloud.app.viewmodel

import android.os.CountDownTimer
import androidx.lifecycle.ViewModel

import com.antcloud.app.common.GlobalData

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