package com.schoolday.app
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private data class TimelineEntry(val id:String,val time:Long,val post:WallPost?=null,val message:SpaceMessage?=null)
@Composable fun WallView(db:LocalDatabase,user:Account,channel:String,lang:String,busy:Boolean,setBusy:(Boolean)->Unit,modifier:Modifier=Modifier){
 fun t(key:String)=SchooldayLanguage.text(key,lang)
 var posts by remember(channel){mutableStateOf(emptyList<WallPost>())}
 var messages by remember(channel){mutableStateOf(emptyList<SpaceMessage>())}
 var isPost by remember(channel){mutableStateOf(false)}
 var text by remember(channel){mutableStateOf("")}
 val comments=remember(channel){mutableStateMapOf<String,String>()}
 var failed by remember(channel){mutableStateOf(false)}
 val scope=rememberCoroutineScope()
 LaunchedEffect(channel){try{val result=withContext(Dispatchers.IO){db.feed(user,channel) to db.spaceMessages(user,channel)};posts=result.first;messages=result.second}catch(e:kotlinx.coroutines.CancellationException){throw e}catch(e:Exception){failed=true}}
 fun act(action:String,body:String,id:String=""){
  if(busy)return;setBusy(true)
  scope.launch{try{val result=withContext(Dispatchers.IO){if(action=="chat")db.sendSpaceMessage(user,channel,body) else db.wallAction(user,channel,action,body,id);db.feed(user,channel) to db.spaceMessages(user,channel)};posts=result.first;messages=result.second;if(action=="post"||action=="chat")text="";if(action=="comment")comments[id]=""}catch(e:kotlinx.coroutines.CancellationException){throw e}catch(e:Exception){failed=true}finally{setBusy(false)}}
 }
 val entries=(posts.map{TimelineEntry("p:"+it.id,it.createdAt,post=it)}+messages.map{TimelineEntry("m:"+it.id,it.createdAt,message=it)}).sortedWith(compareBy<TimelineEntry>{it.time}.thenBy{it.id})
 val listState=rememberLazyListState()
 LaunchedEffect(entries.size){if(entries.isNotEmpty())listState.scrollToItem(entries.lastIndex)}
 Column(modifier.fillMaxWidth()){
 LazyColumn(Modifier.weight(1f).fillMaxWidth(),state=listState,contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
  items(entries,key={it.id}){entry->
   val message=entry.message
   val post=entry.post
   if(message!=null){
    Row(Modifier.fillMaxWidth(),horizontalArrangement=if(message.authorId==user.id)Arrangement.End else Arrangement.Start){
     Surface(shape=RoundedCornerShape(12.dp),color=MaterialTheme.colorScheme.surfaceVariant){
      Column(Modifier.widthIn(max=480.dp).padding(12.dp)){
       if(message.authorId!=user.id)Text(message.name,fontWeight=FontWeight.Bold)
       Text(message.body)
       Text(java.text.DateFormat.getTimeInstance(java.text.DateFormat.SHORT).format(java.util.Date(message.createdAt))+" · "+t(if(message.draft)"Saved locally · Not sent" else "Local only"),style=MaterialTheme.typography.labelSmall)
      }
     }
    }
   }else if(post!=null){
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
 }
 HorizontalDivider()
 Row(Modifier.fillMaxWidth().padding(8.dp),horizontalArrangement=Arrangement.spacedBy(8.dp)){
  if(!isPost)Button(onClick={isPost=false},enabled=!busy,modifier=Modifier.weight(1f)){Text(t("Chat"))} else OutlinedButton(onClick={isPost=false},enabled=!busy,modifier=Modifier.weight(1f)){Text(t("Chat"))}
  if(isPost)Button(onClick={isPost=true},enabled=!busy,modifier=Modifier.weight(1f)){Text(t("Post"))} else OutlinedButton(onClick={isPost=true},enabled=!busy,modifier=Modifier.weight(1f)){Text(t("Post"))}
 }
 Row(Modifier.fillMaxWidth().padding(8.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)){
  OutlinedTextField(text,{text=it.take(if(isPost)2000 else 500)},placeholder={Text(t(if(isPost)"Write a post" else "Message"))},modifier=Modifier.weight(1f),minLines=if(isPost)3 else 1,maxLines=5)
  Button(onClick={act(if(isPost)"post" else "chat",text)},enabled=!busy&&text.isNotBlank()&&text.length<=(if(isPost)2000 else 500)){Text(t(if(isPost)"Post" else "Send"))}
 }
 }
 if(failed)AlertDialog(onDismissRequest={failed=false},title={Text(t("Unable to save locally."))},confirmButton={TextButton(onClick={failed=false}){Text(t("OK"))}})
}
