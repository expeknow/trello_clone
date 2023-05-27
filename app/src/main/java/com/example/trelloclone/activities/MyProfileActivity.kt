package com.example.trelloclone.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore.Images.Media
import android.provider.Settings
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.trelloclone.R
import com.example.trelloclone.databinding.ActivityMyProfileBinding
import com.example.trelloclone.firebase.FirestoreClass
import com.example.trelloclone.models.User
import com.example.trelloclone.utils.Constants
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import java.io.IOException
import java.net.URI


class MyProfileActivity : BaseActivity() {


    var binding : ActivityMyProfileBinding? = null
    var mSelectedImageFileUri : Uri? = null
    var mProfileImageURL : String = ""
    private lateinit var mUserDetails : User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyProfileBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setUpToolBar()

        FirestoreClass().loadUserData(this)

        binding?.ivProfileUserImage?.setOnClickListener {
            openGalleryForImageSelection()
        }
        binding?.btnUpdate?.setOnClickListener {
            if(mSelectedImageFileUri != null){
                uploadUserImage()
            }else{
                showProgressDialog(resources.getString(R.string.please_wait))
                updateUserProfileData()
            }
        }
    }

    /**
     * Set up top tool bar. Adds title and enables back button functionality
     */
    private fun setUpToolBar(){
        setSupportActionBar(binding?.toolbarMyProfileActivity)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        supportActionBar?.title = resources.getString(R.string.my_profile)

        binding?.toolbarMyProfileActivity?.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    /**
     * Calls the function with same name in Constants to handle permission asking and returns
     * the image selected by user as an Intent result
     */
    private fun openGalleryForImageSelection() {
        Constants.openGalleryForImageSelection(this)
    }

    /**
     * Called when an intent is supposed to give some return value. Ex: Gallery image picker intent
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == Activity.RESULT_OK){
            if(requestCode == Constants.GALLERY_REQUEST_CODE){
                if(data != null) {
                    val contentURl = data.data
                    mSelectedImageFileUri = contentURl
                    try {
                        val selectedImageBitmap = Media.getBitmap(this.contentResolver, contentURl)
                        binding?.ivProfileUserImage?.setImageBitmap(selectedImageBitmap)
                    }catch (e: IOException){
                        e.printStackTrace()
                        Toast.makeText(this, "Failed to load the image from gallery. " +
                                "Please try again!",
                            Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    /**
     * Sets the user data on my profile page
     */
    fun setUserDataInUI(user: User){

        mUserDetails = user

        Glide
            .with(this@MyProfileActivity)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(findViewById(R.id.iv_profile_user_image))

        binding?.etName?.setText(user.name)
        binding?.etEmail?.setText(user.email)
        if(user.mobile != 0L){
            binding?.etMobile?.setText(user.mobile.toString())
        }
    }


    /**
     * This is inside MyProfile Activity and it creates the hashmap of all the values that the user
     * wants to update in his profile. It sends the created Hashmap to the function with same name
     * in FirestoreClass and there the data is updated for user in Firebase
     */
    private fun updateUserProfileData() {
        val userHashMap = HashMap<String, Any>()

        // This variable is used so we don't call firebase servers when the user hasn't entered any
        // updating data but clicked update button
        var anyChangesMade: Boolean = false

        //if the image is not empty and not the same as previous image
        if(mProfileImageURL.isNotEmpty() && mProfileImageURL != mUserDetails.image){
            userHashMap[Constants.IMAGE] = mProfileImageURL
            anyChangesMade = true
        }

        if(binding?.etName?.text?.toString() != mUserDetails.name){
            userHashMap[Constants.NAME] = binding?.etName?.text.toString()
            anyChangesMade = true
        }

        if(binding?.etMobile?.text?.toString() != mUserDetails.mobile.toString()){
            userHashMap[Constants.MOBILE] = binding?.etMobile?.text.toString().toLong()
            anyChangesMade = true
        }
        if(anyChangesMade)
            FirestoreClass().updateUserProfileData(this, userHashMap )
    }

    /**
     * Uploads the selected image into Firestore and after uploading, calls the
     * UpdateUserProfileData function to update the modified data in firebase servers for user
     */
    private fun uploadUserImage() {
        showProgressDialog(resources.getString(R.string.please_wait))
        if(mSelectedImageFileUri != null) {
            val sRef: StorageReference =
                FirebaseStorage.getInstance().reference.child(
                    "USER_IMAGE"+System.currentTimeMillis() + "." +
                            Constants.getFileExtension(this, mSelectedImageFileUri)
                )

            //here we are uploading the image in firestore and getting its download link
            sRef.putFile(mSelectedImageFileUri!!).addOnSuccessListener {
                taskSnapshot ->
                Log.e("Firebase Image URL",
                taskSnapshot.metadata!!.reference!!.downloadUrl.toString())

                //here "uri" is the download link we get from firestore
                taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                    uri ->
                    Log.e("Dowload Image URL", uri.toString())
                    mProfileImageURL = uri.toString()
                    updateUserProfileData()
                }
            }.addOnFailureListener{
                exception ->
                Toast.makeText(this@MyProfileActivity,
                    exception.message,
                    Toast.LENGTH_LONG
                ).show()
                hideProgressDialog()
            }

        }
    }

    /**
     * Called when user profile is updated to hide progress dialog
     */
    fun profileUpdateSuccess(){
        hideProgressDialog()
        //when the user profile is updated, the intent that opened the profile page closes and before
        //closing it, we want to send a result with it. With this result, we are updating the
        //Navigation drawer for new content
        setResult(Activity.RESULT_OK)
        finish()
    }

}