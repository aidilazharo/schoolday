package com.schoolday.app
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity:ComponentActivity(){
    private lateinit var db:LocalDatabase
    override fun onCreate(savedInstanceState:Bundle?){
        super.onCreate(savedInstanceState);enableEdgeToEdge();db=LocalDatabase(this)
        setContent{MaterialTheme(colorScheme=lightColorScheme(primary=Color(0xFF171717),background=Color.White,surface=Color.White)){Schoolday(db)}}
    }
    override fun onDestroy(){super.onDestroy();db.close()}
}
@Composable private fun Schoolday(db:LocalDatabase){
    var user by remember{mutableStateOf<Account?>(null)}
    var accounts by remember{mutableStateOf(emptyList<Account>())}
    var normal by remember{mutableStateOf(false)}
    var menu by remember{mutableStateOf(false)}
    var email by remember{mutableStateOf("")}
    var password by remember{mutableStateOf("")}
    var error by remember{mutableStateOf("")}
    var busy by remember{mutableStateOf(false)}
    val scope=rememberCoroutineScope()
    LaunchedEffect(db){try{accounts=withContext(Dispatchers.IO){db.samples()}}catch(e:Exception){error="Unable to open local database."}}
    val current=user
    Surface(Modifier.fillMaxSize()){
        if(current!=null)key(current.id){Workspace(db,current){user=null;normal=false;email="";password="";error=""}}
        else Box(Modifier.fillMaxSize().safeDrawingPadding().imePadding().padding(24.dp),contentAlignment=Alignment.Center){
            Column(Modifier.widthIn(max=420.dp).fillMaxWidth().verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(12.dp)){
                Text("schoolday",style=MaterialTheme.typography.headlineLarge,fontWeight=FontWeight.Bold)
                Text("Sign in",style=MaterialTheme.typography.headlineSmall)
                Box{
                    OutlinedButton(onClick={menu=true},enabled=accounts.isNotEmpty()&&!busy,modifier=Modifier.fillMaxWidth()){Text("Select sample user ▾")}
                    DropdownMenu(expanded=menu,onDismissRequest={menu=false}){accounts.forEach{account->DropdownMenuItem(text={Text(account.name+" — "+account.roles)},onClick={menu=false;error="";user=account})}}
                }
                Button(onClick={normal=!normal;error=""},modifier=Modifier.fillMaxWidth(),enabled=!busy){Text(if(normal)"Hide login form" else "Normal user login")}
                if(normal){
                    OutlinedTextField(email,{email=it},label={Text("Email")},singleLine=true,modifier=Modifier.fillMaxWidth())
                    OutlinedTextField(password,{password=it},label={Text("Password")},singleLine=true,visualTransformation=PasswordVisualTransformation(),modifier=Modifier.fillMaxWidth())
                    Button(onClick={
                        busy=true;error=""
                        val enteredEmail=email;val enteredPassword=password
                        scope.launch{try{val account=withContext(Dispatchers.IO){db.login(enteredEmail,enteredPassword)};if(account!=null){user=account;password=""}else error="Invalid email or password."}catch(e:Exception){error="Sign-in unavailable."}finally{busy=false}}
                    },enabled=!busy&&email.isNotBlank()&&password.isNotEmpty(),modifier=Modifier.fillMaxWidth()){Text("Login")}
                }
                Text("Local test accounts only. Do not enter real credentials.",style=MaterialTheme.typography.bodySmall)
                if(error.isNotEmpty())Text(error,color=MaterialTheme.colorScheme.error)
            }
        }
    }
}
@Composable private fun Workspace(db:LocalDatabase,user:Account,logout:()->Unit){
    var spaces by remember{mutableStateOf(emptyList<ChatSpace>())}
    var selected by remember{mutableStateOf("")}
    var messages by remember{mutableStateOf(emptyList<Draft>())}
    var text by remember{mutableStateOf("")}
    var opened by remember{mutableStateOf(false)}
    var confirm by remember{mutableStateOf(false)}
    var error by remember{mutableStateOf(false)}
    var busy by remember{mutableStateOf(false)}
    val scope=rememberCoroutineScope()
    LaunchedEffect(user.id){try{spaces=withContext(Dispatchers.IO){db.spaces(user)};selected=spaces.firstOrNull()?.id?:""}catch(e:Exception){error=true}}
    LaunchedEffect(selected){if(selected.isNotEmpty())try{messages=withContext(Dispatchers.IO){db.drafts(user,selected)}}catch(e:Exception){error=true}}
    fun change(value:String?){if(busy||selected.isEmpty())return;busy=true;val channel=selected
        scope.launch{try{messages=withContext(Dispatchers.IO){db.write(user,channel,value);db.drafts(user,channel)};if(value!=null)text=""}catch(e:Exception){error=true}finally{busy=false}}
    }
    BoxWithConstraints(Modifier.fillMaxSize().safeDrawingPadding().imePadding()){
        val wide=maxWidth>=768.dp
        val sidebarWidth=(maxWidth*0.32f).coerceAtMost(420.dp)
        BackHandler(enabled=!wide&&opened){opened=false}
        Row(Modifier.fillMaxSize()){
            if(wide||!opened){
                Column(if(wide)Modifier.width(sidebarWidth).fillMaxHeight() else Modifier.fillMaxSize()){
                    Row(Modifier.fillMaxWidth().padding(16.dp),verticalAlignment=Alignment.CenterVertically){Text("schoolday",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold);Spacer(Modifier.weight(1f));TextButton(onClick=logout,enabled=!busy){Text("Logout")}}
                    HorizontalDivider()
                    Column(Modifier.padding(16.dp)){Text(user.name,fontWeight=FontWeight.Bold);Text(user.roles,style=MaterialTheme.typography.bodySmall)}
                    HorizontalDivider()
                    Text("Chats",style=MaterialTheme.typography.headlineSmall,modifier=Modifier.padding(16.dp))
                    LazyColumn{items(spaces,key={it.id}){space->
                        TextButton(onClick={selected=space.id;messages=emptyList();text="";opened=true},enabled=!busy,modifier=Modifier.fillMaxWidth()){Text(space.title,modifier=Modifier.weight(1f).padding(8.dp));Text("›")}
                        HorizontalDivider()
                    }}
                }
            }
            if(wide)VerticalDivider()
            if(wide||opened){
                Column(Modifier.weight(1f).fillMaxHeight()){
                    Row(Modifier.fillMaxWidth().padding(12.dp),verticalAlignment=Alignment.CenterVertically){
                        Column(Modifier.weight(1f)){Text(spaces.firstOrNull{it.id==selected}?.title?:"Chats",fontWeight=FontWeight.Bold);Text("Local only",style=MaterialTheme.typography.bodySmall)}
                        TextButton(onClick={confirm=true},enabled=messages.isNotEmpty()&&!busy){Text("Clear")}
                        if(!wide)TextButton(onClick={opened=false}){Text("Back")}
                    }
                    HorizontalDivider()
                    if(messages.isEmpty())Box(Modifier.weight(1f).fillMaxWidth(),contentAlignment=Alignment.Center){Text("No saved messages")}
                    else LazyColumn(Modifier.weight(1f).fillMaxWidth(),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
                        items(messages,key={it.id}){message->Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.End){Surface(shape=RoundedCornerShape(14.dp),color=Color(0xFFF1F1F1)){Column(Modifier.widthIn(max=480.dp).padding(12.dp)){Text(message.text);Text("Saved locally · Not sent",style=MaterialTheme.typography.labelSmall)}}}}
                    }
                    HorizontalDivider()
                    Row(Modifier.fillMaxWidth().padding(12.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)){
                        OutlinedTextField(text,{text=it.take(500)},placeholder={Text("Message")},modifier=Modifier.weight(1f))
                        Button(onClick={change(text.trim())},enabled=!busy&&selected.isNotEmpty()&&text.isNotBlank()){Text("Save")}
                    }
                }
            }
        }
    }
    if(confirm)AlertDialog(onDismissRequest={confirm=false},title={Text("Clear local drafts?")},confirmButton={TextButton(onClick={confirm=false;change(null)}){Text("Clear")}},dismissButton={TextButton(onClick={confirm=false}){Text("Cancel")}})
    if(error)AlertDialog(onDismissRequest={error=false},title={Text("Local database unavailable")},confirmButton={TextButton(onClick={error=false}){Text("OK")}})
}
