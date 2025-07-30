package com.antcloud.app.logic.auth



import com.antcloud.app.data.SignUpResponse
import com.antcloud.app.data.UserRegisterReq
import com.antcloud.app.dependencyinjection.AuthRepository
import com.antcloud.app.common.Resource
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.json.JSONObject
import retrofit2.http.Body
import javax.inject.Inject

class SignUpLogic @Inject constructor(private val repository: AuthRepository) {
    operator fun invoke(@Body body : UserRegisterReq): Flow<Resource<SignUpResponse>> = flow {
        try {
            emit(Resource.Loading("please wait"))
            val process = repository.userRegister(body)
            coroutineScope {
                if(process.code()==201){
                    if(process.body()?.doc != null) {
                        emit(Resource.Success(process,"","",""))
                    }
                    else{
                       emit(Resource.Error("Error 103A : Something Went Wrong",0))
                    }
                }
                else {
                    if(process.code()==400) {
                        try {
                            val errorValue = process.errorBody()!!.string()
                            val jObj = JSONObject(errorValue)
                            val jObjErrorArray = jObj.getJSONArray("errors")
                            var message = ""
                            if(jObjErrorArray.getJSONObject(0).has("data") && jObjErrorArray.getJSONObject(0).getJSONArray("data").getJSONObject(0).has("message")) {
                                message = jObjErrorArray.getJSONObject(0).getJSONArray("data").getJSONObject(0).getString("message")
                            }
                            emit(Resource.Error(message,process.code()))
                        }
                        catch (e: Exception) {
                            emit(Resource.Error("Error 810 : Something Went Wrong",process.code()))
                        }
                    }
                    else{
                        emit(Resource.Error("Error 203 : Something Went Wrong",process.code()))
                    }
                }
            }
        }
        catch (e : Exception){
            emit(Resource.Error( "Error 403 : Something Went Wrong",0))
        }
    }
}