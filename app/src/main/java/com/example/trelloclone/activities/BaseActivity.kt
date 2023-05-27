package com.example.trelloclone.activities

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.trelloclone.R
import com.example.trelloclone.databinding.ActivityBaseBinding
import com.example.trelloclone.databinding.DialogProgressBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import org.w3c.dom.Text


/**
 * Base activity is going to be a replacement for our AppCompatActivity Implementation for other
 * activities i.e. now activities will inherit from Base Activity so you use AppCompat + Base
 */

open class BaseActivity : AppCompatActivity() {

    private var doubleBackToExitPressedOnce = false

    private lateinit var mProgressDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)
    }

    /**
     * Base Activity Progress Dialog shows progress dialog with custom loading text
     */
    fun showProgressDialog(text: String) {
        val binding : DialogProgressBinding = DialogProgressBinding.inflate(layoutInflater)
        mProgressDialog = Dialog(this)
        mProgressDialog.setContentView(binding.root)
        binding.tvProgresDialogText.text = text
        mProgressDialog.show()
    }

    /**
     * Hide progress dialog which was created using base Activity
     */
    fun hideProgressDialog() {
        mProgressDialog.hide()
    }

    /**
     * Get current logged in user's ID from Firebase. Used for firebase integration
     */
    fun getCurrentUserId() : String{
        return FirebaseAuth.getInstance().currentUser!!.uid
    }

    /**
     * Implementation for double back to exit function
     */
    fun doubleBackToExit() {
        if(doubleBackToExitPressedOnce){
            super.onBackPressed()
            return
        }
        this.doubleBackToExitPressedOnce = true
        Toast.makeText(
            this,
            resources.getString(R.string.please_click_back_again_to_exit),
            Toast.LENGTH_LONG
        ).show()

        //If user presses back only once, we want to reset the counter after a few seconds
        Handler().postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
    }

    /**
     * Snackbar in base Activity. Used to show error logs to user using a snackbar
     */
    fun showErrorSnackBar(message: String) {
        val snackBar = Snackbar.make(
            findViewById(android.R.id.content),
            message,
            Snackbar.LENGTH_LONG)
        val snackbarView = snackBar.view
        snackbarView.setBackgroundColor(ContextCompat.getColor(this, R.color.snackbar_error_color))
        snackBar.show()
    }
}