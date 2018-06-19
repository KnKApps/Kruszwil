package com.knk.kruszwil;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;


import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import co.infinum.princeofversions.LoaderFactory;
import co.infinum.princeofversions.PrinceOfVersions;
import co.infinum.princeofversions.UpdaterResult;
import co.infinum.princeofversions.callbacks.UpdaterCallback;
import co.infinum.princeofversions.loaders.factories.NetworkLoaderFactory;

public class UpdateService extends Service {


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final String factoryUrl = getString(R.string.factory_url);
        final String googlePlayUrl = getString(R.string.google_play_url);

        final PrinceOfVersions updater = new PrinceOfVersions(this);
        final LoaderFactory factory = new NetworkLoaderFactory(factoryUrl);
        final UpdaterCallback callback = new UpdaterCallback() {
            @Override
            public void onNewUpdate(String version, boolean isMandatory, Map<String, String> metadata) {

                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(googlePlayUrl));
                PendingIntent pendingIntent = PendingIntent.getActivity(UpdateService.this,0, i, 0);
                NotificationManager notificationManager = (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);


                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    int importance = NotificationManager.IMPORTANCE_HIGH;
                    NotificationChannel notificationChannel = new NotificationChannel("0", "channel", importance);
                    notificationChannel.enableLights(true);
                    notificationChannel.setLightColor(Color.RED);
                    notificationChannel.enableVibration(true);
                    notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                    notificationManager.createNotificationChannel(notificationChannel);
                }

                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(UpdateService.this, "0")
                        .setSmallIcon(R.drawable.update_icon)
                        .setContentTitle("Nowa wersja!")
                        .setContentText("Nowa wersja aplikacji jest gotowa do pobrania")
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .setSound(Uri.parse("android.resource://com.knk.kruszwil/" + R.raw.zapomnialesoczyms))
                        .addAction(R.drawable.update_icon, "AKTUALIZUJ", pendingIntent);


                notificationManager.notify((int)(System.currentTimeMillis()/1000), mBuilder.build());

            }

            @Override
            public void onNoUpdate(Map<String, String> metadata) {

            }

            @Override
            public void onError(int error) {

            }
        };
            Timer timer = new Timer();
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    UpdaterResult result = updater.checkForUpdates(factory, callback);

                }
            };


            timer.schedule(timerTask,0,10000);


        Log.i("SERVICE", "started");

        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent broadcastIntent = new Intent("com.knk.kruszwil.ActivityRecognition.RestartService");
        sendBroadcast(broadcastIntent);
        Log.i("SERVICE", "dieded");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
