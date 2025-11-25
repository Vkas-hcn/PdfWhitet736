package com.urgolle.pdfslam.alip

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.urgolle.pdfslam.databinding.DialogRenameBinding
import com.urgolle.pdfslam.tihtg.PdfBean
import java.io.File

class RenameDialog(
    val mContext: Context,
    val pdfBean: PdfBean,
    val dialogInterAc: DialogInterAc
) : BottomSheetDialog(mContext) {

    private val file = File(pdfBean.filePath)
    private var binding: DialogRenameBinding =
        DialogRenameBinding.inflate(LayoutInflater.from(context))

    init {
        setContentView(binding.root)
        setupViews()
    }


    @SuppressLint("SetTextI18n")
    private fun setupViews() {
        setCancelable(true)
        window?.setBackgroundDrawableResource(android.R.color.transparent)

        binding.edit.setText(file.name)

        binding.tvCancel.setOnClickListener {
            dismiss()
        }
        binding.tvConfirm.setOnClickListener {
            val newName = binding.edit.text
            if (newName.isNullOrEmpty()) {
                Toast.makeText(mContext, "new name is Empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (newName.toString() == pdfBean.name) {
                Toast.makeText(mContext, "same name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            dismiss()

            runCatching {
                val dest = File(file.parentFile, newName.toString())
                val oldPath = file.absolutePath
                if (file.renameTo(dest)) {
                    dialogInterAc.renameAction(
                        oldPath,
                        pdfBean.copy(
                            filePath = dest.absolutePath,
                            name = dest.name,
                            time = dest.lastModified()
                        )
                    )
                }
            }
        }
    }
}