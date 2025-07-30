package com.antcloud.app.logic.user

import com.antcloud.app.data.RefreshUserResponse
import com.antcloud.app.dependencyinjection.AuthRepository
import com.antcloud.app.common.Resource
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.json.JSONObject

import retrofit2.http.Header
import javax.inject.Inject

class RefreshTokenLogic @Inject constructor(private val repository: AuthRepository) {
    operator fun invoke(@Header("refresh") refresh: String): Flow<Resource<RefreshUserResponse>> = flow {
        try {
            emit(Resource.Loading("please wait"))
            val process = repository.refreshToken(refresh)
            coroutineScope {
                if(process.code()==200)
                    emit(
                        Resource.Success(process,process.body()!!.accessToken,
                        process.body()!!.refreshToken,
                        process.body()!!.message))      else {
                    if (process.code()!= 502) {
                        val errorBody = process.errorBody()?.string()
                        process.body()!!.message  = if(errorBody?.contains("message") == true) {
                            errorBody.let { JSONObject(it).getString("message") }
                                .toString()
                        } else "Something Went Wrong"

                    }
                    else {
                        process.body()!!.message = "Something Went Wrong"
                    }
                    when (process.code()) {
                        400 ->
                            emit(Resource.Error("Error 108 : Error occurred. Kindly login again",process.code()))
                        401 ->
                            emit(Resource.Error("Error 308 : Unable to validate. Kindly login again",process.code()))
                        403 ->
                            emit(Resource.Error("Error 708 : Unable to validate. Kindly login again",process.code()))
                        502->
                            emit(Resource.Error("Error 502 : Something Went Wrong",process.code()))
                        else ->
                            emit(Resource.Error("Error 208 : Something Went Wrong. Kindly login again",process.code()))
                    }
                }
            }
        }
        catch (e: Exception) {
            emit(Resource.Error("Error 408 : Something went wrong",0))
        }
    }
}
