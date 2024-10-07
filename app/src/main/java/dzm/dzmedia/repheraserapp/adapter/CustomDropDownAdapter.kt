package dzm.dzmedia.repheraserapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.pixplicity.easyprefs.library.Prefs
import dzm.dzmedia.repheraserapp.R
import dzm.dzmedia.repheraserapp.activities.MainActivity
import dzm.dzmedia.repheraserapp.databinding.FragmentRephraseParagraphBinding
import dzm.dzmedia.repheraserapp.helpers.Constants
import dzm.dzmedia.repheraserapp.interfaces.CustomSpinnerListener
import dzm.dzmedia.repheraserapp.model.CustomSpinnerModel

class CustomDropDownAdapter(
    val context: Context, var dataSource: List<CustomSpinnerModel>
) : BaseAdapter() {

    var view: View? = null
    private val inflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int {
        return dataSource.size
    }

    override fun getItem(p0: Int): Any {
        return dataSource[p0]
    }

    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    override fun getView(position: Int, view: View?, parent: ViewGroup?): View {
        val view1: View
        val holder: ItemHolder
        if (view == null) {
            view1 = inflater.inflate(R.layout.spinner_item, parent, false)
            holder = ItemHolder(view1)
            view1?.tag = holder
        } else {
            view1 = view
            holder = view1.tag as ItemHolder
        }

        if (position == 0 || position == 1 || position == 2) {
            holder.img.visibility = View.GONE
        } else {
            holder.img.visibility = View.VISIBLE
        }
        if (Prefs.getBoolean(Constants.IS_LOGIN, false)) {
            if (MainActivity.userModel!!.subscription == "3 Days Trial" && !MainActivity.userModel!!.subscribed)
                holder.img.visibility = View.GONE

            if (MainActivity.userModel!!.subscribed)
                holder.img.visibility = View.GONE
        }

        this.view = view1

        holder.label.text = dataSource[position].name
        val id = context.resources.getIdentifier(
            dataSource[position].img, "drawable", context.packageName
        )
        holder.img.setBackgroundResource(id)
        return view1
    }

    private class ItemHolder(row: View?) {
        val label: TextView
        val img: ImageView

        init {
            label = row?.findViewById(R.id.textview) as TextView
            img = row?.findViewById(R.id.spinner_IV) as ImageView
        }
    }
}