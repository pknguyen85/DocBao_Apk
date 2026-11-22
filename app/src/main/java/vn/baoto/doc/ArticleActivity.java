package vn.baoto.doc;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class ArticleActivity extends Activity {
    private WebView web;
    private Button bReader;
    private Button bImg;
    private String readerJs = "";
    private final Handler handler = new Handler(Looper.getMainLooper());

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        String url = getIntent().getStringExtra("url");
        if (url == null) {
            finish();
            return;
        }
        readerJs = loadAsset("reader.js");

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.WHITE);
        root.setFitsSystemWindows(true);

        web = new WebView(this);
        web.setBackgroundColor(Color.WHITE);
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setTextZoom(Prefs.fontPercent(this));
        s.setMediaPlaybackRequiresUserGesture(true);
        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest r) {
                String u = r.getUrl().toString();
                // Chỉ mở http/https trong app; chặn intent:, market: ...
                return !(u.startsWith("http://") || u.startsWith("https://"));
            }

            @Override
            public void onPageFinished(WebView v, String u) {
                if (Prefs.reader(ArticleActivity.this)) {
                    applyReader();
                    handler.postDelayed(() -> applyReader(), 1500);
                }
            }
        });
        root.addView(web, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));

        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.addView(Ui.btn(this, "Thoát", v -> finish()), Ui.weight(this));
        bar.addView(Ui.btn(this, "A-", v -> bump(-20)), Ui.weight(this));
        bar.addView(Ui.btn(this, "A+", v -> bump(20)), Ui.weight(this));
        bReader = Ui.btn(this, "", v -> {
            Prefs.setReader(this, !Prefs.reader(this));
            refreshLabels();
            web.reload();
        });
        bImg = Ui.btn(this, "", v -> {
            Prefs.setImages(this, !Prefs.images(this));
            refreshLabels();
            web.reload();
        });
        bar.addView(bReader, Ui.weight(this, 1.4f));
        bar.addView(bImg, Ui.weight(this, 1.2f));
        bar.addView(Ui.btn(this, "Lên", v -> web.pageUp(false)), Ui.weight(this, 1.3f));
        bar.addView(Ui.btn(this, "Xuống", v -> web.pageDown(false)), Ui.weight(this, 1.6f));
        root.addView(bar);

        setContentView(root);
        refreshLabels();
        web.loadUrl(url);
    }

    private void refreshLabels() {
        bReader.setText("Đọc gọn: " + (Prefs.reader(this) ? "Bật" : "Tắt"));
        bImg.setText("Ảnh: " + (Prefs.images(this) ? "Bật" : "Tắt"));
    }

    private void bump(int delta) {
        int v = Prefs.fontPercent(this) + delta;
        Prefs.setFontPercent(this, v);
        web.getSettings().setTextZoom(Prefs.fontPercent(this));
    }

    private void applyReader() {
        if (readerJs == null || readerJs.isEmpty()) return;
        web.evaluateJavascript("window.__BT_IMG=" + Prefs.images(this) + ";" + readerJs, null);
    }

    private String loadAsset(String name) {
        try (InputStream in = getAssets().open(name)) {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int n;
            while ((n = in.read(buf)) > 0) bo.write(buf, 0, n);
            return bo.toString("UTF-8");
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public void onBackPressed() {
        if (web != null && web.canGoBack()) {
            web.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        if (web != null) web.destroy();
        super.onDestroy();
    }
}
