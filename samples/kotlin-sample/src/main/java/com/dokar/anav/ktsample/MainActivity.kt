package com.dokar.anav.ktsample

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import com.dokar.anav.annotation.Navigable
import com.dokar.anav.ktsample.navigation.NavArgs.Profile.userId
import com.dokar.anav.ktsample.navigation.NavArgs.Profile.userName
import com.dokar.anav.ktsample.navigation.NavMap
import com.dokar.anav.navigationBuilder

@Navigable(
    args = ["key"]
)
class MainActivity : Activity() {

    private var userId: Int? = 0

    private var userName: String? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(
            LinearLayout(this).also { root ->
                root.orientation = LinearLayout.VERTICAL

                EditText(this).let {
                    root.addView(it)
                    it.inputType = EditorInfo.TYPE_CLASS_NUMBER
                    it.hint = "User id"
                    it.addTextChangedListener { text ->
                        userId = text.toString().toIntOrNull()
                    }
                    it.setText("84214")
                }

                EditText(this).let {
                    root.addView(it)
                    it.hint = "Username"
                    it.addTextChangedListener { text ->
                        userName = text.toString()
                    }
                    it.setText("Nameless")
                }

                Button(this).let {
                    root.addView(it)
                    it.text = "Go"
                    it.setOnClickListener { go() }
                }
            }
        )
    }

    private fun go() {
        navigationBuilder(this, NavMap.Profile)
            .also {
                it.userId = userId ?: -1
                it.userName = userName
            }
            .onFailure {
                Toast.makeText(
                    this,
                    "Unable to start activity: $it",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .go()
    }

}