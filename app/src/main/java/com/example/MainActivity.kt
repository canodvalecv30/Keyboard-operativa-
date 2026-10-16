package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.LibraryAdd
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import com.example.ui.screens.AppBackgroundView
import com.example.ui.screens.KeyboardSandboxScreen
import com.example.ui.screens.KeyboardSettingsScreen
import com.example.ui.screens.LockScreen
import com.example.ui.screens.PresetTemplatesScreen
import com.example.ui.screens.SnippetsManagerScreen
import com.example.ui.theme.AutoPasteTheme
import com.example.ui.viewmodel.MainViewModel

enum class NavigationTab(val title: String, val icon: ImageVector) {
    SANDBOX("Teclado", Icons.Default.Keyboard),
    SNIPPETS("Snippets", Icons.Default.ListAlt),
    PRESETS("Plantillas", Icons.Default.LibraryAdd),
    SETTINGS("Ajustes", Icons.Default.Settings)
}

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AutoPasteTheme(darkTheme = viewModel.preferences.isDarkTheme) {
                var isAuthenticated by remember { mutableStateOf(false) }
                var isAdmin by remember { mutableStateOf(false) }
                var selectedTab by remember { mutableIntStateOf(0) }

                if (!isAuthenticated) {
                    LockScreen(
                        viewModel = viewModel,
                        onUnlocked = { adminStatus ->
                            isAuthenticated = true
                            isAdmin = adminStatus
                        }
                    )
                } else {
                    val currentBg by viewModel.appBackground.collectAsState()
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Background image for the whole app
                        AppBackgroundView(backgroundKey = currentBg)

                        // Dark subtle overlay for contrast
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.88f))
                        )

                        Scaffold(
                            containerColor = Color.Transparent,
                            topBar = {
                                TopAppBar(
                                    title = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = MaterialTheme.colorScheme.primaryContainer,
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Image(
                                                    painter = painterResource(id = R.drawable.autopaste_icon_1784711570775),
                                                    contentDescription = "Carátula Operativa",
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = if (isAdmin) "Spam de la Operativa (Admin)" else "Spam de la Operativa Keyboard",
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.titleLarge
                                            )
                                        }
                                    },
                                    colors = TopAppBarDefaults.topAppBarColors(
                                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
                                    )
                                )
                            },
                            bottomBar = {
                                NavigationBar(
                                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
                                ) {
                                    NavigationTab.entries.forEachIndexed { index, tab ->
                                        NavigationBarItem(
                                            selected = selectedTab == index,
                                            onClick = { selectedTab = index },
                                            icon = {
                                                Icon(
                                                    imageVector = tab.icon,
                                                    contentDescription = tab.title
                                                )
                                            },
                                            label = {
                                                Text(
                                                    text = tab.title,
                                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                                                )
                                            }
                                        )
                                    }
                                }
                            }
                        ) { innerPadding ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            ) {
                                when (selectedTab) {
                                    0 -> KeyboardSandboxScreen(
                                        viewModel = viewModel,
                                        onOpenSettings = { selectedTab = 3 }
                                    )
                                    1 -> SnippetsManagerScreen(viewModel = viewModel)
                                    2 -> PresetTemplatesScreen(viewModel = viewModel)
                                    3 -> KeyboardSettingsScreen(
                                        viewModel = viewModel,
                                        isAdmin = isAdmin,
                                        onAdminStatusChange = { newAdmin -> isAdmin = newAdmin }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
