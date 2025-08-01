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
import javax.inject.Inject



class AuthRepository @Inject constructor(private val api : ApiService) : AuthRepositoryInterface {
    override suspend fun userLogin(@Body body: LoginReqData): Response<LoginRespData> {
        return api.getLoginData(body)
    }

    override suspend fun userRegister(@Body body: UserRegisterReq): Response<SignUpResponse> {
        return api.userRegister(body)
    }

    override suspend fun forgotPassword(@Body body: ForgotPasswordReq): Response<ResponseBody> {
        return api.forgotPassword(body)
    }

    override suspend fun postPhoneLogin(@Body body: PhoneVerifyReq): Response<LoginRespData> {
        return api.postPhoneLogin(body)
    }

    override suspend fun postPhoneOtpData(@Body body: PhoneOtpReq): Response<ResponseBody> {
        return api.postPhoneOtpData(body)
    }

    override suspend fun postPhoneVerifyData(@Body body: PhoneVerifyReq): Response<ResponseBody> {
        return api.postPhoneVerifyData(body)
    }

    override suspend fun checkUser(@Header("Authorization") token : String): Response<CheckUserResponse> {
        return api.checkUser(token)
    }

    override suspend fun refreshToken(@Header("refresh") refreshToken: String): Response<RefreshUserResponse> {
        return api.refreshToken(refreshToken)
    }

    override suspend fun logoutUser(@Header("refresh") refreshToken: String): Response<ResponseBody> {
        return api.logoutUser(refreshToken)
    }

    override suspend fun updateUserPhone(@Header("Authorization") token: String, @Path("id") id: String, @Body body: UpdatePhoneReq): Response<UpdateUserFieldResp> {
        return api.updateUserPhone(token,id,body)
    }

    override suspend fun updateUserResolution(@Header("Authorization") token: String, @Path("id") id: String, @Body body: UpdateResolutionReq): Response<UpdateUserFieldResp> {
        return api.updateUserResolution(token,id,body)
    }

    override suspend fun updateUserLocation(@Header("Authorization") token: String, @Path("id") id: String, @Body body: UpdateLocationReq): Response<UpdateUserFieldResp> {
        return api.updateUserLocation(token,id,body)
    }

    override suspend fun resendVerificationEmail(@Header("Authorization") token: String): Response<ResponseBody> {
        return api.resendVerificationEmail(token)
    }

    override suspend fun gameData(@Header("Authorization") token: String): Response<GamesResponse> {
        return api.getGameData(token)
    }

    override suspend fun createPricingOrder(@Header("Authorization") token : String, @Body body: PricingReq): Response<PricingRespData> {
        return api.createPricingOrder(token,body)
    }

    override suspend fun postSupportData(@Header("Authorization") token : String, @Body body: PostSupportReq): Response<ResponseBody> {
        return api.postSupportData(token,body)
    }

    override suspend fun checkPaymentAllowed(): Response<ResponseBody> {
        return api.checkPaymentAllowed()
    }

    override suspend fun checkForSale(): Response<ResponseBody> {
        return api.checkForSale()
    }


    override suspend fun verifyCouponCode(@Header("Authorization") token: String, @Query("code") code: String): Response<ResponseBody> {
        return api.verifyCouponCode(token,code)
    }

    override suspend fun addToWaitList(@Header("Authorization") token: String, @Body body: ForgotPasswordReq): Response<ResponseBody> {
        return api.addToWaitList(token,body)
    }

    override suspend fun checkUserInDB(@Body body: CheckUserInDB): Response<ResponseBody> {
        return api.checkUserInDb(body)
    }

    override suspend fun checkVMStatus(@Header("Authorization") token : String): Response<VMStatusResp> {
        return api.checkVMStatus(token)
    }

    override suspend fun getVmip(@Header("Authorization") token : String): Response<GetVmipResp> {
        return api.getVMAPi(token)
    }

    override  suspend fun checkConnectionStatus(@Header("Authorization") token : String): Response<CheckConnectionStatusResp> {
        return api.checkConnectionStatus(token)
    }


    override suspend fun verifyPin(@Header("Authorization") token: String, @Body body: PinVerifyRequest): Response<PinVerifyResponse> {
        return api.verifyPin(token,body)
    }
}