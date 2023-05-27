package com.example.trelloclone.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.example.trelloclone.R
import com.example.trelloclone.databinding.ActivityCreateBoardBinding
import com.example.trelloclone.firebase.FirestoreClass
import com.example.trelloclone.models.Board
import com.example.trelloclone.utils.Constants
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.IOException

class CreateBoardActivity : BaseActivity() {

    var binding: ActivityCreateBoardBinding? = null
    private var mSelectedImageFileUri : Uri? = null
    private lateinit var mUserName: String
    private var mBoardImageURL : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateBoardBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        binding?.ivBoardImage?.setOnClickListener {
            selectImageForBoard()
        }

        setUpActionBar()

        if(intent.hasExtra(Constants.NAME)){
            mUserName = intent.getStringExtra(Constants.NAME)!!
        }

        binding?.btnCreate?.setOnClickListener {

            // This condition will work when board image is added by user
            if(mSelectedImageFileUri != null){
                uploadBoardImage()
            }
            // When there is no board image, we create board without image
            else{
                showProgressDialog(resources.getString(R.string.please_wait))
                createBoard()
            }
        }
    }

    /**
     * Called when board is created to hide progress bar and close the create board activity screen
     */
    fun boardCreatedSuccessfully() {
        hideProgressDialog()
        //So List of boards in main activity are refreshed after a new board is created
        setResult(Activity.RESULT_OK)
        finish()
    }

    /**
     * Set up action bar with back button and title
     */
    private fun setUpActionBar() {
        val actionBar = binding?.toolbarCreateBoardActivity
        setSupportActionBar(actionBar)
        if(supportActionBar != null){
            supportActionBar?.title = resources.getString(R.string.create_board_title)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        }
        actionBar?.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    /**
     * Select image for board calls the image selection code in constant file for this activity
     */
    private fun selectImageForBoard() {
        Constants.openGalleryForImageSelection(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == Constants.GALLERY_REQUEST_CODE){
            if(data != null) {

                //data.data gives the uri of the file this intent is targeting
                val contentUri = data.data
                mSelectedImageFileUri = contentUri
                try {
                    val selectedImageBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, contentUri)
                    binding?.ivBoardImage?.setImageBitmap(selectedImageBitmap)
                }catch (e: IOException){
                    e.printStackTrace()
                    Toast.makeText(this, "Failed to load the image from gallery. " +
                            "Please try again!",
                        Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * Create board takes all the details of board and updates all those data in Firebase by creating
     * a new board instanace in firebase collection. Usually Called after the board image has been uploaded
     * on Firestore
     */
    private fun createBoard() {

        val assignedUserArrayList : ArrayList<String> = ArrayList()
        assignedUserArrayList.add(getCurrentUserId())
        val boardName = binding?.etBoardName?.text.toString()

        if(validateForm(boardName)){
            val board = Board(boardName, mBoardImageURL, mUserName, assignedUserArrayList)
            FirestoreClass().createBoard(this, board)
            return
        }
        hideProgressDialog()
    }

    /**
     * Upload selected board image in firestore and creates the board with the download link of
     * uploaded board image
     */
    private fun uploadBoardImage(){
        showProgressDialog(resources.getString(R.string.please_wait))
        if(mSelectedImageFileUri != null) {

            //Storage reference is an object of Google Cloud Storage used to upload the image
            val sRef: StorageReference =
                FirebaseStorage.getInstance().reference.child(
                    "BOARD_IMAGE"+System.currentTimeMillis() + "." +
                            Constants.getFileExtension(this, mSelectedImageFileUri)
                )

            //here we are uploading the image in firestore and getting its download link
            sRef.putFile(mSelectedImageFileUri!!).addOnSuccessListener {
                    taskSnapshot ->
                //Sample log: com.google.android.gms.tasks.zzu@31c5ccb
                Log.e("Board Image URL",
                    taskSnapshot.metadata!!.reference!!.downloadUrl.toString())

                //here "uri" is the download link we get from firestore
                taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                        uri ->
                    Log.e("Download Image URL", uri.toString())
                    mBoardImageURL = uri.toString()
                    createBoard()
                }
            }.addOnFailureListener{
                    exception ->
                Toast.makeText(this,
                    exception.message,
                    Toast.LENGTH_LONG
                ).show()
                hideProgressDialog()
            }

        }else
            hideProgressDialog()
    }

    /**
     * Checks if the board name is entered by the user or not.
     */
    private fun validateForm(boardName: String) : Boolean {
        if(TextUtils.isEmpty(boardName)){
            showErrorSnackBar("Please enter a name for the board!")
            return false
        }
        return true
    }


}