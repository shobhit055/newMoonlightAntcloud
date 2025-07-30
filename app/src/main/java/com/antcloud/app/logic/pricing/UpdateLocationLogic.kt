package com.antcloud.app.logic.pricing

import com.antcloud.app.common.Resource
import com.antcloud.app.data.UpdateLocationReq
import com.antcloud.app.data.UpdateUserFieldResp
import com.antcloud.app.dependencyinjection.AuthRepository
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Path
import javax.inject.Inject

class UpdateLocationLogic @Inject constructor(private val repository: AuthRepository) {
    operator fun invoke(@Header("Authorization") token : String, @Path("id") id: String,
                        @Body body: UpdateLocationReq
    ): Flow<Resource<UpdateUserFieldResp>> = flow {
        try {
            emit(Resource.Loading("please wait"))
            val process = repository.updateUserLocation(token,id,body)
            coroutineScope {
                when (process.code()) {
                    200 ->  emit(Resource.Success(process,"", "", process.body()!!.message))
                    400 ->  emit(Resource.Error("Error 118 : Something Went Wrong",process.code()))
                    401 ->  emit(Resource.Error("Error 318 : Something Went Wrong",process.code()))
                    403 ->  emit(Resource.Error("Error 718 : Something Went Wrong",process.code()))
                    else -> emit(Resource.Error("Error 218 : Something Went Wrong. Please try again after sometime.",process.code()))
                }
            }
        }
        catch (e: Exception) {
            emit(Resource.Error(  "Error 418 : Something Went Wrong.",0))
        }
    }
}