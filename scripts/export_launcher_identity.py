"""Export approved SVG artwork; retain the PICO Configurator's existing spatial silhouette/SDF."""
from pathlib import Path
import hashlib
import xml.etree.ElementTree as E
import resvg_py

root = Path(__file__).resolve().parents[1]
identity = root / 'review/identity'
res = root / 'native/app/src/main/res'
ns = 'http://www.w3.org/2000/svg'
E.register_namespace('', ns)
masks = {p: hashlib.sha256(p.read_bytes()).hexdigest() for p in (res / 'drawable').glob('icon_3d_sdf_*.png')}
# Full-square opaque compatibility artwork; keep its approved geometry, without pre-rounded corners.
svg = (identity / 'app_icon_square_green.svg').read_text()
image = resvg_py.svg_to_bytes(svg_string=svg)
(identity / 'app_icon_square_green.png').write_bytes(image)
(res / 'mipmap-xxxhdpi/ic_spatial_launcher.png').write_bytes(image)
# Spatial texture fill changes, but the official circular depth silhouette remains system-owned.
background = E.parse(identity / 'icon_background.svg').getroot()
first = list(background)[0]
assert first.tag.rsplit('}', 1)[-1] in ('circle', 'rect')
background.remove(first)
background.insert(0, E.Element(f'{{{ns}}}rect', x='0', y='0', width='512', height='512', fill='#D7EDE4'))
text = E.tostring(background, encoding='unicode')
(identity / 'icon_background.svg').write_text(text)
image = resvg_py.svg_to_bytes(svg_string=text)
for path in [identity / 'icon_background.png', identity / 'bg.png', res / 'drawable/icon_3d_layer_0.png']:
    path.write_bytes(image)
for path, before in masks.items():
    assert hashlib.sha256(path.read_bytes()).hexdigest() == before
print('Updated compatibility icon and spatial background; official SDF masks unchanged')
