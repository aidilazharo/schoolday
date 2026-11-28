import SwiftUI

struct ChatsView: View {
    @State private var db:LocalDatabase?
    @State private var accounts:[Account]=[]
    @State private var user:Account?
    @State private var normal=false
    @State private var email=""
    @State private var password=""
    @State private var error=""
    var body:some View {
        Group {
            if let db=db,let user=user { Workspace(db:db,user:user){ self.user=nil;normal=false;email="";password="";error="" }.id(user.id) }
            else {
                ScrollView {
                    VStack(alignment:.leading,spacing:16){
                        Text("schoolday").font(.largeTitle.bold())
                        Text("Sign in").font(.title2.bold())
                        Menu("Select sample user"){
                            ForEach(accounts){ account in Button("\(account.name) — \(account.roles)"){error="";user=account} }
                        }.frame(maxWidth:.infinity,alignment:.leading).padding().background(.thinMaterial,in:RoundedRectangle(cornerRadius:10))
                        Button(normal ? "Hide login form":"Normal user login"){normal.toggle();error=""}.buttonStyle(.borderedProminent)
                        if normal {
                            TextField("Email",text:$email).textContentType(.username).keyboardType(.emailAddress).textInputAutocapitalization(.never).autocorrectionDisabled().textFieldStyle(.roundedBorder)
                            SecureField("Password",text:$password).textContentType(.password).textFieldStyle(.roundedBorder)
                            Button("Login"){
                                do { if let account=try db?.login(email:email,password:password){user=account;password="";error=""}else{error="Invalid email or password."} }
                                catch{error="Sign-in unavailable."}
                            }.buttonStyle(.borderedProminent).disabled(db==nil || email.isEmpty || password.isEmpty)
                        }
                        Text("Local test accounts only. Do not enter real credentials.").font(.caption).foregroundStyle(.secondary)
                        if !error.isEmpty{Text(error).foregroundStyle(.red)}
                    }.padding(24).frame(maxWidth:420).frame(maxWidth:.infinity)
                }.frame(maxWidth:.infinity,maxHeight:.infinity)
            }
        }.tint(.primary).task {
            guard db==nil else{return}
            do{let store=try LocalDatabase();accounts=try store.samples();db=store}
            catch{error="Unable to open local database."}
        }
    }
}
private struct Workspace:View {
    let db:LocalDatabase
    let user:Account
    let logout:()->Void
    @State private var spaces:[ChatSpace]=[]
    @State private var selected=""
    @State private var messages:[LocalDraft]=[]
    @State private var text=""
    @State private var opened=false
    @State private var confirm=false
    @State private var error=false
    var body:some View {
        GeometryReader{ geo in
            let wide=geo.size.width>=768
            HStack(spacing:0){
                if wide || !opened {sidebar.frame(width:wide ? min(geo.size.width*0.32,420):geo.size.width)}
                if wide{Divider()}
                if wide || opened{chat(wide:wide)}
            }
        }.task{
            do {spaces=try db.spaces(user);if let first=spaces.first{select(first.id,open:false)}}catch{error=true}
        }.confirmationDialog("Clear local drafts?",isPresented:$confirm){Button("Clear",role:.destructive){change(nil)}}
        .alert("Local database unavailable",isPresented:$error){Button("OK",role:.cancel){}}
    }
    private var sidebar:some View {
        VStack(alignment:.leading,spacing:0){
            HStack{Text("schoolday").font(.title.bold());Spacer();Button("Logout",action:logout)}.padding()
            Divider()
            VStack(alignment:.leading){Text(user.name).font(.headline);Text(user.roles).font(.caption).foregroundStyle(.secondary)}.padding()
            Divider()
            Text("Chats").font(.title2.bold()).padding()
            ScrollView{
                ForEach(spaces){space in
                    Button{select(space.id,open:true)}label:{
                        HStack{Text(String(space.title.prefix(1))).font(.headline).frame(width:44,height:44).background(.thinMaterial,in:Circle());Text(space.title).font(.headline);Spacer();Image(systemName:"chevron.right").font(.caption)}.padding().contentShape(Rectangle()).background(selected==space.id ? Color.primary.opacity(0.06):Color.clear)
                    }.buttonStyle(.plain)
                    Divider()
                }
            }
        }
    }
    private func chat(wide:Bool)->some View{
        VStack(spacing:0){
            HStack{VStack(alignment:.leading){Text(spaces.first(where:{$0.id==selected})?.title ?? "Chats").font(.headline);Text("Local only").font(.caption).foregroundStyle(.secondary)};Spacer();Button("Clear"){confirm=true}.disabled(messages.isEmpty);if !wide{Button("Back"){opened=false}}}.padding()
            Divider()
            if messages.isEmpty{Spacer();Text("No saved messages").foregroundStyle(.secondary);Spacer()}
            else{ScrollView{LazyVStack(alignment:.trailing,spacing:12){ForEach(messages){message in VStack(alignment:.trailing){Text(message.text);Text("Saved locally · Not sent").font(.caption).foregroundStyle(.secondary)}.padding().background(.thinMaterial,in:RoundedRectangle(cornerRadius:14)).frame(maxWidth:.infinity,alignment:.trailing)}}.padding()}}
            Divider()
            HStack{TextField("Message",text:$text).textFieldStyle(.roundedBorder).onChange(of:text){_,v in if v.count>500{text=String(v.prefix(500))}};Button("Save"){change(text.trimmingCharacters(in:.whitespacesAndNewlines))}.buttonStyle(.borderedProminent).disabled(selected.isEmpty || text.trimmingCharacters(in:.whitespacesAndNewlines).isEmpty)}.padding()
        }.frame(maxWidth:.infinity,maxHeight:.infinity)
    }
    private func select(_ id:String,open:Bool){do{messages=try db.drafts(user,id);selected=id;text="";if open{opened=true}}catch{error=true}}
    private func change(_ value:String?){do{try db.write(user,selected,value);messages=try db.drafts(user,selected);if value != nil{text=""}}catch{error=true}}
}
