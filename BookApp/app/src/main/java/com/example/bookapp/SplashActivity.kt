package com.example.bookapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SplashActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        checkVersion()
        firebaseAuth = FirebaseAuth.getInstance()
        Handler().postDelayed({
        checkUser()
        },1000,) // 1 saniye delay
    }
private fun checkVersion(){
    val versionCode = BuildConfig.VERSION_CODE
    val versionName = BuildConfig.VERSION_NAME
}


    private fun checkUser() {
        //giriş yapan kullanıcı varsa bakıcaz
        val firebaseUser = firebaseAuth.currentUser
        if(firebaseUser == null){
            //kullanıcı girişi yok ana menüye
            startActivity(Intent(this,MainActivity::class.java))
            finish()
        }
        else{
            //kullanıcı tip kontrol

            val ref = FirebaseDatabase.getInstance().getReference("Users")
            ref.child(firebaseUser.uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {

                        //kullanıcı bilgilerini getir kullanıcı ya da admin
                        val userType = snapshot.child("userType").value
                        if(userType == "user"){
                            startActivity(Intent(this@SplashActivity,DashboardUserActivity::class.java))
                            finish()
                        }
                        else if(userType =="admin"){
                            startActivity(Intent(this@SplashActivity,DashboardAdminActivity::class.java))
                            finish()
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {

                    }
                })
        }
    }
}