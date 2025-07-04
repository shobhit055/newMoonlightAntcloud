package com.limelight.logic.support


import com.limelight.common.Resource
import com.limelight.data.PostSupportReq
import com.limelight.dependencyinjection.AuthRepository
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.Header
import javax.inject.Inject

class SupportLogic @Inject constructor(private val repository: AuthRepository) {
    operator fun invoke(@Header("Authorization") token : String, @Body body: PostSupportReq):
            Flow<Resource<ResponseBody>> = flow {
        try {
            emit(Resource.Loading("please wait"))
            val process = repository.postSupportData(token,body)
            coroutineScope {
                when (process.code()) {
                    200 -> emit(Resource.Success(process,"","",""))
                    400 -> emit(Resource.Error("Error 112 : Error submitting feedback",process.code()))
                    401 -> emit(Resource.Error("Error 312 : Something Went Wrong",process.code()))
                    403 -> emit(Resource.Error("Error 712 : Error submitting feedback",process.code()))
                    else-> emit(Resource.Error("Error 212 : Something Went Wrong. Please try again after some time.",process.code()))
                }
            }
        }
        catch (e : Exception){
            emit(Resource.Error("Error 412 : Something Went Wrong",0))
        }
    }
}