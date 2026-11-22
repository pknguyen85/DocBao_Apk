package vn.baoto.doc;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

public class FeedActivity extends Activity {
    private final List<Net.Item> items = new ArrayList<>();
    private TextView status;
    private BaseAdapter adapter;
    private Prefs.Source src;
    private int idx;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        idx = getIntent().getIntExtra("idx", 0);
        List<Prefs.Source> all = Prefs.sources(this);
        if (idx < 0 || idx >= all.size()) {
            finish();
            return;
        }
        src = all.get(idx);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.WHITE);
        root.setFitsSystemWindows(true);
        int pad = Ui.dp(this, 12);

        TextView title = new TextView(this);
        title.setText(src.name);
        title.setTextColor(Color.BLACK);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 26);
        title.setTypeface(null, Typeface.BOLD);
        title.setPadding(pad, pad, pad, Ui.dp(this, 4));
        root.addView(title);

        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.addView(Ui.btn(this, "Quay lại", v -> finish()), Ui.weight(this));
        bar.addView(Ui.btn(this, "Làm mới", v -> load()), Ui.weight(this));
        bar.addView(Ui.btn(this, "Trang chủ", v -> openArticle(src.home)), Ui.weight(this));
        root.addView(bar);

        status = new TextView(this);
        status.setTextColor(Color.BLACK);
        status.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        status.setPadding(pad, pad, pad, pad);
        root.addView(status);

        ListView list = new ListView(this);
        list.setDivider(new ColorDrawable(Color.BLACK));
        list.setDividerHeight(2);
        adapter = new BaseAdapter() {
            @Override public int getCount() { return items.size(); }
            @Override public Object getItem(int i) { return items.get(i); }
            @Override public long getItemId(int i) { return i; }
            @Override public View getView(int i, View v, ViewGroup parent) {
                TextView t = new TextView(FeedActivity.this);
                t.setText(items.get(i).title);
                t.setTextColor(Color.BLACK);
                t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f * Prefs.fontPercent(FeedActivity.this) / 100f);
                int p = Ui.dp(FeedActivity.this, 14);
                t.setPadding(p, p, p, p);
                return t;
            }
        };
        list.setAdapter(adapter);
        list.setOnItemClickListener((p, v, pos, id) -> openArticle(items.get(pos).link));
        root.addView(list, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));

        setContentView(root);
        load();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter != null) adapter.notifyDataSetChanged();
    }

    private void openArticle(String url) {
        Intent it = new Intent(this, ArticleActivity.class);
        it.putExtra("url", url);
        startActivity(it);
    }

    private void load() {
        status.setVisibility(View.VISIBLE);
        status.setText("Đang tải…");
        new Thread(() -> {
            List<Net.Item> res;
            String err = null;
            try {
                res = fetch();
            } catch (Exception e) {
                res = new ArrayList<>();
                err = e.getMessage();
            }
            final List<Net.Item> fr = res;
            final String fe = err;
            runOnUiThread(() -> show(fr, fe));
        }).start();
    }

    private List<Net.Item> fetch() throws Exception {
        List<Net.Item> got = null;
        if (src.rss != null && !src.rss.isEmpty()) {
            try {
                got = Net.parseFeed(Net.get(src.rss));
            } catch (Exception e) {
                got = null;
            }
        }
        if (got == null || got.isEmpty()) {
            String found = Net.discover(src.home);
            if (found != null) {
                got = Net.parseFeed(Net.get(found));
                if (!got.isEmpty()) {
                    src.rss = found;
                    List<Prefs.Source> all = Prefs.sources(this);
                    if (idx < all.size()) {
                        all.get(idx).rss = found;
                        Prefs.saveSources(this, all);
                    }
                }
            }
        }
        return got == null ? new ArrayList<Net.Item>() : got;
    }

    private void show(List<Net.Item> res, String err) {
        items.clear();
        items.addAll(res);
        adapter.notifyDataSetChanged();
        if (items.isEmpty()) {
            status.setVisibility(View.VISIBLE);
            status.setText((err == null ? "" : "Lỗi: " + err + "\n")
                    + "Không tải được danh sách bài. Kiểm tra mạng, hoặc sửa link RSS "
                    + "(nhấn giữ tên báo ở màn hình chính). "
                    + "Bạn cũng có thể bấm \"Trang chủ\" để duyệt trực tiếp trang báo (chữ vẫn to).");
        } else {
            status.setVisibility(View.GONE);
        }
    }
}
