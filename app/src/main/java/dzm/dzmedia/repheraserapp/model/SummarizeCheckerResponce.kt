package dzm.dzmedia.repheraserapp.model

import com.google.gson.annotations.SerializedName

data class SummarizeCheckerResponce(
    @SerializedName("summarized_text") val summarizedText: String?
)
