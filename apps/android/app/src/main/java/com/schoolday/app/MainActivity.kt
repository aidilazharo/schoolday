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
    val sessionPrefs=LocalContext.current.getSharedPreferences("schoolday.session",0)
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
    fun rememberAccount(account:Account){sessionPrefs.edit().putString("account",account.id).apply();user=account}
    LaunchedEffect(db){try{
        val id=sessionPrefs.getString("account",null)
        val result=withContext(Dispatchers.IO){db.samples() to id?.let{db.accountById(it)}}
        accounts=result.first;user=result.second
        if(id!=null&&result.second==null)sessionPrefs.edit().remove("account").apply()
    }catch(e:kotlinx.coroutines.CancellationException){throw e}catch(e:Exception){error="Unable to open local database."}}
    val current=user
    Surface(Modifier.fillMaxSize()){
        if(current!=null)key(current.id){Workspace(db,current){sessionPrefs.edit().remove("account").apply();user=null;normal=false;email="";password="";error=""}}
        else Box(Modifier.fillMaxSize().safeDrawingPadding().imePadding().padding(24.dp),contentAlignment=Alignment.Center){
            Column(Modifier.widthIn(max=420.dp).fillMaxWidth().verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(12.dp)){
                Text("schoolday",style=MaterialTheme.typography.headlineLarge,fontWeight=FontWeight.Bold,fontFamily=FontFamily.Serif,fontStyle=FontStyle.Italic)
                Text(t("Sign in"),style=MaterialTheme.typography.headlineSmall)
                Box{
                    OutlinedButton(onClick={menu=true},enabled=accounts.isNotEmpty()&&!busy,modifier=Modifier.fillMaxWidth()){Text(t("Select sample user")+" ▾")}
                    DropdownMenu(expanded=menu,onDismissRequest={menu=false}){accounts.forEach{account->DropdownMenuItem(text={Text(account.name+" — "+SchooldayLanguage.roles(account.roles,lang))},onClick={menu=false;error="";rememberAccount(account)})}}
                }
                Button(onClick={normal=!normal;error=""},modifier=Modifier.fillMaxWidth(),enabled=!busy){Text(t(if(normal)"Hide login form" else "Normal user login"))}
                if(normal){
                    OutlinedTextField(email,{email=it},label={Text(t("Email"))},singleLine=true,modifier=Modifier.fillMaxWidth())
                    OutlinedTextField(password,{password=it},label={Text(t("Password"))},singleLine=true,visualTransformation=PasswordVisualTransformation(),modifier=Modifier.fillMaxWidth())
                    Button(onClick={
                        busy=true;error=""
                        val enteredEmail=email;val enteredPassword=password
                        scope.launch{try{val account=withContext(Dispatchers.IO){db.login(enteredEmail,enteredPassword)};if(account!=null){rememberAccount(account);password=""}else error="Invalid email or password."}catch(e:Exception){error="Sign-in unavailable."}finally{busy=false}}
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
    var category by remember{mutableStateOf("Connect")}
    var moduleId by remember{mutableStateOf("Chats")}
    var record by remember{mutableStateOf("")}
    var revision by remember{mutableStateOf(0)}
    val module=SchoolModules.visible(category,user.roles).firstOrNull{it.id==moduleId}?:SchoolModules.visible(category,user.roles).first()
    val tab=module.id
    val kind=when(tab){"QuizDraft"->"Quiz";"CertificateDraft"->"Certificates";"EventDraft"->"Calendar";else->null}
    val drafts=remember(kind,revision){if(kind!=null)runCatching{db.rows("SELECT id,title,details FROM workspace_drafts WHERE owner_id=? AND kind=? ORDER BY created_at DESC,id",user.id,kind)}.getOrDefault(emptyList()) else emptyList()}
    var moduleMenu by remember{mutableStateOf(false)}
    val staff=user.roles.split(" + ").any{it=="Teacher"||it=="School Admin"}
    val tabs=SchoolModules.categories
    var points by remember{mutableStateOf(0)}
    var displayName by remember{mutableStateOf(SchooldayNames.options(user.name).firstOrNull()?:user.name)}
    var nicknameMenu by remember{mutableStateOf(false)}
    fun t(key:String)=SchooldayLanguage.text(key,lang)
    var spaces by remember{mutableStateOf(emptyList<ChatSpace>())}
    var selected by remember{mutableStateOf("")}
    var opened by remember{mutableStateOf(false)}
    var error by remember{mutableStateOf(false)}
    var busy by remember{mutableStateOf(false)}
    val scope=rememberCoroutineScope()
    LaunchedEffect(user.id){try{displayName=withContext(Dispatchers.IO){db.nickname(user)};points=withContext(Dispatchers.IO){db.points(user)};spaces=withContext(Dispatchers.IO){db.spaces(user)};selected=spaces.firstOrNull()?.id?:""}catch(e:Exception){error=true}}
    BoxWithConstraints(Modifier.fillMaxSize().safeDrawingPadding().imePadding()){
        val wide=maxWidth>=768.dp
        val sidebarWidth=(maxWidth*0.30f).coerceIn(250.dp,390.dp)
        BackHandler(enabled=!wide&&opened){if(!busy)opened=false}
        Row(Modifier.fillMaxSize()){
            if(wide||!opened){
                Column(if(wide)Modifier.width(sidebarWidth).fillMaxHeight() else Modifier.fillMaxSize()){
                    Row(Modifier.fillMaxWidth().padding(12.dp),verticalAlignment=Alignment.CenterVertically){Text("schoolday",modifier=Modifier.weight(1f),maxLines=1,style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold,fontFamily=FontFamily.Serif,fontStyle=FontStyle.Italic);TextButton(onClick=logout,enabled=!busy){Text(t("Logout"))}}
                    HorizontalDivider()
                    LazyColumn(Modifier.weight(1f)){
                        item{Box(Modifier.padding(8.dp)){OutlinedButton(onClick={moduleMenu=true},modifier=Modifier.fillMaxWidth()){Text(category+" · "+module.title)};DropdownMenu(expanded=moduleMenu,onDismissRequest={moduleMenu=false}){SchoolModules.visible(category,user.roles).forEach{m->DropdownMenuItem(text={Text(m.title+if(m.status=="local")"" else " · "+if(m.status=="partial")"Partial" else "Not implemented")},onClick={moduleMenu=false;moduleId=m.id;record="";opened=false})}}}}
                        item{Column(Modifier.fillMaxWidth()){if(module.status!="local")HorizontalDivider(thickness=3.dp,color=androidx.compose.ui.graphics.Color.Red);Text(module.title,modifier=Modifier.padding(12.dp),fontWeight=FontWeight.Bold);if(module.status!="local")Text(if(module.status=="partial")"Partially implemented" else "Not implemented",modifier=Modifier.padding(horizontal=12.dp),color=androidx.compose.ui.graphics.Color.Red)}}
                        if(tab=="Chats"){items(spaces,key={it.id}){space->TextButton(onClick={selected=space.id;opened=true},enabled=!busy,modifier=Modifier.fillMaxWidth()){Text(t(space.title),modifier=Modifier.weight(1f).padding(8.dp));Text("›")};HorizontalDivider()}}
                        else if(kind!=null&&staff){item{TextButton(onClick={record="";opened=true}){Text("+ "+t("Create draft"))}};if(drafts.isEmpty())item{Text(t("No drafts yet"),modifier=Modifier.padding(12.dp))};items(drafts,key={it[0]}){r->TextButton(onClick={record=r[0];opened=true},modifier=Modifier.fillMaxWidth()){Column(Modifier.weight(1f)){Text(r[1],fontWeight=FontWeight.Bold);Text(r[2],maxLines=2,style=MaterialTheme.typography.bodySmall);Text(t("Draft only · Not published"),style=MaterialTheme.typography.labelSmall)}}}}
                        else if(module.records.isNotEmpty()){items(module.records,key={it}){r->TextButton(onClick={record=r;opened=true},modifier=Modifier.fillMaxWidth()){Column(Modifier.weight(1f)){Text(r);Text("Sample preview",style=MaterialTheme.typography.labelSmall)}}}}
                        else item{TextButton(onClick={opened=true}){Text(module.title+" ›")}}
                    }
                    HorizontalDivider()
                    Row(Modifier.fillMaxWidth()){
                        tabs.forEachIndexed{j,name->TextButton(onClick={category=name;moduleId=SchoolModules.visible(name,user.roles).first().id;record="";opened=false},enabled=!busy,modifier=Modifier.weight(1f),contentPadding=PaddingValues(2.dp)){Column(horizontalAlignment=Alignment.CenterHorizontally){Text(listOf("◉","▤","▦","☆","○")[j],style=MaterialTheme.typography.titleLarge);Text(t(name),style=MaterialTheme.typography.labelSmall,fontWeight=if(tab==name)FontWeight.Bold else FontWeight.Normal,maxLines=2)}}}
                    }
                }
            }
            if(wide)VerticalDivider()
            if(wide||opened){
                Column(Modifier.weight(1f).fillMaxHeight()){
                    Row(Modifier.fillMaxWidth().padding(12.dp),verticalAlignment=Alignment.CenterVertically){
                        Column(Modifier.weight(1f)){Text(t(if(tab=="Chats")spaces.firstOrNull{it.id==selected}?.title?:"Chats" else module.title),fontWeight=FontWeight.Bold);if(tab=="Chats")Text(t("Local only"),style=MaterialTheme.typography.bodySmall)}
                        if(!wide)TextButton(onClick={opened=false},enabled=!busy){Text(t("Back"))}
                    }
                    if(module.status!="local"){HorizontalDivider(thickness=3.dp,color=androidx.compose.ui.graphics.Color.Red);Text(if(module.status=="partial")"Partially implemented" else "Not implemented",modifier=Modifier.padding(8.dp),color=androidx.compose.ui.graphics.Color.Red)}else HorizontalDivider()
                    if(tab=="Chats"){if(selected.isNotEmpty())key(selected){WallView(db,user,selected,lang,busy,{busy=it},Modifier.weight(1f))}}
                    else if(kind!=null&&staff){val picked=drafts.firstOrNull{it[0]==record};if(picked!=null){Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(20.dp)){Text(picked[1],style=MaterialTheme.typography.headlineSmall);Text(picked[2]);Text(t("Draft only · Not published"));Button(onClick={record=""}){Text(t("Create draft"))}}}else key(tab){WorkspaceEditor(db,user,kind,lang,Modifier.weight(1f)){revision++}}}
                    else if(tab=="Profile")Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
                    Text(t("Full name"),style=MaterialTheme.typography.bodySmall)
                    Text(user.name,fontWeight=FontWeight.Bold)
                    Text(t("Nickname"),style=MaterialTheme.typography.bodySmall)
                    Box {
                        OutlinedButton(onClick={nicknameMenu=true},enabled=!busy,modifier=Modifier.fillMaxWidth()){Text(displayName+" ▾")}
                        DropdownMenu(expanded=nicknameMenu,onDismissRequest={nicknameMenu=false}){
                            SchooldayNames.options(user.name).forEach{option->
                                DropdownMenuItem(text={Text(option)},onClick={
                                    nicknameMenu=false;busy=true
                                    scope.launch{try{withContext(Dispatchers.IO){db.saveNickname(user,option)};displayName=option}catch(e:Exception){error=true}finally{busy=false}}
                                })
                            }
                        }
                    }
                    Text(t("Choose one or two parts of your full name."),style=MaterialTheme.typography.bodySmall)
                    Text(user.email)
                    Text(t("Points balance")+": "+points)
                    Text(SchooldayLanguage.roles(user.roles,lang))
                    LanguageMenu(lang,::setLang)
                    CountryMenu(country,lang,::setCountry)
                    FamilyFeatures(db,user,"Profile"){points=db.points(user)}

                    }
                    else if(tab=="Rewards"||tab=="Quiz")key(tab){Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(20.dp)){FamilyFeatures(db,user,tab){points=db.points(user)}}}
                    else key(tab,record){ModulePreview(module,db,user,record,Modifier.weight(1f))}
                }
            }
        }
    }
    if(error)AlertDialog(onDismissRequest={error=false},title={Text(t("Local database unavailable"))},confirmButton={TextButton(onClick={error=false}){Text(t("OK"))}})
}
