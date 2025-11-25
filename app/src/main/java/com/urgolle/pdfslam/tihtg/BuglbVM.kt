package com.urgolle.pdfslam.tihtg

import android.content.ContentUris
import android.content.Context
import android.os.Environment
import android.os.Parcelable
import android.provider.MediaStore
import androidx.compose.runtime.mutableIntStateOf
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.ViewModel
import com.urgolle.pdfslam.alip.DialogInterAc
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.max
import java.io.File
import java.util.Locale
import java.util.Locale.getDefault

class BuglbVM : ViewModel(), DialogInterAc {

    private val _pdfList = MutableStateFlow<List<PdfBean>>(emptyList())

    // 公开的只读StateFlow，UI层可以观察但不能修改
    val pdfList: StateFlow<List<PdfBean>> = _pdfList.asStateFlow()

    // 保存 RecyclerView 的布局状态
    var recyclerViewState: Parcelable? = null
    private val _selectedTabIndex = MutableStateFlow(0)
    val selectedTabIndex: StateFlow<Int> = _selectedTabIndex.asStateFlow()

    fun selectTab(index: Int) {
        _selectedTabIndex.value = index
    }


    /**
     * 专门扫描主外部存储目录(Environment.getExternalStorageDirectory())下的PDF文件
     */
    fun scanAllPdfs(
        lifecycleCoroutineScope: LifecycleCoroutineScope,
        onComplete: () -> Unit
    ) {
        lifecycleCoroutineScope.launch(Dispatchers.IO) {

            // 获取主外部存储目录
            val primaryStorage = Environment.getExternalStorageDirectory()
            // 扫描主外部存储目录下的PDF文件
            val primaryStoragePdfs = scanPdfsFromDirectory(primaryStorage)

            _pdfList.value = primaryStoragePdfs

            withContext(Dispatchers.Main) {
                onComplete()
            }
        }
    }

    /**
     * 扫描指定目录及其子目录下的所有PDF文件
     */
    private fun scanPdfsFromDirectory(
        directory: File,
    ): List<PdfBean> {
        val pdfFiles = mutableListOf<PdfBean>()

        if (directory.exists() && directory.isDirectory) {
            findPdfFilesInDirectory(directory, pdfFiles)
        }

        return pdfFiles
    }

    /**
     * 在指定目录中递归查找PDF文件
     */
    private fun findPdfFilesInDirectory(
        directory: File,
        pdfFiles: MutableList<PdfBean>,
    ) {
        try {
            val files = directory.listFiles() ?: return


            for (file in files) {
                // 跳过隐藏文件和系统目录
                if (file.name.startsWith(".")) continue

                if (file.isDirectory) {
                    // 递归遍历子目录（跳过一些已知的非文档目录以提高效率）
                    findPdfFilesInDirectory(file, pdfFiles)
                } else {
                    // 检查是否是PDF文件
                    if (isPdfFile(file)) {
                        val pdfBean = createPdfBeanFromFile(file)
                        pdfFiles.add(pdfBean)
                    }
                }
            }
        } catch (e: SecurityException) {
            // 无权限访问该目录，跳过
        } catch (e: Exception) {
        }
    }

    /**
     * 从File对象创建PdfBean
     */
    private fun createPdfBeanFromFile(file: File): PdfBean {
        return PdfBean(
            filePath = file.absolutePath,
            name = file.name,
            size = formatFileSize(file.length()),
            time = file.lastModified()  // 转换为秒，与MediaStore保持一致
        )
    }


    /**
     * 检查文件是否是PDF
     */
    private fun isPdfFile(file: File): Boolean {
        if (!file.isFile) return false
        if (!file.canRead()) return false

        val name = file.name.lowercase(getDefault())
        return name.endsWith(".pdf") && file.length() > 0
    }

    /**
     * 获取主外部存储的常用文档子目录进行重点扫描
     */
    fun scanCommonDocumentDirs(
        lifecycleCoroutineScope: LifecycleCoroutineScope,
        onComplete: (List<PdfBean>) -> Unit
    ) {
        lifecycleCoroutineScope.launch(Dispatchers.IO) {
            val pdfFiles = mutableListOf<PdfBean>()
            val primaryStorage = Environment.getExternalStorageDirectory()

            val dirPdfs = scanPdfsFromDirectory(primaryStorage)
            pdfFiles.addAll(dirPdfs)

            withContext(Dispatchers.Main) {
                onComplete(pdfFiles)
            }
        }
    }


    // 文件大小格式化工具函数
    private fun formatFileSize(size: Long): String {
        return when {
            size < 1024 -> "$size B"
            size < 1024 * 1024 -> "${size / 1024} KB"
            else -> "${size / (1024 * 1024)} MB"
        }
    }

    fun updatePdfItem(oldFilePath: String, updatedItem: PdfBean) {
        val currentList = _pdfList.value.toMutableList()
        val index = currentList.indexOfFirst { it.filePath == oldFilePath }
        if (index != -1) {
            currentList[index] = updatedItem
            _pdfList.value = currentList
        }
    }


    override fun renameAction(oldFilePath: String, newBean: PdfBean) {
        updatePdfItem(oldFilePath, newBean)
    }

    override fun deleteAction(oldFilePath: String) {
        val currentList = _pdfList.value.toMutableList()
        val index = currentList.indexOfFirst { it.filePath == oldFilePath }
        if (index != -1) {
            currentList.removeAt(index)
            _pdfList.value = currentList
        }
    }
}