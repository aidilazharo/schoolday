package com.schoolday.app
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable fun WallView(db:LocalDatabase,user:Account,channel:String,lang:String,busy:Boolean,setBusy:(Boolean)->Unit,modifier:Modifier=Modifier){
 fun t(key:String)=SchooldayLanguage.text(key,lang)
 var posts by remember(channel){mutableStateOf(emptyList<WallPost>())}
 var text by remember(channel){mutableStateOf("")}
 val comments=remember(channel){mutableStateMapOf<String,String>()}
 var failed by remember(channel){mutableStateOf(false)}
 val scope=rememberCoroutineScope()
 LaunchedEffect(channel){try{posts=withContext(Dispatchers.IO){db.feed(user,channel)}}catch(e:kotlinx.coroutines.CancellationException){throw e}catch(e:Exception){failed=true}}
 fun act(action:String,body:String,id:String=""){
  if(busy)return;setBusy(true)
  scope.launch{try{posts=withContext(Dispatchers.IO){db.wallAction(user,channel,action,body,id);db.feed(user,channel)};if(action=="post")text="";if(action=="comment")comments[id]=""}catch(e:kotlinx.coroutines.CancellationException){throw e}catch(e:Exception){failed=true}finally{setBusy(false)}}
 }
 LazyColumn(modifier.fillMaxWidth(),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
  item{
   OutlinedCard(Modifier.fillMaxWidth()){Column(Modifier.padding(12.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
    OutlinedTextField(text,{text=it.take(2000)},placeholder={Text(t("Write a post"))},modifier=Modifier.fillMaxWidth(),minLines=3)
    Button(onClick={act("post",text)},enabled=!busy&&text.isNotBlank()){Text(t("Post"))}
   }}
  }
  if(posts.isEmpty())item{Text(t("No posts yet"))}
  items(posts,key={it.id}){post->
   OutlinedCard(Modifier.fillMaxWidth()){Column(Modifier.padding(12.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
    Text(post.name,fontWeight=FontWeight.Bold)
    Text(java.text.DateFormat.getDateInstance().format(java.util.Date(post.createdAt)),style=MaterialTheme.typography.bodySmall)
    Text(post.body)
    HorizontalDivider()
    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){
     TextButton(onClick={act("like","",post.id)},enabled=!busy){Text((if(post.liked)"♥ " else "♡ ")+t("Like")+" ("+post.likes+")")}
     Text(t("Comments")+" ("+post.comments.size+")",modifier=Modifier.padding(top=12.dp))
    }
    post.comments.forEach{comment->Surface(shape=RoundedCornerShape(8.dp),color=MaterialTheme.colorScheme.surfaceVariant){Column(Modifier.fillMaxWidth().padding(8.dp)){Text(comment.name,fontWeight=FontWeight.Bold);Text(comment.body)}}}
    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){
     OutlinedTextField(comments[post.id]?:"",{comments[post.id]=it.take(500)},placeholder={Text(t("Comment"))},modifier=Modifier.weight(1f))
     TextButton(onClick={act("comment",comments[post.id]?:"",post.id)},enabled=!busy&&!comments[post.id].isNullOrBlank()){Text(t("Comment"))}
    }
   }}
  }
 }
 if(failed)AlertDialog(onDismissRequest={failed=false},title={Text(t("Unable to save locally."))},confirmButton={TextButton(onClick={failed=false}){Text(t("OK"))}})
}
