package com.schoolday.app
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
object GroupBrand{
 val images=mapOf("school" to R.drawable.group_sk,"class" to R.drawable.group_sk,"staff" to R.drawable.group_sk,"admin" to R.drawable.group_sk,"secondary" to R.drawable.group_smk,"university" to R.drawable.group_um,"uni-course" to R.drawable.group_um)
 val names=mapOf("school" to "SK Cyberjaya","secondary" to "SMK Cyberjaya","university" to "Universiti Malaya")
 fun title(id:String,fallback:String)=names[id]?:fallback
}
@Composable fun GroupAvatar(channel:String,title:String,size:Dp=44.dp){
 val logo=GroupBrand.images[channel]
 Box(Modifier.size(size).clip(RoundedCornerShape(9.dp)).background(if(logo!=null)Color.White else Color(0xFFF0F0F0)),contentAlignment=Alignment.Center){
  if(logo!=null)Image(painterResource(logo),contentDescription=null,modifier=Modifier.fillMaxSize().padding(2.dp),contentScale=ContentScale.Fit)
  else Text(title.take(1).uppercase(),fontSize=16.sp,fontWeight=FontWeight.Bold)
 }
}
