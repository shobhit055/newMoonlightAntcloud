package com.antcloud.app.data


data class LoginState(
    var isLoading: Boolean = false,
    var success: Int = -1,
    var userData: User? = null,
    var error: String = "",
    var errorCode: Int = -1)

data class ForgotState(
    var isLoading: Boolean = false,
    var success: Int = -1,
    var message: String = "",
    var error: String = "",
    var errorCode: Int = -1)

data class PinVerifyState(
    var isLoading: Boolean = false,
    var success: Int = -1,
    var message: String? = null,
    var error: String = "",
    var errorCode: Int = -1)

data class CheckUserState(
    var isLoading: Boolean = false,
    var success: Int = -1,
    var userData: User? = null,
    var token: String? = "",
    var refreshToken: String= "",
    var error: String = "",
    var errorCode: Int = -1)

data class RefreshTokenState(
    var isLoading: Boolean = false,
    var success: Int = -1,
    var message:String = "",
    var accessToken : String= "",
    var refreshToken : String = "",
    var error: String = "",
    var errorCode: Int = -1)

data class UpdatePhoneState(
    var isLoading: Boolean = false,
    var success: Int = -1,
    var message:String = "",
    var phone : String= "",
    var error: String = "",
    var errorCode: Int = -1)

data class UpdateResolutionState(
    var isLoading: Boolean = false,
    var success: Int = -1,
    var message:String = "",
    var resolution : String= "",
    var error: String = "",
    var errorCode: Int = -1)

data class UpdateLocationState(
    var isLoading: Boolean = false,
    var success: Int = -1,
    var message:String = "",
    var error: String = "",
    var errorCode: Int = -1)

data class ResendVerificationEmailState(
    var isLoading: Boolean = false,
    var success: Int = -1,
    var message:String = "",
    var error: String = "",
    var errorCode: Int = -1)

data class LogoutState(
    var isLoading: Boolean = false,
    var success: Int = -1,
    var message:String = "",
    var error: String = "",
    var errorCode: Int = -1)

data class CreatePricingOrderState(
    var isLoading: Boolean = false,
    var success: Int = -1,
    var pricingRespDataNotes: PricingRespNotes? = null,
    var id: String = "",
    var error: String = "",
    var errorCode: Int = -1)

data class VerifyCouponCodeState(
    var isLoading: Boolean = false,
    var success: Int = -1,
    var message: String? = null,
    var error: String = "",
    var errorCode: Int = -1)

data class CheckPaymentState(
    var isLoading: Boolean = false,
    var success: Int = -1,
    var message: String? = null,
    var error: String = "",
    var errorCode: Int = -1)

data class CheckForSaleState(
    var isLoading: Boolean = false,
    var success: Int = -1,
    var message: String? = null,
    var error: String = "",
    var errorCode: Int = -1)

data class AddToWaitListState(
    var isLoading: Boolean = false,
    var success: Int = -1,
    var message: String? = null,
    var error: String = "",
    var errorCode: Int = -1)

data class GameState(
    var isLoading: Boolean = false,
    var success: Int = -1,
    var mobileGames: ArrayList<MobileGames>? = null,
    var error: String = "",
    var errorCode: Int = -1)

data class SupportState(
    var isLoading: Boolean = false,
    var success: Int = -1,
    var error: String = "",
    var errorCode: Int = -1)

data class VMStatusState(
    var isLoading: Boolean = false,
    var success: Int = -1,
    var status : String? ="",
    var error: String = "",
    var errorCode: Int = -1)

data class CheckVMStatusState(
    var isLoading: Boolean = false,
    var success: Int = -1,
    var connected : String ="",
    var error: String = "",
    var errorCode: Int = -1)

data class GetVMIPState(
    var isLoading: Boolean = false,
    var success: Int = -1,
    var message: String? = null,
    var vmIp: String = "",
    var error: String = "",
    var errorCode: Int = -1)