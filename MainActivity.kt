package com.hisabpro.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

private val Green = Color(0xFF176B55)
private val Pale = Color(0xFFF5F7F6)
private val categories = listOf("किराना", "दूध", "पेट्रोल", "डीज़ल", "बिजली बिल", "पानी बिल", "फोन बिल", "किराया", "यात्रा", "स्वास्थ्य", "शिक्षा", "अन्य")
private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
private fun today() = dateFormat.format(Date())
private data class Expense(val id: String, val title: String, val amount: Double, val category: String, val date: String, val note: String)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { HisabProApp() }
    }
}

private class ExpenseStore(context: Context) {
    private val prefs = context.getSharedPreferences("hisab_pro_data", Context.MODE_PRIVATE)
    fun load(): List<Expense> {
        return try {
            val array = JSONArray(prefs.getString("expenses", "[]"))
            (0 until array.length()).map { i ->
                val o = array.getJSONObject(i)
                Expense(o.getString("id"), o.getString("title"), o.getDouble("amount"), o.getString("category"), o.getString("date"), o.optString("note", ""))
            }.sortedByDescending { it.date }
        } catch (_: Exception) { emptyList() }
    }
    fun save(expenses: List<Expense>) {
        val array = JSONArray()
        expenses.forEach { e -> array.put(JSONObject().put("id", e.id).put("title", e.title).put("amount", e.amount).put("category", e.category).put("date", e.date).put("note", e.note)) }
        prefs.edit().putString("expenses", array.toString()).apply()
    }
}

@Composable
private fun HisabProApp() {
    val context = LocalContext.current
    val store = remember { ExpenseStore(context) }
    var expenses by remember { mutableStateOf(store.load()) }
    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(categories.first()) }
    var date by remember { mutableStateOf(today()) }
    var note by remember { mutableStateOf("") }
    var menuOpen by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    val currentMonth = today().take(7)
    val todayTotal = expenses.filter { it.date == today() }.sumOf { it.amount }
    val monthTotal = expenses.filter { it.date.startsWith(currentMonth) }.sumOf { it.amount }

    MaterialTheme(colorScheme = lightColorScheme(primary = Green, background = Pale, surface = Color.White)) {
        Scaffold(containerColor = Pale) { padding ->
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(14.dp), contentPadding = PaddingValues(top = 18.dp, bottom = 28.dp)) {
                item {
                    Column {
                        Text("Hisab Pro", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Green)
                        Text("आपके हर खर्च का आसान हिसाब", color = Color.DarkGray, fontSize = 14.sp)
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        SummaryCard("आज का खर्च", todayTotal, Modifier.weight(1f))
                        SummaryCard("इस महीने", monthTotal, Modifier.weight(1f))
                    }
                }
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("नया खर्च जोड़ें", fontSize = 19.sp, fontWeight = FontWeight.Bold)
                            OutlinedTextField(value = title, onValueChange = { title = it; error = "" }, label = { Text("खर्च का नाम (जैसे दूध)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = amountText, onValueChange = { amountText = it.filter { c -> c.isDigit() || c == '.' }; error = "" }, label = { Text("रकम (₹)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, modifier = Modifier.fillMaxWidth())
                            Box {
                                OutlinedButton(onClick = { menuOpen = true }, modifier = Modifier.fillMaxWidth()) { Text("श्रेणी: $category"); Spacer(Modifier.weight(1f)); Text("⌄") }
                                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                                    categories.forEach { item -> DropdownMenuItem(text = { Text(item) }, onClick = { category = item; menuOpen = false }) }
                                }
                            }
                            OutlinedTextField(value = date, onValueChange = { date = it.filter { c -> c.isDigit() || c == '-' }.take(10); error = "" }, label = { Text("तारीख (YYYY-MM-DD)") }, supportingText = { Text("उदाहरण: 2026-10-28") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("नोट (वैकल्पिक)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                            if (error.isNotBlank()) Text(error, color = MaterialTheme.colorScheme.error)
                            Button(onClick = {
                                val amount = amountText.toDoubleOrNull()
                                if (title.isBlank()) error = "कृपया खर्च का नाम लिखें।"
                                else if (amount == null || amount <= 0.0) error = "कृपया सही रकम लिखें।"
                                else if (!date.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) error = "तारीख YYYY-MM-DD में लिखें।"
                                else {
                                    val newList = listOf(Expense(UUID.randomUUID().toString(), title.trim(), amount, category, date, note.trim())) + expenses
                                    expenses = newList.sortedByDescending { it.date }; store.save(expenses)
                                    title = ""; amountText = ""; note = ""; date = today(); category = categories.first(); error = ""
                                }
                            }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) { Text("+ खर्च सेव करें", modifier = Modifier.padding(vertical = 4.dp)) }
                        }
                    }
                }
                item {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text("खर्चों का रिकॉर्ड", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text("${expenses.size} रिकॉर्ड", color = Color.Gray, fontSize = 12.sp)
                    }
                }
                if (expenses.isEmpty()) {
                    item { Card(shape = RoundedCornerShape(16.dp)) { Text("अभी कोई खर्च नहीं है। ऊपर से पहला खर्च जोड़ें।", modifier = Modifier.padding(20.dp), color = Color.Gray) } }
                } else {
                    items(expenses, key = { it.id }) { expense ->
                        Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                            Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Text(expense.title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                                    Text("${expense.category} • ${expense.date}", color = Color.Gray, fontSize = 12.sp)
                                    if (expense.note.isNotBlank()) Text(expense.note, color = Color.DarkGray, fontSize = 13.sp)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("₹${money(expense.amount)}", color = Green, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    TextButton(onClick = { expenses = expenses.filterNot { it.id == expense.id }; store.save(expenses) }, contentPadding = PaddingValues(horizontal = 4.dp)) { Text("हटाएँ", color = Color(0xFFB3261E), fontSize = 12.sp) }
                                }
                            }
                        }
                    }
                }
                item { Text("डेटा इसी फोन में सेव होता है। ऐप ऑफलाइन काम करता है। ऐप हटाने पर डेटा मिट सकता है; बैकअप सुविधा अभी शामिल नहीं है।", color = Color.Gray, fontSize = 12.sp) }
            }
        }
    }
}

@Composable
private fun SummaryCard(label: String, amount: Double, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Green)) {
        Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(label, color = Color.White.copy(alpha = .88f), fontSize = 13.sp)
            Text("₹${money(amount)}", color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Bold)
        }
    }
}

private fun money(value: Double): String = if (value % 1.0 == 0.0) String.format(Locale.US, "%.0f", value) else String.format(Locale.US, "%.2f", value)
