package com.example.ritodoji.ledcontrolmqtt;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.things.pio.PeripheralManager;
import com.google.android.things.pio.SpiDevice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class SPITempADS111 {
    private static final String TAG = "SPI_RASPBERRY";
    private SpiDevice mDevice;
    private Timer timer;
    DataTemp dataTemp;
    public interface DataTemp{
         void setText(String x);
    }
    public  SPITempADS111(DataTemp dataTemp){
        this.dataTemp = dataTemp;
        PeripheralManager manager = PeripheralManager.getInstance();
        Log.d(TAG,"List of Devices support SPI, PWM : "+ manager.getSpiBusList()+" "+manager.getPwmList());
        try {
            mDevice = manager.openSpiDevice("SPI0.1");
            Log.d(TAG,"aaa" + mDevice.getName());
            configSPIDevice(mDevice);
            setup_Timer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void configSPIDevice(SpiDevice device) throws IOException {
        device.setMode(SpiDevice.MODE1); // MODE0
        device.setFrequency(1000000); //1Mhz
        device.setBitJustification(SpiDevice.BIT_JUSTIFICATION_MSB_FIRST); // MSB trước
        device.setBitsPerWord(8); // 8 bits
        Log.d(TAG,"SPI OK now ....");
    }
    public void sendCommnad(SpiDevice device, byte[] buffer) throws IOException {

        byte[] response = new byte[4];
        device.transfer(buffer, response, buffer.length);

        for(int i = 0; i< 2; i++) {

            Log.d(TAG, "Response byte " + Integer.toString(i) + " is: " + response[i]);
        }

        int a = Integer.parseInt(String.valueOf(response[0])); // 0 8bits
        int b = Integer.parseInt(String.valueOf(response[1]));
        Log.d(TAG,"bits 0 : = "+ Integer.toBinaryString((a+256)%256)+" bits 1 : = "+ Integer.toBinaryString((b+256)%256));

        double raw_value = (double)((response[0]*256) + response[1]); // 16 bits data
        double raw_dataTempInterval = (double)(((response[0]<<8) + response[1])>>2); // 14 bits
        double adc_value = raw_value * 6.144/32768; //Check in data
        double temperature = adc_value/(10.0 / 1000);
        double temperature1 = raw_dataTempInterval*0.03125;
        //nea.setText(String.valueOf(raw_value));
        //asd.setText(String.valueOf(raw_value));
        //Log.d(TAG,"rawdata: " + raw_value +", adc_volt: " +adc_value +" temp : " + temperature);
        Log.d(TAG,"rawdata: " + raw_dataTempInterval +", adc_volt: " +0 +" temp : " + temperature1);
        dataTemp.setText(String.valueOf(temperature1));
    }
    private void setup_Timer(){

        timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {

                try {
                    byte[] test_data = new byte[4]; //Totally, 4 bytes are sent by the master
                    test_data[3] = (byte)(0x00);
                    test_data[2] = (byte)(0x00);
                    test_data[0] = (byte)(0x70); //ADS111 channel 3 = 70, 2 = 600, 1 = 50, 0 = 40
                    test_data[1] = (byte)(0x9b); //Check in datasheet for this value and note in your report
                    sendCommnad(mDevice, test_data);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.d(TAG,"sent!! ~ ");
            }
        };
        timer.schedule(timerTask,2000, 1000);
    }
   public void onDestroy(){
        if(mDevice!=null){
            try {
                mDevice.close();
                mDevice = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
