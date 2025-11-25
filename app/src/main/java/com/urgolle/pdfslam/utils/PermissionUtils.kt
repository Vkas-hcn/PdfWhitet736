package com.urgolle.pdfslam.utils


import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri

typealias PermissionCallback = (Boolean) -> Unit
typealias PreJumpAction = () -> Unit

fun Context.hasStoragePermission(): Boolean =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        Environment.isExternalStorageManager()
    } else {
        val permissions = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        permissions.all { permission ->
            ActivityCompat.checkSelfPermission(
                this,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

private fun Context.createStorageSettingsIntent(): Intent =
    Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
        addCategory(Intent.CATEGORY_DEFAULT)
        data = "package:$packageName".toUri()
    }

// 新增：创建应用设置页面的 Intent
private fun Context.createAppSettingsIntent(): Intent =
    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = "package:$packageName".toUri()
    }

class StoragePermissionRequest(
    private val context: Context,
    private val launcher: ActivityLauncher
) {
    private var onPreJump: PreJumpAction? = null
    private var onResult: PermissionCallback = {}

    fun onPreJump(action: PreJumpAction) = apply {
        this.onPreJump = action
    }

    fun onResult(callback: PermissionCallback) = apply {
        this.onResult = callback
    }

    fun launch() {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R ->
                handleModernPermissionRequest()

            else ->
                handleLegacyPermissionRequest()
        }
    }

    private fun handleLegacyPermissionRequest() {
        val permissions = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        // 检查是否已经拥有权限
        if (context.hasStoragePermission()) {
            onResult(true)
            return
        }

//        if (!shouldShowRationale) {
//            launcher.activity(context.createAppSettingsIntent()).onResult {
//                onResult(context.hasStoragePermission())
//            }.launch()
//        } else {
            launcher.permissions(permissions).onResult { result ->
                val allGranted = result.values.all { it }
                if (!allGranted) {
                    // 检查是否所有权限都被永久拒绝
                    val allPermanentlyDenied = permissions.all { permission ->
                        !ActivityCompat.shouldShowRequestPermissionRationale(
                            (context as Activity),
                            permission
                        )
                    }
                    if (allPermanentlyDenied) {
                        launcher.activity(context.createAppSettingsIntent()).onResult {
                            onResult(context.hasStoragePermission())
                        }.launch()
                    } else {
                        onResult(false)
                    }
                } else {
                    onResult(true)
                }

            }.launch()
//        }

    }

    private fun handleModernPermissionRequest() {
        onPreJump?.invoke()

        launcher.activity(context.createStorageSettingsIntent()).onResult {
            onResult(context.hasStoragePermission())
        }.launch()
    }
}

// 保持原有函数的兼容性（可选）
fun Context.requestStoragePermission(
    launcher: ActivityLauncher,
    onPreJump: PreJumpAction? = null,
    onResult: PermissionCallback
) {
    StoragePermissionRequest(this, launcher)
        .apply { onPreJump?.let { onPreJump(it) } }
        .onResult(onResult)
        .launch()
}