package  com.limelight.data

import com.google.firebase.database.Exclude
import com.google.gson.annotations.SerializedName
import com.limelight.nvstream.http.ComputerDetails



data class DocumentListData(
    @SerializedName("faqs") var faqs: List<FAQCard> = listOf(),
    @SerializedName("policy") var policy: List<PolicyListResp> = listOf(),
    @SerializedName("pricing") var pricing: List<PricingGroups> = listOf(),
    @SerializedName("quality") var quality: List<QualityResp> = listOf(),
    @SerializedName("support") var support: List<SupportCard> = listOf(),
    @SerializedName("terms") var terms: List<TermsResp> = listOf(),
    @SerializedName("tutorial") var tutorial: List<VideoTutorial>? = null,
    @SerializedName("playVersion") var playVersion: Double = 0.0)

data class SubTitle(@SerializedName("title") var title: String, @SerializedName("term") var term: String)

data class PolicyListResp(@SerializedName("title") var title: String, @SerializedName("policy") var policy: String,
                          @SerializedName("subTitle") var subTitle: List<SubTitle>? = null)

data class TermsResp(@SerializedName("title") var title: String, @SerializedName("term") var term: String,
                     @SerializedName("subTitle") var subTitle: List<SubTitle>? = null)

data class QualityResp(@SerializedName("quality") var quality: String)

data class  Features(@SerializedName("feature") var feature: String)

data class  PinVerifyRequest(@SerializedName("pin") var pin: String)

data class  Notes(@SerializedName("note") var note: String)

data class PricingGroups(@SerializedName("id") val id: String, @SerializedName("name") val name: String,
                         @SerializedName("notes") val notes: List<Notes>? = null, @SerializedName("items") val items: List<PricingCard>)

data class VideoTutorial(@SerializedName("id") val id: String, @SerializedName("name") val name: String, @SerializedName("uri") val uri: String)

data class PricingCard(
    @SerializedName("code") val code: String,
    @SerializedName("id") val id: String,
    @SerializedName("img_id") val img_id: String,
    @SerializedName("name") val name: String,
    @SerializedName("display") val display: String,
    @SerializedName("userPlan") val userPlan: String,
    @SerializedName("price") val price: Int,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("features") val features: List<Features>)

data class CheckUserInDB(@SerializedName("email") val email : String, @SerializedName("phone") val phone : String)

data class LoginReqData(@SerializedName("email") val email: String, @SerializedName("password") val password: String)

data class UserRegisterReq(@SerializedName("email") val email: String,
                           @SerializedName("password") val password: String,
                           @SerializedName("phone") val phone: String,
                           @SerializedName("firstName") val firstName:String,
                           @SerializedName("lastName") val lastName:String,
                           @SerializedName("location") val location: Location,
                           @SerializedName("source") val source:String)

data class PhoneOtpReq(@SerializedName("phone") val phone : String, @SerializedName("signup") val signup : Boolean)

data class PhoneVerifyReq(@SerializedName("phone") val phone : String,
                          @SerializedName("_verificationToken") val _verificationToken : String)

data class ForgotPasswordReq(@SerializedName("email") val email : String)

data class UpdatePhoneReq(@SerializedName("phone") val phone : String)

data class UpdateResolutionReq(@SerializedName("resolution") val resolution : String)

data class UpdateLocationReq(@SerializedName("location") val location : Location)

data class Location(@SerializedName("State") var State: String = "", @SerializedName("Pincode") var Pincode: String = "")

data class Screen(val name: String)

data class PricingReq(@SerializedName("planName") val planName: String, @SerializedName("quantity") val quantity : Int = 0,
                      @SerializedName("couponCode") val couponCode : String = "",
                      @SerializedName("platform") val platform: String = "Mobile")

data class PostSupportReq(@SerializedName("internetConnection") val internetConnection : String, @SerializedName("issue") val issue : String,
                          @SerializedName("issueRelatedTo") val issueRelatedTo : List<String>, @SerializedName("source") val source :String)

data class GameData(val id: String)

data class SupportCard(@SerializedName("id") val id: Int, @SerializedName("title") val title: String,
                       @SerializedName("content") val content: String)

data class FAQCard(@SerializedName("id") val id: Int, @SerializedName("title") val title: String,
                   @SerializedName("content") val content: String)

data class OrderHistoryData(val status: String , val description: String)

val statesList: List<String> = listOf(
    "Andaman and Nicobar Islands",
    "Andhra Pradesh",
    "Arunachal Pradesh",
    "Assam",
    "Bihar",
    "Chandigarh",
    "Chhattisgarh",
    "Dadra and Nagar Haveli and Daman and Diu",
    "Delhi",
    "Goa",
    "Gujarat",
    "Haryana",
    "Himachal Pradesh",
    "Jammu and Kashmir",
    "Jharkhand",
    "Karnataka",
    "Kerala",
    "Ladakh",
    "Lakshadweep",
    "Madhya Pradesh",
    "Maharashtra",
    "Manipur",
    "Meghalaya",
    "Mizoram",
    "Nagaland",
    "Odisha",
    "Puducherry",
    "Punjab",
    "Rajasthan",
    "Sikkim",
    "Tamil Nadu",
    "Telangana",
    "Tripura",
    "Uttar Pradesh",
    "Uttarakhand",
    "West Bengal")

data class ReportData(
    @SerializedName("latency") var latency: Boolean = false,
    @SerializedName("gameControls") var gameControls: Boolean = false,
    @SerializedName("gameLibrary") var gameLibrary: Boolean = false,
    @SerializedName("wifi") var wifi: Boolean = false,
    @SerializedName("data") var data: Boolean = false,
    @SerializedName("feedback") var feedback: String = "") {

    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "latency" to latency,
            "gameControls" to gameControls,
            "gameLibrary" to gameLibrary,
            "internetWifi" to wifi,
            "internetMobileData" to data,
            "feedback" to feedback)
    }
}

data class ProductData(var imageList: List<ImageListData>, val title: String, val description: String, val price: Int)

data class ImageListData(val title: String, val image: Int)

data class GemsData(val title: String, val description: String, var details: String)

data class AddressData(val id: Int, val name: String, val phone: String, val houseNo: String, val area: String)

data class GemsHistoryData(val id: Int, val productName: String, val price: Int ,val date : String)

data class ListModel(val gameName: String, val gameImage: Int)

data class AppViewUiState(
    val pendingPairComputer: ComputerDetails? = null,
    val computerName: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

