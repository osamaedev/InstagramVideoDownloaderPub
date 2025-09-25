package com.instagram.video.downloader.common

import android.app.Activity
import android.view.inputmethod.InputMethodManager

class KeyboardUtil {
    companion object {
        fun dismissKeyboard(act: Activity?) {
            if (act != null && act.currentFocus != null) {
                val inputMethodManager =
                    act.getSystemService(Activity.INPUT_METHOD_SERVICE) as? InputMethodManager
                inputMethodManager?.hideSoftInputFromWindow(act.currentFocus?.windowToken, 0)
            }
        }
    }
}

fun Activity.dismissKeyboard() {
    KeyboardUtil.dismissKeyboard(this)
}
