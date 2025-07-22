package com.limelight.logic.pricing


import com.limelight.common.Resource
import com.limelight.dependencyinjection.AuthRepository
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.ResponseBody
import javax.inject.Inject


class CheckSaleLogic @Inject constructor(private val repository: AuthRepository) {
    operator fun invoke(): Flow<Resource<ResponseBody>> = flow {
        try {
            emit(Resource.Loading("please wait"))
            val process = repository.checkForSale()
            coroutineScope {
                if(process.code()==200)
                    emit(Resource.Success(process,"","",""))
                else
                    emit(Resource.Error("Error 120 : Something Went Wrong" , process.code()))
            }
        }
        catch (e : Exception){
            emit(Resource.Error("Error 420 : Something Went Wrong" , 0))
        }
    }
}