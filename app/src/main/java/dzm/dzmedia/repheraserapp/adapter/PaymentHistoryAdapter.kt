package dzm.dzmedia.repheraserapp.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dzm.dzmedia.repheraserapp.activities.MainActivity
import dzm.dzmedia.repheraserapp.databinding.FragmentArticalGenHistoryBinding
import dzm.dzmedia.repheraserapp.databinding.FragmentParaphraserSavedHistoryBinding
import dzm.dzmedia.repheraserapp.databinding.UsageHistoryAdapterLayoutBinding
import dzm.dzmedia.repheraserapp.model.PaymentHistoryData

class PaymentHistoryAdapter(
    val context: Context,
    var list: ArrayList<PaymentHistoryData>,
    val binding: FragmentArticalGenHistoryBinding
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(context)
        val binding = UsageHistoryAdapterLayoutBinding.inflate(inflater, parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val data = list[position]
        val firstName = MainActivity.userModel!!.firstName
        val lastName = MainActivity.userModel!!.last_name
        val fullName = "$firstName $lastName"

        if (holder is MyViewHolder) {
            holder.binding.ruTitle.text = fullName
            holder.binding.ruTime.text = data.payment_date
            holder.binding.amount.text = data.amount.toString()
            holder.binding.payID.text = data.PAYID
            holder.binding.itemNumber.text = holder.adapterPosition.toString()
            holder.binding.menuOptions.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class MyViewHolder(val binding: UsageHistoryAdapterLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(filterList: ArrayList<PaymentHistoryData>) {
        list = filterList
        notifyDataSetChanged()
    }
}