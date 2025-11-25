package com.urgolle.pdfslam.tihtg


data class PdfBean(
    val contentUri: String = "",//content://xxx
    val filePath: String = "",
    val name: String,
    val size: String,
    val time: Long
)