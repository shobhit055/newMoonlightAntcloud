package com.antcloud.app.logic.vm


import com.antcloud.app.common.Resource
import com.antcloud.app.data.VMStatusResp
import com.antcloud.app.dependencyinjection.AuthRepository
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.http.Header
import javax.inject.Inject


class VmStatusLogic @Inject constructor(private val repository: AuthRepository) {
    operator fun invoke(@Header("Authorization") token : String): Flow<Resource<VMStatusResp>> = flow {
        emit(Resource.Loading("please wait"))
        val process = repository.checkVMStatus(token)
        coroutineScope {
            if (process.code() == 200)
                emit(Resource.Success(process, "", "",process.body()?.Status!!))
            else {
                    try {
                      //  val errorValue = process.errorBody()!!.string()
                        emit(Resource.Error("error",process.code()))
                    } catch (_: Exception) {

                }
            }
        }
    }
}