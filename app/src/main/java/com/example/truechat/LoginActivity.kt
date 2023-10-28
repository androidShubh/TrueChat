package com.example.truechat

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {
    private lateinit var phoneLay: RelativeLayout
    private lateinit var otpLay: RelativeLayout
    private lateinit var phoneText: TextInputEditText
    private lateinit var otpText: TextInputEditText
    private lateinit var sendOtpBt: AppCompatButton
    private lateinit var verifyOtpBt: AppCompatButton
    private lateinit var callbacks: OnVerificationStateChangedCallbacks
    private lateinit var loginText: TextView
    private lateinit var progressDialog: ProgressDialog
    private val auth = FirebaseAuth.getInstance()
    private val fireStore = FirebaseFirestore.getInstance()
    private var verificationId = ""

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        phoneLay = findViewById(R.id.phoneLay)
        otpLay = findViewById(R.id.otpLay)
        phoneText = findViewById(R.id.phoneText)
        otpText = findViewById(R.id.otpText)
        sendOtpBt = findViewById(R.id.sendOtpBt)
        verifyOtpBt = findViewById(R.id.verifyOtpBt)
        loginText = findViewById(R.id.loginText)

        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Please Wait")



        callbacks = object : OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(p0: PhoneAuthCredential) {
                verifyOtp(p0)
            }

            override fun onVerificationFailed(p0: FirebaseException) {
                progressDialog.dismiss()
                Toast.makeText(this@LoginActivity, p0.message, Toast.LENGTH_SHORT).show()
            }

            override fun onCodeSent(p0: String, p1: PhoneAuthProvider.ForceResendingToken) {
                super.onCodeSent(p0, p1)
                verificationId = p0
                progressDialog.dismiss()
                phoneLay.visibility = View.GONE
                otpLay.visibility = View.VISIBLE

                Toast.makeText(this@LoginActivity, "Otp Sent successfully", Toast.LENGTH_SHORT)
                    .show()
            }

        }

        sendOtpBt.setOnClickListener {
            if (phoneText.text!!.isEmpty() && (phoneText.text.toString().length != 10)) {
                Toast.makeText(this, "Please Enter Valid Number", Toast.LENGTH_SHORT).show()
            } else {
                sendOtp(phoneText.text.toString())
                progressDialog.show()
                loginText.text = "We Sent A OTP on your Number ${phoneText.text}"
                Toast.makeText(this@LoginActivity, "Otp Sending...", Toast.LENGTH_SHORT).show()
            }
        }
        verifyOtpBt.setOnClickListener {
            if (otpText.text!!.isEmpty()) {
                Toast.makeText(this, "Please Enter OTP", Toast.LENGTH_SHORT).show()
            } else {
                progressDialog.show()
                val credential =
                    PhoneAuthProvider.getCredential(verificationId, otpText.text.toString())
                verifyOtp(credential)
            }
        }


    }

    private fun sendOtp(phone: String) {
        val phoneAuthOptions = PhoneAuthOptions.newBuilder()
            .setPhoneNumber("+91$phone")
            .setTimeout(90L, TimeUnit.SECONDS)
            .setCallbacks(callbacks)
            .setActivity(this)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(phoneAuthOptions)
    }

    private fun verifyOtp(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnSuccessListener {
                checkUser()
            }
            .addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkUser() {
        fireStore.collection("users").whereEqualTo("userId", auth.currentUser?.uid).get()
            .addOnCompleteListener {
                if (it.result.isEmpty) {
                    progressDialog.dismiss()
                    startActivity(Intent(this, RegisterActivity::class.java))
                    finish()
                } else {
                    progressDialog.dismiss()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            }
    }
}