package com.antcloud.app.common

/*import android.app.Activity
import android.content.IntentSender
import android.os.Bundle
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts*/

import com.antcloud.app.activity.SignupActivity
import com.antcloud.app.data.Location
import com.antcloud.app.data.UserSignUp

/*import com.facebook.CallbackManager.Factory.create
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.GraphRequest
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase*/


class OAuthLogic(val activity: SignupActivity, val onSuccess: ((String, String) -> Unit), val onError: ((String) -> Unit), val isLogin: Boolean) {


    //private val auth = Firebase.auth
    //private val database = Firebase.database.reference
    //private var oneTapClient = Identity.getSignInClient(activity)

    /*private var signInRequest: BeginSignInRequest = BeginSignInRequest.builder()
        .setGoogleIdTokenRequestOptions(
            BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                .setSupported(true)
                .setServerClientId("1042300927189-l7jt9bnreppu8khedqvj8glb3hhql5f2.apps.googleusercontent.com")
                .setFilterByAuthorizedAccounts(isLogin)
                .build()
        )
        .build()*/

    /*companion object {
        private const val EMAIL = "email"
        private const val AUTH_TYPE = "rerequest"
        private const val PUBLIC_PROFILE = "public_profile"
    }

    private val startForResult =
        activity.registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                try {
                    val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
                    val idToken = credential.googleIdToken
                    val username = credential.displayName
                    val email = credential.id
                    val password = credential.password
                    when {
                        idToken != null -> {
                            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                            auth.signInWithCredential(firebaseCredential)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        onSuccess(email, username.toString())
                                    } else {
                                        onError("Failed to Authenticate")
                                    }
                                }
                        }
                        password != null -> {
                        }
                        else -> {
                            onError("No Google Account Found")
                        }
                    }
                } catch (e: ApiException) {
                    onError("Something Went Wrong.")
                }
            } else {
                onError("No Google Account Found")
            }
        }*/

    /*fun startGoogleSignIn() {
        oneTapClient.beginSignIn(signInRequest)
            .addOnSuccessListener { result ->
                try {
                    val intentSenderRequest =
                        IntentSenderRequest.Builder(result.pendingIntent.intentSender).build()
                    startForResult.launch(intentSenderRequest)
                } catch (e: IntentSender.SendIntentException) {
                    onError("Something Went Wrong.")
                }
            }
            .addOnFailureListener { e ->
                when (e.message) {
                    "16: Caller has been temporarily blocked due to too many canceled sign-in prompts." -> {
                        onError("User Blocked. Try again Later.")
                    }
                    "16: Cannot find a matching credential." -> {
                        onError("No Linked Accounts.")
                    }
                    else -> {
                        onError("Something Went Wrong.")
                    }
                }
            }
    }

    fun startFacebookSignIn() {
        val callbackManager = create()
        val loginManager = LoginManager.getInstance()
        loginManager.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                val token = result.accessToken.token
                val credential = FacebookAuthProvider.getCredential(token)
                var fb_email = ""
                var name = ""
                val graphRequest = GraphRequest.newMeRequest(result.accessToken) { _, response ->
                    val json = response!!.getJSONObject()
                    if (json != null) {
                        fb_email = json.getString("email")
                        name = json.getString("name")
                    }
                }
                val parameters = Bundle()
                parameters.putString("fields", "name,email")
                graphRequest.parameters = parameters
                graphRequest.executeAsync()
                auth.signInWithCredential(credential)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {

                            val user = auth.currentUser
                            val profileUpdates = userProfileChangeRequest {
                                displayName = name
                            }
                            user!!.updateProfile(profileUpdates)
                            onSuccess(fb_email, name)
                        } else {

                            onError("Something Went Wrong.")
                        }
                    }
            }

            override fun onCancel() {
                onError("Facebook Authentication Cancelled.")
            }

            override fun onError(error: FacebookException) {

                onError("Something Went Wrong.")
            }
        })

        loginManager.logIn(activity, callbackManager, listOf(EMAIL, PUBLIC_PROFILE))
    }*/

    /*fun createUserProfile(uid: String, firstName: String?, email: String, phone: String?) {

        val notNullFirstname: String
        var lastName: String = ""
        if(firstName != null) {
            if(firstName.contains(" ")) {
                lastName = firstName.substring(firstName.indexOf(" ", 0), firstName.length)
                notNullFirstname = firstName.substring(0, firstName.indexOf(" ", 0))
            } else {
                notNullFirstname = firstName
                lastName = ""
            }
        } else {
            notNullFirstname = "User"
        }

        val notNullPhone = when (phone) {
            null -> ""
            else -> phone
        }

        val user = User2(
            firstName = notNullFirstname,
            lastName = lastName,
            email = email,
            phone = notNullPhone
        )

        val userRef = database.child("/users/$uid").ref

        userRef.get().addOnSuccessListener { t ->
            if (!t.exists()) {
                userRef.updateChildren(user.toMap())
                    .addOnCompleteListener {
                        if (it.isSuccessful)
                            Log.d("", "")
                        else
                            //Log.e("Error", "${it.exception?.message}")
                            activity.makeToast("Error occurred while creating account")
                    }
            }
        }

    }*/

    fun createUserProfile(firstName: String?, email: String, phone: String?, location: Location): UserSignUp {
        val notNullFirstname: String
        var lastName = ""
        if(firstName != null) {
            if(firstName.contains(" ")) {
                lastName = firstName.substring(firstName.indexOf(" ", 0), firstName.length)
                notNullFirstname = firstName.substring(0, firstName.indexOf(" ", 0))
            } else {
                notNullFirstname = firstName
                lastName = ""
            }
        } else {
            notNullFirstname = "User"
        }

        val notNullPhone = when (phone) {
            null -> ""
            else -> phone
        }

        return  UserSignUp(
            firstName = notNullFirstname,
            lastName = lastName,
            email = email,
            phone = notNullPhone,
            location = location
        )
//        val userRef = database.child("/users/$uid").ref
//
//        userRef.get().addOnSuccessListener { t ->
//            if (!t.exists()) {
//                userRef.updateChildren(user.toMap())
//                    .addOnCompleteListener {
//                        if (it.isSuccessful)
//                            Log.d("", "")
//                        else
//                        //Log.e("Error", "${it.exception?.message}")
//                            activity.makeToast("Error occurred while creating account")
//                    }
//            }
//        }
    }
}