#!/usr/bin/env python3
"""Bind the public PORT before the JVM starts (Render port detection).

Until Spring Boot publishes /internal/ready, every public request gets the
starting page immediately (no wait on Tomcat). A background probe flips to
reverse-proxy mode only after ready returns 200.
"""

from __future__ import annotations

import http.client
import socket
import sys
import threading
import time
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer

CONNECT_TIMEOUT_SEC = 0.25
READY_TIMEOUT_SEC = 1.0
PROXY_TIMEOUT_SEC = 60.0
READY_POLL_SEC = 1.0
MAX_CONCURRENT = 16
MAX_REQUEST_BYTES = 2 * 1024 * 1024
MAX_RESPONSE_BYTES = 5 * 1024 * 1024

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
  <title>Starting - Retail Stock Manager</title>
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
    <p>The host already printed "Your service is live", but Spring Boot
    is still booting on the free plan. This page refreshes every 5 seconds.
    First boot can take 1-2 minutes; after idle sleep, 30-60 seconds.</p>
  </main>
</body>
</html>
"""


def probe_ready(internal_port: int) -> bool:
    try:
        with socket.create_connection(("127.0.0.1", internal_port), CONNECT_TIMEOUT_SEC):
            pass
    except OSError:
        return False
    conn = http.client.HTTPConnection("127.0.0.1", internal_port, timeout=READY_TIMEOUT_SEC)
    try:
        conn.request("GET", "/internal/ready", headers={"Connection": "close"})
        resp = conn.getresponse()
        body = resp.read(64)
        return resp.status == 200 and body.strip() == b"ok"
    except Exception:
        return False
    finally:
        try:
            conn.close()
        except Exception:
            pass


def readiness_loop(internal_port: int, state: dict):
    try:
        while not state["warm"]:
            if probe_ready(internal_port):
                state["warm"] = True
                sys.stderr.write("[early-proxy] backend ready - forwarding traffic to Tomcat\n")
                sys.stderr.flush()
                return
            time.sleep(READY_POLL_SEC)
    finally:
        with state["lock"]:
            state["probing"] = False


def ensure_readiness_probe(internal_port: int, state: dict):
    with state["lock"]:
        if state["warm"] or state["probing"]:
            return
        state["probing"] = True
    threading.Thread(
        target=readiness_loop,
        args=(internal_port, state),
        name="ready-probe",
        daemon=True,
    ).start()


def read_exact(stream, size: int) -> bytes:
    """Read exactly size bytes. Do not read size+1 — on keep-alive sockets
    that blocks forever waiting for a byte that is not part of the body."""
    if size <= 0:
        return b""
    chunks = []
    remaining = size
    while remaining > 0:
        chunk = stream.read(remaining)
        if not chunk:
            break
        chunks.append(chunk)
        remaining -= len(chunk)
    return b"".join(chunks)


def read_response_limited(resp, max_bytes: int) -> bytes:
    length_header = resp.getheader("Content-Length") if hasattr(resp, "getheader") else None
    if length_header is None and hasattr(resp, "headers"):
        length_header = resp.headers.get("Content-Length")
    if length_header is not None:
        try:
            declared = int(length_header)
        except ValueError:
            declared = -1
        if declared >= 0:
            if declared > max_bytes:
                raise ValueError("body too large")
            return read_exact(resp, declared)
    data = resp.read(max_bytes + 1)
    if data is None:
        return b""
    if len(data) > max_bytes:
        raise ValueError("body too large")
    return data


def make_handler(internal_port: int, state: dict):
    gate = threading.Semaphore(MAX_CONCURRENT)

    class Handler(BaseHTTPRequestHandler):
        protocol_version = "HTTP/1.0"
        close_connection = True

        def log_message(self, fmt, *args):
            sys.stderr.write("[early-proxy] " + (fmt % args) + "\n")
            sys.stderr.flush()

        def handle(self):
            if not gate.acquire(blocking=False):
                try:
                    self._send(
                        503,
                        b"<!DOCTYPE html><html><body><h1>Busy</h1><p>Try again.</p></body></html>",
                        "text/html; charset=UTF-8",
                        True,
                    )
                except Exception:
                    pass
                return
            try:
                super().handle()
            finally:
                gate.release()

        def do_HEAD(self):
            if not state["warm"]:
                self._send(200, b"", "text/html; charset=UTF-8", include_body=False)
                return
            if self._proxy_or_none(include_body=False) is None:
                self._send(200, b"", "text/html; charset=UTF-8", include_body=False)

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
            if not state["warm"]:
                self._starting(include_body)
                return
            if self._proxy_or_none(include_body=include_body) is None:
                if state["warm"]:
                    body = (
                        "<!DOCTYPE html><html><body><h1>Temporary timeout</h1>"
                        "<p>The shop is up - refresh this page.</p></body></html>"
                    ).encode("utf-8")
                    self._send(504, body, "text/html; charset=UTF-8", include_body)
                else:
                    self._starting(include_body)

        def _starting(self, include_body: bool):
            if self.path.startswith("/favicon.ico"):
                self._send(204, b"", "text/plain", include_body=False)
                return
            self._send(200, STARTING_HTML.encode("utf-8"), "text/html; charset=UTF-8", include_body)

        def _send(self, status: int, body: bytes, content_type: str, include_body: bool):
            self.send_response(status)
            self.send_header("Content-Type", content_type)
            self.send_header("Content-Length", str(len(body)))
            self.send_header("Cache-Control", "no-store")
            self.send_header("Connection", "close")
            self.end_headers()
            if include_body and body:
                self.wfile.write(body)
            try:
                self.wfile.flush()
            except OSError:
                pass

        def _proxy_or_none(self, include_body: bool):
            try:
                length = int(self.headers.get("Content-Length") or 0)
            except ValueError:
                length = 0
            if length > MAX_REQUEST_BYTES:
                self._send(413, b"Request too large", "text/plain; charset=UTF-8", True)
                return True
            payload = read_exact(self.rfile, length)
            conn = http.client.HTTPConnection("127.0.0.1", internal_port, timeout=PROXY_TIMEOUT_SEC)
            try:
                headers = {"Connection": "close"}
                for key, value in self.headers.items():
                    if key.lower() in SKIP_REQ:
                        continue
                    headers[key] = value
                if "X-Forwarded-Host" not in headers and self.headers.get("Host"):
                    headers["X-Forwarded-Host"] = self.headers["Host"]
                headers.setdefault("X-Forwarded-Proto", "https")
                if self.client_address:
                    headers.setdefault("X-Forwarded-For", self.client_address[0])
                conn.request(self.command, self.path, body=payload or None, headers=headers)
                resp = conn.getresponse()
                data = read_response_limited(resp, MAX_RESPONSE_BYTES)
                self.send_response(resp.status)
                for key, value in resp.getheaders():
                    if key.lower() in SKIP_RESP:
                        continue
                    self.send_header(key, value)
                self.send_header("Content-Length", str(len(data)))
                self.send_header("Connection", "close")
                self.end_headers()
                if include_body and data:
                    self.wfile.write(data)
                try:
                    self.wfile.flush()
                except OSError:
                    pass
                return True
            except TimeoutError as ex:
                sys.stderr.write(f"[early-proxy] proxy timeout: {ex!r}\n")
                sys.stderr.flush()
                return None
            except OSError as ex:
                sys.stderr.write(f"[early-proxy] proxy error: {ex!r}\n")
                sys.stderr.flush()
                state["warm"] = False
                ensure_readiness_probe(internal_port, state)
                return None
            except ValueError as ex:
                sys.stderr.write(f"[early-proxy] proxy error: {ex!r}\n")
                sys.stderr.flush()
                self._send(502, b"Upstream response too large", "text/plain; charset=UTF-8", True)
                return True
            except Exception as ex:
                sys.stderr.write(f"[early-proxy] proxy error: {ex!r}\n")
                sys.stderr.flush()
                return None
            finally:
                try:
                    conn.close()
                except Exception:
                    pass

    return Handler


def main():
    if len(sys.argv) != 3:
        print("usage: early_proxy.py PUBLIC_PORT INTERNAL_PORT", file=sys.stderr)
        sys.exit(2)
    public_port = int(sys.argv[1])
    internal_port = int(sys.argv[2])
    state = {"warm": False, "probing": False, "lock": threading.Lock()}
    ensure_readiness_probe(internal_port, state)
    server = ThreadingHTTPServer(("0.0.0.0", public_port), make_handler(internal_port, state))
    print(
        f"Entrypoint HTTP bind on 0.0.0.0:{public_port} "
        f"(Tomcat will listen on 127.0.0.1:{internal_port})",
        flush=True,
    )
    server.serve_forever()


if __name__ == "__main__":
    main()
