package dzm.dzmedia.repheraserapp.adapter

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import dzm.dzmedia.repheraserapp.R
import dzm.dzmedia.repheraserapp.activities.HistoryActivity
import dzm.dzmedia.repheraserapp.databinding.FragmentParaphraserSavedHistoryBinding
import dzm.dzmedia.repheraserapp.databinding.FrozenWordsLayoutBinding
import dzm.dzmedia.repheraserapp.databinding.UsageHistoryAdapterLayoutBinding
import dzm.dzmedia.repheraserapp.fragments.ExportTextDialogFragment
import dzm.dzmedia.repheraserapp.helpers.Utils
import dzm.dzmedia.repheraserapp.usage_history.UsageHistoryData


class FrozenWordsAdapter(
    val context: Context,
    var list: ArrayList<String>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(context)
        val binding = FrozenWordsLayoutBinding.inflate(inflater, parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val data = list[position]

        if (holder is MyViewHolder) {
            holder.binding.txt.text = data

            holder.binding.cancel.setOnClickListener {
                list.remove(data)
                notifyDataSetChanged()
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class MyViewHolder(val binding: FrozenWordsLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)
}