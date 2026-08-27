package com.example.data

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State

class UserPreferences(context: Context) {
    private val prefs = context.getSharedPreferences("hbpoint_preferences", Context.MODE_PRIVATE)

    private val _isTvMode = mutableStateOf(prefs.getBoolean(KEY_TV_MODE, false))
    val isTvMode: State<Boolean> = _isTvMode

    private val _isAdmin = mutableStateOf(prefs.getBoolean(KEY_IS_ADMIN, false))
    val isAdmin: State<Boolean> = _isAdmin

    private val _isLoggedIn = mutableStateOf(prefs.getBoolean(KEY_LOGGED_IN, false))
    val isLoggedIn: State<Boolean> = _isLoggedIn

    private val _profileName = mutableStateOf(prefs.getString(KEY_PROFILE_NAME, "HB Member") ?: "HB Member")
    val profileName: State<String> = _profileName

    private val _profileEmail = mutableStateOf(prefs.getString(KEY_PROFILE_EMAIL, "") ?: "")
    val profileEmail: State<String> = _profileEmail

    fun setTvMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_TV_MODE, enabled).apply()
        _isTvMode.value = enabled
    }

    fun setLoggedInUser(name: String, email: String, isAdmin: Boolean) {
        prefs.edit()
            .putBoolean(KEY_LOGGED_IN, true)
            .putBoolean(KEY_IS_ADMIN, isAdmin)
            .putString(KEY_PROFILE_NAME, name)
            .putString(KEY_PROFILE_EMAIL, email)
            .apply()
        _isLoggedIn.value = true
        _isAdmin.value = isAdmin
        _profileName.value = name
        _profileEmail.value = email
    }

    fun setAdmin(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_IS_ADMIN, enabled).apply()
        _isAdmin.value = enabled
    }

    fun setProfileName(name: String) {
        prefs.edit().putString(KEY_PROFILE_NAME, name).apply()
        _profileName.value = name
    }

    fun logout() {
        prefs.edit()
            .putBoolean(KEY_LOGGED_IN, false)
            .putBoolean(KEY_IS_ADMIN, false)
            .putString(KEY_PROFILE_NAME, "HB Member")
            .putString(KEY_PROFILE_EMAIL, "")
            .apply()
        _isLoggedIn.value = false
        _isAdmin.value = false
        _profileName.value = "HB Member"
        _profileEmail.value = ""
    }

    companion object {
        private const val KEY_TV_MODE = "tv_mode"
        private const val KEY_IS_ADMIN = "is_admin"
        private const val KEY_LOGGED_IN = "logged_in"
        private const val KEY_PROFILE_NAME = "profile_name"
        private const val KEY_PROFILE_EMAIL = "profile_email"
    }
}
