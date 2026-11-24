# Habit Nudge — Premium Android Habit Tracker & Smart Reminder App

> **"Small Habits. Stronger You."**

Habit Nudge is a modern, privacy-focused, offline-first Android application designed with clean visual hierarchy, reliable notifications, and a calm, intentional aesthetic. Inspired by the clarity of Linear, the personal progress visualization of Apple Health, the approachable nature of Headspace, and the craft of Things 3.

---

## 🌟 Core Highlights

- **100% Offline-First Architecture**: Zero forced sign-ups, no invasive telemetry or heavy cloud dependencies. Your personal habits and journal entries remain strictly on your device.
- **Smart Reminder Engine**: Native alarms scheduled with precision (`AlarmManager` & `WorkManager`), quiet hours enforcement, interactive notification actions (Mark Done / Snooze), reboot recovery, and timezone change resilience.
- **7 Curated Habit Challenges**:
  1. *Reduce Added Sugar* (30 Days)
  2. *Daily Hydration* (21 Days)
  3. *No Phone Before Bed* (14 Nights)
  4. *Meditation* (21 Days)
  5. *Cool Water Shower* (30 Days - Optional)
  6. *Three Hours Alone* (Weekly Challenge)
  7. *Night Journaling* (30 Days)
- **Deep Personalization & Stats**: Real-time streak tracking, completion percentages, monthly/weekly activity charts, and custom reminder schedules.
- **Distraction-Free Night Journal**: Grounded reflection prompts ("What went well today?", "What wasted my time or drained my energy?", "What's one thing I want to do better tomorrow?").
- **Dark-First Material 3 Design System**: Carefully tuned contrast ratios, tactile micro-interactions, custom circular progress rings, and cohesive typography.

---

## 🛠️ Technology Stack

- **Language**: Kotlin 1.9.22
- **UI Toolkit**: Jetpack Compose + Material Design 3
- **Architecture**: Clean Architecture + MVVM + Unidirectional Data Flow (UDF)
- **Database**: Room Persistence Library (SQLite)
- **Preferences**: Jetpack DataStore (Preferences)
- **Scheduling**: Android `AlarmManager` + `BroadcastReceiver` + `WorkManager`
- **Dependency Injection**: Modern Service Locator / Dependency Container
- **Testing**: JUnit 4, Kotlin Coroutines Test

---

## 🚀 Getting Started

### Prerequisites
- Android SDK 34 (Android 14)
- JDK 17 or 21
- Gradle 8.7+

### Build & Run
```bash
# Clone the repository
git clone https://github.com/ashishverma000007777-sketch/habit-nudge-android.git
cd habit-nudge-android

# Build Debug APK
./gradlew assembleDebug

# Run Unit Tests
./gradlew testDebugUnitTest
```
