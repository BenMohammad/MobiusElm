package com.benmohammad.mobiuselm.login.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.benmohammad.mobiuselm.R

class LoginActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_main)

        if(supportFragmentManager.findFragmentByTag("TAG") == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.login_fragment, LoginFragment(), "TAG")
                .commit()
        }
    }
}