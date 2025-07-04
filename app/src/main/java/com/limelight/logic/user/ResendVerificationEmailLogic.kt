package com.limelight.logic.user

import com.limelight.dependencyinjection.AuthRepository
import com.limelight.common.Resource
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.http.Header
import javax.inject.Inject

class ResendVerificationEmailLogic @Inject constructor(private val repository: AuthRepository) {
    operator fun invoke(@Header("Authorization") token: String): Flow<Resource<ResponseBody>> = flow {
        try {
            emit(Resource.Loading("please wait"))
            val process = repository.resendVerificationEmail(token)
            coroutineScope {

                    when (process.code()) {
                        200 -> {
                            val jObject = JSONObject(process.body()!!.string())
                            if (jObject.has("message"))
                                emit(
                                    Resource.Success(
                                        process,
                                        "",
                                        "",
                                        jObject.getString("message"))
                                )
                            else
                                emit(
                                    Resource.Success(
                                        process,
                                        "",
                                        "",
                                        "Verification email has been sent successfully."
                                    )
                                )

                        }
                        400 -> {
                            val jObject =  JSONObject(process.errorBody()!!.string())
                            if(jObject.has("message"))
                                emit(Resource.Error( "Error 115 : ${jObject.getString("message")}",process.code()))
                            else
                                emit(Resource.Error("Error 115 : Something Went Wrong.",process.code()))
                        }
                        401 -> emit(Resource.Error("Error 315 : Something Went Wrong.",process.code()))
                        403 -> emit(Resource.Error("Error 715 : Something Went Wrong.",process.code()))
                        else -> emit(Resource.Error("Error 215 : Something Went Wrong. Please try again after sometime.",process.code()))

                    }
                }
            }

        catch (e: Exception) {
            emit(Resource.Error( "Error 415 : Something Went Wrong.",0))
        }
    }
}