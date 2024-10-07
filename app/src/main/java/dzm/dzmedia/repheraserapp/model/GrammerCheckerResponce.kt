package dzm.dzmedia.repheraserapp.model

import com.google.gson.annotations.SerializedName

data class GrammerCheckerResponce(
    @SerializedName("corrected_text") val correctedText: String?,
    @SerializedName("input_text") val inputText: String?
)
