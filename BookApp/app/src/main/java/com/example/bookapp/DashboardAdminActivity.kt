package com.example.bookapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import com.example.bookapp.databinding.ActivityDashboardAdminBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DashboardAdminActivity : AppCompatActivity() {
    //view binding
    private lateinit var binding:ActivityDashboardAdminBinding

    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    //kategorileri tutan liste
    private lateinit var categoryArrayList: ArrayList<ModelCategory>
    //adapter
    private lateinit var adapterCategory: AdapterCategory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()
        loadCategories()
        //arama
        binding.searchEt.addTextChangedListener(object:TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                try {
                    adapterCategory.filter.filter(s)
                }
                catch (e: Exception){
                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }
        })
        //çıkış butoununa tıklandığında
        binding.logoutBtn.setOnClickListener{
            firebaseAuth.signOut()
            checkUser()
        }

        //kategori sayfasına gider
        binding.addCategoryBtn.setOnClickListener {
            startActivity(Intent(this,CategoryAddActivity::class.java))
        }

        //pdf ekleme sayfası
        binding.addPdfFab.setOnClickListener{
            startActivity(Intent(this,BookAddActivity::class.java))
        }
    }

    private fun loadCategories() {
        //init arraylist
        categoryArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                categoryArrayList.clear()
                for(ds in snapshot.children){
                    val model = ds.getValue(ModelCategory::class.java)

                    categoryArrayList.add(model!!)
                }
                adapterCategory = AdapterCategory(this@DashboardAdminActivity,categoryArrayList)
                //recyler view ı ayarlama
                binding.categoriesRv.adapter=adapterCategory
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun checkUser() {
        //şu an oturumu olan kullanıcıyı getir
        val firebaseUser = firebaseAuth.currentUser
        if(firebaseUser == null){
            //giriş yapan kullanıcı yok ana menüye yallahh
            startActivity(Intent(this,MainActivity::class.java))
            finish()
        }
        else{
            //giriş yapmış kardeşimizin kullanıcı bilgileri
            val email = firebaseUser.email
            binding.subTittleTv.text = email
        }
    }
}