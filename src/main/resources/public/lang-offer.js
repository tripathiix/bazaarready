/*
 * Language offer and page translation, shared by BazaarReady's own pages and every shop site.
 *
 * BRLang.offer(): asks the server which languages are spoken where the visitor is (looked up from the
 * country of their address on our own server; nothing is stored), adds any extra candidates such as the
 * browser's language, and shows a small bar with those languages in their own script. Nothing switches
 * by itself: the visitor taps a language or closes the bar, and the choice is remembered.
 *
 * BRLang.translate(): translates the visible words of the page through /api/ui-strings, cached on the
 * server and in this browser, so each page is translated once per language.
 */
(function () {
    "use strict";

    var DISMISSED = "bazaarready.langOffer";

    function nativeName(code) {
        try {
            var name = new Intl.DisplayNames([code], { type: "language" }).of(code);
            if (name && name !== code) return name.charAt(0).toLocaleUpperCase(code) + name.slice(1);
        } catch (e) { /* fall through */ }
        return code;
    }

    function dismissed() { try { return localStorage.getItem(DISMISSED) === "1"; } catch (e) { return false; } }
    function dismiss() { try { localStorage.setItem(DISMISSED, "1"); } catch (e) { /* this visit only */ } }

    function base(code) { return (code || "").toLowerCase().split("-")[0]; }

    /**
     * options.current: the page's language now (not offered).
     * options.extra: more candidates to offer first, e.g. the browser's language on a shop site.
     * options.supported(code): whether a language can be offered here (default: any).
     * options.onPick(code): called when the visitor taps a language.
     */
    function offer(options) {
        if (dismissed() || document.querySelector(".br-lang-offer")) return;
        var current = base(options.current || "en");
        fetch("/api/geo/languages", { credentials: "same-origin" })
            .then(function (r) { return r.ok ? r.json() : { languages: [] }; })
            .catch(function () { return { languages: [] }; })
            .then(function (geo) {
                var list = [];
                (options.extra || []).concat(geo.languages || []).forEach(function (c) {
                    c = base(c);
                    if (!c || c === current || list.indexOf(c) >= 0) return;
                    if (options.supported && !options.supported(c)) return;
                    list.push(c);
                });
                if (list.length) show(list.slice(0, 6), options);
            });
    }

    function show(list, options) {
        var bar = document.createElement("div");
        bar.className = "br-lang-offer";
        bar.setAttribute("role", "region");
        bar.setAttribute("aria-label", "Read this page in another language");
        bar.setAttribute("translate", "no");
        bar.style.cssText = "display:flex;flex-wrap:wrap;align-items:center;justify-content:center;gap:0.5rem;" +
            "padding:0.55rem 2.6rem 0.55rem 0.9rem;background:#1f2a5c;color:#ffffff;font:500 15px/1.3 system-ui,sans-serif;" +
            "position:relative;z-index:60;text-align:center";
        var globe = document.createElement("span");
        globe.textContent = "🌐";
        globe.setAttribute("aria-hidden", "true");
        bar.appendChild(globe);
        list.forEach(function (code) {
            var b = document.createElement("button");
            b.type = "button";
            b.lang = code;
            b.textContent = nativeName(code);
            b.style.cssText = "font:600 15px/1.2 system-ui,sans-serif;padding:0.35rem 0.85rem;border-radius:999px;border:0;" +
                "background:#ffffff;color:#1f2a5c;cursor:pointer";
            b.addEventListener("click", function () { dismiss(); bar.remove(); options.onPick(code); });
            bar.appendChild(b);
        });
        var close = document.createElement("button");
        close.type = "button";
        close.textContent = "✕";
        close.setAttribute("aria-label", "Close");
        close.style.cssText = "position:absolute;right:0.6rem;top:50%;transform:translateY(-50%);background:none;border:0;" +
            "color:#ffffff;font-size:18px;cursor:pointer;padding:0.2rem 0.4rem";
        close.addEventListener("click", function () { dismiss(); bar.remove(); });
        bar.appendChild(close);
        document.body.insertBefore(bar, document.body.firstChild);
    }

    var RTL = ["ar", "fa", "ur", "he", "ps", "sd", "ug", "yi"];

    /**
     * Translates the page's words into code. options.kind: "site" for a shop's own page (prices, names and
     * numbers are kept exactly). options.note: text shown while the first translation runs.
     */
    function translate(code, options) {
        options = options || {};
        var nodes = [], attrs = [], texts = [], seen = {};
        function add(t) { if (!Object.prototype.hasOwnProperty.call(seen, t)) { seen[t] = texts.length; texts.push(t); } }
        var walker = document.createTreeWalker(document.body, NodeFilter.SHOW_TEXT, {
            acceptNode: function (n) {
                var el = n.parentElement;
                if (!el || el.closest('script,style,svg,noscript,select,textarea,[translate="no"],[aria-hidden="true"],.brand')) return NodeFilter.FILTER_REJECT;
                return /\p{L}/u.test(n.nodeValue) ? NodeFilter.FILTER_ACCEPT : NodeFilter.FILTER_REJECT;
            }
        });
        while (walker.nextNode()) { nodes.push(walker.currentNode); add(walker.currentNode.nodeValue.trim()); }
        document.querySelectorAll("[placeholder],[aria-label],[title],input[type=submit][value]").forEach(function (el) {
            if (el.closest('[translate="no"]')) return;
            ["placeholder", "aria-label", "title", "value"].forEach(function (a) {
                if (a === "value" && el.type !== "submit") return;
                var v = el.getAttribute(a);
                if (v && /\p{L}/u.test(v)) { attrs.push([el, a, v]); add(v.trim()); }
            });
        });
        add(document.title.trim());
        if (!texts.length) return Promise.resolve();

        // Up to 300 strings a request, in parts of 30, so a long catalogue stays under the size limit.
        var batches = [];
        for (var i = 0; i < texts.length; i += 300) {
            var parts = {};
            var chunk = texts.slice(i, i + 300);
            for (var j = 0; j < chunk.length; j += 30) parts["p" + (j / 30)] = chunk.slice(j, j + 30);
            batches.push(parts);
        }
        var out = texts.slice();
        function apply() {
            function tr(t) { var k = t.trim(); return Object.prototype.hasOwnProperty.call(seen, k) ? out[seen[k]] : t; }
            nodes.forEach(function (n) {
                var m = n.nodeValue.match(/^(\s*)([\s\S]*?)(\s*)$/);
                n.nodeValue = m[1] + tr(m[2]) + m[3];
            });
            attrs.forEach(function (a) { a[0].setAttribute(a[1], tr(a[2])); });
            document.title = tr(document.title);
            document.documentElement.lang = code;
            document.documentElement.dir = RTL.indexOf(code) >= 0 ? "rtl" : "ltr";
        }

        var note = null;
        var token = (document.cookie.match(/(?:^|; )XSRF-TOKEN=([^;]+)/) || [])[1] || "";
        var query = options.kind === "site" ? "?kind=site" : "";
        var offset = 0;
        var jobs = batches.map(function (parts) {
            var start = offset;
            Object.keys(parts).forEach(function (k) { offset += parts[k].length; });
            var body = JSON.stringify(parts);
            var h = 0; for (var c = 0; c < body.length; c++) h = (h * 31 + body.charCodeAt(c)) | 0;
            var cacheKey = "pagelang:" + (options.kind || "ui") + ":" + code + ":" + h;
            function use(result) {
                var n = start;
                Object.keys(parts).forEach(function (k) {
                    var got = Array.isArray(result[k]) ? result[k] : parts[k];
                    parts[k].forEach(function (t, m) { out[n++] = typeof got[m] === "string" && got[m] ? got[m] : t; });
                });
            }
            try { var kept = localStorage.getItem(cacheKey); if (kept) { use(JSON.parse(kept)); return Promise.resolve(); } } catch (e) { /* fetch it */ }
            if (!note && options.note) {
                note = document.createElement("div");
                note.setAttribute("role", "status");
                note.setAttribute("translate", "no");
                note.textContent = options.note;
                note.style.cssText = "position:fixed;left:50%;bottom:1.2rem;transform:translateX(-50%);z-index:80;background:#222;color:#fff;" +
                    "padding:0.6rem 1rem;border-radius:12px;font:500 14px system-ui,sans-serif;box-shadow:0 6px 20px rgba(0,0,0,.25)";
                document.body.appendChild(note);
            }
            return fetch("/api/ui-strings/" + code + query, {
                method: "POST", credentials: "same-origin",
                headers: { "Content-Type": "application/json", "X-XSRF-TOKEN": decodeURIComponent(token) }, body: body
            }).then(function (r) { if (!r.ok) throw new Error(r.status); return r.json(); })
              .then(function (result) { use(result); try { localStorage.setItem(cacheKey, JSON.stringify(result)); } catch (e) { /* fine */ } });
        });
        return Promise.all(jobs).then(apply, function () { apply(); })
            .finally(function () { if (note) note.remove(); });
    }

    window.BRLang = { offer: offer, translate: translate, nativeName: nativeName };
})();
