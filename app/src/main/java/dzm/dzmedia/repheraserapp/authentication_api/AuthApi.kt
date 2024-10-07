package dzm.dzmedia.repheraserapp.authentication_api

import dzm.dzmedia.repheraserapp.model.GetInfo.GetInfoModel
import dzm.dzmedia.repheraserapp.model.GrammarCheckerFree.GrammarCheckerApiResponse
import dzm.dzmedia.repheraserapp.model.PaymetHistoryModel
import dzm.dzmedia.repheraserapp.model.SummarizerFree.SummarizerFree
import dzm.dzmedia.repheraserapp.rephraser_api_responce.RephraserApiResponce
import dzm.dzmedia.repheraserapp.usage_history.UsageHistory
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApi {

    @FormUrlEncoded
    @POST("getfreeinfo")
    fun getInfo(
        @Header("X-Api-Key") key: String,
        @Field("ip_address") ip: String?
    ): Call<GetInfoModel>

    @FormUrlEncoded
    @POST("getrephraser")
    fun getrephraser(
        @Header("X-Api-Key") key: String,
        @Field("input_text") inputText: String?,
        @Field("action_type") actionType: String,
        @Field("ip_address") ipAddress: String,
        @Field("mode_type") modeType: String
    ): Call<RephraserApiResponce>

    @FormUrlEncoded
    @POST("getgrammercheck")
    fun getGrammarChecker(
        @Header("X-Api-Key") key: String,
        @Field("input_text") inputText: String?,
        @Field("action_type") actionType: String,
        @Field("ip_address") ipAddress: String
    ): Call<GrammarCheckerApiResponse>

    @FormUrlEncoded
    @POST("getsummarizer")
    fun getSummarizerFree(
        @Header("X-Api-Key") key: String,
        @Field("input_text") inputText: String?,
        @Field("action_type") actionType: String,
        @Field("ip_address") ipAddress: String
    ): Call<SummarizerFree>

    @FormUrlEncoded
    @POST("register")
    fun UerRegisteration(
        @Field("first_name") first_name: String,
        @Field("last_name") last_name: String,
        @Field("email") user_email: String,
        @Field("password") user_password: String,
        @Field("password_confirmation") user_password_confirmation: String,
        @Field("ip_address") ip_address: String
    ): Call<AuthenticationBaseModel>

    @FormUrlEncoded
    @POST("register")
    fun UerRegisterationWithFacebook(
        @Field("first_name") first_name: String,
        @Field("last_name") last_name: String,
        @Field("email") user_email: String,
        @Field("password") user_password: String,
        @Field("password_confirmation") user_password_confirmation: String,
        @Field("ip_address") ip_address: String,
        @Field("facebook_id") facebookID: String
    ): Call<AuthenticationBaseModel>

    @FormUrlEncoded
    @POST("register")
    fun UerRegisterationWithGoogle(
        @Field("first_name") first_name: String,
        @Field("last_name") last_name: String,
        @Field("email") user_email: String,
        @Field("password") user_password: String,
        @Field("password_confirmation") user_password_confirmation: String,
        @Field("ip_address") ip_address: String,
        @Field("google_id") googleID: String
    ): Call<AuthenticationBaseModel>

    @FormUrlEncoded
    @POST("login")
    fun UserLogin(
        @Field("email") user_name: String,
        @Field("password") user_email: String
    ): Call<AuthenticationBaseModel>

    @FormUrlEncoded
    @POST("sent-otp")
    fun sendOTP(
        @Field("email") user_name: String
    ): Call<AuthenticationBaseModel>

    @FormUrlEncoded
    @POST("user-profile")
    fun userProfile(
        @Header("Authorization") token: String,
        @Field("user_id") user_id: Int?
    ): Call<AuthenticationBaseModel>


    @FormUrlEncoded
    @POST("verify-email")
    fun verifyEmail(
        @Header("Authorization") token: String,
        @Field("OTP") otp: String,
        @Field("user_id") user_id: Int?
    ): Call<AuthenticationBaseModel>

    @FormUrlEncoded
    @POST("verify-otp")
    fun verifyOTP(
        @Header("Authorization") token: String,
        @Field("email") email: String,
        @Field("OTP") otp: String
    ): Call<AuthenticationBaseModel>

    @FormUrlEncoded
    @POST("change-password")
    fun changePassword(
        @Header("Authorization") token: String,
        @Field("user_id") user_id: Int?,
        @Field("old_password") old_password: String,
        @Field("password") newPassword: String,
        @Field("password_confirmation") password_confirmation: String
    ): Call<AuthenticationBaseModel>

    @FormUrlEncoded
    @POST("reset-password")
    fun resetPassword(
        @Field("email") email: String,
        @Field("password") Password: String,
        @Field("password_confirmation") password_confirmation: String
    ): Call<AuthenticationBaseModel>

    @FormUrlEncoded
    @POST("usage-history")
    fun UsageHistory(
        @Header("Authorization") token: String,
        @Field("user_id") user_id: Int?
    ): Call<UsageHistory>

    @FormUrlEncoded
    @POST("payment-history")
    fun paymentHistory(
        @Header("Authorization") token: String,
        @Field("user_id") user_id: Int?
    ): Call<PaymetHistoryModel>

    @FormUrlEncoded
    @POST("user-update")
    fun userUpdate(
        @Header("Authorization") token: String,
        @Field("user_id") user_id: Int?,
        @Field("first_name") firstName: String,
        @Field("last_name") lastName: String,
        @Field("contact_no") contactNumber: String
    ): Call<AuthenticationBaseModel>

    @FormUrlEncoded
    @POST("payment-info")
    fun subscribedUser(
        @Header("Authorization") token: String,
        @Header("X-Api-Key") key: String,
        @Field("payment_id") payment_id: String?,
        @Field("subscription_type") subscriptionType: String,
        @Field("amount") amount: Int,
        @Field("payment_type") paymentType: String,
        @Field("user_id") userId: Int,
        @Field("last_four") last_four: Int,
        @Field("is_test") is_test: Int
    ): Call<AuthenticationBaseModel>

    @FormUrlEncoded
    @POST("cancel-subscription")
    fun cancelSubscription(
        @Header("Authorization") token: String,
        @Field("user_id") user_id: Int?,
        @Field("subscription_id") subscription_id: String?,
        @Field("device_id") deviceId: String?
    ): Call<AuthenticationBaseModel>
}