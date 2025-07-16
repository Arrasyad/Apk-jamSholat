package com.example.jamsholat

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Hanya jalankan jika perangkat baru saja selesai booting
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Gunakan nama SharedPreferences yang sama dengan di MainActivity
            val prefs = context.getSharedPreferences("alarms", Context.MODE_PRIVATE)
            val allAlarms = prefs.all

            val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            // Loop melalui semua alarm yang tersimpan
            allAlarms.forEach { (prayerName, triggerValue) ->
                val triggerAtMillis = triggerValue as? Long ?: -1L

                // Hanya jadwalkan ulang alarm yang waktunya belum lewat
                if (triggerAtMillis > System.currentTimeMillis()) {
                    val alarmIntent = Intent(context, AlarmReceiver::class.java).apply {
                        putExtra("PRAYER_NAME", prayerName)
                    }

                    val pendingIntent = PendingIntent.getBroadcast(
                        context,
                        prayerName.hashCode(), // Gunakan ID unik untuk setiap alarm
                        alarmIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    // PERBAIKAN DI SINI: Cek versi Android sebelum memanggil API alarm
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmMgr.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerAtMillis,
                            pendingIntent
                        )
                    } else {
                        alarmMgr.setExact(
                            AlarmManager.RTC_WAKEUP,
                            triggerAtMillis,
                            pendingIntent
                        )
                    }
                }
            }
        }
    }
}
