package com.urgolle.pdfslam.alip

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.widget.Toast
import androidx.core.content.FileProvider
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.urgolle.pdfslam.databinding.DialogPdfMoreBinding
import com.urgolle.pdfslam.tihtg.PdfBean
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class MoreDialog(
    val mContext: Context,
    val bitmap: Bitmap?,
    val pdfBean: PdfBean,
    val dialogInterAc: DialogInterAc,
) : BottomSheetDialog(mContext) {

    private var binding: DialogPdfMoreBinding =
        DialogPdfMoreBinding.inflate(LayoutInflater.from(mContext))

    init {
        setContentView(binding.root)
        setupViews()
    }


    @SuppressLint("SetTextI18n")
    private fun setupViews() {
        setCancelable(true)
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        if (bitmap != null) {
            binding.iv.setImageBitmap(bitmap)
        }

        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.isFitToContents = true

        binding.tvName.text = pdfBean.name
        binding.tvInfo.text = pdfBean.filePath + " " + pdfBean.size + " " + toTime(pdfBean.time)

        binding.llRename.setOnClickListener {
            dismiss()
            RenameDialog(mContext, pdfBean, dialogInterAc).show()
        }
        binding.llShare.setOnClickListener {
            dismiss()
            sharePdfFile(File(pdfBean.filePath))
        }
        binding.llDelete.setOnClickListener {
            dismiss()
            DeleteDialog(mContext, pdfBean, dialogInterAc).show()
        }

    }

    fun toTime(timestamp: Long): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(timestamp))
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun sharePdfFile(pdfFile: File) {
        val contentUri =
            FileProvider.getUriForFile(mContext, mContext.packageName + ".fileprovider", pdfFile)

        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.setType("application/pdf")
        shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri)
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        // 创建分享选择器
        val chooser = Intent.createChooser(shareIntent, "Share pdf file")

        // 验证是否有应用可以处理该 Intent
        if (shareIntent.resolveActivity(mContext.packageManager) != null) {
            mContext.startActivity(chooser)
        } else {
            Toast.makeText(mContext, "no app can share it", Toast.LENGTH_SHORT).show()
        }
    }

}