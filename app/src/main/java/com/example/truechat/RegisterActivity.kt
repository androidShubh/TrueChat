package com.example.truechat

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class RegisterActivity : AppCompatActivity() {
    private lateinit var nameText: TextInputEditText
    private lateinit var emailText: TextInputEditText
    private lateinit var addressText: TextInputEditText
    private lateinit var registerBt: AppCompatButton
    private lateinit var progressBar: ProgressBar
    private lateinit var registerLay: RelativeLayout
    private lateinit var pImageR:ShapeableImageView
    private lateinit var editImageR:ShapeableImageView
    private val fireStore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()
    lateinit var progressDialogR: ProgressDialog
    private var downloadedUriR: Uri? =null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        registerBt = findViewById(R.id.registerBt)
        nameText = findViewById(R.id.nameText)
        emailText = findViewById(R.id.emailText)
        addressText = findViewById(R.id.addressText)
        progressBar = findViewById(R.id.progressBar)
        registerLay = findViewById(R.id.registerLay)
        pImageR = findViewById(R.id.pImageR)
        editImageR = findViewById(R.id.editImageR)

        progressDialogR = ProgressDialog(this)
        progressDialogR.setMessage("Please Wait")

        registerBt.setOnClickListener {
            if (nameText.text!!.isEmpty()) {
                Toast.makeText(this, "Enter Name First", Toast.LENGTH_SHORT).show()
            } else if (emailText.text!!.isEmpty()) {
                Toast.makeText(this, "Enter Email First", Toast.LENGTH_SHORT).show()
            } else if (addressText.text!!.isEmpty()) {
                Toast.makeText(this, "Enter Address First", Toast.LENGTH_SHORT).show()
            } else {
                progressBar.visibility = View.VISIBLE
                insert()
            }

        }
        editImageR.setOnClickListener {
            val intentForGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            startActivityForResult(intentForGallery,124)
        }

    }

    private fun insert() {
        val user = UserModel(
            auth.currentUser?.uid.toString(),
            nameText.text.toString(),
            emailText.text.toString(),
            addressText.text.toString(),
            auth.currentUser?.phoneNumber.toString(),
            downloadedUriR.toString()
        )
        fireStore.collection("users").document(auth.currentUser?.uid.toString()).set(user)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(this, "User Registered SuccessFully", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    progressBar.visibility = View.GONE
                    finish()
                } else {
                    Toast.makeText(this, it.exception?.message, Toast.LENGTH_SHORT).show()
                    progressBar.visibility = View.GONE
                    finish()
                }
            }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 124){
            val imageUri = data?.data
            pImageR.setImageURI(imageUri)
            progressDialogR.show()
            uploadImage(imageUri!!)
        }
    }
    private fun uploadImage(image: Uri){
        val ref = storage.getReference("profileImages").child(UUID.randomUUID().toString())
        ref.putFile(image)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener {uri ->
                    downloadedUriR = uri
                }
                progressDialogR.dismiss()
                Toast.makeText(this, "Profile Picture Saved Successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                progressDialogR.dismiss()
                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
            }
    }
}