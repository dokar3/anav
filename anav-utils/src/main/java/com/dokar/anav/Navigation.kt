package com.dokar.anav

import android.content.Context
import android.content.Intent

typealias OnFailure = (Throwable) -> Unit

class Navigation private constructor(
    private val context: Context,
    private val intent: Intent,
    private val onFailure: OnFailure? = null
) {

    fun go() {
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            onFailure?.invoke(e)
        }
    }

    class Builder(
        private val context: Context,
        private val className: String
    ) {

        private var onFailure: OnFailure? = null

        private var also: ((Intent) -> Unit)? = null

        fun also(block: (Intent) -> Unit): Builder {
            this.also = block
            return this
        }

        fun onFailure(block: OnFailure): Builder {
            this.onFailure = block
            return this
        }

        fun build(): Navigation {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setClassName(context, className)
                also?.invoke(this)
            }
            return Navigation(context, intent, onFailure)
        }

        fun go() {
            build().go()
        }
    }
}