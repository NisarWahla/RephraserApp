package dzm.dzmedia.repheraserapp.authentication_api

import com.google.gson.annotations.SerializedName

data class ErrorResponse(
    @SerializedName("data")
    val `data`: String,
    @SerializedName("message")
    val message: String,
    @SerializedName("success")
    val success: Boolean
)