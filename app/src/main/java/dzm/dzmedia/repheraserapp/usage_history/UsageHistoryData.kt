package dzm.dzmedia.repheraserapp.usage_history

data class UsageHistoryData(
    val action_type: String,
    val created_at: String,
    val credit_used: Int,
    val id: Int,
    val input_txt: String,
    val ip_address: String,
    val output_txt: String,
    val updated_at: String,
    val user_id: Int,
    val website: String
)