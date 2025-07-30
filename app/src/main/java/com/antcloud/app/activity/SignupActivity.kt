package com.antcloud.app.activity

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import androidx.hilt.navigation.compose.hiltViewModel
import com.antcloud.app.data.ForgotState
import com.antcloud.app.viewmodel.AuthenticateViewModel
import com.antcloud.app.common.AppUtils.Companion.hideStatusBar
import com.antcloud.app.common.AppUtils.Companion.navigateSplashActivity
import com.antcloud.app.screen.auth.SignupScreen


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
            hideStatusBar(this@SignupActivity)
            viewModel = hiltViewModel()
            SignupScreen(this@SignupActivity, viewModel, emailMobileValue)
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        if(viewModel.signUpStateText=="otp") {
            viewModel.updateSignUpState("email")
            viewModel.updateTitleText("Create an Account")
        }
        else {
            viewModel._checkUserInDBState.value =  ForgotState(success = -1)
            navigateSplashActivity(this@SignupActivity)
        }
    }
}

