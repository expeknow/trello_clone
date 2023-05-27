package com.example.trelloclone.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.WindowManager
import com.example.trelloclone.R
import com.example.trelloclone.databinding.ActivitySigninBinding
import com.example.trelloclone.firebase.FirestoreClass
import com.example.trelloclone.models.User
import com.google.firebase.auth.FirebaseAuth

class SigninActivity : BaseActivity() {

    var binding: ActivitySigninBinding? = null
    var mUserEmail : String = ""
    var mUserPassword : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //removing status bar from activity screen
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)

        binding = ActivitySigninBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        binding?.btnSignInSignin?.setOnClickListener {
            logInUser()
        }
        setupActionBar()
    }

    /**
     * used to log in the user in the app when he fills and submits the sign up form
     */
    private fun logInUser(){
        mUserEmail = binding?.etEmailSignin?.text.toString().trim {it <= ' '}
        mUserPassword = binding?.etPasswordSignin?.text.toString().trim {it <= ' ' }

        if(validateForm(mUserEmail, mUserPassword)){
            showProgressDialog(resources.getString(R.string.please_wait))
            FirebaseAuth.getInstance().signInWithEmailAndPassword(
                mUserEmail, mUserPassword
            ).addOnCompleteListener(this) { task ->
                if(task.isSuccessful){
                    FirestoreClass().loadUserData(this)
                }else{
                    showErrorSnackBar(task.exception?.message.toString())
                    hideProgressDialog()
                }
            }

        }
    }

    /**
     * When the user's data has been retrieved from database, we run this function from
     * FireStoreClass to change activity
     */
    fun signInSuccess() {
        hideProgressDialog()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    /**
     * setups the action bar to show the back button
     */
    private fun setupActionBar(){
        setSupportActionBar(binding?.toolbarSigninActivity)
        val actionBar = supportActionBar
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        }
        binding?.toolbarSigninActivity?.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    /**
     * Validates the email and password entered by user
     */
    private fun validateForm(email: String, password: String): Boolean {
        return when {
            TextUtils.isEmpty(email) -> {
                showErrorSnackBar("Please enter an Email address")
                false
            }
            TextUtils.isEmpty(password) -> {
                showErrorSnackBar("Please enter a password")
                false
            }
            else -> {
                true
            }
        }
    }
}