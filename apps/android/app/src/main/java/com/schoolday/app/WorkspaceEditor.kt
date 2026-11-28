package com.schoolday.app
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
@Composable fun WorkspaceEditor(db:LocalDatabase,user:Account,kind:String,lang:String,modifier:Modifier=Modifier,onSaved:()->Unit={}){
 fun t(key:String)=SchooldayLanguage.text(key,lang)
 var title by remember(kind){mutableStateOf("")}
 var details by remember(kind){mutableStateOf("")}
 var drafts by remember(kind){mutableStateOf(emptyList<List<String>>())}
 var busy by remember{mutableStateOf(false)}
 var failed by remember{mutableStateOf(false)}
 val scope=rememberCoroutineScope()
 val query="SELECT id,title,details FROM workspace_drafts WHERE owner_id=? AND kind=? ORDER BY created_at DESC,id"
 LaunchedEffect(kind){try{drafts=withContext(Dispatchers.IO){db.rows(query,user.id,kind)}}catch(e:kotlinx.coroutines.CancellationException){throw e}catch(e:Exception){failed=true}}
 Column(modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
  Text(t(when(kind){"Quiz"->"Quiz editor";"Certificates"->"Certificate studio";else->"Event planner"}),style=MaterialTheme.typography.headlineSmall)
  Text(t("Draft only · Not published"))
  OutlinedTextField(title,{title=it.take(120)},label={Text(t("Title"))},modifier=Modifier.fillMaxWidth())
  OutlinedTextField(details,{details=it.take(4000)},label={Text(t(when(kind){"Quiz"->"Question and answer choices";"Certificates"->"Recipient and achievement";else->"Date, time and location"}))},modifier=Modifier.fillMaxWidth(),minLines=4)
  Button(onClick={busy=true;val x=title.trim();val y=details.trim();scope.launch{try{drafts=withContext(Dispatchers.IO){db.writableDatabase.execSQL("INSERT INTO workspace_drafts VALUES(?,?,?,?,?,?)",arrayOf(UUID.randomUUID().toString(),user.id,kind,x,y,System.currentTimeMillis()));db.rows(query,user.id,kind)};title="";details="";onSaved()}catch(e:kotlinx.coroutines.CancellationException){throw e}catch(e:Exception){failed=true}finally{busy=false}}},enabled=!busy&&title.isNotBlank()&&details.isNotBlank()){Text(t("Save draft"))}
  if(failed)Text(t("Unable to save locally."),color=MaterialTheme.colorScheme.error)
 }
}
