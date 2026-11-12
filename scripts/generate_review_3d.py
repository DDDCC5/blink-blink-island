"""Original procedural GLB review assets; metres, right-handed Y-up.
Independent fallback deliverable after managed Editor cancellation; not an Editor
bundle or proof of native SDK import/performance. No third-party mesh sources.
"""
from pathlib import Path
import math,struct,json,hashlib
root=Path(__file__).resolve().parents[1]
class GLB:
 def __init__(self):
  self.data=bytearray(); self.doc={'asset':{'version':'2.0','generator':'Blink Blink Island procedural review authoring'},'scene':0,'scenes':[{'nodes':[]}],'nodes':[],'meshes':[],'materials':[],'accessors':[],'bufferViews':[],'buffers':[{}]};self.triangles=0
 def accessor(self,values,kind,component=5126,bounds=False):
  flat=[v for row in values for v in row] if kind!='SCALAR' else values
  fmt={5126:'f',5123:'H'}[component]
  while len(self.data)%4:self.data.append(0)
  offset=len(self.data);self.data.extend(struct.pack('<'+fmt*len(flat),*flat));view=len(self.doc['bufferViews']);self.doc['bufferViews'].append({'buffer':0,'byteOffset':offset,'byteLength':len(self.data)-offset})
  a={'bufferView':view,'componentType':component,'count':len(values),'type':kind}
  if bounds:
   n={'SCALAR':1,'VEC3':3,'VEC4':4}[kind];rows=[[x] for x in values] if n==1 else values;a['min']=[min(x[i] for x in rows) for i in range(n)];a['max']=[max(x[i] for x in rows) for i in range(n)]
  self.doc['accessors'].append(a);return len(self.doc['accessors'])-1
 def material(self,name,color):
  c=[int(color[i:i+2],16)/255 for i in (0,2,4)];c=[x/12.92 if x<=.04045 else ((x+.055)/1.055)**2.4 for x in c];self.doc['materials'].append({'name':name,'pbrMetallicRoughness':{'baseColorFactor':c+[1],'metallicFactor':0,'roughnessFactor':.88},'doubleSided':True});return len(self.doc['materials'])-1
 def mesh(self,name,verts,faces,mat):
  # Flat normals give the island a calm faceted silhouette.
  p=[];norm=[];indices=[]
  for f in faces:
   a,b,c=[verts[i] for i in f];u=[b[i]-a[i] for i in range(3)];v=[c[i]-a[i] for i in range(3)];n=[u[1]*v[2]-u[2]*v[1],u[2]*v[0]-u[0]*v[2],u[0]*v[1]-u[1]*v[0]];length=math.sqrt(sum(x*x for x in n)) or 1;n=[x/length for x in n]
   for point in (a,b,c):indices.append(len(p));p.append(point);norm.append(n)
  self.triangles+=len(faces);self.doc['meshes'].append({'name':name,'primitives':[{'attributes':{'POSITION':self.accessor(p,'VEC3',bounds=True),'NORMAL':self.accessor(norm,'VEC3')},'indices':self.accessor(indices,'SCALAR',5123),'material':mat}]});return len(self.doc['meshes'])-1
 def node(self,name,mesh=None,pos=(0,0,0),scale=(1,1,1),parent=None):
  n={'name':name,'translation':list(pos),'scale':list(scale)}
  if mesh is not None:n['mesh']=mesh
  self.doc['nodes'].append(n);i=len(self.doc['nodes'])-1
  if parent is None:self.doc['scenes'][0]['nodes'].append(i)
  else:self.doc['nodes'][parent].setdefault('children',[]).append(i)
  return i
 def save(self,path):
  self.doc['buffers'][0]['byteLength']=len(self.data);js=json.dumps(self.doc,separators=(',',':')).encode();js+=b' '*((-len(js))%4);binary=bytes(self.data)+b'\0'*((-len(self.data))%4)
  result=struct.pack('<III',0x46546c67,2,12+8+len(js)+8+len(binary))+struct.pack('<I4s',len(js),b'JSON')+js+struct.pack('<I4s',len(binary),b'BIN\0')+binary;path.write_bytes(result)
  return {'file':str(path.relative_to(root)),'bytes':len(result),'triangles':self.triangles,'nodes':len(self.doc['nodes']),'meshes':len(self.doc['meshes']),'animations':[a['name'] for a in self.doc.get('animations',[])],'sha256':hashlib.sha256(result).hexdigest()}
def sphere(seg=16,rings=8):
 v=[];f=[]
 for j in range(rings+1):
  t=math.pi*j/rings
  for i in range(seg):
   p=2*math.pi*i/seg;v.append((math.sin(t)*math.cos(p),math.cos(t),math.sin(t)*math.sin(p)))
 for j in range(rings):
  for i in range(seg):
   a=j*seg+i;b=j*seg+(i+1)%seg;c=(j+1)*seg+i;d=(j+1)*seg+(i+1)%seg;f.extend([(a,b,c),(b,d,c)])
 return v,f
def rock():
 v=[];f=[];n=20
 for y,r in [(.28,1.63),(.02,1.7),(-.38,1.25),(-.85,.65),(-1.04,.18)]:
  for i in range(n):
   a=i*math.tau/n;rr=r*(1+.045*math.sin(i*2.1));v.append((rr*math.cos(a),y,rr*.8*math.sin(a)))
 for j in range(4):
  for i in range(n):
   a=j*n+i;b=j*n+(i+1)%n;c=(j+1)*n+i;d=(j+1)*n+(i+1)%n;f.extend([(a,b,c),(b,d,c)])
 v.extend([(0,.28,0),(0,-1.04,0)])
 for i in range(n):f.extend([(len(v)-2,(i+1)%n,i),(len(v)-1,4*n+i,4*n+(i+1)%n)])
 return v,f
def wing(side):
 outline=[(0,0),(.15,-.43),(.47,-.52),(.68,-.36),(.62,-.10),(.42,.05),(.56,.28),(.43,.46),(.20,.43),(.04,.18)]
 v=[(side*x,y,z) for y in (-.012,.012) for x,z in outline];f=[];n=len(outline)
 # Fan triangles with small positive thickness; independent hinge at origin.
 for i in range(1,n-1):f.extend([(0,i+1,i),(n,n+i,n+i+1)])
 for i in range(n):k=(i+1)%n;f.extend([(i,k,n+i),(k,n+k,n+i)])
 return v,f
def butterfly(g,parent=None,pos=(0,0,0),scale=(1,1,1)):
 peach=g.material('PeachWing','F6C9A6');cream=g.material('CreamWingAccent','F6F5E9');forest=g.material('ForestBody','398E86');sv,sf=sphere(12,6)
 flight=g.node('ButterflyFlightRoot',pos=pos,scale=scale,parent=parent)
 g.node('ButterflyBody',g.mesh('BodyMesh',sv,sf,forest),scale=(.045,.055,.25),parent=flight)
 left=g.node('LeftWingPivot',parent=flight);right=g.node('RightWingPivot',parent=flight)
 for sign,pivot in [(1,left),(-1,right)]:
  v,f=wing(sign);g.node('LeftWing' if sign==1 else 'RightWing',g.mesh('WingMesh',v,f,peach),parent=pivot)
  g.node('WingAccent',g.mesh('AccentMesh',sv,sf,cream),pos=(sign*.36,.021,-.23),scale=(.16,.015,.17),parent=pivot)
 g.node('ButterflyHead',g.mesh('HeadMesh',sv,sf,forest),pos=(0,0,-.27),scale=(.075,.065,.075),parent=flight)
 times=[i/8 for i in range(17)];time=g.accessor(times,'SCALAR',bounds=True);samplers=[];channels=[]
 for sign,pivot in [(1,left),(-1,right)]:
  angles=[sign*(.32+.45*math.sin(math.tau*t/2)) for t in times];q=[(0,0,math.sin(a/2),math.cos(a/2)) for a in angles]
  samplers.append({'input':time,'output':g.accessor(q,'VEC4'),'interpolation':'LINEAR'});channels.append({'sampler':len(samplers)-1,'target':{'node':pivot,'path':'rotation'}})
 g.doc['animations']=[{'name':'SlowWingFlap','samplers':samplers,'channels':channels}]
 return flight
out=root/'review/3d';out.mkdir(parents=True,exist_ok=True);reports=[]
g=GLB();butterfly(g);reports.append(g.save(out/'butterfly.glb'))
g=GLB();stone=g.material('IslandEarth','82A79B');mint=g.material('MintGrass','D7ECE1');water=g.material('Water','83D0D2');wood=g.material('TreeWood','B5A48B');forest=g.material('ForestLeaves','398E86');light=g.material('LightLeaves','A7CDB7');cream=g.material('CreamStone','F6F5E9')
v,f=rock();g.node('IslandBase',g.mesh('FloatingRock',v,f,stone))
v,f=sphere();spheres={}
for mat in [mint,water,wood,forest,light,cream]:spheres[mat]=g.mesh('Ellipsoid',v,f,mat)
g.node('GrassTop',spheres[mint],pos=(0,.27,0),scale=(1.65,.18,1.31))
g.node('CalmWater',spheres[water],pos=(.52,.415,.12),scale=(.79,.025,.53))
for i,(x,z,h) in enumerate([(-.92,-.45,.9),(-.22,-.85,.7),(1.03,-.56,.6)]):
 t=g.node('Tree'+str(i),pos=(x,.38,z));g.node('Trunk',spheres[wood],pos=(0,h*.32,0),scale=(.07,h*.4,.07),parent=t);g.node('Canopy',spheres[forest if i!=1 else light],pos=(0,h*.77,0),scale=(h*.39,h*.55,h*.34),parent=t)
for i,(x,z) in enumerate([(-1.12,.45),(-.85,.66),(-.53,.81)]):g.node('RestStone'+str(i),spheres[cream],pos=(x,.43,z),scale=(.17,.07,.13))
butterfly(g,pos=(.15,1.38,.05),scale=(.65,.65,.65));reports.append(g.save(out/'island-scene.glb'))
(out/'manifest.json').write_text(json.dumps({'status':'DESIGN REVIEW: native import, comfort and performance unverified','source':'scripts/generate_review_3d.py; original procedural meshes','license':'Original project-authored geometry; no imported third-party models','coordinates':'metres, right-handed, Y up','animation':'SlowWingFlap: 2 seconds, independent hinge quaternion tracks; preview loops clip','editorStatus':'Managed editor runs cancelled before model production; these independent GLBs are not an accepted Editor bundle','assets':reports},ensure_ascii=False,indent=2))
print(json.dumps(reports,indent=2))
