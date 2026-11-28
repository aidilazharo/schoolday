package com.schoolday.app
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

fun groupSettingsLabel(lang:String)=mapOf("en" to "Group settings","ms" to "Tetapan kumpulan","zh" to "群组设置","ta" to "குழு அமைப்புகள்")[lang]?:"Group settings"
private val groupSections=listOf(listOf("info","Group information","Group name;Description;Visibility","Save group information"),
listOf("members","Members","",""),
listOf("invite","Invitations & join requests","Member or invitation code;Invitation expiry;Join request note","Send invitation"),
listOf("roles","Administrators & roles","Member;Proposed role;Reason","Assign role"),
listOf("election","Admin election","",""),
listOf("recall","Administrator recall","Administrator;Reason for recall","Start recall vote"),
listOf("polls","Group polls","Question;Option 1;Option 2;Closing date","Publish poll"),
listOf("permissions","Permissions","",""),
listOf("rules","Group rules","Rules;Member responsibilities","Save rules"),
listOf("announcements","Announcements & pinned messages","Announcement;Message reference","Pin announcement"),
listOf("calls","Group calls","Meeting title;Start time","Start call"),
listOf("files","Media & documents","File name;Description","Share file"),
listOf("events","Group events","Event title;Date and time;Location","Create event"),
listOf("tasks","Group tasks","Task;Assigned member;Due date","Assign task"),
listOf("quizzes","Group quizzes","Quiz title;Question;Answer choices","Assign quiz"),
listOf("attendance","Attendance","Session;Date;Member reference","Record attendance"),
listOf("certificates","Certificates","Recipient;Achievement;Issuer","Issue certificate"),
listOf("notes","Shared notes","Title;Note","Save note"),
listOf("search","Search messages","Search text","Search messages"),
listOf("notifications","Notifications","Notification preference;Mute duration","Save preference"),
listOf("activity","Activity log","",""),
listOf("safety","Report & moderation","Member or message reference;Reason","Submit report"),
listOf("archive","Archive group","Reason","Archive group"),
listOf("leave","Leave group","Reason (optional)","Leave group"))
private val groupPolicies=mapOf("open" to "Community election with independent safety oversight","secondary" to "Institution-appointed administrator","school" to "Institution-appointed administrator","class" to "Assigned educator; representatives may be elected","staff" to "Institution-appointed administrator","family" to "Verified family permissions","admin" to "Institution-appointed administrator","university" to "Institution-appointed administrator","uni-course" to "Assigned lecturer; representatives may be elected")
private val groupPermissions=listOf("Send messages","Invite members","Edit group information","Remove members","Create polls","Start group video call")
private val groupActions=listOf("Remove member","Ban member","Temporarily mute","Assign moderator","Restore membership")
@Composable fun GroupSettings(db:LocalDatabase,user:Account,channel:String,title:String,lang:String,modifier:Modifier=Modifier,onClose:()->Unit){
 var section by remember(channel){mutableStateOf("")}
 var members by remember(channel){mutableStateOf(emptyList<List<String>>())}
 var failed by remember(channel){mutableStateOf(false)}
 var search by remember(channel){mutableStateOf("")}
 var target by remember(channel){mutableStateOf("")}
 var action by remember(channel){mutableStateOf("Remove member")}
 val values=remember(channel){mutableStateMapOf<String,String>()}
 var period by remember(channel){mutableStateOf("24")}
 var term by remember(channel){mutableStateOf("90")}
 var seats by remember(channel){mutableStateOf("3")}
 var quorum by remember(channel){mutableStateOf("50")}
 var secret by remember(channel){mutableStateOf(true)}
 var recall by remember(channel){mutableStateOf(true)}
 var candidate by remember(channel){mutableStateOf("")}
 var voted by remember(channel){mutableStateOf(false)}
 fun choose(id:String){section=id;values.clear();target="";search="";candidate="";voted=false}
 val active=groupSections.firstOrNull{it[0]==section}
 LaunchedEffect(channel,user.id){try{members=withContext(Dispatchers.IO){db.rows("SELECT u.id,u.name,COALESCE((SELECT group_concat(r.role,' + ') FROM roles r WHERE r.user_id=u.id),'Member'),CASE WHEN d.age>=18 THEN '1' ELSE '0' END FROM memberships m JOIN users u ON u.id=m.user_id LEFT JOIN demographics d ON d.user_id=u.id WHERE m.channel_id=? AND EXISTS(SELECT 1 FROM memberships access WHERE access.user_id=? AND access.channel_id=m.channel_id) ORDER BY u.name,u.id",channel,user.id)}}catch(e:kotlinx.coroutines.CancellationException){throw e}catch(e:Exception){failed=true}}
 Column(modifier.fillMaxWidth().background(Color.White)){
  Row(Modifier.fillMaxWidth().padding(horizontal=12.dp),verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text(groupSettingsLabel(lang),fontWeight=FontWeight.Bold);Text(title+" · "+members.size+" members",style=MaterialTheme.typography.labelSmall)};TextButton(onClick=onClose){Text(SchooldayLanguage.text("Back",lang))}}
  HorizontalDivider()
  if(failed)Text("Local database unavailable",color=MaterialTheme.colorScheme.error)
  BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()){
   val split=maxWidth>=620.dp
   Row(Modifier.fillMaxSize()){
    if(split||section.isEmpty()){
     LazyColumn(if(split)Modifier.width((maxWidth*0.34f).coerceAtMost(250.dp)).fillMaxHeight() else Modifier.fillMaxSize()){
      item{Column(Modifier.padding(12.dp)){Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)){GroupAvatar(channel,title,56.dp);Text(title,fontWeight=FontWeight.Bold)};Text(groupPolicies[channel]?:"Institution or group policy determines authority",style=MaterialTheme.typography.labelSmall)}}
      items(groupSections,key={it[0]}){r->Column{if(r[0]!="members")HorizontalDivider(thickness=2.dp,color=Color(0xFFC62828));TextButton(onClick={choose(r[0])},modifier=Modifier.fillMaxWidth().background(if(section==r[0])Color(0xFFF0F0F0) else Color.White)){Text(r[1],modifier=Modifier.weight(1f));Text("›")};HorizontalDivider()}}
     }
    }
    if(split)VerticalDivider()
    if(split||section.isNotEmpty()){
     key(section){Column(Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState()).padding(12.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
      if(active==null)Text("Choose a group function. Red marks indicate development previews.")
      else{
       if(!split)TextButton(onClick={choose("")}){Text("‹ "+SchooldayLanguage.text("Back",lang))}
       Text(active[1],style=MaterialTheme.typography.titleLarge)
       if(section!="members"){HorizontalDivider(thickness=3.dp,color=Color(0xFFC62828));Text("Development preview · Not implemented",color=Color(0xFFC62828))}
       when(section){
        "members"->{OutlinedTextField(search,{search=it},label={Text("Search members")},modifier=Modifier.fillMaxWidth());TextButton(onClick={choose("invite")}){Text("+ Add member / QR invitation")}
         members.filter{it[1].contains(search,ignoreCase=true)}.forEach{r->TextButton(onClick={target=r[0]},modifier=Modifier.fillMaxWidth()){Column(Modifier.weight(1f)){Text(r[1]+if(r[0]==user.id)" (You)" else "",fontWeight=FontWeight.Bold);Text(r[2]+" · Group member",style=MaterialTheme.typography.labelSmall)};Text("⋯")};HorizontalDivider()}
         if(target.isNotEmpty()){Text(members.firstOrNull{it[0]==target}?.get(1)?:"");Text("Membership action preview. No permission changes.",color=Color(0xFFC62828));GroupChoice("Action",action,groupActions){action=it};OutlinedTextField(values["reason"]?:"",{values["reason"]=it},label={Text("Reason")},modifier=Modifier.fillMaxWidth());BlockedGroupAction(action)}
        }
        "election"->{Text(if(channel=="open")"Community administrator election" else "Representative election only; institutional or family authority cannot be changed.")
         GroupChoice("Voting period (hours)",period,listOf("24","48","72")){period=it};GroupChoice("Term (days)",term,listOf("30","90","180","365")){term=it};GroupChoice("Seats",seats,listOf("1","3","5")){seats=it};GroupChoice("Quorum (%)",quorum,listOf("25","50","75","100")){quorum=it}
         Row(verticalAlignment=Alignment.CenterVertically){Checkbox(secret,{secret=it});Text("Secret ballot (proposed)")};Row(verticalAlignment=Alignment.CenterVertically){Checkbox(recall,{recall=it});Text("Allow recall vote")};BlockedGroupAction("Start election")
         HorizontalDivider();Text("Test ballot",style=MaterialTheme.typography.titleMedium);Text("Sample candidates. No nominations, votes or permissions are saved.")
         members.filter{it[3]=="1"}.forEach{r->Row(verticalAlignment=Alignment.CenterVertically){RadioButton(selected=candidate==r[0],onClick={candidate=r[0]},enabled=!voted);Text(r[1])}}
         Button(onClick={voted=true},enabled=candidate.isNotEmpty()&&!voted){Text(if(voted)"Preview vote recorded" else "Submit preview vote")};if(voted)TextButton(onClick={voted=false;candidate=""}){Text("Reset preview")}
         Text("Real results require reconciled, authenticated ballots.",style=MaterialTheme.typography.labelSmall)
        }
        "permissions"->{groupPermissions.forEach{p->GroupChoice(p,values[p]?:"Admins only",listOf("All members","Moderators & admins","Admins only")){values[p]=it}};BlockedGroupAction("Save permissions")}
        "activity"->Text("No authenticated group governance events are available.")
        else->{active[2].split(";").filter{it.isNotEmpty()}.forEach{f->OutlinedTextField(values[f]?:"",{values[f]=it},label={Text(f)},modifier=Modifier.fillMaxWidth())};BlockedGroupAction(active[3])}
       }
      }
     }}
    }
   }
  }
 }
}
@Composable private fun GroupChoice(label:String,value:String,options:List<String>,onChange:(String)->Unit){
 var expanded by remember{mutableStateOf(false)}
 Text(label,style=MaterialTheme.typography.labelMedium)
 Box(Modifier.fillMaxWidth()){OutlinedButton(onClick={expanded=true},modifier=Modifier.fillMaxWidth()){Text(value+" ▾")};DropdownMenu(expanded=expanded,onDismissRequest={expanded=false}){options.forEach{v->DropdownMenuItem(text={Text(v)},onClick={expanded=false;onChange(v)})}}}
}
@Composable private fun BlockedGroupAction(label:String){Button(onClick={},enabled=false,modifier=Modifier.fillMaxWidth()){Text(label+" · Not implemented")}}
