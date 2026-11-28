import {test} from 'node:test';
import assert from 'node:assert/strict';
import {nicknameOptions} from '../src/names.ts';
import {DatabaseSync} from 'node:sqlite';
import {readFileSync} from 'node:fs';
test('one or two existing name parts only',()=>{assert.deepEqual(nicknameOptions('Muhammad Aiman Hakim'),['Muhammad','Aiman','Hakim','Muhammad Aiman','Muhammad Hakim','Aiman Hakim']);assert.deepEqual(nicknameOptions('  王小明  '),['王小明']);assert.deepEqual(nicknameOptions('Lee Lee Ming'),['Lee','Ming','Lee Lee','Lee Ming']);assert.ok(!nicknameOptions('Nur Sara Amira').includes('Sara Queen'));assert.deepEqual(nicknameOptions(''),[])});
test('nickname migration preserves identity and separates users',()=>{const db=new DatabaseSync(':memory:');try{const schema=readFileSync(new URL('../../../shared/database/schema.sql',import.meta.url),'utf8');db.exec(schema);db.prepare("INSERT INTO user_nicknames VALUES (?,?)").run('student','Aiman Hakim');db.exec(schema);assert.equal(db.prepare("SELECT name FROM users WHERE id='student'").get()?.name,'Muhammad Aiman Hakim');assert.equal(db.prepare("SELECT nickname FROM user_nicknames WHERE user_id='student'").get()?.nickname,'Aiman Hakim');assert.equal(db.prepare("SELECT nickname FROM user_nicknames WHERE user_id='teacher'").get(),undefined)}finally{db.close()}});
