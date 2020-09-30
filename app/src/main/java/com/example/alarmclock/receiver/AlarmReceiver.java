package com.example.alarmclock.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;

import com.example.alarmclock.service.PlayRingtoneService;

import java.util.ArrayList;
import java.util.List;

public class AlarmReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {

        // Get time info from setting the alarm
        int ID = intent.getIntExtra("ID", 1);
        int hour = intent.getIntExtra("hour", 0);
        int min = intent.getIntExtra("min", 0);



        // Put time info to StartAlarmActivity class
        Intent wakeIntent = new Intent(context, PlayRingtoneService.class);
        wakeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        wakeIntent.putExtra("hour", hour);
        wakeIntent.putExtra("minute", min);
        context.startService(wakeIntent);

    }



    // Create a list of local RingTone
    public List<Uri> loadLocalRingtoneUris(Context context) {
        List<Uri> alarms = new ArrayList<>();
        try {
            RingtoneManager ringtoneMgr = new RingtoneManager(context);
            ringtoneMgr.setType(RingtoneManager.TYPE_ALARM);
            Cursor alarmsCursor = ringtoneMgr.getCursor();
            int alarmsCount = alarmsCursor.getCount();
            if (alarmsCount == 0 && !alarmsCursor.moveToFirst()) {
                alarmsCursor.close();
                return null;
            }
            while (!alarmsCursor.isAfterLast() && alarmsCursor.moveToNext()) {
                int currentPosition = alarmsCursor.getPosition();
                alarms.add(ringtoneMgr.getRingtoneUri(currentPosition));
            }
        } catch(Exception ex){
                ex.printStackTrace();
            }

        return alarms;
    }


}
