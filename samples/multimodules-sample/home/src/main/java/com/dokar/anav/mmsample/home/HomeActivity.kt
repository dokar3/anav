package com.dokar.anav.mmsample.home

import android.app.Activity
import android.os.Bundle
import com.dokar.anav.annotation.Navigable
import com.dokar.anav.mmsample.base.navigation.NavArgs.Account.userId
import com.dokar.anav.mmsample.base.navigation.NavArgs.Account.username
import com.dokar.anav.mmsample.base.navigation.NavMap
import com.dokar.anav.mmsample.home.databinding.ActivityHomeBinding
import com.dokar.anav.navigate

@Navigable(group = "home")
class HomeActivity : Activity() {

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnAccount.setOnClickListener {
            navigate(NavMap.Home.Account) {
                userId = 34612
                username = "Bee"
            }
        }

        binding.btnAbout.setOnClickListener {
            navigate(NavMap.About)
        }

        binding.btnSettings.setOnClickListener {
        }

    }
}