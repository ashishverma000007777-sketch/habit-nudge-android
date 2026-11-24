package com.habitnudge.app.ui.screens.journal

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitnudge.app.data.repository.JournalRepository
import com.habitnudge.app.ui.components.EmptyStateCard
import com.habitnudge.app.ui.components.FrostedGlassCard
import com.habitnudge.app.ui.components.GlassBackdrop
import com.habitnudge.app.ui.components.GlassNavigationBar
import com.habitnudge.app.ui.components.GlassTier
import com.habitnudge.app.ui.navigation.Screen
import com.habitnudge.app.ui.theme.AccentOrange
import com.habitnudge.app.ui.theme.Dimens
import com.habitnudge.app.ui.theme.LocalGlassPalette
import com.habitnudge.app.ui.theme.SuccessGreen
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun NightJournalScreen(
    journalRepository: JournalRepository,
    onNavigate: (String) -> Unit
) {
    val glass = LocalGlassPalette.current
    val allEntries by journalRepository.getAllEntries().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    val todayStr = remember { LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE) }
    var selectedTab by remember { mutableIntStateOf(0) }

    var wentWellText by remember { mutableStateOf("") }
    var wastedTimeText by remember { mutableStateOf("") }
    var improveTomorrowText by remember { mutableStateOf("") }
    var saveMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val todayEntry = journalRepository.getTodayEntry()
        if (todayEntry != null) {
            wentWellText = todayEntry.wentWell
            wastedTimeText = todayEntry.wastedTimeOrEnergy
            improveTomorrowText = todayEntry.improveTomorrow
        }
    }

    GlassBackdrop {
        Scaffold(
            bottomBar = {
                GlassNavigationBar(
                    currentRoute = Screen.Journal.route,
                    onNavigate = onNavigate
                )
            },
            containerColor = Color.Transparent
        ) { _ ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(horizontal = Dimens.spaceScreenHorizontal)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Column {
                    Text(
                        text = "DISTRACTION-FREE REFLECTION",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp),
                        color = glass.textSecondary
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Night Journal",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 27.sp
                        ),
                        color = glass.textPrimary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Glass Segmented Tabs
                FrostedGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    tier = GlassTier.Low,
                    shape = RoundedCornerShape(Dimens.radiusPill),
                    contentPadding = PaddingValues(4.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        JournalTabPill(
                            label = "Today's Reflection",
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            modifier = Modifier.weight(1f)
                        )
                        JournalTabPill(
                            label = "Past Entries (${allEntries.size})",
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (selectedTab == 0) {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(bottom = Dimens.bottomBarClearance)
                    ) {
                        item {
                            JournalPromptField(
                                number = 1,
                                prompt = "What went well today?",
                                value = wentWellText,
                                onValueChange = { wentWellText = it }
                            )
                        }

                        item {
                            JournalPromptField(
                                number = 2,
                                prompt = "What wasted my time or drained my energy?",
                                value = wastedTimeText,
                                onValueChange = { wastedTimeText = it }
                            )
                        }

                        item {
                            JournalPromptField(
                                number = 3,
                                prompt = "What's one thing I want to do better tomorrow?",
                                value = improveTomorrowText,
                                onValueChange = { improveTomorrowText = it }
                            )
                        }

                        item {
                            AnimatedVisibility(
                                visible = saveMessage != null,
                                enter = fadeIn(),
                                exit = fadeOut()
                            ) {
                                FrostedGlassCard(
                                    tier = GlassTier.Medium,
                                    accentBorderColor = SuccessGreen.copy(alpha = 0.5f),
                                    contentPadding = PaddingValues(12.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = SuccessGreen)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = saveMessage ?: "",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = SuccessGreen
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            Button(
                                onClick = {
                                    scope.launch {
                                        journalRepository.saveEntry(
                                            date = todayStr,
                                            wentWell = wentWellText,
                                            wastedTimeOrEnergy = wastedTimeText,
                                            improveTomorrow = improveTomorrowText
                                        )
                                        saveMessage = "Reflection saved successfully for today."
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
                                shape = RoundedCornerShape(Dimens.radiusPill)
                            ) {
                                Text(
                                    "Save Tonight's Reflection",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }
                        }
                    }
                } else {
                    if (allEntries.isEmpty()) {
                        EmptyStateCard(
                            title = "No Past Reflections",
                            description = "Your saved night journals will appear here for quiet review.",
                            icon = Icons.Default.Book
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = Dimens.bottomBarClearance)
                        ) {
                            items(allEntries, key = { it.id }) { entry ->
                                FrostedGlassCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    tier = GlassTier.Medium,
                                    contentPadding = PaddingValues(16.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(
                                            text = entry.date,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontFeatureSettings = "tnum"
                                            ),
                                            color = AccentOrange
                                        )

                                        if (entry.wentWell.isNotBlank()) {
                                            EntryBlock(label = "Went well", text = entry.wentWell)
                                        }
                                        if (entry.wastedTimeOrEnergy.isNotBlank()) {
                                            EntryBlock(label = "Drained energy", text = entry.wastedTimeOrEnergy)
                                        }
                                        if (entry.improveTomorrow.isNotBlank()) {
                                            EntryBlock(label = "Improve tomorrow", text = entry.improveTomorrow)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun JournalTabPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val glass = LocalGlassPalette.current
    val bg by animateColorAsState(
        targetValue = if (selected) AccentOrange.copy(alpha = 0.20f) else Color.Transparent,
        label = "journalTabBg"
    )
    val border by animateColorAsState(
        targetValue = if (selected) AccentOrange.copy(alpha = 0.42f) else Color.Transparent,
        label = "journalTabBorder"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(Dimens.radiusPill))
            .background(bg)
            .border(Dimens.strokeThin, border, RoundedCornerShape(Dimens.radiusPill))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
            ),
            color = if (selected) AccentOrange else glass.textSecondary
        )
    }
}

@Composable
private fun EntryBlock(label: String, text: String) {
    val glass = LocalGlassPalette.current
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, letterSpacing = 0.6.sp),
            color = glass.textTertiary
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = glass.textPrimary
        )
    }
}

@Composable
private fun JournalPromptField(
    number: Int,
    prompt: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    val glass = LocalGlassPalette.current

    FrostedGlassCard(
        modifier = Modifier.fillMaxWidth(),
        tier = GlassTier.Medium,
        contentPadding = PaddingValues(16.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(AccentOrange.copy(alpha = 0.18f))
                        .border(Dimens.strokeThin, AccentOrange.copy(alpha = 0.35f), RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$number",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = AccentOrange
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = prompt,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = glass.textPrimary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp),
                placeholder = {
                    Text(
                        "Write your thoughts here...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = glass.textTertiary
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = glass.surfaceLow,
                    unfocusedContainerColor = glass.surfaceLow,
                    focusedBorderColor = AccentOrange.copy(alpha = 0.7f),
                    unfocusedBorderColor = glass.borderSubtle,
                    focusedTextColor = glass.textPrimary,
                    unfocusedTextColor = glass.textPrimary,
                    cursorColor = AccentOrange
                ),
                shape = RoundedCornerShape(Dimens.radiusSmall)
            )
        }
    }
}
