package com.limelight.data

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

import com.google.gson.annotations.SerializedName

data class AccountData(
    var plan: String = "",
    var region: String = "",
    var res: String = "",
    var phone: String = "",
    var email: String = "",
    var firstName: String = "",
    var lastName: String = "",
    var renewDate: String = "",
    var timePlayedMonth: Int = 0,
    var totalTimeMonth: Int = 0,
    var favorite: ArrayList<String> = arrayListOf())


@IgnoreExtraProperties
data class UserSignUp(
    @SerializedName("firstName") val firstName: String = "",
    @SerializedName("lastName") val lastName: String = "",
    @SerializedName("email") val email: String = "",
    @SerializedName("phone") val phone: String = "",
    @SerializedName("location") val location: Location = Location(),
    @SerializedName("source") val source: String = "android",
    /*val parameters: Map<String, String> = mapOf(
        Pair("plBtn", "F1"),
        Pair("vrBtn", "F9"),
        Pair("fsBtn", "F10"),
        Pair("muBtn", "PageUp"),
        Pair("mdBtn", "PageDown")
    )*/
) {
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "firstName" to firstName,
            "lastName" to lastName,
            "email" to email,
            "phone" to phone,
            "source" to source
            //"parameters" to parameters
        )
    }
}
