package com.limelight.activity

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import dagger.hilt.android.AndroidEntryPoint
import androidx.hilt.navigation.compose.hiltViewModel
import com.limelight.data.ForgotState
import com.limelight.viewmodel.AuthenticateViewModel
import com.limelight.common.AppUtils.Companion.hideStatusBar
import com.limelight.common.AppUtils.Companion.navigateSplashActivity
import com.limelight.screen.auth.SignupScreen


@AndroidEntryPoint
class SignupActivity : ComponentActivity() {
    lateinit var  viewModel : AuthenticateViewModel
    var emailMobileValue: String = " "
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if(intent.hasExtra("email")){
            emailMobileValue = intent.getStringExtra("email").toString()
        }
        setContent {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            hideStatusBar(this@SignupActivity)
            viewModel = hiltViewModel()
            SignupScreen(this@SignupActivity, viewModel, emailMobileValue)
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        if(viewModel.signUpStateText=="otp")
            viewModel.updateSignUpState("email")
        else {
            viewModel._checkUserInDBState.value =  ForgotState(success = -1)
            navigateSplashActivity(this@SignupActivity)
        }
    }
}

