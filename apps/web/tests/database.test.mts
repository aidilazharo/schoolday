import {test} from 'node:test';
import assert from 'node:assert/strict';
import {DatabaseSync} from 'node:sqlite';
import {readFileSync} from 'node:fs';
import {pbkdf2Sync} from 'node:crypto';
const schema=readFileSync(new URL('../../../shared/database/schema.sql',import.meta.url),'utf8');
test('role membership and independent outbox',()=>{
 const db=new DatabaseSync(':memory:');try{
 db.exec(schema);
 assert.equal(db.prepare('SELECT count(*) AS n FROM users WHERE sample=1').get()!.n,7);
 assert.equal(db.prepare('SELECT count(*) AS n FROM roles WHERE user_id=?').get('dual')!.n,2);
 assert.equal(db.prepare('SELECT count(*) AS n FROM memberships WHERE user_id=?').get('normal')!.n,1);
 assert.throws(()=>db.prepare('INSERT INTO drafts VALUES(?,?,?,?,?)').run('bad','normal','staff','invalid',1));
 db.prepare('INSERT INTO drafts VALUES(?,?,?,?,?)').run('m','student','open','test',1);
 assert.equal(db.prepare('SELECT count(*) AS n FROM drafts WHERE user_id=?').get('parent')!.n,0);
 const user=db.prepare('SELECT salt,password_hash FROM users WHERE id=?').get('normal')!;
 assert.equal(pbkdf2Sync('SchooldayTest2026!',Buffer.from(String(user.salt),'hex'),600000,32,'sha256').toString('hex'),user.password_hash);
 }finally{db.close()}
});
