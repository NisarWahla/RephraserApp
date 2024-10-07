package dzm.dzmedia.repheraserapp.model

data class PaymetHistoryModel(
    val `data`: List<PaymentHistoryData>,
    val message: String,
    val success: Boolean
)