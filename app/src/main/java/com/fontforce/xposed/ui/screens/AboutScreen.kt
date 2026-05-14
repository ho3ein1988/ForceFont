package com.fontforce.xposed.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fontforce.xposed.BuildConfig

@Composable
fun AboutScreen() {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App logo area
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = colorScheme.primaryContainer,
            modifier = Modifier.size(88.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text(
                    text = "F",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = "FontForce",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
            color = colorScheme.onBackground
        )
        Text(
            text = "v${BuildConfig.VERSION_NAME}",
            style = MaterialTheme.typography.bodyMedium,
            color = colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(32.dp))

        // Info cards
        InfoCard(
            title = "What it does",
            body = "FontForce is an LSPosed/Xposed module that overrides custom fonts in Android apps and forces the system default font, improving consistency across your device."
        )
        Spacer(Modifier.height(12.dp))
        InfoCard(
            title = "How to use",
            body = "1. Enable this module in LSPosed.\n2. Select the apps you want to apply FontForce to in the LSPosed Scope settings.\n3. Reboot or force-stop the target app.\n4. Toggle individual hook methods on the Home tab as needed."
        )
        Spacer(Modifier.height(12.dp))
        InfoCard(
            title = "Compatibility",
            body = "Requires LSPosed (or EdXposed) with API 82+.\nAndroid 8.0 (API 26) and above.\nBest on Android 12+ for Monet dynamic color."
        )

        Spacer(Modifier.height(24.dp))
        Text(
            text = "Built with Jetpack Compose · Material 3",
            style = MaterialTheme.typography.labelSmall,
            color = colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun InfoCard(title: String, body: String) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = colorScheme.primary
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = body,
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onSurface.copy(alpha = 0.85f)
            )
        }
    }
}
