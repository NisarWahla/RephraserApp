package dzm.dzmedia.repheraserapp.model

import com.google.gson.annotations.SerializedName

data class ArticalTItleAndResponce(
    @SerializedName("generated_article") val generatedArtical: String?,
    @SerializedName("input_text") val inputText: String?
)
