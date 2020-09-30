package com.example.alarmclock.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.example.alarmclock.receiver.AlarmReceiver;

import java.io.IOException;
import java.util.List;

public class PlayRingtoneService extends Service {

    private MediaPlayer mediaPlayer;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // Get Ringtone list
        AlarmReceiver alarmReceiver = new AlarmReceiver();
        List<Uri> ringtone = alarmReceiver.loadLocalRingtoneUris(this);

        // Start a ringtone from Ringtone list
        mediaPlayer = new MediaPlayer();

        try {
            mediaPlayer.setDataSource(this, ringtone.get(3));
            mediaPlayer.prepare();
            mediaPlayer.setLooping(true);
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop play the ringtone
        mediaPlayer.stop();
    }
}
