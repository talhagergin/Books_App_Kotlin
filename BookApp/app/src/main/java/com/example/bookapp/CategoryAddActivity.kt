package com.example.bookapp

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.bookapp.databinding.ActivityCategoryAddBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class CategoryAddActivity : AppCompatActivity() {
    //view binding
    private lateinit var binding: ActivityCategoryAddBinding

    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    //progres dialog
    private lateinit var progressDialog: ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()

        //progresdialog
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Lütfen bekleyiniz...")
        progressDialog.setCanceledOnTouchOutside(false)

        //geri dön
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        //kategori yükle buton
        binding.submitBtn.setOnClickListener {
            validateData()
        }

    }

    private var category = ""

    private fun validateData() {
        //veri kontrol
        category = binding.categoryEt.text.toString().trim()

        if(category.isEmpty()){
            Toast.makeText(this,"Bir Kategori Giriniz...",Toast.LENGTH_SHORT).show()
        }
        else{
            addCategoryFirebase()
        }
    }

    private fun addCategoryFirebase() {
        //ilerleme
        progressDialog.show()

        val timestamp = System.currentTimeMillis()

        val hashMap = HashMap<String,Any>()
        hashMap["id"]="$timestamp"
        hashMap["category"] = category
        hashMap["timestamp"] = timestamp
        hashMap["uid"] = "${firebaseAuth.uid}"

        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.child("$timestamp")
            .setValue(hashMap)
            .addOnSuccessListener {
                //başarıllı eklendi
                progressDialog.dismiss()
                Toast.makeText(this,"Kategori başarıyla eklendi",Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener { e->
                progressDialog.dismiss()
                Toast.makeText(this,"Bir hata oluştu ${e.message}",Toast.LENGTH_SHORT).show()
            }


    }
}