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
import dzm.dzmedia.repheraserapp.databinding.UsageHistoryAdapterLayoutBinding
import dzm.dzmedia.repheraserapp.fragments.ExportTextDialogFragment
import dzm.dzmedia.repheraserapp.helpers.Utils
import dzm.dzmedia.repheraserapp.usage_history.UsageHistoryData


class UsageHistoryAdapter(
    val context: Context,
    var list: ArrayList<UsageHistoryData>,
    val binding: FragmentParaphraserSavedHistoryBinding
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(context)
        val binding = UsageHistoryAdapterLayoutBinding.inflate(inflater, parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val data = list[position]

        if (holder is MyViewHolder) {
            holder.binding.ruTitle.text = data.action_type
            holder.binding.ruTime.text = data.created_at

            holder.binding.outputText.text = data.output_txt

            holder.binding.itemNumber.text = holder.adapterPosition.toString()

            holder.binding.menuOptions.setOnClickListener { showBottomSheetDialog(list, position) }
        }
    }

    override fun getItemCount(): Int {
        return list.size

    }

    class MyViewHolder(val binding: UsageHistoryAdapterLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)

    private fun showBottomSheetDialog(list: ArrayList<UsageHistoryData>, pos: Int) {
        val bottomSheetDialog = BottomSheetDialog(context, R.style.CustomBottomSheetDialogTheme)
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_layout)
        val preview: LinearLayout? = bottomSheetDialog.findViewById(R.id.preview)
        val download: LinearLayout? = bottomSheetDialog.findViewById(R.id.download)
        download!!.setOnClickListener {
            /* upGradeToPremium()*/
            val exportTextDialogFragment = ExportTextDialogFragment(
                list[pos].output_txt, list[pos].output_txt
            )
            Utils.openDialog(
                (context as HistoryActivity).supportFragmentManager,
                exportTextDialogFragment
            )
            bottomSheetDialog.dismiss()
        }
        preview!!.setOnClickListener {
            upGradeToPremium(list, pos)
            bottomSheetDialog.dismiss()
        }
        val closeBtn: ImageView? = bottomSheetDialog.findViewById(R.id.close_btn)
        bottomSheetDialog.show()
    }


    private fun upGradeToPremium(list: ArrayList<UsageHistoryData>, pos: Int) {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.preview_dialog)
        dialog.setCancelable(true)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        val saveAsWord = dialog.findViewById<EditText>(R.id.rephrasedET)
        saveAsWord.setText(list[pos].output_txt)
        dialog.show()
    }


    @SuppressLint("NotifyDataSetChanged")
    fun updateList(filterList: ArrayList<UsageHistoryData>) {
        list = filterList
        notifyDataSetChanged()
    }
}