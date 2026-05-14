package com.fontforce.xposed.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fontforce.xposed.MainUiState
import com.fontforce.xposed.data.ALL_HOOK_METHODS
import com.fontforce.xposed.data.HookMethod

// ─── Status colors (semantic, not Monet — intentional green/red) ─────────────
private val ActiveContainerLight   = Color(0xFFCBEDB7)
private val ActiveOnContainerLight = Color(0xFF1B5E20)
private val ActiveBorderLight      = Color(0xFF639922)

private val InactiveContainerLight   = Color(0xFFFFCDD2)
private val InactiveOnContainerLight = Color(0xFF7F1D1D)
private val InactiveBorderLight      = Color(0xFFE53935)

@Composable
fun HomeScreen(
    uiState: MainUiState,
    onToggleMethod: (String, Boolean) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val isDark = !MaterialTheme.colorScheme.surface.luminance().let { it > 0.5f }

    // Status card colors
    val statusContainer by animateColorAsState(
        if (uiState.isModuleActive) ActiveContainerLight else InactiveContainerLight,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow), label = "statusBg"
    )
    val statusOnContainer by animateColorAsState(
        if (uiState.isModuleActive) ActiveOnContainerLight else InactiveOnContainerLight,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow), label = "statusFg"
    )
    val statusBorder by animateColorAsState(
        if (uiState.isModuleActive) ActiveBorderLight else InactiveBorderLight,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow), label = "statusBorder"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp, bottom = 16.dp)
    ) {

        // ── Status card ──────────────────────────────────────────────────────
        Spacer(Modifier.height(8.dp))
        StatusCard(
            isActive = uiState.isModuleActive,
            containerColor = statusContainer,
            contentColor = statusOnContainer,
            borderColor = statusBorder
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Tap card to see details",
            style = MaterialTheme.typography.labelSmall,
            color = colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        // ── Methods section ──────────────────────────────────────────────────
        Spacer(Modifier.height(20.dp))
        Text(
            text = "HOOK METHODS",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.8.sp
            ),
            color = colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
        )
        Spacer(Modifier.height(6.dp))

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = colorScheme.surface,
            tonalElevation = 1.dp,
            shadowElevation = 0.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                ALL_HOOK_METHODS.forEachIndexed { index, method ->
                    val enabled = uiState.methodStates[method.id] ?: method.defaultEnabled
                    MethodRow(
                        method = method,
                        enabled = enabled,
                        onToggle = { onToggleMethod(method.id, it) }
                    )
                    if (index < ALL_HOOK_METHODS.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 14.dp),
                            color = colorScheme.outlineVariant.copy(alpha = 0.5f),
                            thickness = 0.5.dp
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Info note
        Text(
            text = "Changes take effect after rebooting or force-stopping the target app.",
            style = MaterialTheme.typography.bodySmall,
            color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

// ─── Status Card ─────────────────────────────────────────────────────────────
@Composable
fun StatusCard(
    isActive: Boolean,
    containerColor: Color,
    contentColor: Color,
    borderColor: Color
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon circle
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(borderColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isActive) "✓" else "✕",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isActive) "Active" else "Inactive",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = contentColor
                )
                Text(
                    text = if (isActive) "Module enabled in LSPosed" else "Module not detected in LSPosed",
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.75f)
                )
            }
        }
    }
}

// ─── Method Row ──────────────────────────────────────────────────────────────
@Composable
fun MethodRow(
    method: HookMethod,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle(!enabled) }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Checkbox
        Checkbox(
            checked = enabled,
            onCheckedChange = { onToggle(it) },
            colors = CheckboxDefaults.colors(
                checkedColor = colorScheme.primary,
                uncheckedColor = colorScheme.outline,
                checkmarkColor = colorScheme.onPrimary
            )
        )

        Spacer(Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = method.name,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = colorScheme.onSurface
            )
            Text(
                text = method.description,
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onSurfaceVariant
            )
        }

        // Badge
        if (method.badge != null) {
            Spacer(Modifier.width(8.dp))
            val (badgeBg, badgeFg) = when (method.badge) {
                "Beta" -> colorScheme.errorContainer to colorScheme.onErrorContainer
                else   -> colorScheme.primaryContainer to colorScheme.onPrimaryContainer
            }
            Surface(
                shape = CircleShape,
                color = badgeBg
            ) {
                Text(
                    text = method.badge,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = badgeFg,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
    }
}

// Helper to check luminance (simple approach)
private fun Color.luminance(): Float {
    return (red * 0.2126f + green * 0.7152f + blue * 0.0722f)
}
