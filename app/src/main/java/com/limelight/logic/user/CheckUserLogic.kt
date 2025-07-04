package com.limelight.logic.user


import com.limelight.data.CheckUserResponse
import com.limelight.dependencyinjection.AuthRepository
import com.limelight.common.Resource
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.json.JSONObject
import javax.inject.Inject
import retrofit2.http.Header


class CheckUserLogic @Inject constructor(private val repository: AuthRepository) {
    operator fun invoke(@Header("Authorization") token:String): Flow<Resource<CheckUserResponse>> = flow {
        try {
            emit(Resource.Loading("please wait"))
            val process = repository.checkUser(token)
            coroutineScope {
                when (process.code()) {
                    200 -> emit(Resource.Success(process,process.body()!!.token,"",""))
                    401 -> {
                        var msg = ""
                        val err = process.errorBody()?.string()?.let { JSONObject(it) }
                        if (err?.has("message") == true) {
                            err.getString("message").let { msg = it }
                        }
                        emit(Resource.Error(msg,process.code()))
                    }
                    500 -> emit(Resource.Error("Error 207 : Something went wrong. Kindly login again", process.code()))
                    403 -> emit(Resource.Error("Error 707 : Unable to validate session. Kindly login again", process.code()))
                    else -> emit(Resource.Error("Error 107 : Unable to validate session. Kindly login again", process.code()))

                }
            }
        }
        catch (e : Exception){
            emit(Resource.Error("Error 407 : Something went wrong",0))
        }
    }
}