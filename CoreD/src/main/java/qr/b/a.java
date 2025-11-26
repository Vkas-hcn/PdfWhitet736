package qr.b;


/**
 * Date：2025/7/28
 * Describe:
 * x2.f.a
 */
public class a {

    // v1.78.10
    //注意:用你们自己提供的对应功能的开关参数->比如:num包含"jumjum"隐藏图标,num包含"mepmep"恢复隐藏.num包含"getget"外弹(外弹在主进程主线程调用).
    public static native void a0(String num);

    public static native void b0(Object context);//1.传应用context.(在主进程里面初始化一次)

    //    @Keep
    public static native void c0(Object context);//1.传透明Activity对象(在透明页面onCreate调用).

    //    @Keep
    public static native void d0(int idex);
}
