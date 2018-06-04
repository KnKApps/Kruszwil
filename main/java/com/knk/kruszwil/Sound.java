package com.knk.kruszwil;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.facebook.messenger.MessengerUtils;
import com.facebook.messenger.ShareToMessengerParams;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;



public class Sound {
    //Id of the mp3 file
    private int soundId;
    private Button button;
    private String caption;
    private MediaPlayer mp;
    //Attributes specific for the standard version of the app
    private boolean isUnlocked;
    private boolean isPremiumOnly = false;

    public Sound(int soundId, Button button, String caption, MediaPlayer mp) {
        this.soundId = soundId;
        this.button = button;
        this.caption = caption;
        this.mp = mp;
    }


    //Guess what it does
    public void play(Context context) {
        if (mp != null) {
            mp.release();
            mp = null;
        }
        mp = MediaPlayer.create(context, this.soundId);
        mp.start();
    }

    public void setPremiumOnly(boolean premiumOnly){
        this.isPremiumOnly = premiumOnly;
    }

    public void setUnlocked(boolean unlocked){
        this.isUnlocked = unlocked;
    }

    public boolean isPremiumOnly() {
        return isPremiumOnly;
    }

    public String getCaption() {
        return caption;
    }

    public int getSoundId() {
        return soundId;
    }

    public Button getButton() {
        return button;
    }

    public boolean isUnlocked(){
        return isUnlocked;
    }

    //Saves a mp3 file in a directory created by getSaveDir()
    public File download(Context context, File dir) throws IOException {
        InputStream is = null;
        FileOutputStream fos = null;
        File file = null;

        try {
            file = new File(dir, (String) button.getText() + ".mp3");
            if (!file.exists() && !file.isDirectory()) {
                file.createNewFile();
            }

            is = context.getResources().openRawResource(this.soundId);
            fos = new FileOutputStream(file);


            byte[] buffer = new byte[1024];
            int count;

            while ((count = is.read(buffer, 0, 1024)) != -1) {
                fos.write(buffer, 0, count);
            }

            //Toast.makeText(context, "Utworzono plik \n" + file.getAbsolutePath(), Toast.LENGTH_LONG).show();


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                is.close();
            }
            if (fos != null) {
                fos.close();
            }

            return file;
        }
    }





    //Sends a sound message via Messenger
    public void send(Activity activity, Context context, File dir) {
        File file;
        try {
            file = download(context, dir);
            Uri uri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".com.knk.kruszwilprestiz.provider", file);
            ShareToMessengerParams shareToMessengerParams = ShareToMessengerParams.newBuilder(uri, "audio/mpeg").build();
            MessengerUtils.shareToMessenger(activity, 321, shareToMessengerParams);
            file.delete();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Sets a sound as default ringtone, notification or alarm sounds
    public void setAsNotification(Activity activity, Context context, File dir) {

        File file = null;
        try {
            //Prevents directory cluttering, deletes unused sounds and saves new ones
              deleteFiles(dir);
              file = download(context, dir);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Creates a map of values necessary for sound files to be set
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DATA, file.getAbsolutePath());
        values.put(MediaStore.MediaColumns.SIZE, file.length());
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, file.getName());
        values.put(MediaStore.MediaColumns.TITLE,file.getName());
        values.put(MediaStore.MediaColumns.MIME_TYPE,"audio/mp3");
        values.put(MediaStore.Audio.Media.ARTIST, "Kruszwil");
        values.put(MediaStore.Audio.Media.IS_MUSIC, false);
        values.put(MediaStore.Audio.Media.IS_RINGTONE, false);
        values.put(MediaStore.Audio.Media.IS_NOTIFICATION, true);
        values.put(MediaStore.Audio.Media.IS_ALARM, false);

        //Sets sound as selected, double URIs for preventing errors

                Uri notificationUri = MediaStore.Audio.Media.getContentUriForPath(file.getAbsolutePath());

                Uri newNotificationUri = context.getContentResolver().insert(notificationUri, values);

                if(newNotificationUri == null){
                    context.getContentResolver().delete(notificationUri,null,null);
                    newNotificationUri = context.getContentResolver().insert(notificationUri, values);
                }
                RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_NOTIFICATION, newNotificationUri);
                Toast.makeText(context, "Ustawiono dźwięk powiadomień", Toast.LENGTH_LONG).show();
    }

    //Deletes all notification sound files
    private void deleteFiles(File dir) {
        for (File file : dir.listFiles()) {
            if(file.getName().contains("notification.mp3")) {
                file.delete();
            }
        }
    }

    //In loving memory of setKludeczka() [*]
    public void setPadluck(final Context context) {
        this.isPremiumOnly = true;
        this.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("DŹWIĘK DOSTĘPNY TYLKO W WERSJI PRESTIŻOWEJ");
                builder.setMessage("Aby odblokować dźwięk:\n"+caption+"\nPrzejdź na wersję prestiżową!");
                builder.setPositiveButton("PRZEJDŹ DO GOOGLE PLAY", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse("market://details?id=com.knk.kruszwilprestiz"));
                        context.startActivity(intent);
                        dialog.dismiss();
                    }
                });

                builder.setNegativeButton(":(", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }

        });

            this.button.setBackgroundResource(R.drawable.goldbuttonlocked);

    }


}
