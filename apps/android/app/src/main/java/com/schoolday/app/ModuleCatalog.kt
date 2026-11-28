package com.schoolday.app
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
data class SchoolModule(val cat:String,val id:String,val title:String,val status:String,val role:String,val records:List<String>,val fields:List<String>)
object SchoolModules{
 val categories=listOf("Connect","Learn","Campus","Achievements","Me")
 val all=listOf(
SchoolModule("Connect","Chats","Chats","local","all",listOf(),listOf()),
SchoolModule("Connect","Calls","Voice & video calls","preview","all",listOf("Class group","Teacher"),listOf("Contact","Call type")),
SchoolModule("Connect","Classroom","Online classroom","preview","all",listOf("Mathematics lesson","Research seminar"),listOf("Class title","Schedule","Meeting link")),
SchoolModule("Connect","Files","Files & media","preview","all",listOf("Lesson notes.pdf","Science project.jpg"),listOf("File name","Description")),
SchoolModule("Connect","Alerts","Emergency alerts","preview","all",listOf("School closure drill","Weather advisory"),listOf("Notice","Audience","Message")),
SchoolModule("Connect","Offline","Offline & LoRa","preview","all",listOf("Bluetooth","Wi-Fi","LoRa bridge"),listOf("Device","Transport")),
SchoolModule("Learn","Quiz","Quiz","local","all",listOf(),listOf()),
SchoolModule("Learn","Gate","Quiz Unlock","partial","student",listOf(),listOf()),
SchoolModule("Learn","Courses","Subjects & courses","preview","all",listOf("Mathematics Year 5","Research methodology"),listOf("Course code","Title","Education level")),
SchoolModule("Learn","Materials","Learning materials","preview","all",listOf("Workbook","Research guide"),listOf("Resource","Subject","File")),
SchoolModule("Learn","Assignments","Assignments","preview","all",listOf("Science project","Essay draft"),listOf("Assignment","Due date","Submission")),
SchoolModule("Learn","QuizDraft","Quiz editor","partial","staff",listOf(),listOf()),
SchoolModule("Learn","Assessment","Assessment & exams","preview","all",listOf("PBD mastery","UASA","Semester assessment"),listOf("Assessment","Curriculum","Rubric")),
SchoolModule("Learn","Grades","Grades & progress","preview","all",listOf("Mathematics","Science"),listOf("Subject","Grade","Feedback")),
SchoolModule("Learn","Research","Research & supervision","preview","all",listOf("Proposal defence","Literature review","Viva"),listOf("Research title","Milestone","Target date")),
SchoolModule("Learn","Internship","TVET & internship","preview","all",listOf("Workshop safety","Industry placement"),listOf("Placement","Competency","Evidence")),
SchoolModule("Campus","Calendar","Calendar","preview","all",listOf("Science exhibition","Mathematics assessment"),listOf("Event","Date","Venue")),
SchoolModule("Campus","EventDraft","Event planner","partial","staff",listOf(),listOf()),
SchoolModule("Campus","Attendance","Attendance","partial","all",listOf("Morning class","Online seminar"),listOf("Session","Date","Student identity")),
SchoolModule("Campus","Timetable","Timetable","preview","all",listOf("Mathematics 08:00","Science 10:00"),listOf("Subject","Time","Room")),
SchoolModule("Campus","Institutions","Institutions & enrolment","preview","all",listOf("Sample primary school","Sample university"),listOf("Institution","Programme","Student ID")),
SchoolModule("Campus","Activities","Clubs, sports & esports","preview","all",listOf("Science club","Esports tournament"),listOf("Activity","Team","Date")),
SchoolModule("Campus","Library","Library & textbooks","preview","all",listOf("SPBT textbook","Research handbook"),listOf("Book title","Resource ID")),
SchoolModule("Campus","Services","Student services","preview","all",listOf("Transport","Hostel","Canteen"),listOf("Service","Request details")),
SchoolModule("Campus","Welfare","Welfare & accessibility","preview","all",listOf("Counselling","Learning support","Financial aid"),listOf("Support type","Preferred time")),
SchoolModule("Campus","Administration","Administration & PIBG","preview","staff",listOf("School roster","PIBG meeting","Class placement"),listOf("Record type","Title","Details")),
SchoolModule("Campus","University","University administration","preview","all",listOf("Course registration","Final year project","Credits"),listOf("Programme","Course code","Semester")),
SchoolModule("Achievements","Rewards","Rewards","local","all",listOf(),listOf()),
SchoolModule("Achievements","Certificates","Certificates","preview","all",listOf("Science participation","Internship completion"),listOf("Certificate ID","Issuer")),
SchoolModule("Achievements","CertificateDraft","Certificate studio","partial","staff",listOf(),listOf()),
SchoolModule("Achievements","Portfolio","Portfolio & PAJSK","preview","all",listOf("Science project","Football club"),listOf("Achievement","Category","Evidence")),
SchoolModule("Achievements","Credentials","Micro-credentials & transcripts","preview","all",listOf("Digital literacy","Academic transcript"),listOf("Credential","Issuer","Reference")),
SchoolModule("Achievements","Career","Career & alumni","preview","all",listOf("Industry mentoring","Graduate placement"),listOf("Interest","Programme","Details")),
SchoolModule("Me","Profile","Profile & family","local","all",listOf(),listOf()),
SchoolModule("Me","FamilyControls","Family Controls","partial","parent",listOf(),listOf()),
SchoolModule("Me","Friends","Friends & relationships","partial","all",listOf("Friend requests","Parent links"),listOf("Identity code","Relationship")),
SchoolModule("Me","Security","Security & devices","preview","all",listOf("This device","Other device"),listOf("Device","Verification method")),
SchoolModule("Me","Safety","Safety & moderation","preview","all",listOf("Blocked accounts","Report queue"),listOf("Report category","Description")),
SchoolModule("Me","Support","Help & accessibility","preview","all",listOf("Accessibility settings","Contact support"),listOf("Topic","Message"))
 )
 fun visible(cat:String,roles:String):List<SchoolModule>{val r=roles.split(" + ");return all.filter{it.cat==cat&&(it.role=="all"||it.role=="staff"&&("Teacher" in r||"School Admin" in r)||it.role=="parent"&&"Parent" in r||it.role=="student"&&"Student" in r)}}
}
@Composable fun ModulePreview(module:SchoolModule,db:LocalDatabase,user:Account,record:String,modifier:Modifier=Modifier){
 val values=remember(module.id,record){mutableStateMapOf<String,String>()}
 var feedback by remember(module.id){mutableStateOf("")}
 var gate by remember(module.id){mutableStateOf(false)}
 var revision by remember{mutableStateOf(0)}
 var now by remember{mutableStateOf(System.currentTimeMillis())}
 val categories=listOf("Games","Entertainment","Social apps")
 val q=remember(user.id,module.id){if(module.id=="Gate")runCatching{db.rows("SELECT q.prompt,q.a,q.b,q.c,q.d,q.correct,COALESCE(p.enabled,0),q.id FROM demographics d JOIN quiz_questions q ON d.age BETWEEN q.min_age AND q.max_age LEFT JOIN quiz_permissions p ON p.child_id=d.user_id WHERE d.user_id=? AND d.age<18 ORDER BY q.id LIMIT 1",user.id).firstOrNull()}.getOrNull() else null}
 val children=remember(user.id,module.id){if(module.id=="FamilyControls")runCatching{db.rows("SELECT u.id,u.name FROM family_links f JOIN users u ON u.id=f.child_id JOIN demographics d ON d.user_id=u.id WHERE f.parent_id=? AND d.age<18 ORDER BY u.name",user.id)}.getOrDefault(emptyList()) else emptyList()}
 val policy=remember(user.id,module.id,revision){if(module.id=="Gate")runCatching{db.rows("SELECT id,mask,minutes FROM gate_policies WHERE child_id=? ORDER BY rowid DESC LIMIT 1",user.id).firstOrNull()}.getOrNull() else null}
 val expires=remember(user.id,module.id,revision){if(module.id=="Gate")runCatching{db.rows("SELECT g.created_at+p.minutes*60000 FROM gate_grants g JOIN gate_policies p ON p.id=g.policy_id WHERE g.child_id=? AND p.rowid=(SELECT MAX(rowid) FROM gate_policies WHERE child_id=g.child_id) ORDER BY g.rowid DESC LIMIT 1",user.id).firstOrNull()?.firstOrNull()?.toLongOrNull()?:0L}.getOrDefault(0L) else 0L}
 LaunchedEffect(module.id){if(module.id=="Gate")while(true){now=System.currentTimeMillis();kotlinx.coroutines.delay(1000)}}
 val active=expires>now
 fun selectChild(id:String){val row=runCatching{db.rows("SELECT id,mask,minutes FROM gate_policies WHERE child_id=? ORDER BY rowid DESC LIMIT 1",id).firstOrNull()}.getOrNull();val mask=row?.get(1)?.toIntOrNull()?:0;values["child"]=id;values["duration"]=row?.get(2)?:"30";categories.forEachIndexed{n,name->values[name]=if((mask and (1 shl n))!=0)"1" else "0"};feedback=""}
 fun savePolicy(){val child=values["child"]?:return;val mask=categories.mapIndexed{n,name->if(values[name]=="1")1 shl n else 0}.sum();try{db.writableDatabase.execSQL("INSERT INTO gate_policies VALUES(?,?,?,?,?)",arrayOf(java.util.UUID.randomUUID().toString(),user.id,child,mask,values["duration"]?.toIntOrNull()?:30));revision++;feedback="Saved locally. Device enforcement not connected."}catch(e:Exception){feedback="Unable to save policy."}}
 fun grant(answer:Int){val question=q?:return;val p=policy?:return;if(expires>System.currentTimeMillis())return;try{db.writableDatabase.execSQL("INSERT INTO gate_grants(id,child_id,policy_id,question_id,answer) VALUES(?,?,?,?,?)",arrayOf(java.util.UUID.randomUUID().toString(),user.id,p[0],question[7],answer));now=System.currentTimeMillis();revision++;feedback="Temporary access granted in Schoolday demo.";gate=false}catch(e:Exception){feedback="Try again or check parent permissions."}}
 Column(modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
 if(module.id=="Gate"){Text("Quiz Unlock",style=MaterialTheme.typography.headlineSmall);Text("Local access simulation · OS enforcement not implemented");if(policy!=null&&(policy[1].toIntOrNull()?:0)>0){Text(if(active)"Unlocked until "+java.text.DateFormat.getTimeInstance(java.text.DateFormat.SHORT).format(java.util.Date(expires)) else "Locked (demo)");categories.forEachIndexed{n,name->if(((policy[1].toIntOrNull()?:0) and (1 shl n))!=0)OutlinedButton(onClick={if(active)feedback=name+" is available in this demo." else{feedback="";gate=true}},modifier=Modifier.fillMaxWidth()){Text(name+if(active)" · Available" else " · Locked")}};Button(onClick={feedback="";gate=true},enabled=q!=null&&!active){Text("Answer quiz to unlock · "+policy[2]+" minutes")}}else Text("Ask a linked parent to configure recreational access in Family Controls.");Text(feedback);Text("Emergency calls and essential communication always available.")}
 else if(module.id=="FamilyControls"){Text("Family Controls");Text("Linked child");children.forEach{child->Row{RadioButton(selected=values["child"]==child[0],onClick={selectChild(child[0])});Text(child[1],modifier=Modifier.padding(top=12.dp))}};categories.forEach{x->Row{Checkbox(values[x]=="1",{values[x]=if(it)"1" else "0"});Text(x,modifier=Modifier.padding(top=12.dp))}};Text("Unlock duration (minutes)");listOf("15","30","60").forEach{n->Row{RadioButton(selected=(values["duration"]?:"30")==n,onClick={values["duration"]=n});Text(n,modifier=Modifier.padding(top=12.dp))}};Button(onClick={savePolicy()},enabled=!values["child"].isNullOrEmpty()){Text("Save local access policy")};Text("Actual phone restrictions are not connected. Quiz-skip permission remains in Profile. Uncheck all to disable the demo gate.");Text(feedback)}
 else{Text(record.ifEmpty{module.title},style=MaterialTheme.typography.headlineSmall);Text("Sample data · Not implemented");if(module.id in listOf("Calls","Classroom")){OutlinedCard(Modifier.fillMaxWidth()){Column(Modifier.padding(16.dp)){Text("Sample classroom / call");Row{listOf("Teacher","Student A","Student B").forEach{Text(it,modifier=Modifier.weight(1f).padding(8.dp))}};Text("Mic · Camera · Share · Leave")}}};module.fields.forEach{f->OutlinedTextField(values[f]?:"",{values[f]=it},label={Text(f)},modifier=Modifier.fillMaxWidth())};Button(onClick={},enabled=false){Text("Save / continue (not implemented)")}}
 }
 if(gate&&q!=null&&q.size>=8)AlertDialog(onDismissRequest={gate=false},title={Text("Answer to unlock")},text={Column(Modifier.verticalScroll(rememberScrollState())){Text("Schoolday Quiz · No phone apps are actually locked");Text(q[0]);(0..3).forEach{n->OutlinedButton(onClick={grant(n)},modifier=Modifier.fillMaxWidth()){Text(q[n+1])}};if(q[6]=="1"){TextButton(onClick={grant(-1)},enabled=runCatching{db.points(user)>=5}.getOrDefault(false)){Text("Skip · 5 points (simulation)")};Text("No points deducted.")}else Text("Skip disabled by parent");Text(feedback);Text("Emergency calls and essential communication always available.")}},confirmButton={TextButton(onClick={gate=false}){Text("Close")}})
}
