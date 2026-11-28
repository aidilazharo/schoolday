import {useEffect,useState,type FormEvent} from 'react';
import {createRoot} from 'react-dom/client';
import type {Database} from 'sql.js';
import {openDatabase,samples,login,channels,nickname,saveNickname,points,type User} from './database';
import './style.css';
import {Wall} from './Wall';
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
  useEffect(()=>{openDatabase().then(setDb).catch(()=>setError('Unable to open the local database.'))},[]);
  async function signIn(e:FormEvent){
    e.preventDefault();if(!db||busy)return;setBusy(true);setError('');
    try{const u=await login(db,email,password);if(u){setUser(u);setPassword('')}else setError('Invalid email or password.')}
    catch{setError('Sign-in unavailable.')}finally{setBusy(false)}
  }
  if(user&&db)return <Workspace key={user.id} db={db} user={user} logout={()=>{setUser(null);setNormal(false);setEmail('');setPassword('');setError('')}}/>;
  return <main className="login-screen"><section className="login-card">
    <div className="brand">schoolday</div><h1>{t("Sign in")}</h1>
    <label htmlFor="sample">{t("Sample user")}</label>
    <select id="sample" value="" disabled={!db||busy} onChange={e=>{const u=db&&samples(db).find(p=>p.id===e.target.value);if(u){setError('');setUser(u)}}}>
      <option value="">{t("Select sample user")}</option>{db&&samples(db).map(u=><option key={u.id} value={u.id}>{u.name} — {translateRoles(lang,u.roles)}</option>)}
    </select>
    <button className="primary" disabled={!db||busy} onClick={()=>{setNormal(!normal);setError('')}}>{t(normal?'Hide login form':'Normal user login')}</button>
    {normal&&<form className="login-form" onSubmit={signIn}><label htmlFor="email">{t("Email")}</label><input id="email" type="email" autoComplete="username" value={email} onChange={e=>setEmail(e.target.value)} required/><label htmlFor="password">{t("Password")}</label><input id="password" type="password" autoComplete="current-password" value={password} onChange={e=>setPassword(e.target.value)} required/><button type="submit" disabled={busy}>{t("Login")}</button></form>}
    <small>{t("Local test accounts only. Do not enter real credentials.")}</small>{error&&<p role="alert">{t(error)}</p>}
  </section></main>;
}
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
  const [opened,setOpened]=useState(false);
  const [tab,setTab]=useState<'Chats'|'Calendar'|'Quiz'|'Rewards'|'Profile'>('Chats');
  const [busy,setBusy]=useState(false);
  async function chooseNickname(value:string){if(busy)return;setBusy(true);setNameError('');try{await saveNickname(db,user,value);setDisplayName(value)}catch{setNameError('Unable to save locally.')}finally{setBusy(false)}}
  function select(id:string){setTab('Chats');setSelected(id);setOpened(true)}
  return <main className={opened&&tab==='Chats'?'app chat-open':'app'}>
    <aside className="sidebar"><header><div className="brand">schoolday</div><button className="quiet" disabled={busy} onClick={logout}>{t("Logout")}</button></header>
    <h1>{t(tab)}</h1>
    {tab==='Chats'&&<div className="chat-list">
    {spaces.map(c=><button key={c.id} className={'chat-item '+(selected===c.id?'selected':'')} disabled={busy} onClick={()=>select(c.id)}><span className="avatar" aria-hidden="true">{t(c.title)[0]}</span><span className="chat-label"><strong>{t(c.title)}</strong><small>{t("Local only")}</small></span><span className="chevron">›</span></button>)}</div>}
    {tab!=='Chats'&&<div className="section-summary">{tab==='Profile'?<div className="profile-settings"><label>{t("Full name")}</label><strong>{user.name}</strong><label className="language-select"><span>{t("Nickname")}</span><select value={displayName} disabled={busy} onChange={e=>void chooseNickname(e.target.value)}>{nicknameOptions(user.name).map(option=><option key={option} value={option}>{option}</option>)}</select></label><small>{t("Choose one or two parts of your full name.")}</small>{nameError&&<p role="alert">{t(nameError)}</p>}<p>{user.email}</p><p>{translateRoles(lang,user.roles)}</p><LanguageSelect lang={lang} onChange={setLang}/><CountrySelect lang={lang} country={country} onChange={setCountry}/></div>:tab==='Rewards'?<div className="points-card"><small>{t('Points balance')}</small><strong>{points(db,user).toLocaleString(lang)}</strong><span>{t('Points')}</span></div>:<p>{t(tab==='Calendar'?'No events available.':'No quizzes available.')}</p>}</div>}
    <nav className="footer-nav" aria-label={t("Main navigation")}>{(['Chats','Calendar','Quiz','Rewards','Profile'] as const).map((name,index)=><button key={name} aria-current={tab===name?'page':undefined} disabled={busy} onClick={()=>{setTab(name);setOpened(false)}}><svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">{[<path key="chat" d="M20 11a8 8 0 0 1-8 8H4l2-4a8 8 0 1 1 14-4Z"/>,<g key="cal"><rect x="3" y="5" width="18" height="16" rx="2"/><path d="M7 3v4m10-4v4M3 10h18"/></g>,<g key="quiz"><rect x="5" y="3" width="14" height="18" rx="2"/><path d="M9 8h6m-6 4h6m-6 4h3"/></g>,<g key="reward"><path d="m12 3 3 6 7 1-5 5 1 7-6-3-6 3 1-7-5-5 7-1Z"/></g>,<g key="profile"><circle cx="12" cy="8" r="4"/><path d="M4 21v-2a8 8 0 0 1 16 0v2"/></g>][index]}</svg><span>{t(name)}</span></button>)}</nav></aside>
    <section className="chat-panel">{tab!=='Chats'?<div className="empty"><h2>{t(tab)}</h2><p>{tab==='Profile'?displayName+' · '+translateRoles(lang,user.roles):tab==='Rewards'?points(db,user).toLocaleString(lang)+' '+t('Points'):t('No records available.')}</p></div>:<><header><div><h2>{t(spaces.find(c=>c.id===selected)?.title??'Chats')}</h2><small>{t("Local only")}</small></div><div className="actions"><button className="quiet back" disabled={busy} onClick={()=>setOpened(false)}>{t("Back")}</button></div></header>
    {selected&&<Wall key={selected} db={db} user={user} channel={selected} lang={lang} busy={busy} setBusy={setBusy}/>}</>}</section>
  </main>;
}
createRoot(document.getElementById('root')!).render(<App/>);
if(import.meta.env.PROD&&'serviceWorker' in navigator)window.addEventListener('load',()=>navigator.serviceWorker.register(import.meta.env.BASE_URL+'sw.js').catch(console.warn));
