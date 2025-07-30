package com.antcloud.app.common

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.core.view.WindowCompat
import com.antcloud.app.theme.BlueGradient
import com.antcloud.app.theme.PinkGradient
import com.antcloud.app.components.makeToast
import com.antcloud.app.data.User
import com.antcloud.app.R



import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.antcloud.app.activity.LoginActivity
import com.antcloud.app.activity.NavActivity
import com.antcloud.app.activity.SignupActivity
import com.antcloud.app.activity.SplashActivity
import java.io.File
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec


class AppUtils {
    companion object {
        const val ENTER_PASSWORD = "Please enter Password."
        const val ENTER_EMAIL_ID = "Please enter Email Id."
        val gradientColors = listOf(PinkGradient, BlueGradient)
        val discordUrl = "https://discord.gg/fZRwkKwZQB"
        val instaUrl = "https://www.instagram.com/antcloudco"
        private var encryptedPairData: Pair<ByteArray, ByteArray>? = null
        private var globalInstance = GlobalData.getInstance()

        private fun encryptWithKeyStore(plainText: String): Pair<ByteArray, ByteArray>? {
            encryptedPairData = getEncryptedDataPair(plainText)
            return encryptedPairData
        }

        @Composable
        fun hideStatusBar(activity: Activity) {
            WindowCompat.setDecorFitsSystemWindows(activity.window, false)
            val systemUiController = rememberSystemUiController()
            LaunchedEffect(Unit) {
                systemUiController.apply {
                    isStatusBarVisible = false
                    isNavigationBarVisible = false
                }
            }
        }

        fun saveUserToken(activity: Activity, fileName: String, token: String) {
            val fos = activity.openFileOutput(fileName, Context.MODE_PRIVATE)
            val data = encryptWithKeyStore(token)
            if(data == null) {
                activity.makeToast("Error 901: Something Went Wrong.")
                //signOut(activity)
                return
            }
            fos.write(data.first.size)
            fos.write(data.first, 0, data.first.size)
            fos.write(data.second, 0, data.second.size)
            fos.close()
        }

        fun clearCheck(activity : Activity) {
            val file = File(activity.filesDir, "file.nk")
            if(file.exists()) {
                file.delete()
            }
            val file2 = File(activity.filesDir, "file.lt")
            if(file2.exists()) {
                file2.delete()
            }
        }

        @SuppressLint("NewApi")
        fun getKeyGenerator() {
            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            val keyGeneratorSpec = KeyGenParameterSpec.Builder("myKey",
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .setUserAuthenticationRequired(false)
                .build()
            keyGenerator.init(keyGeneratorSpec)
            keyGenerator.generateKey()
        }

        fun getKey(): SecretKey?{
            return try{
                val keyStore = KeyStore.getInstance("AndroidKeyStore")
                keyStore.load(null)
                val secretKeyEntry =
                    keyStore.getEntry("myKey", null) as KeyStore.SecretKeyEntry
                secretKeyEntry.secretKey
            } catch (e:NullPointerException){
                null
            }
        }

        private fun getEncryptedDataPair(data: String): Pair<ByteArray, ByteArray>? {
            val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
            val key = getKey() ?: return null
            cipher.init(Cipher.ENCRYPT_MODE, key)
            val iv: ByteArray = cipher.iv
            val encryptedData = cipher.doFinal(data.toByteArray(Charsets.UTF_8))
            return Pair(iv, encryptedData)
        }



        fun decryptData(iv: ByteArray, encData: ByteArray): String {
            val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
            val keySpec = IvParameterSpec(iv)
            val key = getKey() ?: return ""
            cipher.init(Cipher.DECRYPT_MODE, key, keySpec)
            return cipher.doFinal(encData).toString(Charsets.UTF_8)
        }

        fun navigateScreen(activity: Activity, navigateActivity: Class<out Activity>){
            activity.startActivity(Intent(activity, navigateActivity))
        }

        fun navigateLoginScreen(activity: Activity, emailMobile: String, type: String) {
            val intent = Intent(activity, LoginActivity::class.java)
            intent.putExtra("email", emailMobile)
            intent.putExtra("type", type)
            intent.setFlags(FLAG_ACTIVITY_CLEAR_TOP)
            activity.startActivity(intent)
            activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        fun navigateSignupScreen(activity: Activity, emailMobile: String) {
            val intent = Intent(activity, SignupActivity::class.java)
            intent.putExtra("email", emailMobile)
            intent.setFlags(FLAG_ACTIVITY_CLEAR_TOP)
            activity.startActivity(intent)
            activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            activity.finish()
        }

        fun navigateSplashActivity(activity: Activity) {
            val intent = Intent(activity, SplashActivity::class.java)
            intent.putExtra("flag",1)
            activity.startActivity(intent)
            activity.finishAffinity()
        }

        fun navigateNavScreen(activity: Activity, userData: User) {
            GlobalData.getInstance().accountData = userData
            saveUserToken(activity, "file.nk", userData.token)
            saveUserToken(activity, "file.lt", userData.refreshToken)
            activity.makeToast("Login successful")
            activity.startActivity(Intent(activity, NavActivity::class.java))
            activity.finishAffinity()
        }

        fun saveRefreshTokenData(activity: Activity, accessToken: String, refreshToken: String) {
            saveUserToken(activity, "file.nk",  accessToken)
            saveUserToken(activity, "file.lt", refreshToken)
            GlobalData.getInstance().accountData.token = accessToken
            GlobalData.getInstance().accountData.refreshToken = refreshToken
        }

//        fun setBitrate(resolution : String , viewModel : AppViewModel){
//            if(resolution=="360p")
//                viewModel.updateBitrate(2000f)
//            else if(resolution=="480p")
//                viewModel.updateBitrate(4000f)
//            else if(resolution=="720p")
//                viewModel.updateBitrate(6000f)
//            else if(resolution=="1080p" )
//                viewModel.updateBitrate(10000f)
//            else if(resolution=="1440p")
//                viewModel.updateBitrate(30000f)
//            else if(resolution=="4K" )
//                viewModel.updateBitrate(50000f)
//        }

        fun setBitrate(resolution : String) : Int{
            var value : Int = 0
            if(resolution=="360p")
                value = 2000
            else if(resolution=="480p")
                value = 4000
            else if(resolution=="720p")
                value = 6000
            else if(resolution=="1080p" )
                value = 10000
            else if(resolution=="1440p")
                value = 30000
            else if(resolution=="2160" )
                value = 50000
            return value
        }
    }
}
