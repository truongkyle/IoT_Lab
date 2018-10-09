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
public class cau5 {
    private static String TAG = MainActivity.class.getSimpleName();


    public static Gpio mLedGpioR, mLedGpioG, mLedGpioB;

    private static Handler mHandler = new Handler();

    private static boolean mLedStateR = true, mLedStateB = true, mLedStateG = true ;


    public static void runcau5(Gpio a, Gpio b, Gpio c) {
        Log.i(TAG,"On create");
       // super.onCreate(savedInstanceState);
        mLedGpioR = a;
        mLedGpioG = b;
        mLedGpioB = c;

        mHandler.post(mRunnableLedB);
        mHandler.post(mRunnableLedG);
        mHandler.post(mRunnableLedR);

    }


    private static Runnable mRunnableLedR = new Runnable() {
        @Override
        public void run() {
            mLedStateR = !mLedStateR;
            try{
                mLedGpioR.setValue(mLedStateR);
                Log.d(TAG,"Red");

                mHandler.postDelayed(mRunnableLedR, 500);

            } catch (IOException e){
                Log.e(TAG,"Error: ",e);
            }
        }
    };

    private static Runnable mRunnableLedG = new Runnable() {
        @Override
        public void run() {
            mLedStateG = !mLedStateG;
            try {
                Log.d(TAG, "Green");
                mLedGpioG.setValue(mLedStateG);
                mHandler.postDelayed(mRunnableLedG, 1000);

            } catch (IOException e){
                Log.e(TAG,"Error: ",e);
            }
        }
    };

    private static Runnable mRunnableLedB = new Runnable() {
        @Override
        public void run() {
            mLedStateB = !mLedStateB;
            try{
                Log.d(TAG,"Blue");
                mLedGpioB.setValue(mLedStateB);
                Log.e(TAG,"Blue222222");
                mHandler.postDelayed(mRunnableLedB, 2000);

            } catch (IOException e){
                Log.e(TAG,"Error: ",e);
            }
        }
    };


}
