package dzm.dzmedia.repheraserapp.model

data class PaymentHistoryData(
    val PAYID: String,
    val amount: Double,
    val description: String,
    val payment_date: String,
    val payment_type: String
)