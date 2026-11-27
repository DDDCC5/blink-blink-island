const spec=JSON.parse(document.getElementById('pico-design-spec').textContent);
const app=document.getElementById('app'),viewport=document.getElementById('viewport');
// Accepted Stage attachment is opaque app content, not a system glass window.
app.style.backgroundColor='transparent';
let state=spec.initialStateId,language='zh',mode='butterfly',caseName='normal',overrides={},plan='QUICK',pace='COMFORTABLE',timer,width=spec.surfaces[0].defaultSize.width;
const nodes=new Map(spec.nodes.map(n=>[n.id,n])),actions=new Map(spec.actions.map(a=>[a.id,a]));
const color=x=>spec.theme.brandColors[x]||x;
const bound=path=>path.split('.').reduce((o,k)=>o?.[k],{...spec.dataCases[caseName],...overrides});
function build(id,slotColor){
 const source=nodes.get(id);if(!source)throw Error(id);let n=structuredClone(source);
 for(const rule of spec.responsiveRules||[])if(width<=rule.when.maxWidthDp)for(const o of rule.overrides)if(o.nodeId===id)n={...n,...o};
 const el=document.createElement(n.kind==='text'?'p':n.kind==='media'?'img':n.component||'div');el.dataset.node=id;
 const l=n.layout||{},s=el.style;
 if(n.kind==='layout'){s.display='flex';s.flexDirection=l.mode==='row'?'row':'column';s.gap=(l.gapDp||0)+'px';s.alignItems=l.align==='center'?'center':l.align||'stretch';s.justifyContent=l.justify||'flex-start';if(l.mode==='overlay'){s.display='grid';s.gridTemplateColumns='1fr';s.gridTemplateRows='1fr';}}
 for(const [k,prop] of [['widthDp','width'],['heightDp','height'],['paddingDp','padding']])if(l[k]!==undefined)s[prop]=l[k]+'px';
 if(l.widthDp)s.maxWidth='100%';if(l.grow){s.flexGrow=l.grow;s.flexBasis='0px';s.flexShrink='1';}
 if(n.props?.slot)el.slot=n.props.slot;
 if(n.kind==='text'){
  let content=language==='en'?n.props?.english:n.content;
  if(mode==='gaze'&&n.props?.gazeContent)content=language==='en'?n.props.gazeEnglish:n.props.gazeContent;
  if(n.props?.planContent)content=n.props.planContent[plan][language];if(n.props?.paceContent)content=n.props.paceContent[pace][language];if(n.bindings?.content)content=bound(n.bindings.content)??content;
  el.textContent=content;const t=spec.theme.typography[n.textStyle.typography];Object.assign(s,{fontFamily:t.family,fontSize:t.sizeDp+'px',lineHeight:t.lineHeightDp+'px',fontWeight:t.weight,color:color(n.textStyle.color)});
 }
 if(n.kind==='text'&&slotColor)s.color=slotColor;
 if(n.kind==='media'){el.src=assetBytes[mode==='gaze'&&n.props?.gazeAssetId?n.props.gazeAssetId:n.assetId];el.alt='';}
 if(n.kind==='spatialui'){if(n.props?.choice){const selected=({plan,pace})[n.props.choice.key]===n.props.choice.value;n.props.ui['container-color']=selected?'deepForest':'mintWash';n.props.ui['content-color']=selected?'paperWhite':'forestInk';}
  for(const [k,v]of Object.entries(n.props?.ui||{}))el.setAttribute(k,color(v));
  for(const [k,p]of Object.entries(n.bindings||{}))el.setAttribute(k,bound(p));
  for(const [event,action]of Object.entries(n.events||{}))el.addEventListener(event,()=>act(action));
 }
 if(n.appearance?.fill)s.backgroundColor=color(n.appearance.fill);
 const childColor=n.kind==='spatialui'&&n.props?.ui?.['content-color']?color(n.props.ui['content-color']):slotColor;for(const child of n.children||[]){const childElement=build(child,childColor);if(l.mode==='overlay')childElement.style.gridArea='1 / 1';el.append(childElement);}
 if(n.props?.stretch){s.width='100%';new ResizeObserver(()=>{if(el.firstElementChild)el.firstElementChild.style.width=Math.max(0,el.clientWidth-2*n.props.contentInsetDp)+'px';}).observe(el);}
 return el;
}
function render(){clearTimeout(timer);app.replaceChildren(build(spec.states.find(s=>s.id===state).surfaceRoots['main-panel']));app.style.width=width+'px';app.style.height=spec.surfaces[0].defaultSize.height+'px';fit();if(state==='preparing')countdown(3);}
function countdown(n){const e=app.querySelector('[data-node="countdown"]');if(e)e.textContent=n;timer=setTimeout(()=>{if(n>1)countdown(n-1);else{const t=spec.transitions.find(t=>t.from===state&&t.trigger.event==='preparation-elapsed');state=mode==='rest'?'blink':mode==='gaze'?'gaze':'training';render();}},1000);}
function act(id){const a=actions.get(id);if(!a)return;if(a.type==='toggle'){language=language==='zh'?'en':'zh';render();return;}const p=a.payload||{};if(p.mode)mode=p.mode;if(p.plan)plan=p.plan;if(p.pace)pace=p.pace;if(p.record)overrides.record=p.record;if(p.to)state=p.to;render();}
function fit(){const scale=Math.min(1,(innerWidth-56)/width,(innerHeight-56)/spec.surfaces[0].defaultSize.height);app.style.transformOrigin='top left';app.style.transform=`scale(${scale})`;viewport.style.width=width*scale+'px';viewport.style.height=spec.surfaces[0].defaultSize.height*scale+'px';}
window.addEventListener('resize',fit);
window.designReview={setState(id){if(!spec.states.some(s=>s.id===id))throw Error(id);state=id;render();},setCase(id){caseName=id;overrides={};render();},setLanguage(id){language=id;render();},setSurfaceWidth(w){width=w;render();},action:act,audit(){const r=app.getBoundingClientRect();return{state,width,background:getComputedStyle(document.body).backgroundColor,backgroundImage:getComputedStyle(document.body).backgroundImage,unregistered:[...app.querySelectorAll('*')].filter(e=>e.localName.startsWith('sui-')&&!customElements.get(e.localName)).map(e=>e.localName),overflow:[...app.querySelectorAll('[data-node]')].filter(e=>{const b=e.getBoundingClientRect();return b.bottom>r.bottom+1||b.right>r.right+1||b.left<r.left-1;}).map(e=>e.dataset.node),nativeControls:app.querySelectorAll('button,input,select,textarea').length};}};
const params=new URLSearchParams(location.search); if(spec.states.some(s=>s.id===params.get('state')))state=params.get('state'); if(params.get('width'))width=Number(params.get('width'));if(spec.dataCases[params.get('case')])caseName=params.get('case');if(params.get('lang')==='en')language='en';
render();
