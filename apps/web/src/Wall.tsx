import {useState,useRef,useEffect} from 'react';
import type {Database} from 'sql.js';
import {feed,wallAction,spaceMessages,type User} from './database';
import {translate,type Language} from './i18n';
export function Wall({db,user,channel,lang,busy,setBusy}:{db:Database;user:User;channel:string;lang:Language;busy:boolean;setBusy:(v:boolean)=>void}){
 const t=(s:string)=>translate(lang,s);
 const [posts,setPosts]=useState(()=>feed(db,user,channel));
 const [drafts]=useState(()=>spaceMessages(db,user,channel));
 const [text,setText]=useState('');
 const [expanded,setExpanded]=useState<Record<string,boolean>>({});
 const [comments,setComments]=useState<Record<string,string>>({});
 const [error,setError]=useState(false);
 const end=useRef<HTMLDivElement>(null);
 useEffect(()=>{const feed=end.current?.parentElement;if(feed)feed.scrollTop=feed.scrollHeight},[posts.length,drafts.length]);
 const items=[...posts.map(p=>({kind:'post' as const,id:'p:'+p.id,time:p.createdAt,p})),...drafts.map(m=>({kind:'draft' as const,id:'m:'+m.id,time:m.createdAt,m}))].sort((a,b)=>a.time-b.time||a.id.localeCompare(b.id));
 const time=(ms:number)=>new Date(ms).toLocaleTimeString(lang,{hour:'2-digit',minute:'2-digit'});
 async function act(action:'post'|'comment'|'like',body:string,id=''){
  if(busy)return;setBusy(true);setError(false);
  try{await wallAction(db,user,channel,action,body,id);setPosts(feed(db,user,channel));if(action==='post')setText('');if(action==='comment')setComments(old=>({...old,[id]:''}))}
  catch{setError(true)}finally{setBusy(false)}
 }
 return <><section className="wall-feed unified-feed">
 {error&&<p role="alert">{t('Unable to save locally.')}</p>}
 {!items.length&&<p className="wall-empty">{t('No saved messages')}</p>}
 {items.map(item=>item.kind==='draft'?<div key={item.id} className="timeline-message own"><div className="message-bubble"><div>{item.m.body}</div><small>{time(item.time)} · {t('Saved locally · Not sent')}</small></div></div>:renderMessage(item.p))}
 <div ref={end}/>
 </section>
 <div className="timeline-composer"><form onSubmit={e=>{e.preventDefault();void act('post',text)}}><textarea aria-label={t('Message')} placeholder={t('Message')} rows={1} maxLength={2000} value={text} onChange={e=>setText(e.target.value)} onKeyDown={e=>{if(e.key==='Enter'&&!e.shiftKey&&!e.nativeEvent.isComposing){e.preventDefault();if(text.trim()&&!busy)void act('post',text)}}}/><button disabled={busy||!text.trim()}>{t('Send')}</button></form></div></>;
 function renderMessage(p:typeof posts[number]){
  const own=p.authorId===user.id,open=!!expanded[p.id],long=p.body.length>180||p.body.includes('\n');
  return <div className={'timeline-message '+(own?'own':'other')} key={p.id}><div className={'message-bubble social-message '+(long||open?'expanded-message':'')}>
   {!own&&<strong>{p.nickname||p.name}</strong>}<div className="message-body">{p.body}</div><small>{time(p.createdAt)} · {t('Local only')}</small>
   <div className="message-actions"><button type="button" aria-pressed={!!p.liked} aria-label={t('Like')} disabled={busy} onClick={()=>void act('like','',p.id)}>{p.liked?'♥':'♡'} {p.likes||''}</button><button type="button" aria-expanded={open} onClick={()=>setExpanded(old=>({...old,[p.id]:!open}))}>{t('Comments')} {p.comments.length||''} {open?'▴':'▾'}</button></div>
   {open&&<div className="message-thread">{p.comments.map(c=><div className="wall-comment" key={c.id}><strong>{c.nickname||c.name}</strong><div>{c.body}</div></div>)}<form className="wall-reply" onSubmit={e=>{e.preventDefault();void act('comment',comments[p.id]??'',p.id)}}><input aria-label={t('Comment')} placeholder={t('Comment')} maxLength={500} value={comments[p.id]??''} onChange={e=>setComments(old=>({...old,[p.id]:e.target.value}))}/><button disabled={busy||!comments[p.id]?.trim()}>{t('Send')}</button></form></div>}
  </div></div>
 }
}
