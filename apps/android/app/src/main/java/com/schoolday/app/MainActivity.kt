package com.schoolday.app

import android.os.Bundle
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class Draft(val id: String, val text: String)

private class DraftStore(context: Context) {
    private val prefs = context.getSharedPreferences("schoolday_development", Context.MODE_PRIVATE)
    fun load(): List<Draft> = runCatching {
        val array = JSONArray(prefs.getString("outbox", "[]"))
        List(array.length()) { i -> array.getJSONObject(i).let { Draft(it.getString("id"), it.getString("text")) } }
    }.getOrDefault(emptyList())
    fun save(drafts: List<Draft>): Boolean {
        val array = JSONArray()
        drafts.forEach { array.put(JSONObject().put("id", it.id).put("text", it.text)) }
        return prefs.edit().putString("outbox", array.toString()).commit()
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val store = DraftStore(this)
        setContent {
            MaterialTheme(colorScheme = lightColorScheme(primary = Color(0xFF171717), background = Color.White, surface = Color.White)) {
                Chats(store)
            }
        }
    }
}

@Composable
private fun Chats(store: DraftStore) {
    var drafts by remember { mutableStateOf(store.load()) }
    var text by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf(false) }
    Surface(modifier = Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize().safeDrawingPadding().imePadding(), contentAlignment = Alignment.TopCenter) {
            Column(Modifier.widthIn(max = 760.dp).fillMaxSize()) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("schoolday", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.weight(1f))
                    Text("Local only", style = MaterialTheme.typography.labelSmall)
                }
                HorizontalDivider()
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Chats", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = { confirm = true }, enabled = drafts.isNotEmpty()) { Text("Clear") }
                }
                if (drafts.isEmpty()) {
                    Box(Modifier.weight(1f).fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("No conversations yet", fontWeight = FontWeight.Bold)
                            Text("Save a local test message below.", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                } else {
                    LazyColumn(Modifier.weight(1f).fillMaxWidth(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(drafts, key = { it.id }) { draft ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                Surface(shape = RoundedCornerShape(14.dp), color = Color(0xFFF1F1F1)) {
                                    Column(Modifier.widthIn(max = 480.dp).padding(12.dp)) {
                                        Text(draft.text)
                                        Text("Saved locally · Not sent", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                    }
                }
                HorizontalDivider()
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = text, onValueChange = { text = it.take(500) }, modifier = Modifier.weight(1f), placeholder = { Text("Message") })
                    Button(enabled = text.isNotBlank(), onClick = {
                        val next = drafts + Draft(UUID.randomUUID().toString(), text.trim())
                        if (store.save(next)) { drafts = next; text = "" } else error = true
                    }) { Text("Save") }
                }
            }
        }
    }
    if (confirm) AlertDialog(onDismissRequest = { confirm = false }, title = { Text("Clear local drafts?") },
        confirmButton = { TextButton(onClick = { if (store.save(emptyList())) drafts = emptyList() else error = true; confirm = false }) { Text("Clear") } },
        dismissButton = { TextButton(onClick = { confirm = false }) { Text("Cancel") } })
    if (error) AlertDialog(onDismissRequest = { error = false }, title = { Text("Unable to save locally") },
        confirmButton = { TextButton(onClick = { error = false }) { Text("OK") } })
}
