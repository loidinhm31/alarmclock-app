package com.example.alarmclock.clockactivity;



import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TimePicker;


import androidx.annotation.RequiresApi;
import androidx.fragment.app.DialogFragment;

import com.example.alarmclock.database.AlarmDbHelper;
import com.example.alarmclock.model.AlarmModel;
import com.example.alarmclock.receiver.AlarmReceiver;

import java.util.Calendar;

public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;

    // Initialize variable to checking create new alarm or edit alarm
    private boolean isNew;

    public TimePickerFragment(boolean isNew) {
        this.isNew = isNew;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Use the current time as the default values for the picker
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        // Checking condition to implement interface
        if (isNew) {
            listener.onTimeSet(view, hourOfDay, minute);
        } else {
            listener.onTimeEdit(view, hourOfDay, minute);
        }
    }


    /***********************************************************************************************
     Sending events back to the activity with callback interface
     **********************************************************************************************/

    public interface NoticeDialogListener {
        void onTimeSet(TimePicker view, int hourOfDay, int minute);

        void onTimeEdit(TimePicker view, int hourOfDay, int minute);
    }

    // Use this instance of the interface to deliver action events
    NoticeDialogListener listener;

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = (NoticeDialogListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(context.toString()
                    + " must implement NoticeDialogListener");
        }
    }












    // Add alarm to Database
    private long addAlarmToDb(int hourOfDay, int minute) {



        // Create Alarm Object to add
        AlarmModel alarmModel = new AlarmModel();

        // Setting hour, minute
        alarmModel.setHour(hourOfDay);
        alarmModel.setMinute(minute);
        alarmModel.setEnable(1); // set default value is true (1) for switch



        // Call SQLite Db
        AlarmDbHelper sqLiteDb = new AlarmDbHelper(getContext());

        long newRowId = sqLiteDb.addAlarm(alarmModel);

        sqLiteDb.close();

        return newRowId;

    }




    @RequiresApi(api = Build.VERSION_CODES.M)
    private void setAlarm(Context context, int _ID, int hourOfDay, int minute) {

        alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, AlarmReceiver.class);

        // for test
        intent.putExtra("ID", _ID);
        intent.putExtra("hour", hourOfDay);
        intent.putExtra("min", minute);

        alarmIntent = PendingIntent.getBroadcast(context, _ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);



        // Set the alarm to start at...
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);


        // With setInexactRepeating(), you have to use one of the AlarmManager interval
        // constants--in this case, AlarmManager.INTERVAL_DAY.
        alarmMgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                alarmIntent);
    }




}