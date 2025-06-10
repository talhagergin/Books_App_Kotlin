package com.example.bookapp

import android.content.Intent
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class SplashActivity : AppCompatActivity() {
    private val TAG = "SplashActivity"
    private lateinit var firebaseAuth: FirebaseAuth
    private val PERMISSION_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        
        try {
            Log.d(TAG, "SplashActivity started")
            checkVersion()
            firebaseAuth = FirebaseAuth.getInstance()

            // Bildirim iznini iste
            requestNotificationPermission()

            // FCM Token'ı al
            FirebaseMessaging.getInstance().token.addOnCompleteListener {
                if (!it.isSuccessful) {
                    Log.w(TAG, "Fetching FCM registration token failed", it.exception)
                    return@addOnCompleteListener
                }
                val token = it.result
                Log.d(TAG, "FCM Registration Token: $token")
                // Token'ı bir sunucuya gönderebilirsiniz
            }
            
            Handler(Looper.getMainLooper()).postDelayed({
                checkUser()
            }, 1000) // 1 saniye delay
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate", e)
            Toast.makeText(this, "Uygulama başlatılırken bir hata oluştu", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun requestNotificationPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), PERMISSION_REQUEST_CODE)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "POST_NOTIFICATIONS permission granted")
            } else {
                Log.d(TAG, "POST_NOTIFICATIONS permission denied")
                Toast.makeText(this, "Bildirim izni reddedildi. Uygulamanın bildirim göndermesi kısıtlanabilir.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun checkVersion() {
        try {
            val versionCode = BuildConfig.VERSION_CODE
            val versionName = BuildConfig.VERSION_NAME
            Log.d(TAG, "App version: $versionName ($versionCode)")
        } catch (e: Exception) {
            Log.e(TAG, "Error checking version", e)
        }
    }

    private fun checkUser() {
        try {
            Log.d(TAG, "Checking user authentication status")
            //giriş yapan kullanıcı varsa bakıcaz
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser == null) {
                //kullanıcı girişi yok ana menüye
                Log.d(TAG, "No user logged in, going to MainActivity")
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                //kullanıcı tip kontrol
                Log.d(TAG, "User logged in, checking user type for uid: ${firebaseUser.uid}")
                val ref = FirebaseDatabase.getInstance().getReference("Users")
                ref.child(firebaseUser.uid)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            try {
                                if (!snapshot.exists()) {
                                    Log.e(TAG, "User data not found in database")
                                    Toast.makeText(this@SplashActivity, "Kullanıcı bilgileri bulunamadı", Toast.LENGTH_LONG).show()
                                    startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                                    finish()
                                    return
                                }

                                //kullanıcı bilgilerini getir kullanıcı ya da admin
                                val userType = snapshot.child("userType").value as? String
                                Log.d(TAG, "User type: $userType")

                                when (userType) {
                                    "user" -> {
                                        Log.d(TAG, "User type is user, going to DashboardUserActivity")
                                        startActivity(Intent(this@SplashActivity, DashboardUserActivity::class.java))
                                        finish()
                                    }
                                    "admin" -> {
                                        Log.d(TAG, "User type is admin, going to DashboardAdminActivity")
                                        startActivity(Intent(this@SplashActivity, DashboardAdminActivity::class.java))
                                        finish()
                                    }
                                    else -> {
                                        Log.e(TAG, "Invalid user type: $userType")
                                        Toast.makeText(this@SplashActivity, "Geçersiz kullanıcı tipi", Toast.LENGTH_LONG).show()
                                        startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                                        finish()
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Error processing user data", e)
                                Toast.makeText(this@SplashActivity, "Kullanıcı bilgileri işlenirken hata oluştu", Toast.LENGTH_LONG).show()
                                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                                finish()
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.e(TAG, "Database error: ${error.message}")
                            Toast.makeText(this@SplashActivity, "Veritabanı hatası: ${error.message}", Toast.LENGTH_LONG).show()
                            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                            finish()
                        }
                    })
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in checkUser", e)
            Toast.makeText(this, "Kullanıcı kontrolü sırasında bir hata oluştu", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}