package com.urgolle.pdfslam.mayol

import android.content.Context
import android.content.Intent
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.rajat.pdfviewer.PdfRendererView
import com.urgolle.pdfslam.R
import com.urgolle.pdfslam.databinding.ActivityMayolJpBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import androidx.core.net.toUri
import androidx.lifecycle.LifecycleCoroutineScope
import com.rajat.pdfviewer.PdfViewerActivity.Companion.isZoomEnabled
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MayolJp : AppCompatActivity() {

    private val binding by lazy {
        ActivityMayolJpBinding.inflate(layoutInflater)
    }
    private lateinit var pdfAdapter: PdfPageAdapter
    private var pdfRenderer: PdfRenderer? = null
    private var parcelFileDescriptor: ParcelFileDescriptor? = null
    private var currentPage = 0
    private var isFromUserClick = false // 新增标志位

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val contentUri = intent.extras?.getString("contentUri", "") ?: ""
        val filePath = intent.extras?.getString("filePath", "") ?: ""

        binding.pdfView.statusListener = object : PdfRendererView.StatusCallBack {
            override fun onPageChanged(currentPage: Int, totalPage: Int) {
                super.onPageChanged(currentPage, totalPage)
                this@MayolJp.currentPage = currentPage
            }
        }

        binding.pdfView.setZoomEnabled(isZoomEnabled)

        lifecycleScope.launch {
            when {
                contentUri.isNotEmpty() -> {
                    val file = uriToFile(this@MayolJp, contentUri.toUri())
                    binding.pdfView.initWithFile(file)
                }

                filePath.isNotEmpty() -> {
                    binding.pdfView.initWithFile(File(filePath))
                    loadPdfFile(filePath)
                }
            }
        }
    }

    suspend fun uriToFile(context: Context, uri: Uri): File = withContext(Dispatchers.IO) {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IOException("Failed to open URI: $uri")
        val tempFile = File.createTempFile("pdf_temp", ".pdf", context.cacheDir)
        inputStream.use { it.copyTo(tempFile.outputStream()) }
        tempFile
    }

    private fun loadPdfFile(filePath: String) {
        try {
            val file = File(filePath)
            parcelFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            pdfRenderer = parcelFileDescriptor?.let { PdfRenderer(it) }

            pdfRenderer?.let { renderer ->
                parcelFileDescriptor?.let { pfd ->
                    pdfAdapter = PdfPageAdapter(renderer, pfd).apply {
                        // 设置点击监听器
                        setOnItemClickListener { _, _, position ->
                            navigateToDetail(position)
                        }
                    }
                    binding.rv.adapter = pdfAdapter
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "PDF load failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToDetail(pageIndex: Int) {
        isFromUserClick = true // 标记为用户点击
        pdfAdapter.updateSelectedPage(pageIndex) // 直接更新 Adapter 选中状态
        binding.pdfView.jumpToPage(pageIndex, false)
    }

    override fun onDestroy() {
        super.onDestroy()
        pdfAdapter.release()
    }
}