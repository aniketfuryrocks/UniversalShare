package com.eadded.universalshare;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.eadded.universalshare.CommonLib.Common;

import java.util.ArrayList;
import java.util.List;

public class IntroActivity extends AppCompatActivity {

    private static int current;
    private LinearLayout linearLayout;
    private Animation animation;
    private String[] toAskPermissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        Common.applicationContext = getApplicationContext();
        Common.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        checkPermission();
        linearLayout = findViewById(R.id.introCont);
        if (current == 0) {
            if (!Common.sharedPreferences.getBoolean("introduced", false))
                current = 0;
            else if (toAskPermissions.length > 0)
                current = 3;
            else {
                finish();
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                return;
            }
        }
        start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        linearLayout = findViewById(R.id.introCont);
        checkPermission();
    }

    private void start() {
        if (current < 2) {
            setupLayout(current);
            startInitAnimation(true);
        } else {
            Log.i("hello", toAskPermissions.length + "|" + current);
            if (toAskPermissions.length > 0) {
                setupLayout(current);
                if (current == 2)
                    startInitAnimation(true);
                else
                    startInitAnimation(false);
            } else {
                current = 4;
                setupLayout(current);
                startInitAnimation(false);
            }
        }
    }

    private void startInitAnimation(final boolean finish) {
        animation = new AlphaAnimation(0.0f, 1f);
        animation.setRepeatCount(0);
        animation.setRepeatMode(AlphaAnimation.ABSOLUTE);
        animation.setDuration(1000);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                linearLayout.clearAnimation();
                if (finish)
                    startFinishAnimation();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        linearLayout.setAnimation(animation);
    }

    private void startFinishAnimation() {
        animation = new AlphaAnimation(1.0f, 0f);
        animation.setRepeatCount(0);
        animation.setRepeatMode(AlphaAnimation.ABSOLUTE);
        animation.setDuration(1000);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                linearLayout.setAlpha(0);
                linearLayout.clearAnimation();
                current++;
                start();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        linearLayout.setAnimation(animation);
    }

    private void setupLayout(int index) {
        linearLayout.setAlpha(1);
        final TextView heading = linearLayout.findViewById(R.id.introH1);
        LinearLayout content = linearLayout.findViewById(R.id.introH2);
        content.removeAllViews();
        switch (index) {
            case 0:
                heading.setText("Hi");
                break;
            case 1:
                heading.setText("Welcome");
                break;
            case 2:
                heading.setText("Let's set things up");
                break;
            case 3:
                heading.setText("To access your files we need storage permission from you !");
                LinearLayout linear = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.send_rec_com, null, false);
                Button bt = linear.findViewById(R.id.mainSendBt);
                bt.setText("Allow");
                bt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ActivityCompat.requestPermissions((Activity) v.getContext(), toAskPermissions, 1);
                    }
                });
                bt.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                        return true;
                    }
                });
                bt = linear.findViewById(R.id.mainRecBt);
                bt.setText("Deny");
                bt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        heading.setText("You can't share files without allowing us to access them");
                        v.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                finish();
                            }
                        });
                    }
                });
                content.addView(linear);
                break;
            case 4:
                heading.setText("Everything looks great, lets get Started !");
                Button button = new Button(this);
                button.setText("Start");
                button.setBackgroundResource(R.drawable.button_border);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putBoolean("introduced", true).commit();
                        finish();
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    }
                });
                content.addView(button);
        }
    }

    private void checkPermission() {
        List<String> toAsk = new ArrayList(2);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            toAsk.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            toAsk.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        toAskPermissions = toAsk.toArray(new String[0]);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        List<String> left = new ArrayList<>(permissions.length);
        for (int i = 0; i < permissions.length; i++) {
            if (grantResults[i] == PackageManager.PERMISSION_DENIED)
                left.add(permissions[i]);
        }
        if (left.size() > 0) {
            toAskPermissions = left.toArray(new String[0]);
            Common.makeToast("Permission not allowed");
        } else {
            current = 4;
            start();
        }
    }
}
