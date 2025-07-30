package com.antcloud.app.logic.pricing



import com.antcloud.app.common.Resource
import com.antcloud.app.data.PricingReq
import com.antcloud.app.data.PricingRespData
import com.antcloud.app.dependencyinjection.AuthRepository
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.json.JSONObject
import retrofit2.http.Body
import javax.inject.Inject
import retrofit2.http.Header



class CreatePricingOrderLogic @Inject constructor(private val repository: AuthRepository) {
    operator fun invoke(@Header("Authorization") token : String,
                        @Body body: PricingReq
    ): Flow<Resource<PricingRespData>> = flow {
        try {
            emit(Resource.Loading("please wait"))
            val process = repository.createPricingOrder(token,body)
            coroutineScope {
                when (process.code()) {
                    200 -> emit(Resource.Success(process,process.body()!!.id,"",""))
                    400 -> {
                        val err = process.errorBody()?.string()?.let { JSONObject(it) }
                        if(err?.has("message") == true)
                            emit(Resource.Error("Error 113 : ${err.getString("message")}", process.code()))
                        else
                            emit(Resource.Error("Error 113 : Something Went Wrong", process.code()))
                    }
                    401 -> emit(Resource.Error("Error 313 : Something Went Wrong", process.code()))
                    403 -> emit(Resource.Error("Error 713 : Something Went Wrong", process.code()))
                    else -> emit(Resource.Error("Error 213 : Something Went Wrong. Please try again after some time", process.code()))
                    }

            }
        }
        catch (e : Exception){
            emit(Resource.Error("Error 413 : Something Went Wrong",0))
        }
    }
}