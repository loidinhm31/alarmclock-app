package com.example.alarmclock;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.alarmclock.adapter.AlarmRecyclerAdapter;
import com.example.alarmclock.receiver.AlarmReceiver;
import com.example.alarmclock.clockactivity.TimePickerFragment;
import com.example.alarmclock.database.AlarmDbHelper;
import com.example.alarmclock.model.AlarmModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;


public class MainActivity extends AppCompatActivity
        implements TimePickerFragment.NoticeDialogListener,
        AlarmRecyclerAdapter.OnItemLongClickListener{


    public ArrayList<AlarmModel> alarmList = new ArrayList<>();


    public AlarmRecyclerAdapter mAdapter;

    private FloatingActionButton fab;


    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;

    // Temporary position to edit an alarm
    private int itemPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeAdapter();

        fab = findViewById(R.id.listFragment_FloatingAddAlarmButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {

                showTimePickerDialog(view);

            }
        });


    }


    public void showTimePickerDialog(View v) {

        DialogFragment timePickerFragment = new TimePickerFragment(true);
        timePickerFragment.show(getSupportFragmentManager(), "timePicker");
    }


    public void initializeAdapter() {
        alarmList.clear();

        AlarmDbHelper sqLiteDb = new AlarmDbHelper(this);

        alarmList = sqLiteDb.getAllAlarms();

        sqLiteDb.close();


        RecyclerView alarmRecycler = findViewById(R.id.listFragment_recyclerview);

        // Changes in content do not change the layout size of the RecyclerView
        alarmRecycler.setHasFixedSize(true);


        // Use a linear layout manager
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        alarmRecycler.setLayoutManager(layoutManager);

        // Add Decoration
        alarmRecycler.setItemAnimator(new DefaultItemAnimator());
        alarmRecycler.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

        // Set adapter
        mAdapter = new AlarmRecyclerAdapter(this, alarmList);
        mAdapter.setOnItemLongClickListener(this);
        alarmRecycler.setAdapter(mAdapter);


    }



    private void addNewAlarm(int hourOfDay, int minute) {
        // Add new alarm into database and get Id of this alarm
        int newRowId = (int) addAlarmToDb(hourOfDay, minute);

        // Create Alarm Model to add
        AlarmModel alarmModel = new AlarmModel();
        alarmModel.setItemID(newRowId);
        alarmModel.setHour(hourOfDay);
        alarmModel.setMinute(minute);
        alarmModel.setEnable(0); // set default value is false(0) for switch

        // Add new alarm to list
        alarmList.add(alarmModel);
        mAdapter.notifyDataSetChanged();

    }


    // Set on Alarm Manager
    public void setAlarm(Context context, int _ID, int hourOfDay, int minute) {

        alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, AlarmReceiver.class);

        // Put time info to AlarmReceiver class
        intent.putExtra("ID", _ID);
        intent.putExtra("hour", hourOfDay);
        intent.putExtra("min", minute);

        alarmIntent = PendingIntent.getBroadcast(context, _ID, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        // Set an alarm to start at...
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);

        // Set an alarm
        setExactAndAllowWhileIdle(alarmMgr, AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmIntent);
    }


    // Creating exact alarms on all Android versions
    public static void setExactAndAllowWhileIdle(AlarmManager alarmManager, int type, long
            triggerAtMillis, PendingIntent operation) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            // Skip doze mode
            alarmManager.setExactAndAllowWhileIdle(type, triggerAtMillis, operation);
        } else if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(type, triggerAtMillis, operation);
        } else {
            alarmManager.set(type, triggerAtMillis, operation);
        }
    }

    // Add alarm to Database
    private long addAlarmToDb(int hourOfDay, int minute) {
        // Create Alarm Object to add
        AlarmModel alarmModel = new AlarmModel();

        // Setting hour, minute
        alarmModel.setHour(hourOfDay);
        alarmModel.setMinute(minute);
        alarmModel.setEnable(0); // set default value is false(0) for switch

        // Call SQLite Db
        AlarmDbHelper sqLiteDb = new AlarmDbHelper(this);

        long newRowId = sqLiteDb.addAlarm(alarmModel);

        sqLiteDb.close();

        return newRowId;

    }

    // Implement select item for Long Click
    @Override
    public void onItemSelected(View view, int position) {
        // Show dialog TimePicker to edit
        DialogFragment timePickerFragment = new TimePickerFragment(false);
        timePickerFragment.show(getSupportFragmentManager(), "timeEdit");

        // Change temporary position of alarm on view adapter
        itemPosition = position;

    }

    // Set time for the alarm
    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        // Show info
        Toast.makeText(this, "Created new alarm", Toast.LENGTH_SHORT).show();
        // Add new alarm
        addNewAlarm(hourOfDay, minute);
    }

    // Edit time for the alarm
    @Override
    public void onTimeEdit(TimePicker view, int hourOfDay, int minute) {
        // Show info
        Toast.makeText(this, "Edited the alarm", Toast.LENGTH_SHORT).show();

        AlarmDbHelper sqLiteDb = new AlarmDbHelper(this);
        sqLiteDb.updateAlarm(mAdapter.getItem(itemPosition).getItemID(),
                            hourOfDay,
                            minute,
                            mAdapter.getItem(itemPosition).isEnable());


        // Cancel old alarm
        mAdapter.getItem(itemPosition).cancelAlarm(getBaseContext());

        // Set state of the new alarm
        mAdapter.getItem(itemPosition).setHour(hourOfDay);
        mAdapter.getItem(itemPosition).setMinute(minute);
        mAdapter.getItem(itemPosition).setEnable(0); // set default value is false(0) for switch

        mAdapter.notifyDataSetChanged();
    }
}
