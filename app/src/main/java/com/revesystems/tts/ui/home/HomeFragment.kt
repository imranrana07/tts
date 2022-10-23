package com.revesystems.tts.ui.home

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.media.AudioFormat
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
import androidx.lifecycle.lifecycleScope
import com.itextpdf.text.ExceptionConverter
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.parser.PdfTextExtractor
import com.revesystems.tts.core.BaseFragment
import com.revesystems.tts.data.model.PlayListModel
import com.revesystems.tts.data.model.ReqModel
import com.revesystems.tts.databinding.FragmentHomeBinding
import com.revesystems.tts.ui.SettingsBottomSheetFragment
import com.revesystems.tts.utils.GONE
import com.revesystems.tts.utils.INVISIBLE
import com.revesystems.tts.utils.VISIBLE
import com.revesystems.tts.utils.toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.*


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

//        playerListening = MediaPlayer()
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
            binding!!.includeSetting.playSetting.visibility = VISIBLE
            binding!!.includeSetting.btnPlayBlue.visibility = INVISIBLE
            binding!!.includeSetting.btnPause.visibility = VISIBLE
            splitTexts()
            currentPlayPosition = 0
        }

        binding!!.includeSetting.btnDownloadTxt.setOnClickListener {
            saveFile()
        }

        binding!!.includeSetting.btnDownloadAudio.setOnClickListener {
            saveAudio()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun observers(){
        viewModel.progressBar.observe(this) {
            binding?.progressBar?.visibility = it
        }

        viewModel.success.observe(this) {
            retry = 0

            it?.output?.let {
                    it1 ->if (lines.isNotEmpty()) {
                playList.add(PlayListModel(lines[0], it1))
                lines.removeAt(0)
                Log.v("lineslines "," lines ${lines.size}")
                if (!isPlaying || playerListening?.isPlaying == false) {
//                    playOnlineAudio()
                }
            }
                if (lines.isNotEmpty())
                    viewModel.getAudio(ReqModel(lines[0]))
                playByte(it.output)
            }
        }

        viewModel.error.observe(this) {
            retry += 1
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            if (retry <4){
                if (lines.isNotEmpty())
                    viewModel.getAudio(ReqModel(lines[0]))
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

    private fun seekBarChange(millis:Long){
        val totalMillis = if (millis>1000) millis else 1000
        binding!!.includeSetting.skPlayProgress.progress = 0
        binding!!.includeSetting.skPlayProgress.max =  (totalMillis/1000).toInt()

        object: CountDownTimer(totalMillis, 1000){
            override fun onTick(p0: Long) {
                milliToTime(p0)
                binding!!.includeSetting.skPlayProgress.progress = (totalMillis/1000).toInt() - (p0/1000).toInt()
            }
            override fun onFinish() {
            }
        }.start()

        binding!!.includeSetting.skPlayProgress.setOnSeekBarChangeListener(object :SeekBar.OnSeekBarChangeListener{
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

//        val decoded = java.util.Base64.getUrlDecoder().decode("data:audio/wav;base64,$url")
//        val downloadManager = requireContext().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager?
//        val stringUrl = String(decoded,StandardCharsets.UTF_8)
//        val uri = stringUrl.toUri()//Uri.parse("https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3")
//        val request = DownloadManager.Request(uri)
////        request.setVisibleInDownloadsUi(true)
//        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
//        request.setDestinationInExternalPublicDir(
//            Environment.DIRECTORY_DOWNLOADS,
//            uri.lastPathSegment
//        )
//        toast("download started")
//        downloadManager!!.enqueue(request)
    }

    private fun splitTexts(){
        val regex = Regex("[।,.|!]")
        val delimiter = " "
        viewLifecycleOwner.lifecycleScope.launch {
            val splitText = binding!!.etText.text?.trim().toString().chunked(800)//.split(delimiter).toTypedArray()
            for (i in splitText.indices){
//                if (splitText[i].contains(regex))
//                    lines.add(splitText[i].replace(regex,""))
//                else
                    lines.add(splitText[i])
            }
            viewModel.getAudio(ReqModel(/*binding!!.etText.text?.trim().toString()*/lines[0]))
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
//                    highlightText(playList[0].word)
//                    seekBarChange(playerListening?.duration!!.toLong())
                    playerListening?.start()
                    isPlaying = true
                }
                playerListening?.setOnErrorListener { mediaPlayer, i, i2 ->
//                    playOnlineAudio()
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
                        binding!!.includeSetting.playSetting.visibility = GONE
                        binding!!.includeSetting.btnPlay.visibility = VISIBLE
                        startingPoint = 0
                        /*if ( playerListening?.isPlaying == false && !isPlaying) {
                            playOnlineAudio()
                        }else{
                            if (lines.isNotEmpty()) {
                                isPlaying = false
                                return@setOnCompletionListener
                            }
                            binding!!.includeSetting.playSetting.visibility = GONE
                            binding!!.includeSetting.btnPlay.visibility = VISIBLE
                            startingPoint = 0
                        }*/
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
        val endChar = startingPoint+text.length
        val spannable = SpannableString(binding!!.etText.text)
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

    @RequiresApi(Build.VERSION_CODES.M)
    private fun playByte(data:String?=null){
        val clipData =android.util.Base64.decode(data,0)
        val mediaPlayer = MediaPlayer()
        mediaPlayer.setAudioAttributes(AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).setUsage(AudioAttributes.USAGE_MEDIA).build())

        val soundFile = File(Environment.getExternalStorageDirectory().absolutePath + "/AudioRecording")
        soundFile.mkdirs()
        val file= File(soundFile.path, "/audioRecording1.wav")//File(soundFile, "audioFile.mp3")
        try {
            val output = FileOutputStream(file,true)
//            requireContext().openFileOutput("audioRecording1.wav", Context.MODE_PRIVATE).use {
//                it.write(clipData)
//                it.close()
//            }
//            output.write()
            output.write(clipData)
            output.close()

//            mediaPlayer.setDataSource(file.path)
//            mediaPlayer.prepareAsync()
//            mediaPlayer.setOnPreparedListener {
//                mediaPlayer.start()
//            }
//            mediaPlayer.setOnErrorListener { mediaPlayer, i, i2 ->
//
//                Log.v("","${mediaPlayer.playbackParams}")
//                true
//            }
        }catch (e:Exception){
            toast(e.message!!)
        }
    }
}