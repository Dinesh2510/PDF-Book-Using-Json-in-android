package com.pixeldev.pdfbook.fragments

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.pixeldev.pdfbook.activity.MainActivity
import com.pixeldev.pdfbook.adapter.BooksAdapter
import com.pixeldev.pdfbook.databinding.FragmentRecentBinding
import com.pixeldev.pdfbook.model.Image
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject


class RecentFragment : Fragment() {
    var binding: FragmentRecentBinding? = null
    var lstAnime: MutableList<Image?>? = null
    var mainActivity: MainActivity? = null
    private var request: JsonObjectRequest? = null
    override fun onAttach(activity: Context) {
        super.onAttach(activity)
        mainActivity = activity as MainActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val inflate = FragmentRecentBinding.inflate(
            layoutInflater
        )
        binding = inflate
        val view: View = inflate.root
        //binding.toolbar.setNavigationOnClickListener(v -> ((MainActivity) getActivity()).openDrawer());
        lstAnime = ArrayList<Image?>()
        binding!!.swiperefreshlayout.setOnRefreshListener {
            val recentFragment = this@RecentFragment
            recentFragment.initComponent(recentFragment.lstAnime)
            CoroutineScope(Dispatchers.Main).launch {
                jsonRequest()
            }
            (lstAnime as ArrayList<Image?>).clear()
            binding!!.swiperefreshlayout.isRefreshing = false
        }
        CoroutineScope(Dispatchers.Main).launch {
            jsonRequest()
        }

        //        binding.imgSearch.setOnClickListener(v -> startActivity(new Intent(getActivity(), BookSearch.class)));
//        binding.imgVert.setOnClickListener(v -> {
//            PopupMenu popup = new PopupMenu(getActivity(), binding.imgVert);
//            popup.getMenuInflater().inflate(R.menu.menu_toolbar, popup.getMenu());
//            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//                public boolean onMenuItemClick(MenuItem item) {
//                    if (item.getItemId() == R.id.menu_top_setting) {
//                        startActivity(new Intent(getActivity(), ActivitySettings.class));
//                        return true;
//                    } else if (item.getItemId() == R.id.menu_top_rate) {
//                        try {
//                            RecentFragment recentFragment = RecentFragment.this;
//                            recentFragment.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getActivity().getPackageName())));
//                            return true;
//                        } catch (ActivityNotFoundException e) {
//                            RecentFragment recentFragment2 = RecentFragment.this;
//                            recentFragment2.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + getActivity().getPackageName())));
//                            return true;
//                        }
//                    } else if (item.getItemId() == R.id.menu_top_moreapps) {
//                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.more_apps))));
//                        return true;
//                    } else if (item.getItemId() == R.id.menu_top_share) {
//                        Intent intent = new Intent();
//                        intent.setAction( Intent.ACTION_SEND);
//                        intent.putExtra("android.intent.extra.SUBJECT", getString(R.string.app_name));
//                        intent.putExtra(  Intent.EXTRA_TEXT, getString(R.string.share_text) + "\nhttps://play.google.com/store/apps/details?id=" + getActivity().getPackageName());
//                        intent.setType("text/plain");
//                        startActivity(intent);
//                        return true;
//                    } else if (item.getItemId() != R.id.menu_top_about) {
//                        return true;
//                    } else {
//                        mainActivity.showDialogAbout();
//                        return true;
//                    }
//                }
//            });
//            popup.show();
//        });
        return view
    }

    fun jsonRequest() {
        request = JsonObjectRequest(
            0,
            "https://pixeldev.in/app/books.json",
            null as JSONObject?,
            { response ->
                try {
                    val jsonArray = response.getJSONArray("Books")
                    for (i in 0 until jsonArray.length()) {
                        val book_item = jsonArray.getJSONObject(i)
                        val image = Image()
                        //val s: String = TextUtils.join(", ", list)

                        image.author_name = book_item.optJSONArray("author")?.get(0).toString()
                        image.book_thumb = book_item.optString("book_thumb")
                        image.book_name = book_item.optString("book_name")
                        image.book_url = book_item.optString("book_url")
                        image.book_description = book_item.optString("book_description")
                        image.is_featured =(book_item.optBoolean("status"))
                        image.category = book_item.getString("categories").toString()
                        lstAnime!!.add(image)
                        binding!!.circularIndicator.visibility = View.GONE
                        binding!!.swiperefreshlayout.isRefreshing = false
                    } //https://icseindia.org/document/sample.pdf
                    //https://ncu.rcnpv.com.tw/Uploads/20131231103232738561744.pdf
                    //https://www.orimi.com/pdf-test.pdf
                    //https://www.africau.edu/images/default/sample.pdf
                } catch (e: JSONException) {
                    e.printStackTrace()
                    binding!!.circularIndicator.visibility = View.VISIBLE
                }
                val recentFragment = this@RecentFragment
                recentFragment.initComponent(recentFragment.lstAnime)
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
        fun newInstance(): RecentFragment {
            return RecentFragment()
        }
    }
}