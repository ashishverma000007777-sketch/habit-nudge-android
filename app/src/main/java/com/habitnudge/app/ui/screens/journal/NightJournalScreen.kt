package com.habitnudge.app.ui.screens.journal

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import com.habitnudge.app.ui.components.HabitNudgeBottomBar
import com.habitnudge.app.ui.components.SectionHeader
import com.habitnudge.app.ui.navigation.Screen
import com.habitnudge.app.ui.theme.AccentOrange
import com.habitnudge.app.ui.theme.Dimens
import com.habitnudge.app.ui.theme.SuccessGreen
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun NightJournalScreen(
    journalRepository: JournalRepository,
    onNavigate: (String) -> Unit
) {
    val allEntries by journalRepository.getAllEntries().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    val todayStr = remember { LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Tonight's Entry, 1 = Past Reflections

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

    Scaffold(
        bottomBar = {
            HabitNudgeBottomBar(
                currentRoute = Screen.Journal.route,
                onNavigate = onNavigate
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Column {
                Text(
                    text = "DISTRACTION-FREE REFLECTION",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Night Journal",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = AccentOrange,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = AccentOrange
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            "Today's Reflection",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (selectedTab == 0) AccentOrange else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            "Past Entries (${allEntries.size})",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (selectedTab == 1) AccentOrange else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedTab == 0) {
                // Today's entry writing view
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
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

                    if (saveMessage != null) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(SuccessGreen.copy(alpha = 0.15f), RoundedCornerShape(Dimens.radiusSmall))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
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
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
                            shape = RoundedCornerShape(Dimens.radiusPill)
                        ) {
                            Text(
                                "Save Tonight's Reflection",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.background
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            } else {
                // Past entries list
                if (allEntries.isEmpty()) {
                    EmptyStateCard(
                        title = "No Past Reflections",
                        description = "Your saved night journals will appear here for review.",
                        icon = Icons.Default.Book
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(allEntries, key = { it.id }) { entry ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(Dimens.radiusMedium))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .border(Dimens.strokeThin, MaterialTheme.colorScheme.outline, RoundedCornerShape(Dimens.radiusMedium))
                                    .padding(16.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = entry.date,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = AccentOrange
                                    )

                                    if (entry.wentWell.isNotBlank()) {
                                        Text(
                                            text = "Went well:",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = entry.wentWell,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                    }

                                    if (entry.wastedTimeOrEnergy.isNotBlank()) {
                                        Text(
                                            text = "Drained energy:",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = entry.wastedTimeOrEnergy,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                    }

                                    if (entry.improveTomorrow.isNotBlank()) {
                                        Text(
                                            text = "Improve tomorrow:",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = entry.improveTomorrow,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onBackground
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
}

@Composable
private fun JournalPromptField(
    number: Int,
    prompt: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(AccentOrange.copy(alpha = 0.2f), RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$number",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = AccentOrange
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = prompt,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            placeholder = { Text("Write your thoughts here...", style = MaterialTheme.typography.bodyMedium) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedBorderColor = AccentOrange,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            shape = RoundedCornerShape(Dimens.radiusMedium)
        )
    }
}
