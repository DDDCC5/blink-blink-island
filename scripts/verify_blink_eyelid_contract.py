"""Read-only audit of the project's exact cached official public eye-data contract.
This checks API availability, never eyelid state or runtime device behavior.
Usage: python3 scripts/verify_blink_eyelid_contract.py
"""
from pathlib import Path
from zipfile import ZipFile
import hashlib, json, re

root = Path(__file__).resolve().parents[1]
catalog = (root / 'native/gradle/libs.versions.toml').read_text()
version = re.search(r'spatialBom\s*=\s*"([^"]+)"', catalog).group(1)
cache = Path.home() / '.gradle/caches/modules-2/files-2.1/com.pico.spatial.tracking/tracking' / version
sources = sorted(cache.rglob('*-sources.jar'))
if not sources:
    raise SystemExit('Exact-version official sources are not cached; availability remains unverified.')
jar = sources[0]
with ZipFile(jar) as archive:
    text = archive.read('com/pico/spatial/tracking/eye/EyeTrackingData.kt').decode()
public_fields = re.findall(r'public val (\w+):\s*([\w?]+)', text)
report = {
    'sdk': version,
    'sourceJar': str(jar),
    'sourceJarSha256': hashlib.sha256(jar.read_bytes()).hexdigest(),
    'publicFieldsInEyeDataAndPose': dict(public_fields),
    'verifiedEyelidBindingInApp': False,
    'conclusion': 'Current public EyeTrackingData/EyePose provide gaze pose and timestamp, not a verified eyelid source.',
    'scope': 'Static public API audit only. Does not prove hardware lacks eyelid sensing.',
}
print(json.dumps(report, ensure_ascii=False, indent=2))
