import SwiftUI
struct WallView:View {
    let db:LocalDatabase
    let user:Account
    let channel:String
    let lang:String
    @State private var posts:[WallPost]=[]
    @State private var messages:[SpaceMessage]=[]
    @State private var isPost=false
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
                    }.padding().frame(maxWidth:680).frame(maxWidth:.infinity)
                }.onChange(of:entries.count){_,_ in proxy.scrollTo("end",anchor:.bottom)}
            }
            Divider()
            Picker(t("Space"),selection:$isPost){Text(t("Chat")).tag(false);Text(t("Post")).tag(true)}.pickerStyle(.segmented).padding(8)
            HStack {
                TextField(t(isPost ? "Write a post":"Message"),text:$text,axis:.vertical).lineLimit(isPost ? (3...6):(1...3)).textFieldStyle(.roundedBorder)
                Button(t(isPost ? "Post":"Send")){act(isPost ? "post":"chat",text)}.buttonStyle(.borderedProminent).disabled(text.trimmingCharacters(in:.whitespacesAndNewlines).isEmpty || text.count>(isPost ? 2000:500))
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
        VStack(alignment:.leading,spacing:12){
            Text(post.name).font(.headline)
            Text(Date(timeIntervalSince1970:Double(post.createdAt)/1000),style:.date).font(.caption).foregroundStyle(.secondary)
            Text(post.body).frame(maxWidth:.infinity,alignment:.leading)
            Divider()
            HStack{
                Button{act("like","",post.id)}label:{Label(t("Like")+" ("+String(post.likes)+")",systemImage:post.liked ? "heart.fill":"heart")}
                Spacer()
                Text(t("Comments")+" ("+String(post.comments.count)+")").font(.caption)
            }
            ForEach(post.comments){comment in VStack(alignment:.leading){Text(comment.name).font(.caption.bold());Text(comment.body)}.padding(8).frame(maxWidth:.infinity,alignment:.leading).background(.thinMaterial,in:RoundedRectangle(cornerRadius:8))}
            HStack{
                TextField(t("Comment"),text:Binding(get:{comments[post.id] ?? ""},set:{comments[post.id]=$0})).textFieldStyle(.roundedBorder)
                Button(t("Comment")){act("comment",comments[post.id] ?? "",post.id)}.disabled((comments[post.id] ?? "").trimmingCharacters(in:.whitespacesAndNewlines).isEmpty || (comments[post.id] ?? "").count>500)
            }
        }.padding().background(.background,in:RoundedRectangle(cornerRadius:12)).overlay(RoundedRectangle(cornerRadius:12).stroke(Color.secondary.opacity(0.2)))
    }
    private func reload(){do{posts=try db.feed(user,channel);messages=try db.spaceMessages(user,channel)}catch{failed=true}}
    private func act(_ action:String,_ body:String,_ id:String=""){do{if action=="chat"{try db.sendSpaceMessage(user,channel,body)}else{try db.wallAction(user,channel,action,body,id)};reload();if action=="post" || action=="chat"{text=""};if action=="comment"{comments[id]=""}}catch{failed=true}}
}
