package com.denzcoskun.imageslider.adapters

import android.content.Context
import androidx.viewpager.widget.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.denzcoskun.imageslider.R
import com.denzcoskun.imageslider.interfaces.ItemClickListener
import com.denzcoskun.imageslider.models.SlideModel
import com.denzcoskun.imageslider.transformation.RoundedTransformation


class ViewPagerAdapter(context: Context?, imageList: List<SlideModel>, private var radius: Int, private var errorImage: Int, private var placeholder: Int, private var centerCrop: Boolean?) : PagerAdapter() {

    private var imageList: List<SlideModel>? = imageList
    private var layoutInflater: LayoutInflater? = context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater?
    private var itemClickListener: ItemClickListener? = null
    private var context = context!!

    override fun isViewFromObject(view: View, obj: Any): Boolean {
        return view == obj
    }

    override fun getCount(): Int {
        return imageList!!.size
    }

    override fun instantiateItem(container: ViewGroup, position: Int): View{
        val itemView = layoutInflater!!.inflate(R.layout.pager_row, container, false)
        val imageView = itemView.findViewById<ImageView>(R.id.image_view)
        val linearLayout = itemView.findViewById<LinearLayout>(R.id.linear_layout)
        val textView = itemView.findViewById<TextView>(R.id.text_view)

        if (imageList!![position].title != null){
            textView.text = imageList!![position].title
        }else{
            linearLayout.visibility = View.INVISIBLE
        }

        getPicasso(imageView, position)
        container.addView(itemView)
        imageView.setOnClickListener{itemClickListener?.onItemSelected(position)}

        return itemView
    }

    fun setItemClickListener(itemClickListener: ItemClickListener) {
        this.itemClickListener = itemClickListener
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as RelativeLayout)
    }

    fun getPicasso(imageView: ImageView, position: Int){
        if(imageList!![position].imageUrl == null){
            if(centerCrop!! || imageList!![position].centerCrop){
                Glide.with(context)
                    .load(imageList!![position].imagePath!!) // Int
                    .centerCrop()
                    .placeholder(placeholder)
                    .error(errorImage)
                    .apply( RequestOptions().transform(RoundedCorners(radius)))
                    .into(imageView)
            }else {
                Glide.with(context)
                    .load(imageList!![position].imagePath!!) // Int
                    .placeholder(placeholder)
                    .error(errorImage)
                    .apply( RequestOptions().transform( RoundedCorners(radius)))
                    .into(imageView)
            }
        }else{
            if(centerCrop!! || imageList!![position].centerCrop) {
                Glide.with(context)
                    .load(imageList!![position].imageUrl!!) // String
                    .centerCrop()
                    .placeholder(placeholder)
                    .error(errorImage)
                    .apply( RequestOptions().transform( RoundedCorners(radius)))
                    .into(imageView)
            }else {
                Glide.with(context)
                    .load(imageList!![position].imageUrl!!) // String
                    .placeholder(placeholder)
                    .error(errorImage)
                    .apply( RequestOptions().transform( RoundedCorners(radius)))
                    .into(imageView)
            }
        }
    }
}