package com.revesystems.tts.utils

import android.content.Context
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment

fun Fragment.toast(message:String){
    Toast.makeText(context,message,Toast.LENGTH_SHORT).show()
}
fun Context.toast(message:String){
    Toast.makeText(applicationContext,message,Toast.LENGTH_SHORT).show()
}

const val VISIBLE = View.VISIBLE
const val INVISIBLE = View.INVISIBLE
const val GONE = View.GONE
