package com.pixeldev.pdfbook.activity

import android.app.DownloadManager
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.webkit.CookieManager
import android.webkit.URLUtil
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.FragmentActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textview.MaterialTextView
import com.pixeldev.pdfbook.R
import com.pixeldev.pdfbook.helper.DatabaseHelper

class BookDetails : AppCompatActivity() {
    var author_name: MaterialTextView? = null
    var author_name_str: String? = null
    var book_name: MaterialTextView? = null
    var book_name_str: String? = null
    var book_url: String? = null
    var category: String? = null
    var circularProgressIndicator: CircularProgressIndicator? = null
    var description_detail: MaterialTextView? = null
    var description_detail_str: String? = null
    var favBtn: CheckBox? = null
    var is_featured: String? = null
    private var mAdView: AdView? = null
    var mInterstitialAd: InterstitialAd? = null
    private var materialToolbar: MaterialToolbar? = null
    var nested_scroll: NestedScrollView? = null
    var pages: MaterialTextView? = null
    var shapeableImageView: ShapeableImageView? = null
    var shapeable_imageview_str: String? = null

    /* access modifiers changed from: protected */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_details)
        MobileAds.initialize(this) { }
        mAdView = findViewById<View>(R.id.adView) as AdView
        mAdView!!.loadAd(AdRequest.Builder().build())
        loadInterstitialAdNetwork()
        showInterstitialAdNetwork()
        nested_scroll = findViewById<View>(R.id.nested_scroll) as NestedScrollView
        setTheme(R.style.AppTheme)
        shapeableImageView = findViewById<View>(R.id.image_view) as ShapeableImageView
        setSupportActionBar(materialToolbar)
        book_name = findViewById<View>(R.id.book_name) as MaterialTextView
        author_name = findViewById<View>(R.id.author_name) as MaterialTextView
        shapeableImageView = findViewById<View>(R.id.image_view) as ShapeableImageView
        description_detail = findViewById<View>(R.id.description_detail) as MaterialTextView
        favBtn = findViewById<View>(R.id.fav_check) as CheckBox
        circularProgressIndicator =
            findViewById<View>(R.id.circular_indicator) as CircularProgressIndicator
        val `is` = intent
        book_name_str = `is`.getStringExtra("book_name")
        author_name_str = `is`.getStringExtra("author")
        shapeable_imageview_str = `is`.getStringExtra("book_thumb")
        description_detail_str = `is`.getStringExtra("book_description")
        book_url = `is`.getStringExtra("book_url")
        is_featured = `is`.getStringExtra("is_featured")
        category = `is`.getStringExtra("category")
        val is_featured_boolean = java.lang.Boolean.parseBoolean(is_featured)
        book_name!!.text = book_name_str
        val materialTextView = author_name
        materialTextView!!.text = "By $author_name_str"
        description_detail!!.text = description_detail_str
        val materialToolbar2 = findViewById<View>(R.id.toolbar) as MaterialToolbar
        materialToolbar = materialToolbar2
        materialToolbar2.setNavigationOnClickListener { onBackPressed() }
        findViewById<View>(R.id.floatingActionButton_read).setOnClickListener {
            showInterstitialAdNetwork()
            val intent = Intent(this@BookDetails, PDFActivity::class.java)
            intent.putExtra("book_url", book_url)
            startActivity(intent)
        }
        findViewById<View>(R.id.floatingActionButton).setOnClickListener {
            showInterstitialAdNetwork()
            val request = DownloadManager.Request(Uri.parse(book_url))
            val title = URLUtil.guessFileName(book_url, null as String?, null as String?)
            request.setTitle(book_name_str)
            request.setDescription("Downloading book")
            request.addRequestHeader("cookie", CookieManager.getInstance().getCookie(book_url))
            request.setNotificationVisibility(1)
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOCUMENTS, title)
            (getSystemService(DOWNLOAD_SERVICE) as DownloadManager).enqueue(request)
            Snackbar.make(
                (nested_scroll as View?)!!,
                ("Downloading Started..." as CharSequence),
                BaseTransientBottomBar.LENGTH_LONG
            ).show()
        }
        findViewById<View>(R.id.btn_share).setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_SEND
            intent.putExtra( Intent.EXTRA_SUBJECT, getString(R.string.app_name))
            intent.putExtra(
                Intent.EXTRA_TEXT, """
     ${getString(R.string.share_text)}
     https://play.google.com/store/apps/details?id=$packageName
     """.trimIndent()
            )
            intent.type = "text/plain"
            startActivity(intent)
        }
        Glide.with((this as FragmentActivity))
            .load(shapeable_imageview_str)
            .error(R.drawable.loading_shape)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean,
                ): Boolean {
                    circularProgressIndicator!!.visibility = View.GONE
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean,
                ): Boolean {
                    circularProgressIndicator!!.visibility = View.GONE
                    return false
                }
            })
            .into((shapeableImageView as ImageView?)!!)
        val myDB = DatabaseHelper(this)
        val str = `is`.getStringExtra("book_name")!!.trim { it <= ' ' }
        val cursor = myDB.readAllData()
        while (true) {
            if (!cursor!!.moveToNext()) {
                break
            } else if (str == cursor.getString(1)) {
                favBtn!!.buttonDrawable =
                    resources.getDrawable(R.drawable.ic_favorite_vd_theme_24px)
                favBtn!!.isChecked = false
                break
            } else {
                favBtn!!.buttonDrawable =
                    resources.getDrawable(R.drawable.ic_baseline_favorite_border_24)
            }
        }
        favBtn!!.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                favBtn!!.buttonDrawable =
                    resources.getDrawable(R.drawable.ic_baseline_favorite_border_24)
                DatabaseHelper(this@BookDetails).deleteOneRowbyName(book_name_str!!)
                return@OnCheckedChangeListener
            }
            DatabaseHelper(this@BookDetails).addBook(
                book_name_str,
                author_name_str,
                shapeable_imageview_str,
                book_url,
                description_detail_str,
                java.lang.Boolean.valueOf(is_featured_boolean),
                category
            )
            favBtn!!.buttonDrawable = resources.getDrawable(R.drawable.ic_favorite_vd_theme_24px)
        })
    }

    fun loadInterstitialAdNetwork() {
        InterstitialAd.load(
            this,
            resources.getString(R.string.admob_interstitial_id),
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    mInterstitialAd = interstitialAd
                    val unused = mInterstitialAd
                    mInterstitialAd!!.fullScreenContentCallback =
                        object : FullScreenContentCallback() {
                            override fun onAdDismissedFullScreenContent() {
                                loadInterstitialAdNetwork()
                            }

                            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                Log.d("Interstitial", "The ad failed to show.")
                            }

                            override fun onAdShowedFullScreenContent() {
                                mInterstitialAd = null
                                val unused = mInterstitialAd
                                Log.d("Interstitial", "The ad was shown.")
                            }
                        }
                    Log.i("Interstitial", "onAdLoaded")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.i("Interstitial", loadAdError.message)
                    mInterstitialAd = null
                    val unused = mInterstitialAd
                    Log.d("Interstitial", "Failed load AdMob Interstitial Ad")
                }
            })
    }

    fun showInterstitialAdNetwork() {
        Handler(Looper.getMainLooper()).postDelayed({ showAds() }, 1000)
    }

    fun showAds() {
        if (mInterstitialAd != null) {
            val sharedPrefere = getSharedPreferences(SHARED_PREFS, 0)
            val counter = sharedPrefere.getString("text", "1")!!.toInt()
            if (counter == 4) {
                mInterstitialAd!!.show(this)
                sharedPrefere.edit().clear().apply()
                return
            }
            val editor = getSharedPreferences(SHARED_PREFS, 0).edit()
            editor.putString("text", (counter + 1).toString())
            editor.apply()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    companion object {
        const val SHARED_PREFS = "sharedPrefsDetail"
        const val TEXT = "text"
    }
}