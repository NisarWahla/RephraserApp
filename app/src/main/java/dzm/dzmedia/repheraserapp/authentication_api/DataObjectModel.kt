package dzm.dzmedia.repheraserapp.authentication_api

import com.google.gson.annotations.SerializedName

data class DataObjectModel(
    @SerializedName("token")
    var token: String = "",
    @SerializedName("user_id")
    var user_id: Int,
    @SerializedName("first_name")
    var firstName: String,
    @SerializedName("last_name")
    var last_name: String,
    @SerializedName("image")
    var image: String,
    @SerializedName("email")
    var email: String,
    @SerializedName("contact_no")
    var contact_no: String,
    @SerializedName("ip_address")
    var ip_address: String,
    @SerializedName("country")
    var country: String,
    @SerializedName("otp")
    var otp: Int,
    @SerializedName("eligible")
    var eligible: Boolean,
    @SerializedName("verified")
    var verified: Boolean,
    @SerializedName("subscribed")
    var subscribed: Boolean,
    @SerializedName("subscription")
    var subscription: String,
    @SerializedName("subscription_id")
    var subscription_id: String,
    @SerializedName("subscription_end_at")
    var subscription_end_at: String,
    @SerializedName("word_limit")
    var word_limit: Int,
    @SerializedName("max_limit")
    var max_limit: Int,
    @SerializedName("min_limit")
    var min_limit: Int

)