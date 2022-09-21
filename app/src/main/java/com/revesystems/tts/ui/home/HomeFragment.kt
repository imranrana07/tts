package com.revesystems.tts.ui.home

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.os.FileUtils
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import com.revesystems.tts.core.BaseFragment
import com.revesystems.tts.databinding.FragmentHomeBinding
import com.revesystems.tts.utils.GONE
import com.revesystems.tts.utils.VISIBLE
import java.io.File

class HomeFragment : BaseFragment<FragmentHomeBinding,HomeViewModel>() {

    // Initialize result launcher
    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        Log.v("ADSADA","")
        if (it.resultCode == RESULT_OK && it != null) {
            controlVisibility(true)
            binding.pdfViewer.fromUri(it.data?.data!!).load()
        }else{
            controlVisibility(false)
        }
    }

    override fun setLayout(inflater: LayoutInflater, container: ViewGroup?): FragmentHomeBinding = FragmentHomeBinding.inflate(layoutInflater)
    override fun setViewModel(): Class<HomeViewModel>  = HomeViewModel::class.java

    override fun init(savedInstanceState: Bundle?) {
        binding.etText.addTextChangedListener(tvWatcher)
        clickEvents()
    }

    private fun clickEvents(){
        binding.btnSelectPdf.setOnClickListener { selectPdf() }
    }

    private val tvWatcher = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        @SuppressLint("SetTextI18n")
        override fun afterTextChanged(p0: Editable?) {
            binding.tvLetterCount.text = "${p0.toString().length}/2500"
        }
    }

    private fun selectPdf(){
        val pdfIntent = Intent(Intent.ACTION_GET_CONTENT)
        pdfIntent.type = "application/pdf"
        pdfIntent.addCategory(Intent.CATEGORY_OPENABLE)
        resultLauncher.launch(pdfIntent)
    }

    private fun controlVisibility(shouldVisible:Boolean){
        if (shouldVisible) {
            binding.pdfViewer.visibility = VISIBLE
            binding.tlText.visibility = GONE
            binding.tvLetterCount.visibility = GONE
        }else{
            binding.pdfViewer.visibility =GONE
            binding.tlText.visibility = VISIBLE
            binding.tvLetterCount.visibility = VISIBLE
        }
    }
}