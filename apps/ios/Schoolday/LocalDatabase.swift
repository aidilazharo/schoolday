import Foundation
import SQLite3
import CommonCrypto

struct Account: Identifiable { let id:String; let name:String; let email:String; let roles:String }
struct ChatSpace: Identifiable { let id:String; let title:String }
struct LocalDraft: Identifiable { let id:String; let text:String }

final class LocalDatabase {
    private var db: OpaquePointer?
    private let transient = unsafeBitCast(-1, to: sqlite3_destructor_type.self)
    enum Failure: Error { case database }
    init() throws {
        let url = try FileManager.default.url(for: .applicationSupportDirectory, in: .userDomainMask, appropriateFor: nil, create: true).appendingPathComponent("schoolday.sqlite")
        guard sqlite3_open(url.path, &db) == SQLITE_OK else { sqlite3_close(db); throw Failure.database }
        let schema = """
PRAGMA foreign_keys=ON;
CREATE TABLE IF NOT EXISTS users(id TEXT PRIMARY KEY,name TEXT NOT NULL,email TEXT NOT NULL UNIQUE,sample INTEGER NOT NULL CHECK(sample IN (0,1)),salt TEXT,password_hash TEXT);
CREATE TABLE IF NOT EXISTS roles(user_id TEXT NOT NULL REFERENCES users(id),role TEXT NOT NULL,PRIMARY KEY(user_id,role));
CREATE TABLE IF NOT EXISTS channels(id TEXT PRIMARY KEY,title TEXT NOT NULL,position INTEGER NOT NULL);
CREATE TABLE IF NOT EXISTS memberships(user_id TEXT NOT NULL REFERENCES users(id),channel_id TEXT NOT NULL REFERENCES channels(id),PRIMARY KEY(user_id,channel_id));
CREATE TABLE IF NOT EXISTS drafts(id TEXT PRIMARY KEY,user_id TEXT NOT NULL,channel_id TEXT NOT NULL,body TEXT NOT NULL,created_at INTEGER NOT NULL,FOREIGN KEY(user_id,channel_id) REFERENCES memberships(user_id,channel_id));
CREATE INDEX IF NOT EXISTS drafts_scope ON drafts(user_id,channel_id,created_at);
INSERT OR IGNORE INTO channels VALUES ('open','Open Wall',0);
INSERT OR IGNORE INTO channels VALUES ('school','School Wall',1);
INSERT OR IGNORE INTO channels VALUES ('class','Class 5A Wall',2);
INSERT OR IGNORE INTO channels VALUES ('staff','Staff Room',3);
INSERT OR IGNORE INTO channels VALUES ('family','Family Chat',4);
INSERT OR IGNORE INTO channels VALUES ('admin','School Administration',5);
INSERT OR IGNORE INTO users VALUES ('student','Aiman','aiman@example.invalid',1,NULL,NULL);
INSERT OR IGNORE INTO roles VALUES ('student','Student');
INSERT OR IGNORE INTO memberships VALUES ('student','open');
INSERT OR IGNORE INTO memberships VALUES ('student','school');
INSERT OR IGNORE INTO memberships VALUES ('student','class');
INSERT OR IGNORE INTO users VALUES ('teacher','Cikgu Sara','sara@example.invalid',1,NULL,NULL);
INSERT OR IGNORE INTO roles VALUES ('teacher','Teacher');
INSERT OR IGNORE INTO memberships VALUES ('teacher','open');
INSERT OR IGNORE INTO memberships VALUES ('teacher','school');
INSERT OR IGNORE INTO memberships VALUES ('teacher','class');
INSERT OR IGNORE INTO memberships VALUES ('teacher','staff');
INSERT OR IGNORE INTO users VALUES ('parent','Encik Farid','farid@example.invalid',1,NULL,NULL);
INSERT OR IGNORE INTO roles VALUES ('parent','Parent');
INSERT OR IGNORE INTO memberships VALUES ('parent','open');
INSERT OR IGNORE INTO memberships VALUES ('parent','school');
INSERT OR IGNORE INTO memberships VALUES ('parent','family');
INSERT OR IGNORE INTO users VALUES ('dual','Puan Nadia','nadia@example.invalid',1,NULL,NULL);
INSERT OR IGNORE INTO roles VALUES ('dual','Parent');
INSERT OR IGNORE INTO roles VALUES ('dual','Teacher');
INSERT OR IGNORE INTO memberships VALUES ('dual','open');
INSERT OR IGNORE INTO memberships VALUES ('dual','school');
INSERT OR IGNORE INTO memberships VALUES ('dual','class');
INSERT OR IGNORE INTO memberships VALUES ('dual','staff');
INSERT OR IGNORE INTO memberships VALUES ('dual','family');
INSERT OR IGNORE INTO users VALUES ('admin','School Admin','admin@example.invalid',1,NULL,NULL);
INSERT OR IGNORE INTO roles VALUES ('admin','School Admin');
INSERT OR IGNORE INTO memberships VALUES ('admin','open');
INSERT OR IGNORE INTO memberships VALUES ('admin','school');
INSERT OR IGNORE INTO memberships VALUES ('admin','staff');
INSERT OR IGNORE INTO memberships VALUES ('admin','admin');
INSERT OR IGNORE INTO users VALUES ('normal','Normal User','member@example.invalid',0,'7b3a762d993c1ae5645c8922c2b73af0','d0e7d639ac57271cad1b068ce5b6292386493ab34b24b263f71eb434e007ad87');
INSERT OR IGNORE INTO roles VALUES ('normal','Member');
INSERT OR IGNORE INTO memberships VALUES ('normal','open');
CREATE TABLE IF NOT EXISTS user_nicknames(user_id TEXT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,nickname TEXT NOT NULL CHECK(length(trim(nickname))>0));
UPDATE users SET name='Muhammad Aiman Hakim' WHERE id='student' AND name='Aiman';
UPDATE users SET name='Nur Sara Amira' WHERE id='teacher' AND name='Cikgu Sara';
UPDATE users SET name='Ahmad Farid Rahman' WHERE id='parent' AND name='Encik Farid';
UPDATE users SET name='Nur Nadia Sofia' WHERE id='dual' AND name='Puan Nadia';
UPDATE users SET name='Lee Wei Ming' WHERE id='admin' AND name='School Admin';
UPDATE users SET name='Kavitha Devi' WHERE id='normal' AND name='Normal User';
CREATE TABLE IF NOT EXISTS points_accounts(user_id TEXT PRIMARY KEY REFERENCES users(id),balance INTEGER NOT NULL CHECK(balance>=0));
INSERT OR IGNORE INTO points_accounts(user_id,balance) SELECT id,100 FROM users;
CREATE TABLE IF NOT EXISTS wall_posts(id TEXT PRIMARY KEY,channel_id TEXT NOT NULL,author_id TEXT NOT NULL,body TEXT NOT NULL CHECK(length(trim(body)) BETWEEN 1 AND 2000),created_at INTEGER NOT NULL,FOREIGN KEY(author_id,channel_id) REFERENCES memberships(user_id,channel_id),UNIQUE(id,channel_id));
CREATE TABLE IF NOT EXISTS wall_comments(id TEXT PRIMARY KEY,post_id TEXT NOT NULL,channel_id TEXT NOT NULL,author_id TEXT NOT NULL,body TEXT NOT NULL CHECK(length(trim(body)) BETWEEN 1 AND 500),created_at INTEGER NOT NULL,FOREIGN KEY(post_id,channel_id) REFERENCES wall_posts(id,channel_id),FOREIGN KEY(author_id,channel_id) REFERENCES memberships(user_id,channel_id));
CREATE TABLE IF NOT EXISTS wall_reactions(post_id TEXT NOT NULL,channel_id TEXT NOT NULL,user_id TEXT NOT NULL,PRIMARY KEY(post_id,user_id),FOREIGN KEY(post_id,channel_id) REFERENCES wall_posts(id,channel_id),FOREIGN KEY(user_id,channel_id) REFERENCES memberships(user_id,channel_id));
CREATE INDEX IF NOT EXISTS wall_feed ON wall_posts(channel_id,created_at);
INSERT OR IGNORE INTO wall_posts VALUES('seed-open','open','admin','Welcome to Schoolday!',1795824000000);
INSERT OR IGNORE INTO wall_posts VALUES('seed-school','school','admin','Welcome to our school community.',1795824000000);
INSERT OR IGNORE INTO wall_posts VALUES('seed-class','class','teacher','Remember your mathematics workbook tomorrow.',1795824000000);
"""
        guard sqlite3_exec(db, schema, nil, nil, nil) == SQLITE_OK else { sqlite3_close(db); db=nil; throw Failure.database }
    }
    deinit { sqlite3_close(db) }
    func query(_ sql:String, _ args:[String]=[]) throws -> [[String]] {
        var statement: OpaquePointer?
        guard sqlite3_prepare_v2(db,sql,-1,&statement,nil)==SQLITE_OK else { throw Failure.database }
        defer { sqlite3_finalize(statement) }
        for (i,value) in args.enumerated() {
            guard sqlite3_bind_text(statement,Int32(i+1),value,-1,transient)==SQLITE_OK else { throw Failure.database }
        }
        var output:[[String]]=[]
        while true {
            let result=sqlite3_step(statement)
            if result==SQLITE_DONE { return output }
            guard result==SQLITE_ROW else { throw Failure.database }
            output.append((0..<sqlite3_column_count(statement)).map { idx in
                sqlite3_column_text(statement,idx).map { String(cString:$0) } ?? ""
            })
        }
    }
    private let users = "SELECT u.id,u.name,u.email,(SELECT group_concat(role,' + ') FROM (SELECT role FROM roles WHERE user_id=u.id ORDER BY role)) FROM users u"
    func samples() throws -> [Account] { try query(users+" WHERE sample=1 ORDER BY rowid").map { Account(id:$0[0],name:$0[1],email:$0[2],roles:$0[3]) } }
    func login(email:String,password:String) throws -> Account? {
        guard let row=try query("SELECT id,salt,password_hash FROM users WHERE sample=0 AND email=?",[email.trimmingCharacters(in:.whitespacesAndNewlines).lowercased()]).first else {return nil}
        let hex=Array(row[1]); let salt=stride(from:0,to:hex.count,by:2).compactMap { UInt8(String(hex[$0...$0+1]),radix:16) }
        let secret=Array(password.utf8)
        var hash=[UInt8](repeating:0,count:32)
        let code=secret.withUnsafeBytes { pass in salt.withUnsafeBytes { s in hash.withUnsafeMutableBytes { out in
            CCKeyDerivationPBKDF(CCPBKDFAlgorithm(kCCPBKDF2),pass.baseAddress?.assumingMemoryBound(to:Int8.self),secret.count,s.baseAddress?.assumingMemoryBound(to:UInt8.self),salt.count,CCPseudoRandomAlgorithm(kCCPRFHmacAlgSHA256),600000,out.baseAddress?.assumingMemoryBound(to:UInt8.self),32)
        }}}
        guard code==kCCSuccess,hash.map({String(format:"%02x",$0)}).joined()==row[2] else {return nil}
        return try query(users+" WHERE id=?",[row[0]]).first.map {Account(id:$0[0],name:$0[1],email:$0[2],roles:$0[3])}
    }
    func nickname(_ user:Account) throws -> String {
        let options=SchooldayNames.options(user.name)
        let saved=try query("SELECT nickname FROM user_nicknames WHERE user_id=?",[user.id]).first?.first
        if let saved=saved,options.contains(saved){return saved}
        return options.first ?? user.name
    }
    func saveNickname(_ user:Account,_ value:String) throws {
        guard let name=try query("SELECT name FROM users WHERE id=?",[user.id]).first?.first,SchooldayNames.options(name).contains(value) else {throw Failure.database}
        _=try query("INSERT OR REPLACE INTO user_nicknames VALUES(?,?)",[user.id,value])
    }
    func spaces(_ user:Account) throws -> [ChatSpace] { try query("SELECT c.id,c.title FROM channels c JOIN memberships m ON m.channel_id=c.id WHERE m.user_id=? ORDER BY c.position",[user.id]).map { ChatSpace(id:$0[0],title:$0[1]) } }
    func drafts(_ user:Account,_ channel:String) throws -> [LocalDraft] { try query("SELECT id,body FROM drafts WHERE user_id=? AND channel_id=? ORDER BY created_at,id",[user.id,channel]).map {LocalDraft(id:$0[0],text:$0[1])} }
    func write(_ user:Account,_ channel:String,_ text:String?) throws {
        guard try spaces(user).contains(where:{$0.id==channel}) else {throw Failure.database}
        if let text=text { _=try query("INSERT INTO drafts VALUES(?,?,?,?,?)",[UUID().uuidString,user.id,channel,text,String(Int64(Date().timeIntervalSince1970*1000))]) }
        else { _=try query("DELETE FROM drafts WHERE user_id=? AND channel_id=?",[user.id,channel]) }
    }
}
