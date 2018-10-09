package com.example.ritodoji.loopbackuart;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManager;

import java.io.IOException;

/**
 * Skeleton of an Android Things activity.
 * <p>
 * Android Things peripheral APIs are accessible through the class
 * PeripheralManagerService. For example, the snippet below will open a GPIO pin and
 * set it to HIGH:
 *
 * <pre>{@code
 * PeripheralManagerService service = new PeripheralManagerService();
 * mLedGpio = service.openGpio("BCM6");
 * mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
 * mLedGpio.setValue(true);
 * }</pre>
 * <p>
 * For more complex peripherals, look for an existing user-space driver, or implement one if none
 * is available.
 *
 * @see <a href="https://github.com/androidthings/contrib-drivers#readme">https://github.com/androidthings/contrib-drivers#readme</a>
 */
public class cau1 {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int LED_RED = 1;
    private static final int LED_GREEN = 2;
    private static final int LED_BLUE = 3;
    private static int mLedState = LED_RED;
    private static Gpio mLedGpioR, mLedGpioG, mLedGpioB;
    private static final int INTERVAL_BETWEEN_BLINKS_MS = 1000;
    private static Handler mHandler = new Handler();

    private static boolean mLedStateR = true;
    private static boolean mLedStateG = true;
    private static boolean mLedStateB = true;


    public static void runcau1(Gpio a,Gpio b,Gpio c) {

        mLedGpioR = a;
        mLedGpioG = b;
        mLedGpioB =c;
        mHandler.post(mBlinkRunnable);
    }

    private static Runnable mBlinkRunnable = new Runnable() {
        @Override
        public void run() {
            if(mLedGpioB==null|| mLedGpioG==null||mLedGpioR==null){
                return;
            }
            try{
                switch (mLedState){
                    case LED_RED:
                        mLedStateR = false;
                        mLedStateG = true;
                        mLedStateB = true;
                        mLedState = LED_GREEN;
                        Log.i(TAG,"Red");
                        break;
                    case LED_GREEN:
                        mLedStateR = true;
                        mLedStateG = false;
                        mLedStateB = true;
                        mLedState = LED_BLUE;
                        break;
                    case LED_BLUE:
                        mLedStateR = true;
                        mLedStateG = true;
                        mLedStateB = false;
                        mLedState = LED_RED;
                        break;
                    default:
                        break;

                }
                mLedGpioR.setValue(mLedStateR);
                mLedGpioG.setValue(mLedStateG);
                mLedGpioB.setValue(mLedStateB);
                mHandler.postDelayed(mBlinkRunnable,INTERVAL_BETWEEN_BLINKS_MS);
            }catch (IOException e){
                Log.e(TAG,"ERROR",e);
            }
        }
    };
}
