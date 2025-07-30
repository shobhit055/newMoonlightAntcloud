package com.antcloud.app.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antcloud.app.common.GlobalData
import com.antcloud.app.data.CheckUserState
import com.antcloud.app.data.RefreshTokenState
import com.antcloud.app.data.UpdatePhoneState

import com.antcloud.app.common.Resource
import com.antcloud.app.data.ForgotPasswordReq
import com.antcloud.app.data.ForgotState
import com.antcloud.app.data.LoginState
import com.antcloud.app.data.LogoutState
import com.antcloud.app.data.PhoneOtpReq
import com.antcloud.app.data.PhoneVerifyReq
import com.antcloud.app.data.PinVerifyRequest
import com.antcloud.app.data.PinVerifyState
import com.antcloud.app.data.ResendVerificationEmailState
import com.antcloud.app.data.UpdatePhoneReq
import com.antcloud.app.data.UpdateResolutionReq
import com.antcloud.app.data.UpdateResolutionState
import com.antcloud.app.data.User
import com.antcloud.app.logic.auth.ForgotLogic
import com.antcloud.app.logic.auth.PostPhoneOtpData
import com.antcloud.app.logic.auth.PostPhoneVerifyData
import com.antcloud.app.logic.auth.VerifyPinLogic
import com.antcloud.app.logic.user.CheckUserLogic
import com.antcloud.app.logic.user.LogoutLogic
import com.antcloud.app.logic.user.RefreshTokenLogic
import com.antcloud.app.logic.user.ResendVerificationEmailLogic
import com.antcloud.app.logic.user.UpdatePhoneLogic
import com.antcloud.app.logic.user.UpdateResolutionLogic
import com.antcloud.app.screen.account.BottomSheetState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Path
import javax.inject.Inject




@HiltViewModel
class UserViewModel @Inject constructor(private val checkUserUseCase: CheckUserLogic,
                                        private val refreshTokenUseCase : RefreshTokenLogic,
                                        private val postPhoneOtpLogic: PostPhoneOtpData,
                                        private val postPhoneVerifyLogic: PostPhoneVerifyData,
                                        private val forgotLogic : ForgotLogic,
                                        private val updatePhoneLogic: UpdatePhoneLogic,
                                        private val updateResolutionLogic : UpdateResolutionLogic,
                                        private val resendVerificationEmailLogic: ResendVerificationEmailLogic,
                                        private val logoutLogic: LogoutLogic,
                                        private val verifyPinLogic: VerifyPinLogic,
                                        @ApplicationContext val context: Context): ViewModel() {


    var openSupportScreen: Boolean = false
    var subopenSupportScreen: ((Boolean) -> Unit)? = null

    private var job: Job? = null
    private val _checkUserState = mutableStateOf(CheckUserState())
    val checkUserState: State<CheckUserState> = _checkUserState
    private val _refreshTokenState = mutableStateOf(RefreshTokenState())
    val refreshTokenState: State<RefreshTokenState> = _refreshTokenState
    private val _updatePhoneState = mutableStateOf(UpdatePhoneState())
    val updatePhoneState: State<UpdatePhoneState> = _updatePhoneState
    private val _updateResolutionState= mutableStateOf(UpdateResolutionState())
    val updateResolutionState: State<UpdateResolutionState> = _updateResolutionState
    private val _resendVerificationEmailState = mutableStateOf(ResendVerificationEmailState())
    val resendVerificationEmailState: State<ResendVerificationEmailState> = _resendVerificationEmailState
    private val _logoutState = mutableStateOf(LogoutState())
    val logoutState: State<LogoutState> = _logoutState
    private val _postPhoneOtpState = mutableStateOf(LoginState())
    val postPhoneOtpState: State<LoginState> = _postPhoneOtpState
    private val _postPhoneVerifyState = mutableStateOf(LoginState())
    val postPhoneVerifyState: State<LoginState> = _postPhoneVerifyState
    private val _forgotState = mutableStateOf(ForgotState())
    val forgotState: State<ForgotState> = _forgotState
    private val _verifyPin = mutableStateOf(PinVerifyState())
    val verifyPin: State<PinVerifyState> = _verifyPin
    var bottomSheetState: BottomSheetState = BottomSheetState.LOADING
    var subBottomSheetState: ((BottomSheetState) -> Unit)? = null
    var accountData: User = GlobalData.getInstance().accountData
    var subAccountData: ((User) -> Unit)? = null
    var selectedResolution: String = ""
    var subSelectedResolution: ((String) -> Unit)? = null
    var newNumber: String = ""
    var otpEntered = ""
    var currentRes : String = accountData.resolution
    var subCurrentRes : ((String) -> Unit)? = null
    var userData = GlobalData.getInstance().accountData
    var subUserData: ((User) -> Unit)? = null
    var subShowMaintenanceDialog: ((Boolean) -> Unit)? = null

    fun updateShowMaintenanceDialog(res: Boolean) {
        subShowMaintenanceDialog?.invoke(res)
    }

    var toggle: Boolean = false
    var subToggle: ((Boolean) -> Unit)? = null

    fun updateOpenScreen(route: Boolean) {
        openSupportScreen = route
        subopenSupportScreen?.invoke(route)
    }

    fun updateUserInfo(userInfo: User) {
        userData = userInfo
        subUserData?.invoke(userInfo)
    }

    fun updateBottomSheetState(state: BottomSheetState) {
        bottomSheetState = state
        subBottomSheetState?.invoke(state)
    }

    fun updateAccount(account: User) {
        accountData = account
        subAccountData?.invoke(account)
    }

    fun initializeAccountData() {
        updateAccount(GlobalData.getInstance().accountData)
        selectedResolution = accountData.resolution
    }

    fun updateResolution(res: String) {
        GlobalData.getInstance().accountData.resolution = res
        currentRes = res
        subCurrentRes?.invoke(currentRes)
    }


    @SuppressLint("SuspiciousIndentation")
    fun getCheckUserData(@Header("Authorization") token: String) {
        job?.cancel()
        job = viewModelScope.launch(Dispatchers.IO) {
            checkUserUseCase(token).onEach { result ->
                when (result) {
                    is Resource.Loading -> {
                        _checkUserState.value = CheckUserState(isLoading = true)
                    }
                    is Resource.Success -> {
                        _checkUserState.value = checkUserState.value.copy(
                            isLoading = false,
                            userData = result.data?.body()?.user,
                            token = result.data?.body()?.token,
                            success = 1
                        )
                    }
                    is Resource.Error -> {
                        delay(200)
                        _checkUserState.value = CheckUserState(
                            error = result.message!!.toString(),
                            success = 0,
                            errorCode = result.errorCode!!,
                            isLoading = false)
                    }
                }
            }.launchIn(viewModelScope)
        }
    }
    fun getRefreshTokenData(@Header("refresh") token: String) {
        job?.cancel()
        job = viewModelScope.launch(Dispatchers.IO) {
            refreshTokenUseCase(token).onEach { result ->
                when (result) {
                    is Resource.Loading -> {
                        _refreshTokenState.value = RefreshTokenState(isLoading = true)
                    }
                    is Resource.Success -> {
                        _refreshTokenState.value = refreshTokenState.value.copy(isLoading = false,
                            message = result.message!!,
                            accessToken = result.token!!,
                            refreshToken = result.refreshToken!!,
                            success = 1)
                    }
                    is Resource.Error -> {
                        delay(200)
                        _refreshTokenState.value = RefreshTokenState(
                            error = result.message.toString(),
                            success = 0,
                            errorCode = result.errorCode!!,
                            isLoading = false
                        )
                    }
                }
            }.launchIn(viewModelScope)
        }
    }
    fun getPostPhoneOtpData(@Body body: PhoneOtpReq, apiName : String) {
        job?.cancel()
        job = viewModelScope.launch(Dispatchers.IO) {
            postPhoneOtpLogic(body,apiName).onEach { result ->
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
                        delay(200)
                        _postPhoneOtpState.value = LoginState(error = result.message.toString(), success = 0, errorCode = result.errorCode!!, isLoading = false)
                    }
                }
            }.launchIn(viewModelScope)
        }
    }
    fun getPostPhoneVerifyData(@Body body: PhoneVerifyReq, apiName: String) {
        job?.cancel()
        job = viewModelScope.launch(Dispatchers.IO) {
            postPhoneVerifyLogic(body,apiName).onEach { result ->
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
                        delay(200)
                        _postPhoneVerifyState.value = LoginState(error = result.message.toString(),success = 0, errorCode = result.errorCode!!, isLoading = false)
                    }
                }
            }.launchIn(viewModelScope)
        }
    }
    fun getUpdatePhoneData(@Header("Authorization") token : String, @Path("id") id: String, @Body body: UpdatePhoneReq) {
        job?.cancel()
        job = viewModelScope.launch(Dispatchers.IO) {
            updatePhoneLogic(token,id,body).onEach { result ->
                when (result) {
                    is Resource.Loading -> {
                        _updatePhoneState.value = UpdatePhoneState(isLoading = true)
                    }
                    is Resource.Success -> {
                        _updatePhoneState.value = updatePhoneState.value.copy(isLoading = false,
                            message = result.message!!, phone = result.data?.body()?.doc?.phone!!,
                            success = 1)
                    }
                    is Resource.Error -> {
                        Log.i("test", "Error")
                        delay(200)
                        _updatePhoneState.value = UpdatePhoneState(
                            error = result.message.toString(),
                            success = 0,
                            errorCode = result.errorCode!!,
                            isLoading = false
                        )
                    }
                }
            }.launchIn(viewModelScope)
        }
    }
    fun getUpdateResolutionData(@Header("Authorization") token : String, @Path("id") id: String , @Body body: UpdateResolutionReq) {
        job?.cancel()
        job = viewModelScope.launch(Dispatchers.IO) {
            updateResolutionLogic(token,id,body).onEach { result ->
                when (result) {
                    is Resource.Loading -> {
                        _updateResolutionState.value = UpdateResolutionState(isLoading = true)
                    }
                    is Resource.Success -> {
                        _updateResolutionState.value = updateResolutionState.value.copy(isLoading = false,
                            message = result.message!!, resolution = result.data?.body()?.doc?.resolution!!,
                            success = 1)
                    }
                    is Resource.Error -> {
                        delay(200)
                        _updateResolutionState.value = UpdateResolutionState(
                            error = result.message.toString(),
                            errorCode = result.errorCode!!,
                            success = 0,
                            isLoading = false
                        )
                    }
                }
            }.launchIn(viewModelScope)
        }
    }
    fun getResendVerificationEmailData(@Header("Authorization") token : String) {
        job?.cancel()
        job = viewModelScope.launch(Dispatchers.IO) {
            resendVerificationEmailLogic(token).onEach { result ->
                when (result) {
                    is Resource.Loading -> {
                        _resendVerificationEmailState.value = ResendVerificationEmailState(isLoading = true)
                    }
                    is Resource.Success -> {

                        _resendVerificationEmailState.value = resendVerificationEmailState.value.copy(isLoading = false,
                            message = result.message!!, success = 1)
                    }
                    is Resource.Error -> {
                        delay(200)
                        _resendVerificationEmailState.value = ResendVerificationEmailState(
                            error = result.message.toString(),
                            errorCode = result.errorCode!!,
                            success = 0,
                            isLoading = false
                        )
                    }
                }
            }.launchIn(viewModelScope)
        }
    }
    fun getLogoutData(@Header("refresh")  refresh : String ) {
        job?.cancel()
        job = viewModelScope.launch(Dispatchers.IO) {
            logoutLogic(refresh).onEach { result ->
                when (result) {
                    is Resource.Loading -> {
                        _logoutState.value = LogoutState(isLoading = true)
                    }
                    is Resource.Success -> {
                        _logoutState.value = logoutState.value.copy(isLoading = false,
                            message = result.message!!, success = 1)
                    }
                    is Resource.Error -> {
                        delay(200)
                        _logoutState.value = LogoutState(
                            error = result.message.toString(),
                            success = 0,
                            errorCode = result.errorCode!!,
                            isLoading = false
                        )
                    }
                }
            }.launchIn(viewModelScope)
        }
    }
    fun getForgotData(@Body body: ForgotPasswordReq, apiName:String) {
        job?.cancel()
        job = viewModelScope.launch(Dispatchers.IO) {
            forgotLogic(body,apiName).onEach { result ->
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
                        delay(200)
                        _forgotState.value = ForgotState(error = result.message.toString(), success = 0, errorCode = result.errorCode!!, isLoading = false)
                    }
                }
            }.launchIn(viewModelScope)
        }
    }

    fun getVerifyPinData(@Body body: PinVerifyRequest, @Header("Authorization") token : String) {
        job?.cancel()
        job = viewModelScope.launch(Dispatchers.IO) {
            verifyPinLogic(token,body).onEach { result ->
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
                        delay(200)
                        _forgotState.value = ForgotState(error = result.message.toString(), success = 0, errorCode = result.errorCode!!, isLoading = false)
                    }
                }
            }.launchIn(viewModelScope)
        }
    }

}