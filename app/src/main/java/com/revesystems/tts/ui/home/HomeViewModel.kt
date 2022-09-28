package com.revesystems.tts.ui.home

import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.revesystems.tts.data.model.ReqModel
import com.revesystems.tts.data.model.ResModel
import com.revesystems.tts.data.source.remote.repo.HomeRepo
import com.revesystems.tts.utils.ApiException
import kotlinx.coroutines.launch

class HomeViewModel: ViewModel(){
    private val  synthesizerRepo = HomeRepo()
    val success = MutableLiveData<ResModel?>()
    val error = MutableLiveData<String>()
    val progressBar = MutableLiveData<Int>()

    fun getAudio(reqModel : ReqModel){
        progressBar.postValue(View.VISIBLE)
        viewModelScope.launch {
            try {
                val res = synthesizerRepo.getAudio(reqModel)
                success.postValue(res)
                progressBar.postValue(View.GONE)
            }catch (e: ApiException){
                error.postValue(e.localizedMessage)
                progressBar.postValue(View.GONE)
            }
        }
    }
}