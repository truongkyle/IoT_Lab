package com.example.ritodoji.loopbackuart;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.Pwm;

import java.io.IOException;
import java.util.List;

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
public class cau3{
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final double MIN_ACTIVE_PULSE_DURATION_MS = 0;
    private static final double MAX_ACTIVE_PULSE_DURATION_MS = 20;
    private static final double PULSE_PERIOD_MS = 20;   // Frequency of 50Hz (1000/20)

    private static final double PULSE_CHANGE_PER_STEP_MS = 2;
    private static final int INTERVAL_BETWEEN_STEPS_MS = 1000;

    private static final int LED_RED = 1;
    private static final int LED_GREEN = 2;
    private static final int LED_BLUE = 3;
    private static int mLedState = LED_RED;
    private static Gpio mLedGpioR, mLedGpioG, mLedGpioB;
    private static int i = 0;

    private static Pwm mPwm;

    private static boolean mLedStateR = true;
    private static boolean mLedStateG = true;
    private static boolean mLedStateB = true;

    private static Handler mHandler = new Handler();
    private static boolean mIsPulseIncreasing = true;
    private static double mActivePulseDuration;


    public static void runcau3(Gpio a,Gpio b,Gpio c,Pwm f) {

        Log.i(TAG,"Start changing PWM pulse");


        mPwm =f;
        mLedGpioR = a;
        mLedGpioG = b;
        mLedGpioB = c;
        Log.d(TAG,"Stat changing PWM pulse");
        mHandler.post(mChangePWMRunnable);
    }



    private static final Runnable mChangePWMRunnable = new Runnable() {
        @Override
        public void run() {
            if (mPwm == null || mLedGpioG == null || mLedGpioR == null || mLedGpioB == null) {
                Log.w(TAG, "Stop runnable since mPwm is null");
                return;
            }

            if (mIsPulseIncreasing) {
                mActivePulseDuration += PULSE_CHANGE_PER_STEP_MS;
            } else {
                mActivePulseDuration -= PULSE_CHANGE_PER_STEP_MS;
            }

            if (mActivePulseDuration >= MAX_ACTIVE_PULSE_DURATION_MS) {
                mActivePulseDuration = MAX_ACTIVE_PULSE_DURATION_MS;
                mIsPulseIncreasing = !mIsPulseIncreasing;
            } else if (mActivePulseDuration <= MIN_ACTIVE_PULSE_DURATION_MS) {
                mActivePulseDuration = MIN_ACTIVE_PULSE_DURATION_MS;
                mIsPulseIncreasing = !mIsPulseIncreasing;
            }

//            Log.d(TAG,"Changing PWM active pulse duration to " + mActivePulseDuration + "ms");

            try {
                String a = Double.toString(mActivePulseDuration);


                ++i;

                if (i == 20) {
                    switch (mLedState) {
                        case LED_RED:
                            mLedState = LED_GREEN;
                            mLedStateR = false;
                            mLedStateG = true;
                            mLedStateB = true;
                            break;
                        case LED_GREEN:
                            mLedState = LED_BLUE;
                            mLedStateG = false;
                            mLedStateB = true;
                            mLedStateR = true;
                            break;
                        case LED_BLUE:
                            mLedState = LED_RED;
                            mLedStateB = false;
                            mLedStateG = true;
                            mLedStateR = true;
                            break;
                        default:
                            throw new IllegalStateException("error");
                    }

                    i = 0;
                    mLedGpioG.setValue(mLedStateG);
                    mLedGpioR.setValue(mLedStateR);
                    mLedGpioB.setValue(mLedStateB);
                    Log.d(TAG, "Change state" + " red: " + mLedStateR + " green: " + mLedStateG + " blue: " + mLedStateB);

                }

                mPwm.setPwmDutyCycle(100*mActivePulseDuration / PULSE_PERIOD_MS);
                mHandler.postDelayed(mChangePWMRunnable, INTERVAL_BETWEEN_STEPS_MS);
            } catch (IOException e) {
                Log.e(TAG, "Error on PeripheralIO API", e);
            }
        }
    };

}
