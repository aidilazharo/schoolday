import SwiftUI
struct GroupSettings:View{
 let db:LocalDatabase;let user:Account;let channel:String;let title:String;let lang:String;let onClose:()->Void
 static func label(_ lang:String)->String{["en":"Group settings","ms":"Tetapan kumpulan","zh":"群组设置","ta":"குழு அமைப்புகள்"][lang] ?? "Group settings"}
 private let sections:[[String]]=[["info","Group information","Group name;Description;Visibility","Save group information"],
["members","Members","",""],
["invite","Invitations & join requests","Member or invitation code;Invitation expiry;Join request note","Send invitation"],
["roles","Administrators & roles","Member;Proposed role;Reason","Assign role"],
["election","Admin election","",""],
["recall","Administrator recall","Administrator;Reason for recall","Start recall vote"],
["polls","Group polls","Question;Option 1;Option 2;Closing date","Publish poll"],
["permissions","Permissions","",""],
["rules","Group rules","Rules;Member responsibilities","Save rules"],
["announcements","Announcements & pinned messages","Announcement;Message reference","Pin announcement"],
["calls","Group calls","Meeting title;Start time","Start call"],
["files","Media & documents","File name;Description","Share file"],
["events","Group events","Event title;Date and time;Location","Create event"],
["tasks","Group tasks","Task;Assigned member;Due date","Assign task"],
["quizzes","Group quizzes","Quiz title;Question;Answer choices","Assign quiz"],
["attendance","Attendance","Session;Date;Member reference","Record attendance"],
["certificates","Certificates","Recipient;Achievement;Issuer","Issue certificate"],
["notes","Shared notes","Title;Note","Save note"],
["search","Search messages","Search text","Search messages"],
["notifications","Notifications","Notification preference;Mute duration","Save preference"],
["activity","Activity log","",""],
["safety","Report & moderation","Member or message reference;Reason","Submit report"],
["archive","Archive group","Reason","Archive group"],
["leave","Leave group","Reason (optional)","Leave group"]]
 private let policies:[String:String]=["open":"Community election with independent safety oversight","secondary":"Institution-appointed administrator","school":"Institution-appointed administrator","class":"Assigned educator; representatives may be elected","staff":"Institution-appointed administrator","family":"Verified family permissions","admin":"Institution-appointed administrator","university":"Institution-appointed administrator","uni-course":"Assigned lecturer; representatives may be elected"]
 private let permissions=["Send messages","Invite members","Edit group information","Remove members","Create polls","Start group video call"]
 private let actions=["Remove member","Ban member","Temporarily mute","Assign moderator","Restore membership"]
 @State private var section=""
 @State private var members:[[String]]=[]
 @State private var search=""
 @State private var target=""
 @State private var action="Remove member"
 @State private var values:[String:String]=[:]
 @State private var period="24"
 @State private var term="90"
 @State private var seats="3"
 @State private var quorum="50"
 @State private var secret=true
 @State private var recall=true
 @State private var candidate=""
 @State private var voted=false
 @State private var failed=false
 private let groups:[[String]]=[["Information","info","rules","notifications"],["Members","members","invite","roles"],["Governance","election","recall","polls"],["Shared resources","announcements","files","notes","search"],["Group activities","calls","events","tasks","quizzes","attendance","certificates"],["Safety & administration","permissions","activity","safety","archive","leave"]]
 private var allowed:[[String]]{sections.filter{($0[0] != "recall" || channel=="open") && ($0[0] != "election" || ["open","class","uni-course"].contains(channel))}}
 private var active:[String]?{allowed.first{$0[0]==section}}
 private func t(_ key:String)->String{SchooldayLanguage.text(key,lang)}
 private func choose(_ id:String){section=id;values=[:];target="";search="";candidate="";voted=false}
 var body:some View{
  VStack(spacing:0){
   HStack{VStack(alignment:.leading){Text(Self.label(lang)).font(.headline);Text(title+" · "+String(members.count)+" members").font(.caption)};Spacer();Button(t("Back"),action:onClose)}.padding(.horizontal,12).padding(.vertical,8)
   Divider()
   GeometryReader{geo in
    let split=geo.size.width>=620
    HStack(spacing:0){
     if split || section.isEmpty{menu.frame(width:split ? min(geo.size.width*0.34,250):geo.size.width)}
     if split{Divider()}
     if split || !section.isEmpty{detail(split:split).frame(maxWidth:.infinity,maxHeight:.infinity)}
    }
   }
  }.background(Color(uiColor:.systemBackground)).task(id:channel){do{members=try db.query("SELECT u.id,u.name,COALESCE((SELECT group_concat(r.role,' + ') FROM roles r WHERE r.user_id=u.id),'Member'),CASE WHEN d.age>=18 THEN '1' ELSE '0' END FROM memberships m JOIN users u ON u.id=m.user_id LEFT JOIN demographics d ON d.user_id=u.id WHERE m.channel_id=? AND EXISTS(SELECT 1 FROM memberships access WHERE access.user_id=? AND access.channel_id=m.channel_id) ORDER BY u.name,u.id",[channel,user.id])}catch{failed=true}}.alert(t("Local database unavailable"),isPresented:$failed){Button(t("OK"),role:.cancel){}}
 }
 private var menu:some View{
  ScrollView{LazyVStack(alignment:.leading,spacing:0){
   HStack{GroupAvatar(channel:channel,title:title,size:56);Text(title).font(.headline)}.padding(12)
   Text(policies[channel] ?? "Institution or group policy determines authority").font(.caption).padding(.horizontal,12).padding(.bottom,12)
   ForEach(groups,id:\.self){group in
    DisclosureGroup(group[0]){ForEach(allowed.filter{group.dropFirst().contains($0[0])},id:\.self){r in
    Button{choose(r[0])}label:{HStack{Text(r[1]);Spacer();Image(systemName:"chevron.right").font(.caption)}.padding(12).frame(minHeight:48).background(section==r[0] ? Color.primary.opacity(0.07):Color.clear).overlay(alignment:.top){if r[0] != "members"{Rectangle().fill(.red).frame(height:2)}}}.buttonStyle(.plain)
    Divider()
   }}.padding(.horizontal,8)
   }
  }}
 }
 private func detail(split:Bool)->some View{
  Form{
   if let row=active{
    Section{
     if !split{Button("‹ "+t("Back")){choose("")}}
     Text(row[1]).font(.headline)
     if section != "members"{Text("Development preview · Not implemented").font(.caption).foregroundStyle(.red)}
    }
    if section=="members"{memberSection}
    else if section=="election"{electionSection}
    else if section=="permissions"{Section{ForEach(permissions,id:\.self){p in Picker(p,selection:Binding(get:{values[p] ?? "Admins only"},set:{values[p]=$0})){ForEach(["All members","Moderators & admins","Admins only"],id:\.self){Text($0).tag($0)}}};blocked("Save permissions")}}
    else if section=="activity"{Text("No authenticated group governance events are available.")}
    else{Section{ForEach(row[2].split(separator:";").map(String.init),id:\.self){f in TextField(f,text:Binding(get:{values[f] ?? ""},set:{values[f]=$0}),axis:.vertical)};blocked(row[3])}}
   }else{Text("Choose a group function. Red marks indicate development previews.")}
  }
 }
 private var memberSection:some View{
  Section("Local member directory"){
   TextField("Search members",text:$search)
   Button("+ Add member / QR invitation"){choose("invite")}
   ForEach(members.filter{search.isEmpty || $0[1].localizedCaseInsensitiveContains(search)},id:\.self){r in
    Button{target=r[0]}label:{HStack{VStack(alignment:.leading){Text(r[1]+(r[0]==user.id ? " (You)":""));Text(r[2]+" · Group member").font(.caption).foregroundStyle(.secondary)};Spacer();Image(systemName:"ellipsis")}}.buttonStyle(.plain)
   }
   if !target.isEmpty{
    Text(members.first(where:{$0[0]==target})?[1] ?? "").font(.headline)
    Text("Membership action preview. No permission changes.").font(.caption).foregroundStyle(.red)
    Picker("Action",selection:$action){ForEach(actions,id:\.self){Text($0).tag($0)}}
    TextField("Reason",text:Binding(get:{values["reason"] ?? ""},set:{values["reason"]=$0}),axis:.vertical)
    blocked(action)
   }
  }
 }
 private func options(_ title:String,_ value:Binding<String>,_ choices:[String])->some View{
  Picker(title,selection:value){ForEach(choices,id:\.self){Text($0).tag($0)}}
 }
 private var electionSection:some View{
  Group{
   Section{
    Text(channel=="open" ? "Community administrator election":"Representative election only; institutional or family authority cannot be changed.")
    options("Voting period (hours)",$period,["24","48","72"])
    options("Term (days)",$term,["30","90","180","365"])
    options("Seats",$seats,["1","3","5"])
    options("Quorum (%)",$quorum,["25","50","75","100"])
    Toggle("Secret ballot (proposed)",isOn:$secret);Toggle("Allow recall vote",isOn:$recall)
    blocked("Start election")
   }
   Section("Test ballot"){
    Text("Sample candidates. No nominations, votes or permissions are saved.").font(.caption)
    ForEach(members.filter{$0[3]=="1"},id:\.self){r in Button{candidate=r[0]}label:{HStack{Text(r[1]);Spacer();if candidate==r[0]{Image(systemName:"checkmark")}}}.disabled(voted)}
    Button(voted ? "Preview vote recorded":"Submit preview vote"){voted=true}.disabled(candidate.isEmpty || voted)
    if voted{Button("Reset preview"){voted=false;candidate=""}}
    Text("Real results require reconciled, authenticated ballots.").font(.caption)
   }
  }
 }
 private func blocked(_ label:String)->some View{Button(label+" · Not implemented"){}.disabled(true)}
}
