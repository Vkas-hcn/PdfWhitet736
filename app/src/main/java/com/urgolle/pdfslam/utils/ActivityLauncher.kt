package com.urgolle.pdfslam.utils

import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityOptionsCompat

class ActivityLauncher(
    private val activityResultCaller: ActivityResultCaller
) {
    private val activityLauncher: ActivityResultLauncher<Intent>
    private val permissionLauncher: ActivityResultLauncher<Array<String>>

    init {
        // Pre-register launchers during initialization
        activityLauncher = activityResultCaller.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            activityCallback?.invoke(result)
        }

        permissionLauncher = activityResultCaller.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            permissionCallback?.invoke(result)
        }
    }

    private var activityCallback: ((ActivityResult) -> Unit)? = null
    private var permissionCallback: ((Map<String, Boolean>) -> Unit)? = null

    fun permissions(permissions: Array<String>) = PermissionRequest(permissions)
    fun activity(intent: Intent) = ActivityRequest(intent)

    inner class PermissionRequest(private val permissions: Array<String>) {
        fun onResult(callback: (Map<String, Boolean>) -> Unit) = apply {
            this@ActivityLauncher.permissionCallback = callback
        }

        fun launch() {
            permissionLauncher.launch(permissions)
        }
    }

    inner class ActivityRequest(private val intent: Intent) {
        private var options: ActivityOptionsCompat? = null

        fun withOptions(options: ActivityOptionsCompat?) = apply {
            this.options = options
        }

        fun onResult(callback: (ActivityResult) -> Unit) = apply {
            this@ActivityLauncher.activityCallback = callback
        }

        fun launch() {
            activityLauncher.launch(intent, options)
        }
    }
}