package com.pixeldev.pdfbook.adapter

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.BaseRequestOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.pixeldev.pdfbook.R
import com.pixeldev.pdfbook.activity.BookCategory
import com.pixeldev.pdfbook.model.Image

class CategoryAdapter(val mContext: Context, var mData: List<Image>) :
    RecyclerView.Adapter<CategoryAdapter.MyViewHolder?>() {
    var option = RequestOptions().centerCrop()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val viewHolder = MyViewHolder(
            LayoutInflater.from(
                mContext
            ).inflate(R.layout.category, parent, false)
        )
        viewHolder.category_card.setOnClickListener {
            val i = Intent(mContext, BookCategory::class.java)
            i.putExtra("cat_name", mData[viewHolder.adapterPosition].cat_name)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            mContext.startActivity(i)
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        (Glide.with(mContext).load(mData[position].image_url).error(R.drawable.loading_shape)
            .listener(object : RequestListener<Drawable?> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any,
                    target: Target<Drawable?>,
                    isFirstResource: Boolean,
                ): Boolean {
                    holder.circularProgressIndicator.visibility = View.GONE
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any,
                    target: Target<Drawable?>,
                    dataSource: DataSource,
                    isFirstResource: Boolean,
                ): Boolean {
                    holder.circularProgressIndicator.visibility = View.GONE
                    return false
                }
            }).apply((option as BaseRequestOptions<*>)).centerCrop() as RequestBuilder<*>).into(
            (holder.img_thumbnail)
        )
        holder.cat_name.text = mData[position].cat_name
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var cat_name: TextView
        var category_card: MaterialCardView
        var circularProgressIndicator: CircularProgressIndicator
        var img_thumbnail: ImageView

        init {
            cat_name = itemView.findViewById<View>(R.id.cat_name) as TextView
            category_card = itemView.findViewById<View>(R.id.category_card) as MaterialCardView
            img_thumbnail = itemView.findViewById<View>(R.id.thumbnail) as ImageView
            circularProgressIndicator =
                itemView.findViewById<View>(R.id.circular_indicator) as CircularProgressIndicator
        }
    }
}