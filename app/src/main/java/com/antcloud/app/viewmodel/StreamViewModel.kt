package com.antcloud.app.viewmodel


import android.annotation.SuppressLint
import android.content.Context
import android.os.CountDownTimer
import android.util.Log

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antcloud.app.common.GlobalData
import com.antcloud.app.common.Resource
import com.antcloud.app.data.CheckVMStatusState
import com.antcloud.app.data.GetVMIPState

import com.antcloud.app.data.User
import com.antcloud.app.data.VMStatusState
import com.antcloud.app.logic.vm.CheckVMConnectionStatusLogic
import com.antcloud.app.logic.vm.GetVMIpLogic
import com.antcloud.app.logic.vm.VmStatusLogic

import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.http.Header
import java.net.URISyntaxException
import javax.inject.Inject

enum class VMStatus {
    Idle,
    Starting,
    Connected,
    Error
}

@HiltViewModel
class StreamViewModel @Inject constructor(private val vmStatusLogic : VmStatusLogic,
                                          private val getVmipLogic : GetVMIpLogic,
                                          private val checkVMConnectionStatusLogic : CheckVMConnectionStatusLogic,
                                          @ApplicationContext val context : Context) : ViewModel() {

    private val _requestVmIpEvent = MutableSharedFlow<Unit>(replay = 0)
    val requestVmIpEvent: SharedFlow<Unit> = _requestVmIpEvent

    private val _status = MutableStateFlow(VMStatus.Idle)
    val status: StateFlow<VMStatus> = _status
    val globalInstance =   GlobalData.getInstance()


    private fun stopSocketConnection(socket: Socket, reason: String) {
        if (socket.connected()) {

            Log.d("Socket", "Disconnecting due to testt: $reason")
            socket.disconnect()
        }
    }


    var loadingData: String = "Sending Boot Request \n Please refresh your page and try again if it takes more than 30 seconds."
    var subLoadingData: ((String) -> Unit)? = null

    private var job: Job? = null

    private val _getVMStatus= mutableStateOf(VMStatusState())
    val getVMStatus: State<VMStatusState> = _getVMStatus
    private val _checkVMStatus= mutableStateOf(CheckVMStatusState())
    val checkVMStatus: State<CheckVMStatusState> = _checkVMStatus
    private val _getVMIPState = mutableStateOf(GetVMIPState())
    val getVMIPState: State<GetVMIPState> = _getVMIPState
    var accountData: User = GlobalData.getInstance().accountData
    var userData = GlobalData.getInstance().accountData
    var vmStartingFlag =  false
    val _timeLeft = MutableStateFlow("06:00")
    val timeLeft: StateFlow<String> = _timeLeft
    private var countDownTimer: CountDownTimer? = null

    val _disConnTimeLeft = MutableStateFlow("01:00")
    val disConnTimeLeft: StateFlow<String> = _disConnTimeLeft
    private var countDownTimerDisconn: CountDownTimer? = null



    fun callSocket(){
        try {
            val opts = IO.Options().apply {
                path = "/socket.io/"
                transports = arrayOf("websocket")
                query = "type=client&game=desktop&token=${GlobalData.getInstance().accountData.token}&stream=${GlobalData.getInstance().accountData.resolution}%206000%2060%2010&connection=mobile"
                extraHeaders = mapOf("Origin" to listOf("https://antcloud.co"))
                reconnection = true
            }
            globalInstance.socket = IO.socket("wss://socket.antcloud.co:8000", opts)
            globalInstance.socket.on(Socket.EVENT_CONNECT) {
                Log.d("Socket", "Connected with ID: ${globalInstance.socket.id()}")
            }
            globalInstance.socket.on(Socket.EVENT_CONNECT_ERROR) { args ->
                Log.e("Socket", "Connection error: ${args.getOrNull(0)}")
                stopSocketConnection(globalInstance.socket, "connect_error")
            }
            globalInstance.socket.on(Socket.EVENT_DISCONNECT) { args ->
                Log.w("Socket", "Disconnected: ${args.getOrNull(0)}")

            }
            globalInstance.socket.on("new_connection") { args ->
                val data = args.getOrNull(0) as? JSONObject
                if ((data?.optInt("clients") ?: 0) >= 2) {
                    viewModelScope.launch {
                        _requestVmIpEvent.emit(Unit)
                    }
                }
            }
            globalInstance.socket.on("waiting") { args ->
                val length = (args.getOrNull(0) as? Int) ?: 0
                val waitTime = when {
                    length <= 5 -> "5 mins"
                    length <= 10 -> "5–10 mins"
                    length <= 20 -> "5–15 mins"
                    length <= 25 -> "15–25 mins"
                    else -> "30+ mins"
                }
                Log.d("Socket", "Queue length: $length, estimated wait: $waitTime")
            }
            globalInstance.socket.on("inactive") { args ->
                Log.w("Socket", "Inactive: ${args.getOrNull(0)}")
                stopSocketConnection(globalInstance.socket, "inactive")
            }
            globalInstance.socket.on("control") { args ->
                val data = args.getOrNull(0)
                Log.i("socket_test" , "test $data")
                when (data) {
                    is JSONObject -> {
                        Log.d("Socket", "Time left: ${data.optString("timeLeft")}")
                    }
                    "gamequit" -> Log.w("Socket", "🎮 Game quit signal received.")
                    "planendwarning" -> Log.w("Socket", "Plan ending soon.")
                    is String -> if ("serverID" in data) {
                        val json = JSONObject(data)
                        Log.d("Socket", "🖥 Server ID: ${json.optString("serverID")}")
                    }
                }
            }
            globalInstance.socket.on("status") { args ->
                Log.d("Socket", " VM status: ${args.getOrNull(0)}")
                if (args.getOrNull(0) == "starting") {
                    if(!vmStartingFlag){
                        CoroutineScope(Dispatchers.Main).launch {
                            onSocketStatusChanged(VMStatus.Starting)
                        }
                        vmStartingFlag = true
                    }
                    updateLoadingData("Your PC is starting \n This may take upto ")
                }

            }
            globalInstance.socket.on("gamequit") {
                Log.w("Socket", "Game quit.")
            }
            globalInstance.socket.on("reset") {
                Log.w("Socket", "Stream reset by server.")
                stopSocketConnection(globalInstance.socket, "reset")
            }

            globalInstance.socket.connect()
        }
        catch (e: URISyntaxException) {
            Log.e("Socket", "Invalid URI", e)
        }
    }


fun updateLoadingData(name: String) {
    loadingData = name
    subLoadingData?.invoke(name)
}

    fun getVMStatus(@Header("Authorization") token : String) {
        job?.cancel()
        job = viewModelScope.launch(Dispatchers.IO) {
            checkVMConnectionStatusLogic(token).onEach { result ->
                when (result) {
                    is Resource.Loading -> {
                        _checkVMStatus.value = CheckVMStatusState(isLoading = true)
                    }
                    is Resource.Success -> {
                        _checkVMStatus.value = checkVMStatus.value.copy(
                            isLoading = false,
                            connected = "",
                            success = 1)
                    }
                    is Resource.Error -> {
                        delay(200)
                        _checkVMStatus.value = CheckVMStatusState(error = result.message.toString(), success = 0, errorCode = result.errorCode!!, isLoading = false)
                    }
                }
            }.launchIn(viewModelScope)
        }
    }

    fun getCheckVMStatus(@Header("Authorization") token : String) {
        job?.cancel()
        job = viewModelScope.launch(Dispatchers.IO) {
            vmStatusLogic(token).onEach { result ->
                when (result) {
                    is Resource.Loading -> {
                        _getVMStatus.value = VMStatusState(isLoading = true)
                    }
                    is Resource.Success -> {
                        _getVMStatus.value = getVMStatus.value.copy(
                            isLoading = false,
                            status = result.message,
                            success = 1)
                    }
                    is Resource.Error -> {
                        delay(200)
                        _getVMStatus.value = VMStatusState(error = result.message.toString(), success = 0, errorCode = result.errorCode!!, isLoading = false)
                    }
                }
            }.launchIn(viewModelScope)
        }
    }

    fun getVmip(@Header("Authorization") token : String) {
        job?.cancel()
        job = viewModelScope.launch(Dispatchers.IO) {
            getVmipLogic(token).onEach { result ->
                when (result) {
                    is Resource.Loading -> {
                        _getVMIPState.value = GetVMIPState(isLoading = true)
                    }
                    is Resource.Success -> {
                        _getVMIPState.value = getVMIPState.value.copy(
                            isLoading = false,
                            message = result.message,
                            vmIp = result.token!!,
                            success = 1)
                    }
                    is Resource.Error -> {
                        delay(200)
                        _getVMIPState.value = GetVMIPState(error = result.message.toString(), success = 0, errorCode = result.errorCode!!, isLoading = false)
                    }
                }
            }.launchIn(viewModelScope)
        }
    }

    fun startTimer():Boolean {
        val totalTime = 6 * 60 * 1000L // 6 minutes in milliseconds

        countDownTimer = object : CountDownTimer(totalTime, 1000) {
            @SuppressLint("DefaultLocale")
            override fun onTick(millisUntilFinished: Long) {
                val minutes = (millisUntilFinished / 1000) / 60
                val seconds = (millisUntilFinished / 1000) % 60
                _timeLeft.value = String.format("%02d:%02d", minutes, seconds)
            }

            override fun onFinish() {
                _timeLeft.value = "00:00"
            }
        }.start()
        return  true
    }

    fun stopTimer() {
        countDownTimer?.cancel()
    }

    fun startDisconnectTimer():Boolean {
        val totalTime =  60 * 1000L

        countDownTimerDisconn = object : CountDownTimer(totalTime, 1000) {
            @SuppressLint("DefaultLocale")
            override fun onTick(millisUntilFinished: Long) {
                val minutes = (millisUntilFinished / 1000) / 60
                val seconds = (millisUntilFinished / 1000) % 60
                _disConnTimeLeft.value = String.format("%02d:%02d", minutes, seconds)
            }

            override fun onFinish() {
                _disConnTimeLeft.value = "00:00"
            }
        }.start()
        return  true
    }

    fun stopDisconnectTimer() {
        countDownTimerDisconn?.cancel()
    }

    fun onSocketStatusChanged(newStatus: VMStatus) {
        _status.value = newStatus
        when (newStatus) {
            VMStatus.Starting  -> startTimer()
            else  -> stopTimer()
        }
    }

    fun closeStream() {
        GlobalData.getInstance().socket.emit("closestream")
    }

    fun onBackPressed() {
       GlobalData.getInstance().socket.disconnect()
    }

data class AddComputerUiState(
    val loading: Boolean = false,
    val showDialog: Int? = null,
    val showToast: Int? = null)
}

