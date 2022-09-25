package com.revesystems.tts.ui.home

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.parser.PdfTextExtractor
import com.revesystems.tts.core.BaseFragment
import com.revesystems.tts.databinding.FragmentHomeBinding
import com.revesystems.tts.utils.*
import com.shockwave.pdfium.PdfDocument
import java.io.IOException
import java.io.InputStream

class HomeFragment : BaseFragment<FragmentHomeBinding,HomeViewModel>() {
    private lateinit var inputStream: InputStream

    // Initialize result launcher
    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){

        if (it.resultCode == RESULT_OK && it != null) {
            controlVisibility(true)
            getTextsFromPdf(it.data?.data!!)
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
//        if (shouldVisible) {
//            binding.pdfViewer.visibility = VISIBLE
//            binding.tlText.visibility = GONE
//            binding.tvLetterCount.visibility = GONE
//        }else{
//            binding.pdfViewer.visibility =GONE
//            binding.tlText.visibility = VISIBLE
//            binding.tvLetterCount.visibility = VISIBLE
//        }
    }
    private fun getTextsFromPdf(uri: Uri){
        var pdfTexts = "test "
        val stringBuilder = StringBuilder()
        var pdfReader:PdfReader? = null
        try {
            inputStream = requireActivity().contentResolver.openInputStream(uri)!!
        }catch (e: IOException){
            toast("Something went wrong")
        }

        try {
            pdfReader = PdfReader(inputStream)
            val pageCount = pdfReader.numberOfPages
            for (i in 0 until pageCount){
                pdfTexts += PdfTextExtractor.getTextFromPage(pdfReader,i+1).trim()+"\n"
            }
            stringBuilder.append(pdfTexts)
            pdfReader.close()
            binding.etText.setText(pdfTexts)
        }catch (e:IOException){

        }
    }
}