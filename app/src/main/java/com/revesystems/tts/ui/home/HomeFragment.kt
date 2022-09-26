package com.revesystems.tts.ui.home

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.parser.PdfTextExtractor
import com.revesystems.tts.R
import com.revesystems.tts.core.BaseFragment
import com.revesystems.tts.databinding.FragmentHomeBinding
import com.revesystems.tts.utils.*
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import java.io.IOException
import java.io.InputStream


class HomeFragment : BaseFragment<FragmentHomeBinding,HomeViewModel>() {
    private lateinit var inputStream: InputStream

    // Initialize result launcher
    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){

        if (it.resultCode == RESULT_OK && it != null) {
            controlVisibility(true)
            getTextsFromPdf(it.data?.data!!)
//            stripText(it.data?.data!!)
        }else{
            controlVisibility(false)
        }
    }

    override fun setLayout(inflater: LayoutInflater, container: ViewGroup?): FragmentHomeBinding = FragmentHomeBinding.inflate(layoutInflater)
    override fun setViewModel(): Class<HomeViewModel>  = HomeViewModel::class.java

    override fun init(savedInstanceState: Bundle?) {
        binding.etText.addTextChangedListener(tvWatcher)
        clickEvents()
        PDFBoxResourceLoader.init(requireContext())
    }

    private fun clickEvents(){
        binding.btnSelectPdf.setOnClickListener { selectPdf() }
        binding.includeSetting.btnSettings.setOnClickListener {
            binding.includeSetting.groupSetting.visibility = VISIBLE
            binding.includeSetting.btnPlay.visibility = GONE
            binding.includeSetting.btnPause.visibility = GONE
            binding.includeSetting.btnSettings.visibility = GONE
        }
        binding.includeSetting.btnClose.setOnClickListener {
            binding.includeSetting.groupSetting.visibility = GONE
            binding.includeSetting.btnPlay.visibility = VISIBLE
            binding.includeSetting.btnPause.visibility = GONE
            binding.includeSetting.btnSettings.visibility = VISIBLE
        }

        binding.includeSetting.tvAscii.setOnClickListener {
            binding.includeSetting.tvAscii.setBackgroundResource(R.drawable.bg_r4_sol_136)
            binding.includeSetting.tvAscii.setTextColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.includeSetting.tvUnicode.setBackgroundResource(R.color.white)
            binding.includeSetting.tvUnicode.setTextColor(ContextCompat.getColor(requireContext(),R.color._136EE5))

        }
        binding.includeSetting.tvUnicode.setOnClickListener {
            binding.includeSetting.tvAscii.setBackgroundResource(R.color.white)
            binding.includeSetting.tvAscii.setTextColor(ContextCompat.getColor(requireContext(),R.color._136EE5))
            binding.includeSetting.tvUnicode.setBackgroundResource(R.drawable.bg_r4_sol_136)
            binding.includeSetting.tvUnicode.setTextColor(ContextCompat.getColor(requireContext(),R.color.white))

        }

        binding.includeSetting.tvText.setOnClickListener {
            binding.includeSetting.tvText.setBackgroundResource(R.drawable.bg_r4_sol_136)
            binding.includeSetting.tvText.setTextColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.includeSetting.tvSSML.setBackgroundResource(R.color.white)
            binding.includeSetting.tvSSML.setTextColor(ContextCompat.getColor(requireContext(),R.color._136EE5))

        }
        binding.includeSetting.tvSSML.setOnClickListener {
            binding.includeSetting.tvText.setBackgroundResource(R.color.white)
            binding.includeSetting.tvText.setTextColor(ContextCompat.getColor(requireContext(),R.color._136EE5))
            binding.includeSetting.tvSSML.setBackgroundResource(R.drawable.bg_r4_sol_136)
            binding.includeSetting.tvSSML.setTextColor(ContextCompat.getColor(requireContext(),R.color.white))

        }

        binding.includeSetting.tvMale.setOnClickListener {
            binding.includeSetting.tvMale.setBackgroundResource(R.drawable.bg_r4_sol_fafa)
            binding.includeSetting.tvMale.setTextColor(ContextCompat.getColor(requireContext(),R.color._136EE5))
            binding.includeSetting.tvFemale.setBackgroundResource(R.color.white)
            binding.includeSetting.tvFemale.setTextColor(ContextCompat.getColor(requireContext(),R.color._999DA7))
        }
        binding.includeSetting.tvFemale.setOnClickListener {
            binding.includeSetting.tvMale.setBackgroundResource(R.color.white)
            binding.includeSetting.tvMale.setTextColor(ContextCompat.getColor(requireContext(),R.color._999DA7))
            binding.includeSetting.tvFemale.setBackgroundResource(R.drawable.bg_r4_sol_fafa)
            binding.includeSetting.tvFemale.setTextColor(ContextCompat.getColor(requireContext(),R.color._136EE5))
        }

        binding.includeSetting.tvImmature.setOnClickListener {
            binding.includeSetting.tvImmature.setBackgroundResource(R.drawable.bg_r4_sol_fafa)
            binding.includeSetting.tvImmature.setTextColor(ContextCompat.getColor(requireContext(),R.color._136EE5))
            binding.includeSetting.tvMature.setBackgroundResource(R.color.white)
            binding.includeSetting.tvMature.setTextColor(ContextCompat.getColor(requireContext(),R.color._999DA7))

        }
        binding.includeSetting.tvMature.setOnClickListener {
            binding.includeSetting.tvImmature.setBackgroundResource(R.color.white)
            binding.includeSetting.tvImmature.setTextColor(ContextCompat.getColor(requireContext(),R.color._999DA7))
            binding.includeSetting.tvMature.setBackgroundResource(R.drawable.bg_r4_sol_fafa)
            binding.includeSetting.tvMature.setTextColor(ContextCompat.getColor(requireContext(),R.color._136EE5))

        }

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
        var pdfTexts = ""
        val stringBuilder = StringBuilder()
        var pdfReader:PdfReader? = null
        try {
            inputStream = requireActivity().contentResolver.openInputStream(uri)!!
        }catch (e: IOException){
            toast("Something went wrong")
        }

        try {
//            if (!inputStream.markSupported())
//                return
            pdfReader = PdfReader(inputStream)
            if (pdfReader.cryptoMode != -1) {
                toast("data encrypted")
                return
            }
            val pageCount = pdfReader.numberOfPages
            for (i in 0 until pageCount){
                pdfTexts += PdfTextExtractor.getTextFromPage(pdfReader,i+1).trim()+"\n"
            }
            stringBuilder.append(pdfTexts)
            pdfReader.close()
            binding.etText.setText(pdfTexts)
        }catch (e:IOException){
        }catch (e: java.lang.Exception){

        }
    }

    private fun stripText(uri: Uri) {
        var parsedText: String? = null
        var document: PDDocument? = null
        try {
            inputStream = requireActivity().contentResolver.openInputStream(uri)!!
            document = PDDocument.load(inputStream)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        try {
            val pdfStripper = PDFTextStripper()
            pdfStripper.startPage = 0
            pdfStripper.endPage = 4
            parsedText = pdfStripper.getText(document)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                document?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        binding.etText.setText(parsedText)
    }
}