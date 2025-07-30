package com.antcloud.app.logic.auth

import com.antcloud.app.dependencyinjection.AuthRepository
import com.antcloud.app.common.Resource
import com.antcloud.app.data.PhoneVerifyReq
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.http.Body
import javax.inject.Inject

class PostPhoneVerifyData @Inject constructor(private val repository: AuthRepository) {
    operator fun invoke(@Body body: PhoneVerifyReq, apiName : String): Flow<Resource<ResponseBody>> = flow {
        try {
            emit(Resource.Loading("please wait"))
            val process = repository.postPhoneVerifyData(body)
            coroutineScope {
                if(process.code()==200)
                    emit(Resource.Success(process,"","","Verification Email Sent"))
                else {
                    if (process.code() == 400){
                        try {
                            val errorValue = process.errorBody()!!.string()
                            val jObj = JSONObject(errorValue)
                            val message = if (jObj.has("message")) jObj.getString("message") else ""
                            emit(Resource.Error(message, process.code()))
                        }
                        catch (e: Exception) {
                            if (apiName == "signup")
                                emit(Resource.Error("Error 809 : Something Went Wrong", process.code()))
                            else
                                emit(Resource.Error("Error 804 : Something Went Wrong", process.code()))
                        }
                    }
                    else {
                        if (apiName == "signup")
                            emit(Resource.Error("Error 205B : Something Went Wrong. Please try again after some time", process.code()))
                        else
                            emit(Resource.Error("Error 205A : Something Went Wrong. Please try again after some time", process.code()))
                    }
                }
            }
        }
        catch(e:Exception){
            if(apiName=="signup")
                emit(Resource.Error("Error 405B : Something Went Wrong.",0))
            else
                emit(Resource.Error("Error 405A : Something Went Wrong.",0))
        }
    }
}