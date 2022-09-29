package com.revesystems.tts.ui.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.revesystems.tts.R
import com.revesystems.tts.core.BaseFragment
import com.revesystems.tts.databinding.FragmentSplashBinding
import com.revesystems.tts.ui.SpareViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashFragment : BaseFragment<FragmentSplashBinding,SpareViewModel>() {

    override fun setViewModel(): Class<SpareViewModel> = SpareViewModel::class.java
    override fun setLayout(inflater: LayoutInflater, container: ViewGroup?): FragmentSplashBinding = FragmentSplashBinding.inflate(layoutInflater)

    override fun init(savedInstanceState: Bundle?) {
        lifecycleScope.launch {
            delay(2000)
            findNavController().navigate(R.id.homeFragment)
        }
    }
}