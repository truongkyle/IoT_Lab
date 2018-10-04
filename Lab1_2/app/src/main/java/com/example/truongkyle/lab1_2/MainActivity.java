package com.example.truongkyle.lab1_2;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
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

    private static boolean mLedStateR = true;
    private static boolean mLedStateG = true;
    private static boolean mLedStateB = true;
    private  Gpio mpinLedR, mpinLedG, mpinLedB, mpinButton;
    private int intervalBetweenBlink =2000;
    private static final int  INTERVAL_05S = 500;
    private static final int  INTERVAL_1S = 1000;
    private static final int INTERVAL_01S = 100;
    private Handler mHandler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            PeripheralManager manager = PeripheralManager.getInstance();
            String pinButton = BoardDefaults.getGPIOForButton();
            mpinButton = manager.openGpio(pinButton);
            mpinButton.setDirection(Gpio.DIRECTION_IN);
            mpinButton.setActiveType(Gpio.ACTIVE_HIGH);
            mpinButton.setEdgeTriggerType(Gpio.EDGE_FALLING);

            mpinButton.registerGpioCallback(new GpioCallback() {
                @Override
                public boolean onGpioEdge(Gpio gpio) {
                    Log.e(TAG,"sdfasdfasdfasdfasdf");
                    switch (intervalBetweenBlink) {
                        case INTERVAL_1S:
                            intervalBetweenBlink = 500;
                            break;
                        case INTERVAL_05S:
                            intervalBetweenBlink = 100;
                            break;
                        case INTERVAL_01S:
                            intervalBetweenBlink = 1000;
                            break;
                        default:
                            intervalBetweenBlink = 2000;
                            break;
                    }

                    return true;
                }
            });
            String pinLedR = BoardDefaults.getGPIOForLedR();
            mpinLedR = manager.openGpio(pinLedR);
            mpinLedR.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);

            String pinLedG = BoardDefaults.getGPIOForLedG();
            mpinLedG = manager.openGpio(pinLedG);
            mpinLedG.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);

            String pinLedB = BoardDefaults.getGPIOForLedB();
            mpinLedB = manager.openGpio(pinLedB);
            mpinLedB.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);

            mHandler.post(mBlinkRunnable);

        } catch (IOException e) {
            Log.e(TAG, "error", e);
        }
    }

    private Runnable mBlinkRunnable = new Runnable() {
        @Override
        public void run() {
            if(mpinLedG==null||mpinLedB==null||mpinLedR==null||mpinButton==null){
                return;
            }
            try{
            switch (mLedState){
                case LED_BLUE:
                    mLedStateB = false;
                    mLedStateG = true;
                    mLedStateR = true;
                    mLedState = LED_RED;
                    break;
                case LED_RED:
                    mLedStateB = true;
                    mLedStateG = true;
                    mLedStateR = false;
                    mLedState = LED_GREEN;
                    break;
                case LED_GREEN:
                    mLedStateB = true;
                    mLedStateG = false;
                    mLedStateR = true;
                    mLedState = LED_BLUE;
                    break;
                 default:
                     break;

            }
            mpinLedR.setValue(mLedStateR);
            mpinLedB.setValue(mLedStateB);
            mpinLedG.setValue(mLedStateG);

            mHandler.postDelayed(mBlinkRunnable,intervalBetweenBlink);

        }catch (IOException e){
                Log.e(TAG,"error",e);
            }
        }
    };
        @Override
        protected void onDestroy() {
            super.onDestroy();
            try {
                mpinLedB.close();
                mpinLedG.close();
                mpinLedR.close();
            } catch (IOException e) {
                Log.e(TAG, "Error on PeripheralIO API", e);
            } finally {
                Log.d(TAG, "On destroy");
                mpinLedG = null;
                mpinLedR = null;
                mpinLedB = null;
            }
        }

    }

