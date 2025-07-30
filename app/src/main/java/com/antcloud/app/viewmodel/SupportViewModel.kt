package com.antcloud.app.viewmodel


import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antcloud.app.logic.support.SupportLogic
import com.antcloud.app.common.Resource
import com.antcloud.app.data.PostSupportReq
import com.antcloud.app.data.ReportData
import com.antcloud.app.data.SupportCard
import com.antcloud.app.data.SupportState

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
import javax.inject.Inject


@HiltViewModel
class SupportViewModel @Inject constructor(private val supportLogic: SupportLogic,
                                           @ApplicationContext val context: Context): ViewModel() {
    internal var cardsList: List<SupportCard>? = null
    internal var selectedState: Int = 0
    internal var subSelectedState: ((Int) -> Unit)? = null
    var reportIssues = ReportData(latency = false,gameControls = false,gameLibrary = false,wifi = false,data = false,feedback = "")
    var selectedIssueState = listOf("")
    var feedbackState: String = ""
    var subSelectedIssueState: ((List<String>) -> Unit)? = null
    var subFeedbackState: ((String) -> Unit)? = null
    var subReportSubmit: ((Int) -> Unit)? = null
    var subInternetError: ((Boolean) -> Unit)? = null
    var subReportError: ((Boolean) -> Unit)? = null

    fun initializeCardsList(data: List<SupportCard>) {
        cardsList = data
        selectedState = 0
        subSelectedState?.invoke(selectedState)
    }

    fun updateFeedbackState(feedback: String) {
        feedbackState = feedback
        reportIssues.feedback = feedback
        subFeedbackState?.invoke(feedback)
    }

    fun updateIssueState(issue: String) {
        val newList = ArrayList(selectedIssueState.map { it })
        if (selectedIssueState.contains(issue)) {
            newList.remove(issue)
            when(issue) {
                "data" -> reportIssues.data = false
                "wifi" -> reportIssues.wifi = false
                "latency" -> reportIssues.latency = false
                "gameLibrary" -> reportIssues.gameLibrary = false
                "gameControls" -> reportIssues.gameControls = false
            }
        }
        else if ((reportIssues.data && issue == "wifi") || (reportIssues.wifi && issue == "data")) {
            //do nothing
        }
        else {
            newList.add(issue)
            when(issue) {
                "data" -> reportIssues.data = true
                "wifi" -> reportIssues.wifi = true
                "latency" -> reportIssues.latency = true
                "gameLibrary" -> reportIssues.gameLibrary = true
                "gameControls" -> reportIssues.gameControls = true
            }
        }
        selectedIssueState = newList.toList()
        subSelectedIssueState?.invoke(selectedIssueState)
    }

    fun updateSelectedState(selected: Int) {
        selectedState = selected
        subSelectedState?.invoke(selected)
    }

    fun initializeState() {
        subFeedbackState?.invoke(feedbackState)
        subSelectedIssueState?.invoke(selectedIssueState)
    }

    private var job: Job? = null
    private val _supportState = mutableStateOf(SupportState())
    val supportState: State<SupportState> = _supportState

    @SuppressLint("SuspiciousIndentation")
    fun getSupportData(@Header("Authorization") token : String, @Body body: PostSupportReq) {
        job?.cancel()
        job = viewModelScope.launch(Dispatchers.IO) {
            supportLogic(token,body).onEach { result ->
                when (result) {
                    is Resource.Loading -> {
                        _supportState.value = SupportState(isLoading = true)
                    }
                    is Resource.Success -> {
                        _supportState.value = supportState.value.copy(
                            isLoading = false,
                            success = 1
                        )
                    }
                    is Resource.Error -> {
                        delay(200)
                        _supportState.value = SupportState(
                            error = result.message!!.toString(),
                            success = 0,
                            errorCode = result.errorCode!!,
                            isLoading = false)
                    }
                }
            }.launchIn(viewModelScope)
        }
    }
}