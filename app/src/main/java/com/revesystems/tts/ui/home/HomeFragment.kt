package com.revesystems.tts.ui.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.revesystems.tts.R
import com.revesystems.tts.core.BaseFragment
import com.revesystems.tts.data.source.local.datastore.DataStore
import com.revesystems.tts.data.source.local.datastore.DataStore.getStringValF
import com.revesystems.tts.databinding.FragmentHomeBinding
import com.revesystems.tts.utils.toast
import kotlinx.coroutines.launch

class HomeFragment : BaseFragment<FragmentHomeBinding,HomeViewModel>() {
    override fun setLayout(inflater: LayoutInflater, container: ViewGroup?): FragmentHomeBinding = FragmentHomeBinding.inflate(layoutInflater)
    override fun setViewModel(): Class<HomeViewModel>  = HomeViewModel::class.java

    override fun init(savedInstanceState: Bundle?) {

    }
}