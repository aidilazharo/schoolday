import SwiftUI
enum ModuleNavigation {
 static let labels:[String:[String:String]]=["Chats":["en":"Chats","ms":"Sembang","zh":"聊天","ta":"அரட்டை"],
"Calls":["en":"Meetings","ms":"Mesyuarat","zh":"会议","ta":"சந்திப்புகள்"],
"Classroom":["en":"Live classes","ms":"Kelas langsung","zh":"在线课堂","ta":"நேரலை வகுப்பு"],
"Files":["en":"Files","ms":"Fail","zh":"文件","ta":"கோப்புகள்"],
"Alerts":["en":"Alerts","ms":"Makluman","zh":"提醒","ta":"அறிவிப்புகள்"],
"Offline":["en":"Offline","ms":"Luar talian","zh":"离线","ta":"இணைப்பில்லா"],
"Quiz":["en":"Assessments","ms":"Pentaksiran","zh":"评估","ta":"மதிப்பீடு"],
"Gate":["en":"Quiz unlock","ms":"Buka kunci","zh":"测验解锁","ta":"வினா திறப்பு"],
"Courses":["en":"Courses","ms":"Kursus","zh":"课程","ta":"பாடங்கள்"],
"Materials":["en":"Resources","ms":"Sumber","zh":"资源","ta":"வளங்கள்"],
"Assignments":["en":"Tasks","ms":"Tugasan","zh":"作业","ta":"பணிகள்"],
"QuizDraft":["en":"Quiz editor","ms":"Cipta kuiz","zh":"测验编辑","ta":"வினா உருவாக்கு"],
"Assessment":["en":"Assessments","ms":"Pentaksiran","zh":"评估","ta":"மதிப்பீடு"],
"Grades":["en":"Progress","ms":"Kemajuan","zh":"学习进度","ta":"முன்னேற்றம்"],
"Research":["en":"Research","ms":"Penyelidikan","zh":"研究","ta":"ஆராய்ச்சி"],
"Internship":["en":"Training","ms":"Latihan","zh":"实习培训","ta":"பயிற்சி"],
"Calendar":["en":"Calendar","ms":"Kalendar","zh":"日历","ta":"நாள்காட்டி"],
"EventDraft":["en":"Events","ms":"Acara","zh":"活动","ta":"நிகழ்வுகள்"],
"Attendance":["en":"Attendance","ms":"Kehadiran","zh":"出勤","ta":"வருகை"],
"Timetable":["en":"Schedule","ms":"Jadual","zh":"课表","ta":"அட்டவணை"],
"Institutions":["en":"Institutions","ms":"Institusi","zh":"机构","ta":"நிறுவனங்கள்"],
"Activities":["en":"Activities","ms":"Aktiviti","zh":"活动","ta":"செயல்பாடுகள்"],
"Library":["en":"Library","ms":"Perpustakaan","zh":"图书馆","ta":"நூலகம்"],
"Services":["en":"Services","ms":"Perkhidmatan","zh":"服务","ta":"சேவைகள்"],
"Welfare":["en":"Wellbeing","ms":"Kesejahteraan","zh":"身心健康","ta":"நல்வாழ்வு"],
"Administration":["en":"Admin","ms":"Pentadbiran","zh":"管理","ta":"நிர்வாகம்"],
"University":["en":"Academic","ms":"Akademik","zh":"教务","ta":"கல்வி நிர்வாகம்"],
"Rewards":["en":"Wallet","ms":"Dompet","zh":"钱包","ta":"பணப்பை"],
"Certificates":["en":"Credentials","ms":"Kelayakan","zh":"资历","ta":"தகுதிகள்"],
"CertificateDraft":["en":"Design","ms":"Reka sijil","zh":"证书设计","ta":"சான்று வடிவமைப்பு"],
"Portfolio":["en":"Portfolio","ms":"Portfolio","zh":"档案","ta":"தொகுப்பு"],
"Credentials":["en":"Credentials","ms":"Kredensial","zh":"资历","ta":"தகுதிகள்"],
"Career":["en":"Career","ms":"Kerjaya","zh":"职业","ta":"தொழில்"],
"Profile":["en":"Profile","ms":"Profil","zh":"个人资料","ta":"சுயவிவரம்"],
"FamilyControls":["en":"Family","ms":"Keluarga","zh":"家庭","ta":"குடும்பம்"],
"Friends":["en":"Contacts","ms":"Kenalan","zh":"联系人","ta":"தொடர்புகள்"],
"Security":["en":"Settings","ms":"Tetapan","zh":"设置","ta":"அமைப்புகள்"],
"Safety":["en":"Safety","ms":"Perlindungan","zh":"保护","ta":"காப்பு"],
"Support":["en":"Help & safety","ms":"Bantuan","zh":"帮助与安全","ta":"உதவி"],
"Connect":["en":"Connect","ms":"Hubung","zh":"联系","ta":"தொடர்பு"],
"Learn":["en":"Learn","ms":"Belajar","zh":"学习","ta":"கற்றல்"],
"Campus":["en":"Community","ms":"Komuniti","zh":"社区","ta":"சமூகம்"],
"Achievements":["en":"Awards","ms":"Pencapaian","zh":"成就","ta":"சாதனைகள்"],
"Me":["en":"Me","ms":"Saya","zh":"我的","ta":"நான்"]]
 static let symbols:[String:String]=["Chats":"bubble.left","Calls":"video","Classroom":"person.2.wave.2","Files":"folder","Alerts":"bell","Offline":"antenna.radiowaves.left.and.right","Quiz":"list.bullet.clipboard","Gate":"lock.open","Courses":"book","Materials":"books.vertical","Assignments":"checklist","QuizDraft":"square.and.pencil","Assessment":"doc.text.magnifyingglass","Grades":"chart.bar","Research":"magnifyingglass","Internship":"briefcase","Calendar":"calendar","EventDraft":"calendar.badge.plus","Attendance":"checkmark.circle","Timetable":"clock","Institutions":"building.2","Activities":"trophy","Library":"book.closed","Services":"square.grid.2x2","Welfare":"heart","Administration":"gearshape","University":"graduationcap","Rewards":"star","Certificates":"rosette","CertificateDraft":"pencil.and.outline","Portfolio":"folder.badge.person.crop","Credentials":"seal","Career":"briefcase","Profile":"person.crop.circle","FamilyControls":"person.2","Friends":"person.2.circle","Security":"shield","Safety":"exclamationmark.shield","Support":"questionmark.circle"]
 static let workspaceTools:Set<String>=["Classroom","Offline","QuizDraft","Assessment","Grades","Research","Internship","EventDraft","Institutions","Administration","University","CertificateDraft","Credentials","FamilyControls","Security"]
 static func label(_ id:String,_ lang:String)->String{labels[id]?[lang] ?? labels[id]?["en"] ?? id}
 static func icon(_ id:String)->String{symbols[id] ?? "square.grid.2x2"}
}

struct ModuleToolButton:View{
 let module:SchoolModule
 let lang:String
 let selected:Bool
 let action:()->Void
 private var statusLabel:String{module.status=="local" ? "" : module.status=="partial" ? " · Partially implemented":" · Not implemented"}
 var body:some View{
  Button(action:action){
   VStack(spacing:4){
    Image(systemName:ModuleNavigation.icon(module.id)).font(.system(size:20))
    Text(ModuleNavigation.label(module.id,lang)).font(.caption2).multilineTextAlignment(.center).lineLimit(3)
   }.frame(width:76).frame(minHeight:62)
    .background(selected ? Color.primary.opacity(0.08):Color.clear,in:RoundedRectangle(cornerRadius:5))
    .overlay(alignment:.top){if module.status != "local"{Rectangle().fill(Color.red).frame(height:3)}}
  }.buttonStyle(.plain).accessibilityLabel(module.title+statusLabel).accessibilityAddTraits(selected ? .isSelected:[])
 }
}
