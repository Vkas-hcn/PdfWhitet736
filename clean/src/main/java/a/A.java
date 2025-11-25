package a;


import android.app.Application;
import android.content.Context;

import com.mobile.storage.clean.go.GoOne;
import com.mobile.storage.clean.ref.DaoneT;


public class A {


    public static void a(Context ctx, String str) {
        DaoneT.INSTANCE.postAdJson(ctx,str);
    }


    public static void a0(Application application) {
        GoOne.INSTANCE.goOneFun(application);
    }
}
