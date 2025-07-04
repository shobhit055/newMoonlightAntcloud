package com.limelight.logic.auth


import com.limelight.dependencyinjection.AuthRepository
import com.limelight.common.Resource
import com.limelight.data.CheckUserInDB
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.http.Body
import javax.inject.Inject



class CheckUserInDBLogic @Inject constructor(private val repository: AuthRepository) {
    operator fun invoke(@Body body: CheckUserInDB): Flow<Resource<ResponseBody>> = flow {
        emit(Resource.Loading("please wait"))
        val process = repository.checkUserInDB(body)
        coroutineScope {
            if (process.code() == 200)
                emit(Resource.Success(process, "", "", ""))
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