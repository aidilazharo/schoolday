import SwiftUI

struct ChatsView: View {
    private let lang="ms"
    private func t(_ key:String)->String {SchooldayLanguage.text(key,lang)}
    @State private var db:LocalDatabase?
    @State private var accounts:[Account]=[]
    @State private var user:Account?
    @AppStorage("schoolday.session.account") private var sessionAccount=""
    @State private var normal=false
    @State private var email=""
    @State private var password=""
    @State private var error=""
    var body:some View {
        Group {
            if let db=db,let user=user { Workspace(db:db,user:user){ sessionAccount="";self.user=nil;normal=false;email="";password="";error="" }.id(user.id) }
            else { loginView }
        }.tint(.primary).task {
            guard db==nil else{return}
            do{let store=try LocalDatabase();accounts=try store.samples();db=store;if !sessionAccount.isEmpty{user=try store.accountById(sessionAccount);if user==nil{sessionAccount=""}}}
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
                Button(account.name+" — "+SchooldayLanguage.roles(account.roles,lang)) {error="";remember(account)}
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
    private func remember(_ account:Account){sessionAccount=account.id;user=account}
    private func signIn(){
        do {
            if let account=try db?.login(email:email,password:password){remember(account);password="";error=""}
            else{error="Invalid email or password."}
        }catch{self.error="Sign-in unavailable."}
    }

}
private struct Workspace:View {
    let db:LocalDatabase
    let user:Account
    @AppStorage private var lang:String
    @AppStorage private var country:String
    @State private var tab="Chats"
    @State private var showDrafts=false
    private var staff:Bool{user.roles.components(separatedBy:" + ").contains("Teacher") || user.roles.components(separatedBy:" + ").contains("School Admin")}
    private let tabs=["Chats","Calendar","Quiz","Rewards","Certificates","Profile"]
    @State private var points=0
    @State private var displayName=""
    init(db:LocalDatabase,user:Account,logout:@escaping ()->Void){
        self.db=db;self.user=user;self.logout=logout
        _lang=AppStorage(wrappedValue:"ms","schoolday.language."+user.id)
        _country=AppStorage(wrappedValue:"MY","schoolday.country."+user.id)
    }
    private func t(_ key:String)->String {SchooldayLanguage.text(key,lang)}
    let logout:()->Void
    @State private var spaces:[ChatSpace]=[]
    @State private var selected=""
    @State private var opened=false
    @State private var error=false
    var body:some View {
        GeometryReader{ geo in
            let wide=geo.size.width>=768
            HStack(spacing:0){
                if wide || !opened {sidebar.frame(width:wide ? min(max(geo.size.width*0.30,250),390):geo.size.width)}
                if wide{Divider()}
                if wide || opened{if tab=="Chats"{chat(wide:wide)}else{detail(wide:wide)}}
            }
        }.task{
            do {displayName=try db.nickname(user);points=try db.points(user);spaces=try db.spaces(user);if let first=spaces.first{select(first.id,open:false)}}catch{self.error=true}
        }
        .alert(t("Local database unavailable"),isPresented:$error){Button(t("OK"),role:.cancel){}}
    }
    private var sidebar:some View {
        VStack(alignment:.leading,spacing:0){
            HStack{Text("schoolday").font(.system(size:26,weight:.bold,design:.serif)).italic();Spacer();Button(t("Logout"),action:logout)}.padding()
            Divider()
            ScrollView{
                if tab=="Chats"{
                    ForEach(spaces){space in
                        Button{select(space.id,open:true)}label:{HStack{Text(String(t(space.title).prefix(1))).font(.headline).frame(width:44,height:44).background(.thinMaterial,in:Circle());Text(t(space.title)).font(.headline);Spacer();Image(systemName:"chevron.right")}.padding().contentShape(Rectangle()).background(selected==space.id ? Color.primary.opacity(0.06):Color.clear)}.buttonStyle(.plain)
                        Divider()
                    }
                }else{
                    Button{showDrafts=false;opened=true}label:{HStack{Text(t(tab=="Quiz" ? "My quizzes":tab=="Profile" ? "Personal information":tab));Spacer();Image(systemName:"chevron.right")}.padding().frame(maxWidth:.infinity,alignment:.leading).background(!showDrafts ? Color.primary.opacity(0.06):Color.clear)}.buttonStyle(.plain)
                    if staff && ["Quiz","Certificates","Calendar"].contains(tab){Button{showDrafts=true;opened=true}label:{HStack{Text(t("Create draft"));Spacer();Image(systemName:"chevron.right")}.padding().background(showDrafts ? Color.primary.opacity(0.06):Color.clear)}.buttonStyle(.plain)}
                }
            }
            Divider()
            HStack(spacing:0){
                ForEach(Array(tabs.enumerated()),id:\.offset){entry in
                    Button{tab=entry.element;showDrafts=false;opened=false}label:{VStack(spacing:4){Image(systemName:["bubble.left","calendar","list.bullet.clipboard","star","rosette","person.crop.circle"][entry.offset]).font(.system(size:19));Text(t(entry.element)).font(.system(size:9)).lineLimit(2).multilineTextAlignment(.center)}.frame(maxWidth:.infinity,minHeight:48).foregroundStyle(tab==entry.element ? Color.primary:Color.secondary)}.buttonStyle(.plain)
                }
            }.padding(.horizontal,4)
        }
    }
    private func detail(wide:Bool)->some View{
        VStack(spacing:0){
            HStack{Text(t(showDrafts ? "Create draft":tab)).font(.headline);Spacer();if !wide{Button(t("Back")){opened=false}}}.padding()
            Divider()
            if showDrafts && staff && ["Quiz","Certificates","Calendar"].contains(tab){WorkspaceEditor(db:db,user:user,kind:tab,lang:lang).id(tab)}
            else if tab=="Profile"{profileView}
            else if tab=="Rewards" || tab=="Quiz"{Form{FamilyFeatures(db:db,user:user,screen:tab,onChange:refreshPoints)}.id(tab)}
            else{Spacer();Text(t(tab=="Certificates" ? "No certificates available.":"No events available.")).foregroundStyle(.secondary);Spacer()}
        }.frame(maxWidth:.infinity,maxHeight:.infinity)
    }
    private var nicknameMenu:some View {
        VStack(alignment:.leading,spacing:6) {
            Picker(t("Nickname"),selection:Binding(get:{displayName},set:{value in
                do{try db.saveNickname(user,value);displayName=value}catch{self.error=true}
            })) {
                ForEach(SchooldayNames.options(user.name),id:\.self){option in Text(option).tag(option)}
            }
            Text(t("Choose one or two parts of your full name.")).font(.caption).foregroundStyle(.secondary)
        }
    }
    private var profileView:some View {
        Form {
                Section {
                    Text(t("Full name")).font(.caption).foregroundStyle(.secondary)
                    Text(user.name).font(.headline)
                    nicknameMenu
                    Text(user.email)
                    Text(t("Points balance")+": "+String(points))
                    Text(SchooldayLanguage.roles(user.roles,lang)).foregroundStyle(.secondary)
                }
                Section {
                    HStack {Text(t("Language"));Spacer();LanguageMenu(language:$lang)}
                    HStack {Text(t("Country / region"));Spacer();CountryMenu(country:$country,language:lang)}
                }
                FamilyFeatures(db:db,user:user,screen:"Profile",onChange:refreshPoints)
            }
    }
    private func refreshPoints(){points=(try? db.points(user)) ?? points}
    private func chat(wide:Bool)->some View {
        VStack(spacing:0){
            HStack{VStack(alignment:.leading){Text(t(spaces.first(where:{$0.id==selected})?.title ?? "Chats")).font(.headline);Text(t("Local only")).font(.caption).foregroundStyle(.secondary)};Spacer();if !wide{Button(t("Back")){opened=false}}}.padding()
            Divider()
            if !selected.isEmpty {WallView(db:db,user:user,channel:selected,lang:lang).id(selected)}
        }.frame(maxWidth:.infinity,maxHeight:.infinity)
    }
    private func select(_ id:String,open:Bool){tab="Chats";selected=id;if open{opened=true}}
}
