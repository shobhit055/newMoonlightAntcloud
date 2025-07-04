package com.limelight.logic.auth

import com.limelight.data.ForgotPasswordReq
import com.limelight.dependencyinjection.AuthRepository
import com.limelight.common.Resource
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.ResponseBody
import retrofit2.http.Body
import javax.inject.Inject

class ForgotLogic @Inject constructor(private val repository: AuthRepository) {
    operator fun invoke(@Body body: ForgotPasswordReq, apiName:String): Flow<Resource<ResponseBody>> = flow {
        try {
            emit(Resource.Loading("please wait"))
            val process = repository.forgotPassword(body)
            coroutineScope {
                if(process.code()==200)
                    emit(Resource.Success(process,"","","Verification Email Sent"))
                else {
                    if(process.code() == 400){
                        if(apiName=="login")
                            emit(Resource.Error("Error 106B : Something Went Wrong." ,process.code()))
                        else
                            emit(Resource.Error("Error 106A : Something Went Wrong." ,process.code()))
                    }
                    else {
                        if(apiName=="login")
                            emit(Resource.Error("Error 206B : Something Went Wrong. Please try again after sometime.",process.code()))
                        else
                            emit(Resource.Error("Error 206A : Something Went Wrong. Please try again after sometime.",process.code()))
                    }
                }
            }
        }
        catch(e:Exception){
            if(apiName=="login")
                emit(Resource.Error("Error 406B : Something Went Wrong.",0))
            else
                emit(Resource.Error("Error 406A : Something Went Wrong.",0))
        }
    }
}