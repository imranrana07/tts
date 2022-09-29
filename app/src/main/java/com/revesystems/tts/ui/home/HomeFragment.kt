package com.revesystems.tts.ui.home

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.BackgroundColorSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.parser.PdfTextExtractor
import com.revesystems.tts.R
import com.revesystems.tts.core.BaseFragment
import com.revesystems.tts.data.model.PlayListModel
import com.revesystems.tts.data.model.ReqModel
import com.revesystems.tts.databinding.FragmentHomeBinding
import com.revesystems.tts.utils.GONE
import com.revesystems.tts.utils.INVISIBLE
import com.revesystems.tts.utils.VISIBLE
import com.revesystems.tts.utils.toast
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.*

class HomeFragment : BaseFragment<FragmentHomeBinding,HomeViewModel>() {
    private lateinit var inputStream: InputStream
    private var currentSBPosition = 0
    private var playerListening: MediaPlayer?=null
    private var lines = ArrayList<String>()
    private var playList = ArrayList<PlayListModel>()
    private var isPlaying = false
    private var isPaused = false
    private var startingPoint = 0
    private var currentPlayPosition = 0
    private var url = ""

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
        PDFBoxResourceLoader.init(requireContext())
        observers()
    }

    private fun clickEvents(){
        binding.btnSelectPdf.setOnClickListener { selectPdf() }
        binding.btnSettings.setOnClickListener {
            binding.includeSetting.groupSetting.visibility = VISIBLE
            binding.includeSetting.btnPlay.visibility = GONE
            binding.btnSettings.visibility = GONE
        }
        binding.includeSetting.btnClose.setOnClickListener {
            binding.includeSetting.groupSetting.visibility = GONE
            if (binding.includeSetting.playSetting.visibility != VISIBLE)
                binding.includeSetting.btnPlay.visibility = VISIBLE
            binding.btnSettings.visibility = VISIBLE
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

        binding.includeSetting.btnPlayBlue.setOnClickListener {
            isPaused = false
            if (playerListening!= null){
                playerListening?.seekTo(currentPlayPosition)
                playerListening?.start()
                binding.includeSetting.btnPlayBlue.visibility = INVISIBLE
                binding.includeSetting.btnPause.visibility = VISIBLE
                currentPlayPosition = 0
            }
        }
        binding.includeSetting.btnPause.setOnClickListener {
            isPaused = true
            if (playerListening!=null && playerListening?.isPlaying == true){
                playerListening?.pause()
                currentPlayPosition = playerListening?.currentPosition!!
                binding.includeSetting.btnPause.visibility = INVISIBLE
                binding.includeSetting.btnPlayBlue.visibility = VISIBLE
            }

        }

        binding.includeSetting.btnPlay.setOnClickListener {
            isPaused = false
            binding.includeSetting.btnPlay.visibility = GONE
            binding.includeSetting.playSetting.visibility = VISIBLE
            binding.includeSetting.btnPlayBlue.visibility = INVISIBLE
            binding.includeSetting.btnPause.visibility = VISIBLE
            splitTexts()
            currentPlayPosition = 0
        }

        binding.includeSetting.btnDownloadTxt.setOnClickListener {
            saveFile()
        }

        binding.includeSetting.btnDownloadAudio.setOnClickListener {
            saveAudio()
        }

    }

    private fun observers(){
        viewModel.progressBar.observe(this) {
//            binding.progressBar.visibility = it
        }

        viewModel.success.observe(this) {
            url = it?.output!!
            playOnlineAudio()
            it?.output?.let {
                    it1 -> playList.add(PlayListModel(lines[0],it1))
                lines.removeAt(0)
                if (lines.isNotEmpty())
                    viewModel.getAudio(ReqModel(lines[0]))
            }
        }

        viewModel.error.observe(this) {
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
//            viewModel.getAudio(ReqModel(lines[0]))
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
        var pdfReader: PdfReader? = null
        try {
            inputStream = requireActivity().contentResolver.openInputStream(uri)!!
        }catch (e: IOException){
            toast("Something went wrong")
        }

        try {
            pdfReader = PdfReader(inputStream)
            if (pdfReader.cryptoMode != -1) {
                toast("data encrypted")
//                return
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

    private fun playAudio(){
        binding.includeSetting.skPlayProgress.progress = 0
        binding.includeSetting.skPlayProgress.max = 100
        seekBarChange()
    }

    private fun seekBarChange(){
        object: CountDownTimer(10000, 100){
            override fun onTick(p0: Long) {
                milliToTime(p0)
                binding.includeSetting.skPlayProgress.progress = 100-(p0/100).toInt()
            }
            override fun onFinish() {
                binding.includeSetting.btnPlayBlue.visibility = VISIBLE
                binding.includeSetting.btnPause.visibility = INVISIBLE
            }
        }.start()

        binding.includeSetting.skPlayProgress.setOnSeekBarChangeListener(object :SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, p1: Int, p2: Boolean) {
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {

            }

            override fun onStopTrackingTouch(p0: SeekBar?) {

            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun milliToTime(millis: Long){
        val milliToSecond = (millis/1000)
        val minute = milliToSecond/60
        val seconds = milliToSecond % 60
        val hrs = (minute/60)
        binding.includeSetting.tvTimer.text = "$hrs:$minute:$seconds"
    }

    private fun saveFile(){
        try {
            val exDir = Environment.getExternalStorageDirectory()
            val directory = File(exDir.absolutePath.toString() + "/TTS")
            directory.mkdirs()
            val  file = File(exDir,"data.txt")
            val fileOutputStream = FileOutputStream(file)
            val outputStreamWriter = OutputStreamWriter(fileOutputStream);
            outputStreamWriter.write(binding.etText.text.toString())
            outputStreamWriter.flush();
            outputStreamWriter.close();
            Toast.makeText(requireContext(), "saved", Toast.LENGTH_LONG).show()
        } catch (e: IOException) {
            Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG)
                .show();
        }
    }

    @SuppressLint("NewApi")
    private fun saveAudio(){
        val urls = java.util.Base64.getUrlDecoder().decode("data:audio/wav;base64,$url")
        val downloadManager = requireContext().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager?
        val uri = Uri.parse(String(urls)/*"data:audio/wav;base64,${ playList[2].url }"*//*"https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"*/)
        val request = DownloadManager.Request(uri)
//        request.setVisibleInDownloadsUi(true)
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalPublicDir(
            Environment.DIRECTORY_DOWNLOADS,
            uri.lastPathSegment
        )
        toast("download started")
        downloadManager!!.enqueue(request)
    }

    private fun splitTexts(){
            val delimiter = " "
            lines.addAll(binding.etText.text?.trim().toString().split(delimiter).toTypedArray())
            viewModel.getAudio(ReqModel(lines[0]))
    }

    @SuppressLint("NewApi")
    private fun playOnlineAudio() {
        if (playList.isEmpty() || playerListening?.isPlaying == true) {
            return
        }
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                playerListening = MediaPlayer()
                playerListening?.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                playerListening?.setDataSource("data:audio/wav;base64,${playList[0].url}")
                playerListening?.prepareAsync()
                playerListening?.setOnPreparedListener {
                    if (isPaused)
                        return@setOnPreparedListener
                    highlightText(playList[0].word)
                    playerListening?.start()
                    isPlaying = true
                }
                playerListening?.setOnCompletionListener {
                    playerListening?.stop()
                    playerListening?.reset()
                    if (playList.isNotEmpty()) {
                        playList.removeAt(0)
                        if (playList.isNotEmpty()) {
                            playOnlineAudio()
                        }else{
                            binding.includeSetting.playSetting.visibility = GONE
                            binding.includeSetting.groupSetting.visibility = GONE
                            binding.includeSetting.btnPlay.visibility = VISIBLE
                            startingPoint = 0
                        }
                    }
                    isPlaying = false
                }
            } catch (e: IOException) {
                e.message?.let { toast(it) }
            } catch ( e: IllegalArgumentException) {
                e.message?.let { toast(it) }
            } catch (e: IllegalStateException) {
                e.message?.let { toast(it) }
            }
        }
    }

    private fun highlightText(text: String){
        binding.etText.setBackgroundColor(Color.WHITE)
        val spannable = SpannableString(binding.etText.text)
        val colorW = BackgroundColorSpan(Color.WHITE)
        spannable.setSpan(colorW,0,binding.etText.text?.length!!,Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        val color = BackgroundColorSpan(Color.YELLOW)
        spannable.setSpan(color,startingPoint,startingPoint+text.length,Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        binding.etText.setText(spannable)
        if (playList[playList.size-1].word != text) {
            startingPoint += text.length + 1
        }
    }
}