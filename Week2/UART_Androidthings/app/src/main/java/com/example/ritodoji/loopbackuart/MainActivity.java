package com.example.ritodoji.loopbackuart;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManager;
import com.google.android.things.pio.UartDevice;
import com.google.android.things.pio.UartDeviceCallback;
import com.google.android.things.pio.Pwm;

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
    private static String mLedPinR = BoardDefaults.getGPIOForLedR();
    private static String mLedPinG = BoardDefaults.getGPIOForLedG();
    private static String mLedPinB = BoardDefaults.getGPIOForLedB();
    private static final String PWM_NAME = BoardDefaults.getGPIOForPwm();
    private static final String buttonPin = BoardDefaults.getGPIOForButton();

    private Pwm mPwm;

    public Gpio mButtonGpio, mLedGpioR, mLedGpioG, mLedGpioB;


    private static final double MIN_ACTIVE_PULSE_DURATION_MS = 0;
    private static final double PULSE_PERIOD_MS = 20;

    private static boolean flag = false;

    private double mActivePulseDuration;

    private static final int INTERVAL_BETWEEN_UART_MS = 2000;
    private static final String UART_DEVICE_NAME = "UART0";
    private Handler mHandler = new Handler();
    private UartDevice mDevice;
    private cau4 cau4;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PeripheralManager manager = PeripheralManager.getInstance();

        try {
            mLedGpioB = manager.openGpio(mLedPinB);
            mLedGpioB.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
            mLedGpioB.setValue(true);

            mLedGpioG = manager.openGpio(mLedPinG);
            mLedGpioG.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
            mLedGpioG.setValue(true);

            mLedGpioR = manager.openGpio(mLedPinR);
            mLedGpioR.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
            mLedGpioR.setValue(true);

            mActivePulseDuration = MIN_ACTIVE_PULSE_DURATION_MS;
            mPwm = manager.openPwm(PWM_NAME);
            mPwm.setPwmFrequencyHz(1000 / PULSE_PERIOD_MS);
            mPwm.setPwmDutyCycle(mActivePulseDuration);
            mPwm.setEnabled(true);

            mButtonGpio = manager.openGpio(buttonPin);
            mButtonGpio.setDirection(Gpio.DIRECTION_IN);
            mButtonGpio.setActiveType(Gpio.ACTIVE_HIGH);
            mButtonGpio.setEdgeTriggerType(Gpio.EDGE_FALLING);


            List<String> deviceList = manager.getUartDeviceList();
            if (deviceList.isEmpty()) {
                Log.i(TAG, "No UART port available on this device.");
            } else {
                Log.i(TAG, "List of available devices: " + deviceList);
            }
            mDevice = manager.openUartDevice(UART_DEVICE_NAME);
            configureUartFrame(mDevice);

        } catch (IOException e) {
            Log.w(TAG, "Unable to access UART device", e);
        }
        mHandler.post(mUartRunnable);
    }
    @Override
    protected void onDestroy() {

        super.onDestroy();

        if (mDevice != null) {
            try {
                mDevice.unregisterUartDeviceCallback(mCallback);
                mDevice.close();
                mDevice = null;
            } catch (IOException e) {
                Log.w(TAG, "Unable to close UART device", e);
            }
        }
    }

    public void configureUartFrame(UartDevice uart) throws IOException {
        // Configure the UART port
        uart.setBaudrate(9600); //baud rate 9600
        uart.setDataSize(8); // 8 data bits
        uart.setParity(UartDevice.PARITY_NONE); // none parity
        uart.setStopBits(1); // stop bits = 1
    }
    private UartDeviceCallback mCallback = new UartDeviceCallback() {
        @Override
        public boolean onUartDeviceDataAvailable(UartDevice uart) {
            try{
                recieveData(uart);

            }catch (IOException e){
                Log.w(TAG,"Unable to access UART device");
            }
            //Continue listening for more interrupts
            return true;
        }

        @Override
        public void onUartDeviceError(UartDevice uart, int error) {
            Log.w(TAG, uart + ": Error event " + error);
        }
    };
    private Runnable mUartRunnable = new Runnable() {
        @Override
        public void run() {
            if (mDevice == null) return;

            try {
                mDevice.registerUartDeviceCallback(mCallback);
                //sendData();
                mHandler.postDelayed(mUartRunnable, INTERVAL_BETWEEN_UART_MS);
            } catch (IOException e) {
                Log.e(TAG, "Error on PeripheralIO API", e);
            }

        }
    };
    public void recieveData(UartDevice uart) throws IOException {
        final int maxCount = 100;
        byte[] buffer = new byte[maxCount] ;

        int count;


        while ((count = uart.read(buffer, buffer.length)) > 0) {
            String str = new String(buffer,"UTF-8");
            String c = str.substring(0,count);
            Log.i(TAG,"nhan 5" +"=="+c.substring(0,1)+"==");
            if(c.substring(0,1).equals("0")){
                flag = true;
            }
            if(flag) {
                switch (c.substring(0, 1)) {

                    case "F":
                        mLedGpioR.close();
                        mLedGpioG.close();
                        mLedGpioB.close();
                        mPwm.close();
                        mButtonGpio.close();

                        mLedGpioB= null;
                        mLedGpioG = null;
                        mLedGpioR = null;
                        mPwm = null;
                        mButtonGpio = null;
                        break;
                    case "1":
                        Log.i(TAG, "nhan 1");
                        cau1.runcau1(mLedGpioR, mLedGpioG, mLedGpioB);
                        break;
                    case "2":
                        Log.i(TAG, "nhan 2");
                        cau2.runcau2(mLedGpioR, mLedGpioG, mLedGpioB,mButtonGpio);
                        break;
                    case "3":
                        mActivePulseDuration = MIN_ACTIVE_PULSE_DURATION_MS;
                        mPwm.setPwmDutyCycle(100*mActivePulseDuration / PULSE_PERIOD_MS);
                        Log.i(TAG, "nhan 3");
                        cau3.runcau3(mLedGpioR, mLedGpioG, mLedGpioB, mPwm);
                        break;
                    case "4":
                        mActivePulseDuration = MIN_ACTIVE_PULSE_DURATION_MS;
                        mPwm.setPwmDutyCycle(100*mActivePulseDuration / PULSE_PERIOD_MS);
                        Log.i(TAG, "nhan 4");
                        cau4.runcau4(mLedGpioR, mLedGpioG, mLedGpioB, mButtonGpio, mPwm);
                        break;
                    case "5":
                        Log.i(TAG, "nhan 5");
                        cau5.runcau5(mLedGpioR, mLedGpioG, mLedGpioB);
                        break;
                    default:

                        break;
                }
            }


            Log.d(TAG, "Read " + count + " bytes from peripheral: "+c );
        }
    }
    /*public  void sendData(){
        if (mDevice == null) return;
        try {
            byte[] buffer = new byte[32];
            String a = " hello";
            buffer =a.getBytes();
            int count = mDevice.write(buffer, buffer.length);
            Log.i(TAG,"Send: " +a + " from peripheral ");
        } catch (IOException e) {
            Log.e(TAG, "Error on PeripheralIO API", e);
        }
    }*/
}