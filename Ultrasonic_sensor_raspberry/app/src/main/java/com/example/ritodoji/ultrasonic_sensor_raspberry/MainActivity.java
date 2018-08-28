package com.example.ritodoji.ultrasonic_sensor_raspberry;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.widget.Switch;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManager;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.awt.font.TextAttribute;
import java.io.IOException;
import java.util.Arrays;

import javax.net.ssl.HandshakeCompletedEvent;

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
public class MainActivity extends Activity implements MqttCallback {
    private  static final String ledpin = "BCM23";
    private static final String TAG = "MQTT_Ultrasonic";
    private static final int INTERVAL_BETWEEN_TRIGGER = 1000;
    Gpio mLed;
    MqttSendData mqttSendData = new MqttSendData();
    UltraSonicSensor mUltraSonic;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mUltraSonic = new UltraSonicSensor();
        PeripheralManager manager = PeripheralManager.getInstance();
        try {
            mLed = manager.openGpio(ledpin);
            mLed.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
            mLed.setValue(true);

            mqttSendData.subcribeToTopic();
            mqttSendData.client.setCallback(this);


        } catch (IOException | MqttException e) {
            e.printStackTrace();
        }
        Log.d("Ultrasonic","Cool");

    }
    protected void onDestroy(){
        super.onDestroy();
        if(mUltraSonic.mTrig!=null){
            try {
                mUltraSonic.mTrig.close();
                mUltraSonic.mTrig = null;
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        if(mUltraSonic.mEcho!=null){
            try {
                mUltraSonic.mEcho.close();
                mUltraSonic.mEcho = null;
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        if(mLed!=null){
            try {
                mLed.close();
                mLed = null;
        }catch (IOException e){
                Log.e("ULtrasonic","fuck",e);
            }
        }
        if(mqttSendData.client!=null){
            try {
                mqttSendData.close();
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void connectionLost(Throwable cause) {
        try {
            Log.d(TAG,"Reconnecting ~ ................(-______-)#......");
            mqttSendData.client.connect(mqttSendData.connectOptionchoice());
            Log.d(TAG,"Connected success ! ");
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        String payload = new String(message.getPayload());
        Log.d(TAG,"mat set : "+ payload +"  ");
        double payloadd = Double.parseDouble(payload);
        Log.d(TAG,"mat set : "+ payloadd +"  ");
        switch (payload){
            case "CLOSE":
                mLed.setValue(true);
                break;
            case "FAR":
                mLed.setValue(false);
                break;
            default:
                Log.d(TAG,"message not supported !!");
                break;
            case "send":
                mqttSendData.sendmessage(String.valueOf(mUltraSonic.getDistance()));
                break;
            case "eh":
                mqttSendData.sendmessage("cai gi");
                break;
        }

       /* switch (payloadd){
            default:
                Log.d(TAG,"message not supported !!");
                break;
            case 80.0 > payloadd:
                mqttSendData.sendmessage(String.valueOf(mUltraSonic.getDistance()));
                break;
            case "eh":
                mqttSendData.sendmessage("cai gi");
                break;*/
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        Log.d(TAG,"data sent !! ~ ");
    }
}
