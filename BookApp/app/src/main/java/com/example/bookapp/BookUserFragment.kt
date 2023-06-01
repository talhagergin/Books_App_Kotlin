package com.example.bookapp

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.bookapp.databinding.FragmentBookUserBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class BookUserFragment : Fragment {
//view binding
    private lateinit var binding : FragmentBookUserBinding

    public  companion object{
        private const val TAG = "BOOKS_USER_TAG"

        //get data from act
        public fun newInstance(categoryId:String,category:String,uid:String) : BookUserFragment{
            val fragment = BookUserFragment()
            //put data to bundle intent
            val args = Bundle()
            args.putString("categoryId",categoryId)
            args.putString("category",category)
            args.putString("uid",uid)
            fragment.arguments = args
            return fragment
        }
    }

    private var categoryId = ""
    private var category = ""
    private var uid = ""

    private lateinit var pdfArrayList: ArrayList<ModelBook>
    private lateinit var adapterBookUser: AdapterBookUser

    constructor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = arguments
        if(args != null){
            categoryId = args.getString("categoryId")!!
            category = args.getString("category")!!
            uid = args.getString("uid")!!
        }
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentBookUserBinding.inflate(LayoutInflater.from(context),container,false)

        Log.d(TAG,"onCreateView: Kategori $category")
        if(category == "All"){
            //load books
            loadAllBooks()
        }else if(category == "Most Viewed"){
            loadMostViewedDownloadedBooks("viewsCount")
        }else if(category == "Most Downloaded"){
            loadMostViewedDownloadedBooks("downloadsCount")
        }else {
            loadCategorizedBooks()
        }

        //search
        binding.searchEt.addTextChangedListener(object:TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                try {
                    adapterBookUser.filter!!.filter(s)
                }catch (e:Exception){
                    Log.d(TAG,"onTextChanged: ${e.message}")
                }
            }
            override fun afterTextChanged(p0: Editable?) {
            }
        })
        //searcc
        binding.searchEt.addTextChangedListener(object:TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                try {
                    adapterBookUser.filter.filter(s)
                }
                catch (e: Exception){
                    Log.d(TAG,"onTextChanged: Arama hatası ${e.message}")
                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }
        })

        return binding.root
    }


    private fun loadMostViewedDownloadedBooks(orderBy: String) {
        //init list
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                pdfArrayList.clear()
                for(ds in snapshot.children){
                    val model = ds.getValue(ModelBook::class.java)
                    //add to list
                    pdfArrayList.add(model!!)
                }
                //adapter setup
                adapterBookUser = AdapterBookUser(context!!,pdfArrayList)
                binding.booksRv.adapter = adapterBookUser
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun loadCategorizedBooks() {
        //init list
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                pdfArrayList.clear()
                for(ds in snapshot.children){
                    val model = ds.getValue(ModelBook::class.java)
                    //add to list
                    pdfArrayList.add(model!!)
                }
                //adapter setup
                adapterBookUser = AdapterBookUser(context!!,pdfArrayList)
                binding.booksRv.adapter = adapterBookUser
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }


    private fun loadAllBooks() {
    //init list
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                pdfArrayList.clear()
                for(ds in snapshot.children){
                    val model = ds.getValue(ModelBook::class.java)
                    //add to list
                    pdfArrayList.add(model!!)
                }
                //adapter setup
                adapterBookUser = AdapterBookUser(context!!,pdfArrayList)
                binding.booksRv.adapter = adapterBookUser
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

}