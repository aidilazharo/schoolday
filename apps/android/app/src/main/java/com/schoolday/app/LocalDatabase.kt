package com.schoolday.app
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.util.UUID
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

data class Account(val id:String,val name:String,val email:String,val roles:String)
data class ChatSpace(val id:String,val title:String)
data class Draft(val id:String,val text:String)
class LocalDatabase(context:Context):SQLiteOpenHelper(context,"schoolday.sqlite",null,3){
    override fun onConfigure(db:SQLiteDatabase){db.setForeignKeyConstraintsEnabled(true)}
    override fun onCreate(db:SQLiteDatabase){
        val schema = """
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
        """.trimIndent()
        schema.split(";").map{it.trim()}.filter{it.isNotEmpty()&&!it.startsWith("PRAGMA")}.forEach{db.execSQL(it)}
        migrateNames(db);migrateWall(db)
    }
    override fun onUpgrade(db:SQLiteDatabase,oldVersion:Int,newVersion:Int){if(oldVersion<2)migrateNames(db);if(oldVersion<3)migrateWall(db)}
    private fun migrateWall(db:SQLiteDatabase){
        val migration="""
CREATE TABLE IF NOT EXISTS points_accounts(user_id TEXT PRIMARY KEY REFERENCES users(id),balance INTEGER NOT NULL CHECK(balance>=0));
INSERT OR IGNORE INTO points_accounts(user_id,balance) SELECT id,100 FROM users;
CREATE TABLE IF NOT EXISTS wall_posts(id TEXT PRIMARY KEY,channel_id TEXT NOT NULL,author_id TEXT NOT NULL,body TEXT NOT NULL CHECK(length(trim(body)) BETWEEN 1 AND 2000),created_at INTEGER NOT NULL,FOREIGN KEY(author_id,channel_id) REFERENCES memberships(user_id,channel_id),UNIQUE(id,channel_id));
CREATE TABLE IF NOT EXISTS wall_comments(id TEXT PRIMARY KEY,post_id TEXT NOT NULL,channel_id TEXT NOT NULL,author_id TEXT NOT NULL,body TEXT NOT NULL CHECK(length(trim(body)) BETWEEN 1 AND 500),created_at INTEGER NOT NULL,FOREIGN KEY(post_id,channel_id) REFERENCES wall_posts(id,channel_id),FOREIGN KEY(author_id,channel_id) REFERENCES memberships(user_id,channel_id));
CREATE TABLE IF NOT EXISTS wall_reactions(post_id TEXT NOT NULL,channel_id TEXT NOT NULL,user_id TEXT NOT NULL,PRIMARY KEY(post_id,user_id),FOREIGN KEY(post_id,channel_id) REFERENCES wall_posts(id,channel_id),FOREIGN KEY(user_id,channel_id) REFERENCES memberships(user_id,channel_id));
CREATE INDEX IF NOT EXISTS wall_feed ON wall_posts(channel_id,created_at);
INSERT OR IGNORE INTO wall_posts VALUES('seed-open','open','admin','Welcome to Schoolday!',1795824000000);
INSERT OR IGNORE INTO wall_posts VALUES('seed-school','school','admin','Welcome to our school community.',1795824000000);
INSERT OR IGNORE INTO wall_posts VALUES('seed-class','class','teacher','Remember your mathematics workbook tomorrow.',1795824000000);
        """.trimIndent()
        migration.split(";").map{it.trim()}.filter{it.isNotEmpty()}.forEach{db.execSQL(it)}
    }
    private fun migrateNames(db:SQLiteDatabase){
        val migration="""
CREATE TABLE IF NOT EXISTS user_nicknames(user_id TEXT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,nickname TEXT NOT NULL CHECK(length(trim(nickname))>0));
UPDATE users SET name='Muhammad Aiman Hakim' WHERE id='student' AND name='Aiman';
UPDATE users SET name='Nur Sara Amira' WHERE id='teacher' AND name='Cikgu Sara';
UPDATE users SET name='Ahmad Farid Rahman' WHERE id='parent' AND name='Encik Farid';
UPDATE users SET name='Nur Nadia Sofia' WHERE id='dual' AND name='Puan Nadia';
UPDATE users SET name='Lee Wei Ming' WHERE id='admin' AND name='School Admin';
UPDATE users SET name='Kavitha Devi' WHERE id='normal' AND name='Normal User';
        """.trimIndent()
        migration.split(";").map{it.trim()}.filter{it.isNotEmpty()}.forEach{db.execSQL(it)}
    }
    fun rows(sql:String,vararg args:String):List<List<String>> = readableDatabase.rawQuery(sql,args).use{cursor->
        buildList{while(cursor.moveToNext())add((0 until cursor.columnCount).map{cursor.getString(it)?:""})}
    }
    private val users="SELECT u.id,u.name,u.email,(SELECT group_concat(role,' + ') FROM (SELECT role FROM roles WHERE user_id=u.id ORDER BY role)) FROM users u"
    fun samples()=rows(users+" WHERE sample=1 ORDER BY rowid").map{Account(it[0],it[1],it[2],it[3])}
    fun login(email:String,password:String):Account?{
        val row=rows("SELECT id,salt,password_hash FROM users WHERE sample=0 AND email=?",email.trim().lowercase()).firstOrNull()?:return null
        val salt=row[1].chunked(2).map{it.toInt(16).toByte()}.toByteArray()
        val spec=PBEKeySpec(password.toCharArray(),salt,600000,256)
        val hash=try{SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded.joinToString(""){ "%02x".format(it.toInt() and 255) }}finally{spec.clearPassword()}
        return if(hash==row[2])rows(users+" WHERE id=?",row[0]).firstOrNull()?.let{Account(it[0],it[1],it[2],it[3])}else null
    }
    fun nickname(user:Account):String{
        val options=SchooldayNames.options(user.name)
        val saved=rows("SELECT nickname FROM user_nicknames WHERE user_id=?",user.id).firstOrNull()?.firstOrNull()
        return saved?.takeIf{it in options}?:options.firstOrNull()?:user.name
    }
    fun saveNickname(user:Account,value:String){
        val name=rows("SELECT name FROM users WHERE id=?",user.id).firstOrNull()?.firstOrNull()?:error("Account missing")
        require(value in SchooldayNames.options(name))
        writableDatabase.execSQL("INSERT OR REPLACE INTO user_nicknames VALUES(?,?)",arrayOf(user.id,value))
    }
    fun spaces(user:Account)=rows("SELECT c.id,c.title FROM channels c JOIN memberships m ON m.channel_id=c.id WHERE m.user_id=? ORDER BY c.position",user.id).map{ChatSpace(it[0],it[1])}
    fun drafts(user:Account,channel:String)=rows("SELECT id,body FROM drafts WHERE user_id=? AND channel_id=? ORDER BY created_at,id",user.id,channel).map{Draft(it[0],it[1])}
    fun write(user:Account,channel:String,text:String?){
        require(spaces(user).any{it.id==channel})
        if(text==null)writableDatabase.execSQL("DELETE FROM drafts WHERE user_id=? AND channel_id=?",arrayOf(user.id,channel))
        else writableDatabase.execSQL("INSERT INTO drafts VALUES(?,?,?,?,?)",arrayOf(UUID.randomUUID().toString(),user.id,channel,text,System.currentTimeMillis()))
    }
}
