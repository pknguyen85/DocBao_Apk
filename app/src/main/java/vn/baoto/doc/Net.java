package vn.baoto.doc;

import android.text.Html;
import android.util.Xml;
import org.xmlpull.v1.XmlPullParser;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Net {
    public static class Item {
        public String title;
        public String link;
    }

    /** Thêm https:// nếu người dùng quên gõ. */
    public static String norm(String u) {
        u = u == null ? "" : u.trim();
        if (u.isEmpty()) return "";
        if (!u.contains("://")) u = "https://" + u;
        return u;
    }

    /** Tải một URL (tự theo chuyển hướng, kể cả http -> https). */
    public static String get(String url) throws Exception {
        String cur = url;
        for (int hop = 0; hop < 6; hop++) {
            HttpURLConnection c = (HttpURLConnection) new URL(cur).openConnection();
            c.setInstanceFollowRedirects(false);
            c.setConnectTimeout(12000);
            c.setReadTimeout(20000);
            c.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 15) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0 Mobile Safari/537.36");
            c.setRequestProperty("Accept", "application/rss+xml, application/xml, text/xml, text/html, */*");
            c.setRequestProperty("Accept-Language", "vi,en;q=0.8");
            int code = c.getResponseCode();
            if (code >= 300 && code < 400) {
                String loc = c.getHeaderField("Location");
                c.disconnect();
                if (loc == null) throw new IOException("Chuyển hướng không có đích");
                cur = new URL(new URL(cur), loc).toString();
                continue;
            }
            if (code >= 400) {
                c.disconnect();
                throw new IOException("HTTP " + code);
            }
            InputStream in = c.getInputStream();
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) > 0) bo.write(buf, 0, n);
            in.close();
            c.disconnect();
            return bo.toString("UTF-8");
        }
        throw new IOException("Quá nhiều lần chuyển hướng");
    }

    private static String clean(String s) {
        if (s == null) return "";
        return Html.fromHtml(s, Html.FROM_HTML_MODE_LEGACY).toString().trim();
    }

    /** Đọc RSS 2.0 hoặc Atom. */
    public static List<Item> parseFeed(String xml) throws Exception {
        List<Item> items = new ArrayList<>();
        if (xml == null) return items;
        xml = xml.trim();
        if (xml.startsWith("\uFEFF")) xml = xml.substring(1);
        if (!xml.startsWith("<")) return items;

        XmlPullParser p = Xml.newPullParser();
        p.setInput(new StringReader(xml));
        Item cur = null;
        int ev = p.getEventType();
        while (ev != XmlPullParser.END_DOCUMENT) {
            if (ev == XmlPullParser.START_TAG) {
                String n = p.getName();
                if (n.equals("item") || n.equals("entry")) {
                    cur = new Item();
                } else if (cur != null) {
                    if (n.equals("title")) {
                        cur.title = clean(p.nextText());
                    } else if (n.equals("link")) {
                        String href = p.getAttributeValue(null, "href");
                        String rel = p.getAttributeValue(null, "rel");
                        String txt = p.nextText();
                        if (href != null) {
                            if ((rel == null || rel.equals("alternate")) && cur.link == null) cur.link = href;
                        } else if (txt != null && txt.trim().length() > 0) {
                            cur.link = txt.trim();
                        }
                    }
                }
            } else if (ev == XmlPullParser.END_TAG) {
                String n = p.getName();
                if ((n.equals("item") || n.equals("entry")) && cur != null) {
                    if (cur.title != null && cur.title.length() > 0 && cur.link != null && cur.link.length() > 0) {
                        items.add(cur);
                    }
                    cur = null;
                }
            }
            ev = p.next();
        }
        return items;
    }

    private static boolean works(String url) {
        try {
            return !parseFeed(get(url)).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    /** Tự tìm link RSS của một trang báo bất kỳ. Trả về null nếu không thấy. */
    public static String discover(String home) {
        try {
            String base = norm(home);
            if (base.isEmpty()) return null;
            try {
                String html = get(base);
                // Chính link nhập vào đã là RSS?
                try {
                    if (!parseFeed(html).isEmpty()) return base;
                } catch (Exception ignore) {
                }
                // Tìm <link rel="alternate" type="application/rss+xml" href="...">
                Matcher m = Pattern.compile("<link[^>]+>", Pattern.CASE_INSENSITIVE).matcher(html);
                while (m.find()) {
                    String tag = m.group();
                    String low = tag.toLowerCase(Locale.ROOT);
                    if (low.contains("alternate") && (low.contains("rss+xml") || low.contains("atom+xml"))) {
                        Matcher h = Pattern.compile("href\\s*=\\s*[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE).matcher(tag);
                        if (h.find()) {
                            String cand = new URL(new URL(base), h.group(1).replace("&amp;", "&")).toString();
                            if (works(cand)) return cand;
                        }
                    }
                }
            } catch (Exception ignore) {
            }
            // Thử các đường dẫn RSS phổ biến
            String[] paths = {"/rss", "/feed", "/rss.xml", "/feed.xml", "/rss/home.rss",
                    "/trang-chu.rss", "/rss/tin-moi-nhat.rss", "/rss/tin-moi.rss"};
            URL b = new URL(base);
            String origin = b.getProtocol() + "://" + b.getHost();
            for (String path : paths) {
                String cand = origin + path;
                if (works(cand)) return cand;
            }
        } catch (Exception ignore) {
        }
        return null;
    }
}
