package com.example.truongkyle.lab1_1;

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
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int LED_RED = 1;
    private static final int LED_GREEN = 2;
    private static final int LED_BLUE = 3;
    private int mLedState = LED_RED;
    private Gpio mLedGpioR, mLedGpioG, mLedGpioB;
    private static final int INTERVAL_BETWEEN_BLINKS_MS = 1000;
    private Handler mHandler = new Handler();

    private boolean mLedStateR = true;
    private boolean mLedStateG = true;
    private boolean mLedStateB = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try{
            PeripheralManager manager = PeripheralManager.getInstance();
            String pinLedR = BoardDefaults.getGPIOForLedR();
            mLedGpioR = manager.openGpio(pinLedR);
            mLedGpioR.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);

            String pinLedG = BoardDefaults.getGPIOForLedG();
            mLedGpioG =manager.openGpio(pinLedG);
            mLedGpioG.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);

            String pinLedB = BoardDefaults.getGPIOForLedB();
            mLedGpioB = manager.openGpio(pinLedB);
            mLedGpioB.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);

            mHandler.post(mBlinkRunnable);
        } catch (IOException e){
            Log.e(TAG,"Error",e);
        }
    }
    @Override
    protected void onDestroy(){
        try{
            mLedGpioB.close();
            mLedGpioG.close();
            mLedGpioR.close();
        }catch (IOException e) {
            Log.e(TAG, "Error", e);
        } finally {
            mLedGpioR=null;
            mLedGpioG=null;
            mLedGpioB=null;
        }
        super.onDestroy();
    }
    private Runnable mBlinkRunnable = new Runnable() {
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
