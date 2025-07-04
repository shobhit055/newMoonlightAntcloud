package com.limelight.dependencyinjection


import com.limelight.data.CheckConnectionStatusResp
import com.limelight.data.CheckUserInDB
import com.limelight.data.LoginRespData
import com.limelight.data.SignUpResponse
import com.limelight.data.CheckUserResponse
import com.limelight.data.ForgotPasswordReq
import com.limelight.data.GamesResponse
import com.limelight.data.GetVmipResp
import com.limelight.data.RefreshUserResponse
import com.limelight.data.LoginReqData
import com.limelight.data.PhoneOtpReq
import com.limelight.data.PhoneVerifyReq
import com.limelight.data.PinVerifyRequest
import com.limelight.data.PinVerifyResponse
import com.limelight.data.PostSupportReq
import com.limelight.data.PricingReq
import com.limelight.data.PricingRespData
import com.limelight.data.UpdateLocationReq
import com.limelight.data.UpdatePhoneReq
import com.limelight.data.UpdateResolutionReq
import com.limelight.data.UpdateUserFieldResp
import com.limelight.data.UserRegisterReq
import com.limelight.data.VMStatusResp
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query


interface ApiService {
    @Headers("Content-Type: application/json")
    @POST("users/phone/login")
    suspend fun postPhoneLogin(@Body body: PhoneVerifyReq): Response<LoginRespData>

    @Headers("Content-Type: application/json")
    @POST("phone/otp")
    suspend fun postPhoneOtpData(@Body body: PhoneOtpReq): Response<ResponseBody>

    @Headers("Content-Type: application/json")
    @POST("phone/verify")
    suspend fun postPhoneVerifyData(@Body body: PhoneVerifyReq): Response<ResponseBody>

    @Headers("Content-Type: application/json")
    @POST("users/login")
    suspend fun getLoginData(@Body body: LoginReqData): Response<LoginRespData>

    @Headers("Content-Type: application/json")
    @POST("users")
    suspend fun userRegister(@Body body: UserRegisterReq): Response<SignUpResponse>

    @Headers("Content-Type: application/json")
    @POST("users/forgot-password")
    suspend  fun forgotPassword(@Body body: ForgotPasswordReq): Response<ResponseBody>

    @Headers("Content-Type: application/json")
    @GET("users/me")
    suspend fun checkUser(@Header("Authorization")  token : String): Response<CheckUserResponse>

    @Headers("Content-Type: application/json")
    @GET("users/refresh")
    suspend fun refreshToken(@Header("refresh")  refresh : String): Response<RefreshUserResponse>

    @Headers("Content-Type: application/json")
    @GET("users/logout")
    suspend fun logoutUser(@Header("refresh")  refresh : String ): Response<ResponseBody>


    @Headers("Content-Type: application/json")
    @PATCH("users/{id}")
    suspend fun updateUserPhone(@Header("Authorization") token : String,
                        @Path("id") id: String,
                        @Body body: UpdatePhoneReq
    ): Response<UpdateUserFieldResp>

    @Headers("Content-Type: application/json")
    @PATCH("users/{id}")
    suspend fun updateUserResolution(@Header("Authorization") token : String, @Path("id") id: String,
                             @Body body: UpdateResolutionReq
    ): Response<UpdateUserFieldResp>

    @Headers("Content-Type: application/json")
    @PATCH("users/{id}")
    suspend fun updateUserLocation(@Header("Authorization") token : String, @Path("id") id: String ,
                           @Body body: UpdateLocationReq
    ): Response<UpdateUserFieldResp>

    @Headers("Content-Type: application/json")
    @POST("users/email/otp")
    suspend fun resendVerificationEmail(@Header("Authorization") token : String): Response<ResponseBody>

    @Headers("Content-Type: application/json")
    @GET("globals/games")
    suspend fun getGameData(@Header("Authorization")  token : String ): Response<GamesResponse>

    @Headers("Content-Type: application/json")
    @POST("order/create")
    suspend fun createPricingOrder(@Header("Authorization") token : String, @Body body: PricingReq): Response<PricingRespData>

    @Headers("Content-Type: application/json")
    @POST("support/create")
    suspend fun postSupportData(@Header("Authorization") token : String, @Body body: PostSupportReq): Response<ResponseBody>

    @Headers("Content-Type: application/json")
    @GET("globals/payment")
    suspend fun checkPaymentAllowed(): Response<ResponseBody>

    @Headers("Content-Type: application/json")
    @GET("coupons/verify")
    suspend fun verifyCouponCode(@Header("Authorization") token : String, @Query("code") code: String) : Response<ResponseBody>

    @Headers("Content-Type: application/json")
    @POST("waitlist")
    suspend fun addToWaitList(@Header("Authorization") token : String, @Body body: ForgotPasswordReq): Response<ResponseBody>

    @Headers("Content-Type: application/json")
    @POST("users/check/exist")
    suspend fun checkUserInDb(@Body body : CheckUserInDB): Response<ResponseBody>


    @Headers("Content-Type: application/json")
    @GET("vm/checkvmstatus")
    suspend fun checkVMStatus(@Header("Authorization") token : String): Response<VMStatusResp>

    @Headers("Content-Type: application/json")
    @GET("vm/getVmIp")
    suspend fun getVMAPi(@Header("Authorization") token : String): Response<GetVmipResp>


    @Headers("Content-Type: application/json")
    @GET("vm/checkConnectionStatus")
    suspend fun checkConnectionStatus(@Header("Authorization") token : String): Response<CheckConnectionStatusResp>

    @Headers("Content-Type: application/json")
    @POST("vm/vmauth")
    suspend fun verifyPin(
        @Header("Authorization") token: String,
        @Body body: PinVerifyRequest
    ): Response<PinVerifyResponse>

}

