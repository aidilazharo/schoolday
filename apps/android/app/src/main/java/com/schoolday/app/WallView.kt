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
 val expanded=remember(channel){mutableStateMapOf<String,Boolean>()}
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
   Row(Modifier.fillMaxWidth(),horizontalArrangement=if(post.authorId==user.id)Arrangement.End else Arrangement.Start){
    Surface(shape=RoundedCornerShape(12.dp),color=MaterialTheme.colorScheme.surfaceVariant){
     Column(Modifier.widthIn(max=if(post.body.length>180||expanded[post.id]==true)600.dp else 420.dp).padding(12.dp),verticalArrangement=Arrangement.spacedBy(6.dp)){
      if(post.authorId!=user.id)Text(post.name,fontWeight=FontWeight.Bold,style=MaterialTheme.typography.labelMedium)
      Text(post.body)
      Text(java.text.DateFormat.getTimeInstance(java.text.DateFormat.SHORT).format(java.util.Date(post.createdAt))+" · "+t("Local only"),style=MaterialTheme.typography.labelSmall)
      Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){
       TextButton(onClick={act("like","",post.id)},enabled=!busy){Text((if(post.liked)"♥ " else "♡ ")+post.likes)}
       TextButton(onClick={expanded[post.id]=expanded[post.id]!=true}){Text(t("Comments")+" ("+post.comments.size+")")}
      }
      if(expanded[post.id]==true){
       post.comments.forEach{comment->Surface(shape=RoundedCornerShape(8.dp)){Column(Modifier.padding(8.dp)){Text(comment.name,fontWeight=FontWeight.Bold);Text(comment.body)}}}
       Row(verticalAlignment=Alignment.CenterVertically){OutlinedTextField(comments[post.id]?:"",{comments[post.id]=it.take(500)},placeholder={Text(t("Comment"))},modifier=Modifier.weight(1f));TextButton(onClick={act("comment",comments[post.id]?:"",post.id)},enabled=!busy&&!comments[post.id].isNullOrBlank()){Text(t("Send"))}}
      }
     }
    }
   }
   }
  }
 }
 HorizontalDivider()
 Row(Modifier.fillMaxWidth().padding(8.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)){
  OutlinedTextField(text,{text=it.take(2000)},placeholder={Text(t("Message"))},modifier=Modifier.weight(1f),minLines=1,maxLines=5)
  Button(onClick={act("post",text)},enabled=!busy&&text.isNotBlank()&&text.length<=2000){Text(t("Send"))}
 }
 }
 if(failed)AlertDialog(onDismissRequest={failed=false},title={Text(t("Unable to save locally."))},confirmButton={TextButton(onClick={failed=false}){Text(t("OK"))}})
}
