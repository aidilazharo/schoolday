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
    var showProfile by remember{mutableStateOf(false)}
    var showRewards by remember{mutableStateOf(false)}
    var showQuiz by remember{mutableStateOf(false)}
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
        val sidebarWidth=(maxWidth*0.36f).coerceIn(290.dp,420.dp)
        BackHandler(enabled=!wide&&opened){if(!busy)opened=false}
        Row(Modifier.fillMaxSize()){
            if(wide||!opened){
                Column(if(wide)Modifier.width(sidebarWidth).fillMaxHeight() else Modifier.fillMaxSize()){
                    if(showProfile){Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
                    Text(t("Profile"),style=MaterialTheme.typography.headlineSmall)
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
                    Button(onClick={showProfile=false},modifier=Modifier.fillMaxWidth()){Text(t("Done"))}
                    }}else{
                    Row(Modifier.fillMaxWidth().padding(16.dp),verticalAlignment=Alignment.CenterVertically){Text("schoolday",modifier=Modifier.weight(1f),maxLines=1,overflow=androidx.compose.ui.text.style.TextOverflow.Ellipsis,style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold,fontFamily=FontFamily.Serif,fontStyle=FontStyle.Italic);TextButton(onClick={showProfile=true},enabled=!busy){Text(t("Profile"))};TextButton(onClick=logout,enabled=!busy){Text(t("Logout"))}}
                    HorizontalDivider()
                    Row(Modifier.fillMaxWidth().padding(16.dp),verticalAlignment=Alignment.CenterVertically){Spacer(Modifier.weight(1f));TextButton(onClick={showQuiz=true}){Text(t("Quiz"))};TextButton(onClick={showRewards=true}){Text(t("Rewards"))}}
                    LazyColumn{items(spaces,key={it.id}){space->
                        TextButton(onClick={selected=space.id;opened=true},enabled=!busy,modifier=Modifier.fillMaxWidth()){Text(t(space.title),modifier=Modifier.weight(1f).padding(8.dp));Text("›")}
                        HorizontalDivider()
                    }}
                    }
                }
            }
            if(wide)VerticalDivider()
            if(wide||opened){
                Column(Modifier.weight(1f).fillMaxHeight()){
                    Row(Modifier.fillMaxWidth().padding(12.dp),verticalAlignment=Alignment.CenterVertically){
                        Column(Modifier.weight(1f)){Text(t(spaces.firstOrNull{it.id==selected}?.title?:"Chats"),fontWeight=FontWeight.Bold);Text(t("Local only"),style=MaterialTheme.typography.bodySmall)}
                        if(!wide)TextButton(onClick={opened=false},enabled=!busy){Text(t("Back"))}
                    }
                    HorizontalDivider()
                    if(selected.isNotEmpty())key(selected){WallView(db,user,selected,lang,busy,{busy=it},Modifier.weight(1f))}
                }
            }
        }
    }
    if(showRewards) FeatureDialog(db,user,"Rewards",{showRewards=false},{points=db.points(user)})
    if(showQuiz) FeatureDialog(db,user,"Quiz",{showQuiz=false},{points=db.points(user)})
    if(error)AlertDialog(onDismissRequest={error=false},title={Text(t("Local database unavailable"))},confirmButton={TextButton(onClick={error=false}){Text(t("OK"))}})
}
