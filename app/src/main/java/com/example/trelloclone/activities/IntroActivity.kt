package com.example.trelloclone.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import com.example.trelloclone.databinding.ActivityIntroBinding

class IntroActivity : BaseActivity() {

    var binding: ActivityIntroBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        //removing status bar from activity screen
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)

        binding?.btnSignUpIntro?.setOnClickListener{
            startActivity(Intent(this, SignupActivity::class.java))
        }

        binding?.btnSignInIntro?.setOnClickListener {
            startActivity(Intent(this, SigninActivity::class.java))
        }

    }
}