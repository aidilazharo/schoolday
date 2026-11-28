import {useEffect,useState,type FormEvent} from 'react';
import {createRoot} from 'react-dom/client';
import type {Database} from 'sql.js';
import {openDatabase,samples,login,channels,drafts,write,type User,type Draft} from './database';
import './style.css';

function App(){
  const [db,setDb]=useState<Database|null>(null);
  const [user,setUser]=useState<User|null>(null);
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
    <div className="brand">schoolday</div><h1>Sign in</h1>
    <label htmlFor="sample">Sample user</label>
    <select id="sample" value="" disabled={!db||busy} onChange={e=>{const u=db&&samples(db).find(p=>p.id===e.target.value);if(u){setError('');setUser(u)}}}>
      <option value="">Select sample user</option>{db&&samples(db).map(u=><option key={u.id} value={u.id}>{u.name} — {u.roles}</option>)}
    </select>
    <button className="primary" disabled={!db||busy} onClick={()=>{setNormal(!normal);setError('')}}>{normal?'Hide login form':'Normal user login'}</button>
    {normal&&<form className="login-form" onSubmit={signIn}><label htmlFor="email">Email</label><input id="email" type="email" autoComplete="username" value={email} onChange={e=>setEmail(e.target.value)} required/><label htmlFor="password">Password</label><input id="password" type="password" autoComplete="current-password" value={password} onChange={e=>setPassword(e.target.value)} required/><button type="submit" disabled={busy}>Login</button></form>}
    <small>Local test accounts only. Do not enter real credentials.</small>{error&&<p role="alert">{error}</p>}
  </section></main>;
}
function Workspace({db,user,logout}:{db:Database;user:User;logout:()=>void}){
  const spaces=channels(db,user);
  const [selected,setSelected]=useState(spaces[0]?.id??'');
  const [opened,setOpened]=useState(false);
  const [tab,setTab]=useState<'Chats'|'Calendar'|'Quiz'|'Rewards'|'Profile'>('Chats');
  const [messages,setMessages]=useState<Draft[]>(()=>drafts(db,user,selected));
  const [text,setText]=useState('');
  const [error,setError]=useState('');
  const [busy,setBusy]=useState(false);
  function select(id:string){setTab('Chats');setSelected(id);setMessages(drafts(db,user,id));setText('');setError('');setOpened(true)}
  async function change(value:string|null){if(busy)return;setBusy(true);setError('');try{await write(db,user,selected,value);setMessages(drafts(db,user,selected));if(value!==null)setText('')}catch{setError('Unable to save locally.')}finally{setBusy(false)}}
  return <main className={opened&&tab==='Chats'?'app chat-open':'app'}>
    <aside className="sidebar"><header><div className="brand">schoolday</div><button className="quiet" disabled={busy} onClick={logout}>Logout</button></header>
    <div className="profile"><strong>{user.name}</strong><small>{user.roles}</small></div><h1>{tab}</h1>
    {tab==='Chats'&&<div className="chat-list">
    {spaces.map(c=><button key={c.id} className={'chat-item '+(selected===c.id?'selected':'')} disabled={busy} onClick={()=>select(c.id)}><span className="avatar" aria-hidden="true">{c.title[0]}</span><span className="chat-label"><strong>{c.title}</strong><small>Local only</small></span><span className="chevron">›</span></button>)}</div>}
    {tab!=='Chats'&&<div className="section-summary">{tab==='Profile'?<><strong>{user.name}</strong><p>{user.email}</p><p>{user.roles}</p></>:<p>No {tab==='Calendar'?'events':tab==='Quiz'?'quizzes':'rewards'} available.</p>}</div>}
    <nav className="footer-nav" aria-label="Main navigation">{(['Chats','Calendar','Quiz','Rewards','Profile'] as const).map((name,index)=><button key={name} aria-current={tab===name?'page':undefined} disabled={busy} onClick={()=>{setTab(name);setOpened(false)}}><svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">{[<path key="chat" d="M20 11a8 8 0 0 1-8 8H4l2-4a8 8 0 1 1 14-4Z"/>,<g key="cal"><rect x="3" y="5" width="18" height="16" rx="2"/><path d="M7 3v4m10-4v4M3 10h18"/></g>,<g key="quiz"><rect x="5" y="3" width="14" height="18" rx="2"/><path d="M9 8h6m-6 4h6m-6 4h3"/></g>,<g key="reward"><path d="m12 3 3 6 7 1-5 5 1 7-6-3-6 3 1-7-5-5 7-1Z"/></g>,<g key="profile"><circle cx="12" cy="8" r="4"/><path d="M4 21v-2a8 8 0 0 1 16 0v2"/></g>][index]}</svg><span>{name}</span></button>)}</nav></aside>
    <section className="chat-panel">{tab!=='Chats'?<div className="empty"><h2>{tab}</h2><p>{tab==='Profile'?user.name+' · '+user.roles:'No records available.'}</p></div>:<><header><div><h2>{spaces.find(c=>c.id===selected)?.title}</h2><small>Local only</small></div><div className="actions"><button className="quiet" disabled={busy||!messages.length} onClick={()=>{if(confirm('Clear local drafts?'))void change(null)}}>Clear</button><button className="quiet back" onClick={()=>setOpened(false)}>Back</button></div></header>
    <section className="conversation">{messages.length?messages.map(m=><article key={m.id}>{m.text}<small>Saved locally · Not sent</small></article>):<div className="empty"><h2>No saved messages</h2></div>}</section>
    {error&&<p role="alert">{error}</p>}<form onSubmit={e=>{e.preventDefault();if(text.trim())void change(text.trim())}}><input aria-label="Message" placeholder="Message" maxLength={500} value={text} onChange={e=>setText(e.target.value)}/><button disabled={busy||!text.trim()}>Save</button></form></>}</section>
  </main>;
}
createRoot(document.getElementById('root')!).render(<App/>);
if(import.meta.env.PROD&&'serviceWorker' in navigator)window.addEventListener('load',()=>navigator.serviceWorker.register(import.meta.env.BASE_URL+'sw.js').catch(console.warn));
