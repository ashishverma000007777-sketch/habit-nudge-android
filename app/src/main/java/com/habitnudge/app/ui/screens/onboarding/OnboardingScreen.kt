package com.habitnudge.app.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitnudge.app.data.preferences.UserPreferencesRepository
import com.habitnudge.app.data.repository.HabitRepository
import com.habitnudge.app.domain.model.Habit
import com.habitnudge.app.reminder.AlarmScheduler
import com.habitnudge.app.reminder.HabitNotificationRegistry
import com.habitnudge.app.ui.components.FrostedGlassCard
import com.habitnudge.app.ui.components.GlassBackdrop
import com.habitnudge.app.ui.components.GlassTier
import com.habitnudge.app.ui.theme.AccentOrange
import com.habitnudge.app.ui.theme.Dimens
import com.habitnudge.app.ui.theme.LocalGlassPalette
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    habitRepository: HabitRepository,
    preferencesRepository: UserPreferencesRepository,
    alarmScheduler: AlarmScheduler,
    onFinishOnboarding: () -> Unit
) {
    val glass = LocalGlassPalette.current
    val habits by habitRepository.getAllHabits().collectAsState(initial = emptyList())
    val selectedHabitIds = remember { mutableStateListOf<String>() }
    val scope = rememberCoroutineScope()

    remember(habits) {
        if (selectedHabitIds.isEmpty() && habits.isNotEmpty()) {
            selectedHabitIds.add("daily_hydration")
            selectedHabitIds.add("no_phone_before_bed")
        }
    }

    GlassBackdrop {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = Dimens.spaceScreenHorizontal)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "CURATED WELLNESS CHALLENGES",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp),
                color = AccentOrange
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Welcome to Habit Nudge",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 27.sp
                ),
                color = glass.textPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Select the intentional habits you wish to begin today. Customize nudges or pause anytime.",
                style = MaterialTheme.typography.bodyMedium,
                color = glass.textSecondary
            )

            Spacer(modifier = Modifier.height(18.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 12.dp)
            ) {
                items(habits, key = { it.id }) { habit ->
                    val isSelected = selectedHabitIds.contains(habit.id)
                    OnboardingHabitItem(
                        habit = habit,
                        isSelected = isSelected,
                        onToggle = {
                            if (isSelected) {
                                selectedHabitIds.remove(habit.id)
                            } else {
                                selectedHabitIds.add(habit.id)
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    scope.launch {
                        habits.forEach { habit ->
                            val shouldActivate = selectedHabitIds.contains(habit.id)
                            habitRepository.setHabitActive(habit.id, shouldActivate)
                        }
                        alarmScheduler.rescheduleAllAlarms()
                        preferencesRepository.setOnboardingCompleted(true)
                        onFinishOnboarding()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
                shape = RoundedCornerShape(Dimens.radiusPill)
            ) {
                val count = selectedHabitIds.size
                Text(
                    text = if (count > 0) "Start My $count Habit Challenge${if (count > 1) "s" else ""}" else "Continue without Challenges",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    ),
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(18.dp))
        }
    }
}

@Composable
private fun OnboardingHabitItem(
    habit: Habit,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    val glass = LocalGlassPalette.current
    val identity = HabitNotificationRegistry.getIdentity(habit.id, habit.title)
    val accentColor = try {
        Color(android.graphics.Color.parseColor(habit.accentColorHex))
    } catch (e: Exception) {
        AccentOrange
    }

    FrostedGlassCard(
        modifier = Modifier.fillMaxWidth(),
        tier = if (isSelected) GlassTier.High else GlassTier.Low,
        accentBorderColor = if (isSelected) AccentOrange else null,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        onClick = onToggle
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = 0.16f))
                    .border(Dimens.strokeThin, accentColor.copy(alpha = 0.35f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = identity.emoji, fontSize = 19.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = habit.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = glass.textPrimary
                    )
                    if (habit.isOptional) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(Dimens.radiusPill))
                                .background(glass.surfaceLow)
                                .border(Dimens.strokeThin, glass.borderSubtle, RoundedCornerShape(Dimens.radiusPill))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Optional",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = glass.textSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = habit.tagline,
                    style = MaterialTheme.typography.bodyMedium,
                    color = glass.textSecondary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${habit.durationDays} Days • ${habit.category}",
                    style = MaterialTheme.typography.labelSmall,
                    color = accentColor
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) AccentOrange else glass.surfaceLow)
                    .border(
                        width = 1.5.dp,
                        color = if (isSelected) AccentOrange else glass.borderHighlight,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
