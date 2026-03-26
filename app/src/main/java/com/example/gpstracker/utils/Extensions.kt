package com.example.gpstracker.utils

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.gpstracker.R
import com.example.gpstracker.fragments.TracksFragment

@SuppressLint("CommitTransaction")
fun Fragment.openFragment(f: Fragment){
    (activity as AppCompatActivity).supportFragmentManager
        .beginTransaction()
        .setCustomAnimations(android.R.anim.fade_in,android.R.anim.fade_out)
        .replace(R.id.plaseHolder,f).commit()
}
fun AppCompatActivity.openFragment(f: Fragment){
    if (supportFragmentManager.fragments.isNotEmpty()){
        if (supportFragmentManager.fragments[0].javaClass == f.javaClass){
            return
        }
    }
    supportFragmentManager
        .beginTransaction()
        .setCustomAnimations(android.R.anim.fade_in,android.R.anim.fade_out)
        .replace(R.id.plaseHolder,f).commit()
}
fun Fragment.showToast(s:String){
    Toast.makeText(activity, s, Toast.LENGTH_SHORT).show()
}
fun Fragment.checkPermission(p: String): Boolean{
    return when(PackageManager.PERMISSION_GRANTED){
        ContextCompat.checkSelfPermission(activity as AppCompatActivity,p)->true
        else -> false
    }
}
