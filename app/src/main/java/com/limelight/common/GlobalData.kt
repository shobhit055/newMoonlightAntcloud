package com.limelight.common

import com.limelight.data.AppMessage
import com.limelight.data.DocumentListData
import com.limelight.data.FAQCard
import com.limelight.data.GamesMaintenance
import com.limelight.data.MobileGames
import com.limelight.data.PolicyListResp
import com.limelight.data.PricingGroups
import com.limelight.data.ProductData
import com.limelight.data.QualityResp
import com.limelight.data.SupportCard
import com.limelight.data.TermsResp
import com.limelight.data.User
import com.limelight.data.VideoTutorial
import io.socket.client.Socket
import com.google.firebase.perf.metrics.Trace


class GlobalData {
    var accountData = User()
    var androidData : DocumentListData = DocumentListData()
    var ourGames : ArrayList<MobileGames> = arrayListOf()
    var productList : List<ProductData> = listOf()
    var libraryDetailsName : String = "Our Games"
    var gameId : String = ""
    var gameStream : Boolean = false
    var remoteDataFaq : List<FAQCard> = listOf()
    var remoteDataPolicy : List<PolicyListResp> = listOf()
    var remoteDataPricing : List<PricingGroups> = listOf()
    var remoteDataResolution : List<QualityResp> = listOf()
    var remoteDataSupport : List<SupportCard> = listOf()
    var remoteDataTerms : List<TermsResp> = listOf()
    var remoteDataTutorial : List<VideoTutorial> = listOf()
    var logoutUserApi: Boolean = false
    var couponSubmit : Boolean = false
    var joinWaitList : Boolean = false
    var paymentPlan : String = ""
    var paymentPrice : Double = 0.0
    var remotePlayVersion : Double = 0.0

    var remoteShowIntroPlans : Boolean = false
    var remoteShowAdvPlans : Boolean = false
    var remoteShowSuperPlans : Boolean = false

    var remoteDisableSwitchToIntroPlans: Boolean = false
    var remoteDisableSwitchToAdvPlans: Boolean = false
    var remoteDisableSwitchToSuperPlans: Boolean = false

    var remoteIntroOldUsersAllowed: Boolean = false
    var remoteAdvOldUsersAllowed: Boolean = false
    var remoteSuperOldUsersAllowed: Boolean = false

    var remoteAppMessage : List<AppMessage> = listOf()
    var remoteGamesMaintenance: List<GamesMaintenance> = listOf()
    var imageLoading : Boolean = false
    var paymentStatus : Boolean = false
    var paymentPcTimerMins : Int = 1
    var paymentPcTimerSecs : Int = 60
    var toolbarInvisible : Boolean = false
    var openSupportScreen : Boolean = false
    var gemsHistoryFlag : Int = 0
    var vmIP : String = ""
    lateinit  var socket: Socket

    var checkUserApi: Boolean = false
    var appStartToken: Boolean = false
    var appStartWithoutToken: Boolean = false
    var verifyOTPBtn: Boolean = false
    var signUp: Boolean = false
    var emailClickLoginBtn : Boolean = false
    var phoneClickLoginBtn : Boolean = false
    var signOutBtn : Boolean = false
    var purchasePlan : Boolean = false
    var createPricingOrder : Boolean = false
    var forgotPassword : Boolean = false
    var emailLoginApi : Boolean = false
    var phoneLoginApi : Boolean = false
    var otpSubmit : Boolean = false
    var updateResolution : Boolean = false
    var updateLocation : Boolean = false
    var postSupportData : Boolean = false
    var updateUserPhone : Boolean = false
    var userRegisterApi : Boolean = false

    lateinit var traceAppStartToken: Trace
    lateinit var traceAppStartWithoutToken: Trace
    lateinit var traceVerifyOTPBtn: Trace
    lateinit var traceEmailClickLoginBtn: Trace
    lateinit var traceSignUp: Trace
    lateinit var tracePhoneClickLoginBtn: Trace
    lateinit var traceSignOut: Trace
    lateinit var tracePurchasePlan: Trace
    lateinit var traceGameStream: Trace
    lateinit var traceImageLoading: Trace
    lateinit var traceCreatePricingOrder: Trace
    lateinit var traceForgotPassword: Trace
    lateinit var traceEmailLoginApi: Trace
    lateinit var tracePhoneLoginApi: Trace
    lateinit var traceGenerateOTPApi: Trace
    lateinit var traceOtpSubmit: Trace
    lateinit var traceUserRegisterApi: Trace
    lateinit var traceGetAllMobileData: Trace
    lateinit var traceGetAllGameData: Trace
    lateinit var traceCheckUserApi: Trace
    lateinit var traceRefreshTokenApi: Trace
    lateinit var traceLogoutUserApi: Trace
    lateinit var traceCouponSubmit: Trace
    lateinit var traceUpdateUserPhone: Trace
    lateinit var traceUpdateResolution: Trace
    lateinit var traceUpdateLocation: Trace
    lateinit var tracePostSupportData: Trace
    lateinit var traceJoinWaitList: Trace



    companion object {
        private var dataInstance: GlobalData = GlobalData()
        fun getInstance(): GlobalData = dataInstance
    }
}