package org.minsithu.stillmind

import android.app.*
import android.content.*
import android.os.Build
import java.util.Calendar

object ReminderScheduler {
    const val CHANNEL = "meditation_reminders"
    fun schedule(context: Context) {
        val time = context.getSharedPreferences("stillmind_profile", Context.MODE_PRIVATE).getString("reminder", "20:00") ?: "20:00"
        val parts = time.split(":").mapNotNull { it.toIntOrNull() }
        if (parts.size != 2 || parts[0] !in 0..23 || parts[1] !in 0..59) return
        val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)
        val pending = PendingIntent.getBroadcast(context, 200, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val next = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, parts[0]); set(Calendar.MINUTE, parts[1]); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0); if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1) }
        manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, next.timeInMillis, AlarmManager.INTERVAL_DAY, pending)
        context.getSharedPreferences("stillmind_profile", Context.MODE_PRIVATE).edit().putBoolean("reminder_enabled", true).apply()
    }
    fun cancel(context:Context) {
        val alarm=context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pending=PendingIntent.getBroadcast(context,200,Intent(context,ReminderReceiver::class.java),
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE)
        if(pending!=null){alarm.cancel(pending);pending.cancel()}
        context.getSharedPreferences("stillmind_profile", Context.MODE_PRIVATE).edit().putBoolean("reminder_enabled",false).apply()
    }

}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= 26) manager.createNotificationChannel(NotificationChannel(ReminderScheduler.CHANNEL, "Daily meditation", NotificationManager.IMPORTANCE_DEFAULT))
        val open = PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val notification = Notification.Builder(context, ReminderScheduler.CHANNEL).setSmallIcon(android.R.drawable.ic_lock_idle_alarm).setContentTitle("စိတ် - Mind · Meditation time").setContentText("A mindful moment is waiting for you.").setAutoCancel(true).setContentIntent(open).build()
        try { manager.notify(200, notification) } catch (_: SecurityException) {}
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED && context.getSharedPreferences("stillmind_profile", Context.MODE_PRIVATE).getBoolean("reminder_enabled", false)) ReminderScheduler.schedule(context)
    }
}
