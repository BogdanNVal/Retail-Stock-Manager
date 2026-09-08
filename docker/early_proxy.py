#!/usr/bin/env python3
"""Bind the public PORT before the JVM starts (Render port detection).

Render restarts the deploy when it discovers the HTTP port mid-boot
(\"New primary port detected\"). Binding here in the entrypoint means the
port is open from process start. Until Tomcat is up, serve a starting page;
afterward reverse-proxy to 127.0.0.1:internal.
"""

from __future__ import annotations

import http.client
import sys
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer

SKIP_REQ = {
    "connection",
    "content-length",
    "expect",
    "host",
    "keep-alive",
    "proxy-authenticate",
    "proxy-authorization",
    "te",
    "trailers",
    "transfer-encoding",
    "upgrade",
}
SKIP_RESP = SKIP_REQ | {"content-length"}

STARTING_HTML = """<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8"/>
  <meta http-equiv="refresh" content="5"/>
  <meta name="viewport" content="width=device-width, initial-scale=1"/>
  <title>Starting — Retail Stock Manager</title>
  <style>
    body { margin:0; font-family:"Segoe UI","Helvetica Neue",sans-serif;
      color:#1f2a24; background:linear-gradient(180deg,#ebe6db,#e7efe9); min-height:100vh; }
    main { max-width:40rem; margin:12vh auto; padding:2rem;
      background:#fffdf8; border:1px solid #d7d0c3; border-radius:12px; }
    h1 { color:#1f6f5b; }
    p { color:#5c6b63; line-height:1.5; }
  </style>
</head>
<body>
  <main>
    <h1>Retail Stock Manager is starting</h1>
    <p>The host already printed &ldquo;Your service is live&rdquo;, but Spring Boot
    is still booting on the free plan. This page refreshes every 5 seconds.
    First boot can take 1–2 minutes; after idle sleep, 30–60 seconds.</p>
  </main>
</body>
</html>
"""


def make_handler(internal_port: int):
    class Handler(BaseHTTPRequestHandler):
        protocol_version = "HTTP/1.1"

        def log_message(self, fmt, *args):  # quieter on free-tier logs
            sys.stderr.write("[early-proxy] " + (fmt % args) + "\n")

        def do_HEAD(self):
            # Render's port probe uses HEAD /. Answer 200 even before Tomcat.
            if self._proxy_or_none(include_body=False) is None:
                self.send_response(200)
                self.send_header("Content-Type", "text/html; charset=UTF-8")
                self.send_header("Content-Length", "0")
                self.send_header("Cache-Control", "no-store")
                self.end_headers()

        def do_GET(self):
            self._handle(True)

        def do_POST(self):
            self._handle(True)

        def do_PUT(self):
            self._handle(True)

        def do_DELETE(self):
            self._handle(True)

        def do_PATCH(self):
            self._handle(True)

        def do_OPTIONS(self):
            self._handle(True)

        def _handle(self, include_body: bool):
            if self._proxy_or_none(include_body=include_body) is None:
                self._starting(include_body)

        def _starting(self, include_body: bool):
            if self.path.startswith("/favicon.ico"):
                self.send_response(204)
                self.end_headers()
                return
            body = STARTING_HTML.encode("utf-8")
            self.send_response(200)
            self.send_header("Content-Type", "text/html; charset=UTF-8")
            self.send_header("Content-Length", str(len(body)))
            self.send_header("Cache-Control", "no-store")
            self.end_headers()
            if include_body:
                self.wfile.write(body)

        def _proxy_or_none(self, include_body: bool):
            length = int(self.headers.get("Content-Length") or 0)
            payload = self.rfile.read(length) if length > 0 else b""
            conn = http.client.HTTPConnection("127.0.0.1", internal_port, timeout=120)
            try:
                headers = {}
                for key, value in self.headers.items():
                    if key.lower() in SKIP_REQ:
                        continue
                    headers[key] = value
                if "X-Forwarded-Host" not in headers and self.headers.get("Host"):
                    headers["X-Forwarded-Host"] = self.headers["Host"]
                headers.setdefault("X-Forwarded-Proto", "http")
                if self.client_address:
                    headers.setdefault("X-Forwarded-For", self.client_address[0])
                conn.request(self.command, self.path, body=payload or None, headers=headers)
                resp = conn.getresponse()
                data = resp.read()
                self.send_response(resp.status)
                for key, value in resp.getheaders():
                    if key.lower() in SKIP_RESP:
                        continue
                    self.send_header(key, value)
                self.send_header("Content-Length", str(len(data)))
                self.end_headers()
                if include_body and data:
                    self.wfile.write(data)
                return True
            except OSError:
                return None
            finally:
                conn.close()

    return Handler


def main():
    if len(sys.argv) != 3:
        print("usage: early_proxy.py PUBLIC_PORT INTERNAL_PORT", file=sys.stderr)
        sys.exit(2)
    public_port = int(sys.argv[1])
    internal_port = int(sys.argv[2])
    server = ThreadingHTTPServer(("0.0.0.0", public_port), make_handler(internal_port))
    print(
        f"Entrypoint HTTP bind on 0.0.0.0:{public_port} "
        f"(Tomcat will listen on 127.0.0.1:{internal_port})",
        flush=True,
    )
    server.serve_forever()


if __name__ == "__main__":
    main()
