package com.benmohammad.mobiuselm

import android.content.Intent
import androidx.fragment.app.FragmentActivity

class AndroidNavigator(private val activity: FragmentActivity): Navigator {

    override fun goToMainScreen() {
        val i = Intent(activity, MainActivity::class.java)
        activity.startActivity(i)
        activity.finish()
    }
}