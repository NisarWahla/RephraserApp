package dzm.dzmedia.repheraserapp.model.GrammarCheckerFree

import dzm.dzmedia.repheraserapp.model.GrammarCheckerFree.Data

data class GrammarCheckerApiResponse(
    val `data`: Data,
    val message: String,
    val success: Boolean
)