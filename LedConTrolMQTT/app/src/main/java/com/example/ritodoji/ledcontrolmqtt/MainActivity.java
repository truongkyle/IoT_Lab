package com.example.ritodoji.ledcontrolmqtt;

import android.app.Activity;
import android.os.Bundle;
import android.system.StructTimespec;
import android.text.LoginFilter;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManager;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.IOException;


public class MainActivity extends Activity implements MqttControl.MqttInterface, SPITempADS111.DataTemp{
    private  TextView textview, texta, textViewTemp, textViewshow;
    private EditText editText;
    private static final String TAG = MainActivity.class.getSimpleName();
    public static final String LED_PIN = "BCM23";
    private static final String topicTemp ="smarthome/temp/value";
    MqttControl mqttControl;
    SPITempADS111 spiTempADS111;
    Gpio Ledpin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init_view();
        try {
            init_led();
            mqttControl = new MqttControl(this,this, Ledpin );
            mqttControl.sendmessage("test","smarthome/led/state");
            spiTempADS111 = new SPITempADS111(this);
            Log.d(TAG,"mqtt done ! ");
        } catch ( IOException e ) {
            e.printStackTrace();
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }
    private void init_view(){
        textViewTemp = findViewById(R.id.textViewTemp);
        textViewshow = findViewById(R.id.textViewShowtemp);
        textview = findViewById(R.id.textview);
        editText = findViewById(R.id.editText);
    }
    private void init_led() throws IOException {
        PeripheralManager manager= PeripheralManager.getInstance();
        Ledpin = manager.openGpio(LED_PIN);
        Ledpin.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
        Ledpin.setValue(true);
        Log.d(TAG,"set led success");
    }
    @Override
    public void getMessage(String payload) {
        Log.d(TAG,"got a message from phone : " + payload);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        try {
            mqttControl.close();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
    public void setText(final TextView text, final String value){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                text.setText(value);
            }
        });
    }

    @Override
    public void setText(String x) {
        Log.d(TAG,"aaaaaaaaaaaaaaaa" + x);
        setText(textViewshow,x);
        try {
            mqttControl.sendmessage(x,topicTemp);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
