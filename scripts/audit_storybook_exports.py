"""Audit exported self-contained GLBs, including instanced faces and wing pivots."""
from pathlib import Path
import hashlib
import json
import struct

BASE = Path(__file__).resolve().parents[1] / 'review/tripo-storybook-2026-11-27'

def inspect(path):
    raw = path.read_bytes()
    magic, version, length = struct.unpack_from('<4sII', raw)
    assert magic == b'glTF' and version == 2 and length == len(raw), path
    size, kind = struct.unpack_from('<II', raw, 12)
    assert kind == 0x4e4f534a
    doc = json.loads(raw[20:20 + size])
    assert all('uri' not in item for item in doc.get('buffers', []) + doc.get('images', []))
    accessors = doc['accessors']

    def faces(mesh):
        return sum(accessors[p['indices']]['count'] // 3 if 'indices' in p else
                   accessors[p['attributes']['POSITION']]['count'] // 3
                   for p in mesh['primitives'])

    def visit(index):
        node = doc['nodes'][index]
        return (faces(doc['meshes'][node['mesh']]) if 'mesh' in node else 0) + sum(
            visit(child) for child in node.get('children', []))

    pivots = [n for n in doc['nodes'] if n.get('name') in ('LeftWingPivot', 'RightWingPivot')]
    if pivots:
        assert len(pivots) == 2
        assert all(n.get('rotation', [0, 0, 0, 1]) == [0, 0, 0, 1] for n in pivots)
    return dict(file=path.relative_to(BASE).as_posix(), sha256=hashlib.sha256(raw).hexdigest(),
                sizeBytes=len(raw), uniqueTriangles=sum(faces(m) for m in doc['meshes']),
                instancedTriangles=sum(visit(i) for i in doc['scenes'][doc.get('scene', 0)]['nodes']),
                materials=len(doc.get('materials', [])), textures=len(doc.get('images', [])),
                externalDependencies=False, wingPivots=[n['name'] for n in pivots],
                skins=len(doc.get('skins', [])), animations=[a.get('name') for a in doc.get('animations', [])])

if __name__ == '__main__':
    results = [inspect(p) for p in sorted((BASE / 'exports').glob('*.glb'))]
    (BASE / 'exports/manifest.json').write_text(json.dumps(results, ensure_ascii=False, indent=2) + '\n')
    print(json.dumps(results, ensure_ascii=False))
