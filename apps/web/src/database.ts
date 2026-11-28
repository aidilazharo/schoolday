import initSqlJs, {type Database, type SqlValue} from 'sql.js';
import wasmUrl from 'sql.js/dist/sql-wasm.wasm?url';
import {nicknameOptions} from './names';
export type User = {id:string; name:string; email:string; roles:string; sample:number};
export type Channel = {id:string; title:string};
export type Draft = {id:string; text:string; createdAt:number};
const schema = "PRAGMA foreign_keys=ON;\nCREATE TABLE IF NOT EXISTS users(id TEXT PRIMARY KEY,name TEXT NOT NULL,email TEXT NOT NULL UNIQUE,sample INTEGER NOT NULL CHECK(sample IN (0,1)),salt TEXT,password_hash TEXT);\nCREATE TABLE IF NOT EXISTS roles(user_id TEXT NOT NULL REFERENCES users(id),role TEXT NOT NULL,PRIMARY KEY(user_id,role));\nCREATE TABLE IF NOT EXISTS channels(id TEXT PRIMARY KEY,title TEXT NOT NULL,position INTEGER NOT NULL);\nCREATE TABLE IF NOT EXISTS memberships(user_id TEXT NOT NULL REFERENCES users(id),channel_id TEXT NOT NULL REFERENCES channels(id),PRIMARY KEY(user_id,channel_id));\nCREATE TABLE IF NOT EXISTS drafts(id TEXT PRIMARY KEY,user_id TEXT NOT NULL,channel_id TEXT NOT NULL,body TEXT NOT NULL,created_at INTEGER NOT NULL,FOREIGN KEY(user_id,channel_id) REFERENCES memberships(user_id,channel_id));\nCREATE INDEX IF NOT EXISTS drafts_scope ON drafts(user_id,channel_id,created_at);\nINSERT OR IGNORE INTO channels VALUES ('open','Open Wall',0);\nINSERT OR IGNORE INTO channels VALUES ('school','School Wall',1);\nINSERT OR IGNORE INTO channels VALUES ('class','Class 5A Wall',2);\nINSERT OR IGNORE INTO channels VALUES ('staff','Staff Room',3);\nINSERT OR IGNORE INTO channels VALUES ('family','Family Chat',4);\nINSERT OR IGNORE INTO channels VALUES ('admin','School Administration',5);\nINSERT OR IGNORE INTO users VALUES ('student','Aiman','aiman@example.invalid',1,NULL,NULL);\nINSERT OR IGNORE INTO roles VALUES ('student','Student');\nINSERT OR IGNORE INTO memberships VALUES ('student','open');\nINSERT OR IGNORE INTO memberships VALUES ('student','school');\nINSERT OR IGNORE INTO memberships VALUES ('student','class');\nINSERT OR IGNORE INTO users VALUES ('teacher','Cikgu Sara','sara@example.invalid',1,NULL,NULL);\nINSERT OR IGNORE INTO roles VALUES ('teacher','Teacher');\nINSERT OR IGNORE INTO memberships VALUES ('teacher','open');\nINSERT OR IGNORE INTO memberships VALUES ('teacher','school');\nINSERT OR IGNORE INTO memberships VALUES ('teacher','class');\nINSERT OR IGNORE INTO memberships VALUES ('teacher','staff');\nINSERT OR IGNORE INTO users VALUES ('parent','Encik Farid','farid@example.invalid',1,NULL,NULL);\nINSERT OR IGNORE INTO roles VALUES ('parent','Parent');\nINSERT OR IGNORE INTO memberships VALUES ('parent','open');\nINSERT OR IGNORE INTO memberships VALUES ('parent','school');\nINSERT OR IGNORE INTO memberships VALUES ('parent','family');\nINSERT OR IGNORE INTO users VALUES ('dual','Puan Nadia','nadia@example.invalid',1,NULL,NULL);\nINSERT OR IGNORE INTO roles VALUES ('dual','Parent');\nINSERT OR IGNORE INTO roles VALUES ('dual','Teacher');\nINSERT OR IGNORE INTO memberships VALUES ('dual','open');\nINSERT OR IGNORE INTO memberships VALUES ('dual','school');\nINSERT OR IGNORE INTO memberships VALUES ('dual','class');\nINSERT OR IGNORE INTO memberships VALUES ('dual','staff');\nINSERT OR IGNORE INTO memberships VALUES ('dual','family');\nINSERT OR IGNORE INTO users VALUES ('admin','School Admin','admin@example.invalid',1,NULL,NULL);\nINSERT OR IGNORE INTO roles VALUES ('admin','School Admin');\nINSERT OR IGNORE INTO memberships VALUES ('admin','open');\nINSERT OR IGNORE INTO memberships VALUES ('admin','school');\nINSERT OR IGNORE INTO memberships VALUES ('admin','staff');\nINSERT OR IGNORE INTO memberships VALUES ('admin','admin');\nINSERT OR IGNORE INTO users VALUES ('normal','Normal User','member@example.invalid',0,'7b3a762d993c1ae5645c8922c2b73af0','d0e7d639ac57271cad1b068ce5b6292386493ab34b24b263f71eb434e007ad87');\nINSERT OR IGNORE INTO roles VALUES ('normal','Member');\nINSERT OR IGNORE INTO memberships VALUES ('normal','open');\nCREATE TABLE IF NOT EXISTS user_nicknames(user_id TEXT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,nickname TEXT NOT NULL CHECK(length(trim(nickname))>0));\nUPDATE users SET name='Muhammad Aiman Hakim' WHERE id='student' AND name='Aiman';\nUPDATE users SET name='Nur Sara Amira' WHERE id='teacher' AND name='Cikgu Sara';\nUPDATE users SET name='Ahmad Farid Rahman' WHERE id='parent' AND name='Encik Farid';\nUPDATE users SET name='Nur Nadia Sofia' WHERE id='dual' AND name='Puan Nadia';\nUPDATE users SET name='Lee Wei Ming' WHERE id='admin' AND name='School Admin';\nUPDATE users SET name='Kavitha Devi' WHERE id='normal' AND name='Normal User';\nCREATE TABLE IF NOT EXISTS points_accounts(user_id TEXT PRIMARY KEY REFERENCES users(id),balance INTEGER NOT NULL CHECK(balance>=0));\nINSERT OR IGNORE INTO points_accounts(user_id,balance) SELECT id,100 FROM users;\nCREATE TABLE IF NOT EXISTS wall_posts(id TEXT PRIMARY KEY,channel_id TEXT NOT NULL,author_id TEXT NOT NULL,body TEXT NOT NULL CHECK(length(trim(body)) BETWEEN 1 AND 2000),created_at INTEGER NOT NULL,FOREIGN KEY(author_id,channel_id) REFERENCES memberships(user_id,channel_id),UNIQUE(id,channel_id));\nCREATE TABLE IF NOT EXISTS wall_comments(id TEXT PRIMARY KEY,post_id TEXT NOT NULL,channel_id TEXT NOT NULL,author_id TEXT NOT NULL,body TEXT NOT NULL CHECK(length(trim(body)) BETWEEN 1 AND 500),created_at INTEGER NOT NULL,FOREIGN KEY(post_id,channel_id) REFERENCES wall_posts(id,channel_id),FOREIGN KEY(author_id,channel_id) REFERENCES memberships(user_id,channel_id));\nCREATE TABLE IF NOT EXISTS wall_reactions(post_id TEXT NOT NULL,channel_id TEXT NOT NULL,user_id TEXT NOT NULL,PRIMARY KEY(post_id,user_id),FOREIGN KEY(post_id,channel_id) REFERENCES wall_posts(id,channel_id),FOREIGN KEY(user_id,channel_id) REFERENCES memberships(user_id,channel_id));\nCREATE INDEX IF NOT EXISTS wall_feed ON wall_posts(channel_id,created_at);\nINSERT OR IGNORE INTO wall_posts VALUES('seed-open','open','admin','Welcome to Schoolday!',1795824000000);\nINSERT OR IGNORE INTO wall_posts VALUES('seed-school','school','admin','Welcome to our school community.',1795824000000);\nINSERT OR IGNORE INTO wall_posts VALUES('seed-class','class','teacher','Remember your mathematics workbook tomorrow.',1795824000000);\n";
function openStorage():Promise<IDBDatabase>{
  return new Promise((resolve,reject)=>{
    const request=indexedDB.open('schoolday-sqlite',1);
    request.onupgradeneeded=()=>request.result.createObjectStore('database');
    request.onsuccess=()=>resolve(request.result);
    request.onerror=()=>reject(request.error);
  });
}
async function bytes():Promise<Uint8Array|undefined>{
  const storage=await openStorage();
  return new Promise((resolve,reject)=>{
    const tx=storage.transaction('database','readonly');
    const r=tx.objectStore('database').get('main');
    tx.oncomplete=()=>{storage.close();resolve(r.result as Uint8Array|undefined)};
    tx.onabort=()=>{storage.close();reject(tx.error)};
  });
}
async function persist(db:Database){
  const storage=await openStorage();
  const data=db.export();
  db.run('PRAGMA foreign_keys=ON');
  return new Promise<void>((resolve,reject)=>{
    const tx=storage.transaction('database','readwrite');
    tx.objectStore('database').put(data,'main');
    tx.oncomplete=()=>{storage.close();resolve()};
    tx.onabort=()=>{storage.close();reject(tx.error)};
  });
}
function rows<T>(db:Database,sql:string,params:SqlValue[]=[]):T[]{
  const statement=db.prepare(sql);
  try{statement.bind(params);const result:T[]=[];while(statement.step())result.push(statement.getAsObject() as T);return result}
  finally{statement.free()}
}
const userQuery="SELECT u.id,u.name,u.email,u.sample,(SELECT group_concat(role, ' + ') FROM (SELECT role FROM roles WHERE user_id=u.id ORDER BY role)) AS roles FROM users u";
export async function openDatabase(){
  const SQL=await initSqlJs({locateFile:()=>wasmUrl});
  const db=new SQL.Database(await bytes());
  db.run(schema);
  await persist(db);
  return db;
}
export function samples(db:Database){return rows<User>(db,userQuery+" WHERE sample=1 ORDER BY rowid")}
export async function login(db:Database,email:string,password:string):Promise<User|undefined>{
  const u=rows<{id:string;salt:string;password_hash:string}>(db,"SELECT id,salt,password_hash FROM users WHERE sample=0 AND email=?",[email.trim().toLowerCase()])[0];
  if(!u||!u.salt)return undefined;
  const salt=Uint8Array.from(u.salt.match(/../g)!.map(x=>parseInt(x,16)));
  const key=await crypto.subtle.importKey('raw',new TextEncoder().encode(password),'PBKDF2',false,['deriveBits']);
  const hash=Array.from(new Uint8Array(await crypto.subtle.deriveBits({name:'PBKDF2',hash:'SHA-256',salt,iterations:600000},key,256)),x=>x.toString(16).padStart(2,'0')).join('');
  return hash===u.password_hash?rows<User>(db,userQuery+" WHERE id=?",[u.id])[0]:undefined;
}
export function channels(db:Database,user:User){return rows<Channel>(db,"SELECT c.id,c.title FROM channels c JOIN memberships m ON m.channel_id=c.id WHERE m.user_id=? ORDER BY c.position",[user.id])}
export function drafts(db:Database,user:User,channel:string){return rows<Draft>(db,"SELECT d.id,d.body AS text,d.created_at AS createdAt FROM drafts d JOIN memberships m ON m.user_id=d.user_id AND m.channel_id=d.channel_id WHERE d.user_id=? AND d.channel_id=? ORDER BY d.created_at,d.id",[user.id,channel])}
export async function write(db:Database,user:User,channel:string,text:string|null){
  if(!channels(db,user).some(c=>c.id===channel))throw Error('Not a member');
  const previous=db.export();
  try{
    if(text===null)db.run("DELETE FROM drafts WHERE user_id=? AND channel_id=?",[user.id,channel]);
    else db.run("INSERT INTO drafts VALUES(?,?,?,?,?)",[crypto.randomUUID(),user.id,channel,text,Date.now()]);
    await persist(db);
  }catch(e){
    // Restore only this outbox from the snapshot if durable storage fails.
    const SQL=await initSqlJs({locateFile:()=>wasmUrl});
    const old=new SQL.Database(previous);
    try{db.run("DELETE FROM drafts WHERE user_id=? AND channel_id=?",[user.id,channel]);for(const d of drafts(old,user,channel))db.run("INSERT INTO drafts VALUES(?,?,?,?,?)",[d.id,user.id,channel,d.text,d.createdAt]);}finally{old.close()}
    throw e;
  }
}

export function nickname(db:Database,user:User):string {
  const value=rows<{nickname:string}>(db,"SELECT nickname FROM user_nicknames WHERE user_id=?",[user.id])[0]?.nickname;
  const options=nicknameOptions(user.name);
  return value&&options.includes(value)?value:options[0]??user.name;
}
export async function saveNickname(db:Database,user:User,value:string){
  const account=rows<{name:string}>(db,"SELECT name FROM users WHERE id=?",[user.id])[0];
  if(!account||!nicknameOptions(account.name).includes(value))throw Error('Invalid nickname');
  const previous=rows<{nickname:string}>(db,"SELECT nickname FROM user_nicknames WHERE user_id=?",[user.id])[0];
  db.run("INSERT OR REPLACE INTO user_nicknames VALUES(?,?)",[user.id,value]);
  try{await persist(db)}catch(e){if(previous)db.run("INSERT OR REPLACE INTO user_nicknames VALUES(?,?)",[user.id,previous.nickname]);else db.run("DELETE FROM user_nicknames WHERE user_id=?",[user.id]);throw e}
}

export type WallComment={id:string;name:string;nickname:string;body:string};
export type WallPost={id:string;name:string;nickname:string;body:string;createdAt:number;likes:number;liked:number;comments:WallComment[]};
export function points(db:Database,user:User){return rows<{balance:number}>(db,"SELECT balance FROM points_accounts WHERE user_id=?",[user.id])[0]?.balance??0}
export function feed(db:Database,user:User,channel:string):WallPost[]{
 return rows<Omit<WallPost,'comments'>>(db,"SELECT p.id,u.name,COALESCE(n.nickname,'') AS nickname,p.body,p.created_at AS createdAt,(SELECT count(*) FROM wall_reactions r WHERE r.post_id=p.id) AS likes,EXISTS(SELECT 1 FROM wall_reactions r WHERE r.post_id=p.id AND r.user_id=?) AS liked FROM wall_posts p JOIN users u ON u.id=p.author_id LEFT JOIN user_nicknames n ON n.user_id=u.id JOIN memberships m ON m.channel_id=p.channel_id AND m.user_id=? WHERE p.channel_id=? ORDER BY p.created_at DESC,p.id",[user.id,user.id,channel]).map(p=>({...p,comments:rows<WallComment>(db,"SELECT c.id,u.name,COALESCE(n.nickname,'') AS nickname,c.body FROM wall_comments c JOIN users u ON u.id=c.author_id LEFT JOIN user_nicknames n ON n.user_id=u.id JOIN memberships m ON m.channel_id=c.channel_id AND m.user_id=? WHERE c.channel_id=? AND c.post_id=? ORDER BY c.created_at,c.id",[user.id,channel,p.id])}));
}
export async function wallAction(db:Database,user:User,channel:string,action:'post'|'comment'|'like',body:string,postId=''){
 if(!channels(db,user).some(c=>c.id===channel))throw Error('Not a member');
 if(action!=='post'&&!rows(db,"SELECT 1 FROM wall_posts WHERE id=? AND channel_id=?",[postId,channel]).length)throw Error('Post unavailable');
 const trimmed=body.trim(); if(action!=='like'&&(!trimmed||trimmed.length>(action==='post'?2000:500)))throw Error('Invalid text');
 const previous=db.export();db.run('PRAGMA foreign_keys=ON');
 try{
  if(action==='post')db.run("INSERT INTO wall_posts VALUES(?,?,?,?,?)",[crypto.randomUUID(),channel,user.id,trimmed,Date.now()]);
  else if(action==='comment')db.run("INSERT INTO wall_comments VALUES(?,?,?,?,?,?)",[crypto.randomUUID(),postId,channel,user.id,trimmed,Date.now()]);
  else if(rows(db,"SELECT 1 FROM wall_reactions WHERE post_id=? AND user_id=?",[postId,user.id]).length)db.run("DELETE FROM wall_reactions WHERE post_id=? AND user_id=?",[postId,user.id]);
  else db.run("INSERT INTO wall_reactions VALUES(?,?,?)",[postId,channel,user.id]);
  await persist(db);
 }catch(e){
  const SQL=await initSqlJs({locateFile:()=>wasmUrl});const old=new SQL.Database(previous);
  try{
   db.run("BEGIN");db.run("DELETE FROM wall_reactions");db.run("DELETE FROM wall_comments");db.run("DELETE FROM wall_posts");
   for(const table of ['wall_posts','wall_comments','wall_reactions'])for(const row of old.exec('SELECT * FROM '+table)[0]?.values??[])db.run('INSERT INTO '+table+' VALUES('+row.map(()=>'?').join(',')+')',row);
   db.run("COMMIT");
  }catch{db.run("ROLLBACK")}finally{old.close()}throw e;
 }
}
