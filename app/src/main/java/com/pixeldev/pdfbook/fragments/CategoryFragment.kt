package com.pixeldev.pdfbook.fragments

import android.content.Context
import android.os.Bundle
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
import com.pixeldev.pdfbook.activity.MainActivity
import com.pixeldev.pdfbook.adapter.CategoryAdapter
import com.pixeldev.pdfbook.databinding.FragmentCategoryBinding
import com.pixeldev.pdfbook.model.Image
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject

class CategoryFragment : Fragment() {
    var binding: FragmentCategoryBinding? = null
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
        val inflate = FragmentCategoryBinding.inflate(
            layoutInflater
        )
        binding = inflate
        val view: View = inflate.root
        lstAnime = ArrayList<Image?>()
        binding!!.swiperefreshlayout.setOnRefreshListener {
            val categoryFragment = this@CategoryFragment
            categoryFragment.initComponent(categoryFragment.lstAnime)
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
        request = JsonObjectRequest(Request.Method.POST, Config.URL, null as JSONObject?, { response ->
            try {
                val jsonArray = response.getJSONArray("Books")
                for (i in 0 until jsonArray.length()) {
                    val category = jsonArray.getJSONObject(i)
                    val image = Image()
                    image.image_url = "https://img.freepik.com/free-photo/aesthetic-product-backdrop-with-patterned-glass_53876-132977.jpg?w=900&t=st=1687632062~exp=1687632662~hmac=0a583a905b65e125274826589b8a7243e2546467e18740818b522676f200af58"
                    image.cat_name = category.getString("categories")
                    lstAnime!!.add(image)
                    binding!!.circularIndicator.visibility = View.GONE
                    binding!!.swiperefreshlayout.isRefreshing = false
                }
            } catch (e: JSONException) {
                e.printStackTrace()
                binding!!.circularIndicator.visibility = View.VISIBLE
            }
            val categoryFragment = this@CategoryFragment
            categoryFragment.initComponent(categoryFragment.lstAnime)
        }) { binding!!.circularIndicator.visibility = View.VISIBLE }
        val newRequestQueue = Volley.newRequestQueue(context)
        requestQueue = newRequestQueue
        newRequestQueue.add(request)
    }

    fun initComponent(lstAnime2: List<Image?>?) {
        val adapter = context?.let { CategoryAdapter(it, lstAnime2 as List<Image>) }
        binding!!.recyclerView.layoutManager = GridLayoutManager(activity, 2)
        binding!!.recyclerView.setHasFixedSize(true)
        binding!!.recyclerView.adapter = adapter
        adapter!!.notifyDataSetChanged()
    }

    override fun onStart() {
        binding!!.swiperefreshlayout.isRefreshing = false
        super.onStart()
    }

    companion object {
        fun newInstance(): CategoryFragment {
            return CategoryFragment()
        }
    }
}