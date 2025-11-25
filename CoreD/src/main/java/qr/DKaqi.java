package qr;

import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import qr.b.a;


/**
 * Date：2025/7/28
 * Describe:
 */
public class DKaqi extends WebChromeClient {
    @Override
    public void onProgressChanged(WebView webView, int i10) {
        super.onProgressChanged(webView, i10);
        Log.e("LOG-->", "onProgressChanged: " + i10);
        if (i10 == 100) {
            a.d0(i10);
        }
    }
}
