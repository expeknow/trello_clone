package com.example.trelloclone.activities

import android.content.Intent
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import com.example.trelloclone.databinding.ActivitySplashBinding
import com.example.trelloclone.firebase.FirestoreClass

/**
 * Splash Activity is the first logo screen that popup's up whenever the app is launched.
 */

class SplashActivity : AppCompatActivity() {

    var binding: ActivitySplashBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        //removing status bar from activity screen
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)


        //to use custom fonts you need asset folder (New -> Folders -> Asset Folder) and ttf file
        //just put the ttf file in the asset folder
        val typeFace: Typeface = Typeface.createFromAsset(assets, "SemiBold.ttf")
        binding?.tvAppNameSplashScreen?.typeface = typeFace

        //moving from this screen to Intro screen if the user id is not present or in the Main
        //Activity if the user Id is present
        Handler().postDelayed({
            val currentUserID = FirestoreClass().getCurrentUserId()
            if(currentUserID.isNotEmpty()){
                startActivity(Intent(this, MainActivity::class.java))
            }
            else{
                startActivity(Intent(this, IntroActivity::class.java))
            }
            finish()
        }, 2000)
    }
}