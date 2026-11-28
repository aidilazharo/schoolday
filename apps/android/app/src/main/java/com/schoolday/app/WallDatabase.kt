package com.schoolday.app
import java.util.UUID

data class WallComment(val id:String,val name:String,val body:String)
data class WallPost(val id:String,val name:String,val body:String,val createdAt:Long,val likes:Int,val liked:Boolean,val comments:List<WallComment>)
fun LocalDatabase.points(user:Account)=rows("SELECT balance FROM points_accounts WHERE user_id=?",user.id).firstOrNull()?.firstOrNull()?.toIntOrNull()?:0
fun LocalDatabase.feed(user:Account,channel:String)=rows("SELECT p.id,u.name,COALESCE(n.nickname,'') AS nickname,p.body,p.created_at AS createdAt,(SELECT count(*) FROM wall_reactions r WHERE r.post_id=p.id) AS likes,EXISTS(SELECT 1 FROM wall_reactions r WHERE r.post_id=p.id AND r.user_id=?) AS liked FROM wall_posts p JOIN users u ON u.id=p.author_id LEFT JOIN user_nicknames n ON n.user_id=u.id JOIN memberships m ON m.channel_id=p.channel_id AND m.user_id=? WHERE p.channel_id=? ORDER BY p.created_at DESC,p.id",user.id,user.id,channel).map{p->
 val comments=rows("SELECT c.id,u.name,COALESCE(n.nickname,'') AS nickname,c.body FROM wall_comments c JOIN users u ON u.id=c.author_id LEFT JOIN user_nicknames n ON n.user_id=u.id JOIN memberships m ON m.channel_id=c.channel_id AND m.user_id=? WHERE c.channel_id=? AND c.post_id=? ORDER BY c.created_at,c.id",user.id,channel,p[0]).map{c->WallComment(c[0],c[2].ifEmpty{c[1]},c[3])}
 WallPost(p[0],p[2].ifEmpty{p[1]},p[3],p[4].toLong(),p[5].toInt(),p[6]=="1",comments)
}
fun LocalDatabase.wallAction(user:Account,channel:String,action:String,body:String,postId:String=""){
 require(spaces(user).any{it.id==channel})
 if(action!="post")require(rows("SELECT 1 FROM wall_posts WHERE id=? AND channel_id=?",postId,channel).isNotEmpty())
 val text=body.trim();if(action!="like")require(text.isNotEmpty()&&text.length<=if(action=="post")2000 else 500)
 val id=UUID.randomUUID().toString();val now=System.currentTimeMillis()
 val db=writableDatabase
 db.beginTransaction()
 try{
  when(action){
   "post"->db.execSQL("INSERT INTO wall_posts VALUES(?,?,?,?,?)",arrayOf(id,channel,user.id,text,now))
   "comment"->db.execSQL("INSERT INTO wall_comments VALUES(?,?,?,?,?,?)",arrayOf(id,postId,channel,user.id,text,now))
   "like"->if(rows("SELECT 1 FROM wall_reactions WHERE post_id=? AND user_id=?",postId,user.id).isEmpty())db.execSQL("INSERT INTO wall_reactions VALUES(?,?,?)",arrayOf(postId,channel,user.id))else db.execSQL("DELETE FROM wall_reactions WHERE post_id=? AND user_id=?",arrayOf(postId,user.id))
   else->error("Unknown wall action")
  }
  db.setTransactionSuccessful()
 }finally{db.endTransaction()}
}
