package com.limelight.dependencyinjection


import com.limelight.data.CheckConnectionStatusResp
import com.limelight.data.CheckUserInDB
import com.limelight.data.CheckUserResponse
import com.limelight.data.ForgotPasswordReq
import com.limelight.data.GamesResponse
import com.limelight.data.GetVmipResp
import com.limelight.data.LoginReqData
import com.limelight.data.LoginRespData
import com.limelight.data.PhoneOtpReq
import com.limelight.data.PhoneVerifyReq
import com.limelight.data.PinVerifyRequest
import com.limelight.data.PinVerifyResponse
import com.limelight.data.PostSupportReq
import com.limelight.data.PricingReq
import com.limelight.data.PricingRespData
import com.limelight.data.RefreshUserResponse
import com.limelight.data.SignUpResponse
import com.limelight.data.UpdateLocationReq
import com.limelight.data.UpdatePhoneReq
import com.limelight.data.UpdateResolutionReq
import com.limelight.data.UpdateUserFieldResp
import com.limelight.data.UserRegisterReq
import com.limelight.data.VMStatusResp
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query



interface AuthRepositoryInterface {

    //Authentication  Api...................

    suspend fun checkUserInDB(@Body body: CheckUserInDB) : Response<ResponseBody>
    suspend fun userLogin(@Body body: LoginReqData) : Response<LoginRespData>
    suspend fun userRegister(@Body body: UserRegisterReq) : Response<SignUpResponse>
    suspend fun forgotPassword(@Body body: ForgotPasswordReq) : Response<ResponseBody>
    suspend fun postPhoneLogin(@Body body: PhoneVerifyReq) : Response<LoginRespData>
    suspend fun postPhoneOtpData(@Body body: PhoneOtpReq) : Response<ResponseBody>
    suspend fun postPhoneVerifyData(@Body body: PhoneVerifyReq) : Response<ResponseBody>

    //****************************************************************************

    // User Api ....
    suspend fun checkUser(@Header("Authorization") token : String) : Response<CheckUserResponse>
    suspend fun refreshToken(@Header("refresh") refreshToken : String) : Response<RefreshUserResponse>
    suspend fun logoutUser(@Header("refresh") refreshToken : String) : Response<ResponseBody>
    suspend fun updateUserPhone(@Header("Authorization") token : String, @Path("id") id: String, @Body body: UpdatePhoneReq) : Response<UpdateUserFieldResp>
    suspend fun updateUserResolution(@Header("Authorization") token : String, @Path("id") id: String, @Body body: UpdateResolutionReq): Response<UpdateUserFieldResp>
    suspend fun updateUserLocation(@Header("Authorization") token : String, @Path("id") id: String , @Body body: UpdateLocationReq): Response<UpdateUserFieldResp>
    suspend fun resendVerificationEmail(@Header("Authorization") token : String): Response<ResponseBody>

    //****************************************************************************

    // Pricing  Api ....
    suspend fun createPricingOrder(@Header("Authorization") token : String, @Body body: PricingReq): Response<PricingRespData>
    suspend fun checkPaymentAllowed(): Response<ResponseBody>
    suspend fun verifyCouponCode(@Header("Authorization") token : String, @Query("code") code: String) : Response<ResponseBody>
    suspend fun addToWaitList(@Header("Authorization") token : String, @Body body: ForgotPasswordReq): Response<ResponseBody>


    //***********************************************************************************


    // Other Api ......
    suspend fun gameData(@Header("Authorization") token : String): Response<GamesResponse>
    suspend fun postSupportData(@Header("Authorization") token : String, @Body body: PostSupportReq): Response<ResponseBody>
    suspend fun checkVMStatus(@Header("Authorization") token : String) : Response<VMStatusResp>
    suspend fun getVmip(@Header("Authorization") token : String) : Response<GetVmipResp>
    suspend fun checkConnectionStatus(@Header("Authorization") token : String) : Response<CheckConnectionStatusResp>
    suspend fun verifyPin(@Header("Authorization") token : String, @Body body: PinVerifyRequest): Response<PinVerifyResponse>
}



