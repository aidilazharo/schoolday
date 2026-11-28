import {useState,useRef,useEffect} from 'react';
import type {Database} from 'sql.js';
import {feed,wallAction,spaceMessages,sendSpaceMessage,type User} from './database';
import {translate,type Language} from './i18n';
export function Wall({db,user,channel,lang,busy,setBusy}:{db:Database;user:User;channel:string;lang:Language;busy:boolean;setBusy:(v:boolean)=>void}){
 const t=(s:string)=>translate(lang,s);
 const [posts,setPosts]=useState(()=>feed(db,user,channel));
 const [messages,setMessages]=useState(()=>spaceMessages(db,user,channel));
 const [mode,setMode]=useState<'chat'|'post'>('chat');
 const [text,setText]=useState('');
 const end=useRef<HTMLDivElement>(null);
 useEffect(()=>{end.current?.scrollIntoView({block:'end'})},[messages.length,posts.length]);
 const items=[...posts.map(p=>({kind:'post' as const,id:'p:'+p.id,time:p.createdAt,p})),...messages.map(m=>({kind:'chat' as const,id:'m:'+m.id,time:m.createdAt,m}))].sort((a,b)=>a.time-b.time||a.id.localeCompare(b.id));
 const [comments,setComments]=useState<Record<string,string>>({});
 const [error,setError]=useState(false);
 async function act(action:'post'|'comment'|'like'|'chat',body:string,id=''){
  if(busy)return;setBusy(true);setError(false);
  try{if(action==='chat')await sendSpaceMessage(db,user,channel,body);else await wallAction(db,user,channel,action,body,id);setPosts(feed(db,user,channel));setMessages(spaceMessages(db,user,channel));if(action==='post'||action==='chat')setText('');if(action==='comment')setComments(old=>({...old,[id]:''}))}
  catch{setError(true)}finally{setBusy(false)}
 }
 return <><section className="wall-feed unified-feed">
 {error&&<p role="alert">{t('Unable to save locally.')}</p>}
 {!items.length&&<p className="wall-empty">{t('No saved messages')}</p>}
 {items.map(item=>item.kind==='chat'?<div key={item.id} className={'timeline-message '+(item.m.authorId===user.id?'own':'other')}><div className="message-bubble">{item.m.authorId!==user.id&&<strong>{item.m.nickname||item.m.name}</strong>}<div>{item.m.body}</div><small>{new Date(item.m.createdAt).toLocaleTimeString(lang,{hour:'2-digit',minute:'2-digit'})} · {t(item.m.draft?'Saved locally · Not sent':'Local only')}</small></div></div>:renderPost(item.p))}
 <div ref={end}/>
 </section>
 <div className="timeline-composer"><div className="space-tabs" role="group" aria-label={t('Space')}><button type="button" aria-pressed={mode==='chat'} disabled={busy} onClick={()=>setMode('chat')}>{t('Chat')}</button><button type="button" aria-pressed={mode==='post'} disabled={busy} onClick={()=>setMode('post')}>{t('Post')}</button></div>
 <form onSubmit={e=>{e.preventDefault();void act(mode,text)}}>{mode==='post'?<textarea aria-label={t('Write a post')} placeholder={t('Write a post')} maxLength={2000} value={text} onChange={e=>setText(e.target.value)}/>:<input aria-label={t('Message')} placeholder={t('Message')} maxLength={500} value={text} onChange={e=>setText(e.target.value)}/>}<button disabled={busy||!text.trim()||text.length>(mode==='chat'?500:2000)}>{t(mode==='chat'?'Send':'Post')}</button></form></div></>;
 function renderPost(p:typeof posts[number]){return <section className="wall-post" key={p.id}><div className="wall-author"><span className="avatar">{(p.nickname||p.name)[0]}</span><div><strong>{p.nickname||p.name}</strong><small>{new Date(p.createdAt).toLocaleString(lang)}</small></div></div><p className="wall-body">{p.body}</p><div className="wall-actions"><button className="quiet" aria-pressed={!!p.liked} disabled={busy} onClick={()=>void act('like','',p.id)}>{p.liked?'♥':'♡'} {t('Like')} ({p.likes})</button><span>{t('Comments')} ({p.comments.length})</span></div>
 {p.comments.map(c=><div className="wall-comment" key={c.id}><strong>{c.nickname||c.name}</strong><div>{c.body}</div></div>)}
 <form className="wall-reply" onSubmit={e=>{e.preventDefault();void act('comment',comments[p.id]??'',p.id)}}><input aria-label={t('Comment')} placeholder={t('Comment')} maxLength={500} value={comments[p.id]??''} onChange={e=>setComments(old=>({...old,[p.id]:e.target.value}))}/><button disabled={busy||!comments[p.id]?.trim()}>{t('Comment')}</button></form></section>}
}
