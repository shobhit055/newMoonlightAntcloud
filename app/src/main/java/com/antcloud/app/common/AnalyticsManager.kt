package com.antcloud.app.common




import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.logEvent



class AnalyticsManager {
    companion object {
        var firebaseAnalytics = Firebase.analytics
        fun setAnalyticsUserId(userId: String) {
            firebaseAnalytics.setUserId(userId)
        }
        fun removeAnalyticsUserId() {
            firebaseAnalytics.setUserId(null)
        }
        fun emailLoginButton() {
            firebaseAnalytics.logEvent("EmailLogin_Button") {}
        }
        fun phoneLoginButton() {
            firebaseAnalytics.logEvent("PhoneLogin_Button") {}
        }
        fun emailLoginSuccess() {
            firebaseAnalytics.logEvent("EmailLogin_Success") {}
        }
        fun phoneLoginSuccess() {
            firebaseAnalytics.logEvent("PhoneLogin_Success") {}
        }
        fun signupButton() {
            firebaseAnalytics.logEvent("Signup_Button") {}
        }
        fun signupOTPButton() {
            firebaseAnalytics.logEvent("SignupOTP_Button") {}
        }
        fun signupSuccess() {
            firebaseAnalytics.logEvent("Signup_Success") {}
        }
        fun desktopStreamButton() {
            firebaseAnalytics.logEvent("DesktopStream_Button") {}
        }
        fun libraryDetailsButton() {
            firebaseAnalytics.logEvent("LibraryDetails_Button") {}
        }
        fun libraryAccountButton() {
            firebaseAnalytics.logEvent("LibraryAccount_Button") {}
        }
        fun gameButton(gameCode:String) {
            firebaseAnalytics.logEvent("Game_Button") {
                param("gameCode", gameCode)
            }
        }
        fun gameStreamButton() {
            firebaseAnalytics.logEvent("GameStream_Button") {}
        }
        fun accountButton() {
            firebaseAnalytics.logEvent("Account_Button") {}
        }
        fun accountEditButton(edit : Boolean) {
            if(edit) {
                firebaseAnalytics.logEvent("AccountSave_Button") {}
            } else
                firebaseAnalytics.logEvent("AccountEdit_Button") {}
        }
        fun emailVerificationButton() {
            firebaseAnalytics.logEvent("EmailVerification_Button") {}
        }

        fun changeResolutionButton(resolution:String) {
            firebaseAnalytics.logEvent("ChangeResolution_Button") {
                param("resolution", resolution)
            }
        }
        fun controllerMappingButton() {
            firebaseAnalytics.logEvent("ControllerMapping_Button") {}
        }
        fun buttonMappingButton() {
            firebaseAnalytics.logEvent("ButtonMapping_Button") {}
        }
        fun transactionHistoryButton() {
            firebaseAnalytics.logEvent("TransactionHistory_Button") {}
        }
        fun loginHistoryButton() {
            firebaseAnalytics.logEvent("LoginHistory_Button") {}
        }
        fun privacyPolicyButton() {
            firebaseAnalytics.logEvent("PrivacyPolicy_Button") {}
        }
        fun faqButton() {
            firebaseAnalytics.logEvent("FAQ_Button") {}
        }
        fun tcButton() {
            firebaseAnalytics.logEvent("T&C_Button") {}
        }
        fun pricingButton() {
            firebaseAnalytics.logEvent("Pricing_Button") {}
        }
        fun pricingTabButton(name:String) {
            firebaseAnalytics.logEvent("${name}Tab_Button") {}
        }
        fun pricingPlanButton(planName: String , hrs:String) {
            firebaseAnalytics.logEvent("PricingPlan_Button") {
                param("planName", planName)
                param("hrs", hrs)
            }
        }
        fun privacyPolicyNavButton() {
            firebaseAnalytics.logEvent("PrivacyPolicyNav_Button") {}
        }
        fun faqNavButton() {
            firebaseAnalytics.logEvent("FAQNav_Button") {}
        }
        fun tcNavButton() {
            firebaseAnalytics.logEvent("T&CNav_Button") {}
        }
        fun supportNavButton() {
            firebaseAnalytics.logEvent("T&SupportNav_Button") {}
        }
        fun reportButton() {
            firebaseAnalytics.logEvent("Report_Button") {}
        }
        fun submitReportButton() {
            firebaseAnalytics.logEvent("SubmitReport_Button") {}
        }
        fun tutorialButton() {
            firebaseAnalytics.logEvent("Tutorial_Button") {}
        }
        fun pingTestButton() {
            firebaseAnalytics.logEvent("PingTest_Button") {}
        }
        fun signOutButton() {
            firebaseAnalytics.logEvent("SignOut_Button") {}
        }
        fun checkoutPlan() {
            firebaseAnalytics.logEvent("CheckOutPlan_Button") {}
        }
        fun pgResponse(response:String) {
            firebaseAnalytics.logEvent("PGResponse") {
                param("response" , response)
            }
        }
        fun paymentSuccess(planName: String, price: Double) {
            firebaseAnalytics.logEvent("PaymentSuccess") {
                param(FirebaseAnalytics.Param.ITEM_NAME, planName)
                param(FirebaseAnalytics.Param.PRICE, price)
                param(FirebaseAnalytics.Param.CURRENCY, "INR")
            }
        }
        fun paymentFailed(planName: String) {
            firebaseAnalytics.logEvent("PaymentFailed") {
                param(FirebaseAnalytics.Param.ITEM_NAME, planName)
            }
        }
    }
}
