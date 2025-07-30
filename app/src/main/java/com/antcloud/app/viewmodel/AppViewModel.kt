package com.antcloud.app.viewmodel

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.IBinder
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antcloud.app.binding.crypto.AndroidCryptoProvider
import com.antcloud.app.common.Resource
import com.antcloud.app.computers.ComputerManagerService
import com.antcloud.app.computers.ComputerManagerService.ComputerManagerBinder
import com.antcloud.app.data.AppViewUiState
import com.antcloud.app.data.PinVerifyRequest
import com.antcloud.app.data.PinVerifyState
import com.antcloud.app.logic.auth.VerifyPinLogic
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import retrofit2.http.Body
import retrofit2.http.Header
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(private val verifyPinLogic: VerifyPinLogic,
                                       @ApplicationContext val context: Context): ViewModel(){

    private val _uiState = MutableStateFlow(AppViewUiState())
    val uiState: StateFlow<AppViewUiState> = _uiState.asStateFlow()
    private var managerBinder: ComputerManagerBinder? = null
    private var managerBinder1: ComputerManagerBinder? = null
    private var poller: ComputerManagerService.ApplistPoller? = null
    private var freezeUpdates = false
    private var runningPolling = false
    private var inForeground = true

    private val serviceConnection1: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, binder: IBinder) {
            val localBinder = (binder as ComputerManagerBinder)

            object : Thread() {
                override fun run() {
                    localBinder.waitForReady()
                    managerBinder1 = localBinder
              //     startComputerUpdates1()

                    AndroidCryptoProvider(context).clientCertificate
                }
            }.start()
        }

        override fun onServiceDisconnected(className: ComponentName) {
            managerBinder1 = null
        }
    }
//    private val serviceConnection: ServiceConnection = object : ServiceConnection {
//        override fun onServiceConnected(className: ComponentName, binder: IBinder) {
//            val localBinder =
//                (binder as ComputerManagerBinder)
//
//            object : Thread() {
//                override fun run() {
//                    localBinder.waitForReady()
//                    try {
//                        appGridAdapter = AppGridAdapter(
//                            context, PreferenceConfiguration.readPreferences(context),
//                            computer, localBinder.uniqueId, showHiddenApps
//                        )
//                    } catch (e: Exception) {
//                        e.printStackTrace()
//                        finish()
//                        return
//                    }
//                    appGridAdapter!!.updateHiddenApps(hiddenAppIds, true)
//                    managerBinder = localBinder
//                    populateAppGridWithCache()
//                    startComputerUpdates()
//                }
//            }.start()
//        }
//
//        override fun onServiceDisconnected(className: ComponentName) {
//            managerBinder = null
//        }
//    }
//
//
//    fun bindServices() {
//        context.bindService(Intent(context, ComputerManagerService::class.java), serviceConnection1, Context.BIND_AUTO_CREATE)
//    }



    var resolution: String = ""
    var subResolution: ((String) -> Unit)? = null

    var fpsNames: String = ""
    var subFPSNames: ((String) -> Unit)? = null

    var initialValueKbps: Float = 5000f
    var subIntialValueKbps: ((Float) -> Unit)? = null

    fun updateResolution(name: String) {
        resolution = name
        subResolution?.invoke(name)
    }

    fun updateFPSNames(name: String) {
        fpsNames = name
        subFPSNames?.invoke(name)
    }

    fun updateBitrate(name: Float) {
        initialValueKbps = name
        subIntialValueKbps?.invoke(name)
    }

    private var job: Job? = null
    private val _verifyPin = mutableStateOf(PinVerifyState())
    val verifyPin: State<PinVerifyState> = _verifyPin

    fun getVerifyPinData(@Body body: PinVerifyRequest, @Header("Authorization") token : String) {
        job?.cancel()
        job = viewModelScope.launch(Dispatchers.IO) {
            verifyPinLogic(token,body).onEach { result ->
                when (result) {
                    is Resource.Loading -> {
                        _verifyPin.value = PinVerifyState(isLoading = true)
                    }
                    is Resource.Success -> {
                        _verifyPin.value = verifyPin.value.copy(
                            isLoading = false,
                            message = result.message,
                            success = 1)

                    }
                    is Resource.Error -> {
                        delay(200)
                        _verifyPin.value = PinVerifyState(error = result.message.toString(), success = 0, errorCode = result.errorCode!!, isLoading = false)
                    }
                }
            }.launchIn(viewModelScope)
        }
    }
}