package com.example.ritodoji.ultrasonic_sensor_raspberry;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManager;

import java.io.IOException;

public class UltraSonicSensor {
    private static final String trigger = "BCM5";
    private static final String echo = "BCM6";
    private static final int INTERVAL_BETWEEN_TRIGGER = 1000;
    Gpio mEcho;
    Gpio mTrig;
    private long time1, time2;
    double distance;
    private Handler ultrasonicTriggerHandler;

    public  UltraSonicSensor() {
        HandlerThread handlerThread = new HandlerThread("callbackHandlerThread");
        handlerThread.start();
        Handler mCallbackHandler = new Handler(handlerThread.getLooper());
        ///
        HandlerThread trigHandlerThread = new HandlerThread("triggerHandlerThread");
        trigHandlerThread.start();
        ultrasonicTriggerHandler = new Handler(trigHandlerThread.getLooper());

        PeripheralManager manager = PeripheralManager.getInstance();
        try {
            mEcho = manager.openGpio(echo);
            mEcho.setDirection(Gpio.DIRECTION_IN);
            mEcho.setEdgeTriggerType(Gpio.EDGE_BOTH);
            mEcho.setActiveType(Gpio.ACTIVE_HIGH);
            mEcho.registerGpioCallback(mCallbackHandler, mCallback);

            mTrig = manager.openGpio(trigger);
            mTrig.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);


            Log.i("Ultrasonic", " Available devices GPIO" + manager.getGpioList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d("Ultrasonic","Cool");
        ultrasonicTriggerHandler.post(triggerRunnable);

    }
    private void readDistanceAsnyc() throws IOException, InterruptedException {
        mTrig.setValue(false);
        Thread.sleep(0,2000);

        // Hold the trigger pin high for at least 10 us
        mTrig.setValue(true);
        Thread.sleep(0,10000); //10 microsec

        // Reset the trigger pin
        mTrig.setValue(false);
    }
    private GpioCallback mCallback  = new GpioCallback() {
        @Override
        public boolean onGpioEdge(Gpio gpio) {
            try {

                if (!gpio.getValue()){
                    // The end of the pulse on the ECHO pin

                    time2 = System.nanoTime();

                    long pluseWidth = time2 - time1;
                    //Log.d("Ultrasonic", "pluseWidth: " + pluseWidth);
                    distance = (pluseWidth / 1000000000.0) * 340.0 / 2.0 * 100.0;
                    //double distance = (pluseWidth / 1000.0 ) / 58.23 ; //cm
                    Log.i("Ultrasonic", "distance: " + distance + " cm");
                } else {
                    // The pulse arrived on ECHO pin
                    time1 = System.nanoTime();


                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Step 6. Return true to keep callback active.
            return true;
        }
    };

    private    Runnable triggerRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                readDistanceAsnyc();
                ultrasonicTriggerHandler.postDelayed(triggerRunnable, INTERVAL_BETWEEN_TRIGGER);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }

        }
    };
    public double getDistance(){
        return distance;
    }
}
