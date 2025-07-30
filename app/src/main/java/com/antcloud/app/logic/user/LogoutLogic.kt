package com.antcloud.app.logic.user


import com.antcloud.app.dependencyinjection.AuthRepository
import com.antcloud.app.common.Resource
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.ResponseBody
import retrofit2.http.Header
import javax.inject.Inject


class LogoutLogic @Inject constructor(private val repository: AuthRepository) {
    operator fun invoke(@Header("refresh") refresh: String): Flow<Resource<ResponseBody>> = flow {
        try {
            emit(Resource.Loading("please wait"))
            val process = repository.logoutUser(refresh)
            coroutineScope {
                if(process.code()==200)
                    emit(Resource.Success(process,"", "", ""))
                else {
                    emit(Resource.Error("Error 117 : An error occurred while logging out.",process.code()))
                }
            }
        }
        catch (e: Exception) {
            emit(Resource.Error("Error 417 : Something Went Wrong.",0))
        }
    }
}