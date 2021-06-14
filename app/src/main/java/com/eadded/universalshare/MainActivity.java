package com.eadded.universalshare;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.eadded.universalshare.CommonLib.Common;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setEvents();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void setEvents() {
        findViewById(R.id.mainSendBt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Common.vibrate();
                startActivity(new Intent(getApplicationContext(), FileExplorer.class).putExtra("type", 0));
            }
        });
        findViewById(R.id.mainRecBt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Common.vibrate();
                startActivity(new Intent(getApplicationContext(), Send.class).putExtra("type", 1));
            }
        });
        findViewById(R.id.mainInfoBt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Common.vibrate();
                startActivity(new Intent(getApplicationContext(), info.class).putExtra("type", 1));
            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}