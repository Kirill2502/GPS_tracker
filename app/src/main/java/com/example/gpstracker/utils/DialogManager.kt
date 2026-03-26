package com.example.gpstracker.utils

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import com.example.gpstracker.R
import com.example.gpstracker.data.TrackItemEntity
import com.example.gpstracker.databinding.DeleteDialogBinding
import com.example.gpstracker.databinding.SaveDialogBinding


object DialogManager {
     fun showLocEnableDialog(context: Context,listener: Listener) {
        val builder = AlertDialog.Builder(context)
         val dialog = builder.create()

        builder.setTitle(R.string.location_disabled)
        builder.setMessage(R.string.location_dialog_message)
        builder.setPositiveButton(R.string.open_settings) { _, _ ->
            listener.onClick()
            dialog.dismiss()
        }
        builder.setNegativeButton(R.string.cancel,null)
         dialog.dismiss()
        builder.show()
    }
    @SuppressLint("UseKtx")
    fun showDeleteTrackDialog(context: Context, listener: Listener){
        val builder = AlertDialog.Builder(context)
        val binding = DeleteDialogBinding.inflate(LayoutInflater.from(context),null,false)
        builder.setView(binding.root)
        val dialog = builder.create()
        binding.apply {
            bDel.setOnClickListener {
                listener.onClick()
                dialog.dismiss()
            }
            bCancle.setOnClickListener {
                dialog.dismiss()
            }
        }
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }
    @SuppressLint("UseKtx", "SetTextI18n")
    fun showSaveDialog(context: Context, item: TrackItemEntity?, listener: Listener){
        val builder = AlertDialog.Builder(context)
        val binding = SaveDialogBinding.inflate(LayoutInflater.from(context),null,false)
        builder.setView(binding.root)
        val dialog = builder.create()
        binding.apply {
            tvTimeD.text = " ${item?.time}"
            tvSpeed.text = "Скорость: ${item?.velocity}км/ч"

            tvDistanceD.text = "Дистанция: ${item?.distance}км"
            bSave.setOnClickListener {
                listener.onClick()
                dialog.dismiss()
            }
            bCancle.setOnClickListener {
                dialog.dismiss()
            }
        }
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))//Сделали фон стандартного
        // диалога прозрачным что бы отображались скругления нашего cardView
        dialog.show()

    }



    interface Listener{
        fun onClick()
    }
}