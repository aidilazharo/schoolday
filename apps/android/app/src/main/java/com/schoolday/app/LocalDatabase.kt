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
class LocalDatabase(context:Context):SQLiteOpenHelper(context,"schoolday.sqlite",null,6){
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
        migrateNames(db);migrateWall(db);migrateTimeline(db);migrateFamily(db);migrateUnified(db)
    }
    override fun onUpgrade(db:SQLiteDatabase,oldVersion:Int,newVersion:Int){if(oldVersion<2)migrateNames(db);if(oldVersion<3)migrateWall(db);if(oldVersion<4)migrateTimeline(db);if(oldVersion<5)migrateFamily(db);if(oldVersion<6)migrateUnified(db)}
    private fun migrateUnified(db:SQLiteDatabase){db.execSQL("INSERT OR IGNORE INTO wall_posts(id,channel_id,author_id,body,created_at) SELECT 'chat:'||id,channel_id,author_id,body,created_at FROM space_messages;")}
    private fun migrateFamily(db:SQLiteDatabase){
        listOf(
            """CREATE TABLE IF NOT EXISTS demographics(user_id TEXT PRIMARY KEY REFERENCES users(id),age INTEGER NOT NULL CHECK(age BETWEEN 5 AND 120))""",
            """INSERT OR IGNORE INTO users VALUES('child8','Nur Hana Rahman','child8@example.invalid',1,NULL,NULL)""",
            """INSERT OR IGNORE INTO roles VALUES('child8','Student')""",
            """INSERT OR IGNORE INTO memberships VALUES('child8','open')""",
            """INSERT OR IGNORE INTO memberships VALUES('child8','school')""",
            """INSERT OR IGNORE INTO memberships VALUES('child8','family')""",
            """INSERT OR IGNORE INTO space_messages VALUES('hello-child8','open','child8','Hello everyone!',1795827600000)""",
            """INSERT OR IGNORE INTO wall_posts VALUES('post-child8','open','child8','Looking forward to school activities!',1795827660000)""",
            """INSERT OR IGNORE INTO users VALUES('child15','Muhammad Danish Rahman','child15@example.invalid',1,NULL,NULL)""",
            """INSERT OR IGNORE INTO roles VALUES('child15','Student')""",
            """INSERT OR IGNORE INTO memberships VALUES('child15','open')""",
            """INSERT OR IGNORE INTO memberships VALUES('child15','school')""",
            """INSERT OR IGNORE INTO memberships VALUES('child15','family')""",
            """INSERT OR IGNORE INTO space_messages VALUES('hello-child15','open','child15','Hello everyone!',1795827600000)""",
            """INSERT OR IGNORE INTO wall_posts VALUES('post-child15','open','child15','Looking forward to school activities!',1795827660000)""",
            """INSERT OR IGNORE INTO memberships VALUES('student','family')""",
            """INSERT OR IGNORE INTO demographics VALUES('student',11),('child8',8),('child15',15),('teacher',34),('parent',40),('dual',38),('admin',45),('normal',30)""",
            """CREATE TABLE IF NOT EXISTS family_links(parent_id TEXT NOT NULL REFERENCES users(id),child_id TEXT NOT NULL REFERENCES users(id),PRIMARY KEY(parent_id,child_id),CHECK(parent_id<>child_id))""",
            """INSERT OR IGNORE INTO family_links VALUES('parent','student'),('dual','student'),('parent','child8'),('dual','child8'),('parent','child15'),('dual','child15')""",
            """INSERT OR IGNORE INTO points_accounts SELECT id,100 FROM users""",
            """CREATE TABLE IF NOT EXISTS avatars(user_id TEXT PRIMARY KEY REFERENCES users(id),style TEXT NOT NULL CHECK(style IN ('initials','star','leaf','book','cat','rocket')))""",
            """CREATE TABLE IF NOT EXISTS quiz_permissions(child_id TEXT PRIMARY KEY REFERENCES users(id),enabled INTEGER NOT NULL CHECK(enabled IN (0,1)),parent_id TEXT NOT NULL,FOREIGN KEY(parent_id,child_id) REFERENCES family_links(parent_id,child_id))""",
            """CREATE TABLE IF NOT EXISTS quiz_questions(id TEXT PRIMARY KEY,min_age INTEGER,max_age INTEGER,prompt TEXT,a TEXT,b TEXT,c TEXT,d TEXT,correct INTEGER)""",
            """INSERT OR IGNORE INTO quiz_questions VALUES('q8',5,9,'What is 7 + 5?','10','11','12','13',2),('q11',10,12,'What is 3 × 8?','18','21','24','28',2),('q15',13,17,'Solve 2x + 6 = 20.','5','6','7','8',2),('qadult',18,120,'Which is renewable energy?','Coal','Oil','Solar','Gas',2)""",
            """CREATE TABLE IF NOT EXISTS quiz_attempts(user_id TEXT REFERENCES users(id),question_id TEXT REFERENCES quiz_questions(id),status TEXT NOT NULL CHECK(status IN ('correct','skipped')),PRIMARY KEY(user_id,question_id))""",
            """CREATE TABLE IF NOT EXISTS identity_tokens(user_id TEXT PRIMARY KEY REFERENCES users(id),token TEXT NOT NULL UNIQUE)""",
            """INSERT OR IGNORE INTO identity_tokens SELECT id,lower(hex(randomblob(16))) FROM users""",
            """CREATE TABLE IF NOT EXISTS identity_requests(actor TEXT REFERENCES users(id),target TEXT REFERENCES users(id),kind TEXT CHECK(kind IN ('friend','parent')),status TEXT DEFAULT 'pending' CHECK(status IN ('pending','accepted')),PRIMARY KEY(actor,target,kind),CHECK(actor<>target))""",
            """CREATE TABLE IF NOT EXISTS attendance_demo(student TEXT REFERENCES users(id),day TEXT NOT NULL,staff TEXT REFERENCES users(id),PRIMARY KEY(student,day))""",
            """CREATE TABLE IF NOT EXISTS point_activity(id TEXT PRIMARY KEY,user_id TEXT REFERENCES users(id),delta INTEGER NOT NULL,reason TEXT NOT NULL,created_at INTEGER NOT NULL)""",
            """CREATE TABLE IF NOT EXISTS feature_actions(id TEXT PRIMARY KEY,actor TEXT NOT NULL REFERENCES users(id),kind TEXT NOT NULL CHECK(kind IN ('buy','transfer','avatar','permission','answer','skip','friend','parent','attendance','accept')),target TEXT NOT NULL DEFAULT '',amount INTEGER NOT NULL DEFAULT 0,value TEXT NOT NULL DEFAULT '',created_at INTEGER NOT NULL)""",
            """CREATE TRIGGER IF NOT EXISTS feature_buy AFTER INSERT ON feature_actions WHEN NEW.kind='buy' BEGIN SELECT CASE WHEN NOT (NEW.amount IN (100,200,500) AND EXISTS(SELECT 1 FROM demographics WHERE user_id=NEW.actor AND age>=18)) THEN RAISE(ABORT,'Adult demo purchase only') END;UPDATE points_accounts SET balance=balance+NEW.amount WHERE user_id=NEW.actor AND balance+NEW.amount<=1000000000; SELECT CASE WHEN NOT (changes()=1) THEN RAISE(ABORT,'Invalid balance') END;INSERT INTO point_activity VALUES(NEW.id||'',NEW.actor,NEW.amount,'Demo purchase',NEW.created_at); END""",
            """CREATE TRIGGER IF NOT EXISTS feature_transfer AFTER INSERT ON feature_actions WHEN NEW.kind='transfer' BEGIN SELECT CASE WHEN NOT (EXISTS(SELECT 1 FROM family_links f JOIN roles r ON r.user_id=f.parent_id AND r.role='Parent' WHERE f.parent_id=NEW.actor AND f.child_id=NEW.target) AND NEW.amount BETWEEN 1 AND 1000000) THEN RAISE(ABORT,'Linked parent required') END;UPDATE points_accounts SET balance=balance-NEW.amount WHERE user_id=NEW.actor AND balance>=NEW.amount; SELECT CASE WHEN NOT (changes()=1) THEN RAISE(ABORT,'Insufficient points') END;UPDATE points_accounts SET balance=balance+NEW.amount WHERE user_id=NEW.target AND balance+NEW.amount<=1000000000; SELECT CASE WHEN NOT (changes()=1) THEN RAISE(ABORT,'Invalid balance') END;INSERT INTO point_activity VALUES(NEW.id||'-out',NEW.actor,-NEW.amount,'Transfer to child',NEW.created_at);INSERT INTO point_activity VALUES(NEW.id||'-in',NEW.target,NEW.amount,'Transfer from parent',NEW.created_at); END""",
            """CREATE TRIGGER IF NOT EXISTS feature_avatar AFTER INSERT ON feature_actions WHEN NEW.kind='avatar' BEGIN SELECT CASE WHEN NOT (NEW.value IN ('initials','star','leaf','book','cat','rocket') AND NEW.value<>COALESCE((SELECT style FROM avatars WHERE user_id=NEW.actor),'initials')) THEN RAISE(ABORT,'Choose a different avatar') END;UPDATE points_accounts SET balance=balance-10 WHERE user_id=NEW.actor AND balance>=10; SELECT CASE WHEN NOT (changes()=1) THEN RAISE(ABORT,'Insufficient points') END;INSERT OR REPLACE INTO avatars VALUES(NEW.actor,NEW.value);INSERT INTO point_activity VALUES(NEW.id||'',NEW.actor,-10,'Avatar change',NEW.created_at); END""",
            """CREATE TRIGGER IF NOT EXISTS feature_permission AFTER INSERT ON feature_actions WHEN NEW.kind='permission' BEGIN SELECT CASE WHEN NOT (EXISTS(SELECT 1 FROM family_links f JOIN roles r ON r.user_id=f.parent_id AND r.role='Parent' WHERE f.parent_id=NEW.actor AND f.child_id=NEW.target) AND NEW.amount IN (0,1)) THEN RAISE(ABORT,'Linked parent required') END;INSERT OR REPLACE INTO quiz_permissions VALUES(NEW.target,NEW.amount,NEW.actor); END""",
            """CREATE TRIGGER IF NOT EXISTS feature_answer AFTER INSERT ON feature_actions WHEN NEW.kind='answer' BEGIN SELECT CASE WHEN NOT (EXISTS(SELECT 1 FROM quiz_questions q JOIN demographics d ON d.user_id=NEW.actor AND d.age BETWEEN q.min_age AND q.max_age WHERE q.id=NEW.target) AND NOT EXISTS(SELECT 1 FROM quiz_attempts WHERE user_id=NEW.actor AND question_id=NEW.target) AND EXISTS(SELECT 1 FROM quiz_questions WHERE id=NEW.target AND correct=NEW.amount)) THEN RAISE(ABORT,'Incorrect answer or quiz unavailable') END;INSERT INTO quiz_attempts VALUES(NEW.actor,NEW.target,'correct'); END""",
            """CREATE TRIGGER IF NOT EXISTS feature_skip AFTER INSERT ON feature_actions WHEN NEW.kind='skip' BEGIN SELECT CASE WHEN NOT (EXISTS(SELECT 1 FROM quiz_questions q JOIN demographics d ON d.user_id=NEW.actor AND d.age BETWEEN q.min_age AND q.max_age WHERE q.id=NEW.target) AND NOT EXISTS(SELECT 1 FROM quiz_attempts WHERE user_id=NEW.actor AND question_id=NEW.target) AND EXISTS(SELECT 1 FROM quiz_permissions WHERE child_id=NEW.actor AND enabled=1)) THEN RAISE(ABORT,'Parent permission required') END;UPDATE points_accounts SET balance=balance-5 WHERE user_id=NEW.actor AND balance>=5; SELECT CASE WHEN NOT (changes()=1) THEN RAISE(ABORT,'Insufficient points') END;INSERT INTO quiz_attempts VALUES(NEW.actor,NEW.target,'skipped');INSERT INTO point_activity VALUES(NEW.id||'',NEW.actor,-5,'Quiz skip',NEW.created_at); END""",
            """CREATE TRIGGER IF NOT EXISTS feature_friend AFTER INSERT ON feature_actions WHEN NEW.kind='friend' BEGIN SELECT CASE WHEN NOT (NEW.actor<>NEW.target AND EXISTS(SELECT 1 FROM identity_tokens WHERE user_id=NEW.target AND token=NEW.value)) THEN RAISE(ABORT,'Unknown identity') END;INSERT OR IGNORE INTO identity_requests VALUES(NEW.actor,NEW.target,'friend','pending'); END""",
            """CREATE TRIGGER IF NOT EXISTS feature_parent AFTER INSERT ON feature_actions WHEN NEW.kind='parent' BEGIN SELECT CASE WHEN NOT (NEW.actor<>NEW.target AND EXISTS(SELECT 1 FROM identity_tokens WHERE user_id=NEW.target AND token=NEW.value) AND EXISTS(SELECT 1 FROM roles WHERE user_id=NEW.actor AND role='Parent') AND EXISTS(SELECT 1 FROM roles WHERE user_id=NEW.target AND role='Student')) THEN RAISE(ABORT,'Parent request requires student identity') END;INSERT OR IGNORE INTO identity_requests VALUES(NEW.actor,NEW.target,'parent','pending'); END""",
            """CREATE TRIGGER IF NOT EXISTS feature_attendance AFTER INSERT ON feature_actions WHEN NEW.kind='attendance' BEGIN SELECT CASE WHEN NOT (NEW.actor<>NEW.target AND EXISTS(SELECT 1 FROM identity_tokens WHERE user_id=NEW.target AND token=NEW.value) AND EXISTS(SELECT 1 FROM roles WHERE user_id=NEW.actor AND role IN ('Teacher','School Admin')) AND EXISTS(SELECT 1 FROM roles WHERE user_id=NEW.target AND role='Student') AND EXISTS(SELECT 1 FROM memberships WHERE user_id=NEW.actor AND channel_id='school') AND EXISTS(SELECT 1 FROM memberships WHERE user_id=NEW.target AND channel_id='school')) THEN RAISE(ABORT,'School staff and student required') END;INSERT OR IGNORE INTO attendance_demo VALUES(NEW.target,date(NEW.created_at/1000,'unixepoch','localtime'),NEW.actor); END""",
            """CREATE TRIGGER IF NOT EXISTS feature_accept AFTER INSERT ON feature_actions WHEN NEW.kind='accept' BEGIN SELECT CASE WHEN NOT (EXISTS(SELECT 1 FROM identity_requests WHERE actor=NEW.target AND target=NEW.actor AND kind='friend' AND status='pending')) THEN RAISE(ABORT,'Incoming friend request required') END;UPDATE identity_requests SET status='accepted' WHERE actor=NEW.target AND target=NEW.actor AND kind='friend'; END"""
        ).forEach{db.execSQL(it)}
    }
    private fun migrateTimeline(db:SQLiteDatabase){
        val migration="""
CREATE TABLE IF NOT EXISTS space_messages(id TEXT PRIMARY KEY,channel_id TEXT NOT NULL,author_id TEXT NOT NULL,body TEXT NOT NULL CHECK(length(trim(body)) BETWEEN 1 AND 500),created_at INTEGER NOT NULL,FOREIGN KEY(author_id,channel_id) REFERENCES memberships(user_id,channel_id));
CREATE INDEX IF NOT EXISTS space_message_order ON space_messages(channel_id,created_at);
INSERT OR IGNORE INTO space_messages VALUES('sample-chat-0','open','student','Hi everyone! Looking forward to sports day.',1795824120000);
INSERT OR IGNORE INTO space_messages VALUES('sample-chat-1','open','teacher','Welcome! Please keep our community friendly.',1795824240000);
INSERT OR IGNORE INTO space_messages VALUES('sample-chat-2','open','normal','Hello, glad to join the community.',1795824360000);
INSERT OR IGNORE INTO space_messages VALUES('sample-chat-3','open','parent','Thanks for the updates.',1795824480000);
INSERT OR IGNORE INTO space_messages VALUES('sample-chat-4','open','dual','Happy to help with school activities.',1795824600000);
INSERT OR IGNORE INTO space_messages VALUES('sample-chat-5','open','admin','Please check the school calendar for announcements.',1795824720000);
INSERT OR IGNORE INTO space_messages VALUES('sample-chat-6','school','admin','The school activity schedule is ready.',1795824840000);
INSERT OR IGNORE INTO space_messages VALUES('sample-chat-7','school','parent','Thank you for sharing.',1795824960000);
INSERT OR IGNORE INTO space_messages VALUES('sample-chat-8','school','student','Can we join the science club?',1795825080000);
INSERT OR IGNORE INTO space_messages VALUES('sample-chat-9','school','teacher','Yes, please register with your class teacher.',1795825200000);
INSERT OR IGNORE INTO space_messages VALUES('sample-chat-10','school','dual','I can help with the registration.',1795825320000);
INSERT OR IGNORE INTO space_messages VALUES('sample-chat-11','class','student','Good morning, teacher!',1795825440000);
INSERT OR IGNORE INTO space_messages VALUES('sample-chat-12','class','teacher','Good morning. Please bring your workbook.',1795825560000);
INSERT OR IGNORE INTO space_messages VALUES('sample-chat-13','class','dual','Remember to complete page 25.',1795825680000);
INSERT OR IGNORE INTO space_messages VALUES('sample-chat-14','class','student','Thank you, teacher!',1795825800000);
INSERT OR IGNORE INTO space_messages VALUES('sample-chat-15','family','parent','See you after school.',1795825920000);
INSERT OR IGNORE INTO space_messages VALUES('sample-chat-16','family','dual','I will pick up the books on the way.',1795826040000);
INSERT OR IGNORE INTO space_messages VALUES('sample-chat-17','staff','teacher','I have prepared the lesson materials.',1795826160000);
INSERT OR IGNORE INTO space_messages VALUES('sample-chat-18','staff','dual','I can review them this afternoon.',1795826280000);
INSERT OR IGNORE INTO space_messages VALUES('sample-chat-19','staff','admin','Thank you for coordinating.',1795826400000);
INSERT OR IGNORE INTO space_messages VALUES('sample-chat-20','admin','admin','The next administration meeting is on Monday.',1795826520000);
INSERT OR IGNORE INTO wall_posts VALUES('sample-post-0','open','student','Science club: our team has finished the model!',1795824300000);
INSERT OR IGNORE INTO wall_posts VALUES('sample-post-1','open','normal','Community notice: happy to join the school activity group.',1795824540000);
INSERT OR IGNORE INTO wall_posts VALUES('sample-post-2','open','parent','Volunteer invitation: families are welcome to help at the next event.',1795824780000);
INSERT OR IGNORE INTO wall_posts VALUES('sample-post-3','school','dual','Reminder: bring reusable water bottles for school activities.',1795825020000);
INSERT OR IGNORE INTO wall_posts VALUES('sample-post-4','class','teacher','Mathematics assignment: complete page 25 before the next lesson.',1795825260000);
INSERT OR IGNORE INTO wall_posts VALUES('sample-post-5','staff','admin','Staff announcement: please review the upcoming activity plan.',1795825500000);
INSERT OR IGNORE INTO wall_posts VALUES('sample-post-6','family','parent','Family reminder: bring the library books tomorrow.',1795825740000);
INSERT OR IGNORE INTO wall_posts VALUES('sample-post-7','admin','admin','Administration update: review the school calendar.',1795825980000);
INSERT OR IGNORE INTO wall_posts VALUES('sample-post-8','staff','teacher','Lesson resources are ready for the next meeting.',1795826220000);
INSERT OR IGNORE INTO wall_comments VALUES('sample-comment-0','sample-post-4','class','student','Thank you, teacher.',1795826400000);
INSERT OR IGNORE INTO wall_comments VALUES('sample-comment-1','sample-post-2','open','dual','I would be happy to help.',1795826460000);
CREATE TABLE IF NOT EXISTS seed_markers(id TEXT PRIMARY KEY);
INSERT OR IGNORE INTO wall_reactions SELECT 'sample-post-4','class','student' WHERE NOT EXISTS(SELECT 1 FROM seed_markers WHERE id='timeline-v4');
INSERT OR IGNORE INTO wall_reactions SELECT 'sample-post-2','open','normal' WHERE NOT EXISTS(SELECT 1 FROM seed_markers WHERE id='timeline-v4');
INSERT OR IGNORE INTO seed_markers VALUES('timeline-v4');
        """.trimIndent()
        migration.split(";").map{it.trim()}.filter{it.isNotEmpty()}.forEach{db.execSQL(it)}
    }
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
    fun accountById(id:String)=rows(users+" WHERE id=?",id).firstOrNull()?.let{Account(it[0],it[1],it[2],it[3])}
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
