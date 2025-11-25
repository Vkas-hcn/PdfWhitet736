package com.urgolle.pdfslam.tihtg

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.urgolle.pdfslam.R
import com.urgolle.pdfslam.databinding.FragmentRecentBinding
import com.urgolle.pdfslam.mayol.MayolJp
import kotlinx.coroutines.launch

class RecentFragment : Fragment() {

    private lateinit var binding: FragmentRecentBinding


    private val viewModel: BuglbVM by lazy {
        (requireActivity() as BuglbMu).viewModel
    }
    private lateinit var adapter: BugleAdapter

    private var tempList: List<PdfBean> = listOf()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_recent, container, false)
        binding = FragmentRecentBinding.bind(view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = BugleAdapter(viewModel)
        binding.rv.adapter = adapter


        adapter.clickAction = {
            startActivity(Intent(requireContext(), MayolJp::class.java).apply {
                putExtra("contentUri", it.contentUri)
                putExtra("filePath", it.filePath)
            })
        }


        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.pdfList.collect { list ->
                    val sortList = list.sortedBy { it.time }
                    tempList = sortList
                    adapter.submitList(sortList)
                    binding.llEmpty.isVisible = list.isEmpty()
                    // 恢复 RecyclerView 状态
                    viewModel.recyclerViewState?.let {
                        binding.rv.layoutManager?.onRestoreInstanceState(it)
                    }
                }
            }
        }

        binding.ivSet.setOnClickListener {
            val activity = (requireActivity() as BuglbMu)
            activity.viewModel.selectTab(2)
            activity.navNoPop(R.id.setFragment)
        }
        binding.editSearch.addTextChangedListener(object : TextWatcher {
            private var searchTask: Runnable? = null

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                // 移除之前的搜索任务

                // 创建新的搜索任务
                searchTask = Runnable {
                    val search = s.toString()
                    if (search.isEmpty()) {
                        adapter.submitList(tempList)
                        binding.llEmpty.isVisible = tempList.isEmpty()
                    } else {
                        val filterList = tempList.filter { it.name.contains(search) }
                        adapter.submitList(filterList)
                        binding.llEmpty.isVisible = filterList.isEmpty()
                    }

                }

                // 延迟执行搜索，避免频繁请求
                binding.editSearch.postDelayed(searchTask, 500) // 500ms 延迟
            }

        })
    }

    override fun onPause() {
        super.onPause()
        // 保存 RecyclerView 状态
        viewModel.recyclerViewState = binding.rv.layoutManager?.onSaveInstanceState()
    }


    fun subList(list: List<PdfBean>) {
        adapter.submitList(list)
    }

    override fun onDestroy() {
        super.onDestroy()
        adapter.release()
    }
}