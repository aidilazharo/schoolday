import SwiftUI
struct WallView:View {
    let db:LocalDatabase
    let user:Account
    let channel:String
    let lang:String
    @State private var posts:[WallPost]=[]
    @State private var messages:[SpaceMessage]=[]
    @State private var expanded:Set<String>=[]
    @State private var text=""
    @State private var comments:[String:String]=[:]
    @State private var failed=false
    private func t(_ key:String)->String{SchooldayLanguage.text(key,lang)}
    private struct Entry:Identifiable {
        let id:String;let time:Int64;let post:WallPost?;let message:SpaceMessage?
    }
    private var entries:[Entry]{
        (posts.map{Entry(id:"p:"+$0.id,time:$0.createdAt,post:$0,message:nil)} + messages.map{Entry(id:"m:"+$0.id,time:$0.createdAt,post:nil,message:$0)}).sorted{$0.time==$1.time ? $0.id<$1.id : $0.time<$1.time}
    }
    var body:some View {
        VStack(spacing:0){
            ScrollViewReader{proxy in
                ScrollView {
                    LazyVStack(alignment:.leading,spacing:12){
                        ForEach(entries){entry in
                            if let post=entry.post{postCard(post)}
                            if let message=entry.message{bubble(message)}
                        }
                        Color.clear.frame(height:1).id("end")
                    }.frame(maxWidth:.infinity,alignment:.leading).padding(12)
                }.onChange(of:entries.count){_,_ in proxy.scrollTo("end",anchor:.bottom)}
            }
            Divider()
            HStack {
                TextField(t("Message"),text:$text,axis:.vertical).lineLimit(1...6).textFieldStyle(.roundedBorder)
                Button(t("Send")){act("post",text)}.frame(minHeight:44).buttonStyle(.borderedProminent).disabled(text.trimmingCharacters(in:.whitespacesAndNewlines).isEmpty || text.count>2000)
            }.padding()
        }.task(id:channel){reload()}
         .alert(t("Unable to save locally."),isPresented:$failed){Button(t("OK"),role:.cancel){}}
    }
    private func bubble(_ message:SpaceMessage)->some View{
        VStack(alignment:.leading,spacing:4){
            if message.authorId != user.id{Text(message.name).font(.caption.bold())}
            Text(message.body)
            HStack{Text(Date(timeIntervalSince1970:Double(message.createdAt)/1000),style:.time);Text(t(message.draft ? "Saved locally · Not sent":"Local only"))}.font(.caption2).foregroundStyle(.secondary)
        }.padding(12).background(.thinMaterial,in:RoundedRectangle(cornerRadius:12)).frame(maxWidth:.infinity,alignment:message.authorId==user.id ? .trailing:.leading)
    }
    private func postCard(_ post:WallPost)->some View{
        VStack(alignment:.leading,spacing:6){
            if post.authorId != user.id {Text(post.name).font(.caption.bold())}
            Text(post.body).fixedSize(horizontal:false,vertical:true)
            HStack{Text(Date(timeIntervalSince1970:Double(post.createdAt)/1000),style:.time);Image(systemName:"clock").accessibilityLabel(t("Local only"))}.font(.caption2).foregroundStyle(.secondary)
            HStack(spacing:12){
                Button{act("like","",post.id)}label:{Label(String(post.likes),systemImage:post.liked ? "heart.fill":"heart").frame(minHeight:44)}
                Button{if expanded.contains(post.id){expanded.remove(post.id)}else{expanded.insert(post.id)}}label:{Label(String(post.comments.count),systemImage:"bubble.left").accessibilityLabel(t("Comments")+" "+String(post.comments.count)).frame(minHeight:44)}
            }.font(.caption).buttonStyle(.borderless)
            if expanded.contains(post.id){
                ForEach(post.comments){comment in VStack(alignment:.leading){Text(comment.name).font(.caption.bold());Text(comment.body)}.padding(8).frame(maxWidth:.infinity,alignment:.leading).background(.background,in:RoundedRectangle(cornerRadius:8))}
                HStack{
                    TextField(t("Comment"),text:Binding(get:{comments[post.id] ?? ""},set:{comments[post.id]=$0})).textFieldStyle(.roundedBorder)
                    Button(t("Send")){act("comment",comments[post.id] ?? "",post.id)}.disabled((comments[post.id] ?? "").trimmingCharacters(in:.whitespacesAndNewlines).isEmpty || (comments[post.id] ?? "").count>500)
                }
            }
        }.padding(8).frame(maxWidth:post.body.count>180 || expanded.contains(post.id) ? 600:420,alignment:.leading).background(.thinMaterial,in:RoundedRectangle(cornerRadius:12)).frame(maxWidth:.infinity,alignment:post.authorId==user.id ? .trailing:.leading)
    }
    private func reload(){do{posts=try db.feed(user,channel);messages=try db.spaceMessages(user,channel)}catch{failed=true}}
    private func act(_ action:String,_ body:String,_ id:String=""){do{if action=="chat"{try db.sendSpaceMessage(user,channel,body)}else{try db.wallAction(user,channel,action,body,id)};reload();if action=="post" || action=="chat"{text=""};if action=="comment"{comments[id]=""}}catch{failed=true}}
}
