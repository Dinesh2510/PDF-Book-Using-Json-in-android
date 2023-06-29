package com.pixeldev.pdfbook.activity

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView.OnEditorActionListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.volley.Request
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
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.pixeldev.pdfbook.Config
import com.pixeldev.pdfbook.Config.URL
import com.pixeldev.pdfbook.R
import com.pixeldev.pdfbook.adapter.CategoryBooksAdapter
import com.pixeldev.pdfbook.model.Image
import org.json.JSONException
import org.json.JSONObject

class BookSearch : AppCompatActivity() {
    var circularProgressIndicator: CircularProgressIndicator? = null
    var et_search: EditText? = null
    var lstAnime: MutableList<Image?>? = null
    private var mAdView: AdView? = null
    var mInterstitialAd: InterstitialAd? = null
    var recyclerView: RecyclerView? = null
    var request: JsonObjectRequest? = null
    var requestQueue: RequestQueue? = null
    var swipeRefreshLayout: SwipeRefreshLayout? = null

    /* access modifiers changed from: protected */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.book_search)
        et_search = findViewById<View>(R.id.et_search) as EditText
        MobileAds.initialize(this) { }
        mAdView = findViewById<View>(R.id.adView) as AdView
        mAdView!!.loadAd(AdRequest.Builder().build())
        mAdView!!.visibility = View.GONE
        loadInterstitialAdNetwork()
        showInterstitialAdNetwork()
        circularProgressIndicator =
            findViewById<View>(R.id.circular_indicator) as CircularProgressIndicator
        val swipeRefreshLayout2 = findViewById<View>(R.id.swiperefreshlayout) as SwipeRefreshLayout
        swipeRefreshLayout = swipeRefreshLayout2
        swipeRefreshLayout2.setOnRefreshListener {
            val bookSearch = this@BookSearch
            bookSearch.initComponent(bookSearch.lstAnime)
            swipeRefreshLayout!!.isRefreshing = false
        }
        lstAnime = ArrayList<Image?>()
        recyclerView = findViewById<View>(R.id.recyclerView) as RecyclerView
        val swipeRefreshLayout3 = findViewById<View>(R.id.swiperefreshlayout) as SwipeRefreshLayout
        swipeRefreshLayout = swipeRefreshLayout3
        swipeRefreshLayout3.setOnRefreshListener {
            val bookSearch = this@BookSearch
            bookSearch.initComponent(bookSearch.lstAnime)
            swipeRefreshLayout!!.isRefreshing = false
        }
        et_search!!.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            if (actionId != 3) {
                return@OnEditorActionListener false
            }
            hideKeyboard()
            jsonRequest()
            true
        })
        jsonRequest()
    }

    fun hideKeyboard() {
        val view = currentFocus
        if (view != null) {
            (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(
                view.windowToken,
                0
            )
        }
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
        Handler(Looper.getMainLooper()).postDelayed({ showAds() }, 1000)
    }

    fun showAds() {
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

    /* access modifiers changed from: protected */
    fun jsonRequest() {
        circularProgressIndicator!!.visibility = View.VISIBLE
        lstAnime!!.clear()
        Handler().postDelayed({
            request = JsonObjectRequest(
                Request.Method.POST,
                URL, null as JSONObject?,
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
                            image.category = book_item.getString("categories")
                            if (!book_item.getString("book_name").lowercase()
                                    .contains(et_search!!.text.toString().lowercase())
                            ) {
                                if (!book_item.getString("author").lowercase()
                                        .contains(et_search!!.text.toString().lowercase())
                                ) {
                                    Toast.makeText(this@BookSearch, "No Found", Toast.LENGTH_LONG)
                                    circularProgressIndicator!!.visibility = View.GONE
                                    Toast.makeText(this@BookSearch, "No Found", Toast.LENGTH_LONG)
                                    swipeRefreshLayout!!.isRefreshing = false
                                }
                            }
                            lstAnime!!.add(image)
                            recyclerView!!.visibility = View.VISIBLE
                            circularProgressIndicator!!.visibility = View.GONE
                            Toast.makeText(this@BookSearch, "No Found", Toast.LENGTH_LONG)
                            swipeRefreshLayout!!.isRefreshing = false
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        circularProgressIndicator!!.visibility = View.VISIBLE
                    }
                    initComponent(lstAnime)
                }) { circularProgressIndicator!!.visibility = View.VISIBLE }
            val unused = request
            val bookSearch = this@BookSearch
            bookSearch.requestQueue = Volley.newRequestQueue(bookSearch.applicationContext)
            val unused2 = bookSearch.requestQueue
            requestQueue!!.add(request)
        }, 1000)
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
        initComponent(lstAnime)
        swipeRefreshLayout!!.isRefreshing = false
        super.onStart()
    }

    companion object {
        const val SHARED_PREFS = "sharedPrefs"
        const val TEXT = "text"
    }
}