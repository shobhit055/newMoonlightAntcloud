package com.antcloud.app.logic.vm


import com.antcloud.app.common.Resource
import com.antcloud.app.data.CheckConnectionStatusResp
import com.antcloud.app.dependencyinjection.AuthRepository
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.json.JSONObject
import retrofit2.http.Header
import javax.inject.Inject


class CheckVMConnectionStatusLogic @Inject constructor(private val repository: AuthRepository) {
    operator fun invoke(@Header("Authorization") token : String): Flow<Resource<CheckConnectionStatusResp>> = flow {
        emit(Resource.Loading("please wait"))
        val process = repository.checkConnectionStatus(token)
        coroutineScope {
                if (process.code() == 200)
                    emit(Resource.Success(process, "", "", ""))
                else {
                    if(process.code() == 400) {
                        try {
                            val errorValue = process.errorBody()!!.string()
                            emit(Resource.Error(JSONObject(errorValue).get("connected").toString(),process.code()))
                        } catch (_: Exception) {
                            emit(Resource.Error("false",-1))


                        }
                    }
                }
        }
    }
}