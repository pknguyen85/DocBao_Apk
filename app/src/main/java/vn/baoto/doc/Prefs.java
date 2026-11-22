package vn.baoto.doc;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class Prefs {
    public static class Source {
        public String name;
        public String home;
        public String rss;

        public Source(String name, String home, String rss) {
            this.name = name;
            this.home = home;
            this.rss = rss == null ? "" : rss;
        }
    }

    private static SharedPreferences sp(Context c) {
        return c.getSharedPreferences("baochuto", Context.MODE_PRIVATE);
    }

    public static int fontPercent(Context c) {
        return sp(c).getInt("font", 200);
    }

    public static void setFontPercent(Context c, int v) {
        sp(c).edit().putInt("font", Math.max(100, Math.min(500, v))).apply();
    }

    public static boolean reader(Context c) {
        return sp(c).getBoolean("reader", true);
    }

    public static void setReader(Context c, boolean v) {
        sp(c).edit().putBoolean("reader", v).apply();
    }

    public static boolean images(Context c) {
        return sp(c).getBoolean("images", true);
    }

    public static void setImages(Context c, boolean v) {
        sp(c).edit().putBoolean("images", v).apply();
    }

    public static List<Source> defaults() {
        List<Source> l = new ArrayList<>();
        l.add(new Source("VnExpress", "https://vnexpress.net", "https://vnexpress.net/rss/tin-moi-nhat.rss"));
        l.add(new Source("CafeBiz", "https://cafebiz.vn", "https://cafebiz.vn/trang-chu.rss"));
        l.add(new Source("BaoMoi", "https://baomoi.com", ""));
        l.add(new Source("Dân Trí", "https://dantri.com.vn", "https://dantri.com.vn/rss/home.rss"));
        l.add(new Source("Tuổi Trẻ", "https://tuoitre.vn", "https://tuoitre.vn/rss/tin-moi-nhat.rss"));
        l.add(new Source("Thanh Niên", "https://thanhnien.vn", "https://thanhnien.vn/rss/home.rss"));
        l.add(new Source("ZNews", "https://znews.vn", "https://znews.vn/trang-chu.rss"));
        return l;
    }

    public static List<Source> sources(Context c) {
        String s = sp(c).getString("sources", null);
        List<Source> out = new ArrayList<>();
        if (s != null) {
            try {
                JSONArray a = new JSONArray(s);
                for (int i = 0; i < a.length(); i++) {
                    JSONObject o = a.getJSONObject(i);
                    out.add(new Source(o.getString("name"), o.getString("home"), o.optString("rss", "")));
                }
            } catch (Exception e) {
                out.clear();
            }
        }
        if (out.isEmpty()) {
            out = defaults();
            saveSources(c, out);
        }
        return out;
    }

    public static void saveSources(Context c, List<Source> list) {
        JSONArray a = new JSONArray();
        try {
            for (Source s : list) {
                JSONObject o = new JSONObject();
                o.put("name", s.name);
                o.put("home", s.home);
                o.put("rss", s.rss);
                a.put(o);
            }
        } catch (Exception e) {
            return;
        }
        sp(c).edit().putString("sources", a.toString()).apply();
    }
}
