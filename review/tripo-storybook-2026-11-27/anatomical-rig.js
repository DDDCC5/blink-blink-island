import * as THREE from '../../.scratch/tripo-tools/node_modules/three/build/three.module.js';

// Preserve source topology and UVs. Repair the generated head relief locally,
// then add smooth wing weights. The original GLB remains untouched.
export function rigAnatomicalButterfly(source){
  source.rotation.y=Math.PI/2;
  const bounds=new THREE.Box3().setFromObject(source),size=bounds.getSize(new THREE.Vector3()),center=bounds.getCenter(new THREE.Vector3());
  const normalized=new THREE.Group();normalized.add(source);source.position.sub(center);normalized.scale.setScalar(.7/Math.max(size.x,size.y,size.z));normalized.updateMatrixWorld(true);
  const root=new THREE.Group();root.name='Butterfly';
  const body=new THREE.Bone();body.name='BodyRoot';root.add(body);
  const bones=[body],pivots=[];
  for(const sign of [-1,1]){
    const frame=new THREE.Bone();frame.name=sign<0?'LeftHingeFrame':'RightHingeFrame';frame.position.x=sign*.010;frame.rotation.x=-Math.PI/2;
    const pivot=new THREE.Bone();pivot.name=sign<0?'LeftWingPivot':'RightWingPivot';frame.add(pivot);body.add(frame);const tip=new THREE.Bone();tip.name=sign<0?'LeftWingTip':'RightWingTip';tip.position.x=sign*.18;pivot.add(tip);bones.push(frame,pivot,tip);pivots.push({pivot,tip,sign});
  }
  const meshes=[];let vertices=0,fullyBody=0,fullyWing=0,headAdjusted=0,wingVerticesUnchanged=0;
  normalized.traverse(node=>{
    if(!node.isMesh)return;
    const geometry=node.geometry.clone().applyMatrix4(node.matrixWorld),positions=geometry.attributes.position,indices=[],weights=[],bodyProtection=[];
    // Tripo exaggerated the dorsal head into a snout. Keep the outer wings
    // exact while compressing the central head toward the existing wing plane.
    for(let i=0;i<positions.count;i++){
      const x=positions.getX(i),y=positions.getY(i),z=positions.getZ(i);
      const inner=.024+Math.max(0,y-.08)*.62,outer=inner+.014;
      const side=1-THREE.MathUtils.smoothstep(Math.abs(x),inner,outer);
      const w=side*THREE.MathUtils.smoothstep(y,-.020,.008);bodyProtection.push(w);
      if(w===0){wingVerticesUnchanged++;continue}
      const newY=y-Math.min(Math.max(y-.010,0),.080)*.50;
      const newZ=-.044+(z+.044)*.22;
      positions.setXYZ(i,x,THREE.MathUtils.lerp(y,newY,w),THREE.MathUtils.lerp(z,newZ,w));headAdjusted++;
    }
    positions.needsUpdate=true;geometry.computeVertexNormals();geometry.computeBoundingBox();geometry.computeBoundingSphere();
    for(let i=0;i<positions.count;i++){
      const x=positions.getX(i),y=positions.getY(i);
      // The upper central cone includes the two antennae. Wing leading edges
      // remain outside this cone; the abdomen stays on BodyRoot.
      const bodyHalfWidth=.012+Math.max(0,y-.025)*.62;
      const t=THREE.MathUtils.clamp((Math.abs(x)-bodyHalfWidth)/.018,0,1),w=t*t*(3-2*t)*(1-bodyProtection[i]);
      const flex=THREE.MathUtils.smoothstep(Math.abs(x),.15,.30)*.65;
      indices.push(0,x<0?2:5,x<0?3:6,0);weights.push(1-w,w*(1-flex),w*flex,0);vertices++;if(w===0)fullyBody++;if(w===1)fullyWing++;
    }
    geometry.setAttribute('skinIndex',new THREE.Uint16BufferAttribute(indices,4));geometry.setAttribute('skinWeight',new THREE.Float32BufferAttribute(weights,4));
    const mesh=new THREE.SkinnedMesh(geometry,node.material);mesh.name=node.name||'AnatomicalButterflyMesh';mesh.frustumCulled=false;root.add(mesh);meshes.push(mesh);
  });
  root.updateMatrixWorld(true);const skeleton=new THREE.Skeleton(bones);for(const mesh of meshes)mesh.bind(skeleton);
  const tracks=[];
  // Four slightly different beats; quick supporting downstroke, softer recovery.
  // Frequency is a visual design parameter, not a species measurement.
  const durations=[.18,.17,.19,.18],total=.72;
  function stroke(time){
    let phase=((time+.0414)%total+total)%total,beat=0;
    while(beat<3&&phase>=durations[beat])phase-=durations[beat++];
    phase/=durations[beat];
    const high=[79,76,81,78][beat],low=[-44,-46,-43,-45][beat];
    return THREE.MathUtils.degToRad(phase<.38
      ?THREE.MathUtils.lerp(high,low,(1-Math.cos(Math.PI*phase/.38))/2)
      :THREE.MathUtils.lerp(low,high,(1-Math.cos(Math.PI*(phase-.38)/.62))/2));
  }
  for(const {pivot,tip,sign} of pivots){
    const times=[],values=[],tipValues=[];
    for(let i=0;i<=144;i++){
      const time=total*i/144,angle=stroke(time);
      const q=new THREE.Quaternion().setFromAxisAngle(new THREE.Vector3(0,0,1),sign*angle);
      // Small passive wingtip lag, with feathering on recovery.
      const flex=THREE.MathUtils.clamp((stroke(time-.018)-angle)*.24,-.20,.20);
      const feather=.065*Math.sin(time*2*Math.PI/total*4);
      const tipQ=new THREE.Quaternion().setFromEuler(new THREE.Euler(feather,0,sign*flex));
      times.push(time);values.push(...q.toArray());tipValues.push(...tipQ.toArray());
    }
    tracks.push(new THREE.QuaternionKeyframeTrack(pivot.name+'.quaternion',times,values));
    tracks.push(new THREE.QuaternionKeyframeTrack(tip.name+'.quaternion',times,tipValues));
  }
  root.animations=[new THREE.AnimationClip('NaturalWingbeat',total,tracks)];
  root.userData.rigAudit={sourceVertices:vertices,fullyBodyVertices:fullyBody,fullyWingVertices:fullyWing,geometryClipped:false,headReliefRepaired:true,headAdjustedVertices:headAdjusted,unchangedVertices:wingVerticesUnchanged};
  return {root,pivots};
}
