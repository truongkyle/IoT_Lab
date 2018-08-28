package com.ritodoji.basicmqttandroidthings;


import android.util.Log;



import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.IOException;

public class MqttControl implements MqttCallback {
    private static final String username = "lffmuuqx";
    private static final String password = "3pDD46wQjsnp";
    private static final String serveruri = "tcp://m14.cloudmqtt.com:11944";
    private static final String clientId = "raspberryz";
    private static final String toPic = "test/command";
    private static final String TAG = MqttControl.class.getSimpleName();
    private static final  int QOs = 1;
    private MqttClient client ;


    public  MqttControl() throws MqttException {
        subcribeToTopic();
    }
    public void subcribeToTopic() throws MqttException {
        client = new MqttClient(serveruri,clientId,new MemoryPersistence());
        client.connect(connectOptionchoice());
        client.subscribe(toPic);
        client.setCallback(this);
        Log.d(TAG,"nice connected ~");

    }

    private static MqttConnectOptions connectOptionchoice(){
        MqttConnectOptions connectOptions = new MqttConnectOptions();
        connectOptions.setUserName(username);
        connectOptions.setPassword(password.toCharArray());
        connectOptions.setCleanSession(false);
        connectOptions.setAutomaticReconnect(true);
        connectOptions.setKeepAliveInterval(30);
        return connectOptions;
    }

    public void close() throws MqttException {
        client.disconnect();
        client.close();
        Log.d(TAG,"client  disconnected ! ");
    }

    public void sendmessage(String data, String topic) throws MqttException {
        if(!client.isConnected()){
            subcribeToTopic();
        }
        MqttMessage message = new MqttMessage(data.getBytes());
        message.setQos(QOs);
        client.publish(topic,message);
    }

    @Override
    public void connectionLost(Throwable cause) {
        try {
            Log.d(TAG,"Reconnecting ~ ................(-______-)#......");
            client.connect(MqttControl.connectOptionchoice());
            Log.d(TAG,"Connected success ! ");

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws IOException, MqttException {
        String payload = new String(message.getPayload());
        Log.d(TAG,"message : " + payload);
    }
    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }

}

