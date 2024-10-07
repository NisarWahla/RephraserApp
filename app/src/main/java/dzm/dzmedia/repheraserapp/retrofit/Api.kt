package dzm.dzmedia.repheraserapp.retrofit

import dzm.dzmedia.repheraserapp.model.ArticalTItleAndResponce
import dzm.dzmedia.repheraserapp.rephraser_api_responce.RephraserApiResponce
import retrofit2.Call
import retrofit2.http.*

interface Api {

    @POST("rephraser")
    @FormUrlEncoded
    fun sendToRephraser(
        @Header("Authorization") token: String,
        @Field("input_text") input_text: String,
        @Field("action_type") action_type: String,
        @Field("user_id") user_id: Int?,
        @Field("ip_address") ip_address: String,
        @Field("mode_type") mode_type: String,
        @Field("forced_words") forcedWords: String,
    ): Call<RephraserApiResponce>

    @Headers("Dzine-Media-API: ABC123")
    @FormUrlEncoded
    @POST("article_generator")
    fun SendToArticalGenarator(@Field("text") requestBody: String): Call<ArticalTItleAndResponce>

    @FormUrlEncoded
    @POST("grammer-checker")
    fun SendToGrammarChecker(
        @Header("Authorization") token: String,
        @Field("input_text") input_text: String,
        @Field("action_type") action_type: String,
        @Field("user_id") user_id: Int?,
        @Field("ip_address") ip_address: String
    ): Call<RephraserApiResponce>

    @FormUrlEncoded
    @POST("summarizer")
    fun SendToSummarizeChecker(
        @Header("Authorization") token: String,
        @Field("input_text") input_text: String,
        @Field("action_type") action_type: String,
        @Field("user_id") user_id: Int?,
        @Field("ip_address") ip_address: String
    ): Call<RephraserApiResponce>
}