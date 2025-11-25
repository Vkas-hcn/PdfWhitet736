package com.urgolle.pdfslam.alip

import com.urgolle.pdfslam.tihtg.PdfBean

interface DialogInterAc {

    fun renameAction(oldFilePath: String, newBean: PdfBean)

    fun deleteAction(oldFilePath: String)

}