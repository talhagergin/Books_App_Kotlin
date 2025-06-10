package com.example.bookapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import com.example.bookapp.databinding.ActivityRegisterBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {
    //view binding
    private lateinit var binding: ActivityRegisterBinding

    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    //loading dialog
    private lateinit var loadingDialog: MaterialAlertDialogBuilder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()

        //init loading dialog
        loadingDialog = MaterialAlertDialogBuilder(this)
            .setView(R.layout.loading_dialog)
            .setCancelable(false)

        //handle back button click
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })

        //handle click register begin
        binding.registerBtn.setOnClickListener {
            validateData()
        }
    }

    private var name = ""
    private var email = ""
    private var password = ""

    private fun validateData() {
        //veri giriş
        name = binding.nameEt.text.toString().trim()
        email = binding.emailEt.text.toString().trim()
        password = binding.passwordEt.text.toString().trim()
        val confirm_password = binding.confirmPasswordEt.text.toString().trim()

        //veri kontrolleri
        if (name.isEmpty()) {
            Toast.makeText(this, "İsminizi giriniz...", Toast.LENGTH_SHORT).show()
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            //geçersiz email
            Toast.makeText(this, "Geçerli bir e-posta giriniz...", Toast.LENGTH_SHORT).show()
        } else if (password.isEmpty()) {
            //şifre boş ise
            Toast.makeText(this, "Şifrenizi giriniz...", Toast.LENGTH_SHORT).show()
        } else if (confirm_password.isEmpty()) {
            Toast.makeText(this, "Şifrenizi tekrar giriniz...", Toast.LENGTH_SHORT).show()
        } else if (password != confirm_password) {
            Toast.makeText(this, "Şifreler uyuşmuyor...", Toast.LENGTH_SHORT).show()
        } else {
            //veri girişinde sorun yok veritabanına kayıt işlemi
            createUserAccount()
        }
    }

    private fun createUserAccount() {
        val dialog = loadingDialog.create()
        dialog.show()

        //firebase kullanıcı oluşturma
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                //hesap oluşturuldu
                updateUserInfo(dialog)
            }
            .addOnFailureListener { e ->
                //patladı
                dialog.dismiss()
                Toast.makeText(this, "Hesabınız oluşturulurken bir hata oluştu ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateUserInfo(dialog: AlertDialog) {
        //timestamp
        val timestamp = System.currentTimeMillis()

        val uid = firebaseAuth.uid

        val hashMap: HashMap<String, Any?> = HashMap()
        hashMap["uid"] = uid
        hashMap["email"] = email
        hashMap["name"] = name
        hashMap["profileImage"] = "" // boş ekler sonra düzenlenebilir
        hashMap["userType"] = "user"
        hashMap["timestamp"] = timestamp

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(uid!!)
            .setValue(hashMap)
            .addOnSuccessListener {
                //kullanıcnı bilgileri kaydedildi ana menüyü aç
                dialog.dismiss()
                Toast.makeText(this, "Hesabınız başarıyla oluşturuldu...", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@RegisterActivity, DashboardUserActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                //patladı
                dialog.dismiss()
                Toast.makeText(this, "Kullanıcı bilglileri kaydedilirken bir hata oluştu ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}