package com.benmohammad.mobiuselm.login.view

import android.media.audiofx.AudioEffect

interface ILoginView {

    fun setProgress(show: Boolean)

    fun showPasswordError(errorText: String?)

    fun showLoginError(errorText: String?)

    fun setError(error: String?)

    fun hideKeyBoard();

    fun setEnabledLoginBtn(enabled: Boolean)
}