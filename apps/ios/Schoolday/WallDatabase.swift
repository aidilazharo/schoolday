import Foundation

struct WallComment:Identifiable {let id:String;let name:String;let body:String}
struct WallPost:Identifiable {let id:String;let authorId:String;let name:String;let body:String;let createdAt:Int64;let likes:Int;let liked:Bool;let comments:[WallComment]}
extension LocalDatabase {
    func points(_ user:Account)throws->Int {Int(try query("SELECT balance FROM points_accounts WHERE user_id=?",[user.id]).first?.first ?? "") ?? 0}
    func feed(_ user:Account,_ channel:String)throws->[WallPost] {
        try query("SELECT p.id,u.name,COALESCE(n.nickname,'') AS nickname,p.body,p.created_at AS createdAt,(SELECT count(*) FROM wall_reactions r WHERE r.post_id=p.id) AS likes,EXISTS(SELECT 1 FROM wall_reactions r WHERE r.post_id=p.id AND r.user_id=?) AS liked,p.author_id AS authorId FROM wall_posts p JOIN users u ON u.id=p.author_id LEFT JOIN user_nicknames n ON n.user_id=u.id JOIN memberships m ON m.channel_id=p.channel_id AND m.user_id=? WHERE p.channel_id=? ORDER BY p.created_at DESC,p.id",[user.id,user.id,channel]).map{p in
            let comments=try query("SELECT c.id,u.name,COALESCE(n.nickname,'') AS nickname,c.body FROM wall_comments c JOIN users u ON u.id=c.author_id LEFT JOIN user_nicknames n ON n.user_id=u.id JOIN memberships m ON m.channel_id=c.channel_id AND m.user_id=? WHERE c.channel_id=? AND c.post_id=? ORDER BY c.created_at,c.id",[user.id,channel,p[0]]).map{c in WallComment(id:c[0],name:c[2].isEmpty ? c[1]:c[2],body:c[3])}
            return WallPost(id:p[0],authorId:p[7],name:p[2].isEmpty ? p[1]:p[2],body:p[3],createdAt:Int64(p[4]) ?? 0,likes:Int(p[5]) ?? 0,liked:p[6]=="1",comments:comments)
        }
    }
    func wallAction(_ user:Account,_ channel:String,_ action:String,_ body:String,_ postId:String="")throws {
        guard try spaces(user).contains(where:{$0.id==channel}) else {throw Failure.database}
        if action != "post" {guard !(try query("SELECT 1 FROM wall_posts WHERE id=? AND channel_id=?",[postId,channel])).isEmpty else {throw Failure.database}}
        let text=body.trimmingCharacters(in:.whitespacesAndNewlines)
        if action != "like" {guard !text.isEmpty,text.count <= (action=="post" ? 2000:500) else {throw Failure.database}}
        let id=UUID().uuidString, now=String(Int64(Date().timeIntervalSince1970*1000))
        switch action {
        case "post": _=try query("INSERT INTO wall_posts VALUES(?,?,?,?,?)",[id,channel,user.id,text,now])
        case "comment": _=try query("INSERT INTO wall_comments VALUES(?,?,?,?,?,?)",[id,postId,channel,user.id,text,now])
        case "like":
            if try query("SELECT 1 FROM wall_reactions WHERE post_id=? AND user_id=?",[postId,user.id]).isEmpty {_=try query("INSERT INTO wall_reactions VALUES(?,?,?)",[postId,channel,user.id])}
            else {_=try query("DELETE FROM wall_reactions WHERE post_id=? AND user_id=?",[postId,user.id])}
        default: throw Failure.database
        }
    }
}

struct SpaceMessage:Identifiable {let id:String;let authorId:String;let name:String;let body:String;let createdAt:Int64;let draft:Bool}
extension LocalDatabase {
    func spaceMessages(_ user:Account,_ channel:String)throws->[SpaceMessage] {
        try query("SELECT s.id,s.author_id AS authorId,u.name,COALESCE(n.nickname,'') AS nickname,s.body,s.created_at AS createdAt,0 AS draft FROM space_messages s JOIN users u ON u.id=s.author_id LEFT JOIN user_nicknames n ON n.user_id=u.id JOIN memberships m ON m.channel_id=s.channel_id AND m.user_id=? WHERE s.channel_id=? AND NOT EXISTS(SELECT 1 FROM wall_posts p WHERE p.id='chat:'||s.id) UNION ALL SELECT d.id,d.user_id,u.name,COALESCE(n.nickname,''),d.body,d.created_at,1 FROM drafts d JOIN users u ON u.id=d.user_id LEFT JOIN user_nicknames n ON n.user_id=u.id JOIN memberships m ON m.user_id=d.user_id AND m.channel_id=d.channel_id WHERE d.user_id=? AND d.channel_id=? ORDER BY 6,1",[user.id,channel,user.id,channel]).map{r in SpaceMessage(id:r[0],authorId:r[1],name:r[3].isEmpty ? r[2]:r[3],body:r[4],createdAt:Int64(r[5]) ?? 0,draft:r[6]=="1")}
    }
    func sendSpaceMessage(_ user:Account,_ channel:String,_ body:String)throws {
        let text=body.trimmingCharacters(in:.whitespacesAndNewlines)
        guard !text.isEmpty,text.count<=500,try spaces(user).contains(where:{$0.id==channel}) else{throw Failure.database}
        _=try query("INSERT INTO space_messages VALUES(?,?,?,?,?)",[UUID().uuidString,channel,user.id,text,String(Int64(Date().timeIntervalSince1970*1000))])
    }
}
