package com.antcloud.app.logic.pricing


import com.antcloud.app.common.Resource
import com.antcloud.app.dependencyinjection.AuthRepository
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.ResponseBody
import javax.inject.Inject


class CheckPaymentAllowLogic @Inject constructor(private val repository: AuthRepository) {
    operator fun invoke(): Flow<Resource<ResponseBody>> = flow {
        try {
            emit(Resource.Loading("please wait"))
            val process = repository.checkPaymentAllowed()
            coroutineScope {
                if(process.code()==200)
                    emit(Resource.Success(process,"","",""))
                else
                    emit(Resource.Error("Error 116 : Something Went Wrong" , process.code()))
            }
        }
        catch (e : Exception){
            emit(Resource.Error("Error 416 : Something Went Wrong" , 0))
        }
    }
}