package com.habitnudge.app.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.habitnudge.app.ui.navigation.Screen
import com.habitnudge.app.ui.theme.AccentOrange
import com.habitnudge.app.ui.theme.Dimens
import com.habitnudge.app.ui.theme.SuccessGreen
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    habitRepository: HabitRepository,
    preferencesRepository: UserPreferencesRepository,
    alarmScheduler: AlarmScheduler,
    onFinishOnboarding: () -> Unit
) {
    val habits by habitRepository.getAllHabits().collectAsState(initial = emptyList())
    val selectedHabitIds = remember { mutableStateListOf<String>() }
    val scope = rememberCoroutineScope()

    // Pre-select 2 popular starter habits for user convenience (Hydration & No Phone)
    remember(habits) {
        if (selectedHabitIds.isEmpty() && habits.isNotEmpty()) {
            selectedHabitIds.add("daily_hydration")
            selectedHabitIds.add("no_phone_before_bed")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "Welcome to Habit Nudge",
            style = MaterialTheme.typography.displayMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp
            ),
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Small Habits. Stronger You.",
            style = MaterialTheme.typography.titleMedium,
            color = AccentOrange
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Select the habit challenges you wish to begin today. You can customize reminders and pause or restart challenges anytime.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
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

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                scope.launch {
                    // Activate selected habits
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
                .height(52.dp),
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
                color = MaterialTheme.colorScheme.background
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun OnboardingHabitItem(
    habit: Habit,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    val borderColor = if (isSelected) AccentOrange else MaterialTheme.colorScheme.outline
    val bgColor = if (isSelected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimens.radiusMedium))
            .background(bgColor)
            .border(Dimens.strokeThin, borderColor, RoundedCornerShape(Dimens.radiusMedium))
            .clickable(onClick = onToggle)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(if (isSelected) AccentOrange else Color.Transparent, CircleShape)
                .border(2.dp, if (isSelected) AccentOrange else MaterialTheme.colorScheme.outline, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.background,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = habit.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                if (habit.isOptional) {
                    Text(
                        text = "Optional",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = habit.tagline,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${habit.durationDays} Days • ${habit.category}",
                style = MaterialTheme.typography.labelSmall,
                color = AccentOrange.copy(alpha = 0.85f)
            )
        }
    }
}
