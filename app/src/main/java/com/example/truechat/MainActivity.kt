package com.example.truechat

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuInflater
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {
    private lateinit var pImage:ShapeableImageView
    private lateinit var pName:TextView
    private lateinit var showProfile:Button
    private val fireStore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    lateinit var toolBar:Toolbar
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        toolBar = findViewById(R.id.toolBar)
        pImage = findViewById(R.id.pImage)
        pName = findViewById(R.id.pName)
        showProfile = findViewById(R.id.showProfileBt)
        show()
        showProfile.setOnClickListener {
            ProfileFragment().show(supportFragmentManager,"ProfileFragment")
        }


        toolBar.setOnMenuItemClickListener { it ->
            when(it.itemId){
                R.id.emailOption ->{
                    openEmail()
                }
                R.id.callOption ->{
                    openCall()
                }
                R.id.logoutOption ->{
                    val dialogBuilder = AlertDialog.Builder(this)
                    dialogBuilder.setPositiveButton("Yes",
                        DialogInterface.OnClickListener { dialogInterface, i ->
                        auth.signOut()
                        finish()
                        startActivity(Intent(this,LoginActivity::class.java))
                    })
                    dialogBuilder.setNegativeButton("No",DialogInterface.OnClickListener { dialogInterface, i ->

                    })
                    val dialog = dialogBuilder.create()
                    dialog.setTitle("Logout")
                    dialog.setMessage("Are you sure you want to logout?")
                    dialog.show()
                }
                R.id.deleteOption ->{
                    val dialogBuilder = AlertDialog.Builder(this)
                    dialogBuilder.setPositiveButton("Yes",
                        DialogInterface.OnClickListener { dialogInterface, i ->
                            auth.currentUser?.delete()
                            fireStore.collection("users").document(auth.currentUser?.uid.toString()).delete()
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Account Deleted Successfully", Toast.LENGTH_SHORT).show()
                                    finish()
                                    startActivity(Intent(this,LoginActivity::class.java))
                                }
                                .addOnFailureListener {ex ->
                                    Toast.makeText(this, ex.message, Toast.LENGTH_SHORT).show()
                                }
                        })
                    dialogBuilder.setNegativeButton("No",DialogInterface.OnClickListener { dialogInterface, i ->

                    })
                    val dialog = dialogBuilder.create()
                    dialog.setTitle("Delete")
                    dialog.setMessage("Are you sure you want to Delete your Account?")
                    dialog.show()
                }
            }
            true
        }




    }
    private fun show(){
        fireStore.collection("users").document(auth.currentUser?.uid.toString()).get()
            .addOnSuccessListener {
                val data = it.toObject(UserModel::class.java)

                pName.text = data?.name
//                pImage.setImageURI(data?.image)
                Glide.with(this).load(Uri.parse(data?.image.toString())).into(pImage)
            }
    }
    private fun openEmail(){
        val emails = arrayOf("akp886@gmail.com")
        val intentEmail= Intent(Intent.ACTION_SEND)
        intentEmail.putExtra(Intent.EXTRA_EMAIL,emails )
        intentEmail.type = "message/rfc822"
        startActivity(Intent.createChooser(intentEmail,  "Choose an email client :"))


    }
    private fun openCall(){
        val intentCall = Intent(Intent.ACTION_DIAL)
        intentCall.data = Uri.parse("tel:7759870382")
        startActivity(intentCall)

    }






}