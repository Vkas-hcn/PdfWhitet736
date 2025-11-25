package com.urgolle.pdfslam.alip

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.urgolle.pdfslam.databinding.DialogDeleteBinding
import com.urgolle.pdfslam.tihtg.PdfBean
import java.io.File

class DeleteDialog(
    val mContext: Context,
    val pdfBean: PdfBean,
    val dialogInterAc: DialogInterAc
) : BottomSheetDialog(mContext) {

    private var binding: DialogDeleteBinding =
        DialogDeleteBinding.inflate(LayoutInflater.from(context))

    init {
        setContentView(binding.root)
        setupViews()
    }

    @SuppressLint("SetTextI18n")
    private fun setupViews() {
        setCancelable(true)
        window?.setBackgroundDrawableResource(android.R.color.transparent)

        binding.tvCancel.setOnClickListener {
            dismiss()
        }
        binding.tvConfirm.setOnClickListener {
            dismiss()
            val file = File(pdfBean.filePath)
            val oldPath = pdfBean.filePath
            runCatching {
                if (file.delete()) {
                    dialogInterAc.deleteAction(oldPath)
                }
            }
        }
    }
}