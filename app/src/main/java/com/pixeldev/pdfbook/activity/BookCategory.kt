package com.pixeldev.pdfbook.activity

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.pixeldev.pdfbook.Config
import com.pixeldev.pdfbook.R
import com.pixeldev.pdfbook.adapter.CategoryBooksAdapter
import com.pixeldev.pdfbook.model.Image
import org.json.JSONException
import org.json.JSONObject
import java.util.Arrays

class BookCategory : AppCompatActivity() {
    var cat_name: String? = null
    var circularProgressIndicator: CircularProgressIndicator? = null
    var lstAnime: MutableList<Image?>? = null
    private var mAdView: AdView? = null
    var mInterstitialAd: InterstitialAd? = null
    var materialToolbar: MaterialToolbar? = null
    private var recyclerView: RecyclerView? = null
    private var request: JsonObjectRequest? = null
    private var requestQueue: RequestQueue? = null
    var swipeRefreshLayout: SwipeRefreshLayout? = null

    /* access modifiers changed from: protected */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_category)
        MobileAds.initialize(this) { }
        mAdView = findViewById<View>(R.id.adView) as AdView
        mAdView!!.loadAd(AdRequest.Builder().build())
        loadInterstitialAdNetwork()
        showInterstitialAdNetwork()
        circularProgressIndicator =
            findViewById<View>(R.id.circular_indicator) as CircularProgressIndicator
        val swipeRefreshLayout2 = findViewById<View>(R.id.swiperefreshlayout) as SwipeRefreshLayout
        swipeRefreshLayout = swipeRefreshLayout2
        swipeRefreshLayout2.setOnRefreshListener {
            val bookCategory = this@BookCategory
            bookCategory.initComponent(bookCategory.lstAnime)
            swipeRefreshLayout!!.isRefreshing = false
        }
        cat_name = intent.getStringExtra("cat_name")
        val materialToolbar2 = findViewById<View>(R.id.toolbar) as MaterialToolbar
        materialToolbar = materialToolbar2
        materialToolbar2.title = cat_name
        this.title = cat_name

        materialToolbar!!.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
        lstAnime = ArrayList<Image?>()
        recyclerView = findViewById<View>(R.id.recyclerView) as RecyclerView
        val swipeRefreshLayout3 = findViewById<View>(R.id.swiperefreshlayout) as SwipeRefreshLayout
        swipeRefreshLayout = swipeRefreshLayout3
        swipeRefreshLayout3.setOnRefreshListener {
            val bookCategory = this@BookCategory
            bookCategory.initComponent(bookCategory.lstAnime)
            swipeRefreshLayout!!.isRefreshing = false
        }
        jsonRequest()
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

    private fun showInterstitialAdNetwork() {
        Handler(Looper.getMainLooper()).postDelayed({ showAdsInter() }, 1000)
    }

    private fun showAdsInter() {
        if (mInterstitialAd != null) {
            val sharedPrefere = getSharedPreferences("sharedPrefs", 0)
            val counter = sharedPrefere.getString("text", "1")!!.toInt()
            if (counter == 4) {
                mInterstitialAd!!.show(this)
                sharedPrefere.edit().clear().apply()
                return
            }
            val editor = getSharedPreferences("sharedPrefs", 0).edit()
            editor.putString("text", (counter + 1).toString())
            editor.apply()
        }
    }

    fun jsonRequest() {
        request = JsonObjectRequest(
            0,
            Config.URL,
            null as JSONObject?,
            { response ->
                try {
                    val jsonArray = response!!.getJSONArray("Books")
                    for (i in 0 until jsonArray.length()) {
                        val book_item = jsonArray.getJSONObject(i)
                        val image = Image()
                        image.author_name = book_item.optJSONArray("author")?.get(0).toString()
                        image.book_thumb = book_item.optString("book_thumb")
                        image.book_name = book_item.optString("book_name")
                        image.book_url = book_item.optString("book_url")
                        image.book_description = book_item.optString("book_description")
                        image.is_featured = (book_item.optBoolean("status"))
                        image.category = book_item.getString("categories").toString()
                        if (Arrays.asList(
                                *book_item.getString("categories").split(",".toRegex())
                                    .dropLastWhile { it.isEmpty() }
                                    .toTypedArray()).contains(cat_name)
                        ) {
                            lstAnime!!.add(image)
                        }
                        circularProgressIndicator!!.visibility = View.GONE
                        swipeRefreshLayout!!.isRefreshing = false
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    circularProgressIndicator!!.visibility = View.VISIBLE
                }
                val bookCategory = this@BookCategory
                bookCategory.initComponent(bookCategory.lstAnime)
            }) { circularProgressIndicator!!.visibility = View.VISIBLE }
        val newRequestQueue = Volley.newRequestQueue(applicationContext)
        requestQueue = newRequestQueue
        newRequestQueue.add(request)
    }

    fun initComponent(lstAnime2: List<Image?>?) {
        val adapter = CategoryBooksAdapter(applicationContext, lstAnime2 as List<Image>)
        recyclerView!!.layoutManager = GridLayoutManager(this, 1)
        recyclerView!!.setHasFixedSize(true)
        recyclerView!!.adapter = adapter
        adapter.notifyDataSetChanged()
    }

    /* access modifiers changed from: protected */
    public override fun onStart() {
        swipeRefreshLayout!!.isRefreshing = false
        super.onStart()
    }

    companion object {
        const val SHARED_PREFS = "sharedPrefs"
        const val TEXT = "text"
    }
}