package com.revesystems.tts.ui.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.revesystems.tts.R
import com.revesystems.tts.core.BaseFragment
import com.revesystems.tts.databinding.FragmentHomeBinding

class HomeFragment : BaseFragment<FragmentHomeBinding>() {
    override fun init(savedInstanceState: Bundle?) {
        binding.tv.text = "Abc"
    }

    override fun setLayout(inflater: LayoutInflater, container: ViewGroup?): FragmentHomeBinding = FragmentHomeBinding.inflate(layoutInflater)

}