package com.antcloud.app.logic.game



import com.antcloud.app.common.Resource
import com.antcloud.app.data.GamesResponse
import com.antcloud.app.dependencyinjection.AuthRepository
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.http.Header
import javax.inject.Inject


class GameLogic @Inject constructor(private val repository: AuthRepository) {
    operator fun invoke(@Header("Authorization") token : String): Flow<Resource<GamesResponse>> = flow {
        try {
            emit(Resource.Loading("please wait"))
            val process = repository.gameData(token)
            coroutineScope {
                when (process.code()) {
                    200 -> emit(Resource.Success(process, "", "", ""))
                    400 -> emit(Resource.Error("Error 109 : Error getting data", process.code()))
                    401 ->  emit(Resource.Error("Error 309 : Something Went Wrong",process.code()))
                    403 -> emit(Resource.Error("Error 709 : Something Went Wrong", process.code()))
                    else -> emit(Resource.Error("Error 209 : Something Went Wrong. Please try again after sometime.", process.code()))
                }
            }
        }
        catch (e : Exception){
            emit(Resource.Error("Error 409 : Something went wrong",0))
        }
    }
}