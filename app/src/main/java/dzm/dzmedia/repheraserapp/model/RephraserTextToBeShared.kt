package dzm.dzmedia.repheraserapp.model

import com.google.gson.annotations.SerializedName

data class RephraserTextToBeShared(
    @SerializedName("input_text") val textualData: String?,
    @SerializedName("action_type") val type: String?,
    @SerializedName("user_id") val userId: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("output_text") val output_text: String?,
    @SerializedName("credits") val credits: String?,
)