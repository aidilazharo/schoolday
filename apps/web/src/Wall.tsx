import {useState} from 'react';
import type {Database} from 'sql.js';
import {feed,wallAction,type User} from './database';
import {translate,type Language} from './i18n';
export function Wall({db,user,channel,lang,busy,setBusy}:{db:Database;user:User;channel:string;lang:Language;busy:boolean;setBusy:(v:boolean)=>void}){
 const t=(s:string)=>translate(lang,s);
 const [posts,setPosts]=useState(()=>feed(db,user,channel));
 const [text,setText]=useState('');
 const [comments,setComments]=useState<Record<string,string>>({});
 const [error,setError]=useState(false);
 async function act(action:'post'|'comment'|'like',body:string,id=''){
  if(busy)return;setBusy(true);setError(false);
  try{await wallAction(db,user,channel,action,body,id);setPosts(feed(db,user,channel));if(action==='post')setText('');if(action==='comment')setComments(old=>({...old,[id]:''}))}
  catch{setError(true)}finally{setBusy(false)}
 }
 return <section className="wall-feed">
 <form className="wall-compose" onSubmit={e=>{e.preventDefault();void act('post',text)}}><textarea aria-label={t('Write a post')} placeholder={t('Write a post')} maxLength={2000} value={text} onChange={e=>setText(e.target.value)}/><button disabled={busy||!text.trim()}>{t('Post')}</button></form>
 {error&&<p role="alert">{t('Unable to save locally.')}</p>}
 {!posts.length&&<p className="wall-empty">{t('No posts yet')}</p>}
 {posts.map(p=><section className="wall-post" key={p.id}><div className="wall-author"><span className="avatar">{(p.nickname||p.name)[0]}</span><div><strong>{p.nickname||p.name}</strong><small>{new Date(p.createdAt).toLocaleString(lang)}</small></div></div><p className="wall-body">{p.body}</p><div className="wall-actions"><button className="quiet" aria-pressed={!!p.liked} disabled={busy} onClick={()=>void act('like','',p.id)}>{p.liked?'♥':'♡'} {t('Like')} ({p.likes})</button><span>{t('Comments')} ({p.comments.length})</span></div>
 {p.comments.map(c=><div className="wall-comment" key={c.id}><strong>{c.nickname||c.name}</strong><div>{c.body}</div></div>)}
 <form className="wall-reply" onSubmit={e=>{e.preventDefault();void act('comment',comments[p.id]??'',p.id)}}><input aria-label={t('Comment')} placeholder={t('Comment')} maxLength={500} value={comments[p.id]??''} onChange={e=>setComments(old=>({...old,[p.id]:e.target.value}))}/><button disabled={busy||!comments[p.id]?.trim()}>{t('Comment')}</button></form></section>)}
 </section>
}
