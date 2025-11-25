package com.urgolle.pdfslam.mayol

import android.content.Context
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter4.BaseQuickAdapter
import com.urgolle.pdfslam.R
import com.urgolle.pdfslam.databinding.ItemPdfPageBinding
import androidx.core.graphics.createBitmap

class PdfPageAdapter(
    private val pdfRenderer: PdfRenderer,
    private val parcelFileDescriptor: ParcelFileDescriptor
) : BaseQuickAdapter<Int, PdfPageAdapter.PgVH>() {

    private var selectedPage = 0

    init {
        // 初始化数据列表，每个页面索引作为一个数据项
        val pageList = (0 until pdfRenderer.pageCount).toList()
        submitList(pageList)
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): PgVH {
        val view = LayoutInflater.from(context).inflate(R.layout.item_pdf_page, parent, false)
        return PgVH(view)
    }

    override fun onBindViewHolder(holder: PgVH, position: Int, item: Int?) {
        item ?: return
        holder.bindPage(position)

        // 根据选中状态更新UI
        holder.binding.vSelect.visibility = if (position == selectedPage) {
            View.VISIBLE
        } else {
            View.INVISIBLE // 或者 View.GONE
        }
    }

    /**
     * 更新选中的页面
     */
    fun updateSelectedPage(pageIndex: Int) {
        selectedPage = pageIndex
        notifyDataSetChanged()
    }

    inner class PgVH(view: View) : RecyclerView.ViewHolder(view) {
        val binding = ItemPdfPageBinding.bind(view)

        fun bindPage(pageIndex: Int) {
            try {
                val page = pdfRenderer.openPage(pageIndex)

                // 计算合适的 Bitmap 尺寸
                val displayMetrics = binding.root.resources.displayMetrics
                val width = displayMetrics.widthPixels
                val height = (width * page.height / page.width)

                // 创建并渲染 Bitmap
                val bitmap = createBitmap(width, height)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

                // 设置到 ImageView
                binding.iv.setImageBitmap(bitmap)

                // 关闭页面
                page.close()

            } catch (e: Exception) {
                e.printStackTrace()
                // 可以设置一个错误占位图
                // binding.iv.setImageResource(R.drawable.ic_error_placeholder)
            }

            // 设置选中状态
            binding.vSelect.visibility = if (pageIndex == selectedPage) {
                View.VISIBLE
            } else {
                View.INVISIBLE // 或者 View.GONE
            }
        }
    }

    /**
     * 释放资源
     */
    fun release() {
        try {
            pdfRenderer.close()
            parcelFileDescriptor.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}