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
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.BackgroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.itextpdf.text.ExceptionConverter
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.parser.PdfTextExtractor
import com.revesystems.tts.core.BaseFragment
import com.revesystems.tts.data.model.PlayListModel
import com.revesystems.tts.data.model.ReqModel
import com.revesystems.tts.databinding.FragmentHomeBinding
import com.revesystems.tts.ui.SettingsBottomSheetFragment
import com.revesystems.tts.utils.*
import kotlinx.coroutines.*
import java.io.*
import java.lang.Short.reverseBytes
import java.nio.ByteBuffer
import java.nio.ByteOrder.BIG_ENDIAN
import java.nio.ByteOrder.LITTLE_ENDIAN
import java.nio.charset.StandardCharsets


class HomeFragment : BaseFragment<FragmentHomeBinding,HomeViewModel>() {
    private lateinit var inputStream: InputStream
    //    private var currentSBPosition = 0
    private var playerListening: MediaPlayer?=null
    private var lines = ArrayList<String>()
    private var playList = ArrayList<PlayListModel>()
    private var isPlaying = false
    private var isPaused = false
    private var startingPoint = 0
    private var currentPlayPosition = 0
    private var url = ""
    private var retry = 0
    private var timeCount = 0
    private var allByteArray = ArrayList<ByteArray>()
    private var chunkSize = 0
    private var totalDataSize = 0
    private var subChunk2Size = 0
    private val regex = Regex("[।,.|!]")

    // Initialize result launcher
    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if (it.resultCode == RESULT_OK && it != null) {
            controlVisibility(true)
            getTextsFromPdf(it.data?.data!!)
            if (playerListening!= null)
                playerListening?.reset()
            lines.clear()
            playList.clear()
            binding?.includeSetting?.btnPlay?.visibility = VISIBLE
        }else{
            controlVisibility(false)
        }
    }

    override fun setLayout(inflater: LayoutInflater, container: ViewGroup?): FragmentHomeBinding = FragmentHomeBinding.inflate(layoutInflater)
    override fun setViewModel(): Class<HomeViewModel>  = HomeViewModel::class.java
    @RequiresApi(Build.VERSION_CODES.M)
    override fun init(savedInstanceState: Bundle?) {

        playerListening = MediaPlayer()
        binding!!.etText.addTextChangedListener(tvWatcher)
        clickEvents()
        observers()
        binding!!.tvLetterCount.text = "${binding!!.etText.text.toString().length}/2500"
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun clickEvents(){
        binding!!.btnSelectPdf.setOnClickListener { selectPdf() }
        binding!!.btnSettings.setOnClickListener {
            val bottomSheet = SettingsBottomSheetFragment()
            bottomSheet.show(requireActivity().supportFragmentManager,"")

        }

        binding!!.includeSetting.btnPlayBlue.setOnClickListener {
            isPaused = false
            if (playerListening!= null){
                playerListening?.seekTo(currentPlayPosition)
                playerListening?.start()
                binding!!.includeSetting.btnPlayBlue.visibility = INVISIBLE
                binding!!.includeSetting.btnPause.visibility = VISIBLE
                currentPlayPosition = 0
            }
        }
        binding!!.includeSetting.btnPause.setOnClickListener {
            isPaused = true
            if (playerListening!=null && playerListening?.isPlaying == true){
                playerListening?.pause()
                currentPlayPosition = playerListening?.currentPosition!!
                binding!!.includeSetting.btnPause.visibility = INVISIBLE
                binding!!.includeSetting.btnPlayBlue.visibility = VISIBLE
            }
        }

        binding!!.includeSetting.btnPlay.setOnClickListener {
            lines.clear()
            playList.clear()
//            seekBarChange(8000)
            isPaused = false
            binding!!.includeSetting.btnPlay.visibility = GONE
            binding!!.btnSettings.visibility = GONE
            binding!!.btnSelectPdf.visibility = GONE
            binding!!.includeSetting.playSetting.visibility = VISIBLE
            binding!!.includeSetting.btnPlayBlue.visibility = INVISIBLE
            binding!!.includeSetting.btnPause.visibility = VISIBLE
            splitTexts()
            currentPlayPosition = 0
        }

//        binding!!.includeSetting.btnDownloadTxt.setOnClickListener {
//            saveFile()
//        }
//
//        binding!!.includeSetting.btnDownloadAudio.setOnClickListener {
//            saveAudio()
//        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun observers(){
        viewModel.progressBar.observe(this) {
//            binding?.progressBar?.visibility = it
        }

        viewModel.success.observe(this) {

            retry = 0
            it?.output?.let {
                    it1 ->if (lines.isNotEmpty()) {
                playList.add(PlayListModel(lines[0], it1))
                lines.removeAt(0)
//                playByte(it1)
                if (!isPlaying || playerListening?.isPlaying == false) {
                    playOnlineAudio()
                }
            }

                if (lines.isNotEmpty()) {
                    reqWord()
                }
            }
        }

        viewModel.error.observe(this) {
            retry += 1
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            if (retry <4){
                if (lines.isNotEmpty())
                    reqWord()
            }
        }
    }

    private val tvWatcher = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        @SuppressLint("SetTextI18n")
        override fun afterTextChanged(p0: Editable?) {
            binding!!.tvLetterCount.text = "${p0.toString().length}/2500"
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
//            binding!!.pdfViewer.visibility = VISIBLE
//            binding!!.tlText.visibility = GONE
//            binding!!.tvLetterCount.visibility = GONE
//        }else{
//            binding!!.pdfViewer.visibility =GONE
//            binding!!.tlText.visibility = VISIBLE
//            binding!!.tvLetterCount.visibility = VISIBLE
//        }
    }

    private fun getTextsFromPdf(uri: Uri){
        var pdfTexts = ""
        val stringBuilder = StringBuilder()
        val pdfReader: PdfReader?
        try {
            inputStream = requireActivity().contentResolver.openInputStream(uri)!!
        }catch (e: IOException){
            toast("Something went wrong")
        }

        try {
            pdfReader = PdfReader(inputStream)
            if (pdfReader.cryptoMode != -1) {
                toast("Format not supported")
            }
            val pageCount = pdfReader.numberOfPages
            for (i in 0 until pageCount){
                pdfTexts += PdfTextExtractor.getTextFromPage(pdfReader,i+1).trim()+"\n"
            }
            stringBuilder.append(pdfTexts)
            pdfReader.close()
            binding!!.etText.setText(pdfTexts)
            binding!!.etText.movementMethod
        }catch (e:IOException){
        }catch (e: ExceptionConverter){
        }
    }

    @SuppressLint("SetTextI18n")
    private fun milliToTime(millis: Long){
        val milliToSecond = (millis/1000)
        val seconds = if (milliToSecond % 60 <= 9) "0${milliToSecond % 60}" else milliToSecond % 60
        val minute = if ((milliToSecond/60) %60 <= 9) "0${(milliToSecond/60) %60}" else (milliToSecond/60) %60
        val hrs = if ((milliToSecond.toString().toInt()/3600) <= 9) "0${milliToSecond.toString().toInt()/3600}" else (milliToSecond.toString().toInt()/3600)
        binding!!.includeSetting.tvTimer.text = "$hrs:$minute:$seconds"
    }

    private fun saveFile(){
        try {
            val exDir = Environment.getExternalStorageDirectory()
            val directory = File(exDir.absolutePath.toString() + "/TTS")
            directory.mkdirs()
            val  file = File(exDir,"data.txt")
            val fileOutputStream = FileOutputStream(file)
            val outputStreamWriter = OutputStreamWriter(fileOutputStream)
            outputStreamWriter.write(binding!!.etText.text.toString())
            outputStreamWriter.flush()
            outputStreamWriter.close()
            Toast.makeText(requireContext(), "saved", Toast.LENGTH_LONG).show()
        } catch (e: IOException) {
            Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG)
                .show()
        }
    }

    @SuppressLint("NewApi")
    private fun saveAudio(){
        viewLifecycleOwner.lifecycleScope.launch {
            val splitText = binding!!.etText.text?.trim().toString().chunked(700)//.split(delimiter).toTypedArray()
            for (i in splitText.indices){
                lines.add(splitText[i])
            }
            viewModel.getAudio(ReqModel(lines[0]))
        }
    }

    private fun splitTexts(){
        val delimiter = " "
        viewLifecycleOwner.lifecycleScope.launch {
            val splitText = binding!!.etText.text?.trim().toString().split(delimiter).toTypedArray()
            for (i in splitText.indices){
                    lines.add(splitText[i])
            }
            reqWord()
        }
    }

    @SuppressLint("NewApi")
    private fun playOnlineAudio() {
        if (playList.isEmpty() || playerListening?.isPlaying == true || isPlaying) {
            return
        }
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                playerListening?.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                if (playList.isEmpty())
                    return@launch
                playerListening?.setDataSource("data:audio/wav;base64,${playList[0].url}")
                playerListening?.prepareAsync()
                playerListening?.setOnPreparedListener {
                    if (isPaused || isPlaying)
                        return@setOnPreparedListener
                    highlightText(playList[0].word)
//                    seekBarChange(playerListening?.duration!!.toLong())
                    playerListening?.start()
                    timeCount(1)
                    isPlaying = true
                }
                playerListening?.setOnErrorListener { mediaPlayer, i, i2 ->
                    playOnlineAudio()
                    true
                }
                playerListening?.setOnCompletionListener {
                    playerListening?.stop()
                    playerListening?.reset()
                    if (playList.isNotEmpty()) {
                        playList.removeAt(0)
                        if (lines.isNotEmpty() || playList.isNotEmpty()) {
                            isPlaying = false
                            playOnlineAudio()
                            return@setOnCompletionListener
                        }
//                        binding!!.includeSetting.playSetting.visibility = GONE
//                        binding!!.includeSetting.btnPlay.visibility = VISIBLE
                        binding!!.includeSetting.btnDownloadEnabled.visibility = VISIBLE
                        binding!!.includeSetting.btnDownloadDisabled.visibility = INVISIBLE
                        startingPoint = 0
                    }
                    isPlaying = false
                }
            } catch (e: IOException) {
                Dispatchers.Main(){
                    e.message?.let { toast(it) }
                }
            } catch ( e: IllegalArgumentException) {
                Dispatchers.Main(){
                    e.message?.let { toast(it) }
                }
            } catch (e: IllegalStateException) {
                Dispatchers.Main(){
                    e.message?.let { toast(it) }
                }
            }
        }
    }

    private fun highlightText(text: String){
        val endChar = startingPoint+text.length
        val spannable = SpannableString(binding!!.etText.text.toString())
        val colorW = BackgroundColorSpan(Color.WHITE)
        spannable.setSpan(colorW,0,binding!!.etText.text?.length!!,Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        val color = BackgroundColorSpan(Color.YELLOW)
        if (endChar > binding!!.etText.text!!.length)
            spannable.setSpan(color,startingPoint,binding!!.etText.text!!.length,Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        else
            spannable.setSpan(color,startingPoint,endChar,Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        binding!!.etText.setText(spannable)
        if (playList[playList.size-1].word != text) {
            startingPoint += text.length + 1
        }
    }

    private fun byteArrayToNumber(bytes: ByteArray?, numOfBytes: Int, type: Int): ByteBuffer {
        val buffer: ByteBuffer = ByteBuffer.allocate(numOfBytes)
        if (type == 0) {
            buffer.order(BIG_ENDIAN)
        } else {
            buffer.order(LITTLE_ENDIAN)
        }
        buffer.put(bytes)
        buffer.rewind()
        return buffer
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun playByte(data:String?=null){

        val clipData =android.util.Base64.decode(data,0)
        subChunk2Size += byteArrayToNumber(byteArrayOf(clipData[40],clipData[41],clipData[42],clipData[43]),4,1).int
        chunkSize += byteArrayToNumber(byteArrayOf(clipData[4],clipData[5],clipData[6],clipData[7]),4,1).int
        allByteArray.add(clipData)
        totalDataSize += clipData.size

        if (lines.isNotEmpty())
            return
        val mediaPlayer = MediaPlayer()
        mediaPlayer.setAudioAttributes(AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).setUsage(AudioAttributes.USAGE_MEDIA).build())

        val soundFile = File(Environment.getExternalStorageDirectory().absolutePath + "/AudioRecording")
        soundFile.mkdirs()
        val file= File(soundFile.path, "/audioRecording1.wav")//File(soundFile, "audioFile.mp3")
        try {
            val output = FileOutputStream(file,true)
            for (i in 0 until allByteArray.size){
                if (i>0) {
                    val bytes = allByteArray[i]//allByteArray[i].copyOfRange(44, allByteArray[i].size)
                    output.write(bytes)
                }else{
                    allByteArray[i][40] = numberToByteArray(subChunk2Size)[0]
                    allByteArray[i][41] = numberToByteArray(subChunk2Size)[1]
                    allByteArray[i][42] = numberToByteArray(subChunk2Size)[2]
                    allByteArray[i][43] = numberToByteArray(subChunk2Size)[3]
                    output.write(allByteArray[i])
                }
            }
            output.close()
        }catch (e:Exception){
            toast(e.message!!)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun timeCount(times: Int?){
        timeCount += times!!
        val second = if (timeCount % 60 <= 9) "0${timeCount % 60}" else timeCount % 60
        val minutes = timeCount/60 % 60
        val hours = minutes/60 % 24
        binding?.includeSetting?.tvTimer?.text = "$hours:$minutes:${timeCount%60}"
    }

    private fun reqWord(){
        if (lines[0].contains(regex))
            viewModel.getAudio(ReqModel(lines[0].replace(regex,"")))
        else{
            viewModel.getAudio(ReqModel(lines[0]))
        }
    }

}