package com.antcloud.app.logic.user


import com.antcloud.app.data.UpdateUserFieldResp
import com.antcloud.app.dependencyinjection.AuthRepository
import com.antcloud.app.common.Resource
import com.antcloud.app.data.UpdatePhoneReq
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Path
import javax.inject.Inject

class UpdatePhoneLogic @Inject constructor(private val repository: AuthRepository) {
    operator fun invoke(@Header("Authorization") token: String, @Path("id") id: String, @Body body: UpdatePhoneReq): Flow<Resource<UpdateUserFieldResp>> = flow {
        try {
            emit(Resource.Loading("please wait"))
            val process = repository.updateUserPhone(token,id,body)
            coroutineScope {

                    when (process.code()) {
                        200 -> emit(Resource.Success(process,process.body()!!.doc.phone, "", process.body()!!.message))
                        400 ->
                            emit(Resource.Error("Error 110 : Something Went Wrong",process.code()))
                        401 ->
                            emit(Resource.Error( "Error 310 : Something Went Wrong",process.code()))
                        403 ->
                            emit(Resource.Error("Error 710 : Something Went Wrong",process.code()))
                        else -> {
                            emit(Resource.Error("Error 211 : Something Went Wrong. Please try again after sometime.",process.code()))

                        }
                }
            }
        }
        catch (e: Exception) {
            emit(Resource.Error("Error 411 : Something Went Wrong.",0))
        }
    }
}