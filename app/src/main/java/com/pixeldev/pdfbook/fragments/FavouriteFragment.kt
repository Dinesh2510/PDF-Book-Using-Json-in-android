package com.pixeldev.pdfbook.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.pixeldev.pdfbook.activity.MainActivity
import com.pixeldev.pdfbook.adapter.FavouriteAdapter
import com.pixeldev.pdfbook.databinding.FragmentFavouriteBinding
import com.pixeldev.pdfbook.helper.DatabaseHelper

class FavouriteFragment : Fragment() {
    var binding: FragmentFavouriteBinding? = null
    var book_author: ArrayList<String>? = null
    var book_category: ArrayList<String>? = null
    var book_desc: ArrayList<String>? = null
    var book_feature: ArrayList<String>? = null
    var book_id: ArrayList<String>? = null
    var book_thumb: ArrayList<String>? = null
    var book_title: ArrayList<String>? = null
    var book_url: ArrayList<String>? = null
    var mainActivity: MainActivity? = null
    var myDB: DatabaseHelper? = null
    var position = 0
    override fun onAttach(activity: Context) {
        super.onAttach(activity)
        mainActivity = activity as MainActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val inflate = FragmentFavouriteBinding.inflate(
            layoutInflater
        )
        binding = inflate
        val view: View = inflate.root
        myDB = DatabaseHelper(requireActivity())
        book_id = ArrayList()
        book_title = ArrayList()
        book_author = ArrayList()
        book_thumb = ArrayList()
        book_url = ArrayList()
        book_desc = ArrayList()
        book_feature = ArrayList()
        book_category = ArrayList()
        binding!!.recyclerView.adapter = FavouriteAdapter(
            requireActivity(),
            book_id!!,
            book_title!!,
            book_author!!,
            book_thumb!!,
            book_url!!,
            book_desc!!,
            book_feature!!,
            book_category!!
        )
        binding!!.recyclerView.layoutManager = GridLayoutManager(activity, 2)
        binding!!.recyclerView.setHasFixedSize(true)
        storeDataInArreys()
        return view
    }

    /* access modifiers changed from: package-private */
    fun storeDataInArreys() {
        val cursor = myDB!!.readAllData()
        if (cursor!!.count == 0) {
            binding!!.noFound.visibility = View.VISIBLE
            Toast.makeText(activity, "No Favourites", Toast.LENGTH_SHORT).show()
            return
        }
        binding!!.noFound.visibility = View.GONE
        while (cursor!!.moveToNext()) {
            book_id!!.add(cursor.getString(0))
            book_title!!.add(cursor.getString(1))
            book_author!!.add(cursor.getString(2))
            book_thumb!!.add(cursor.getString(3))
            book_url!!.add(cursor.getString(4))
            book_desc!!.add(cursor.getString(5))
            book_feature!!.add(cursor.getString(6))
            book_category!!.add(cursor.getString(7))
        }
    }

    override fun onResume() {
        FavouriteAdapter(
            requireActivity(),
            book_id!!,
            book_title!!,
            book_author!!,
            book_thumb!!,
            book_url!!,
            book_desc!!,
            book_feature!!,
            book_category!!
        ).notifyItemRemoved(
            position
        )
        super.onResume()
    }

    override fun onStart() {
        myDB = DatabaseHelper(requireContext())
        book_id = ArrayList()
        book_title = ArrayList()
        book_author = ArrayList()
        book_thumb = ArrayList()
        book_url = ArrayList()
        book_desc = ArrayList()
        book_feature = ArrayList()
        book_category = ArrayList()
        binding!!.recyclerView.adapter = FavouriteAdapter(
            requireActivity()!!,
            book_id!!,
            book_title!!,
            book_author!!,
            book_thumb!!,
            book_url!!,
            book_desc!!,
            book_feature!!,
            book_category!!
        )
        binding!!.recyclerView.layoutManager = GridLayoutManager(activity, 1)
        binding!!.recyclerView.setHasFixedSize(true)
        storeDataInArreys()
        super.onStart()
    }

    companion object {
        fun newInstance(): FavouriteFragment {
            return FavouriteFragment()
        }
    }
}