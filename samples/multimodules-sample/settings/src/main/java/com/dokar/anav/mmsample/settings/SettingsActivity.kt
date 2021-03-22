package com.dokar.anav.mmsample.settings

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import com.dokar.anav.annotation.Navigable

@Navigable(
    args = ["name", "age", "avatar", "twitter"]
)
class SettingsActivity : Activity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(
            TextView(this).also {
                it.text = "Settings"
            }
        )
    }
}