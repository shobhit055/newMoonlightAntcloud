package com.limelight.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.hilt.navigation.compose.hiltViewModel
import com.limelight.R
import com.limelight.data.ForgotState
import com.limelight.data.LoginState

import com.limelight.viewmodel.AuthenticateViewModel
import com.limelight.common.AppUtils
import com.limelight.screen.auth.LoginScreen
import dagger.hilt.android.AndroidEntryPoint

@Suppress("DEPRECATION")
@AndroidEntryPoint
class LoginActivity : ComponentActivity() {
    var emailMobileValue: String = ""
    var type: String = ""
    var postPhoneOtpState : LoginState? = null
    lateinit  var viewModel : AuthenticateViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        postPhoneOtpState = LoginState(success = -1)
        if(intent.hasExtra("email"))
            emailMobileValue = intent.getStringExtra("email").toString()
        if(intent.hasExtra("type"))
            type = intent.getStringExtra("type").toString()
        setContent {
            viewModel =  hiltViewModel()
            viewModel.updateType(type)
            viewModel._postPhoneVerifyState.value = LoginState(success = -1)
            LoginScreen(this@LoginActivity , emailMobileValue , viewModel)
        }
    }

    override fun onResume()  {
        super.onResume()
        if(AppUtils.getKey()==null){
            AppUtils.getKeyGenerator()
        }
    }

    override fun onBackPressed() {
        if(!viewModel.textVisible){
            viewModel.updateSuccessText(false)
            viewModel.updateTextVisible(true)
            viewModel.updateType("email")
        }
        else{
            viewModel._checkUserInDBState.value =  ForgotState(success = -1)
            viewModel._postPhoneOtpState.value =  LoginState(success = -1)
            super.onBackPressed()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
    }
}