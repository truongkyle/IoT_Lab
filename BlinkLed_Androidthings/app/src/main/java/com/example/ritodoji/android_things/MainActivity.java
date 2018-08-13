package com.example.ritodoji.android_things;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManager;

import java.util.concurrent.TimeUnit;
import java.io.IOException;
import java.util.List;

/**
 * Skeleton of an Android Things activity.
 * <p>
 * Android Things peripheral APIs are accessible through the class
 * PeripheralManagerService. For example, the snippet below will open a GPIO pin and
 * set it to HIGH:
 * <p>
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
    private static final String TAG = "MainActivity";
    private static final int MS_DELAY = 200;
    private Handler mHandler = new Handler();
    boolean state = true ;
    Gpio ledGpio;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PeripheralManager manager = PeripheralManager.getInstance();
        List<String> portList = manager.getGpioList();
        if(portList.isEmpty()){
            Log.i(TAG, "The device dont support GPIO API.");
            return;
        }else {
            Log.i(TAG,"List of GPIO : "+ portList);
        }
        // open led gpio

        try {
            ledGpio = manager.openGpio("BCM4");
            ledGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
            ledGpio.setActiveType(Gpio.ACTIVE_HIGH);

            /* while (true) {
                    try {
                        if (ledGpio != null) {
                            ledGpio.setValue(state);
                        }
                        state = !state;
                        TimeUnit.MILLISECONDS.sleep(50);

                    } catch (InterruptedException ex) {
                        Log.e(TAG, "ERORR");
                    }
                }*/ // another way to blink led
            mHandler.post(mRunnable);
        }catch(IOException e){
            Log.e(TAG,"ERROR GPIO pins !!");
        }

    }
    @Override
    protected void onDestroy(){
        super.onDestroy();

        if(ledGpio != null){
            try{
                ledGpio.close();
            }catch (IOException e){
                Log.e(TAG, "Error on Pins");
            }
        }
    }

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            if ( ledGpio == null){
                return;
            }
            try {

                state =!state;
                Log.i(TAG, ledGpio + ":" + state);
                ledGpio.setValue(state);
                mHandler.postDelayed(mRunnable,MS_DELAY);
            }
            catch (IOException e){
                Log.e("ERRRROOORR","Cant access GPIO !");
            }
        }
    };
}
