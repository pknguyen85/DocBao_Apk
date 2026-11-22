// Chế độ "đọc gọn": tìm phần nội dung chính của bài báo, bỏ quảng cáo/menu,
// dựng lại trang nền trắng chữ đen, đơn giản cho màn hình e-ink.
// Chạy được trên mọi trang báo; nếu không nhận ra bài báo thì giữ nguyên trang.
(function () {
  try {
    if (document.getElementById('bt-rd')) return;
    var path = location.pathname || '/';
    if (path.length < 12) return; // trang chủ / chuyên mục: không làm gọn

    // 1) Chấm điểm: đoạn văn dài cộng điểm cho phần tử cha (và 1/2 cho ông)
    var paras = document.querySelectorAll('p');
    var score = new Map();
    var i;
    for (i = 0; i < paras.length; i++) {
      var t = (paras[i].innerText || '').trim();
      if (t.length < 40) continue;
      var p1 = paras[i].parentElement;
      if (p1) score.set(p1, (score.get(p1) || 0) + t.length);
      var p2 = p1 && p1.parentElement;
      if (p2) score.set(p2, (score.get(p2) || 0) + t.length * 0.5);
    }
    var best = null, bestScore = 0;
    score.forEach(function (v, k) { if (v > bestScore) { bestScore = v; best = k; } });
    if (!best || bestScore < 500) return;

    // 2) Loại vùng toàn link (menu, danh sách tin liên quan)
    var total = (best.innerText || '').length || 1;
    var linkLen = 0, as = best.querySelectorAll('a');
    for (i = 0; i < as.length; i++) linkLen += (as[i].innerText || '').length;
    if (linkLen / total > 0.4) return;

    // 3) Sao chép và dọn rác
    var clone = best.cloneNode(true);
    var junk = clone.querySelectorAll(
      'script,style,noscript,iframe,form,button,nav,aside,video,audio,svg,input,select,textarea,source,h1,' +
      '[class*="related"],[class*="share"],[class*="social"],[class*="advert"],[class*="banner"],[id*="ad-"]');
    for (i = 0; i < junk.length; i++) {
      if (junk[i].parentNode) junk[i].parentNode.removeChild(junk[i]);
    }
    var all = clone.querySelectorAll('*');
    for (i = 0; i < all.length; i++) {
      var el = all[i];
      if (el.tagName === 'IMG') {
        var src = el.getAttribute('data-src') || el.getAttribute('data-original') ||
                  el.getAttribute('data-lazy-src') || el.currentSrc || el.getAttribute('src');
        el.removeAttribute('srcset');
        el.removeAttribute('loading');
        if (src) el.setAttribute('src', src);
      }
      el.removeAttribute('style');
      el.removeAttribute('class');
      el.removeAttribute('width');
      el.removeAttribute('height');
      el.removeAttribute('onclick');
    }

    // 4) Tiêu đề và đoạn mở đầu (sapo)
    var h1 = document.querySelector('h1');
    var title = h1 ? (h1.innerText || '').trim() : (document.title || '');
    var meta = document.querySelector('meta[property="og:description"]') ||
               document.querySelector('meta[name="description"]');
    var sapo = meta ? (meta.getAttribute('content') || '').trim() : '';
    var body = (clone.innerText || '');
    if (sapo && body.indexOf(sapo.slice(0, 40)) >= 0) sapo = '';

    // 5) Dựng lại trang
    var css =
      'html,body{background:#fff!important;color:#000!important;margin:0!important;padding:0!important;' +
      'display:block!important;position:static!important;height:auto!important;overflow:auto!important}' +
      '#bt-rd{padding:12px 14px 48px;font-family:serif;font-size:16px;line-height:1.55;word-wrap:break-word}' +
      '#bt-rd h1{font-size:1.5em;line-height:1.25;margin:0 0 .6em}' +
      '#bt-rd .bt-sapo{font-weight:bold;margin:.4em 0 .8em}' +
      '#bt-rd p,#bt-rd div,#bt-rd li{margin:.7em 0;font-size:1em;color:#000}' +
      '#bt-rd img{max-width:100%;height:auto;display:block;margin:.6em auto}' +
      '#bt-rd figcaption,#bt-rd em{font-size:.85em;color:#222}' +
      '#bt-rd a{color:#000;text-decoration:none}' +
      (window.__BT_IMG === false ? '#bt-rd img,#bt-rd picture,#bt-rd figure{display:none!important}' : '');

    var st = document.createElement('style');
    st.textContent = css;
    document.head.appendChild(st);

    var wrap = document.createElement('div');
    wrap.id = 'bt-rd';
    var h = document.createElement('h1');
    h.textContent = title;
    wrap.appendChild(h);
    if (sapo) {
      var sp = document.createElement('p');
      sp.className = 'bt-sapo';
      sp.textContent = sapo;
      wrap.appendChild(sp);
    }
    wrap.appendChild(clone);

    document.body.innerHTML = '';
    document.body.appendChild(wrap);
    window.scrollTo(0, 0);
  } catch (e) { /* lỗi gì thì giữ nguyên trang gốc */ }
})();
