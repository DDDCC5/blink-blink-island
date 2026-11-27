"""Read local generated GLBs and produce a sanitized asset-review index."""
from pathlib import Path
import json,struct,hashlib
ROOT=Path(__file__).resolve().parents[1]
BASE=ROOT/'review/tripo-storybook-2026-11-27'
NAMES={'IslandBase':'漂浮岛屿','CanopyTree':'圆冠树','FlowerPatch':'花草簇','PebbleCluster':'圆润石簇','ButterflyBody':'旧蝴蝶身体（停用）','ButterflyWing':'旧蝴蝶翅膀（停用）','ButterflyAnatomical':'完整蓝闪蝶'}
rows=[]
items=json.loads((BASE/'generation-input.json').read_text())['items']
if list((BASE/'models/ButterflyAnatomical').rglob('model.glb')):
 items.append({'id':'ButterflyAnatomical','instances':1})
for item in items:
 row={'id':item['id'],'name':NAMES[item['id']],'instances':item['instances'],'status':'pending'}
 files=list((BASE/'models'/item['id']).rglob('model.glb'))
 if files:
  p=files[0];b=p.read_bytes();length,kind=struct.unpack_from('<II',b,12)
  assert b[:4]==b'glTF' and kind==0x4e4f534a
  j=json.loads(b[20:20+length]);a=j.get('accessors',[])
  assert all('uri' not in x or x['uri'].startswith('data:') for x in j.get('buffers',[])+j.get('images',[]))
  faces=sum(a[q['indices']]['count']//3 if 'indices' in q else a[q['attributes']['POSITION']]['count']//3 for m in j.get('meshes',[]) for q in m['primitives'])
  preview=p.parent/'preview.png'
  row.update(status='generated',model=p.relative_to(BASE).as_posix(),preview=preview.relative_to(BASE).as_posix(),faces=faces,materials=len(j.get('materials',[])),textures=len(j.get('images',[])),sizeBytes=len(b),sha256=hashlib.sha256(b).hexdigest(),extensions=j.get('extensionsRequired',[]))
 rows.append(row)
(BASE/'gallery-index.json').write_text(json.dumps(rows,ensure_ascii=False,indent=2)+'\n')
print(json.dumps({'generated':sum(r['status']=='generated' for r in rows),'archetypes':len(rows),'instancedTriangles':sum(r.get('faces',0)*r['instances'] for r in rows)},ensure_ascii=False))
