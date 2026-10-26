package org.minsithu.stillmind

import android.media.MediaPlayer
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.LinearGradient
import android.graphics.Shader
import androidx.core.content.FileProvider
import java.io.File
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.TextStyle
import androidx.compose.material3.LocalTextStyle
import kotlinx.coroutines.delay
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.graphicsLayer
import android.provider.MediaStore
import android.content.ContentValues
import android.os.Handler
import android.os.Looper
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import java.time.LocalDate
import java.util.Calendar
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Android emulator: 10.0.2.2 maps to your development machine.
// On a physical Android phone, replace with your computer's LAN IP and use the same Wi-Fi.
private val API_BASE get() = BuildConfig.API_BASE_URL.trimEnd('/')
data class Meditation(val id: Int, val title: String, val category: String, val minutes: Int, val description: String, val audioUrl: String)
class MainActivity : ComponentActivity() {
    private val notificationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) ReminderScheduler.schedule(this)
    }
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState)
        setContent { MaterialTheme { CompositionLocalProvider(LocalTextStyle provides TextStyle(fontFamily = FontFamily(Font(R.font.pyidaungsu_regular)))) { MeditationHome(onPlay = { session, minutes, fade -> startMeditation(session, minutes, fade) }, onControl = { action ->
            if (getSharedPreferences(MeditationService.PREFS, MODE_PRIVATE).getBoolean("session_running", false)) {
                startService(Intent(this, MeditationService::class.java).setAction(action))
            }
        }, onShare = { minutes, mood, showName, showStreak, showStars, name, streak, stars, save, language, format, theme -> shareCard(minutes, mood, showName, showStreak, showStars, name, streak, stars, save, language, format, theme) }, onReminder = {
            if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            else ReminderScheduler.schedule(this)
        }) } } }
    }
    private fun startMeditation(s:Meditation, minutes:Int, fade:Boolean){
        val intent=Intent(this,MeditationService::class.java).setAction(MeditationService.ACTION_START).putExtra("audio","$API_BASE${s.audioUrl}").putExtra("minutes",minutes).putExtra("fade",fade)
        androidx.core.content.ContextCompat.startForegroundService(this,intent)
    }
    private fun shareCard(minutes:Int,mood:String,showName:Boolean,showStreak:Boolean,showStars:Boolean,name:String,streak:Int,stars:Int,save:Boolean,language:String,format:String,theme:String){
        val bmp=try { MindShareRenderer.render(this,minutes,mood,name,streak,stars,
            showName,showStreak,showStars,language,format,theme) } catch(e:Exception){
            android.widget.Toast.makeText(this,"Could not create card: ${e.message}",android.widget.Toast.LENGTH_LONG).show()
            return
        }
        val dir=File(cacheDir,"shared").apply{mkdirs()};val file=File(dir,"mind-progress-$format.png");file.outputStream().use{bmp.compress(Bitmap.CompressFormat.PNG,100,it)}
        if (save) {
            try {
                if (Build.VERSION.SDK_INT >= 29) {
                    val values = ContentValues().apply {
                        put(MediaStore.Images.Media.DISPLAY_NAME, "seit-mind-$format-${System.currentTimeMillis()}.png")
                        put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                        put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/SeitMind")
                        put(MediaStore.Images.Media.IS_PENDING, 1)
                    }
                    val image = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                        ?: error("Could not create gallery image")
                    try {
                        contentResolver.openOutputStream(image)?.use { stream ->
                            check(bmp.compress(Bitmap.CompressFormat.PNG, 100, stream))
                        } ?: error("Could not open gallery image")
                        contentResolver.update(image, ContentValues().apply {
                            put(MediaStore.Images.Media.IS_PENDING, 0)
                        }, null, null)
                        android.widget.Toast.makeText(this, "Saved to Pictures/SeitMind", android.widget.Toast.LENGTH_LONG).show()
                    } catch (e: Exception) {
                        contentResolver.delete(image, null, null)
                        throw e
                    }
                } else {
                    // Android 8–9: app-specific directory, no broad storage permission required.
                    val dir = File(getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES), "SeitMind").apply { mkdirs() }
                    File(dir, "seit-mind-$format-${System.currentTimeMillis()}.png").outputStream().use {
                        check(bmp.compress(Bitmap.CompressFormat.PNG, 100, it))
                    }
                    android.widget.Toast.makeText(this, "Saved in app's Pictures/SeitMind folder", android.widget.Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                android.widget.Toast.makeText(this, "Save failed: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
            }
            return
        }
        val uri=FileProvider.getUriForFile(this,"${packageName}.fileprovider",file)
        startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply{type="image/png";putExtra(Intent.EXTRA_STREAM,uri);addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)},"Share meditation progress"))
    }

}
@Composable fun MeditationHome(onPlay: (Meditation, Int, Boolean) -> Unit, onControl:(String)->Unit, onShare:(Int,String,Boolean,Boolean,Boolean,String,Int,Int,Boolean,String,String,String)->Unit, onReminder: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("stillmind_profile", Context.MODE_PRIVATE) }
    var name by remember { mutableStateOf(prefs.getString("name", "Meditator") ?: "Meditator") }
    var stars by remember { mutableIntStateOf(prefs.getInt("stars", 0)) }
    var streak by remember { mutableIntStateOf(prefs.getInt("streak", 0)) }
    var lastCheckin by remember { mutableStateOf(prefs.getString("last_checkin", "") ?: "") }
    var completedMinutes by remember { mutableIntStateOf(0) }
    var earned by remember { mutableIntStateOf(0) }
    var running by remember { mutableStateOf(false) }
    var playing by remember { mutableStateOf(false) }
    var sessionError by remember { mutableStateOf("") }
    var elapsed by remember { mutableLongStateOf(0L) }
    var mood by remember { mutableStateOf("Calm & peaceful") }
    var showName by remember { mutableStateOf(prefs.getBoolean("show_name",false)) }
    var showStreak by remember { mutableStateOf(prefs.getBoolean("show_streak",false)) }
    var showStars by remember { mutableStateOf(prefs.getBoolean("show_stars",false)) }
    var quoteLanguage by remember { mutableStateOf("both") }
    var cardFormat by remember { mutableStateOf("square") }
    var cardTheme by remember { mutableStateOf("forest") }
    var developerExpanded by remember { mutableStateOf(true) }
    var screen by remember { mutableStateOf("Home") }
    var selectedSession by remember { mutableStateOf<Meditation?>(null) }
    var fadeOut by remember { mutableStateOf(true) }
    var breathing by remember { mutableStateOf(false) }
    LaunchedEffect(Unit){while(true){running=prefs.getBoolean("session_running",false);playing=prefs.getBoolean("session_playing",false);sessionError=prefs.getString("session_error","")?:"";elapsed=prefs.getLong("session_elapsed",0L);stars=prefs.getInt("stars",0);streak=prefs.getInt("streak",0);lastCheckin=prefs.getString("last_checkin","")?:"";if(prefs.getBoolean("session_completed",false)){completedMinutes=prefs.getInt("completed_minutes",0);earned=prefs.getInt("completed_earned",0);prefs.edit().putBoolean("session_completed",false).apply();screen="Complete"};delay(750)}}
    var reminder by remember { mutableStateOf(prefs.getString("reminder", "20:00") ?: "20:00") }
    var avatarPath by remember { mutableStateOf(prefs.getString("avatar", "") ?: "") }
    val imagePicker = androidx.activity.compose.rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) { try {
            val bytes = context.contentResolver.openInputStream(uri)?.use { input ->
                val out = java.io.ByteArrayOutputStream()
                val chunk = ByteArray(8192)
                while (out.size() <= 2_000_000) {
                    val read = input.read(chunk)
                    if (read == -1) break
                    out.write(chunk, 0, read)
                }
                out.toByteArray()
            }
            if (bytes != null && bytes.size <= 2_000_000) {
                context.openFileOutput("profile_photo", Context.MODE_PRIVATE).use { it.write(bytes) }
                avatarPath = "profile_photo"; prefs.edit().putString("avatar", avatarPath).apply()
            }
        } catch (_: Exception) {} }
    }
    fun saveReminder(value: String) { reminder=value; prefs.edit().putString("reminder", value).apply() }

    var meditationMinutes by remember { mutableIntStateOf(10) }
    var customMinutes by remember { mutableStateOf("15") }
    var sessions by remember { mutableStateOf(listOf<Meditation>()) }
    var selectedCategory by remember { mutableStateOf("All") }
    var message by remember { mutableStateOf("Loading sessions...") }
    LaunchedEffect(Unit) { withContext(Dispatchers.IO) {
        try { val connection = URL("$API_BASE/api/sessions").openConnection() as HttpURLConnection
            connection.connectTimeout = 5000; connection.readTimeout = 5000
            try { val data = JSONArray(connection.inputStream.bufferedReader().use { it.readText() })
                val loaded = (0 until data.length()).map { i -> val o = data.getJSONObject(i); Meditation(o.getInt("id"), o.getString("title"), o.getString("category"), o.getInt("duration_minutes"), o.getString("description"), o.getString("audio_url")) }
                withContext(Dispatchers.Main) { sessions = loaded; message = "" }
            } finally { connection.disconnect() }
        } catch (e: Exception) { withContext(Dispatchers.Main) { message = "Backend unavailable. Start the API and check its address." } }
    } }
    val green = Color(0xFF244A39)
    Scaffold(
        topBar = {
            Row(Modifier.fillMaxWidth().background(Color(0xFFFAFAF7)).padding(horizontal=20.dp, vertical=12.dp),
                horizontalArrangement=Arrangement.SpaceBetween, verticalAlignment=Alignment.CenterVertically) {
                Text("စိတ် - Mind",fontSize=22.sp,color=green,fontWeight=FontWeight.Bold)
                TextButton(onClick={ screen="Settings" }) { Text("⚙ Settings",color=green) }
            }
        },
        bottomBar = {
            NavigationBar(containerColor=Color.White) {
                listOf("Home","Explore","Progress","Profile").forEach { tab ->
                    NavigationBarItem(selected=screen==tab,onClick={screen=tab},
                        icon={ Text(when(tab){"Home"->"⌂";"Explore"->"◈";"Progress"->"◎";else->"◉"},fontSize=20.sp) },
                        label={Text(tab,fontSize=11.sp)})
                }
            }
        }
    ) { innerPadding ->
      LazyColumn(Modifier.fillMaxSize().background(Color(0xFFF9F9F5)).padding(innerPadding).padding(horizontal=18.dp,vertical=18.dp),
          verticalArrangement=Arrangement.spacedBy(18.dp)) {
        if(screen=="Home") {
        item { Text("◉ စိတ် - Mind", fontSize = 27.sp, color = green); Spacer(Modifier.height(16.dp))
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFE4ECDF)), shape = RoundedCornerShape(22.dp)) {
                Column(Modifier.fillMaxWidth().padding(28.dp)) { Text("A LITTLE SPACE FOR YOURSELF", fontSize = 11.sp, color = green)
                    Spacer(Modifier.height(16.dp)); Text("Find your calm.", fontSize = 34.sp, color = green)
                    Text("One breath at a time.", fontSize = 22.sp, color = green)
                    Spacer(Modifier.height(12.dp)); Text("Mindful moments for focus and rest.", color = green) }
            }; Spacer(Modifier.height(22.dp)); Text("Explore meditations", fontSize = 25.sp, color = green) }
          item {
            Card(colors=CardDefaults.cardColors(containerColor=Color.White),shape=RoundedCornerShape(18.dp)) {
                Column(Modifier.fillMaxWidth().padding(23.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
                    Text("Welcome, $name",fontSize=22.sp,color=green)
                    Text("⭐ $stars stars   ·   🔥 $streak day streak",color=green)
                    Text("Every mindful minute matters.",color=Color(0xFF728879))
                    Button(onClick={screen="Explore"}) { Text("Explore meditations →") }
                }
            }
          }
          item { Card(colors=CardDefaults.cardColors(containerColor=Color(0xFFE6EFE3))) {
              Column(Modifier.fillMaxWidth().padding(24.dp)) {
                Text("QUOTE OF THE DAY",fontSize=12.sp,color=green)
                Spacer(Modifier.height(12.dp))
                Text(QuoteRepository.daily(context).en,color=green,fontSize=18.sp)
                Spacer(Modifier.height(10.dp))
                Text(QuoteRepository.daily(context).mm,color=green)
              }
          } }
        }
        if(screen=="Explore") {
          item { Text("Explore meditations",fontSize=29.sp,color=green,fontWeight=FontWeight.Bold)
                 Text("Choose a session to open its player.",color=Color(0xFF728879)) }
        item { Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf("All", "Mindfulness", "Focus", "Sleep", "Nature").forEach { category ->
            FilterChip(selected = selectedCategory == category, onClick = { selectedCategory = category }, label = { Text(category, fontSize = 11.sp) })
        } } }
        if (message.isNotBlank()) item { Text(message, color = green) }
        items(sessions.filter { selectedCategory == "All" || it.category == selectedCategory }) { s ->
            Card(onClick = { completedMinutes=0; selectedSession=s; screen="Player" }, shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFDDE8D4))) {
                Column(Modifier.fillMaxWidth().padding(24.dp)) { Text("✦   ${s.category} · ${s.minutes} min", color = green)
                    Spacer(Modifier.height(18.dp)); Text(s.title, fontSize = 24.sp, color = green)
                    Text(s.description, color = green); Spacer(Modifier.height(18.dp)); Text("Choose session →", color = green) }
            }
        }
        }
        if(screen=="Player") {
          item { TextButton(onClick={screen="Explore"}){Text("← Explore")}
            Text(selectedSession?.title?:"Choose a meditation",fontSize=29.sp,color=green,fontWeight=FontWeight.Bold)
            Text(selectedSession?.description?:"Select a track from Explore",color=Color(0xFF728879)) }
        item { Text("Meditation duration", fontSize = 20.sp, color = green)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf(10,20,30).forEach { m -> FilterChip(selected=meditationMinutes==m,onClick={meditationMinutes=m},label={Text("$m min")}) } }
            OutlinedTextField(value=customMinutes,onValueChange={customMinutes=it.filter { c -> c.isDigit() }.take(3)},label={Text("Custom minutes (1–180)")},singleLine=true,keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number),modifier=Modifier.fillMaxWidth())
            OutlinedButton(onClick={ customMinutes.toIntOrNull()?.takeIf{it in 1..180}?.let{meditationMinutes=it} },enabled=(customMinutes.toIntOrNull() ?: 0) in 1..180) { Text("Use custom duration") }
            Text("Selected: $meditationMinutes minutes · Audio loops until timer ends",fontSize=12.sp)
        }
        item { Text("Meditation controls",fontSize=22.sp,color=green)
            Button(onClick={ selectedSession?.let { onPlay(it,meditationMinutes,fadeOut) } },enabled=selectedSession!=null&&!running){ Text("▶ Start $meditationMinutes-minute meditation") }
            Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) { Checkbox(checked=fadeOut,onCheckedChange={fadeOut=it});Text("30-second sleep timer fade-out") }
            Text("${if (playing) "Playing" else if (running) "Paused / loading" else "Not playing"} · Listened: ${elapsed/60000} minutes")
            if (sessionError.isNotBlank()) Text(sessionError, color=Color(0xFFAA3333))
            Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick={onControl(MeditationService.ACTION_PAUSE)}){Text("Pause")}
                OutlinedButton(onClick={onControl(MeditationService.ACTION_RESUME)}){Text("Resume")}
                OutlinedButton(onClick={onControl(MeditationService.ACTION_FINISH)}){Text("Finish")}
                OutlinedButton(onClick={onControl(MeditationService.ACTION_STOP)}){Text("Discard")}
            }
            Text("Guided breathing · 4 seconds in / 4 seconds out")
            OutlinedButton(onClick={breathing=!breathing}){Text(if(breathing)"Stop breathing" else "Start breathing")}
            if(breathing){val transition=rememberInfiniteTransition(label="breath");val scale by transition.animateFloat(initialValue=.8f,targetValue=1.2f,animationSpec=infiniteRepeatable(tween(4000,easing=FastOutSlowInEasing),RepeatMode.Reverse),label="breathing scale");Box(Modifier.fillMaxWidth().height(170.dp),contentAlignment=Alignment.Center){Box(Modifier.size(120.dp).graphicsLayer(scaleX=scale,scaleY=scale).background(Color(0xFF8EBAA2),RoundedCornerShape(80.dp)),contentAlignment=Alignment.Center){Text("Breathe",color=Color.White)}}}
        }
        }
        if(screen=="Progress") {
          item { Text("My progress",fontSize=30.sp,color=green,fontWeight=FontWeight.Bold)
            Text("A missed day is a fresh start, never a failure.",color=Color(0xFF728879)) }
          item { Card(colors=CardDefaults.cardColors(containerColor=Color.White)) {
            Column(Modifier.fillMaxWidth().padding(26.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
              Text("⭐ $stars total stars",fontSize=24.sp,color=green)
              Text("🔥 $streak day streak",fontSize=22.sp,color=green)
              Text("Rewards unlock at 80% of your chosen time. Maximum 60 stars per day.",color=Color(0xFF728879))
            }
          } }
          item { Card(colors=CardDefaults.cardColors(containerColor=Color(0xFFE6EFE3))) {
            Column(Modifier.fillMaxWidth().padding(24.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
              Text("Daily wisdom",fontSize=22.sp,color=green)
              Text(QuoteRepository.daily(context).en,color=green)
              Text(QuoteRepository.daily(context).mm,color=green)
            }
          } }
        }
        if(screen=="Profile") {
          item { Text("My profile",fontSize=30.sp,color=green,fontWeight=FontWeight.Bold) }
          item { Card(colors=CardDefaults.cardColors(containerColor=Color.White),shape=RoundedCornerShape(20.dp)) {
            Column(Modifier.fillMaxWidth().padding(25.dp),verticalArrangement=Arrangement.spacedBy(15.dp)) {
              val avatarBitmap=remember(avatarPath){if(avatarPath.isNotEmpty())try{context.openFileInput(avatarPath).use{BitmapFactory.decodeStream(it)}}catch(_:Exception){null}else null}
              if(avatarBitmap!=null) Image(avatarBitmap.asImageBitmap(),"Your profile photo",Modifier.size(88.dp).clip(RoundedCornerShape(44.dp)),contentScale=ContentScale.Crop)
              else Box(Modifier.size(88.dp).background(Color(0xFFDDE8D4),RoundedCornerShape(44.dp)),contentAlignment=Alignment.Center){Text("◉",fontSize=40.sp,color=green)}
              Text(name,fontSize=27.sp,color=green,fontWeight=FontWeight.Bold)
              Text("⭐ $stars stars · 🔥 $streak day streak",color=green)
              Button(onClick={screen="Settings"}){Text("Edit profile & settings →")}
            }
          } }
          item { OutlinedButton(onClick={screen="Progress"},modifier=Modifier.fillMaxWidth()){Text("My progress →")} }
          item { OutlinedButton(onClick={screen="About"},modifier=Modifier.fillMaxWidth()){Text("About & developer →")} }
        }
        if(screen=="Settings") {
          item { TextButton(onClick={screen="Profile"}){Text("← Profile")}
            Text("App settings",fontSize=30.sp,color=green,fontWeight=FontWeight.Bold)
            Text("Personalize your calm.",color=Color(0xFF728879)) }
          item { Card(colors=CardDefaults.cardColors(containerColor=Color.White),shape=RoundedCornerShape(18.dp)) {
            Column(Modifier.fillMaxWidth().padding(22.dp),verticalArrangement=Arrangement.spacedBy(14.dp)) {
              Text("◉ Profile",fontSize=21.sp,color=green,fontWeight=FontWeight.Bold)
              val photo=remember(avatarPath) { if(avatarPath.isNotEmpty()) try { context.openFileInput(avatarPath).use { BitmapFactory.decodeStream(it) } } catch (_:Exception) { null } else null }
              if(photo!=null) Image(photo.asImageBitmap(),"Your profile photo",Modifier.size(72.dp).clip(RoundedCornerShape(40.dp)),contentScale=ContentScale.Crop)
              else Box(Modifier.size(72.dp).background(Color(0xFFDDE8D4),RoundedCornerShape(40.dp)),contentAlignment=Alignment.Center){Text("◉",fontSize=32.sp,color=green)}
              OutlinedTextField(value=name,onValueChange={name=it.take(40);prefs.edit().putString("name",name).apply()},label={Text("Display name")},singleLine=true,modifier=Modifier.fillMaxWidth())
              OutlinedButton(onClick={imagePicker.launch("image/*")}){Text("Change profile photo")}
              if(avatarPath.isNotEmpty()) TextButton(onClick={context.deleteFile("profile_photo");avatarPath="";prefs.edit().remove("avatar").apply()}){Text("Remove photo")}
            }
          } }
          item { Card(colors=CardDefaults.cardColors(containerColor=Color.White),shape=RoundedCornerShape(18.dp)) {
            Column(Modifier.fillMaxWidth().padding(22.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
              Text("◷ Daily reminder",fontSize=21.sp,color=green,fontWeight=FontWeight.Bold)
              Text("A gentle notification, not an alarm clock.",color=Color(0xFF728879))
              OutlinedTextField(value=reminder,onValueChange={saveReminder(it.take(5))},label={Text("Time (HH:mm)")},singleLine=true,modifier=Modifier.fillMaxWidth())
              Button(onClick={if(Regex("^([01]\\d|2[0-3]):[0-5]\\d$").matches(reminder))onReminder()},enabled=Regex("^([01]\\d|2[0-3]):[0-5]\\d$").matches(reminder)){Text("Schedule daily reminder")}
              OutlinedButton(onClick={ReminderScheduler.cancel(context)}){Text("Disable reminder")}
            }
          } }
          item { Card(colors=CardDefaults.cardColors(containerColor=Color.White),shape=RoundedCornerShape(18.dp)) {
            Column(Modifier.fillMaxWidth().padding(22.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
              Text("▧ Share card privacy",fontSize=21.sp,color=green,fontWeight=FontWeight.Bold)
              Text("Choose which personal details appear on cards.",color=Color(0xFF728879))
              Row(verticalAlignment=Alignment.CenterVertically){Checkbox(checked=showName,onCheckedChange={showName=it;prefs.edit().putBoolean("show_name",it).apply()});Text("Show my name")}
              Row(verticalAlignment=Alignment.CenterVertically){Checkbox(checked=showStreak,onCheckedChange={showStreak=it;prefs.edit().putBoolean("show_streak",it).apply()});Text("Show my streak")}
              Row(verticalAlignment=Alignment.CenterVertically){Checkbox(checked=showStars,onCheckedChange={showStars=it;prefs.edit().putBoolean("show_stars",it).apply()});Text("Show my stars")}
              Text("Your photo never appears on shared cards.",color=Color(0xFF728879),fontSize=12.sp)
            }
          } }
          item { OutlinedButton(onClick={screen="About"},modifier=Modifier.fillMaxWidth()){Text("About & developer →")} }
        }
        if(screen=="Complete") {
          item { Text("Meditation complete",fontSize=29.sp,color=green,fontWeight=FontWeight.Bold)
            Text("You made time for yourself today.",color=Color(0xFF728879)) }
        if(completedMinutes>0) item {
            Card(colors=CardDefaults.cardColors(containerColor=Color(0xFFE2EEDD))) { Column(Modifier.padding(20.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
                Text("✦ Meditation completed",fontSize=24.sp,color=green)
                Text("$completedMinutes minutes · $streak day streak · +$earned stars",color=green)
                Text(QuoteRepository.daily(context).en,color=green)
                Text(QuoteRepository.daily(context).mm,color=green)
                Text("How do you feel?")
                listOf("Calm & peaceful","Positive & grateful","Focused & clear","Refreshed").forEach { value -> FilterChip(selected=mood==value,onClick={mood=value},label={Text(value)}) }
                Text("Your mood quote",color=green)
                val selectedQuote = remember(mood) { QuoteRepository.forMood(context,mood) }
                if (quoteLanguage!="mm") Text(selectedQuote.en,color=green)
                if (quoteLanguage!="en") Text(selectedQuote.mm,color=green)
                Text("Quote language",fontSize=13.sp,color=green)
                Row(horizontalArrangement=Arrangement.spacedBy(6.dp)) { listOf("en","mm","both").forEach{ lang->FilterChip(selected=quoteLanguage==lang,onClick={quoteLanguage=lang},label={Text(if(lang=="mm")"မြန်မာ" else if(lang=="en")"English" else "Both")}) } }
                Text("PNG layout",fontSize=13.sp,color=green)
                Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) { listOf("square","story").forEach{fmt->FilterChip(selected=cardFormat==fmt,onClick={cardFormat=fmt},label={Text(if(fmt=="square")"1080×1080" else "1080×1920")})} }
                Text("Card theme",fontSize=13.sp,color=green)
                Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(6.dp)) { listOf("forest","sunset","ocean","cream").forEach{t->FilterChip(selected=cardTheme==t,onClick={cardTheme=t},label={Text(t)})} }
                Button(onClick={onShare(completedMinutes,mood,showName,showStreak,showStars,name,streak,stars,false,quoteLanguage,cardFormat,cardTheme)}){Text("Share PNG")}
                OutlinedButton(onClick={onShare(completedMinutes,mood,showName,showStreak,showStars,name,streak,stars,true,quoteLanguage,cardFormat,cardTheme)}){Text("Save PNG to Pictures")}
                Text("Missing a day is okay. Begin again whenever you're ready.",fontSize=12.sp)
            } }
        }
          item { Button(onClick={completedMinutes=0;screen="Home"},modifier=Modifier.fillMaxWidth()){Text("Back to Home →")} }
        }
        if(screen=="About") {
          item { TextButton(onClick={screen="Settings"}){Text("← Settings")}
                 Text("Meet the Developer",fontSize=29.sp,color=green,fontWeight=FontWeight.Bold) }
        item { Column(verticalArrangement=Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick={ developerExpanded=!developerExpanded }) { Text(if(developerExpanded) "Hide developer information" else "About · Meet the developer") }
            if(developerExpanded) {
                Row(horizontalArrangement=Arrangement.spacedBy(14.dp),verticalAlignment=Alignment.CenterVertically) {
                    Image(painterResource(R.drawable.developer_avatar),"Original developer photo",Modifier.size(75.dp).clip(RoundedCornerShape(38.dp)),contentScale=ContentScale.Crop)
                    Column { Text("Min SiThu",fontSize=20.sp,color=green)
                        Text("Independent App Developer · Creator of စိတ် - Mind",fontSize=12.sp,color=green)
                        Text("minsithu.org",fontSize=13.sp,color=green) }
                }
                Text("Building thoughtful digital experiences that make everyday life calmer and more meaningful.",fontSize=12.sp,color=green)
            }
        } }
        }
        if(running && screen!="Player" && screen!="Complete") {
          item { Card(onClick={screen="Player"},colors=CardDefaults.cardColors(containerColor=Color(0xFF244A39))) {
            Row(Modifier.fillMaxWidth().padding(17.dp),horizontalArrangement=Arrangement.SpaceBetween) {
              Text("♫ Meditation in progress",color=Color.White)
              Text("Open player →",color=Color.White)
            }
          } }
        }
      }
    }
}
