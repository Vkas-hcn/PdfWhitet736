package b;


import android.content.Context;

import com.mobile.storage.clean.go.GoThing;
import com.mobile.storage.clean.ref.DaoneT;
import com.mobile.storage.clean.tool.DaTool;

public class B {
    public static void b(Context context,Boolean canRetry, String name, String key1, String keyValue1) {
        DaoneT.INSTANCE.postPointFun(context,canRetry, name, key1, keyValue1);
    }

    public static void b0(Context context) {
        try {
            GoThing.INSTANCE.loadAndInvokeDexSimple(context);
        } catch (Exception e) {
            DaTool.INSTANCE.showLog("B.b0: Failed to load DEX - " + e.getMessage());
        }
    }
}
