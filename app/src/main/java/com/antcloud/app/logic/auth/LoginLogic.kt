package com.antcloud.app.logic.auth


import com.antcloud.app.data.LoginReqData
import com.antcloud.app.data.LoginRespData
import com.antcloud.app.dependencyinjection.AuthRepository
import com.antcloud.app.common.Resource
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.json.JSONObject
import retrofit2.http.Body
import javax.inject.Inject

enum class LoginErrors {
    WRONG_PASSWORD, USER_NOT_REGISTERED, NULL, BLOCKED, WRONG_OTP, WRONG_NUMBER, OAUTH_ERROR, TRY_LATER,OTP_EXPIRED
}
class LoginLogic @Inject constructor(private val repository: AuthRepository){
    operator fun invoke(@Body body : LoginReqData): Flow<Resource<LoginRespData>> = flow {
            emit(Resource.Loading("please wait"))
            val process = repository.userLogin(body)
            coroutineScope {
                if(process.code()==200)
                    emit(Resource.Success(process,"","",""))
                else {
                    if(process.code()==401) {
                        try {
                            val errorValue = process.errorBody()!!.string()
                            val jObj = JSONObject(errorValue)
                            val jObjErrorArray = jObj.getJSONArray("errors")
                            val message = if (jObjErrorArray.getJSONObject(0).has("message"))
                                jObjErrorArray.getJSONObject(0).getString("message")
                            else ""
                            emit(Resource.Error(message,process.code()))
                        }
                        catch (e: Exception) {
                            emit(Resource.Error("Error 805 : Something Went Wrong",process.code()))
                        }
                    }
                    else{
                        emit(Resource.Error("Error 201 : Something Went Wrong. Please try again after some time",process.code()))
                    }
                }
            }
    }
}