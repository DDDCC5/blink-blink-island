"""Loopback-only review server; explicit gallery exports save to a fixed allowlist."""
from http.server import ThreadingHTTPServer, SimpleHTTPRequestHandler
from pathlib import Path
import json

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'review/tripo-storybook-2026-11-27/exports'
ALLOWED = {'island-storybook.glb', 'butterfly-storybook.glb',
           'island-storybook.png', 'butterfly-storybook.png'}

class Handler(SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=str(ROOT), **kwargs)

    def do_POST(self):
        name = self.path.removeprefix('/export/')
        size = int(self.headers.get('Content-Length', '0'))
        if not self.path.startswith('/export/') or name not in ALLOWED or not 0 < size < 64 * 1024 * 1024:
            self.send_error(400)
            return
        data = self.rfile.read(size)
        if len(data) != size or not (data.startswith(b'glTF') if name.endswith('.glb') else data.startswith(b'\x89PNG\r\n\x1a\n')):
            self.send_error(400)
            return
        OUT.mkdir(parents=True, exist_ok=True)
        path = OUT / name
        path.write_bytes(data)
        response = json.dumps({'saved': path.relative_to(ROOT).as_posix(), 'bytes': size}).encode()
        self.send_response(200)
        self.send_header('Content-Type', 'application/json')
        self.send_header('Content-Length', str(len(response)))
        self.end_headers()
        self.wfile.write(response)

if __name__ == '__main__':
    ThreadingHTTPServer(('127.0.0.1', 8769), Handler).serve_forever()
