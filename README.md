# Habit Nudge — Premium Android Habit Tracker & Smart Reminder App

> **"Small Habits. Stronger You."**

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.24-7F52FF.svg?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4.svg?logo=android&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Architecture](https://img.shields.io/badge/Architecture-MVVM%20%2B%20Clean-00C853.svg)](https://developer.android.com/topic/architecture)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

**Habit Nudge** is a production-quality, privacy-first, 100% offline Android habit tracking application and smart reminder engine. Built from scratch with Kotlin and Jetpack Compose (Material 3), it delivers an intentional, calm, and distraction-free experience inspired by Linear's clarity, Apple Health's progress visualization, Headspace's approachable tone, and Things 3's task elegance.

---

## 📱 Screenshots & UI Architecture

| Screen | Description |
|---|---|
| **1. Splash Screen** | Minimalist animated brand reveal displaying the app icon, title, and tagline with smooth transition. |
| **2. Onboarding** | Explains the philosophy, guides permission setup, and lets users hand-pick their starting habit challenges. |
| **3. Home Dashboard** | Core central cockpit: dynamic greeting, circular daily progress ring, weekly streak counter, active and pending habit lists, and quick check-in actions. |
| **4. Habit Details** | Deep-dive screen: challenge duration, specific directives, live streak calculation, best streak, missed days count, pause/resume, restart, and custom reminder management. |
| **5. Nudge Center** | Comprehensive reminder manager: precision alarms, quiet hours status, individual toggles, snooze controls (10m, 15m, 30m), and new reminder creation. |
| **6. Progress & Stats** | Visual analytics: 7-day execution rhythm chart, daily completion rate, all-time best streaks, and challenge milestone counters. |
| **7. Night Journal** | Distraction-free evening reflection interface featuring 3 foundational prompts, local date-stamped storage, and historical entry browsing. |
| **8. Settings** | Sophisticated preferences: Theme selection (Dark / Light / System), quiet hours configuration, notification sounds, challenge reset controls, and privacy guarantee. |

---

## 🎯 The 7 Habit Challenges

Habit Nudge strictly implements the 7 core habit challenges. None are forced upon the user; users activate the specific challenges they choose to undertake.

1. **Reduce Added Sugar** (30 Days)
   - Replace sugary drinks with water.
   - Reduce candy and packaged sugary snacks.
   - Check added sugar in regular foods.
   - Do not unnecessarily avoid natural whole fruit.

2. **Daily Hydration** (21 Days)
   - Keep water nearby throughout the day.
   - Drink when thirsty.
   - Adjust according to physical activity, ambient weather, and exertion.
   - Avoid forcing excessive water consumption.

3. **No Phone Before Bed** (14 Nights)
   - Put the phone away one hour before bed.
   - Avoid scrolling, short videos, and unnecessary messaging.
   - Read a few pages or prepare clothing/tasks for tomorrow.

4. **Meditation** (21 Days)
   - Meditate for 10 minutes daily.
   - Sit in a quiet, undisturbed space.
   - Anchor focus on natural breathing.
   - Return attention gently whenever distracted.

5. **Cool Water Shower** (30 Days — *Optional Challenge*)
   - Conclude regular shower with mildly cool water.
   - Adjust temperature gradually.
   - Stop immediately if feeling unwell.
   - Never force extreme cold exposure.

6. **Three Hours Alone** (Weekly Challenge — 4 Weeks)
   - Spend three dedicated hours alone each week.
   - Disconnect completely from social media and constant notifications.
   - Walk, visit a park, sit in a quiet cafe, or engage in solitary contemplation.
   - Reflect on personal aspirations and weekly habits.

7. **Night Journaling** (30 Days)
   - Write for 10 minutes before sleeping.
   - *Prompt 1*: What went well today?
   - *Prompt 2*: What wasted my time or drained my energy?
   - *Prompt 3*: What's one thing I want to do better tomorrow?

---

## 🔔 Smart Reminder Engine

The Smart Reminder Engine is engineered for reliability, respect, and zero spam:

- **Exact & Inexact Alarm Scheduling**: Uses native Android `AlarmManager` with `setExactAndAllowWhileIdle()` where permitted, falling back gracefully to battery-optimized alarms.
- **Smart Completion Detection**: Reminders are automatically suppressed once a habit has been completed for the day.
- **Interactive Notifications**: Action buttons directly in the system shade:
  - **Mark Done**: Immediately writes a check-in record to the Room database and dismisses the notification.
  - **Snooze 10m**: Snoozes the alarm by 10 minutes and schedules a follow-up alert.
- **Quiet Hours Enforcement**: User-defined quiet window (default: 22:00 to 07:00). Nudges within this window are quietly suppressed.
- **System Event Recovery**: Registered `BootAndScheduleReceiver` handles `ACTION_BOOT_COMPLETED`, `ACTION_LOCKED_BOOT_COMPLETED`, `ACTION_TIME_CHANGED`, `ACTION_TIMEZONE_CHANGED`, and `ACTION_MY_PACKAGE_REPLACED` to guarantee schedule integrity after restarts or timezone travel.

---

## 🏗️ Architecture & Technology Stack

```
com.habitnudge.app
├── data
│   ├── local (Room DB, DAOs, Entities, Converters)
│   ├── preferences (DataStore Preferences, UserSettings)
│   └── repository (HabitRepository, ReminderRepository, JournalRepository)
├── di (Hilt Dependency Injection modules)
├── domain
│   ├── model (Habit, HabitProgress, DayCompletion, OverallDailyStats)
│   └── usecase (CalculateStreakUseCase)
├── reminder (AlarmScheduler, NotificationHelper, Receivers)
│   └── receiver (HabitReminderReceiver, HabitNotificationActionReceiver, BootAndScheduleReceiver)
└── ui
    ├── components (CircularProgressRing, HabitCard, CompletionButton, StreakBadge, WeeklyBarChart)
    ├── navigation (Screen, HabitNudgeNavHost, HabitNudgeBottomBar)
    ├── screens (Splash, Onboarding, Home, Detail, Reminders, Stats, Journal, Settings)
    └── theme (Color, Dimens, Theme, Type)
```

- **Kotlin 1.9.24**: Modern, safe, expressive language features.
- **Jetpack Compose + Material 3**: Declarative UI, state hoisting, custom canvas drawing for circular progress rings.
- **Room Database 2.6.1**: SQLite abstraction with TypeConverters, Flow observation, and indices.
- **DataStore Preferences 1.1.1**: Modern, thread-safe asynchronous key-value persistence.
- **Dagger Hilt 2.51.1**: Standardized dependency injection for Android components and view models.
- **Coroutines & Kotlin Flow**: Reactive Unidirectional Data Flow (UDF).

---

## 🧪 Quality Assurance & Unit Tests

The test suite thoroughly verifies core business calculations, streak rules, weekly scheduling, quiet hours boundaries, and challenge specifications:

- **`CalculateStreakUseCaseTest`**:
  - `empty check-ins returns zero streak and zero progress`: PASS
  - `check-in today gives streak of 1`: PASS
  - `consecutive 5 days ending yesterday maintains streak of 5 before today is checked`: PASS
  - `checking in today increases streak from 5 to 6`: PASS
  - `broken streak resets current streak to 0 if yesterday was missed and today is not checked`: PASS
  - `challenge completion is detected when completed days reach duration`: PASS
- **`HabitChallengesAndSchedulingTest`**:
  - `verify all seven habit challenges match required specifications`: PASS
  - `verify weekly habit scheduling finds next Saturday accurately`: PASS
  - `verify quiet hours boundary logic`: PASS
  - `streak calculation with multiple gaps retains overall best streak`: PASS
- **`AlarmSchedulerLogicTest`**:
  - `snooze duration calculation adds exact minutes`: PASS
  - `repeat days parsing correctly handles CSV`: PASS
  - `single repeat day for weekly habit like Three Hours Alone parses correctly`: PASS

```bash
# Run unit tests
./gradlew testDebugUnitTest
# Result: BUILD SUCCESSFUL — 13/13 tests passed (0 failures, 0 skipped)
```

---

## 📦 Building & Installing the APK

### Build Debug APK
```bash
./gradlew assembleDebug
# Generated at: app/build/outputs/apk/debug/app-debug.apk (16.6 MB)
```

### Build Production-Ready Release APK
```bash
./gradlew assembleRelease
# Generated at: app/build/outputs/apk/release/app-release.apk (11.4 MB)
```

### Install via ADB
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Install Directly on Android Device
1. Transfer `app-debug.apk` to your Android device via USB, Google Drive, or local storage.
2. Open your device's File Manager and tap `app-debug.apk`.
3. Allow "Install unknown apps" if prompted.
4. Launch **Habit Nudge** from your home screen.

---

## 🔒 Privacy & Permissions

- **`POST_NOTIFICATIONS`**: Required on Android 13+ to post reminder nudges.
- **`SCHEDULE_EXACT_ALARM` / `USE_EXACT_ALARM`**: Allows precise delivery of scheduled habit reminders.
- **`RECEIVE_BOOT_COMPLETED`**: Re-schedules alarms automatically after device reboots.
- **Zero Internet Permissions**: The app requires NO internet permission (`android.permission.INTERNET` is omitted). Your personal thoughts, habit check-ins, and data never leave your device.

---

## 📋 Known Limitations & Hardware Considerations

1. **Aggressive Battery Optimizers (OEM Specific)**: Manufacturers such as Xiaomi (MIUI/HyperOS), Samsung (OneUI), and Huawei apply aggressive background process limits. For exact minute delivery on these devices, users may need to set Battery Saver to "No restrictions" for Habit Nudge.
2. **Exact Alarm Permission**: On Android 12+, if the user revokes "Alarms & Reminders" permission from system settings, Habit Nudge automatically degrades gracefully to inexact alarms instead of crashing.

---

## 📄 License

Habit Nudge is open-source software licensed under the MIT License.
