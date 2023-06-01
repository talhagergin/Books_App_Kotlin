package com.example.bookapp

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.example.bookapp.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {
    //view binding
    private lateinit var binding:ActivityLoginBinding

    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    //progress dialog
    private lateinit var progresDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()

        //init progres
        progresDialog = ProgressDialog(this)
        progresDialog.setTitle("Lütfen bekleyiniz...")
        progresDialog.setCanceledOnTouchOutside(false)

        //hesabım yok kayıt ol seçeneğine tıkladığında kayıt sayfasına gidecek
        binding.noAccTv.setOnClickListener {
            startActivity(Intent(this,RegisterActivity::class.java))
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
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this,"Geçersiz e-posta formatı...",Toast.LENGTH_SHORT).show()

        }else if(password.isEmpty()){
            Toast.makeText(this,"Şifre giriniz..",Toast.LENGTH_SHORT).show()
        }
        else{
            loginUser()
        }
    }

    private fun loginUser() {
        //giriş işlemi firebase ile

        //ilerleme bilgisi
        progresDialog.setMessage("Giriş Yapılıyor...")
        progresDialog.show()

        firebaseAuth.signInWithEmailAndPassword(email,password)
            .addOnSuccessListener {
                //giriş başarılı
                checkUser()
            }
            .addOnFailureListener { e->
                progresDialog.dismiss()
                Toast.makeText(this,"Giriş Başarısız Oldu ${e.message}...",Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkUser() {
        progresDialog.setMessage("Kullanıcı kontrol ediliyor...")

        val firebaseUser = firebaseAuth.currentUser!!

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseUser.uid)
            .addListenerForSingleValueEvent(object :  ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    progresDialog.dismiss()

                    //kullanıcı bilgilerini getir kullanıcı ya da admin
                    val userType = snapshot.child("userType").value
                    if(userType == "user"){
                        startActivity(Intent(this@LoginActivity,DashboardUserActivity::class.java))
                        finish()
                    }
                    else if(userType =="admin"){
                        startActivity(Intent(this@LoginActivity,DashboardAdminActivity::class.java))
                        finish()
                    }
                }
                override fun onCancelled(error: DatabaseError) {

                }
            })
    }
}