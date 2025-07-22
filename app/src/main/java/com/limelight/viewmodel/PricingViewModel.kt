package com.limelight.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.limelight.logic.pricing.CreatePricingOrderLogic
import com.limelight.common.Resource
import com.limelight.data.AddToWaitListState
import com.limelight.data.CheckForSaleState
import com.limelight.data.CheckPaymentState
import com.limelight.data.CreatePricingOrderState
import com.limelight.data.ForgotPasswordReq
import com.limelight.data.PricingGroups
import com.limelight.data.PricingReq
import com.limelight.data.SalesDetails
import com.limelight.data.UpdateLocationReq
import com.limelight.data.UpdateLocationState
import com.limelight.data.VerifyCouponCodeState
import com.limelight.logic.pricing.AddToWaitListLogic
import com.limelight.logic.pricing.CheckPaymentAllowLogic
import com.limelight.logic.pricing.CheckSaleLogic
import com.limelight.logic.pricing.UpdateLocationLogic
import com.limelight.logic.pricing.VerifyCouponCodeLogic
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
import retrofit2.http.Query
import javax.inject.Inject

@HiltViewModel
class PricingViewModel @Inject constructor(private val createPricingOrderLogic: CreatePricingOrderLogic,
                                           private val verifyCouponCodeLogic : VerifyCouponCodeLogic,
                                           private val checkPaymentAllowedLogic : CheckPaymentAllowLogic,
                                           private val updateLocationLogic: UpdateLocationLogic,
                                           private val addToWaitListLogic : AddToWaitListLogic,
                                           private val checkSaleLogic : CheckSaleLogic,
                                           @ApplicationContext val context: Context): ViewModel() {

    private var job: Job? = null
    private val _createPricingOrderState = mutableStateOf(CreatePricingOrderState())
    val createPricingOrderState: State<CreatePricingOrderState> = _createPricingOrderState
    private val _verifyCouponCodeState = mutableStateOf(VerifyCouponCodeState())
    val verifyCouponCodeState: State<VerifyCouponCodeState> = _verifyCouponCodeState
    private val _checkPaymentAllowedState = mutableStateOf(CheckPaymentState())
    val checkPaymentAllowedState: State<CheckPaymentState> = _checkPaymentAllowedState
    private val _updateLocationState = mutableStateOf(UpdateLocationState())
    val updateLocationState: State<UpdateLocationState> = _updateLocationState
    private val _addToWaitListState = mutableStateOf(AddToWaitListState())
    val addToWaitListState: State<AddToWaitListState> = _addToWaitListState

    private val _checkForSaleState = mutableStateOf(CheckForSaleState())
    val checkForSaleState: State<CheckForSaleState> = _checkForSaleState
    var pricingData: List<PricingGroups>? = null
    var idToken: String = ""
    var subIdToken: ((String) -> Unit)? = null
    private var selectedTabState: Long? = 0
    private var pageState: Int = 0
    var paymentsAllowed : Boolean = false
    var subPaymentAllowed: ((Boolean) -> Unit)? = null
    var openLoadingDialogState: Boolean = false
    var subOpenLoadingDialogState: ((Boolean) -> Unit)? = null
    var couponState: String = ""
    var subCouponState: ((String) -> Unit)? = null
    var showCouponError: Boolean = false
    var subShowCouponError: ((Boolean) -> Unit)? = null
    var selectedPlan: String = ""
    var bundleSelected: Boolean = false
    var selectedQuantity: Int = 1
    var appliedCoupon: String = ""
    var messageState: Boolean = false
    var subMessageState: ((Boolean) -> Unit)? = null
    var loadingState: Boolean = false
    var subLoadingState: ((Boolean) -> Unit)? = null
    var subSelectedTabState: ((Long?) -> Unit)? = null
    var subPageState: ((Int) -> Unit)? = null
    var stateLocation: String = ""
    var subStateLocation: ((String) -> Unit)? = null
    var pinCodeState: String = ""
    var subPinCodeState: ((String) -> Unit)? = null
    var showWaitList: Boolean = false
    var subShowWaitList: ((Boolean) -> Unit)? = null
    var showIntroWarning: Boolean = false
    var subShowIntroWarning: ((Boolean) -> Unit)? = null
    var showAdvWarning: Boolean = false
    var subShowAdvWarning: ((Boolean) -> Unit)? = null

    var showSuperWarning: Boolean = false
    var subShowSuperWarning: ((Boolean) -> Unit)? = null
    var saleDetails : SalesDetails = SalesDetails()
    var subSaleDetails: ((SalesDetails) -> Unit)? = null


    fun initializePricingState(data: List<PricingGroups>) {
        pricingData = data
        selectedTabState = 2 //show Advanced plans by default
        subSelectedTabState?.invoke(selectedTabState)
    }

    fun updatePaymentsAllowedState(res: Boolean) {
        paymentsAllowed = res
        subPaymentAllowed?.invoke(res)
    }

    fun updateIdToken(token: String) {
        //Log.d("pricing", "updating token")
        idToken = token
        subIdToken?.invoke(token)
    }

    fun updateTabState(tabId: Long?) {
        selectedTabState = tabId
        subSelectedTabState?.invoke(tabId)
    }

    fun updatePageState(pageState: Int) {
        this.pageState = pageState
        subPageState?.invoke(pageState)
    }

    fun updateLoadingState(load: Boolean) {
        loadingState = load
        subLoadingState?.invoke(load)
    }

    fun updateMessageState(msg: Boolean) {
        messageState = msg
        subMessageState?.invoke(msg)
        updateShowCouponError(!msg)
    }

    fun updateCouponState(coupon: String) {
        couponState = coupon
        subCouponState?.invoke(coupon)
    }

    fun updateShowCouponError(showCouponError: Boolean) {
        this.showCouponError = showCouponError
        subShowCouponError?.invoke(showCouponError)
    }

    fun updateStateLocation(name: String) {
        stateLocation = name
        subStateLocation?.invoke(name)
    }

    fun updatePinCodeState(name: String) {
        pinCodeState = name
        subPinCodeState?.invoke(name)
    }

    fun updateShowWaitList(res: Boolean) {
        showWaitList = res
        subShowWaitList?.invoke(res)
    }

    fun updateShowIntroWarning(res: Boolean) {
        showIntroWarning = res
        subShowIntroWarning?.invoke(res)
    }
    fun updateShowAdvWarning(res: Boolean) {
        showAdvWarning = res
        subShowAdvWarning?.invoke(res)
    }

    fun updateShowSuperWarning(res: Boolean) {
        showSuperWarning = res
        subShowSuperWarning?.invoke(res)
    }
    fun updateSalesDetails(res: SalesDetails) {
        saleDetails = res
        subSaleDetails?.invoke(res)
    }

    @SuppressLint("SuspiciousIndentation")
    fun getCreatePricingOrderData(@Header("Authorization") token : String, @Body body: PricingReq) {
        job?.cancel()
        job = viewModelScope.launch(Dispatchers.IO) {
            createPricingOrderLogic(token,body).onEach { result ->
                when (result) {
                    is Resource.Loading -> {
                        _createPricingOrderState.value = CreatePricingOrderState(isLoading = true)
                    }
                    is Resource.Success -> {
                        _createPricingOrderState.value = createPricingOrderState.value.copy(
                            isLoading = false,
                            pricingRespDataNotes = result.data?.body()?.notes,
                            id = result.data?.body()?.id!!,
                            success = 1)
                    }
                    is Resource.Error -> {
                        delay(200)
                        _createPricingOrderState.value = CreatePricingOrderState(
                            error = result.message!!.toString(),
                            success = 0,
                            errorCode = result.errorCode!!,
                            isLoading = false)
                    }
                }
            }.launchIn(viewModelScope)
        }
    }

    fun getVerifyCouponCodeData(@Header("Authorization") token : String, @Query("code") code: String) {
        job?.cancel()
        job = viewModelScope.launch(Dispatchers.IO) {
            verifyCouponCodeLogic(token,code).onEach { result ->
                when (result) {
                    is Resource.Loading -> {
                        _verifyCouponCodeState.value = VerifyCouponCodeState(isLoading = true)
                    }
                    is Resource.Success -> {
                        _verifyCouponCodeState.value = verifyCouponCodeState.value.copy(
                            isLoading = false,
                            success = 1)
                    }
                    is Resource.Error -> {
                        delay(200)
                        _verifyCouponCodeState.value = VerifyCouponCodeState(
                            error = result.message!!.toString(),
                            success = 0,
                            errorCode = result.errorCode!!,
                            isLoading = false)
                    }
                }
            }.launchIn(viewModelScope)
        }
    }

    fun getCheckPaymentAllowedData() {
        job?.cancel()
        job = viewModelScope.launch(Dispatchers.IO) {
            checkPaymentAllowedLogic().onEach { result ->
                when (result) {
                    is Resource.Loading -> {
                        _checkPaymentAllowedState.value = CheckPaymentState(isLoading = true)
                    }
                    is Resource.Success -> {
                        _checkPaymentAllowedState.value = checkPaymentAllowedState.value.copy(
                            message = result.data?.body()?.string(),
                            isLoading = false,
                            success = 1)
                    }
                    is Resource.Error -> {
                        delay(200)
                        _checkPaymentAllowedState.value = CheckPaymentState(
                            error = result.message!!.toString(),
                            success = 0,
                            errorCode = result.errorCode!!,
                            isLoading = false)
                    }
                }
            }.launchIn(viewModelScope)
        }
    }

    fun getCheckForSale() {
        job?.cancel()
        job = viewModelScope.launch(Dispatchers.IO) {
            checkSaleLogic().onEach { result ->
                when (result) {
                    is Resource.Loading -> {
                        _checkForSaleState.value = CheckForSaleState(isLoading = true)
                    }
                    is Resource.Success -> {
                        _checkForSaleState.value = checkForSaleState.value.copy(
                            message = result.data?.body()?.string(),
                            isLoading = false,
                            success = 1)
                    }
                    is Resource.Error -> {
                        delay(200)
                        _checkForSaleState.value = CheckForSaleState(
                            error = result.message!!.toString(),
                            success = 0,
                            errorCode = result.errorCode!!,
                            isLoading = false)
                    }
                }
            }.launchIn(viewModelScope)
        }
    }

    fun getUpdateLocationData(@Header("Authorization") token : String, @Path("id") id: String, @Body body: UpdateLocationReq) {
        job?.cancel()
        job = viewModelScope.launch(Dispatchers.IO) {
            updateLocationLogic(token,id,body).onEach { result ->
                when (result) {
                    is Resource.Loading -> {
                        _updateLocationState.value = UpdateLocationState(isLoading = true)
                    }

                    is Resource.Success -> {
                        _updateLocationState.value = updateLocationState.value.copy(isLoading = false,
                            message = result.message!!, success = 1)
                    }

                    is Resource.Error -> {
                        Log.i("test", "Error")
                        delay(200)
                        _updateLocationState.value = UpdateLocationState(
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

    fun getAddToWishListData(@Header("Authorization") token : String, @Body body: ForgotPasswordReq) {
        job?.cancel()
        job = viewModelScope.launch(Dispatchers.IO) {
            addToWaitListLogic(token,body).onEach { result ->
                when (result) {
                    is Resource.Loading -> {
                        _addToWaitListState.value = AddToWaitListState(isLoading = true)
                    }
                    is Resource.Success -> {
                        _addToWaitListState.value = addToWaitListState.value.copy(isLoading = false, success = 1)
                    }
                    is Resource.Error -> {
                        delay(200)
                        _addToWaitListState.value = AddToWaitListState(error = result.message!!.toString(), success = 0, errorCode = result.errorCode!!, isLoading = false)
                    }
                }
            }.launchIn(viewModelScope)
        }
    }


}