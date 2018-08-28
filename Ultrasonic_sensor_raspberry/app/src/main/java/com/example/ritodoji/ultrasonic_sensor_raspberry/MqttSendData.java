package com.example.ritodoji.ultrasonic_sensor_raspberry;

import android.util.Log;

import com.google.android.things.pio.Gpio;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.IOException;

class MqttSendData  {
    private static final String username = "keiwlcur";
    private static final String password = "ZMj8CepYpK6e";
    private static final String serveruri = "tcp://m14.cloudmqtt.com:11116";
    private static final String clientId = "Control_Distance";
    private static final String toPic = "data";
    private static final String TAG = "MQTT_Ultrasonic";
    private static final  int QOs = 1;
    public MqttClient client ;
    String payload;

    public void subcribeToTopic() throws MqttException {
        client = new MqttClient(serveruri,clientId,new MemoryPersistence());
        client.connect(connectOptionchoice());
        client.subscribe(toPic);
        Log.d(TAG,"nice connected ~");
    }

    public MqttConnectOptions connectOptionchoice(){
        MqttConnectOptions connectOptions = new MqttConnectOptions();
        connectOptions.setUserName(username);
        connectOptions.setPassword(password.toCharArray());
        connectOptions.setCleanSession(true);
        connectOptions.setAutomaticReconnect(true);
        return connectOptions;
    }

    public void close() throws MqttException {
        client.disconnect();
        Log.d(TAG,"client  disconnected ! ");
    }

    public void sendmessage(String data) throws MqttException {
        MqttMessage message = new MqttMessage(data.getBytes());
        message.setQos(QOs);
        client.publish(toPic,message);
    }
    public String getPayload() {
        return payload;
    }
}
