package com.limelight.logic.vm



import com.limelight.common.Resource
import com.limelight.data.GetVmipResp
import com.limelight.dependencyinjection.AuthRepository
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.json.JSONObject
import retrofit2.http.Header
import javax.inject.Inject


class GetVMIpLogic @Inject constructor(private val repository: AuthRepository) {
    operator fun invoke(@Header("Authorization") token : String): Flow<Resource<GetVmipResp>> = flow {
        emit(Resource.Loading("please wait"))
        val process = repository.getVmip(token)
        coroutineScope {
            if (process.code() == 200)
                emit(Resource.Success(process, process.body()!!.vmip, "", process.body()!!.message))
            else {
                if(process.code() == 400) {
                    try {
                        val errorValue = process.errorBody()!!.string()
                        emit(Resource.Error(JSONObject(errorValue).get("message").toString(),process.code()))
                    } catch (_: Exception) {
                    }
                }
            }
        }
    }
}