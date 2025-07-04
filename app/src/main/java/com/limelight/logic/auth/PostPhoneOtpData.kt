package com.limelight.logic.auth

import com.limelight.dependencyinjection.AuthRepository
import com.limelight.common.Resource
import com.limelight.data.PhoneOtpReq
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.http.Body
import javax.inject.Inject

class PostPhoneOtpData @Inject constructor(private val repository: AuthRepository) {
    operator fun invoke(@Body body: PhoneOtpReq, apiName:String ): Flow<Resource<ResponseBody>> = flow {
            emit(Resource.Loading("please wait"))
            val process = repository.postPhoneOtpData(body)
            coroutineScope {
                if(process.code()==200)
                    emit(Resource.Success(process,"","","Verification Email Sent"))
                else if (process.code() == 400) {
                    try {
                        val errorValue = process.errorBody()!!.string()
                        val jObj = JSONObject(errorValue)
                        val message = if (jObj.has("message")) jObj.getString("message") else ""
                        emit(Resource.Error(message, process.code()))
                    }
                    catch (e: Exception) {
                        if (apiName == "login")
                            emit(Resource.Error("Error 807 : Something went wrong. Please try again after sometime.", process.code()))
                        else if (apiName == "signup")
                            emit(Resource.Error("Error 808 : Something Went Wrong", process.code()))
                        else
                            emit(Resource.Error("Error 803 : Something Went Wrong", process.code()))
                    }
                }
                else {
                    if (apiName == "login")
                        emit(Resource.Error("Error 204C : Something Went Wrong. Please try again after sometime.", process.code()))
                    else if (apiName == "signup")
                        emit(Resource.Error("Error 204B : Something Went Wrong. Please try again after some time", process.code()))
                    else
                        emit(Resource.Error("Error 204A : Something went wrong. Please try again after some time", process.code()))
                }
            }
    }
}
