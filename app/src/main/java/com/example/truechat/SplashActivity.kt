package com.example.truechat

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        val auth = FirebaseAuth.getInstance()
        val fireStore = FirebaseFirestore.getInstance()
        var uId: String? = ""

        fireStore.collection("users").whereEqualTo("userId", auth.currentUser?.uid).get()
            .addOnSuccessListener {
                for (data in it) {
                    uId = data.getString("userId")
                }
            }
        Handler().postDelayed({


            if (auth.currentUser?.uid != null) {
                if (uId!!.isNotEmpty()) {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    startActivity(Intent(this, RegisterActivity::class.java))
                    finish()
                }
            } else {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }

        }, 4000)
    }
}