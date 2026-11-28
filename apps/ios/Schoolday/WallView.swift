import SwiftUI
struct WallView:View {
    let db:LocalDatabase
    let user:Account
    let channel:String
    let lang:String
    @State private var posts:[WallPost]=[]
    @State private var text=""
    @State private var comments:[String:String]=[:]
    @State private var failed=false
    private func t(_ key:String)->String{SchooldayLanguage.text(key,lang)}
    var body:some View {
        ScrollView {
            LazyVStack(alignment:.leading,spacing:16){
                VStack(alignment:.leading){
                    TextField(t("Write a post"),text:$text,axis:.vertical).lineLimit(3...6).textFieldStyle(.roundedBorder)
                    Button(t("Post")){act("post",text)}.buttonStyle(.borderedProminent).disabled(text.trimmingCharacters(in:.whitespacesAndNewlines).isEmpty || text.count>2000)
                }.padding().background(.thinMaterial,in:RoundedRectangle(cornerRadius:12))
                if posts.isEmpty {Text(t("No posts yet")).foregroundStyle(.secondary)}
                ForEach(posts){post in postCard(post)}
            }.padding().frame(maxWidth:680).frame(maxWidth:.infinity)
        }.task(id:channel){reload()}
         .alert(t("Unable to save locally."),isPresented:$failed){Button(t("OK"),role:.cancel){}}
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
    private func reload(){do{posts=try db.feed(user,channel)}catch{failed=true}}
    private func act(_ action:String,_ body:String,_ id:String=""){do{try db.wallAction(user,channel,action,body,id);reload();if action=="post"{text=""};if action=="comment"{comments[id]=""}}catch{failed=true}}
}
