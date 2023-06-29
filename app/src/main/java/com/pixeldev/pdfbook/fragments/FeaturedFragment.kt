package com.pixeldev.pdfbook.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.pixeldev.pdfbook.Config
import com.pixeldev.pdfbook.Config.URL
import com.pixeldev.pdfbook.activity.MainActivity
import com.pixeldev.pdfbook.adapter.BooksAdapter
import com.pixeldev.pdfbook.databinding.FragmentFeaturedBinding
import com.pixeldev.pdfbook.model.Image
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject

class FeaturedFragment : Fragment() {
    var binding: FragmentFeaturedBinding? = null
    var lstAnime: MutableList<Image?>? = null
    var mainActivity: MainActivity? = null
    private var request: JsonObjectRequest? = null
    private var requestQueue: RequestQueue? = null
    override fun onAttach(activity: Context) {
        super.onAttach(activity)
        mainActivity = activity as MainActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val inflate = FragmentFeaturedBinding.inflate(
            layoutInflater
        )
        binding = inflate
        val view: View = inflate.root
        lstAnime = ArrayList()
        binding!!.swiperefreshlayout.setOnRefreshListener {
            val featuredFragment = this@FeaturedFragment
            featuredFragment.initComponent(featuredFragment.lstAnime)
            CoroutineScope(Dispatchers.Main).launch {
                jsonRequest()
            }
            (lstAnime as ArrayList<Image?>).clear()
            binding!!.swiperefreshlayout.isRefreshing = false
        }
        CoroutineScope(Dispatchers.Main).launch {
            jsonRequest()
        }
        return view
    }

    fun jsonRequest() {
        request = JsonObjectRequest(
            Request.Method.POST,
            URL, null as JSONObject?,
            { response ->
                try {
                    val jsonArray = response.getJSONArray("Books")
                    for (i in 0 until jsonArray.length()) {
                        val book_item = jsonArray.getJSONObject(i)
                        val image = Image()
                        image.author_name = book_item.optJSONArray("author")?.get(0).toString()
                        image.book_thumb = book_item.optString("book_thumb")
                        image.book_name = book_item.optString("book_name")
                        image.book_url = book_item.optString("book_url")
                        image.book_description = book_item.optString("book_description")
                        image.is_featured
                        image.category = book_item.getString("categories")
                        if (book_item.getBoolean("status")) {
                            lstAnime!!.add(image)
                        }
                        binding!!.circularIndicator.visibility = View.GONE
                        binding!!.swiperefreshlayout.isRefreshing = false
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    binding!!.circularIndicator.visibility = View.VISIBLE
                }
                val featuredFragment = this@FeaturedFragment
                featuredFragment.initComponent(featuredFragment.lstAnime)
            }) { error ->
            Log.e("VOLLEY_ERROR", "onErrorResponse: $error")
            binding!!.circularIndicator.visibility = View.GONE
        }
        val newRequestQueue = Volley.newRequestQueue(context)
        newRequestQueue.add(request)
    }

    fun initComponent(lstAnime2: List<Image?>?) {
        val adapter = context?.let { BooksAdapter(it, lstAnime2 as List<Image>) }
        binding!!.recyclerView.layoutManager = GridLayoutManager(activity, 1)
        binding!!.recyclerView.setHasFixedSize(true)
        binding!!.recyclerView.adapter = adapter
        adapter!!.notifyDataSetChanged()
    }

    override fun onStart() {
        binding!!.swiperefreshlayout.isRefreshing = false
        super.onStart()
    }

    companion object {
        fun newInstance(): FeaturedFragment {
            return FeaturedFragment()
        }
    }
}