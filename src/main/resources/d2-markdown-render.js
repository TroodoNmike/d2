// Copyright 2025 D2 Diagram Plugin. Apache 2.0 license.
// Injected into the IntelliJ Markdown JCEF preview.
//
// Timing notes (based on MarkdownJCEFHtmlPanel internals):
//   1. This script executes as an inline <script> during HTML parsing.
//   2. After the page fully loads, JcefBrowserPipeImpl sets up the message
//      bridge and dispatches window.IdeReady — only then can we post messages.
//   3. Markdown content is patched into the DOM via Incremental DOM.
//      IncrementalDOM.notifications.afterPatchListeners is the correct hook
//      for detecting content changes — the same mechanism used by MathJax and
//      ScrollSync in the platform.  setTimeout / MutationObserver are not needed.
(function () {
  'use strict';

  var ideReady = false;
  var pendingRequests = {};
  var requestCounter = 0;

  // Receive rendered SVG (or error) back from Kotlin
  window.__IntelliJTools.messagePipe.subscribe('d2/result', function (data) {
    try {
      var msg = JSON.parse(data);
      var callback = pendingRequests[msg.id];
      if (callback) {
        delete pendingRequests[msg.id];
        callback(msg.svg || null, msg.error || null);
      }
    } catch (e) {
      console.error('[D2] Failed to parse result:', e);
    }
  });

  // JCEF/Chromium does not resolve @font-face declared inside a Shadow DOM
  // style sheet. To fix this, extract @font-face rules from the SVG and inject
  // them into document.head (light DOM) where the browser can load them.
  // Base64 data URIs never contain '}', so the regex boundary is unambiguous.
  // Font-family names are unique per D2 render (e.g. "d2-1447744005-font-bold"),
  // so we deduplicate by name to avoid re-injecting on every re-render.
  function hoistFontFaces(svg) {
    var matches = svg.match(/@font-face\s*\{[^}]*\}/g) || [];
    matches.forEach(function (rule) {
      var nameMatch = rule.match(/font-family:\s*([^;"\n]+)/);
      if (!nameMatch) return;
      var key = nameMatch[1].trim().replace(/['"]/g, '');
      if (!document.head.querySelector('style[data-d2-font="' + key + '"]')) {
        var el = document.createElement('style');
        el.setAttribute('data-d2-font', key);
        el.textContent = rule;
        document.head.appendChild(el);
      }
    });
  }

  // Proportional scale factor: 1.0 means the diagram renders at its natural
  // pixel size at the current IDE Markdown font size. Increase to make diagrams
  // larger relative to the text, decrease to make them smaller.
  var D2_SCALE = 1.0;

  // Read from document.body at IdeReady — this is where IntelliJ sets the
  // Markdown preview font size. Font size changes cause a full page reload,
  // so this value is always correct for the current session.
  var refFontSize;

  // Returns the viewBox width of the root <svg>, or null if not found.
  function svgViewBoxWidth(svg) {
    var vb = svg.match(/viewBox="[\d.+-]+\s+[\d.+-]+\s+([\d.]+)\s+[\d.]+"/);
    return vb ? parseFloat(vb[1]) : null;
  }

  // Removes the canvas background by finding the rect whose width and height match
  // the viewBox dimensions (i.e. the full-canvas background rect D2 always emits).
  // The rect is removed entirely rather than set to fill="none" because the SVG's
  // embedded CSS class (fill-N7) would override a fill attribute, winning over it.
  function stripSvgBackground(svg) {
    var vb = svg.match(/viewBox="[\d.+-]+\s+[\d.+-]+\s+([\d.]+)\s+([\d.]+)"/);
    if (!vb) return svg;
    var vbW = parseFloat(vb[1]);
    var vbH = parseFloat(vb[2]);
    var replaced = false;
    return svg.replace(/<rect\b([^>]*)\/>/g, function (match, attrs) {
      if (replaced) return match;
      var wMatch = attrs.match(/\bwidth="([^"]+)"/);
      var hMatch = attrs.match(/\bheight="([^"]+)"/);
      var fillMatch = attrs.match(/\bfill="([^"]+)"/);
      if (!wMatch || !hMatch || !fillMatch) return match;
      if (parseFloat(wMatch[1]) === vbW && parseFloat(hMatch[1]) === vbH && fillMatch[1] !== 'none') {
        replaced = true;
        return '';
      }
      return match;
    });
  }

  // Removes explicit width/height attributes from the root <svg> so CSS controls sizing.
  function stripSvgDimensions(svg) {
    var s = svg.replace(/(<svg\b[^>]*?)\s+width="[^"]*"/, '$1');
    return s.replace(/(<svg\b[^>]*?)\s+height="[^"]*"/, '$1');
  }

  function showD2Error(pre, error) {
    var box = document.createElement('pre');
    box.style.cssText =
      'color:#c00;border:1px solid #c00;border-radius:4px;' +
      'padding:8px;font-size:12px;white-space:pre-wrap;margin:4px 0;';
    box.textContent = '\u26A0 D2: ' + error;
    pre.replaceWith(box);
  }

  function showD2Svg(pre, svg) {
    var host = document.createElement('div');
    // Shadow DOM isolates D2's embedded <style> tags from the page.
    var shadow = host.attachShadow({ mode: 'open' });
    // Hoist @font-face rules to the light DOM so JCEF/Chromium resolves them.
    hoistFontFaces(svg);

    svg = stripSvgBackground(svg);
    // Width in em causes the diagram to scale automatically with the IDE
    // Markdown font size. height:auto preserves the aspect ratio.
    // Falls back to max-width:100% if the SVG has no viewBox.
    var vbWidth = svgViewBoxWidth(svg);
    var svgCss = vbWidth
      ? 'svg { width: ' + ((vbWidth * D2_SCALE) / refFontSize).toFixed(3) + 'em; height: auto; max-width: 100%; display: block; }'
      : 'svg { max-width: 100%; height: auto; display: block; }';
    shadow.innerHTML = '<style>' + svgCss + '</style>' + stripSvgDimensions(svg);
    pre.replaceWith(host);
  }

  function renderD2Blocks() {
    if (!ideReady) return;
    document.querySelectorAll('code.language-d2').forEach(function (el) {
      if (el.dataset.d2Rendered) return;
      el.dataset.d2Rendered = 'pending';

      var pre = el.closest('pre') || el.parentElement;
      var source = el.textContent;
      var id = String(requestCounter++);

      pendingRequests[id] = function (svg, error) {
        if (error) {
          showD2Error(pre, error);
        } else {
          showD2Svg(pre, svg);
        }
      };

      window.__IntelliJTools.messagePipe.post(
        'd2/render',
        JSON.stringify({ id: id, source: source })
      );
    });
  }

  // IdeReady fires once the Kotlin JCEF bridge is fully set up.
  // Only after this event can we safely call messagePipe.post().
  window.addEventListener('IdeReady', function () {
    ideReady = true;
    // Fallback of 16 is a last resort — if body has no computable font size
    // something is already broken and diagrams will render at an arbitrary size.
    refFontSize = parseFloat(getComputedStyle(document.body).fontSize) || 16;
    renderD2Blocks();
  });

  // IncrementalDOM.notifications.afterPatchListeners is the platform-correct
  // hook for reacting to Markdown content changes.  Every time the preview
  // re-renders (user edits, file switch, scroll), this callback fires.
  // This is the same mechanism used by MathJax and ScrollSync.
  IncrementalDOM.notifications.afterPatchListeners.push(function () {
    renderD2Blocks();
  });
})();
