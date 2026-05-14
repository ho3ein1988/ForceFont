package com.fontforce.xposed

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fontforce.xposed.ui.screens.AboutScreen
import com.fontforce.xposed.ui.screens.AppsScreen
import com.fontforce.xposed.ui.screens.HomeScreen
import com.fontforce.xposed.ui.theme.FontForceTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            FontForceTheme {
                FontForceApp(viewModel = viewModel)
            }
        }
    }
}

// ─── Nav destinations ────────────────────────────────────────────────────────
data class NavItem(
    val label: String,
    val iconSelected: ImageVector,
    val iconUnselected: ImageVector
)

private val navItems = listOf(
    NavItem("Home",  Icons.Filled.Home,  Icons.Outlined.Home),
    NavItem("Apps",  Icons.Filled.Apps,  Icons.Outlined.Apps),
    NavItem("About", Icons.Filled.Info,  Icons.Outlined.Info)
)

// ─── Root composable ─────────────────────────────────────────────────────────
@Composable
fun FontForceApp(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedIndex by remember { mutableIntStateOf(0) }
    val colorScheme = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            AppHeader(uiState = uiState)

            Box(
                modifier = Modifier
                    .weight(1f)
                    .navigationBarsPadding()
                    .padding(bottom = 90.dp)
            ) {
                when (selectedIndex) {
                    0 -> HomeScreen(
                        uiState = uiState,
                        onToggleMethod = { id, enabled -> viewModel.toggleMethod(id, enabled) }
                    )
                    1 -> AppsScreen()
                    2 -> AboutScreen()
                }
            }
        }

        FloatingNavBar(
            items = navItems,
            selectedIndex = selectedIndex,
            onSelect = { selectedIndex = it },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        )
    }
}

// ─── App header ──────────────────────────────────────────────────────────────
@Composable
fun AppHeader(uiState: MainUiState) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = colorScheme.primary,
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text(
                    text = "F",
                    color = colorScheme.onPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = "FontForce",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
            color = colorScheme.onBackground,
            modifier = Modifier.weight(1f)
        )
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = colorScheme.primaryContainer
        ) {
            Text(
                text = "v${BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                color = colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
    }
    HorizontalDivider(
        color = colorScheme.outlineVariant.copy(alpha = 0.3f),
        thickness = 0.5.dp
    )
}

// ─── Floating nav bar with Material Icons ────────────────────────────────────
@Composable
fun FloatingNavBar(
    items: List<NavItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme

    Surface(
        shape = RoundedCornerShape(32.dp),
        color = colorScheme.surfaceContainerHigh,
        tonalElevation = 3.dp,
        shadowElevation = 16.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            items.forEachIndexed { index, item ->
                val selected = index == selectedIndex

                val bgColor by animateColorAsState(
                    targetValue = if (selected) colorScheme.primaryContainer else Color.Transparent,
                    label = "navBg_$index"
                )
                val iconColor by animateColorAsState(
                    targetValue = if (selected) colorScheme.onPrimaryContainer else colorScheme.onSurfaceVariant,
                    label = "navFg_$index"
                )

                Surface(
                    onClick = { onSelect(index) },
                    shape = RoundedCornerShape(24.dp),
                    color = bgColor,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = if (selected) item.iconSelected else item.iconUnselected,
                            contentDescription = item.label,
                            tint = iconColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                            ),
                            color = iconColor
                        )
                    }
                }
            }
        }
    }
}
