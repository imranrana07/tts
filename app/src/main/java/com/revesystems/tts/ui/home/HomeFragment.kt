package com.revesystems.tts.ui.home

import android.Manifest
import android.Manifest.permission.*
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.BackgroundColorSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import com.itextpdf.text.ExceptionConverter
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.parser.PdfTextExtractor
import com.revesystems.tts.R
import com.revesystems.tts.core.BaseFragment
import com.revesystems.tts.data.model.PlayListModel
import com.revesystems.tts.data.model.ReqModel
import com.revesystems.tts.databinding.FragmentHomeBinding
import com.revesystems.tts.ui.SettingsBottomSheetFragment
import com.revesystems.tts.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.invoke
import kotlinx.coroutines.launch
import java.io.*

val PERMISSIONS = arrayOf(WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE)
class HomeFragment : BaseFragment<FragmentHomeBinding,HomeViewModel>() {
    //initialization
    private lateinit var inputStream: InputStream
    private var playerListening: MediaPlayer?=null
    private var lines = ArrayList<String>()
    private var downloadTextList = ArrayList<String>()
    private var playList = ArrayList<PlayListModel>()
    private var isPlaying = false
    private var isPaused = false
    private var startingPoint = 0
    private var currentPlayPosition = 0
    private var retry = 0
    private var retryDownload = 0
    private var timeCount = 0
    private var allByteArray = ArrayList<ByteArray>()
    private var chunkSize = 0
    private var totalDataSize = 0
    private var subChunk2Size = 0
    private val regex = Regex("[।,.|!]")
    private var currentTime:Long? = null
    private var isFromSpannableString = false
    private var lastWord:String? = null


    // launcher for below 11
    private val permReqLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        val granted = permissions.entries.all {
            it.value
        }
        if (granted) {
            startDownload()
        }else{
            toast(resources.getString(R.string.permission_required))
        }
    }

    //result launcher to get pdf file
    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if (it.resultCode == RESULT_OK && it != null) {
            getTextsFromPdf(it.data?.data!!)
            if (playerListening!= null)
                playerListening?.reset()
            lines.clear()
            playList.clear()
            binding?.includeSetting?.btnPlay?.visibility = VISIBLE
        }
    }

    // launcher for storage permission android 11
    private val permReqLauncherStorage11 = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if (it.resultCode == RESULT_OK && it != null) {

        }
    }

    override fun setLayout(inflater: LayoutInflater, container: ViewGroup?): FragmentHomeBinding = FragmentHomeBinding.inflate(layoutInflater)

    override fun setViewModel(): Class<HomeViewModel>  = HomeViewModel::class.java

    @SuppressLint("SetTextI18n")
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
            if (playerListening!= null){
                isPaused = false
                if (playList.isEmpty()){
                    binding!!.includeSetting.btnDownloadDisabled.visibility = VISIBLE
                    binding!!.includeSetting.btnDownloadEnabled.visibility = INVISIBLE
                    binding!!.includeSetting.tvTimer.text = resources.getText(R.string._00_00_00)
                    timeCount = 0
                    splitTexts()
                }

                playerListening?.seekTo(currentPlayPosition)
                playerListening?.start()
                binding!!.includeSetting.btnPlayBlue.visibility = INVISIBLE
                binding!!.includeSetting.btnPause.visibility = VISIBLE
                currentPlayPosition = 0
            }
        }
        binding!!.includeSetting.btnPause.setOnClickListener {
            if (playerListening!=null && playerListening?.isPlaying == true){
                isPaused = true
                playerListening?.pause()
                currentPlayPosition = playerListening?.currentPosition!!
                binding!!.includeSetting.btnPause.visibility = INVISIBLE
                binding!!.includeSetting.btnPlayBlue.visibility = VISIBLE
            }
        }

        binding!!.includeSetting.btnPlay.setOnClickListener {
            if (binding!!.etText.text.toString().isEmpty()) {
                return@setOnClickListener
            }
            lines.clear()
            playList.clear()
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

        binding!!.includeSetting.btnDownloadEnabled.setOnClickListener {
            permissionChecker()
//            startDownload()
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
            }else{
                retry = 0
                binding!!.includeSetting.btnPlayBlue.visibility = VISIBLE
                binding!!.includeSetting.btnPause.visibility = INVISIBLE
            }
        }

        viewModel.successDownloadAudio.observe(this){
            retryDownload = 0
            it?.output?.let{ output ->
                saveByteToWav(output)
            }
            downloadTextList.removeAt(0)
            if (downloadTextList.isNotEmpty())
                viewModel.downloadAudio(ReqModel(downloadTextList[0]))
        }

        viewModel.errorDownloadAudio.observe(this) {
            retryDownload += 1
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            if (retryDownload <4){
                if (downloadTextList.isNotEmpty())
                    viewModel.downloadAudio(ReqModel(downloadTextList[0]))
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
            if (p0.toString().isEmpty()){
                binding!!.includeSetting.btnPlay.visibility = GONE
            }else if (!isFromSpannableString){
                binding!!.includeSetting.btnPlay.visibility = VISIBLE
                isFromSpannableString = false
            }
        }
    }

    private fun selectPdf(){
        val pdfIntent = Intent(Intent.ACTION_GET_CONTENT)
        pdfIntent.type = "application/pdf"
        pdfIntent.addCategory(Intent.CATEGORY_OPENABLE)
        resultLauncher.launch(pdfIntent)
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

    private fun saveFile(fileName:String){
        try {
            val directory = File(Environment.getExternalStorageDirectory().absolutePath + "/TTS")
            directory.mkdirs()
            val  file = File(directory,"ttsText${fileName}.txt")
            val fileOutputStream = FileOutputStream(file)
            val outputStreamWriter = OutputStreamWriter(fileOutputStream)
            outputStreamWriter.write(binding!!.etText.text.toString())
            outputStreamWriter.flush()
            outputStreamWriter.close()
//            Toast.makeText(requireContext(), "saved", Toast.LENGTH_LONG).show()
        } catch (e: IOException) {
            Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG)
                .show()
        }
    }

    @SuppressLint("NewApi")
    private fun downloadAudio(){
        viewLifecycleOwner.lifecycleScope.launch {
            val splitText = binding!!.etText.text?.trim().toString().chunked(700)
            for (i in splitText.indices){
                downloadTextList.add(splitText[i])
            }
            viewModel.downloadAudio(ReqModel(downloadTextList[0]))
            saveFile("$currentTime")
        }
    }

    private fun splitTexts(){
        val delimiter = " "
        viewLifecycleOwner.lifecycleScope.launch {
            val splitText = binding!!.etText.text?.trim().toString().split(delimiter).toTypedArray()
            for (i in splitText.indices){
                    lines.add(splitText[i])
            }
            lastWord = lines[lines.size-1]
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
                playerListening?.setOnErrorListener { _, _, _ ->
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
                        binding!!.includeSetting.btnDownloadEnabled.visibility = VISIBLE
                        binding!!.includeSetting.btnDownloadDisabled.visibility = INVISIBLE
                        binding!!.includeSetting.btnPause.visibility = INVISIBLE
                        binding!!.includeSetting.btnPlayBlue.visibility = VISIBLE
                        startingPoint = 0
                    }
                    isPlaying = false
                }
            } catch (e: IOException) {
                Dispatchers.Main{
                    e.message?.let { toast(it) }
                }
            } catch ( e: IllegalArgumentException) {
                Dispatchers.Main{
                    e.message?.let { toast(it) }
                }
            } catch (e: IllegalStateException) {
                Dispatchers.Main{
                    e.message?.let { toast(it) }
                }
            }
        }
    }

    private fun highlightText(text: String){
        isFromSpannableString = true

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
        if (lastWord!=null && lastWord != text) {
            startingPoint += text.length + 1
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun saveByteToWav(data:String?=null){
        val clipData =android.util.Base64.decode(data,0)
        subChunk2Size += byteArrayToNumber(byteArrayOf(clipData[40],clipData[41],clipData[42],clipData[43]),4,1).int
        chunkSize += byteArrayToNumber(byteArrayOf(clipData[4],clipData[5],clipData[6],clipData[7]),4,1).int
        allByteArray.add(clipData)
        totalDataSize += clipData.size

        if (lines.isNotEmpty())
            return

        val soundFile = File(Environment.getExternalStorageDirectory().absolutePath + "/TTS")
        soundFile.mkdirs()
        val file= File(soundFile.path, "/ttsAudio${currentTime}.wav")//File(soundFile, "audioFile.mp3")
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
            allByteArray.clear()
        }catch (e:Exception){
            toast(e.message!!)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun timeCount(times: Int?){
        timeCount += times!!
        val second = if (timeCount % 60 <= 9) "0${timeCount % 60}" else timeCount % 60
        val minutes = if (timeCount/60 % 60 <=9) "0${timeCount/60 % 60}" else timeCount % 60
        val hours = if (timeCount/60/60 % 24 <= 9) "0${timeCount/60/60 % 24}" else timeCount/60/60 % 24
        binding?.includeSetting?.tvTimer?.text = "$hours:$minutes:$second"
    }

    private fun reqWord(){
        if (lines[0].contains(regex))
            viewModel.getAudio(ReqModel(lines[0].replace(regex,"")))
        else{
            viewModel.getAudio(ReqModel(lines[0]))
        }
    }

    private fun permissionChecker(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            if (!hasPermission11()){
                toast(resources.getString(R.string.permission_required))
                permReqLauncherStorage11.launch(requireContext().requestStoragePermission11())
                return
            }else{
                startDownload()
            }
        }else {
            if (hasPermissions10(requireContext(), PERMISSIONS)){
                startDownload()
            }else{
//                toast(resources.getString(R.string.permission_required))
                permReqLauncher.launch(PERMISSIONS)
                return
            }
        }
    }

    private fun startDownload(){
        currentTime = System.currentTimeMillis()
        downloadAudio()
    }
}