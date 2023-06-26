package com.pixeldev.pdfbook.adapter

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.BaseRequestOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAd.OnNativeAdLoadedListener
import com.google.android.gms.ads.nativead.NativeAdView
import com.pixeldev.pdfbook.R
import com.pixeldev.pdfbook.activity.BookDetails
import com.pixeldev.pdfbook.databinding.BookItemBinding
import com.pixeldev.pdfbook.databinding.ItemNativeAdBinding
import com.pixeldev.pdfbook.helper.DatabaseHelper
import com.pixeldev.pdfbook.helper.SharedPref
import com.pixeldev.pdfbook.model.Image

class BooksAdapter(val mContext: Context, var mData: List<Image>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var option = RequestOptions().centerCrop()
    var sharedPref: SharedPref? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_VIEW -> MainViewHolder(
                LayoutInflater.from(mContext).inflate(R.layout.book_item, parent, false)
            )

            AD_VIEW -> AdViewHolder(
                LayoutInflater.from(mContext).inflate(R.layout.item_native_ad, parent, false)
            )

            else -> throw IllegalArgumentException("Unsupported layout") // in case populated with a model we don't know how to display.
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder.itemViewType == ITEM_VIEW) {
            (holder as MainViewHolder).bindData(mData[position - Math.round((position / ITEM_FEED_COUNT).toFloat())])
        } else if (holder.itemViewType == AD_VIEW) {
            (holder as AdViewHolder).bindAdData()
        }
    }

    override fun getItemCount(): Int {
        return if (mData.size > 0) {
            mData.size + Math.round((mData.size / ITEM_FEED_COUNT).toFloat())
        } else mData.size
    }

    override fun getItemViewType(position: Int): Int {
        return if ((position + 1) % ITEM_FEED_COUNT == 0) {
            AD_VIEW
        } else ITEM_VIEW
    }

    private fun populateNativeADView(nativeAd: NativeAd, adView: NativeAdView) {
        // Set the media view.
        adView.mediaView =
            adView.findViewById(R.id.ad_media)

        // Set other ad assets.
        adView.headlineView = adView.findViewById(R.id.ad_headline)
        adView.bodyView = adView.findViewById(R.id.ad_body)
        adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
        adView.iconView = adView.findViewById(R.id.ad_app_icon)
        adView.priceView = adView.findViewById(R.id.ad_price)
        adView.starRatingView = adView.findViewById(R.id.ad_stars)
        adView.storeView = adView.findViewById(R.id.ad_store)
        adView.advertiserView = adView.findViewById(R.id.ad_advertiser)

        // The headline and mediaContent are guaranteed to be in every UnifiedNativeAd.
        (adView.headlineView as TextView?)!!.text = nativeAd.headline
        adView.mediaView!!.mediaContent = nativeAd.mediaContent

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        if (nativeAd.body == null) {
            adView.bodyView!!.visibility = View.INVISIBLE
        } else {
            adView.bodyView!!.visibility = View.VISIBLE
            (adView.bodyView as TextView?)!!.text = nativeAd.body
        }
        if (nativeAd.callToAction == null) {
            adView.callToActionView!!.visibility = View.INVISIBLE
        } else {
            adView.callToActionView!!.visibility = View.VISIBLE
            (adView.callToActionView as Button?)!!.text = nativeAd.callToAction
        }
        if (nativeAd.icon == null) {
            adView.iconView!!.visibility = View.GONE
        } else {
            (adView.iconView as ImageView?)!!.setImageDrawable(
                nativeAd.icon!!.drawable
            )
            adView.iconView!!.visibility = View.VISIBLE
        }
        if (nativeAd.price == null) {
            adView.priceView!!.visibility = View.INVISIBLE
        } else {
            adView.priceView!!.visibility = View.VISIBLE
            (adView.priceView as TextView?)!!.text = nativeAd.price
        }
        if (nativeAd.store == null) {
            adView.storeView!!.visibility = View.INVISIBLE
        } else {
            adView.storeView!!.visibility = View.VISIBLE
            (adView.storeView as TextView?)!!.text = nativeAd.store
        }
        if (nativeAd.starRating == null) {
            adView.starRatingView!!.visibility = View.INVISIBLE
        } else {
            (adView.starRatingView as RatingBar?)!!.rating = nativeAd.starRating!!.toFloat()
            adView.starRatingView!!.visibility = View.VISIBLE
        }
        if (nativeAd.advertiser == null) {
            adView.advertiserView!!.visibility = View.INVISIBLE
        } else {
            (adView.advertiserView as TextView?)!!.text = nativeAd.advertiser
            adView.advertiserView!!.visibility = View.VISIBLE
        }

        // This method tells the Google Mobile Ads SDK that you have finished populating your
        // native ad view with this native ad.
        adView.setNativeAd(nativeAd)
    }

    inner class MainViewHolder(itemView: View?) : RecyclerView.ViewHolder(
        (itemView)!!
    ) {
        var binding: BookItemBinding

        init {
            binding = BookItemBinding.bind((itemView)!!)
        }

        fun bindData(main: Image) {
            binding.bookName.text = main.book_name
            binding.authorName.text = main.author_name
            binding.bookDesc.text = main.book_description
            (Glide.with(mContext).load(main.book_thumb)
                .listener(object : RequestListener<Drawable?> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any,
                        target: Target<Drawable?>,
                        isFirstResource: Boolean,
                    ): Boolean {
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any,
                        target: Target<Drawable?>,
                        dataSource: DataSource,
                        isFirstResource: Boolean,
                    ): Boolean {
                        return false
                    }
                }).apply((option as BaseRequestOptions<*>)).centerCrop() as RequestBuilder<*>).into(
                (binding.bookThumb as ImageView)
            )
            binding.booksCard.setOnClickListener(View.OnClickListener {
                val i = Intent(mContext, BookDetails::class.java)
                i.putExtra("author", main.author_name)
                i.putExtra("book_thumb", main.book_thumb)
                i.putExtra("book_name", main.book_name)
                i.putExtra("book_url", main.book_url)
                i.putExtra("book_description", main.book_description)
                i.putExtra("is_featured", main.is_featured)
                i.putExtra("category", main.category)
                i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                mContext.startActivity(i)
            })
            sharedPref = SharedPref(mContext)
            if (sharedPref!!.loadNightModeState()) {
                binding.container.background =
                    mContext.resources.getDrawable(R.drawable.container_bg_dark)
            } else {
                binding.container.background =
                    mContext.resources.getDrawable(R.drawable.container_bg)
            }
            val myDB = DatabaseHelper(mContext)
            val str = main.book_name!!.trim { it <= ' ' }
            val cursor = myDB.readAllData()
            while (true) {
                if (!cursor!!.moveToNext()) {
                    break
                } else if ((str == cursor.getString(1))) {
                    if (sharedPref!!.loadNightModeState()) {
                        binding.favBtn.buttonDrawable =
                            mContext.resources.getDrawable(R.drawable.ic_favorite_vd_theme_24px)
                    } else {
                        binding.favBtn.buttonDrawable =
                            mContext.resources.getDrawable(R.drawable.iv_favorite_vd_theme_light)
                    }
                    binding.favBtn.isChecked = false
                } else if (sharedPref!!.loadNightModeState()) {
                    binding.favBtn.buttonDrawable =
                        mContext.resources.getDrawable(R.drawable.ic_baseline_favorite_border_24)
                } else {
                    binding.favBtn.buttonDrawable =
                        mContext.resources.getDrawable(R.drawable.ic_baseline_faorite_border_white)
                }
            }
            binding.favBtn.setOnCheckedChangeListener(object :
                CompoundButton.OnCheckedChangeListener {
                override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
                    if (isChecked) {
                        if (sharedPref!!.loadNightModeState()) {
                            binding.favBtn.buttonDrawable =
                                mContext.resources.getDrawable(R.drawable.ic_baseline_favorite_border_24)
                        } else {
                            binding.favBtn.buttonDrawable =
                                mContext.resources.getDrawable(R.drawable.ic_baseline_faorite_border_white)
                        }
                        DatabaseHelper(mContext).deleteOneRowbyName(main.book_name!!)
                        return
                    }
                    DatabaseHelper(mContext).addBook(
                        main.book_name!!.trim { it <= ' ' },
                        main.author_name!!.trim { it <= ' ' },
                        main.book_thumb!!.trim { it <= ' ' },
                        main.book_url!!.trim { it <= ' ' },
                        main.book_description!!.trim { it <= ' ' },
                        main.is_featured,
                        main.category!!.trim { it <= ' ' })
                    if (sharedPref!!.loadNightModeState()) {
                        binding.favBtn.buttonDrawable =
                            mContext.resources.getDrawable(R.drawable.ic_favorite_vd_theme_24px)
                    } else {
                        binding.favBtn.buttonDrawable =
                            mContext.resources.getDrawable(R.drawable.iv_favorite_vd_theme_light)
                    }
                }
            })
        }
    }

    inner class AdViewHolder(itemView: View?) : RecyclerView.ViewHolder(
        (itemView)!!
    ) {
        var binding: ItemNativeAdBinding

        init {
            binding = ItemNativeAdBinding.bind((itemView)!!)
        }

        fun bindAdData() {
            val builder = AdLoader.Builder(mContext, "ca-app-pub-3940256099942544/2247696110")
                .forNativeAd(object : OnNativeAdLoadedListener {
                    override fun onNativeAdLoaded(nativeAd: NativeAd) {
                        val layoutInflater = LayoutInflater.from(mContext)
                        val nativeAdView =
                            layoutInflater.inflate(R.layout.layout_native_ad, null) as NativeAdView
                        populateNativeADView(nativeAd, nativeAdView)
                        binding.adLayout.removeAllViews()
                        binding.adLayout.addView(nativeAdView)
                    }
                })
            val adLoader = builder.withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    super.onAdFailedToLoad(loadAdError)
                    Toast.makeText(mContext, loadAdError.message, Toast.LENGTH_SHORT).show()
                }
            }).build()
            adLoader.loadAd(AdRequest.Builder().build())
        }
    }

    private val isNetworkConnected: Boolean
        private get() {
            val cm = mContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            return cm.activeNetworkInfo != null && cm.activeNetworkInfo!!.isConnected
        }

    companion object {
        private val ITEM_VIEW = 0
        private val AD_VIEW = 1
        private val ITEM_FEED_COUNT = 5
    }
}