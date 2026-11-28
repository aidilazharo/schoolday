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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
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
    val lang="ms"
    fun t(key:String)=SchooldayLanguage.text(key,lang)
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
                Text("schoolday",style=MaterialTheme.typography.headlineLarge,fontWeight=FontWeight.Bold,fontFamily=FontFamily.Serif,fontStyle=FontStyle.Italic)
                Text(t("Sign in"),style=MaterialTheme.typography.headlineSmall)
                Box{
                    OutlinedButton(onClick={menu=true},enabled=accounts.isNotEmpty()&&!busy,modifier=Modifier.fillMaxWidth()){Text(t("Select sample user")+" ▾")}
                    DropdownMenu(expanded=menu,onDismissRequest={menu=false}){accounts.forEach{account->DropdownMenuItem(text={Text(account.name+" — "+SchooldayLanguage.roles(account.roles,lang))},onClick={menu=false;error="";user=account})}}
                }
                Button(onClick={normal=!normal;error=""},modifier=Modifier.fillMaxWidth(),enabled=!busy){Text(t(if(normal)"Hide login form" else "Normal user login"))}
                if(normal){
                    OutlinedTextField(email,{email=it},label={Text(t("Email"))},singleLine=true,modifier=Modifier.fillMaxWidth())
                    OutlinedTextField(password,{password=it},label={Text(t("Password"))},singleLine=true,visualTransformation=PasswordVisualTransformation(),modifier=Modifier.fillMaxWidth())
                    Button(onClick={
                        busy=true;error=""
                        val enteredEmail=email;val enteredPassword=password
                        scope.launch{try{val account=withContext(Dispatchers.IO){db.login(enteredEmail,enteredPassword)};if(account!=null){user=account;password=""}else error="Invalid email or password."}catch(e:Exception){error="Sign-in unavailable."}finally{busy=false}}
                    },enabled=!busy&&email.isNotBlank()&&password.isNotEmpty(),modifier=Modifier.fillMaxWidth()){Text(t("Login"))}
                }
                Text(t("Local test accounts only. Do not enter real credentials."),style=MaterialTheme.typography.bodySmall)
                if(error.isNotEmpty())Text(t(error),color=MaterialTheme.colorScheme.error)
            }
        }
    }
}
@Composable private fun Workspace(db:LocalDatabase,user:Account,logout:()->Unit){
    val prefs=LocalContext.current.getSharedPreferences("schoolday.settings",0)
    var lang by remember(user.id){mutableStateOf(SchooldayLanguage.normalize(prefs.getString("language."+user.id,null)))}
    fun setLang(value:String){lang=value;prefs.edit().putString("language."+user.id,value).apply()}
    var country by remember(user.id){mutableStateOf(SchooldayCountry.normalize(prefs.getString("country."+user.id,null)))}
    fun setCountry(value:String){country=value;prefs.edit().putString("country."+user.id,value).apply()}
    var showProfile by remember{mutableStateOf(false)}
    fun t(key:String)=SchooldayLanguage.text(key,lang)
    var spaces by remember{mutableStateOf(emptyList<ChatSpace>())}
    var selected by remember{mutableStateOf("")}
    var messages by remember{mutableStateOf(emptyList<Draft>())}
    var text by remember{mutableStateOf("")}
    var opened by remember{mutableStateOf(false)}
    var confirm by remember{mutableStateOf(false)}
    var error by remember{mutableStateOf(false)}
    var busy by remember{mutableStateOf(false)}
    var loading by remember{mutableStateOf(true)}
    val scope=rememberCoroutineScope()
    LaunchedEffect(user.id){try{spaces=withContext(Dispatchers.IO){db.spaces(user)};selected=spaces.firstOrNull()?.id?:""}catch(e:Exception){error=true}}
    LaunchedEffect(selected){if(selected.isNotEmpty()){loading=true;try{messages=withContext(Dispatchers.IO){db.drafts(user,selected)}}catch(e:kotlinx.coroutines.CancellationException){throw e}catch(e:Exception){error=true}finally{loading=false}}}
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
                    Row(Modifier.fillMaxWidth().padding(16.dp),verticalAlignment=Alignment.CenterVertically){Text("schoolday",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold,fontFamily=FontFamily.Serif,fontStyle=FontStyle.Italic);Spacer(Modifier.weight(1f));TextButton(onClick=logout,enabled=!busy){Text(t("Logout"))}}
                    HorizontalDivider()
                    Row(Modifier.fillMaxWidth().padding(16.dp),verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text(user.name,fontWeight=FontWeight.Bold);Text(SchooldayLanguage.roles(user.roles,lang),style=MaterialTheme.typography.bodySmall)};TextButton(onClick={showProfile=true}){Text(t("Profile"))}}
                    HorizontalDivider()
                    Text(t("Chats"),style=MaterialTheme.typography.headlineSmall,modifier=Modifier.padding(16.dp))
                    LazyColumn{items(spaces,key={it.id}){space->
                        TextButton(onClick={if(selected!=space.id){selected=space.id;messages=emptyList();text=""};opened=true},enabled=!busy,modifier=Modifier.fillMaxWidth()){Text(t(space.title),modifier=Modifier.weight(1f).padding(8.dp));Text("›")}
                        HorizontalDivider()
                    }}
                }
            }
            if(wide)VerticalDivider()
            if(wide||opened){
                Column(Modifier.weight(1f).fillMaxHeight()){
                    Row(Modifier.fillMaxWidth().padding(12.dp),verticalAlignment=Alignment.CenterVertically){
                        Column(Modifier.weight(1f)){Text(t(spaces.firstOrNull{it.id==selected}?.title?:"Chats"),fontWeight=FontWeight.Bold);Text(t("Local only"),style=MaterialTheme.typography.bodySmall)}
                        TextButton(onClick={confirm=true},enabled=messages.isNotEmpty()&&!busy){Text(t("Clear"))}
                        if(!wide)TextButton(onClick={opened=false}){Text(t("Back"))}
                    }
                    HorizontalDivider()
                    if(messages.isEmpty())Box(Modifier.weight(1f).fillMaxWidth(),contentAlignment=Alignment.Center){Text(t("No saved messages"))}
                    else LazyColumn(Modifier.weight(1f).fillMaxWidth(),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
                        items(messages,key={it.id}){message->Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.End){Surface(shape=RoundedCornerShape(14.dp),color=Color(0xFFF1F1F1)){Column(Modifier.widthIn(max=480.dp).padding(12.dp)){Text(message.text);Text(t("Saved locally · Not sent"),style=MaterialTheme.typography.labelSmall)}}}}
                    }
                    HorizontalDivider()
                    Row(Modifier.fillMaxWidth().padding(12.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)){
                        OutlinedTextField(text,{text=it.take(500)},placeholder={Text(t("Message"))},modifier=Modifier.weight(1f))
                        Button(onClick={change(text.trim())},enabled=!busy&&!loading&&selected.isNotEmpty()&&text.isNotBlank()){Text(t("Save"))}
                    }
                }
            }
        }
    }
    if(showProfile) {
        androidx.compose.ui.window.Dialog(onDismissRequest={showProfile=false},properties=androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth=false)){
            Surface(shape=RoundedCornerShape(16.dp),modifier=Modifier.padding(20.dp).widthIn(max=460.dp).fillMaxWidth()){
                Column(Modifier.verticalScroll(rememberScrollState()).padding(24.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
                    Text(t("Profile"),style=MaterialTheme.typography.headlineSmall)
                    Text(user.name,fontWeight=FontWeight.Bold)
                    Text(user.email)
                    Text(SchooldayLanguage.roles(user.roles,lang))
                    LanguageMenu(lang,::setLang)
                    CountryMenu(country,lang,::setCountry)
                    Button(onClick={showProfile=false},modifier=Modifier.fillMaxWidth()){Text(t("Done"))}
                }
            }
        }
    }
    if(confirm)AlertDialog(onDismissRequest={confirm=false},title={Text(t("Clear local drafts?"))},confirmButton={TextButton(onClick={confirm=false;change(null)}){Text(t("Clear"))}},dismissButton={TextButton(onClick={confirm=false}){Text(t("Cancel"))}})
    if(error)AlertDialog(onDismissRequest={error=false},title={Text(t("Local database unavailable"))},confirmButton={TextButton(onClick={error=false}){Text(t("OK"))}})
}
