package com.antcloud.app.logic.auth

import com.antcloud.app.dependencyinjection.AuthRepository
import com.antcloud.app.common.Resource
import com.antcloud.app.data.PinVerifyRequest
import com.antcloud.app.data.PinVerifyResponse
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.http.Body
import retrofit2.http.Header
import javax.inject.Inject

class VerifyPinLogic @Inject constructor(private val repository: AuthRepository) {
    operator fun invoke(@Header("Authorization") token: String ,@Body body: PinVerifyRequest): Flow<Resource<PinVerifyResponse>> = flow {
        try {
            emit(Resource.Loading("please wait"))
            val process = repository.verifyPin(token , body)
            coroutineScope {
                if(process.code()==200)
                    emit(Resource.Success(process,"","",""))
                else {
                    emit(Resource.Error("Something Went Wrong. Please try again later",0))
                }
            }
        }
        catch(e:Exception){
        }
    }
}