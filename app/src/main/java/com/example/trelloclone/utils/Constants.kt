package com.example.trelloclone.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import android.provider.Settings
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.content.ContextCompat.startActivity
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener

object Constants {

    //Table name
    const val USERS: String = "USERS"

    const val BOARDS: String = "boards"

    //Firebase database fields key names
    const val IMAGE: String = "image"
    const val MOBILE : String = "mobile"
    const val NAME: String = "name"
    const val GALLERY_REQUEST_CODE: Int = 1
    const val ASSIGNED_TO: String = "assignedTo"
    const val DOCUMENT_ID: String = "documentId"
    const val TASK_LIST: String = "taskList"

    const val ID: String = "id"
    const val EMAIL: String = "email"

    const val BOARD_MEMBERS_LIST : String = "board_members_list"
    const val SELECT : String = "select"
    const val UN_SELECT : String = "unselect"

    const val TAG: String = "MyFirebaseMessagingService"

    const val BOARD_DETAIL: String = "board_detail"
    const val TASK_LIST_ITEM_POSITION : String = "task_list_item_position"
    const val CARD_LIST_ITEM_POSiTION : String = "card_list_item_position"

    const val TRELLO_PREFERENCES = "TrelloPreferences"
    const val FCM_TOKEN_UPDATED: String = "fcmTokenUpdated"
    const val FCM_TOKEN : String = "fcmToken"

    const val FCM_BASE_URL:String = "https://fcm.googleapis.com/fcm/send"
    const val FCM_AUTHORIZATION:String = "authorization"
    const val FCM_KEY:String = "key"
    //this server key is unique to every user
    const val FCM_SERVER_KEY:String = "AAAA_ggZS9I:APA91bHrrnvXfJc3UeM4wWgayUrjqopkH2FC00fNWZFNAR9P492bpIAF4frHws-3qsv3oF49g7_-k8ftbb-kzp9SXM4Mw217DzQFo0pb5gsNZ41Nc0nyl0x50nFdADEwAAXB8iO88UlT"
    const val FCM_KEY_TITLE:String = "title"
    const val FCM_KEY_MESSAGE:String = "message"
    const val FCM_KEY_DATA:String = "data"
    const val FCM_KEY_TO:String = "to"


    /**
     * returns true if read storage permission is granted, else returns false
     */
    private fun checkForStorageAccess(activity: Activity) : Boolean {
        return checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED
    }

    /**
     * Asks for storage permission and launches the image selector when permission is granted
     */
    private fun askStoragePermission(activity: Activity) {
        Dexter.withContext(activity)
            .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse) {
                    openGalleryForImageSelection(activity)
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse) {
                    Toast.makeText(activity,
                        "Storage permission is needed to update profile picture",
                        Toast.LENGTH_LONG).show()
                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: PermissionRequest?,
                    token: PermissionToken?
                ) {
                    storagePermissionRationale(activity)
                }
            }).check()
    }

    /**
     * Open gallery for image selection if read permission granted. Else ask for storage read permission
     */
    fun openGalleryForImageSelection(activity: Activity) {
        if(checkForStorageAccess(activity)){
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(activity, intent, GALLERY_REQUEST_CODE, null)
        }else{
            askStoragePermission(activity)
        }
    }

    /**
     * When permission for storage isn't granted, this rationale is shown which directs the user
     * to App's settings page
     */
    fun storagePermissionRationale(activity: Activity) {
        val rationaleDialog = AlertDialog.Builder(activity)
        rationaleDialog.setMessage("Storage Permission needed to select image for profile picture. " +
                "Kindly grant the permissions from settings.")
        rationaleDialog.setNegativeButton("Cancel") {
                dialog, _ ->
            dialog.dismiss()
        }
        rationaleDialog.setPositiveButton("Go To Settings") {
                _, _ ->
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", activity.packageName, null)
            intent.data = uri
            startActivity(activity, intent, null)
        }

        rationaleDialog.show()
    }

    /**
     * This function takes the url of the uploaded image on Firestore and checks the extension of that
     * image and returns the extension name as string
     */
    fun getFileExtension(activity: Activity, uri: Uri?): String? {
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(activity.contentResolver.getType(uri!!))
    }

}