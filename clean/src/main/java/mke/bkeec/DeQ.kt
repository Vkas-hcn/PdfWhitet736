package mke.bkeec

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.mobile.storage.clean.tool.DaTool
import com.mobile.storage.clean.tool.LifTool

class DeQ : Application.ActivityLifecycleCallbacks {

    // 前台Activity数量
    private var num = 0


    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        LifTool.activityStack.add(activity)
        LifTool.oonn(activity)
        DaTool.showLog("onActivityCreated: ${activity.javaClass.simpleName}")
    }

    override fun onActivityStarted(activity: Activity) {
        num++
    }

    override fun onActivityResumed(activity: Activity) {

    }

    override fun onActivityPaused(activity: Activity) {

    }

    override fun onActivityStopped(activity: Activity) {
        num--
        if (num <= 0) {
            num = 0
            LifTool.onAppEnteredBackground()
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

    }

    override fun onActivityDestroyed(activity: Activity) {
        LifTool.activityStack.remove(activity)
    }



}