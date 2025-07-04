package com.limelight.logic.pricing



import com.limelight.common.Resource
import com.limelight.dependencyinjection.AuthRepository
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.ResponseBody
import javax.inject.Inject
import retrofit2.http.Header
import retrofit2.http.Query


class VerifyCouponCodeLogic @Inject constructor(private val repository: AuthRepository) {
    operator fun invoke(@Header("Authorization") token : String, @Query("code") code: String): Flow<Resource<ResponseBody>> = flow {
        try {
            emit(Resource.Loading("please wait"))
            val process = repository.verifyCouponCode(token,code)
            coroutineScope {
                when (process.code()) {
                    200 ->  emit(Resource.Success(process,"","",""))
                    400 ->  emit(Resource.Error("", process.code()))
                    401 ->  emit(Resource.Error("", process.code()))
                    403 ->  emit(Resource.Error("Error 714 : Something Went Wrong", process.code()))
                    else -> emit(Resource.Error("Error 214 : Something Went Wrong. Please try again after some time.", process.code()))
                }
            }
        }
        catch (e : Exception){
            emit(Resource.Error("Error 414 : Something Went Wrong.",0))
        }
    }
}