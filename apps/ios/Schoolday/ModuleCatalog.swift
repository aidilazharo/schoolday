import SwiftUI
struct SchoolModule:Identifiable{
 let cat:String;let id:String;let title:String;let status:String;let role:String;let records:[String];let fields:[String]
}
enum SchoolModules{
 static let categories=["Connect","Learn","Campus","Achievements","Me"]
 static let all:[SchoolModule]=[
SchoolModule(cat:"Connect",id:"Chats",title:"Chats",status:"local",role:"all",records:[],fields:[]),
SchoolModule(cat:"Connect",id:"Calls",title:"Voice & video calls",status:"preview",role:"all",records:["Learning group","Educator"],fields:["Contact","Call type"]),
SchoolModule(cat:"Connect",id:"Classroom",title:"Live classes",status:"preview",role:"all",records:["Mathematics lesson","Research seminar"],fields:["Class title","Schedule","Meeting link"]),
SchoolModule(cat:"Connect",id:"Files",title:"Files & media",status:"preview",role:"all",records:["Lesson notes.pdf","Science project.jpg"],fields:["File name","Description"]),
SchoolModule(cat:"Connect",id:"Alerts",title:"Emergency alerts",status:"preview",role:"all",records:["Institution closure drill","Weather advisory"],fields:["Notice","Audience","Message"]),
SchoolModule(cat:"Connect",id:"Offline",title:"Offline & LoRa",status:"preview",role:"all",records:["Bluetooth","Wi-Fi","LoRa bridge"],fields:["Device","Transport"]),
SchoolModule(cat:"Learn",id:"Quiz",title:"Quiz",status:"local",role:"all",records:[],fields:[]),
SchoolModule(cat:"Learn",id:"Gate",title:"Quiz Unlock",status:"partial",role:"student",records:[],fields:[]),
SchoolModule(cat:"Learn",id:"Courses",title:"Subjects & courses",status:"preview",role:"all",records:["Mathematics Year 5","Research methodology"],fields:["Course code","Title","Education level"]),
SchoolModule(cat:"Learn",id:"Materials",title:"Learning materials",status:"preview",role:"all",records:["Workbook","Research guide"],fields:["Resource","Subject","File"]),
SchoolModule(cat:"Learn",id:"Assignments",title:"Assignments",status:"preview",role:"all",records:["Science project","Essay draft"],fields:["Assignment","Due date","Submission"]),
SchoolModule(cat:"Learn",id:"QuizDraft",title:"Quiz editor",status:"partial",role:"staff",records:[],fields:[]),
SchoolModule(cat:"Learn",id:"Assessment",title:"Assessment & exams",status:"preview",role:"all",records:["PBD mastery","UASA","Semester assessment"],fields:["Assessment","Curriculum","Rubric"]),
SchoolModule(cat:"Learn",id:"Grades",title:"Grades & progress",status:"preview",role:"all",records:["Mathematics","Science"],fields:["Subject","Grade","Feedback"]),
SchoolModule(cat:"Learn",id:"Research",title:"Research & supervision",status:"preview",role:"all",records:["Proposal defence","Literature review","Viva"],fields:["Research title","Milestone","Target date"]),
SchoolModule(cat:"Learn",id:"Internship",title:"Practical training & internship",status:"preview",role:"all",records:["Workshop safety","Industry placement"],fields:["Placement","Competency","Evidence"]),
SchoolModule(cat:"Campus",id:"Calendar",title:"Calendar",status:"preview",role:"all",records:["Science exhibition","Mathematics assessment"],fields:["Event","Date","Venue"]),
SchoolModule(cat:"Campus",id:"EventDraft",title:"Event planner",status:"partial",role:"staff",records:[],fields:[]),
SchoolModule(cat:"Campus",id:"Attendance",title:"Attendance",status:"partial",role:"all",records:["Morning class","Online seminar"],fields:["Session","Date","Student identity"]),
SchoolModule(cat:"Campus",id:"Timetable",title:"Timetable",status:"preview",role:"all",records:["Mathematics 08:00","Science 10:00"],fields:["Subject","Time","Room"]),
SchoolModule(cat:"Campus",id:"Institutions",title:"Institutions & enrolment",status:"preview",role:"all",records:["Sample primary school","Sample university"],fields:["Institution","Programme","Student ID"]),
SchoolModule(cat:"Campus",id:"Activities",title:"Clubs, sports & esports",status:"preview",role:"all",records:["Science club","Esports tournament"],fields:["Activity","Team","Date"]),
SchoolModule(cat:"Campus",id:"Library",title:"Library & learning resources",status:"preview",role:"all",records:["SPBT textbook","Research handbook"],fields:["Book title","Resource ID"]),
SchoolModule(cat:"Campus",id:"Services",title:"Student services",status:"preview",role:"all",records:["Transport","Hostel","Canteen"],fields:["Service","Request details"]),
SchoolModule(cat:"Campus",id:"Welfare",title:"Welfare & accessibility",status:"preview",role:"all",records:["Counselling","Learning support","Financial aid"],fields:["Support type","Preferred time"]),
SchoolModule(cat:"Campus",id:"Administration",title:"Institution administration",status:"preview",role:"staff",records:["Institution roster","Community meeting","Group placement"],fields:["Record type","Title","Details"]),
SchoolModule(cat:"Campus",id:"University",title:"Academic administration",status:"preview",role:"all",records:["Course registration","Final year project","Credits"],fields:["Programme","Course code","Semester"]),
SchoolModule(cat:"Achievements",id:"Rewards",title:"Rewards",status:"local",role:"all",records:[],fields:[]),
SchoolModule(cat:"Achievements",id:"Certificates",title:"Certificates",status:"preview",role:"all",records:["Science participation","Internship completion"],fields:["Certificate ID","Issuer"]),
SchoolModule(cat:"Achievements",id:"CertificateDraft",title:"Certificate studio",status:"partial",role:"staff",records:[],fields:[]),
SchoolModule(cat:"Achievements",id:"Portfolio",title:"Portfolio & co-curricular activities",status:"preview",role:"all",records:["Science project","Football club"],fields:["Achievement","Category","Evidence"]),
SchoolModule(cat:"Achievements",id:"Credentials",title:"Micro-credentials & transcripts",status:"preview",role:"all",records:["Digital literacy","Academic transcript"],fields:["Credential","Issuer","Reference"]),
SchoolModule(cat:"Achievements",id:"Career",title:"Career & alumni",status:"preview",role:"all",records:["Industry mentoring","Graduate placement"],fields:["Interest","Programme","Details"]),
SchoolModule(cat:"Me",id:"Profile",title:"Profile & family",status:"local",role:"all",records:[],fields:[]),
SchoolModule(cat:"Me",id:"FamilyControls",title:"Family Controls",status:"partial",role:"parent",records:[],fields:[]),
SchoolModule(cat:"Me",id:"Friends",title:"Friends & relationships",status:"partial",role:"all",records:["Friend requests","Parent links"],fields:["Identity code","Relationship"]),
SchoolModule(cat:"Me",id:"Security",title:"Security & devices",status:"preview",role:"all",records:["This device","Other device"],fields:["Device","Verification method"]),
SchoolModule(cat:"Me",id:"Safety",title:"Safety & moderation",status:"preview",role:"all",records:["Blocked accounts","Report queue"],fields:["Report category","Description"]),
SchoolModule(cat:"Me",id:"Support",title:"Help & accessibility",status:"preview",role:"all",records:["Accessibility settings","Contact support"],fields:["Topic","Message"])
 ]
 static let destinationCategory:[String:String]=["Chats":"Connect","Alerts":"Connect","Calls":"Connect","Classroom":"Connect","Friends":"Connect","Courses":"Learn","Assignments":"Learn","Quiz":"Learn","Gate":"Learn","QuizDraft":"Learn","Assessment":"Learn","Grades":"Learn","Materials":"Learn","Files":"Learn","Library":"Learn","Research":"Learn","Calendar":"Campus","EventDraft":"Campus","Timetable":"Campus","Attendance":"Campus","Institutions":"Campus","Administration":"Campus","University":"Campus","Activities":"Campus","Services":"Campus","Welfare":"Campus","Rewards":"Achievements","Certificates":"Achievements","CertificateDraft":"Achievements","Credentials":"Achievements","Portfolio":"Achievements","Career":"Achievements","Internship":"Achievements","Profile":"Me","FamilyControls":"Me","Security":"Me","Offline":"Me","Support":"Me","Safety":"Me"]
 static let parentIDs:[String:String]=["Chats":"Chats","Alerts":"Chats","Calls":"Calls","Classroom":"Calls","Friends":"Friends","Courses":"Courses","Assignments":"Assignments","Quiz":"Quiz","Gate":"Quiz","QuizDraft":"Quiz","Assessment":"Quiz","Grades":"Grades","Materials":"Materials","Files":"Materials","Library":"Materials","Research":"Research","Calendar":"Calendar","EventDraft":"Calendar","Timetable":"Calendar","Attendance":"Attendance","Institutions":"Institutions","Administration":"Institutions","University":"Institutions","Activities":"Activities","Services":"Services","Welfare":"Services","Rewards":"Rewards","Certificates":"Certificates","CertificateDraft":"Certificates","Credentials":"Certificates","Portfolio":"Portfolio","Career":"Career","Internship":"Career","Profile":"Profile","FamilyControls":"FamilyControls","Security":"Security","Offline":"Security","Support":"Support","Safety":"Support"]
 static func parentID(_ id:String)->String{parentIDs[id] ?? id}
 static func destinations(_ cat:String,_ roles:String,minor:Bool)->[SchoolModule]{visible(cat,roles,minor:minor).filter{parentID($0.id)==$0.id}}
 static func related(_ cat:String,_ roles:String,minor:Bool,id:String)->[SchoolModule]{visible(cat,roles,minor:minor).filter{parentID($0.id)==parentID(id)}}
 static func visible(_ cat:String,_ roles:String,minor:Bool=true)->[SchoolModule]{let r=roles.components(separatedBy:" + ");return all.filter{(destinationCategory[$0.id] ?? $0.cat)==cat && ($0.id != "Gate" || minor) && (!minor || !["Research","Internship","University"].contains($0.id)) && ($0.id=="FamilyControls" || $0.role=="all" || $0.role=="staff" && (r.contains("Teacher") || r.contains("School Admin")) || $0.role=="parent" && r.contains("Parent") || $0.role=="student" && r.contains("Student"))}}
}
struct ModulePreview:View{
 let module:SchoolModule;let db:LocalDatabase;let user:Account;let record:String
 @State private var values:[String:String]=[:]
 @State private var gate=false
 @State private var feedback=""
 @State private var child=""
 @State private var duration="30"
 @State private var revision=0
 private let categories=["Games","Entertainment","Social apps"]
 private var q:[String]?{(try? db.query("SELECT q.prompt,q.a,q.b,q.c,q.d,q.correct,COALESCE(p.enabled,0),q.id FROM demographics d JOIN quiz_questions q ON d.age BETWEEN q.min_age AND q.max_age LEFT JOIN quiz_permissions p ON p.child_id=d.user_id WHERE d.user_id=? AND d.age<18 ORDER BY q.id LIMIT 1",[user.id]))?.first}
 private var children:[[String]]{(try? db.query("SELECT u.id,u.name FROM family_links f JOIN users u ON u.id=f.child_id JOIN demographics d ON d.user_id=u.id WHERE f.parent_id=? AND d.age<18 ORDER BY u.name",[user.id])) ?? []}
 private var policy:[String]?{_ = revision;return (try? db.query("SELECT id,mask,minutes FROM gate_policies WHERE child_id=? ORDER BY rowid DESC LIMIT 1",[user.id]))?.first}
 private var expiry:Double{_ = revision;return Double((try? db.query("SELECT g.created_at+p.minutes*60000 FROM gate_grants g JOIN gate_policies p ON p.id=g.policy_id WHERE g.child_id=? AND p.rowid=(SELECT MAX(rowid) FROM gate_policies WHERE child_id=g.child_id) ORDER BY g.rowid DESC LIMIT 1",[user.id]))?.first?.first ?? "") ?? 0}
 private func selectChild(_ id:String){let row=(try? db.query("SELECT id,mask,minutes FROM gate_policies WHERE child_id=? ORDER BY rowid DESC LIMIT 1",[id]))?.first;let mask=Int(row?[1] ?? "0") ?? 0;duration=row?[2] ?? "30";for (n,name) in categories.enumerated(){values[name]=(mask & (1<<n)) != 0 ? "1":"0"};feedback=""}
 private func savePolicy(){let mask=categories.enumerated().reduce(0){sum,item in sum+(values[item.element]=="1" ? 1<<item.offset:0)};do{_=try db.query("INSERT INTO gate_policies VALUES(?,?,?,?,?)",[UUID().uuidString,user.id,child,String(mask),duration]);revision+=1;feedback="Saved locally. Device enforcement not connected."}catch{feedback="Unable to save policy."}}
 private func grant(_ answer:Int){guard let q=q,let p=policy,expiry<=Date().timeIntervalSince1970*1000 else{return};do{_=try db.query("INSERT INTO gate_grants(id,child_id,policy_id,question_id,answer) VALUES(?,?,?,?,?)",[UUID().uuidString,user.id,p[0],q[7],String(answer)]);revision+=1;feedback="Temporary access granted in Schoolday demo.";gate=false}catch{feedback="Try again or check parent permissions."}}
 var body:some View{
  Form{
   if module.id=="Gate"{Section("Quiz Unlock"){Text("Local access simulation · OS enforcement not implemented");if let p=policy,(Int(p[1]) ?? 0)>0{TimelineView(.periodic(from:.now,by:1)){context in
    let active=expiry>context.date.timeIntervalSince1970*1000
    VStack(alignment:.leading,spacing:12){Text(active ? "Unlocked until "+Date(timeIntervalSince1970:expiry/1000).formatted(date:.omitted,time:.shortened):"Locked (demo)")
    ForEach(Array(categories.enumerated()),id:\.offset){item in if ((Int(p[1]) ?? 0) & (1<<item.offset)) != 0{Button(item.element+(active ? " · Available":" · Locked")){if active{feedback=item.element+" is available in this demo."}else{feedback="";gate=true}}}}
    Button("Answer quiz to unlock · "+p[2]+" minutes"){feedback="";gate=true}.disabled(active || q==nil)}}}else{Text("Ask a linked parent to configure recreational access in Family Controls.")};Text(feedback);Text("Emergency calls and essential communication always available.")}}
   else if module.id=="FamilyControls"{Section("Family Controls"){Picker("Linked child",selection:$child){Text("Select child").tag("");ForEach(children,id:\.self){r in Text(r[1]).tag(r[0])}}.onChange(of:child){_,id in selectChild(id)}
    ForEach(categories,id:\.self){n in Toggle(n,isOn:Binding(get:{values[n]=="1"},set:{values[n]=$0 ? "1":"0"}))}
    Picker("Unlock duration (minutes)",selection:$duration){ForEach(["15","30","60"],id:\.self){Text($0).tag($0)}}
    Button("Save local access policy",action:savePolicy).disabled(child.isEmpty);Text("Actual phone restrictions are not connected. Quiz-skip permission remains in Profile. Uncheck all to disable the demo gate.").font(.caption);Text(feedback)}}
   else{Section(record.isEmpty ? module.title:record){Text("Sample data · Not implemented").font(.caption);if ["Classroom","Calls"].contains(module.id){VStack{Image(systemName:"video").font(.largeTitle);HStack{ForEach(["Teacher","Student A","Student B"],id:\.self){n in VStack{Image(systemName:"person.crop.circle").font(.largeTitle);Text(n).font(.caption)}}};Text("Mic · Camera · Share · Leave").font(.caption)}.frame(maxWidth:.infinity).padding()};ForEach(module.fields,id:\.self){f in TextField(f,text:Binding(get:{values[f] ?? ""},set:{values[f]=$0}))};Button("Save / continue (not implemented)"){}.disabled(true)}}
  }.sheet(isPresented:$gate){NavigationStack{Form{Section("Answer to unlock"){Text("Schoolday Quiz · No phone apps are actually locked");if let question=q,question.count>=8{Text(question[0]).font(.headline);ForEach(0..<4,id:\.self){n in Button(question[n+1]){grant(n)}};if question[6]=="1"{Button("Skip · 5 points (simulation)"){grant(-1)}.disabled(((try? db.points(user)) ?? 0)<5);Text("No points are deducted in this simulation.")}else{Text("Skip disabled by parent")}};Text(feedback);Text("Emergency calls and essential communication always available.")}}.navigationTitle("Quiz Unlock").toolbar{ToolbarItem(placement:.confirmationAction){Button("Close"){gate=false}}}}}
 }
}
