package com.antcloud.app.logic.user

import android.util.Log
import com.antcloud.app.data.UpdateUserFieldResp
import com.antcloud.app.dependencyinjection.AuthRepository
import com.antcloud.app.common.Resource
import com.antcloud.app.data.UpdateResolutionReq
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Path
import javax.inject.Inject

class UpdateResolutionLogic @Inject constructor(private val repository: AuthRepository) {
    operator fun invoke(@Header("Authorization") token : String, @Path("id") id: String,
                        @Body body: UpdateResolutionReq
    ): Flow<Resource<UpdateUserFieldResp>> = flow {
        try {
            emit(Resource.Loading("please wait"))
            val process = repository.updateUserResolution(token,id,body)
            coroutineScope {
                when (process.code()) {
                    200 -> emit(Resource.Success(process,process.body()!!.doc.resolution, "", process.body()!!.message))
                    400 -> emit(Resource.Error("Error 111 : Something Went Wrong",process.code()))
                    401 -> emit(Resource.Error("Error 311 : Something Went Wrong",process.code()))
                    403 -> emit(Resource.Error("Error 711 : Something Went Wrong",process.code()))
                    500 -> emit(Resource.Error("Error 502 : Something Went Wrong. Kindly login again",process.code()))
                    else -> emit(Resource.Error("Error 208 : Something Went Wrong. Kindly login again",process.code()))
                }
            }
        }
        catch (e: Exception) {
                e.localizedMessage?.let { Log.e("Log :::", it) }
        }
    }
}