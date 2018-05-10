package com.example.calista.kalacc;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

/**
 * Created by calista on 30/04/2018.
 */



public class MainActivityFix extends Activity {


    Switch wSwitch;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.round_activity_main);

        wSwitch = (Switch) findViewById(R.id.switchstart);
        wSwitch.setOnCheckedChangeListener(checkSwitch);

    }

    CompoundButton.OnCheckedChangeListener checkSwitch = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean isSwitched) {
            if (isSwitched) {
                startService(new Intent(MainActivityFix.this, AccelerationProcessing.class));

            } else {
                stopService(new Intent(MainActivityFix.this, AccelerationProcessing.class));
            }

        }
    };
}
