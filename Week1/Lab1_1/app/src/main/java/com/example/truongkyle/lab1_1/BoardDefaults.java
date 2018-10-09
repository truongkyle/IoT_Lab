package com.example.truongkyle.lab1_1;

import android.os.Build;

public class BoardDefaults {
    private  static final String DEVICE_RPI3 = "rpi3";
    public static String getGPIOForLedB(){
        switch (Build.DEVICE) {
            case DEVICE_RPI3:
                return "BCM17";
            default:
                throw new IllegalStateException("Unknown Build.DEVICE " + Build.DEVICE);
        }
    }

    public static String getGPIOForLedR(){
        switch(Build.DEVICE){
            case DEVICE_RPI3:
                return "BCM27";
            default:
                throw new IllegalStateException("Unknown Build.DEVICE " + Build.DEVICE);
        }
    }

    public static String getGPIOForLedG(){
        switch(Build.DEVICE){
            case DEVICE_RPI3:
                return "BCM22";
            default:
                throw new IllegalStateException("Unknown Build.DEVICE " + Build.DEVICE);
        }
    }
}
