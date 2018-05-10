package com.example.calista.kalacc;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;


/**
 * Created by calista on 15/04/2018.
 */

/** Fall processing dari HEALTH-E**/

public class AccelerationProcessing extends Service implements SensorEventListener {



    float gravity[] = {0f, 0f, 0f}, linear_acceleration[] = {0f, 0f, 0f}; //0f = float 0
    double Zvalue, totLinear, totAcc, FallCounter = 0, threshold = 38, g = 9.8;
    double Aj, Ajtot, Mu, Sigma, AI, AItot = 0;
    double N, k, sum1, sum2 = 0;
    int z = 0;
    int i, j;

    AlertDialog popup;
    CountDownTimer time;
    long[] pattern = {400, 400};
    List<Double> ajList;
    //List<Double> subAjList;
    List<Double> sigmaList;
    //List<Double> subSigmaList;


    SensorManager sensorManager;
    Sensor sensorAcc;


    boolean flag = false;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
       // di tutorial gaada, kalo gabisa dimasukin super.onCreate();
        // Create our Sensor Manager
        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);


        // Accelerometer Sensor
        sensorAcc = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);


        // Register sensor Listener
        sensorManager.registerListener(this, sensorAcc, SensorManager.SENSOR_DELAY_NORMAL); //*120418 13333 delaynya
        ajList = new ArrayList<>();
        //subAjList = new ArrayList<>();
        sigmaList = new ArrayList<>();
        //subSigmaList = new ArrayList<>();




    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        //super.onDestroy();
        stopForeground(true);
        sensorManager.unregisterListener((SensorEventListener) this, sensorAcc); //diubah

    }

    @Override
    public void onAccuracyChanged(Sensor sensorAcc, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // Update the accelerometer

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            final float alpha = (float) 0.8;

            // Isolate the force of gravity with the low-pass filter.
            gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0]; // gravity = 0.8 * gravity[0] + (1-0.8) * acceleration
            gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
            gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];


            // Remove the gravity contribution with the high-pass filter.
            linear_acceleration[0] = event.values[0] - gravity[0];
            linear_acceleration[1] = event.values[1] - gravity[1];
            linear_acceleration[2] = event.values[2] - gravity[2];

            //Log.i("Fall", " gravity 0:" + gravity[0] + " gravity 1: " + gravity[1] + " gravity 2: " + gravity[2]);
            //Log.i("Fall", " acc 0:" + event.values[0] + " gravity 1: " + event.values[1] + " gravity 2: " + event.values[2]);
            //Log.i("Fall", " linear 0:" + linear_acceleration[0] + " linear 1: " + linear_acceleration[1] + " linear 2: " + linear_acceleration[2]);

            totAcc = Math.sqrt(event.values[0] * event.values[0] +
                    event.values[1] * event.values[1] +
                    event.values[2] * event.values[2]);

            //Log.i("Fall", "totAcc = " + totAcc);

            totLinear = Math.sqrt(linear_acceleration[0] * linear_acceleration[0] +
                    linear_acceleration[1] * linear_acceleration[1] +
                    linear_acceleration[2] * linear_acceleration[2]);

            //Log.i("Fall", "totLinear = " + totLinear);

            Zvalue = ((totAcc * totAcc) - (totLinear * totLinear) - (g * g))/(2 *g);

            //Log.i("Fall", "Z value = " + Zvalue);

            flag = false;

            FallCounter = ((Zvalue > threshold) ? FallCounter + 1 : 0); //if fall counter = totacc > threshold, fallcounter = +1, else 0.

            if (Zvalue > threshold) {
                Log.i("Fall", "melebihi threshold");
            }

            Log.i("Fall", "fall counter = " + FallCounter);

            if (FallCounter == 3) { //if (FallCounter == 5 && !detected)
                Log.i("Fall", "FALL DETECTED");

            }

            if (N<250) { //N<250

                N = N+1;

                //Log.i("AI", "bikin N value, N " + N);

                Aj = totAcc;

                ajList.add(Aj);

                //Log.i("AI", "bikin Aj value, Aj " + Aj);

                Ajtot = Ajtot + Aj;


                //Log.i("AI", "bikin Ajtot value, Ajtot " + Ajtot);

                Mu = (1/N)*Ajtot;

                //Log.i("AI", "bikin Mu value, Mu " + Mu);

                Sigma = Math.sqrt((1/N) * ((Aj - Mu) * (Aj - Mu)));
                //Log.i("AI", "bikin Sigma value, Sigma N " + N + " , " + Sigma);


                if (N == 250.0) {


                    AI = AI + Sigma;
                   // Log.i("AI", "bikin AI value 1 " + AI);
                    k = k + 1;
                    sigmaList.add(Sigma);

                    N = 251.0;

                }
            }

            else if (N == 251.0)
            {
                Aj = totAcc;
                ajList.add(Aj);
                //Log.i("AI", "ajlist " + (ajList.size()));
                ajList = ajList.subList(1, (ajList.size()));
               // Log.i("AI", "ajlist baru " + (ajList.size()));

                i = 0;
                sum1 = 0;
                for(i = 0; i < ajList.size(); i++)
                {
                    sum1 += ajList.get(i);
                }

                Ajtot = sum1;

                //Log.i("AI", "bikin Ajtot value 251, Ajtot " + Ajtot);

                Mu = Ajtot / 250.0;

                //Log.i("AI", "bikin Mu value, Mu " + Mu);

                Sigma = Math.sqrt(((Aj - Mu) * (Aj - Mu))/250.0);

                //Log.i("AI", "bikin Sigma value 251, Sigma " + Sigma);


                if (k<12){
                    k = k + 1;
                    AI = AI + Sigma;
                   // Log.i("AI", "bikin AI value, AI k " + k + " , " + AI);
                    sigmaList.add(Sigma);
                    if(k==12){
                        AItot = AI;
                       // Log.i("AI", "bikin AItot value, AItot 1" + AItot);
                        k = 13;
                    }
                }

                else if (k==13){
                    sigmaList.add(Sigma);
                   // Log.i("AI", "sigmalist " + (sigmaList.size()));
                    sigmaList = sigmaList.subList(1, (sigmaList.size()));
                   // Log.i("AI", "sigmalist baru" + (sigmaList.size()));


                    j = 0;
                    sum2 = 0;
                    for(j = 0; j < sigmaList.size(); j++)
                    {
                        sum2 += sigmaList.get(j);
                    }
                    AI = sum2;
                    AItot = AI;
                  //  Log.i("AI", "bikin AItot value, AItot seterus" + AItot);

                }


            }

            if ((AItot != 0) && (AItot < 0.50))
            {
                Log.i("AI Activity conclusion", "Resting, AItot " + AItot);
            }

            if ((AItot != 0)  && (AItot >= 0.50) && (AItot < 4.0))
            {
                Log.i("AI Activity conclusion", "Moderate Activity, AItot " + AItot);
            }
            if ((AItot != 0)  && (AItot >= 4.00))
            {
                Log.i("AI Activity conclusion", "Vigurous Activity, AItot " + AItot);
            }

        /* if Anything, heart rate < 60 = abnormal
         if Resting, heart rate > 100 = abnormal
         if light to moderate activity, hr > 220-age*07 = take a rest
         if vigorous activity, hr > 220-age*0,85 = take a rest*/

        }

    }

}
