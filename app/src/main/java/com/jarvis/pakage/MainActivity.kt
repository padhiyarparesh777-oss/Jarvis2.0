package com.jarvis.pakage

import android.content.Context
import androidx.compose.ui.layout.ContentScale
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.Image as ComposeImage
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.UUID
import java.util.concurrent.TimeUnit
import android.content.Intent
import android.net.Uri
import android.os.BatteryManager
import android.provider.ContactsContract
import android.provider.CalendarContract
import android.provider.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.platform.LocalContext
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jarvis.pakage.ui.theme.JarvisTheme
import kotlinx.coroutines.launch

private const val PREFS_NAME = "jarvis_preferences"
private const val THEME_MODE_KEY = "theme_mode"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val preferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val initialTheme = preferences.getString(THEME_MODE_KEY, "system") ?: "system"

        setContent {
            var themeMode by rememberSaveable { mutableStateOf(initialTheme) }
            val darkTheme = when (themeMode) {
                "light" -> false
                "dark" -> true
                else -> androidx.compose.foundation.isSystemInDarkTheme()
            }

            JarvisTheme(darkTheme = darkTheme) {
                JarvisApp(
                    themeMode = themeMode,
                    onThemeChange = { newMode ->
                        themeMode = newMode
                        preferences.edit().putString(THEME_MODE_KEY, newMode).apply()
                    }
                )
            }
        }
    }
}

private data class QuickAction(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val destination: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun JarvisApp(
    themeMode: String,
    onThemeChange: (String) -> Unit
) {
    var message by rememberSaveable { mutableStateOf("") }
    var selectedTab by rememberSaveable { mutableStateOf(0) }
    val chatMessages = remember { mutableStateListOf<ChatMessage>() }
    var isLoading by rememberSaveable { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val actions = listOf(
        QuickAction("Phone Manager", "Calls, contacts & SMS", Icons.Default.Phone, 1),
        QuickAction("Email Manager", "Gmail & messages", Icons.Default.Email, 1),
        QuickAction("Reminders", "Tasks & calendar", Icons.Default.CalendarMonth, 1),
        QuickAction("Video Studio", "Create daily reels", Icons.Default.Movie, 2)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(40.dp),
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.SmartToy,
                                    contentDescription = "JARVIS",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("JARVIS 2.0", fontWeight = FontWeight.Bold)
                            Text(
                                "Your Personal AI Assistant",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { selectedTab = 3 }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar {
                val items = listOf(
                    "Chat" to Icons.Default.Message,
                    "Tools" to Icons.Default.GridView,
                    "Videos" to Icons.Default.VideoLibrary,
                    "Settings" to Icons.Default.Settings
                )
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { Icon(item.second, contentDescription = item.first) },
                        label = { Text(item.first) }
                    )
                }
            }
        }
    ) { innerPadding ->
        when (selectedTab) {
            0 -> HomeScreen(
                modifier = Modifier.padding(innerPadding),
                actions = actions,
                message = message,
                onMessageChange = { message = it },
                onSend = {
                    val prompt = message.trim()
                    if (prompt.isNotEmpty() && !isLoading) {
                        chatMessages.add(ChatMessage(prompt, true))
                        message = ""
                        isLoading = true
                        coroutineScope.launch {
                            try {
                                chatMessages.add(ChatMessage(AiRepository.ask(prompt), false))
                            } catch (error: Exception) {
                                chatMessages.add(ChatMessage(error.message ?: "AI request failed.", false))
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                },
                onActionClick = { selectedTab = it },
                chatMessages = chatMessages,
                isLoading = isLoading
            )
            1 -> ToolsScreen(Modifier.padding(innerPadding))
            2 -> VideoStudioScreen(Modifier.padding(innerPadding))
            else -> SettingsScreen(
                modifier = Modifier.padding(innerPadding),
                themeMode = themeMode,
                onThemeChange = onThemeChange
            )
        }
    }
}

@Composable
private fun HomeScreen(
    modifier: Modifier = Modifier,
    actions: List<QuickAction>,
    message: String,
    onMessageChange: (String) -> Unit,
    onSend: () -> Unit,
    onActionClick: (Int) -> Unit,
    chatMessages: List<ChatMessage>,
    isLoading: Boolean
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(Modifier.height(2.dp)) }
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(vertical = 26.dp, horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        modifier = Modifier.size(76.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.SmartToy,
                                contentDescription = "AI assistant",
                                modifier = Modifier.size(38.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    Text("Hello Tittu!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(
                        "I'm ready to help you today.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(10.dp))
                    Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.secondaryContainer) {
                        Text(
                            "●  Gemini 3.8 Flash • Groq GPT-OSS fallback",
                            modifier = Modifier.padding(horizontal = 13.dp, vertical = 7.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }
        item {
            Text("Quick Actions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
        items(actions.chunked(2)) { rowActions ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                rowActions.forEach { action ->
                    Card(
                        modifier = Modifier.weight(1f).height(124.dp).clickable { onActionClick(action.destination) },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Icon(action.icon, contentDescription = action.title, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(27.dp))
                            Column {
                                Text(action.title, fontWeight = FontWeight.SemiBold)
                                Text(action.subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
                if (rowActions.size == 1) Spacer(Modifier.weight(1f))
            }
        }
        item {
            Text("Ask JARVIS anything", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            OutlinedTextField(
                value = message,
                onValueChange = onMessageChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Message JARVIS...") },
                trailingIcon = {
                    IconButton(onClick = onSend, enabled = message.isNotBlank()) {
                        Icon(Icons.Default.Send, contentDescription = "Send message")
                    }
                },
                shape = RoundedCornerShape(18.dp),
                singleLine = true
            )
            if (chatMessages.isNotEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    chatMessages.forEach { chat ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (chat.fromUser) Arrangement.End else Arrangement.Start
                        ) {
                            Surface(
                                color = if (chat.fromUser) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text(
                                    chat.text,
                                    modifier = Modifier.widthIn(max = 300.dp).padding(12.dp),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                    if (isLoading) {
                        Text("JARVIS is thinking…", style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }
        }
        item { Spacer(Modifier.height(12.dp)) }
    }
}

private data class InstalledApp(
    val label: String,
    val packageName: String
)

@Composable
private fun ToolsScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var search by rememberSaveable { mutableStateOf("") }
    var actionMessage by rememberSaveable { mutableStateOf("") }
    var reminderTitle by rememberSaveable { mutableStateOf("") }
    var reminderMessage by rememberSaveable { mutableStateOf("") }
    var reminderMinutes by rememberSaveable { mutableStateOf("10") }
    var reminderStatus by rememberSaveable { mutableStateOf("") }

    fun scheduleReminder() {
        val delayMinutes = reminderMinutes.toLongOrNull()
        if (reminderTitle.isBlank() || delayMinutes == null || delayMinutes < 1L) {
            reminderStatus = "Title aur 1 ya usse zyada minutes enter karo."
            return
        }
        val request = OneTimeWorkRequestBuilder<JarvisReminderWorker>()
            .setInputData(
                workDataOf(
                    JarvisReminderWorker.KEY_TITLE to reminderTitle.trim(),
                    JarvisReminderWorker.KEY_MESSAGE to reminderMessage.trim()
                )
            )
            .setInitialDelay(delayMinutes, TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            UUID.randomUUID().toString(),
            ExistingWorkPolicy.REPLACE,
            request
        )
        reminderStatus = "Reminder scheduled in $delayMinutes minutes."
        reminderTitle = ""
        reminderMessage = ""
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) scheduleReminder()
        else reminderStatus = "Notifications permission denied. Allow it in Android Settings."
    }

    val installedApps = remember(context) {
        val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        context.packageManager.queryIntentActivities(launcherIntent, 0)
            .mapNotNull { resolveInfo ->
                val activity = resolveInfo.activityInfo ?: return@mapNotNull null
                InstalledApp(
                    label = resolveInfo.loadLabel(context.packageManager).toString(),
                    packageName = activity.packageName
                )
            }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }
    }

    val batteryPercent = remember(context) {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }

    val filteredApps = installedApps.filter {
        it.label.contains(search, ignoreCase = true) ||
            it.packageName.contains(search, ignoreCase = true)
    }

    fun openIntent(intent: Intent, successMessage: String) {
        try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            actionMessage = successMessage
        } catch (_: Exception) {
            actionMessage = "Ye action is device par available nahi hai."
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Phone Manager", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(
                "JARVIS ke through phone ke basic actions control karo.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        item {
            Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Quick phone actions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = { openIntent(Intent(Intent.ACTION_DIAL), "Phone dialer opened.") }
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text("Dialer")
                        }
                        OutlinedButton(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                openIntent(
                                    Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI),
                                    "Choose a contact from your contacts."
                                )
                            }
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text("Contacts")
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                openIntent(
                                    Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:")),
                                    "SMS composer opened. Review before sending."
                                )
                            }
                        ) {
                            Icon(Icons.Default.Message, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text("New SMS")
                        }
                        OutlinedButton(
                            modifier = Modifier.weight(1f),
                            onClick = { openIntent(Intent(Settings.ACTION_SETTINGS), "Android Settings opened.") }
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text("Settings")
                        }
                    }
                    Text(
                        "Battery: ${if (batteryPercent in 0..100) "$batteryPercent%" else "Not available"}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (actionMessage.isNotBlank()) {
                        Text(actionMessage, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
        item {
            Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Google & Productivity", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text("Quick access to your Google productivity apps.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(modifier = Modifier.weight(1f), onClick = {
                            val gmail = context.packageManager.getLaunchIntentForPackage("com.google.android.gm")
                            if (gmail != null) openIntent(gmail, "Gmail opened.")
                            else openIntent(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:")), "Email app chooser opened.")
                        }) { Icon(Icons.Default.Email, contentDescription = null); Spacer(Modifier.width(6.dp)); Text("Gmail") }
                        OutlinedButton(modifier = Modifier.weight(1f), onClick = {
                            openIntent(Intent(Intent.ACTION_VIEW, Uri.parse("content://com.android.calendar/time/" + System.currentTimeMillis())), "Calendar opened.")
                        }) { Icon(Icons.Default.CalendarMonth, contentDescription = null); Spacer(Modifier.width(6.dp)); Text("Calendar") }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(modifier = Modifier.weight(1f), onClick = {
                            val drive = context.packageManager.getLaunchIntentForPackage("com.google.android.apps.docs")
                            if (drive != null) openIntent(drive, "Google Drive opened.") else actionMessage = "Google Drive app is not installed."
                        }) { Icon(Icons.Default.GridView, contentDescription = null); Spacer(Modifier.width(6.dp)); Text("Drive") }
                        OutlinedButton(modifier = Modifier.weight(1f), onClick = {
                            val photos = context.packageManager.getLaunchIntentForPackage("com.google.android.apps.photos")
                            if (photos != null) openIntent(photos, "Google Photos opened.") else actionMessage = "Google Photos app is not installed."
                        }) { Icon(Icons.Default.Image, contentDescription = null); Spacer(Modifier.width(6.dp)); Text("Photos") }
                    }
                    OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = {
                        val start = System.currentTimeMillis() + 60 * 60 * 1000
                        val createEvent = Intent(Intent.ACTION_INSERT).setData(CalendarContract.Events.CONTENT_URI)
                            .putExtra(CalendarContract.Events.TITLE, "JARVIS reminder")
                            .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, start)
                            .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, start + 30 * 60 * 1000)
                        openIntent(createEvent, "Calendar event form opened. Review and save it.")
                    }) { Icon(Icons.Default.CalendarMonth, contentDescription = null); Spacer(Modifier.width(6.dp)); Text("Create calendar event") }
                    Text("These shortcuts open Google apps or Android Calendar. Direct account sync and Gmail, Drive, Photos data access need Google OAuth consent and will be added separately.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    Text("JARVIS Automation & Reminders", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text("Schedule a notification reminder. It can run even when JARVIS is closed.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedTextField(
                        value = reminderTitle,
                        onValueChange = { reminderTitle = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Reminder title") }
                    )
                    OutlinedTextField(
                        value = reminderMessage,
                        onValueChange = { reminderMessage = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Reminder details (optional)") }
                    )
                    OutlinedTextField(
                        value = reminderMinutes,
                        onValueChange = { reminderMinutes = it.filter(Char::isDigit).take(5) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Remind me in how many minutes?") }
                    )
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            if (Build.VERSION.SDK_INT >= 33 &&
                                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                            ) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                scheduleReminder()
                            }
                        }
                    ) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Schedule reminder")
                    }
                    if (reminderStatus.isNotBlank()) {
                        Text(reminderStatus, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
                    }
                    Text("Reminders use Android WorkManager and notification permission. Delivery time can be delayed by battery optimization.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        item {
            Text("Installed apps", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Search apps...") },
                shape = RoundedCornerShape(16.dp)
            )
            Text(
                "${filteredApps.size} apps found • Tap an app to open",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        items(filteredApps, key = { it.packageName }) { app ->
            Card(
                modifier = Modifier.fillMaxWidth().clickable {
                    val launchIntent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                    if (launchIntent != null) openIntent(launchIntent, "Opened ${app.label}.")
                    else actionMessage = "Is app ko launch nahi kiya ja sakta."
                },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(38.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.GridView, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(app.label, fontWeight = FontWeight.SemiBold)
                        Text(app.packageName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(Icons.Default.Send, contentDescription = "Open ${app.label}", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
        if (filteredApps.isEmpty()) {
            item { Text("No matching apps found.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
        item {
            Text(
                "JARVIS opens the selected app or Android screen. Calls and messages are not sent automatically.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private data class ReelClip(
    val uri: Uri,
    val durationSeconds: Int = 5
)

@Composable
private fun VideoStudioScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val clips = remember { mutableStateListOf<ReelClip>() }
    var reelText by rememberSaveable { mutableStateOf("") }
    var durationText by rememberSaveable { mutableStateOf("5") }
    var aspectRatio by rememberSaveable { mutableStateOf("9:16") }
    var transition by rememberSaveable { mutableStateOf("Fade") }
    var projectName by rememberSaveable { mutableStateOf("My Reel") }
    var status by rememberSaveable { mutableStateOf("Add multiple photos to build your video timeline.") }

    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            uris.forEach { uri ->
                runCatching {
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                }
                if (clips.none { it.uri == uri }) clips.add(ReelClip(uri))
            }
            status = "${uris.size} photo(s) added to your timeline."
        }
    }

    val totalSeconds = clips.sumOf { it.durationSeconds }
    val previewBitmap = remember(clips.firstOrNull()?.uri) {
        clips.firstOrNull()?.uri?.let { uri ->
            runCatching {
                context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
            }.getOrNull()
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Advanced Video Studio", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Build a multi-photo reel with a timeline, captions and editing settings.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = projectName,
                        onValueChange = { projectName = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Project name") }
                    )
                    if (previewBitmap != null) {
                        ComposeImage(
                            bitmap = previewBitmap.asImageBitmap(),
                            contentDescription = "First timeline photo",
                            modifier = Modifier.fillMaxWidth().height(250.dp).clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Movie, contentDescription = null, modifier = Modifier.size(42.dp), tint = MaterialTheme.colorScheme.primary)
                                Text("9:16 video preview")
                            }
                        }
                    }
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { photoPicker.launch(arrayOf("image/*")) }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Add photos to timeline")
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("Canvas:", fontWeight = FontWeight.SemiBold)
                        listOf("9:16", "1:1", "16:9").forEach { ratio ->
                            OutlinedButton(onClick = { aspectRatio = ratio }) {
                                Text(if (aspectRatio == ratio) "✓ $ratio" else ratio)
                            }
                        }
                    }
                    OutlinedTextField(
                        value = reelText,
                        onValueChange = { reelText = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("On-screen text / caption") },
                        minLines = 2
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("Transition:")
                        listOf("Fade", "Cut", "Slide").forEach { item ->
                            OutlinedButton(onClick = { transition = item }) {
                                Text(if (transition == item) "✓ $item" else item)
                            }
                        }
                    }
                }
            }
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Timeline", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Text("${clips.size} clips • ${totalSeconds}s", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
            }
            Text("Move clips up/down to change their order. Each photo becomes one video segment.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (clips.isEmpty()) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Text("Your timeline is empty. Tap “Add photos to timeline” to begin.", modifier = Modifier.padding(18.dp))
                }
            }
        }
        items(clips.size, key = { clips[it].uri.toString() }) { index ->
            val clip = clips[index]
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Row(
                    Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        Modifier.size(56.dp).clip(RoundedCornerShape(10.dp)).background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("${index + 1}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Photo ${index + 1}", fontWeight = FontWeight.SemiBold)
                        Text("${clip.durationSeconds} seconds", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedButton(onClick = {
                                if (clip.durationSeconds > 1) clips[index] = clip.copy(durationSeconds = clip.durationSeconds - 1)
                            }) { Text("−") }
                            Text("  ${clip.durationSeconds}s  ")
                            OutlinedButton(onClick = {
                                if (clip.durationSeconds < 30) clips[index] = clip.copy(durationSeconds = clip.durationSeconds + 1)
                            }) { Text("+") }
                        }
                    }
                    Column {
                        IconButton(onClick = {
                            if (index > 0) {
                                val current = clips[index]
                                clips[index] = clips[index - 1]
                                clips[index - 1] = current
                            }
                        }, enabled = index > 0) { Icon(Icons.Default.ArrowUpward, contentDescription = "Move clip up") }
                        IconButton(onClick = {
                            if (index < clips.lastIndex) {
                                val current = clips[index]
                                clips[index] = clips[index + 1]
                                clips[index + 1] = current
                            }
                        }, enabled = index < clips.lastIndex) { Icon(Icons.Default.ArrowDownward, contentDescription = "Move clip down") }
                        IconButton(onClick = { clips.removeAt(index) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove clip", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
        item {
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Export settings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text("Project: $projectName")
                    Text("Canvas: $aspectRatio • Transition: $transition")
                    Text("Estimated duration: $totalSeconds seconds")
                    Text("MP4 rendering, music/voice-over mixing and Gallery export are not connected yet. This phase is building the editor and timeline first.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { status = "Project settings ready. MP4 export engine is the next implementation step." },
                        enabled = clips.isNotEmpty()
                    ) {
                        Icon(Icons.Default.Movie, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Prepare video project")
                    }
                    Text(status, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun SettingsScreen(
    modifier: Modifier = Modifier,
    themeMode: String,
    onThemeChange: (String) -> Unit
) {
    LazyColumn(modifier = modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Text("Settings", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Personalize your JARVIS experience.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                    Text("Appearance", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text("Choose how JARVIS looks.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    ThemeOption("System default", "system", themeMode, Icons.Default.BrightnessAuto, onThemeChange)
                    ThemeOption("Light mode", "light", themeMode, Icons.Default.WbSunny, onThemeChange)
                    ThemeOption("Dark mode", "dark", themeMode, Icons.Default.DarkMode, onThemeChange)
                }
            }
        }
        item {
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("App information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text("JARVIS 2.0", fontWeight = FontWeight.Medium)
                    Text("Package: com.jarvis.pakage", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("JARVIS 2.0 • Phone Manager development", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun ThemeOption(
    title: String,
    value: String,
    selected: String,
    icon: ImageVector,
    onSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable { onSelected(value) }.padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(12.dp))
        Text(title, modifier = Modifier.weight(1f))
        if (selected == value) {
            Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary)
        }
        RadioButton(selected = selected == value, onClick = { onSelected(value) })
    }
}
