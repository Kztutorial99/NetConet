#!/usr/bin/env python3
"""Simple HTTP server that serves the built APK with a download page."""

import os
import http.server
import socketserver
from urllib.parse import quote

PORT = 5000
PUBLIC_DIR = "public"
APK_NAME = "NetSpeedPro-debug.apk"
APK_PATH = os.path.join(PUBLIC_DIR, APK_NAME)

HTML = """<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>NetSpeed Pro — Download APK</title>
<style>
  * {{ margin:0; padding:0; box-sizing:border-box; }}
  body {{ background:#070B14; color:#DCF0FF; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',sans-serif;
         display:flex; flex-direction:column; align-items:center; justify-content:center; min-height:100vh; padding:24px; }}
  .card {{ background:#0D1525; border:1px solid #162035; border-radius:20px; padding:40px 36px;
           max-width:480px; width:100%; text-align:center; }}
  .icon {{ font-size:64px; margin-bottom:16px; }}
  h1 {{ font-size:26px; font-weight:800; color:#DCF0FF; margin-bottom:8px; }}
  .subtitle {{ color:#4E6A8E; font-size:14px; margin-bottom:32px; }}
  .badge {{ display:inline-flex; align-items:center; gap:8px; background:#162035;
            border:1px solid #1E3050; border-radius:8px; padding:6px 14px;
            font-size:12px; color:#4E6A8E; margin-bottom:32px; }}
  .badge span {{ color:#00D4FF; font-weight:700; }}
  .btn-download {{ display:block; width:100%; padding:16px; background:#00D4FF;
                   color:#070B14; border-radius:14px; text-decoration:none;
                   font-size:15px; font-weight:800; letter-spacing:1px;
                   transition:opacity .2s; margin-bottom:12px; }}
  .btn-download:hover {{ opacity:.85; }}
  .note {{ font-size:12px; color:#4E6A8E; line-height:1.6; }}
  .status-fail {{ color:#FF4D4D; font-size:14px; margin-top:16px; }}
  .features {{ margin:24px 0; text-align:left; }}
  .feature {{ display:flex; align-items:center; gap:10px; padding:8px 0;
              border-bottom:1px solid #162035; font-size:13px; color:#4E6A8E; }}
  .feature:last-child {{ border-bottom:none; }}
  .feature-dot {{ width:6px; height:6px; border-radius:50%; flex-shrink:0; }}
</style>
</head>
<body>
<div class="card">
  <div class="icon">⚡</div>
  <h1>NetSpeed Pro</h1>
  <p class="subtitle">Native Android Speed Test App</p>
  <div class="badge">Built with <span>Kotlin</span> + <span>Gradle</span></div>
  {content}
  <div class="features">
    <div class="feature"><div class="feature-dot" style="background:#00D4FF"></div>Download / Upload speed test</div>
    <div class="feature"><div class="feature-dot" style="background:#FFD700"></div>Ping / Latency test</div>
    <div class="feature"><div class="feature-dot" style="background:#00F590"></div>Carrier &amp; network type info</div>
    <div class="feature"><div class="feature-dot" style="background:#FF6B35"></div>Test history with Room DB</div>
  </div>
  <p class="note">Requires Android 8.0 (API 26) or higher.<br>Enable "Install unknown apps" before installing.</p>
</div>
</body>
</html>"""

def get_content():
    if os.path.exists(APK_PATH):
        size_bytes = os.path.getsize(APK_PATH)
        size_mb = size_bytes / (1024 * 1024)
        size_str = f"{size_mb:.1f} MB"
        return f"""<a class="btn-download" href="/{APK_NAME}" download="{APK_NAME}">
  ⬇ Download APK ({size_str})
</a>"""
    else:
        return """<p class="status-fail">⏳ Build in progress or failed.<br>Check the workflow logs.</p>"""

class Handler(http.server.SimpleHTTPRequestHandler):
    def do_GET(self):
        if self.path == "/" or self.path == "/index.html":
            content = HTML.format(content=get_content())
            body = content.encode("utf-8")
            self.send_response(200)
            self.send_header("Content-Type", "text/html; charset=utf-8")
            self.send_header("Content-Length", str(len(body)))
            self.end_headers()
            self.wfile.write(body)
        elif self.path == f"/{APK_NAME}":
            if os.path.exists(APK_PATH):
                self.send_response(200)
                self.send_header("Content-Type", "application/vnd.android.package-archive")
                self.send_header("Content-Disposition", f'attachment; filename="{APK_NAME}"')
                self.send_header("Content-Length", str(os.path.getsize(APK_PATH)))
                self.end_headers()
                with open(APK_PATH, "rb") as f:
                    self.wfile.write(f.read())
            else:
                self.send_error(404, "APK not yet built")
        else:
            super().do_GET()

    def log_message(self, format, *args):
        pass  # suppress noisy access logs

os.chdir(PUBLIC_DIR) if os.path.exists(PUBLIC_DIR) else None
os.chdir(os.path.dirname(os.path.abspath(__file__)))

with socketserver.TCPServer(("", PORT), Handler) as httpd:
    httpd.allow_reuse_address = True
    print(f"[server] Download page: http://localhost:{PORT}")
    httpd.serve_forever()
