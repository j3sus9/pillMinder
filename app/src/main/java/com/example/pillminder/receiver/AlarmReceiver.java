package com.example.pillminder.receiver;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.example.pillminder.R;
import com.example.pillminder.view.MainActivity;
import android.app.AlarmManager;
import android.util.Log;

import java.util.Calendar;

public class AlarmReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "pillminder_channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        // 1. Recoger datos
        String medNombre = intent.getStringExtra("medicamento_nombre");
        String medId = intent.getStringExtra("medicamento_id");
        int hour = intent.getIntExtra("hora_original", -1);
        int minute = intent.getIntExtra("minuto_original", -1);

        // LOG DE DEBUG (Para ver que funciona)
        android.util.Log.d("ALARM_DEBUG", "¡Alarma recibida! Reprogramando para mañana...");

        // 2. MOSTRAR NOTIFICACIÓN
        createNotificationChannel(context);
        int notificationId = (medId != null) ? medId.hashCode() : (int) System.currentTimeMillis();

        Intent mainIntent = new Intent(context, MainActivity.class);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntentClick = PendingIntent.getActivity(context, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm) // Usamos el icono seguro del sistema
                .setContentTitle("Hora de tu medicamento")
                .setContentText("Es hora de tomar tu " + medNombre)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntentClick)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notificationId, builder.build());

        // 3. REPROGRAMAR PARA MAÑANA
        if (medId != null && hour != -1 && minute != -1) {
            programarSiguienteAlarma(context, medNombre, medId, hour, minute);
        }
    }

    private void programarSiguienteAlarma(Context context, String nombre, String id, int hour, int minute) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent newIntent = new Intent(context, AlarmReceiver.class);
        newIntent.putExtra("medicamento_nombre", nombre);
        newIntent.putExtra("medicamento_id", id);
        newIntent.putExtra("hora_original", hour);
        newIntent.putExtra("minuto_original", minute);

        String horaStr = String.format(java.util.Locale.getDefault(), "%02d:%02d", hour, minute);
        int requestCode = (id + horaStr).hashCode();

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, newIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        calendar.add(Calendar.DATE, 1);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            // Verificamos permisos de alarma exacta (buena práctica en Android 12+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                    Log.d("ALARM_DEBUG", "Alarma reprogramada para: " + calendar.getTime());
                } else {
                    // Si perdemos el permiso, usamos setExact normal (mejor que nada)
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                }
            }
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "PillMinder Channel";
            String description = "Channel for PillMinder reminders";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
