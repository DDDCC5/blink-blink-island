"""Structural artifact verification; not a native SDK conformance/performance test."""
from pathlib import Path
import json,struct,wave,hashlib,re,subprocess,math
root=Path(__file__).resolve().parents[1];results=[]
for path in sorted((root/'review/3d').glob('*.glb')):
 b=path.read_bytes(); magic,version,total=struct.unpack_from('<III',b)
 assert magic==0x46546c67 and version==2 and total==len(b),path
 n,kind=struct.unpack_from('<I4s',b,12);assert kind==b'JSON'
 j=json.loads(b[20:20+n]);off=20+n;bn,bkind=struct.unpack_from('<I4s',b,off);assert bkind==b'BIN\0';binary=b[off+8:off+8+bn]
 assert len(binary)>=j['buffers'][0]['byteLength']
 def read(a):
  spec=j['accessors'][a];v=j['bufferViews'][spec['bufferView']];count=spec['count'];width={'SCALAR':1,'VEC3':3,'VEC4':4}[spec['type']];fmt={5123:'H',5126:'f'}[spec['componentType']];size=struct.calcsize(fmt)*count*width;start=v.get('byteOffset',0)+spec.get('byteOffset',0);assert size<=v['byteLength'];return struct.unpack_from('<'+fmt*count*width,binary,start)
 for mesh in j['meshes']:
  for primitive in mesh['primitives']:
   idx=read(primitive['indices']);p=j['accessors'][primitive['attributes']['POSITION']];assert len(idx)%3==0 and min(idx)>=0 and max(idx)<p['count']
   assert all(math.isfinite(x) for x in read(primitive['attributes']['POSITION']))
 for node in j['nodes']:
  assert all(0<=i<len(j['nodes']) for i in node.get('children',[]))
 for animation in j['animations']:
  for sampler in animation['samplers']:
   times=read(sampler['input']);assert all(a<b for a,b in zip(times,times[1:]));assert len(times)==j['accessors'][sampler['output']]['count']
   quats=read(sampler['output']);assert all(abs(sum(x*x for x in quats[i:i+4])-1)<1e-5 for i in range(0,len(quats),4))
  assert len(animation['channels'])==2
 results.append({'asset':str(path.relative_to(root)),'result':'structural checks passed','bytes':len(b)})
voice_count=0
for path in sorted((root/'review/audio').rglob('*.wav')):
 with wave.open(str(path)) as w:
  assert w.getnchannels()==1 and w.getsampwidth()==2 and w.getnframes()>0
  if '/voice/' in str(path):voice_count+=1;assert w.getframerate()==24000
  else:assert w.getnframes()/w.getframerate()==96
assert voice_count==30
results.append({'audio':'30 voice clips + 3 96-second stems','result':'PCM headers and durations passed'})
for rel in ['review/prototype/index.html','review/index.html']:
 s=(root/rel).read_text()
 for script in re.findall(r'<script(?:\s[^>]*)?>(.*?)</script>',s,re.S):
  if script.lstrip().startswith('{'):json.loads(script);continue
  p=subprocess.run(['node','--input-type=module','--check'],input=script,text=True,capture_output=True);assert p.returncode==0,(rel,p.stderr)
 results.append({'script':rel,'result':'JavaScript syntax passed'})
manifest=json.loads((root/'reports/handoff-inventory.json').read_text())
for entry in manifest:assert hashlib.sha256((root/entry['path']).read_bytes()).hexdigest()==entry['sha256']
results.append({'handoff':len(manifest),'result':'original extracted files unchanged'})
report={'checks':results,'limits':['Not the Khronos glTF validator','No Spatial SDK import or native animation proof','No device comfort, audio calibration or sensor verdict']}
(root/'reports/artifact-verification.json').write_text(json.dumps(report,ensure_ascii=False,indent=2));print(json.dumps(report,ensure_ascii=False,indent=2))
