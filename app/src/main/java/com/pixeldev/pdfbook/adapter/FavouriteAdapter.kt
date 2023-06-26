package com.pixeldev.pdfbook.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.request.BaseRequestOptions
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView
import com.pixeldev.pdfbook.R
import com.pixeldev.pdfbook.activity.BookDetails
import com.pixeldev.pdfbook.helper.DatabaseHelper
import com.pixeldev.pdfbook.helper.SharedPref

class FavouriteAdapter(
    var context: Context,
    private val book_id: ArrayList<*>,
    val book_title: ArrayList<*>,
    val book_author: ArrayList<*>,
    val book_thumb: ArrayList<*>,
    val book_url: ArrayList<*>,
    val book_desc: ArrayList<*>,
    val book_feature: ArrayList<*>,
    val book_category: ArrayList<*>
) : RecyclerView.Adapter<FavouriteAdapter.MyViewHolder>() {
    var option = RequestOptions().fitCenter().placeholder(R.drawable.loading_shape)
        .error(R.drawable.loading_shape)
    var sharedPref: SharedPref? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(
                context
            ).inflate(R.layout.book_item_fav, parent, false)
        )
    }

    override fun onBindViewHolder(
        holder: MyViewHolder,
        @SuppressLint("RecyclerView") position: Int
    ) {
        val i = position
        (Glide.with(context).load(book_thumb[position].toString())
            .apply((option as BaseRequestOptions<*>))
            .centerCrop() as RequestBuilder<*>).into(holder.book_thumb_img)
        holder.book_title_txt.text = book_title[position].toString()
        holder.book_author_txt.text = book_author[position].toString()
        holder.book_desc.text = book_desc[position].toString()
        holder.material_books_card.setOnClickListener {
            val i = Intent(context, BookDetails::class.java)
            i.putExtra("author", book_author[position].toString())
            i.putExtra("book_thumb", book_thumb[position].toString())
            i.putExtra("book_name", book_title[position].toString())
            i.putExtra("book_url", book_url[position].toString())
            i.putExtra("book_description", book_desc[position].toString())
            i.putExtra("is_featured", book_feature[position].toString())
            i.putExtra("category", book_category[position].toString())
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(i)
        }
        holder.fav_del_btn.setOnClickListener {
            DatabaseHelper(context).deleteOneRowbyName(book_title[position].toString())
            notifytoview(holder.adapterPosition)
        }
        val sharedPref2 = SharedPref(context)
        sharedPref = sharedPref2
        if (sharedPref2.loadNightModeState()) {
            holder.container.background =
                context.resources.getDrawable(R.drawable.container_bg_dark)
            holder.fav_del_btn.backgroundTintList =
                ContextCompat.getColorStateList(context, R.color.md_theme_dark_primary)
            return
        }
        holder.container.background = context.resources.getDrawable(R.drawable.container_bg)
        holder.fav_del_btn.backgroundTintList =
            ContextCompat.getColorStateList(context, R.color.md_theme_light_primary)
    }

    fun notifytoview(position: Int) {
        book_id.removeAt(position)
        notifyItemRemoved(position)
    }

    override fun getItemCount(): Int {
        return book_id.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var book_author_txt: MaterialTextView
        var book_desc: MaterialTextView
        var book_thumb_img: ImageView
        var book_title_txt: MaterialTextView
        var container: ShapeableImageView
        var fav_del_btn: MaterialButton
        var material_books_card: MaterialCardView

        init {
            book_title_txt = itemView.findViewById<View>(R.id.book_name) as MaterialTextView
            book_author_txt = itemView.findViewById<View>(R.id.author_name) as MaterialTextView
            this.book_desc = itemView.findViewById<View>(R.id.book_desc) as MaterialTextView
            book_thumb_img = itemView.findViewById<View>(R.id.book_thumb) as ImageView
            material_books_card = itemView.findViewById<View>(R.id.books_card) as MaterialCardView
            fav_del_btn = itemView.findViewById<View>(R.id.fav_del_Btn) as MaterialButton
            container = itemView.findViewById<View>(R.id.container) as ShapeableImageView
        }
    }
}