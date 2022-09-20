package com.revesystems.tts.core

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import kotlin.reflect.KClass

abstract class BaseFragment<VIEW_BINDING: ViewBinding,VIEW_MODEL: ViewModel>:Fragment() {
    protected lateinit var binding: VIEW_BINDING
    protected lateinit var viewModel: VIEW_MODEL

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = setLayout(inflater,container)
        viewModel = ViewModelProvider(this)[setViewModel()]
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(savedInstanceState)
    }

    protected abstract fun init(savedInstanceState: Bundle?)

    abstract fun setLayout(inflater: LayoutInflater, container: ViewGroup?): VIEW_BINDING
    abstract fun setViewModel(): Class<VIEW_MODEL>
}
