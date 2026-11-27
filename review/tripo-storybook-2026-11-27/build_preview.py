"""JSON-first design preview only. Does not build or alter the Android application."""
from pathlib import Path
import json, base64, mimetypes
import jsonschema

HERE = Path(__file__).resolve().parent
ROOT = HERE.parents[1]
SKILL = ROOT / '.agents/skills/pico-spatial-app-designer'
nodes = []
def node(id, kind='layout', **kw):
    nodes.append(dict(id=id, kind=kind, **kw)); return id
def text(id, zh, en='', style='body', color='forestInk', **kw):
    return node(id, 'text', content=zh, textStyle=dict(typography=style,color=color), props=dict(english=en or zh), **kw)
def layout(id, children, mode='column', gap=16, **kw):
    return node(id, children=children, layout=dict(mode=mode,gapDp=gap,**kw))
def media(id, asset, width=None, height=None):
    l=dict(mode='column')
    if width: l['widthDp']=width
    if height: l['heightDp']=height
    return node(id, 'media', assetId=asset, layout=l)
def button(id, zh, en, action=None, primary=False, width=None, height=80):
    label=text(id+'-label',zh,en,'button','paperWhite' if primary else 'forestInk')
    body=layout(id+'-content',[label],mode='row',gap=0,heightDp=height,align='center',justify='center')
    props={'ui':{'size':'max','container-color':'deepForest' if primary else 'transparent','content-color':'paperWhite' if primary else 'forestInk'},'contentInsetDp':24,'stretch':bool(width)}
    return node(id,'spatialui',component='sui-button',children=[body],events={'click-action':action} if action else {},props=props,layout=dict(mode='row',**({'widthDp':width} if width else {})))
def header(id, home=False):
    brand=layout(id+'-brand',[media(id+'-logo','brand',48,48),text(id+'-name','眨眨岛','Blink Blink Island','brand')],mode='row',align='center',grow=1)
    controls=[button(id+'-language','English','中文','language')] if home else [button(id+'-back','返回','Back','home')]
    return layout(id,[brand,*controls],mode='row',align='center',heightDp=80)
def foot(id): return text(id,'感到不适时，请停下来，摘下头显休息。','If uncomfortable, stop and remove the headset.','note','mutedInk')
def root(id, children): return layout(id,children,gap=16,paddingDp=32,heightDp=1120)

# Existing accepted fixed panel; prototype data is illustrative, not sensor evidence.
state_roots={}
def spacer(id):return layout(id,[],grow=1)
def page(id,title,en,children,pre=False):
    return root(id,[header(id+'-header',pre),text(id+'-title',title,en,'headline'),*children,foot(id+'-foot')])
def note(id,zh,en):return text(id,zh,en,'note','mutedInk')
def row(id,children,**kw):return layout(id,children,mode='row',gap=16,align='center',**kw)
def mode(id,asset,zh,en,sub,en_sub,badge,action):
    copy=layout(id+'-copy',[text(id+'-name',zh,en,'title'),note(id+'-sub',sub,en_sub)],gap=4,grow=1)
    line=row(id+'-row',[media(id+'-art',asset,76,76),copy,note(id+'-badge',badge,{'10轮':'10 rounds','4组':'4 paths'}.get(badge,badge))],heightDp=88)
    nodes[-1].setdefault('props',{})['slot']='headline'
    return node(id,'spatialui',component='sui-list-item',children=[line],props={'ui':{'container-color':'mintWash','headline-color':'forestInk'}},events={'click-action':action},layout={'mode':'row'})
hero_copy=layout('home-copy',[note('home-eyebrow','给自己一点安静','A quiet moment for yourself'),text('home-title','来岛上\n歇一会儿。','A little island.\nA gentle pause.','hero'),note('home-sub','不赶时间，跟着自己的节奏。','No rush. Find your own pace.')],gap=8,grow=1)
hero=row('home-hero',[hero_copy,media('home-hero-art','hero',420,220)],heightDp=240)
starts=row('home-starts',[button('quick','快速休息 · 约2–3分','Quick · ~2–3 min','setup-quick',True,510),button('full','完整休息','Full session','setup-full',False,510)])
modes=layout('home-modes',[
 mode('rest-mode','leaf','闭眼休息','Close & rest','听着语音，轻轻闭眼再睁眼','Let the voice guide your rest','10轮','setup-rest'),
 mode('butterfly-mode','butterfly','蝴蝶漫游','Butterfly pursuit','跟随一对轻盈的翅膀','Follow a gentle pair of wings','60s','setup-butterfly'),
 mode('gaze-mode','orbit','眼球运动','Eye movements','让目光沿着小球舒展','Follow the orange target','4组','setup-gaze')],gap=10)
utilities=row('home-tools',[button('history-open','休息足迹','History','history'),button('settings-open','声音与设置','Settings','settings'),button('help-open','使用说明','Help','help')],justify='space-between')
state_roots['home']=root('home-root',[header('home-header',True),hero,starts,modes,spacer('home-space'),utilities,foot('home-foot')])
state_roots['welcome']=page('welcome','给眼睛的一段留白','A little room to pause',[
 media('welcome-art','hero',600,260),text('welcome-body','闭眼休息跟随语音即可；蝴蝶和小球训练需要眼动权限。','Close & rest is voice-guided. Following activities need eye tracking.'),note('welcome-note','记录只留在本机，语言可以在训练前切换。','Records stay on this device. Choose your language before training.'),spacer('welcome-space'),button('welcome-start','了解并继续','Continue','home',True,1036)],True)
state_roots['setup']=page('setup','先选一个舒服的节奏','Make yourself comfortable',[
 note('setup-mode','三项休息，按自己的节奏完成。','Three activities, at your own pace.'),row('plan-row',[button('plan-quick','短练习','Short','plan-quick',True),button('plan-full','完整练习','Full','plan-full')]),
 row('pace-row',[button('pace-comfort','舒缓','Comfortable','pace-comfort',True),button('pace-standard','标准','Standard','pace-standard')]),note('setup-pace-note','舒缓：飞得慢一点，范围小一点。','Comfortable: slower movement and smaller paths.'),
 text('setup-dose','闭眼3轮 · 蝴蝶30秒\n小球四组路径，各1轮','3 guided rounds · 30 seconds of butterfly\nFour target paths, one round each'),note('setup-duration','时长随跟随节奏变化，随时可以暂停。','Duration varies with your pace. Pause whenever you like.'),spacer('setup-space'),button('setup-begin','准备好了，开始休息','Begin my break','preparing',True,1036),button('setup-back','返回选择','Back to activities','home')],True)

def train(id,title,en,main,en_main,number,detail,en_detail,art):
    progress=node(id+'-progress','spatialui',component='sui-linear-progress',props={'ui':{'value':.35,'size':'regular','track-color':'mintWash','progress-color':'deepForest'}})
    clock=layout(id+'-clock',[text(id+'-count',number,number,'number'),text(id+'-detail',detail,en_detail),progress],heightDp=280,align='center',justify='center',gap=16)
    return page(id,title,en,[row(id+'-chapter',[media(id+'-icon',art,76,76),note(id+'-status','正在休息','A moment of rest')]),text(id+'-main',main,en_main,'title'),clock,note(id+'-note','慢慢来，感到不适时可以停止。','Take your time. Stop if you feel uncomfortable.'),spacer(id+'-space'),row(id+'-actions',[button(id+'-pause','暂停','Pause','pause',True),button(id+'-finish','结束休息','Finish','exit')]),button(id+'-home','返回选择','Back to activities','exit-home')])
state_roots['blink']=train('blink','闭眼休息','Close & rest','跟着语音，轻轻闭上眼睛。','Follow the voice. Gently close your eyes.','2 / 3','闭眼保持3秒，再慢慢睁眼','Close for 3 seconds, then slowly open','leaf')
state_roots['training']=train('training','蝴蝶漫游','Butterfly pursuit','让目光轻轻跟随翅膀。','Let your gaze follow the wings.','12 / 30','秒有效跟随','seconds of active following','butterfly')
state_roots['gaze']=train('gaze','眼球运动','Eye movements','沿着小球，让目光缓缓移动。','Let your eyes follow the orange target.','1 / 4','四组路径 · 允许自然转头','Four paths · turn your head naturally','orbit')

def message(id,title,en,body,enbody,buttons,art='leaf'):
    return page(id,title,en,[media(id+'-art',art,120,120),text(id+'-body',body,enbody),spacer(id+'-space'),*buttons])
state_roots['preparing']=message('preparing','安静片刻，就开始。','A moment to settle in.','放松坐好，倒计时后一起出发。','Sit comfortably. We begin after the countdown.',[text('countdown','3','3','number'),button('preparing-pause','暂停','Pause','pause')])
state_roots['paused']=message('paused','这片安静，为你保留。','Your quiet moment is kept.','准备好后点继续，我们会先倒计时。','Choose Resume when ready. A countdown comes first.',[button('resume','继续休息','Resume','preparing',True,1036),button('pause-exit','结束休息','Finish','exit')])
state_roots['tracking-paused']=message('tracking','先调整一下佩戴。','Take a moment to adjust.','目光准备好后继续，进度已保留。','We continue when tracking is ready. Your progress is kept.',[button('tracking-exit','结束休息','Finish','exit')])
state_roots['permission']=message('permission','让目光连接小岛。','Let your gaze find the island.','这项训练需要眼动权限。闭眼语音休息无需眼动权限。','This activity needs eye tracking. Voice-guided rest does not.',[button('permission-enable','开启眼动','Enable eye tracking','preparing',True,1036),button('permission-rest','先做闭眼休息','Try guided rest','setup-rest')])
state_roots['incomplete']=message('incomplete','没跟上，也没关系。','Missed it? That is okay.','可以再试一次，或跳过这一轮。','Try again, or move on to the next round.',[button('retry','再试一次','Retry','preparing',True,1036),button('skip','跳过本轮','Skip round','preparing')])
state_roots['entering']=message('entering','小岛的伙伴，来陪你了。','A friend from the island.','抵达中央后开始训练，先轻轻看着它。','Training begins after arrival. Gently watch it approach.',[button('entering-pause','暂停','Pause','pause'),button('entering-exit','结束休息','Finish','returning')])
state_roots['returning']=message('returning','送它回小岛。','Back to the island.','这次休息已经保存，伙伴正在回岛。','Your session is saved. Your companion is returning.',[])
summary_hero=row('summary-hero',[layout('summary-copy',[note('summary-eyebrow','这一次，慢了下来','YOUR LITTLE PAUSE'),text('summary-title','谢谢自己，\n停下来一会儿。','A little pause,\njust for you.','headline')],gap=12,grow=1),media('summary-art','hero',340,220)],heightDp=240)
stat_nodes=[]
for id,zh,en,val in [('done','完成','Complete','3'),('miss','未完成','Incomplete','0'),('skip','跳过','Skipped','0'),('pause','中断','Pauses','0')]:stat_nodes.append(layout('stat-'+id,[text('stat-'+id+'-v',val,val,'stat'),note('stat-'+id+'-l',zh,en)],align='center',grow=1))
state_roots['summary']=root('summary-root',[header('summary-header'),summary_hero,row('summary-stats',stat_nodes,heightDp=170),text('summary-saved','这次休息已保存在本机。','Your break is saved on this device.'),note('summary-note','闭眼休息记录语音流程，不检测眼睛开合。','Guided rest records the sequence; eye closure is not measured.'),spacer('summary-space'),button('summary-finish','结束休息，送它回岛','Finish & return to island','returning',True,1036),button('summary-home','返回小岛','Back to the island','home'),foot('summary-foot')])
state_roots['history']=page('history','我的休息足迹','My little pauses',[
 row('history-top',[note('history-overview','今天已休息2次','2 breaks today'),button('history-stats','七天统计','Last 7 days','statistics')],justify='space-between'),
 text('history-row-1','今天 14:20 · 快速休息 · 完成\n语音引导与目光跟随组合','Today 14:20 · Quick session · Complete\nGuided rest and gaze following'),
 text('history-row-2','昨天 20:10 · 闭眼休息 · 退出\n已完成2轮语音引导','Yesterday 20:10 · Guided rest · Exited\n2 guided rounds complete'),spacer('history-space'),row('history-pages',[button('history-prev','上一页','Previous','history'),note('history-page','1 / 1','1 / 1'),button('history-next','下一页','Next','history')]),button('history-delete','删除本机记录…','Delete local records…','delete-confirm')])
bars=[]
for i,value in enumerate([.15,0,.35,.5,.1,.65,.8]):
    bar=node('day-'+str(i)+'-bar','spatialui',component='sui-linear-progress',props={'ui':{'value':value,'size':'regular','track-color':'mintWash','progress-color':'deepForest'}},layout={'mode':'row','grow':1})
    bars.append(row('day-'+str(i),[note('day-'+str(i)+'-date','11/'+str(21+i),'11/'+str(21+i)),bar,note('day-'+str(i)+'-value',str(round(value*5,1))+'分',str(round(value*5,1))+' min')],heightDp=44))
state_roots['statistics']=page('statistics','把休息，留在日常。','Make room for little pauses.',[text('stats-today','今天2次 · 4.0分钟','Today 2 breaks · 4.0 min'),note('stats-week','近七天共9次 · 12.8分钟有效练习','7 days: 9 breaks · 12.8 active minutes'),layout('stats-bars',bars,gap=12),note('stats-categories','语音引导3次 · 目光跟随2次 · 组合4次','Guided 3 · Gaze 2 · Combined 4'),note('stats-note','只统计本机有效练习，不表示佩戴时长或效果。','Local active practice only, not wear time or benefit.'),spacer('stats-space'),button('stats-back','返回足迹','Back to history','history')])
settings_rows=[]
for id,zh,en,value in [('music','背景音乐','Music','25%'),('voice','引导语音','Voice','70%'),('effects','提示音','Effects','40%')]:settings_rows.append(row('setting-'+id,[text('setting-'+id+'-label',zh,en),spacer('setting-'+id+'-space'),button(id+'-minus','−','−','volume-down'),note(id+'-value',value,value),button(id+'-plus','+','+','volume-up')]))
state_roots['settings']=page('settings','声音，也按你的节奏。','A soundscape of your own.',[note('settings-intro','让环境轻一点，让引导清楚一点。','Keep the ambience gentle and the voice clear.'),*settings_rows,row('settings-actions',[button('mute','全部静音','Mute all','mute'),button('preview-voice','试听女声','Preview voice','voice-preview')]),note('settings-reminder','佩戴累计提醒暂未开放。','Wear-time reminders are not available yet.'),spacer('settings-space'),button('settings-back','返回小岛','Back to the island','home')])
state_roots['help']=page('help','在小岛，轻松一点。','A gentle way around the island.',[text('help-1','闭眼休息听语音即可。蝴蝶和小球训练需要眼动权限。','Follow the voice for guided rest. Butterfly and target activities need eye tracking.'),text('help-2','可以自然转头。跟不上时，小球会等你；感到不适请停止。','Turn your head naturally. The target waits when you lose it. Stop if uncomfortable.'),note('help-3','虚拟远近不代表真实调焦；不测闭眼力度。记录只保存在本机，可随时删除。','Virtual depth is not real accommodation; eyelid force is not measured. Records stay local and can be deleted.'),spacer('help-space'),button('help-back','返回小岛','Back to the island','home')])
state_roots['delete-confirm']=message('delete','删除本机休息记录？','Delete local history?','语言和声音设置会保留。删除后无法在应用内恢复。','Language and audio preferences stay. This cannot be undone in the app.',[button('delete-cancel','保留记录','Keep my history','history',True,1036),button('delete-confirm-action','确认删除记录','Delete records','history')])

base=json.loads((ROOT/'review/nature-illustration-2026-11-24/design-spec.json').read_text())
for asset in base['assets']:
    if asset['id']=='butterfly': asset['src']='design-assets/butterfly.png'
base['metadata']={'projectName':'眨眨岛 · Tripo自然绘本重制','sourceMode':'hybrid','revision':1}
base['experience']['signature']='A cream storybook panel and a tactile Tripo miniature island form one calm visual world; central training stays clear.'
base['theme']['brandColors']={'forestInk':'#26483F','deepForest':'#365E48','mutedInk':'#586B5B','mintWash':'#E8EEDC','paperWhite':'#F6F5E9'}
# Explicit bounded paper artwork behind the shared Stage attachment content.
node('paper-surface','media',assetId='paper',appearance={'fill':'paperWhite'},layout={'mode':'column','widthDp':1100,'heightDp':1120})
base['assets'].append({'id':'paper','kind':'image','src':'design-assets/paper.svg','purpose':'Bounded opaque paper backdrop of the existing Stage attachment'})
for state_id,content_id in list(state_roots.items()):
    state_roots[state_id]=layout(state_id+'-surface',['paper-surface',content_id],mode='overlay',widthDp=1100,heightDp=1120)
base['surfaces'][0]['rootNodeId']=state_roots['home']
base['initialStateId']='home';base['nodes']=nodes;base['responsiveRules']=[]
base['states']=[{'id':id,'surfaceRoots':{'main-panel':rid}} for id,rid in state_roots.items()]
base['actions']=[{'id':id,'type':'custom','purpose':'Preview existing application navigation','payload':{'to':target}} for id,target in {'returning':'returning','home':'home','history':'history','settings':'settings','help':'help','statistics':'statistics','setup-quick':'setup','setup-full':'setup','setup-rest':'setup','setup-butterfly':'setup','setup-gaze':'setup','preparing':'preparing','pause':'paused','exit':'summary','exit-home':'home','delete-confirm':'delete-confirm','plan-quick':'setup','plan-full':'setup','pace-comfort':'setup','pace-standard':'setup','volume-down':'settings','volume-up':'settings','mute':'settings','voice-preview':'settings'}.items()]
base['actions'].append({'id':'language','type':'toggle','purpose':'Language before training only','binding':'language'})
base['transitions']=[{'id':'countdown-finished','from':'preparing','to':'blink','trigger':{'nodeId':'countdown','event':'preparation-elapsed'}}]
base['dataCases']={key:{'description':'Illustrative design data; never a sensor result'} for key in ['normal','fallback','error']}
for a in base['actions']:
    if a['id'].startswith('setup-'):
        a['payload']['mode']={'setup-rest':'rest','setup-gaze':'gaze','setup-butterfly':'butterfly','setup-quick':'rest','setup-full':'rest'}[a['id']]
        a['payload']['plan']='FULL' if a['id']=='setup-full' else 'QUICK'
    if a['id'] in ['plan-quick','plan-full']:a['payload']['plan']='QUICK' if a['id']=='plan-quick' else 'FULL'
    if a['id'] in ['pace-comfort','pace-standard']:a['payload']['pace']='COMFORTABLE' if a['id']=='pace-comfort' else 'STANDARD'
for n in base['nodes']:
    if n['id'] in ['quick','full']:n['props']['stretch']=False
    if n['id']=='setup-dose':n['props']['planContent']={'QUICK':{'zh':'闭眼3轮 · 蝴蝶30秒\n小球四组路径，各1轮','en':'3 guided rounds · 30 seconds of butterfly\nFour target paths, one round each'},'FULL':{'zh':'闭眼10轮 · 蝴蝶60秒\n左右/上下各10轮，顺逆圆各5轮','en':'10 guided rounds · 60 seconds of butterfly\n10 horizontal/vertical, 5 each circle'}}
    if n['id']=='setup-pace-note':n['props']['paceContent']={'COMFORTABLE':{'zh':'舒缓：飞得慢一点，范围小一点。','en':'Comfortable: slower movement and smaller paths.'},'STANDARD':{'zh':'标准：使用熟悉的速度和范围。','en':'Standard: the familiar speed and movement range.'}}
    if n['id'] in ['plan-quick','plan-full','pace-comfort','pace-standard']:
        n['props']['choice']={'key':'plan' if n['id'].startswith('plan') else 'pace','value':{'plan-quick':'QUICK','plan-full':'FULL','pace-comfort':'COMFORTABLE','pace-standard':'STANDARD'}[n['id']]}
schema=json.loads((SKILL/'assets/design-spec.schema.json').read_text())
jsonschema.Draft202012Validator(schema).validate(base)
(HERE/'design-spec.json').write_text(json.dumps(base,ensure_ascii=False,indent=2)+'\n')
assets={a['id']:'data:'+mimetypes.guess_type(a['src'])[0]+';base64,'+base64.b64encode((HERE/a['src']).read_bytes()).decode() for a in base['assets']}
bundle=(SKILL/'assets/spatialui-web/spatialui.bundle.js').read_text();renderer=(HERE/'renderer.js').read_text()
html='''<!doctype html><html lang="zh-CN"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>眨眨岛 · Tripo自然绘本设计审阅</title><style>*{box-sizing:border-box}html,body{margin:0;min-height:100%;font-family:"PingFang SC",sans-serif}body{background:#DAD6D3;display:flex;justify-content:center;padding:28px}#viewport{position:relative}#app{position:relative;overflow:hidden;background:var(--Background-MaterialRegular,rgba(255,255,255,.34));border-radius:32px}img{object-fit:contain;max-width:100%;flex-shrink:1}[data-node]{min-width:0;flex-shrink:0}p{margin:0;white-space:pre-line}sui-button,sui-list-item{max-width:100%}@media(prefers-reduced-motion:reduce){*{transition:none!important;animation:none!important}}</style><main id="viewport"><div id="app" data-design-spec-revision="1"></div></main><script>'''+bundle+'''</script><script type="application/json" id="pico-design-spec">'''+json.dumps(base,ensure_ascii=False).replace('</','<\/')+'''</script><script>const assetBytes='''+json.dumps(assets)+''';</script><script>'''+renderer+'''</script></html>'''
(HERE/'preview.html').write_text(html)
print('Wrote',len(nodes),'nodes and',len(state_roots),'review states')
