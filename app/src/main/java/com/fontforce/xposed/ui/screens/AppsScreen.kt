package com.fontforce.xposed.ui.screens

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Android
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

data class AppInfo(val name: String, val packageName: String, val isSystem: Boolean)

@Composable
fun AppsScreen() {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme

    // لیست اپ‌های نصب‌شده رو بار میکنیم
    val apps by produceState<List<AppInfo>>(initialValue = emptyList()) {
        val pm = context.packageManager
        value = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            .map { info ->
                AppInfo(
                    name = pm.getApplicationLabel(info).toString(),
                    packageName = info.packageName,
                    isSystem = (info.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                )
            }
            .filter { !it.isSystem }   // فقط اپ‌های کاربر
            .sortedBy { it.name.lowercase() }
    }

    var showSystemApps by remember { mutableStateOf(false) }
    val allApps by produceState<List<AppInfo>>(initialValue = emptyList(), showSystemApps) {
        val pm = context.packageManager
        value = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            .map { info ->
                AppInfo(
                    name = pm.getApplicationLabel(info).toString(),
                    packageName = info.packageName,
                    isSystem = (info.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                )
            }
            .filter { if (showSystemApps) true else !it.isSystem }
            .sortedBy { it.name.lowercase() }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // هدر
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${allApps.size} apps",
                style = MaterialTheme.typography.labelMedium,
                color = colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "System",
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(6.dp))
                Switch(
                    checked = showSystemApps,
                    onCheckedChange = { showSystemApps = it },
                    modifier = Modifier.height(24.dp)
                )
            }
        }

        HorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.3f), thickness = 0.5.dp)

        // نکته مهم
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            shape = RoundedCornerShape(12.dp),
            color = colorScheme.primaryContainer
        ) {
            Text(
                text = "FontForce applies to all apps in LSPosed scope. Manage scope directly in LSPosed.",
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(12.dp)
            )
        }

        // لیست اپ‌ها
        if (allApps.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier.size(32.dp))
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(allApps, key = { it.packageName }) { app ->
                    AppRow(app = app)
                }
            }
        }
    }
}

@Composable
private fun AppRow(app: AppInfo) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = colorScheme.surfaceVariant,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = Icons.Outlined.Android,
                        contentDescription = null,
                        tint = colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.name,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = app.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (app.isSystem) {
                Spacer(Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = colorScheme.secondaryContainer
                ) {
                    Text(
                        text = "SYS",
                        style = MaterialTheme.typography.labelSmall,
                        color = colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
