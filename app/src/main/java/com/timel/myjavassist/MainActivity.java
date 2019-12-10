package com.timel.myjavassist;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.timel.bus.TimelBus;
import com.timel.bus.annotation.Bus;
import com.timel.bus.annotation.LogTime;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("MainActivity", "onCreate");

        onClick();
    }

    private void onClick() {


        TimelBus.getInstance().onStickyEvent(EventTags.TEST);
    }

    @LogTime
    @Bus(value = EventTags.TEST)
    public void test() {
        Log.d("MainActivity", "test");
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
