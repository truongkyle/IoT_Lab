package com.example.truongkyle.lab1_5;

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
public class MainActivity extends Activity {
    private static String TAG = MainActivity.class.getSimpleName();
    private static String mLedPinR = BoardDefaults.getGPIOForLedR();
    private static String mLedPinG = BoardDefaults.getGPIOForLedG();
    private static String mLedPinB = BoardDefaults.getGPIOForLedB();
    private static int i = 1;

    private Gpio mLedGpioR, mLedGpioG, mLedGpioB;

    private Handler mHandler = new Handler();

    private boolean mLedStateR = true, mLedStateB = true, mLedStateG = true ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG,"On create");
        super.onCreate(savedInstanceState);
        PeripheralManager manager = PeripheralManager.getInstance();
        try{
            mLedGpioB = manager.openGpio(mLedPinB);
            mLedGpioB.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
            mLedGpioB.setValue(true);

            mLedGpioG = manager.openGpio(mLedPinG);
            mLedGpioG.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
            mLedGpioG.setValue(true);

            mLedGpioR = manager.openGpio(mLedPinR);
            mLedGpioR.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
            mLedGpioR.setValue(true);
        } catch (IOException e){
            Log.e(TAG,"Error: ",e);
        }

        mHandler.post(mRunnableLedB);
        mHandler.post(mRunnableLedG);
        mHandler.post(mRunnableLedR);

    }


    private Runnable mRunnableLedR = new Runnable() {
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

    private Runnable mRunnableLedG = new Runnable() {
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

    private Runnable mRunnableLedB = new Runnable() {
        @Override
        public void run() {
            mLedStateB = !mLedStateB;
            try{
                Log.d(TAG,"Blue");
                mLedGpioB.setValue(mLedStateB);
                mHandler.postDelayed(mRunnableLedB, 2000);

            } catch (IOException e){
                Log.e(TAG,"Error: ",e);
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mRunnableLedR);
        mHandler.removeCallbacks(mRunnableLedG);
        mHandler.removeCallbacks(mRunnableLedB);

        if (mLedGpioR != null || mLedGpioB != null || mLedGpioG != null) {
            // Close the Gpio pin
            Log.i(TAG, "Closing Button GPIO pin");
            try {
                mLedGpioR.close();
                mLedGpioG.close();
                mLedGpioB.close();
            } catch (IOException e) {
                Log.e(TAG, "Error on PeripheralIO API27", e);
            } finally {
                mLedGpioR = null;
                mLedGpioB = null;
                mLedGpioG = null;
            }
        }
    }
}
