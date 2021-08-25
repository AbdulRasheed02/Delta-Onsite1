package com.example.deltaonsite1;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.InputFilter;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.lang.reflect.Field;
import java.sql.Time;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    int progressSeconds,totalSeconds;
    long START_TIME_IN_MILLIS,TIME_LEFT_IN_MILLIS,endTime;
    int hour,minute,second;
    CountDownTimer countDownTimer;
    boolean timerRunning;

    ProgressBar progressbar;
    TextView tv_progress;
    FloatingActionButton btn_start,btn_stop,btn_pause,btn_rewind,btn_forward,btn_reset;
    ExtendedFloatingActionButton btn_setTime;
    Context context;

    Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        this.setTitle("TIMER");

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.FOREGROUND_SERVICE}, PackageManager.PERMISSION_GRANTED);

        context=MainActivity.this;
        totalSeconds=0;
        progressSeconds=0;

        progressbar=findViewById(R.id.progressBar);
        tv_progress=findViewById(R.id.tv_progress);

        btn_start=findViewById(R.id.fab_start);
        btn_stop=findViewById(R.id.fab_stop);
        btn_pause=findViewById(R.id.fab_pause);
        btn_reset=findViewById(R.id.fab_reset);
        btn_forward=findViewById(R.id.fab_forward);
        btn_rewind=findViewById(R.id.fab_rewind);
        btn_setTime=findViewById(R.id.fab_setTime);

        progressbar.setProgress(0);
        tv_progress.setText(String.format(Locale.getDefault(),"%02d:%02d:%02d",00,00,00));

        btn_pause.setVisibility(View.INVISIBLE);
        btn_start.setVisibility(View.VISIBLE);



        btn_setTime.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                try {
                    setTimer();
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        });

        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(totalSeconds!=0) {
                    btn_start.setVisibility(View.INVISIBLE);
                    btn_pause.setVisibility(View.VISIBLE);
                    startTimer();
                    progressbar();
                    if (progressSeconds != 0 && timerRunning) {
                        Intent intentService = new Intent(MainActivity.this, myService.class);
                        Integer integerTimeSet = progressSeconds;
                        intentService.putExtra("TimeValue", integerTimeSet);
                        startService(intentService);
                    }
                }
            }
        });

        btn_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(timerRunning){
                    stopTimer();
                }
            }
        });

        btn_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_pause.setVisibility(View.INVISIBLE);
                btn_start.setVisibility(View.VISIBLE);
                pauseTimer();
            }
        });

        btn_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetTimer();
            }
        });

        btn_rewind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (timerRunning && TIME_LEFT_IN_MILLIS!=0) {
                    if (TIME_LEFT_IN_MILLIS - 10000 < 0) {
                        TIME_LEFT_IN_MILLIS = 0;
                        progressSeconds = (int) TIME_LEFT_IN_MILLIS / 1000;
                    } else {
                        TIME_LEFT_IN_MILLIS = TIME_LEFT_IN_MILLIS - 10000;
                        progressSeconds = (int) TIME_LEFT_IN_MILLIS / 1000;
                    }
                    if (timerRunning) {
                        stopService(new Intent(MainActivity.this, myService.class));
                        countDownTimer.cancel();
                        startTimer();
                        Intent intentService = new Intent(MainActivity.this, myService.class);
                        progressSeconds = (int) TIME_LEFT_IN_MILLIS / 1000;
                        Integer integerTimeSet = progressSeconds;
                        intentService.putExtra("TimeValue", integerTimeSet);
                        startService(intentService);
                    }
                    progressbar.setProgress(progressSeconds);
                    updateCountDownText(TIME_LEFT_IN_MILLIS);
                }
            }
        });

        btn_forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(timerRunning) {
                    if (TIME_LEFT_IN_MILLIS + 10000 > START_TIME_IN_MILLIS) {
                        TIME_LEFT_IN_MILLIS = START_TIME_IN_MILLIS;
                        progressSeconds = (int) TIME_LEFT_IN_MILLIS / 1000;
                    } else {
                        TIME_LEFT_IN_MILLIS = TIME_LEFT_IN_MILLIS + 10000;
                        progressSeconds = (int) TIME_LEFT_IN_MILLIS / 1000;
                    }
                    if (timerRunning) {
                        stopService(new Intent(MainActivity.this, myService.class));
                        countDownTimer.cancel();
                        startTimer();
                        Intent intentService = new Intent(MainActivity.this, myService.class);
                        progressSeconds = (int) TIME_LEFT_IN_MILLIS / 1000;
                        Integer integerTimeSet = progressSeconds;
                        intentService.putExtra("TimeValue", integerTimeSet);
                        startService(intentService);

                    }
                    progressSeconds = (int) TIME_LEFT_IN_MILLIS / 1000;
                    progressbar.setProgress(progressSeconds);
                    updateCountDownText(TIME_LEFT_IN_MILLIS);
                }
            }
        });

    }

    private void progressbar() {
        totalSeconds=(int)START_TIME_IN_MILLIS/1000;
        progressbar.setMax(totalSeconds);
    }


    private void startTimer() {
        endTime = System.currentTimeMillis() + TIME_LEFT_IN_MILLIS;
        countDownTimer=new CountDownTimer(TIME_LEFT_IN_MILLIS,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                progressSeconds--;
                progressbar.setProgress(progressSeconds);
                TIME_LEFT_IN_MILLIS=millisUntilFinished;
                updateCountDownText(TIME_LEFT_IN_MILLIS);
            }

            @Override
            public void onFinish() {
                timerRunning=false;
                btn_pause.setVisibility(View.INVISIBLE);
                btn_start.setVisibility(View.VISIBLE);
                stopService(new Intent(MainActivity.this, myService.class));
                if(alert == null){
                    alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

                    if(alert == null) {
                        alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
                    }
                }
                MediaPlayer mp = MediaPlayer.create(getApplicationContext(), alert);
                mp.start();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mp.stop();
                    }
                }, 2000);
            }
        }.start();
        timerRunning=true;
    }

    private void updateCountDownText(long TIME_LEFT_IN_MILLIS) {
        second = (int) (TIME_LEFT_IN_MILLIS / 1000) % 60;
        minute = (int) ((TIME_LEFT_IN_MILLIS / (1000 * 60)) % 60);
        hour = (int) ((TIME_LEFT_IN_MILLIS / (1000 * 60 * 60)) % 24);
        tv_progress.setText(String.format(Locale.getDefault(), "%02d:%02d:%02d", hour, minute, second));
    }

    private void pauseTimer() {
        if(timerRunning){
            stopService(new Intent(MainActivity.this, myService.class));
            countDownTimer.cancel();
        }
    }

    private void stopTimer() {
        progressbar.setProgress(0);
        if(timerRunning) {
            countDownTimer.cancel();
            stopService(new Intent(MainActivity.this, myService.class));
            timerRunning = false;
            btn_pause.setVisibility(View.INVISIBLE);
            btn_start.setVisibility(View.VISIBLE);
            START_TIME_IN_MILLIS=0;
            TIME_LEFT_IN_MILLIS=0;
            progressSeconds=0;
            progressbar();
            updateCountDownText(TIME_LEFT_IN_MILLIS);
        }
    }

    private void resetTimer() {
        if(timerRunning) {
            stopService(new Intent(MainActivity.this, myService.class));
            progressSeconds=totalSeconds;
            progressbar.setProgress(progressSeconds);
            progressbar();
            countDownTimer.cancel();
            TIME_LEFT_IN_MILLIS = START_TIME_IN_MILLIS;
            btn_pause.setVisibility(View.INVISIBLE);
            btn_start.setVisibility(View.VISIBLE);
            updateCountDownText(TIME_LEFT_IN_MILLIS);
            timerRunning=false;
        }
    }


    @SuppressLint("ResourceType")
    private void setTimer() throws NoSuchFieldException, IllegalAccessException {
        RelativeLayout linearLayout = new RelativeLayout(context);

        NumberPicker pickerHour = new NumberPicker(context);
        pickerHour.setFormatter(new NumberPicker.Formatter() {
            @Override
            public String format(int value) {
                return String.format("%02d"+"h",value);
            }
        });

        NumberPicker pickerMinute = new NumberPicker(context);
        pickerMinute.setFormatter(new NumberPicker.Formatter() {
            @Override
            public String format(int value) {
                return String.format("%02d"+"m",value);
            }
        });

        NumberPicker pickerSecond = new NumberPicker(context);
        pickerSecond.setFormatter(new NumberPicker.Formatter() {
            @Override
            public String format(int value) {
                return String.format("%02d"+"s",value);
            }
        });

        pickerHour.setMinValue(00);
        pickerHour.setMaxValue(24);
        pickerMinute.setMinValue(00);
        pickerMinute.setMaxValue(60);
        pickerMinute.setId(View.generateViewId());
        pickerSecond.setMinValue(00);
        pickerSecond.setMaxValue(60);

        Field f = NumberPicker.class.getDeclaredField("mInputText");
        f.setAccessible(true);
        EditText inputText = (EditText)f.get(pickerHour);
        EditText inputText2 = (EditText)f.get(pickerMinute);
        EditText inputText3 = (EditText)f.get(pickerSecond);
        inputText.setFilters(new InputFilter[0]);
        inputText2.setFilters(new InputFilter[0]);
        inputText3.setFilters(new InputFilter[0]);

        TextView tv1;
        tv1=new TextView(context);
        tv1.setText(" : ");
        tv1.setTextSize(30);
        tv1.setId(View.generateViewId());

        TextView tv2;
        tv2=new TextView(context);
        tv2.setText(" : ");
        tv2.setTextSize(30);
        tv2.setId(View.generateViewId());

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(50, 50);
        RelativeLayout.LayoutParams numPicerParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        numPicerParams.addRule(RelativeLayout.CENTER_HORIZONTAL);

        RelativeLayout.LayoutParams params4 = new RelativeLayout.LayoutParams(50, 50);
        RelativeLayout.LayoutParams numPicerParams4 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        numPicerParams4.addRule(RelativeLayout.LEFT_OF,pickerMinute.getId());
        numPicerParams4.addRule(RelativeLayout.CENTER_VERTICAL);

        RelativeLayout.LayoutParams params5 = new RelativeLayout.LayoutParams(50, 50);
        RelativeLayout.LayoutParams numPicerParams5 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        numPicerParams5.addRule(RelativeLayout.RIGHT_OF,pickerMinute.getId());
        numPicerParams5.addRule(RelativeLayout.CENTER_VERTICAL);

        RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(50, 50);
        RelativeLayout.LayoutParams numPicerParams2 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        numPicerParams2.addRule(RelativeLayout.LEFT_OF,tv1.getId());

        RelativeLayout.LayoutParams params3 = new RelativeLayout.LayoutParams(50, 50);
        RelativeLayout.LayoutParams numPicerParams3 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        numPicerParams3.addRule(RelativeLayout.RIGHT_OF,tv2.getId());


        linearLayout.setLayoutParams(params);
        linearLayout.addView(pickerMinute,numPicerParams);

        linearLayout.setLayoutParams(params2);
        linearLayout.addView(pickerHour,numPicerParams2);

        linearLayout.setLayoutParams(params3);
        linearLayout.addView(pickerSecond,numPicerParams3);

        linearLayout.setLayoutParams(params4);
        linearLayout.addView(tv1,numPicerParams4);

        linearLayout.setLayoutParams(params5);
        linearLayout.addView(tv2,numPicerParams5);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setView(linearLayout)
                .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {

                    hour=pickerHour.getValue();
                    minute=pickerMinute.getValue();
                    second=pickerSecond.getValue();
                    if(timerRunning) {
                        countDownTimer.cancel();
                        btn_pause.setVisibility(View.INVISIBLE);
                        btn_start.setVisibility(View.VISIBLE);
                    }
                    tv_progress.setText(String.format(Locale.getDefault(),"%02d:%02d:%02d",hour,minute,second));
                    START_TIME_IN_MILLIS=second*1000+minute*60000+hour*3600000;
                    TIME_LEFT_IN_MILLIS=START_TIME_IN_MILLIS;

                    totalSeconds=(int)START_TIME_IN_MILLIS/1000;
                    progressbar.setMax(totalSeconds);
                    progressSeconds=totalSeconds;
                    progressbar.setProgress(progressSeconds);
                })
                .setTitle("Select Duration")
                .setNegativeButton(android.R.string.cancel, null);
        alertDialogBuilder.show();
    }

    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong("millisStart", START_TIME_IN_MILLIS);
        editor.putLong("millisLeft", TIME_LEFT_IN_MILLIS);
        editor.putLong("endTime", endTime);
        editor.putBoolean("timerRunning", timerRunning);
        editor.apply();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        TIME_LEFT_IN_MILLIS = prefs.getLong("millisLeft", START_TIME_IN_MILLIS);
        START_TIME_IN_MILLIS= prefs.getLong("millisStart", START_TIME_IN_MILLIS);
        timerRunning = prefs.getBoolean("timerRunning", false);
        if (timerRunning) {
            btn_pause.setVisibility(View.VISIBLE);
            btn_start.setVisibility(View.INVISIBLE);
            endTime = prefs.getLong("endTime", 0);
            TIME_LEFT_IN_MILLIS = endTime - System.currentTimeMillis();
            if (TIME_LEFT_IN_MILLIS < 0) {
                TIME_LEFT_IN_MILLIS = 0;
                timerRunning = false;
                updateCountDownText(TIME_LEFT_IN_MILLIS);
            } else {
                totalSeconds=(int)START_TIME_IN_MILLIS/1000;
                progressbar.setMax(totalSeconds);
                progressSeconds=(int)TIME_LEFT_IN_MILLIS/1000;
                progressbar.setProgress(progressSeconds);
                startTimer();
            }
        }
        else {
            btn_pause.setVisibility(View.INVISIBLE);
            btn_start.setVisibility(View.VISIBLE);
        }
        updateCountDownText(TIME_LEFT_IN_MILLIS);
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
        super.onBackPressed();
    }
}