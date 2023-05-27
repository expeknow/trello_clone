package com.example.trelloclone.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.WindowManager
import android.widget.Toast
import com.example.trelloclone.R
import com.example.trelloclone.databinding.ActivitySignupBinding
import com.example.trelloclone.firebase.FirestoreClass
import com.example.trelloclone.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class SignupActivity : BaseActivity() {

    var binding : ActivitySignupBinding? = null
    var mUserEmail : String = ""
    var mUserPassword : String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        //removing status bar from activity screen
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)

        setupActionBar()

        binding?.btnSignUpSignUp?.setOnClickListener {
            registerUser()
        }
    }

    /**
     * this function is called to change activities after the user has signed up and his data
     * is successfully stored in firebase database.
     */
    fun userRegisteredSuccess() {
        Toast.makeText(
            this@SignupActivity,
            "You have successfully registered",
            Toast.LENGTH_LONG
        ).show()
        hideProgressDialog()
        FirebaseAuth.getInstance().signOut()

        //Log in user with new account details
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

    /**
     * Called when user has signed up and his user data has been retrieved from firebase to
     * change the activity
     */
    fun signUpSuccess() {
        hideProgressDialog()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    /**
     * setups the action bar to show the back button
     */
    private fun setupActionBar() {
        setSupportActionBar(binding?.toolbarSignUpActivity)
        val actionBar = supportActionBar
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        }
        binding?.toolbarSignUpActivity?.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    /**
     * Register user with the entered credentials in the Sign Up Form. This function is located in
     * SingupActivity
     */
    private fun registerUser() {
        val name: String = binding?.etName?.text.toString().trim {it  <= ' '}
        val email: String = binding?.etEmail?.text.toString().trim {it  <= ' '}
        val password: String = binding?.etPassword?.text.toString().trim {it  <= ' '}
        mUserEmail = email
        mUserPassword = password

        if(validateForm(name, email, password)){
            showProgressDialog(resources.getString(R.string.please_wait))
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(
                email, password
            ).addOnCompleteListener { task ->

                if (task.isSuccessful) {
                    //here we are saving user data in firestore using our Firestore class
                    val firebaseUser: FirebaseUser = task.result!!.user!!
                    val registerEmail = firebaseUser.email!!
                    val user = User(firebaseUser.uid, name, registerEmail)
                    FirestoreClass().registerUser(this, user)

                } else {
                    Toast.makeText(
                        this@SignupActivity,
                        task.exception!!.message,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

    }

    /**
     * Validates the email and password entered by user
     */
    private fun validateForm(name: String, email: String, password: String): Boolean {
        return when {
            TextUtils.isEmpty(name) -> {
                showErrorSnackBar("Please enter a name")
                false
            }
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