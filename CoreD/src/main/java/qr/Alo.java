package qr;


import android.os.Handler;
import android.os.Message;

import qr.b.a;


/**
 * Date：2025/7/28
 * Describe:
 */
// todo 重命名
public class Alo extends Handler {
    @Override
    public void handleMessage(Message message) {
        a.d0(message.what);
    }
}
