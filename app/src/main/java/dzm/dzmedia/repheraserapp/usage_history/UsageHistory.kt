package dzm.dzmedia.repheraserapp.usage_history

data class UsageHistory(
    val `data`: List<UsageHistoryData>,
    val message: String,
    val success: Boolean
)