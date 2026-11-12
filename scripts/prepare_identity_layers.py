"""Split the supplied SVG into editable PICO background/foreground; render with macOS sips."""
from pathlib import Path
import copy
import shutil
import subprocess
import xml.etree.ElementTree as E

root = Path(__file__).resolve().parents[1]
out = root / 'review/identity'
out.mkdir(parents=True, exist_ok=True)
ns = 'http://www.w3.org/2000/svg'
E.register_namespace('', ns)
source = E.parse(root / 'handoff/assets/brand/app_icon.svg').getroot()
for kind in ('background', 'foreground'):
    svg = E.Element(f'{{{ns}}}svg', width='1024', height='1024', viewBox='0 0 512 512')
    if kind == 'background':
        E.SubElement(svg, f'{{{ns}}}circle', cx='256', cy='256', r='256', fill='#D7EDE4')
        E.SubElement(svg, f'{{{ns}}}circle', cx='256', cy='256', r='175', fill='#F6F5E9')
    else:
        for node in list(source):
            if node.tag.rsplit('}', 1)[-1] not in ('path', 'g'):
                continue
            child = copy.deepcopy(node)
            for item in child.iter():
                item.attrib.pop('filter', None)
                item.attrib.pop('opacity', None)
            svg.append(child)
    path = out / f'icon_{kind}.svg'
    E.ElementTree(svg).write(path, encoding='utf-8', xml_declaration=True)
    subprocess.run(['sips', '-s', 'format', 'png', str(path), '--out', str(path.with_suffix('.png'))], check=True)
    shutil.copy2(path.with_suffix('.png'), out / ('bg.png' if kind == 'background' else 'fg.png'))
print(out)
