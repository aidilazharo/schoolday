import SwiftUI
struct SchoolModule:Identifiable{
 let cat:String;let id:String;let title:String;let status:String;let role:String;let records:[String];let fields:[String]
}
enum SchoolModules{
 static let categories=["Connect","Learn","Campus","Achievements","Me"]
 static let all:[SchoolModule]=[
SchoolModule(cat:"Connect",id:"Chats",title:"Chats",status:"local",role:"all",records:[],fields:[]),
SchoolModule(cat:"Connect",id:"Calls",title:"Voice & video calls",status:"preview",role:"all",records:["Class group","Teacher"],fields:["Contact","Call type"]),
SchoolModule(cat:"Connect",id:"Classroom",title:"Online classroom",status:"preview",role:"all",records:["Mathematics lesson","Research seminar"],fields:["Class title","Schedule","Meeting link"]),
SchoolModule(cat:"Connect",id:"Files",title:"Files & media",status:"preview",role:"all",records:["Lesson notes.pdf","Science project.jpg"],fields:["File name","Description"]),
SchoolModule(cat:"Connect",id:"Alerts",title:"Emergency alerts",status:"preview",role:"all",records:["School closure drill","Weather advisory"],fields:["Notice","Audience","Message"]),
SchoolModule(cat:"Connect",id:"Offline",title:"Offline & LoRa",status:"preview",role:"all",records:["Bluetooth","Wi-Fi","LoRa bridge"],fields:["Device","Transport"]),
SchoolModule(cat:"Learn",id:"Quiz",title:"Quiz",status:"local",role:"all",records:[],fields:[]),
SchoolModule(cat:"Learn",id:"Gate",title:"Quiz Unlock",status:"preview",role:"student",records:[],fields:[]),
SchoolModule(cat:"Learn",id:"Courses",title:"Subjects & courses",status:"preview",role:"all",records:["Mathematics Year 5","Research methodology"],fields:["Course code","Title","Education level"]),
SchoolModule(cat:"Learn",id:"Materials",title:"Learning materials",status:"preview",role:"all",records:["Workbook","Research guide"],fields:["Resource","Subject","File"]),
SchoolModule(cat:"Learn",id:"Assignments",title:"Assignments",status:"preview",role:"all",records:["Science project","Essay draft"],fields:["Assignment","Due date","Submission"]),
SchoolModule(cat:"Learn",id:"QuizDraft",title:"Quiz editor",status:"partial",role:"staff",records:[],fields:[]),
SchoolModule(cat:"Learn",id:"Assessment",title:"Assessment & exams",status:"preview",role:"all",records:["PBD mastery","UASA","Semester assessment"],fields:["Assessment","Curriculum","Rubric"]),
SchoolModule(cat:"Learn",id:"Grades",title:"Grades & progress",status:"preview",role:"all",records:["Mathematics","Science"],fields:["Subject","Grade","Feedback"]),
SchoolModule(cat:"Learn",id:"Research",title:"Research & supervision",status:"preview",role:"all",records:["Proposal defence","Literature review","Viva"],fields:["Research title","Milestone","Target date"]),
SchoolModule(cat:"Learn",id:"Internship",title:"TVET & internship",status:"preview",role:"all",records:["Workshop safety","Industry placement"],fields:["Placement","Competency","Evidence"]),
SchoolModule(cat:"Campus",id:"Calendar",title:"Calendar",status:"preview",role:"all",records:["Science exhibition","Mathematics assessment"],fields:["Event","Date","Venue"]),
SchoolModule(cat:"Campus",id:"EventDraft",title:"Event planner",status:"partial",role:"staff",records:[],fields:[]),
SchoolModule(cat:"Campus",id:"Attendance",title:"Attendance",status:"partial",role:"all",records:["Morning class","Online seminar"],fields:["Session","Date","Student identity"]),
SchoolModule(cat:"Campus",id:"Timetable",title:"Timetable",status:"preview",role:"all",records:["Mathematics 08:00","Science 10:00"],fields:["Subject","Time","Room"]),
SchoolModule(cat:"Campus",id:"Institutions",title:"Institutions & enrolment",status:"preview",role:"all",records:["Sample primary school","Sample university"],fields:["Institution","Programme","Student ID"]),
SchoolModule(cat:"Campus",id:"Activities",title:"Clubs, sports & esports",status:"preview",role:"all",records:["Science club","Esports tournament"],fields:["Activity","Team","Date"]),
SchoolModule(cat:"Campus",id:"Library",title:"Library & textbooks",status:"preview",role:"all",records:["SPBT textbook","Research handbook"],fields:["Book title","Resource ID"]),
SchoolModule(cat:"Campus",id:"Services",title:"Student services",status:"preview",role:"all",records:["Transport","Hostel","Canteen"],fields:["Service","Request details"]),
SchoolModule(cat:"Campus",id:"Welfare",title:"Welfare & accessibility",status:"preview",role:"all",records:["Counselling","Learning support","Financial aid"],fields:["Support type","Preferred time"]),
SchoolModule(cat:"Campus",id:"Administration",title:"Administration & PIBG",status:"preview",role:"staff",records:["School roster","PIBG meeting","Class placement"],fields:["Record type","Title","Details"]),
SchoolModule(cat:"Campus",id:"University",title:"University administration",status:"preview",role:"all",records:["Course registration","Final year project","Credits"],fields:["Programme","Course code","Semester"]),
SchoolModule(cat:"Achievements",id:"Rewards",title:"Rewards",status:"local",role:"all",records:[],fields:[]),
SchoolModule(cat:"Achievements",id:"Certificates",title:"Certificates",status:"preview",role:"all",records:["Science participation","Internship completion"],fields:["Certificate ID","Issuer"]),
SchoolModule(cat:"Achievements",id:"CertificateDraft",title:"Certificate studio",status:"partial",role:"staff",records:[],fields:[]),
SchoolModule(cat:"Achievements",id:"Portfolio",title:"Portfolio & PAJSK",status:"preview",role:"all",records:["Science project","Football club"],fields:["Achievement","Category","Evidence"]),
SchoolModule(cat:"Achievements",id:"Credentials",title:"Micro-credentials & transcripts",status:"preview",role:"all",records:["Digital literacy","Academic transcript"],fields:["Credential","Issuer","Reference"]),
SchoolModule(cat:"Achievements",id:"Career",title:"Career & alumni",status:"preview",role:"all",records:["Industry mentoring","Graduate placement"],fields:["Interest","Programme","Details"]),
SchoolModule(cat:"Me",id:"Profile",title:"Profile & family",status:"local",role:"all",records:[],fields:[]),
SchoolModule(cat:"Me",id:"FamilyControls",title:"Family Controls",status:"preview",role:"parent",records:[],fields:[]),
SchoolModule(cat:"Me",id:"Friends",title:"Friends & relationships",status:"partial",role:"all",records:["Friend requests","Parent links"],fields:["Identity code","Relationship"]),
SchoolModule(cat:"Me",id:"Security",title:"Security & devices",status:"preview",role:"all",records:["This device","Other device"],fields:["Device","Verification method"]),
SchoolModule(cat:"Me",id:"Safety",title:"Safety & moderation",status:"preview",role:"all",records:["Blocked accounts","Report queue"],fields:["Report category","Description"]),
SchoolModule(cat:"Me",id:"Support",title:"Help & accessibility",status:"preview",role:"all",records:["Accessibility settings","Contact support"],fields:["Topic","Message"])
 ]
 static func visible(_ cat:String,_ roles:String)->[SchoolModule]{let r=roles.components(separatedBy:" + ");return all.filter{$0.cat==cat && ($0.role=="all" || $0.role=="staff" && (r.contains("Teacher") || r.contains("School Admin")) || $0.role=="parent" && r.contains("Parent") || $0.role=="student" && r.contains("Student"))}}
}
struct ModulePreview:View{
 let module:SchoolModule;let db:LocalDatabase;let user:Account;let record:String
 @State private var values:[String:String]=[:]
 @State private var gate=false
 @State private var feedback=""
 @State private var child=""
 @State private var duration="30"
 private var q:[String]?{(try? db.query("SELECT q.prompt,q.a,q.b,q.c,q.d,q.correct,COALESCE(p.enabled,0) FROM demographics d JOIN quiz_questions q ON d.age BETWEEN q.min_age AND q.max_age LEFT JOIN quiz_permissions p ON p.child_id=d.user_id WHERE d.user_id=? LIMIT 1",[user.id]))?.first}
 private var children:[String]{((try? db.query("SELECT u.name FROM family_links f JOIN users u ON u.id=f.child_id WHERE f.parent_id=? ORDER BY u.name",[user.id])) ?? []).compactMap{$0.first}}
 var body:some View{
  Form{
   if module.id=="Gate"{Section("Quiz Unlock"){Text("Preview only. No device is locked or points deducted.");Button("Preview unlock pop-up"){feedback="";gate=true}.disabled(q==nil);Text(feedback);Text("Emergency calls and essential communication always available.")}}
   else if module.id=="FamilyControls"{Section("Family Controls"){Picker("Linked child",selection:$child){Text("Select child").tag("");ForEach(children,id:\.self){Text($0).tag($0)}}
    ForEach(["Games","Entertainment","Social apps","Allow optional quiz skip (5 points)"],id:\.self){n in Toggle(n,isOn:Binding(get:{values[n]=="1"},set:{values[n]=$0 ? "1":"0"}))}
    Picker("Unlock duration (minutes)",selection:$duration){ForEach(["15","30","60"],id:\.self){Text($0).tag($0)}}
    Button("Save device restriction"){}.disabled(true);Text("Preview only. Actual quiz-skip permission remains in Profile. Essential services are never gated.").font(.caption)}}
   else{Section(record.isEmpty ? module.title:record){Text("Sample data · Not implemented").font(.caption);if ["Classroom","Calls"].contains(module.id){VStack{Image(systemName:"video").font(.largeTitle);HStack{ForEach(["Teacher","Student A","Student B"],id:\.self){n in VStack{Image(systemName:"person.crop.circle").font(.largeTitle);Text(n).font(.caption)}}};Text("Mic · Camera · Share · Leave").font(.caption)}.frame(maxWidth:.infinity).padding()};ForEach(module.fields,id:\.self){f in TextField(f,text:Binding(get:{values[f] ?? ""},set:{values[f]=$0}))};Button("Save / continue (not implemented)"){}.disabled(true)}}
  }.sheet(isPresented:$gate){NavigationStack{Form{Section("Answer to unlock"){Text("Preview only · No points deducted");if let question=q,question.count>=7{Text(question[0]).font(.headline);ForEach(0..<4,id:\.self){n in Button(question[n+1]){if String(n)==question[5]{feedback="Unlocked (preview)";gate=false}else{feedback="Try again."}}};if question[6]=="1"{Button("Skip · 5 points (preview)"){feedback="Unlocked (preview)";gate=false}.disabled(((try? db.points(user)) ?? 0)<5)}else{Text("Skip disabled by parent")}};Text(feedback);Text("Emergency calls and essential communication always available.")}}.navigationTitle("Quiz Unlock").toolbar{ToolbarItem(placement:.confirmationAction){Button("Close preview"){gate=false}}}}}
 }
}
