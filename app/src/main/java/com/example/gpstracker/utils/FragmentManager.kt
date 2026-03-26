package com.example.gpstracker.utils

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.gpstracker.R

object FragmentManager {//ОБЪЕКТ В КОТОРОМ ФУНКЦИЯ ДЛЯ ВЫЗОВОВ ФРАГМЕНТОВ
    var currentFragment: Fragment? = null
    @SuppressLint("CommitTransaction")
    fun setFragment(newFragment: Fragment, activ: AppCompatActivity) {
        val transaction = activ.supportFragmentManager.beginTransaction()
        transaction.replace(R.id.plaseHolder,newFragment)
        transaction.commit()
        currentFragment = newFragment
        //transaction.setCustomAnimations()

    }
}