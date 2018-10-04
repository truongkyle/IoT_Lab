package com.example.truongkyle.lab1_3;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManager;
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
public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final double MIN_ACTIVE_PULSE_DURATION_MS = 0;
    private static final double MAX_ACTIVE_PULSE_DURATION_MS = 20;
    private static final double PULSE_PERIOD_MS = 20;   // Frequency of 50Hz (1000/20)

    private static final double PULSE_CHANGE_PER_STEP_MS = 2;
    private static final int INTERVAL_BETWEEN_STEPS_MS = 1000;

    private static final int LED_RED = 1;
    private static final int LED_GREEN = 2;
    private static final int LED_BLUE = 3;
    private int mLedState = LED_RED;
    private Gpio mLedGpioR, mLedGpioG, mLedGpioB;
    private static final String PWM_NAME = BoardDefaults.getLedPin();
    private static int i = 0;

    private Pwm mPwm;

    private boolean mLedStateR = true;
    private boolean mLedStateG = true;
    private boolean mLedStateB = true;

    private Handler mHandler = new Handler();
    private boolean mIsPulseIncreasing = true;
    private double mActivePulseDuration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG,"Start changing PWM pulse");

        PeripheralManager manager = PeripheralManager.getInstance();
//      List all PWM pin
        List<String> portList = manager.getPwmList();
        if(portList.isEmpty()){
            Log.i(TAG,"No PWM port available on this device.");
        } else {
            Log.i(TAG,"List of available ports: " + portList);
        }

        try{
            mActivePulseDuration = MIN_ACTIVE_PULSE_DURATION_MS;
            mPwm = manager.openPwm(PWM_NAME);
            mPwm.setPwmFrequencyHz(1000/PULSE_PERIOD_MS);
            mPwm.setPwmDutyCycle(mActivePulseDuration);
            mPwm.setEnabled(true);

            Log.d(TAG,"Stat changing PWM pulse");

            String ledB = BoardDefaults.getLedBPin();
            mLedGpioB = manager.openGpio(ledB);
            mLedGpioB.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);

            String ledG = BoardDefaults.getLedGPin();
            mLedGpioG = manager.openGpio(ledG);
            mLedGpioG.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);

            String ledR = BoardDefaults.getLedRPin();
            mLedGpioR = manager.openGpio(ledR);
            mLedGpioR.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);


            mHandler.post(mChangePWMRunnable);
        } catch (IOException e){
            Log.w(TAG,"Error on IOException: ",e);
        }
    }

    protected void onDestroy(){
//      A pin configured for PWM continues to output its signal even after the close() method is
//      called. Call setEnabled(false) to stop the signal.
        super.onDestroy();
        mHandler.removeCallbacks(mChangePWMRunnable);
        Log.i(TAG,"Closing port");

        if(mPwm != null || mLedGpioR != null || mLedGpioB != null || mLedGpioG != null){
            try{
                mPwm.close();
                mLedGpioB.close();
                mLedGpioG.close();
                mLedGpioR.close();
            } catch(IOException e){
                Log.w(TAG,"Unable to close PWM",e);
            } finally {
                mPwm = null;
                mLedGpioR = null;
                mLedGpioG = null;
                mLedGpioB = null;
            }
        }
    }

    private final Runnable mChangePWMRunnable = new Runnable() {
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
                Log.e(TAG, "ádfsdadsaaaaaaaaaaaa");

                if(mIsPulseIncreasing) {
                    Log.e(TAG, "fsdtang "+Double.toString(100*mActivePulseDuration / PULSE_PERIOD_MS));
                }else {
                    Log.e(TAG,"giammmm"+Double.toString(100*mActivePulseDuration / PULSE_PERIOD_MS));
                }
                ++i;
                Log.e(TAG, "ádfsda");
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
                Log.e(TAG, "ádfsdayyyyyyyyyyyyyyy");
                mHandler.postDelayed(mChangePWMRunnable, INTERVAL_BETWEEN_STEPS_MS);
            } catch (IOException e) {
                Log.e(TAG, "Error on PeripheralIO API", e);
            }
        }
    };

}
