package com.habitnudge.app.data.backup

import com.habitnudge.app.data.local.dao.CheckInDao
import com.habitnudge.app.data.local.dao.HabitDao
import com.habitnudge.app.data.local.dao.JournalDao
import com.habitnudge.app.data.local.dao.ReminderDao
import com.habitnudge.app.data.local.entity.HabitCheckInEntity
import com.habitnudge.app.data.local.entity.NightJournalEntity
import com.habitnudge.app.data.local.entity.ReminderEntity
import com.habitnudge.app.data.preferences.AppTheme
import com.habitnudge.app.data.preferences.UserPreferencesRepository
import com.habitnudge.app.data.preferences.UserSettings
import com.habitnudge.app.reminder.AlarmScheduler
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

data class BackupPayload(
    val version: Int = 1,
    val exportedAtMillis: Long,
    val habitStates: List<HabitStateBackup>,
    val checkIns: List<HabitCheckInEntity>,
    val reminders: List<ReminderEntity>,
    val journals: List<NightJournalEntity>,
    val settings: UserSettings
)

data class HabitStateBackup(
    val id: String,
    val isActive: Boolean,
    val startDate: Long?,
    val isPaused: Boolean
)

data class RestoreSummary(
    val habitsRestored: Int,
    val checkInsRestored: Int,
    val remindersRestored: Int,
    val journalsRestored: Int
)

@Singleton
class BackupRestoreManager @Inject constructor(
    private val habitDao: HabitDao,
    private val checkInDao: CheckInDao,
    private val reminderDao: ReminderDao,
    private val journalDao: JournalDao,
    private val preferencesRepository: UserPreferencesRepository,
    private val alarmScheduler: AlarmScheduler
) {
    suspend fun exportBackupJson(): String {
        val habits = habitDao.getAllHabits().first()
        val checkIns = checkInDao.getAllCheckInsList()
        val reminders = reminderDao.getAllReminders().first()
        val journals = journalDao.getAllEntries().first()
        val settings = preferencesRepository.userSettingsFlow.first()

        val payload = BackupPayload(
            exportedAtMillis = System.currentTimeMillis(),
            habitStates = habits.map {
                HabitStateBackup(
                    id = it.id,
                    isActive = it.isActive,
                    startDate = it.startDate,
                    isPaused = it.isPaused
                )
            },
            checkIns = checkIns,
            reminders = reminders,
            journals = journals,
            settings = settings
        )
        return serializePayload(payload)
    }

    suspend fun importBackupJson(rawJson: String): RestoreSummary {
        val payload = parsePayload(rawJson)

        // Restore habit active/pause/start state for known habits
        var habitsRestored = 0
        for (state in payload.habitStates) {
            val existing = habitDao.getHabitById(state.id)
            if (existing != null) {
                habitDao.updateActiveStatus(state.id, state.isActive, state.startDate)
                habitDao.updatePausedStatus(state.id, state.isPaused)
                habitsRestored++
            }
        }

        // Restore check-ins
        for (checkIn in payload.checkIns) {
            checkInDao.insertOrUpdate(checkIn)
        }

        // Replace existing reminders cleanly with backed-up reminders
        val existingReminders = reminderDao.getAllReminders().first()
        for (old in existingReminders) {
            alarmScheduler.cancelReminder(old.id)
            reminderDao.deleteById(old.id)
        }
        for (reminder in payload.reminders) {
            reminderDao.insert(reminder.copy(id = 0))
        }

        // Restore night journal entries
        for (entry in payload.journals) {
            journalDao.insertOrUpdate(entry.copy(id = 0))
        }

        // Restore preferences
        val s = payload.settings
        preferencesRepository.setTheme(s.theme)
        preferencesRepository.setNotificationsEnabled(s.notificationsEnabled)
        preferencesRepository.setQuietHours(
            s.quietHoursEnabled,
            s.quietHoursStartHour,
            s.quietHoursStartMinute,
            s.quietHoursEndHour,
            s.quietHoursEndMinute
        )
        preferencesRepository.setSoundEnabled(s.reminderSoundEnabled)
        preferencesRepository.setVibrationEnabled(s.reminderVibrationEnabled)
        preferencesRepository.setSoundTone(s.soundToneUri, s.soundToneTitle)
        preferencesRepository.setRepeatingAlarmEnabled(s.repeatingAlarmEnabled)
        preferencesRepository.setAlarmMaxDurationSeconds(s.alarmMaxDurationSeconds)
        preferencesRepository.setAlarmVolume(s.alarmVolume)
        preferencesRepository.setAlarmTone(s.alarmToneUri, s.alarmToneTitle)
        preferencesRepository.setWeekStartMonday(s.weekStartMonday)
        preferencesRepository.setBedtime(s.bedtimeHour, s.bedtimeMinute)
        preferencesRepository.setOnboardingCompleted(s.isOnboardingCompleted)

        alarmScheduler.rescheduleAllAlarms()

        return RestoreSummary(
            habitsRestored = habitsRestored,
            checkInsRestored = payload.checkIns.size,
            remindersRestored = payload.reminders.size,
            journalsRestored = payload.journals.size
        )
    }

    companion object {
        fun serializePayload(payload: BackupPayload): String {
            val root = JSONObject()
            root.put("version", payload.version)
            root.put("exportedAtMillis", payload.exportedAtMillis)

            val habitsArray = JSONArray()
            payload.habitStates.forEach { state ->
                habitsArray.put(JSONObject().apply {
                    put("id", state.id)
                    put("isActive", state.isActive)
                    if (state.startDate != null) put("startDate", state.startDate) else put("startDate", JSONObject.NULL)
                    put("isPaused", state.isPaused)
                })
            }
            root.put("habitStates", habitsArray)

            val checkInsArray = JSONArray()
            payload.checkIns.forEach { ci ->
                checkInsArray.put(JSONObject().apply {
                    put("habitId", ci.habitId)
                    put("date", ci.date)
                    put("timestamp", ci.timestamp)
                    put("completed", ci.completed)
                })
            }
            root.put("checkIns", checkInsArray)

            val remindersArray = JSONArray()
            payload.reminders.forEach { r ->
                remindersArray.put(JSONObject().apply {
                    put("habitId", r.habitId)
                    put("timeHour", r.timeHour)
                    put("timeMinute", r.timeMinute)
                    put("isEnabled", r.isEnabled)
                    put("repeatDaysCsv", r.repeatDaysCsv)
                    if (r.label != null) put("label", r.label) else put("label", JSONObject.NULL)
                })
            }
            root.put("reminders", remindersArray)

            val journalsArray = JSONArray()
            payload.journals.forEach { j ->
                journalsArray.put(JSONObject().apply {
                    put("date", j.date)
                    put("timestamp", j.timestamp)
                    put("wentWell", j.wentWell)
                    put("wastedTimeOrEnergy", j.wastedTimeOrEnergy)
                    put("improveTomorrow", j.improveTomorrow)
                })
            }
            root.put("journals", journalsArray)

            val s = payload.settings
            root.put("settings", JSONObject().apply {
                put("theme", s.theme.name)
                put("notificationsEnabled", s.notificationsEnabled)
                put("quietHoursEnabled", s.quietHoursEnabled)
                put("quietHoursStartHour", s.quietHoursStartHour)
                put("quietHoursStartMinute", s.quietHoursStartMinute)
                put("quietHoursEndHour", s.quietHoursEndHour)
                put("quietHoursEndMinute", s.quietHoursEndMinute)
                put("reminderSoundEnabled", s.reminderSoundEnabled)
                put("reminderVibrationEnabled", s.reminderVibrationEnabled)
                put("soundToneUri", s.soundToneUri)
                put("soundToneTitle", s.soundToneTitle)
                put("repeatingAlarmEnabled", s.repeatingAlarmEnabled)
                put("alarmMaxDurationSeconds", s.alarmMaxDurationSeconds)
                put("alarmVolume", s.alarmVolume.toDouble())
                put("alarmToneUri", s.alarmToneUri)
                put("alarmToneTitle", s.alarmToneTitle)
                put("weekStartMonday", s.weekStartMonday)
                put("bedtimeHour", s.bedtimeHour)
                put("bedtimeMinute", s.bedtimeMinute)
                put("isOnboardingCompleted", s.isOnboardingCompleted)
            })

            return root.toString(2)
        }

        fun parsePayload(rawJson: String): BackupPayload {
            val root = JSONObject(rawJson)
            val version = root.optInt("version", 1)
            val exportedAtMillis = root.optLong("exportedAtMillis", System.currentTimeMillis())

            val habitStates = mutableListOf<HabitStateBackup>()
            val habitsArray = root.optJSONArray("habitStates") ?: JSONArray()
            for (i in 0 until habitsArray.length()) {
                val obj = habitsArray.getJSONObject(i)
                habitStates.add(
                    HabitStateBackup(
                        id = obj.getString("id"),
                        isActive = obj.optBoolean("isActive", false),
                        startDate = if (obj.isNull("startDate")) null else obj.optLong("startDate"),
                        isPaused = obj.optBoolean("isPaused", false)
                    )
                )
            }

            val checkIns = mutableListOf<HabitCheckInEntity>()
            val checkInsArray = root.optJSONArray("checkIns") ?: JSONArray()
            for (i in 0 until checkInsArray.length()) {
                val obj = checkInsArray.getJSONObject(i)
                checkIns.add(
                    HabitCheckInEntity(
                        habitId = obj.getString("habitId"),
                        date = obj.getString("date"),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                        completed = obj.optBoolean("completed", true)
                    )
                )
            }

            val reminders = mutableListOf<ReminderEntity>()
            val remindersArray = root.optJSONArray("reminders") ?: JSONArray()
            for (i in 0 until remindersArray.length()) {
                val obj = remindersArray.getJSONObject(i)
                reminders.add(
                    ReminderEntity(
                        habitId = obj.getString("habitId"),
                        timeHour = obj.getInt("timeHour").coerceIn(0, 23),
                        timeMinute = obj.getInt("timeMinute").coerceIn(0, 59),
                        isEnabled = obj.optBoolean("isEnabled", true),
                        repeatDaysCsv = obj.optString("repeatDaysCsv", "1,2,3,4,5,6,7"),
                        label = if (obj.isNull("label")) null else obj.optString("label")
                    )
                )
            }

            val journals = mutableListOf<NightJournalEntity>()
            val journalsArray = root.optJSONArray("journals") ?: JSONArray()
            for (i in 0 until journalsArray.length()) {
                val obj = journalsArray.getJSONObject(i)
                journals.add(
                    NightJournalEntity(
                        date = obj.getString("date"),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                        wentWell = obj.optString("wentWell", ""),
                        wastedTimeOrEnergy = obj.optString("wastedTimeOrEnergy", ""),
                        improveTomorrow = obj.optString("improveTomorrow", "")
                    )
                )
            }

            val sObj = root.optJSONObject("settings")
            val settings = if (sObj != null) {
                val theme = try {
                    AppTheme.valueOf(sObj.optString("theme", AppTheme.DARK.name))
                } catch (e: Exception) {
                    AppTheme.DARK
                }
                UserSettings(
                    theme = theme,
                    notificationsEnabled = sObj.optBoolean("notificationsEnabled", true),
                    quietHoursEnabled = sObj.optBoolean("quietHoursEnabled", true),
                    quietHoursStartHour = sObj.optInt("quietHoursStartHour", 22).coerceIn(0, 23),
                    quietHoursStartMinute = sObj.optInt("quietHoursStartMinute", 0).coerceIn(0, 59),
                    quietHoursEndHour = sObj.optInt("quietHoursEndHour", 7).coerceIn(0, 23),
                    quietHoursEndMinute = sObj.optInt("quietHoursEndMinute", 0).coerceIn(0, 59),
                    reminderSoundEnabled = sObj.optBoolean("reminderSoundEnabled", true),
                    reminderVibrationEnabled = sObj.optBoolean("reminderVibrationEnabled", true),
                    soundToneUri = sObj.optString("soundToneUri", ""),
                    soundToneTitle = sObj.optString("soundToneTitle", "Default notification tone"),
                    repeatingAlarmEnabled = sObj.optBoolean("repeatingAlarmEnabled", true),
                    alarmMaxDurationSeconds = sObj.optInt("alarmMaxDurationSeconds", 60).coerceIn(5, 300),
                    alarmVolume = sObj.optDouble("alarmVolume", 0.85).toFloat().coerceIn(0f, 1f),
                    alarmToneUri = sObj.optString("alarmToneUri", ""),
                    alarmToneTitle = sObj.optString("alarmToneTitle", "Default alarm sound"),
                    weekStartMonday = sObj.optBoolean("weekStartMonday", true),
                    bedtimeHour = sObj.optInt("bedtimeHour", 23).coerceIn(0, 23),
                    bedtimeMinute = sObj.optInt("bedtimeMinute", 0).coerceIn(0, 59),
                    isOnboardingCompleted = sObj.optBoolean("isOnboardingCompleted", true)
                )
            } else {
                UserSettings(isOnboardingCompleted = true)
            }

            return BackupPayload(
                version = version,
                exportedAtMillis = exportedAtMillis,
                habitStates = habitStates,
                checkIns = checkIns,
                reminders = reminders,
                journals = journals,
                settings = settings
            )
        }
    }
}
