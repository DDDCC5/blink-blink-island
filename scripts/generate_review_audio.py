"""Review-only narration with macOS voices and 96-second mixes from supplied stems.
No external service, voice cloning, or new package installation. Distribution rights
for system voices must be cleared separately before release.
"""
from pathlib import Path
import subprocess, wave, array, math, json, hashlib, sys
root=Path(__file__).resolve().parents[1]
out=root/'review/audio'; (out/'voice').mkdir(parents=True,exist_ok=True)
rows=[]
for line in (root/'handoff/content/VOICE_SCRIPT.md').read_text().splitlines():
    cells=[x.strip() for x in line.strip().strip('|').split('|')]
    if not line.startswith('|') or len(cells)!=3 or cells[0] in ('id','---'): continue
    key,zh,en=cells
    for lang,text,voice,rate in [('zh',zh,'Tingting',155),('en',en,'Samantha',145)]:
        target=out/'voice'/f'voice_{lang}_{key}.wav'
        if not target.exists(): subprocess.run(['say','-v',voice,'-r',str(rate),'--file-format=WAVE','--data-format=LEI16@24000','-o',str(target),text],check=True)
        with wave.open(str(target)) as w: duration=w.getnframes()/w.getframerate()
        rows.append({'id':key,'language':lang,'text':text,'voice':voice,'path':str(target.relative_to(root/'review')),'durationSeconds':round(duration,3),'releaseLicenseStatus':'PENDING: review draft only','sha256':hashlib.sha256(target.read_bytes()).hexdigest()})
for name,target_db in [('bgm_island_loop',-28),('amb_wind_loop',-35),('amb_water_loop',-33)]:
    with wave.open(str(root/'handoff/assets/audio'/f'{name}.wav')) as w:
        rate=w.getframerate(); values=array.array('h',w.readframes(w.getnframes()))
    if sys.byteorder!='little': values.byteswap()
    n=len(values); cross=rate; hop=n-cross; length=96*rate
    mixed=array.array('f',[0.0])*length
    # Overlap-add with complementary linear crossfades.
    for start in range(-hop,length,hop):
        for i,v in enumerate(values):
            k=start+i
            if 0<=k<length:
                gain=min(1.0,i/cross,(n-1-i)/cross)
                mixed[k]+=v/32768*max(0,gain)
    rms=math.sqrt(sum(v*v for v in mixed)/length)
    peak=max(abs(v) for v in mixed)
    scale=min(10**(target_db/20)/max(rms,1e-9),10**(-9/20)/max(peak,1e-9))
    result=array.array('h',(round(max(-1,min(1,v*scale))*32767) for v in mixed))
    if sys.byteorder!='little': result.byteswap()
    target=out/f'{name}_96s_review.wav'
    with wave.open(str(target),'wb') as w: w.setparams((1,2,rate,0,'NONE','not compressed'));w.writeframes(result.tobytes())
    rows.append({'id':name,'path':str(target.relative_to(root/'review')),'durationSeconds':96,'rmsDbfs':round(20*math.log10(max(rms*scale,1e-12)),2),'peakDbfs':round(20*math.log10(max(peak*scale,1e-12)),2),'status':'review mix; headset loudness and loop seam pending'})
(out/'manifest.json').write_text(json.dumps({'status':'DESIGN REVIEW ONLY','voiceSource':'macOS built-in Tingting / Samantha; commercial distribution clearance pending','assets':rows},ensure_ascii=False,indent=2))
print('Generated',len(rows),'review audio assets')
