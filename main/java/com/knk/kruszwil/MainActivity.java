package com.knk.kruszwil;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    //Map associating buttons and sounds, core of app
    private Map<View, Sound> soundMap =  new HashMap<>();
    private MediaPlayer mp;
    //Directory for app sounds
    private File fileSaveDir;
    //SharedPreferences to save state of an app
    public static SharedPreferences sharedPreferences;
    //Editor for SharedPreferences
    public static SharedPreferences.Editor editor;
    //Ads!
    private RewardedVideoAd mRewardedAd;
    private AdView mAdView;

    //Attributes specific for the standard version of the app
    public Sound toUnlock;
    public boolean wasAdActivated = false;

    private FirebaseAnalytics firebaseAnalytics;

    Intent serviceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAnalytics = FirebaseAnalytics.getInstance(this);


        //Navigation bar for Lollipop-and-above users
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(getResources().getColor(R.color.colorNavBar));
        }

        //Initialize ads and override all the methods required
        MobileAds.initialize(this, "ca-app-pub-8349688339545762~2580413300");
        mRewardedAd = MobileAds.getRewardedVideoAdInstance(this);
        mRewardedAd.setRewardedVideoAdListener(new RewardedVideoAdListener() {
            @Override
            public void onRewardedVideoAdLoaded() {
                if(wasAdActivated){
                    mRewardedAd.show();
                    wasAdActivated=false;
                }
            }

            @Override
            public void onRewardedVideoAdOpened() {

            }

            @Override
            public void onRewardedVideoStarted() {

            }

            @Override
            public void onRewardedVideoAdClosed() {
                wasAdActivated=false;
                loadRewardedVideoAd();
            }

            @Override
            public void onRewarded(RewardItem rewardItem) {
                toUnlock.setUnlocked(true);
                editor.putBoolean(getString(toUnlock.getSoundId()),true);
                editor.commit();

                toUnlock.getButton().setBackgroundResource(R.drawable.button_bg);
                toUnlock.getButton().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        buttonOnClick(view);
                    }
                });

                toUnlock.getButton().setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        createMenu(v);
                        return true;
                    }
                });
                wasAdActivated=false;
                //loadRewardedVideoAd();
            }

            @Override
            public void onRewardedVideoAdLeftApplication() {
                loadRewardedVideoAd();
            }

            @Override
            public void onRewardedVideoAdFailedToLoad(int i) {
                loadRewardedVideoAd();
            }

            @Override
            public void onRewardedVideoCompleted() {

                }
        });
        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);



        loadRewardedVideoAd();
        sharedPreferences = getPreferences(getApplicationContext().MODE_PRIVATE);
        editor = sharedPreferences.edit();

        //Create directory for app sounds
        getFileSaveDir();

        //Delete temporary files
        if(fileSaveDir != null) Sound.deleteFiles(fileSaveDir);



        //Associate buttons with sounds
        addSound(R.id.kupujetensyf, R.raw.kupujetensyf, "Kupuję ten syf, żeby Janusze dostali zawału.");
        addSound(R.id.wsadzrolexa, R.raw.wsadzrolexa,  "Wsadź w dupę Rolexa");
        addSound(R.id.bylynajdrozsze, R.raw.bylynajdrozsze,"Były najdroższe, dlatego je wziąłem");
        addSound(R.id.ekskluzywnewidowisko, R.raw.ekskluzywnewidowisko,  "To już czas na ekskluzywne widowisko");
        addSound(R.id.niczymlord, R.raw.niczymlord,  "Niczym lord");
        addSound(R.id.prestizowo, R.raw.prestizowo,  "Prestiżowo");
        addSound(R.id.zakladamRolexa, R.raw.zakladamroleksa,  "Zakładam Rolexa i wyrywam następną");
        addSound(R.id.szacunek, R.raw.szacunek,  "Nie mając szacunku do pieniędzy nie masz szacunku do samego siebie");
        addSound(R.id.parkowacsamochod, R.raw.parkowacsamochod,  "Samochód należy parkować w takim miejscu, aby każdy zobaczył mój prestiż");
        addSound(R.id.jestemmarek, R.raw.jestemmarek,  "Siema, jestem Marek, mam 16 lat");
        addSound(R.id.prostytutka, R.raw.prostytutka,  "Prostytutka");
        addSound(R.id.blachara, R.raw.blachara,  "Blachara");
        addSound(R.id.donperignon, R.raw.donperignon1, "Don Perignon");
        addSound(R.id.cotyzrobiles, R.raw.cotyzrobiles, "Co Ty zrobiłeś ?!");
        addSound(R.id.rolexodmierzaczas, R.raw.rolexodmierzaczas,  "Mój Rolex odmierza czas na odpoczynek");
        addSound(R.id.lustro, R.raw.lustro,  "Lustra używam do oglądania mojego Rolexa z dwóch stron");
        addSound(R.id.sluzacychodz, R.raw.sluzacychodz, "Służacy, chodź");


        addSound(R.id.zapomnialesoczyms, R.raw.zapomnialesoczyms, "Zapomniałeś o czymś czy nie !?", false);
        addSound(R.id.stosunek, R.raw.stosunek,  "Codziennie odbywam stosunek seksualny z co najmniej jedną dziewczyną", false);
        addSound(R.id.zasmiecackonta, R.raw.zasmiecackonta,  "Nie zamierzam nawet zaśmiecać tym sobie konta", false);
        addSound(R.id.ledwo300, R.raw.ledwo300,  "Trochę mało prestiżowy, ledwo 300 zł kosztuje", false);
        addSound(R.id.przystepnacena, R.raw.przystepnacena,  "Cena jak na kalkulator jest bardzo przystępna, bo kosztuje zaledwie półtora tysiąca złotych", false);
        addSound(R.id.wiekliczba, R.raw.wiekliczba,  "Wiek to tylko liczba", false);
        addSound(R.id.jezykiobce, R.raw.jezykiobce, "Jeżeli nie znacie języków obcych, to wasz problem", false);
        addSound(R.id.mocneslowa, R.raw.mocneslowa, "Muszę przyznać, że to dosyć mocne słowa jak na dwunastoletnią dziewczynkę", false);
        addSound(R.id.ocochodzi, R.raw.ocochodzi, "Ej o co Ci chodzi ej ziom", false);
        addSound(R.id.tanutabuja, R.raw.tanutabuja, "Ta nuta mną buja", false);


        addSound(R.id.wartoscczlowieka, R.raw.wartoscczlowieka, "Wartość człowieka liczy się w dolarach amerykańskich");
        addSound(R.id.obrzydliwe, R.raw.obrzydliwe, "Jest to obrzydliwe i na samą myśl robi mi się niedobrze");
        addSound(R.id.fekalia, R.raw.fekalia, "Moje fekalia są warte około 200zł");



        //Set Premium Only Sounds
        soundMap.get(findViewById(R.id.mocneslowa)).setPadluck(MainActivity.this);
        soundMap.get(findViewById(R.id.wsadzrolexa)).setPadluck(MainActivity.this);
        soundMap.get(findViewById(R.id.wartoscczlowieka)).setPadluck(MainActivity.this);
        soundMap.get(findViewById(R.id.obrzydliwe)).setPadluck(MainActivity.this);
        soundMap.get(findViewById(R.id.fekalia)).setPadluck(MainActivity.this);


        //Updater
        serviceIntent = new Intent(this, UpdateService.class);
        if (!isMyServiceRunning(UpdateService.class)) startService(serviceIntent);
    }

    //Checks whether service is running
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i ("isMyServiceRunning?", true+"");
                return true;
            }
        }
        Log.i ("isMyServiceRunning?", false+"");
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Delete temporary files
        if(fileSaveDir != null) Sound.deleteFiles(fileSaveDir);
    }

    //Associates button with sound
    private void addSound(int buttonId, int soundId, String caption) {
        Button button = findViewById(buttonId);
        Sound sound = new Sound(soundId, button, caption, this.mp);
        soundMap.put(button, sound);


        button.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                createMenu(v);
                return true;
            }
        });
    }

    @Override
    protected void onDestroy() {
        stopService(serviceIntent);
        super.onDestroy();
    }

    //The addSound() version for locked sounds
    private void addSound(int buttonId, int soundId, String caption, boolean active) {
        Button button = findViewById(buttonId);
        final Sound sound = new Sound(soundId, button, caption, this.mp);
        soundMap.put(button, sound);

        boolean isActive = sharedPreferences.getBoolean(getString(sound.getSoundId()),active);
        Log.i("adsy",getString(sound.getSoundId()));
        sound.setUnlocked(isActive);

        if(!sound.isUnlocked()) {
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i("adsy", "" + sharedPreferences.getBoolean(Integer.toString(soundMap.get(v).getSoundId()),true));
                    toUnlock = sound;
                    createUnlockDialog(v);

                }
            });
            button.setBackgroundResource(R.drawable.inactivebutton_bg);
        } else {
            button.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    createMenu(v);
                    return true;
                }
            });
        }
    }

    //Creates dialog for locked buttons
    public void createUnlockDialog(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if(!soundMap.get(view).isPremiumOnly()){
            builder.setMessage("Czy chcesz objerzeć reklamę, by odblokować dźwięk: " +
                    "\n\n" + soundMap.get(view).getCaption());

        builder.setPositiveButton("tak", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mRewardedAd.isLoaded()) {
                    mRewardedAd.show();
                } else {
                    Toast.makeText(getApplicationContext(),
                            "poczekaj na wczytanie reklamy",
                            Toast.LENGTH_LONG).show();
                }
                dialog.dismiss();
                wasAdActivated = true;
            }
        });
            builder.setNegativeButton("nie", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {

                    dialog.dismiss();
                }
            });
        }
        AlertDialog alertDialog = builder.create();
        alertDialog.show();


    }

    //Plays a sound, duh
    public void buttonOnClick(View view) {
        soundMap.get(view).play(this);
    }

    //Creates a menu on long click
    private void createMenu(final View view) {
        CharSequence[] options = {"WYŚLIJ MESSENGEREM", "USTAW JAKO POWIADOMIENIE"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Wybierz opcję:");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch(which) {
                    case 0:
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                            // Check whether has the write settings permission or not.
                            boolean settingsCanWrite = Settings.System.canWrite(getApplicationContext());

                            if(!settingsCanWrite) {
                                // If do not have write settings permission, reload activity
                                MainActivity.this.finish();
                                startActivity(getIntent());
                                break;

                            }
                        }
                        soundMap.get(view).send(MainActivity.this, getApplicationContext(), fileSaveDir);
                        break;
                    case 1:
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                            // Check whether has the write settings permission or not.
                            boolean settingsCanWrite = Settings.System.canWrite(getApplicationContext());

                            if(!settingsCanWrite) {
                                // If do not have write settings permission, reload activity
                                MainActivity.this.finish();
                                startActivity(getIntent());
                                break;

                            }
                        }
                        soundMap.get(view).setAsNotification(MainActivity.this, getApplicationContext(), fileSaveDir);
                        break;
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    //Gets permissions to write and read files and creates a directory for app sounds
    private void getFileSaveDir() {
        //A flag to check whether permission is granted
        boolean isK = true;
        String[] permissions = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CHANGE_CONFIGURATION,
                Manifest.permission.WRITE_SETTINGS,
                Manifest.permission.MODIFY_AUDIO_SETTINGS
        };

        //If permission not granted, change flag to false and break
        for(String s: permissions) {
            if(ContextCompat.checkSelfPermission(getApplicationContext(),s)!= PackageManager.PERMISSION_GRANTED) {
                isK = false;
                break;
            }
        }

        //If any permission not granted, request them
        if(isK) {
            getSaveDir();
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, permissions, 123);
        }
    }


    //Creates directory for app sounds
    private void getSaveDir() {
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), "Kruszwil Soundboard");
        if(!file.mkdirs()) {
            Log.i("halko", "Directory not created :c");
        }

        fileSaveDir = file;
    }


    //Creates directory on permission grant
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 123:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getSaveDir();
                    //Check permissions :)
                    checkPermissions(this);
                } else {
                    Toast.makeText(this,
                            "Aby móc pobierać dźwięki, wysyłać je, oraz ustawiać jako dźwięk powiadomień lub dzwonka, musisz udzielić aplikacji odpowiednich uprawnień",
                            Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    //Loads a video add to unlock a button
    private void loadRewardedVideoAd() {
        mRewardedAd.loadAd("ca-app-pub-8349688339545762/3873136064", new AdRequest.Builder().build());
    }

    //Checks necessary permissions
    private void checkPermissions(Context context) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            // Check whether has the write settings permission or not.
            boolean settingsCanWrite = Settings.System.canWrite(context);

            if(!settingsCanWrite) {
                // If do not have write settings permission then open the Can modify system settings panel.
                createPermissionsDialog(context);

            }
        }
    }

    //Create a dialog to enter settings
    private void createPermissionsDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Uprawnienia");
        builder.setMessage("Nadaj aplikacji niezbędne uprawnienia, aby móc korzystać z jej wszystkich funkcji.");
        builder.setPositiveButton("PRZEJDŹ DO USTAWIEŃ", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                startActivity(intent);
            }
        });


        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void createAppInfoDialog(View view) {
        PackageInfo packageInfo = null;

        try {
            packageInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Informacje o aplikacji");
        builder.setMessage("Wersja: "+packageInfo.versionName+"\nKliknij DODAJ DŹWIĘK, aby przejść do formularza i dodać własną propozycję nowego, prestiżowego dźwięku!");
        builder.setPositiveButton("ZAMKNIJ", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("DODAJ DŹWIĘK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://kruszwil.000webhostapp.com/")));
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    
    //Shows only on first launch
    private void createFirstLaunchDialog(Context context){
        boolean isFirstLaunch = sharedPreferences.getBoolean("isFirstLaunch", true);
        if (isFirstLaunch) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Witaj w Kruszwil Soundboard!");
            builder.setMessage("Dziękujemy za pobranie! Wciśnij przycisk by odtworzyć dźwięk," +
                    " przytrzymaj by skorzystać z dodatkowych funkcji. Miłej zabawy! :)");
            builder.setPositiveButton("OKEJ!", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            AlertDialog alertDialog = builder.create();
            alertDialog.show();
            isFirstLaunch = false;
            editor.putBoolean("isFirstLaunch", isFirstLaunch);
            editor.commit();
        }

    }
}
