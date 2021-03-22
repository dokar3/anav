package com.dokar.anav.mmsample.home

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import com.dokar.anav.annotation.Navigable
import com.dokar.anav.mmsample.base.navigation.NavArgs.Account.userId
import com.dokar.anav.mmsample.base.navigation.NavArgs.Account.username
import com.dokar.anav.mmsample.home.databinding.ActivityAccountBinding

@Navigable(
    group = "home",
    args = ["userId", "username"],
    argTypes = [Int::class, String::class]
)
class AccountActivity : Activity() {

    private lateinit var binding: ActivityAccountBinding

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userId: Int? = intent.userId
        val username: String? = intent.username

        binding.tvUserId.text = "User id: $userId"
        binding.tvUsername.text = "Username: $username"
    }
}