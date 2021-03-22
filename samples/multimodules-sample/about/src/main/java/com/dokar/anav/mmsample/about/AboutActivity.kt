package com.dokar.anav.mmsample.about

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import com.dokar.anav.annotation.Navigable

@Navigable
class AboutActivity : Activity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(
            TextView(this).also {
                it.text = "About"
            }
        )
    }
}