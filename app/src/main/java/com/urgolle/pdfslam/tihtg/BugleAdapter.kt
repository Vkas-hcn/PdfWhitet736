package com.urgolle.pdfslam.tihtg

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter4.BaseQuickAdapter
import com.urgolle.pdfslam.R
import com.urgolle.pdfslam.databinding.ItemPdfBinding
import java.io.IOException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.min
import androidx.core.graphics.createBitmap
import com.urgolle.pdfslam.alip.DialogInterAc
import com.urgolle.pdfslam.alip.MoreDialog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BugleAdapter(val dialogInterAc: DialogInterAc) : BaseQuickAdapter<PdfBean, BugleAdapter.VH>() {

    // 线程池用于异步加载
    private val executor: ExecutorService = Executors.newFixedThreadPool(4)

    // 缓存已加载的缩略图
    private val thumbnailCache = mutableMapOf<String, Bitmap>()


    override fun onCreateViewHolder(
        context: Context,
        parent: ViewGroup,
        viewType: Int
    ): VH {
        return VH(
            ItemPdfBinding.bind(
                LayoutInflater.from(context).inflate(R.layout.item_pdf, parent, false)
            )
        )
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: VH, position: Int, item: PdfBean?) {
        item ?: return

        holder.binding.tvName.text = item.name
        holder.binding.tvSize.text = item.filePath + " " + item.size + " " + toTime(item.time)
        holder.binding.root.setOnClickListener {
            clickAction.invoke(item)
        }
        val context = holder.binding.root.context
        holder.binding.ivMore.setOnClickListener {
            val bitmap = thumbnailCache[item.filePath]
            MoreDialog(context, bitmap, item, dialogInterAc).show()
        }

        // 异步加载缩略图
        loadPdfThumbnailAsync(item.filePath, holder.binding.iv)
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        // 清理可能的内存引用
        if (holder is VH) {
            holder.binding.iv.setImageBitmap(null)
        }
    }

    fun toTime(timestamp: Long): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(timestamp))
    }

    var clickAction: (item: PdfBean) -> Unit = {}

    inner class VH(val binding: ItemPdfBinding) : RecyclerView.ViewHolder(binding.root)

    /**
     * 异步加载PDF缩略图
     */
    private fun loadPdfThumbnailAsync(filePath: String, imageView: ImageView) {
        // 检查缓存
        thumbnailCache[filePath]?.let { cachedBitmap ->
            imageView.setImageBitmap(cachedBitmap)
            return
        }

        // 设置tag用于防止错乱
        imageView.tag = filePath

        executor.execute {
            try {
                val bitmap = renderPdfThumbnail(filePath, imageView.width)
                // 回到主线程更新UI
                imageView.post {
                    // 检查view是否仍然显示同一个文件（防止快速滑动导致的错乱）
                    if (imageView.tag == filePath) {
                        // 缓存结果
                        bitmap?.let {
                            imageView.setImageBitmap(it)
                            thumbnailCache[filePath] = it
                        }
                    } else {
                        // 如果不是同一个文件，回收bitmap
                        bitmap?.recycle()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // 可以在这里设置错误图片
                imageView.post {
                    if (imageView.tag == filePath) {
                        imageView.setImageResource(R.mipmap.ic_launcher)
                    }
                }
            }
        }
    }

    /**
     * 渲染PDF缩略图（在后台线程执行）- 使用文件路径加载
     */
    private fun renderPdfThumbnail(filePath: String, targetWidth: Int): Bitmap? {
        var fileDescriptor: ParcelFileDescriptor? = null
        var pdfRenderer: PdfRenderer? = null
        var page: PdfRenderer.Page? = null

        try {
            // 直接使用文件路径打开文件描述符
            val file = java.io.File(filePath)
            fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            fileDescriptor ?: return null

            pdfRenderer = PdfRenderer(fileDescriptor)

            // 检查是否有页面
            if (pdfRenderer.pageCount == 0) return null

            page = pdfRenderer.openPage(0)

            // 获取页面原始尺寸
            val pageWidth = page.width
            val pageHeight = page.height

            // 计算缩放比例
            val scale = if (targetWidth > 0) {
                min(targetWidth.toFloat() / pageWidth, 1.0f) // 不超过原始尺寸
            } else {
                0.2f // 默认缩放
            }

            // 计算目标尺寸
            val bitmapWidth = (pageWidth * scale).toInt().coerceAtLeast(1)
            val bitmapHeight = (pageHeight * scale).toInt().coerceAtLeast(1)

            // 使用ARGB_8888保持图片质量
            val bitmap = createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888)

            // 渲染到Bitmap
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

            return bitmap

        } catch (e: IOException) {
            e.printStackTrace()
            return null
        } finally {
            // 确保资源释放
            page?.close()
            pdfRenderer?.close()
            try {
                fileDescriptor?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 清理缓存，防止内存泄漏
     */
    fun clearCache() {
        thumbnailCache.values.forEach { bitmap ->
            if (!bitmap.isRecycled) {
                bitmap.recycle()
            }
        }
        thumbnailCache.clear()
    }

    /**
     * 释放资源
     */
    fun release() {
        clearCache()
        executor.shutdown()
    }
}