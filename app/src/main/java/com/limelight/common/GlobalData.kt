package com.limelight.common

import com.limelight.data.AppMessage
import com.limelight.data.DocumentListData
import com.limelight.data.FAQCard
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
    var remoteAppMessage : List<AppMessage> = listOf()
    var imageLoading : Boolean = false
    var paymentStatus : Boolean = false
    var paymentPcTimerMins : Int = 1
    var paymentPcTimerSecs : Int = 60
    var toolbarInvisible : Boolean = false
    var openSupportScreen : Boolean = false
    var gemsHistoryFlag : Int = 0
    var vmIP : String = ""
    lateinit  var socket: Socket

    var purchasePlan : Boolean = false

    companion object {
        private var dataInstance: GlobalData = GlobalData()
        fun getInstance(): GlobalData = dataInstance
    }
}