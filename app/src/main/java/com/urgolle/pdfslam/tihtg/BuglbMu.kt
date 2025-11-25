package com.urgolle.pdfslam.tihtg

import android.app.AlertDialog
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.urgolle.pdfslam.R
import com.urgolle.pdfslam.databinding.ActivityMainBinding
import com.urgolle.pdfslam.utils.ActivityLauncher
import com.urgolle.pdfslam.utils.hasStoragePermission
import com.urgolle.pdfslam.utils.requestStoragePermission
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

//主页
class BuglbMu : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var launcher: ActivityLauncher
    private var permissionDialog: AlertDialog? = null

    val viewModel: BuglbVM by viewModels()


    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        launcher = ActivityLauncher(this)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        navController =
            (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment).navController

        binding.composeView.setContent {

            val selectedTabIndex by viewModel.selectedTabIndex.collectAsStateWithLifecycle()
            TabNav(
                selectedTabIndex,
                onTabSelected = { index ->
                    viewModel.selectTab(index)
                    // 这里可以添加导航逻辑
                    when (index) {
                        0 -> navNoPop(R.id.homeFragment)
                        1 -> navNoPop(R.id.recentFragment)
                        2 -> navNoPop(R.id.setFragment)
                    }
                },
                modifier = Modifier
                    .wrapContentWidth()
                    .height(75.dp)
            )
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.pdfList.collectLatest {
                    val fragment = this@BuglbMu.getCurrentFragment()
                    if (fragment is HomeFragment) {
                        fragment.subList(it)
                    }
                    if (fragment is RecentFragment) {
                        fragment.subList(it)
                    }
                }
            }
        }

        // 延迟显示权限对话框，避免 Window 泄漏
        binding.root.post {
            if (this.hasStoragePermission()) {
                scan()
            } else {
                showPermissionDialog()
            }
        }

    }

    fun getCurrentFragment(): Fragment? {
        return supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
            ?.childFragmentManager
            ?.fragments
            ?.firstOrNull()
    }

    /**
     * 显示权限请求对话框
     */
    private fun showPermissionDialog() {
        // 确保在主线程且 Activity 有效
        if (isFinishing || isDestroyed) return
        
        permissionDialog = AlertDialog.Builder(this)
            .setTitle("Need Storage Permission")
            .setMessage("Requires permission to search for PDF files.")
            .setPositiveButton("Yes") { dialog, which ->
                requestStoragePermission(launcher) {
                    if (it) {
                        scan()
                    }
                }
            }
            .setNegativeButton("Cancel") { dialog, which ->
                dialog.dismiss()
            }
            .setOnDismissListener {
                permissionDialog = null
            }
            .create()
        
        permissionDialog?.show()
    }

    fun scan() {
        viewModel.scanAllPdfs(lifecycleScope) {

        }
    }

    fun navNoPop(id: Int) {

        val currentDestination = navController.currentDestination?.id

        if (currentDestination == id) {
            return
        }

        // 使用 popUpTo 但包含目标 Fragment
        val navOptions = NavOptions.Builder()
            .setPopUpTo(navController.graph.id, false)
            .setLaunchSingleTop(true)
            .setRestoreState(true)
            .build()

        navController.navigate(id, null, navOptions)
    }

    override fun onDestroy() {
        // 防止 Window 泄漏：销毁时关闭对话框
        permissionDialog?.dismiss()
        permissionDialog = null
        super.onDestroy()
    }

}

@Composable
fun TabNav(
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {

    // 定义标签项，包含图标和对应的文本
    val tabs = listOf(
        TabItem(
            R.drawable.ic_home_s,
            R.drawable.ic_home_n,
            "Home"
        ),
        TabItem(
            R.drawable.ic_recent_s,
            R.drawable.ic_recent_n,
            "Recent"
        ),
        TabItem(
            R.drawable.ic_set_s,
            R.drawable.ic_set_n,
            "Setting"
        ),
    )

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(95.dp))
            .background(Color.Black)
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        tabs.forEachIndexed { index, tabItem ->
            // 每个标签项包含按钮和文本
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable { onTabSelected(index) }
                    .padding(horizontal = 4.dp)
            ) {
                TabButton(
                    isSelected = index == selectedTabIndex,
                    selectedIconRes = tabItem.selectedIconRes,
                    unselectedIconRes = tabItem.unselectedIconRes
                )

                // 文本动画效果
                AnimatedVisibility(
                    visible = index == selectedTabIndex,
                    enter = slideInHorizontally(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    ) + fadeIn(
                        animationSpec = tween(durationMillis = 300)
                    ),
                    exit = fadeOut() // 使用默认的淡出效果，但设置时间为0
                ) {
                    Text(
                        text = tabItem.text,
                        style = TextStyle(
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier.padding(start = 8.dp, end = 4.dp)
                    )
                }
            }

            if (index < tabs.size - 1) {
                Spacer(modifier = Modifier.width(4.dp))
            }
        }
    }
}

@Composable
fun TabButton(
    isSelected: Boolean,
    selectedIconRes: Int,
    unselectedIconRes: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(58.dp)
            .clip(CircleShape)
            .background(if (isSelected) Color(0xFFC2F46B) else Color(0xFF272727)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = if (isSelected) selectedIconRes else unselectedIconRes),
            contentDescription = null,
            tint = if (isSelected) Color.Black else Color.White,
            modifier = Modifier.size(24.dp)
        )
    }
}

data class TabItem(
    val selectedIconRes: Int, // drawable 资源 ID
    val unselectedIconRes: Int, // drawable 资源 ID
    val text: String // 标签文本
)