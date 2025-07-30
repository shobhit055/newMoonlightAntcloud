package com.antcloud.app.dependencyinjection


import com.antcloud.app.data.CheckConnectionStatusResp
import com.antcloud.app.data.CheckUserInDB
import com.antcloud.app.data.CheckUserResponse
import com.antcloud.app.data.ForgotPasswordReq
import com.antcloud.app.data.GamesResponse
import com.antcloud.app.data.GetVmipResp
import com.antcloud.app.data.LoginReqData
import com.antcloud.app.data.LoginRespData
import com.antcloud.app.data.PhoneOtpReq
import com.antcloud.app.data.PhoneVerifyReq
import com.antcloud.app.data.PinVerifyRequest
import com.antcloud.app.data.PinVerifyResponse
import com.antcloud.app.data.PostSupportReq
import com.antcloud.app.data.PricingReq
import com.antcloud.app.data.PricingRespData
import com.antcloud.app.data.RefreshUserResponse
import com.antcloud.app.data.SignUpResponse
import com.antcloud.app.data.UpdateLocationReq
import com.antcloud.app.data.UpdatePhoneReq
import com.antcloud.app.data.UpdateResolutionReq
import com.antcloud.app.data.UpdateUserFieldResp
import com.antcloud.app.data.UserRegisterReq
import com.antcloud.app.data.VMStatusResp
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
    suspend fun checkForSale(): Response<ResponseBody>
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



