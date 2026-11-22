package vn.baoto.doc;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.net.URL;
import java.util.List;

public class MainActivity extends Activity {
    private List<Prefs.Source> sources;
    private BaseAdapter adapter;
    private TextView sizeLabel;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        sources = Prefs.sources(this);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.WHITE);
        root.setFitsSystemWindows(true);
        int pad = Ui.dp(this, 12);

        TextView title = new TextView(this);
        title.setText("Báo chữ to");
        title.setTextColor(Color.BLACK);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
        title.setTypeface(null, Typeface.BOLD);
        title.setPadding(pad, pad, pad, 0);
        root.addView(title);

        sizeLabel = new TextView(this);
        sizeLabel.setTextColor(Color.BLACK);
        sizeLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        sizeLabel.setPadding(pad, Ui.dp(this, 4), pad, Ui.dp(this, 4));
        root.addView(sizeLabel);

        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.addView(Ui.btn(this, "A-", v -> bump(-20)), Ui.weight(this));
        bar.addView(Ui.btn(this, "A+", v -> bump(20)), Ui.weight(this));
        bar.addView(Ui.btn(this, "+ Thêm báo", v -> editSource(-1)), Ui.weight(this, 2f));
        root.addView(bar);

        TextView hint = new TextView(this);
        hint.setText("Chạm để mở. Nhấn giữ tên báo để sửa / xoá.");
        hint.setTextColor(Color.DKGRAY);
        hint.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        hint.setPadding(pad, Ui.dp(this, 4), pad, Ui.dp(this, 4));
        root.addView(hint);

        ListView list = new ListView(this);
        list.setDivider(new ColorDrawable(Color.BLACK));
        list.setDividerHeight(2);
        adapter = new BaseAdapter() {
            @Override public int getCount() { return sources.size(); }
            @Override public Object getItem(int i) { return sources.get(i); }
            @Override public long getItemId(int i) { return i; }
            @Override public View getView(int i, View v, ViewGroup parent) {
                TextView t = new TextView(MainActivity.this);
                t.setText(sources.get(i).name);
                t.setTextColor(Color.BLACK);
                t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f * Prefs.fontPercent(MainActivity.this) / 100f);
                int p = Ui.dp(MainActivity.this, 14);
                t.setPadding(p, p, p, p);
                return t;
            }
        };
        list.setAdapter(adapter);
        list.setOnItemClickListener((p, v, pos, id) -> {
            Intent it = new Intent(this, FeedActivity.class);
            it.putExtra("idx", pos);
            startActivity(it);
        });
        list.setOnItemLongClickListener((p, v, pos, id) -> {
            showMenu(pos);
            return true;
        });
        root.addView(list, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));

        setContentView(root);
        updateLabel();
    }

    @Override
    protected void onResume() {
        super.onResume();
        sources = Prefs.sources(this);
        adapter.notifyDataSetChanged();
    }

    private void updateLabel() {
        sizeLabel.setText("Cỡ chữ: " + Prefs.fontPercent(this) + "%");
    }

    private void bump(int delta) {
        Prefs.setFontPercent(this, Prefs.fontPercent(this) + delta);
        updateLabel();
        adapter.notifyDataSetChanged();
    }

    private void showMenu(final int pos) {
        String[] opts = {"Sửa", "Xoá", "Khôi phục danh sách mặc định"};
        new AlertDialog.Builder(this)
                .setTitle(sources.get(pos).name)
                .setItems(opts, (d, which) -> {
                    if (which == 0) {
                        editSource(pos);
                    } else if (which == 1) {
                        sources.remove(pos);
                        Prefs.saveSources(this, sources);
                        adapter.notifyDataSetChanged();
                    } else {
                        sources.clear();
                        sources.addAll(Prefs.defaults());
                        Prefs.saveSources(this, sources);
                        adapter.notifyDataSetChanged();
                    }
                })
                .show();
    }

    private EditText field(String hint, String val, boolean uri) {
        EditText e = new EditText(this);
        e.setHint(hint);
        e.setText(val);
        e.setSingleLine(true);
        e.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        e.setTextColor(Color.BLACK);
        e.setInputType(InputType.TYPE_CLASS_TEXT | (uri ? InputType.TYPE_TEXT_VARIATION_URI : 0));
        return e;
    }

    /** idx < 0: thêm mới; ngược lại: sửa. */
    private void editSource(final int idx) {
        final Prefs.Source s = idx >= 0 ? sources.get(idx) : null;
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        int p = Ui.dp(this, 16);
        box.setPadding(p, p, p, 0);
        final EditText name = field("Tên báo (có thể để trống)", s == null ? "" : s.name, false);
        final EditText home = field("Trang chủ, ví dụ vnexpress.net", s == null ? "" : s.home, true);
        final EditText rss = field("Link RSS (để trống = tự tìm)", s == null ? "" : s.rss, true);
        box.addView(name);
        box.addView(home);
        box.addView(rss);

        new AlertDialog.Builder(this)
                .setTitle(s == null ? "Thêm tờ báo" : "Sửa tờ báo")
                .setView(box)
                .setPositiveButton("Lưu", (d, w) -> {
                    String h = Net.norm(home.getText().toString());
                    String r = Net.norm(rss.getText().toString());
                    if (h.isEmpty() && !r.isEmpty()) {
                        try {
                            URL u = new URL(r);
                            h = u.getProtocol() + "://" + u.getHost();
                        } catch (Exception ignore) {
                        }
                    }
                    if (h.isEmpty()) {
                        Toast.makeText(this, "Cần nhập trang chủ hoặc link RSS", Toast.LENGTH_LONG).show();
                        return;
                    }
                    String n = name.getText().toString().trim();
                    if (n.isEmpty()) {
                        String host = Uri.parse(h).getHost();
                        n = host == null ? h : host.replaceFirst("^www\\.", "");
                    }
                    if (s == null) {
                        sources.add(new Prefs.Source(n, h, r));
                    } else {
                        s.name = n;
                        s.home = h;
                        s.rss = r;
                    }
                    Prefs.saveSources(this, sources);
                    adapter.notifyDataSetChanged();
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }
}
