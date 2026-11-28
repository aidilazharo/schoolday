package com.schoolday.app

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.print.PrintHelper
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

private object FeatureQuery {
    const val children="SELECT u.id,u.name,d.age,p.balance,COALESCE(q.enabled,0) FROM family_links f JOIN users u ON u.id=f.child_id JOIN demographics d ON d.user_id=u.id JOIN points_accounts p ON p.user_id=u.id LEFT JOIN quiz_permissions q ON q.child_id=u.id JOIN roles r ON r.user_id=f.parent_id AND r.role='Parent' WHERE f.parent_id=? ORDER BY d.age"
    const val parents="SELECT u.name FROM family_links f JOIN users u ON u.id=f.parent_id WHERE f.child_id=? ORDER BY u.name"
    const val quiz="SELECT q.id,q.prompt,q.a,q.b,q.c,q.d,COALESCE(t.status,''),COALESCE(p.enabled,0) FROM demographics d JOIN quiz_questions q ON d.age BETWEEN q.min_age AND q.max_age LEFT JOIN quiz_attempts t ON t.user_id=d.user_id AND t.question_id=q.id LEFT JOIN quiz_permissions p ON p.child_id=d.user_id WHERE d.user_id=? ORDER BY q.id"
    const val activity="SELECT reason,delta FROM point_activity WHERE user_id=? ORDER BY created_at DESC,id DESC"
    const val requests="SELECT r.actor,r.target,r.kind,r.status,u.name FROM identity_requests r JOIN users u ON u.id=CASE WHEN r.actor=? THEN r.target ELSE r.actor END WHERE r.actor=? OR r.target=?"
    const val avatar="SELECT style FROM avatars WHERE user_id=?"
    const val token="SELECT token FROM identity_tokens WHERE user_id=?"
    const val age="SELECT age FROM demographics WHERE user_id=?"
    const val resolve="SELECT user_id FROM identity_tokens WHERE token=?"
}
@Composable fun FamilyFeatures(db:LocalDatabase,user:Account,screen:String,onChange:()->Unit={}){
 val context=LocalContext.current
 val clipboard=LocalClipboardManager.current
 val scope=rememberCoroutineScope()
 var revision by remember{mutableStateOf(0)}
 var busy by remember{mutableStateOf(false)}
 var feedback by remember{mutableStateOf("")}
 var confirm by remember{mutableStateOf<Pair<String,()->Unit>?>(null)}
 fun rows(sql:String,vararg args:String):List<List<String>>{revision;return runCatching{db.rows(sql,*(if(args.isEmpty())arrayOf(user.id) else args))}.getOrDefault(emptyList())}
 val balance=runCatching{db.points(user)}.getOrDefault(0)
 val age=rows(FeatureQuery.age).firstOrNull()?.firstOrNull()?.toIntOrNull()?:0
 val kids=rows(FeatureQuery.children)
 val token=rows(FeatureQuery.token).firstOrNull()?.firstOrNull()?:""
 val payload="schoolday:identity:v1:"+token
 val current=rows(FeatureQuery.avatar).firstOrNull()?.firstOrNull()?:"initials"
 val styles=listOf("initials","star","leaf","book","cat","rocket")
 val icons=listOf("Aa","★","❋","▤","🐱","🚀")
 var choice by remember{mutableStateOf(current)}
 var child by remember{mutableStateOf("")}
 var amount by remember{mutableStateOf("10")}
 var purpose by remember{mutableStateOf("friend")}
 var code by remember{mutableStateOf("")}
 fun act(kind:String,target:String="",n:Int=0,value:String=""){
  if(busy)return
  busy=true
  scope.launch{
   try{withContext(Dispatchers.IO){db.writableDatabase.execSQL("INSERT INTO feature_actions VALUES(?,?,?,?,?,?,?)",arrayOf(UUID.randomUUID().toString(),user.id,kind,target,n,value,System.currentTimeMillis()))};revision++;onChange();feedback="Saved locally."}
   catch(e:kotlinx.coroutines.CancellationException){throw e}
   catch(e:Exception){feedback="Action rejected. Check permissions, balance or answer."}
   finally{busy=false}
  }
 }
 fun ask(message:String,action:()->Unit){confirm=message to action}
 val bitmap=remember(token){if(token.isBlank())null else runCatching{
  val matrix=QRCodeWriter().encode(payload,BarcodeFormat.QR_CODE,300,300)
  val pixels=IntArray(matrix.width*matrix.height){j->if(matrix[j%matrix.width,j/matrix.width])android.graphics.Color.BLACK else android.graphics.Color.WHITE}
  Bitmap.createBitmap(pixels,matrix.width,matrix.height,Bitmap.Config.ARGB_8888)
 }.getOrNull()}
 Column(verticalArrangement=Arrangement.spacedBy(10.dp)){
 if(screen=="Rewards"){
  Text("Points balance",style=MaterialTheme.typography.titleMedium);Text(balance.toString(),style=MaterialTheme.typography.headlineLarge)
  if(age>=18){Text("Buy points",style=MaterialTheme.typography.titleMedium);Text("Demo only · No real payment")
   listOf(100,200,500).forEach{n->Button(onClick={ask("Simulate purchase?"){act("buy","",n)}},enabled=!busy,modifier=Modifier.fillMaxWidth()){Text(n.toString()+" points · RM "+n/10)}}
  }
  if(kids.isNotEmpty()){Text("Transfer to children",style=MaterialTheme.typography.titleMedium)
   FeaturePicker("Child",kids.map{it[0] to (it[1]+" · "+it[3]+" points")},child,{child=it})
   OutlinedTextField(amount,{amount=it},label={Text("Points")},modifier=Modifier.fillMaxWidth())
   Button(onClick={val n=amount.toIntOrNull()?:0;ask("Transfer "+n+" points?"){act("transfer",child,n)}},enabled=!busy&&child.isNotEmpty()&&(amount.toIntOrNull()?:0) in 1..balance){Text("Transfer points")}
  }
  Text("Activity",style=MaterialTheme.typography.titleMedium);rows(FeatureQuery.activity).forEach{r->Text(r[0]+" · "+r[1])}
 }
 if(screen=="Profile"){
  Text("Avatar",style=MaterialTheme.typography.titleMedium);Text(icons[styles.indexOf(current).coerceAtLeast(0)],style=MaterialTheme.typography.headlineLarge)
  FeaturePicker("Avatar",styles.mapIndexed{j,v->v to icons[j]},choice,{choice=it})
  Text("10 points will be deducted. Balance: "+balance)
  Button(onClick={ask("Deduct 10 points?"){act("avatar","",0,choice)}},enabled=!busy&&choice!=current&&balance>=10){Text("Save avatar · 10 points")}
  HorizontalDivider();Text("QR identity",style=MaterialTheme.typography.titleMedium)
  bitmap?.let{Image(it.asImageBitmap(),"Schoolday QR identity",Modifier.widthIn(max=240.dp).fillMaxWidth().aspectRatio(1f));Button(onClick={PrintHelper(context).apply{scaleMode=PrintHelper.SCALE_MODE_FIT}.printBitmap("Schoolday identity",it)}){Text("Print QR")}}
  Text("Local demo identifier, not proof of identity.")
  Button(onClick={clipboard.setText(AnnotatedString(payload))}){Text("Copy identity code")}
  FeaturePicker("Use identity",listOf("friend" to "Friend request","parent" to "Parent link request","attendance" to "Demo attendance"),purpose,{purpose=it})
  OutlinedTextField(code,{code=it},label={Text("Paste scanned QR identity")},modifier=Modifier.fillMaxWidth())
  Button(onClick={val prefix="schoolday:identity:v1:";val value=code.trim().removePrefix(prefix);val target=if(code.trim().startsWith(prefix))rows(FeatureQuery.resolve,value).firstOrNull()?.firstOrNull() else null;if(target==null)feedback="Unknown identity" else act(purpose,target,0,value)},enabled=!busy&&code.isNotBlank()){Text("Continue")}
  Text("Parent requests require verification. Staff-only attendance is a demo.")
  rows(FeatureQuery.requests,user.id,user.id,user.id).forEach{r->Text(r[4]+" · "+r[2]+" · "+r[3]);if(r[1]==user.id&&r[2]=="friend"&&r[3]=="pending")Button(onClick={act("accept",r[0])},enabled=!busy){Text("Accept")}}
  if(kids.isNotEmpty()){HorizontalDivider();Text("Linked children",style=MaterialTheme.typography.titleMedium);kids.forEach{c->Text(c[1]);Text("Age "+c[2]+" · "+c[3]+" points");Row{Checkbox(checked=c[4]=="1",onCheckedChange={act("permission",c[0],if(it)1 else 0)},enabled=!busy);Text("Allow quiz skip · 5 points",modifier=Modifier.padding(top=12.dp))}}}
  val parents=rows(FeatureQuery.parents);if(parents.isNotEmpty()){Text("Linked parents",style=MaterialTheme.typography.titleMedium);parents.forEach{Text(it[0])}}
 }
 if(screen=="Quiz"){
  Text("Quiz · age "+age,style=MaterialTheme.typography.titleMedium)
  rows(FeatureQuery.quiz).forEach{q->Text(q[1]);if(q[6].isNotEmpty())Text(if(q[6]=="correct")"Completed" else "Skipped") else {
   (0..3).forEach{j->OutlinedButton(onClick={act("answer",q[0],j)},enabled=!busy,modifier=Modifier.fillMaxWidth()){Text(q[j+2])}}
   if(q[7]=="1")Button(onClick={ask("Deduct 5 points?"){act("skip",q[0])}},enabled=!busy&&balance>=5){Text("Skip · 5 points")} else Text("Quiz skip not enabled by parent.")
  }}
 }
 if(feedback.isNotEmpty())Text(feedback)
 }
 confirm?.let{pending->AlertDialog(onDismissRequest={confirm=null},title={Text(pending.first)},confirmButton={TextButton(onClick={confirm=null;pending.second()}){Text("Confirm")}},dismissButton={TextButton(onClick={confirm=null}){Text("Cancel")}})}
}
@Composable private fun FeaturePicker(label:String,options:List<Pair<String,String>>,selected:String,onChange:(String)->Unit){
 var open by remember{mutableStateOf(false)}
 Box{OutlinedButton(onClick={open=true},modifier=Modifier.fillMaxWidth()){Text(label+" · "+(options.firstOrNull{it.first==selected}?.second?:"Select"))};DropdownMenu(expanded=open,onDismissRequest={open=false}){options.forEach{(id,name)->DropdownMenuItem(text={Text(name)},onClick={open=false;onChange(id)})}}}
}
@Composable fun FeatureDialog(db:LocalDatabase,user:Account,screen:String,onClose:()->Unit,onChange:()->Unit){
 Dialog(onDismissRequest=onClose,properties=DialogProperties(usePlatformDefaultWidth=false)){
  Surface(modifier=Modifier.padding(20.dp).widthIn(max=500.dp).fillMaxWidth(),shape=MaterialTheme.shapes.large){
   Column(Modifier.verticalScroll(rememberScrollState()).padding(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){Text(screen,style=MaterialTheme.typography.headlineSmall);FamilyFeatures(db,user,screen,onChange);Button(onClick=onClose,modifier=Modifier.fillMaxWidth()){Text("Done")}}
  }
 }
}
