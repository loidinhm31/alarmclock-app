package com.example.alarmclock.model;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.example.alarmclock.receiver.AlarmReceiver;
import com.example.alarmclock.service.PlayRingtoneService;

public class AlarmModel {
    private int itemID;
    private int hour;
    private int minute;
    private int isEnable;


    public AlarmModel() {

    }

    public AlarmModel(int itemID, int hour, int minute, int isEnable) {
        this.itemID = itemID;
        this.hour = hour;
        this.minute = minute;
        this.isEnable = isEnable;
    }


    public int getItemID() {return itemID;}

    public void setItemID(int itemID) {
        this.itemID = itemID;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public int isEnable() {
        return isEnable;
    }

    public void setEnable(int isEnable) {
        this.isEnable = isEnable;
    }



    public void cancelAlarm(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(context, itemID, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        // If the alarm has been set, cancel it.
        if(alarmPendingIntent != null) {
            alarmManager.cancel(alarmPendingIntent);

            // Stop play ringtone from Service
            context.stopService(new Intent(context, PlayRingtoneService.class));

            // Make Toast to notify change
            Toast.makeText(context, String.format("Alarm %d has been cancelled", itemID), Toast.LENGTH_SHORT).show();
        }
    }



}
