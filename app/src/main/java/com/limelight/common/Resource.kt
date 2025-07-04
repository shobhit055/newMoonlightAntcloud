package com.limelight.common

import retrofit2.Response


sealed class Resource<T>(val data: Response<T>? = null, val token : String?=null, val refreshToken:String? = null,
                         val message:String? = null, val errorCode:Int?=-1) {
    class Loading<T>(message : String) : Resource<T>(message = message)
    class Success<T>(data: Response<T>, token: String, refreshToken: String, message: String) :
                     Resource<T>(data = data, token = token, refreshToken = refreshToken, message = message)
    class Error<T>(message : String, errorCode : Int) : Resource<T>(message = message, errorCode = errorCode)
}