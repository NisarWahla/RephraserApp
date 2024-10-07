package dzm.dzmedia.repheraserapp.authentication_api

import com.google.gson.annotations.SerializedName

data class AuthenticationBaseModel(
    @SerializedName("success")
    var success: Boolean,
    val `data`: DataObjectModel,
    @SerializedName("message")
    var message: String
)