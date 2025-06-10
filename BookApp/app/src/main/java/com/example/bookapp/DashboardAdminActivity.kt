package com.example.bookapp

import android.content.Intent
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.bookapp.databinding.ActivityDashboardAdminBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging

class DashboardAdminActivity : AppCompatActivity() {
    private val TAG = "DashboardAdminActivity"
    //view binding
    private lateinit var binding: ActivityDashboardAdminBinding

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
        binding.searchEt.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                try {
                    adapterCategory.filter.filter(s)
                } catch (e: Exception) {
                    Log.e(TAG, "Error filtering categories", e)
                }
            }
            override fun afterTextChanged(p0: Editable?) {}
        })

        //çıkış butonuna tıklandığında
        binding.logoutBtn.setOnClickListener {
            firebaseAuth.signOut()
            checkUser()
        }

        //kategori sayfasına gider
        binding.addCategoryBtn.setOnClickListener {
            startActivity(Intent(this, CategoryAddActivity::class.java))
        }

        //pdf ekleme sayfası
        binding.addPdfFab.setOnClickListener {
            startActivity(Intent(this, BookAddActivity::class.java))
        }

        //Kullanıcı yönetimi butonu ekle
        binding.manageUsersBtn.setOnClickListener {
            showUserManagementDialog()
        }

        //Takvim butonu ekle
        binding.calendarBtn.setOnClickListener {
            startActivity(Intent(this, CalendarActivity::class.java))
        }

        //Bildirim Gönder butonu
        binding.sendNotificationBtn.setOnClickListener {
            subscribeToTopic("allNotifications")
            Toast.makeText(this, "Bildirimler için abone olundu. Firebase Console'dan bildirim gönderebilirsiniz.", Toast.LENGTH_LONG).show()
        }
    }

    private fun showUserManagementDialog() {
        val emailEditText = android.widget.EditText(this)
        emailEditText.hint = "Kullanıcı e-posta adresi"

        AlertDialog.Builder(this)
            .setTitle("Kullanıcı Rolü Güncelle")
            .setView(emailEditText)
            .setPositiveButton("Admin Yap") { _, _ ->
                val email = emailEditText.text.toString().trim()
                if (email.isNotEmpty()) {
                    updateUserRole(email)
                } else {
                    Toast.makeText(this, "Lütfen e-posta adresi girin", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("İptal", null)
            .show()
    }

    private fun updateUserRole(email: String) {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.orderByChild("email")
            .equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (userSnapshot in snapshot.children) {
                            val userId = userSnapshot.key
                            if (userId != null) {
                                // Kullanıcının rolünü admin olarak güncelle
                                ref.child(userId).child("userType")
                                    .setValue("admin")
                                    .addOnSuccessListener {
                                        Toast.makeText(this@DashboardAdminActivity, 
                                            "Kullanıcı admin olarak güncellendi", 
                                            Toast.LENGTH_SHORT).show()
                                        Log.d(TAG, "User role updated to admin for: $email")
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(this@DashboardAdminActivity,
                                            "Güncelleme başarısız: ${e.message}",
                                            Toast.LENGTH_SHORT).show()
                                        Log.e(TAG, "Error updating user role", e)
                                    }
                            }
                        }
                    } else {
                        Toast.makeText(this@DashboardAdminActivity,
                            "Bu e-posta adresiyle kayıtlı kullanıcı bulunamadı",
                            Toast.LENGTH_SHORT).show()
                        Log.d(TAG, "No user found with email: $email")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@DashboardAdminActivity,
                        "Veritabanı hatası: ${error.message}",
                        Toast.LENGTH_SHORT).show()
                    Log.e(TAG, "Database error", error.toException())
                }
            })
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

    private fun subscribeToTopic(topic: String) {
        FirebaseMessaging.getInstance().subscribeToTopic(topic)
            .addOnCompleteListener { task ->
                var msg = "Abone olundu"
                if (!task.isSuccessful) {
                    msg = "Abone olma başarısız oldu"
                }
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                Log.d(TAG, msg)
            }
    }
}