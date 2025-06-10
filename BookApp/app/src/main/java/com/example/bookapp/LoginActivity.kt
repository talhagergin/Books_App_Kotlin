package com.example.bookapp

import android.content.Intent
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.bookapp.databinding.ActivityLoginBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LoginActivity : AppCompatActivity() {
    private val TAG = "LoginActivity"
    
    //view binding
    private lateinit var binding: ActivityLoginBinding

    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    //loading dialog
    private lateinit var loadingDialog: MaterialAlertDialogBuilder
    private var currentDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()

        //init loading dialog
        loadingDialog = MaterialAlertDialogBuilder(this)
            .setView(R.layout.loading_dialog)
            .setCancelable(false)

        //hesabım yok kayıt ol seçeneğine tıkladığında kayıt sayfasına gidecek
        binding.noAccTv.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        //giriş butonuna tıklandığında
        binding.loginBtn.setOnClickListener {
            validateData()
        }
    }

    private var email = ""
    private var password = ""

    private fun validateData() {
        //veri girişi
        email = binding.emailEt.text.toString().trim()
        password = binding.passwordEt.text.toString().trim()

        //veri kontrolü
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Geçersiz e-posta formatı...", Toast.LENGTH_SHORT).show()
        } else if (password.isEmpty()) {
            Toast.makeText(this, "Şifre giriniz..", Toast.LENGTH_SHORT).show()
        } else {
            loginUser()
        }
    }

    private fun loginUser() {
        try {
            //giriş işlemi firebase ile
            currentDialog = loadingDialog.create()
            currentDialog?.show()

            Log.d(TAG, "Login attempt for email: $email")

            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    Log.d(TAG, "Firebase Auth successful")
                    //giriş başarılı
                    checkUser()
                }
                .addOnFailureListener { e ->
                    currentDialog?.dismiss()
                    Log.e(TAG, "Firebase Auth failed", e)
                    val errorMessage = when {
                        e.message?.contains("no user record") == true -> "Bu e-posta adresi ile kayıtlı kullanıcı bulunamadı"
                        e.message?.contains("password is invalid") == true -> "Şifre hatalı"
                        e.message?.contains("badly formatted") == true -> "Geçersiz e-posta formatı"
                        else -> "Giriş başarısız: ${e.message}"
                    }
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                }
        } catch (e: Exception) {
            currentDialog?.dismiss()
            Log.e(TAG, "Login process failed", e)
            Toast.makeText(this, "Giriş işlemi sırasında bir hata oluştu: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun checkUser() {
        try {
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser == null) {
                currentDialog?.dismiss()
                Log.e(TAG, "Firebase user is null after successful login")
                Toast.makeText(this, "Kullanıcı bilgileri alınamadı", Toast.LENGTH_LONG).show()
                return
            }

            Log.d(TAG, "Checking user type for uid: ${firebaseUser.uid}")

            val ref = FirebaseDatabase.getInstance().getReference("Users")
            ref.child(firebaseUser.uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        currentDialog?.dismiss()
                        
                        if (!snapshot.exists()) {
                            Log.e(TAG, "User data not found in database")
                            Toast.makeText(this@LoginActivity, "Kullanıcı bilgileri bulunamadı", Toast.LENGTH_LONG).show()
                            return
                        }

                        //kullanıcı bilgilerini getir kullanıcı ya da admin
                        val userType = snapshot.child("userType").value as? String
                        Log.d(TAG, "User type: $userType")

                        when (userType) {
                            "user" -> {
                                startActivity(Intent(this@LoginActivity, DashboardUserActivity::class.java))
                                finish()
                            }
                            "admin" -> {
                                startActivity(Intent(this@LoginActivity, DashboardAdminActivity::class.java))
                                finish()
                            }
                            else -> {
                                Log.e(TAG, "Invalid user type: $userType")
                                Toast.makeText(this@LoginActivity, "Geçersiz kullanıcı tipi", Toast.LENGTH_LONG).show()
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        currentDialog?.dismiss()
                        Log.e(TAG, "Database error: ${error.message}")
                        Toast.makeText(this@LoginActivity, "Veritabanı hatası: ${error.message}", Toast.LENGTH_LONG).show()
                    }
                })
        } catch (e: Exception) {
            currentDialog?.dismiss()
            Log.e(TAG, "Check user process failed", e)
            Toast.makeText(this, "Kullanıcı kontrolü sırasında bir hata oluştu: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        currentDialog?.dismiss()
    }
}