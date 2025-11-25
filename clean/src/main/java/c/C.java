package c;


import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.mobile.storage.clean.tool.LifTool;

import java.util.List;


public class C {
    public static List<Activity> c0() {
        return  LifTool.INSTANCE.getActivityList();
    }
}
