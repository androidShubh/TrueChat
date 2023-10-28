package com.example.truechat

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.util.UUID


class ProfileFragment : BottomSheetDialogFragment() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private lateinit var nameShow: EditText
    private lateinit var emailShow: EditText
    private lateinit var phoneShow: EditText
    private lateinit var addressShow: EditText
    private lateinit var updateBt: Button
    private lateinit var logoutBt: Button
    private lateinit var editImage: ShapeableImageView
    private lateinit var pImageF: ShapeableImageView
    private lateinit var progressDialog: ProgressDialog
    private var downloadedUri: Uri? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val rowView = inflater.inflate(R.layout.fragment_profile, container, false)
        nameShow = rowView.findViewById(R.id.nameShow)
        emailShow = rowView.findViewById(R.id.emailShow)
        phoneShow = rowView.findViewById(R.id.phoneShow)
        addressShow = rowView.findViewById(R.id.addressShow)
        updateBt = rowView.findViewById(R.id.updateBt)
        logoutBt = rowView.findViewById(R.id.logoutBt)
        editImage = rowView.findViewById(R.id.editImage)
        pImageF = rowView.findViewById(R.id.pImageF)



        progressDialog = ProgressDialog(requireActivity())
        progressDialog.setMessage("Please Wait")


        showData()

        updateBt.setOnClickListener {
            val dialogBuilder = AlertDialog.Builder(requireContext())
            dialogBuilder.setPositiveButton(
                "Yes",
                DialogInterface.OnClickListener { dialogInterface, i ->
                    update()
                    requireActivity().finish()
                    startActivity(Intent(requireActivity(), MainActivity::class.java))
                })
            dialogBuilder.setNegativeButton(
                "No",
                DialogInterface.OnClickListener { dialogInterface, i ->

                })
            val dialog = dialogBuilder.create()
            dialog.setTitle("Profile Update")
            dialog.setMessage("Are you sure you want to Update?")
            dialog.show()

        }
        logoutBt.setOnClickListener {


        }
        editImage.setOnClickListener {
            optionDialog()
        }
        return rowView
    }

    private fun showData() {
        firestore.collection("users").document(auth.currentUser?.uid.toString()).get()
            .addOnSuccessListener {
                val data = it.toObject(UserModel::class.java)
                nameShow.setText("${data?.name}")
                emailShow.setText("${data?.email}")
                phoneShow.setText("${data?.phone}")
                addressShow.setText("${data?.address}")
//                pImageF.setImageURI(data?.image)
                Glide.with(requireContext()).load(Uri.parse(data?.image.toString())).into(pImageF)
            }

    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 123) {
            val imageUri = data?.data
            pImageF.setImageURI(imageUri)
            progressDialog.show()
            uploadImage(imageUri!!)
        }
        if (requestCode == 111 && resultCode == Activity.RESULT_OK) {
            val imageUriFromCamera =
                getImageUri(requireContext(), data?.extras?.get("data") as Bitmap)
            pImageF.setImageURI(imageUriFromCamera)
            uploadImage(imageUriFromCamera!!)
            progressDialog.show()
        }
    }

    private fun getImageUri(inContext: Context, inImage: Bitmap): Uri? {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(
            inContext.contentResolver,
            inImage,
            "Title",
            null
        )
        return Uri.parse(path)
    }

    private fun uploadImage(image: Uri) {

        val ref = storage.getReference("profileImages").child(UUID.randomUUID().toString())
        ref.putFile(image)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { uri ->
                    downloadedUri = uri
                }
                progressDialog.dismiss()
                Toast.makeText(
                    requireContext(),
                    "Profile Picture Saved Successfully",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun update() {

        firestore.collection("users").document(auth.currentUser?.uid.toString()).get()
            .addOnSuccessListener {
                val data = it.toObject(UserModel::class.java)
                val image = data?.image
                val updateUser =
                    if (downloadedUri != null) {
                        UserModel(
                            auth.currentUser?.uid.toString(),
                            nameShow.text.toString(),
                            emailShow.text.toString(),
                            addressShow.text.toString(),
                            auth.currentUser?.phoneNumber.toString(),
                            downloadedUri.toString()
                        )
                    } else {
                        UserModel(
                            auth.currentUser?.uid.toString(),
                            nameShow.text.toString(),
                            emailShow.text.toString(),
                            addressShow.text.toString(),
                            auth.currentUser?.phoneNumber.toString(),
                            image
                        )

                    }

                firestore.collection("users").document(auth.currentUser?.uid.toString())
                    .set(updateUser)
                    .addOnSuccessListener {
                        Toast.makeText(
                            activity,
                            "User Updated SuccessFully",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                    }
            }

    }

    @SuppressLint("MissingInflatedId")
    private fun optionDialog() {
        val layout = LayoutInflater.from(requireActivity()).inflate(R.layout.option, null, false)
        val fromGallery = layout.findViewById<Button>(R.id.fromGallery)
        val fromCamera = layout.findViewById<Button>(R.id.fromCamera)

        val dialog = AlertDialog.Builder(requireActivity())
        val alert = dialog.create()
        alert.setView(layout)
        alert.window?.setBackgroundDrawable(requireActivity().getDrawable(R.drawable.dialog))
        alert.show()
        fromGallery.setOnClickListener {

            val intentForGallery =
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            startActivityForResult(intentForGallery, 123)
            alert.dismiss()
        }
        fromCamera.setOnClickListener {

            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(cameraIntent, 111)
            alert.dismiss()
        }
    }


}