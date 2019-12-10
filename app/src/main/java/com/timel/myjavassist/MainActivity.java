package com.timel.myjavassist;


import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.timel.bus.TimelBus;
import com.timel.bus.annotation.Bus;
import com.timel.bus.annotation.LogTime;

public class MainActivity extends Activity {

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

}
