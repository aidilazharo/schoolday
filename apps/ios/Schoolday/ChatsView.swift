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
                Button(account.name+" — "+(account.id=="university" ? (lang=="ms" ? "Pelajar universiti":"University student"):SchooldayLanguage.roles(account.roles,lang))) {error="";remember(account)}
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
    @Environment(\.verticalSizeClass) private var verticalSizeClass
    @State private var category="Connect"
    @State private var choosing=false
    @State private var groupOpen=false
    @State private var moduleId="Chats"
    @State private var record=""
    @State private var revision=0
    private var minor:Bool{(Int((try? db.query("SELECT age FROM demographics WHERE user_id=?",[user.id]))?.first?.first ?? "18") ?? 18)<18}
    private var module:SchoolModule{SchoolModules.visible(category,user.roles,minor:minor).first(where:{$0.id==moduleId}) ?? SchoolModules.visible(category,user.roles,minor:minor)[0]}
    private var tab:String{module.id}
    private var kind:String?{tab=="QuizDraft" ? "Quiz":tab=="CertificateDraft" ? "Certificates":tab=="EventDraft" ? "Calendar":nil}
    private var drafts:[[String]]{_ = revision; guard staff,let kind=kind else{return []};return (try? db.query("SELECT id,title,details FROM workspace_drafts WHERE owner_id=? AND kind=? ORDER BY created_at DESC,id",[user.id,kind])) ?? []}
    private var staff:Bool{user.roles.components(separatedBy:" + ").contains("Teacher") || user.roles.components(separatedBy:" + ").contains("School Admin")}
    private let tabs=SchoolModules.categories
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
            let wide=geo.size.width>=768 && verticalSizeClass != .compact
            HStack(spacing:0){
                if wide || !opened {sidebar(wide:wide).frame(width:wide ? min(max(geo.size.width*0.30,250),390):geo.size.width)}
                if wide{Divider()}
                if wide || opened{workspace(wide:wide)}
            }.onChange(of:selected){_,_ in groupOpen=false}.onChange(of:tab){_,_ in groupOpen=false}.onChange(of:wide,initial:true){_,isWide in if isWide{opened=true;choosing=false}}
        }.task{
            do {displayName=try db.nickname(user);points=try db.points(user);spaces=try db.spaces(user);if selected.isEmpty,let first=spaces.first{selected=first.id}}catch{self.error=true}
        }
        .alert(t("Local database unavailable"),isPresented:$error){Button(t("OK"),role:.cancel){}}
    }
    private func selectModule(_ m:SchoolModule,wide:Bool){
        moduleId=m.id;record="";choosing=false;opened=wide
    }
    private func selectCategory(_ name:String,wide:Bool){
        if name==category && !wide{choosing.toggle();return}
        category=name;moduleId=SchoolModules.destinations(name,user.roles,minor:minor)[0].id;record="";choosing = !wide;opened=wide
    }
    private func sidebar(wide:Bool)->some View {
        VStack(alignment:.leading,spacing:0){
            HStack{
                Text("schoolday").font(.system(size:26,weight:.bold,design:.serif)).italic()
                Spacer()
                if !wide{Button(ModuleNavigation.label(tab,lang)+" ▾"){choosing.toggle()}.font(.caption)}
                Button{category="Me";moduleId="Profile";record="";choosing=false;opened=true}label:{Image(systemName:"person.crop.circle").frame(width:44,height:44)}.accessibilityLabel(t("Profile"))
            }.padding(.horizontal,12).frame(height:66)
            Divider()
            GeometryReader{geo in
                VStack(spacing:0){
                    ScrollView{recordList.padding(8)}.frame(maxHeight:.infinity)
                    if !wide && choosing{
                        Divider()
                        moduleChooser.frame(height:min(geo.size.height*(geo.size.height<300 ? 0.80:0.72),440))
                    }
                }
            }
            Divider()
            categoryToolbar(wide:wide)
        }
    }
    private var recordList:some View {
        VStack(alignment:.leading,spacing:8){
            let tools=SchoolModules.related(category,user.roles,minor:minor,id:tab)
            if tools.count>1{ScrollView(.horizontal,showsIndicators:false){HStack{ForEach(tools){m in Button{moduleId=m.id;record=""}label:{Text(m.title).font(.caption).padding(8).background(tab==m.id ? Color.primary.opacity(0.10):Color.clear).overlay(alignment:.top){if m.status != "local"{Rectangle().fill(.red).frame(height:2)}}}.buttonStyle(.plain)}}}}
            if tab != "Chats" {
                VStack(alignment:.leading){
                    Text(module.title).font(.headline)
                    if module.status != "local"{Text(module.status=="partial" ? "Partially implemented":"Not implemented").font(.caption).foregroundStyle(.red)}
                }.frame(maxWidth:.infinity,alignment:.leading).padding(8)

            }
            if tab=="Chats"{
                ForEach(spaces){space in
                    Button{select(space.id,open:true)}label:{
                        HStack(spacing:10){GroupAvatar(channel:space.id,title:GroupBrand.title(space.id,t(space.title)));Text(GroupBrand.title(space.id,t(space.title)));Spacer();Image(systemName:"chevron.right")}
                            .padding().background(selected==space.id ? Color.primary.opacity(0.06):Color.clear)
                    }.buttonStyle(.plain)
                    Divider()
                }
            }else if kind != nil && staff {
                Button("+ "+t("Create draft")){record="";opened=true}.padding(8)
                if drafts.isEmpty{Text(t("No drafts yet")).font(.caption)}
                ForEach(drafts,id:\.self){r in
                    Button{record=r[0];opened=true}label:{
                        VStack(alignment:.leading){Text(r[1]).font(.headline);Text(r[2]).font(.caption).lineLimit(2);Text(t("Draft only · Not published")).font(.caption2)}
                            .frame(maxWidth:.infinity,alignment:.leading).padding(8)
                            .background(record==r[0] ? Color.primary.opacity(0.06):Color.clear)
                    }.buttonStyle(.plain)
                }
            }else if !module.records.isEmpty{
                ForEach(module.records,id:\.self){r in
                    Button{record=r;opened=true}label:{
                        VStack(alignment:.leading){Text(r);Text("Sample preview").font(.caption)}.frame(maxWidth:.infinity,alignment:.leading).padding(8)
                    }.buttonStyle(.plain)
                }
            }else{Button(module.title){opened=true}.padding(8)}
        }
    }
    private var moduleChooser:some View{
        VStack(spacing:0){
            HStack{Text(ModuleNavigation.label(category,lang)).font(.headline);Spacer();Button{choosing=false}label:{Image(systemName:"xmark").frame(width:44,height:44)}.accessibilityLabel(t("Close"))}.padding(.horizontal,16)
            ScrollView{
                LazyVStack(spacing:0){
                    ForEach(SchoolModules.destinations(category,user.roles,minor:minor)){m in
                        Button{selectModule(m,wide:false)}label:{
                            HStack(spacing:12){
                                VStack(spacing:3){
                                    Rectangle().fill(m.status=="local" ? Color.clear:Color.red).frame(width:22,height:2)
                                    Image(systemName:ModuleNavigation.icon(m.id)).font(.system(size:20)).frame(width:24)
                                }
                                Text(ModuleNavigation.label(m.id,lang)).font(.body)
                                Spacer()
                                Image(systemName:SchoolModules.parentID(moduleId)==m.id ? "checkmark":"chevron.right").font(.caption)
                            }.frame(minHeight:52).padding(.horizontal,12)
                            .background(SchoolModules.parentID(moduleId)==m.id ? Color.primary.opacity(0.07):Color.clear,in:RoundedRectangle(cornerRadius:8))
                            .contentShape(Rectangle())
                        }.buttonStyle(.plain).accessibilityLabel(m.title+(m.status=="local" ? "":m.status=="partial" ? " · Partially implemented":" · Not implemented")).accessibilityAddTraits(SchoolModules.parentID(moduleId)==m.id ? .isSelected:[])
                        Divider()
                    }
                }.padding(.horizontal,16)
            }
        }.padding(.top,4).background(Color(uiColor:.systemBackground))
    }
    private func moduleToolbar(_ modules:[SchoolModule],wide:Bool)->some View{
        ScrollView(.horizontal,showsIndicators:true){
            HStack(alignment:.top,spacing:4){
                ForEach(modules){m in
                    ModuleToolButton(module:m,lang:lang,selected:SchoolModules.parentID(moduleId)==m.id){selectModule(m,wide:wide)}
                }
            }.padding(.horizontal,4).padding(.vertical,3)
        }.fixedSize(horizontal:false,vertical:true).id(category)
    }
    private func categoryToolbar(wide:Bool)->some View{
        HStack(spacing:0){
            ForEach(Array(tabs.enumerated()),id:\.offset){entry in
                Button{selectCategory(entry.element,wide:wide)}label:{
                    VStack(spacing:4){
                        Image(systemName:["bubble.left","book","building.2","rosette","person.crop.circle"][entry.offset]).font(.system(size:19))
                        Text(ModuleNavigation.label(entry.element,lang)).font(.caption2).lineLimit(2).multilineTextAlignment(.center)
                    }.frame(maxWidth:.infinity,minHeight:48).foregroundStyle(category==entry.element ? Color.primary:Color.secondary)
                }.buttonStyle(.plain).accessibilityAddTraits(category==entry.element ? .isSelected:[])
            }
        }.padding(.horizontal,4)
    }
    private func workspace(wide:Bool)->some View{
        VStack(spacing:0){
            if wide{
                HStack(spacing:8){
                    if tab=="Chats"{GroupAvatar(channel:selected,title:GroupBrand.title(selected,t(spaces.first(where:{$0.id==selected})?.title ?? "Chats")),size:36)}
                    VStack(alignment:.leading,spacing:2){
                        Text(tab=="Chats" ? GroupBrand.title(selected,t(spaces.first(where:{$0.id==selected})?.title ?? "Chats")):t(module.title)).font(.subheadline.bold()).lineLimit(2)
                        if tab=="Chats"{Text(t("Local only")).font(.caption2).foregroundStyle(.secondary)}
                    }.frame(minWidth:80,maxWidth:150,alignment:.leading)
                    moduleToolbar(SchoolModules.destinations(category,user.roles,minor:minor),wide:true)
                    if tab=="Chats"{groupButton}
                }.padding(.horizontal,12).frame(height:66)
                Divider()
            }
            if tab=="Chats"{chat(wide:wide)}else{detail(wide:wide)}
        }.frame(maxWidth:.infinity,maxHeight:.infinity)
    }
    private func detail(wide:Bool)->some View{
        VStack(spacing:0){
            if !wide{HStack{Text(module.title).font(.headline);Spacer();Button(t("Back")){opened=false}}.padding();Divider()}
            if module.status != "local"{Text(module.status=="partial" ? "Partially implemented":"Not implemented").font(.caption).foregroundStyle(.red).padding(6)}
            if let kind=kind,staff{if let r=drafts.first(where:{$0[0]==record}){Form{Section(r[1]){Text(r[2]);Text(t("Draft only · Not published")).font(.caption);Button(t("Create draft")){record=""}}}}else{WorkspaceEditor(db:db,user:user,kind:kind,lang:lang,onSaved:{revision+=1}).id(tab)}}
            else if tab=="Profile"{profileView}
            else if tab=="Rewards" || tab=="Quiz"{Form{FamilyFeatures(db:db,user:user,screen:tab,onChange:refreshPoints)}.id(tab)}
            else{ModulePreview(module:module,db:db,user:user,record:record).id(tab+record)}
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
                Section{Button(t("Logout"),action:logout)}
            }
    }
    private func refreshPoints(){points=(try? db.points(user)) ?? points}
    private func chat(wide:Bool)->some View {
        VStack(spacing:0){
            if !wide{HStack{GroupAvatar(channel:selected,title:GroupBrand.title(selected,t(spaces.first(where:{$0.id==selected})?.title ?? "Chats")),size:36);VStack(alignment:.leading){Text(GroupBrand.title(selected,t(spaces.first(where:{$0.id==selected})?.title ?? "Chats"))).font(.headline);Text(t("Local only")).font(.caption).foregroundStyle(.secondary)};Spacer();groupButton;Button(t("Back")){if groupOpen{groupOpen=false}else{opened=false}}}.padding();Divider()}
            if !selected.isEmpty {ZStack{WallView(db:db,user:user,channel:selected,lang:lang).id(selected).opacity(groupOpen ? 0:1).allowsHitTesting(!groupOpen).accessibilityHidden(groupOpen);if groupOpen{GroupSettings(db:db,user:user,channel:selected,title:GroupBrand.title(selected,t(spaces.first(where:{$0.id==selected})?.title ?? "Chats")),lang:lang,onClose:{groupOpen=false}).id(selected)}}}
        }.frame(maxWidth:.infinity,maxHeight:.infinity)
    }
    private var groupButton:some View{Button{groupOpen.toggle()}label:{Image(systemName:"gearshape").frame(width:44,height:44)}.disabled(selected.isEmpty).accessibilityLabel(GroupSettings.label(lang))}
    private func select(_ id:String,open:Bool){category="Connect";moduleId="Chats";selected=id;if open{opened=true}}
}
