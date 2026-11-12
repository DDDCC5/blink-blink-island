"""Package local review assets and probe source, excluding machine-specific caches."""
from pathlib import Path
import zipfile,hashlib,json,shutil
root=Path(__file__).resolve().parents[1]; dest=root/'deliverables'; dest.mkdir(exist_ok=True)
apk=root/'probes/spatial/app/build/outputs/apk/debug/app-debug.apk'
shutil.copy2(apk,dest/'BlinkCapabilityProbe-sdk6.1.9-debug.apk')
archive=dest/'Blink_Blink_Island_Review_v0.1.zip'
with zipfile.ZipFile(archive,'w',zipfile.ZIP_DEFLATED) as z:
 for folder in ['handoff','review','probes/spatial','scripts','reports']:
  for p in (root/folder).rglob('*'):
   if not p.is_file():continue
   parts=p.relative_to(root).parts
   if any(x in ('.gradle','build','builds','captures','.kotlin','__pycache__') for x in parts) or p.name=='local.properties':continue
   if p.name=='pico-mcp.mjs':continue
   z.write(p,p.relative_to(root))
 z.write(root/'README.md','README.md')
rows=[]
for p in [archive,dest/'BlinkCapabilityProbe-sdk6.1.9-debug.apk']:
 rows.append({'path':str(p.relative_to(root)),'bytes':p.stat().st_size,'sha256':hashlib.sha256(p.read_bytes()).hexdigest()})
(dest/'SHA256.json').write_text(json.dumps(rows,indent=2));print(json.dumps(rows,indent=2))
