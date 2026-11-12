package com.jarvis.pakage

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.BatteryManager
import android.provider.ContactsContract
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
                            "●  Hybrid AI • UI preview",
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

@Composable
private fun VideoStudioScreen(modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Text("Daily Reel Maker", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Your simple photo + text video workflow.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Default.Movie, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(34.dp))
                    Text("Daily Reel Template", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text("New photo • New text • Slow zoom • Fade in/out • 9:16 MP4", style = MaterialTheme.typography.bodyMedium)
                    Text("FFmpeg rendering and automatic Gallery export are planned for Phase 6.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item {
            Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Image, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(30.dp))
                    Text("Photo library", fontWeight = FontWeight.SemiBold)
                    Text("Choose a daily image source and text style when the video module is implemented.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                    Text("Version: 1.0.0 • Phase 1 UI foundation", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
