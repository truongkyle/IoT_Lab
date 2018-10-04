package com.example.truongkyle.lab01_4;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManager;
import com.google.android.things.pio.Pwm;

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
    private static final String ledPinR = BoardDefaults.getGPIOForLedR();
    private static final String ledPinB = BoardDefaults.getGPIOForLedB();
    private static final String ledPinG = BoardDefaults.getGPIOForLedG();
    private static final String PWM_NAME = BoardDefaults.getGPIOForPwm();

    private static final double MIN_ACTIVE_PULSE_DURATION_MS = 0;
    private static final double MAX_ACTIVE_PULSE_DURATION_MS = 20;
    private static final double PULSE_PERIOD_MS = 20;

    private static final double PULSE_CHANGE_PER_STEP_MS = 2;
    private static final int INTERVAL_BETWEEN_STEPS_MS = 50;

    private static final String buttonPin = BoardDefaults.getGPIOForButton();

    private Gpio mButtonGpio, mLedGpioR, mLedGpioG, mLedGpioB;
    private boolean mLedStateR = true;
    private boolean mLedStateG = true;
    private boolean mLedStateB = true;
    private Pwm mPwm;
    private int i = 0;

    private final int STATE_LED = 0;
    private final int STATE_RED = 1;
    private final int STATE_GREEN = 2;
    private final int STATE_BLUE = 3;
    private int state = STATE_LED;

    private static final int LED_RED = 1;
    private static final int LED_GREEN = 2;
    private static final int LED_BLUE = 3;

    private int mLedState = LED_RED;

    private Handler mHandler = new Handler();
    private double mActivePulseDuration;
    private boolean mIsPulseIncreasing = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "Starting ButtonActivity");
        try {
            PeripheralManager manager = PeripheralManager.getInstance();

            mLedGpioR = manager.openGpio(ledPinR);
            mLedGpioR.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
            mLedGpioR.setValue(true);

            mLedGpioG = manager.openGpio(ledPinG);
            mLedGpioG.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
            mLedGpioG.setValue(true);

            mLedGpioB = manager.openGpio(ledPinB);
            mLedGpioB.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
            mLedGpioB.setValue(true);

            mActivePulseDuration = MIN_ACTIVE_PULSE_DURATION_MS;
            mPwm = manager.openPwm(PWM_NAME);
            mPwm.setPwmFrequencyHz(1000 / PULSE_PERIOD_MS);
            mPwm.setPwmDutyCycle(mActivePulseDuration);
            mPwm.setEnabled(true);

            mButtonGpio = manager.openGpio(buttonPin);
            mButtonGpio.setDirection(Gpio.DIRECTION_IN);
            mButtonGpio.setActiveType(Gpio.ACTIVE_HIGH);
            mButtonGpio.setEdgeTriggerType(Gpio.EDGE_FALLING);

            Log.i(TAG, "Gonna go register for button");
            mButtonGpio.registerGpioCallback(new GpioCallback() {
                @Override
                public boolean onGpioEdge(Gpio gpio) {
                    Log.e(TAG,"co nut nhan...");
                    switch (state) {
                        case STATE_LED:
                            state = STATE_RED;
                            break;
                        case STATE_RED:
                            state = STATE_GREEN;
                            break;
                        case STATE_GREEN:
                            state = STATE_BLUE;
                            break;
                        case STATE_BLUE:
                            state = STATE_LED;
                            break;
                        default:
                            Log.e(TAG, "Error");
                            break;
                    }
                    Log.e(TAG, "Button pressed" + state);
                    return true;
                }
            });

            mHandler.post(mChangePWMRunnable);
        } catch (IOException e) {
            Log.e(TAG, "Error press button", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mChangePWMRunnable);
        if (mButtonGpio != null || mPwm != null || mLedGpioR != null || mLedGpioB != null || mLedGpioG != null) {
            // Close the Gpio pin
            Log.i(TAG, "Closing Button GPIO pin");
            try {
                mPwm.close();
                mLedGpioR.close();
                mLedGpioG.close();
                mLedGpioB.close();
                mButtonGpio.close();
            } catch (IOException e) {
                Log.e(TAG, "Error on PeripheralIO API27", e);
            } finally {
                mButtonGpio = null;
                mLedGpioR = null;
                mLedGpioB = null;
                mLedGpioG = null;
                mPwm = null;
            }
        }
    }

    private Runnable mChangePWMRunnable = new Runnable() {
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

            if (mActivePulseDuration > MAX_ACTIVE_PULSE_DURATION_MS) {
                mActivePulseDuration = MAX_ACTIVE_PULSE_DURATION_MS;
                mIsPulseIncreasing = !mIsPulseIncreasing;
            } else if (mActivePulseDuration < MIN_ACTIVE_PULSE_DURATION_MS) {
                mActivePulseDuration = MIN_ACTIVE_PULSE_DURATION_MS;
                mIsPulseIncreasing = !mIsPulseIncreasing;
            }

//            Log.d(TAG,"Changing PWM active pulse duration to " + mActivePulseDuration + "ms");

            try {
                ++i;
                if (i == 60) {
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

                if((state == STATE_RED && mLedState == LED_RED) || (state == STATE_GREEN && mLedState != LED_BLUE) || state == LED_BLUE ){
                    mPwm.setPwmDutyCycle(100 * mActivePulseDuration / PULSE_PERIOD_MS);
                    Log.i(TAG," Set PWMDUTYCYCLE"+ Double.toString(mActivePulseDuration / PULSE_PERIOD_MS));
                } else {
                    mPwm.setPwmDutyCycle(100);
                    Log.i(TAG,"DutyCycle = 100 ");
                }
                mHandler.postDelayed(this, INTERVAL_BETWEEN_STEPS_MS);
            } catch (IOException e) {
                Log.e(TAG, "Error on PeripheralIO API", e);
            }
        }
    };
}

