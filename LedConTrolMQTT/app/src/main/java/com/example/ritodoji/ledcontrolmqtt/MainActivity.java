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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class MainActivity extends Activity implements  SPITempADS111.DataTemp{
    private TextView textViewshow;
    private static final String TAG = MainActivity.class.getSimpleName();
    public static final String LED_PIN = "BCM23";
    public static final String LED_PIN_2 = "BCM22";
    public static final String LED_PIN_3 = "BCM24";
    private static final String topicTemp ="smarthome/temp/value";
    MqttControl mqttControl;
    SPITempADS111 spiTempADS111;
    Gpio Ledpin,ledpin2,ledpin3;
    private static String timeDay;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init_view();
        try {
            init_led();
            DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss a", Locale.US);
            timeDay = dateFormat.format(new Date());
            mqttControl = new MqttControl(this, Ledpin, ledpin2,ledpin3 );
            mqttControl.sendmessage("test","smarthome/led/state");
            spiTempADS111 = new SPITempADS111(this);
            Log.d(TAG,"mqtt done ! ");
        } catch ( IOException | MqttException e ) {
            e.printStackTrace();
        }

    }
    private void init_view(){
        textViewshow = findViewById(R.id.textViewShowtemp);
    }
    private void init_led() throws IOException {
        PeripheralManager manager= PeripheralManager.getInstance();

        Ledpin = manager.openGpio(LED_PIN);
        ledpin2 = manager.openGpio(LED_PIN_2);
        ledpin3 = manager.openGpio(LED_PIN_3);

        Ledpin.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
        ledpin2.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
        ledpin3.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);

        Ledpin.setValue(true);
        ledpin2.setValue(true);
        ledpin3.setValue(true);
        Log.d(TAG,"set led success");
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        spiTempADS111.onDestroyz();
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
            Log.d(TAG,"temp: " + x);
            String data = "LivingRoom_"+timeDay+"_"+x;
            Log.d(TAG,"data: "+data);
            setText(textViewshow,x);
            try {
                mqttControl.sendmessage(data,topicTemp);
            } catch (MqttException e) {
                e.printStackTrace();
            }
    }
}
