package com.limelight.logic.pricing

import com.limelight.common.Resource
import com.limelight.data.ForgotPasswordReq
import com.limelight.dependencyinjection.AuthRepository
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.Header
import javax.inject.Inject


class AddToWaitListLogic @Inject constructor(private val repository: AuthRepository) {
    operator fun invoke(@Header("Authorization") token : String, @Body body: ForgotPasswordReq): Flow<Resource<ResponseBody>> = flow {
        try {
            emit(Resource.Loading("please wait"))
            val process = repository.addToWaitList(token,body)
            coroutineScope {
                if(process.code()==201) {
                    emit(Resource.Success(process,"","",""))
                }
                else {
                    if (process.code() == 401)
                        emit(Resource.Error("Error 319 : Something Went Wrong", process.code()))
                    else
                        emit(Resource.Error("Error 219 : Something Went Wrong. Unable to add to wait list.", process.code()))
                }
            }
        }
        catch (e : Exception){
            emit(Resource.Error("Error 419 : Something Went Wrong.",0))
        }
    }
}