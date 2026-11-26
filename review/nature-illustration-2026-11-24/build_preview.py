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
    controls=[button(id+'-recenter','摆正位置','Recenter','recenter'),button(id+'-language','English','中文','language')] if home else [button(id+'-back','返回','Back','home')]
    return layout(id,[brand,*controls],mode='row',align='center',heightDp=80)
def foot(id): return text(id,'感到不适时，请停下来，摘下头显休息。','If uncomfortable, stop and remove the headset.','note','mutedInk')
def root(id, children): return layout(id,children,gap=16,paddingDp=32,heightDp=1120)

home_header=header('home-header',True)
hero_copy=layout('hero-copy',[
    text('hero-eyebrow','给眼睛的一段留白','A little room to pause','note','mutedInk'),
    text('hero-title','在岛上\n歇一会儿。','Pause on\nthe island.','hero'),
    text('hero-note','跟随目光，让节奏慢下来。','Follow gently. Take your time.','note','mutedInk')],gap=8,grow=1)
hero=layout('home-hero',[hero_copy,media('home-island','hero',440,220)],mode='row',gap=24,align='center',heightDp=240)
full=button('full-session','开始完整休息','Start full session','blocked',True,1036)
def mode(id, asset, name, en, subtitle, en_sub, badge, en_badge, action):
    col=layout(id+'-copy',[text(id+'-name',name,en,'title'),text(id+'-detail',subtitle,en_sub,'note','mutedInk')],gap=4,grow=1)
    row=layout(id+'-row',[media(id+'-art',asset,76,76),col,text(id+'-badge',badge,en_badge,'note','deepForest')],mode='row',gap=24,align='center',heightDp=88)
    nodes[-1].setdefault('props',{})['slot']='headline'
    return node(id,'spatialui',component='sui-list-item',children=[row],props={'ui':{'container-color':'mintWash','headline-color':'forestInk'}},events={'click-action':action},layout=dict(mode='row'))
butterfly=mode('butterfly-mode','butterfly','蝴蝶漫游','Butterfly pursuit','用目光，跟随翩飞的蝴蝶','Gently follow the butterfly','60 秒','60 sec','start-butterfly')
gaze=mode('gaze-mode','orbit','眼球运动','Eye movements','沿着橙色小球，走一段轨迹','Follow the orange target','四组动作','4 patterns','start-gaze')
blocked_row=layout('home-closed-row',[media('home-closed-leaf','leaf',40,40),text('home-closed-copy','闭眼休息 · 暂未开放','Close & rest · Unavailable','note','mutedInk')],mode='row',align='center',heightDp=56)
home_modes=layout('home-modes',[butterfly,gaze,blocked_row],gap=12)
utilities=layout('home-utilities',[button('history-open','记录与统计','History','history'),button('settings-open','声音与设置','Settings','settings'),button('help-open','使用说明','Help','help')],mode='row',justify='space-between')
home_root=root('home-root',[home_header,hero,full,home_modes,layout('home-spacer',[],grow=1),utilities,foot('home-foot')])

training_header=header('training-header')
training_title=layout('training-title',[
    layout('training-mode-row',[media('training-butterfly','butterfly',64,64),text('training-mode-name','蝴蝶漫游','Butterfly pursuit','title'),text('training-state','跟随中','Following','note','deepForest')],mode='row',align='center'),
    text('training-main','让目光，轻轻跟随。','Let your gaze follow gently.','headline')],gap=16)
progress_count=text('progress-count','24','24','number')
nodes[-1]['bindings']={'content':'training.seconds'}
progress= node('training-progress','spatialui',component='sui-linear-progress',props={'ui':{'value':0.4,'size':'regular','track-color':'mintWash','progress-color':'deepForest'}},bindings={'value':'training.progress'})
clock=layout('training-clock',[progress_count,text('training-time-label','/ 60 秒有效追随','/ 60 seconds of following','body','mutedInk'),progress],align='center',justify='center',gap=16,heightDp=280)
hint=layout('training-hint',[text('training-hint-main','中央的蝴蝶会带你慢慢移动。','Gently follow the butterfly in the center.'),text('training-hint-sub','可以自然转头，也可以轻轻眨眼。','Turn your head naturally, and blink as usual.','note','mutedInk')],align='center',gap=8)
controls=layout('training-controls',[button('pause','暂停','Pause','pause',True),button('skip','跳过本轮','Skip round','preparing')],mode='row',justify='center',gap=24)
ends=layout('training-exit-controls',[button('finish','结束休息','Finish','exit'),button('back-to-activities','返回选择','Back to activities','home')],mode='row',justify='center',gap=24)
prepare_again=button('prepare-again','重新准备','Prepare again','preparing')
training_root=root('training-root',[training_header,training_title,clock,hint,text('training-rounds','本次已完成 0 轮 · 已跳过 0 轮','0 rounds complete · 0 skipped','note','mutedInk'),layout('training-spacer',[],grow=1),controls,ends,prepare_again,foot('training-foot')])

def message_root(id, title, en, body, en_body, buttons, art='leaf'):
    return root(id,[header(id+'-header'),media(id+'-art',art,120,120),text(id+'-title',title,en,'headline'),text(id+'-body',body,en_body),layout(id+'-spacer',[],grow=1),*buttons,foot(id+'-foot')])
paused_root=message_root('paused-root','慢慢来，进度已保留。','Take your time. Your progress is kept.','准备好后，继续和蝴蝶一起出发。','Resume whenever you are ready.',[button('resume','继续','Resume','preparing',True,1036),button('pause-finish','结束休息','Finish','exit')])
tracking_root=message_root('tracking-root','先休息一下。','Take a moment.','调整好佩戴后会继续，进度已经保留。','Adjust your headset to continue. Your progress is kept.',[button('tracking-finish','结束休息','Finish','exit')])
preparing_root=message_root('preparing-root','准备好，再出发。','A moment to get ready.','看向中央目标，倒计时后开始。','Look at the center target. We begin after the countdown.',[text('countdown','3','3','number'),button('preparing-pause','暂停','Pause','pause')])
blocked_root=message_root('blocked-root','闭眼休息暂未开放。','Close & rest is not available yet.','可以先跟随蝴蝶，或试试眼球运动。','Try the butterfly or eye movements first.',[button('skip-closed','跳过此项','Skip this activity','start-butterfly',True,1036),button('closed-back','返回选择','Back to activities','home')])

summary_title=text('summary-title','这段小憩，已被记录。','Your little pause has a place.','headline');nodes[-1]['bindings']={'content':'record.title'}
result_copy=layout('summary-hero-copy',[text('summary-eyebrow','这次休息的记录','YOUR MOMENT OF REST','note','mutedInk'),summary_title,text('summary-note','每一次停下来，都值得被记录。','Every little pause deserves a place.','note','mutedInk')],gap=12,grow=1)
result_hero=layout('summary-hero',[result_copy,media('summary-art','hero',360,220)],mode='row',align='center',gap=16,heightDp=236)
def statistic(id,label,en,binding):
    value=text(id+'-value','0','0','stat');nodes[-1]['bindings']={'content':binding}
    return layout(id,[value,text(id+'-label',label,en,'note','mutedInk')],align='center',gap=8,grow=1)
stats=layout('summary-statistics',[statistic('stat-complete','完成','Complete','record.completed'),statistic('stat-incomplete','未完成','Incomplete','record.incomplete'),statistic('stat-skipped','跳过','Skipped','record.skipped'),statistic('stat-pauses','中断','Pauses','record.interruptions')],mode='row',gap=16,heightDp=170)
status=text('summary-save-status','记录已保存在这台设备上。','Your record is saved on this device.','body');nodes[-1]['bindings']={'content':'record.status'}
summary_root=root('summary-root',[header('summary-header'),result_hero,stats,layout('summary-space-a',[],heightDp=32),status,text('summary-private','只留下一份记录，把轻松留给自己。','A record stays here. The moment is yours.','note','mutedInk'),layout('summary-spacer',[],grow=1),button('view-history','查看本地记录','View local history','history',True,1036),button('summary-home','返回选择','Back to activities','home'),foot('summary-foot')])
history_root=message_root('history-root','我的休息记录','Local activity history','记录只保存在本机，可在原生应用内查看统计与删除。','Records stay on this device. Statistics and deletion remain available in the native app.',[text('history-sample','11-24 15:20 · 蝴蝶漫游 · 完成\n完成 1 / 未完成 0 / 跳过 0','11-24 15:20 · Butterfly pursuit · Complete\nComplete 1 / Incomplete 0 / Skipped 0','body'),button('history-back','返回选择','Back to activities','home')])
settings_root=message_root('settings-root','按你的节奏','Your own pace','自然女声、背景音乐与提示音，沿用当前音量设置。','Natural voice, music and effects keep your current settings.',[button('settings-back','返回选择','Back to activities','home')])
help_root=message_root('help-root','使用说明','A gentle guide','目光跟随蝴蝶或橙色小球，可自然转头。追踪不佳时暂停。闭眼休息与佩戴提醒仍未开放。','Follow the butterfly or orange target. Natural head movement is allowed. Poor tracking pauses progress. Close & rest and wear reminders are not available.',[button('help-back','返回选择','Back to activities','home')])

roles=['fillPrimary','fillSecondary','fillTertiary','fillLight','labelPrimaryLight','labelPrimary','labelSecondary','labelTertiary','labelQuaternary','lightenHover','lightenPressed','error','alert','passable','interaction','dividerLine']
typo={name:dict(family='PingFang SC, Noto Sans CJK SC, sans-serif',sizeDp=size,lineHeightDp=line,weight=weight) for name,size,line,weight in [('hero',56,68,600),('headline',48,62,600),('title',34,44,600),('brand',34,44,600),('body',32,44,400),('note',26,36,400),('button',32,42,500),('number',132,152,400),('stat',72,88,500)]}
state_roots={'home':home_root,'training':training_root,'paused':paused_root,'tracking-paused':tracking_root,'preparing':preparing_root,'blocked':blocked_root,'summary':summary_root,'history':history_root,'settings':settings_root,'help':help_root}
actions=[]
for id,to in [('home','home'),('history','history'),('settings','settings'),('help','help'),('blocked','blocked'),('pause','paused'),('preparing','preparing')]:actions.append(dict(id=id,type='custom',purpose='Navigate existing design state',payload={'to':to}))
actions += [dict(id='start-butterfly',type='custom',purpose='Review butterfly entry without changing scoring',payload={'mode':'butterfly','to':'preparing'}),dict(id='start-gaze',type='custom',purpose='Review orange target entry without changing scoring',payload={'mode':'gaze','to':'preparing'}),dict(id='exit',type='custom',purpose='Preview exit saved separately from completion',payload={'exit':True,'to':'summary'}),dict(id='language',type='toggle',purpose='Pretraining language only',binding='language'),dict(id='recenter',type='custom',purpose='Retain existing pretraining recenter workflow',payload={'recenter':True})]
spec=dict(schemaVersion='1.0',metadata=dict(projectName='眨眨岛 · 自然插画视觉打磨',sourceMode='hybrid',revision=2),experience=dict(subject='Nature-illustrated gaze rest on Blink Blink Island',user='PICO Swan wearer',context='Existing fixed left control panel, center training target and right island in Stage',singleJob='Choose and follow a gentle gaze activity with clear controls and honest records',space='full',form='stage_mixed',signature='A quiet watercolor island vignette welcomes the user, then yields attention to the actual training target.'),theme=dict(scheme='vibrant',colorScheme={r:{'source':'vibrant','role':r} for r in roles},brandColors=dict(forestInk='#254E43',deepForest='#2E6D57',mutedInk='#526C60',mintWash='#E3EBDD',paperWhite='#FAFBF5'),typography=typo,spacing=dict(small=8,regular=16,medium=24,large=32)),surfaces=[dict(id='main-panel',type='stage_mixed',defaultSize=dict(width=1100,height=1120,unit='dp'),minSize=dict(width=960,height=1120,unit='dp'),maxSize=dict(width=1100,height=1120,unit='dp'),rootNodeId=home_root,placement=dict(inherit='existing fixed left attachment; no new window or movement',reviewOnly=True))],initialStateId='home',states=[dict(id=k,surfaceRoots={'main-panel':v}) for k,v in state_roots.items()],transitions=[dict(id='preparation-elapsed',**{'from':'preparing','to':'training'},trigger={'nodeId':'countdown','event':'preparation-elapsed'})],actions=actions,dataCases=dict(normal=dict(training={'seconds':'24','progress':.4},record={'title':'这段小憩，已被记录。','completed':'1','incomplete':'0','skipped':'0','interruptions':'0','status':'记录已保存在这台设备上。'}),fallback=dict(training={'seconds':'0','progress':0},record={'title':'这次休息，先到这里。','completed':'0','incomplete':'0','skipped':'0','interruptions':'0','status':'尚无本机记录。'}),error=dict(training={'seconds':'24','progress':.4},record={'title':'记录还在保存中。','completed':'0','incomplete':'0','skipped':'0','interruptions':'0','status':'暂时无法保存，请在原生应用中重试。'})),nodes=nodes,responsiveRules=[dict(id='compact-hero',surfaceId='main-panel',when={'maxWidthDp':1039},overrides=[dict(nodeId='home-island',layout={'mode':'column','widthDp':330,'heightDp':220}),dict(nodeId='summary-art',layout={'mode':'column','widthDp':280,'heightDp':220})])],assets=[dict(id=id,kind='image',src='design-assets/'+src,purpose=purpose) for id,src,purpose in [('hero','hero-island.png','Generated quiet watercolor welcome/result vignette; never a gaze target'),('brand','brand.png','Existing approved brand identity'),('butterfly','butterfly.svg','Existing butterfly mode illustration'),('orbit','orbit.svg','Existing eye-motion mode illustration'),('leaf','leaf.svg','Existing quiet rest state accent')]])
# Mode variants remain executable design facts, never renderer copy.
variants={'training-mode-name':('眼球运动','Eye movements'),'training-main':('沿着小球，缓缓移动目光。','Follow the orange target gently.'),'training-time-label':('当前动作进度','Current movement progress'),'training-hint-main':('跟随中央橙色小球的轨迹。','Follow the orange target in the center.')}
for n in nodes:
    if n['id'] in variants:
        n['props']['gazeContent'],n['props']['gazeEnglish']=variants[n['id']]
    if n['id']=='training-butterfly': n['props']={'gazeAssetId':'orbit'}
    if n['id']=='training-clock': n['layout']['heightDp']=248
for a in actions:
    if a['id']=='exit': a['payload']['record']={'title':'这次休息，先到这里。','completed':'0','incomplete':'0','skipped':'0','interruptions':'0','status':'退出记录已保存在这台设备上。'}
schema=json.loads((SKILL/'assets/design-spec.schema.json').read_text())
jsonschema.Draft202012Validator(schema).validate(spec)
(HERE/'design-spec.json').write_text(json.dumps(spec,ensure_ascii=False,indent=2)+'\n')

assets={a['id']:'data:'+mimetypes.guess_type(a['src'])[0]+';base64,'+base64.b64encode((HERE/a['src']).read_bytes()).decode() for a in spec['assets']}
bundle=(SKILL/'assets/spatialui-web/spatialui.bundle.js').read_text()
renderer=(HERE/'renderer.js').read_text()
html='''<!doctype html><html lang="zh-CN"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>眨眨岛 · 自然插画预览</title><style>
*{box-sizing:border-box}html,body{margin:0;min-height:100%;font-family:"PingFang SC",sans-serif}body{background-color:#DAD6D3;background-image:none;display:flex;justify-content:center;padding:28px}#viewport{position:relative}#app{position:relative;overflow:hidden;background:var(--Background-MaterialRegular,rgba(255,255,255,.34));border-radius:32px}img{object-fit:contain;max-width:100%;flex-shrink:1}[data-node]{min-width:0;flex-shrink:0}p{margin:0;white-space:pre-line} [data-design-surface]{background:var(--pico-design-surface-fill)}sui-button,sui-list-item{max-width:100%}@media(prefers-reduced-motion:reduce){*{transition:none!important;animation:none!important}}
</style><main id="viewport"><div id="app" data-design-spec-revision="2"></div></main><script>'''+bundle+'''</script><script type="application/json" id="pico-design-spec">'''+json.dumps(spec,ensure_ascii=False).replace('</','<\/')+'''</script><script>const assetBytes='''+json.dumps(assets)+''';</script><script>'''+renderer+'''</script></html>'''
(HERE/'preview.html').write_text(html)
print('Wrote schema-valid design-spec.json and self-contained preview.html;',len(nodes),'nodes;',len(state_roots),'states')
