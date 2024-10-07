package dzm.dzmedia.repheraserapp.model.GetInfo

data class Data(
    val authorization: String,
    val free_max_limit: Int,
    val paid_max_limit: Int,
    val today_limit: Int
)