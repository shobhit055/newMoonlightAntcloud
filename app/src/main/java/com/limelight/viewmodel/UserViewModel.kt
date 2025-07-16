package com.limelight.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.limelight.common.GlobalData
import com.limelight.data.CheckUserState
import com.limelight.data.RefreshTokenState
import com.limelight.data.UpdatePhoneState

import com.limelight.common.Resource
import com.limelight.data.ForgotPasswordReq
import com.limelight.data.ForgotState
import com.limelight.data.LoginState
import com.limelight.data.LogoutState
import com.limelight.data.PhoneOtpReq
import com.limelight.data.PhoneVerifyReq
import com.limelight.data.PinVerifyRequest
import com.limelight.data.PinVerifyState
import com.limelight.data.ResendVerificationEmailState
import com.limelight.data.UpdatePhoneReq
import com.limelight.data.UpdateResolutionReq
import com.limelight.data.UpdateResolutionState
import com.limelight.data.User
import com.limelight.logic.auth.ForgotLogic
import com.limelight.logic.auth.PostPhoneOtpData
import com.limelight.logic.auth.PostPhoneVerifyData
import com.limelight.logic.auth.VerifyPinLogic
import com.limelight.logic.user.CheckUserLogic
import com.limelight.logic.user.LogoutLogic
import com.limelight.logic.user.RefreshTokenLogic
import com.limelight.logic.user.ResendVerificationEmailLogic
import com.limelight.logic.user.UpdatePhoneLogic
import com.limelight.logic.user.UpdateResolutionLogic
import com.limelight.screen.account.BottomSheetState
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