package com.dokar.anav.ktsample

import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import com.dokar.anav.annotation.Navigable
import com.dokar.anav.ktsample.navigation.NavArgs.Profile.userId
import com.dokar.anav.ktsample.navigation.NavArgs.Profile.userName

@Navigable(
    args = ["userName", "userId"],
    argTypes = [String::class, Int::class]
)
class ProfileActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val text = "userId: ${intent.userId}\n" +
                "userName: ${intent.userName}"
        setContentView(
            TextView(this).also {
                it.textSize = 18f
                it.text = text
            }
        )
    }
}