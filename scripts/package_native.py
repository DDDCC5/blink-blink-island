"""Reproducible source inventory and debug APK handoff; exclude local caches/secrets."""
from pathlib import Path
import hashlib
import json
import shutil
import zipfile

root = Path(__file__).resolve().parents[1]
dest = root / 'deliverables'
dest.mkdir(exist_ok=True)
apk = dest / 'BlinkBlinkIsland-native-dev-debug.apk'
shutil.copy2(root / 'native/app/build/outputs/apk/debug/app-debug.apk', apk)
archive = dest / 'Blink_Blink_Island_Native_Dev_Source.zip'
excluded = {'.gradle', '.kotlin', '.scratch', '.idea', 'build', 'builds', '__pycache__', '.git', 'captures'}
files = [root / 'README.md', root / 'docs/superpowers/plans/2026-11-11-native-app.md']
for folder in ['native', 'handoff', 'content', 'review', 'reports', 'scripts']:
    for path in (root / folder).rglob('*'):
        if not path.is_file() or any(part in excluded for part in path.relative_to(root).parts):
            continue
        if folder == 'reports' and path.suffix != '.md' and path.name != 'DEVICE_OBSERVATIONS.json':
            continue
        if path.name in {'local.properties', 'pico-mcp.mjs'} or path.suffix in {'.apk', '.keystore', '.jks'}:
            continue
        files.append(path)
files.extend(path for path in (root / 'docs/superpowers').rglob('*.md') if path not in files)
inventory = []
with zipfile.ZipFile(archive, 'w', zipfile.ZIP_DEFLATED) as z:
    for path in sorted(files):
        relative = path.relative_to(root).as_posix()
        z.write(path, relative)
        inventory.append({'path': relative, 'sha256': hashlib.sha256(path.read_bytes()).hexdigest()})
    z.writestr('SOURCE_INVENTORY.json', json.dumps(inventory, ensure_ascii=False, indent=2))
with zipfile.ZipFile(archive) as z:
    assert z.testzip() is None
    assert 'native/app/src/main/java/com/example/blinkblinkisland/dev/Main.kt' in z.namelist()
    assert not any(name.endswith('local.properties') for name in z.namelist())
rows = [{'path': path.name, 'bytes': path.stat().st_size, 'sha256': hashlib.sha256(path.read_bytes()).hexdigest()} for path in [apk, archive]]
(dest / 'NATIVE_SHA256.json').write_text(json.dumps(rows, ensure_ascii=False, indent=2))
print(json.dumps(rows, ensure_ascii=False, indent=2))
