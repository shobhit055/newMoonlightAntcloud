package com.limelight.data

import com.google.gson.annotations.SerializedName


data class LoginRespData(@SerializedName("user") var user : User)

data class SignUpResponse(@SerializedName("doc") var doc : User, @SerializedName("message") var message: String)

data class CheckUserResponse(@SerializedName("user") var user : User, @SerializedName("token") var token: String = "")

data class RefreshUserResponse(@SerializedName("message") var message : String = "",
                               @SerializedName("accessToken") var accessToken: String = "",
                               @SerializedName("refreshToken") var refreshToken : String = "")

data class UpdateUserFieldResp(@SerializedName("message") var message : String, @SerializedName("doc") var doc : DocPhone)

data class DocPhone(@SerializedName("phone") var phone : String, @SerializedName("resolution") var resolution: String)

data class GamesResponse(@SerializedName("mobileGames") var mobileGames: ArrayList<MobileGames>)

data class PricingRespData(@SerializedName("id") var id: String = "",
                           @SerializedName("notes") var notes: PricingRespNotes = PricingRespNotes())

data class PricingRespNotes(
    @SerializedName("city") var city: String = "",
    @SerializedName("email") var email: String = "",
    @SerializedName("fullname") var fullname: String = "",
    @SerializedName("phone") var phone: String = "",
    @SerializedName("planHourLimit") var planHourLimit: Int = 0,
    @SerializedName("planName") var planName: String = "",
    @SerializedName("planPrice") var planPrice: String = "",
    @SerializedName("platform") var platform: String = "",
    @SerializedName("quantity") var quantity: Int = 0,
    @SerializedName("uid") var uid: String = "")

data class MobileGames(
//    var libraryRows: List<LibraryRows>,
    @SerializedName("games") var games: List<Game> = listOf(),
    @SerializedName("Popular") var Popular : List<Popular> = listOf())

data class RefreshUserResult(@SerializedName("code") var code: Int = 0,
                             @SerializedName("body") var body: RefreshUserResponse = RefreshUserResponse())

data class User (
    @SerializedName("id") var id: String = "",
    @SerializedName("email") var email: String = "",
    @SerializedName("firstName") var firstName: String = "",
    @SerializedName("lastName") var lastName: String = "",
    @SerializedName("token") var token: String = "",
    @SerializedName("location") var location: Location = Location(),
    @SerializedName("refreshToken") var refreshToken: String = "",
    @SerializedName("currentPlan") var currentPlan: String = "",
    @SerializedName("timeUsedMonth") var timeUsedMonth: Int = 0,
    @SerializedName("totalTimeMonth") var totalTimeMonth: Int = 0,
    @SerializedName("emailVerified") var emailVerified: Boolean = false,
    @SerializedName("phoneVerified") var phoneVerified: Boolean = false,
    @SerializedName("phone") var phone: String = "",
    @SerializedName("renewDate") var renewDate: String = "",
    @SerializedName("vmId") var vmId: String = "",
    @SerializedName("favorite") var favorite: ArrayList<String>? = arrayListOf(),
    @SerializedName("region") var region: String = "",
    @SerializedName("resolution") var resolution: String = "",
    @SerializedName("transactions") var transactions : List<TransactionHistory>?= null,
    @SerializedName("subscriptionStatus") var subscriptionStatus : String = "",
    @SerializedName("expiredPlan") var expiredPlan : String = "",
    @SerializedName("upcomingPlans") var upcomingPlans : ArrayList<UpcomingPlans> = arrayListOf())

data class UpcomingPlans(@SerializedName("planName") var planName: String)

data class TransactionHistory(@SerializedName("transactionId") var transactionId :String,
                              @SerializedName("planName") var planName : String,
                              @SerializedName("planTerm") var planTerm : String,
                              @SerializedName("purchaseDate") var purchaseDate : String,
                              @SerializedName("id") var id : String)

data class Properties(@SerializedName("property") var property :String = "")

data class Genre(@SerializedName("genre") var genre :String = "")

data class Popular(@SerializedName("popular") var popular :String = "")

data class AppMessage(@SerializedName("showMessage") var showMessage: Boolean, @SerializedName("messageText") var messageText: String)

class Service(@SerializedName("name") val name: String = "", @SerializedName("code") val code: String = "", @SerializedName("message") val message: String = "")

data class VMStatusResp(@SerializedName("Status") var Status :String = "")

data class GetVmipResp(@SerializedName("message") var message :String = "" , @SerializedName("vmip") var vmip :String = "")
data class CheckConnectionStatusResp(@SerializedName("message") var connected :Boolean? = false)

data class PinVerifyResponse(@SerializedName("message") var message :String , @SerializedName("data") var verifyData : PinVerifyData)

data class PinVerifyData(@SerializedName("status") var status : String)

