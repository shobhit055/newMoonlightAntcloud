package com.limelight.common

import android.app.Application
import android.util.Log
import com.limelight.R
import com.limelight.data.AppMessage
import com.limelight.data.FAQCard
import com.limelight.data.PolicyListResp
import com.limelight.data.PricingGroups
import com.limelight.data.QualityResp
import com.limelight.data.SupportCard
import com.limelight.data.TermsResp
import com.limelight.data.VideoTutorial
import com.google.firebase.FirebaseApp
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ConfigUpdate
import com.google.firebase.remoteconfig.ConfigUpdateListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigException
import com.google.firebase.remoteconfig.get
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.google.gson.Gson
import com.limelight.data.GamesMaintenance
import dagger.hilt.android.HiltAndroidApp


@HiltAndroidApp
class MyApplication: Application() {
    private val globalInstance = GlobalData.getInstance()
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(applicationContext)
        sApplication = this
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3600
        }

        getRemoteConfig().setConfigSettingsAsync(configSettings)
        getRemoteConfig().setDefaultsAsync(R.xml.remote_config_ant)
        getRemoteConfig().all.keys.forEach { key ->
            mapRemoteData(key)
            globalInstance.androidData.policy = globalInstance.remoteDataPolicy
            globalInstance.androidData.terms = globalInstance.remoteDataTerms
        }
        getRemoteConfig().addOnConfigUpdateListener(object : ConfigUpdateListener {
            override fun onUpdate(configUpdate: ConfigUpdate) {
                getRemoteConfig().activate().addOnCompleteListener {
                    configUpdate.updatedKeys.forEach { key ->
                        mapRemoteData(key)
                    }
                }
            }
            override fun onError(error: FirebaseRemoteConfigException) {
            }
        })
    }

    companion object{
        private var sApplication: Application? = MyApplication()
        fun getApplication(): Application? {
            return sApplication
        }
        fun getRemoteConfig(): FirebaseRemoteConfig {
            return Firebase.remoteConfig
        }
        fun mapData(key: String) {
            return MyApplication().mapRemoteData(key)
        }
    }

    fun mapRemoteData(key: String){
        val dataJson = getRemoteConfig()[key].asString()
        if(dataJson.isNotEmpty()) {
            if(key.contains("appMessage")) {
                globalInstance.remoteAppMessage = listOf()
                val appMessage: Array<AppMessage> = Gson().fromJson(dataJson, Array<AppMessage>::class.java)
                Log.d("webView", "remote App Message $appMessage")
                for (message in appMessage) {
                    globalInstance.remoteAppMessage += listOf(message)
                }
            }

            if(key.contains("faq")) {
                globalInstance.remoteDataFaq = listOf()
                val faqArray: Array<FAQCard> = Gson().fromJson(dataJson, Array<FAQCard>::class.java)
                //Log.d("webView", "remote App Message, $faqArray")
                for (faqData in faqArray) {
                    globalInstance.remoteDataFaq += listOf(faqData)
                }
            }
            if(key.contains("policy")) {
                globalInstance.remoteDataPolicy = listOf()
                val policyArray: Array<PolicyListResp> = Gson().fromJson(dataJson, Array<PolicyListResp>::class.java)
                for (policyCard in policyArray) {
                    globalInstance.remoteDataPolicy += listOf(policyCard)
                }
            }
            if(key.contains("pricing")) {
                globalInstance.remoteDataPricing = listOf()
                val pricingArray: Array<PricingGroups> = Gson().fromJson(dataJson, Array<PricingGroups>::class.java)
                for (pricingData in pricingArray) {
                    globalInstance.remoteDataPricing += listOf(pricingData)
                }
            }
            if(key.contains("resolution")) {
                globalInstance.remoteDataResolution = listOf()
                val resolutionArray: Array<QualityResp> = Gson().fromJson(dataJson, Array<QualityResp>::class.java)
                for (resolutionData in resolutionArray) {
                    globalInstance.remoteDataResolution += listOf(resolutionData)
                }
            }
            if(key.contains("support")) {
                globalInstance.remoteDataSupport = listOf()
                val supportArray: Array<SupportCard> = Gson().fromJson(dataJson, Array<SupportCard>::class.java)
                for (supportData in supportArray) {
                    globalInstance.remoteDataSupport += listOf(supportData)
                }
            }
            if(key.contains("terms")) {
                globalInstance.remoteDataTerms = listOf()
                val termsArray: Array<TermsResp> = Gson().fromJson(dataJson, Array<TermsResp>::class.java)
                for (termsData  in termsArray) {
                    globalInstance.remoteDataTerms += listOf(termsData)
                }
            }
            if(key.contains("tutorial")) {
                globalInstance.remoteDataTutorial = listOf()
                val tutorialArray: Array<VideoTutorial> = Gson().fromJson(dataJson, Array<VideoTutorial>::class.java)
                for (tutorialData  in tutorialArray) {
                    globalInstance.remoteDataTutorial += listOf(tutorialData)
                }
            }
            if(key.contains("disableSwitchToIntroPlans")) {
                globalInstance.remoteDisableSwitchToIntroPlans = false
                val disableSwitchToIntroPlans: Boolean = Gson().fromJson(dataJson, Boolean::class.java)
                globalInstance.remoteDisableSwitchToIntroPlans = disableSwitchToIntroPlans
            }
            if(key.contains("disableSwitchToAdvPlans")) {
                globalInstance.remoteDisableSwitchToAdvPlans = false
                val disableSwitchToAdvPlans: Boolean = Gson().fromJson(dataJson, Boolean::class.java)
                globalInstance.remoteDisableSwitchToAdvPlans = disableSwitchToAdvPlans
            }
            if(key.contains("disableSwitchToSuperPlans")) {
                globalInstance.remoteDisableSwitchToSuperPlans = false
                val disableSwitchToSuperPlans: Boolean = Gson().fromJson(dataJson, Boolean::class.java)
                globalInstance.remoteDisableSwitchToSuperPlans = disableSwitchToSuperPlans
            }
            if(key.contains("introOldUsersAllowed")) {
                globalInstance.remoteIntroOldUsersAllowed = false
                val introOldUsersAllowed: Boolean = Gson().fromJson(dataJson, Boolean::class.java)
                globalInstance.remoteIntroOldUsersAllowed = introOldUsersAllowed
            }
            if(key.contains("advOldUsersAllowed")) {
                globalInstance.remoteAdvOldUsersAllowed = false
                val advOldUsersAllowed: Boolean = Gson().fromJson(dataJson, Boolean::class.java)
                globalInstance.remoteAdvOldUsersAllowed = advOldUsersAllowed
            }
            if(key.contains("superOldUsersAllowed")) {
                globalInstance.remoteSuperOldUsersAllowed = false
                val superOldUsersAllowed: Boolean = Gson().fromJson(dataJson, Boolean::class.java)
                globalInstance.remoteSuperOldUsersAllowed = superOldUsersAllowed
            }

            if(key.contains("playVersion")) {
                globalInstance.remotePlayVersion = 0.0
                val play: Double = Gson().fromJson(dataJson, Double::class.java)
                globalInstance.remotePlayVersion = play
                //Log.d("test", "play remote version fetched ${globalInstance.remotePlayVersion}")
            }
            if(key.contains("showIntroPlans")) {
                globalInstance.remoteShowIntroPlans = false
                val showIntroPlans: Boolean = Gson().fromJson(dataJson, Boolean::class.java)
                globalInstance.remoteShowIntroPlans = showIntroPlans
            }
            if(key.contains("showAdvPlans")) {
                globalInstance.remoteShowAdvPlans = false
                val showAdvPlans: Boolean = Gson().fromJson(dataJson, Boolean::class.java)
                globalInstance.remoteShowAdvPlans = showAdvPlans
            }
            if(key.contains("showSuperPlans")) {
                globalInstance.remoteShowSuperPlans = false
                val showSuperPlans: Boolean = Gson().fromJson(dataJson, Boolean::class.java)
                globalInstance.remoteShowSuperPlans = showSuperPlans
            }
            if(key.contains("gamesMaintenance")) {
                globalInstance.remoteGamesMaintenance = listOf()
                val gamesMaintenance: Array<GamesMaintenance> = Gson().fromJson(dataJson, Array<GamesMaintenance>::class.java)
                for (gamesMaintenanceData  in gamesMaintenance) {
                    globalInstance.remoteGamesMaintenance += listOf(gamesMaintenanceData)
                }
            }
        }
    }
}