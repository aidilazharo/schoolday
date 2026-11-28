import SwiftUI

struct ChatsView: View {
    private let lang="ms"
    private func t(_ key:String)->String {SchooldayLanguage.text(key,lang)}
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
            else { loginView }
        }.tint(.primary).task {
            guard db==nil else{return}
            do{let store=try LocalDatabase();accounts=try store.samples();db=store}
            catch{self.error="Unable to open local database."}
        }
    }
    private var loginView:some View {
        ScrollView {
            VStack(alignment:.leading,spacing:16) {
                Text("schoolday").font(.system(size:44,weight:.bold,design:.serif)).italic()
                Text(t("Sign in")).font(.title2.bold())
                sampleMenu
                Button(t(normal ? "Hide login form":"Normal user login")) {normal.toggle();error=""}.buttonStyle(.borderedProminent)
                if normal { loginForm }
                Text(t("Local test accounts only. Do not enter real credentials.")).font(.caption).foregroundStyle(.secondary)
                if !error.isEmpty {Text(t(error)).foregroundStyle(.red)}
            }.padding(24).frame(maxWidth:420).frame(maxWidth:.infinity)
        }.frame(maxWidth:.infinity,maxHeight:.infinity)
    }
    private var sampleMenu:some View {
        Menu(t("Select sample user")) {
            ForEach(accounts) { account in
                Button(account.name+" — "+SchooldayLanguage.roles(account.roles,lang)) {error="";user=account}
            }
        }.frame(maxWidth:.infinity,alignment:.leading).padding().background(.thinMaterial,in:RoundedRectangle(cornerRadius:10))
    }
    private var loginForm:some View {
        VStack(alignment:.leading,spacing:12){
            TextField(t("Email"),text:$email).textContentType(.username).keyboardType(.emailAddress).textInputAutocapitalization(.never).autocorrectionDisabled().textFieldStyle(.roundedBorder)
            SecureField(t("Password"),text:$password).textContentType(.password).textFieldStyle(.roundedBorder)
            Button(t("Login"),action:signIn).buttonStyle(.borderedProminent).disabled(db==nil || email.isEmpty || password.isEmpty)
        }
    }
    private func signIn(){
        do {
            if let account=try db?.login(email:email,password:password){user=account;password="";error=""}
            else{error="Invalid email or password."}
        }catch{self.error="Sign-in unavailable."}
    }

}
private struct Workspace:View {
    let db:LocalDatabase
    let user:Account
    @AppStorage private var lang:String
    @AppStorage private var country:String
    @State private var showProfile=false
    init(db:LocalDatabase,user:Account,logout:@escaping ()->Void){
        self.db=db;self.user=user;self.logout=logout
        _lang=AppStorage(wrappedValue:"ms","schoolday.language."+user.id)
        _country=AppStorage(wrappedValue:"MY","schoolday.country."+user.id)
    }
    private func t(_ key:String)->String {SchooldayLanguage.text(key,lang)}
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
            do {spaces=try db.spaces(user);if let first=spaces.first{select(first.id,open:false)}}catch{self.error=true}
        }.confirmationDialog(t("Clear local drafts?"),isPresented:$confirm){Button(t("Clear"),role:.destructive){change(nil)}}
        .alert(t("Local database unavailable"),isPresented:$error){Button(t("OK"),role:.cancel){}}
        .sheet(isPresented:$showProfile){profileView}
    }
    private var sidebar:some View {
        VStack(alignment:.leading,spacing:0){
            HStack{Text("schoolday").font(.system(size:28,weight:.bold,design:.serif)).italic();Spacer();Button(t("Logout"),action:logout)}.padding()
            Divider()
            Button {showProfile=true} label:{HStack{VStack(alignment:.leading){Text(user.name).font(.headline);Text(SchooldayLanguage.roles(user.roles,lang)).font(.caption).foregroundStyle(.secondary)};Spacer();Label(t("Profile"),systemImage:"person.crop.circle")}}.buttonStyle(.plain).padding()
            Divider()
            Text(t("Chats")).font(.title2.bold()).padding()
            ScrollView{
                ForEach(spaces){space in
                    Button{select(space.id,open:true)}label:{
                        HStack{Text(String(t(space.title).prefix(1))).font(.headline).frame(width:44,height:44).background(.thinMaterial,in:Circle());Text(t(space.title)).font(.headline);Spacer();Image(systemName:"chevron.right").font(.caption)}.padding().contentShape(Rectangle()).background(selected==space.id ? Color.primary.opacity(0.06):Color.clear)
                    }.buttonStyle(.plain)
                    Divider()
                }
            }
        }
    }
    private var profileView:some View {
        NavigationStack {
            Form {
                Section {
                    Text(user.name).font(.headline)
                    Text(user.email)
                    Text(SchooldayLanguage.roles(user.roles,lang)).foregroundStyle(.secondary)
                }
                Section {
                    HStack {Text(t("Language"));Spacer();LanguageMenu(language:$lang)}
                    HStack {Text(t("Country / region"));Spacer();CountryMenu(country:$country,language:lang)}
                }
            }.navigationTitle(t("Profile"))
             .toolbar{ToolbarItem(placement:.confirmationAction){Button(t("Done")){showProfile=false}}}
        }
    }
    private func chat(wide:Bool)->some View{
        VStack(spacing:0){
            HStack{VStack(alignment:.leading){Text(t(spaces.first(where:{$0.id==selected})?.title ?? "Chats")).font(.headline);Text(t("Local only")).font(.caption).foregroundStyle(.secondary)};Spacer();Button(t("Clear")){confirm=true}.disabled(messages.isEmpty);if !wide{Button(t("Back")){opened=false}}}.padding()
            Divider()
            if messages.isEmpty{Spacer();Text(t("No saved messages")).foregroundStyle(.secondary);Spacer()}
            else{ScrollView{LazyVStack(alignment:.trailing,spacing:12){ForEach(messages){message in VStack(alignment:.trailing){Text(message.text);Text(t("Saved locally · Not sent")).font(.caption).foregroundStyle(.secondary)}.padding().background(.thinMaterial,in:RoundedRectangle(cornerRadius:14)).frame(maxWidth:.infinity,alignment:.trailing)}}.padding()}}
            Divider()
            HStack{TextField(t("Message"),text:$text).textFieldStyle(.roundedBorder).onChange(of:text){_,v in if v.count>500{text=String(v.prefix(500))}};Button(t("Save")){change(text.trimmingCharacters(in:.whitespacesAndNewlines))}.buttonStyle(.borderedProminent).disabled(selected.isEmpty || text.trimmingCharacters(in:.whitespacesAndNewlines).isEmpty)}.padding()
        }.frame(maxWidth:.infinity,maxHeight:.infinity)
    }
    private func select(_ id:String,open:Bool){do{messages=try db.drafts(user,id);selected=id;text="";if open{opened=true}}catch{self.error=true}}
    private func change(_ value:String?){do{try db.write(user,selected,value);messages=try db.drafts(user,selected);if value != nil{text=""}}catch{self.error=true}}
}
