package com.antcloud.app.viewmodel


import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antcloud.app.data.LoginReqData
import com.antcloud.app.data.UserRegisterReq
import com.antcloud.app.data.LoginState
import com.antcloud.app.logic.auth.LoginLogic
import com.antcloud.app.logic.auth.SignUpLogic
import com.antcloud.app.common.Resource
import com.antcloud.app.data.CheckUserInDB
import com.antcloud.app.data.ForgotPasswordReq
import com.antcloud.app.data.ForgotState
import com.antcloud.app.data.PhoneOtpReq
import com.antcloud.app.data.PhoneVerifyReq
import com.antcloud.app.logic.auth.CheckUserInDBLogic
import com.antcloud.app.logic.auth.ForgotLogic
import com.antcloud.app.logic.auth.LoginErrors
import com.antcloud.app.logic.auth.PostPhoneLogic
import com.antcloud.app.logic.auth.PostPhoneOtpData
import com.antcloud.app.logic.auth.PostPhoneVerifyData
import com.antcloud.app.screen.auth.SignupErrors

import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import retrofit2.http.Body
import javax.inject.Inject




@HiltViewModel
class AuthenticateViewModel @Inject constructor(private val loginLogic: LoginLogic,
                                                private val checkUserInDBLogic: CheckUserInDBLogic?,
                                                private val signupLogic: SignUpLogic?,
                                                private val postPhoneLogic: PostPhoneLogic?,
                                                private val postPhoneOtpLogic: PostPhoneOtpData?,
                                                private val postPhoneVerifyLogic: PostPhoneVerifyData?,
                                                private val forgotLogic: ForgotLogic?,
                                                @ApplicationContext val context: Context): ViewModel() {



    var titleText: String = "Create an Account"
    var subTitleText: ((String) -> Unit)? = null

    var flag: Boolean = false
    var signUpStateText: String = "email"
    var subSignUpStateText: ((String) -> Unit)? = null
    var pagerInitialState: Int = -1
    var subPagerInitialState: ((Int) -> Unit)? = null
    var loginLoadingTextState: String = ""
    var LoginSubLoadingState: ((String) -> Unit)? = null
    var signupLoadingTextState: String = ""
    var signupSubLoadingState: ((String) -> Unit)? = null
    var resendOTPText: Boolean = true
    var subResendOTPText: ((Boolean) -> Unit)? = null
    var textVisible: Boolean = true
    var subTextVisible: ((Boolean) -> Unit)? = null
    var forgotSuccessText: Boolean = false
    var subForgotSuccessText: ((Boolean) -> Unit)? = null
    var loginError: LoginErrors = LoginErrors.NULL
    var subLoginErrorState: ((LoginErrors) -> Unit)? = null
    var type: String = ""
    var subType: ((String) -> Unit)? = null
    var otpState: String = ""
    var subOtpState: ((String) -> Unit)? = null
    var signupErrorState: SignupErrors = SignupErrors.NULL
    var subSignupErrorState: ((SignupErrors) -> Unit)? = null

    fun updatePagerState(value:Int){
        pagerInitialState = value
        subPagerInitialState?.invoke(value)
    }

    fun updateSignUpState(value:String){
        signUpStateText = value
        subSignUpStateText?.invoke(value)
    }

    fun updateSignUpLoadingText(loadingText: String) {
        signupLoadingTextState = loadingText
        signupSubLoadingState?.invoke(loadingText)
    }

    fun updateTitleText(value: String) {
        titleText = value
        subTitleText?.invoke(value)
    }

    fun updateLoginLoadingText(loadingText: String) {
        loginLoadingTextState = loadingText
        LoginSubLoadingState?.invoke(loadingText)
    }

    fun updateResendText(value: Boolean) {
        resendOTPText = value
        subResendOTPText?.invoke(value)
    }

    fun updateTextVisible(value: Boolean) {
        textVisible = value
        subTextVisible?.invoke(value)
    }

    fun updateSuccessText(value: Boolean) {
        forgotSuccessText = value
        subForgotSuccessText?.invoke(value)
    }

    fun updateType(value:String){
        type = value
        subType?.invoke(value)
    }

    fun updateOtpState(otp: String) {
        otpState = otp
        subOtpState?.invoke(otp)
    }

    fun updateLoginOtpState(otp: String) {
        otpState = otp
        subOtpState?.invoke(otp)
    }

    fun updateLoginError(error: LoginErrors) {
        loginError = error
        subLoginErrorState?.invoke(error)
    }

    fun updateSignupError(error: SignupErrors) {
        signupErrorState = error
        subSignupErrorState?.invoke(error)
    }

    var nameState: String = ""
    var subNameState: ((String) -> Unit)? = null

    var phoneNumberState: String = ""
    var subPhoneNumberState: ((String) -> Unit)? = null

    var emailState: String = ""
    var subEmailState: ((String) -> Unit)? = null

    var stateLocation: String = ""
    var subStateLocation: ((String) -> Unit)? = null

    var pinCodeState: String = ""
    var subPinCodeState: ((String) -> Unit)? = null

    var passwordState: String = ""
    var subPasswordState: ((String) -> Unit)? = null

    fun updateNameState(name: String) {
        nameState = name
        subNameState?.invoke(name)
    }

    fun updateEmailState(email: String) {
        emailState = email
        subEmailState?.invoke(email)
    }

    fun updatePhoneNumber(number: String) {
        phoneNumberState = number
        subPhoneNumberState?.invoke(number)
    }

    fun updateStateLocation(name: String) {
        stateLocation = name
        subStateLocation?.invoke(name)
    }

    fun updatePincode(name: String) {
        pinCodeState = name
        subPinCodeState?.invoke(name)
    }

    fun updatePasswordState(password: String) {
        passwordState = password
        subPasswordState?.invoke(password)
    }

    val _checkUserInDBState = mutableStateOf(ForgotState())
    val checkUserInDBState: State<ForgotState> = _checkUserInDBState
     val _loginState = mutableStateOf(LoginState())
    val loginState: State<LoginState> = _loginState
    private val _signUpState = mutableStateOf(LoginState())
    val signUpState: State<LoginState> = _signUpState
    private val _postPhoneState = mutableStateOf(LoginState())
     val postPhoneState: State<LoginState> = _postPhoneState
    val _postPhoneOtpState = mutableStateOf(LoginState())
     val postPhoneOtpState: State<LoginState> = _postPhoneOtpState
    public val _postPhoneVerifyState = mutableStateOf(LoginState())
     val postPhoneVerifyState: State<LoginState> = _postPhoneVerifyState
    private val _forgotState = mutableStateOf(ForgotState())
     val forgotState: State<ForgotState> = _forgotState
    private var job: Job? = null

    @SuppressLint("SuspiciousIndentation")
    fun checkUserInDB(@Body body: CheckUserInDB) {
        job?.cancel()
        job = viewModelScope.launch(Dispatchers.IO) {
            checkUserInDBLogic?.let { it(body) }?.onEach { result ->
                when (result) {
                    is Resource.Loading -> {
                        _checkUserInDBState.value = ForgotState(isLoading = true)
                    }
                    //                    {"message":"User Found!"}
                    is Resource.Success -> {
                        _checkUserInDBState.value = checkUserInDBState.value.copy(
                            isLoading = false,
                            success = 1)

                    }

                    is Resource.Error -> {
                        delay(200)
                        _checkUserInDBState.value = ForgotState(error = result.message.toString(), success = 0, errorCode = result.errorCode!!, isLoading = false)
                    }

                }
            }?.launchIn(viewModelScope)
        }
    }

    @SuppressLint("SuspiciousIndentation")
    fun getUserLogin(@Body body: LoginReqData) {
        job?.cancel()
        job = viewModelScope.launch(Dispatchers.IO) {
            loginLogic(body).onEach { result ->
                  when (result) {
                      is Resource.Loading -> {
                          _loginState.value = LoginState(isLoading = true)
                      }
                      is Resource.Success -> {
                          _loginState.value = loginState.value.copy(
                              isLoading = false,
                              userData = result.data?.body()?.user,
                              success = 1)

                      }
                      is Resource.Error -> {
                          Log.i("test" ,"Error")
                          delay(200)
                          _loginState.value = LoginState(error = result.message.toString(), success = 0, errorCode = result.errorCode!!, isLoading = false)
                      }

                }
            }.launchIn(viewModelScope)
        }
    }

    fun getSignUpData(@Body body: UserRegisterReq) {
        job?.cancel()
        job = viewModelScope.launch(Dispatchers.IO) {
            signupLogic?.let { it(body) }?.onEach { result ->
                when (result) {
                    is Resource.Loading -> {
                        _signUpState.value = LoginState(isLoading = true)
                    }

                    is Resource.Success -> {
                        _signUpState.value = signUpState.value.copy(
                            isLoading = false,
                            userData = result.data?.body()?.doc,
                            success = 1)

                    }

                    is Resource.Error -> {
                        Log.i("test" ,"Error")
                        delay(200)
                        _signUpState.value = LoginState(error = result.message.toString(), success = 0, errorCode = result.errorCode!!, isLoading = false)
                    }

                }
            }?.launchIn(viewModelScope)
        }
    }

    fun getPostPhoneData(@Body body: PhoneVerifyReq) {
        job?.cancel()
        job = viewModelScope.launch(Dispatchers.IO) {
            postPhoneLogic?.let { it(body) }?.onEach { result ->
                when (result) {
                    is Resource.Loading -> {
                        _postPhoneState.value = LoginState(isLoading = true)
                    }

                    is Resource.Success -> {
                        _postPhoneState.value = postPhoneState.value.copy(
                            isLoading = false,
                            userData = result.data?.body()?.user,
                            success = 1)

                    }

                    is Resource.Error -> {
                        Log.i("test" ,"Error")
                        delay(200)
                        _postPhoneState.value = LoginState(error = result.message.toString(), success = 0, errorCode = result.errorCode!!, isLoading = false)
                    }

                }
            }?.launchIn(viewModelScope)
        }
    }

    fun getPostPhoneOtpData(@Body body: PhoneOtpReq, apiName : String) {
        job?.cancel()
        job = viewModelScope.launch(Dispatchers.IO) {
            postPhoneOtpLogic?.let { it(body,apiName) }?.onEach { result ->
                when (result) {
                    is Resource.Loading -> {
                        _postPhoneOtpState.value = LoginState(isLoading = true)
                    }

                    is Resource.Success -> {
                        _postPhoneOtpState.value = postPhoneOtpState.value.copy(
                            isLoading = false,
                            success = 1)

                    }

                    is Resource.Error -> {
                        Log.i("test" ,"Error")
                        delay(200)
                        _postPhoneOtpState.value = LoginState(error = result.message.toString(), success = 0, errorCode = result.errorCode!!, isLoading = false)
                    }

                }
            }?.launchIn(viewModelScope)
        }
    }

    fun getPostPhoneVerifyData(@Body body: PhoneVerifyReq, apiName: String) {
        job?.cancel()
        job = viewModelScope.launch(Dispatchers.IO) {
            postPhoneVerifyLogic?.let { it(body,apiName) }?.onEach { result ->
                when (result) {
                    is Resource.Loading -> {
                        _postPhoneVerifyState.value = LoginState(isLoading = true)
                    }

                    is Resource.Success -> {
                        _postPhoneVerifyState.value = postPhoneVerifyState.value.copy(
                            isLoading = false,
                            success = 1)

                    }

                    is Resource.Error -> {
                        Log.i("test" ,"Error")
                        delay(200)
                        _postPhoneVerifyState.value = LoginState(error = result.message.toString(),success = 0, errorCode = result.errorCode!!, isLoading = false)
                    }

                }
            }?.launchIn(viewModelScope)
        }
    }

    fun getForgotData(@Body body: ForgotPasswordReq, apiName:String) {
        job?.cancel()
        job = viewModelScope.launch(Dispatchers.IO) {
            forgotLogic?.let { it(body,apiName) }?.onEach { result ->
                when (result) {
                    is Resource.Loading -> {
                        _forgotState.value = ForgotState(isLoading = true)
                    }

                    is Resource.Success -> {
                        _forgotState.value = forgotState.value.copy(
                            isLoading = false,
                            message = result.message!!,
                            success = 1)

                    }

                    is Resource.Error -> {
                        Log.i("test" ,"Error")
                        delay(200)
                        _forgotState.value = ForgotState(error = result.message.toString(), success = 0, errorCode = result.errorCode!!, isLoading = false)
                    }

                }
            }?.launchIn(viewModelScope)
        }
    }

}