import {useEffect,useState,type FormEvent} from 'react';
import {createRoot} from 'react-dom/client';
import type {Database} from 'sql.js';
import {openDatabase,samples,login,accountById,channels,nickname,saveNickname,points,type User} from './database';
import './style.css';
import {Wall} from './Wall';
import {GroupSettings,groupLabel} from './GroupSettings';
import {Features} from './Features';
import {WorkspaceEditor} from './WorkspaceEditor';
import {categories,visible,draftKind} from './moduleCatalog';
import {navLabel,modulePaths} from './moduleNavigation';
import {ModulePreview} from './ModulePreview';
import {featureRows} from './database';
import {nicknameOptions} from './names';
import {countryOptions,savedCountry,saveCountry} from './countries';
import {languages,translate,translateRoles,savedLanguage,saveLanguage,type Language} from './i18n';

function LanguageSelect({lang,onChange}:{lang:Language;onChange:(lang:Language)=>void}){return <label className="language-select"><span>{translate(lang,'Language')}</span><select value={lang} onChange={e=>onChange(e.target.value as Language)}>{languages.map(([id,label])=><option key={id} value={id}>{label}</option>)}</select></label>}

function CountrySelect({lang,country,onChange}:{lang:Language;country:string;onChange:(country:string)=>void}){
 const options=countryOptions(lang);
 return <label className="language-select country-select"><span>{translate(lang,'Country / region')}</span><select value={country} onChange={e=>onChange(e.target.value)}>{options.map(({code,name})=><option key={code} value={code}>{name} ({code})</option>)}</select></label>;
}

function App(){
  const lang:Language='ms';
  const t=(key:string)=>translate(lang,key);
  const [db,setDb]=useState<Database|null>(null);
  const [user,setUser]=useState<User|null>(null);
  useEffect(()=>{if(!user)document.documentElement.lang='ms'},[user]);
  const [error,setError]=useState('');
  const [normal,setNormal]=useState(false);
  const [email,setEmail]=useState('');
  const [password,setPassword]=useState('');
  const [busy,setBusy]=useState(false);
  useEffect(()=>{openDatabase().then(store=>{
    let restored:User|undefined;
    try{const id=localStorage.getItem('schoolday.session.account');if(id){restored=accountById(store,id);if(!restored)localStorage.removeItem('schoolday.session.account')}}catch{/* Local storage unavailable: login remains available. */}
    setDb(store);if(restored)setUser(restored);
  }).catch(()=>setError('Unable to open the local database.'))},[]);
  function remember(account:User){try{localStorage.setItem('schoolday.session.account',account.id)}catch{/* Session still works in memory. */}setUser(account)}
  function logout(){try{localStorage.removeItem('schoolday.session.account')}catch{/* Storage unavailable. */}setUser(null);setNormal(false);setEmail('');setPassword('');setError('')}
  async function signIn(e:FormEvent){
    e.preventDefault();if(!db||busy)return;setBusy(true);setError('');
    try{const u=await login(db,email,password);if(u){remember(u);setPassword('')}else setError('Invalid email or password.')}
    catch{setError('Sign-in unavailable.')}finally{setBusy(false)}
  }
  if(user&&db)return <Workspace key={user.id} db={db} user={user} logout={logout}/>;
  return <main className="login-screen"><section className="login-card">
    <div className="brand">schoolday</div><h1>{t("Sign in")}</h1>
    <label htmlFor="sample">{t("Sample user")}</label>
    <select id="sample" value="" disabled={!db||busy} onChange={e=>{const u=db&&samples(db).find(p=>p.id===e.target.value);if(u){setError('');remember(u)}}}>
      <option value="">{t("Select sample user")}</option>{db&&samples(db).map(u=><option key={u.id} value={u.id}>{u.name} — {u.id==='university'?(lang==='ms'?'Pelajar universiti':'University student'):translateRoles(lang,u.roles)}</option>)}
    </select>
    <button className="primary" disabled={!db||busy} onClick={()=>{setNormal(!normal);setError('')}}>{t(normal?'Hide login form':'Normal user login')}</button>
    {normal&&<form className="login-form" onSubmit={signIn}><label htmlFor="email">{t("Email")}</label><input id="email" type="email" autoComplete="username" value={email} onChange={e=>setEmail(e.target.value)} required/><label htmlFor="password">{t("Password")}</label><input id="password" type="password" autoComplete="current-password" value={password} onChange={e=>setPassword(e.target.value)} required/><button type="submit" disabled={busy}>{t("Login")}</button></form>}
    <small>{t("Local test accounts only. Do not enter real credentials.")}</small>{error&&<p role="alert">{t(error)}</p>}
  </section></main>;
}
const wideWorkspaceQuery='(min-width: 768px) and (min-height: 480px)';
function Workspace({db,user,logout}:{db:Database;user:User;logout:()=>void}){
  const [displayName,setDisplayName]=useState(()=>nickname(db,user));
  const [nameError,setNameError]=useState('');
  const [lang,setLang]=useState<Language>(()=>savedLanguage(user.id));
  const [country,setCountry]=useState(()=>savedCountry(user.id));
  useEffect(()=>{document.documentElement.lang=lang==='zh'?'zh-Hans':lang;saveLanguage(lang,user.id)},[lang,user.id]);
  useEffect(()=>{saveCountry(country,user.id)},[country,user.id]);
  const t=(key:string)=>translate(lang,key);
  const spaces=channels(db,user);
  const [selected,setSelected]=useState(spaces[0]?.id??'');
  const [opened,setOpened]=useState(()=>window.matchMedia(wideWorkspaceQuery).matches);
  useEffect(()=>{const mq=window.matchMedia(wideWorkspaceQuery);const changed=(event:MediaQueryListEvent)=>{if(event.matches){setOpened(true);setChoosing(false)}};mq.addEventListener('change',changed);return()=>mq.removeEventListener('change',changed)},[]);
  const [category,setCategory]=useState('Connect');
  const [choosing,setChoosing]=useState(false);
  useEffect(()=>{if(!choosing)return;const dismiss=(e:KeyboardEvent)=>{if(e.key==='Escape'){e.preventDefault();setChoosing(false)}};window.addEventListener('keydown',dismiss);return()=>window.removeEventListener('keydown',dismiss)},[choosing]);
  const [moduleId,setModuleId]=useState('Chats');
  const [record,setRecord]=useState('');
  const [revision,setRevision]=useState(0);
  const [groupOpen,setGroupOpen]=useState(false);
  useEffect(()=>setGroupOpen(false),[selected,moduleId,category]);
  const staff=user.roles.split(' + ').some(role=>role==='Teacher'||role==='School Admin');
  const minor=Number(featureRows(db,"SELECT age FROM demographics WHERE user_id=?",[user.id])[0]?.[0]??18)<18;
  const module=visible(category,user.roles,minor).find(m=>m.id===moduleId)??visible(category,user.roles,minor)[0]!;
  const tab=module.id;
  const kind=draftKind(tab);
  const drafts=kind&&staff?featureRows(db,"SELECT id,title,details FROM workspace_drafts WHERE owner_id=? AND kind=? ORDER BY created_at DESC,id",[user.id,kind]):[];
  const picked=drafts.find(r=>r[0]===record);
  const [busy,setBusy]=useState(false);
  async function chooseNickname(value:string){if(busy)return;setBusy(true);setNameError('');try{await saveNickname(db,user,value);setDisplayName(value)}catch{setNameError('Unable to save locally.')}finally{setBusy(false)}}
  function select(id:string){setCategory('Connect');setModuleId('Chats');setSelected(id);setOpened(true)}
  return <main className={opened?'app chat-open':'app'}>
    <aside className="sidebar"><header className="pane-header"><div className="brand">schoolday</div><button className="quiet mobile-modules" disabled={busy} onClick={()=>setChoosing(v=>!v)} aria-expanded={choosing}>{navLabel(tab,lang)} ▾</button><button className="quiet" disabled={busy} onClick={logout}>{t("Logout")}</button></header>
    <div className={"module-nav"+(choosing?" picker-open":"")} data-revision={revision}><div className="record-list">{tab!=='Chats'&&<div className={'module-heading '+(module.status!=='local'?'unfinished':'')}><strong>{t(module.title)}</strong>{module.status!=='local'&&<small>{module.status==='partial'?'Partially implemented':'Not implemented'}</small>}</div>}
    {tab==='Chats'?spaces.map(c=><button key={c.id} className={'chat-item '+(selected===c.id?'selected':'')} disabled={busy} onClick={()=>select(c.id)}><span className="avatar">{t(c.title)[0]}</span><span className="chat-label"><strong>{t(c.title)}</strong><small>{t("Local only")}</small></span><span className="chevron">›</span></button>):<>{kind&&staff?<><button className="chat-item" disabled={busy} onClick={()=>{setRecord('');setOpened(true)}}>+ {t('Create draft')}</button>{drafts.map(r=><button key={r[0]} className={'chat-item record-preview '+(record===r[0]?'selected':'')} disabled={busy} onClick={()=>{setRecord(r[0]);setOpened(true)}}><span className="chat-label"><strong>{r[1]}</strong><small>{r[2]}</small><small>{t('Draft only · Not published')}</small></span></button>)}{!drafts.length&&<p className="empty-preview">{t('No drafts yet')}</p>}</>:module.records.length?module.records.map(r=><button key={r} className={'chat-item '+(record===r?'selected':'')} disabled={busy} onClick={()=>{setRecord(r);setOpened(true)}}><span className="chat-label"><strong>{r}</strong><small>Sample preview</small></span></button>):<button className="chat-item" disabled={busy} onClick={()=>setOpened(true)}>{t(module.title)} ›</button>}</>}
    </div>{choosing&&<section className="mobile-module-sheet" aria-labelledby="module-sheet-title"><div className="module-sheet-body"><div className="sheet-heading"><h2 id="module-sheet-title">{navLabel(category,lang)}</h2><button className="quiet sheet-close" aria-label={t("Close")} onClick={()=>setChoosing(false)}>×</button></div><nav className="module-list" aria-label="Modules" key={category}>{visible(category,user.roles,minor).map(m=><button key={m.id} data-module={m.id} aria-label={m.title+(m.status==='local'?'':m.status==='partial'?' · Partially implemented':' · Not implemented')} title={m.title} aria-current={tab===m.id?'page':undefined} className={m.status==='local'?'':'unfinished-module'} disabled={busy} onClick={()=>{setModuleId(m.id);setRecord('');setChoosing(false);setOpened(window.matchMedia(wideWorkspaceQuery).matches)}}><svg width="23" height="23" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true"><path d={modulePaths[m.id]}/></svg><span>{navLabel(m.id,lang)}</span>{tab===m.id?<span className="module-check" aria-hidden="true">✓</span>:<span className="module-chevron" aria-hidden="true">›</span>}</button>)}</nav></div></section> }</div><nav className="footer-nav" aria-label={t("Main navigation")}>{categories.map((name,index)=><button key={name} aria-current={category===name?'page':undefined} disabled={busy} onClick={()=>{const wide=window.matchMedia(wideWorkspaceQuery).matches;if(name===category&&!wide){setChoosing(v=>!v);return}setCategory(name);setModuleId(visible(name,user.roles,minor)[0]!.id);setRecord('');setChoosing(!wide);setOpened(wide)}}><svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">{[<path key="chat" d="M20 11a8 8 0 0 1-8 8H4l2-4a8 8 0 1 1 14-4Z"/>,<path key="learn" d="m2 8 10-5 10 5-10 5-10-5Zm4 2v7q6 4 12 0v-7"/>,<path key="campus" d="M3 21h18M5 21V9l7-5 7 5v12M10 21v-5h4v5"/>,<g key="award"><circle cx="12" cy="9" r="6"/><path d="m8 14-2 7 6-3 6 3-2-7"/></g>,<g key="me"><circle cx="12" cy="8" r="4"/><path d="M4 21v-2a8 8 0 0 1 16 0v2"/></g>][index]}</svg><span>{navLabel(name,lang)}</span></button>)}</nav></aside>
    <section aria-label="Workspace detail" className={'chat-panel '+(module.status!=='local'?'unfinished-workspace':'')}><header className="pane-header wide-workspace-header"><div className="workspace-heading"><h2>{tab==='Chats'?t(spaces.find(c=>c.id===selected)?.title??'Chats'):t(module.title)}</h2>{tab==='Chats'&&<small>{t("Local only")}</small>}</div><nav className="workspace-tools" aria-label="Workspace modules" key={category}>{visible(category,user.roles,minor).map(m=><button key={m.id} data-module={m.id} aria-label={m.title+(m.status==='local'?'':m.status==='partial'?' · Partially implemented':' · Not implemented')} title={m.title} aria-current={tab===m.id?'page':undefined} className={m.status==='local'?'':'unfinished-module'} disabled={busy} onClick={()=>{setModuleId(m.id);setRecord('');setChoosing(false);setOpened(window.matchMedia(wideWorkspaceQuery).matches)}}><svg width="23" height="23" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true"><path d={modulePaths[m.id]}/></svg><span>{navLabel(m.id,lang)}</span></button>)}</nav>{tab==='Chats'&&<button className="quiet group-settings-toggle" disabled={busy||!selected} aria-label={groupLabel(lang)} title={groupLabel(lang)} aria-pressed={groupOpen} onClick={()=>setGroupOpen(v=>!v)}><svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true"><path d="M12 3L15 6L19 6L21 10L19 14L19 18L15 21L12 19L9 21L5 18L5 14L3 10L5 6L9 6ZM12 9A3 3 0 1 0 12 15A3 3 0 1 0 12 9Z"/></svg></button>}</header>{tab==='Chats'?<><header className="compact-detail-header"><div><h2>{t(spaces.find(c=>c.id===selected)?.title??'Chats')}</h2><small>{t("Local only")}</small></div><div className="actions"><button className="quiet group-settings-toggle" disabled={busy||!selected} aria-label={groupLabel(lang)} title={groupLabel(lang)} aria-pressed={groupOpen} onClick={()=>setGroupOpen(v=>!v)}><svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true"><path d="M12 3L15 6L19 6L21 10L19 14L19 18L15 21L12 19L9 21L5 18L5 14L3 10L5 6L9 6ZM12 9A3 3 0 1 0 12 15A3 3 0 1 0 12 9Z"/></svg></button><button className="quiet back" disabled={busy} onClick={()=>{if(groupOpen)setGroupOpen(false);else setOpened(false)}}>{t("Back")}</button></div></header>
    {selected&&<><div className="group-chat-content" hidden={groupOpen}><Wall key={selected} db={db} user={user} channel={selected} lang={lang} busy={busy} setBusy={setBusy}/></div>{groupOpen&&<GroupSettings key={selected} db={db} user={user} channel={selected} title={t(spaces.find(c=>c.id===selected)?.title??"Chats")} lang={lang} onClose={()=>setGroupOpen(false)}/>}</>}</>:<><header className="compact-detail-header"><h2>{t(module.title)}</h2><button className="quiet back" disabled={busy} onClick={()=>setOpened(false)}>{t('Back')}</button></header><div className="workspace-content">{module.status!=='local'&&<p className="module-status">{module.status==='partial'?'Partially implemented':'Not implemented'}</p>}{kind&&staff?(picked?<section className="feature-card"><h2>{picked[1]}</h2><p className="draft-detail">{picked[2]}</p><small>{t('Draft only · Not published')}</small><button onClick={()=>setRecord('')}>{t('Create draft')}</button></section>:<WorkspaceEditor key={tab} db={db} user={user} kind={kind} lang={lang} busy={busy} setBusy={setBusy} onSaved={()=>setRevision(v=>v+1)}/>):tab==='Profile'?<div className="profile-settings"><label>{t("Full name")}</label><strong>{user.name}</strong><label className="language-select"><span>{t("Nickname")}</span><select value={displayName} disabled={busy} onChange={e=>void chooseNickname(e.target.value)}>{nicknameOptions(user.name).map(option=><option key={option} value={option}>{option}</option>)}</select></label><small>{t("Choose one or two parts of your full name.")}</small>{nameError&&<p role="alert">{t(nameError)}</p>}<p>{user.email}</p><p>{translateRoles(lang,user.roles)}</p><LanguageSelect lang={lang} onChange={setLang}/><CountrySelect lang={lang} country={country} onChange={setCountry}/><Features db={db} user={user} busy={busy} setBusy={setBusy} screen="Profile"/></div>:tab==='Rewards'||tab==='Quiz'?<Features key={tab} db={db} user={user} busy={busy} setBusy={setBusy} screen={tab}/>:<ModulePreview key={tab+record} module={module} db={db} user={user} record={record}/>}</div></>}</section>

  </main>;
}
createRoot(document.getElementById('root')!).render(<App/>);
if(import.meta.env.PROD&&'serviceWorker' in navigator)window.addEventListener('load',()=>navigator.serviceWorker.register(import.meta.env.BASE_URL+'sw.js').catch(console.warn));
