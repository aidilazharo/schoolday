import {useState} from 'react';
import type {Database} from 'sql.js';
import {featureRows,saveWorkspaceDraft,type User} from './database';
import {translate,type Language} from './i18n';
export function WorkspaceEditor({db,user,kind,lang,busy,setBusy}:{db:Database;user:User;kind:'Quiz'|'Certificates'|'Calendar';lang:Language;busy:boolean;setBusy:(b:boolean)=>void}){
 const t=(key:string)=>translate(lang,key);
 const [title,setTitle]=useState(''),[details,setDetails]=useState(''),[error,setError]=useState(false),[revision,setRevision]=useState(0);
 const rows=featureRows(db,"SELECT id,title,details FROM workspace_drafts WHERE owner_id=? AND kind=? ORDER BY created_at DESC,id",[user.id,kind]);
 const heading=kind==='Quiz'?'Quiz editor':kind==='Certificates'?'Certificate studio':'Event planner';
 const hint=kind==='Quiz'?'Question and answer choices':kind==='Certificates'?'Recipient and achievement':'Date, time and location';
 async function save(){if(busy||!title.trim()||!details.trim())return;setBusy(true);setError(false);try{await saveWorkspaceDraft(db,user,kind,title,details);setTitle('');setDetails('');setRevision(v=>v+1)}catch{setError(true)}finally{setBusy(false)}}
 return <div className="editor-grid" data-revision={revision}><section className="feature-card"><h2>{t(heading)}</h2><small>{t('Draft only · Not published')}</small><label>{t('Title')}<input value={title} maxLength={120} onChange={e=>setTitle(e.target.value)}/></label><label>{t(hint)}<textarea rows={6} maxLength={4000} value={details} onChange={e=>setDetails(e.target.value)}/></label><button disabled={busy||!title.trim()||!details.trim()} onClick={()=>void save()}>{t('Save draft')}</button>{error&&<p role="alert">{t('Unable to save locally.')}</p>}</section><section className="feature-card"><h2>{t('My drafts')}</h2>{!rows.length&&<small>{t('No drafts yet')}</small>}{rows.map(r=><article className="workspace-draft" key={r[0]}><strong>{r[1]}</strong><p>{r[2]}</p><small>{t('Draft only · Not published')}</small></article>)}</section></div>
}
